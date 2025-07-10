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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.seapanda.bunnyhop.bhprogram.common.BhSymbolId;
import org.mozilla.javascript.NativeObject;

/**
 * BhProgram が操作するスレッドコンテキストを Java から参照するためのラッパークラス.
 *
 * <p>スレッドコンテキスト : 特定のスレッドに紐づくデータ一式
 *
 * @author K.Koike
 */
public class ScriptThreadContext {
  
  private final long threadId;
  private final List<?> context;
  private final List<?> callStack;
  private final List<?> errMsgs;
  private final List<?> varStack;
  private final int idxNextNodeInstId;

  /**
   * コンストラクタ.
   *
   * @param threadId このスレッドコンテキストと紐づくスレッドの ID
   * @param context スレッドコンテキストの各情報が格納された配列
   * @param idxCallStack {@code context} の中でコールスタックが格納された場所のインデックス
   * @param idxNextNodeInstId {@code context} の中で次の処理の ID が格納された場所のインデックス
   * @param idxErrorMsgs {@code context} の中でエラーメッセージが格納された場所のインデックス
   * @param idxVarStack {@code context} の中で変数スタックが格納された場所のインデックス
   */
  public ScriptThreadContext(
      long threadId,
      List<?> context, 
      int idxCallStack,
      int idxNextNodeInstId,
      int idxErrorMsgs,
      int idxVarStack) {
    this.threadId = threadId;
    this.context = context;
    this.callStack = (List<?>) context.get(idxCallStack);
    this.errMsgs = (List<?>) context.get(idxErrorMsgs);
    this.varStack = (List<?>) context.get(idxVarStack);
    this.idxNextNodeInstId = idxNextNodeInstId;
  }

  /** このスレッドコンテキストと紐づくスレッドの ID を返す. */
  public long getThreadId() {
    return threadId;
  }

  /**
   * コールスタックのコピーを返す.
   *
   * <p>コールスタック : 各関数呼び出しに対応するシンボルの ID を格納するスタック
   *
   * @return コールスタックのコピー
   */
  public List<BhSymbolId> getCallStack() {
    return callStack.stream()
        .map(item -> BhSymbolId.of(item.toString()))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /** コールスタックの要素数を返す. */
  public int getCallStackSize() {
    return callStack.size();
  }

  /** {@code idx} で指定した変数スタックのスタックフレームを返す. */
  public List<NativeObject> getVarStackFrame(int idx) {
    var frame = (List<?>) varStack.get(idx);
    return frame.stream()
        .filter(variable -> variable instanceof NativeObject)
        .map(variable -> (NativeObject) variable)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /** 変数スタックの要素数を返す. */
  public int getVarStackSize() {
    return varStack.size();    
  }

  /** エラーメッセージのリストを返す. */
  public List<String> getErrorMessages() {
    return errMsgs.stream()
        .map(Object::toString)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /** 次に実行するノードの ID を取得する. */
  public BhSymbolId getNextNodeInstanceId() {
    Object nextNodeInstId = context.get(idxNextNodeInstId);
    if (nextNodeInstId == null) {
      return BhSymbolId.NONE;
    }
    return BhSymbolId.of(nextNodeInstId.toString());
  }

  /** スレッドコンテキストの各情報が格納された配列を返す. */
  public List<?> getRaw() {
    return context;
  }
}
