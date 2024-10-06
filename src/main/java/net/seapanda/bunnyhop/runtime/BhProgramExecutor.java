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

package net.seapanda.bunnyhop.runtime;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramException;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramMessage;
import net.seapanda.bunnyhop.runtime.script.ScriptHelper;
import net.seapanda.bunnyhop.runtime.script.ScriptParams;
import net.seapanda.bunnyhop.runtime.tools.LogManager;
import net.seapanda.bunnyhop.runtime.tools.Util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * BhProgram を実行するクラス.
 *
 *@author K.Koike
 */
public class BhProgramExecutor {

  private final AtomicBoolean isBhAppInitialized = new AtomicBoolean(false);
  private Script bhAppScript;
  /**  global this オブジェクト. */
  private ScriptableObject bhAppScope;
  private final ExecutorService bhProgramExec = Executors.newFixedThreadPool(16);
  /** BhProgram に公開するヘルパークラス. */
  private final ScriptHelper scriptHelper;
  /** BunnyHop への送信データを格納するキュー. */
  private final BlockingQueue<BhProgramMessage> sendMsgList;

  /**
   * コンストラクタ.
   *
   * @param scriptHelper BhProgram に公開するヘルパークラス.
   * @param sendMsgList BunnyHopへの送信データキュー
   */
  public BhProgramExecutor(
        ScriptHelper scriptHelper, BlockingQueue<BhProgramMessage> sendMsgList) {
    this.scriptHelper = scriptHelper;
    this.sendMsgList = sendMsgList;
    Context cx = ContextFactory.getGlobal().enterContext();
    bhAppScope = cx.initStandardObjects();
    Context.exit();
  }

  /**
   * {@code fileName} で指定したスクリプトを実行する.
   *
   * @param fileName 実行するスクリプトファイル名
   * @param event 実行時にスクリプトに渡すイベントデータ
   */
  public boolean runScript(String fileName, BhProgramEvent event) {
    Path scriptPath = Paths.get(Util.INSTANCE.execPath, BhParams.Path.SCRIPT_DIR, fileName);
    try (BufferedReader reader = Files.newBufferedReader(scriptPath, StandardCharsets.UTF_8)) {
      Context context = ContextFactory.getGlobal().enterContext();
      context.setLanguageVersion(Context.VERSION_ES6);
      context.setOptimizationLevel(9);
      bhAppScript = context.compileReader(reader, scriptPath.getFileName().toString(), 1, null);
      Executors.newSingleThreadExecutor().submit(() -> startBhApp(fileName, event));
    } catch (Exception e) {
      LogManager.INSTANCE.errMsgForDebug("runScript 2 " +  e.toString() + " " + fileName);
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
      Context cx = ContextFactory.getGlobal().enterContext();
      ScriptableObject.putProperty(
          bhAppScope, ScriptParams.Properties.BH_SCRIPT_HELPER, scriptHelper);
      bhAppScript.exec(cx, bhAppScope);
      isBhAppInitialized.set(true);
      // 自動実行する関数を実行
      fireEvent(event);
    } catch (Exception e) {
      LogManager.INSTANCE.errMsgForDebug("runScript 1 " +  e.toString() + " " + fileName);
    } finally {
      Context.exit();
    }
  }

  /**
   * BhProgram のイベントハンドラを呼び出す.
   *
   * @param event このイベントに関連するイベントハンドラを呼び出す.
   */
  public synchronized void fireEvent(BhProgramEvent event) {
    if (!isBhAppInitialized.get()) {
      return;
    }
    try {
      Context cx = ContextFactory.getGlobal().enterContext();
      Function getEventHandlers = (Function) bhAppScope.get(event.eventHandlerResolver);
      NativeArray funcNameList = (NativeArray) getEventHandlers.call(
          cx, bhAppScope, bhAppScope, new String[] {event.name.toString()});

      for (Object funcName : funcNameList) {
        callAsync((String) funcName, bhAppScope);
      }
    } catch (Exception e) {
      LogManager.INSTANCE.errMsgForDebug(
          BhProgramHandlerImpl.class.getSimpleName() + "::fireEvent\n" + e);
    } finally {
      Context.exit();
    }
  }

  /** 非同期的に Javascript の関数を呼ぶ. */
  private void callAsync(String funcName, ScriptableObject scope) {
    bhProgramExec.submit(() -> {
      ScriptableObject thisObj = null;
      try {
        Context cx = ContextFactory.getGlobal().enterContext();
        thisObj = cx.initStandardObjects();  // funcName.call(thisObj, args...);
        Function func = (Function) bhAppScope.get(funcName);
        func.call(cx, bhAppScope, thisObj, new Object[0]);
      } catch (Throwable e) {
        sendException(e, thisObj);
        LogManager.INSTANCE.errMsgForDebug(
            BhProgramHandlerImpl.class.getSimpleName() + "::callAsync(" + funcName + ")\n" + e);
      } finally {
        Context.exit();
      }
    });
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
      sendMsgList.add(exception);
      return;
    }

    BhProgramException exception = scriptHelper.util.newBhProgramException(
        getCallStack(thisObj),
        src.toString() + getErrMsgs(thisObj),
        src.toString());
    sendMsgList.add(exception);
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
    if (context != null
        && ScriptableObject.hasProperty(context, ScriptParams.Properties.ADDITIONAL_ERROR_MSGS)) {
      Object tmp = context.get(ScriptParams.Properties.ADDITIONAL_ERROR_MSGS);
      msgList = (tmp instanceof NativeArray) ? (NativeArray) tmp : msgList;
    }

    return ((Collection<?>) msgList).stream()
        .map(obj -> obj.toString())
        .reduce("\n", (lhs, rhs) -> lhs + "\n" + rhs);
  }

  /* BhProgram の呼び出しコンテキストからコールスタックを抽出する.  */
  private NativeArray getCallStack(ScriptableObject context) {
    NativeArray callStack = new NativeArray(0);
    if (context != null
        && ScriptableObject.hasProperty(context, ScriptParams.Properties.CALL_STACK)) {
      Object tmp = context.get(ScriptParams.Properties.CALL_STACK);
      callStack = (tmp instanceof NativeArray) ? (NativeArray) tmp : callStack;
    }
    return callStack;
  }
}
