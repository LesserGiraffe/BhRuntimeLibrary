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


/**
 * BhSimulator の制御 API を定義したインタフェース.
 *
 * @author K.Koike
 */
public interface BhSimulatorCtrl {

  /**
   * RaspiCar を前進させる.
   *
   * @param speedLevel 速度レベル
   * @param time 前進する時間 (sec)
   * @throws Exception シミュレータの制御に失敗した
   */
  public void moveForwardRaspiCar(double speedLevel, double time) throws Exception;

  /**
   * RaspiCar を後退させる.
   *
   * @param speedLevel 速度レベル
   * @param time 後退する時間 (sec)
   * @throws Exception シミュレータの制御に失敗した
   */
  public void moveBackwardRaspiCar(double speedLevel, double time) throws Exception;

  /**
   * RaspiCar を右回転させる.
   *
   * @param speedLevel 速度レベル
   * @param time 右回転する時間 (sec)
   * @throws Exception シミュレータの制御に失敗した
   */
  public void turnRightRaspiCar(double speedLevel, double time) throws Exception;

  /**
   * RaspiCar を左回転させる.
   *
   * @param speedLevel 速度レベル
   * @param time 左回転する時間 (sec)
   * @throws Exception シミュレータの制御に失敗した
   */
  public void turnLeftRaspiCar(double speedLevel, double time) throws Exception;

  /**
   * RaspiCar を停止させる.
   *
   * @throws Exception シミュレータの制御に失敗した
   */
  public void stopRaspiCar() throws Exception;

  /**
   * RaspiCar の距離センサの値を取得する.
   *
   * @throws Exception シミュレータの制御に失敗した
   */
  public double measureDistance() throws Exception;

  /**
   * RaspiCar の色センサの値を取得する.
   *
   * @return [赤の輝度値, 緑の輝度値, 青の輝度値]
   * @throws Exception シミュレータの制御に失敗した
   */
  public int[] detectColor() throws Exception;

  /**
   * RaspiCar の左目の色を設定する.
   *
   * @param red 赤の輝度値
   * @param green 緑の輝度値
   * @param blue 青の輝度値
   * @throws Exception シミュレータの制御に失敗した
   */
  public void setLeftEyeColor(int red, int green, int blue) throws Exception;

  /**
   * RaspiCar の右目の色を設定する.
   *
   * @param red 赤の輝度値
   * @param green 緑の輝度値
   * @param blue 青の輝度値
   * @throws Exception シミュレータの制御に失敗した
   */
  public void setRightEyeColor(int red, int green, int blue) throws Exception;

  /**
   * RaspiCar の両目の色を設定する.
   *
   * @param red 赤の輝度値
   * @param green 緑の輝度値
   * @param blue 青の輝度値
   * @throws Exception シミュレータの制御に失敗した
   */
  public void setBothEyesColor(int red, int green, int blue) throws Exception;
}
