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
 * スレッドの一時停止条件を有効化するコマンド.
 *
 * @author K.Koike
 */
public class EnableThreadWaitConditionCmd extends BhDebugCmd {

  /** {@link #threadId} がこの値である場合, 全てのスレッドが対象であることを表す. */
  public static long ALL_THREADS = -1;

  /** 一時停止条件を有効化するスレッドの ID. */
  public final long threadId;

  /**
   * コンストラクタ.
   *
   * @param threadId 一時停止条件を有効化するスレッドの ID
   */
  public EnableThreadWaitConditionCmd(long threadId) {
    this.threadId = threadId;
  }

  /**
   * コンストラクタ.
   *
   * <p>動作中の全てのスレッドの一時停止条件を有効化するコマンドを作成する.
   */
  public EnableThreadWaitConditionCmd() {
    this.threadId = ALL_THREADS;
  }
}
