package com.etao.adhoc.analyse.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.etao.adhoc.analyse.dao.MysqlService;
import com.etao.adhoc.analyse.vo.QueryLog;

public class LoadService {
	MysqlService server;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static String CTRL_A = "\u0001";
	
	public LoadService(){
		server = new MysqlService();
	}
	
	public void loadQueryLogFromFile(File file) throws IOException, SQLException {
		if(file.isDirectory() ){
			File[] files = file.listFiles();
			for(File f : files){
				loadQueryLogFromFile(f);
			}
		} else if (file.isFile()){
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while((line = br.readLine()) != null) {
				String[] fields = line.split(CTRL_A);
				Date date = null;
				try {
					date = sdf.parse(fields[0]);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				System.out.println(sdf.format(date));
				QueryLog queryLog = new QueryLog();
				queryLog.setDate(date);
				queryLog.setNick(fields[1]);
				queryLog.setEmail(fields[2]);
				queryLog.setSetName(fields[3]);
				queryLog.setDimvalue(fields[4]);
				queryLog.setFilter(fields[5]);
				queryLog.setBizdate(fields[6]);
				server.insertQueryLog(queryLog);
			}
		}
	}
	public void close() throws SQLException{
		if(server != null)
			server.close();
	}

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, SQLException {
		// TODO Auto-generated method stub
		LoadService server = new LoadService();
		File file = new File(args[0]);
		if(file.exists())
			server.loadQueryLogFromFile(file);
		else
			System.err.println("File not exists");
		server.close();

	}

}
