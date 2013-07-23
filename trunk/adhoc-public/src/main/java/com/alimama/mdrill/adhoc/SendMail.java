package com.alimama.mdrill.adhoc;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;





public class SendMail
{
	private static Logger LOG = Logger.getLogger(SendMail.class);

	private final static String DATE_TEMPLET = "d MMM yyyy HH:mm:ss Z";//"EEE, d MMM yyyy HH:mm:ss Z";  //"Sat, 12 Sep 2009 20:52:10 +0800"

    private final static String BOUNDARY_PREFIX = "--";

    private final static String CRLF = "\r\n";

    private Socket socket;

    private BufferedReader input;

    private PrintStream output=null;

    private SimpleDateFormat format;

    public SendMail()
    {}
    
    
    
    public static void main(String[] args) throws Exception {
    	SendMail send=new SendMail();
		send.send("adhoc@alipay.com","adhoc",args[0], args[0],args[1],args[2],"", "gbk", "172.24.108.36","172.24.108.36","","", 25);

	}
    

    public void send(String to,String subject,String content,String attachment,String charset)
    {
    	send(to,subject,content,attachment,charset,"","");
    }
    
    public void send(String to,String subject,String content,String attachment,String charset,String user,String pass)
    {
    	String[] toList=to.split("[,|;]+");
        for(String tostr:toList)
        {
       	 try {
	        	 String[] addressList= getSMTPServerByJNDI(tostr);
	         	 for(int i=0;i<addressList.length;i++)
	         	 {
	         		 Boolean result=false;
	         		 String[] fromlist={"yannian.mu@alipay.com","myn@163.com","165162897@qq.com"};
	         		 //这样做是有些服务器，堆特定网站的邮箱进行了校验，有可能会失败
	         		for(String from:fromlist)
	         		{
	         			result=send(from,"yannian",tostr, to,subject,content,attachment, charset, addressList[i],addressList[i],user,pass, 25);
	         			if(result)
		         		{
		         			break;
		         		}
	         		}
	         		if(result)
	         		{
	         			break;
	         		}
	         	 }
			} catch (Exception e) {}
        }
    }
    
    public void send(String from,String formName,String to,String subject,String content,String attachment,String charset,String user,String pass)
    {
    	String[] toList=to.split("[,|;]+");
         for(String tostr:toList)
         {
        	 try {
	        	 String[] addressList= getSMTPServerByJNDI(tostr);
	         	 for(int i=0;i<addressList.length;i++)
	         	 {
	         		Boolean result=send(from,formName,tostr, to,subject,content,attachment, charset, addressList[i],addressList[i],user,pass, 25);
	         		if(result)
	         		{
	         			break;
	         		}
	         	 }
			} catch (Exception e) {}
         }
    }

  

    private String getDomainFromAddress( String EmailAddress )
    {
       StringTokenizer   tokenizer = new StringTokenizer( EmailAddress, "@>;" );
       String            DomainOnly = tokenizer.nextToken();

       DomainOnly = tokenizer.nextToken();

       return DomainOnly;
    }
    
    private String[] getSMTPServerByJNDI(String to) throws Exception {  
    	String host=getDomainFromAddress(to);
        Properties jndiEnvironmentProperties = new Properties();  
        jndiEnvironmentProperties.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");  
        InitialDirContext initialDirContext = new InitialDirContext(jndiEnvironmentProperties);  
        Attributes attributes = initialDirContext.getAttributes(host, new String[] {"MX"});  
        
        Attribute attribute = attributes.get("MX");  
        String[] servers = new String[attribute.size()];  
        for (int i = 0; i < attribute.size(); i++) {  
            servers[i] = attribute.get(i).toString();  
            servers[i]=servers[i].substring(servers[i].indexOf(" ") + 1, servers[i].length() -1);  
            
        }  
        return servers;  
    } 
        
    public boolean send(String from,String formName,String to,String replyTO,String subject,String content,String attachment,String charset,String smtpAddress,String smtpHost,String user,String pass,int port)
    {    	
    	try
        {
    		String boundary = "=======ThisIsBoundary=======";
            socket =  new Socket();
            socket.setSoTimeout(10000);
            socket.connect(new InetSocketAddress(smtpAddress, port));//
            input = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            output = new PrintStream( socket.getOutputStream() );
            getResponse( "220", "Failed to connect to: " + smtpHost, true );
            sendCommand( "HELO " + smtpHost/*NO SPACE IN IT*/ + CRLF);
            getResponse( "250", "Failed to get HELO response from server.", true );
            if(user!=null && !user.isEmpty() && pass!=null && !pass.isEmpty())
            {
	            sendCommand( "AUTH LOGIN" + CRLF );
	            getResponse( "334", "Failed to get USERNAME request from server.", true );
	            sendCommand( getBase64String( user ) + CRLF );  //Username
	            getResponse( "334", "Failed to get PASSWORD request from server.", true );
	            sendCommand( getBase64String( pass ) + CRLF );  //Password
	            getResponse( "235", "Failed to send AUTH LOGIN username and password to server.", true );
            }

            sendCommand( "MAIL FROM: <" + from + ">" + CRLF );
            getResponse( "250", "Failed to get MAIL FROM response from server.", true );
            
            String[] toList=to.split("[,|;]+");
            for(String tostr:toList)
            {
            	sendCommand( "RCPT TO: <" + tostr + ">" + CRLF );
            	getResponse( "250", "Failed to get RCPT TO response from server.", false/*NOTE*/ );
            }
            
            if(replyTO==null||replyTO.isEmpty())
            {
            	replyTO=to;
            }

            sendCommand( "DATA" + CRLF );
            getResponse( "354", "Failed to get DATA response from server.", true );
            sendCommand( "Subject: " + getBase64Subject( subject, charset ) + CRLF );
            sendCommand( "Date: " + getDateString() + CRLF );
            sendCommand( "From: " + ""+formName+"<" + from + ">" + CRLF );
            sendCommand( "To: "  +replyTO + CRLF );
            sendCommand( "MIME-Version: 1.0" + CRLF );

            sendCommand( "Content-Type: multipart/mixed; boundary=\"" + boundary + "\"" + CRLF );
            sendCommand( "Content-Transfer-Encoding: 7bit" + CRLF + CRLF/*NOTE*/ );
            sendCommand( "This is a multi-part message in MIME format." + CRLF + CRLF/*NOTE*/ );
            
            sendCommand( BOUNDARY_PREFIX + boundary + CRLF );
            sendCommand( "Content-Type: text/html;" + CRLF );
            sendCommand( "Content-Transfer-Encoding: base64" + CRLF + CRLF/*NOTE*/ );
            sendCommand( getBase64String( content, charset  ) + CRLF + CRLF/*NOTE*/ );
            String[] fileList=attachment.split("[ |\t|,]+");

            for(String eachFile:fileList){
            	if(eachFile.trim().isEmpty())
            	{
            		continue;
            	}
	            sendCommand( BOUNDARY_PREFIX + boundary + CRLF );
	            String [] filenamesplit=eachFile.split("[\\\\|/]+");
	            String filename=filenamesplit[filenamesplit.length-1];
	            
	            if(eachFile.startsWith("hdfs@")&&filename.indexOf(".")<0)
	            {
	            	filename+=".txt";
	            }
	            
	            sendCommand( "Content-Type: application/octet-stream; name=\"" + filename + "\"" + CRLF );
	            sendCommand( "Content-Transfer-Encoding: base64" + CRLF );
	            sendCommand( "Content-Disposition: attachment; filename=\"" + filename + "\"" + CRLF + CRLF/*NOTE*/ );
	            sendAttachment( eachFile );
            }
            
 

            sendCommand( CRLF + "." + CRLF/*NOTE*/ );  //Indicate the end of date using "/r/n./r/n"
            getResponse( "250", "Failed to send DATA content to server.", true );
            sendCommand( "QUIT" + CRLF );
            getResponse( "221", "Failed to get QUIT response from server.", true );
            LOG.info("succ "+from+" to "+to+"    "+smtpAddress);

            return true;
        }
        catch( Exception e ){
        	e.printStackTrace();
        	LOG.info("fail "+from+" to "+to+"    "+smtpAddress);
        	return false;
        }

        finally{
        	try{ output.close(); input.close(); socket.close(); }catch( Exception e ){} 
        }

    }

    private void sendAttachment( String fileName ) throws Exception
    {

    	
    	FileInputStream inputStream = new FileInputStream( fileName );
        int base64PerLine = 76;  //76 base64 charactors per line
        int charsPerLine = 57;  //57 = 76 / 4 * 3
        byte[] src = new byte[4096];
        byte[] dest = new byte[src.length * 2];
        int length = 0, remain = 0, sOffset = 0, dOffset = 0;
        int maxlength=1024*1024;
        int readlean=0;
        while( ( length = inputStream.read( src, remain, src.length - remain ) ) != -1 )
        {
        	readlean+=length;
        	length = length + remain;
            remain = length % charsPerLine;
            length = length / charsPerLine * charsPerLine;
            for( sOffset = 0, dOffset = 0; sOffset < length; )
            {
                Base64Encode.encode( src, sOffset, charsPerLine, dest, dOffset );
                sOffset += charsPerLine; dOffset += base64PerLine;
                dest[dOffset ++] = '\r'; dest[dOffset ++] = '\n';
            }
            output.print( new String( dest, 0, dOffset ) );
            if( remain > 0 ){ System.arraycopy( src, sOffset, src, 0, remain ); }
            
            if(readlean>maxlength)
        	{
        		break;
        	}
        }
        if( remain > 0 )
        {
            Base64Encode.encode( src, 0, remain, dest, 0 );
            dOffset = ( remain + 2 ) / 3 * 4;
            dest[dOffset ++] = '\r'; dest[dOffset ++] = '\n';
            output.print( new String( dest, 0, dOffset ) );
        }
        inputStream.close();
    }
    
    

    private void sendCommand( String command ) throws Exception
    {
        if( output == null || command == null || command.length() < 1 ){ return ; }
        output.print( command ); 
    }

    private void getResponse( String code, String message, boolean shouldQuit ) throws Exception
    {
        if( input == null || code == null || code.length() < 1 ){ return; }

        String line = input.readLine();
        if( line.startsWith( code ) ){ /**/ }else if( shouldQuit ){ 
        	
        	System.out.println(line);
        	throw new Exception( message ); }
    }

    private String getBase64String( String message )
    {
        if( message == null ){ return null; }

        byte[] bytes = message.getBytes();

        return Base64Encode.encode( bytes, 0, bytes.length );
    }
    
    
    private String getBase64String( String subject, String charset )
    {
        if( subject == null ){ return null; }

        byte[] bytes = null;

        try{ bytes = ( charset == null ? subject.getBytes() : subject.getBytes( charset ) ); }catch( Exception e ){
        }

        return ( bytes == null ? null : Base64Encode.encode( bytes, 0, bytes.length ));
    }

    private String getBase64Subject( String subject, String charset )
    {
        if( subject == null ){ return null; }

        byte[] bytes = null;

        try{ bytes = ( charset == null ? subject.getBytes() : subject.getBytes( charset ) ); }catch( Exception e ){
        }

        return ( bytes == null ? null : "=?" + charset + "?B?" + Base64Encode.encode( bytes, 0, bytes.length ) + "?=" );
    }

    private String getDateString()
    {
        if( format == null ){ try{ format = new SimpleDateFormat( DATE_TEMPLET, Locale.ENGLISH ); }catch( Exception e ){} }

        return ( format == null ? new Date().toString() : format.format( new Date() ) );
    }

   
}

