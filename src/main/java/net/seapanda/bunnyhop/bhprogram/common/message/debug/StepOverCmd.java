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

/**
 * {@link #threadId} で指定したスレッドが一時停止中であった場合, 次に停止可能な位置まで処理を進めるコマンド.
 *
 * <p>次の処理が関数呼び出しであった場合, その中では止まらず, 関数呼び出し終了後の次に停止可能な位置で止まる.
 *
 * @author K.Koike
 */
public class StepOverCmd extends BhDebugCmd {
  
  public final long threadId;

  /**
   * コンストラクタ.
   *
   * @param threadId 処理を進めるスレッドの ID
   */
  public StepOverCmd(long threadId) {
    this.threadId = threadId;
  }
}
