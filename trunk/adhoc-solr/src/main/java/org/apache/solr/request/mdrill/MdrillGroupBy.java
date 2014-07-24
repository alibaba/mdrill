package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import com.alimama.mdrill.utils.EncodeUtils;
import com.alimama.mdrill.utils.UniqConfig;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.ColumnKey;
import org.apache.solr.request.compare.GroupbyRow;
import org.apache.solr.request.compare.MergerGroupByGroupbyRowCompare;
import org.apache.solr.request.compare.RecordCount;
import org.apache.solr.request.compare.ShardGroupByTermNum;
import org.apache.solr.request.join.HigoJoinInvert;
import org.apache.solr.request.mdrill.MdrillUtils.*;

/**
 * 多列group by 分类 汇总的实现
 * @author yannian.mu
 */
public class MdrillGroupBy {
    private static Logger LOG = Logger.getLogger(MdrillGroupBy.class);

	public static Integer MAX_CROSS_ROWS=UniqConfig.defaultCrossMaxLimit();
	private SolrIndexSearcher searcher;
	private SolrQueryRequest req;
	public int mergercount=0;

	private RecordCount recordCount ;
	

	private SegmentReader reader;
	private MdrillParseGroupby parse;
	
	private ShardGroupByTermNum smallestShardGroup=null;
	private MdrillParseGroupby.fetchContaioner container=null;

	public MdrillGroupBy(SolrIndexSearcher _searcher,SegmentReader reader,SolrParams _params,SolrQueryRequest req)
	{
		this.reader=reader;
		this.searcher=_searcher;
		this.req=req;
		this.parse=new MdrillParseGroupby(_params);
		this.recordCount = new RecordCount();
		this.recordCount.setFinalResult(false);
		this.recordCount.setMaxUniqSize(this.parse.maxlimit);
	}
		
	public NamedList get(String[] fields, DocSet baseDocs) throws IOException,
			ParseException {
		long t1=System.currentTimeMillis();
		this.container=this.parse.createContainer(fields, baseDocs, this.reader, this.searcher, this.req);
		long t2=System.currentTimeMillis();
		QuickHashMap<GroupListCache.GroupList,RefRow> groups=this.makeTopGroups(fields);
		long t3=System.currentTimeMillis();
		this.transGroupValue(groups);
		long t4=System.currentTimeMillis();
		
		NamedList rtn= this.toNameList();	
		container.free(groups);
		long t5=System.currentTimeMillis();
		LOG.info("##FacetCross## time taken "+",total:"+(t5-t1)+",init:"+(t2-t1)+",makeGroups:"+(t3-t2)+",transGroupValue:"+(t4-t3)+",groups.size:"+groups.size());
		return rtn;
	}
	
	
	public QuickHashMap<GroupListCache.GroupList,RefRow> makeTopGroups(String[] fields) throws IOException
	{
		GroupListCache.GroupList group = GroupListCache.GroupList.INSTANCE(container.groupListCache, container.groupbySize);
		QuickHashMap<GroupListCache.GroupList,RefRow> groups=new QuickHashMap<GroupListCache.GroupList,RefRow>(this.parse.limit_offset_maxgroups+1);
		
		
		boolean issetDist=this.parse.isMustSetDistResult();
		if(container.groupNonEmptySize==0)
		{
			group.reset();
			RefRow cnt = this.makeOrGetGroup(groups, group);
			
			if(container.countOnly())
			{
				cnt.val+=container.baseDocs.size();
			}else{
				DocIterator iter = container.baseDocs.iterator();
				if(container.noDist()){
					while (iter.hasNext()) {
						int doc = iter.nextDoc();
						cnt.val++;
						container.updateStat(cnt, doc);
					}
				}else if(container.noStat()){
					while (iter.hasNext()) {
						int doc = iter.nextDoc();
						cnt.val++;
						if(issetDist)
						{
							container.updateDist(cnt, doc);
						}
					}
				}else{
					while (iter.hasNext()) {
						int doc = iter.nextDoc();
						cnt.val++;
						container.updateStat(cnt, doc);
						if(issetDist)
						{
						container.updateDist(cnt, doc);
						}
					}
				}
			}
		}else{
			DocIterator iter = container.baseDocs.iterator();

			if (container.countOnly())
			{
				while (iter.hasNext()) {
					int doc = iter.nextDoc();
					if (container.toGroupsByJoin(doc, group)&&container.pre.contains(group)) {
						RefRow cnt = this.makeOrGetGroup(groups, group);
						cnt.val++;
						this.delayPut(groups, cnt,group);
					}
				
				}

			} else if (container.noDist()) {
				while (iter.hasNext()) {
					int doc = iter.nextDoc();

					if (container.toGroupsByJoin(doc, group)&&container.pre.contains(group)) {
						RefRow cnt = this.makeOrGetGroup(groups, group);
						cnt.val++;
						container.updateStat(cnt, doc);
						this.delayPut(groups, cnt, group);
					}
				
				}

			} else if (container.noStat()) {
				while (iter.hasNext()) {
					int doc = iter.nextDoc();

					if (container.toGroupsByJoin(doc, group)&&container.pre.contains(group)) {
						RefRow cnt = this.makeOrGetGroup(groups, group);
						cnt.val++;
						if(issetDist)
						{
							container.updateDist(cnt, doc);
						}
						this.delayPut(groups, cnt, group);
					}
				
				}
			} else {
				while (iter.hasNext()) {
					int doc = iter.nextDoc();

					if (container.toGroupsByJoin(doc, group)&&container.pre.contains(group)) {
						RefRow cnt = this.makeOrGetGroup(groups, group);
						cnt.val++;
						container.updateStat(cnt, doc);
						if(issetDist)
						{
							container.updateDist(cnt, doc);
						}
						this.delayPut(groups, cnt, group);
					}
				
				}
			}
		}
		
		TopMaps(groups);
		return groups;
	}
	
		
	private void TopMaps(QuickHashMap<GroupListCache.GroupList,RefRow> groups)
	{
		long t1=System.currentTimeMillis();
		int groupsize=groups.size();
		if(groupsize<=this.parse.limit_offset)
		{
			return ;
		}
		PriorityQueue<ShardGroupByTermNum> res = new PriorityQueue<ShardGroupByTermNum>(this.parse.limit_offset, Collections.reverseOrder(this.container.cmpTermNum));
		LinkedBlockingQueue<GroupListCache.GroupList> toremove=new LinkedBlockingQueue<GroupListCache.GroupList>();;
		QuickHashMap<GroupListCache.GroupList,RefRow> debug=new QuickHashMap<GroupListCache.GroupList, MdrillUtils.RefRow>(this.parse.limit_offset);

		for(Entry<GroupListCache.GroupList,RefRow> e:groups.entrySet())
		{
			debug.put(e.getKey(), e.getValue());
			ShardGroupByTermNum mrow=new ShardGroupByTermNum(e.getKey(), e.getValue());
			if (res.size() < this.parse.limit_offset) {
				res.add(mrow);
			} else if (this.container.cmpTermNum.compare(res.peek(), mrow) > 0) {
				res.add(mrow);
				ShardGroupByTermNum free=res.poll();
				toremove.add(free.key);
			}else{
				toremove.add(mrow.key);
			}
		}
		int cnt1=0;

		for(GroupListCache.GroupList torm:toremove)
		{
			groups.remove(torm);
			this.container.freeRow(torm);
			this.container.groupListCache.add(torm);
			cnt1++;
		}
		
		smallestShardGroup=res.peek();
		
		long t2=System.currentTimeMillis();
		LOG.info("TopMaps groups.size="+groupsize+"@"+debug.size() +" to "+groups.size()+"@"+this.parse.limit_offset+",res.size="+res.size()+",remove="+cnt1+",timetaken="+(t2-t1)+",mergercount="+this.mergercount);
	}

	private NamedList toNameList() {
		java.util.ArrayList<GroupbyRow> recommendations = new ArrayList<GroupbyRow>(this.container.res.size());
		recommendations.addAll(this.container.res);
		Collections.sort(recommendations, this.container.cmpString);

		Integer index = 0;
		NamedList res = new NamedList();
		res.add("count", recordCount.toNamedList());
		
		ConcurrentHashMap<Long,String> cache=null;

		boolean issetCrc=this.parse.crcOutputSet!=null;
		MergerGroupByGroupbyRowCompare mergerCmp=null;
		if(issetCrc)
		{
			synchronized (MdrillUtils.CRC_CACHE_SIZE) {
				cache=MdrillUtils.CRC_CACHE_SIZE.get(this.parse.crcOutputSet);
				if(cache==null)
				{
					cache=new ConcurrentHashMap<Long,String>();
					MdrillUtils.CRC_CACHE_SIZE.put(this.parse.crcOutputSet, cache);

				}
			}
			
			FacetComponent.FieldFacet facet=new FacetComponent.FieldFacet(this.parse.params, "solrCorssFields_s");
			mergerCmp=facet.createMergerGroupCmp();
		}
		
		ArrayList<Object> list=new ArrayList<Object>();
		
		for (GroupbyRow kv : recommendations) {
			if (index >= this.parse.offset) {
				if(issetCrc)
				{
					kv.ToCrcSet(mergerCmp,cache);
				}
				list.add(kv.toNamedList());
			}
			index++;
		}
		res.add("list", list);
		return res;
	}
		  

	private void setCrossRow(RefRow ref,String groupname) throws ParseException, IOException
	  {
		  this.recordCount.setCrcRecord(groupname);
		  GroupbyRow row = new GroupbyRow(new ColumnKey(groupname), ref.val);
		  row.setCross(this.parse.crossFs, this.parse.distFS);
		  if(this.parse.hasStat())
		{
				for(int i=0;i<this.parse.crossFs.length;i++)
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
			
			if(this.parse.hasDist())
			{
				for(int i=0;i<this.parse.distFS.length;i++)
				{
					row.setDistinct(i, ref.dist[i]);
				}
			}
			
			QueuePutUtils.put2Queue(row, this.container.res, this.parse.limit_offset, this.container.cmpString);
	  }
	  

	
	public static interface Iprecontains{
		public boolean contains(GroupListCache.GroupList g);
	}
	public static class PreContains implements Iprecontains{
		HashSet<GroupListCache.GroupList> preSet;

		public PreContains(HashSet<GroupListCache.GroupList> preSet) {
			this.preSet = preSet;
		}
		
		public boolean contains(GroupListCache.GroupList g)
		{
			return this.preSet.contains(g);
		}
	}
	public static class EmptyPrecontains implements Iprecontains{
		
		public boolean contains(GroupListCache.GroupList g)
		{
			return true;
		}
	}
	
	
	public void transGroupValue(QuickHashMap<GroupListCache.GroupList,RefRow> groups) throws ParseException, IOException
	{
		TermNumToString[] tm= this.container.prefetch(groups);
		 for(Entry<GroupListCache.GroupList,RefRow> e:groups.entrySet())
		 {
			 int[] group=e.getKey().list;
			 StringBuffer buff=new StringBuffer();
			 String j="";
			 for(int i=0;i<container.ufs.length;i++)
			 {
				 Integer termNum=group[i];
				 buff.append(j);
				 if(container.ufs.cols[i]!=null)
				 {
					 buff.append(EncodeUtils.encode(tm[i].getTermValue(termNum)));
				 }else{
					 buff.append("-"); 
				 }
				 j=UniqConfig.GroupJoinString();
			 }
			 
			 int joinoffset=container.ufs.length;
			for(HigoJoinInvert inv: this.container.joinInvert)
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
			 this.setCrossRow(e.getValue(), groupname);
		 }
	}
	
	private void delayPut(QuickHashMap<GroupListCache.GroupList,RefRow> groups,RefRow cnt,GroupListCache.GroupList group)
	{
		if(cnt.delayPut)
		{
			if( this.container.cmpTermNum.compare(smallestShardGroup,new ShardGroupByTermNum(group, cnt))>0){
				cnt.delayPut=false;
				groups.put(group.copy( this.container.groupListCache), cnt);

			}else{
				this.container.freeRow(group);
			}
		}
	}

	private RefRow makeOrGetGroup(QuickHashMap<GroupListCache.GroupList, RefRow> groups,GroupListCache.GroupList group) {
		RefRow cnt = groups.get(group);
		if (cnt == null) {
			if (groups.size() >= this.parse.limit_offset_maxgroups) {
				mergercount++;
				if (mergercount >= this.parse.limit_offset_maxgroups_merger) {
					return this.container.getEmptyRow();
				}
				this.recordCount.setCrcRecord("-");
				this.recordCount.setIsoversize(true);
				TopMaps(groups);
			}

			cnt = this.container.createRow(group);
			if (smallestShardGroup == null) {
				groups.put(group.copy(this.container.groupListCache), cnt);
			} else {
				cnt.delayPut = true;
			}
		}
		return cnt;
	}
	
}
