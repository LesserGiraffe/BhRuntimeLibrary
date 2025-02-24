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

import net.seapanda.bunnyhop.runtime.script.hw.HwCmdDispatcher;
import net.seapanda.bunnyhop.runtime.script.io.BhTextInput;
import net.seapanda.bunnyhop.runtime.script.io.BhTextIo;
import net.seapanda.bunnyhop.runtime.script.io.BhTextOutput;
import net.seapanda.bunnyhop.runtime.script.simulator.BhSimulatorCtrl;

/**
 * BhProgram に公開するヘルパークラス.
 *
 * @author K.Koike
 */
public class ScriptHelper {

  public final BhTextIo io;
  public final BhSimulatorCtrl simulator;
  public final ScriptUtil util;
  public final HwCmdDispatcher hw;
  public final ThreadUtil thread;
  public final Factory factory;

  /** コンストラクタ. */
  public ScriptHelper(
      BhTextInput textInput,
      BhTextOutput textOutput,
      BhSimulatorCtrl simulator,
      HwCmdDispatcher hw) {
    this.io = new BhTextIo() {
      @Override
      public String scanln() throws Exception {
        return textInput.scanln();
      }

      @Override
      public void println(String text) throws Exception {
        textOutput.println(text);
      }
    };
    this.simulator = simulator;
    this.hw = hw;
    this.util = new ScriptUtil();
    this.thread = new ThreadUtil();
    this.factory = new Factory();
  }
}
