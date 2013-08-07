package com.alipay.bluewhale.core.task.error;

/**
 * task上报错误的接口
 * @author yannian
 *
 */
public interface ITaskReportErr {
	 public void report(Throwable error);
}
