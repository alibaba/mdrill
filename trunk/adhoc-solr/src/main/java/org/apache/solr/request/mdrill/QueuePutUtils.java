package org.apache.solr.request.mdrill;

import java.util.PriorityQueue;

import org.apache.log4j.Logger;
import org.apache.solr.request.compare.GroupbyRow;
import org.apache.solr.request.compare.SelectDetailRow;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowCompare;
import org.apache.solr.request.compare.ShardDetailSelectDetailRowStringCompare;
import org.apache.solr.request.compare.ShardGroupByGroupbyRowCompare;

public class QueuePutUtils {
    static Logger LOG = Logger.getLogger(QueuePutUtils.class);

	public static void put2Queue(GroupbyRow mrow,PriorityQueue<GroupbyRow> res,int limit_offset,ShardGroupByGroupbyRowCompare cmp)
	{
		if (res.size() < limit_offset) {
			res.add(mrow);
		} else if (cmp.compare(res.peek(), mrow) > 0) {
			res.add(mrow);
			res.poll();
		}
	}

	public static void put2QueueDetail(SelectDetailRow mrow,PriorityQueue<SelectDetailRow> res,int limit_offset,ShardDetailSelectDetailRowCompare cmp)
	{
		if (res.size() < limit_offset) {
			res.add(mrow);
		} else if (cmp.compare(res.peek(), mrow) > 0) {
			res.add(mrow);
			SelectDetailRow.FREE(res.poll());
		}else{
			SelectDetailRow.FREE(mrow);
		}
	}

	public static void put2QueueDetail(SelectDetailRow mrow,PriorityQueue<SelectDetailRow> res,int limit_offset,ShardDetailSelectDetailRowStringCompare cmp)
	{
		if (res.size() < limit_offset) {
			res.add(mrow);
		} else if (cmp.compare(res.peek(), mrow) > 0) {
			res.add(mrow);
			SelectDetailRow.FREE(res.poll());
		}else{
			SelectDetailRow.FREE(mrow);
		}
	}

}
