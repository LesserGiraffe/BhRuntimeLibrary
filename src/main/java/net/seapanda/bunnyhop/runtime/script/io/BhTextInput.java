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

package net.seapanda.bunnyhop.runtime.script.io;


/**
 * BunnyHop のテキスト IO を操作する処理を定義したインタフェース.
 *
 * <p>BunnyHop のテキスト IO : BunnyHop と BhRuntimeLibrary 間で文字列データをやり取りするためのインタフェース.
 *
 * @author K.Koike
 */
public interface BhTextInput {

  /**
   * BunnyHop のテキスト I/O から文字列を読みだす.
   *
   * @return 読み出された文字列
   * @throws Exception 文字列の読み出しに失敗した
   */
  public String scanln() throws Exception;
}
