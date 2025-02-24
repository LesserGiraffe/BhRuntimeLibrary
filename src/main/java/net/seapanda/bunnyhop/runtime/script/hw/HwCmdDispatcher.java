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

package net.seapanda.bunnyhop.runtime.script.hw;

import java.util.List;
import net.seapanda.bunnyhop.runtime.script.AgencyFailedException;

/**
 * HW を制御するコマンドを送信して, そのレスポンスを受信するメソッドを定義したクラス.
 *
 * @author K.Koike
 */
public interface HwCmdDispatcher {

  /**
   * HW を制御するコマンドを送信して, そのレスポンスを受信する.
   *
   * @param cmd 送信するコマンド
   * @return {@code cmd} に対するレスポンス
   * @throws AgencyFailedException コマンドの送信もしくは, そのレスポンスの受信に失敗した
   */
  public default List<String> sendCmd(String... cmd) throws AgencyFailedException {
    throw new AgencyFailedException("Hardware control is not supported.");
  }
}
