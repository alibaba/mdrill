package org.apache.solr.request.mdrill;

import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.alimama.mdrill.utils.UniqConfig;

public class GroupListCache {
	private static Logger LOG = Logger.getLogger(GroupListCache.class);

	public static class GroupList {
		public int[] list;

		private GroupList(int size) {
			list = new int[size];
		}

		public void reset() {
			for (int i = 0; i < list.length; i++) {
				list[i] = -1;
			}
		}

		public static GroupList INSTANCE(LinkedBlockingQueue<GroupList> free,int size) {
			GroupList rtn = free.poll();
			if (rtn == null) {
				rtn = new GroupList(size);
			} else {
				if (rtn.list.length != size) {
					rtn = new GroupList(size);
				}
			}
			return rtn;
		}

		public GroupList copy(LinkedBlockingQueue<GroupList> free) {
			GroupList rtn = INSTANCE(free, this.list.length);
			for (int i = 0; i < this.list.length; i++) {
				rtn.list[i] = this.list[i];
			}
			return rtn;
		}

		@Override
		public int hashCode() {
			int result = 1;
			for (int element : this.list) {
				result = 31 * result + element;
			}
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			GroupList other = (GroupList) obj;
			for (int i = 0; i < this.list.length; i++) {
				if (this.list[i] != other.list[i]) {
					return false;
				}
			}
			return true;
		}

	}
	private static WeakHashMap<Integer, LinkedBlockingQueue<GroupListCache.GroupList>> fieldValueCache = new WeakHashMap<Integer, LinkedBlockingQueue<GroupListCache.GroupList>>();
	public static synchronized LinkedBlockingQueue<GroupListCache.GroupList>  getGroupListQueue(int size)
	{
		LinkedBlockingQueue<GroupListCache.GroupList> rtn=fieldValueCache.get(size);
		if(rtn==null)
		{
			rtn=new LinkedBlockingQueue<GroupListCache.GroupList>();
			fieldValueCache.put(size, rtn);
		}
		
		return rtn;
	}
	public static void cleanFieldValueCache(int size)
	{
		LinkedBlockingQueue<GroupListCache.GroupList> rtn=fieldValueCache.get(size);
		if(rtn==null)
		{
			return ;
		}
		MdrillUtils.LOG.info("fieldValueCache.size="+rtn.size()+",size="+size);
	
		int sz=Math.min(UniqConfig.ShardMaxGroups(), 640000);
		if(rtn.size()>sz)
		{
			int left=rtn.size()-sz+1;
			for(int i=0;i<left;i++)
			{
				rtn.poll();
			}
		}
	}

}
