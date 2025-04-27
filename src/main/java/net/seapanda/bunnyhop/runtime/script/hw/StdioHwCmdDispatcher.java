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

package net.seapanda.bunnyhop.runtime.script.hw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import net.seapanda.bunnyhop.runtime.BhConstants;
import net.seapanda.bunnyhop.runtime.script.AgencyFailedException;
import net.seapanda.bunnyhop.runtime.service.LogManager;

/**
 * HW を制御するプログラムに対して, 標準入出力を用いてコマンドの送信とレスポンスの受信を行うクラス.
 *
 * @author K.Koike
 */
public class StdioHwCmdDispatcher implements HwCmdDispatcher {
  
  /** コマンドおよびレスポンス内で使用される区切り文字. */
  private static final String delimiter = ",";

  /** HW を制御するプログラムの {@link Process} オブジェクト. */
  private Process process;
  /** HW を制御するプログラムの標準出力を {@link BufferedReader} でラップしたオブジェクト. */
  private final BufferedReader stdout;
  /** HW を制御するプログラムからコマンドの応答を取得する Executor. */
  private final ExecutorService respReader = Executors.newSingleThreadExecutor();
  /** HW を制御するプログラムに送信するコマンドの ID. */
  private AtomicLong commandId = new AtomicLong();
  /** コマンド ID とその ID のコマンドの完了を待つための同期用オブジェクトのマップ. */
  private final Map<Long, CountDownLatch> cmdIdToBarrier = new ConcurrentHashMap<>();
  /** コマンド ID とその ID のコマンドのレスポンスのマップ. */
  private final Map<Long, List<String>> cmdIdToResp = new ConcurrentHashMap<>();
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * コンストラクタ.
   *
   * @param command HW を制御するプログラムを実行するコマンド
   * @throws AgencyFailedException HW を制御するプログラムの実行に失敗した
   */
  public StdioHwCmdDispatcher(String... command) throws AgencyFailedException {
    ProcessBuilder procBuilder = new ProcessBuilder(command);
    try {
      process = procBuilder.start();
      stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
      respReader.submit(this::readResponse);
    } catch (IOException e) {
      throw new AgencyFailedException(e.toString());
    }
  }

  /** HW を制御するプログラムからコマンドのレスポンスを読み続ける. */
  private void readResponse() {
    while (true) {
      String respStr = "";
      try {
        respStr = stdout.readLine();
        List<String> resp = Arrays.asList(respStr.split(delimiter));
        long respId = Long.valueOf(resp.removeFirst());
        CountDownLatch latch = cmdIdToBarrier.remove(respId);
        if (latch != null) {
          cmdIdToResp.put(respId, resp);
          latch.countDown();
        }
      } catch (IOException e) {
        break;
      } catch (Exception e) {
        LogManager.logger().error(
            "Received an invalid HW ctrl response.  (%s)\n%s".formatted(respStr, e));
      }
    }
  }

  @Override
  public List<String> sendCmd(String... cmd) throws AgencyFailedException {    
    long cmdId = commandId.getAndIncrement();
    var latch = new CountDownLatch(1);
    cmdIdToBarrier.put(cmdId, latch);
    try {
      sendCmd(cmdId, cmd);
      return waitForResp(cmdId, latch);
    } catch (AgencyFailedException e) {
      cmdIdToBarrier.remove(cmdId);
      throw e;
    }
  }

  /** HW を制御するプログラムにコマンドを送信する. */
  private void sendCmd(long cmdId, String... cmd) throws AgencyFailedException {
    boolean unlocked = false;
    try {
      lock.lock();
      if (process == null) {
        throw new AgencyFailedException("HW Ctrl Program has ended.");
      }
      process.getOutputStream().write(createCmd(cmdId, cmd));
      lock.unlock();
      unlocked = true;
    } catch (IOException e) {
      throw new AgencyFailedException("Failed to send a HW ctrl cmd.\n" + e);
    } finally {
      // unlock 前に StackOverflow していた場合, ここでスタックに空きがある状態で unlock 可能.
      // finally 節でのみ unlock すると, ここで StackOverflow した場合 unlock できない可能性がある.
      // try 節と finally 節の両方で StackOverflow することは想定しない.
      if (!unlocked) {
        lock.unlock();
      }
    }
  }

  /** HW を制御するプログラムからコマンドのレスポンスが到着するのを待つ. */
  private List<String> waitForResp(long cmdId, CountDownLatch latch) throws AgencyFailedException {
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new AgencyFailedException("Failed to receive a HW ctrl response.\n" + e);
    }
    return cmdIdToResp.remove(cmdId);
  }

  /** HW を制御するプログラムに送信するコマンドのバイト列を作成する. */
  private byte[] createCmd(long cmdId, String... cmd) throws UnsupportedEncodingException {
    StringJoiner joiner = new StringJoiner(delimiter);
    joiner.add(Long.toString(cmdId));
    for (String field : cmd) {
      joiner.add(field);
    }
    return (joiner.toString() + "\n").getBytes("UTF-8");
  }

  /** 現在実行中の HW を制御するプログラムを停止し, このオブジェクトに関連するリソースを全て開放する. */
  public void end() {
    if (process == null) {
      return;
    }
    try {
      lock.lock();
      process.getOutputStream().write("terminate\n".getBytes("UTF-8"));
      process.waitFor(BhConstants.PROC_END_TIMEOUT, TimeUnit.SECONDS);
      closeStreams();
      respReader.close();
      process = null;
    } catch (Throwable e) {
      LogManager.logger().error("Failed to end the HW ctrl program.\n" + e);
    } finally {
      lock.unlock();
    }
  }

  /** HW を制御するプログラムの標準入力と標準/エラー出力を閉じる. */
  private void closeStreams() throws IOException {
    process.getErrorStream().close();
    stdout.close();
    process.getOutputStream().close();
  }
}
