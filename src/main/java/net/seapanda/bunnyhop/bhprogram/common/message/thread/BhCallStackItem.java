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
import net.seapanda.bunnyhop.bhprogram.common.BhSymbolId;

/**
 * コールスタックのシンボルを格納するレコード.
 * 
 * <p>コールスタック : 各関数呼び出しに対応するシンボルの ID を格納するスタック
 *
 * @param frameIdx {@code symbolId} のコールスタック内におけるインデックス
 * @param symbolId コールスタックの {@code idx} 番目に格納された ID
 */
public record BhCallStackItem(int frameIdx, BhSymbolId symbolId) implements Serializable {}
