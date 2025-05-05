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

package net.seapanda.bunnyhop.bhprogram.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;

/**
 * BhProgram の実行環境 (BhRuntime) に対する操作を規定したインタフェース.
 *
 * @author K.Koike
 */
public interface BhRuntimeFacade extends Remote {

  /**
   * 引数で指定した BhProgram を実行する.
   *
   * @param fileName 実行ファイル名
   * @param event BhProgram 開始時に BhProgram に渡されるイベント
   * @return 実行に成功した場合true
   */
  boolean runScript(String fileName, BhProgramEvent event) throws RemoteException;

  /** BhRuntime との通信を無効化する. */
  void disconnect() throws RemoteException;

  /** BhRuntime との通信を有効化する. */
  void connect() throws RemoteException;

  /** BunnyHopとの通信状態を取得する. */
  boolean isConnected() throws RemoteException;

  /**
   * BhRuntime にメッセージを送信する.
   *
   * @param notif 送信する通知. null不可.
   * @return 送信に成功した場合 true
   */
  boolean sendNotifToRuntime(BhProgramNotification notif) throws RemoteException;

  /**
   * BhRuntime からレスポンスを受信する.
   *
   * @return 受信したレスポンス. 受信に失敗した場合もしくは受信可能なレスポンスがなかった場合 null.
   */
  BhProgramResponse recvRespFromRuntime() throws RemoteException;

  /**
   * BhRuntime からメッセージを受信する.
   *
   * @return 受信したメッセージ. 受信に失敗した場合もしくは受信可能なメッセージがなかった場合 null.
   */
  BhProgramNotification recvNotifFromRuntime() throws RemoteException;

  /**
   * BhRuntime にレスポンスを送信する.
   *
   * @param resp 送信するレスポンス. null不可.
   * @return 送信に成功した場合 true
   */
  boolean sendRespToRuntime(BhProgramResponse resp) throws RemoteException;
}
