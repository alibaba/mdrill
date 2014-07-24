package org.apache.solr.handler.component;

import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.utils.UniqConfig;

public class MergerServerThreads {
	

	protected static Logger log = LoggerFactory.getLogger(MergerServerThreads.class);

	public static ExecutorService[] EXECUTE = new ExecutorService[UniqConfig.getMergerRequestMaxDepth()];
	static {
		for (int i = 0; i < EXECUTE.length; i++) {
			EXECUTE[i] = new ThreadPoolExecutor(UniqConfig.getMergerRequestThreadsMin(i), UniqConfig.getMergerRequestThreadsMax(i),
                    300L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());
		}
	}
	 
	 static Executor commExecutor = new ThreadPoolExecutor(
	          0,
	          Integer.MAX_VALUE,
	          300, TimeUnit.SECONDS, // terminate idle threads after 5 sec
	          new LinkedBlockingQueue<Runnable>()  // directly hand off tasks
	  );
	
	  public static class SingleThread{
		  public CompletionService<ShardResponse> completionService ;
		  public SingleThread(ExecutorService exe)
		  {
			  completionService=new ExecutorCompletionService<ShardResponse>(exe);
		  }
	  }
	  
	  public static CompletionService<ShardResponse> create(int i)
	  {
		  if(i<EXECUTE.length)
		  {
				 return new ExecutorCompletionService<ShardResponse>(EXECUTE[i]);
		  }
		  
		  
		return new ExecutorCompletionService<ShardResponse>(commExecutor);

	  }
	  
}

//
//public class MergerServerThreads {
//	
//	
//	/**
//	 * 
//	 * 
//	  new ThreadPoolExecutor(
//	          2,
//	          Integer.MAX_VALUE,
//	          300, TimeUnit.SECONDS, // terminate idle threads after 5 sec
//	          new LinkedBlockingQueue<Runnable>()  // directly hand off tasks
//	  );
//	 
//	 
//	 * 
//	 */
//	
//	 static Executor commExecutor =new ThreadPoolExecutor(5, 60,
//             300L, TimeUnit.SECONDS,
//             new LinkedBlockingQueue<Runnable>());
//	
//	  
//	  public static CompletionService<ShardResponse> create(int i)
//	  {
//		return new ExecutorCompletionService<ShardResponse>(commExecutor);
//	  }
//	  
//}
