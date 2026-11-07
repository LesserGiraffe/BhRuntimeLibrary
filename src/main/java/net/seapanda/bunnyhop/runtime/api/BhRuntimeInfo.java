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

package net.seapanda.bunnyhop.runtime.api;

import net.seapanda.bunnyhop.utility.version.AppVersion;

/**
 * BhRuntimeLibrary 自身の情報を取得する機能を規定したクラス.
 *
 * @author K.Koike
 */
public class BhRuntimeInfo {

  /** BhRuntimeLibrary の名前. */
  private static final String NAME = "BhRuntime";
  /** BhRuntimeLibrary のバージョン. */
  private static final AppVersion VERSION = AppVersion.of("bhrun-0.7.9");

  /**
   * BhRuntimeLibrary の名前を取得する.
   *
   * @return BhRuntimeLibrary の名前
   */
  public static String getName() {
    return NAME;
  }

  /**
   * BhRuntimeLibrary のバージョンを取得する.
   *
   * @return BhRuntimeLibrary のバージョン
   */
  public static AppVersion getVersion() {
    return VERSION;
  }

  private BhRuntimeInfo() {}
}
