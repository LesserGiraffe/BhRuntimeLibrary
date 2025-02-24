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

import net.seapanda.bunnyhop.utility.Utility;

/**
 * BhProgram の実行時に使用する Utility クラス.
 *
 * @author K.Koike
 */
public class ScriptUtil {

  private final String osName = System.getProperty("os.name").toLowerCase();
  public final Platform platform = this.new Platform();

  public ScriptUtil() {}

  /** 実行ファイルがあるディレクトリのパスを取得する. */
  public String getExecPath() {
    return Utility.execPath;
  }

  public byte toByte(int num) {
    return (byte) num;
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
