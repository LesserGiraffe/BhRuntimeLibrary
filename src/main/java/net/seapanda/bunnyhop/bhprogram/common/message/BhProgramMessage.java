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

package net.seapanda.bunnyhop.bhprogram.common.message;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BunnyHop と BhProgram の実行環境間で送受信されるメッセージ.
 *
 * @author K.Koike
 */
public interface BhProgramMessage extends Serializable {

  public static final AtomicLong nextId = new AtomicLong(1);
  
  public default long genId() {
    return nextId.getAndIncrement();
  }

  public long getId();
}
