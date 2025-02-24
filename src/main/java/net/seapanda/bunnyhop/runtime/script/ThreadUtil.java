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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * スレッドに紐づくデータの取得と格納を行う機能を提供するクラス.
 *
 * @author K.Koike
 */
public class ThreadUtil {

  private final Map<Long, Object> threadIdToUserData = new ConcurrentHashMap<>();


  /** {@code data} をこのメソッドを呼び出したスレッド固有のデータとして保持する. */
  public void setThreadData(Object userData) {
    threadIdToUserData.put(Thread.currentThread().threadId(), userData);
  }

  /** {@link #storeThreadData} で保存したデータを取得する. */
  public Object getThreadData() {
    return threadIdToUserData.get(Thread.currentThread().threadId());
  }

  /** {@link #storeThreadData} で保存したデータを削除する. */
  public Object removeThreadData() {
    return threadIdToUserData.remove(Thread.currentThread().threadId());
  }  
}
