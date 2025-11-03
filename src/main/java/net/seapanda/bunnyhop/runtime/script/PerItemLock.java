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

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link U} 型のオブジェクトごとにロックの取得と解放を行うためのクラス.
 *
 * <p>ロックを確保するメソッドと解放するメソッドを呼び出す際に指定した {@link U} 型のオブジェクトが
 * {@link Object#equals} で比較して同じ場合, それらのメソッド呼び出しで同一のロックが使用される.
 *
 * @author K.Koike
 */
public class PerItemLock<U> {

  /** オブジェクトごとにロックを保持するためのマップ. */
  private final HashMap<U, ReentrantReadWriteLock> itemToLock = new HashMap<>();
  /** オブジェクトごとのロック回数を保持するためのマップ. */
  private final HashMap<U, Integer> itemToLockCount = new HashMap<>();


  /** {@code item} に対して書き込みロックを取得する. */
  public void acquireWriteLockFor(U item) {
    ReentrantReadWriteLock lock;
    synchronized (itemToLock) {
      lock = itemToLock.computeIfAbsent(item, key -> new ReentrantReadWriteLock(true));
      itemToLockCount.compute(item, (key, val) -> (val == null) ? 1 : val + 1);
    }
    lock.writeLock().lock();
  }

  /** {@code item} に対する書き込みロックを解放する. */
  public void releaseWriteLockFor(U item) {
    ReentrantReadWriteLock lock;
    synchronized (itemToLock) {
      lock = itemToLock.get(item);
      int lockCount = itemToLockCount.compute(item, (key, val) -> val - 1);
      if (lockCount == 0) {
        removeLock(item);
      }
    }
    lock.writeLock().unlock();
  }

  /** {@code item} に対して読み出しロックを取得する. */
  public void acquireReadLockFor(U item) {
    ReentrantReadWriteLock lock;
    synchronized (itemToLock) {
      lock = itemToLock.computeIfAbsent(item, key -> new ReentrantReadWriteLock(true));
      itemToLockCount.compute(item, (key, val) -> (val == null) ? 1 : val + 1);
    }
    lock.readLock().lock();
  }

  /** {@code item} に対する読み出しロックを解放する. */
  public void releaseReadLockFor(U item) {
    ReentrantReadWriteLock lock;
    synchronized (itemToLock) {
      lock = itemToLock.get(item);
      int lockCount = itemToLockCount.compute(item, (key, val) -> val - 1);
      if (lockCount == 0) {
        removeLock(item);
      }
    }
    lock.readLock().unlock();
  }

  private void removeLock(U item) {
    itemToLock.remove(item);
    itemToLockCount.remove(item);
  }
}
