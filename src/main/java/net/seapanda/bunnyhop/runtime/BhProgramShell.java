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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.seapanda.bunnyhop.bhprogram.common.message.BhCallStackItem;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramException;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd.InputTextCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd.OutputTextCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp.OutputTextResp;
import net.seapanda.bunnyhop.runtime.executor.BhProgramExecutor;
import net.seapanda.bunnyhop.runtime.script.BhProgramMessageProcessor;
import net.seapanda.bunnyhop.runtime.script.Keywords;
import net.seapanda.bunnyhop.runtime.script.MessageQueueSet;
import net.seapanda.bunnyhop.runtime.service.LogManager;

/**
 * 外部プログラムとの通信をせずに BhProgram を実行するクラス.
 *
 * @author K.Koike
 */
public class BhProgramShell {

  private final ExecutorService outputExecutor = Executors.newSingleThreadExecutor();
  private final ExecutorService inoutputExecutor = Executors.newSingleThreadExecutor();
  private final MessageQueueSet queueSet;
  private final BhProgramExecutor executor;
  private final BhProgramMessageProcessor<BhTextIoCmd> textIoCmdProcessor;
  private final BhProgramMessageProcessor<BhTextIoResp> textIoRespProcessor;

  /** コンストラクタ. */
  BhProgramShell(
      MessageQueueSet queueSet,
      BhProgramExecutor executor,
      BhProgramMessageProcessor<BhTextIoCmd> textIoCmdProcessor,
      BhProgramMessageProcessor<BhTextIoResp> textIoRespProcessor) {
    this.queueSet = queueSet;
    this.executor = executor;
    this.textIoCmdProcessor = textIoCmdProcessor;
    this.textIoRespProcessor = textIoRespProcessor;
    outputExecutor.submit(() -> output());
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

  private void output() {
    while (true) {
      try {
        BhProgramNotification notif = queueSet.sendNotifList().take();
        switch (notif) {
          case BhProgramException exception -> {
            System.out.println("error message: " + exception.toString() + "\n");
            if (exception.getCause() != null) {
              System.out.println("cause: " + exception.getCause().toString() + "\n");
            }
            System.out.println("call stack");
            for (BhCallStackItem item : exception.getCallStack().reversed()) {
              System.out.println("  %s".formatted(item.nodeId().toString()));
            }
          }

          case OutputTextCmd cmd -> {
            System.out.print(cmd.text);
            textIoRespProcessor.process(new OutputTextResp(cmd.getId(), true, cmd.text));
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
        } else if (line.startsWith(BhConstants.BhProgram.EVENT_INPUT_PREFIX)) {
          fireEvent(line);
        } else {
          printCmdFormat();
        }
      }
    } catch (Exception e) {
      LogManager.logger().error("Failed to input string to BhProgram.\n%s".formatted(e));
      e.printStackTrace();
    }
  }

  private void pushToStdinBuf(String line) {
    String[] splited = line.split("\\:");
    if (splited.length >= 2) {
      textIoCmdProcessor.process(new InputTextCmd(splited[1]));
    } else {
      textIoCmdProcessor.process(new InputTextCmd(""));
    }
  }


  private void fireEvent(String line) {
    String[] splited = line.split("\\:");
    if (splited.length >= 2) {
      String eventName = splited[1];
      try {
        var event = BhProgramEvent.Name.valueOf(eventName);
        executor.fireEvent(new BhProgramEvent(event, Keywords.Funcs.GET_EVENT_HANDLER_NAMES));
      } catch (Exception e) {
        System.err.println("invalid event name  " + eventName);
      }
    } else {
      System.err.println("An event name must be specified.");
    }
  }

  private void printCmdFormat() {
    System.out.println("Input command format");
    System.out.println("  %s input string to stdin".formatted(BhConstants.BhProgram.STDIN_PREFIX));
    System.out.println("  %s EVENT_NAME".formatted(BhConstants.BhProgram.EVENT_INPUT_PREFIX));
    System.out.println();
  }
}
