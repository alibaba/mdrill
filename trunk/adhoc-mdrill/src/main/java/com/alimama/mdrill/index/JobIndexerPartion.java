package com.alimama.mdrill.index;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;

import com.alimama.mdrill.partion.MdrillPartions;
import com.alimama.mdrill.partion.MdrillPartionsInterface;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.IndexUtils;
import com.alimama.mdrill.utils.TryLockFile;

public class JobIndexerPartion extends Configured implements Tool {
	private int shards;
	private String solrHome;
	private FileSystem fs;
	private String inputBase;
	private int dayDelay = 10;
	private int maxRunDays = 365;
	private String startday = "19831107";
	private String filetype = "txt";
	private Path tmp;
	private Path workDir;
	private Path index;
	private String type="default";
	
	private JobIndexParse parse=null;
	private MdrillPartionsInterface mdrillpartion;

	TryLockFile flock=null;
	public JobIndexerPartion(String tablename,Configuration conf,int _shards, String _solrHome,  String _inputBase,  int _dayplus,int _maxRunDays, String _startday, String _filetype,String type)
			throws IOException {
		this.shards = _shards;
		this.solrHome = _solrHome;
		this.tmp = new Path(_solrHome,"tmp");
		this.workDir= new Path(this.tmp,java.util.UUID.randomUUID().toString());
		this.index = new Path(_solrHome,"index");
		this.inputBase = _inputBase;
		this.dayDelay = _dayplus;
		this.maxRunDays = _maxRunDays;
		this.startday = _startday;
		this.filetype = _filetype;
		this.fs = FileSystem.get(conf);
		this.parse=new JobIndexParse(fs);
		this.type=type;
		this.mdrillpartion=MdrillPartions.INSTANCE(this.type);
		
		
		String stormhome = System.getProperty("storm.home");
		if (stormhome == null) {
			stormhome=".";
		}
		
		String lockPathBase=stormhome+"/lock";
		File file = new File(lockPathBase);
		file.mkdirs();
		flock=new TryLockFile(lockPathBase+"/"+tablename);

	}

	private void cleanTmp() throws IOException
	{
		HadoopUtil.cleanHistoryFile(this.fs, this.tmp);
	}
	
	
	private TreeMap<String,HashSet<String>> getPartions() throws Exception
	{
		HashSet<String> namelist = this.mdrillpartion.getNameList(fs, this.inputBase,  this.startday, dayDelay, maxRunDays);
		HashMap<String,HashSet<String>> partions=this.mdrillpartion.indexPartions(namelist, startday, dayDelay, maxRunDays);
		TreeMap<String, HashSet<String>> rtn= new TreeMap<String, HashSet<String>>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}
			
		});
		
		rtn.putAll(partions);
		
		return rtn;
	}
	
	public HashMap<String,String> getVertify(HashMap<String,HashSet<String>> partions) throws Exception
	{
		HashMap<String,String> vertifyset=this.mdrillpartion.indexVertify(partions, shards, startday, dayDelay, maxRunDays);
		return vertifyset;
	}
	

	public String getCurrentVertify(String partion,HashSet<String> partionDays,String submatch) throws Exception
	{
		HashMap<String,HashSet<String>> partions=new HashMap<String, HashSet<String>>();
		partions.put(partion, partionDays);
		HashMap<String,String> vertifyset=this.mdrillpartion.indexVertify(partions, shards, startday, dayDelay, maxRunDays);
		String partionvertify = vertifyset.get(partion);
		if(partionvertify==null||partionvertify.isEmpty())
		{
			partionvertify=	"partionV"+MdrillPartions.PARTION_VERSION+"@001@single@" + this.shards + "@"+ java.util.UUID.randomUUID().toString();
		}
		
		HashSet<FileStatus> pathlist=MakeIndex.getInputList(this.fs, this.inputBase,partionDays,submatch);
		long dusize=0;
		long mintime=Long.MAX_VALUE;
		long maxtime=Long.MIN_VALUE;
		for(FileStatus p:pathlist)
		{
			if( p.isDir())
			{
				dusize+=HadoopUtil.duSize(fs, p.getPath());
			}else{
				dusize +=  p.getLen();
			}
			long lasttimes=p.getModificationTime();
			mintime=Math.min(mintime, lasttimes);
			maxtime=Math.max(maxtime, lasttimes);
		}
				
		return partionvertify+"@"+dusize+"@"+pathlist.size()+"@"+parseDate(mintime)+"@"+parseDate(maxtime);
	}
	
	SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
	public String parseDate(long t)
	{
		try{
		Date d=new Date(t);
		return fmt.format(d);
		}catch(Throwable e){
			return String.valueOf(t);
		}
	}
	
	public static class rebuildPartion{
		String partion;
		HashSet<String> days;
		Path tmpindexOtherPath;
		Path otherveritify;
		String partionvertify;
	}
	public int run(String[] args) throws Exception {
		String split=args[0];
		String submatch=args[1];
		Integer parallel=Integer.parseInt(args[2]);
		String tablemode=args[3];
		Integer rep=Integer.parseInt(args[4]);

		this.cleanTmp();
		while(true)
		{
			TreeMap<String,HashSet<String>> partions=this.getPartions();
			rebuildPartion runPartion=null;
			for(Entry<String,HashSet<String>> e:partions.entrySet())
			{
				String partion=e.getKey();
				HashSet<String> days=e.getValue();
				String currentvertify = this.getCurrentVertify(partion, days,submatch);
				
				Path indexOtherPath = new Path(this.index, partion);
				Path tmpindexOtherPath = new Path(this.workDir, partion);
				Path otherveritify=new Path(indexOtherPath, "vertify");
				if (!currentvertify.equals(parse.readFirstLineStr(otherveritify))) {
					if (days.size() > 0) {
						runPartion=new rebuildPartion();
						runPartion.partion=partion;
						runPartion.days=days;
						runPartion.tmpindexOtherPath=tmpindexOtherPath;
						runPartion.otherveritify=otherveritify;
						runPartion.partionvertify=currentvertify;
						break;
					}
				}
			}
			if(runPartion!=null)
			{
				System.out.println("vertify:"+runPartion.partion+">>>"+runPartion.partionvertify);
				int ret=0;
				try{
					flock.trylock();
					String currentvertify = this.getCurrentVertify(runPartion.partion, runPartion.days,submatch);
					Path indexOtherPath = new Path(this.index, runPartion.partion);
					Path otherveritify=new Path(indexOtherPath, "vertify");
					if (currentvertify.equals(parse.readFirstLineStr(otherveritify))) {
						System.out.println("##########finiesd by other process #########");
						continue;
					}
					
					
					ret = this.subRun(runPartion.days, runPartion.tmpindexOtherPath.toString(),split,submatch,parallel,tablemode,rep);
					parse.writeStr(new Path(runPartion.tmpindexOtherPath, "vertify"), runPartion.partionvertify);
				}finally{
					flock.unlock();
				}
				if (ret != 0) {
					return ret;
				}
				
				TreeMap<String, HashSet<String>> partionscomplete=this.getPartions();
				HashSet<String> days=partionscomplete.get(runPartion.partion);
				if(days==null)
				{
					continue;
				}
				String currentVertify = this.getCurrentVertify(runPartion.partion, days,submatch);
				System.out.println("vertify:"+runPartion.partion+">>>"+runPartion.partionvertify+"<<<"+currentVertify);

				if (!currentVertify.equals(runPartion.partionvertify)) {
					System.out.println("##########changed#########");
					continue;
				}
				
				try{
				Path indexOtherPath = new Path(this.index, runPartion.partion);
				if(this.fs.exists(indexOtherPath))
				{
					this.fs.delete(indexOtherPath,true);
				}
				this.fs.mkdirs(indexOtherPath.getParent());
				this.fs.rename(runPartion.tmpindexOtherPath, indexOtherPath);
				}catch(Throwable e)
				{
					e.printStackTrace();
				}
				continue;
			}
			break;
		}
		
		this.cleanNotUsedPartion();
		
		this.cleanTmp();
		this.fs.delete(this.workDir,true);
		return 0;
	}
	
	private void cleanNotUsedPartion() throws Exception
	{
		TreeMap<String, HashSet<String>> partions=this.getPartions();
		Set<String> olds=parse.readPartion(this.index);
		for(Entry<String,HashSet<String>> e:partions.entrySet())
		{
			String partion=e.getKey();
			olds.remove(partion);
		}
		
		for(String old:olds)
		{
			Path indexOtherPath = new Path(this.index, old);
			if(this.fs.exists(indexOtherPath))
			{
				this.fs.delete(indexOtherPath,true);
			}
		}
	}

	

	private int subRun(HashSet<String> inputs, String output,String split,String submatch,Integer parallel,String tablemode,int rep) throws Exception {
		Path smallindex = this.parse.smallIndex(output);
		Configuration conf=this.getConf();
		conf.set("mdrill.table.mode", tablemode);
		conf.setInt("dfs.replication", rep);
		
		 conf.set("io.sort.mb", "80");
		 Pattern mapiPattern      = Pattern.compile("@iosortmb:([0-9]+)@");
		 Matcher mat=mapiPattern.matcher(tablemode);
         if (mat.find()) {
     		conf.set("io.sort.mb", mat.group(1));
         }
		
		String hdfsPral = "1";
		mapiPattern = Pattern.compile("@sigment:([0-9]+)@");
		mat = mapiPattern.matcher(tablemode);
		if (mat.find()) {
			hdfsPral = mat.group(1);
		}
         
         
		
		int sigcount=1;
		try{
			sigcount=Integer.parseInt(hdfsPral);
		}catch(Throwable e){}
		
		if(sigcount<=1)
		{
			return MakeIndex.make(fs, solrHome, conf, this.filetype, this.inputBase, inputs, submatch, output, smallindex, shards, split,true,"",null,parallel);
		}
		
		int rtn=MakeIndex.make(fs, solrHome, conf, this.filetype, this.inputBase, inputs, submatch, output, smallindex, shards*sigcount, split,true,"",null,parallel);
		if(rtn==0)
		{
			Path subdir=new Path(output,"sigment");
			for(int i=0;i<shards*sigcount;i++)
			{
				String dir=IndexUtils.getHdfsForder(i);
				String sig=IndexUtils.getHdfsForder(i/sigcount);
				if(fs.exists(new Path(output,dir)))
				{
					Path newname=new Path(subdir,sig);
					fs.mkdirs(newname);
					fs.rename(new Path(output,dir), new Path(newname,dir));
				}
			}
		}
		return rtn;
		
	}
}
