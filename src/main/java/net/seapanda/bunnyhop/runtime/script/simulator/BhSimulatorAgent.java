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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.bhprogram.common.message.simulator.BhSimulatorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.simulator.BhSimulatorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.simulator.StringBhSimulatorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.simulator.StringBhSimulatorResp;
import net.seapanda.bunnyhop.runtime.script.AgencyFailedException;
import net.seapanda.bunnyhop.runtime.script.BhProgramMessageProcessor;
import net.seapanda.bunnyhop.utility.Utility;

/**
 * BhSimulator の機能を実行するコマンドを発行するクラス.
 *
 * @author K.Koike
 */
public class BhSimulatorAgent implements
    BhSimulatorCtrl, BhProgramMessageProcessor<BhSimulatorResp> {
  /** コマンドを格納する FIFO. */
  private final BlockingQueue<BhProgramNotification> sendNotifList;
  /** コマンド ID とその ID のコマンドの完了を待つための同期用オブジェクトのマップ. */
  private final Map<Long, CountDownLatch> cmdIdToBarrier = new ConcurrentHashMap<>();
  /** コマンド ID とその ID のコマンドのレスポンスのマップ. */
  private final Map<Long, BhSimulatorResp> cmdIdToResp = new ConcurrentHashMap<>();

  /**
   * コンストラクタ.
   *
   * @param sendNotifList 発行したコマンドを格納する FIFO
   */
  public BhSimulatorAgent(BlockingQueue<BhProgramNotification> sendNotifList) {
    this.sendNotifList = sendNotifList;
  }

  @Override
  public String[] sendCmd(String... cmd) throws AgencyFailedException {
    var command = new StringBhSimulatorCmd(cmd);
    sendCmdAndWait(command);
    BhSimulatorResp resp = cmdIdToResp.remove(command.getId());
    if (resp instanceof StringBhSimulatorResp strResp && resp.isSuccessful()) {
      return strResp.getComponents();
    }
    throw new AgencyFailedException(
        "%s failed.\n(%s)".formatted(Utility.getCurrentMethodName(), resp));
  }

  /** コマンドを送って応答を待つ. */
  private void sendCmdAndWait(BhSimulatorCmd cmd) {
    var isAdded = false;
    var latch = new CountDownLatch(1);
    cmdIdToBarrier.put(cmd.getId(), latch);
    try {
      isAdded = sendNotifList.offer(cmd, Long.MAX_VALUE, TimeUnit.DAYS);
      if (isAdded) {
        latch.await();
      }
    } catch (InterruptedException e) {
      cmdIdToBarrier.remove(cmd.getId());
    }
  }

  @Override
  public void process(BhSimulatorResp resp) {
    CountDownLatch latch = cmdIdToBarrier.remove(resp.getId());
    if (latch != null) {
      cmdIdToResp.put(resp.getId(), resp);
      latch.countDown();
    }
  }
}
