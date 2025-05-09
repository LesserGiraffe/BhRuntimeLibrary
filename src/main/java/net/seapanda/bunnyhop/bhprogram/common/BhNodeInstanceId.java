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

package net.seapanda.bunnyhop.bhprogram.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * BunnyHop と BhProgram の実行環境間でノードのインスタンスを特定するための識別子.
 *
 * @author K.Koike
 */
public class BhNodeInstanceId implements Serializable {

  private final String id;
  /** ID が無いことを表す null オブジェクト. */
  public static final BhNodeInstanceId NONE = new BhNodeInstanceId("");

  private BhNodeInstanceId(String id) {
    this.id = id;
  }

  /**
   * {@link BhNodeInstanceId} を作成する.
   *
   * @param id 識別子名
   * @return {@link BhNodeInstanceId} オブジェクト.
   */
  public static BhNodeInstanceId of(String id) {
    return new BhNodeInstanceId(id == null ? "" : id);
  }

  @Override
  public String toString() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return (getClass() == obj.getClass()) && (id.equals(((BhNodeInstanceId) obj).id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.id);
  }
}

