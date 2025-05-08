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

package net.seapanda.bunnyhop.runtime.script.simulator;


/**
 * BhSimulator の制御 API を定義したインタフェース.
 *
 * @author K.Koike
 */
public interface BhSimulatorCtrl {

  /** BhSimulator にコマンドを送る.
   *
   * @param cmd BhSimulator に送るコマンド
   * @return {@code cmd} に対するレスポンス
   * @throws Exception シミュレータの制御に失敗した
   */
  String[] sendCmd(String... cmd) throws Exception;
}
