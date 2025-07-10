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

import java.util.ArrayList;
import java.util.Collections;
import java.util.SequencedCollection;
import net.seapanda.bunnyhop.bhprogram.common.message.variable.Variable;

/**
 * {@link net.seapanda.bunnyhop.bhprogram.common.message.debug.GetGlobalVarsCmd} のレスポンス.
 *
 * @author K.Koike
 */
public class GetGlobalVarsResp extends BhDebugResp {
  
  /** コマンドの処理で取得した変数情報のリスト. (read-only) */
  public final SequencedCollection<Variable> variables;

  /**
   * コンストラクタ.  (コマンドの実行に成功したとき)
   *
   * @param id 実行したコマンドの ID
   * @param variables コマンドの処理で取得した変数情報のリスト
   */
  public GetGlobalVarsResp(long id, SequencedCollection<Variable> variables) {
    this(id, true, variables, null);
  }

  /**
   * コンストラクタ.  (コマンドの実行に失敗したとき)
   *
   * @param id 実行したコマンドの ID
   * @param exception コマンドの実行中に発生した例外
   */
  public GetGlobalVarsResp(long id, Exception exception) {
    this(id, false, null, exception);
  }

  /**
   * コンストラクタ.  (コマンドの実行に失敗したとき)
   *
   * @param id 実行したコマンドの ID
   */
  public GetGlobalVarsResp(long id) {
    this(id, false, null, null);
  }

  private GetGlobalVarsResp(
      long id, boolean success, SequencedCollection<Variable> variables, Exception exception) {
    super(id, success, exception);
    this.variables = Collections.unmodifiableSequencedCollection(new ArrayList<>(variables));
  }  
}
