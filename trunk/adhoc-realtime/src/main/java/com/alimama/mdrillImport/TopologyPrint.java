package com.alimama.mdrillImport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alimama.mdrill.topology.MdrillDefaultTaskAssignment;
import com.alimama.mdrill.topology.MdrillTaskAssignment;
import com.alipay.bluewhale.core.custom.CustomAssignment;
import com.alipay.bluewhale.core.utils.StormUtils;


import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

public class TopologyPrint {
	public static class TopologyText extends TopologyContext
	{
		static Map<Integer, String> taskToComponent=new HashMap<Integer, String>();
		static {
			taskToComponent.put(0, "text");
		}
		public TopologyText() {
			super(null, taskToComponent, "", "", "", 0);
		}
		
	}
	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
		Map stormconf = Utils.readStormConfig();
		Config conf = new Config();
		conf.putAll(stormconf);
		conf.setMessageTimeoutSecs(10);
		
		String topologyName=args[0];
		String[] prefixlist=args[1].split(",");
		TopologyBuilder builder = new TopologyBuilder();
		
		if(args.length>=6)
		{
			String tttime=args[5];
			for(String prefix:prefixlist)
			{
				conf.put(prefix+"-start-time", tttime);
			}
		}
		
		for(String prefix:prefixlist)
		{
			conf.put(prefix+"-validate-time", System.currentTimeMillis());
		}
		
		
		for(String prefix:prefixlist)
		{
			ImportSpoutLocalForParseTest test=new ImportSpoutLocalForParseTest(prefix);
			test.open(conf,new TopologyText());
			while(true)
			{
				test.nextTuple();
			}
		}
		

	}
}
