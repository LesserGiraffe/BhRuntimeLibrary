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

import java.nio.file.Paths;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import net.seapanda.bunnyhop.bhprogram.common.BhRuntimeFacade;
import net.seapanda.bunnyhop.bhprogram.common.LocalClientSocketFactory;
import net.seapanda.bunnyhop.bhprogram.common.RemoteClientSocketFactory;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramEvent;
import net.seapanda.bunnyhop.runtime.executor.JsBhProgramExecutor;
import net.seapanda.bunnyhop.runtime.script.Keywords;
import net.seapanda.bunnyhop.runtime.script.MessageQueueSet;
import net.seapanda.bunnyhop.runtime.script.ScriptHelper;
import net.seapanda.bunnyhop.runtime.script.hw.HwCmdDispatcher;
import net.seapanda.bunnyhop.runtime.script.hw.StdioHwCmdDispatcher;
import net.seapanda.bunnyhop.runtime.script.io.BhTextInputAgent;
import net.seapanda.bunnyhop.runtime.script.io.BhTextOutputAgent;
import net.seapanda.bunnyhop.runtime.script.simulator.BhSimulatorAgent;
import net.seapanda.bunnyhop.runtime.service.LogManager;
import net.seapanda.bunnyhop.runtime.socket.LocalServerSocketFactory;
import net.seapanda.bunnyhop.runtime.socket.RemoteServerSocketFactory;
import net.seapanda.bunnyhop.utility.Utility;
import net.seapanda.bunnyhop.utility.log.FileLogger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * メインクラス.
 *
 * @author K.Koike
 */
public class AppMain {

  /** メインメソッド. */
  public static void main(String[] args) {
    FileLogger logger = null;
    try {
      logger = new FileLogger(
          Paths.get(Utility.execPath, BhConstants.Path.LOG_DIR, BhConstants.APP_NAME),
          BhConstants.Path.LOG_FILE_NAME,
          BhConstants.LOG_FILE_SIZE_LIMIT,
          BhConstants.MAX_LOG_FILE_NUM);
      LogManager.initialize(logger);
    } catch (Exception e) {
      System.out.println(e);
      System.exit(-1);
    }
    var options = new Options();
    CommandLine cmd = parseCmd(args, options, logger);
    boolean isLocal = !cmd.hasOption("remote");
    boolean enableHwCtrl = cmd.hasOption("hwctrl");

    if (cmd.hasOption("help")) {
      HelpFormatter hf = new HelpFormatter();
      hf.printHelp("[opts]", options);
      return;
    }
    if (cmd.hasOption("version")) {
      System.out.println(BhConstants.APP_VERSION.toString());
      return;
    }
    if (cmd.hasOption("run")) {
      executeScript(cmd.getOptionValue("run"), enableHwCtrl);
    } else {
      exportRmiObject(isLocal, enableHwCtrl);
    }
  }

  /** コマンドライン引数をパースする. */
  private static CommandLine parseCmd(String[] args, Options options, FileLogger logger) {
    options.addOption(Option.builder()
        .longOpt("remote")
        .hasArg(false)
        .desc("If set, BhRuntime can communicate with a remote machine.")
        .build());

    options.addOption(Option.builder()
        .longOpt("hwctrl")
        .hasArg(false)
        .desc("If set, BhRuntime uses hwctrl.")
        .build());

    options.addOption(Option.builder()
        .longOpt("version")
        .hasArg(false)
        .desc("Output the version of BhRuntime and exit.")
        .build());

    options.addOption(Option.builder()
        .longOpt("run")
        .hasArg(true)
        .desc(
        """
        Run a script immediately.
        Specify the script path to execute as an absolute path or a relative path from the runtime path.
        """)
        .build());

    options.addOption(Option.builder()
        .longOpt("help")
        .hasArg(false)
        .desc("Print help about BhRuntime environment variables and exit.")
        .build());

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      String msg = "Invalid command-line arguments.\n%s".formatted(e);
      logger.error(msg);
      System.err.println(msg);
    }
    return cmd;
  }

  /**
   * BunnyHop と通信するための RMI オブジェクトをエクスポートする.
   *
   * @param isLocal BunnyHop と同じマシン上で動作する場合 true
   * @param enableHwCtrl BhRuntime による HW 制御機能を有効にする場合 true
   */
  private static void exportRmiObject(boolean isLocal, boolean enableHwCtrl) {
    try {
      BhRuntimeFacade facade = createRuntimeFacade(enableHwCtrl);
      Remote remote = UnicastRemoteObject.exportObject(
          facade,
          0,
          createClientSocketFactory(0, isLocal),
          createServerSocketFactory(0, isLocal));
      RMIServerSocketFactory socketFactory = createServerSocketFactory(1, isLocal);
      Registry registry = LocateRegistry.createRegistry(
          0,
          RMISocketFactory.getDefaultSocketFactory(),
          socketFactory);
      registry.rebind(BhRuntimeFacade.class.getSimpleName(), remote);
      outputLocalTcpPort(socketFactory);
    } catch (Exception e) {
      System.out.println("\n-1" + BhConstants.BhProgram.RIM_TCP_PORT_SUFFIX);  //don't remove
      LogManager.logger().error("Failed to export an rmi object.\n" + e);
    }
  }

  private static void outputLocalTcpPort(RMIServerSocketFactory socketFactory) {
    if (socketFactory instanceof LocalServerSocketFactory localSocketFactory) {
      System.out.println("\n"
          + localSocketFactory.getLocalPort() 
          + BhConstants.BhProgram.RIM_TCP_PORT_SUFFIX);  //don't remove
    } else {
      System.out.println("\n"
          + ((RemoteServerSocketFactory) socketFactory).getLocalPort() 
          + BhConstants.BhProgram.RIM_TCP_PORT_SUFFIX);  //don't remove
    }
  }

  private static RMIClientSocketFactory createClientSocketFactory(int id, boolean isLocal) {
    return isLocal ? new LocalClientSocketFactory(id) : new RemoteClientSocketFactory(id);
  }

  private static RMIServerSocketFactory createServerSocketFactory(int id, boolean isLocal) {
    return isLocal ? new LocalServerSocketFactory(id) : new RemoteServerSocketFactory(id);
  }

  /**
   * 引数で指定したスクリプトを実行する.
   *
   * @param fileName スクリプトファイル名
   * @param enableHwCtrl BhRuntime による HW 制御を有効にする場合 true
   */
  private static void executeScript(String fileName, boolean enableHwCtrl) {
    try {
      String cmd = Paths.get(
          Utility.execPath, BhConstants.Path.ACTIONS, BhConstants.Path.HW_CTRL).toString();
      var dispatcher = enableHwCtrl ? new StdioHwCmdDispatcher(cmd) : new HwCmdDispatcher() {};
      var queueSet = new MessageQueueSet();
      var simAgent = new BhSimulatorAgent(queueSet.sendNotifList());
      var textInAgent = new BhTextInputAgent(queueSet.sendRespList());
      var textOutAgent = new BhTextOutputAgent(queueSet.sendNotifList(), true);
      var helper =
          new ScriptHelper(textInAgent, textOutAgent, simAgent, dispatcher);
      var executor = new JsBhProgramExecutor(helper, queueSet.sendNotifList());
      var shell = new BhProgramShell(queueSet, executor, textInAgent, textOutAgent);
      var event = new BhProgramEvent(
          BhProgramEvent.Name.PROGRAM_START, Keywords.Funcs.GET_EVENT_HANDLER_NAMES);
      shell.runScript(fileName, event);
    } catch (Exception e) {
      String msg = "Failed to run a script.  (%s)\n%s".formatted(fileName, e);
      System.err.println(msg);
      LogManager.logger().error(msg);
    }
  }

  /**
   * {@link BhRuntimeFacade} オブジェクトを作成する.
   *
   * @param enableHwCtrl BhRuntime による HW 制御を有効にする場合 true
   * @return {@link BhRuntimeFacade} オブジェクト
   * @throws Exception {@link BhRuntimeFacade} オブジェクトの作成に失敗した場合
   */
  private static BhRuntimeFacade createRuntimeFacade(boolean enableHwCtrl) throws Exception {
    String cmd = Paths.get(
        Utility.execPath, BhConstants.Path.ACTIONS, BhConstants.Path.HW_CTRL).toString();
    var dispatcher = enableHwCtrl ? new StdioHwCmdDispatcher(cmd) : new HwCmdDispatcher() {};
    var queueSet = new MessageQueueSet();
    var simAgent = new BhSimulatorAgent(queueSet.sendNotifList());
    var textInAgent = new BhTextInputAgent(queueSet.sendRespList());
    var textOutAgent = new BhTextOutputAgent(queueSet.sendNotifList(), false);
    var helper =
        new ScriptHelper(textInAgent, textOutAgent, simAgent, dispatcher);
    var executor = new JsBhProgramExecutor(helper, queueSet.sendNotifList());
    var facade = new BhRuntimeFacadeImpl(
        queueSet, executor, textInAgent, textOutAgent, resp -> {}, simAgent);
    setEventHandlers(textOutAgent, facade);
    return facade;
  }

  private static void setEventHandlers(BhTextOutputAgent agent, BhRuntimeFacadeImpl facade) {
    facade.getEventManager().addOnConnected(() -> agent.enableTextOutput());
    facade.getEventManager().addOnDisconnected(() -> agent.disableTextOutput());
    facade.disconnect();
  }
}
