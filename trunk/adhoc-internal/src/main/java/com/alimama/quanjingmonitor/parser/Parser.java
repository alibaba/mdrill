package com.alimama.quanjingmonitor.parser;


public interface Parser
extends java.io.Serializable
{
public Object parse(String raw)
throws InvalidEntryException;
}