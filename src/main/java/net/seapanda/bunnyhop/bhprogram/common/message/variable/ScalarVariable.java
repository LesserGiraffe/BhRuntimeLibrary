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

package net.seapanda.bunnyhop.bhprogram.common.message.variable;

import net.seapanda.bunnyhop.bhprogram.common.BhSymbolId;

/**
 * スカラ変数の情報を格納するクラス.
 *
 * @author K.Koike
 */
public class ScalarVariable extends Variable {

  /** 変数の値. */
  public final String val;

  public ScalarVariable(BhSymbolId varId, String val) {
    super(varId);
    this.val = val;
  }
}
