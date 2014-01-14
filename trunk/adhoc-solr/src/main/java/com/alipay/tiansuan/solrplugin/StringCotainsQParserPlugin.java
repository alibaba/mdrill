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
package com.alipay.tiansuan.solrplugin;


import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;

/**
 * select in file
 * 
 *      <queryParser name="inhdfs" class="com.alipay.tiansuan.solrplugin.SelectInLocalFileQParserPlugin"/>

http://hdphadoop:8983/solr/select/?q=user_id%3A2*&version=2.2&start=0&rows=10&indent=on&fq={!contains%20f=user_id}hdfs://hdphadoop.com:9000/data/tiansuan/filein2
http://hdphadoop:8983/solr/select/?q=user_id%3A2*&version=2.2&start=0&rows=100&indent=on&fq={!infile%20f=user_id}/data/testdata/filein
 * {!inhdfs f=myfield}filename
 * @author yannian
 * @version $Id: SelectInLocalFileQParserPlugin.java, v 0.1 2012-7-19 上午10:44:24 yannian Exp $
 */
public class StringCotainsQParserPlugin extends QParserPlugin {
    public static String NAME = "contains";

    public void init(NamedList args) {
    }

    @Override
    public QParser createParser(String qstr, SolrParams localParams,
	    SolrParams params, SolrQueryRequest req) {
	return new QParser(qstr, localParams, params, req) {
	    @Override
	    public Query parse() throws ParseException {
		try {
		    String contains = localParams.get(QueryParsing.V);
		    String field=localParams.get(QueryParsing.F);
		    return new StringContainsQuery(contains.split(","), field);

		} catch (Exception e) {
		    throw new ParseException(e.toString());
		}
	    }
	};
    }
}

