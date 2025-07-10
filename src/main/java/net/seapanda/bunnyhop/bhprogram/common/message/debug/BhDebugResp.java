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

package net.seapanda.bunnyhop.bhprogram.common.message.debug;

import java.util.Optional;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;

/**
 * {@link BhDebugCmd} のレスポンス.
 *
 * @author K.Koike
 */
public abstract class BhDebugResp implements BhProgramResponse {

  private final long id;
  private final boolean success;
  private final Exception exception;

  protected BhDebugResp(long id, boolean success) {
    this.id = id;
    this.success = success;
    this.exception = null;
  }

  protected BhDebugResp(long id, boolean success, Exception exception) {
    this.id = id;
    this.success = success;
    this.exception = exception;
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public boolean isSuccessful() {
    return success;
  }

  /**
   * コマンドの実行中に発生した例外を取得する.
   *
   * @return コマンドの実行中に発生した例外.
   */
  public Optional<Exception> getException() {
    return Optional.ofNullable(exception);
  }
}
