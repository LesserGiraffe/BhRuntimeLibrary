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

package net.seapanda.bunnyhop.runtime.service;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import net.seapanda.bunnyhop.runtime.BhConstants;
import net.seapanda.bunnyhop.utility.LogManager;
import net.seapanda.bunnyhop.utility.Utility;

/**
 * メッセージ出力クラス.
 *
 * @author K.Koike
 */
public class MsgPrinter implements Closeable {

  private final LogManager logManager;
  private boolean isClosed = false;

  MsgPrinter() throws IOException {
    logManager = new LogManager(
      Paths.get(Utility.execPath, BhConstants.Path.LOG_DIR),
      BhConstants.Path.LOG_FILE_NAME,
      BhConstants.LOG_FILE_SIZE_LIMIT,
      BhConstants.MAX_LOG_FILE_NUM);
  }

  /** デバッグ用エラーメッセージ出力メソッド. */
  public synchronized void errForDebug(String msg) {
    if (isClosed) {
      return;
    }
    Date date = Calendar.getInstance().getTime();
    msg = "[ERR] : %s @ %s\n%s\n----\n".formatted(
        Utility.getMethodName(2),
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date),
        msg);
    System.err.print(msg);
    logManager.writeToFile(msg);
  }

  /** デバッグ用メッセージ出力メソッド. */
  public synchronized void infoForDebug(String msg) {
    if (isClosed) {
      return;
    }
    Date date = Calendar.getInstance().getTime();
    msg = "[INFO] : %s @ %s\n%s\n----\n".formatted(
        Utility.getMethodName(2),
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date),
        msg);
    System.out.print(msg);
    logManager.writeToFile(msg);
  }


  /** 終了処理をする. */
  @Override
  public synchronized void close() {
    isClosed = true;
    logManager.close();
  }
}
