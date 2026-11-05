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

package net.seapanda.bunnyhop.runtime.script.ipc;

import java.util.List;
import net.seapanda.bunnyhop.runtime.script.AgencyFailedException;

/**
 * 標準入出力を使って他のプロセスにコマンドを送信して, そのレスポンスを受信するメソッドを規定したインタフェース.
 *
 * <p>コマンドは, 送信ごとに割り振られるコマンド ID とコマンドの各フィールドを
 * {@link #getDelimiter} が返す区切り文字で連結し, その末尾に {@link #getTerminator} が返す終端文字を
 * 付けた文字列として送信される.
 * レスポンスも同じ形式であり, その先頭のコマンド ID によって対応するコマンドが決まる.
 *
 * @author K.Koike
 */
public interface StdioCmdDispatcher {

  /**
   * このオブジェクトが管理するプロセスにコマンドを送信して, そのレスポンスを受信する.
   *
   * <p>レスポンスが到着するまで呼び出し元のスレッドをブロックする.
   *
   * @param cmd 送信するコマンド. 各要素がコマンドの 1 フィールドに対応する.
   * @return {@code cmd} に対するレスポンス. 先頭のコマンド ID を除いた各フィールドのリスト.
   * @throws AgencyFailedException コマンドの送信もしくは, そのレスポンスの受信に失敗した
   */
  List<String> sendCmd(String... cmd) throws AgencyFailedException;

  /**
   * 現在実行中のプロセスを停止し, このオブジェクトに関連するリソースを全て開放する.
   *
   * <p>プロセスが既に停止している場合は何もしない.
   *
   * @param timeout プロセスの停止を待つ時間 (秒)
   * @param addCmdId プロセスを停止するために送信するコマンドにコマンド ID をつける場合 true
   * @param terminationCmd プロセスを停止するために送信するコマンド. 各要素がコマンドの 1 フィールドに対応する.
   * @throws AgencyFailedException プロセスの停止もしくは, リソースの解放に失敗した
   */
  void end(int timeout, boolean addCmdId, String... terminationCmd) throws AgencyFailedException;

  /**
   * コマンドおよびレスポンス内のフィールドの区切り文字を設定する.
   *
   * @param delimiter 設定する区切り文字.  null および空文字不可.
   */
  void setDelimiter(String delimiter);

  /**
   * コマンドおよびレスポンス内のフィールドの区切り文字を取得する.
   *
   * @return 現在設定されている区切り文字
   */
  String getDelimiter();

  /**
   * コマンドおよびレスポンスの終了を表す終端文字を設定する.
   *
   * @param terminator 設定する終端文字.  null および空文字不可.
   */
  void setTerminator(String terminator);

  /**
   * コマンドおよびレスポンスの終了を表す終端文字を取得する.
   *
   * @return 現在設定されている終端文字
   */
  String getTerminator();
}
