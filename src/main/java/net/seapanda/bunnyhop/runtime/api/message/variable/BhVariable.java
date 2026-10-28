package net.seapanda.bunnyhop.runtime.api.message.variable;

import java.io.Serializable;
import net.seapanda.bunnyhop.runtime.api.message.BhSymbolId;

/**
 * 変数の情報を格納するクラスの基底クラス.
 *
 * @author K.Koike
 */
public abstract class BhVariable implements Serializable {
  
  /** 変数のシンボル ID. */
  public final BhSymbolId id;

  BhVariable(BhSymbolId id) {
    this.id = id;
  }
}
