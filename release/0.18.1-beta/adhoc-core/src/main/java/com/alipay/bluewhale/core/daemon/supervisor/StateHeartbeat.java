package com.alipay.bluewhale.core.daemon.supervisor;

import com.alipay.bluewhale.core.daemon.State;
import com.alipay.bluewhale.core.work.refresh.WorkerHeartbeat;
/**
 *worker的心跳和worker的状态
 */
public class StateHeartbeat {
	private State state;
	private WorkerHeartbeat hb;

	public StateHeartbeat(State state, WorkerHeartbeat hb) {
		this.state = state;
		this.hb = hb;
	}

	public State getState() {
		return this.state;
	}

	public WorkerHeartbeat getHeartbeat() {
		return this.hb;
	}

}
