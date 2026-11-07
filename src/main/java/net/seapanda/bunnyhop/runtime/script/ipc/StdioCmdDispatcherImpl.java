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

package net.seapanda.bunnyhop.runtime.script.ipc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import net.seapanda.bunnyhop.runtime.script.AgencyFailedException;
import net.seapanda.bunnyhop.runtime.service.LogManager;
import org.apache.commons.lang3.StringUtils;

/**
 * 標準入出力を使って他のプロセスにコマンドを送信して, そのレスポンスを受信するメソッドを提供するクラス.
 *
 * <p>コンストラクタで指定したコマンドでプロセスを起動し, そのプロセスの標準入力にコマンドを書き込んで,
 * 標準出力からレスポンスを読み取る.
 * レスポンスの読み取りは専用のスレッドが行い, 受信したレスポンスはその先頭のコマンド ID を基に
 * 対応する {@link #sendCmd} の呼び出し元に渡される.
 * このため, 複数のスレッドから同時に {@link #sendCmd} を呼び出すことができる.
 *
 * @author K.Koike
 */
public class StdioCmdDispatcherImpl implements StdioCmdDispatcher {

  /** コマンドおよびレスポンス内で使用される区切り文字. */
  private volatile String delimiter = ",";
  /** コマンドおよびレスポンスの終了を表す区切り文字. */
  private volatile String terminator = "\n";
  /** コマンドを送受信するプロセスの {@link Process} オブジェクト. */
  private final Process process;
  /** {@link #end} による終了処理が行われた場合 true. */
  private boolean ended = false;
  /** プロセスからコマンドの応答を取得する Executor. */
  private final ExecutorService respReader = Executors.newSingleThreadExecutor();
  /** 送信するコマンドの ID. */
  private final AtomicLong commandId = new AtomicLong();
  /** コマンド ID とその ID のコマンドの完了を待つための同期用オブジェクトのマップ. */
  private final Map<Long, CountDownLatch> cmdIdToBarrier = new ConcurrentHashMap<>();
  /** コマンド ID とその ID のコマンドのレスポンスのマップ. */
  private final Map<Long, List<String>> cmdIdToResp = new ConcurrentHashMap<>();
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * コンストラクタ.
   *
   * @param command コマンドの送受信先となるプロセスを起動するコマンドとその引数
   * @throws AgencyFailedException プロセスの起動に失敗した
   */
  public StdioCmdDispatcherImpl(String... command) throws AgencyFailedException {
    ProcessBuilder procBuilder = new ProcessBuilder(command);
    try {
      process = procBuilder.start();
      respReader.submit(this::readResponse);
    } catch (IOException e) {
      throw new AgencyFailedException(e.toString());
    }
  }

  @Override
  public void setDelimiter(String delimiter) {
    if (StringUtils.isEmpty(delimiter)) {
      throw new IllegalArgumentException("Invalid delimiter %s".formatted(delimiter));
    }
    this.delimiter = delimiter;
  }

  @Override
  public String getDelimiter() {
    return this.delimiter;
  }

  @Override
  public void setTerminator(String terminator) {
    if (StringUtils.isEmpty(terminator)) {
      throw new IllegalArgumentException("Invalid terminator %s".formatted(terminator));
    }
    this.terminator = terminator;
  }

  @Override
  public String getTerminator() {
    return this.terminator;
  }

  /**
   * プロセスの標準出力からコマンドのレスポンスを読み続ける.
   *
   * <p>{@link #getTerminator} が返す終端文字を読むまでを 1 つのレスポンスとして扱う.
   * 終端文字を読まずに入力が終わった場合, 読み込み済みの文字列は破棄する.
   * 入力が終わった時点でレスポンスを待っているコマンドは, 全てエラーとして待機を解除する.
   */
  private void readResponse() {
    try (BufferedReader ir = process.inputReader()) {
      var buf = new StringBuilder();
      while (!Thread.currentThread().isInterrupted()) {
        int ch = ir.read();
        if (ch == -1) {
          break;
        }
        buf.append((char) ch);
        String term = terminator;
        if (!StringUtils.endsWith(buf, term)) {
          continue;
        }
        String respStr = buf.substring(0, buf.length() - term.length());
        buf.setLength(0);
        try {
          onRespReceived(respStr);
        } catch (Exception e) {
          LogManager.logger().error(e, "Received an invalid response.  (%s)", respStr);
        } catch (Throwable ignored) { /* Do nothing. */  }
      }
    } catch (IOException ignored) { /* Do nothing. */
    } finally {
      releaseAllBarriers();
    }
  }

  /**
   * レスポンスの到着を待っている全てのコマンドの待機を解除する.
   *
   * <p>レスポンスを格納せずに待機を解除するため, 待機していたスレッドはエラーとなる.
   * これ以上レスポンスを受信できなくなったときに呼ぶこと.
   */
  private void releaseAllBarriers() {
    cmdIdToBarrier.values().forEach(CountDownLatch::countDown);
  }

  /**
   * 受信したレスポンス文字列を解析し, 対応するコマンドの待機を解除する.
   *
   * <p>先頭のフィールドをコマンド ID として扱う. 対応するコマンドが存在しない場合, そのレスポンスは破棄する.
   */
  private void onRespReceived(String respStr) {
    List<String> resp = new ArrayList<>(Arrays.asList(respStr.split(Pattern.quote(delimiter))));
    long respId = Long.parseLong(resp.removeFirst());
    CountDownLatch latch = cmdIdToBarrier.remove(respId);
    if (latch != null) {
      cmdIdToResp.put(respId, resp);
      latch.countDown();
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

  /** プロセスの標準入力に {@code cmdId} と {@code cmd} から作成したコマンドを書き込む. */
  private void sendCmd(long cmdId, String... cmd) throws AgencyFailedException {
    boolean unlocked = false;
    try {
      lock.lock();
      if (ended) {
        throw new AgencyFailedException("Program has ended.");
      }
      byte[] cmdData = createCmd(cmdId, cmd).getBytes(UTF_8);
      process.getOutputStream().write(cmdData);
      process.getOutputStream().flush();
      lock.unlock();
      unlocked = true;
    } catch (IOException e) {
      throw new AgencyFailedException("Failed to send a cmd.\n" + e);
    } finally {
      // unlock 前に StackOverflow していた場合, ここでスタックに空きがある状態で unlock 可能.
      // finally 節でのみ unlock すると, ここで StackOverflow した場合 unlock できない可能性がある.
      // try 節と finally 節の両方で StackOverflow することは想定しない.
      if (!unlocked) {
        lock.unlock();
      }
    }
  }

  /**
   * {@code cmdId} のコマンドのレスポンスが到着するのを待って, それを返す.
   *
   * <p>レスポンスが到着するまで呼び出し元のスレッドをブロックする.
   * レスポンスを受信できないことが確定した場合, {@link AgencyFailedException} を投げる.
   */
  private List<String> waitForResp(long cmdId, CountDownLatch latch) throws AgencyFailedException {
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new AgencyFailedException("Failed to receive a response.\n" + e);
    }
    List<String> resp = cmdIdToResp.remove(cmdId);
    if (resp == null) {
      throw new AgencyFailedException("No response was received.  (cmd id: %s)".formatted(cmdId));
    }
    return resp;
  }

  /**
   * プロセスに送信するコマンドの文字列を作成する.
   *
   * <p>先頭に {@code cmdId} を付けた各フィールドを {@link #delimiter} で連結し, 最後に {@link #terminator} をつける.
   */
  private String createCmd(long cmdId, String... cmd) {
    StringJoiner joiner = new StringJoiner(delimiter);
    joiner.add(Long.toString(cmdId));
    for (String field : cmd) {
      joiner.add(field);
    }
    return joiner + terminator;
  }

  /**
   * プロセスに送信するコマンドの文字列を作成する.
   *
   * <p>各フィールドを {@link #delimiter} で連結し, 最後に {@link #terminator} をつける.
   */
  private String createCmd(String... cmd) {
    StringJoiner joiner = new StringJoiner(delimiter);
    for (String field : cmd) {
      joiner.add(field);
    }
    return joiner + terminator;
  }

  @Override
  public void end(int timeout, boolean addCmdId, String... terminationCmd)
      throws AgencyFailedException {
    try {
      lock.lock();
      if (ended) {
        return;
      }
      sendTerminationCmd(addCmdId, terminationCmd);
      if (!process.waitFor(timeout, SECONDS)) {
        LogManager.logger().error(
            "The process did not exit in %s seconds.  Kill it forcibly.", timeout);
        killProcessTree(timeout);
      }
      closeStreams();
      respReader.shutdownNow();
      if (!respReader.awaitTermination(100, MILLISECONDS)) {
        throw new TimeoutException("Failed to stop the thread reading responses.");
      }
    } catch (Throwable e) {
      String msg = "Failed to end the program.";
      LogManager.logger().error(msg, e);
      throw new AgencyFailedException(msg, e);
    } finally {
      ended = true;
      releaseAllBarriers();
      lock.unlock();
    }
  }

  /**
   * {@link #process} が管理するプロセスとその子孫プロセスを全て強制終了する.
   *
   * @param timeout 全てのプロセスの終了を待つ時間 (秒)
   */
  private void killProcessTree(int timeout) throws InterruptedException {
    var procs = new ArrayList<ProcessHandle>();
    procs.add(process.toHandle());
    process.descendants().forEach(procs::add);
    CompletableFuture<?>[] exits =
        procs.stream().map(ProcessHandle::onExit).toArray(CompletableFuture[]::new);
    procs.forEach(ProcessHandle::destroyForcibly);
    try {
      CompletableFuture.allOf(exits).get(timeout, SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      LogManager.logger().error(
          "Some of the processes did not exit in %s seconds.  (%s)", timeout, e);
    }
  }

  /** プロセスを終了するためのコマンドを送信する. */
  private void sendTerminationCmd(boolean addCmdId, String[] terminationCmd) throws IOException {
    String command = addCmdId
        ? createCmd(commandId.getAndIncrement(), terminationCmd)
        : createCmd(terminationCmd);
    process.getOutputStream().write(command.getBytes(UTF_8));
    process.getOutputStream().flush();
  }

  /**
   * プロセスの標準入力と標準エラー出力を閉じる.
   *
   * <p>プロセスの標準出力は {@link #readResponse} が閉じる.
   */
  private void closeStreams() throws IOException {
    process.getErrorStream().close();
    process.getOutputStream().close();
  }
}
