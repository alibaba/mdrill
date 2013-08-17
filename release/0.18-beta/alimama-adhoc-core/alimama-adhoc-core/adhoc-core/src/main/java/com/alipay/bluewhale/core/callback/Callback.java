package com.alipay.bluewhale.core.callback;

/**
 * 回调接口
 * 
 * @author lixin 2012-3-12 下午2:22:54
 *
 */
public interface Callback {
	
	public<T> Object execute(T ...args);

}
