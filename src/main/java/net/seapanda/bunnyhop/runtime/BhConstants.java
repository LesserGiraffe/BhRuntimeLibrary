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

import net.seapanda.bunnyhop.utility.version.AppVersion;

/**
 * BhProgramExecutor のパラメータ一式をまとめたクラス.
 *
 * @author K.Koike
 */
public class BhConstants {
  /** アプリケーションの名前. */
  public static final String APP_NAME = "BhRuntimeLibrary";
  /** アプリケーションのバージョン. */
  public static final AppVersion APP_VERSION = AppVersion.of("bhrun-0.7.3");
  /** BhProgram に入力されたテキストデータを格納する FIFO のサイズ. */
  public static final int MAX_INPUT_TEXT_QUEUE_SIZE = 1024;
  /** BunnyHop との通信データを格納する FIFO のサイズ. */
  public static final int MAX_MSG_QUEUE_SIZE = 2048;
  /** BunnyHop への送信データキューの読み出しタイムアウト (ms). */
  public static final int POP_MSG_TIMEOUT = 1500;
  /** BunnyHopからの受信データキューの書き込みタイムアウト (ms). */
  public static final int PUSH_MSG_TIMEOUT = 1500;
  /** ログファイル1つあたりの最大バイト数. */
  public static final int LOG_FILE_SIZE_LIMIT = 1024 * 1024;
  /** ログファイルの最大個数. */
  public static final int MAX_LOG_FILE_NUM = 4;
  /** プロセスの終了完了待ちタイムアウト時間 (sec). */
  public static final int PROC_END_TIMEOUT = 4;

  /** BhProgram に関するパラメータ. */
  public static class BhProgram {
    /** BunnyHop との通信に使う RMI オブジェクトを探す際の TCP ポートに付けられる接尾辞. */
    public static final String RIM_TCP_PORT_SUFFIX = "@RmiTcpPort";
    public static final String STDIN_PREFIX = "i:";
    public static final String EVENT_INPUT_PREFIX = "e:";
  }

  /** ファイルパスに関するパラメータ. */
  public static class Path {
    public static final String LOG_DIR = "Log";
    public static final String LOG_FILE_NAME = "msg";
    /** HW 制御プログラムがあるディレクトリの名前. */
    public static final String ACTIONS = "Actions";
    /** HW 制御プログラムの名前. */
    public static final String HW_CTRL = "hwctrl";
    /** BhProgram によって作成されたデータを格納する場所のルートディレクトリの名前. */
    public static final String USER_DATA = "UserData";
    /** BhProgram によってテキストデータを格納するディレクトリの名前. */
    public static final String TEXT = "Text";
    /** BhProgram によって音声データを格納するディレクトリの名前. */
    public static final String AUDIO = "Audio";
  }
}
