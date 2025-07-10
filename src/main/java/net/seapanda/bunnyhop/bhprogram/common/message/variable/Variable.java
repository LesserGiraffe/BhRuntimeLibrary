package net.seapanda.bunnyhop.bhprogram.common.message.variable;

import java.io.Serializable;
import net.seapanda.bunnyhop.bhprogram.common.BhSymbolId;

/**
 * スカラ変数の情報を格納するクラスの基底クラス.
 *
 * @author K.Koike
 */
public abstract class Variable implements Serializable {
  
  /** 変数のシンボル ID. */
  public final BhSymbolId varId;

  Variable(BhSymbolId varId) {
    this.varId = varId;
  }
}
