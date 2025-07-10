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

import net.seapanda.bunnyhop.bhprogram.common.message.variable.ListVariable;

/**
 * {@link net.seapanda.bunnyhop.bhprogram.common.message.debug.GetGlobalListValsCmd} のレスポンス.
 *
 * @author K.Koike
 */
public class GetGlobalListValsResp extends BhDebugResp {
  
  /** リストの値を格納したオブジェクト. */
  public final ListVariable variable;
  
  /**
   * コンストラクタ.  (コマンドの実行に成功したとき)
   *
   * @param id 実行したコマンドの ID
   * @param variable リストの値を格納したオブジェクト
   */
  public GetGlobalListValsResp(long id, ListVariable variable) {
    this(id, true, variable, null);
  }

  /**
   * コンストラクタ.  (コマンドの実行に失敗したとき)
   *
   * @param id 実行したコマンドの ID
   * @param exception コマンドの実行中に発生した例外
   */
  public GetGlobalListValsResp(long id, Exception exception) {
    this(id, false, null, exception);
  }

  /**
   * コンストラクタ.  (コマンドの実行に失敗したとき)
   *
   * @param id 実行したコマンドの ID
   */
  public GetGlobalListValsResp(long id) {
    this(id, false, null, null);
  }

  private GetGlobalListValsResp(
      long id, boolean success, ListVariable variable, Exception exception) {
    super(id, success, exception);
    this.variable = variable;
  }
}
