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

package net.seapanda.bunnyhop.runtime.script;

import java.util.List;
import net.seapanda.bunnyhop.bhprogram.common.message.exception.BhProgramException;
import net.seapanda.bunnyhop.utility.concurrent.SynchronizingTimer;

/**
 * BhProgram が使用するオブジェクトを生成する機能を提供するクラス.
 *
 * @author K.Koike
 */
public class Factory {

  /**
   * {@link BhProgramException} オブジェクトを作成して返す.
   *
   * @param msg 例外メッセージ
   * @return 例外オブジェクト
   */
  public BhProgramException newBhProgramException(String msg) {
    return new BhProgramException(msg);
  }

  /**
   * {@link BhProgramException} オブジェクトを作成して返す.
   *
   * @param msg 例外メッセージ
   * @param cause 例外の原因.  BhScript から引数を渡すために {@link Object} 型にしておく.
   * @return 例外オブジェクト
   */
  public BhProgramException newBhProgramException(String msg, Object cause) {
    if (cause instanceof Throwable e) {
      return new BhProgramException(msg, e);
    }
    return new BhProgramException(msg, new Exception(cause.toString()));
  }

  /** {@link SynchronizingTimer} を新規作成する. */
  public SynchronizingTimer newSyncTimer(int count, boolean autoReset) {
    return new SynchronizingTimer(count, autoReset);
  }

  /**
   * {@link ScriptThreadContext} オブジェクトを作成する.
   *
   * @param context BhProgram が操作するスレッドコンテキストオブジェクト
   * @param idxCallStack コールスタックが格納された {@code context} のインデックス
   * @param idxNextNodeInstId 次に実行される処理の ID が格納された {@code context} のインデックス
   * @param idxErrorMsgs エラーメッセージが格納された {@code context} のインデックス
   * @param idxVarStack 変数スタックが格納された {@code context} のインデックス
   * @return {@link ScriptThreadContext} オブジェクト
   */
  public ScriptThreadContext newScriptThreadContext(
      List<?> context, 
      int idxCallStack,
      int idxNextNodeInstId,
      int idxErrorMsgs,
      int idxVarStack) {
    return new ScriptThreadContext(
        Thread.currentThread().threadId(),
        context,
        idxCallStack,
        idxNextNodeInstId,
        idxErrorMsgs,
        idxVarStack);
  }
}
