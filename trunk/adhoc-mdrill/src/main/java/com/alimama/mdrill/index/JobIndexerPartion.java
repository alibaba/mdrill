package com.alimama.mdrill.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;

import com.alimama.mdrill.partion.MdrillPartions;
import com.alimama.mdrill.partion.MdrillPartionsInterface;

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
	private Path index;
	private String type="default";
	
	private JobIndexParse parse=null;
	private MdrillPartionsInterface mdrillpartion;

	public JobIndexerPartion(Configuration conf,int _shards, String _solrHome,  String _inputBase,  int _dayplus,int _maxRunDays, String _startday, String _filetype,String type)
			throws IOException {
		this.shards = _shards;
		this.solrHome = _solrHome;
		this.tmp = new Path(_solrHome,"tmp");
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

	}

	
	private void cleanTmp() throws IOException
	{
		if(this.fs.exists(this.tmp))
		{
			this.fs.delete(this.tmp,true);
		}
	}
	public int run(String[] args) throws Exception {
		
		this.cleanTmp();
		
		HashSet<String> namelist = this.mdrillpartion.getNameList(fs, this.inputBase,  this.startday, dayDelay, maxRunDays);
		HashMap<String,HashSet<String>> partions=this.mdrillpartion.indexPartions(namelist, startday, dayDelay, maxRunDays);
		HashMap<String,String> vertifyset=this.mdrillpartion.indexVertify(partions, shards, startday, dayDelay, maxRunDays);
		
		HashSet<String> copy=new HashSet<String>();
		
		Set<String> olds=parse.readPartion(this.index);
		
		for(Entry<String,HashSet<String>> e:partions.entrySet())
		{
			String partion=e.getKey();
			olds.remove(partion);
			HashSet<String> days=e.getValue();
		
			String partionvertify = vertifyset.get(partion);
			if(partionvertify==null||partionvertify.isEmpty())
			{
				partionvertify=	"partionV"+MdrillPartions.PARTION_VERSION+"@001@single@" + this.shards + "@"+ java.util.UUID.randomUUID().toString();
			}
			
			Path indexOtherPath = new Path(this.index, partion);
			Path tmpindexOtherPath = new Path(this.tmp, partion);
			Path otherveritify=new Path(indexOtherPath, "vertify");
			if (!partionvertify.equals(parse.readFirstLineStr(otherveritify))) {
				int ret = 0;
				if (days.size() > 0) {
					ret = this.subRun(days, tmpindexOtherPath.toString(),args[0],args[1],Integer.parseInt(args[2]));
					parse.writeStr(new Path(tmpindexOtherPath, "vertify"), partionvertify);
					copy.add(partion);
				}
				if (ret != 0) {
					return ret;
				}
			}
		}
		
		for(String old:olds)
		{
			Path indexOtherPath = new Path(this.index, old);
			if(this.fs.exists(indexOtherPath))
			{
				this.fs.delete(indexOtherPath,true);
			}
		}
		
		for(String partion:copy)
		{
			Path indexOtherPath = new Path(this.index, partion);
			Path tmpindexOtherPath = new Path(this.tmp, partion);
			if(this.fs.exists(indexOtherPath))
			{
				this.fs.delete(indexOtherPath,true);
			}
			this.fs.mkdirs(indexOtherPath.getParent());
			this.fs.rename(tmpindexOtherPath, indexOtherPath);
		}
		
		this.cleanTmp();
		return 0;
	}

	

	private int subRun(HashSet<String> inputs, String output,String split,String submatch,Integer parallel) throws Exception {
		Path smallindex = this.parse.smallIndex(output);
		return MakeIndex.make(fs, solrHome, getConf(), this.filetype, this.inputBase, inputs, submatch, output, smallindex, shards, split,true,"",null,parallel);
		
	}
}
