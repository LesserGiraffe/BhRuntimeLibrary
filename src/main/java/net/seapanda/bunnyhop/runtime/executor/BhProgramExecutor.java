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

package net.seapanda.bunnyhop.runtime.executor;

import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;

/**
 * BhProgram を実行処理を規定したインタフェース.
 *
 * @author K.Koike
 */
public interface BhProgramExecutor {

  /**
   * {@code fileName} で指定したスクリプトを実行する.
   *
   * @param fileName 実行するスクリプトファイル名. 絶対パスか BhRuntime の実行時パスからの相対パスで指定すること.
   */
  boolean runScript(String fileName);

  /**
   * BhProgram のイベントハンドラを呼び出す.
   *
   * @param event このイベントに関連するイベントハンドラを呼び出す.
   */
  void fireEvent(BhProgramEvent event);
}
