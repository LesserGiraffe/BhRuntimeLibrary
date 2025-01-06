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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.seapanda.bunnyhop.bhprogram.common.BhNodeInstanceId;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramException;
import net.seapanda.bunnyhop.utility.SynchronizingTimer;
import net.seapanda.bunnyhop.utility.Utility;

/**
 * BhProgram の実行時に使用する Utility クラス.
 *
 * @author K.Koike
 */
public class ScriptUtil {

  private final String osName = System.getProperty("os.name").toLowerCase();
  public final Platform platform = this.new Platform();
  private final Map<Long, Object> threadIdToUserData = new ConcurrentHashMap<>();

  public ScriptUtil() {}

  /**
   * {@link BhNodeInstanceId} オブジェクトを作成して返す.
   *
   * @param id 作成するIDの文字列表現
   * @return {@link BhNodeInstanceId} オブジェクト
   */
  public BhNodeInstanceId newBhNodeInstanceId(String id) {
    return new BhNodeInstanceId(id);
  }

  /**
   * {@link BhProgramException} オブジェクトを作成して返す.
   *
   * @param callStack 例外発生時のコールスタック
   * @param msg 例外メッセージ
   * @return 例外オブジェクト
   */
  public BhProgramException newBhProgramException(List<?> callStack, String msg) {
    return newBhProgramException(callStack, msg, "");
  }

  /**
   * {@link BhProgramException} オブジェクトを作成して返す.
   *
   * @param callStack 例外発生時のコールスタック
   * @param msg 例外メッセージ
   * @param scriptEngineMsg BhProgram の実行エンジンから返されたエラーメッセージ
   * @return 例外オブジェクト
   */
  public BhProgramException newBhProgramException(
      List<?> callStack, String msg, String scriptEngineMsg) {
    ArrayList<BhNodeInstanceId> funcCallStack = callStack.stream()
        .map(nodeInstanceID -> newBhNodeInstanceId(nodeInstanceID.toString()))
        .collect(Collectors.toCollection(ArrayList::new));

    return new BhProgramException(funcCallStack, msg, scriptEngineMsg);
  }

  /** {@link SynchronizingTimer} を新規作成する. */
  public SynchronizingTimer newSyncTimer(int count, boolean autoReset) {
    return new SynchronizingTimer(count, autoReset);
  }

  /** 実行ファイルがあるディレクトリのパスを取得する. */
  public String getExecPath() {
    return Utility.execPath;
  }

  public byte toByte(int num) {
    return (byte) num;
  }

  /** {@code data} をこのメソッドを呼び出したスレッド固有のデータとして保持する. */
  public void setThreadData(Object userData) {
    threadIdToUserData.put(Thread.currentThread().threadId(), userData);
  }

  /** {@link #storeThreadData} で保存したデータを取得する. */
  public Object getThreadData() {
    return threadIdToUserData.get(Thread.currentThread().threadId());
  }

  /** {@link #storeThreadData} で保存したデータを削除する. */
  public Object removeThreadData() {
    return threadIdToUserData.remove(Thread.currentThread().threadId());
  }


  /** OS を識別するためのクラス. */
  public class Platform {
    public boolean isWindows() {
      return osName.startsWith("windows");
    }

    public boolean isLinux() {
      return osName.startsWith("linux");
    }
  }
}
