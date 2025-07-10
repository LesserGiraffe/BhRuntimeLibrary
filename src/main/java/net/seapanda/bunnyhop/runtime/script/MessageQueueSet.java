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

package net.seapanda.bunnyhop.runtime.script;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;
import net.seapanda.bunnyhop.runtime.BhConstants;

/**
 * BunnyHop と BhRuntimeで送受信されるメッセージを格納するキューのセット.
 *
 * @param sendNotifList BunnyHop に送信するメッセージを格納する FIFO.
 * @param recvNotifList BunnyHop から受信したメッセージを格納する FIFO.
 * @param sendRespList BunnyHop に送信するレスポンスを格納する FIFO.
 * @param recvRespList BunnyHop から受信したレスポンスを格納する FIFO.
 */
public record MessageQueueSet(
    BlockingQueue<BhProgramNotification> sendNotifList,
    BlockingQueue<BhProgramNotification> recvNotifList,
    BlockingQueue<BhProgramResponse> sendRespList,
    BlockingQueue<BhProgramResponse> recvRespList) {

  /** コンストラクタ. */
  public MessageQueueSet() {
    this(
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE),
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE),
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE),
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE));
  }
}
