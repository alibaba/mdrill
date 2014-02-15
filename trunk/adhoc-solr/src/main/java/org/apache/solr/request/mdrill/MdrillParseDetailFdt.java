package org.apache.solr.request.mdrill;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

public class MdrillParseDetailFdt {
    private static final Log logger = LogFactory.getLog(MdrillParseDetailFdt.class);

	public int offset ;
	public int limit_offset=0;
	
	public String crcOutputSet=null;
	public MdrillParseDetailFdt(SolrParams params)
	{

		logger.info("params:"+params.toString());
		this.offset = params.getInt(FacetParams.FACET_CROSS_OFFSET, 0);
		int limit = params.getInt(FacetParams.FACET_CROSS_LIMIT, 100);
		this.limit_offset=this.offset+limit;
		
		this.crcOutputSet=params.get("mdrill.crc.key.set");

	}
	public fetchContaioner createContainer(String[] fields, DocSet baseDocs,SegmentReader reader,SolrIndexSearcher searcher,SolrQueryRequest req) throws IOException, ParseException
	{
		return new fetchContaioner(this,fields,baseDocs,reader,searcher,req);
	}
	public static class fetchContaioner{
		public MdrillParseDetailFdt parse;
		public int groupbySize;
		
		public fetchContaioner(MdrillParseDetailFdt parse,String[] fields, DocSet baseDocs,SegmentReader reader,SolrIndexSearcher searcher,SolrQueryRequest req) throws IOException, ParseException
		{
			this.parse=parse;
			this.groupbySize=fields.length;
		}
		
		public void free()
		{
			GroupListCache.cleanFieldValueCache(groupbySize);
			SelectDetailRow.CLEAN();
		}
	}

}
