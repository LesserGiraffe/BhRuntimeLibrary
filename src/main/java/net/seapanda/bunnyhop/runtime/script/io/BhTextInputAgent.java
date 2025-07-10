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

package net.seapanda.bunnyhop.runtime.script.io;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;
import net.seapanda.bunnyhop.bhprogram.common.message.io.BhTextIoCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.io.InputTextCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.io.InputTextResp;
import net.seapanda.bunnyhop.runtime.BhConstants;
import net.seapanda.bunnyhop.runtime.script.AgencyFailedException;
import net.seapanda.bunnyhop.runtime.script.BhProgramMessageProcessor;
import net.seapanda.bunnyhop.runtime.service.LogManager;
import net.seapanda.bunnyhop.utility.Utility;

/**
 * BhProgram へのテキストの入力機能を提供するクラス.
 *
 * @author K.Koike
 */
public class BhTextInputAgent implements BhTextInput, BhProgramMessageProcessor<BhTextIoCmd> {

  /** 発行したレスポンスを格納する FIFO. */
  private final BlockingQueue<BhProgramResponse> sendRespList;
  /** BhProgram に入力された文字列のバッファ. */
  private final BlockingQueue<String> inputTextList =
      new ArrayBlockingQueue<>(BhConstants.MAX_INPUT_TEXT_QUEUE_SIZE);

  /**
   * コンストラクタ.
   *
   * @param sendRespList 発行したレスポンスを格納する FIFO
   */
  public BhTextInputAgent(BlockingQueue<BhProgramResponse> sendRespList) {
    this.sendRespList = sendRespList;
  }

  @Override
  public String scanln() throws AgencyFailedException {
    try {
      String text = inputTextList.take();
      return text;
    } catch (InterruptedException e) {
      throw new AgencyFailedException(Utility.getCurrentMethodName() + " failed");
    }
  }

  @Override
  public void process(BhTextIoCmd cmd) {
    try {
      if (cmd instanceof InputTextCmd inputTextCmd) {
        boolean success = inputTextList.offer(inputTextCmd.text);
        sendRespList.put(new InputTextResp(cmd.getId(), success, inputTextCmd.text));
      }
    } catch (InterruptedException e) {
      LogManager.logger().error(e.toString());
    }
  }
}
