package com.alipay.bluewhale.core.cluster;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.utils.EvenSampler;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.LocalState;
import backtype.storm.utils.Utils;

public class StormConfig {
	private final static Logger LOG = Logger.getLogger(StormConfig.class);
	public static String RESOURCES_SUBDIR = "resources";

	public static String clojureConfigName(String name) {
		return name.toUpperCase().replace("_", "-");
	}

	public static Map read_storm_config() {
		return Utils.readStormConfig();
	}

	public static Map read_yaml_config(String name) {
		return Utils.findAndReadConfigFile(name, true);
	}

	public static Map read_default_config() {
		return Utils.readDefaultConfig();
	}

	public static List<Object> All_CONFIGS() {
		List<Object> rtn = new ArrayList<Object>();
		Config config = new Config();
		Class<?> ConfigClass = config.getClass();
		Field[] fields = ConfigClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				Object obj = fields[i].get(null);
				rtn.add(obj);
			} catch (IllegalArgumentException e) {
				LOG.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return rtn;
	}

	public static HashMap<String, Object> getClassFields(Class<?> cls)
			throws IllegalArgumentException, IllegalAccessException {
		java.lang.reflect.Field[] list = cls.getDeclaredFields();
		HashMap<String, Object> rtn = new HashMap<String, Object>();
		for (java.lang.reflect.Field f : list) {
			String name = f.getName();
			rtn.put(name, f.get(null).toString());

		}
		return rtn;
	}

	public static String cluster_mode(Map conf) {
		String mode = (String) conf.get(Config.STORM_CLUSTER_MODE);
		return mode;

	}

	public static boolean local_mode(Map conf) {
		String mode = (String) conf.get(Config.STORM_CLUSTER_MODE);
		if (mode != null) {
			if (mode.equals("local")) {
				return true;
			}

			if (mode.equals("distributed")) {
				return false;
			}
		}
		throw new IllegalArgumentException("Illegal cluster mode in conf:"
				+ mode);

	}

	public static String worker_root(Map conf) {
		return String.valueOf(conf.get(Config.STORM_LOCAL_DIR)) + "/workers";
	}

	public static String worker_root(Map conf, String id) {
		return worker_root(conf) + "/" + id;
	}

	public static String worker_pids_root(Map conf, String id) {
		String rtn = worker_root(conf, id) + "/pids";
		try {
			FileUtils.forceMkdir(new File(rtn));
		} catch (IOException e) {
		}
		return rtn;
	}

	public static String worker_pid_path(Map conf, String id, String pid) {
		return worker_pids_root(conf, id) + "/" + pid;
	}

	public static String worker_heartbeats_root(Map conf, String id) {
		return worker_root(conf, id) + "/heartbeats";
	}

	private static String supervisor_local_dir(Map conf) throws IOException {
		String ret = new String((String) conf.get(Config.STORM_LOCAL_DIR))
				+ "/supervisor";
		FileUtils.forceMkdir(new File(ret));
		return ret;
	}

	public static String supervisor_stormdist_root(Map conf) throws IOException {
		return supervisor_local_dir(conf) + "/stormdist";
	}

	public static String supervisor_stormdist_root(Map conf, String storm_id)
			throws IOException {
		return supervisor_stormdist_root(conf) + "/" + storm_id;
	}

	public static String supervisor_stormjar_path(String stormroot) {
		return stormroot + "/stormjar.jar";
	}

	public static String supervisor_stormcode_path(String stormroot) {
		return stormroot + "/stormcode.ser";
	}

	public static String supervisor_sotrmconf_path(String stormroot) {
		return stormroot + "/stormconf.ser";
	}

	public static String supervisor_storm_resources_path(String stormroot) {
		return stormroot + "/" + RESOURCES_SUBDIR;

	}

	public static LocalState worker_state(Map conf, String id)
			throws IOException {
		String path = worker_heartbeats_root(conf, id);

		LocalState rtn = new LocalState(path);
		return rtn;

	}

	public static boolean isLocalMode(Map conf) {
		String mode = (String) conf.get(Config.STORM_CLUSTER_MODE);
		if (mode.equals("local")) {
			return true;
		}

		if (mode.equals("distributed")) {
			return false;
		}

		throw new IllegalArgumentException("Illegal cluster mode in conf:"
				+ mode);
	}

	public static String masterLocalDir(Map conf) {
		String ret = conf.get(Config.STORM_LOCAL_DIR) + "/nimbus";
		try {
			FileUtils.forceMkdir(new File(ret));
		} catch (IOException e) {
		}
		return ret;
	}

	public static String masterStormdistRoot(Map conf) {
		return masterLocalDir(conf) + "/stormdist";
	}

	public static String masterStormdistRoot(Map conf, String stormId) {
		return masterStormdistRoot(conf) + "/" + stormId;
	}

	public static String masterStormjarPath(String stormroot) {
		return stormroot + "/stormjar.jar";
	}

	public static String masterStormconfPath(String stormroot) {
		return stormroot + "/stormconf.ser";
	}

	public static String masterStormcodePath(String stormroot) {
		return stormroot + "/stormcode.ser";
	}

	public static String masterInbox(Map conf) {
		String ret = masterLocalDir(conf) + "/inbox";
		try {
			FileUtils.forceMkdir(new File(ret));
		} catch (IOException e) {
		}
		return ret;
	}

	public static String supervisorTmpDir(Map conf) {
		String ret = null;
		try {
			ret = supervisor_local_dir(conf) + "/tmp";
			FileUtils.forceMkdir(new File(ret));
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}

		return ret;
	}

	public static LocalState supervisorState(Map conf) {
		LocalState localState = null;
		try {
			localState = new LocalState(supervisor_local_dir(conf)
					+ "/localstate");
		} catch (IOException e) {
		}
		return localState;
	}



	public static Map read_supervisor_storm_conf(Map conf, String storm_id)
			throws IOException {
		String stormroot = StormConfig
				.supervisor_stormdist_root(conf, storm_id);
		String conf_path = StormConfig.supervisor_sotrmconf_path(stormroot);
		// String topology_path =
		// StormConfig.supervisor_stormcode_path(stormroot);
		Map rtn = new HashMap();
		rtn.putAll(conf);
		rtn.putAll((Map) Utils.deserialize(FileUtils
				.readFileToByteArray(new File(conf_path))));
		return rtn;
	}

	public static StormTopology read_supervisor_topology(Map conf,
			String topologyid) throws IOException {
		String topologyroot = StormConfig.supervisor_stormdist_root(conf,
				topologyid);
		String topology_path = StormConfig
				.supervisor_stormcode_path(topologyroot);
		return (StormTopology) Utils.deserialize(FileUtils
				.readFileToByteArray(new File(topology_path)));
	}

	public static Integer sampling_rate(Map conf) {
		return (int) (1 / Double.parseDouble(String.valueOf(conf
				.get(Config.TOPOLOGY_STATS_SAMPLE_RATE))));
	}
	
	public static EvenSampler mk_stats_sampler(Map conf) {
		return new EvenSampler(sampling_rate(conf));
	}

	public static Integer samplingRate(Map conf) {
		return sampling_rate(conf);
	}
}
