/**
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
module net.seapanda.bhprogramexecenv {
  requires java.rmi;
  requires java.base;
  requires java.desktop;  //for javax.audio in the bhAppScript
  requires rhino;
  requires javafx.fxml;

  exports net.seapanda.bunnyhop.programexecenv;
  exports net.seapanda.bunnyhop.programexecenv.socket;
  exports net.seapanda.bunnyhop.programexecenv.script;
  exports net.seapanda.bunnyhop.programexecenv.lib;
  exports net.seapanda.bunnyhop.bhprogram.common;
}
