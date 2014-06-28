package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.util.Collections;
import java.util.PriorityQueue;

import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowCompare;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowStringCompare;
import org.apache.solr.request.compare.UniqTypeNum;
import org.apache.solr.request.join.HigoJoinInvert;
import org.apache.solr.request.join.HigoJoinSort;
import org.apache.solr.request.join.HigoJoinUtils;
import org.apache.solr.request.mdrill.MdrillUtils.UnvertFields;
import org.apache.solr.request.mdrill.MdrillUtils.UnvertFile;
import org.apache.solr.request.uninverted.UnInvertedField;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

public class MdrillParseDetail {
	public int offset ;
	public String sort_fl;
	public boolean isNeedSort=true;
	public int limit_offset=0;
	public String[] joinList ;
	public boolean isdesc;
	public String sort_column_type;
	
	public String crcOutputSet=null;
	public MdrillParseDetail(SolrParams params)
	{
		this.joinList=params.getParams(HigoJoinUtils.getTables());
		if(this.joinList==null)
		{
			this.joinList= new String[0];
		}
		this.offset = params.getInt(FacetParams.FACET_CROSS_OFFSET, 0);
		int limit = params.getInt(FacetParams.FACET_CROSS_LIMIT, 100);
		this.limit_offset=this.offset+limit;
		this.sort_fl=params.get(FacetParams.FACET_CROSS_SORT_FL,null);
		
		if(this.sort_fl==null||this.sort_fl.isEmpty())
		{
			this.sort_fl="higoempty_sort_s";
			this.isNeedSort=false;
		}
		this.sort_column_type=params.get("facet.cross.sort.cp");
		this.isdesc=params.getBool(FacetParams.FACET_CROSS_SORT_ISDESC, true);
		this.crcOutputSet=params.get("mdrill.crc.key.set");


	}
	public fetchContaioner createContainer(String[] fields, DocSet baseDocs,SegmentReader reader,SolrIndexSearcher searcher,SolrQueryRequest req) throws IOException, ParseException
	{
		return new fetchContaioner(this,fields,baseDocs,reader,searcher,req);
	}
	public static class fetchContaioner{
		public ShardDetailSelectDetailRowCompare cmpTermNum;
		public ShardDetailSelectDetailRowStringCompare cmpresult;
//		public UnvertFields ufs=null;
		public UnvertFields sortufs=null;
		public HigoJoinInvert[] joinInvert={};
		public UniqTypeNum.SelectDetailSort SelectDetailSort=null;
		public boolean nonJoins=true;
		public MdrillParseDetail parse;
		public PriorityQueue<SelectDetailRow> res;
		public int groupbySize;
		
		HigoJoinInvert invforSortValue=null;
		int invforSortOffset=0;
		UnInvertedField columnSortcif=null;
		public String[] fields;
		
		public fetchContaioner(MdrillParseDetail parse,String[] fields, DocSet baseDocs,SegmentReader reader,SolrIndexSearcher searcher,SolrQueryRequest req) throws IOException, ParseException
		{
			this.parse=parse;
			this.fields=fields;
			sortufs=new UnvertFields(baseDocs,new String[]{parse.sort_fl}, reader,searcher.getPartionKey(),searcher.getSchema(),true);
			this.joinInvert=new HigoJoinInvert[parse.joinList.length];

			HigoJoinSort[] joinSort=new HigoJoinSort[this.parse.joinList.length];

			for(int i=0;i<this.parse.joinList.length;i++)
			{
				joinSort[i]=new HigoJoinSort(this.parse.joinList[i], req);
				this.joinInvert[i]=new HigoJoinInvert(this.parse.joinList[i], reader,searcher.getPartionKey(),searcher.getSchema());

				this.joinInvert[i].open(req);
				baseDocs=this.joinInvert[i].filterByRight(baseDocs);
			}
			this.SelectDetailSort=UniqTypeNum.parseSelectDetailType(fields, joinSort);
			this.cmpTermNum=new ShardDetailSelectDetailRowCompare(this.parse.isdesc);
			if(this.SelectDetailSort!=null)
			{
				this.parse.isNeedSort=true;
				this.cmpresult=new ShardDetailSelectDetailRowStringCompare("string",this.parse.isdesc);
				this.invforSortOffset=this.SelectDetailSort.selfOffset;
				this.invforSortValue=this.joinInvert[this.SelectDetailSort.sortIndex];


			}else{
				this.cmpresult=new ShardDetailSelectDetailRowStringCompare(this.parse.sort_column_type,this.parse.isdesc);
				if(this.isColumnSort())
				{
					UnvertFile uf=this.sortufs.cols[0];
					this.columnSortcif=uf.uif;
				}
			}
			
			res = new PriorityQueue<SelectDetailRow>(	this.parse.limit_offset, Collections.reverseOrder(cmpTermNum));
			
			this.groupbySize=this.fields.length;
			for(HigoJoinInvert inv:joinInvert)
			{
				this.groupbySize+=inv.fieldCount();
			}
			this.nonJoins=this.joinInvert.length<=0;

			
			
		}
		
		public boolean containsInJoins(int doc) throws IOException
		{
			if(this.nonJoins)
			{
				return true;
			}
			
			for(HigoJoinInvert inv:this.joinInvert)
			{
				if(!inv.contains(doc))
				{
					return false;
				}
			}
			return true;
		}
		
		public boolean isUseJoinSort()
		{
			return this.SelectDetailSort!=null;
		}
		
		public boolean isColumnSort()
		{
			return this.parse.isNeedSort&&this.sortufs.listIndex.length>0;
		}
		
		public boolean isOnlyColumnSort()
		{
			return this.parse.isNeedSort&&this.SelectDetailSort==null;
		}
		
		public int getJoinCompareValue(int doc) throws IOException
		{
			return this.invforSortValue.fieldNumForSort(doc, this.invforSortOffset);
		}
		
		public int getColumnCompareValue(int doc) throws IOException
		{
			return columnSortcif.termNum(doc);
		}
		
		public void free()
		{
			for(int i=0;i<this.parse.joinList.length;i++)
			{
				this.joinInvert[i].close();
			}
			sortufs.free();
			GroupListCache.cleanFieldValueCache(groupbySize);
			SelectDetailRow.CLEAN();
		}
	}

}
