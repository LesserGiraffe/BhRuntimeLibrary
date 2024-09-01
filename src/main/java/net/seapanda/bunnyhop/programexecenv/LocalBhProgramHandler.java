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

import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.seapanda.bunnyhop.bhprogram.common.BhNodeInstanceId;
import net.seapanda.bunnyhop.bhprogram.common.BhProgramData;
import net.seapanda.bunnyhop.bhprogram.common.BhProgramData.Event;
import net.seapanda.bunnyhop.programexecenv.script.RemoteScriptInOut;
import net.seapanda.bunnyhop.programexecenv.script.ScriptParams;
import net.seapanda.bunnyhop.programexecenv.tools.LogManager;

/**
 * ローカル環境で  BhProgram を実行するクラス.
 *
 * @author K.Koike
 */
public class LocalBhProgramHandler {

  private final ExecutorService outputExecutor = Executors.newSingleThreadExecutor();
  private final ExecutorService inoutputExecutor = Executors.newSingleThreadExecutor();
  private final BlockingQueue<BhProgramData> sendDataList =
      new ArrayBlockingQueue<>(BhParams.MAX_QUEUE_SIZE);
  private final RemoteScriptInOut scriptIo =
      new RemoteScriptInOut(sendDataList, new AtomicBoolean(true));
  private final BhProgramExecutor executor = new BhProgramExecutor(scriptIo, sendDataList);

  LocalBhProgramHandler() {
    outputExecutor.submit(() -> outputSendData());
    inoutputExecutor.submit(() -> input());
  }

  /**
   * 引数で指定したスクリプトを実行する.
   *
   * @param fileName 実行ファイル名
   * @param data 実行時にスクリプトに渡すイベントデータ
   * @return 実行に成功した場合true
   */
  public boolean runScript(String fileName, BhProgramData data) {
    return executor.runScript(fileName, data);
  }


  private void outputSendData() {
    while (true) {
      try {
        BhProgramData data = sendDataList.take();
        switch (data.type) {
          case OUTPUT_EXCEPTION:
            System.out.println(data.exception.getMessage() + "\n");
            System.out.println(data.exception.getScriptEngineMsg() + "\n");
            Iterator<BhNodeInstanceId> iter = data.exception.getCallStack().descendingIterator();
            while (iter.hasNext()) {
              System.out.println("  " + iter.next().toString() + "\n");
            }
            break;

          case OUTPUT_STR:
            System.out.println(data.str);
            break;

          default:
            break;
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
        if (line.startsWith(BhParams.BhProgram.STDIN_PREFIX)) {
          putToStdin(line);
        } else if (line.startsWith(BhParams.BhProgram.EVENT_INPUT_PREFIX)) {
          fireEvent(line);
        } else {
          printCmdFormat();
        }
      }
    } catch (Exception e) {
      LogManager.INSTANCE.errMsgForDebug(getClass().getSimpleName() + "  input  " + e.toString());
      e.printStackTrace();
    }
  }

  private void putToStdin(String line) {
    String[] splited = line.split("\\:");
    if (splited.length >= 2) {
      scriptIo.putLineToStdin(splited[1]);
    } else {
      scriptIo.putLineToStdin("");
    }
  }

  private void fireEvent(String line) {
    String[] splited = line.split("\\:");
    if (splited.length >= 2) {
      String eventName = splited[1];
      try {
        Event event = Event.valueOf(eventName);
        executor.fireEvent(new BhProgramData(event, ScriptParams.Funcs.GET_EVENT_HANDLER_NAMES));
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
