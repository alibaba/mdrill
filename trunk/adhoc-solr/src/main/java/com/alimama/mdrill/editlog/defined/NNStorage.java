package com.alimama.mdrill.editlog.defined;

import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.editlog.write.FileJournalManager;

public class NNStorage {

	public static Path getInProgressEditsFile(StorageDirectory sd, long txid) {
		return new Path(sd.getCurrentDir(),FileJournalManager.EDITS_INPROGRESS_FILE_NAME+"_"+txid);
	}

	public static Path getFinalizedEditsFile(StorageDirectory sd,long firstTxId, long lastTxId) {
		return new Path(sd.getCurrentDir(),FileJournalManager.EDITS_FILE_NAME+"_"+firstTxId+"-"+lastTxId);

	}

}
