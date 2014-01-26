package com.alimama.mdrillImport;

public class SpoutStatus {

	public volatile long ackCnt=0;




	public volatile long failCnt=0;
	public volatile long ttInput=0;
	public volatile long groupInput=0;
	public volatile long groupCreate=0;


	

	public void reset()
	{
		ttInput=0;
		groupCreate=0;
		groupInput=0;
		ackCnt=0;
		failCnt=0;
	}
	
	@Override
	public String toString() {
		return "SpoutStatus [ackCnt=" + ackCnt + ", failCnt=" + failCnt
				+ ", ttInput=" + ttInput + ", groupInput=" + groupInput
				+ ", groupCreate=" + groupCreate + "]";
	}

}
