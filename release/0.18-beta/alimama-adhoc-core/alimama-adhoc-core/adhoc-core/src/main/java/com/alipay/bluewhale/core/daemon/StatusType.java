package com.alipay.bluewhale.core.daemon;
/**
 * topology的状态：
 * 需要持久化的状态，写入zk中的：  active、inactive、killed和rebalancing
 * 改变状态的操作:startup、monitor、inactivate、activate、rebalance、do_rebalance和kill
 *             每个改变状态都对应一个操作
 * 例如：需要将active的状态变为inactive状态，就要找到inactivate对应的操作。
 * 
 */
public enum StatusType {

	kill(":kill"),
	killed(":killed"),
	monitor(":monitor"),
	inactive(":inactive"),
	inactivate(":inactivate"),
	active(":active"),
	activate(":activate"),
	startup(":startup"),
	remove(":remove"),
	rebalance(":rebalance"),
	rebalancing(":rebalancing"),
	do_rebalance(":do-rebalance");

	private String status;
	StatusType(String status) {
		this.status=status;
	}
	
	//获取对应字符串状态
	public String getStatus(){
		return status;		
	}
}
