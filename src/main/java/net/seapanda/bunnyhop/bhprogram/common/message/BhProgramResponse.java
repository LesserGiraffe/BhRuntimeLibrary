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


/**
 * BunnyHop と BhRuntime 間で送受信される応答データ.
 * 特定の {@link BhProgramNotification} を受け取ったプログラムが, このオブジェクトを応答として返す.
 *
 * @author K.Koike
 */
public interface BhProgramResponse extends BhProgramMessage {

  /** この応答の元となった通知の処理に成功した場合 true を返す. */
  boolean isSuccessful();
}
