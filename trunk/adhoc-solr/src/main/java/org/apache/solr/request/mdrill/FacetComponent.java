/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.SolrException;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.handler.component.ShardResponse;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.ColumnKey;
import org.apache.solr.request.compare.GroupbyAgent;
import org.apache.solr.request.compare.GroupbyItem;
import org.apache.solr.request.compare.GroupbyRow;
import org.apache.solr.request.compare.MergerDetailSelectDetailRowCompare;
import org.apache.solr.request.compare.MergerGroupByGroupbyRowCompare;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.compare.UniqTypeNum;
import org.apache.solr.request.join.HigoJoinSort;
import org.apache.solr.request.join.HigoJoinUtils;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexReader;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;

import com.alimama.mdrill.distinct.DistinctCount.DistinctCountAutoAjuest;
import com.alimama.mdrill.utils.UniqConfig;


public class  FacetComponent extends SearchComponent
{
    private static Logger LOG = Logger.getLogger(FacetComponent.class);

  public static final String COMPONENT_NAME = "facet";

  @Override
  public void prepare(ResponseBuilder rb) throws IOException
  {
    if (rb.req.getParams().getBool(FacetParams.FACET,false)) {
      rb.setNeedDocSet( true );
      rb.doFacets = true;
    }
  }


	public static ThreadPoolExecutor SUBMIT_POOL = new ThreadPoolExecutor(Math.max(UniqConfig.getFacetThreads()/2, 1),UniqConfig.getFacetThreads(), 100L, TimeUnit.SECONDS,	new LinkedBlockingQueue<Runnable>());

  @Override
  public void process(ResponseBuilder rb1) throws IOException
  {
    if (!rb1.doFacets) {
      return ;
    }
    
    final ResponseBuilder rb=rb1;
    
    final long t1=System.currentTimeMillis();
    
	ExecutorCompletionService<String> submit=new ExecutorCompletionService<String>(SUBMIT_POOL);
	Callable<String> task = new Callable<String>() {
	      public String call() throws Exception {

	    	  long t2=System.currentTimeMillis();
	    	  SolrParams params = rb.req.getParams();
	    	  String[] facetFs = params.getParams(FacetParams.FACET_FIELD);

	    	  try{
	    		    if (null != facetFs) {
	    		    	boolean isdetail = params.getBool(FacetParams.FACET_CROSS_DETAIL,false);
	    		    	 Object res= FacetComponent.this.getResult(isdetail,rb.req.getSearcher(), params,rb.req,facetFs,rb.getResults().docSet);
	    		        rb.rsp.add( "mdrill_data", res);
	    		    }else{
	    		    	throw new Exception("null != facetFs");
	    		    }
	    	  }catch(Throwable e)
	    	  {
	    	  	LOG.error("getFacetCounts",e);
	    	  	throw new SolrException(ErrorCode.SERVER_ERROR,e);
	    	  }
	    	  
	    	  long t3=System.currentTimeMillis();
	    	  
	    	  LOG.info("####task####"+(t3-t2)+","+(t2-t1));
	    	  
	    	  return "";
	   }
	      

	};
	submit.submit(task);
	try {
		submit.take().get();
	} catch (Throwable e) {
		throw new IOException(e);
	} 
    
  }
  private Object getResult(boolean isdetail,SolrIndexSearcher searcher,SolrParams params,SolrQueryRequest req,String[] fields, DocSet base)throws Exception 
	{
	  String crcget=params.get("mdrill.crc.key.get",null);
	  if(crcget!=null&&(params.getBool("fetchfdt", false)||isdetail))
	  {
		  SolrIndexReader reader=searcher.getReader();
			IndexReader.InvertParams invparam=new IndexReader.InvertParams();
			invparam.searcher=searcher;
			invparam.params=params;
			invparam.fields=fields;
			invparam.base=base;
			invparam.req=req;
			invparam.isdetail=isdetail;
			IndexReader.InvertResult result=reader.invertScan(searcher.getSchema(), invparam);
			ArrayList<NamedList> resultlist=result.getResult();
			
			Map<Long,String> crcvalue=new HashMap<Long,String>();
			
			for(NamedList nl:resultlist)
			{
				
				crcvalue.putAll((Map<? extends Long, ? extends String>) nl.get("fdtcre"));
				
			}
			
			return crcvalue;

	  }
	  
	  
	  if(crcget!=null)
	  {
			ConcurrentHashMap<Long,String> cache=MdrillUtils.CRC_CACHE_SIZE.remove(crcget);
			
			Map<Long,String> crcvalue=new HashMap<Long,String>();
			if(cache==null)
			{
				return crcvalue;
			}
			
			String crcliststr=params.get("mdrill.crc.key.get.crclist");
			if(crcliststr!=null)
			{
				String[] crclist=crcliststr.split(",");
				for(String s:crclist)
				{
					Long crc=Long.parseLong(s);
					String v=cache.get(crc);
					if(v!=null)
					{
						crcvalue.put(crc, v);
					}
				}
			}

			return crcvalue;
	  }
	  
	  
		SolrIndexReader reader=searcher.getReader();
		IndexReader.InvertParams invparam=new IndexReader.InvertParams();
		invparam.searcher=searcher;
		invparam.params=params;
		invparam.fields=fields;
		invparam.base=base;
		invparam.req=req;
		invparam.isdetail=isdetail;
		IndexReader.InvertResult result=reader.invertScan(searcher.getSchema(), invparam);
		ArrayList<NamedList> resultlist=result.getResult();
		if(resultlist.size()==1)
		{
			return resultlist.get(0);
		}
		
		FacetComponent.FacetInfo fi = new FacetComponent.FacetInfo(params);
      DistribFieldFacet dff = fi.cross;
      dff.isdetail=isdetail;
	     for (NamedList nl: resultlist) {
	         dff.add(nl);
	     }
	     
		int offset = params.getInt(FacetParams.FACET_CROSS_OFFSET, 0);
		int limit = params.getInt(FacetParams.FACET_CROSS_LIMIT, 100);
		int limit_offset = offset + limit;
	     
	     NamedList fieldCounts = new NamedList();
	      GroupbyItem[] counts = dff.getPairSorted(limit_offset);
	      if(dff.recordcount!=null)
	      {
	    	  GroupbyItem recordcount=dff.recordcount;
		      fieldCounts.add("count", recordcount.toNamedList());
	      }
			ArrayList<Object> list=new ArrayList<Object>();

	      int end = limit_offset> counts.length ?counts.length:limit_offset;
	      for (int i=offset; i<end; i++) {
	    	  list.add(counts[i].toNamedList());
	      }
	      
	      fieldCounts.add("list", list);


		return fieldCounts;
	}
  
  
  @Override
  public int distributedProcess(ResponseBuilder rb) throws IOException {
	    return ResponseBuilder.STAGE_DONE;
  }

  @Override
  public void modifyRequest(ResponseBuilder rb, SearchComponent who, ShardRequest sreq) {
    if (!rb.doFacets) 
    	{return;
    	}
        FacetInfo fi = rb._facetInfo;
        if (fi == null) {
          rb._facetInfo = fi = new FacetInfo(rb.req.getParams());
        }
        sreq.params.remove(FacetParams.FACET_MINCOUNT);
        sreq.params.remove(FacetParams.FACET_OFFSET);
        sreq.params.remove(FacetParams.FACET_LIMIT);
        if(sreq.params.getBool("fetchfdt", false))
		  {
        	 int offset=sreq.params.getInt(FacetParams.FACET_CROSS_OFFSET,0);
        	 int limit=sreq.params.getInt(FacetParams.FACET_CROSS_LIMIT,0);
        	 sreq.params.remove(FacetParams.FACET_CROSS_OFFSET);
             sreq.params.remove(FacetParams.FACET_CROSS_LIMIT);
             sreq.params.set(FacetParams.FACET_CROSS_OFFSET,  0);
         	sreq.params.set(FacetParams.FACET_CROSS_LIMIT,  offset+limit);
        	
		  }else{
        
      
        sreq.params.remove(FacetParams.FACET_CROSS_OFFSET);
        sreq.params.remove(FacetParams.FACET_CROSS_LIMIT);
        
        int maxlimit=MdrillGroupBy.MAX_CROSS_ROWS;
        sreq.params.set(FacetParams.FACET_CROSS_OFFSET,  0);
    	sreq.params.set(FacetParams.FACET_CROSS_LIMIT,  maxlimit);
		  }
  }
  

  @Override
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
    if (!rb.doFacets){
    	return;
    }

	long t1=System.currentTimeMillis();
    FacetInfo fi = rb._facetInfo;
    for (ShardResponse srsp: sreq.responses) {
    	NamedList<Object>  rspq=srsp.getSolrResponse().getResponse();
      Map<String,String> shardtime=(Map<String,String>)rspq.get("mdrill_shard_time");
      if(shardtime!=null)
      {
    	  rb.timetaken.putAll(shardtime);
      }
      
      Object facet_counts = rspq.get("mdrill_data");

      if(facet_counts==null)
      {
    	 SolrCore.log.error("mdrill_data is null "+srsp.getShard(),new Exception());
	     continue;
      }
      
      if(rb.req.getParams().get("mdrill.crc.key.get",null)!=null)
      {
    	  rb.crcvalue.putAll((Map<Long,String>)facet_counts);
      }else{
    	  fi.cross.add((NamedList)facet_counts);
      }
      
    }
    
	long t2=System.currentTimeMillis();
	LOG.info("##countFacets## time taken "+(t2-t1)+",responses.size="+sreq.responses.size());
  }


  @Override
  public void finishStage(ResponseBuilder rb) {
    if (!rb.doFacets){
    	return;
    }

	long t1=System.currentTimeMillis();
    FacetInfo fi = rb._facetInfo;
    if(rb.req.getParams().get("mdrill.crc.key.get",null)!=null)
    {
        rb.rsp.add("mdrill_data", rb.crcvalue);
    }else{
	    NamedList fieldCounts = new SimpleOrderedMap();
	
	    DistribFieldFacet dff=fi.cross;
	
	    int saverecords=dff.offset + dff.limit;
	    GroupbyItem[] counts = dff.getPairSorted(saverecords);
	    if(dff.recordcount!=null)
	    {
	  	  	GroupbyItem recordcount=dff.recordcount;
		    fieldCounts.add("count", recordcount.toNamedList());
	    }
	
	    int end = dff.limit < 0 ? counts.length : Math.min(dff.offset + dff.limit, counts.length);
		ArrayList<Object> list=new ArrayList<Object>();

	    for (int i=dff.offset; i<end; i++) {
	  	  GroupbyItem item=counts[i];
	  	  list.add(item.toNamedList());
	    }
	    fieldCounts.add("list", list);
	    rb.rsp.add("mdrill_data", fieldCounts);
    }



    rb.rsp.add("mdrill_shard_time", rb.timetaken);
    rb.crcvalue=new HashMap<Long,String>() ;
    rb.timetaken=new LinkedHashMap<String,String>();
    rb._facetInfo = null;  // could be big, so release asap
    long t2=System.currentTimeMillis();
	LOG.info("##finishStage## time taken "+(t2-t1));
  }


  /////////////////////////////////////////////
  ///  SolrInfoMBean
  ////////////////////////////////////////////

  @Override
  public String getDescription() {
    return "Handle Faceting";
  }

  @Override
  public String getVersion() {
    return "$Revision: 1152531 $";
  }

  @Override
  public String getSourceId() {
    return "$Id: FacetComponent.java 1152531 2011-07-31 00:43:33Z koji $";
  }

  @Override
  public String getSource() {
    return "$URL: https://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_3_5/solr/core/src/java/org/apache/solr/handler/component/FacetComponent.java $";
  }

  @Override
  public URL[] getDocs() {
    return null;
  }

  /**
   * <b>This API is experimental and subject to change</b>
   */
  public static class FacetInfo {
	DistribFieldFacet cross;
	public FacetInfo(SolrParams params) {
		this.cross = new DistribFieldFacet(params,"solrCorssFields_s");
	}
  }

 

	public static class FieldFacet  {
	    private String key; 
		public int offset;
		public int limit;
//		public boolean isFinalResult = true;
		public String sort_fl = null;
		public String sort_type = null;
		public boolean isdesc = true;
		public String[] facetFs = null;
		public HigoJoinSort[] joinSort={};
		public String[] crossFs ;
		public String[] distFS ;
		public boolean isdetail;
		public String sort_column_type;

		public FieldFacet(SolrParams params, String facetStr) {
			this.key = facetStr;

			this.offset = params.getInt(FacetParams.FACET_CROSS_OFFSET, 0);
			this.limit = params.getInt(FacetParams.FACET_CROSS_LIMIT, 100);
//			boolean issub = params.getBool(FacetParams.IS_SUB_SHARDS, false);
//			if (issub) {
//				isFinalResult = false;
//			} else {
//				isFinalResult = true;
//			}

			this.isdetail = params.getBool(FacetParams.FACET_CROSS_DETAIL,false);
			if (this.isdetail) {
				this.sort_type = "detailMerge";
			} else {
				this.sort_type = params.get(FacetParams.FACET_CROSS_SORT_TYPE,"index");
			}
			this.sort_fl = params.get(FacetParams.FACET_CROSS_SORT_FL, null);

			this.isdesc = params.getBool(FacetParams.FACET_CROSS_SORT_ISDESC,true);
			this.crossFs = params.getParams(FacetParams.FACET_CROSS_FL);
			this.distFS = params.getParams(FacetParams.FACET_CROSSDIST_FL);
			this.facetFs = params.getParams(FacetParams.FACET_FIELD);
			String[] joinList = params.getParams(HigoJoinUtils.getTables());
			if (joinList == null) {
				joinList = new String[0];
			}
			this.joinSort = new HigoJoinSort[joinList.length];
			for (int i = 0; i < joinList.length; i++) {
				this.joinSort[i] = new HigoJoinSort(joinList[i], params);
			}

			if (UniqTypeNum.parseSelectDetailType(this.facetFs, joinSort) != null) {
				this.sort_column_type = "string";

			} else {
				this.sort_column_type = params.get("facet.cross.sort.cp");
			}

		}
		
		public MergerGroupByGroupbyRowCompare createMergerGroupCmp()
		{
			return new MergerGroupByGroupbyRowCompare(this.sort_column_type,this.facetFs, this.crossFs,this.distFS, this.joinSort, this.sort_fl, this.sort_type, this.isdesc);
		}
		
		public MergerDetailSelectDetailRowCompare createMergerDetailCmp()
		{
			return new MergerDetailSelectDetailRowCompare(this.sort_column_type,this.isdesc);
		}

	    public String getKey() { return key; }
	}

  public static class DistribFieldFacet extends FieldFacet {

    public HashMap<ColumnKey,GroupbyRow> counts = new HashMap<ColumnKey,GroupbyRow>(128);
    public ArrayList<SelectDetailRow> countsDetail = new ArrayList<SelectDetailRow>(128);
    
	 DistinctCountAutoAjuest autoDist=new DistinctCountAutoAjuest(UniqConfig.DistinctCountSize());

    public GroupbyItem recordcount=null;
    DistribFieldFacet(SolrParams localParams, String facetStr) {
      super(localParams, facetStr);
    }

	long add(NamedList shardCounts) {
		long t1 = System.currentTimeMillis();
		ArrayList<Object> count=(ArrayList<Object>) shardCounts.get("count");
		if(count!=null)
		{
			GroupbyAgent p = new GroupbyAgent(count);
			p.setCross(this.crossFs, this.distFS);
			if (recordcount == null) {
				recordcount = p;
			} else {
				recordcount.shardsMerge(p);
			}
		}
		
		
		ArrayList<Object> list=(ArrayList<Object>) shardCounts.get("list");
		int sz = list == null ? 0 : list.size();
		
		for (int i = 0; i < sz; i++) {
			ArrayList<Object> obj=(ArrayList<Object>)list.get(i);
			GroupbyAgent p = new GroupbyAgent(obj);
			p.setCross(this.crossFs, this.distFS);

			if (this.isdetail) {
				countsDetail.add((SelectDetailRow) p.getRaw());
			} else {
				GroupbyRow row=(GroupbyRow) p.getRaw();
				GroupbyRow sfc = counts.get(row.getKey());
				if (sfc == null) {
					row.setDist(autoDist);
					counts.put(row.getKey(), row);
				} else {
					sfc.shardsMerge(row);
				}
			}
		
		}

		long t2 = System.currentTimeMillis();
		return t2 - t1;
	}

    public GroupbyItem[] getPairSortedDetail(int saverecords) {
    	long t1=System.currentTimeMillis();
    	int sz=countsDetail.size();
        final MergerDetailSelectDetailRowCompare cmp=this.createMergerDetailCmp();
    	 if(sz<=(saverecords*2))
         {
    		 SelectDetailRow[] arr = new SelectDetailRow[sz];
         	int index=0;
         	for(SelectDetailRow f:countsDetail)
         	{
         		arr[index]=f;
         		index++;
         	}
 	        Arrays.sort(arr, cmp);
 	        long t2=System.currentTimeMillis();
 	        LOG.info("####merger sort#### by array sort size="+sz+",timetaken="+(t2-t1));
 	        return arr;
 	        
         }
    	 
	    	PriorityQueue<SelectDetailRow> res = new PriorityQueue<SelectDetailRow>(saverecords, Collections.reverseOrder(cmp));
			for(SelectDetailRow f:countsDetail)
			{
				SelectDetailRow mrow=f;
				if (res.size() < saverecords) {
					res.add(mrow);
				} else if (cmp.compare(res.peek(), mrow) > 0) {
					res.add(mrow);
					res.poll();
				}
			}
			
			SelectDetailRow[] rtn=new SelectDetailRow[res.size()];
			res.toArray(rtn);
			Arrays.sort(rtn, cmp);
			
			 long t2=System.currentTimeMillis();
			 LOG.info("####merger sort#### by PriorityQueue size="+rtn.length+"@"+sz+",timetaken="+(t2-t1));
			return rtn;
	
    }
    

    public  GroupbyItem[] getPairSortedGroup(int saverecords) {
    	
    	
    	long t1=System.currentTimeMillis();
        Collection<GroupbyRow> collections=counts.values();

    	int sz=counts.size();
        final MergerGroupByGroupbyRowCompare cmp=this.createMergerGroupCmp();
    	 if(sz<=(saverecords*2))
         {
    		 GroupbyRow[] arr = new GroupbyRow[sz];
         	int index=0;
         	for(GroupbyRow f:collections)
         	{
         		arr[index]=f;
         		index++;
         	}
 	        Arrays.sort(arr, cmp);
 	        long t2=System.currentTimeMillis();
 	        LOG.info("####merger sort#### by array sort size="+sz+",timetaken="+(t2-t1));
 	        return arr;
 	        
         }
    	 


	    	PriorityQueue<GroupbyRow> res = new PriorityQueue<GroupbyRow>(saverecords, Collections.reverseOrder(cmp));
			for(GroupbyRow f:collections)
			{
				GroupbyRow mrow=f;
				if (res.size() < saverecords) {
					res.add(mrow);
				} else if (cmp.compare(res.peek(), mrow) > 0) {
					res.add(mrow);
					res.poll();
				}
			}
			
			GroupbyRow[] rtn=new GroupbyRow[res.size()];
			res.toArray(rtn);
			Arrays.sort(rtn, cmp);
			
			 long t2=System.currentTimeMillis();
			 LOG.info("####merger sort#### by PriorityQueue size="+rtn.length+"@"+sz+",timetaken="+(t2-t1));
			return rtn;
	
    }


    public GroupbyItem[] getPairSorted(int saverecords) {
        if(this.isdetail)
    	{
        	return getPairSortedDetail(saverecords);
    	}else{
    		return getPairSortedGroup( saverecords);
    	}
        
        
      }
  }

}
