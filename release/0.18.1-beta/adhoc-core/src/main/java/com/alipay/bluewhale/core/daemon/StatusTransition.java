package com.alipay.bluewhale.core.daemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.generated.NotAliveException;

import com.alipay.bluewhale.core.callback.Callback;
import com.alipay.bluewhale.core.callback.impl.ActiveTransitionCallback;
import com.alipay.bluewhale.core.callback.impl.DelayRebalanceTransitionCallback;
import com.alipay.bluewhale.core.callback.impl.DelayRemoveTransitionCallback;
import com.alipay.bluewhale.core.callback.impl.InactiveTransitionCallback;
import com.alipay.bluewhale.core.callback.impl.KillTransitionCallback;
import com.alipay.bluewhale.core.callback.impl.ReassignTransitionCallback;
import com.alipay.bluewhale.core.callback.impl.RebalanceTransitionCallback;
import com.alipay.bluewhale.core.callback.impl.DoRebalanceTransitionCallback;
import com.alipay.bluewhale.core.callback.impl.RemoveTransitionCallback;
import com.alipay.bluewhale.core.cluster.Common;
import com.alipay.bluewhale.core.cluster.StormBase;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormStatus;

/**
 * 状态转换类
 * 
 * @author lixin 2012-3-21 下午4:17:10
 * 
 */
public class StatusTransition {

	private final static Logger LOG = Logger.getLogger(StatusTransition.class);
	 
	//所有状态转换集合
	//FIXME 建议挪动到transition方法里面，没必要弄一个静态的成员变量
	private static Map<StatusType, Map<StatusType, Callback>> rtn;
	
	//系统事件---:startup和:monitor集合
	private static List<String>system_events;
	
	/**
	 * 状态转换触发对应事件
	 * 
	 * @param data
	 *            nimbus数据对象
	 * @param topologyId
	 *            topologyid
	 * @param errorOnNTransition
	 * @param eventtype
	 *            要转换事件类型
	 * @param args
	 *            事件参数
	 */
	public static <T> void transition(NimbusData data, String topologyid,
			boolean errorOnNoTransition, StatusType transition_status, T... args) {
		synchronized (data.getSubmitLock()) {
			// 加锁，防止clean和submittopology等操作
			if(system_events==null){
				//系统事件---:startup和:monitor集合
				system_events=new ArrayList<String>();
				system_events.add(StatusType.startup.getStatus());
				system_events.add(StatusType.monitor.getStatus());
			}
			//获取指定topology对应的状态对象
			StormBase stormbase = data.getStormClusterState().storm_base(topologyid, null);
			StormStatus topologyStatus=null;
			if (stormbase != null){
			    topologyStatus = stormbase.getStatus();
			}
			if (topologyStatus != null) {
				
				//if(rtn==null){
					//必须每次获取所有状态转换规则
					rtn=stateTransitions(data,topologyid, topologyStatus);
				//}
				//获取当前topology状态可转换的状态集合
				Map<StatusType, Callback> transition_map=rtn.get(topologyStatus.getStatusType());
				
				if(transition_map.containsKey(transition_status)){
					//获取转换新对象执行方法并执行。
					Callback callback=transition_map.get(transition_status);
					//返回新的状态对象StormStatus
					if(callback!=null){
						Object obj=callback.execute(args);
						if (obj != null && obj instanceof StormStatus) {
							StormStatus newStatus=(StormStatus)obj;
							//更新状态
						    data.getStormClusterState().update_storm(topologyid, newStatus);
							LOG.info("Updated " + topologyid + " with status " + newStatus);
						}	
					}
				}else{
					String msg = "No transition for event: " + transition_status.getStatus() + ", status: " + topologyStatus + " topology-id: " + topologyid;
			    	        if (errorOnNoTransition) {
			    		      throw new RuntimeException(msg);
			    	       }else if (system_events.contains(transition_status.getStatus())) {
			    		LOG.info(msg);
			    	     }
				}
				
			}else{
				LOG.info("Cannot apply event " + transition_status.getStatus() + " to " + topologyid + " because topology no longer exists");
			}

		}
	}



	public static <T> void transitionName(NimbusData data, String topologyName,
		boolean errorOnNoTransition, StatusType transition_status, T... args) throws NotAliveException {
	    StormClusterState stormClusterState = data.getStormClusterState();
	    String topologyId = Common.get_storm_id(stormClusterState, topologyName);
	    if (topologyId == null){
		throw new NotAliveException(topologyName);
	    }
	    transition(data, topologyId, errorOnNoTransition, transition_status, args);
	}
	
	/**
	 * 初始化状态类型及对应处理逻辑
	 * 
	 * @param data
	 * @param topologyid
	 * @param status
	 * @return
	 */
	private static Map<StatusType, Map<StatusType, Callback>> stateTransitions(NimbusData data,
			String topologyid, StormStatus status) {

		rtn = new HashMap<StatusType, Map<StatusType, Callback>>();

		Map<StatusType, Callback> activeMap = new HashMap<StatusType, Callback>();
		activeMap.put(StatusType.monitor, new ReassignTransitionCallback(data,topologyid));
		activeMap.put(StatusType.inactive, new InactiveTransitionCallback());
		activeMap.put(StatusType.activate, null);
		activeMap.put(StatusType.rebalance, new RebalanceTransitionCallback(data,topologyid, status));
		activeMap.put(StatusType.kill, new KillTransitionCallback(data, topologyid,status));
		rtn.put(StatusType.active, activeMap);

		Map<StatusType, Callback> inactiveMap = new HashMap<StatusType, Callback>();
		inactiveMap.put(StatusType.monitor, new ReassignTransitionCallback(data,topologyid));
		inactiveMap.put(StatusType.activate, new ActiveTransitionCallback());
		inactiveMap.put(StatusType.inactivate, null);
		inactiveMap.put(StatusType.rebalance, new RebalanceTransitionCallback(data,topologyid, status));
		inactiveMap.put(StatusType.kill, new KillTransitionCallback(data, topologyid,status));

		rtn.put(StatusType.inactive, inactiveMap);

		Map<StatusType, Callback> killedMap = new HashMap<StatusType, Callback>();
		killedMap.put(StatusType.startup, new DelayRemoveTransitionCallback(data, topologyid, status));
		killedMap.put(StatusType.kill, new KillTransitionCallback(data, topologyid,status));
		killedMap.put(StatusType.remove, new RemoveTransitionCallback(data, topologyid));
		rtn.put(StatusType.killed, killedMap);

		Map<StatusType, Callback> rebalancingMap = new HashMap<StatusType, Callback>();
		rebalancingMap.put(StatusType.startup, new DelayRebalanceTransitionCallback(data, topologyid, status));
		rebalancingMap.put(StatusType.kill, new KillTransitionCallback(data, topologyid,status));
		rebalancingMap.put(StatusType.do_rebalance, new DoRebalanceTransitionCallback(data, topologyid, status));
		rtn.put(StatusType.rebalancing, rebalancingMap);

		return rtn;

	}
	


}
