/*
 * Copyright 2017 K.Koike
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.seapanda.bunnyhop.runtime.executor;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramException;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.runtime.script.Keywords;
import net.seapanda.bunnyhop.runtime.script.ScriptHelper;
import net.seapanda.bunnyhop.runtime.service.LogManager;
import net.seapanda.bunnyhop.utility.Utility;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * JavaScript で書かれた BhProgram を実行する機能を提供するクラス.
 *
 *@author K.Koike
 */
public class JsBhProgramExecutor implements BhProgramExecutor {

  private final AtomicBoolean isBhAppInitialized = new AtomicBoolean(false);
  private Script bhAppScript;
  /**  global this オブジェクト. */
  private ScriptableObject bhAppScope;
  private final ExecutorService bhProgramExec = Executors.newFixedThreadPool(16);
  /** BhProgram に公開するヘルパークラス. */
  private final ScriptHelper scriptHelper;
  /** BunnyHop への送信データを格納するキュー. */
  private final BlockingQueue<BhProgramNotification> sendNotifList;

  /**
   * コンストラクタ.
   *
   * @param scriptHelper BhProgram に公開するヘルパークラス.
   * @param sendNotifList 発行した通知を格納する FIFO
   */
  public JsBhProgramExecutor(
        ScriptHelper scriptHelper, BlockingQueue<BhProgramNotification> sendNotifList) {
    this.scriptHelper = scriptHelper;
    this.sendNotifList = sendNotifList;
    Context cx = Context.enter();
    bhAppScope = cx.initStandardObjects();
    Context.exit();
  }

  @Override
  public synchronized boolean runScript(String fileName, BhProgramEvent event) {
    Path scriptPath = Paths.get(fileName).isAbsolute()
        ? Paths.get(fileName) : Paths.get(Utility.execPath, fileName);
    try (BufferedReader reader = Files.newBufferedReader(scriptPath, StandardCharsets.UTF_8)) {
      Context context = Context.enter();
      context.setLanguageVersion(Context.VERSION_ES6);
      context.setOptimizationLevel(9);
      bhAppScript = context.compileReader(reader, scriptPath.getFileName().toString(), 1, null);
      Executors.newSingleThreadExecutor().submit(() -> startBhApp(fileName, event));
    } catch (Exception e) {
      LogManager.logger().error(
          "Failed to run a script.  (%s)\n%s".formatted(fileName, e));
      return false;
    } finally {
      Context.exit();
    }
    return true;
  }


  /** BhProgram を実行する. */
  private void startBhApp(String fileName, BhProgramEvent event) {
    try {
      // 初期化
      Context cx = Context.enter();
      bhAppScope.put(
          Keywords.Properties.BH_SCRIPT_HELPER,
          bhAppScope,
          Context.javaToJS(scriptHelper, bhAppScope));
      bhAppScript.exec(cx, bhAppScope);
      isBhAppInitialized.set(true);
      // 自動実行する関数を実行
      fireEvent(event);
    } catch (Exception e) {
      LogManager.logger().error(
          "Failed to start a script.  (%s)\n%s".formatted(fileName, e));
    } finally {
      Context.exit();
    }
  }

  @Override
  public synchronized void fireEvent(BhProgramEvent event) {
    if (!isBhAppInitialized.get()) {
      return;
    }
    try {
      Context cx = Context.enter();
      Function getEventHandlers = (Function) bhAppScope.get(event.eventHandlerResolver);
      NativeArray funcNameList = (NativeArray) getEventHandlers.call(
          cx, bhAppScope, bhAppScope, new String[] {event.name.toString()});

      for (Object funcName : funcNameList) {
        bhProgramExec.submit(() -> callFunc(funcName.toString()));
      }
    } catch (Exception e) {
      LogManager.logger().error(
          "Failed to fire an event.  (%s)\n%s".formatted(event, e));
    } finally {
      Context.exit();
    }
  }

  /** {@code funcName} で指定した JavaScript の関数を呼ぶ. */
  private Object callFunc(String funcName) {
    ScriptableObject thisObj = null;
    try {
      Context cx = Context.enter();
      thisObj = cx.initStandardObjects();  // funcName.call(thisObj, args...);
      Function func = (Function) bhAppScope.get(funcName);
      return func.call(cx, bhAppScope, thisObj, new Object[0]);
    } catch (Throwable e) {
      sendException(e, thisObj);
      LogManager.logger().error(
          "Failed to call a function.  (%s)\n%s".formatted(funcName, e));
    } finally {
      Context.exit();
    }
    return null;
  }

  /**
   * 例外データを BunnyHop に送信する.
   *
   * @param src 送信する例外データの元となるオブジェクト
   * @param thisObj 例外を起こした関数の this オブジェクト
   */
  private void sendException(Throwable src, ScriptableObject thisObj) {
    BhProgramException throwed = getBhProgramExceptionFrom(src);
    if (throwed != null) {
      BhProgramException exception = new BhProgramException(
          throwed.getCallStack(), throwed.getMessage(), src.toString());
      sendNotifList.add(exception);
      return;
    }

    BhProgramException exception = scriptHelper.factory.newBhProgramException(
        getCallStack(thisObj),
        src.toString() + getErrMsgs(thisObj),
        src.toString());
    sendNotifList.add(exception);
  }

  /**
   * 例外オブジェクトから BhProgramException オブジェクトを抽出する.
   *
   * @param src BhProgramException オブジェクトを抜き出す例外オブジェクト
   * @return BhProgramException オブジェクト. src の中に存在しない場合は null.
   */
  private BhProgramException getBhProgramExceptionFrom(Throwable src) {
    if (src instanceof JavaScriptException jsException) {
      if (jsException.getValue() instanceof NativeJavaObject nativeObj) {
        if (nativeObj.unwrap() instanceof BhProgramException exception) {
          return exception;
        }
      }
    }
    return null;
  }

  /* BhProgram の呼び出しコンテキストからエラーメッセージを抽出する.  */
  private String getErrMsgs(ScriptableObject context) {
    NativeArray msgList = new NativeArray(0);
    if (context != null) {
      Object msgs = context.get(Keywords.Properties.ERROR_MSGS);
      msgList = (msgs instanceof NativeArray) ? (NativeArray) msgs : msgList;
    }

    return ((Collection<?>) msgList).stream()
        .map(obj -> obj.toString())
        .reduce("\n", (lhs, rhs) -> lhs + "\n" + rhs);
  }

  /* BhProgram の呼び出しコンテキストからコールスタックを抽出する.  */
  private List<?> getCallStack(ScriptableObject context) {
    List<Object> callStack = new ArrayList<>();
    if (context != null) {
      if (context.get(Keywords.Properties.CALL_STACK) instanceof NativeArray cs) {
        for (Object nodeInstId : cs) {
          callStack.add(nodeInstId);
        }
      }
      Object currentNodeInstId = ScriptableObject.hasProperty(
          context, Keywords.Properties.CURRENT_NODE_INST_ID);
      if (currentNodeInstId != null) {
        callStack.add(currentNodeInstId);
      }
    }
    return callStack;
  }
}
