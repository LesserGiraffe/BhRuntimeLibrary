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

package net.seapanda.bunnyhop.runtime.script.debug;

import java.util.Collection;
import java.util.SequencedCollection;
import net.seapanda.bunnyhop.bhprogram.common.BhSymbolId;
import net.seapanda.bunnyhop.bhprogram.common.message.exception.NoSuchSymbolException;
import net.seapanda.bunnyhop.bhprogram.common.message.exception.NoSuchThreadException;
import net.seapanda.bunnyhop.bhprogram.common.message.exception.ThreadNotSuspendedException;
import net.seapanda.bunnyhop.bhprogram.common.message.variable.ListVariable;
import net.seapanda.bunnyhop.bhprogram.common.message.variable.Variable;

/**
 * デバッガの機能を規定したインタフェース.
 *
 * <p> このインタフェースのメンバメソッドはスレッドセーフであることを保証しなくて良い. </p>
 * <p> このインタフェースのメンバメソッドは, 単一のスレッドから同期的に呼ぶこと. </p>
 *
 * @author K.Koike
 */
public interface Debugger {

  /**
   * {@code threadId} で指定したスレッドの一時停止条件が必ず満たされるようになる.
   *
   * @param threadId この ID のスレッドの一時停止条件が必ず満たされるようになる
   * @throws NoSuchThreadException {@code threadId} で指定したスレッドが見つからなかった場合
   */
  void enableWaitCondition(long threadId) throws NoSuchThreadException;

  /** デバッガが現在監視している全てのスレッドの一時停止条件が必ず満たされるようになる. */
  void enableWaitConditionAll();

  /**
   * {@code threadId} で指定したスレッドが一時停止中であった場合, 処理を再開させる.
   *
   * @param threadId この ID のスレッドの動作を再開させる
   * @throws NoSuchThreadException {@code threadId} で指定したスレッドが見つからなかった場合
   * @throws ThreadNotSuspendedException {@code threadId} で指定したスレッドが一時停止中でなかった場合
   */
  void resume(long threadId) throws NoSuchThreadException, ThreadNotSuspendedException;

  /** 一時停止中の全てのスレッドの動作を再開させる. */
  void resumeAll();

  /**
   * {@code threadId} で指定したスレッドが一時停止中であった場合, 次に停止可能な位置まで処理を進める.
   *
   * <p>次の処理が関数呼び出しであった場合, その中では止まらず, 関数呼び出し終了後の次に停止可能な位置で止まる.
   *    このメソッドはスレッドが再度止まるのを待たない.
   *
   * @throws NoSuchThreadException {@code threadId} で指定したスレッドが見つからなかった場合
   * @throws ThreadNotSuspendedException {@code threadId} で指定したスレッドが一時停止中でなかった場合
   */
  void stepOver(long threadId) throws NoSuchThreadException, ThreadNotSuspendedException;

  /**
   * {@code threadId} で指定したスレッドが一時停止中であった場合, 次に停止可能な位置まで処理を進める.
   *
   * <p>次の処理が関数呼び出しであった場合, その中に停止可能な位置があれば止まる.
   *    このメソッドはスレッドが再度止まるのを待たない.
   *
   * @throws NoSuchThreadException {@code threadId} で指定したスレッドが見つからなかった場合
   * @throws ThreadNotSuspendedException {@code threadId} で指定したスレッドが一時停止中でなかった場合
   */
  void stepInto(long threadId) throws NoSuchThreadException, ThreadNotSuspendedException;

  /**
   * {@code threadId} で指定したスレッドが一時停止中であった場合,
   * 現在実行している関数の呼び出し元関数の中で次に停止可能な位置まで処理を進める.
   *
   * <p>このメソッドはスレッドが再度止まるのを待たない.
   *
   * @throws NoSuchThreadException {@code threadId} で指定したスレッドが見つからなかった場合
   * @throws ThreadNotSuspendedException {@code threadId} で指定したスレッドが一時停止中でなかった場合
   */
  void stepOut(long threadId) throws NoSuchThreadException, ThreadNotSuspendedException;

  /**
   * ブレークポイントを追加する.
   *
   * @param ids 追加するブレークポイントの ID のリスト
   */
  void addBreakpoints(Collection<BhSymbolId> ids);

  /**
   * 設定済みのブレークポイントを削除する.
   *
   * @param ids 削除するブレークポイントの ID のリスト
   */
  void removeBreakpoints(Collection<BhSymbolId> ids);

  /**
   * ブレークポイントを設定する.
   * このメソッドを呼ぶ前に設定済みのブレークポイントは削除される.
   *
   * @param ids 設定するブレークポイントの ID のリスト
   */
  void setBreakpoints(Collection<BhSymbolId> ids);

  /**
   * ローカル変数の情報を取得する.
   *
   * @param threadId この ID のスレッドと紐づくローカル変数の情報を取得する.
   * @param frameIdx このインデックスで指定される関数フレームに存在するローカル変数の情報を取得する.
   * @throws NoSuchThreadException {@code threadId} で指定したスレッドが見つからなかった場合
   * @throws ThreadNotSuspendedException {@code threadId} で指定したスレッドが一時停止中でなかった場合
   * @throws IndexOutOfBoundsException {@code frameIdx} で指定したスタックフレームが存在しなかった場合
   */
  SequencedCollection<Variable> getLocalVariables(long threadId, int frameIdx)
      throws NoSuchThreadException, ThreadNotSuspendedException, IndexOutOfBoundsException;

  /**
   * ローカル変数のリストの値を取得する.
   *
   * @param threadId この ID のスレッドと紐づくローカル変数のリストの値を取得する.
   * @param frameIdx このインデックスで指定される関数フレームに存在するローカル変数の情報を取得する.
   * @param varId 値を取得するリストの ID
   * @param startIdx 値を取得する範囲の最初のインデックス
   * @param length 取得する要素数
   * @return リストの値
   * @throws NoSuchThreadException {@code threadId} で指定したスレッドが見つからなかった場合
   * @throws ThreadNotSuspendedException {@code threadId} で指定したスレッドが一時停止中でなかった場合
   * @throws NoSuchSymbolException {@code varId} で指定したリストが見つからなかった場合
   * @throws IndexOutOfBoundException {@code startIdx} と {@code length} で指定したリストの範囲が不正であった場合
   */
  ListVariable getLocalListValues(
      long threadId, int frameIdx, BhSymbolId varId, int startIdx, int length)
      throws
        NoSuchThreadException,
        ThreadNotSuspendedException,
        NoSuchSymbolException,
        IndexOutOfBoundsException;

  /** グローバル変数の情報を取得する. */
  SequencedCollection<Variable> getGlobalVariables();

  /**
   * グローバル変数のリストの値を取得する.
   *
   * @param varId 値を取得するリストの ID
   * @param startIdx 値を取得する範囲の最初のインデックス
   * @param length 取得する要素数
   * @return リストの値
   * @throws NoSuchSymbolException {@code varId} で指定したリストが見つからなかった場合
   * @throws IndexOutOfBoundsException {@code startIdx} と {@code length} で指定したリストの範囲が不正であった場合
   */
  ListVariable getGlobalListValues(BhSymbolId varId, int startIdx, int length)
      throws NoSuchSymbolException, IndexOutOfBoundsException;
  
  /** デバッガが監視している全てのスレッドのコンテキストを BunnyHop に送信する. */
  void sendThreadContexts();
}
