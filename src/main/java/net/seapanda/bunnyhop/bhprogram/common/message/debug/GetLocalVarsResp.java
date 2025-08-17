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

package net.seapanda.bunnyhop.bhprogram.common.message.debug;

import java.io.Serializable;
import net.seapanda.bunnyhop.bhprogram.common.message.thread.BhVarStackFrame;

/**
 * {@link net.seapanda.bunnyhop.bhprogram.common.message.debug.GetLocalVarsCmd} のレスポンス.
 *
 * @author K.Koike
 */
public class GetLocalVarsResp extends BhDebugResp {
  
  /** コマンドの処理結果.  コマンドの実行に失敗した場合は null. */
  public final Result result;

  /**
   * コンストラクタ.  (コマンドの実行に成功したとき)
   *
   * @param id 実行したコマンドの ID
   * @param result コマンドの処理結果
   */
  public GetLocalVarsResp(long id, Result result) {
    this(id, true, result, null);
  }

  /**
   * コンストラクタ.  (コマンドの実行に失敗したとき)
   *
   * @param id 実行したコマンドの ID
   * @param exception コマンドの実行中に発生した例外
   */
  public GetLocalVarsResp(long id, Exception exception) {
    this(id, false, null, exception);
  }

  /**
   * コンストラクタ.  (コマンドの実行に失敗したとき)
   *
   * @param id 実行したコマンドの ID
   */
  public GetLocalVarsResp(long id) {
    this(id, false, null, null);
  }

  private GetLocalVarsResp(long id, boolean success, Result result, Exception exception) {
    super(id, success, exception);
    this.result = result;
  }

  /**
   * {@link net.seapanda.bunnyhop.bhprogram.common.message.debug.GetLocalVarsCmd} の処理結果.
   *
   * @param threadId コマンドの処理で参照した変数スタックを持つスレッドの ID
   * @param frame コマンドの処理で取得した変数情報を格納したオブジェクト
   */
  public record Result(long threadId, BhVarStackFrame frame) implements Serializable {}
}
