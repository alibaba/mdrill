package com.alipay.bluewhale.core.schedule;

import java.io.File;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.utils.OlderFileFilter;

/**
 * 定时清理过期jar线程操作
 * 
 * @author lixin 2012-3-20 下午12:53:50
 *
 */
public class CleanRunnable implements Runnable {

	private static Logger log = Logger.getLogger(CleanRunnable.class);

	private String dir_location;

	private int seconds;

	public CleanRunnable(String dir_location, int inbox_jar_expiration_secs) {
		this.dir_location = dir_location;
		this.seconds = inbox_jar_expiration_secs;
	}

	@Override
	public void run() {
		log.info("Deletes jar files in dir older than seconds.");

		File inboxdir = new File(dir_location);

		//过滤器
		OlderFileFilter filter = new OlderFileFilter(seconds);
		
		File[] files = inboxdir.listFiles(filter);
		for (File f : files) {
			log.info("Cleaning inbox ... deleted: " + f.getName());
			try {
				f.delete();
			} catch (Exception e) {
				log.error("Cleaning inbox ... error deleting:" + f.getName() + "," + e);
			}
		}
	}

}
