package com.alimama.mdrill.ui.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;



import java.util.Map;

import org.apache.log4j.Logger;

import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.partion.GetPartions;
import com.alimama.mdrill.partion.MdrillPartions;
import com.alimama.mdrill.partion.MdrillPartionsInterface;
import com.alimama.mdrill.partion.GetPartions.TablePartion;
import com.alimama.mdrill.ui.service.utils.WebServiceParams;
import com.alimama.mdrill.ui.service.utils.WebServiceParams.HigoJoinParams;
import com.alimama.mdrill.ui.service.utils.WebServiceParams.SortParam;
import com.alimama.mdrill.utils.UniqConfig;

public class MdrillRequest {
	private static Logger LOG = Logger.getLogger(MdrillRequest.class);

	public String queryStr;
	public int start;
	public int rows;
	
	public ArrayList<String> groupbyFields ;
	public ArrayList<String> showFields ;
	public HashSet<String> commonStatMap ;
	public HashSet<String> distStatFieldMap;
	public SortParam sortType;
	
	
	public String[] partionsAll;
	public String leftjoin;
	public MdrillRequest(MdrillTableConfig tblconf,TablePartion part,Map stormconf,String projectName,
			String startStr, String rowsStr, String queryStr, String dist,
			String fl, String groupby, String sort, String order,String leftjoin) throws Exception
	{
		this.leftjoin=leftjoin;
		this.queryStr = WebServiceParams.query(queryStr);
		this.start = WebServiceParams.parseStart(startStr);
		this.rows = WebServiceParams.parseRows(rowsStr);
		this.groupbyFields = WebServiceParams.groupFields(groupby);
		this.showFields = WebServiceParams.showFields(fl);
		this.commonStatMap = new HashSet<String>();
		this.distStatFieldMap = new HashSet<String>();
		WebServiceParams.setCrossStatMap(this.showFields,this.commonStatMap,this.distStatFieldMap);
		
		
		this.sortType = WebServiceParams.sort(sort, order,tblconf.fieldColumntypeMap,this.groupbyFields);

		
		MdrillPartionsInterface drillpart=MdrillPartions.INSTANCE(part.parttype);
		this.partionsAll = drillpart.SqlPartions(this.queryStr);
		this.queryStr=drillpart.SqlFilter(this.queryStr);

		Arrays.sort(partionsAll);
		LOG.info("partionsAll:" + MdrillRequestLog.cutString(Arrays.toString(partionsAll)));

	

	}
	
	public HigoJoinParams[] parseJoins(MdrillTableCoreInfo coreinfo,GetPartions.Shards shard) throws Exception
	{
		return WebServiceParams.parseJoins(leftjoin, shard);
	}
	
	public ArrayList<String> parseFq(MdrillTableConfig tblconf,GetPartions.Shards shard) throws JSONException
	{
		ArrayList<String> fqList = WebServiceParams.fqList(tblconf.isnothedate,this.queryStr, shard,tblconf.fieldColumntypeMap);
		if(tblconf.isnothedate&&tblconf.mode.indexOf("@fdt@")<0)
		{
			fqList.add("-higoempty_emptydoc_s:[* TO *]");		
		}
		return fqList;
	}
	
	
	public static class StartLimit{
		public int start;
		public int rows;
	}
	public StartLimit getReqStartEnd()
	{
		int extandRows=50;
		if(this.distStatFieldMap.size()>0)
		{
			extandRows=20;
		}
		
		int minstart = this.start;
		int maxEend = this.rows;
		if (this.sortType.isStatNum) {
			minstart = this.start - extandRows;
			if (minstart < 0) {
				minstart = 0;
			}
			maxEend = Math.min(this.rows + extandRows+extandRows,UniqConfig.defaultCrossMaxLimit());
		}
		
		StartLimit rtn=new StartLimit();
		rtn.start=minstart;
		rtn.rows=maxEend;

		return rtn;
	}
	
	
	
}
