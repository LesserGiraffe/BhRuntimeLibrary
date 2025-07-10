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

package net.seapanda.bunnyhop.bhprogram.common.message.debug;

/**
 * {@link net.seapanda.bunnyhop.bhprogram.common.message.debug.GetThreadContextsCmd} のレスポンス.
 *
 * <p>このレスポンスには, スレッドコンテキストを含めない.
 * 理由は, レスポンスにスレッドコンテキスト含めると, 通知キュー経由で BunnyHop に送られるスレッドコンテキストと,
 * 応答キュー経由で送られるこのレスポンスのスレッドコンテキストのどちらが新しいのか BunnyHop で判断できないからである.
 *
 * @author K.Koike
 */
public class GetThreadContextsResp extends BhDebugResp {
  
  /**
   * コンストラクタ.
   *
   * @param id 実行したコマンドの ID
   * @param success コマンドの処理に成功した場合 true
   */
  public GetThreadContextsResp(long id, boolean success) {
    super(id, success);
  }

  /**
   * コンストラクタ.
   *
   * @param id 実行したコマンドの ID
   * @param exception コマンドの実行中に発生した例外
   */
  public GetThreadContextsResp(long id, Exception exception) {
    super(id, false, exception);
  }
}
