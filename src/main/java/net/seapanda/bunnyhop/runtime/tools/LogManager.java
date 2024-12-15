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

package net.seapanda.bunnyhop.runtime.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import net.seapanda.bunnyhop.runtime.BhConstants;

/**
 * ログ出力クラス.
 *
 * @author K.Koike
 */
public class LogManager {

  /** シングルトンインスタンス. */
  public static final LogManager INSTANCE = new LogManager();
  private boolean isLocal = false;

  private LogManager() {}

  /** このオブジェクトを初期化する. */
  public boolean init(boolean isLocal) {
    this.isLocal = isLocal;
    initLogSystem();  //ログシステムがエラーでも処理は続ける
    return true;
  }

  /** ログ機能を初期化する. */
  private boolean initLogSystem() {
    Path logFilePath = genLogFilePath(0);
    if (!Util.INSTANCE.createDirectoryIfNotExists(
        Paths.get(Util.INSTANCE.execPath, BhConstants.Path.LOG_DIR))) {
      return false;
    }
    if (!Util.INSTANCE.createFileIfNotExists(logFilePath)) {
      return false;
    }
    try {
      //ログローテーション
      if (Files.size(logFilePath) > BhConstants.LOG_FILE_SIZE_LIMIT && !renameLogFiles()) {
        return false;
      }
    } catch (IOException | SecurityException e) {
      return false;
    }
    return true;
  }

  /** デバッグ用メッセージを出力する. */
  public void errMsgForDebug(String msg) {
    String logMsg = 
        (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(Calendar.getInstance().getTime())
        + "  ERR : " + msg + "\n";
    writeMsgToLogFile(logMsg);
    if (isLocal) {
      System.err.println(msg + "\n");
    }
  }

  /** デバッグ用メッセージ出力メソッド. */
  public void msgForDebug(String msg) {
    String logMsg =
        (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(Calendar.getInstance().getTime())
        + "  MSG : " + msg + "\n";
    writeMsgToLogFile(logMsg);
    if (isLocal) {
      System.out.println(msg);
    }
  }

  /**
   * ログファイルにメッセージを書き込む.
   *
   * @param msg ログファイルに書き込むメッセージ
   */
  private synchronized void writeMsgToLogFile(String msg) {

    try (OutputStream logOutputStream =
        Files.newOutputStream(
            genLogFilePath(0),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND,
            StandardOpenOption.WRITE);) {
      logOutputStream.write(msg.getBytes(StandardCharsets.UTF_8));
    } catch (IOException | SecurityException e) { /* do nothing */ }
  }

  /**
   * ログローテンションのため, ログファイルをリネームする.
   *
   * @return リネームに成功した場合true
   */
  private boolean renameLogFiles() {
    try {
      Path oldestLogFilePath = genLogFilePath(BhConstants.MAX_LOG_FILE_NUM - 1);
      if (Files.exists(oldestLogFilePath)) {
        Files.delete(oldestLogFilePath);
      }
      for (int fileNo = BhConstants.MAX_LOG_FILE_NUM - 2; fileNo >= 0; --fileNo) {
        Path oldLogFilePath = genLogFilePath(fileNo);
        Path newLogFilePath = genLogFilePath(fileNo + 1);
        if (Files.exists(oldLogFilePath)) {
          Files.move(oldLogFilePath, newLogFilePath, StandardCopyOption.ATOMIC_MOVE);
        }
      }
    } catch (IOException | SecurityException e) {
      return false;
    }
    return true;
  }

  private Path genLogFilePath(int fileNo) {

    String numStr = ("0000" + fileNo);
    numStr = numStr.substring(numStr.length() - 4, numStr.length());
    String logFileName = BhConstants.Path.LOG_FILE_NAME + numStr + ".log";
    return Paths.get(Util.INSTANCE.execPath, BhConstants.Path.LOG_DIR, logFileName);
  }
}
