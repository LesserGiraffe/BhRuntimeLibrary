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
 * BunnyHop で作成したプログラムの各種シンボルを特定するための ID.
 *
 * @author K.Koike
 */
public class BhSymbolId implements Serializable {

  private final String id;
  /** ID が無いことを表す null オブジェクト. */
  public static final BhSymbolId NONE = new BhSymbolId("");

  private BhSymbolId(String id) {
    this.id = id;
  }

  /**
   * {@link BhSymbolId} を作成する.
   *
   * @param id 識別子名
   * @return {@link BhSymbolId} オブジェクト.
   */
  public static BhSymbolId of(String id) {
    return new BhSymbolId(id == null ? "" : id);
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
    return (getClass() == obj.getClass()) && (id.equals(((BhSymbolId) obj).id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.id);
  }
}

