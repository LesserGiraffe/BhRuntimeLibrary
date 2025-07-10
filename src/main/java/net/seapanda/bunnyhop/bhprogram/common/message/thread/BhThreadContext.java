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

package net.seapanda.bunnyhop.bhprogram.common.message.thread;

import java.util.ArrayList;
import java.util.SequencedCollection;
import net.seapanda.bunnyhop.bhprogram.common.BhSymbolId;
import net.seapanda.bunnyhop.bhprogram.common.BhThreadState;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.bhprogram.common.message.exception.BhProgramException;

/**
 * BhProgram のスレッドに関連する情報を格納するクラス.
 */
public class BhThreadContext implements BhProgramNotification {
 
  private final long msgId;
  /** コールスタック. */
  private final SequencedCollection<BhCallStackItem> callStack;
  /** スレッド ID. */
  private final long threadId;
  /* スレッドの状態. */
  private final BhThreadState state;
  /** スレッドで発生した例外. */
  private final BhProgramException exception;
  /** 次に実行する処理に対応するシンボルの ID. */
  private final BhSymbolId nextStep;
  
  /**
   * コンストラクタ.
   *
   * @param threadId スレッド ID 
   * @param state スレッドの状態
   */
  public BhThreadContext(long threadId, BhThreadState state) {
    this.threadId = threadId;
    this.state = state;
    this.callStack = new ArrayList<>();
    this.exception = null;
    this.nextStep = BhSymbolId.NONE;
    this.msgId = genId();
  }

  /**
   * コンストラクタ.
   *
   * @param threadId スレッド ID 
   * @param state スレッドの状態
   * @param callStack コールスタック.
   * @param exception スレッドで発生した例外
   */
  public BhThreadContext(
      long threadId,
      BhThreadState state,
      SequencedCollection<BhCallStackItem> callStack,
      BhProgramException exception) {
    this.threadId = threadId;
    this.state = state;
    this.callStack = new ArrayList<>(callStack);
    this.exception = exception;
    this.nextStep = BhSymbolId.NONE;
    this.msgId = genId();
  }

  /**
   * コンストラクタ.
   *
   * @param threadId スレッド ID 
   * @param state スレッドの状態
   * @param callStack コールスタック.
   * @param nextStep 次に実行する処理に対応するシンボルの ID
   */
  public BhThreadContext(
      long threadId,
      BhThreadState state,
      SequencedCollection<BhCallStackItem> callStack,
      BhSymbolId nextStep) {
    this.threadId = threadId;
    this.state = state;
    this.callStack = new ArrayList<>(callStack);
    this.exception = null;
    this.nextStep = nextStep;
    this.msgId = genId();
  }

  @Override
  public long getId() {
    return msgId;
  }

  /** スレッドの ID を取得する. */
  public long getThreadId() {
    return threadId;
  }

  /** スレッドの状態を取得する. */
  public BhThreadState getState() {
    return state;
  }

  /** コールスタックを取得する. */
  public SequencedCollection<BhCallStackItem> getCallStack() {
    return new ArrayList<>(callStack);
  }

  /**
   * スレッドで発生した例外を取得する.
   * 例外が発生していない場合は null.
   */
  public BhProgramException getException() {
    return exception;
  }

  /** 次に実行される処理の ID. */
  public BhSymbolId getNextStep() {
    return nextStep;
  }
}
