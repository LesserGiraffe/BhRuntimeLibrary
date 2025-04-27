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
import net.seapanda.bunnyhop.bhprogram.common.BhNodeInstanceId;
import net.seapanda.bunnyhop.bhprogram.common.message.BhCallStackItem;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramException;
import net.seapanda.bunnyhop.utility.SynchronizingTimer;

/**
 * BhProgram が使用するオブジェクトを生成する機能を提供するクラス.
 *
 * @author K.Koike
 */
public class Factory {
  
  /**
   * {@link BhNodeInstanceId} オブジェクトを作成して返す.
   *
   * @param id 作成するIDの文字列表現
   * @return {@link BhNodeInstanceId} オブジェクト
   */
  public BhNodeInstanceId newBhNodeInstanceId(String id) {
    return BhNodeInstanceId.of(id);
  }

  /**
   * {@link BhProgramException} オブジェクトを作成して返す.
   *
   * @param callStack 例外発生時のコールスタック
   * @param msg 例外メッセージ
   * @return 例外オブジェクト
   */
  public BhProgramException newBhProgramException(List<?> callStack, String msg) {
    return newBhProgramException(callStack, msg, null);
  }

  /**
   * {@link BhProgramException} オブジェクトを作成して返す.
   *
   * @param callStack 例外発生時のコールスタック
   * @param msg 例外メッセージ
   * @param cause 例外の原因
   * @return 例外オブジェクト
   */
  public BhProgramException newBhProgramException(
      List<?> callStack, String msg, Throwable cause) {
    var funcCallStack = new ArrayList<BhCallStackItem>();
    long id = 0;
    for (var elem : callStack) {
      var item = new BhCallStackItem(id++, newBhNodeInstanceId(elem.toString()));
      funcCallStack.add(item);  
    }
    return new BhProgramException(funcCallStack, msg, Thread.currentThread().threadId(), cause);
  }

  /** {@link SynchronizingTimer} を新規作成する. */
  public SynchronizingTimer newSyncTimer(int count, boolean autoReset) {
    return new SynchronizingTimer(count, autoReset);
  }
}
