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

import java.util.Arrays;

/**
 * {@link BhSimulatorCmd} のレスポンス.
 *
 * @author K.Koike
 */
public abstract class BhSimulatorResp implements BhProgramResponse {

  private final long id;
  public final boolean success;

  private BhSimulatorResp(long id, boolean success) {
    this.id = id;
    this.success = success;
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public boolean isSuccessful() {
    return success;
  }

  /** 文字列の配列として返される {@link BhSimulatorCmd} のレスポンス. */
  public static class StrBhSimulatorResp extends BhSimulatorResp {
    private String[] resp;

    public StrBhSimulatorResp(long id, boolean success, String[] resp) {
      super(id, success);
      this.resp = Arrays.copyOf(resp, resp.length);
    }

    /** レスポンスを構成する要素を返す. */
    public String[] getComponents() {
      return Arrays.copyOf(resp, resp.length);
    }

    @Override
    public String toString() {
      return String.join(",", resp);
    }
  }
}
