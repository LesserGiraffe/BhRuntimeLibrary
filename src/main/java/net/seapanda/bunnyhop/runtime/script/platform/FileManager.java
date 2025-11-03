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

package net.seapanda.bunnyhop.runtime.script.platform;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * ファイル操作のためのオブジェクトをまとめて保持するクラス.
 *
 * @author K.Koike
 */
public class FileManager {

  public final TextFileManager text;

  /** コンストラクタ. */
  public FileManager(TextFileManager text) {
    this.text = text;
  }

  /**
   * テキストデータの保存と読み出しのための機能を規定したインタフェース.
   *
   * <p>このクラスのメソッドのパスに相対パスを指定した場合, 特定のディレクトリ (root と呼称) からの相対パスとなる
   *
   * @author K.Koike
   */
  public interface TextFileManager {

    /**
     * 指定したパスにテキストデータを保存する.
     *
     * @param data 保存するテキストデータ
     * @param path {@code data} を保存するファイルのパス
     * @throws IOException ファイル操作に失敗した場合
     */
    void save(String data, String path) throws IOException;

    /**
     * 指定したパスからテキストデータを読み出す.
     *
     * @param path テキストデータを読みだすファイルのパス
     * @return 読み出したテキストデータ
     * @throws IOException ファイル操作に失敗した場合
     */
    String load(String path) throws IOException;

    /**
     * 指定したパスのファイルを削除する.
     *
     * <p>ファイルが存在しない場合は何もしない
     *
     * @param path 削除するファイルのパス
     * @throws IOException ファイルの削除に失敗した場合
     */
    void delete(String path) throws IOException;

    /**
     * root 以下のディレクトリにある全てのファイルのパスを root 部分を除いて返す.
     *
     * @return ファイルパスのリスト (root からの相対パス)
     * @throws IOException ファイル操作に失敗した場合
     */
    List<String> getFiles() throws IOException;

    /**
     * root パスを取得する.
     *
     * @return root パス
     */
    Path getRoot();
  }
}
