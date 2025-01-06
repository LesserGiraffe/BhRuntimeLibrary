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

package net.seapanda.bunnyhop.runtime.service;

/**
 * アプリケーション全体で使用するクラスのオブジェクトをまとめて保持する.
 *
 * @author K.Koike
 */
public class BhService {

  private static volatile MsgPrinter msgPrinter;

  /** 保持している全てのオブジェクトの初期化処理を行う. */
  public static boolean initialize() {
    try {
      msgPrinter = new MsgPrinter();
    } catch (Exception e) {
      if (msgPrinter != null) {
        msgPrinter.errForDebug(e.toString());
      }
      return false;
    }
    return true;
  }

  public static MsgPrinter msgPrinter() {
    return msgPrinter;
  }
}
