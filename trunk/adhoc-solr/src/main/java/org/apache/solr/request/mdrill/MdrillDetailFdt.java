package org.apache.solr.request.mdrill;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.compare.ColumnKey;
import org.apache.solr.request.compare.RecordCountDetail;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowStringCompare;

import com.alimama.mdrill.utils.EncodeUtils;
import com.alimama.mdrill.utils.UniqConfig;

/**
	查询明细的实现-无排序，无join
 * @author yannian.mu
 */
public class MdrillDetailFdt {
	private SolrIndexSearcher searcher;
	private RecordCountDetail recordCount ;
	private SolrQueryRequest req;
	private MdrillParseDetailFdt parse;
	private SegmentReader reader;
	private MdrillParseDetailFdt.fetchContaioner container=null;
	String crcget=null;
	String segKey=null;
	private String hostId=java.util.UUID.randomUUID().toString();
	SolrParams params;
	public MdrillDetailFdt(SolrIndexSearcher _searcher,SegmentReader reader,SolrParams _params,SolrQueryRequest req)
	{
		try {
			this.hostId=java.net.InetAddress.getLocalHost().getHostAddress()+","+String.valueOf(java.net.InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			hostId=java.util.UUID.randomUUID().toString();
		}
		this.segKey=reader.getSigmentUniqKey();
		this.reader=reader;
		this.req=req;
		this.searcher=_searcher;
		this.parse=new MdrillParseDetailFdt(_params);
		this.crcget=_params.get("mdrill.crc.key.get",null);
		this.recordCount = new RecordCountDetail();
		this.params=_params;
	}
	
	public static class Doclist{
		public int[] list;
		public Doclist(int size) {
			super();
			this.list = new int[size];
		}
		
		public void add(int doc)
		{
			list[index]=doc;
			this.index++;
		}
		public int index=0;
	}

	public NamedList get(String[] fields, DocSet baseDocs) throws IOException,
			ParseException {
			if(this.crcget==null)
			{
				this.container=this.parse.createContainer(fields, baseDocs, this.reader, this.searcher, this.req);
				DocIterator iter = baseDocs.iterator();
				this.recordCount.inc(baseDocs.size());
				Doclist res=new Doclist(this.parse.limit_offset);
				int doc=-1;
				while (iter.hasNext()) {
					doc = iter.nextDoc();
					res.add(doc);
					if(res.index>=this.parse.limit_offset)
					{
						break;
					}
				}
				PriorityQueue<SelectDetailRow> topItems=this.transGroupValue(res,fields);
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
				MapFieldSelector selector=new MapFieldSelector(fields); 
				FieldType[] ftlist=new FieldType[fields.length];
				IndexSchema schema=this.searcher.getSchema();

				for (int j = 0; j < fields.length; j++) {
					ftlist[j]=schema.getFieldType(fields[j]);
					
				}

				String crcliststr=params.get("mdrill.crc.key.get.crclist");
				if(crcliststr!=null)
				{
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
								
								SortGroupVal buff = new SortGroupVal();
								buff.groupbuff.append("-");
								buff.groupbuff.append(UniqConfig.GroupJoinString());
								buff.groupbuff.append("-");
								Document docfields=this.reader.document(doc,selector);
								if(docfields==null)
								{
									for (int j = 0; j < fields.length; j++) {
										buff.groupbuff.append(UniqConfig.GroupJoinString());
										buff.groupbuff.append(EncodeUtils.encode("-"));
									}
									if(!crcvalue.containsKey(crc))
									{
										crcvalue.put(crc, buff.groupbuff.toString());
									}
								}else{

									for (int j = 0; j < fields.length; j++) {
										buff.groupbuff.append(UniqConfig.GroupJoinString());
	
										Fieldable fv=docfields.getFieldable(fields[j]);
										
										
										if(fv!=null)
										{
											buff.groupbuff.append(EncodeUtils.encode(ftlist[j].toExternal(fv)));
										}
										else{
											buff.groupbuff.append(EncodeUtils.encode("-"));
										}
									}
									crcvalue.put(crc, buff.groupbuff.toString());

								}
							}
						}
					}
				}
			}
			return rtn;
	}
	

	private long getkeyCrc()
	{
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(hostId+"@"+this.segKey).getBytes());
		return crc32.getValue();
	}
	


public PriorityQueue<SelectDetailRow> transGroupValue(Doclist res,String[] fields)throws ParseException, IOException {
		
		long key=getkeyCrc();

		PriorityQueue<SelectDetailRow> topItems = new PriorityQueue<SelectDetailRow>(this.parse.limit_offset, Collections.reverseOrder(defcmp));
		for(int i=0;i<res.index;i++)
		{
			int doc = res.list[i];
			SortGroupVal buff = new SortGroupVal();
			buff.groupbuff.append(String.valueOf(doc));
			buff.groupbuff.append(UniqConfig.GroupJoinString());
			buff.groupbuff.append(String.valueOf(key));

			for (int j = 0; j < fields.length; j++) {
				buff.groupbuff.append(UniqConfig.GroupJoinString());
				buff.groupbuff.append(EncodeUtils.encode("-"));
			}
			buff.sortString="0";
			SelectDetailRow newrow = SelectDetailRow.INSTANCE(doc, doc);
			newrow.setKey(new ColumnKey(buff.groupbuff.toString()));
			newrow.colVal=buff.sortString;
			QueuePutUtils.put2QueueDetail(newrow, topItems, this.parse.limit_offset,defcmp);
		}
		return topItems;
	}


	
	private ShardDetailSelectDetailRowStringCompare defcmp=new ShardDetailSelectDetailRowStringCompare("string", true);
	
	private NamedList toNameList(PriorityQueue<SelectDetailRow> topItems) {
		java.util.ArrayList<SelectDetailRow> recommendations = new ArrayList<SelectDetailRow>(topItems.size());
		recommendations.addAll(topItems);
		Collections.sort(recommendations,defcmp);
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
	
}
