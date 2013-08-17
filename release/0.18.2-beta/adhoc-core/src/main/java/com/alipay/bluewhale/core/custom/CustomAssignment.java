package com.alipay.bluewhale.core.custom;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;


import backtype.storm.generated.JavaObject;
import backtype.storm.generated.JavaObjectArg;
import backtype.storm.grouping.CustomStreamGrouping;

import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.task.common.TaskInfo;
import com.alipay.bluewhale.core.thrift.Thrift;
import com.alipay.bluewhale.core.utils.StormUtils;
/**
 * mvn clean package assembly:assembly -Dmaven.test.skip=true
 * 
 * @author yannian
 * @version $Id: Separate.java, v 0.1 2012-4-28 ÏÂÎç3:13:31 yannian Exp $
 */
public class CustomAssignment {
    private static Logger LOG = Logger.getLogger(CustomAssignment.class);

    public static String TOPOLOGY_CUSTOM_ASSIGNMENT = "topology.assignment.custom";

    public static IAssignment getAssignmentInstance(Map topology_conf) {
        String customClassName = null;
        Object  repClassName= topology_conf.get(TOPOLOGY_CUSTOM_ASSIGNMENT);
        if (repClassName != null) {
            customClassName = String.valueOf(repClassName);
        }
        
        LOG.info("CustomAssignment:"+customClassName);

        if(customClassName==null)
        {
            return null;
        }
        return instance(customClassName);
    }
    
    
    private static IAssignment instance(String clsName) {
        try {
            Class[] paraTypes = new Class[0];
            Object[] paraValues = new Object[0];
            Class clas = Class.forName(clsName);
            Constructor cons = clas.getConstructor(paraTypes);
            return (IAssignment) cons.newInstance(paraValues);
        } catch (Exception e) {
            LOG.error("instance fail", e);
        }

        return null;
    }

}
