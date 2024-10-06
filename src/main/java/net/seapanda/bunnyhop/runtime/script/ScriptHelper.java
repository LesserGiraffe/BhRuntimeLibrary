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

import java.util.concurrent.BlockingQueue;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramMessage;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;

/**
 * BhProgram に公開するヘルパークラス.
 *
 * @author K.Koike
 */
public class ScriptHelper {

  public final BhIoAgent io;
  public final BhSimulatorAgent simulator;
  public final ScriptUtil util;

  /**
   * コンストラクタ.
   *
   * @param sendMsgList BunnyHop へ送信するメッセージを格納する FIFO
   * @param sendRespList BunnyHop へ送信するレスポンスを格納する FIFO
   * @param enableTextOutput 初期状態で, BunnyHop へのテキストデータの送信を有効化する場合 true
   */
  public ScriptHelper(
      BlockingQueue<BhProgramMessage> sendMsgList,
      BlockingQueue<BhProgramResponse> sendRespList,
      boolean enableTextOutput) {
    io = new BhIoAgent(sendMsgList, sendRespList, enableTextOutput);
    simulator = new BhSimulatorAgent(sendMsgList);
    util = new ScriptUtil();
  }
}
