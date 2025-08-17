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

import net.seapanda.bunnyhop.bhprogram.common.BhSymbolId;

/**
 * グローバル変数のリストの値を取得するコマンド.
 *
 * <p>コマンドの詳細.<br><br>
 * グローバル変数一式の中にシンボル ID が {@link #varId} に一致するリストが存在する場合, 
 * そのリストの {@link #startIdx} から {@link #length} 個の要素を取得する.
 *
 * @author K.Koike
 */
public class GetGlobalListValsCmd extends BhDebugCmd {
  
  public final BhSymbolId varId;
  public final long startIdx;
  public final long length;

  /** コンストラクタ. */
  public GetGlobalListValsCmd(BhSymbolId varId, long startIdx, long length) {
    this.varId = varId;
    this.startIdx = startIdx;
    this.length = length;
  }
}
