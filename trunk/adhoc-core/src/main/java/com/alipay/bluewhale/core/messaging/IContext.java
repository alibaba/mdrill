package com.alipay.bluewhale.core.messaging;

public interface IContext extends IZMQContext {
	public IConnection bind(String topologyId, int virtual_port);

	public IConnection connect(String topologyId, String host, int port);

	public void send_local_task_empty(String topologyId, int virtual_port);

	public void term();
}
