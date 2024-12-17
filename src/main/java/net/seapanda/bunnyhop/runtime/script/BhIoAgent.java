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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramMessage;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd.InputTextCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd.OutputTextCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp.InputTextResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp.OutputTextResp;
import net.seapanda.bunnyhop.runtime.BhConstants;
import net.seapanda.bunnyhop.runtime.tools.LogManager;
import net.seapanda.bunnyhop.runtime.tools.Util;

/**
 * BhProgram と BunnyHop 間のテキストの入出力に伴うコマンドとレスポンスを発行するクラス.
 *
 * @author K.Koike
 */
public class BhIoAgent {
  /** 送信するメッセージを格納する FIFO. */
  private final BlockingQueue<BhProgramMessage> sendMsgList;
  /** 送信するレスポンスを格納する FIFO. */
  private final BlockingQueue<BhProgramResponse> sendRespList;
  /** BunnyHop との接続状態フラグ. */
  private boolean isTextOutputEnabled = false;
  /** コマンド ID とその ID のコマンドの完了を待つための同期用オブジェクトのマップ. */
  private final Map<Long, CountDownLatch> cmdIdToBarrier = new ConcurrentHashMap<>();
  /** コマンド ID とその ID のコマンドのレスポンスのマップ. */
  private final Map<Long, BhTextIoResp> cmdIdToResp = new ConcurrentHashMap<>();
  private final ReentrantLock lock = new ReentrantLock();
  /** BhProgram に入力された文字列のバッファ. */
  private final BlockingQueue<String> inputTextList =
      new ArrayBlockingQueue<>(BhConstants.MAX_INPUT_TEXT_QUEUE_SIZE);

  /**
   * コンストラクタ.
   *
   * @param sendMsgList 発行したコマンドを格納する FIFO
   * @param sendRespList 発行したレスポンスを格納する FIFO
   * @param enableTextOutput 初期状態で, BunnyHop へのテキストデータの送信を有効化する場合 true
   */
  public BhIoAgent(
        BlockingQueue<BhProgramMessage> sendMsgList,
        BlockingQueue<BhProgramResponse> sendRespList,
        boolean enableTextOutput) {
    this.sendMsgList = sendMsgList;
    this.sendRespList = sendRespList;
    this.isTextOutputEnabled = enableTextOutput;
  }

  /**
   * BunnyHop に文字列データを送信するコマンドを発行する.
   *
   * @param text 送信する文字列
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
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
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**
   * BunnyHop からテキストデータを受信する.
   *
   * @return 受信したテキストデータ
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public String scanln() throws AgencyFailedException {
    try {
      String text = inputTextList.take();
      return text;
    } catch (InterruptedException e) {
      throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
    }
  }

  /** コマンドを送って応答を待つ. */
  private void sendCmdAndWait(BhTextIoCmd cmd, boolean unlock) {
    var isAdded = false;
    var latch = new CountDownLatch(1);
    cmdIdToBarrier.put(cmd.getId(), latch);
    try {
      isAdded = sendMsgList.offer(cmd, Long.MAX_VALUE, TimeUnit.DAYS);
      if (unlock) {
        lock.unlock();
      }
      if (isAdded) {
        latch.await();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      cmdIdToBarrier.remove(cmd.getId());
    }
  }

  /**
   * このオブジェクトにコマンドのレスポンスを渡す.
   *
   * @param resp コマンドのレスポンス.
   */
  public void notify(BhTextIoResp resp) {
    CountDownLatch latch = cmdIdToBarrier.remove(resp.getId());
    if (latch != null) {
      cmdIdToResp.put(resp.getId(), resp);
      latch.countDown();
    }
  }

  /**
   * このオブジェクトにコマンドを渡す.
   *
   * @param cmd このオブジェクトが処理するコマンド.
   */
  public void notify(BhTextIoCmd cmd) {
    try {
      if (cmd instanceof InputTextCmd inputTextCmd) {
        boolean success = inputTextList.offer(inputTextCmd.text);
        sendRespList.put(new InputTextResp(cmd.getId(), success, inputTextCmd.text));
      }
    } catch (InterruptedException e) {
      LogManager.INSTANCE.errMsgForDebug(Util.INSTANCE.getCurrentMethodName() + " failed");
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
    List<BhProgramMessage> outputTextCmdList = sendMsgList.stream()
        .filter(msg -> msg instanceof OutputTextCmd)
        .toList();
    sendMsgList.removeAll(outputTextCmdList);
    outputTextCmdList.forEach(cmd -> notify(
        new OutputTextResp(cmd.getId(), true, ((OutputTextCmd) cmd).text)));
  }
}
