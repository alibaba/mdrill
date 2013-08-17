package com.alipay.bluewhale.core.command;

import java.util.Map;

import org.apache.thrift7.TException;
import org.apache.thrift7.protocol.TBinaryProtocol;
import org.apache.thrift7.transport.TFramedTransport;
import org.apache.thrift7.transport.TSocket;

import backtype.storm.Config;
import backtype.storm.generated.KillOptions;
import backtype.storm.generated.Nimbus;
import backtype.storm.generated.NotAliveException;

import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.utils.StormUtils;
/**
 * kill topology，用来被 python脚本所调用
 * @author yannian
 *
 */
public class KillTopology {
    public static void main(String[] args) throws NotAliveException, TException {
	
	String name=args[0];
	KillOptions ops= new KillOptions();
	if (args.length > 1) {
	    Integer wait = 0;
	    wait = StormUtils.parseInt(args[1]);
	    ops.set_wait_secs(wait);
	}
	
	Map conf=StormConfig.read_storm_config();
	String host=String.valueOf(conf.get(Config.NIMBUS_HOST));
	Integer port=StormUtils.parseInt(conf.get(Config.NIMBUS_THRIFT_PORT));
	TFramedTransport transport=new TFramedTransport(new TSocket(host, port));
	TBinaryProtocol prot=new TBinaryProtocol(transport);
	Nimbus.Client client=new Nimbus.Client(prot);
	transport.open();
	try{
	    client.killTopologyWithOpts(name,ops);
	}finally{
	    transport.close();
	}

    }
}
