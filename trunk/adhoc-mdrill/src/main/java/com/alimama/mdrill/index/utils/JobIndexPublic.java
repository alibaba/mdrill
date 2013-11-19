package com.alimama.mdrill.index.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import backtype.storm.topology.TopologyBuilder;

import com.alimama.mdrill.utils.EncodeUtils;
import com.alimama.mdrill.utils.HadoopBaseUtils;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alimama.mdrill.utils.UniqConfig;

public class JobIndexPublic {
	public static String getOutFileName(TaskAttemptContext context,
			String prefix) {
		TaskID taskId = context.getTaskAttemptID().getTaskID();
		int partition = taskId.getId();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(5);
		nf.setGroupingUsed(false);
		StringBuilder result = new StringBuilder();
		result.append(prefix);
		result.append("-");
		result.append(nf.format(partition));
		return result.toString();
	}

	public static Analyzer setAnalyzer(Configuration _conf) throws Exception {
		return  new StandardAnalyzer((Version) Enum.valueOf((Class) Class.forName("org.apache.lucene.util.Version"),	Version.LUCENE_35.name()));
	}



	public static String readFieldsFromSchemaXml(String schemaFile,
			FileSystem fs, Configuration conf) throws Exception {
		String regex = "<field\\s+name=\"([^\"]*?)\"\\s+type=\"([^\"]*?)\"\\s+indexed=\"([^\"]*?)\"\\s+stored=\"([^\"]*?)\"\\s*.*/>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher("");
		List<String> list = new ArrayList<String>();
		BufferedReader br = null;
		String[] fields = null;
		try {
			FSDataInputStream in=fs.open(new Path(schemaFile));
			br = new BufferedReader(new InputStreamReader(in));
			String temp = null;// 获得的值以空格分隔，"fieldName fieldType"
			while ((temp = br.readLine()) != null) {
				matcher.reset(temp);
				if (matcher.find()) {
					String fnft = matcher.group(1) + " " + matcher.group(2)
							+ " " + matcher.group(3) + " " + matcher.group(4);
					System.out.println(fnft);
					list.add(fnft);
				}
			}
			in.close();
		} finally {
			if (br != null) {
				br.close();
			}
		}
		StringBuilder result = new StringBuilder();
		fields = list.toArray(new String[0]);
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			String[] ss = field.split(" ");
			if (ss.length == 4) {
				String temp = field.replaceAll(" ", ":");
				result.append(temp);
				if (i != fields.length - 1) {
					result.append(",");
				}
			}
		}
		return result.toString();
	}
	
    private static String findContainingJar(Class<?> myClass) {
        ClassLoader loader = myClass.getClassLoader();
        String class_file = myClass.getName().replaceAll("\\.", "/") + ".class";
        if(loader==null)
        {
        	return null;
        }
        try {
            for (Enumeration<URL> itr = loader.getResources(class_file); itr.hasMoreElements();) {
                URL url = itr.nextElement();
                if ("jar".equals(url.getProtocol())) {
                    String toReturn = url.getPath();
                    if (toReturn.startsWith("file:")) {
                        toReturn = toReturn.substring("file:".length());
                    }
                    toReturn = URLDecoder.decode(toReturn, "UTF-8");
                    return toReturn.replaceAll("!.*$", "");
                }
            }
        } catch (IOException e) {}
        return null;
    }
    
    private static void addpath(HashSet<String> jars,Class<?> myClass)
    {
    	String path=findContainingJar(myClass);
    	if(path==null)
    	{
    		return ;
    	}
    	jars.add(path);
    }

	public static void setJars(Configuration conf)
			throws IOException, URISyntaxException {
		HashSet<String> jars = new HashSet<String>();
		addpath(jars, UniqConfig.class);
		addpath(jars, HadoopBaseUtils.class);
		addpath(jars, EncodeUtils.class);
		addpath(jars, HadoopUtil.class);
		addpath(jars, TopologyBuilder.class);
		addpath(jars, org.slf4j.Logger.class);
		addpath(jars, org.slf4j.LoggerFactory.class);
		addpath(jars, org.apache.commons.httpclient.HttpClient.class);
		addpath(jars, org.apache.commons.io.IOUtils.class);
		addpath(jars, org.apache.solr.request.mdrill.MdrillDetail.class);
		addpath(jars, org.xml.sax.SAXException.class);
		jars.remove(findContainingJar(JobIndexPublic.class));
		
		StringBuilder b = new StringBuilder();
		String join = "";
		for (String s : jars) {
			b.append(join);
			b.append(s);
			join = ",";
		}

		
		new GenericOptionsParser(conf, new String[]{"-libjars",b.toString()});
		System.out.println("tmpjars:"+b.toString()+"@@@@@"+conf.get("tmpjars"));


	}

	public static void setDistributecache(Path distpath, FileSystem fs,
			Configuration conf) throws IOException, URISyntaxException {
		DistributedCache.createSymlink(conf);

		FileStatus[] flist = fs.listStatus(distpath);

		for (FileStatus f : flist) {
			if (f.isDir()) {
				continue;
			}
			DistributedCache.addCacheFile(new URI(new Path(distpath, f
					.getPath().getName()).toUri().toString()
					+ "#"
					+ f.getPath().getName()), conf);

		}

	}
	
	
	  public static void main(String[] args) {
			 System.out.println(parseThedate("dt=20120321"));
			 System.out.println(parseThedate("dt=20120321000000"));
			 System.out.println(parseThedate("ef=20120321000000"));
			 System.out.println(parseThedate("dt=2012032w"));
			 System.out.println(parseThedate("dt=201203212"));
			 System.out.println(parseThedate("201203212"));
			 System.out.println(parseThedate("201203212123"));
			 System.out.println(parseThedate("201203212877d"));
			 System.out.println("===");
			 System.out.println(parseThedate(new Path("/xxx/xxx//xxx/x/xxxxx///x"), new Path("/xxx/////////////.////////./////////////////////////xxx//xxx/x/xxxxx///x","a/dt=20120321/abc.txt")));
			 System.out.println(parseThedate(new Path("/xxx/xxx//xxx/x/xxxxx///x"), new Path("/xxx/xxx//xxx/x/xxxxx///x","a/dt=20120321//e/abc.txt")));
			 System.out.println(parseThedate(new Path("/xxx/xxx//xxx/x/xxxxx///x"), new Path("/xxx/xxx//xxx/x/xxxxx///x","a/dt=20120321//e/dt=20120322")));
			 System.out.println(parseThedate(new Path("/xxx/xxx/./xxx/x/xxxxx///x"), new Path("/xxx/xxx//////////////////xxx/x/xxxxx///x","a/dt=20120321//e/dt=20120322")));

		}
		  
			  public static String parseThedate(String name)
			  {
					
					if(name.indexOf("=") >= 0)
					{
						name = name.replaceAll(".*=", "");
					}
					
					if(name.length()>8)
					{
						name=name.substring(0,8);
					}
					
			
					if (name.length()==8 && name.matches("\\d{8}")) {
						SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
						try {
							fmt.parse(name);
							return name;

						} catch (ParseException e) {
						}
					}
					
					return null;
			}
			  
			  
			  public static String parseThedate(Path base,Path p)
			  {
				  Path parent=p.getParent();
				  while(!parent.toUri().equals(base.toUri())&&base.toUri().compareTo(parent.toUri())<=0)
				  {
					  String name=parseThedate(parent.getName());
					  if(name!=null)
					  {
						  return name;
					  }
					  parent=parent.getParent();
				  }
				  return null;
				  
			  }
}
