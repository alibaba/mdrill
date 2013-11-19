package com.alipay.bluewhale.core.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alipay.bluewhale.core.task.acker.Acker;
import com.alipay.bluewhale.core.task.common.Assignment;
import com.alipay.bluewhale.core.task.common.TaskInfo;
import com.alipay.bluewhale.core.thrift.Thrift;
import com.alipay.bluewhale.core.utils.StormUtils;

import backtype.storm.Config;
import backtype.storm.generated.Bolt;
import backtype.storm.generated.ComponentCommon;
import backtype.storm.generated.GlobalStreamId;
import backtype.storm.generated.Grouping;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.SpoutSpec;
import backtype.storm.generated.StateSpoutSpec;
import backtype.storm.generated.StormTopology;
import backtype.storm.generated.StormTopology._Fields;
import backtype.storm.generated.StreamInfo;
import backtype.storm.task.IBolt;
import backtype.storm.utils.Utils;
/**
 * 提交的topology的验证
 * ack作为bolt
 * TOPOLOGY_ACKERS为并发数
 * StreamID: ACKER_INIT_STREAM_ID、ACKER_ACK_STREAM_ID和ACKER_FAIL_STREAM_ID
 * @author yannian
 *
 */
public class Common {
	
	   static List<String> sysEventFields = StormUtils.mk_list("event");

    public static boolean system_id(String id) {
        return Utils.isSystemId(id);
    }

    public static String ACKER_COMPONENT_ID = Acker.ACKER_COMPONENT_ID;
    public static String ACKER_INIT_STREAM_ID = Acker.ACKER_INIT_STREAM_ID;
    public static String ACKER_ACK_STREAM_ID = Acker.ACKER_ACK_STREAM_ID;
    public static String ACKER_FAIL_STREAM_ID = Acker.ACKER_FAIL_STREAM_ID;

    public static String SYSTEM_STREAM_ID = "__system";

    public static String LS_WORKER_HEARTBEAT = "worker-heartbeat";
    public static String LS_ID = "supervisor-id";
    public static String LS_LOCAL_ASSIGNMENTS = "local-assignments";
    public static String LS_APPROVED_WORKERS = "approved-workers";

    public static Integer[] storm_task_ids(StormClusterState stat,
            String storm_id) {
        HashSet<Integer> rtn = new HashSet<Integer>();
        Assignment ass = stat.assignment_info(storm_id, null);
        if (ass != null) {
            for (Integer task : ass.getTaskToNodeport().keySet()) {
                rtn.add(task);
            }
        }

        Integer[] rtnarr = new Integer[rtn.size()];
        return rtn.toArray(rtnarr);
    }

    public static void validate_distribute_mode(Map conf) {
        if (StormConfig.local_mode(conf)) {
            throw new IllegalArgumentException(
                    "Cannot start server in local mode!");
        }
    }

    public static HashMap<Integer, String> topology_task_info(
            StormClusterState zkCluster, String topologyid) {

        List<Integer> taks_ids = zkCluster.task_ids(topologyid);
        HashMap<Integer, String> rtn = null;
        if (taks_ids != null) {
        	rtn = new HashMap<Integer, String>();
            for (Integer task : taks_ids) {
                TaskInfo info = zkCluster.task_info(topologyid, task);
                String componment_id = info.getComponentId();
                rtn.put(task, componment_id);
            }
        }
        return rtn;
    }

    public static String get_storm_id(StormClusterState zkCluster,
            String storm_name) {
        List<String> active_storms = zkCluster.active_storms();
        String rtn = null;
        if (active_storms != null) {
            for (String strom_id : active_storms) {
                StormBase base = zkCluster.storm_base(strom_id, null);
                if (base != null && storm_name.equals(base.getStormName())) {
                    rtn = strom_id;
                    break;
                }
            }
        }
        return rtn;
    }

    public static HashMap<String, StormBase> topology_bases(
            StormClusterState zkCluster) {
        return get_storm_id(zkCluster);
    }

    public static HashMap<String, StormBase> get_storm_id(
            StormClusterState zkCluster) {
        HashMap<String, StormBase> rtn = new HashMap<String, StormBase>();
        List<String> active_storms = zkCluster.active_storms();
        if (active_storms != null) {
            for (String storm_id : active_storms) {
                StormBase base = zkCluster.storm_base(storm_id, null);
                if (base != null) {
                    rtn.put(storm_id, base);
                }
            }
        }
        return rtn;
    }

    static void validate_component(Object obj)
            throws InvalidTopologyException {

        if (obj instanceof StateSpoutSpec) {
            StateSpoutSpec spec = (StateSpoutSpec) obj;
            for (String id : spec.get_common().get_streams().keySet()) {
                if (system_id(id)) {
                    throw new InvalidTopologyException(id
                            + " is not a valid component id");
                }
            }

        }

        if (obj instanceof SpoutSpec) {
            SpoutSpec spec = (SpoutSpec) obj;
            for (String id : spec.get_common().get_streams().keySet()) {
                if (system_id(id)) {
                    throw new InvalidTopologyException(id
                            + " is not a valid component id");
                }
            }
        }
        if (obj instanceof Bolt) {
            Bolt spec = (Bolt) obj;
            for (String id : spec.get_common().get_streams().keySet()) {
                if (system_id(id)) {
                    throw new InvalidTopologyException(id
                            + " is not a valid component id");
                }
            }
        }

    }

 
    
    public static void add_system_streams(StormTopology topology) {
        for (Object obj : Common.all_components(topology)) {
            Common.add_component_system_streams(obj);
        }
    }

    public static StormTopology system_topology(Map storm_conf,StormTopology topology) throws InvalidTopologyException {
        Common.validate_basic(topology);
        StormTopology ret = topology.deepCopy();
        String key = Config.TOPOLOGY_ACKERS;
        Integer ackercount = StormUtils.parseInt(storm_conf.get(key));
        Common.add_acker(ackercount, ret);
        add_system_streams(ret);
        return ret;
    }

	/**
	 * 检查点 重复的ID Bolt or spout is not system_id componentIDis not systemid
	 * 
	 * @param topology
	 * @throws InvalidTopologyException
	 */
	public static void validate_ids(StormTopology topology)
	        throws InvalidTopologyException {
	    List<String> list = new ArrayList<String>();
	    for (StormTopology._Fields field : Thrift.STORM_TOPOLOGY_FIELDS) {
	        Object value = topology.getFieldValue(field);
	        if (value != null) {
	            Map<String, Object> obj_map = (Map<String, Object>) value;
	            Set<String> commids = obj_map.keySet();
	            list.addAll(commids);
	
	            for (String id : commids) {
	                if (system_id(id)) {
	                    throw new InvalidTopologyException(id
	                            + " is not a valid component id");
	                }
	            }
	
	            for (Object obj : obj_map.values()) {
	                validate_component(obj);
	            }
	        }
	    }
	
	    List<String> offending = StormUtils.getRepeat(list);
	    if (!offending.isEmpty()) {
	        throw new InvalidTopologyException("Duplicate component ids: "
	                + offending);
	    }
	
	}

	static void validate_component_inputs(Object obj)
	        throws InvalidTopologyException {
	    if (obj instanceof StateSpoutSpec) {
	        StateSpoutSpec spec = (StateSpoutSpec) obj;
	        if (!spec.get_common().get_inputs().isEmpty()) {
	            throw new InvalidTopologyException(
	                    "May not declare inputs for a spout");
	        }
	
	    }
	
	    if (obj instanceof SpoutSpec) {
	        SpoutSpec spec = (SpoutSpec) obj;
	        if (!spec.get_common().get_inputs().isEmpty()) {
	            throw new InvalidTopologyException(
	                    "May not declare inputs for a spout");
	        }
	    }
	}

	public static void validate_basic(StormTopology topology)
	        throws InvalidTopologyException {
	    validate_ids(topology);
	
	    for (StormTopology._Fields field : Thrift.SPOUT_FIELDS) {
	        Object value = topology.getFieldValue(field);
	        if (value != null) {
	            Map<String, Object> obj_map = (Map<String, Object>) value;
	            for (Object obj : obj_map.values()) {
	                validate_component_inputs(obj);
	            }
	        }
	
	    }
	
	}

	public static Map<GlobalStreamId, Grouping> acker_inputs(
	        StormTopology topology) {
	
	    Map<String, Bolt> bolt_ids = topology.get_bolts();
	    Map<String, SpoutSpec> spout_ids = topology.get_spouts();
	
	    Map<GlobalStreamId, Grouping> spout_inputs = new HashMap<GlobalStreamId, Grouping>();
	    for (Entry<String, SpoutSpec> spout : spout_ids.entrySet()) {
	        String id = spout.getKey();
	        GlobalStreamId stream = new GlobalStreamId(id, ACKER_INIT_STREAM_ID);
	        Grouping group = Thrift.mkFieldsGrouping(StormUtils.mk_list("id"));
	        spout_inputs.put(stream, group);
	    }
	
	    Map<GlobalStreamId, Grouping> bolt_inputs = new HashMap<GlobalStreamId, Grouping>();
	    for (Entry<String, Bolt> bolt : bolt_ids.entrySet()) {
	        String id = bolt.getKey();
	
	        GlobalStreamId streamAck = new GlobalStreamId(id,
	                ACKER_ACK_STREAM_ID);
	        Grouping groupAck = Thrift.mkFieldsGrouping(StormUtils
	                .mk_list("id"));
	
	        GlobalStreamId streamFail = new GlobalStreamId(id,
	                ACKER_FAIL_STREAM_ID);
	        Grouping groupFail = Thrift.mkFieldsGrouping(StormUtils
	                .mk_list("id"));
	
	        bolt_inputs.put(streamAck, groupAck);
	        bolt_inputs.put(streamFail, groupFail);
	    }
	
	    Map<GlobalStreamId, Grouping> allInputs = new HashMap<GlobalStreamId, Grouping>();
	    allInputs.putAll(bolt_inputs);
	    allInputs.putAll(spout_inputs);
	    return allInputs;
	}

	public static void add_acker(Integer num_tasks, StormTopology ret) {
	    HashMap<String, StreamInfo> outputs = new HashMap<String, StreamInfo>();
	    ArrayList<String> fields = new ArrayList<String>();
	    fields.add("id");
	
	    outputs.put(ACKER_ACK_STREAM_ID, Thrift.directOutputFields(fields));
	    outputs.put(ACKER_FAIL_STREAM_ID, Thrift.directOutputFields(fields));
	
	    IBolt ackerbolt= new Acker();
	    Map<GlobalStreamId, Grouping> inputs=acker_inputs(ret);
	    Bolt acker_bolt = Thrift.mkAckerBolt(inputs, ackerbolt, outputs, num_tasks);
	    for (Entry<String, Bolt> e : ret.get_bolts().entrySet()) {
	        Bolt bolt = e.getValue();
	        ComponentCommon common = bolt.get_common();
	        List<String> ackList = StormUtils.mk_list("id", "ack-val");
	        common.put_to_streams(ACKER_ACK_STREAM_ID,Thrift.outputFields(ackList));
	        
	        List<String> failList = StormUtils.mk_list("id");
	        common.put_to_streams(ACKER_FAIL_STREAM_ID,Thrift.outputFields(failList));
	        bolt.set_common(common);
	    }
	
	    for (Entry<String, SpoutSpec> kv : ret.get_spouts().entrySet()) {
	        SpoutSpec bolt = kv.getValue();
	        ComponentCommon common = bolt.get_common();
	        List<String> initList = StormUtils.mk_list("id", "init-val", "spout-task");
	        common.put_to_streams(ACKER_INIT_STREAM_ID,Thrift.outputFields(initList));
	        
	        GlobalStreamId ack_ack=new GlobalStreamId(ACKER_COMPONENT_ID, ACKER_ACK_STREAM_ID);
	        common.put_to_inputs(ack_ack, Thrift.mkDirectGrouping());
	        
	        GlobalStreamId ack_fail=new GlobalStreamId(ACKER_COMPONENT_ID,ACKER_FAIL_STREAM_ID);
	        common.put_to_inputs(ack_fail,Thrift.mkDirectGrouping());
	    }
	
	    ret.put_to_bolts("__acker", acker_bolt);
	}

	public static List<Object> all_components(StormTopology topology) {
	    List<Object> rtn = new ArrayList<Object>();
	    for (StormTopology._Fields field : Thrift.STORM_TOPOLOGY_FIELDS) {
	        Object fields = topology.getFieldValue(field);
	        if (fields != null) {
	            rtn.addAll(((Map) fields).values());
	        }
	    }
	    return rtn;
	}

	static void add_component_system_streams(Object obj)
	{
	    ComponentCommon common=null;
	    if (obj instanceof StateSpoutSpec) {
	        StateSpoutSpec spec = (StateSpoutSpec) obj;
	        common=spec.get_common();
	    }
	
	    if (obj instanceof SpoutSpec) {
	        SpoutSpec spec = (SpoutSpec) obj;
	        common=spec.get_common();
	    }
	    
	    if (obj instanceof Bolt) {
	        Bolt spec = (Bolt) obj;
	        common=spec.get_common();
	    }
	    
	    if(common!=null)
	    {
	        StreamInfo sinfo=Thrift.outputFields(sysEventFields);
	        common.put_to_streams(SYSTEM_STREAM_ID,sinfo);
	    }
	}

}
