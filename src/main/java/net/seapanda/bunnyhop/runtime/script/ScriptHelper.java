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

import net.seapanda.bunnyhop.runtime.script.debug.DebugInstrumentation;
import net.seapanda.bunnyhop.runtime.script.hw.HwCmdDispatcher;
import net.seapanda.bunnyhop.runtime.script.io.BhTextInput;
import net.seapanda.bunnyhop.runtime.script.io.BhTextIo;
import net.seapanda.bunnyhop.runtime.script.io.BhTextOutput;
import net.seapanda.bunnyhop.runtime.script.platform.AudioController;
import net.seapanda.bunnyhop.runtime.script.platform.FileManager;
import net.seapanda.bunnyhop.runtime.script.platform.FileManager.TextFileManager;
import net.seapanda.bunnyhop.runtime.script.simulator.BhSimulatorCtrl;

/**
 * BhProgram に公開するヘルパークラス.
 *
 * @author K.Koike
 */
public class ScriptHelper {

  public final BhTextIo io;
  public final BhSimulatorCtrl simulator;
  public final HwCmdDispatcher hw;
  public final AudioController audio;
  public final DebugInstrumentation debug;
  public final ScriptUtil util;
  public final FileManager file;
  public final Factory factory;

  /** コンストラクタ. */
  public ScriptHelper(
      BhTextInput textInput,
      BhTextOutput textOutput,
      TextFileManager textFileManager,
      BhSimulatorCtrl simulator,
      HwCmdDispatcher hw,
      AudioController audio,
      DebugInstrumentation debug) {

    this.io = createTextIo(textInput, textOutput);
    this.file = new FileManager(textFileManager);
    this.simulator = simulator;
    this.hw = hw;
    this.audio = audio;
    this.debug = debug;
    this.util = new ScriptUtil();
    this.factory = new Factory();
  }

  private static BhTextIo createTextIo(BhTextInput textInput, BhTextOutput textOutput) {
    return new BhTextIo() {
      @Override
      public String scanln() throws Exception {
        return textInput.scanln();
      }

      @Override
      public void print(String text) throws Exception {
        textOutput.print(text);
      }

      @Override
      public void println(String text) throws Exception {
        textOutput.println(text);
      }
    };
  }
}
