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

package net.seapanda.bunnyhop.bhprogram.common.message.thread;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.SequencedCollection;
import net.seapanda.bunnyhop.bhprogram.common.message.variable.BhVariable;

/**
 * 変数スタック内の特定のスタックフレームに対応するローカル変数の情報を格納するレコード.
 *
 * <p>変数スタック : ローカス変数の情報を格納するスタック.
 *
 * @param idx スタックフレームのインデックス
 * @param variables 変数スタックに格納された変数の情報一覧 (read-only)
 */
public record BhVarStackFrame(int idx, SequencedCollection<BhVariable> variables)
    implements Serializable {

  public BhVarStackFrame(int idx, SequencedCollection<BhVariable> variables) {
    this.idx = idx;
    this.variables = Collections.unmodifiableList(new ArrayList<>(variables));
  }
}
