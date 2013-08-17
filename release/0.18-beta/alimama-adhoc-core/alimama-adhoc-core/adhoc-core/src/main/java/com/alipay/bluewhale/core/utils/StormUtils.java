package com.alipay.bluewhale.core.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

import backtype.storm.utils.Time;
import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.callback.RunnableCallback;

/**
 * storm utils
 * 
 * 2012-03-27 去掉那些根本就没使用过的函数
 * 
 * @author yannian
 * 
 */
public class StormUtils {

	static Logger LOG = Logger.getLogger(StormUtils.class);

	public static UUID uuid() {
		return java.util.UUID.randomUUID();
	}

	public static <K, V> Map<K, V> select_keys_pred(Set<K> filter, Map<K, V> all) {
		Map<K, V> filterMap = new HashMap<K, V>();

		for (Entry<K, V> entry : all.entrySet()) {
			if (!filter.contains(entry.getKey())) {
				filterMap.put(entry.getKey(), entry.getValue());
			}
		}

		return filterMap;
	}

	public static byte[] barr(byte v) {
		byte[] byteArray = new byte[1];
		byteArray[0] = v;

		return byteArray;
	}

	public static byte[] barr(Short v) {
		byte[] byteArray = new byte[Short.SIZE / 8];
		for (int i = 0; i < byteArray.length; i++) {
			int off = (byteArray.length - 1 - i) * 8;
			byteArray[i] = (byte) ((v >> off) & 0xFF);
		}
		return byteArray;
	}

	public static byte[] barr(Integer v) {
		byte[] byteArray = new byte[Integer.SIZE / 8];
		for (int i = 0; i < byteArray.length; i++) {
			int off = (byteArray.length - 1 - i) * 8;
			byteArray[i] = (byte) ((v >> off) & 0xFF);
		}
		return byteArray;
	}

	// for test
	public static int byteToInt2(byte[] b) {

		int iOutcome = 0;
		byte bLoop;

		for (int i = 0; i < 4; i++) {
			bLoop = b[i];
			int off = (b.length - 1 - i) * 8;
			iOutcome += (bLoop & 0xFF) << off;

		}

		return iOutcome;
	}

	public static void halt_process(int val, String msg) {
		LOG.info("Halting process: " + msg);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOG.error("halt_process", e);
		}
		Runtime.getRuntime().halt(val);
	}

	/**
	 * "{:a 1 :b 1 :c 2} -> {1 [:a :b] 2 :c}"
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V> HashMap<V, List<K>> reverse_map(Map<K, V> map) {
		HashMap<V, List<K>> rtn = new HashMap<V, List<K>>();
		if (map == null) {
			return rtn;
		}
		for (Entry<K, V> entry : map.entrySet()) {
			K key = entry.getKey();
			V val = entry.getValue();
			List<K> list = rtn.get(val);
			if (list == null) {
				list = new ArrayList<K>();
			}
			list.add(key);
			rtn.put(entry.getValue(), list);
		}
		return rtn;
	}

	/**
	 * Gets the pid of this JVM. Hacky because Java doesn't provide a real way
	 * to do this.
	 * 
	 * @return
	 */
	public static String process_pid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String[] split = name.split("@");
		if (split.length != 2) {
			throw new RuntimeException("Got unexpected process name: " + name);
		}

		return split[0];
	}

	public static void exec_command(String command) throws ExecuteException,
			IOException {
		String[] cmdlist = command.split(" ");
		CommandLine cmd = new CommandLine(cmdlist[0]);
		for (int i = 1; i < cmdlist.length; i++) {
			cmd.addArgument(cmdlist[i]);
		}

		DefaultExecutor exec = new DefaultExecutor();
		exec.execute(cmd);
	}

	// 正常情况下这个命令由于jar包中没有resources是会报错的
	// 如果将来使用python等non-jvm程序 ，要求我们打包的时候 必须放到resoruce目录里
	public static void extract_dir_from_jar(String jarpath, String dir,
			String destdir) {
		String cmd = "unzip -qq " + jarpath + " " + dir + "/** -d " + destdir;
		try {
			exec_command(cmd);
		} catch (ExecuteException e) {
			LOG.error("Error when trying to extract " + dir + " from "
					+ jarpath + "by cmd:" + cmd, e);
		} catch (IOException e) {
			LOG.error("Error when trying to extract " + dir + " from "
					+ jarpath + " IOException" + "by cmd:" + cmd, e);
		}

	}

	public static void ensure_process_killed(Integer pid) {
		// FIXME kill -9 无法调用shutdownhook，可以先执行kill操作，线程等待一会，然后再去执行次kill -9
		try {
			exec_command("kill -9 " + pid);
		} catch (ExecuteException e) {
			LOG.info("Error when trying to kill " + pid
					+ ". Process is probably already dead. ");
		} catch (IOException e) {
			LOG.info("Error when trying to kill " + pid + ".IOException ");
		}
	}

	public static java.lang.Process launch_process(String command,
			Map<String, String> environment) throws IOException {
		String[] cmdlist = (new String("nohup " + command)).split(" ");
		ArrayList<String> buff = new ArrayList<String>();
		for (String tok : cmdlist) {
			if (!tok.isEmpty()) {
				buff.add(tok);
			}
		}

		ProcessBuilder builder = new ProcessBuilder(buff);
		Map<String, String> process_evn = builder.environment();
		for (Entry<String, String> entry : environment.entrySet()) {
			process_evn.put(entry.getKey(), entry.getValue());
		}

		return builder.start();
	}
	
	public static java.lang.Process launch_work_process(String command,
		Map<String, String> environment) throws IOException {
	String[] cmdlist = {"nohup","/bin/sh","-c",command+" 1>/dev/null 2>/dev/null"};
	ArrayList<String> buff = new ArrayList<String>();
	for (String tok : cmdlist) {
		if (!tok.isEmpty()) {
			buff.add(tok);
		}
	}

	ProcessBuilder builder = new ProcessBuilder(buff);
	Map<String, String> process_evn = builder.environment();
	for (Entry<String, String> entry : environment.entrySet()) {
		process_evn.put(entry.getKey(), entry.getValue());
	}

	return builder.start();
}

	public static void sleep_secs(long secs) throws InterruptedException {
		Time.sleep(1000 * secs);
	}

	public static RunnableCallback getDefaultKillfn() {

		return new AsyncLoopDefaultKill();
	}

	public static String current_classpath() {
		return System.getProperty("java.class.path");
	}

	// public static String add_to_classpath(String classpath, String[] paths) {
	// for (String path : paths) {
	// classpath += ":" + path;
	// }
	// return classpath;
	// }

	public static TreeMap<Integer, Integer> integer_divided(int sum,
			int num_pieces) {
		return Utils.integerDivided(sum, num_pieces);
	}

	public static String to_json(Map m) {
		return JSONValue.toJSONString(m);
	}

	public static Object from_json(String json) {
		// TODO 是否替换parseWithException？
		return JSONValue.parse(json);
	}

	public static <V> HashMap<V, Integer> multi_set(List<V> list) {
		HashMap<V, Integer> rtn = new HashMap<V, Integer>();
		for (V v : list) {
			int cnt = 1;
			if (rtn.containsKey(v)) {
				cnt += rtn.get(v);
			}
			rtn.put(v, cnt);
		}
		return rtn;
	}

	public static <K, V> HashMap<K, V> filter_val(RunnableCallback fn,
			Map<K, V> amap) {
		HashMap<K, V> rtn = new HashMap<K, V>();

		for (Entry<K, V> entry : amap.entrySet()) {
			V value = entry.getValue();
			Object result = fn.execute(value);

			if (result == (Boolean) true) {
				rtn.put(entry.getKey(), value);
			}
		}
		return rtn;
	}

	/**
	 * 用于在commmon里验证StormTopology，防止有bolt，spout之间使用相同的commonId
	 * 这个函数，会将重复的返回，如果有返回，则表示List里面有重复
	 * 
	 * @param sets
	 * @return
	 */
	public static List<String> getRepeat(List<String> sets) {

		RunnableCallback fn = new RunnableCallback() {
			@Override
			public <T> Object execute(T... args) {
				int num = (Integer) args[0];
				boolean isFilter = false;
				if (num > 1) {
					isFilter = true;
				} else {
					isFilter = false;
				}
				return isFilter;
			}
		};

		HashMap<String, Integer> multi = multi_set(sets);

		HashMap<String, Integer> filters = filter_val(fn, multi);
		ArrayList<String> rtn = new ArrayList<String>();
		for (Entry<String, Integer> entry : filters.entrySet()) {
			rtn.add(entry.getKey());
		}
		return rtn;
	}

	/**
	 * 用于nimbus中，用于分配task，尽量将相同的task，分配到不同的机器上
	 * 
	 * @param <T>
	 * @param splitup
	 * @return
	 */
	public static <T> List<T> interleave_all(List<List<T>> splitup) {
		ArrayList<T> rtn = new ArrayList<T>();
		int maxLength = 0;
		for (List<T> e : splitup) {
			int len = e.size();
			if (maxLength < len) {
				maxLength = len;
			}
		}

		for (int i = 0; i < maxLength; i++) {
			for (List<T> e : splitup) {
				if (e.size() > i) {
					rtn.add(e.get(i));
				}
			}
		}

		return rtn;
	}

	public static String stringify_error(Throwable error) {
		StringWriter result = new StringWriter();
		PrintWriter printer = new PrintWriter(result);
		error.printStackTrace(printer);
		return result.toString();
	}

	public static Long bit_xor_vals(Object... vals) {
		Long rtn = 0l;
		for (Object n : vals) {
			rtn = bit_xor(rtn, n);
		}

		return rtn;
	}

	public static Long bit_xor_vals_sets(java.util.Set<Object> vals) {
		Long rtn = 0l;
		for (Object n : vals) {
			rtn = bit_xor(rtn, n);
		}
		return rtn;
	}

	public static Long bit_xor(Object a, Object b) {
		Long rtn = 0l;

		if (b instanceof Set) {
			Long bs = bit_xor_vals_sets((Set) b);
			return bit_xor(a, bs);
		}

		if (a instanceof Set) {
			Long as = bit_xor_vals_sets((Set) a);
			return bit_xor(as, b);
		}

		if (a instanceof Long && b instanceof Long) {
			rtn = ((Long) a) ^ ((Long) b);
		} else {
			Long ai = Long.parseLong(String.valueOf(a));
			Long bi = Long.parseLong(String.valueOf(b));
			rtn = ai ^ bi;
		}

		return rtn;
	}

	public static <V> List<V> mk_list(V... args) {
		ArrayList<V> rtn = new ArrayList<V>();
		for (V o : args) {
			rtn.add(o);
		}
		return rtn;
	}

	public static <V> List<V> mk_list(java.util.Set<V> args) {
		ArrayList<V> rtn = new ArrayList<V>();
		if (args != null) {
			for (V o : args) {
				rtn.add(o);
			}
		}
		return rtn;
	}

	public static <V> V[] mk_arr(V... args) {
		return args;
	}

	public static Integer parseInt(Object o) {
		if (o == null) {
			return null;
		}
		return Integer.parseInt(String.valueOf(o));
	}

	public static <V> Set<V> listToSet(List<V> list) {
		if (list == null) {
			return null;
		}

		Set<V> set = new HashSet<V>();
		set.addAll(list);
		return set;
	}

	/**
	 * supervisor 所用,用来处理jar文件
	 * 
	 * @param zipfile
	 * @param resources
	 * @return
	 */
	public static boolean zipContainsDir(String zipfile, String resources) {

		Enumeration<? extends ZipEntry> entries = null;
		try {
			entries = (new ZipFile(zipfile)).entries();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			LOG.error(e + "zipContainsDir error");
		}

		while (entries != null && entries.hasMoreElements()) {
			ZipEntry ze = entries.nextElement();
			String name = ze.getName();
			if (name.startsWith(resources + "/")) {
				return true;
			}
		}
		return false;
	}

}