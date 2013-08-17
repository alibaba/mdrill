package com.alimama.web;

import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;


public class SupervisorContainer {
	
	String name;
	
	SupervisorInfo info;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public SupervisorInfo getInfo() {
		return info;
	}
	public void setInfo(SupervisorInfo info) {
		this.info = info;
	}
	
	@Override
	public String toString() {
		return "SupervisorContainer [name=" + name + ", info=" + info + "]";
	}
	
}
