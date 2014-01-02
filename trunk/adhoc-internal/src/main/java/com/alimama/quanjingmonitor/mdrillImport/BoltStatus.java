package com.alimama.quanjingmonitor.mdrillImport;

public class BoltStatus {
	public volatile long InputCount = 0;
	public volatile long groupCreate = 0;

	@Override
	public String toString() {
		return "BoltStatus [InputCount=" + InputCount + ", groupCreate="
				+ groupCreate + "]";
	}

	public void reset() {

		InputCount = 0;
		groupCreate = 0;

	}
}
