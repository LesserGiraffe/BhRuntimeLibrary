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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import net.seapanda.bunnyhop.runtime.script.PerItemLock;
import net.seapanda.bunnyhop.utility.concurrent.SynchronizingTimer;
import net.seapanda.bunnyhop.utility.function.TriConsumer;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

/**
 * 音声データの取得と再生のための機能を提供するクラス.
 *
 * @author  K.Koike
 */
public class AudioControllerImpl implements AudioController {

  // オーディオフォーマットの設定
  private static final float SAMPLE_RATE = 44100.0f;
  private static final int SAMPLE_SIZE = 16; // bits
  private static final int CHANNELS = 1;
  private static final boolean SIGNED = true;
  private static final boolean BIG_ENDIAN = false;

  private final Path root;
  /** パスごとにロックオブジェクトを保持するためのオブジェクト. */
  private final PerItemLock<Path> lock = new PerItemLock<>();

  /**
   * コンストラクタ.
   *
   * @param rootPath ルートディレクトリのパス
   */
  public AudioControllerImpl(String rootPath) {
    this.root = Paths.get(rootPath).toAbsolutePath().normalize();
  }

  @Override
  public synchronized void record(String path, double time) throws Exception {
    checkRecordingTime(time);
    Path targetPath = toPath(path);
    try {
      lock.acquireWriteLockFor(targetPath);
      createDir(targetPath.getParent());

      var format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, BIG_ENDIAN);
      DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
      if (!AudioSystem.isLineSupported(info)) {
        throw new LineUnavailableException("The specified audio format is not supported");
      }
      TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
      line.open(format);
      line.start();
      var timer = new SynchronizingTimer(1, false);
      final CompletableFuture<Exception> future = CompletableFuture.supplyAsync(
          () -> recordToFile(line, targetPath, timer),
          Executors.newSingleThreadExecutor());
      timer.await();
      Thread.sleep((long) (time * 1000L));
      line.stop();
      line.close();
      Exception exception = future.get();
      if (exception != null) {
        throw exception;
      }
    } finally {
      lock.releaseWriteLockFor(targetPath);
    }
  }

  /** 録音時間が正常かチェックする. */
  private static void checkRecordingTime(double time) {
    if (time < 0) {
      throw new IllegalArgumentException("Recording time must be positive.  (%s)".formatted(time));
    }
  }

  private static void createDir(Path dirPath) throws IOException {
    if (dirPath != null) {
      Files.createDirectories(dirPath);
    }
  }

  /** {@code line} から取得した音声データを {@code path} に保存する. */
  private static Exception recordToFile(TargetDataLine line, Path path, SynchronizingTimer timer) {
    AudioInputStream audioStream = new AudioInputStream(line);
    File outputFile = path.toFile();
    try {
      timer.countdown();
      AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  @Override
  public void play(String path, double volume) throws Exception {
    Path targetPath = toPath(path);
    try {
      lock.acquireReadLockFor(targetPath);
      File audioFile = targetPath.toFile();
      AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
      AudioFormat format = audioStream.getFormat();
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      if (!AudioSystem.isLineSupported(info)) {
        throw new LineUnavailableException("The specified audio format is not supported");
      }
      SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(format);
      controlVolume((float) volume, line);
      line.start();
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = audioStream.read(buffer, 0, buffer.length)) != -1) {
        line.write(buffer, 0, bytesRead);
      }
      line.drain();
      line.close();
      audioStream.close();
    } finally {
      lock.releaseReadLockFor(targetPath);
    }
  }

  /** 音量を調整する. */
  private static void controlVolume(float volume, SourceDataLine line) {
    if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
      volume = Math.clamp(volume, 0f, 1f);
      var volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
      float range = volumeControl.getMaximum() - volumeControl.getMinimum();
      float gain = (range * volume) + volumeControl.getMinimum();
      volumeControl.setValue(gain);
    }
  }

  @Override
  public void delete(String path) throws IOException {
    Path targetPath = toPath(path);
    try {
      lock.acquireWriteLockFor(targetPath);
      Files.delete(targetPath);
    } catch (NoSuchFileException ignored) {
      // Do nothing.
    } finally {
      lock.releaseWriteLockFor(targetPath);
    }
  }

  @Override
  public double findSoundPressureAverage(double time) throws LineUnavailableException {
    MutableLong sum = new MutableLong(0);
    MutableInt numSamples = new MutableInt(0);
    TriConsumer<byte[], Integer, Boolean> calcSum =
        (sampleBytes, length, littleEndian) -> {
          sum.add(calculateSum(sampleBytes, length, littleEndian));
          numSamples.add(length / 2);
        };
    readMicrophoneData(time, calcSum);
    if (numSamples.getValue() == 0) {
      return 0;
    }
    return ((double) sum.getValue()) / numSamples.getValue();
  }

  @Override
  public double findSoundPressurePeak(double time) throws LineUnavailableException {
    MutableInt peak = new MutableInt(0);
    TriConsumer<byte[], Integer, Boolean> calcPeak =
        (sampleBytes, length, littleEndian) -> {
          int partialPeak = calculatePeak(sampleBytes, length, littleEndian);
          peak.setValue(Math.max(partialPeak, peak.getValue()));
        };
    readMicrophoneData(time, calcPeak);
    return (double) peak.getValue();
  }

  /** マイクのデータを {@code time} 秒読み取り, 小分けにして callBack に渡す. */
  private void readMicrophoneData(double time, TriConsumer<byte[], Integer, Boolean> callback)
      throws LineUnavailableException {
    checkRecordingTime(time);
    var format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, BIG_ENDIAN);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    if (!AudioSystem.isLineSupported(info)) {
      throw new LineUnavailableException("The specified audio format is not supported");
    }
    TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
    targetLine.open(format);
    targetLine.start();

    int bufferSize = 4096;
    byte[] buffer = new byte[bufferSize];
    int bytesPerSecond = (int) (SAMPLE_RATE * CHANNELS * (SAMPLE_SIZE / 8));
    long totalBytesToRead = (long) (bytesPerSecond * time);
    long totalBytesRead = 0;
    try {
      while (totalBytesRead < totalBytesToRead) {
        long bytesToRead = Math.min(bufferSize, totalBytesToRead - totalBytesRead);
        int bytesRead = targetLine.read(buffer, 0, (int) bytesToRead);
        if (bytesRead > 0) {
          totalBytesRead += bytesRead;
        }
        callback.accept(buffer, bytesRead, !BIG_ENDIAN);
      }
    } finally {
      targetLine.stop();
      targetLine.close();
    }
  }

  /**
   * {@code sampleBytes} を 16-bit 符号付整数のリストとして読み取り, その絶対値の合計を求める.
   *
   * @param sampleBytes サンプルデータのバイト列
   * @param length {@code sampleBytes} の中の有効なデータの数
   * @param littleEndian {@code sampleBytes} を整数値に変換するときのエンディアン
   * @return {@code sampleBytes} の絶対値の合計
   */
  private static long calculateSum(byte[] sampleBytes, int length, boolean littleEndian) {
    if (length < 2) {
      return 0;
    }
    length = length / 2 * 2;
    long sum = 0;
    for (int i = 0; i < length - 1; i += 2) {
      int up = littleEndian ? i + 1 : i;
      int low = littleEndian ? i : i + 1;
      int sample = (sampleBytes[up] << 8) | (sampleBytes[low] & 0xFF);
      sum += Math.abs(sample);
    }
    return sum;
  }

  /**
   * {@code sampleBytes} を 16-bit 符号付整数のリストとして読み取り, その絶対値の最大値を求める.
   *
   * @param sampleBytes サンプルデータのバイト列
   * @param length {@code sampleBytes} の中の有効なデータの数
   * @param littleEndian {@code sampleBytes} を整数値に変換するときのエンディアン
   * @return {@code sampleBytes} の絶対値の最大値
   */
  private static int calculatePeak(byte[] sampleBytes, int length, boolean littleEndian) {
    if (length < 2) {
      return 0;
    }
    length = length / 2 * 2;
    int peak = 0;
    for (int i = 0; i < length - 1; i += 2) {
      int up = littleEndian ? i + 1 : i;
      int low = littleEndian ? i : i + 1;
      int sample = (sampleBytes[up] << 8) | (sampleBytes[low] & 0xFF);
      peak = Math.max(peak, Math.abs(sample));
    }
    return peak;
  }

  @Override
  public List<String> getFiles() throws IOException {
    if (!Files.exists(root)) {
      return new ArrayList<>();
    }
    try (Stream<Path> paths = Files.walk(root)) {
      return paths
          .filter(Files::isRegularFile)  // 通常ファイルのみ
          .map(root::relativize)          // root からの相対パスに変換
          .map(Path::toString)            // String に変換
          .collect(Collectors.toCollection(ArrayList::new));
    }
  }

  @Override
  public Path getRoot() {
    return root;
  }

  private Path toPath(String path) {
    Path targetPath = Paths.get(path);
    if (!targetPath.isAbsolute()) {
      targetPath = root.resolve(targetPath);
    }
    return targetPath.normalize();
  }
}
