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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.seapanda.bunnyhop.bhprogram.common.BhSymbolId;
import net.seapanda.bunnyhop.bhprogram.common.BhThreadState;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramNotification;
import net.seapanda.bunnyhop.bhprogram.common.message.exception.BhProgramException;
import net.seapanda.bunnyhop.bhprogram.common.message.exception.NoSuchSymbolException;
import net.seapanda.bunnyhop.bhprogram.common.message.exception.NoSuchThreadException;
import net.seapanda.bunnyhop.bhprogram.common.message.exception.ThreadNotSuspendedException;
import net.seapanda.bunnyhop.bhprogram.common.message.thread.BhCallStackItem;
import net.seapanda.bunnyhop.bhprogram.common.message.thread.BhThreadContext;
import net.seapanda.bunnyhop.bhprogram.common.message.variable.BhListVariable;
import net.seapanda.bunnyhop.bhprogram.common.message.variable.BhScalarVariable;
import net.seapanda.bunnyhop.bhprogram.common.message.variable.BhVariable;
import net.seapanda.bunnyhop.runtime.script.Keywords;
import net.seapanda.bunnyhop.runtime.script.ScriptThreadContext;
import net.seapanda.bunnyhop.utility.concurrent.SynchronizingTimer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

/**
 * デバッガの機能を提供するクラス.
 *
 * @author K.Koike
 */
public class BhProgramDebugger implements Debugger, DebugInstrumentation {

  /** スレッド ID とその ID のスレッドに関連する情報を格納したオブジェクトのマップ. */
  private final Map<Long, ThreadInfo> threadToInfo = new ConcurrentHashMap<>();
  /** ブレークポイント一覧. */
  private final Set<String> breakpoints = ConcurrentHashMap.<String>newKeySet();
  /** 発行した通知を格納する FIFO. */
  private final BlockingQueue<BhProgramNotification> sendNotifList;
  /** BhProgram のデータを文字列に変換するメソッド. */
  private volatile Function toStr;
  /** グローバル変数のリスト. */
  volatile List<?> globalVars = new ArrayList<>();

  /**
   * コンストラクタ.
   *
   * @param sendNotifList 発行した通知を格納する FIFO
   */
  public BhProgramDebugger(BlockingQueue<BhProgramNotification> sendNotifList) {
    this.sendNotifList = sendNotifList;
  }

  @Override
  public void notifyThreadStart(ScriptThreadContext context) {
    long threadId = Thread.currentThread().threadId();
    threadToInfo.put(threadId, new ThreadInfo(context));
  }

  @Override
  public void notifyThreadEnd() {
    long threadId = Thread.currentThread().threadId();
    if (threadToInfo.containsKey(threadId)) {
      ThreadInfo info = threadToInfo.remove(threadId);
      synchronized (info) {
        info.state.set(BhThreadState.FINISHED);
        sendNotification(new BhThreadContext(info.context.getThreadId(), BhThreadState.FINISHED));
      }
    }
  }

  @Override
  public void notifyThreadEnd(Throwable exception) {
    long threadId = Thread.currentThread().threadId();
    if (threadToInfo.containsKey(threadId)) {
      ThreadInfo info = threadToInfo.remove(threadId);
      synchronized (info) {
        info.state.set(BhThreadState.ERROR);
        sendNotification(createThreadContext(info, exception));
      }
    }
  }

  @Override
  public ScriptThreadContext getThreadContext() {
    long threadId = Thread.currentThread().threadId();
    if (threadToInfo.containsKey(threadId)) {
      return threadToInfo.get(threadId).context;
    }
    return null;
  }

  @Override
  public void conditionalWait(String stepId) {
    if (stepId != null && isThreadToBePaused(stepId)) {
      pause();
    }
  }

  @Override
  public void setStringGenerator(Function fn) {
    toStr = fn;
  }

  @Override
  public void setGlobalVariables(List<?> vars) {
    if (vars != null) {
      globalVars = new ArrayList<>(vars);
    }
  }

  @Override
  public void suspend(long threadId) throws NoSuchThreadException {
    ThreadInfo info = threadToInfo.get(threadId);
    if (info == null) {
      throw new NoSuchThreadException("Thread ID : %s".formatted(threadId));
    }
    info.stopThreshold.set(Integer.MAX_VALUE);
  }

  @Override
  public void suspendAll() {
    threadToInfo.values().forEach(context -> context.stopThreshold.set(Integer.MAX_VALUE));
  }

  @Override
  public void resume(long threadId) throws NoSuchThreadException, ThreadNotSuspendedException {
    ThreadInfo info = threadToInfo.get(threadId);
    if (info == null) {
      throw new NoSuchThreadException("Thread ID : %s".formatted(threadId));
    }
    if (info.state.get() != BhThreadState.SUSPENDED) {
      throw new ThreadNotSuspendedException("Thread ID : %s".formatted(threadId));
    }
    restart(info, -1);
  }

  @Override
  public void resumeAll() {
    threadToInfo.values().stream()
        .filter(context -> context.state.get() == BhThreadState.SUSPENDED)
        .forEach(context -> restart(context, -1));
  }

  @Override
  public void stepOver(long threadId) throws NoSuchThreadException, ThreadNotSuspendedException {
    ThreadInfo info = threadToInfo.get(threadId);
    if (info == null) {
      throw new NoSuchThreadException("Thread ID : %s".formatted(threadId));
    }
    if (info.state.get() != BhThreadState.SUSPENDED) {
      throw new ThreadNotSuspendedException("Thread ID : %s".formatted(threadId));
    }
    restart(info, info.context.getCallStackSize());
  }

  @Override
  public void stepInto(long threadId) throws NoSuchThreadException, ThreadNotSuspendedException {
    ThreadInfo info = threadToInfo.get(threadId);
    if (info == null) {
      throw new NoSuchThreadException("Thread ID : %s".formatted(threadId));
    }
    if (info.state.get() != BhThreadState.SUSPENDED) {
      throw new ThreadNotSuspendedException("Thread ID : %s".formatted(threadId));
    }
    restart(info, info.context.getCallStackSize() + 1);
  }

  @Override
  public void stepOut(long threadId) throws NoSuchThreadException, ThreadNotSuspendedException {
    ThreadInfo info = threadToInfo.get(threadId);
    if (info == null) {
      throw new NoSuchThreadException("Thread ID : %s".formatted(threadId));
    }
    if (info.state.get() != BhThreadState.SUSPENDED) {
      throw new ThreadNotSuspendedException("Thread ID : %s".formatted(threadId));
    }
    restart(info, Math.max(info.context.getCallStackSize() - 1, 1));
  }

  @Override
  public void addBreakpoints(Collection<BhSymbolId> ids) {
    List<String> idList = ids.stream().map(BhSymbolId::toString).toList();
    breakpoints.addAll(idList);
  }

  @Override
  public void removeBreakpoints(Collection<BhSymbolId> ids) {
    List<String> idList = ids.stream().map(BhSymbolId::toString).toList();
    breakpoints.removeAll(idList);
  }

  @Override
  public void setBreakpoints(Collection<BhSymbolId> ids) {
    List<String> idList = ids.stream().map(BhSymbolId::toString).toList();
    breakpoints.retainAll(idList);
    breakpoints.addAll(idList);
  }

  @Override
  public SequencedCollection<BhVariable> getLocalVariables(long threadId, int frameIdx)
      throws NoSuchThreadException, ThreadNotSuspendedException, IndexOutOfBoundsException {
    ThreadInfo info = threadToInfo.get(threadId);
    if (info == null) {
      throw new NoSuchThreadException("Thread ID : %s".formatted(threadId));
    }
    if (info.state.get() != BhThreadState.SUSPENDED) {
      throw new ThreadNotSuspendedException("Thread ID : %s".formatted(threadId));
    }
    int varStackSize = info.context.getVarStackSize();
    if (varStackSize <= frameIdx || frameIdx < 0) {
      throw new IndexOutOfBoundsException(
          "Stack Frame Size : %s.  %s was specified".formatted(varStackSize, frameIdx));
    }
    try {
      Context cx = ContextFactory.getGlobal().enterContext();
      ScriptableObject scope = cx.initStandardObjects();
      var frame = (SequencedCollection<?>) info.context.getVarStackFrame(frameIdx);
      return frame.stream()
          .filter(variable -> variable instanceof NativeObject)
          .map(variable -> createVarInfo(cx, scope, (NativeObject) variable))
          .collect(Collectors.toCollection(ArrayList::new));
    } finally {
      Context.exit();
    }
  }

  @Override
  public BhListVariable getLocalListValues(
      long threadId, int frameIdx, BhSymbolId varId, long startIdx, long length)
      throws
        NoSuchThreadException,
        ThreadNotSuspendedException,
        NoSuchSymbolException,
        IndexOutOfBoundsException {
    ThreadInfo info = threadToInfo.get(threadId);
    if (info == null) {
      throw new NoSuchThreadException("Thread ID : %s".formatted(threadId));
    }
    if (info.state.get() != BhThreadState.SUSPENDED) {
      throw new ThreadNotSuspendedException("Thread ID : %s".formatted(threadId));
    }
    int varStackSize = info.context.getVarStackSize();
    if (varStackSize <= frameIdx || frameIdx < 0) {
      throw new IndexOutOfBoundsException(
          "Stack Frame Size : %s.  %s was specified".formatted(varStackSize, frameIdx));
    }
    var frame = (SequencedCollection<?>) info.context.getVarStackFrame(frameIdx);
    try {
      Context cx = ContextFactory.getGlobal().enterContext();
      ScriptableObject scope = cx.initStandardObjects();
      Object val = findVal(varId, frame, cx, scope);
      if (val instanceof NativeArray list) {
        return getListElems(cx, scope, varId, list, startIdx, length);
      }
      throw new NoSuchSymbolException("Symbol (%s) is not a list.".formatted(varId));
    } finally {
      Context.exit();
    }
  }

  @Override
  public SequencedCollection<BhVariable> getGlobalVariables() {
    try {
      Context cx = ContextFactory.getGlobal().enterContext();
      ScriptableObject scope = cx.initStandardObjects();
      return globalVars.stream()
          .filter(variable -> variable instanceof NativeObject)
          .map(variable -> createVarInfo(cx, scope, (NativeObject) variable))
          .collect(Collectors.toCollection(ArrayList::new));
    } finally {
      Context.exit();
    }
  }

  @Override
  public BhListVariable getGlobalListValues(BhSymbolId varId, long startIdx, long length)
      throws NoSuchSymbolException {
    // AtomicBoolean のメモリバリア効果を利用して, 現在止まっているスレッドが書き込んだ最新の値を参照できるようにする
    for (ThreadInfo info : threadToInfo.values()) {
      info.state.get();
    }
    try {
      Context cx = ContextFactory.getGlobal().enterContext();
      ScriptableObject scope = cx.initStandardObjects();
      Object val = findVal(varId, globalVars, cx, scope);
      if (val instanceof NativeArray list) {
        return getListElems(cx, scope, varId, list, startIdx, length);
      } 
      throw new NoSuchSymbolException("Symbol (%s) is not a list.".formatted(varId));
    } finally {
      Context.exit();
    }
  }

  @Override
  public void sendThreadContexts() {
    for (ThreadInfo info : threadToInfo.values()) {
      synchronized (info) {
        if (info.state.get() == BhThreadState.SUSPENDED) {
          sendNotification(createThreadContext(info));
        } else if (info.state.get() == BhThreadState.RUNNING) {
          sendNotification(new BhThreadContext(info.context.getThreadId(), BhThreadState.RUNNING));
        }
      }
    }
  }

  /** このメソッドを呼び出したスレッドが一時停止の条件を満たしているか調べる.
   *
   * @param stepId このメソッドを呼び出すスレッドが, 次に実行する処理の ID
   * @return このメソッドを呼び出したスレッドが一時停止の条件を満たしている場合 true
   */  
  private boolean isThreadToBePaused(String stepId) {
    ThreadInfo info = getCurrentThreadInfo();
    if (info == null) {
      return false;
    }
    return info.context.getCallStackSize() <= info.stopThreshold.get()
        || breakpoints.contains(stepId);
  }

  /** このメソッドを呼び出したスレッドを一時停止させる. */
  private void pause() {
    ThreadInfo info = getCurrentThreadInfo();
    if (info == null) {
      return;
    }
    synchronized (info) {
      info.state.set(BhThreadState.SUSPENDED);
      sendNotification(createThreadContext(info));
    }
    info.syncTimer.countdownAndAwait();
  }

  /** {@code accessor} から変数情報を取得して返す. */
  private BhVariable createVarInfo(Context cx, ScriptableObject scope, NativeObject accessor) {
    String id = accessor.get(Keywords.Properties.ID).toString();
    Function getter = (Function) accessor.get(Keywords.Properties.GET);
    Object val = getter.call(cx, scope, scope, new Object[0]);
    if (val instanceof NativeArray list) {
      return new BhListVariable(BhSymbolId.of(id), list.getLength());
    } else {
      return new BhScalarVariable(BhSymbolId.of(id), getValStr(cx, scope, val));
    }
  }

  /** {@code val} を文字列化して返す. */
  private String getValStr(Context cx, ScriptableObject scope, Object val) {
    if (toStr == null) {
      return val.toString();
    }
    // val が ConsString などの場合, JavaScript 側での文字列変換が正常に動作しないので,
    // ここで Java の String に変換しておく.
    if (val instanceof CharSequence charSeq) {
      val = charSeq.toString();
    }
    return toStr.call(cx, scope, scope, new Object[] {Context.javaToJS(val, scope)}).toString();
  }

  /** 変数スタックのスタックフレーム ({@code frame}) からシンボル ID が {@code varId} である変数を探してその値を返す. */
  private Object findVal(
      BhSymbolId varId, SequencedCollection<?> frame, Context cx, ScriptableObject scope)
      throws NoSuchSymbolException {
    NativeObject accessor = findAccessor(frame, varId);
    Function getter = (Function) accessor.get(Keywords.Properties.GET);
    return getter.call(cx, scope, getter, new Object[0]);
  }

  /** 変数スタックのスタックフレーム ({@code frame}) からシンボル ID が {@code varId} である変数のアクセサを探す. */
  private NativeObject findAccessor(SequencedCollection<?> frame, BhSymbolId varId)
      throws NoSuchSymbolException {
    for (Object obj : frame) {
      if (obj instanceof NativeObject accessor) {
        BhSymbolId id = BhSymbolId.of(accessor.get(Keywords.Properties.ID).toString());
        if (varId.equals(id)) {
          return accessor;
        }
      }  
    }
    throw new NoSuchSymbolException("Symbol ID : %s".formatted(varId));
  }

  /**
   * {@code list} の {@code startIdx} から {@code length} 個の要素の値を保持する
   * {@link BhListVariable} オブジェクトを返す.
   */
  private BhListVariable getListElems(
      Context cx,
      ScriptableObject scope,
      BhSymbolId varId,
      NativeArray list,
      long startIdx,
      long length) {
    if (length == 0) {
      return new BhListVariable(varId, list.getLength());
    }
    if (length < 0) {
      startIdx = (startIdx + length + 1);
      length = -length;
    }
    long endIdx = (startIdx + length - 1);
    var valList = new ArrayList<String>();
    // リストを複数のスレッドで操作している場合, list の範囲チェックは無意味なので行わない.
    // 代わりに例外が発生するまで, 指定された範囲の値を取得する.
    try {
      for (long i = startIdx; i <= endIdx; ++i) {
        valList.add(getValStr(cx, scope, list.get(i)));
      }
    } catch (Exception ignored) { /* Do nothing. */ }
    return new BhListVariable(
        varId,
        list.getLength(),
        List.of(new BhListVariable.Slice(startIdx, valList)));
  }

  /**
   * このメソッドを呼び出したスレッドの {@link ThreadInfo} を取得する.
   *
   * @return このメソッドを呼び出したスレッドの {@link ThreadInfo}.  見つからない場合は null.
   */
  private ThreadInfo getCurrentThreadInfo() {
    long threadId = Thread.currentThread().threadId();
    return threadToInfo.get(threadId);
  }

  private void sendNotification(BhProgramNotification notif) {
    try {
      sendNotifList.put(notif);
    } catch (InterruptedException e) { /* Do nothing. */ }
  }

  /** {@code info} に対応するスレッドを再開する. */
  private void restart(ThreadInfo info, int stopThreshold) {
    info.stopThreshold.set(stopThreshold);
    // BhProgram を実行するスレッド動き出す前に, 状態を RUNNING にしなければならない.
    // さもないと, 動き出したスレッドの変数を読んで送信してしまう可能性がある.
    synchronized (info) {
      info.state.set(BhThreadState.RUNNING);
      sendNotification(new BhThreadContext(info.context.getThreadId(), BhThreadState.RUNNING));
    }
    info.syncTimer.countdown();
  }

  /** {@code info} と {@code exception} を元に {@link BhThreadContext} を作成する. */
  private static BhThreadContext createThreadContext(ThreadInfo info, Throwable exception) {
    BhProgramException threw = createBhProgramException(info, exception);
    SequencedCollection<BhCallStackItem> callStack = createCallStack(info.context, true);
    return new BhThreadContext(
        info.context.getThreadId(),
        info.state.get(),
        callStack,
        threw);
  }

  /** {@code info} を元に {@link BhThreadContext} を作成する. */
  private static BhThreadContext createThreadContext(ThreadInfo info) {
    SequencedCollection<BhCallStackItem> callStack = createCallStack(info.context, false);
    return new BhThreadContext(
        info.context.getThreadId(),
        info.state.get(),
        callStack,
        info.context.getNextNodeInstanceId());
  }

  /** {@code info} と {@code src} から {@link BhProgramException} オブジェクトを作成する. */
  private static BhProgramException createBhProgramException(ThreadInfo info, Throwable src) {
    if (src instanceof BhProgramException exception) {
      return new BhProgramException(exception);
    }
    if (src.getCause() instanceof BhProgramException exception) {
      return new BhProgramException(exception);
    }
    String errMsg = info.context.getErrorMessages().stream()
          .reduce((lhs, rhs) -> lhs + "\n" + rhs)
          .orElse("");
    return new BhProgramException(errMsg, toSerializable(src));
  }

  /**
   * スレッドコンテキストからコールスタックを作成する.
   *
   * @param context このスレッドコンテキストを参照してコールスタックを作成する
   * @param containNextNodeId 次に実行する処理の ID をコールスタックに追加する場合 true.
   */
  private static SequencedCollection<BhCallStackItem> createCallStack(
      ScriptThreadContext context, boolean containNextNodeId) {
    var callStack = new ArrayList<BhCallStackItem>();
    List<BhSymbolId> cs = context.getCallStack();
    for (int i = 0; i < cs.size(); ++i) {
      callStack.add(new BhCallStackItem(i, cs.get(i)));
    }
    if (!containNextNodeId) {
      return callStack;
    }
    BhSymbolId nextNodeId = context.getNextNodeInstanceId();
    if (!nextNodeId.equals(BhSymbolId.NONE)) {
      callStack.add(new BhCallStackItem(callStack.size(), nextNodeId));
    }
    return callStack;    
  }

  /**
   * {@code obj} がシリアライズ不可能な場合, {@code obj} のメッセージを格納した
   * {@link Throwable} オブジェクトを返す.
   */
  private static Throwable toSerializable(Throwable obj) {
    if (!isSerializable(obj)) {
      return new Throwable(obj.getMessage());
    }
    return obj;
  }

  /** {@code obj} がシリアライズ可能か調べる. */
  private static boolean isSerializable(Object obj) {
    try (
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
    ) {
      oos.writeObject(obj);
      oos.flush();
      bos.toByteArray();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /** 
   * スレッドごとに固有のデータを格納するレコード.
   *
   * @param context BhProgram 側で操作されるスレッド固有のデータ
   * @param state スレッドの状態
   * @param stopThreshold {@code callStack} のサイズがこの値以上の場合, スレッドの停止条件を満たしているものとする
   * @param syncTimer スレッドの停止に使うオブジェクト
   */
  private record ThreadInfo(
      ScriptThreadContext context,
      AtomicReference<BhThreadState> state,
      AtomicInteger stopThreshold,
      SynchronizingTimer syncTimer) {

    ThreadInfo(ScriptThreadContext context) {
      this(
          context,
          new AtomicReference<>(BhThreadState.RUNNING),
          new AtomicInteger(-1),
          new SynchronizingTimer(2, true));
    }
  }
}
