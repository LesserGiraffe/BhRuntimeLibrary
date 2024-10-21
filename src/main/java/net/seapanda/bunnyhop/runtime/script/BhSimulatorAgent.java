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

package net.seapanda.bunnyhop.runtime.script;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramMessage;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.DetectColorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.MeasureDistanceCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.MoveBackwardRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.MoveForwardRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.SetBothEyesColorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.SetLeftEyeColorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.SetRightEyeColorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.StopRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.TurnLeftRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.TurnRightRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.DetectColorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.MeasureDistanceResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.MoveBackwardRaspiCarResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.MoveForwardRaspiCarResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.SetBothEyesColorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.SetLeftEyeColorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.SetRightEyeColorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.StopRaspiCarResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.TurnLeftRaspiCarResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.TurnRightRaspiCarResp;
import net.seapanda.bunnyhop.runtime.tools.Util;

/**
 * BhSimulator の機能を実行するコマンドを発行するクラス.
 *
 * @author K.Koike
 */
public class BhSimulatorAgent {
  /** コマンドを格納する FIFO. */
  private final BlockingQueue<BhProgramMessage> sendMsgList;
  /** コマンド ID とその ID のコマンドの完了を待つための同期用オブジェクトのマップ. */
  private final Map<Integer, CountDownLatch> cmdIdToBarrier = new ConcurrentHashMap<>();
  /** コマンド ID とその ID のコマンドのレスポンスのマップ. */
  private final Map<Integer, BhSimulatorResp> cmdIdToResp = new ConcurrentHashMap<>();

  /**
   * コンストラクタ.
   *
   * @param sendMsgList 発行したコマンドを格納する FIFO
   */
  public BhSimulatorAgent(BlockingQueue<BhProgramMessage> sendMsgList) {
    this.sendMsgList = sendMsgList;
  }

  /**
   * RaspiCar を前進させるコマンドを発行する.
   *
   * @param speedLevel 速度レベル
   * @param time 前進する時間 (sec)
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public void moveForwardRaspiCar(double speedLevel, double time) throws AgencyFailedException {
    var cmd = new MoveForwardRaspiCarCmd(speedLevel, time);
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof MoveForwardRaspiCarResp && resp.success) {
      return;
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**
   * RaspiCar を後退させるコマンドを発行する.
   *
   * @param speedLevel 速度レベル
   * @param time 後退する時間 (sec)
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public void moveBackwardRaspiCar(double speedLevel, double time) throws AgencyFailedException {
    var cmd = new MoveBackwardRaspiCarCmd(speedLevel, time);
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof MoveBackwardRaspiCarResp && resp.success) {
      return;
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**
   * RaspiCar を右回転させるコマンドを発行する.
   *
   * @param speedLevel 速度レベル
   * @param time 右回転する時間 (sec)
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public void turnRightRaspiCar(double speedLevel, double time) throws AgencyFailedException {
    var cmd = new TurnRightRaspiCarCmd(speedLevel, time);
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof TurnRightRaspiCarResp && resp.success) {
      return;
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**
   * RaspiCar を左回転させるコマンドを発行する.
   *
   * @param speedLevel 速度レベル
   * @param time 左回転する時間 (sec)
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public void turnLeftRaspiCar(double speedLevel, double time) throws AgencyFailedException {
    var cmd = new TurnLeftRaspiCarCmd(speedLevel, time);
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof TurnLeftRaspiCarResp && resp.success) {
      return;
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**
   * RaspiCar を停止させるコマンドを発行する.
   *
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public void stopRaspiCar() throws AgencyFailedException {
    var cmd = new StopRaspiCarCmd();
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof StopRaspiCarResp && resp.success) {
      return;
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**
   * RaspiCar の距離センサの値を取得するコマンドを発行する.
   *
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public double measureDistance() throws AgencyFailedException {
    var cmd = new MeasureDistanceCmd();
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof MeasureDistanceResp measureDistanceResp && resp.success) {
      return measureDistanceResp.distance;
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**
   * RaspiCar の色センサの値を取得するコマンドを発行する.
   *
   * @return [赤の輝度値, 緑の輝度値, 青の輝度値]
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public int[] detectColor() throws AgencyFailedException {
    var cmd = new DetectColorCmd();
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof DetectColorResp detectColorResp && resp.success) {
      return new int[] { detectColorResp.red, detectColorResp.green, detectColorResp.blue };
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**detectColor
   * RaspiCar の左目の色を設定するコマンドを発行する.
   *
   * @param red 赤の輝度値
   * @param green 緑の輝度値
   * @param blue 青の輝度値
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public void setLeftEyeColor(int red, int green, int blue) throws AgencyFailedException {
    var cmd = new SetLeftEyeColorCmd(red, green, blue);
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof SetLeftEyeColorResp && resp.success) {
      return;
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**
   * RaspiCar の右目の色を設定するコマンドを発行する.
   *
   * @param red 赤の輝度値
   * @param green 緑の輝度値
   * @param blue 青の輝度値
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public void setRightEyeColor(int red, int green, int blue) throws AgencyFailedException {
    var cmd = new SetRightEyeColorCmd(red, green, blue);
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof SetRightEyeColorResp && resp.success) {
      return;
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /**
   * RaspiCar の両目の色を設定するコマンドを発行する.
   *
   * @param red 赤の輝度値
   * @param green 緑の輝度値
   * @param blue 青の輝度値
   * @throws AgencyFailedException コマンドの実行が失敗した
   */
  public void setBothEyesColor(int red, int green, int blue) throws AgencyFailedException {
    var cmd = new SetBothEyesColorCmd(red, green, blue);
    sendCmdAndWait(cmd);
    BhSimulatorResp resp = cmdIdToResp.remove(cmd.getId());
    if (resp instanceof SetBothEyesColorResp && resp.success) {
      return;
    }
    throw new AgencyFailedException(Util.INSTANCE.getCurrentMethodName() + " failed");
  }

  /** コマンドを送って応答を待つ. */
  private void sendCmdAndWait(BhSimulatorCmd cmd) {
    var isAdded = false;
    var latch = new CountDownLatch(1);
    cmdIdToBarrier.put(cmd.getId(), latch);
    try {
      isAdded = sendMsgList.offer(cmd, Long.MAX_VALUE, TimeUnit.DAYS);
      if (isAdded) {
        latch.await();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      cmdIdToBarrier.remove(cmd.getId());
    }
  }

  /**
   * このオブジェクトにコマンドのレスポンスを渡す.
   *
   * @param resp コマンドのレスポンス.
   */
  public void notify(BhSimulatorResp resp) {
    cmdIdToResp.put(resp.getId(), resp);
    CountDownLatch latch = cmdIdToBarrier.remove(resp.getId());
    if (latch != null) {
      latch.countDown();
    }
  }
}
