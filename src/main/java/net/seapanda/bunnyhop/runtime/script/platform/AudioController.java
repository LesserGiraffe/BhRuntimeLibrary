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
import javax.sound.sampled.LineUnavailableException;

/**
 * 音声データの取得と再生のための機能を規定したインタフェース.
 *
 * <p>このクラスのメソッドのパスに相対パスを指定した場合, 特定のディレクトリ (root と呼称) からの相対パスとなる
 *
 * @author  K.Koike
 */
public interface AudioController {

  /**
   * マイクから音声を録音して WAVE ファイルとして保存する.
   *
   * @param path 音声データを保存するファイルのパス
   * @param time 録音する秒数
   * @throws Exception 録音に失敗した場合
   */
  void record(String path, double time) throws Exception;

  /**
   * WAVEファイルを読み出して指定した音量で再生する.
   *
   * @param path 再生する音声ファイルのパス
   * @param volume 音量 (0.0 ~ 1.0)
   * @throws Exception 再生に失敗した場合
   */
  void play(String path, double volume) throws Exception;

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
   * マイクから音声データを取得し, その絶対値の平均値を計算する.
   *
   * @param time 録音時間 (秒)
   * @return マイクから取得したサンプルの絶対値の平均値
   * @throws LineUnavailableException オーディオラインが利用できない場合
   */
  double findSoundPressureAverage(double time) throws LineUnavailableException;

  /**
   * マイクから音声データを取得し, その最大値を見つける.
   *
   * @param time 録音時間 (秒)
   * @return マイクから取得したサンプルの絶対値の最大値
   * @throws LineUnavailableException オーディオラインが利用できない場合
   */
  double findSoundPressurePeak(double time) throws LineUnavailableException;

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
