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

package net.seapanda.bunnyhop.runtime.script;

/**
 * BhProgram 内で使用するキーワード.
 *
 * @author K.Koike
 */
public class Keywords {

  /** Javascript オブジェクトのプロパティ名. */
  public static class Properties {
    public static final String BH_SCRIPT_HELPER = "bhScriptHelper";
    public static final String CALL_STACK = "_callStack";
    public static final String CURRENT_NODE_INST_ID = "_currentNodeInstId";
    public static final String ERROR_MSGS = "_errorMsgs";
  }

  /** BhProgram に定義された関数名. */
  public static class Funcs {
    public static final String GET_EVENT_HANDLER_NAMES = "_getEventHandlerNames";
  }
}
