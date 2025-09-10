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
import net.seapanda.bunnyhop.bhprogram.common.message.variable.BhListVariable;

/**
 * {@link net.seapanda.bunnyhop.bhprogram.common.message.debug.GetLocalListValsCmd} のレスポンス.
 *
 * @author K.Koike
 */
public class GetLocalListValsResp extends BhDebugResp {
  
  /** コマンドの処理結果. */
  public final Result result;

  /**
   * コンストラクタ.  (コマンドの実行に成功したとき)
   *
   * @param id 実行したコマンドの ID
   * @param result コマンドの処理結果
   */
  public GetLocalListValsResp(long id, Result result) {
    this(id, true, result, null);
  }

  /**
   * コンストラクタ.  (コマンドの実行に失敗したとき)
   *
   * @param id 実行したコマンドの ID
   * @param exception コマンドの実行中に発生した例外
   */
  public GetLocalListValsResp(long id, Exception exception) {
    this(id, false, null, exception);
  }

  /**
   * コンストラクタ.  (コマンドの実行に失敗したとき)
   *
   * @param id 実行したコマンドの ID
   */
  public GetLocalListValsResp(long id) {
    this(id, false, null, null);
  }

  private GetLocalListValsResp(long id, boolean success, Result result, Exception exception) {
    super(id, success, exception);
    this.result = result;
  }

  /**
   * {@link net.seapanda.bunnyhop.bhprogram.common.message.debug.GetLocalListValsCmd} の処理結果.
   *
   * @param threadId コマンドの処理で参照した変数スタックを持つスレッドの ID
   * @param frameIdx コマンドの処理で参照した変数スタックのスタックフレームのインデックス
   * @param variable リストの値を格納したオブジェクト
   */
  public record Result(
      long threadId, int frameIdx, BhListVariable variable) implements Serializable {}
}
