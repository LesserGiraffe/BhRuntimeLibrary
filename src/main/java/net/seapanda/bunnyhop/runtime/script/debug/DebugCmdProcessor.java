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

package net.seapanda.bunnyhop.runtime.script.debug;

import java.util.SequencedCollection;
import java.util.concurrent.BlockingQueue;
import net.seapanda.bunnyhop.bhprogram.common.message.BhProgramResponse;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.AddBreakpointsCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.AddBreakpointsResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.BhDebugCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.BhDebugResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetEntryPointsCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetEntryPointsResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetGlobalListValsCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetGlobalListValsResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetGlobalVarsCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetGlobalVarsResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetLocalListValsCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetLocalListValsResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetLocalVarsCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetLocalVarsResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetThreadContextsCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.GetThreadContextsResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.RemoveBreakpointsCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.RemoveBreakpointsResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.ResumeThreadCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.ResumeThreadResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.SetBreakpointsCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.SetBreakpointsResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.StepIntoCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.StepIntoResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.StepOutCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.StepOutResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.StepOverCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.StepOverResp;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.SuspendThreadCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.debug.SuspendThreadResp;
import net.seapanda.bunnyhop.bhprogram.common.message.thread.BhVarStackFrame;
import net.seapanda.bunnyhop.bhprogram.common.message.variable.BhListVariable;
import net.seapanda.bunnyhop.bhprogram.common.message.variable.BhVariable;
import net.seapanda.bunnyhop.runtime.script.BhProgramMessageProcessor;
import net.seapanda.bunnyhop.runtime.service.LogManager;

/**
 * {@link BhDebugCmd} を処理するクラス.
 *
 * @author K.Koike
 */
public class DebugCmdProcessor implements BhProgramMessageProcessor<BhDebugCmd> {
  
  private final Debugger debugger;
  /** 発行したレスポンスを格納する FIFO. */
  private final BlockingQueue<BhProgramResponse> sendRespList;

  /** コンストラクタ. */
  public DebugCmdProcessor(Debugger debugger, BlockingQueue<BhProgramResponse> sendRespList) {
    this.debugger = debugger;
    this.sendRespList = sendRespList;
  }

  @Override
  public void process(BhDebugCmd debugCmd) {
    BhDebugResp resp = switch (debugCmd) {
      case AddBreakpointsCmd cmd -> process(cmd);
      case SuspendThreadCmd cmd -> process(cmd);
      case GetGlobalListValsCmd cmd -> process(cmd);
      case GetGlobalVarsCmd cmd -> process(cmd);
      case GetLocalListValsCmd cmd -> process(cmd);
      case GetLocalVarsCmd cmd -> process(cmd);
      case GetThreadContextsCmd cmd -> process(cmd);
      case GetEntryPointsCmd cmd -> process(cmd);
      case RemoveBreakpointsCmd cmd -> process(cmd);
      case ResumeThreadCmd cmd -> process(cmd);
      case SetBreakpointsCmd cmd -> process(cmd);
      case StepIntoCmd cmd -> process(cmd);
      case StepOutCmd cmd -> process(cmd);
      case StepOverCmd cmd -> process(cmd);
      default -> null;
    };
    try {
      if (resp != null) {
        sendRespList.put(resp);
      }
    } catch (InterruptedException e) {
      LogManager.logger().error(e.toString());
    }
  }

  private AddBreakpointsResp process(AddBreakpointsCmd cmd) {
    try {
      debugger.addBreakpoints(cmd.breakpoints);
      return new AddBreakpointsResp(cmd.getId(), true);
    } catch (Exception e) {
      return new AddBreakpointsResp(cmd.getId(), e);
    }
  }

  private SuspendThreadResp process(SuspendThreadCmd cmd) {
    try {
      if (cmd.threadId == SuspendThreadCmd.ALL_THREADS) {
        debugger.suspendAll();
      } else {
        debugger.suspend(cmd.threadId);
      }
      return new SuspendThreadResp(cmd.getId(), true);
    } catch (Exception e) {
      return new SuspendThreadResp(cmd.getId(), e);
    }
  }

  private GetGlobalListValsResp process(GetGlobalListValsCmd cmd) {
    try {
      BhListVariable listVar = debugger.getGlobalListValues(cmd.varId, cmd.startIdx, cmd.length);
      return new GetGlobalListValsResp(cmd.getId(), listVar);
    } catch (Exception e) {
      return new GetGlobalListValsResp(cmd.getId(), e);
    }
  }

  private GetGlobalVarsResp process(GetGlobalVarsCmd cmd) {
    try {
      SequencedCollection<BhVariable> vars = debugger.getGlobalVariables();
      return new GetGlobalVarsResp(cmd.getId(), vars);
    } catch (Exception e) {
      return new GetGlobalVarsResp(cmd.getId(), e);
    }
  }

  private GetLocalListValsResp process(GetLocalListValsCmd cmd) {
    try {
      BhListVariable listVar = debugger.getLocalListValues(
          cmd.threadId, cmd.frameIdx, cmd.varId, cmd.startIdx, cmd.length);
      return new GetLocalListValsResp(
          cmd.getId(), new GetLocalListValsResp.Result(cmd.threadId, cmd.frameIdx, listVar));
    } catch (Exception e) {
      return new GetLocalListValsResp(cmd.getId(), e);
    }
  }

  private GetLocalVarsResp process(GetLocalVarsCmd cmd) {
    try {
      SequencedCollection<BhVariable> vars = debugger.getLocalVariables(cmd.threadId, cmd.frameIdx);
      var varStackFrame = new BhVarStackFrame(cmd.frameIdx, vars);
      return new GetLocalVarsResp(
          cmd.getId(), new GetLocalVarsResp.Result(cmd.threadId, varStackFrame));
    } catch (Exception e) {
      return new GetLocalVarsResp(cmd.getId(), e);
    }
  }

  private GetThreadContextsResp process(GetThreadContextsCmd cmd) {
    try {
      debugger.sendThreadContexts();
      return new GetThreadContextsResp(cmd.getId(), true);
    } catch (Exception e) {
      return new GetThreadContextsResp(cmd.getId(), e);
    }
  }

  private GetEntryPointsResp process(GetEntryPointsCmd cmd) {
    try {
      return new GetEntryPointsResp(cmd.getId(), debugger.getEntryPointIds());
    } catch (Exception e) {
      return new GetEntryPointsResp(cmd.getId(), e);
    }
  }

  private RemoveBreakpointsResp process(RemoveBreakpointsCmd cmd) {
    try {
      debugger.removeBreakpoints(cmd.breakpoints);
      return new RemoveBreakpointsResp(cmd.getId(), true);
    } catch (Exception e) {
      return new RemoveBreakpointsResp(cmd.getId(), e);
    }
  }

  private ResumeThreadResp process(ResumeThreadCmd cmd) {
    try {
      if (cmd.threadId == ResumeThreadCmd.ALL_THREADS) {
        debugger.resumeAll();
      } else {
        debugger.resume(cmd.threadId);
      }
      return new ResumeThreadResp(cmd.getId(), true);
    } catch (Exception e) {
      return new ResumeThreadResp(cmd.getId(), e);
    }
  }

  private SetBreakpointsResp process(SetBreakpointsCmd cmd) {
    try {
      debugger.setBreakpoints(cmd.breakpoints);
      return new SetBreakpointsResp(cmd.getId(), true);
    } catch (Exception e) {
      return new SetBreakpointsResp(cmd.getId(), e);
    }
  }

  private StepIntoResp process(StepIntoCmd cmd) {
    try {
      debugger.stepInto(cmd.threadId);
      return new StepIntoResp(cmd.getId(), true);
    } catch (Exception e) {
      return new StepIntoResp(cmd.getId(), e);
    }
  }

  private StepOutResp process(StepOutCmd cmd) {
    try {
      debugger.stepOut(cmd.threadId);
      return new StepOutResp(cmd.getId(), true);
    } catch (Exception e) {
      return new StepOutResp(cmd.getId(), e);
    }
  }

  private StepOverResp process(StepOverCmd cmd) {
    try {
      debugger.stepOver(cmd.threadId);
      return new StepOverResp(cmd.getId(), true);
    } catch (Exception e) {
      return new StepOverResp(cmd.getId(), e);
    }
  }
}
