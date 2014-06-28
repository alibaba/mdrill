package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.CRC32;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.DocSetCollector;
import org.apache.solr.search.SolrIndexSearcher;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.ColumnKey;
import org.apache.solr.request.compare.RecordCountDetail;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.join.HigoJoinInvert;
import org.apache.solr.request.mdrill.MdrillUtils.*;

import com.alimama.mdrill.utils.EncodeUtils;
import com.alimama.mdrill.utils.UniqConfig;

/**
	查询明细的实现
 * @author yannian.mu
 */
public class MdrillDetail {
//	public static Integer MAX_CROSS_ROWS=UniqConfig.defaultCrossMaxLimit();
	private SolrIndexSearcher searcher;
	private RecordCountDetail recordCount ;
	private SolrQueryRequest req;
	private MdrillParseDetail parse;
	private SegmentReader reader;
	private MdrillParseDetail.fetchContaioner container=null;
	
	String crcget=null;
	String segKey=null;
	private String hostId=java.util.UUID.randomUUID().toString();
	SolrParams params;

	public MdrillDetail(SolrIndexSearcher _searcher,SegmentReader reader,SolrParams _params,SolrQueryRequest req)
	{
		try {
			this.hostId=java.net.InetAddress.getLocalHost().getHostAddress()+","+String.valueOf(java.net.InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			hostId=java.util.UUID.randomUUID().toString();
		}
		
		this.segKey=reader.getSigmentUniqKey();
		this.crcget=_params.get("mdrill.crc.key.get",null);
		
		this.reader=reader;
		this.req=req;
		this.searcher=_searcher;
		this.parse=new MdrillParseDetail(_params);
		this.recordCount = new RecordCountDetail();
		this.params=_params;

	}
	
	public static class FieldValueGet{
		String[] fields;
		SegmentReader reader;
		SolrIndexSearcher searcher;
		boolean[] isStore;
		FieldType[] ftlist;
		MapFieldSelector selector;
		String[] fieldsnostore;
		UnvertFields ufs;
		public FieldValueGet(DocSet baseAdvanceDocs,String[] fields,SegmentReader reader,SolrIndexSearcher searcher) throws IOException
		{
			this.fields=fields;
			this.reader=reader;
			this.searcher=searcher;
			this.isStore=new boolean[this.fields.length];
			this.fieldsnostore=new String[this.fields.length];
			
			this.ftlist=new FieldType[fields.length];
			IndexSchema schema=this.searcher.getSchema();

			ArrayList<String> storedField=new ArrayList<String>();
			
			for (int j = 0; j < fields.length; j++) {
				ftlist[j]=schema.getFieldType(fields[j]);
				this.isStore[j]=schema.getField(fields[j]).stored();
				this.fieldsnostore[j]=fields[j];
				if(this.isStore[j])
				{
					storedField.add(fields[j]);
					this.fieldsnostore[j]="higoempty_"+j+"_s";
				}
				
			}
			this.selector=new MapFieldSelector(storedField); 
			this.ufs=new UnvertFields(baseAdvanceDocs,this.fieldsnostore, reader,searcher.getPartionKey(),searcher.getSchema(),false);;
			
		}
		
		public void free()
		{
			this.ufs.free();
		}
		public String[] doc(int docid) throws CorruptIndexException, IOException
		{
			Document docfields=this.reader.document(docid,selector);

			String[] rtn=new String[this.fields.length];
			for (int j = 0; j < fields.length; j++) {
				rtn[j]="-";
				String field=fields[j];
				if(this.isStore[j])
				{
					Fieldable fv=docfields.getFieldable(field);
					if(fv!=null)
					{
						rtn[j]=ftlist[j].toExternal(fv);
					}
				}else{
					UnvertFile uf=ufs.cols[j];
					if(uf!=null)
					{	
						Integer termNum = uf.uif.termNum(docid);
						 String fieldvalue=uf.uif.tNumToString(termNum, uf.filetype, uf.ti,"null");
						 if(fieldvalue!=null)
						 {
							 rtn[j]=fieldvalue;
						 }
					}
				}
			}
			
			return rtn;
		}
	
	}
	

	public NamedList get(String[] fields, DocSet baseDocs) throws IOException,
			ParseException {
		if(this.crcget==null)
		{
		
			this.container=this.parse.createContainer(fields, baseDocs, this.reader, this.searcher, this.req);
			DocIterator iter = baseDocs.iterator();
			int doc=-1;
			if(this.container.isUseJoinSort())
			{
				int cmpValue=0;
				while (iter.hasNext()) {
					doc = iter.nextDoc();
					if(!this.container.containsInJoins(doc))
					{
						continue;
					}
					cmpValue=this.container.getJoinCompareValue(doc);
					SelectDetailRow row = SelectDetailRow.INSTANCE(doc,cmpValue);
					QueuePutUtils.put2QueueDetail(row, this.container.res, this.parse.limit_offset, this.container.cmpTermNum);
					this.recordCount.inc(1);
				}
			}else if(this.container.isColumnSort())
			{
				int cmpValue=0;
				
					while (iter.hasNext()) {
						doc = iter.nextDoc();
						if(!this.container.containsInJoins(doc))
						{
							continue;
						}
						cmpValue =this.container.getColumnCompareValue(doc);
						SelectDetailRow row = SelectDetailRow.INSTANCE(doc,cmpValue);
						QueuePutUtils.put2QueueDetail(row, this.container.res, this.parse.limit_offset, this.container.cmpTermNum);
						this.recordCount.inc(1);
					}
			}else{
				while (iter.hasNext()) {
					doc = iter.nextDoc();
					if(!this.container.containsInJoins(doc))
					{
						continue;
					}
					SelectDetailRow row = SelectDetailRow.INSTANCE(doc,doc);
					QueuePutUtils.put2QueueDetail(row, this.container.res, this.parse.limit_offset, this.container.cmpTermNum);
					this.recordCount.inc(1);
					if(this.container.res.size()>=this.parse.limit_offset)
					{
						break;
					}
				}
				while (iter.hasNext()) {
					doc = iter.nextDoc();
					if(!this.container.containsInJoins(doc))
					{
						continue;
					}
					this.recordCount.inc(1);
				}
			}
			
			PriorityQueue<SelectDetailRow> topItems=this.transGroupValue(this.container.res,this.container.sortufs);
					
			 this.container.free();
			
			return this.toNameList(topItems);	
		
		}
		
		
		String hostkey=String.valueOf(this.getkeyCrc());

		ConcurrentHashMap<Long,String> cache=MdrillUtils.CRC_CACHE_SIZE.remove(crcget+"@"+hostkey);
		NamedList rtn=new NamedList();
		Map<Long,String> crcvalue=new HashMap<Long,String>();
		rtn.add("fdtcre", crcvalue);
		if(cache!=null)
		{

			String crcliststr=params.get("mdrill.crc.key.get.crclist");
			if(crcliststr!=null)
			{
				

				DocSetCollector collect=new DocSetCollector(10240, reader.maxDoc());

				String[] crclist=crcliststr.split(",");
				for(String s:crclist)
				{
					Long crc=Long.parseLong(s);
					String v=cache.get(crc);
					if(v!=null)
					{
						String cols[]=v.split(UniqConfig.GroupJoinString(),-1);
						if(cols.length>=2)
						{
							int doc=Integer.parseInt(cols[0]);
							collect.collect(doc);
						}
					}
				}
				
				
				FieldValueGet fvget=new FieldValueGet(collect.getDocSet(),fields, reader, searcher);

				for(String s:crclist)
				{
					Long crc=Long.parseLong(s);
					String v=cache.get(crc);
					if(v!=null)
					{
						String cols[]=v.split(UniqConfig.GroupJoinString(),-1);
						if(cols.length>=2)
						{
							int doc=Integer.parseInt(cols[0]);
							
							String[] fieldValue=fvget.doc(doc);
							SortGroupVal buff = new SortGroupVal();
							buff.groupbuff.append("-");
							buff.groupbuff.append(UniqConfig.GroupJoinString());
							buff.groupbuff.append("-");
							for (int j = 0; j < fields.length; j++) {
								buff.groupbuff.append(UniqConfig.GroupJoinString());
								buff.groupbuff.append(EncodeUtils.encode(fieldValue[j]));
							}
							
							for(int j=(fields.length+2);j<cols.length;j++)
							{
								buff.groupbuff.append(UniqConfig.GroupJoinString());
								buff.groupbuff.append(cols[j]);
							}
							
							crcvalue.put(crc, buff.groupbuff.toString());

						}
					}
				}
				fvget.free();

			}
			
		}
		return rtn;
	}

	
	
	private TermNumToString prefetchValues(PriorityQueue<SelectDetailRow> res,UnvertFields sortufs) throws IOException
	{
		TermNumToString forsort=new TermNumToString(sortufs,0);
		boolean usedsort=this.container.isOnlyColumnSort();

		for (SelectDetailRow row : res) {
			int doc = row.docid;
				
			if(usedsort)
			{
				int ternnum=(int)row.value;
				forsort.addTermNum(ternnum);
			}
			
			
			for(HigoJoinInvert inv:this.container.joinInvert)
			{
				int fc=inv.fieldCount();
				LinkedBlockingQueue<GroupListCache.GroupList> groupListCache=inv.getGroupListCache();
				GroupListCache.GroupList base=GroupListCache.GroupList.INSTANCE(groupListCache, fc);
				base.reset();
				if(inv.fieldNum(doc, base))
				{
					for (int i = 0; i < fc; i++) {
						inv.addTermNum(base.list[i], i);
					}
				}

				groupListCache.add(base);
			}
		}
		
		
		forsort.fetchValues();
		 
		 for(HigoJoinInvert inv:this.container.joinInvert)
		 {
			 inv.fetchValues();
		 }
		 
		 return forsort;
	}

	public PriorityQueue<SelectDetailRow> transGroupValue(PriorityQueue<SelectDetailRow> res,UnvertFields sortufs)throws ParseException, IOException {
		TermNumToString tm=this.prefetchValues( res, sortufs);
		
		PriorityQueue<SelectDetailRow> topItems = new PriorityQueue<SelectDetailRow>(this.parse.limit_offset, Collections.reverseOrder(this.container.cmpresult));

		boolean usedsort=this.container.isOnlyColumnSort();

		long key=getkeyCrc();

		for (SelectDetailRow row : res) {
			int doc = row.docid;
			String sortString=null;
			if(usedsort)
			{
				sortString=tm.getTermValueWithNull((int)row.value);
			}
			SortGroupVal buff = new SortGroupVal();
			buff.groupbuff.append(String.valueOf(doc));
			buff.groupbuff.append(UniqConfig.GroupJoinString());
			buff.groupbuff.append(String.valueOf(key));
			for (int i = 0; i < this.container.fields.length; i++) {
				buff.groupbuff.append(UniqConfig.GroupJoinString());
				buff.groupbuff.append(EncodeUtils.encode("-"));
			}
			buff.sortString=sortString;
			this.setGroupJoin(buff,this.container.fields.length,doc);
			SelectDetailRow newrow = SelectDetailRow.INSTANCE(doc, row.getCompareValue());
			newrow.setKey(new ColumnKey(buff.groupbuff.toString()));
			newrow.colVal=buff.sortString;
			QueuePutUtils.put2QueueDetail(newrow, topItems, this.parse.limit_offset, this.container.cmpresult);
		}

		return topItems;
	}
	
	
	public void setGroupJoin(SortGroupVal buff,int offset,int doc) throws IOException
	{
		for(HigoJoinInvert inv:this.container.joinInvert)
		{
			int fc=inv.fieldCount();
			LinkedBlockingQueue<GroupListCache.GroupList> groupListCache=inv.getGroupListCache();
			GroupListCache.GroupList base=GroupListCache.GroupList.INSTANCE(groupListCache, fc);
			base.reset();
			if(inv.fieldNum(doc, base))
			{

				for (int i = 0; i < fc; i++) {
					buff.groupbuff.append(UniqConfig.GroupJoinString());
					buff.groupbuff.append(EncodeUtils.encode(inv.getTermNumValue(base.list[i], i)));
					if(this.container.SelectDetailSort!=null&&(offset+i)==this.container.SelectDetailSort.offset)
					{
						buff.sortString=inv.getTermNumValue(base.list[this.container.SelectDetailSort.selfOffset], i);
					}
				}
			}
			groupListCache.add(base);
			offset+=fc;
		}
	}
	
	

	private NamedList toNameList(PriorityQueue<SelectDetailRow> topItems) {
		java.util.ArrayList<SelectDetailRow> recommendations = new ArrayList<SelectDetailRow>(topItems.size());
		recommendations.addAll(topItems);
		Collections.sort(recommendations, this.container.cmpresult);
		Integer index = 0;
		NamedList res = new NamedList();
		res.add("count", recordCount.toNamedList());
		
		ConcurrentHashMap<Long,String> cache=null;
		boolean issetCrc=this.parse.crcOutputSet!=null;
		long crckey=this.getkeyCrc();
		

		if(issetCrc)
		{	
			synchronized (MdrillUtils.CRC_CACHE_SIZE) {
				cache=MdrillUtils.CRC_CACHE_SIZE.get(this.parse.crcOutputSet+"@"+crckey);
				if(cache==null)
				{
					cache=new ConcurrentHashMap<Long,String>();
					MdrillUtils.CRC_CACHE_SIZE.put(this.parse.crcOutputSet+"@"+crckey, cache);
				}
			}
			
			
		}
		
		ArrayList<Object> list=new ArrayList<Object>();
		for (SelectDetailRow kv : recommendations) {
			if (index >= this.parse.offset) {
				if(issetCrc)
				{
					kv.ToCrcSet(cache);
				}
				list.add(kv.toNamedList());
			}
			index++;
		}
		res.add("list", list);
		return res;
	}
	
	
	
	public static class SortGroupVal{
		public StringBuffer groupbuff=new StringBuffer();
		public String sortString;
	}
	
	private long getkeyCrc()
	{
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(hostId+"@"+this.segKey).getBytes());
		return crc32.getValue();
	}
	
}
