package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.GroupbyRow;
import org.apache.solr.request.compare.ShardGroupByGroupbyRowCompare;
import org.apache.solr.request.compare.ShardGroupByTermNumCompare;
import org.apache.solr.request.join.HigoJoinInvert;
import org.apache.solr.request.join.HigoJoinSort;
import org.apache.solr.request.join.HigoJoinUtils;
import org.apache.solr.request.mdrill.GroupListCache.GroupList;
import org.apache.solr.request.mdrill.MdrillGroupBy.EmptyPrecontains;
import org.apache.solr.request.mdrill.MdrillGroupBy.Iprecontains;
import org.apache.solr.request.mdrill.MdrillGroupBy.PreContains;
import org.apache.solr.request.mdrill.MdrillUtils.RefRow;
import org.apache.solr.request.mdrill.MdrillUtils.RefRowStat;
import org.apache.solr.request.mdrill.MdrillUtils.TermNumToString;
import org.apache.solr.request.mdrill.MdrillUtils.UnvertFields;
import org.apache.solr.request.mdrill.MdrillUtils.UnvertFile;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import com.alimama.mdrill.distinct.DistinctCount;
import com.alimama.mdrill.utils.EncodeUtils;
import com.alimama.mdrill.utils.UniqConfig;

public class MdrillParseGroupby {
    private static Logger LOG = Logger.getLogger(MdrillParseGroupby.class);

	public int offset ;
	public String[] crossFs ;
	public String[] distFS ;
	public int limit_offset=0;
	public int limit_offset_maxgroups=0;
	public Integer maxlimit=10010;
	public String[] joinList ;
	public String[] preGroupList ;
	
	public String sort_fl;
	public String sort_type;
	public String sort_column_type;
	public boolean isdesc;
	
	public MdrillParseGroupby(SolrParams params)
	{
		this.offset = params.getInt(FacetParams.FACET_CROSS_OFFSET, 0);
		int limit = params.getInt(FacetParams.FACET_CROSS_LIMIT, 100);
		this.limit_offset=this.offset+limit;
		this.limit_offset_maxgroups=UniqConfig.ShardMaxGroups();
		this.sort_fl=params.get(FacetParams.FACET_CROSS_SORT_FL,null);
		this.sort_type=params.get(FacetParams.FACET_CROSS_SORT_TYPE,"index");
		this.isdesc=params.getBool(FacetParams.FACET_CROSS_SORT_ISDESC, true);
		this.crossFs = params.getParams(FacetParams.FACET_CROSS_FL);
		this.distFS=params.getParams(FacetParams.FACET_CROSSDIST_FL);
		this.joinList=params.getParams(HigoJoinUtils.getTables());
		this.sort_column_type=params.get("facet.cross.sort.cp");
		if(this.joinList==null)
		{
			this.joinList= new String[0];
		}
		
		this.preGroupList=params.getParams(FacetParams.FACET_CROSS_FL_PRE_GROUPS);
		if(this.preGroupList==null)
		{
			this.preGroupList= new String[0];
		}
		this.maxlimit=MdrillGroupBy.MAX_CROSS_ROWS;
	}
	
	public fetchContaioner createContainer(String[] fields, DocSet baseDocs,SegmentReader reader,SolrIndexSearcher searcher,SolrQueryRequest req) throws IOException, ParseException
	{
		return new fetchContaioner(this,fields,baseDocs,reader,searcher,req);
	}
	
	public boolean hasStat()
	{
		return this.crossFs !=null&&this.crossFs.length>0;
	}
	
	public boolean hasDist()
	{
		return this.distFS !=null&&this.distFS.length>0;
	}
	
	
	public static class fetchContaioner{
		public HashSet<GroupListCache.GroupList> preSet;
		public UnvertFields ufs=null;
		public UnvertFields crossufs=null;
		public UnvertFields distufs=null;
		public HigoJoinInvert[] joinInvert={};
		public HigoJoinSort[] joinSort={};
		public LinkedBlockingQueue<GroupListCache.GroupList> groupListCache;
		public ShardGroupByTermNumCompare cmpTermNum;
		public ShardGroupByGroupbyRowCompare cmpString;
		public PriorityQueue<GroupbyRow> res;
		public int groupbySize;
		public int groupNonEmptySize;
		public Iprecontains pre;
		public MdrillParseGroupby parse;
		public DocSet baseDocs;
		public HigoJoinUtils.MakeGroups make;
		public  fetchContaioner(MdrillParseGroupby parse,String[] fields, DocSet baseDocs,SegmentReader reader,SolrIndexSearcher searcher,SolrQueryRequest req) throws IOException, ParseException
		{
			this.parse=parse;
			this.ufs=new UnvertFields(fields, reader,searcher.getPartionKey(),searcher.getSchema());
			this.crossufs=new UnvertFields(parse.crossFs, reader,searcher.getPartionKey(),searcher.getSchema());
			this.distufs=new UnvertFields(parse.distFS, reader,searcher.getPartionKey(),searcher.getSchema());
			
			
			this.joinInvert=new HigoJoinInvert[parse.joinList.length];
			this.joinSort=new HigoJoinSort[parse.joinList.length];
			int presize=baseDocs.size();
			for(int i=0;i<parse.joinList.length;i++)
			{
				this.joinSort[i]=new HigoJoinSort(parse.joinList[i], req);
				this.joinInvert[i]=new HigoJoinInvert(parse.joinList[i], reader,searcher.getPartionKey(),searcher.getSchema());

				this.joinInvert[i].open(req);
				baseDocs=this.joinInvert[i].filterByRight(baseDocs);
			}
			this.baseDocs=baseDocs;
			this.cmpString=new ShardGroupByGroupbyRowCompare(parse.sort_column_type,fields, parse.crossFs, parse.distFS, this.joinSort, parse.sort_fl, parse.sort_type, parse.isdesc);
			this.cmpTermNum=new ShardGroupByTermNumCompare(fields, parse.crossFs, parse.distFS, this.joinSort, parse.sort_fl, parse.sort_type, parse.isdesc);
			this.res = new PriorityQueue<GroupbyRow>(	parse.limit_offset, Collections.reverseOrder(this.cmpString));
			
			this.groupbySize=this.ufs.length;
			for(HigoJoinInvert inv:this.joinInvert)
			{
				this.groupbySize+=inv.fieldCount();
			}
			this.groupListCache=GroupListCache.getGroupListQueue(this.groupbySize);
			
			this.preSet=new HashSet<GroupListCache.GroupList>(parse.preGroupList.length);

			for(int i=0;i<parse.preGroupList.length;i++)
			{
				this.preSet.add(makePreGroup(parse.preGroupList[i]));
			}	
			this.pre=new EmptyPrecontains();
			if(this.preSet.size()>0)
			{
				this.pre=new PreContains(this.preSet);
			}
			if(joinInvert.length>=0)
			{
				make=new HigoJoinUtils.MakeGroupsJoin(this.groupListCache);;
			}else{
				make=new HigoJoinUtils.MakeGroupsDefault();
			}
			
			groupNonEmptySize=this.ufs.listIndex.length;
			for(HigoJoinInvert inv:joinInvert)
			{
				groupNonEmptySize+=inv.fieldCount();
			}
			LOG.info("##baseDocs.size## "+baseDocs.size()+"@"+presize);

		}
		
		private GroupListCache.GroupList makePreGroup(String g)
		{
			String[] values = EncodeUtils.decode(g.split(UniqConfig.GroupJoinString()));
			GroupListCache.GroupList group=GroupListCache.GroupList.INSTANCE(groupListCache, values.length);
			group.reset();
			for (int i:ufs.listIndex) {
				UnvertFile uf=ufs.cols[i];
				try {
					group.list[i]=uf.uif.getTermNum(uf.ti,values[i],uf.filetype);
				} catch (Throwable e) {
					LOG.error("makePreGroup",e);
					group.list[i]=uf.uif.getNullTm();
				}
			}
			
			int joinoffset=ufs.length;
			for(HigoJoinInvert inv:joinInvert)
			{
				inv.setfieldNum(values, joinoffset, group);
				joinoffset+=inv.fieldCount();
			}
			return group;
		}
		
		public boolean countOnly()
		{
			return this.crossufs.listIndex.length==0&&(this.parse.distFS==null||this.distufs.listIndex.length==0);
		}
		
		public boolean noDist()
		{
			return this.parse.distFS==null||distufs.listIndex.length==0;
		}
		
		
		
		public boolean noStat()
		{
			return this.crossufs.listIndex.length==0;
		}
		
		
		public void updateStat(RefRow cnt,int doc) throws IOException
		{
			for (int i:this.crossufs.listIndex) {
				UnvertFile uf=this.crossufs.cols[i];
				double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
				cnt.stat[i].update(value);
			}
		}
		
		public void updateDist(RefRow cnt,int doc) throws IOException
		{
			for (int i:this.distufs.listIndex) {
				UnvertFile uf=this.distufs.cols[i];
				double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
				cnt.dist[i].set(String.valueOf(value));
			}
		}
		
		public boolean toGroupsByJoin(int doc, GroupListCache.GroupList group) throws IOException {
			 return this.make.toGroupsByJoin(doc, group, this.ufs, this.joinInvert);
		}
		
		RefRow emptyrow=null;
		public RefRow getEmptyRow()
		{
			if(emptyrow==null)
			{
				emptyrow=this.createRow();
			}
			
			return emptyrow;
		}
		
		public RefRow createRow()
		{
			RefRow cnt=new RefRow();
	      	  if(this.parse.crossFs!=null)
	      	  {
	      		  cnt.stat=new RefRowStat[this.parse.crossFs.length];
	      		  for(int i=0;i<this.parse.crossFs.length;i++)
	      		  {
	      			  cnt.stat[i]=new RefRowStat();
	      		  }
	      	  }
	      	  
	      	 if(this.parse.distFS!=null)
	     	  {
	     		  cnt.dist=new DistinctCount[this.parse.distFS.length];
	     		  for(int i=0;i<this.parse.distFS.length;i++)
	     		  {
	     			  cnt.dist[i]=new DistinctCount();
	     		  }
	     	  }
	      	 
	      	 return cnt;
		}
		
		public TermNumToString[] prefetch(QuickHashMap<GroupListCache.GroupList,RefRow> groups) throws IOException
		{
			TermNumToString[] tm=new TermNumToString[this.ufs.length];
			for(int i=0;i<this.ufs.length;i++)
			 {
				tm[i]=new TermNumToString(this.ufs,i);
			 }
			
			 for(Entry<GroupListCache.GroupList,RefRow> e:groups.entrySet())
			 {
				 int[] group=e.getKey().list;
				 for(int i=0;i<this.ufs.length;i++)
				 {
					 if(this.ufs.cols[i]!=null)
					 {
						 tm[i].addTermNum(group[i]);
					 }
				 }
				 
				 
				 int joinoffset=this.ufs.length;
				for(HigoJoinInvert inv: this.joinInvert)
				{
					int fc=inv.fieldCount();
					for(int i=0;i<fc;i++)
					{
						inv.addTermNum(group[joinoffset+i], i);
					}
					joinoffset+=fc;
				}
			 }
			 
			 
			 //fetch by field 
			 for(int i=0;i<this.ufs.length;i++)
			 {
				tm[i].fetchValues();
			 }
			 
			 for(HigoJoinInvert inv: this.joinInvert)
			 {
				 inv.fetchValues();
			 }
			 
			 return tm;
		}
		
		public void free(QuickHashMap<GroupListCache.GroupList,RefRow> groups)
		{
			ufs.free();
			crossufs.free();
			distufs.free();
			for(GroupListCache.GroupList g:preSet)
			{
				this.groupListCache.add(g);
			}
			
			for(GroupListCache.GroupList g:groups.keySet())
			{
				this.groupListCache.add(g);
			}
			
			GroupListCache.cleanFieldValueCache(groupbySize);

			for(int i=0;i<this.parse.joinList.length;i++)
			{
				this.joinInvert[i].close();
			}
		}
	}
}
