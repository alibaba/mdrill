package com.alimama.mdrill.ui.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.alimama.mdrill.partion.GetPartions.TablePartion;

public class MdrillTableConfig {
	public String mode;
	public boolean isnothedate;
	
	public LinkedHashMap<String, String> fieldColumntypeMap;
	
	public MdrillTableConfig(TablePartion part,Map stormconf) throws Exception
	{
		this.fieldColumntypeMap = MdrillFieldInfo.readFieldsFromSchemaXml(stormconf,part.name);
		this.mode=String.valueOf(stormconf.get("higo.mode."+part.name));
		this.isnothedate=this.mode.indexOf("@nothedate@")>=0;

	}

}
