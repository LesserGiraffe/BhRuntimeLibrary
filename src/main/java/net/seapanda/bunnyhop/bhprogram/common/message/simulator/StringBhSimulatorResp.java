package net.seapanda.bunnyhop.bhprogram.common.message.simulator;

import java.util.Arrays;

/**
 * {@link net.seapanda.bunnyhop.bhprogram.common.message.simulator.BhSimulatorCmd} のレスポンス.
 *
 * @author K.Koike
 */
public class StringBhSimulatorResp extends BhSimulatorResp {

  private final String[] resp;

  public StringBhSimulatorResp(long id, boolean success, String[] resp) {
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
