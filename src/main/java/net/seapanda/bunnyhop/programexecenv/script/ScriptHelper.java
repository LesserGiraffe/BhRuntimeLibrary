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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.seapanda.bunnyhop.bhprogram.common.BhNodeInstanceID;
import net.seapanda.bunnyhop.bhprogram.common.BhProgramException;
import net.seapanda.bunnyhop.programexecenv.tools.Util;

/**
 * BhProgram が実行時に使用するヘルパークラス
 * @author K.Koike
 */
public class ScriptHelper {

	private final String OS_NAME = System.getProperty("os.name").toLowerCase();
	public final Platform PLATFORM = this.new Platform();
	public static final ScriptHelper INSTANCE = new ScriptHelper();

	private ScriptHelper() {}

	/**
	 * BhNodeInstanceID を作成して返す
	 * @param id 作成するIDの文字列表現
	 * @return BhNodeInstanceID オブジェクト
	 */
	public BhNodeInstanceID newBhNodeInstanceID(String id) {
		return new BhNodeInstanceID(id);
	}

	/**
	 * BhNodeException を作成して返す
	 * @param callStack 例外発生時のコールスタック
	 * @param msg 例外メッセージ
	 * @return 例外オブジェクト
	 */
	public BhProgramException newBhProgramException(List<?> callStack, String msg) {
		return newBhProgramException(callStack, msg, "");
	}

	/**
	 * BhNodeException を作成して返す
	 * @param callStack 例外発生時のコールスタック
	 * @param msg 例外メッセージ
	 * @param scriptEngineMsg BhProgram の実行エンジンから返されたエラーメッセージ
	 * @return 例外オブジェクト
	 */
	public BhProgramException newBhProgramException(List<?> callStack, String msg, String scriptEngineMsg) {

		ArrayList<BhNodeInstanceID> funcCallStack = callStack.stream()
			.map(nodeInstanceID -> newBhNodeInstanceID(nodeInstanceID.toString()))
			.collect(Collectors.toCollection(ArrayList::new));

		return new BhProgramException(funcCallStack, msg, scriptEngineMsg);
	}

	/**
	 * 実行ファイルがあるディレクトリのパスを取得する
	 */
	public String getExecPath() {
		return Util.INSTANCE.EXEC_PATH;
	}

	public byte toByte(int num) {
		return (byte)num;
	}

	public class Platform {

		public boolean isWindows() {
			return OS_NAME.startsWith("windows");
		}

		public boolean isLinux() {
			return OS_NAME.startsWith("linux");
		}
	}
}
