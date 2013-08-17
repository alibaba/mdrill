package com.alipay.bluewhale.core.utils;

/**
 * storm线程基类
 * @author yannian
 *
 */
public interface SmartThread {
	 public void start();
	 public void join() throws InterruptedException;;
	 public void interrupt();
	 public Boolean isSleeping();
}
