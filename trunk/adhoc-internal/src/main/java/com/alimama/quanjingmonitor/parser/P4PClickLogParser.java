package com.alimama.quanjingmonitor.parser;

import com.taobao.loganalyzer.input.click.parser.*;


public class P4PClickLogParser
    implements Parser
{
    public P4PClickLogParser()
    {
    }

    @Override
    public Object parse(String raw)
	throws InvalidEntryException
    {
	try {
	    ClickLog p4pclicklog = com.taobao.loganalyzer.input.click.parser.ClickLogParser.parse(raw);
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


