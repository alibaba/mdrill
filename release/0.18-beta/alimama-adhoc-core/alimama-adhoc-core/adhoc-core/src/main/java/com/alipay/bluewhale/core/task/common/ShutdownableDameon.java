package com.alipay.bluewhale.core.task.common;

import backtype.storm.daemon.Shutdownable;

import com.alipay.bluewhale.core.cluster.DaemonCommon;

public interface ShutdownableDameon extends  Shutdownable,DaemonCommon
{
	
}
