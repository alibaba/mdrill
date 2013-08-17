package com.alipay.bluewhale.core.command;

import java.util.Map;

import org.apache.thrift7.protocol.TBinaryProtocol;
import org.apache.thrift7.transport.TFramedTransport;
import org.apache.thrift7.transport.TSocket;

import backtype.storm.Config;
import backtype.storm.generated.Nimbus;

import com.alipay.bluewhale.core.cluster.ClusterState;
import com.alipay.bluewhale.core.cluster.DistributedClusterState;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.cluster.StormZkClusterState;
import com.alipay.bluewhale.core.task.error.TaskError;
import com.alipay.bluewhale.core.utils.StormUtils;

public class PrintStats {
    public static void main(String[] args) throws Exception {
	
	Map conf=StormConfig.read_storm_config();
	String host=String.valueOf(conf.get(Config.NIMBUS_HOST));
	Integer port=StormUtils.parseInt(conf.get(Config.NIMBUS_THRIFT_PORT));
	TFramedTransport transport=new TFramedTransport(new TSocket(host, port));
	TBinaryProtocol prot=new TBinaryProtocol(transport);
	Nimbus.Client client=new Nimbus.Client(prot);
	transport.open();
	try{
	    if(args[0].equals("cluster"))
	    {
		System.out.println(client.getClusterInfo().toString());

	    } 
	    else{
		String stromId=args[0];
		System.out.println(client.getTopologyInfo(stromId));
		
		
		ClusterState cluster_state = new DistributedClusterState(conf);
		StormClusterState zk = new StormZkClusterState(
			cluster_state);
		for(Integer taskid:zk.task_ids(stromId))
		{
			System.out.println("########"+taskid);
		    for(TaskError err:zk.task_errors(stromId, taskid))
		    {
			System.out.println(err.getError());
		    }

		}
		System.out.println("disconnect");

		zk.disconnect();


	    }
	}finally{
	    transport.close();
	}

    }
}
