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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.runtime.script.Keywords;
import net.seapanda.bunnyhop.runtime.script.ScriptHelper;
import net.seapanda.bunnyhop.runtime.service.LogManager;
import net.seapanda.bunnyhop.utility.Utility;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * JavaScript で書かれた BhProgram を実行する機能を提供するクラス.
 *
 * @author K.Koike
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
      bhAppScript = context.compileReader(reader, scriptPath.getFileName().toString(), 1, null);
      Executors.newSingleThreadExecutor().submit(() -> startBhApp(fileName, event));
    } catch (Exception e) {
      LogManager.logger().error("Failed to run a script.  (%s)\n%s".formatted(fileName, e));
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
      executeScript(bhAppScript, cx, bhAppScope);
      isBhAppInitialized.set(true);
      // 自動実行する関数を実行
      fireEvent(event);
    } catch (Exception e) {
      LogManager.logger().error("Failed to start a script.  (%s)\n%s".formatted(fileName, e));
    } finally {
      Context.exit();
    }
  }

  /** {@code cx} と {@code scope} を使って, {@code script} を実行する. */
  private void executeScript(Script script, Context cx, ScriptableObject scope) {
    try {
      script.exec(cx, scope);
    } catch (Throwable e) {
      notifyThreadEnd(cx, scope, e);
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
      var funcNameList = (NativeArray) getEventHandlers.call(
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
    Context cx = Context.enter();
    ScriptableObject thisObj = cx.initStandardObjects(); // funcName.call(thisObj, args...);
    try {
      Function func = (Function) bhAppScope.get(funcName);
      return func.call(cx, bhAppScope, thisObj, new Object[0]);
    } catch (Throwable e) {
      // 本来, この処理はスクリプトの中で呼びたいが, 
      // Rhino の初期設定では catch 節で {@link Throwable} をキャッチできないので,
      // Java 側でキャッチしてから JavaScript のメソッドを呼び出すことで対処する.
      notifyThreadEnd(cx, thisObj, e);
      LogManager.logger().error("Failed to call a function.  (%s)\n%s".formatted(funcName, e));
    } finally {
      Context.exit();
    }
    return null;
  }

  /** スレッドが例外で終了したときの処理を呼ぶ. */
  private void notifyThreadEnd(Context cx, ScriptableObject thisObj, Throwable exception) {
    String funcName = Keywords.Funcs.NOTIFY_THREAD_END;
    try {
      Function func = (Function) bhAppScope.get(funcName);
      Object[] args = new Object[] {Context.javaToJS(exception, thisObj)};
      func.call(cx, bhAppScope, thisObj, args);
    } catch (Throwable e) {
      LogManager.logger().error("Failed to call a function.  (%s)\n%s".formatted(funcName, e));
    }
  }
}
