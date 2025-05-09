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

package net.seapanda.bunnyhop.bhprogram.common.message;

import java.io.Serializable;
import net.seapanda.bunnyhop.bhprogram.common.BhNodeInstanceId;

/**
 * コールスタックの要素.
 *
 * @param frameId このオブジェクトに対応するスタックフレームの ID
 * @param nodeId この要素に対応する関数呼び出しを行った BhNode を特定するための識別子
 */
public record BhCallStackItem(long frameId, BhNodeInstanceId nodeId) implements Serializable {}
