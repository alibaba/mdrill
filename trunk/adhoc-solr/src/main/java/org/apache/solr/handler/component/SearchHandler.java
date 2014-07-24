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

package org.apache.solr.handler.component;

import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.handler.component.ResponseBuilder.ScheduleInfo;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.mdrill.FacetComponent;
import org.apache.solr.request.mdrill.MdrillGroupBy;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.apache.solr.core.SolrCore;
import org.apache.lucene.queryParser.ParseException;
import org.apache.commons.httpclient.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;


public class SearchHandler extends RequestHandlerBase implements SolrCoreAware
{
  protected static Logger log = LoggerFactory.getLogger(SearchHandler.class);

  protected List<SearchComponent> components = null;

  protected List<String> getDefaultComponents()
  {
    ArrayList<String> names = new ArrayList<String>(6);
    names.add( QueryComponent.COMPONENT_NAME );
    names.add( FacetComponent.COMPONENT_NAME );
    return names;
  }

  public void inform(SolrCore core)
  {
	  if(components!=null)
	  {
		  return ;
	  }
    List<String> list = getDefaultComponents();

    components = new ArrayList<SearchComponent>( list.size() );
    for(String c : list){
      SearchComponent comp = core.getSearchComponent( c );
      components.add(comp);
//      log.info("Adding  component:"+comp);
    }

  }

  public List<SearchComponent> getComponents() {
    return components;
  }
  

  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception, ParseException, InstantiationException, IllegalAccessException
  {
    ResponseBuilder rb = new ResponseBuilder();
    rb.req = req;
    rb.rsp = rsp;
    rb.components = components;
    rb.setDebug(false);//清理用不到的debug

    for( SearchComponent c : components ) {
        c.prepare(rb);
      }
    SolrParams paramsr=req.getParams();
    String shards = paramsr.get(ShardParams.SHARDS);

    if (shards == null) {
	 for( SearchComponent c : components ) {
         c.process(rb);
       }
	 	return ;
    }
    
    

    String mergeServers=paramsr.get(FacetParams.MERGER_SERVERS);
	List<String> lst = StrUtils.splitSmart(shards, ",", true);
	List<String> mslist = StrUtils.splitSmart(mergeServers, ",", true);

    String[] partions=paramsr.getParams(ShardParams.PARTIONS);
    
    ScheduleInfo scheduleInfo=MergerSchedule.schedule(paramsr, lst, mslist,partions);
    
    
    int depth=req.getParams().getInt("__higo_ms_depth__", 0);
    HttpCommComponent comm = new HttpCommComponent(depth);

    ShardRequest sreq = new ShardRequest();
    sreq.params = new ModifiableSolrParams(rb.req.getParams());

    for (SearchComponent c : components) {
		c.modifyRequest(rb, c, sreq);
		c.distributedProcess(rb);
    }

    sreq.responses = new ArrayList<ShardResponse>();

    for (int i=0;i<scheduleInfo.shards.length;i++) {
        ModifiableSolrParams params = new ModifiableSolrParams(sreq.params);
        params.remove(ShardParams.SHARDS);      // not a top-level request
  		params.remove("indent");
		params.remove(ShardParams.PARTIONS);
  		params.remove(CommonParams.HEADER_ECHO_PARAMS);
  		params.set(ShardParams.IS_SHARD, true);  // a sub (shard) request
  		if(scheduleInfo.partions!=null)
        {
            params.set(ShardParams.PARTIONS, scheduleInfo.partions);
        }
        if(scheduleInfo.hasSubShards)
        {
       	  params.set(ShardParams.SHARDS,scheduleInfo.subShards[i]);
        }
        
//        params.set(FacetParams.IS_SUB_SHARDS, true);
		params.set(FacetParams.FACET_CROSS_OFFSET, 0);
  	  	params.set(FacetParams.FACET_CROSS_LIMIT, MdrillGroupBy.MAX_CROSS_ROWS); 
  	  
        params.remove(CommonParams.QT);
        comm.submit( scheduleInfo,sreq, scheduleInfo.shards[i], params,depth);
    }
  
		while (true) {
			ShardResponse srsp = comm.takeCompletedOrError();
			if (srsp == null)
			{
				break; // no more requests to wait for
			}
			if (srsp.getException() != null) {
				comm.cancelAll();
				if (srsp.getException() instanceof SolrException) {
					throw (SolrException) srsp.getException();
				} else {
					throw new SolrException(
							SolrException.ErrorCode.SERVER_ERROR,
							srsp.getException());
				}
			}

			for (SearchComponent c : components) {
				c.handleResponses(rb, srsp.getShardRequest());
			}
		}
	        

      for(SearchComponent c : components) {
          c.finishStage(rb);
       }

  
  }


  @Override
  public String getDescription() {
    StringBuilder sb = new StringBuilder();
    sb.append("Search using components: ");
    if( components != null ) {
      for(SearchComponent c : components){
        sb.append(c.getName());
        sb.append(",");
      }
    }
    return sb.toString();
  }

  @Override
  public String getVersion() {
    return "$Revision: 1052938 $";
  }

  @Override
  public String getSourceId() {
    return "$Id: SearchHandler.java 1052938 2010-12-26 20:21:48Z rmuir $";
  }

  @Override
  public String getSource() {
    return "$URL: https://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_3_5/solr/core/src/java/org/apache/solr/handler/component/SearchHandler.java $";
  }
}



class HttpCommComponent {
  CompletionService<ShardResponse> completionService =null;
  Set<Future<ShardResponse>> pending = new HashSet<Future<ShardResponse>>();
  HttpCommComponent(int depth) {
	  this.completionService= MergerServerThreads.create(depth);
  }

  public static class SimpleSolrResponse extends SolrResponse {
	private static final long serialVersionUID = 1L;
	long elapsedTime;
    NamedList<Object> nl;
    
    @Override
    public long getElapsedTime() {
      return elapsedTime;
    }

    @Override
    public NamedList<Object> getResponse() {
      return nl;
    }

    @Override
    public void setResponse(NamedList<Object> rsp) {
      nl = rsp;
    }
  }
  
	private static void setupParams(final ModifiableSolrParams params, String[] shardparams) {
		if (shardparams.length > 1) {
			params.set(CommonParams.PARTION, shardparams[1]);
		}

		params.remove(CommonParams.WT); // use default (currently javabin)
		params.remove(CommonParams.VERSION);
	}
	
	private static HttpClient makeHttpClient()
	{
		 HttpClient cli = new HttpClient();
		 cli.getHttpConnectionManager().getParams().setConnectionTimeout(1000*60*100);
		 cli.getHttpConnectionManager().getParams().setSoTimeout(1000*60*100);
         cli.getParams().setConnectionManagerTimeout(1000*60*100);
         
         return cli;
	}

  void submit(final ScheduleInfo scheduleInfo,final ShardRequest sreq, final String shard, final ModifiableSolrParams params,final int depth) {
	  

	    final long begintime=System.currentTimeMillis();
    Callable<ShardResponse> task = new Callable<ShardResponse>() {
      public ShardResponse call() throws Exception {

        ShardResponse srsp = new ShardResponse();
        srsp.setScheduleInfo(scheduleInfo);
        srsp.setShardRequest(sreq);
        srsp.setShard(shard);
        SimpleSolrResponse ssr = new SimpleSolrResponse();
        srsp.setSolrResponse(ssr);
        long startTime = System.currentTimeMillis();
    	String[] shardparams=shard.split("@");
        String url = "http://" + shardparams[0];
        try {
        	params.set("__higo_ms_depth__", (depth+1));
	         HttpCommComponent.setupParams(params, shardparams);
    		 HttpClient cli =HttpCommComponent.makeHttpClient();
	         SolrServer server = new CommonsHttpSolrServer(url, cli);
	      
	         QueryRequest req = new QueryRequest(params);
	         req.setMethod(SolrRequest.METHOD.POST);
	         ssr.nl = server.request(req);
	         cli.getHttpConnectionManager().closeIdleConnections(0);
	         
	         Map<String,String> timetaken=(Map<String,String>) ssr.nl.get("mdrill_shard_time");
	         if(timetaken==null)
	         {
	        	 timetaken=new LinkedHashMap<String,String>();
		         ssr.nl.add("mdrill_shard_time", timetaken);
	         }
	        
	         long t2=System.currentTimeMillis();
	         long timetake=t2-startTime;
	         long waittime=startTime-begintime;
	         
	         timetaken.put(String.valueOf(depth)+"@"+shard, String.valueOf(timetake)+"@"+waittime);
	         
	         SearchHandler.log.info("##HttpClient## timetaken="+(timetake)+"@"+waittime+",shard:"+shard);
        	
        }catch (RuntimeException th) {
        	SearchHandler.log.error(shard+"@"+params.toString(),th);
            srsp.setException(new Exception(shard, th));
            srsp.setResponseCode(-1);
        }
        catch (Throwable th) {
        SearchHandler.log.error(shard+"@"+params.toString(),th);
          srsp.setException(new Exception(shard, th));
          if (th instanceof SolrException) {
            srsp.setResponseCode(((SolrException)th).code());
          } else {
            srsp.setResponseCode(-1);
          }
        }

        ssr.elapsedTime = System.currentTimeMillis() - startTime;
        return srsp;
      }
	};
    
    pending.add( completionService.submit(task) );
  }


  ShardResponse takeCompletedOrError() {
    while (pending.size() > 0) {
      try {
        Future<ShardResponse> future = completionService.take();
        pending.remove(future);
        ShardResponse rsp = future.get();
        if (rsp.getException() != null)
    	{
    		return rsp; // if exception, return immediately
    	}

        rsp.getShardRequest().responses.add(rsp);
        if(rsp.getShardRequest().responses.size()==rsp.getScheduleInfo().shards.length)
        {
        	return rsp;
        }
      } catch (InterruptedException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      } catch (ExecutionException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Impossible Exception",e);
      }
    }
    return null;
  }


  void cancelAll() {
    for (Future<ShardResponse> future : pending) {
      future.cancel(true);
    }
  }

}
