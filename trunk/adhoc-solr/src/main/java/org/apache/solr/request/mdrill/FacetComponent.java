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

import org.apache.solr.common.params.CommonParams;
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
import org.apache.solr.request.SimpleFacets;
import org.apache.solr.request.compare.GroupbyAgent;
import org.apache.solr.request.compare.GroupbyItem;
import org.apache.solr.request.compare.GroupbyRow;
import org.apache.solr.request.compare.MergerDetailSelectDetailRowCompare;
import org.apache.solr.request.compare.MergerGroupByGroupbyRowCompare;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.compare.UniqTypeNum;
import org.apache.solr.request.join.HigoJoinSort;
import org.apache.solr.request.join.HigoJoinUtils;
import org.apache.solr.search.QueryParsing;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;


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

  /**
   * Actually run the query
   * @param rb
   */
  @Override
  public void process(ResponseBuilder rb) throws IOException
  {
    if (rb.doFacets) {
      SolrParams params = rb.req.getParams();
      SimpleFacets f = new SimpleFacets(rb.req,
              rb.getResults().docSet,
              params,
              rb );

      rb.rsp.add( "facet_counts", f.getFacetCounts() );
    }
  }

  
  @Override
  public int distributedProcess(ResponseBuilder rb) throws IOException {
	    return ResponseBuilder.STAGE_DONE;
  }

  @Override
  public void modifyRequest(ResponseBuilder rb, SearchComponent who, ShardRequest sreq) {
    if (!rb.doFacets) return;

    if ((sreq.purpose & ShardRequest.PURPOSE_GET_TOP_IDS) != 0) {
        sreq.purpose |= ShardRequest.PURPOSE_GET_FACETS;

        FacetInfo fi = rb._facetInfo;
        if (fi == null) {
          rb._facetInfo = fi = new FacetInfo();
          fi.parse(rb.req.getParams(), rb);
        }
        
        sreq.params.remove(FacetParams.FACET_MINCOUNT);
        sreq.params.remove(FacetParams.FACET_OFFSET);
        sreq.params.remove(FacetParams.FACET_LIMIT);
        sreq.params.remove(FacetParams.FACET_CROSS_OFFSET);
        sreq.params.remove(FacetParams.FACET_CROSS_LIMIT);
        
        int maxlimit=MdrillGroupBy.MAX_CROSS_ROWS;
        sreq.params.set(FacetParams.FACET_CROSS_OFFSET,  0);
    	sreq.params.set(FacetParams.FACET_CROSS_LIMIT,  maxlimit);
    } else {
      sreq.params.set(FacetParams.FACET, "false");
    }
  }
  
  
  

  @Override
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
    if (!rb.doFacets) return;

    if ((sreq.purpose & ShardRequest.PURPOSE_GET_FACETS)!=0) {
      countFacets(rb, sreq);
    } else if ((sreq.purpose & ShardRequest.PURPOSE_REFINE_FACETS)!=0) {
      refineFacets(rb, sreq);
    }
  }


  private void countFacets(ResponseBuilder rb, ShardRequest sreq) {
		long t1=System.currentTimeMillis();

    FacetInfo fi = rb._facetInfo;

    for (ShardResponse srsp: sreq.responses) {
      int shardNum = rb.getShardNum(srsp.getShard());
      NamedList facet_counts = (NamedList)srsp.getSolrResponse().getResponse().get("facet_counts");

      if(facet_counts!=null)
      {
      // handle facet queries
	      NamedList facet_queries = (NamedList)facet_counts.get("facet_queries");
	      if (facet_queries != null) {
	        for (int i=0; i<facet_queries.size(); i++) {
	          String returnedKey = facet_queries.getName(i);
	          long count = ((Number)facet_queries.getVal(i)).longValue();
	          QueryFacet qf = fi.queryFacets.get(returnedKey);
	          qf.count += count;
	        }
	      }
	
	      // step through each facet.field, adding results from this shard
	      NamedList facet_fields = (NamedList)facet_counts.get("facet_fields");
	    
	      if (facet_fields != null) {
	        for (DistribFieldFacet dff : fi.facets.values()) {
	          dff.add((NamedList)facet_fields.get(dff.getKey()),dff);
	        }
	      }
      }else{
    	  SolrCore.log.error("facet_counts is null "+srsp.getShard(),new Exception());
      }
    }
    
	long t2=System.currentTimeMillis();
	LOG.info("##countFacets## time taken "+(t2-t1)+",responses.size="+sreq.responses.size());

  }


  private void refineFacets(ResponseBuilder rb, ShardRequest sreq) {
		long t1=System.currentTimeMillis();

    FacetInfo fi = rb._facetInfo;

    long addtime=0;
    for (ShardResponse srsp: sreq.responses) {
      // int shardNum = rb.getShardNum(srsp.shard);
      NamedList facet_counts = (NamedList)srsp.getSolrResponse().getResponse().get("facet_counts");
      NamedList facet_fields = (NamedList)facet_counts.get("facet_fields");

      if (facet_fields == null) continue; // this can happen when there's an exception      

      for (int i=0; i<facet_fields.size(); i++) {
        String key = facet_fields.getName(i);
        DistribFieldFacet dff = fi.facets.get(key);
        if (dff == null) continue;

        NamedList shardCounts = (NamedList)facet_fields.getVal(i);
        addtime+=dff.add(shardCounts, dff);
      }
    }
    
    long t2=System.currentTimeMillis();
	LOG.info("##refineFacets## time taken "+(t2-t1)+",responses.size="+sreq.responses.size()+",addtime="+addtime);

  }

  @Override
  public void finishStage(ResponseBuilder rb) {
    if (!rb.doFacets || rb.stage != ResponseBuilder.STAGE_GET_FIELDS) return;
	long t1=System.currentTimeMillis();

    FacetInfo fi = rb._facetInfo;
    NamedList facet_counts = new SimpleOrderedMap();
    NamedList facet_queries = new SimpleOrderedMap();
    facet_counts.add("facet_queries",facet_queries);
    for (QueryFacet qf : fi.queryFacets.values()) {
      facet_queries.add(qf.getKey(), qf.count);
    }

    NamedList facet_fields = new SimpleOrderedMap();
    facet_counts.add("facet_fields", facet_fields);

    for (DistribFieldFacet dff : fi.facets.values()) {
      NamedList fieldCounts = new NamedList(); // order is more important for facets
      facet_fields.add(dff.getKey(), fieldCounts);

      int saverecords=dff.offset + dff.limit;
      GroupbyItem[] counts = dff.getPairSorted(dff.sort_column_type,dff.joinSort,dff.facetFs,dff.crossFs,dff.distFS,dff.sort_fl, dff.sort_type, dff.isdesc,saverecords);
      if(dff.recordcount!=null)
      {
    	  GroupbyItem recordcount=dff.recordcount;
	      fieldCounts.add(recordcount.getKey(), recordcount.toNamedList());
      }

      int end = dff.limit < 0 ? counts.length : Math.min(dff.offset + dff.limit, counts.length);
      for (int i=dff.offset; i<end; i++) {
        fieldCounts.add(counts[i].getKey(), counts[i].toNamedList());
      }
    }


    facet_counts.add("facet_dates", new SimpleOrderedMap());
    facet_counts.add("facet_ranges", new SimpleOrderedMap());

    rb.rsp.add("facet_counts", facet_counts);

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
    public LinkedHashMap<String,QueryFacet> queryFacets;
    public LinkedHashMap<String,DistribFieldFacet> facets;

	void parse(SolrParams params, ResponseBuilder rb) {
		queryFacets = new LinkedHashMap<String, QueryFacet>();
		facets = new LinkedHashMap<String, DistribFieldFacet>();
		String[] facetQs = params.getParams(FacetParams.FACET_QUERY);
		if (facetQs != null) {
			for (String query : facetQs) {
				QueryFacet queryFacet = new QueryFacet(rb, query);
				queryFacets.put(queryFacet.getKey(), queryFacet);
			}
		}

		DistribFieldFacet cross = new DistribFieldFacet(rb,"solrCorssFields_s");
		facets.put(cross.getKey(), cross);
	}
	
	void parse(SolrParams params) {
		queryFacets = new LinkedHashMap<String, QueryFacet>();
		facets = new LinkedHashMap<String, DistribFieldFacet>();
		String[] facetQs = params.getParams(FacetParams.FACET_QUERY);
		if (facetQs != null) {
			for (String query : facetQs) {
				QueryFacet queryFacet = new QueryFacet(params, query);
				queryFacets.put(queryFacet.getKey(), queryFacet);
			}
		}

		DistribFieldFacet cross = new DistribFieldFacet(params,"solrCorssFields_s");
		facets.put(cross.getKey(), cross);
	}
  }

  /**
   * <b>This API is experimental and subject to change</b>
   */
  public static class FacetBase {
    String facetType;  // facet.field, facet.query, etc (make enum?)
    String facetStr;   // original parameter value of facetStr
    String facetOn;    // the field or query, absent localParams if appropriate
    private String key; // label in the response for the result... "foo" for {!key=foo}myfield
    SolrParams localParams;  // any local params for the facet

    public FacetBase(ResponseBuilder rb, String facetType, String facetStr) {
      this.facetType = facetType;
      this.facetStr = facetStr;
      try {
        this.localParams = QueryParsing.getLocalParams(facetStr, rb.req.getParams());
      } catch (ParseException e) {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
      }
      this.facetOn = facetStr;
      this.key = facetStr;

      if (localParams != null) {
        // remove local params unless it's a query
        if (!facetType.equals(FacetParams.FACET_QUERY)) {
          facetOn = localParams.get(CommonParams.VALUE);
          key = facetOn;
        }

        key = localParams.get(CommonParams.OUTPUT_KEY, key);
      }
    }
      
      public FacetBase(SolrParams localParams, String facetType, String facetStr) {
          this.facetType = facetType;
          this.facetStr = facetStr;
          this.localParams = localParams;
          this.facetOn = facetStr;
          this.key = facetStr;

          if (localParams != null) {
            // remove local params unless it's a query
            if (!facetType.equals(FacetParams.FACET_QUERY)) {
              facetOn = localParams.get(CommonParams.VALUE);
              key = facetOn;
            }

            key = localParams.get(CommonParams.OUTPUT_KEY, key);
          }
    }

    /** returns the key in the response that this facet will be under */
    public String getKey() { return key; }
    public String getType() { return facetType; }
  }

  public static class QueryFacet extends FacetBase {
    public long count;

    public QueryFacet(ResponseBuilder rb, String facetStr) {
      super(rb, FacetParams.FACET_QUERY, facetStr);
    }
    public QueryFacet(SolrParams localParams, String facetStr) {
        super(localParams, FacetParams.FACET_QUERY, facetStr);
      }
  }
	public static class FieldFacet extends FacetBase {
		public int offset;
		public int limit;
		public boolean isFinalResult = true;
		public String sort_fl = null;
		public String sort_type = null;
		public boolean isdesc = true;
	    String[] facetFs = null;
		  HigoJoinSort[] joinSort={};

		public String[] crossFs ;
		public String[] distFS ;
		public boolean isdetail;
		
		public String sort_column_type;

		public FieldFacet(ResponseBuilder rb, String facetStr) {
			super(rb, FacetParams.FACET_FIELD, facetStr);
			fillParams(rb.req.getParams(), facetOn);
		}
		
		public FieldFacet(SolrParams localParams, String facetStr) {
			super(localParams, FacetParams.FACET_FIELD, facetStr);
			fillParams(localParams, facetOn);
		}

		private void fillParams(SolrParams params,
				String field) {
			this.offset = params.getInt(FacetParams.FACET_CROSS_OFFSET, 0);
			this.limit = params.getInt(FacetParams.FACET_CROSS_LIMIT, 100);
			boolean issub = params.getBool(FacetParams.IS_SUB_SHARDS, false);
			if (issub) {
				isFinalResult = false;
			} else {
				isFinalResult = true;
			}

            this.isdetail = params.getBool(FacetParams.FACET_CROSS_DETAIL,false);
            if(this.isdetail)
            {
            	this.sort_type="detailMerge";
            }else{
            	this.sort_type = params.get(FacetParams.FACET_CROSS_SORT_TYPE,"index");
            }
            this.sort_fl = params.get(FacetParams.FACET_CROSS_SORT_FL, null);
			
			this.isdesc = params.getBool(FacetParams.FACET_CROSS_SORT_ISDESC,true);
			this.crossFs = params.getParams(FacetParams.FACET_CROSS_FL);
			this.distFS=params.getParams(FacetParams.FACET_CROSSDIST_FL);
			this.facetFs=params.getParams(FacetParams.FACET_FIELD);
			String[] joinList=params.getParams(HigoJoinUtils.getTables());
			if(joinList==null)
			{
				joinList= new String[0];
			}
			this.joinSort=new HigoJoinSort[joinList.length];
			for(int i=0;i<joinList.length;i++)
			{
				this.joinSort[i]=new HigoJoinSort(joinList[i],params);
			}
			
			if(UniqTypeNum.parseSelectDetailType(this.facetFs, joinSort)!=null)
			{
				this.sort_column_type="string";

			}else{
				this.sort_column_type=params.get("facet.cross.sort.cp");
			}


		}
	}

  public static class DistribFieldFacet extends FieldFacet {

    public HashMap<String,GroupbyRow> counts = new HashMap<String,GroupbyRow>(128);
    public ArrayList<SelectDetailRow> countsDetail = new ArrayList<SelectDetailRow>(128);
    
    public GroupbyItem recordcount=null;
    DistribFieldFacet(SolrParams localParams, String facetStr) {
      super(localParams, facetStr);
    }
    DistribFieldFacet(ResponseBuilder rb, String facetStr) {
        super(rb, facetStr);
      }

    long add(NamedList shardCounts,DistribFieldFacet dff) {
    	long t1=System.currentTimeMillis();
      int sz = shardCounts == null ? 0 : shardCounts.size();
      for (int i=0; i<sz; i++) {
        String name = shardCounts.getName(i);
        Object obj=shardCounts.getVal(i);
        GroupbyAgent p=new GroupbyAgent(name,(NamedList)obj);
        p.setCross(this.crossFs, this.distFS);
        p.setFinalResult(dff.isFinalResult);
    	if(p.isrecordcount())
        {
    		if(recordcount==null)
    		{
    			recordcount=p;
    		}else{
    			recordcount.shardsMerge(p);
    		}
        }else{
        	if(this.isdetail)
        	{
        		countsDetail.add((SelectDetailRow)p.getRaw());
        	}else{
        		GroupbyRow sfc = counts.get(name);
	          if (sfc == null) {
	            counts.put(name,(GroupbyRow) p.getRaw());
	          }else{
		    	 sfc.shardsMerge((GroupbyRow)p.getRaw());
	          }
        	}
        }
    	
    	
        
      }
      
      long t2=System.currentTimeMillis();
      return t2-t1;
    }

    public GroupbyItem[] getPairSortedDetail(String sort_column_type,String[] crossFs, String[] distFS,String fl,String type,boolean isdesc,int saverecords) {
    	long t1=System.currentTimeMillis();
    	int sz=countsDetail.size();
        final MergerDetailSelectDetailRowCompare cmp=new MergerDetailSelectDetailRowCompare(sort_column_type,isdesc);
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
    
    
    public  GroupbyItem[] getPairSortedGroup(String sort_column_type,HigoJoinSort[] joinSort,String[] facetFs,String[] crossFs, String[] distFS,String fl,String type,boolean isdesc,int saverecords) {
    	
    	
    	long t1=System.currentTimeMillis();
        Collection<GroupbyRow> collections=counts.values();

    	int sz=counts.size();
        final MergerGroupByGroupbyRowCompare cmp=new MergerGroupByGroupbyRowCompare(sort_column_type,facetFs, crossFs, distFS, joinSort, fl, type, isdesc);
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


    public GroupbyItem[] getPairSorted(String sort_column_type,HigoJoinSort[] joinSort,String[] facetFs,String[] crossFs, String[] distFS,String fl,String type,boolean isdesc,int saverecords) {
        if(this.isdetail)
    	{
        	return getPairSortedDetail(sort_column_type,crossFs, distFS, fl, type, isdesc, saverecords);
    	}else{
    		return getPairSortedGroup(sort_column_type,joinSort,facetFs,crossFs, distFS, fl, type, isdesc, saverecords);
    	}
        
        
      }
  }

}
