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

package net.seapanda.bunnyhop.runtime;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.seapanda.bunnyhop.bhprogram.common.BhProgramHandler;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramMessage;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp;
import net.seapanda.bunnyhop.runtime.script.ScriptHelper;

/**
 * スクリプトと BunnyHop 間でデータを送受信するクラス.
 *
 * @author K.Koike
 */
public class BhProgramHandlerImpl implements BhProgramHandler {

  /** BunnyHop に送信するメッセージを格納する FIFO. */
  private final BlockingQueue<BhProgramMessage> sendMsgList = 
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE);
  /** BunnyHop から受信したメッセージを格納する FIFO. */
  private final BlockingQueue<BhProgramMessage> recvMsgList =
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE);
  /** BunnyHop に送信するレスポンスを格納する FIFO. */
  private final BlockingQueue<BhProgramResponse> sendRespList = 
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE);
  /** BunnyHop から受信したレスポンスを格納する FIFO. */
  private final BlockingQueue<BhProgramResponse> recvRespList =
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE);

  /** BunnyHop との通信が有効な場合 true. */
  private boolean connected = false;
  private final ScriptHelper scriptHelper =
      new ScriptHelper(sendMsgList, sendRespList, connected);
  private final BhProgramExecutor executor = new BhProgramExecutor(scriptHelper, sendMsgList);
  /** BunnyHop から受信したメッセージを処理する Executor. */
  private final ExecutorService recvMsgProcessor = Executors.newSingleThreadExecutor();
  /** BunnyHop から受信したメッセージを処理する Executor. */
  private final ExecutorService recvRespProcessor = Executors.newSingleThreadExecutor();

  public BhProgramHandlerImpl() {
    recvMsgProcessor.submit(() -> processRecvMsg());
    recvRespProcessor.submit(() -> processRecvResp());
  }

  @Override
  public boolean runScript(String fileName, BhProgramEvent event) {
    return executor.runScript(fileName, event);
  }

  @Override
  public boolean sendMsgToScript(BhProgramMessage data) {
    boolean success = false;
    try {
      success = recvMsgList.offer(data, BhConstants.PUSH_MSG_TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return success;
  }

  @Override
  public BhProgramResponse recvRespFromScript() {
    BhProgramResponse resp = null;
    try {
      resp = sendRespList.poll(BhConstants.POP_MSG_TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return resp;
  }

  @Override
  public BhProgramMessage recvMsgFromScript() {
    BhProgramMessage msg = null;
    try {
      msg = sendMsgList.poll(BhConstants.POP_MSG_TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return msg;
  }

  @Override
  public boolean sendRespToScript(BhProgramResponse resp) {
    boolean success = false;
    try {
      success = recvRespList.offer(resp, BhConstants.PUSH_MSG_TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return success; 
  }

  @Override
  public void connect() {
    connected = true;
    scriptHelper.io.enableTextOutput();
  }

  @Override
  public void disconnect() {
    connected = false;
    scriptHelper.io.disableTextOutput();
  }

  /** BunnyHop から受信したメッセージを処理し続ける. */
  private void processRecvMsg() {
    while (true) {
      BhProgramMessage msg = null;
      try {
        msg = recvMsgList.take();
      } catch (InterruptedException e) {
        break;
      }
      switch (msg) {
        case BhTextIoCmd textIoCmd -> scriptHelper.io.notify(textIoCmd);
        case BhProgramEvent event -> executor.fireEvent(event);
        default -> { }
      }
    }
  }

  /** BunnyHop から受信したレスポンスを処理し続ける. */
  private void processRecvResp() {
    while (true) {
      BhProgramResponse resp = null;
      try {
        resp = recvRespList.take();
      } catch (InterruptedException e) {
        break;
      }
      switch (resp) {
        case BhTextIoResp textIoResp -> scriptHelper.io.notify(textIoResp);
        case BhSimulatorResp simResp -> scriptHelper.simulator.notify(simResp);
        default -> { }
      }
    }
  }
}
