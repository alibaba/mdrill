package com.alimama.mdrillImport;



public interface Parser
extends java.io.Serializable
{
public Object parse(Object raw)
throws InvalidEntryException;
}