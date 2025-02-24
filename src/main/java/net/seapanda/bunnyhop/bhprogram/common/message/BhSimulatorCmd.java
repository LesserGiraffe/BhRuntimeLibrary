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

package net.seapanda.bunnyhop.bhprogram.common.message;

/**
 * BhSimulator が処理するコマンド.
 *
 * @author K.Koike
 */
public abstract class BhSimulatorCmd implements BhProgramNotification {

  private final long id;

  private BhSimulatorCmd() {
    id = genId();
  }

  @Override
  public long getId() {
    return id;
  }

  /** RaspiCar を動作させるコマンドの基底クラス. */
  public abstract static class MoveRaspiCarCmd extends BhSimulatorCmd {
    /** 速度レベル. */
    public final float speedLevel;
    /** 動作時間 (sec). */
    public final float time;

    private MoveRaspiCarCmd(double speedLevel, double time) {
      this.speedLevel = (float) speedLevel;
      this.time = (float) time;
    }
  }

  /** RaspiCar を前進させるコマンド. */
  public static class MoveForwardRaspiCarCmd extends MoveRaspiCarCmd {

    public MoveForwardRaspiCarCmd(double speedLevel, double time) {
      super(speedLevel, time);
    }
  }

  /** RaspiCar を後退させるコマンド. */
  public static class MoveBackwardRaspiCarCmd extends MoveRaspiCarCmd {

    public MoveBackwardRaspiCarCmd(double speedLevel, double time) {
      super(speedLevel, time);
    }
  }

  /** RaspiCar を右回転させるコマンド. */
  public static class TurnRightRaspiCarCmd extends MoveRaspiCarCmd {

    public TurnRightRaspiCarCmd(double speedLevel, double time) {
      super(speedLevel, time);
    }
  }

  /** RaspiCar を左回転させるコマンド. */
  public static class TurnLeftRaspiCarCmd extends MoveRaspiCarCmd {
    
    public TurnLeftRaspiCarCmd(double speedLevel, double time) {
      super(speedLevel, time);
    }
  }

  /** RaspiCar を停止させるコマンド. */
  public static class StopRaspiCarCmd extends BhSimulatorCmd {
    
    public StopRaspiCarCmd() {}
  }

  /** RaspiCar の距離センサの値を取得するコマンド. */
  public static class MeasureDistanceCmd extends BhSimulatorCmd {
    
    public MeasureDistanceCmd() {}
  }

  /** RaspiCar の色センサの値を取得するコマンド. */
  public static class DetectColorCmd extends BhSimulatorCmd {
    
    public DetectColorCmd() {}
  }

  /** 色を設定するコマンドの基底クラス. */
  public abstract static class SetColorCmd extends BhSimulatorCmd {
    public final float red;
    public final float green;
    public final float blue;

    /** コンストラクタ. */
    public SetColorCmd(int red, int green, int blue) {
      this.red = Math.clamp((float) (red / 255f), 0f, 1f);
      this.green = Math.clamp((float) (green / 255f), 0f, 1f);
      this.blue = Math.clamp((float) (blue / 255f), 0f, 1f);
    }
  }

  /** RaspiCar の左目の色を設定するコマンド. */
  public static class SetLeftEyeColorCmd extends  SetColorCmd {
    
    public SetLeftEyeColorCmd(int red, int green, int blue) {
      super(red, green, blue);
    }
  }

  /** RaspiCar の右目の色を設定するコマンド. */
  public static class SetRightEyeColorCmd extends  SetColorCmd {
    
    public SetRightEyeColorCmd(int red, int green, int blue) {
      super(red, green, blue);
    }
  }

  /** RaspiCar の両目の色を設定するコマンド. */
  public static class SetBothEyesColorCmd extends  SetColorCmd {
  
    public SetBothEyesColorCmd(int red, int green, int blue) {
      super(red, green, blue);
    }
  }
}
