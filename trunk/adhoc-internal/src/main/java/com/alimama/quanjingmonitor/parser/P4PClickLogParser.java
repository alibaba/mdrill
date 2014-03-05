package com.alimama.quanjingmonitor.parser;

import com.alimama.mdrillImport.InvalidEntryException;
import com.alimama.mdrillImport.Parser;
import com.taobao.loganalyzer.input.click.parser.*;


public class P4PClickLogParser
    implements Parser
{
    public P4PClickLogParser()
    {
    }

    @Override
    public Object parse(Object raw)
	throws InvalidEntryException
    {
	try {
	    ClickLog p4pclicklog = com.taobao.loganalyzer.input.click.parser.ClickLogParser.parse((String)raw);
	    if (p4pclicklog == null)
	    {
	    	throw new InvalidEntryException("Invalid log `" + raw + "'");
	    }
	    return p4pclicklog;
	} catch (Throwable nfe) {
		throw new InvalidEntryException("Invalid log `" + raw + "'\n" + nfe);
	}
    }
}


