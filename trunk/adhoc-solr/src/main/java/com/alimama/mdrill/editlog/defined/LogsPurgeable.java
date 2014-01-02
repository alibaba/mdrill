package com.alimama.mdrill.editlog.defined;

import java.io.IOException;
import java.util.Collection;

import com.alimama.mdrill.editlog.read.EditLogInputStream;


public interface LogsPurgeable {
  
  public void purgeLogsOlderThan(long minTxIdToKeep) throws IOException;
  
  void selectInputStreams(Collection<EditLogInputStream> streams,
      long fromTxId, boolean inProgressOk, boolean forReading) throws IOException;
  
}
