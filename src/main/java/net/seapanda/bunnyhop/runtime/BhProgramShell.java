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

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.seapanda.bunnyhop.bhprogram.common.BhNodeInstanceId;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramException;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramMessage;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd.InputTextCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd.OutputTextCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp.InputTextResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp.OutputTextResp;
import net.seapanda.bunnyhop.runtime.script.ScriptHelper;
import net.seapanda.bunnyhop.runtime.script.ScriptParams;
import net.seapanda.bunnyhop.runtime.service.BhService;

/**
 * ローカル環境で BhProgram を実行するクラス.
 *
 * @author K.Koike
 */
public class BhProgramShell {

  private final ExecutorService outputExecutor = Executors.newSingleThreadExecutor();
  private final ExecutorService inoutputExecutor = Executors.newSingleThreadExecutor();
  /** BunnyHop へ送信するメッセージを格納する FIFO. */
  private final BlockingQueue<BhProgramMessage> sendMsgList =
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE);
  /** BunnyHop へ送信するレスポンスを格納する FIFO. */
  private final BlockingQueue<BhProgramResponse> sendRespList =
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE);
  private final ScriptHelper scriptHelper = new ScriptHelper(sendMsgList, sendRespList, true);
  private final BhProgramExecutor executor = new BhProgramExecutor(scriptHelper, sendMsgList);
  private final BlockingQueue<Long> scanCmdIdList =
      new ArrayBlockingQueue<>(BhConstants.MAX_MSG_QUEUE_SIZE);
  private final BlockingQueue<String> stdInBuf =
      new ArrayBlockingQueue<>(BhConstants.MAX_INPUT_TEXT_QUEUE_SIZE);

  BhProgramShell() {
    outputExecutor.submit(() -> outputSendData());
    inoutputExecutor.submit(() -> input());
  }

  /**
   * 標準入出力から BhProgram の入出力とデータをやり取りするためのクラス.
   *
   * @param fileName 実行ファイル名
   * @param event 実行時にスクリプトに渡すイベントデータ
   * @return 実行に成功した場合 true
   */
  public boolean runScript(String fileName, BhProgramEvent event) {
    return executor.runScript(fileName, event);
  }


  private void outputSendData() {
    while (true) {
      try {
        BhProgramMessage msg = sendMsgList.take();
        switch (msg) {
          case BhProgramException exception -> {
            System.out.println(exception.getMessage() + "\n");
            System.out.println(exception.getScriptEngineMsg() + "\n");
            for (BhNodeInstanceId instId : exception.getCallStack().reversed()) {
              System.out.println("  " + instId.toString() + "\n");
            }
          }

          case OutputTextCmd cmd -> {
            System.out.println(cmd.text);
            scriptHelper.io.notify(new OutputTextResp(cmd.getId(), true, cmd.text));
          }

          case InputTextCmd cmd -> {
            scanCmdIdList.offer(cmd.getId());
            inputToScript();
          }

          default -> { }
        }
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  /** BhProgram への入力を処理する. */
  private void input() {
    try (Scanner scan = new Scanner(System.in)) {
      while (true) {
        String line = scan.nextLine();
        if (line.startsWith(BhConstants.BhProgram.STDIN_PREFIX)) {
          pushToStdinBuf(line);
          inputToScript();
        } else if (line.startsWith(BhConstants.BhProgram.EVENT_INPUT_PREFIX)) {
          fireEvent(line);
        } else {
          printCmdFormat();
        }
      }
    } catch (Exception e) {
      BhService.msgPrinter().errForDebug(
          "Failed to input string to BhProgram.\n%s".formatted(e));
      e.printStackTrace();
    }
  }

  private void pushToStdinBuf(String line) {
    String[] splited = line.split("\\:");
    if (splited.length >= 2) {
      stdInBuf.offer(splited[1]);
    } else {
      stdInBuf.offer("");
    }
  }

  /** バッファリングしたテキストをスクリプトに入力する. */
  private void inputToScript() {
    if (!stdInBuf.isEmpty() && !scanCmdIdList.isEmpty()) {
      try {
        String inputText = stdInBuf.take();
        long cmdId = scanCmdIdList.take();
        scriptHelper.io.notify(new InputTextResp(cmdId, true, inputText));
      } catch (InterruptedException e) { /* do nothing */ }
    }
  }

  private void fireEvent(String line) {
    String[] splited = line.split("\\:");
    if (splited.length >= 2) {
      String eventName = splited[1];
      try {
        var event = BhProgramEvent.Name.valueOf(eventName);
        executor.fireEvent(new BhProgramEvent(event, ScriptParams.Funcs.GET_EVENT_HANDLER_NAMES));
      } catch (Exception e) {
        System.err.println("invalid event name  " + eventName);
      }
    } else {
      System.err.println("An event name must be specified.");
    }
  }

  private void printCmdFormat() {
    System.out.println("Input command format");
    System.out.println("  i:input string to stdin");
    System.out.println("  e:EVENT_NAME");
    System.out.println();
  }
}
