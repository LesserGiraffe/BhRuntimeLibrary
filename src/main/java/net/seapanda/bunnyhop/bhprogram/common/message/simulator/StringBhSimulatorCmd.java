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

package net.seapanda.bunnyhop.bhprogram.common.message.simulator;

import java.util.Arrays;

/**
 * 文字列ベースの BhSimulator コマンド.
 *
 * @author K.Koike
 */
public class StringBhSimulatorCmd extends BhSimulatorCmd {

  String[] cmd;

  public StringBhSimulatorCmd(String[] cmd) {
    this.cmd = Arrays.copyOf(cmd, cmd.length);
  }

  /** コマンドを構成する要素を返す. */
  public String[] getComponents() {
    return Arrays.copyOf(cmd, cmd.length);
  }
}
