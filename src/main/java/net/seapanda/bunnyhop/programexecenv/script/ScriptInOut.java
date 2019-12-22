package net.seapanda.bunnyhop.programexecenv.script;

/**
 * BhProgram の入出力用関数インタフェース
 * @author K.Koike
 */
public interface ScriptInOut {

	/**
	 * BhProgram の標準出力にデータを追加する
	 * @param str 出力する文字列
	 */
	public void println(String str);

	/**
	 * BhProgram の標準入力のデータを取得する
	 * @return 標準入力に入力された文字
	 */
	public String scanln();
}
