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

package net.seapanda.bunnyhop.runtime.factory;


import net.seapanda.bunnyhop.runtime.executor.BhProgramExecutor;
import net.seapanda.bunnyhop.runtime.script.io.BhTextInputAgent;
import net.seapanda.bunnyhop.runtime.script.simulator.BhSimulatorAgent;

/**
 * BhProgram の実行に必要なオブジェクトを生成する機能を規定したインタフェース.
 *
 * @author K.Koike
 */
public interface RuntimeComponentFactory {
  
  /** {@link BhSimulatorAgent} オブジェクトを作成する. */
  BhSimulatorAgent createSimulatorAgent();

  /** {@link BhTextInputAgent} オブジェクトを作成する. */
  BhTextInputAgent createIoAgent();

  /** {@link BhProgramExecutor} オブジェクトを作成する. */
  BhProgramExecutor createBhProgramExecutor();
}
