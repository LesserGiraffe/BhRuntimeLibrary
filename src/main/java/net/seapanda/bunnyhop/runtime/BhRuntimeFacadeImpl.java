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

package net.seapanda.bunnyhop.runtime;

import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.seapanda.bunnyhop.bhprogram.common.BhRuntimeFacade;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhTextIoResp;
import net.seapanda.bunnyhop.runtime.executor.BhProgramExecutor;
import net.seapanda.bunnyhop.runtime.script.BhProgramMessageProcessor;
import net.seapanda.bunnyhop.runtime.script.MessageQueueSet;

/**
 * スクリプトと BunnyHop 間でデータを送受信するクラス.
 *
 * @author K.Koike
 */
public class BhRuntimeFacadeImpl implements BhRuntimeFacade {

  

  /** BunnyHop との通信が有効な場合 true. */
  private boolean connected = false;
  private final MessageQueueSet queueSet;
  private final BhProgramExecutor executor;
  private final BhProgramMessageProcessor<BhTextIoCmd> textIoCmdProcessor;
  private final BhProgramMessageProcessor<BhTextIoResp> textIoRespProcessor;
  private final BhProgramMessageProcessor<BhSimulatorCmd> simCmdProcessor;
  private final BhProgramMessageProcessor<BhSimulatorResp> simRespProcessor;

  /** BunnyHop から受信したメッセージを処理する Executor. */
  private final ExecutorService recvMsgProcessor = Executors.newSingleThreadExecutor();
  /** BunnyHop から受信したメッセージを処理する Executor. */
  private final ExecutorService recvRespProcessor = Executors.newSingleThreadExecutor();

  private final EventManager eventManager = this.new EventManager();

  /** コンストラクタ. */
  public BhRuntimeFacadeImpl(
      MessageQueueSet queueSet,
      BhProgramExecutor executor,
      BhProgramMessageProcessor<BhTextIoCmd> textIoCmdProcessor,
      BhProgramMessageProcessor<BhTextIoResp> textIoRespProcessor,
      BhProgramMessageProcessor<BhSimulatorCmd> simCmdProcessor,
      BhProgramMessageProcessor<BhSimulatorResp> simRespProcessor) {
    this.queueSet = queueSet;
    this.executor = executor;
    this.textIoCmdProcessor = textIoCmdProcessor;
    this.textIoRespProcessor = textIoRespProcessor;
    this.simCmdProcessor = simCmdProcessor;
    this.simRespProcessor = simRespProcessor;
    recvMsgProcessor.submit(() -> processRecvMsg());
    recvRespProcessor.submit(() -> processRecvResp());
  }

  @Override
  public boolean runScript(String fileName, BhProgramEvent event) {
    return executor.runScript(fileName, event);
  }

  @Override
  public boolean sendNotifToRuntime(BhProgramNotification notif) {
    try {
      return queueSet.recvNotifList().offer(
          notif, BhConstants.PUSH_MSG_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return false;
  }

  @Override
  public BhProgramResponse recvRespFromRuntime() {
    try {
      return queueSet.sendRespList().poll(BhConstants.POP_MSG_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return null;
  }

  @Override
  public BhProgramNotification recvNotifFromRuntime() {
    try {
      return queueSet.sendNotifList().poll(BhConstants.POP_MSG_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return null;
  }

  @Override
  public boolean sendRespToRuntime(BhProgramResponse resp) {
    try {
      return queueSet.recvRespList().offer(
          resp, BhConstants.PUSH_MSG_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) { /* do nothing */ }
    return false;
  }

  @Override
  public synchronized void connect() {
    connected = true;
    eventManager.invokeOnConnected();
  }

  @Override
  public synchronized void disconnect() {
    connected = false;
    eventManager.invokeOnDisconnected();
  }

  @Override
  public synchronized boolean isConnected() {
    return connected;
  }

  /** BunnyHop から受信したメッセージを処理し続ける. */
  private void processRecvMsg() {
    while (true) {
      BhProgramNotification notif = null;
      try {
        notif = queueSet.recvNotifList().take();
      } catch (InterruptedException e) {
        break;
      }
      switch (notif) {
        case BhTextIoCmd textIoCmd -> textIoCmdProcessor.process(textIoCmd);
        case BhSimulatorCmd simCmd -> simCmdProcessor.process(simCmd);
        case BhProgramEvent event -> executor.fireEvent(event);
        default -> { }
      }
    }
  }

  /** BunnyHop から受信したレスポンスを処理し続ける. */
  private void processRecvResp() {
    while (true) {
      BhProgramResponse resp = null;
      try {
        resp = queueSet.recvRespList().take();
      } catch (InterruptedException e) {
        break;
      }
      switch (resp) {
        case BhTextIoResp textIoResp -> textIoRespProcessor.process(textIoResp);
        case BhSimulatorResp simResp -> simRespProcessor.process(simResp);
        default -> { }
      }
    }
  }

  /**
   * このオブジェクトに対するイベントハンドラの追加と削除を行うオブジェクトを返す.
   *
   * @return このオブジェクトに対するイベントハンドラの追加と削除を行うオブジェクト
   */
  public EventManager getEventManager() {
    return eventManager;
  }

  /** イベントハンドラの管理を行うクラス. */
  public class EventManager {

    /** このオブジェクトと BunnyHop の通信が有効化されたときに呼び出されるメソッドのリスト. */
    private SequencedSet<Runnable> onConnected = new LinkedHashSet<>();
    /** このオブジェクトと BunnyHop の通信が無効化されたときに呼び出されるメソッドのリスト. */
    private SequencedSet<Runnable> onDisconnected = new LinkedHashSet<>();

    /**
     * このオブジェクトと BunnyHop の通信が有効化されたときのイベントハンドラを追加する.
     *
     * @param handler 追加するイベントハンドラ
     */
    public void addOnConnected(Runnable handler) {
      onConnected.addLast(handler);
    }

    /**
     * このオブジェクトと BunnyHop の通信が有効化されたときのイベントハンドラを削除する.
     *
     * @param handler 追加するイベントハンドラ
     */
    public void removeOnConnected(Runnable handler) {
      onConnected.remove(handler);
    }

    /** このオブジェクトと BunnyHop の通信が有効化されたときのイベントハンドラを呼び出す. */
    private void invokeOnConnected() {
      onConnected.forEach(Runnable::run);
    }

    /**
     * このオブジェクトと BunnyHop の通信が無効化されたときのイベントハンドラを追加する.
     *
     * @param handler 追加するイベントハンドラ
     */
    public void addOnDisconnected(Runnable handler) {
      onDisconnected.addLast(handler);
    }

    /**
     * このオブジェクトと BunnyHop の通信が無効化されたときのイベントハンドラを削除する.
     *
     * @param handler 追加するイベントハンドラ
     */
    public void removeOnDisconnected(Runnable handler) {
      onDisconnected.remove(handler);
    }

    /** このオブジェクトと BunnyHop の通信が無効化されたときのイベントハンドラを呼び出す. */
    private void invokeOnDisconnected() {
      onDisconnected.forEach(Runnable::run);
    }
  }  
}
