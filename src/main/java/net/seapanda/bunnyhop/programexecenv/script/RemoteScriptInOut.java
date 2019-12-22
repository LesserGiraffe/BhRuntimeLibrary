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
package net.seapanda.bunnyhop.programexecenv.script;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.seapanda.bunnyhop.bhprogram.common.BhProgramData;
import net.seapanda.bunnyhop.programexecenv.BhParams;

/**
 * リモート通信用 BhProgream 入出力クラス
 * @author K.Koike
 */
public class RemoteScriptInOut implements ScriptInOut {

	private final BlockingQueue<BhProgramData> sendDataList;	//!< 送信データキュー
	private final BlockingQueue<String> stdinDataList = new ArrayBlockingQueue<>(BhParams.MAX_QUEUE_SIZE);
	private final AtomicBoolean connected;	//!< リモート環境との接続状態フラグ

	/**
	 * コンストラクタ
	 * @param sendDataList 送信データキュー
	 * @param connected リモート環境との接続状態フラグ
	 */
	public RemoteScriptInOut(BlockingQueue<BhProgramData> sendDataList, AtomicBoolean connected) {

		this.sendDataList = sendDataList;
		this.connected = connected;
	}

	@Override
	public void println(String str) {

		if (!connected.get())
			return;

		boolean add = false;
		BhProgramData data = new BhProgramData(BhProgramData.TYPE.OUTPUT_STR, str);
		while (!add) {
			try {
				add = sendDataList.offer(data, BhParams.PUSH_SEND_DATA_TIMEOUT, TimeUnit.SECONDS);
				if (!connected.get()) {
					sendDataList.clear();
					return;
				}
			}
			catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	@Override
	public String scanln() {

		String data = "";
		try {
			data = stdinDataList.take();
		}
		catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return data;
	}

	/**
	 * BhProgram の標準入力にデータを追加する
	 * @param input 標準入力に入力する文字
	 */
	public void putLineToStdin(String input) {
		stdinDataList.offer(input);
	}
}





























