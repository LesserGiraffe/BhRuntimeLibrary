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
import net.seapanda.bunnyhop.bhprogram.common.BhNodeInstanceId;

/**
 * BhProgam 実行時に発生した例外情報を保持するクラス.
 *
 * @author K.Koike
 */
public class BhProgramException extends RuntimeException implements BhProgramMessage {

  private final Deque<BhNodeInstanceId> callStack;
  private final String scriptEngineMsg;
  private final long threadId;
  private final long msgId;

  /**
   * コンストラクタ.
   *
   * @param callStack 例外が発生した時のコールスタック
   * @param msg 例外メッセージ
   */
  public BhProgramException(SequencedCollection<BhNodeInstanceId> callStack, String msg) {
    this(callStack, msg, "", -1);
  }

  /**
   * コンストラクタ.
   *
   * @param callStack 例外が発生した時のコールスタック
   * @param msg 例外メッセージ
   * @param scriptEngineMsg BhProgram の実行エンジンから返されたエラーメッセージ
   */
  public BhProgramException(
        SequencedCollection<BhNodeInstanceId> callStack, String msg, String scriptEngineMsg) {
    this(callStack, msg, scriptEngineMsg, -1);
  }

  /**
   * コンストラクタ.
   *
   * @param callStack 例外が発生した時のコールスタック
   * @param msg 例外メッセージ
   * @param scriptEngineMsg BhProgram の実行エンジンから返されたエラーメッセージ
   * @param threadId 例外を出したスレッドの ID
   */
  public BhProgramException(
        SequencedCollection<BhNodeInstanceId> callStack,
        String msg,
        String scriptEngineMsg,
        long threadId) {
    super(msg);
    this.callStack = new LinkedList<>(callStack);
    this.scriptEngineMsg = scriptEngineMsg;
    this.threadId = threadId;
    msgId = genId();
  }

  /** BhProgram の実行エンジンから返されたエラーメッセージを取得する. */
  public String getScriptEngineMsg() {
    return scriptEngineMsg;
  }

  /** 例外が発生した時のコールスタックを取得する. */
  public SequencedCollection<BhNodeInstanceId> getCallStack() {
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
