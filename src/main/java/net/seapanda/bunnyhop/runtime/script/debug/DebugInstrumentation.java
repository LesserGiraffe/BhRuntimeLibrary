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

package net.seapanda.bunnyhop.runtime.script.debug;

import java.util.List;
import net.seapanda.bunnyhop.runtime.script.ScriptThreadContext;
import org.mozilla.javascript.Function;

/**
 * デバッグのために BhProgram に追加するコードで使用する機能を規定したインタフェース.
 *
 * @author K.Koike
 */
public interface DebugInstrumentation {

  /**
   * スレッドの開始をデバッガに通知する.
   *
   * @param context このメソッド読んだスレッドに関連する情報を格納したオブジェクト
   */
  void notifyThreadStart(ScriptThreadContext context);

  /**
   * スレッドの終了をデバッガに通知する.
   *
   * <p>スレッドの処理が正常に終了した場合にこのメソッドを呼ぶこと
   */
  void notifyThreadEnd();

  /**
   * スレッドの終了をデバッガに通知する.
   *
   * <p>スレッドの処理を実行が例外により中断された場合にこのメソッドを呼ぶこと.
   *
   * @param exception 発生した例外
   */
  void notifyThreadEnd(Throwable exception);

  /**
   * このメソッドを呼び出したスレッドのスレッドコンテキストを取得する.
   *
   * @return このメソッドを呼び出したスレッドのスレッドコンテキスト.  見つからない場合は null.
   */
  ScriptThreadContext getThreadContext();

  /**
   * このメソッドを呼び出したスレッドが一時停止の条件を満たしている場合, 一時停止する.
   *
   * @param stepId このメソッドを呼び出すスレッドが, 次に実行する処理の ID (nullable)
   */
  void conditionalWait(String stepId);

  /** BhProgram のデータを文字列に変換するためのメソッドをデバッガに登録する. */
  void setStringGenerator(Function fn);
 
  /** グローバル変数の一覧をデバッガに登録する. */
  void setGlobalVariables(List<?> vars);
}
