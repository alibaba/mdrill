package com.alimama.mdrill.topology.assignment;

import java.util.HashSet;
import java.util.Map;

import com.alimama.mdrill.topology.MdrillTaskAssignment;

public class SupervisorPortType {

	private HashSet<Integer> msPorts = new HashSet<Integer>();
	private HashSet<Integer> rPorts = new HashSet<Integer>();
	public void setup(Map topology_conf)
	{
		String[] ports = String.valueOf(topology_conf.get(MdrillTaskAssignment.MS_PORTS)).trim().split(",");
		for (String p : ports) {
			this.msPorts.add(Integer.parseInt(p.trim()));
		}
	
	}
	
	boolean isMergerPort(int p)
	{
		return msPorts.contains(p);
	}
	
	boolean isRealTimePort(int p)
	{
		return rPorts.contains(p);
	}
	
	public boolean isType(PortTypeEnum t,int p)
	{
		if(t.equals(PortTypeEnum.mergerserver))
		{
			return isMergerPort(p);
		}else if(t.equals(PortTypeEnum.realtime))
		{
			return isRealTimePort(p);
		}

		return !isRealTimePort(p)&&!isMergerPort(p);
	}

}
