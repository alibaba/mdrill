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

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.*;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.mdrill.FdtMdrillCollector;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;


public class QueryComponent extends SearchComponent
{
	protected static Logger log = LoggerFactory.getLogger(QueryComponent.class);
	public static final String COMPONENT_NAME = "query";
  
  @Override
  public void prepare(ResponseBuilder rb) throws IOException
  {

    SolrQueryRequest req = rb.req;
    SolrParams params = req.getParams();
    if (!params.getBool(COMPONENT_NAME, true)) {
      return;
    }

    int fieldFlags = 0;

    rb.setFieldFlags( fieldFlags );

    String defType = params.get(QueryParsing.DEFTYPE,QParserPlugin.DEFAULT_QTYPE);
    if (rb.getQueryString() == null) {
      rb.setQueryString( params.get( CommonParams.Q ) );
    }

    try {
      QParser parser = QParser.getParser(rb.getQueryString(), defType, req);
      rb.setQuery( parser.getQuery() );
      rb.setSortSpec( parser.getSort(true) );
      rb.setQparser(parser);

      String[] fqs = req.getParams().getParams(CommonParams.FQ);
      if (fqs!=null && fqs.length!=0) {
        List<Query> filters = rb.getFilters();
        if (filters==null) {
          filters = new ArrayList<Query>(fqs.length);
        }
        for (String fq : fqs) {
          if (fq != null && fq.trim().length()!=0) {
            QParser fqp = QParser.getParser(fq, null, req);
            filters.add(fqp.getQuery());
          }
        }
        if (!filters.isEmpty()) {
          rb.setFilters( filters );
        }
      }
    } catch (ParseException e) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
    }


  }

  /**
   * Actually run the query
   */
  @Override
  public void process(ResponseBuilder rb) throws IOException
  {
    SolrQueryRequest req = rb.req;
    SolrQueryResponse rsp = rb.rsp;
    SolrParams params = req.getParams();
    if (!params.getBool(COMPONENT_NAME, true)) {
      return;
    }
    SolrIndexSearcher searcher = req.getSearcher();

    if (rb.getQueryCommand().getOffset() < 0) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "'start' parameter cannot be negative");
    }

    // -1 as flag if not set.
    long timeAllowed = (long)params.getInt( CommonParams.TIME_ALLOWED, -1 );

    SolrIndexSearcher.QueryCommand cmd = rb.getQueryCommand();
    cmd.setTimeAllowed(timeAllowed);
    SolrIndexSearcher.QueryResult result = new SolrIndexSearcher.QueryResult();
      
    if(params.getBool("fetchfdt", false))
	  {
    	 String crcget=params.get("mdrill.crc.key.get",null);
	   	  if(crcget!=null)
	   	  {
	   		  result.setDocSet(new BitDocSet());
	   	  }else{
		    	//TODO QUICK TOP N
		    	ArrayList<Query> qlist=new ArrayList<Query>();
		    	List<Query> list=rb.getFilters();
		    	if(list!=null)
		    	{
		    		for(Query q:list)
		    		{
		    			qlist.add(q);
		    		}
		    	}
				qlist.add(rb.getQuery());
		
		    	
		    	BooleanQuery query=new BooleanQuery();
		    	for(Query q:qlist)
		    	{
		    		query.add(q, BooleanClause.Occur.MUST);
		    	}
		    	int offset = params.getInt(FacetParams.FACET_CROSS_OFFSET, 0);
				int limit = params.getInt(FacetParams.FACET_CROSS_LIMIT, 100);
				int limit_offset=offset+limit;
		    	FdtMdrillCollector coll=new FdtMdrillCollector(limit_offset+2,searcher.maxDoc());
		    	searcher.ScoreFind(query, null,coll);
		    	result.setDocSet(new BitDocSet(coll.getBits()));
	   	  }
    	
	  }else{
		  searcher.search(result,cmd);
	  }
    rb.setResult( result );
    rsp.add("response",rb.getResults().docList);
  }
  
  


	@Override
	public int distributedProcess(ResponseBuilder rb) throws IOException {
		ShardRequest sreq = new ShardRequest();
		sreq.params = new ModifiableSolrParams(rb.req.getParams());
		rb.addRequest(this, sreq);
		return ResponseBuilder.STAGE_DONE;
	}



  @Override
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {

  }



  @Override
  public void finishStage(ResponseBuilder rb) {

  }

  @Override
  public String getDescription() {
    return "query";
  }

  @Override
  public String getVersion() {
    return "$Revision: 1173289 $";
  }

  @Override
  public String getSourceId() {
    return "$Id: QueryComponent.java 1173289 2011-09-20 18:18:55Z mvg $";
  }

  @Override
  public String getSource() {
    return "$URL: https://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_3_5/solr/core/src/java/org/apache/solr/handler/component/QueryComponent.java $";
  }

  @Override
  public URL[] getDocs() {
    return null;
  }
}
