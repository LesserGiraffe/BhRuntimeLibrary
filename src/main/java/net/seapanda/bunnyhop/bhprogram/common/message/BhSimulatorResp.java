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
 * {@link BhSimulatorCmd} のレスポンス.
 *
 * @author K.Koike
 */
public abstract class BhSimulatorResp implements BhProgramResponse {

  private final int id;
  public final boolean success;

  private BhSimulatorResp(int id, boolean success) {
    this.id = id;
    this.success = success;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public boolean isSuccessful() {
    return success;
  }

  /** RaspiCar を前進させるコマンドのレスポンス. */
  public static class MoveForwardRaspiCarResp extends BhSimulatorResp {

    public MoveForwardRaspiCarResp(int id, boolean success) {
      super(id, success);
    }
  }

  /** RaspiCar を後退させるコマンドのレスポンス. */
  public static class MoveBackwardRaspiCarResp extends BhSimulatorResp {

    public MoveBackwardRaspiCarResp(int id, boolean success) {
      super(id, success);
    }
  }

  /** RaspiCar を右回転させるコマンドのレスポンス. */
  public static class TurnRightRaspiCarResp extends BhSimulatorResp {

    public TurnRightRaspiCarResp(int id, boolean success) {
      super(id, success);
    }
  }

  /** RaspiCar を左回転させるコマンドのレスポンス. */
  public static class TurnLeftRaspiCarResp extends BhSimulatorResp {
    
    public TurnLeftRaspiCarResp(int id, boolean success) {
      super(id, success);
    }
  }

  /** RaspiCar を停止させるコマンドのレスポンス. */
  public static class StopRaspiCarResp extends BhSimulatorResp {
    
    public StopRaspiCarResp(int id, boolean success) {
      super(id, success);
    }
  }

  /** RaspiCar の距離センサの値を取得するコマンドのレスポンス. */
  public static class MeasureDistanceResp extends BhSimulatorResp {
    
    public final double distance;

    public MeasureDistanceResp(int id, boolean success, double distance) {
      super(id, success);
      this.distance = distance;
    }
  }

  /** RaspiCar の色センサの値を取得するコマンドのレスポンス. */
  public static class DetectColorResp extends BhSimulatorResp {
    public final int red;
    public final int green;
    public final int blue;

    /** コンストラクタ. */
    public DetectColorResp(
        int id, boolean success, float red, float green, float blue) {
      super(id, success);
      this.red = Math.clamp((int) (red * 255), 0, 255);
      this.green = Math.clamp((int) (green * 255), 0, 255);
      this.blue = Math.clamp((int) (blue * 255), 0, 255);
    }
  }

  /** RaspiCar の左目の色を設定するコマンドのレスポンス. */
  public static class SetLeftEyeColorResp extends  BhSimulatorResp {
    
    public SetLeftEyeColorResp(int id, boolean success) {
      super(id, success);
    }
  }

  /** RaspiCar の右目の色を設定するコマンドのレスポンス. */
  public static class SetRightEyeColorResp extends  BhSimulatorResp {
    
    public SetRightEyeColorResp(int id, boolean success) {
      super(id, success);
    }
  }

  /** RaspiCar の両目の色を設定するコマンド. */
  public static class SetBothEyesColorResp extends  BhSimulatorResp {
  
    public SetBothEyesColorResp(int id, boolean success) {
      super(id, success);
    }
  }
}
