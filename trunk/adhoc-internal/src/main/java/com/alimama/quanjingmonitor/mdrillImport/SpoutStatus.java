package com.alimama.quanjingmonitor.mdrillImport;

public class SpoutStatus {

	public volatile long ttInput=0;
	public volatile long groupInput=0;
	public volatile long groupCreate=0;


	@Override
	public String toString() {
		return "SpoutStatus [ttInput=" + ttInput + ", groupInput=" + groupInput
				+ ", groupCreate=" + groupCreate + "]";
	}

	public void reset()
	{
		ttInput=0;
		groupCreate=0;
		groupInput=0;
	}
}
