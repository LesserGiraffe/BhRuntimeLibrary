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

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd.OutputTextCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp.OutputTextResp;
import net.seapanda.bunnyhop.runtime.script.AgencyFailedException;
import net.seapanda.bunnyhop.runtime.script.BhProgramMessageProcessor;
import net.seapanda.bunnyhop.utility.Utility;

/**
 * BhProgram によるテキストの出力機能を提供するクラス.
 *
 * @author K.Koike
 */
public class BhTextOutputAgent implements BhTextOutput, BhProgramMessageProcessor<BhTextIoResp> {

  /** 発行した通知を格納する FIFO. */
  private final BlockingQueue<BhProgramNotification> sendNotifList;
  /** BunnyHop へのテキストデータの送信が有効な場合 true. */
  private boolean isTextOutputEnabled = false;
  /** コマンド ID とその ID のコマンドの完了を待つための同期用オブジェクトのマップ. */
  private final Map<Long, CountDownLatch> cmdIdToBarrier = new ConcurrentHashMap<>();
  /** コマンド ID とその ID のコマンドのレスポンスのマップ. */
  private final Map<Long, BhTextIoResp> cmdIdToResp = new ConcurrentHashMap<>();
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * コンストラクタ.
   *
   * @param sendNotifList 発行した通知を格納する FIFO
   * @param enableTextOutput 初期状態で, BunnyHop へのテキストデータの送信を有効化する場合 true
   */
  public BhTextOutputAgent(
      BlockingQueue<BhProgramNotification> sendNotifList,
      boolean enableTextOutput) {
    this.sendNotifList = sendNotifList;
    this.isTextOutputEnabled = enableTextOutput;
  }
  
  @Override
  public void println(String text) throws AgencyFailedException {
    lock.lock();
    if (!isTextOutputEnabled) {
      lock.unlock();
      return;
    }
    var cmd = new OutputTextCmd(text + "\n");
    sendCmdAndWait(cmd, true);
    BhTextIoResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof OutputTextResp && resp.success) {
      return;
    }
    throw new AgencyFailedException(Utility.getCurrentMethodName() + " failed");
  }


  /** コマンドを送って応答を待つ. */
  private void sendCmdAndWait(BhTextIoCmd cmd, boolean unlock) {
    var isAdded = false;
    var latch = new CountDownLatch(1);
    cmdIdToBarrier.put(cmd.getId(), latch);
    try {
      isAdded = sendNotifList.offer(cmd, Long.MAX_VALUE, TimeUnit.DAYS);
      if (unlock) {
        lock.unlock();
      }
      if (isAdded) {
        latch.await();
      }
    } catch (InterruptedException e) {
      cmdIdToBarrier.remove(cmd.getId());
    }
  }

  @Override
  public void process(BhTextIoResp resp) {
    CountDownLatch latch = cmdIdToBarrier.remove(resp.getId());
    if (latch != null) {
      cmdIdToResp.put(resp.getId(), resp);
      latch.countDown();
    }
  }

  /**
   * BunnyHop へのテキストデータの送信を無効化する.
   * 既にキューイングされているテキスト出力コマンドはキャンセルされる. 
   */
  public void disableTextOutput() {
    lock.lock();
    isTextOutputEnabled = false;
    cancelTextOutput();
    lock.unlock();
  }

  /** BunnyHop へのテキストデータの送信を有効化する. */
  public void enableTextOutput() {
    lock.lock();
    isTextOutputEnabled = true;
    lock.unlock();
  }

  /** BunnyHop にテキストデータを送信するコマンドをキャンセルする. */
  private void cancelTextOutput() {
    List<BhProgramNotification> outputTextCmdList = sendNotifList.stream()
        .filter(msg -> msg instanceof OutputTextCmd)
        .toList();
    sendNotifList.removeAll(outputTextCmdList);
    outputTextCmdList.forEach(cmd -> process(
        new OutputTextResp(cmd.getId(), true, ((OutputTextCmd) cmd).text)));
  }
}
