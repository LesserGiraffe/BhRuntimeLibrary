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

/** BunnyHop と BhProgram の間で送受信されるイベント. */
public class BhProgramEvent implements BhProgramMessage {

  /** イベント名. */
  public final Name name;
  /** イベントに対応する BhProgram の関数を解決する関数の名前. */
  public final String eventHandlerResolver;
  private final long id;

  /**
   * コンストラクタ.
   *
   * @param name イベント名
   * @param eventHandlerResolver {@code name} に指定したイベントから呼び出すべき BhProgram の関数を解決する関数の名前.
   */
  public BhProgramEvent(Name name, String eventHandlerResolver) {
    this.name = name;
    this.eventHandlerResolver = eventHandlerResolver;
    id = genId();
  }

  @Override
  public long getId() {
    return id;
  }

  /** イベント名一覧. */
  public enum Name {
    KEY_DIGIT0_PRESSED("KEY_DIGIT0_PRESSED"),
    KEY_DIGIT1_PRESSED("KEY_DIGIT1_PRESSED"),
    KEY_DIGIT2_PRESSED("KEY_DIGIT2_PRESSED"),
    KEY_DIGIT3_PRESSED("KEY_DIGIT3_PRESSED"),
    KEY_DIGIT4_PRESSED("KEY_DIGIT4_PRESSED"),
    KEY_DIGIT5_PRESSED("KEY_DIGIT5_PRESSED"),
    KEY_DIGIT6_PRESSED("KEY_DIGIT6_PRESSED"),
    KEY_DIGIT7_PRESSED("KEY_DIGIT7_PRESSED"),
    KEY_DIGIT8_PRESSED("KEY_DIGIT8_PRESSED"),
    KEY_DIGIT9_PRESSED("KEY_DIGIT9_PRESSED"),
    KEY_ENTER_PRESSED("KEY_ENTER_PRESSED"),
    KEY_SPACE_PRESSED("KEY_SPACE_PRESSED"),
    KEY_SHIFT_PRESSED("KEY_SHIFT_PRESSED"),
    KEY_CTRL_PRESSED("KEY_CTRL_PRESSED"),
    KEY_RIGHT_PRESSED("KEY_RIGHT_PRESSED"),
    KEY_LEFT_PRESSED("KEY_LEFT_PRESSED"),
    KEY_UP_PRESSED("KEY_UP_PRESSED"),
    KEY_DOWN_PRESSED("KEY_DOWN_PRESSED"),
    KEY_A_PRESSED("KEY_A_PRESSED"),
    KEY_B_PRESSED("KEY_B_PRESSED"),
    KEY_C_PRESSED("KEY_C_PRESSED"),
    KEY_D_PRESSED("KEY_D_PRESSED"),
    KEY_E_PRESSED("KEY_E_PRESSED"),
    KEY_F_PRESSED("KEY_F_PRESSED"),
    KEY_G_PRESSED("KEY_G_PRESSED"),
    KEY_H_PRESSED("KEY_H_PRESSED"),
    KEY_I_PRESSED("KEY_I_PRESSED"),
    KEY_J_PRESSED("KEY_J_PRESSED"),
    KEY_K_PRESSED("KEY_K_PRESSED"),
    KEY_L_PRESSED("KEY_L_PRESSED"),
    KEY_M_PRESSED("KEY_M_PRESSED"),
    KEY_N_PRESSED("KEY_N_PRESSED"),
    KEY_O_PRESSED("KEY_O_PRESSED"),
    KEY_P_PRESSED("KEY_P_PRESSED"),
    KEY_Q_PRESSED("KEY_Q_PRESSED"),
    KEY_R_PRESSED("KEY_R_PRESSED"),
    KEY_S_PRESSED("KEY_S_PRESSED"),
    KEY_T_PRESSED("KEY_T_PRESSED"),
    KEY_U_PRESSED("KEY_U_PRESSED"),
    KEY_V_PRESSED("KEY_V_PRESSED"),
    KEY_W_PRESSED("KEY_W_PRESSED"),
    KEY_X_PRESSED("KEY_X_PRESSED"),
    KEY_Y_PRESSED("KEY_Y_PRESSED"),
    KEY_Z_PRESSED("KEY_Z_PRESSED"),
    /** プログラムの開始時に必ず1回発行されるイベント. */
    PROGRAM_START("PROGRAM_START");

    private final String name;

    private Name(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
