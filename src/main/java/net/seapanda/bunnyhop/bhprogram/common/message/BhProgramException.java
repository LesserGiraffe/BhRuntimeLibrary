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

import java.util.Deque;
import java.util.LinkedList;
import java.util.SequencedCollection;

/**
 * BhProgam 実行時に発生した例外情報を保持するクラス.
 *
 * @author K.Koike
 */
public class BhProgramException extends RuntimeException implements BhProgramNotification {

  private final Deque<BhCallStackItem> callStack;
  private final long threadId;
  private final long msgId;

  /**
   * コンストラクタ.
   *
   * @param callStack 例外が発生した時のコールスタック
   * @param msg 例外メッセージ
   * @param threadId スレッド ID
   */
  public BhProgramException(
      SequencedCollection<BhCallStackItem> callStack, String msg, long threadId) {
    this(callStack, msg, threadId, null);
  }

  /**
   * コンストラクタ.
   *
   * @param callStack 例外が発生した時のコールスタック
   * @param msg 例外メッセージ
   * @param threadId 例外を出したスレッドの ID
   * @param cause この例外を引き起こした原因
   */
  public BhProgramException(
        SequencedCollection<BhCallStackItem> callStack,
        String msg,
        long threadId,
        Throwable cause) {
    super(msg, cause);
    this.callStack = new LinkedList<>(callStack);
    this.threadId = threadId;
    msgId = genId();
  }

  /** 例外が発生した時のコールスタックを取得する. */
  public SequencedCollection<BhCallStackItem> getCallStack() {
    return new LinkedList<>(callStack);
  }

  /**
   * 例外を発生させたスレッドの ID を取得する.
   * この情報が無い場合, 負の値を返す.
   */
  public long getThreadId() {
    return threadId;
  }

  @Override
  public long getId() {
    return msgId;
  }
}
