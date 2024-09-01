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

package net.seapanda.bunnyhop.programexecenv;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.seapanda.bunnyhop.bhprogram.common.BhProgramData;
import net.seapanda.bunnyhop.bhprogram.common.BhProgramHandler;
import net.seapanda.bunnyhop.programexecenv.script.RemoteScriptInOut;

/**
 * スクリプトと BunnyHop 間でデータを送受信するクラス.
 *
 * @author K.Koike
 */
public class BhProgramHandlerImpl implements BhProgramHandler {

  private final ExecutorService recvDataProcessor = Executors.newSingleThreadExecutor();
  /** BunnyHop に送信するデータを格納する. */
  private final BlockingQueue<BhProgramData> sendDataList =
      new ArrayBlockingQueue<>(BhParams.MAX_QUEUE_SIZE);
  /** BunnyHop から受信したデータを格納する. */
  private final BlockingQueue<BhProgramData> recvDataList =
      new ArrayBlockingQueue<>(BhParams.MAX_QUEUE_SIZE);
  /** BunnyHop との通信が有効な場合 true. */
  private final AtomicBoolean connected = new AtomicBoolean(false);
  /** BhProgramの 入出力用オブジェクト. */
  private final RemoteScriptInOut scriptIo = new RemoteScriptInOut(sendDataList, connected);
  private final BhProgramExecutor executor = new BhProgramExecutor(scriptIo, sendDataList);

  public BhProgramHandlerImpl() {
    recvDataProcessor.submit(() -> processRecvData());
  }

  @Override
  public boolean runScript(String fileName, BhProgramData data) {
    return executor.runScript(fileName, data);
  }

  @Override
  public boolean sendDataToScript(BhProgramData data) {
    boolean success = false;
    try {
      success = recvDataList.offer(data, BhParams.PUSH_RECV_DATA_TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return success;
  }

  @Override
  public BhProgramData recvDataFromScript() {
    BhProgramData data = null;
    try {
      data = sendDataList.poll(BhParams.POP_SEND_DATA_TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return data;
  }

  @Override
  public void connect() {
    connected.set(true);
  }

  @Override
  public void disconnect() {
    connected.set(false);
    sendDataList.clear();
  }

  /** BunnyHop から受信したデータを処理し続ける. */
  private void processRecvData() {
    while (true) {
      BhProgramData data = null;
      try {
        data = recvDataList.take();
      } catch (InterruptedException e) {
        break;
      }
      switch (data.type) {
        case INPUT_STR:
          scriptIo.putLineToStdin(data.str);
          break;

        case INPUT_EVENT:
          executor.fireEvent(data);
          break;

        default:
      }
    }
  }
}
