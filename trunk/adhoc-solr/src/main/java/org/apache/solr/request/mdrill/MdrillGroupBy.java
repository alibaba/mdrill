package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexReader;
import org.apache.solr.search.SolrIndexSearcher;

import com.alimama.mdrill.distinct.DistinctCount;
import com.alimama.mdrill.utils.EncodeUtils;
import com.alimama.mdrill.utils.UniqConfig;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.GroupbyItem;
import org.apache.solr.request.compare.GroupbyRow;
import org.apache.solr.request.compare.RecordCount;
import org.apache.solr.request.compare.ShardGroupByGroupbyRowCompare;
import org.apache.solr.request.compare.ShardGroupByTermNum;
import org.apache.solr.request.compare.ShardGroupByTermNumCompare;
import org.apache.solr.request.join.HigoJoinInvert;
import org.apache.solr.request.join.HigoJoinSort;
import org.apache.solr.request.join.HigoJoinUtils;
import org.apache.solr.request.mdrill.FacetComponent.DistribFieldFacet;
import org.apache.solr.request.mdrill.MdrillPorcessUtils.*;

/**
 * 多列group by 分类 汇总的实现
 * @author yannian.mu
 */
public class MdrillGroupBy {
    private static Logger LOG = Logger.getLogger(MdrillGroupBy.class);

	public static Integer MAX_CROSS_ROWS=UniqConfig.defaultCrossMaxLimit();
	private SolrIndexSearcher searcher;
	private SolrParams params;
	private int offset ;
	private RecordCount recordCount ;
	
	private String[] crossFs ;
	private String[] distFS ;
	private int limit_offset=0;
	private int limit_offset_maxgroups=0;
	private Integer maxlimit=10010;
	private int mergercount=0;
	private String[] joinList ;
	private SolrQueryRequest req;
	private String[] preGroupList ;
	
	private String sort_fl;
	private String sort_type;
	private String sort_column_type;
	private boolean isdesc;
	private ShardGroupByTermNumCompare cmpTermNum;
	private ShardGroupByGroupbyRowCompare cmpString;

	public MdrillGroupBy(SolrIndexSearcher _searcher,SolrParams _params,SolrQueryRequest req)
	{
		this.req=req;
		this.searcher=_searcher;
		this.params=_params;
		this.init();
	}
	
	private SegmentReader reader;
	private boolean isSchemaReaderType=false;
	public MdrillGroupBy(SolrIndexSearcher _searcher,SegmentReader reader,SolrParams _params,SolrQueryRequest req)
	{
		this.isSchemaReaderType=true;
		this.reader=reader;
		this.searcher=_searcher;
		this.req=req;
		this.params=_params;
		this.init();
	}
	
	private void init()
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
		this.recordCount = new RecordCount();
		this.recordCount.setFinalResult(false);
		this.recordCount.setMaxUniqSize(this.maxlimit);
	}
	
	
	public NamedList getBySchemaReader(String[] fields, DocSet base)throws Exception 
	{
		SolrIndexReader reader=this.searcher.getReader();
		IndexReader.InvertParams invparam=new IndexReader.InvertParams();
		invparam._searcher=this.searcher;
		invparam._params=this.params;
		invparam.fields=fields;
		invparam.base=base;
		invparam.req=this.req;
		invparam.isdetail=false;
		IndexReader.InvertResult result=reader.invertScan(this.searcher.getSchema(), invparam);
		ArrayList<NamedList> resultlist=result.getResult();
		if(resultlist.size()==1)
		{
			return resultlist.get(0);
		}
		
		FacetComponent.FacetInfo fi = new FacetComponent.FacetInfo();
	     fi.parse(params);
        DistribFieldFacet dff = fi.cross;
        dff.isdetail=false;
	     for (NamedList nl: resultlist) {
	         dff.add(nl, dff);
	     }
	     
	     NamedList fieldCounts = new NamedList();
	      GroupbyItem[] counts = dff.getPairSorted(dff.sort_column_type,dff.joinSort,dff.facetFs,dff.crossFs,dff.distFS,dff.sort_fl, dff.sort_type, dff.isdesc,this.limit_offset);
	      if(dff.recordcount!=null)
	      {
	    	  GroupbyItem recordcount=dff.recordcount;
		      fieldCounts.add(recordcount.getKey(), recordcount.toNamedList());
	      }
	      int end = this.limit_offset> counts.length ?counts.length:this.limit_offset;
	      for (int i=this.offset; i<end; i++) {
	        fieldCounts.add(counts[i].getKey(), counts[i].toNamedList());
	      }

		return fieldCounts;
	}
	
	public NamedList getCross(String[] fields, DocSet base) throws IOException,
			ParseException {
		synchronized (MdrillPorcessUtils.getLock()) {
			PriorityQueue<GroupbyRow> topItems = new PriorityQueue<GroupbyRow>(	this.limit_offset, Collections.reverseOrder(cmpString));
			this.execute(topItems, fields, base);
			return this.toNameList(topItems);	
		}
	}
		
	ShardGroupByTermNum smallestShardGroup=null;
	public void TopMaps(UnvertFields crossufs,UnvertFields distufs,QuickHashMap<GroupList,RefRow> groups)
	{
		long t1=System.currentTimeMillis();
		int groupsize=groups.size();
		if(groupsize<=this.limit_offset)
		{
			return ;
		}
		PriorityQueue<ShardGroupByTermNum> res = new PriorityQueue<ShardGroupByTermNum>(this.limit_offset, Collections.reverseOrder(this.cmpTermNum));
		LinkedBlockingQueue<GroupList> toremove=new LinkedBlockingQueue<MdrillPorcessUtils.GroupList>();;
		QuickHashMap<GroupList,RefRow> debug=new QuickHashMap<MdrillPorcessUtils.GroupList, MdrillPorcessUtils.RefRow>(this.limit_offset);

		for(Entry<GroupList,RefRow> e:groups.entrySet())
		{
			debug.put(e.getKey(), e.getValue());
			ShardGroupByTermNum mrow=new ShardGroupByTermNum(e.getKey(), e.getValue());
			if (res.size() < limit_offset) {
				res.add(mrow);
			} else if (this.cmpTermNum.compare(res.peek(), mrow) > 0) {
				res.add(mrow);
				ShardGroupByTermNum free=res.poll();
				toremove.add(free.key);
			}else{
				toremove.add(mrow.key);
			}
		}
		int cnt1=0;

		for(GroupList torm:toremove)
		{
			groups.remove(torm);
			this.groupListCache.add(torm);
			cnt1++;
		}
		
		smallestShardGroup=res.peek();
		
		long t2=System.currentTimeMillis();
		LOG.info("TopMaps groups.size="+groupsize+"@"+debug.size() +" to "+groups.size()+"@"+this.limit_offset+",res.size="+res.size()+",remove="+cnt1+",timetaken="+(t2-t1)+",mergercount="+mergercount);
	}

	private NamedList toNameList(PriorityQueue<GroupbyRow> topItems) {
		java.util.ArrayList<GroupbyRow> recommendations = new ArrayList<GroupbyRow>(topItems.size());
		recommendations.addAll(topItems);
		Collections.sort(recommendations, cmpString);

		Integer index = 0;
		NamedList res = new NamedList();
		res.add(recordCount.getKey(), recordCount.toNamedList());
		for (GroupbyRow kv : recommendations) {
			if (index >= this.offset) {
				res.add(kv.getKey(), kv.toNamedList());
			}
			index++;
		}
		return res;
	}
		  
	private GroupbyRow setCrossRow(RefRow ref,String groupname,PriorityQueue<GroupbyRow> res) throws ParseException, IOException
	  {
		  this.recordCount.setCrcRecord(groupname);
		  GroupbyRow row = new GroupbyRow(groupname, ref.val);
		  row.setCross(this.crossFs, this.distFS);
		  row.setFinalResult(false);
		  if(this.crossFs !=null&&this.crossFs.length>0)
			{
				for(int i=0;i<this.crossFs.length;i++)
				{
					RefRowStat s=ref.stat[i];
					if(s.issetup)
					{
						row.addStat(i, 1, s.sum);
						row.addStat(i, 2, s.max);
						row.addStat(i, 3, s.min);
						row.addStat(i, 4, (double)s.cnt);
					}else{
						row.addStat(i, 1, 0d);
						row.addStat(i, 2, 0d);
						row.addStat(i, 3, 0d);
						row.addStat(i, 4, 0d);
					}
				}
			}
			
			if(this.distFS !=null&&this.distFS.length>0)
			{
				for(int i=0;i<this.distFS.length;i++)
				{
					row.addDistinct(i, ref.dist[i]);
				}
			}
			
			MdrillPorcessUtils.put2Queue(row, res, this.limit_offset,this.cmpString);
			return row;
			
	  }
	  
	private HigoJoinInvert[] joinInvert={};
	private HigoJoinSort[] joinSort={};
	private LinkedBlockingQueue<GroupList> groupListCache;
	
	public void execute(PriorityQueue<GroupbyRow> res,String[] fields,DocSet baseDocs) throws IOException, ParseException
	{
		long t1=System.currentTimeMillis();
		UnvertFields ufs=null;
		UnvertFields crossufs=null;
		UnvertFields distufs=null;
		if(!isSchemaReaderType)
		{
			ufs=new UnvertFields(fields, searcher);
			crossufs=new UnvertFields(this.crossFs, searcher);
			distufs=new UnvertFields(this.distFS, searcher);
		}else{
			ufs=new UnvertFields(fields, this.reader,this.searcher.getPartionKey(),this.searcher.getSchema());
			crossufs=new UnvertFields(this.crossFs, this.reader,this.searcher.getPartionKey(),this.searcher.getSchema());
			distufs=new UnvertFields(this.distFS, this.reader,this.searcher.getPartionKey(),this.searcher.getSchema());
		}
		this.joinInvert=new HigoJoinInvert[this.joinList.length];
		this.joinSort=new HigoJoinSort[this.joinList.length];
		int presize=baseDocs.size();
		for(int i=0;i<this.joinList.length;i++)
		{
			this.joinSort[i]=new HigoJoinSort(this.joinList[i], this.req);
			if(!isSchemaReaderType)
			{
				this.joinInvert[i]=new HigoJoinInvert(this.joinList[i], this.searcher);
			}else{
				this.joinInvert[i]=new HigoJoinInvert(this.joinList[i], this.reader,this.searcher.getPartionKey(),this.searcher.getSchema());
			}
			this.joinInvert[i].open(this.req);
			baseDocs=this.joinInvert[i].filterByRight(baseDocs);
		}
		this.cmpString=new ShardGroupByGroupbyRowCompare(this.sort_column_type,fields, crossFs, distFS, this.joinSort, this.sort_fl, this.sort_type, this.isdesc);
		this.cmpTermNum=new ShardGroupByTermNumCompare(fields, crossFs, distFS, this.joinSort, this.sort_fl, this.sort_type, this.isdesc);

		int groupbySize=ufs.length;
		for(HigoJoinInvert inv:joinInvert)
		{
			groupbySize+=inv.fieldCount();
		}
		
		this.groupListCache=MdrillPorcessUtils.getGroupListQueue(groupbySize);
		
		HashSet<GroupList> preSet=new HashSet<GroupList>(this.preGroupList.length);

		for(int i=0;i<this.preGroupList.length;i++)
		{
			preSet.add(makePreGroup(this.preGroupList[i],ufs,this.joinInvert));
		}	
		Iprecontains pre=new EmptyPrecontains();
		if(preSet.size()>0)
		{
			pre=new PreContains(preSet);
		}
		
		LOG.info("##baseDocs.size## "+baseDocs.size()+"@"+presize);

		long t2=System.currentTimeMillis();
		QuickHashMap<GroupList,RefRow> groups=this.makeGroups(this.maxlimit,crossufs, distufs, ufs, fields, baseDocs,pre);
		long t3=System.currentTimeMillis();
		this.transGroupValue(groups, ufs, res);
		long t4=System.currentTimeMillis();
		ufs.free();
		crossufs.free();
		distufs.free();
		
		for(GroupList g:preSet)
		{
			this.groupListCache.add(g);
		}
		
		for(GroupList g:groups.keySet())
		{
			this.groupListCache.add(g);
		}
		
		MdrillPorcessUtils.cleanFieldValueCache(groupbySize);

		for(int i=0;i<this.joinList.length;i++)
		{
			this.joinInvert[i].close();
		}
		long t5=System.currentTimeMillis();
		
		LOG.info("##FacetCross## time taken "+",total:"+(t5-t1)+",init:"+(t2-t1)+",makeGroups:"+(t3-t2)+",transGroupValue:"+(t4-t3)+",groups.size:"+groups.size());
	}
	
	public static interface Iprecontains{
		public boolean contains(GroupList g);
	}
	public static class PreContains implements Iprecontains{
		HashSet<GroupList> preSet;

		public PreContains(HashSet<GroupList> preSet) {
			this.preSet = preSet;
		}
		
		public boolean contains(GroupList g)
		{
			return this.preSet.contains(g);
		}
	}
	public static class EmptyPrecontains implements Iprecontains{
		
		public boolean contains(GroupList g)
		{
			return true;
		}
	}
	
	private GroupList makePreGroup(String g,UnvertFields ufs,HigoJoinInvert[] joinInvert)
	{
		String[] values = EncodeUtils.decode(g.split(UniqConfig.GroupJoinString()));
		GroupList group=GroupList.INSTANCE(this.groupListCache, values.length);
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
	

	private TermNumToString[] prefetch(QuickHashMap<GroupList,RefRow> groups,UnvertFields ufs) throws IOException
	{
		TermNumToString[] tm=new TermNumToString[ufs.length];
		for(int i=0;i<ufs.length;i++)
		 {
			tm[i]=new TermNumToString(ufs,i);
		 }
		
		 for(Entry<GroupList,RefRow> e:groups.entrySet())
		 {
			 int[] group=e.getKey().list;
			 for(int i=0;i<ufs.length;i++)
			 {
				 if(ufs.cols[i]!=null)
				 {
					 tm[i].addTermNum(group[i]);
				 }
			 }
			 
			 
			 int joinoffset=ufs.length;
			for(HigoJoinInvert inv:joinInvert)
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
		 for(int i=0;i<ufs.length;i++)
		 {
			tm[i].fetchValues();
		 }
		 
		 for(HigoJoinInvert inv:joinInvert)
		 {
			 inv.fetchValues();
		 }
		 
		 return tm;
	}
	
	public void transGroupValue(QuickHashMap<GroupList,RefRow> groups,UnvertFields ufs,PriorityQueue<GroupbyRow> res) throws ParseException, IOException
	{
		
		TermNumToString[] tm= this.prefetch(groups, ufs);
		 
		 for(Entry<GroupList,RefRow> e:groups.entrySet())
		 {
			 int[] group=e.getKey().list;
			 StringBuffer buff=new StringBuffer();
			 String j="";
			 for(int i=0;i<ufs.length;i++)
			 {
				 Integer termNum=group[i];
				 buff.append(j);
				 if(ufs.cols[i]!=null)
				 {
					 buff.append(EncodeUtils.encode(tm[i].getTermValue(termNum)));
				 }else{
					 buff.append("-"); 
				 }
				 j=UniqConfig.GroupJoinString();
			 }
			 
			 int joinoffset=ufs.length;
			for(HigoJoinInvert inv:joinInvert)
			{
				int fc=inv.fieldCount();
				for(int i=0;i<fc;i++)
				{
					 buff.append(j);
					 buff.append(EncodeUtils.encode(inv.getTermNumValue(group[joinoffset+i], i)));
					 j=UniqConfig.GroupJoinString();
				}
				joinoffset+=inv.fieldCount();
			}
			 
			 String groupname=buff.toString();
			 this.setCrossRow(e.getValue(), groupname, res);
		 }
	}
	
	private void delayPut(QuickHashMap<GroupList,RefRow> groups,RefRow cnt,GroupList group)
	{
		if(cnt.delayPut)
		{
			if(this.cmpTermNum.compare(smallestShardGroup,new ShardGroupByTermNum(group, cnt))>0){
				cnt.delayPut=false;
				groups.put(group.copy(this.groupListCache), cnt);

			}
		}
	}

	/**
	 * 
	 * 对count进行了特殊优化，代码分支比较多，需要在不降低效率的前提下重构
	 */
	public QuickHashMap<GroupList,RefRow> makeGroups(int limit ,UnvertFields crossufs,UnvertFields distufs,UnvertFields ufs,String[] fields,DocSet baseDocs,Iprecontains pre) throws IOException
	{
		HigoJoinUtils.MakeGroups make=null;
		if(joinInvert.length>=0)
		{
			make=new HigoJoinUtils.MakeGroupsJoin(this.groupListCache);;
		}else{
			make=new HigoJoinUtils.MakeGroupsDefault();
		}
		int groupbySize=ufs.length;
		for(HigoJoinInvert inv:joinInvert)
		{
			groupbySize+=inv.fieldCount();
		}
		
		int groupNonEmptySize=ufs.listIndex.length;
		for(HigoJoinInvert inv:joinInvert)
		{
			groupNonEmptySize+=inv.fieldCount();
		}
		
		GroupList group = GroupList.INSTANCE(this.groupListCache, groupbySize);
		QuickHashMap<GroupList,RefRow> groups=new QuickHashMap<GroupList,RefRow>(this.limit_offset_maxgroups+1);
		if(groupNonEmptySize==0)
		{
			group.reset();
			RefRow cnt = this.getGroup(groups, group, crossufs,distufs);
			
			if(crossufs.listIndex.length==0&&(this.distFS==null||distufs.listIndex.length==0))
			{
				cnt.val+=baseDocs.size();
			}else{
				if((this.distFS==null||distufs.listIndex.length==0)){
					DocIterator iter = baseDocs.iterator();
					while (iter.hasNext()) {
						int doc = iter.nextDoc();
						cnt.val++;
						for (int i:crossufs.listIndex) {
							UnvertFile uf=crossufs.cols[i];
							double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
							cnt.stat[i].update(value);
						}
					}
				}else if(crossufs.listIndex.length==0){
					DocIterator iter = baseDocs.iterator();
					while (iter.hasNext()) {
						int doc = iter.nextDoc();
						cnt.val++;
						for (int i:distufs.listIndex) {
							UnvertFile uf=distufs.cols[i];

							double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
							cnt.dist[i].set(String.valueOf(value));
						}
					}
				}else{
					DocIterator iter = baseDocs.iterator();
					while (iter.hasNext()) {
						int doc = iter.nextDoc();
						cnt.val++;
						for (int i:crossufs.listIndex) {
							UnvertFile uf=crossufs.cols[i];
							double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
							cnt.stat[i].update(value);
						}
						for (int i:distufs.listIndex) {
							UnvertFile uf=distufs.cols[i];
							double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
							cnt.dist[i].set(String.valueOf(value));
						}
					}
				}
			}
		}else if(crossufs.listIndex.length==0&&(this.distFS==null||distufs.listIndex.length==0))
		{
			DocIterator iter = baseDocs.iterator();
			while (iter.hasNext()) {
				int doc = iter.nextDoc();
				ArrayList<GroupList> groupsall=make.toGroupsByJoin(doc, group, ufs, joinInvert);
				for(GroupList g:groupsall)
				{
					if(pre.contains(g))
					{
						RefRow cnt = this.getGroup(groups, g, crossufs,distufs);
						cnt.val++;
						this.delayPut(groups, cnt, g);
					}
				}
			}
			
		}else if(this.distFS==null||distufs.listIndex.length==0)
		{
			DocIterator iter = baseDocs.iterator();
			while (iter.hasNext()) {
				int doc = iter.nextDoc();
				ArrayList<GroupList> groupsall=make.toGroupsByJoin(doc, group, ufs, joinInvert);
				for(GroupList g:groupsall)
				{
					if(pre.contains(g))
					{
						RefRow cnt = this.getGroup(groups, g, crossufs,distufs);
						cnt.val++;
						for (int i:crossufs.listIndex) {
							UnvertFile uf=crossufs.cols[i];
							double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
							cnt.stat[i].update(value);
						}
						this.delayPut(groups, cnt, g);
					}
				}
			}
			
		}else if(crossufs.listIndex.length==0){
			DocIterator iter = baseDocs.iterator();
			while (iter.hasNext()) {
				int doc = iter.nextDoc();
				ArrayList<GroupList> groupsall=make.toGroupsByJoin(doc, group, ufs, joinInvert);
				for(GroupList g:groupsall)
				{
					if(pre.contains(g))
					{
						RefRow cnt = this.getGroup(groups, g, crossufs,distufs);
						cnt.val++;
							for (int i:distufs.listIndex) {
								UnvertFile uf=distufs.cols[i];
								double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
								DistinctCount dist = cnt.dist[i];
								dist.set(String.valueOf(value));
							}
							
							this.delayPut(groups, cnt, g);
					}
				}
			}
		}else{
			DocIterator iter = baseDocs.iterator();
			while (iter.hasNext()) {
				int doc = iter.nextDoc();
				ArrayList<GroupList> groupsall=make.toGroupsByJoin(doc, group, ufs, joinInvert);
				for(GroupList g:groupsall)
				{
					if(pre.contains(g))
					{
						RefRow cnt = this.getGroup(groups, g, crossufs,distufs);
						cnt.val++;
						for (int i:crossufs.listIndex) {
							UnvertFile uf=crossufs.cols[i];
							double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
							 cnt.stat[i].update(value);
						}
						for (int i:distufs.listIndex) {
							UnvertFile uf=distufs.cols[i];
							double value = uf.uif.quickToDouble(doc,uf.filetype,uf.ti);
							DistinctCount dist = cnt.dist[i];
							dist.set(String.valueOf(value));
						}
						this.delayPut(groups, cnt, g);
					}
				}
			}
		}
		
		TopMaps(crossufs, distufs,groups);
		return groups;
	}

	private RefRow getGroup(QuickHashMap<GroupList,RefRow> groups,GroupList group,UnvertFields crossufs,UnvertFields distufs)
	{
		RefRow cnt=groups.get(group);
        if(cnt==null)
        {
        	if (groups.size() >= this.limit_offset_maxgroups) {
        		mergercount++;
        		if(mergercount>=UniqConfig.shardMergerCount())
        		{
        			return getEmptyRow();
        		}
        		this.recordCount.setCrcRecord("-");
				this.recordCount.setIsoversize(true);
				 TopMaps(crossufs, distufs,groups);
			}
      	  
      	  cnt=new RefRow();
      	  if(this.crossFs!=null)
      	  {
      		  cnt.stat=new RefRowStat[this.crossFs.length];
      		  for(int i=0;i<this.crossFs.length;i++)
      		  {
      			  cnt.stat[i]=new RefRowStat();
      		  }
      	  }
      	  
      	 if(this.distFS!=null)
     	  {
     		  cnt.dist=new DistinctCount[this.distFS.length];
     		  for(int i=0;i<this.distFS.length;i++)
     		  {
     			  cnt.dist[i]=new DistinctCount();
     		  }
     	  }
      	 if(smallestShardGroup==null)
      	 {
      		 groups.put(group.copy(this.groupListCache), cnt);
      	 }else{
      		cnt.delayPut=true;
      	 }
        }
        return cnt;
	}
	
	RefRow emptyrow=null;
	public RefRow getEmptyRow()
	{
		if(emptyrow==null)
		{
			emptyrow=new RefRow();
	      	  if(this.crossFs!=null)
	      	  {
	      		emptyrow.stat=new RefRowStat[this.crossFs.length];
	      		  for(int i=0;i<this.crossFs.length;i++)
	      		  {
	      			emptyrow.stat[i]=new RefRowStat();
	      		  }
	      	  }
	      	  
	      	 if(this.distFS!=null)
	     	  {
	      		emptyrow.dist=new DistinctCount[this.distFS.length];
	     		  for(int i=0;i<this.distFS.length;i++)
	     		  {
	     			 emptyrow.dist[i]=new DistinctCount();
	     		  }
	     	  }
		}
		
		return emptyrow;
	}
		

}
