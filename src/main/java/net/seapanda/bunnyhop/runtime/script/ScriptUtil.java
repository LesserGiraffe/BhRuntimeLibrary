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

import java.nio.charset.Charset;
import java.util.Arrays;
import net.seapanda.bunnyhop.utility.Utility;

/**
 * BhProgram の実行時に使用する Utility クラス.
 *
 * @author K.Koike
 */
public class ScriptUtil {

  public final Platform platform = new Platform();
  public final Timer timer = new Timer();

  public ScriptUtil() {}

  /** 実行ファイルがあるディレクトリのパスを取得する. */
  public String getExecPath() {
    return Utility.execPath;
  }

  public byte toByte(int num) {
    return (byte) num;
  }

  /**
   * 文字列を指定したバイト数以下の文字列に切り詰める.
   *
   * @param text 切り詰める文字列
   * @param numBytes このバイト数以下になるように {@code input} を切り詰める
   * @param charset バイト数を計算する際の文字コード
   * @return 切り詰められt文字列
   */
  public String substringByBytes(String text, int numBytes, String charset) {
    Charset cs = Charset.forName(charset);
    if (text == null) {
      return null;
    }
    if (numBytes <= 0) {
      return "";
    }
    String substring = new String(Arrays.copyOf(text.getBytes(cs), numBytes), cs);
    if (text.startsWith(substring)) {
      return substring;
    }
    return substringByBytes(text, numBytes - 1, charset);
  }

  /** OS を識別するためのクラス. */
  public static class Platform {

    private final String osName = System.getProperty("os.name").toLowerCase();

    public boolean isWindows() {
      return osName.startsWith("windows");
    }

    public boolean isLinux() {
      return osName.startsWith("linux");
    }
  }

  /** 時間を取得するためのクラス. */
  public static class Timer {
    private long startTime = 0L;

    public void start() {
      startTime = System.currentTimeMillis();
    }
  
    public long getMillis() {
      return System.currentTimeMillis() - startTime;
    }
  }
}

