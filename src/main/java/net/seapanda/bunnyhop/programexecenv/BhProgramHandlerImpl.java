/**
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
package net.seapanda.bunnyhop.programexecenv;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

import net.seapanda.bunnyhop.bhprogram.common.BhProgramData;
import net.seapanda.bunnyhop.bhprogram.common.BhProgramHandler;
import net.seapanda.bunnyhop.programexecenv.tools.LogManager;
import net.seapanda.bunnyhop.programexecenv.tools.Util;

/**
 * スクリプトとBunnyHop間でデータを送受信するクラス
 * @author K.Koike
 */
public class BhProgramHandlerImpl implements BhProgramHandler {

	private final ExecutorService bhProgramExec = Executors.newSingleThreadExecutor();
	private final ExecutorService recvDataProcessor = Executors.newSingleThreadExecutor();
	private final BlockingQueue<BhProgramData> sendDataList = new ArrayBlockingQueue<>(BhParams.MAX_QUEUE_SIZE);	//!< to BunnyHop
	private final BlockingQueue<BhProgramData> recvDataList = new ArrayBlockingQueue<>(BhParams.MAX_QUEUE_SIZE);	//!< from BunnyHop
	private final AtomicBoolean connected = new AtomicBoolean(false);	//!< BunnyHopとの通信が有効な場合true
	private final ScriptInOut scriptIO = new ScriptInOut(sendDataList, connected);	//!< BhProgramの入出力用オブジェクト
	ScriptableObject bhAppScope;
	Script bhAppScript;

	public BhProgramHandlerImpl(){}

	/**
	 * 初期化する
	 */
	public boolean init() {

		recvDataProcessor.submit(() ->{
			processRecvData();
		});

		Context cx = ContextFactory.getGlobal().enterContext();
		bhAppScope = cx.initStandardObjects();
		Context.exit();
		return true;
	}

	@Override
	public boolean runScript(String fileName, BhProgramData data) {

		Path scriptPath = Paths.get(Util.INSTANCE.EXEC_PATH, BhParams.Path.SCRIPT_DIR, fileName);
		boolean success = true;
		Context context = ContextFactory.getGlobal().enterContext();

		try (BufferedReader reader = Files.newBufferedReader(scriptPath, StandardCharsets.UTF_8)){

			context.setLanguageVersion(Context.VERSION_ES6);
			context.setOptimizationLevel(9);
			bhAppScript = context.compileReader(reader, scriptPath.getFileName().toString(), 1, null);
			bhProgramExec.submit(() -> {
				try {
					Context cx = ContextFactory.getGlobal().enterContext();
					ScriptableObject.putProperty(bhAppScope, BhParams.JsKeyword.KEY_BH_INOUT, scriptIO);
					ScriptableObject.putProperty(bhAppScope, BhParams.JsKeyword.KEY_BH_NODE_UTIL, Util.INSTANCE);
					bhAppScript.exec(cx, bhAppScope);
					Function main = (Function)bhAppScope.get(data.fireEventFuncName);
					main.call(cx, bhAppScope, bhAppScope, new String[] {data.event.toString()});
					Context.exit();
				}
				catch (Exception e) {
					LogManager.INSTANCE.errMsgForDebug("runScript 1 " +  e.toString() + " " + fileName);
				}
			});
		}
		catch (Exception e) {
			LogManager.INSTANCE.errMsgForDebug("runScript 2 " +  e.toString() + " " + fileName);
			success = false;
		}
		Context.exit();
		return success;
	}

	@Override
	public boolean sendDataToScript(BhProgramData data) {

		boolean success = false;
		try {
			success = recvDataList.offer(data, BhParams.PUSH_RECV_DATA_TIMEOUT, TimeUnit.SECONDS);
		}
		catch(InterruptedException e) {}
		return success;
	}

	@Override
	public BhProgramData recvDataFromScript() {
		BhProgramData data = null;
		try {
			data = sendDataList.poll(BhParams.POP_SEND_DATA_TIMEOUT, TimeUnit.SECONDS);
		}
		catch(InterruptedException e) {}
		return data;
	}

	@Override
	public void connect() {
		connected.set(true);
	}

	@Override
	public void disconnect() {
		connected.set(false);
		sendDataList.clear();
	}

	/**
	 * BunnyHopから受信したデータを処理し続ける
	 */
	private void processRecvData() {

		while(true) {

			BhProgramData data = null;
			try {
				data = recvDataList.take();
			}
			catch(InterruptedException e) {
				break;
			}

			switch (data.type) {
				case INPUT_STR:
					scriptIO.addStdInData(data.str);
					break;

				case INPUT_EVENT:
					fireEvent(data);
					break;

				default:
			}
		}
	}

	/**
	 * BhProgram のイベントハンドラを呼び出す
	 * @param data イベント情報の入ったデータ
	 * */
	private void fireEvent(BhProgramData data) {

		if (bhAppScript == null)
			return;

		try {
			Context cx = ContextFactory.getGlobal().enterContext();
			Function main = (Function)bhAppScope.get(data.fireEventFuncName);
			main.call(cx, bhAppScope, bhAppScope, new String[] {data.event.toString()});
			Context.exit();
		}
		catch (Exception e) {
			LogManager.INSTANCE.errMsgForDebug(BhProgramHandlerImpl.class.getSimpleName() + "::fireEvent\n" + e.toString());
		}
	}
}
















