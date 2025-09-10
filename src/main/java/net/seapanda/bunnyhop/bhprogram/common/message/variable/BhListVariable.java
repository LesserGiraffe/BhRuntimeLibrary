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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.seapanda.bunnyhop.bhprogram.common.BhSymbolId;

/**
 * リスト変数の情報を格納するクラス.
 *
 * @author K.Koike
 */
public class BhListVariable extends BhVariable {
  
  /** リストの長さ. */
  public final int length;
  /** サブリストの値を格納したオブジェクトの配列. (read-only) */
  public final Collection<Slice> slices;

  /** コンストラクタ. */
  public BhListVariable(BhSymbolId id, int length) {
    super(id);
    this.length = length;
    slices = Collections.unmodifiableCollection(new ArrayList<>());
  }

  /** コンストラクタ. */
  public BhListVariable(BhSymbolId id, int length, Collection<Slice> slices) {
    super(id);
    this.length = length;
    this.slices = Collections.unmodifiableCollection(new ArrayList<>(slices));
  }

  /**
   * サブリストの値を格納するレコード.
   *
   * @param startIdx リスト内における {@code vals} 範囲の先頭のインデックス
   * @param vals リストの値を格納した配列 (read-only)
   */
  public record Slice(long startIdx, List<String> vals) implements Serializable {

    public Slice(long startIdx, List<String> vals) {
      this.startIdx = startIdx;
      this.vals = Collections.unmodifiableList(new ArrayList<>(vals));
    }
  }
}
