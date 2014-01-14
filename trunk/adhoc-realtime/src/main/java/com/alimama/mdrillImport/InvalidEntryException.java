package com.alimama.mdrillImport;


import java.io.*;

public class InvalidEntryException
    extends IOException
{
    public InvalidEntryException()
    {
	super();
    }

    public InvalidEntryException(String message)
    {
	super(message);
    }

    public InvalidEntryException(String message, Throwable cause)
    {
	super(message, cause);
    }
}