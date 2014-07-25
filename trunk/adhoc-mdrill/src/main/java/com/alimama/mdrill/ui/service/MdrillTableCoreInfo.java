package com.alimama.mdrill.ui.service;

import java.util.Map;

import com.alimama.mdrill.partion.GetShards;
import com.alimama.mdrill.partion.GetPartions.TablePartion;
import com.alimama.mdrill.partion.GetShards.ShardsList;

public class MdrillTableCoreInfo {
	public ShardsList[] cores ;
	public ShardsList[] ms ;
	
	public MdrillTableCoreInfo(TablePartion part,Map stormconf) throws Exception
	{
		this.cores = GetShards.getCores(stormconf, part);
		this.ms = GetShards.getMergers(part.name);
	}
}
