package com.alipay.bluewhale.core.daemon.supervisor;

import java.util.Map;

public interface SupervisorDaemon {
	
	public String getId();
	public Map getConf();
	public void ShutdownAllWorkers();

}
