package com.etao.adhoc.analyse.common.util;

import java.io.IOException;
import java.util.Map;

import backtype.storm.utils.Utils;

public class YamlUtils {
	public static Map getConfigFromYamlFile(String fileName) throws IOException{
		return Utils.readStormConfig();
	}

}
