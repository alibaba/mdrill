package com.alimama.mdrill.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.index.utils.JobIndexPublic;


public class JobIndexParse {
	public JobIndexParse(FileSystem fs, String inputBase) {
		super();
		this.fs = fs;
		this.inputBase = inputBase;
		this.yyyymmdd = this.getToday();
	}

	private FileSystem fs;
	private String inputBase;
	private String yyyymmdd;

	public String getYyyymmdd() {
		return yyyymmdd;
	}

	public HashSet<String> getDayList(String starday) throws IOException,
			ParseException {
		HashSet<String> rtn = new HashSet<String>();
		Date start = this.transDay(starday);
		FileStatus[] list = this.fs.listStatus(new Path(this.inputBase));
		if (list != null) {
			for (FileStatus f : list) {
				String name = f.getPath().getName().trim();
				name=JobIndexPublic.parseThedate(name);
				if (name!=null&& this.transDay(name).compareTo(start) >= 0) {
					rtn.add(name);
				}
			}
		}

		return rtn;
	}
	
	public void writeStr(Path file, String contents) throws IOException {
		if (fs.exists(file)) {
			fs.delete(file, true);
		}

		FSDataOutputStream write = fs.create(file);
		write.write(contents.getBytes());
		write.close();
	}
	
	
	public Set<String> readPartion(Path dir) throws IOException {
		HashSet<String> rtn = new HashSet<String>();
		if (fs.exists(dir)) {
			FileStatus[] list = fs.listStatus(dir);
			for (FileStatus d : list) {
				String dirname = d.getPath().getName();
				if (!d.isDir() || dirname.startsWith("_")
						|| dirname.startsWith(".") || dirname.equals("index")) {
					continue;
				}
				Path p = d.getPath();
				rtn.add(p.getName());
			}
		}
		return rtn;
	}

	public String readFirstLineStr(Path file) {
		StringBuffer buff = new StringBuffer();
		try {
			if (fs.exists(file)) {
				FSDataInputStream r = fs.open(file);
				BufferedReader in = new BufferedReader(new InputStreamReader(r,
						"UTF-8"));
				buff.append(in.readLine());
				in.close();
				r.close();
			}
		} catch (IOException e) {
		}
		return buff.toString();
	}

	public Path distribute(String output) {
		return new Path(output + "_DistributedCache");
	}

	public Path smallIndex(String output) {
		return new Path(output + "_smallIndex");
	}
	
	public String getStartDay( int delay,int maxRunDays, String starday) throws ParseException
	{
		String minday = this.DatePlus(this.yyyymmdd, maxRunDays+delay);
		Date min = this.transDay(minday);
		Date start = this.transDay(starday);
		if(min.compareTo(start)>=0)
		{
			return minday;
		}
		
		return starday;
	}
	
	public String getUpdateFinishDay( int delay,String starday) throws ParseException
	{
		String minday = this.DatePlus(this.yyyymmdd,delay);
		Date min = this.transDay(minday);
		Date start = this.transDay(starday);

		if(min.compareTo(start)>=0)
		{
			return minday;
		}
		
		return starday;
	}

	public HashSet<String> getOtherMonth(HashSet<String> all,HashSet<String> currentmonth, int delay,String starday) throws ParseException {
		String plusday = this.DatePlus(this.yyyymmdd, delay);
		
		String strtmonth = getMonth(starday);

		Date currentMonth = this.transMonth(plusday);
		HashSet<String> rtn = new HashSet<String>();
		for (String str : all) {
			Date m = this.transMonth(str);
			if (m.compareTo(currentMonth) < 0 ) {
				String month = getMonth(str);
				if (month.equals(strtmonth) ) {
					currentmonth.add(str);
				} else {
					rtn.add(month);
				}

			}
		}
		return rtn;
	}
	
	public String getToday()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		return fmt.format(new Date());
	}

	public HashSet<String> getCurrentMonth(HashSet<String> all, int delay, String starday) throws ParseException {
		String plusday = this.DatePlus(this.yyyymmdd, delay);
		Date currentMonth = this.transMonth(plusday);
		HashSet<String> rtn = new HashSet<String>();
		String startMonth = getMonth(starday);
		for (String str : all) {
			Date m = this.transMonth(str);
			if (m.compareTo(currentMonth) >= 0) {
				String month = getMonth(str);
				if (month.equals(startMonth) ) {
					rtn.add(str);
				} else {
					rtn.add(month);
				}
			}
		}
		return rtn;
	}

	public String getMonth(String str) {
		return str.substring(0, 6);
	}

	public Date transDay(String str) throws ParseException {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		return fmt.parse(str);
	}
	
	
	


	public Date transMonth(String str) throws ParseException {
		String month = getMonth(str) + "00";
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		return fmt.parse(month);
	}

	public String DatePlus(String str, int day) throws ParseException {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		Date d = fmt.parse(str);
		Date pd = new Date();
		pd.setTime(d.getTime() - 1000l * 3600 * 24 * day);
		return fmt.format(pd);
	}
}
