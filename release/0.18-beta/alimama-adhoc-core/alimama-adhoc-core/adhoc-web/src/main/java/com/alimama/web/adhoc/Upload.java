package com.alimama.web.adhoc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.log4j.Logger;
import org.mortbay.log.Log;

import com.alimama.web.ByteBuffer;



public class Upload {
	private static Logger LOG = Logger.getLogger(Upload.class);

	private static int BUFFER_LEN=10240;
	private static int HEADER_SIZE=102400;
	byte[]  buffer = new byte[BUFFER_LEN];
    
	//----boundary----
    private byte[] m_boundary = new byte[HEADER_SIZE];
    private int m_boundary_length=0;
    private boolean foundBoundary = false;
    
    //----for file split---
    int boundarySkipLen=0;
    
    //----for header read----
    private boolean foundHeader=false;
    //----header info-----
    private String dataHeader = new String();
    private String fieldName = new String();
    private String fileName = new String();
    private String fileExt = new String();
    private String filePathName = new String();
    private String contentType = new String();
    private String contentDisp = new String();
    private String typeMIME = new String();
    private String subTypeMIME = new String();
    private boolean isFile = false;
    
    
	private void resetHeader()
	{
		 this.foundHeader=false;
		 this.boundarySkipLen=0;

	}
	
	
	private int setBoundary(int readBytes,int i)
	{
		for(; !foundBoundary && i<readBytes; i++)
        {
            if(buffer[i] == 13)
            {
                foundBoundary = true;
                
                byte[] newboundary=new byte[m_boundary_length];
                for(int j=0;j<m_boundary_length;j++)
                {
                	newboundary[j]=m_boundary[j];
                }
                m_boundary=newboundary;
                break;
            }
            else
            {
            	int currindex=m_boundary_length;
            	m_boundary_length++;
                m_boundary[currindex]=buffer[i];
            }
        }
		return i;
	}
	
	private static byte[] headerSplit={13,42,13};
	
	
	private byte[] splitbuffer=new byte[0];
	private boolean setHearder(int readBytes,int i,String charset) throws UnsupportedEncodingException
	{
		int leftdata=readBytes-i;
		int lastlen=splitbuffer.length;
		byte[] tmpbyte=new byte[lastlen+leftdata];
		for(int j=0;j<lastlen;j++)
		{
			tmpbyte[j]=splitbuffer[j];
		}
		for(int j=0;j<leftdata;j++)
		{
			tmpbyte[j+lastlen]=buffer[j+i];
		}
		
		ByteSplit split=this.readUntil(tmpbyte, headerSplit,true);
		splitbuffer=split.left;
		foundHeader=split.isfullcut;
 	    if(foundHeader)
 	    {
				dataHeader = new String(split.read, 0, split.read.length,charset);
				LOG.info("@@@@@@@dataHeader:" + dataHeader + "@@@@"
						+ new String(m_boundary, 0, m_boundary_length, "utf-8"));

				isFile = dataHeader.indexOf("filename") > 0;
				fieldName = getDataFieldValue(dataHeader, "name");
				if (isFile) {
					filePathName = getDataFieldValue(dataHeader, "filename");
					fileName = getFileName(filePathName);
					fileExt = getFileExt(fileName);
					contentType = getContentType(dataHeader);
					contentDisp = getContentDisp(dataHeader);
					typeMIME = getTypeMIME(contentType);
					subTypeMIME = getSubTypeMIME(contentType);
					LOG.info("@@@@@@@file:" + fieldName + "," + filePathName
							+ "," + fileName);

				}
 	    }
 	    
 	    return foundHeader;
	}
	
	private boolean setHeadVal(int readBytes,int i,String charset,FSDataOutputStream out,HashMap<String,String> params) throws IOException
	{
		int leftdata=readBytes-i;
		int lastlen=splitbuffer.length;
		byte[] tmpbyte=new byte[lastlen+leftdata];
		for(int j=0;j<lastlen;j++)
		{
			tmpbyte[j]=splitbuffer[j];
		}
		for(int j=0;j<leftdata;j++)
		{
			tmpbyte[j+lastlen]=buffer[j+i];
		}
		
		ByteSplit split=this.readUntil(tmpbyte, m_boundary,false);
		splitbuffer=split.left;
		foundHeader=split.isfullcut;
		
		
		for(int j=0;j<split.read.length;j++)
		{
			writebuff.append(split.read[j]);
		}
		
		if(writebuff.size()>10240&&isFile)
        {
        	out.write(writebuff.toArray(),0,writebuff.size());
        	writebuff=new ByteBuffer(10752);
        }
		
		if(split.isfullcut)
		{
 			this.skip(out, writebuff, charset, params);
 			writebuff=new ByteBuffer(10752);

		}
		return split.isfullcut;
	}
	
	
    ByteBuffer writebuff=new ByteBuffer(10752);
    
	public void mergerTo(HttpServletRequest request, HttpServletResponse response,String charset,FSDataOutputStream out,HashMap<String,String> params) throws IOException
	{
		int totalBytes  = request.getContentLength();
        ServletInputStream in=request.getInputStream();
        int readBytes = 0;
        
        this.resetHeader();
        boolean isReadHeader=true;
        
        int index=0;
        for(int totalRead=0; totalRead < totalBytes; totalRead += readBytes)
        {
        	int size=totalBytes - totalRead;
            readBytes = in.read(buffer, 0, Math.min(size,BUFFER_LEN));
            if(index++%10000==0)
            {
            	Log.info("total read="+totalRead);
            }
            
            int i=0;
            i=this.setBoundary(readBytes, i);
            if(i>=readBytes)
            {
            	continue;
            }
            
            boolean isloop=true;
            
            while(isloop)
            {
    			isloop=false;
	            if(isReadHeader)
	        	{
	        		if(this.setHearder(readBytes, i,charset))
	        		{
	        			isReadHeader=false;
	        			isloop=true;
	        		}
	        	}else{
	        		if(this.setHeadVal( readBytes, i, charset, out, params))
	        		{
	        			isReadHeader=true;
	        			isloop=true;
	        		}
	        	}
	            i=readBytes;
            }
            
        }
        
        if(this.splitbuffer.length>0)
        {
	    	for(int j=0;j<this.splitbuffer.length;j++)
			{
				writebuff.append(splitbuffer[j]);
			}
        }
        
        

        if(writebuff.size()>0&&!keys.contains(fieldName))
        {
        	if(isFile)
        	{
        		out.write(writebuff.toArray(),0,writebuff.size());
        	}else{
        		String val=new String(writebuff.toArray(),0,writebuff.size(),charset).replaceAll("^[\r|\n]*", "").replaceAll("[\r|\n]*$", "");
				params.put(fieldName, val);
    			LOG.info("@@@@@@@keyval_final:"+fieldName+"="+val);
        	}
        	writebuff=new ByteBuffer(10752);
        }
        
        

        
	}
	
	HashSet<String> keys=new HashSet<String>(); 
	
	private void skip(FSDataOutputStream out, ByteBuffer writebuff,String charset,HashMap<String,String> params) throws IOException
	{
		keys.add(fieldName);
		if(this.isFile)
		{
			out.write(writebuff.toArray(),0,writebuff.size());
		}else{
			String val=new String(writebuff.toArray(),0,writebuff.size(),charset).replaceAll("^[\r|\n]*", "").replaceAll("[\r|\n]*$", "");
			params.put(fieldName, val);
			LOG.info("@@@@@@@keyval:"+fieldName+"="+val);

		}

		this.resetHeader();
	}
	
	private String getFileExt(String fileName)
	    {
	        String value = new String();
	        int start = 0;
	        int end = 0;
	        if(fileName == null)
	            return null;
	        start = fileName.lastIndexOf(46) + 1;
	        end = fileName.length();
	        value = fileName.substring(start, end);
	        if(fileName.lastIndexOf(46) > 0)
	            return value;
	        else
	            return "";
	    }

	    private String getContentType(String dataHeader)
	    {
	        String token = new String();
	        String value = new String();
	        int start = 0;
	        int end = 0;
	        token = "Content-Type:";
	        start = dataHeader.indexOf(token) + token.length();
	        if(start != -1)
	        {
	            end = dataHeader.length();
	            value = dataHeader.substring(start, end);
	        }
	        return value;
	    }

	    private String getTypeMIME(String ContentType)
	    {
	        int pos = 0;
	        pos = ContentType.indexOf("/");
	        if(pos != -1)
	            return ContentType.substring(1, pos);
	        else
	            return ContentType;
	    }

	    private String getSubTypeMIME(String ContentType)
	    {
	        int start = 0;
	        int end = 0;
	        start = ContentType.indexOf("/") + 1;
	        if(start != -1)
	        {
	            end = ContentType.length();
	            return ContentType.substring(start, end);
	        } else
	        {
	            return ContentType;
	        }
	    }

	    private String getContentDisp(String dataHeader)
	    {
	        String value = new String();
	        int start = 0;
	        int end = 0;
	        start = dataHeader.indexOf(":") + 1;
	        end = dataHeader.indexOf(";");
	        value = dataHeader.substring(start, end);
	        return value;
	    }
	
	 private String getDataFieldValue(String dataHeader, String fieldName)
	    {
	        String token = new String();
	        String value = new String();
	        int pos = 0;
	        int i = 0;
	        int start = 0;
	        int end = 0;
	        token = String.valueOf((new StringBuffer(String.valueOf(fieldName))).append("=").append('"'));
	        pos = dataHeader.indexOf(token);
	        if(pos > 0)
	        {
	            i = pos + token.length();
	            start = i;
	            token = "\"";
	            end = dataHeader.indexOf(token, i);
	            if(start > 0 && end > 0)
	                value = dataHeader.substring(start, end);
	        }
	        return value;
	    }
	 
	 private String getFileName(String filePathName)
	    {
	        int pos = 0;
	        pos = filePathName.lastIndexOf(47);
	        if(pos != -1)
	            return filePathName.substring(pos + 1, filePathName.length());
	        pos = filePathName.lastIndexOf(92);
	        if(pos != -1)
	            return filePathName.substring(pos + 1, filePathName.length());
	        else
	            return filePathName;
	    }
	 
	 

	    private static class ByteSplit{
	    	public boolean isfullcut;
			public byte[] read;
	    	public byte[] left;
	    	public ByteSplit(boolean isfullcut, byte[] read, byte[] left) {
				super();
				this.isfullcut = isfullcut;
				this.read = read;
				this.left = left;
			}
	    	
	    }
	    private ByteSplit readUntil(byte[] buffer,byte[] search,boolean fuzzle) {
	        final int len = buffer.length;
	        int matpos=len;
	        boolean isfound=false;
	        for (int i = 0; i < len; ++i) {
		   		int leftlen=len-i;
	        	if(this.ismatch(buffer, search,i,0,Math.min(search.length, leftlen),fuzzle))
	      		{
	        		isfound=true;
	    	   		matpos=i;
	    	   		break;
	      		}
	        }
	        
	        boolean isfullcut=isfound&&(len-matpos>=search.length);
	        byte[] read=new byte[matpos];
	        for(int i=0;i<matpos;i++)
	        {
	        	read[i]=buffer[i];
	        }
	        int offset=matpos;
	        if(isfullcut)
	        {
	        	offset+=search.length;
	        }
	        
	        int leftlen=len-offset;
	        leftlen=leftlen>0?leftlen:0;
	        
	        byte[] left=new byte[leftlen];
	        for(int i=0;i<leftlen;i++)
	        {
	        	left[i]=buffer[i+offset];

	        }
	        
	        return new ByteSplit(isfullcut, read, left);
	      }
	    
	    private boolean ismatch(byte[] buffer,byte[] search,int offset,int start,int end,boolean fuzzle)
	    {
	    	 boolean isallMatch=true;
	   		 for(int j=start;j<end;j++)
	   		 {
	   			 if(!this.ismatch(buffer[j+offset], search[j],fuzzle))
	   			 {
	   				 isallMatch=false;
	   				 break;
	   			 }
	   		 }
	   		 
	   		 return isallMatch;
	    }
	    
	    private boolean ismatch(byte a,byte mat,boolean fuzzle){
	    	if(fuzzle)
	    	{
	    		return mat==42||a==mat;
	    	}
	    	return a==mat;
	    }
}
