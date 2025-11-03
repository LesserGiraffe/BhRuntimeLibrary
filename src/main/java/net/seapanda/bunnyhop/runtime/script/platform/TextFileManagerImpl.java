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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.seapanda.bunnyhop.runtime.script.PerItemLock;
import net.seapanda.bunnyhop.runtime.script.platform.FileManager.TextFileManager;

/**
 * テキストデータの保存と読み出しのための機能を提供するクラス.
 *
 * @author K.Koike
 */
public class TextFileManagerImpl implements TextFileManager {

  private final Path root;
  /** パスごとにロックオブジェクトを保持するためのオブジェクト. */
  private final PerItemLock<Path> lock = new PerItemLock<>();

  /**
   * コンストラクタ.
   *
   * @param rootPath このパス以下にファイルを保存する.
   */
  public TextFileManagerImpl(String rootPath) {
    this.root = Paths.get(rootPath).toAbsolutePath().normalize();
  }

  @Override
  public void save(String data, String path) throws IOException {
    Path targetPath = toPath(path);
    try {
      lock.acquireWriteLockFor(targetPath);
      createDir(targetPath.getParent());
      Files.writeString(
          targetPath,
          data,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    } finally {
      lock.releaseWriteLockFor(targetPath);
    }
  }

  private static void createDir(Path dirPath) throws IOException {
    if (dirPath != null) {
      Files.createDirectories(dirPath);
    }
  }

  @Override
  public String load(String path) throws IOException {
    Path targetPath = toPath(path);
    try {
      lock.acquireReadLockFor(targetPath);
      return Files.readString(targetPath, StandardCharsets.UTF_8);
    } finally {
      lock.releaseReadLockFor(targetPath);
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
  public List<String> getFiles() throws IOException {
    if (!Files.exists(root)) {
      return new ArrayList<>();
    }
    try (Stream<Path> paths = Files.walk(root)) {
      return paths
          .filter(Files::isRegularFile)
          .map(root::relativize)
          .map(Path::toString)
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
