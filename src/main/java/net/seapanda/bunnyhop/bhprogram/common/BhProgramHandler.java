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
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramMessage;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;

/**
 * BhProgram の実行環境との通信を行うクラス.
 *
 * @author K.Koike
 */
public interface BhProgramHandler extends Remote {

  /**
   * 引数で指定した BhProgram を実行する.
   *
   * @param fileName 実行ファイル名
   * @param event BhProgram 開始時に BhProgram に渡されるイベント
   * @return 実行に成功した場合true
   */
  public boolean runScript(String fileName, BhProgramEvent event) throws RemoteException;

  /** BunnyHopとの通信を切断する. */
  public void disconnect() throws RemoteException;

  /** BunnyHopとの通信を始める. */
  public void connect() throws RemoteException;

  /**
   * BhProgram の実行環境にメッセージを送信する.
   *
   * @param msg 送信するメッセージ. null不可.
   * @return 送信に成功した場合 true
   */
  public boolean sendMsgToScript(BhProgramMessage msg) throws RemoteException;

  /**
   * BhProgram の実行環境からレスポンスを受信する.
   *
   * @return 受信したレスポンス. 受信に失敗した場合もしくは受信可能なレスポンスがなかった場合 null.
   */
  public BhProgramResponse recvRespFromScript() throws RemoteException;

  /**
   * BhProgram の実行環境からメッセージを受信する.
   *
   * @return 受信したメッセージ. 受信に失敗した場合もしくは受信可能なメッセージがなかった場合 null.
   */
  public BhProgramMessage recvMsgFromScript() throws RemoteException;

  /**
   * BhProgram の実行環境にレスポンスを送信する.
   *
   * @param resp 送信するレスポンス. null不可.
   * @return 送信に成功した場合 true
   */
  public boolean sendRespToScript(BhProgramResponse resp) throws RemoteException;
}
