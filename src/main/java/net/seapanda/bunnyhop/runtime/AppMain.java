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

package net.seapanda.bunnyhop.runtime;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import net.seapanda.bunnyhop.bhprogram.common.BhProgramHandler;
import net.seapanda.bunnyhop.bhprogram.common.LocalClientSocketFactory;
import net.seapanda.bunnyhop.bhprogram.common.RemoteClientSocketFactory;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.runtime.script.ScriptParams;
import net.seapanda.bunnyhop.runtime.service.BhService;
import net.seapanda.bunnyhop.runtime.socket.LocalServerSocketFactory;
import net.seapanda.bunnyhop.runtime.socket.RemoteServerSocketFactory;

/**
 * メインクラス.
 *
 * @author K.Koike
 */
public class AppMain {

  /** メインメソッド. */
  public static void main(String[] args) {
    if (args.length >= 1) {
      if (args[0].equals("--version")) {
        System.out.println(
            AppMain.class.getSimpleName()
            + " version " + VersionInfo.APP_VERSION);
        return;
      }
      
      if (!BhService.initialize()) {
        System.exit(-1);
      }
      if (args[0].equals("-run") && args.length >= 2) {
        executeScript(args[1]);
      } else {
        exportRmiObject(Boolean.valueOf(args[0]));
      }
      BhService.msgPrinter().close();
    }
  }

  /** BunnyHop と通信するための RMI オブジェクトをエクスポートする. */
  private static void exportRmiObject(boolean isLocal) {
    try {
      BhProgramHandlerImpl programHandler = new BhProgramHandlerImpl();
      Remote remote = UnicastRemoteObject.exportObject(
          programHandler,
          0,
          isLocal ? new LocalClientSocketFactory(0) : new RemoteClientSocketFactory(0),
          isLocal ? new LocalServerSocketFactory(0) : new RemoteServerSocketFactory(0));
      RMIServerSocketFactory socketFactory =
          isLocal ? new LocalServerSocketFactory(1) : new RemoteServerSocketFactory(1);
      Registry registry = LocateRegistry.createRegistry(
          0,
          RMISocketFactory.getDefaultSocketFactory(),
          socketFactory);
      registry.rebind(BhProgramHandler.class.getSimpleName(), remote);

      if (socketFactory instanceof LocalServerSocketFactory localSocketFactory) {
        System.out.println("\n"
            + localSocketFactory.getLocalPort() 
            + BhConstants.BhProgram.RIM_TCP_PORT_SUFFIX);  //don't remove
      } else {
        System.out.println("\n"
            + ((RemoteServerSocketFactory) socketFactory).getLocalPort() 
            + BhConstants.BhProgram.RIM_TCP_PORT_SUFFIX);  //don't remove
      }
    } catch (Exception e) {
      System.out.println("\n" + "null" + BhConstants.BhProgram.RIM_TCP_PORT_SUFFIX);  //don't remove
      BhService.msgPrinter().errForDebug("Failed to export an rmi object.\n" + e);
    }
  }

  /**
   * 引数で指定したスクリプトを実行する.
   *
   * @param fileName スクリプトファイル名
   */
  private static void executeScript(String fileName) {
    BhProgramShell programHandler = new BhProgramShell();
    var event = new BhProgramEvent(
        BhProgramEvent.Name.PROGRAM_START, ScriptParams.Funcs.GET_EVENT_HANDLER_NAMES);
    programHandler.runScript(fileName, event);
  }
}
