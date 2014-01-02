package com.alimama.mdrill.editlog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.alimama.mdrill.editlog.JournalSet;
import com.alimama.mdrill.editlog.JournalSet.JournalAndStream;
import com.alimama.mdrill.editlog.defined.HdfsConstants;
import com.alimama.mdrill.editlog.defined.LogsPurgeable;
import com.alimama.mdrill.editlog.defined.StorageDirectory;
import com.alimama.mdrill.editlog.read.EditLogInputStream;
import com.alimama.mdrill.editlog.read.FSEditLogOp;
import com.alimama.mdrill.editlog.read.RedundantEditLogInputStream;
import com.alimama.mdrill.editlog.util.IOUtils;
import com.alimama.mdrill.editlog.write.EditLogOutputStream;
import com.alimama.mdrill.editlog.write.FileJournalManager;

import org.apache.hadoop.conf.Configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


public class FSEditLog implements LogsPurgeable {

  static final Log LOG = LogFactory.getLog(FSEditLog.class);

  private enum State {
    UNINITIALIZED,
    BETWEEN_LOG_SEGMENTS,
    IN_SEGMENT,
    OPEN_FOR_READING,
    CLOSED;
  }  
  private State state = State.UNINITIALIZED;
  
  private JournalSet journalSet = null;
  private EditLogOutputStream editLogStream = null;

  private long txid = 0;

  private long synctxid = 0;

  private long curSegmentTxId = HdfsConstants.INVALID_TXID;

  private long lastPrintTime;

  private volatile boolean isSyncRunning;

  private volatile boolean isAutoSyncScheduled = false;
  
  private long numTransactions;        // number of transactions
  private long numTransactionsBatchedInSync;
  private long totalTimeTransactions;  // total time for all transactions

  private List<StorageDirectory> editsDirs;

  private static class TransactionId {
    public long txid;
    TransactionId(long value) {
      this.txid = value;
    }
  }

  private static final ThreadLocal<TransactionId> myTransactionId = new ThreadLocal<TransactionId>() {
    @Override
    protected synchronized TransactionId initialValue() {
      return new TransactionId(Long.MAX_VALUE);
    }
  };

  public FSEditLog(Configuration conf,  List<StorageDirectory> editsDirs) {
    init(conf,  editsDirs);
  }
  
  private void init(Configuration conf,  List<StorageDirectory> editsDirs) {
    isSyncRunning = false;
    lastPrintTime = System.currentTimeMillis();
    this.editsDirs = Lists.newArrayList(editsDirs);
  }
  
  public synchronized void initJournalsForWrite() {
    Preconditions.checkState(state == State.UNINITIALIZED ||
        state == State.CLOSED, "Unexpected state: %s", state);
    
    initJournals(this.editsDirs);
    state = State.BETWEEN_LOG_SEGMENTS;
  }
  
  public synchronized void initSharedJournalsForRead() {
    if (state == State.OPEN_FOR_READING) {
      LOG.warn("Initializing shared journals for READ, already open for READ",
          new Exception());
      return;
    }
    Preconditions.checkState(state == State.UNINITIALIZED ||
        state == State.CLOSED);
    
    initJournals(this.editsDirs);
    state = State.OPEN_FOR_READING;
  }
  
  private synchronized void initJournals(List<StorageDirectory> dirs) {

    journalSet = new JournalSet(1);

    for (StorageDirectory sd : dirs) {
        journalSet.add(new FileJournalManager(sd), sd.isrequire());

    }
    if (journalSet.isEmpty()) {
      LOG.error("No edits directories configured!");
    } 
  }

  /**
   * Get the list of URIs the editlog is using for storage
   * @return collection of URIs in use by the edit log
   */
  Collection<StorageDirectory> getEditURIs() {
    return editsDirs;
  }

  /**
   * Initialize the output stream for logging, opening the first
   * log segment.
   */
  public synchronized void openForWrite() throws IOException {
    Preconditions.checkState(state == State.BETWEEN_LOG_SEGMENTS,   "Bad state: %s", state);

    long segmentTxId = getLastWrittenTxId() + 1;
    // Safety check: we should never start a segment if there are
    // newer txids readable.
    List<EditLogInputStream> streams = new ArrayList<EditLogInputStream>();
    journalSet.selectInputStreams(streams, segmentTxId, true, true);
    if (!streams.isEmpty()) {
      String error = String.format("Cannot start writing at txid %s " +
        "when there is a stream available for read: %s",
        segmentTxId, streams.get(0));
      IOUtils.cleanup(LOG, streams.toArray(new EditLogInputStream[0]));
      throw new IllegalStateException(error);
    }
    
    startLogSegment(segmentTxId, true);
    assert state == State.IN_SEGMENT : "Bad state: " + state;
  }
  
  /**
   * @return true if the log is currently open in write mode, regardless
   * of whether it actually has an open segment.
   */
  synchronized boolean isOpenForWrite() {
    return state == State.IN_SEGMENT ||
      state == State.BETWEEN_LOG_SEGMENTS;
  }
  
  /**
   * @return true if the log is open in write mode and has a segment open
   * ready to take edits.
   */
  synchronized boolean isSegmentOpen() {
    return state == State.IN_SEGMENT;
  }

  /**
   * @return true if the log is open in read mode.
   */
  public synchronized boolean isOpenForRead() {
    return state == State.OPEN_FOR_READING;
  }

  /**
   * Shutdown the file store.
 * @throws IOException 
   */
  synchronized void close() throws IOException {
    if (state == State.CLOSED) {
      LOG.debug("Closing log when already closed");
      return;
    }

    try {
      if (state == State.IN_SEGMENT) {
        assert editLogStream != null;
        waitForSyncToFinish();
        endCurrentLogSegment(true);
      }
    } finally {
      if (journalSet != null && !journalSet.isEmpty()) {
        try {
          journalSet.close();
        } catch (IOException ioe) {
          LOG.warn("Error closing journalSet", ioe);
        }
      }
      state = State.CLOSED;
    }
  }


  public void logEdit(final FSEditLogOp op) throws Exception {
    synchronized (this) {
      assert isOpenForWrite() :
        "bad state: " + state;
      
      // wait if an automatic sync is scheduled
      waitIfAutoSyncScheduled();
      
      long start = beginTransaction();
      op.setTransactionId(txid);

      try {
        editLogStream.write(op);
      } catch (IOException ex) {
        // All journals failed, it is handled in logSync.
      }

      endTransaction(start);
      isAutoSyncScheduled = true;
    }
    
    // sync buffered edit log entries to persistent store
    logSync();
  }

  /**
   * Wait if an automatic sync is scheduled
   * @throws InterruptedException
   */
  synchronized void waitIfAutoSyncScheduled() {
    try {
      while (isAutoSyncScheduled) {
        this.wait(1000);
      }
    } catch (InterruptedException e) {
    }
  }
  
  /**
   * Signal that an automatic sync scheduling is done if it is scheduled
   */
  synchronized void doneWithAutoSyncScheduling() {
    if (isAutoSyncScheduled) {
      isAutoSyncScheduled = false;
      notifyAll();
    }
  }
  

  
  private long beginTransaction() {
    assert Thread.holdsLock(this);
    // get a new transactionId
    txid++;

    //
    // record the transactionId when new data was written to the edits log
    //
    TransactionId id = myTransactionId.get();
    id.txid = txid;
    return System.currentTimeMillis();
  }
  
  private void endTransaction(long start) {
    assert Thread.holdsLock(this);
    
    // update statistics
    long end = System.currentTimeMillis();
    numTransactions++;
    totalTimeTransactions += (end-start);
  }

  /**
   * Return the transaction ID of the last transaction written to the log.
   */
  public synchronized long getLastWrittenTxId() {
    return txid;
  }
  
  /**
   * @return the first transaction ID in the current log segment
   */
  synchronized long getCurSegmentTxId() {
    Preconditions.checkState(isSegmentOpen(),
        "Bad state: %s", state);
    return curSegmentTxId;
  }
  
  /**
   * Set the transaction ID to use for the next transaction written.
   */
  public synchronized void setNextTxId(long nextTxId) {
    Preconditions.checkArgument(synctxid <= txid &&
       nextTxId >= txid,
       "May not decrease txid." +
      " synctxid=%s txid=%s nextTxId=%s",
      synctxid, txid, nextTxId);
      
    txid = nextTxId - 1;
  }
    
  /**
   * Blocks until all ongoing edits have been synced to disk.
   * This differs from logSync in that it waits for edits that have been
   * written by other threads, not just edits from the calling thread.
   *
   * NOTE: this should be done while holding the FSNamesystem lock, or
   * else more operations can start writing while this is in progress.
 * @throws Exception 
   */
  void logSyncAll() throws Exception {
    // Record the most recent transaction ID as our own id
    synchronized (this) {
      TransactionId id = myTransactionId.get();
      id.txid = txid;
    }
    // Then make sure we're synced up to this point
    logSync();
  }
  
 
	public static void terminate(int status, String msg) throws IOException {
		LOG.info("Exiting with status " + status);
		IOException ee = new IOException(status + "@" + msg);
		LOG.error("Terminate called", ee);
		throw ee;

	}
  
  public void logSync() throws IOException {
    long syncStart = 0;

    // Fetch the transactionId of this thread. 
    long mytxid = myTransactionId.get().txid;
    
    boolean sync = false;
    try {
      EditLogOutputStream logStream = null;
      synchronized (this) {
        try {
          printStatistics(false);

          // if somebody is already syncing, then wait
          while (mytxid > synctxid && isSyncRunning) {
            try {
              wait(1000);
            } catch (InterruptedException ie) {
            }
          }
  
          if (mytxid <= synctxid) {
            numTransactionsBatchedInSync++;
            return;
          }
     
          // now, this thread will do the sync
          syncStart = txid;
          isSyncRunning = true;
          sync = true;
  
          // swap buffers
          try {
            if (journalSet.isEmpty()) {
              throw new IOException("No journals available to flush");
            }
            editLogStream.setReadyToFlush();
          } catch (IOException e) {
            final String msg =
                "Could not sync enough journals to persistent storage " +
                "due to " + e.getMessage() + ". " +
                "Unsynced transactions: " + (txid - synctxid);
            LOG.fatal(msg, new Exception());
            IOUtils.cleanup(LOG, journalSet);
            terminate(1, msg);
          }
        } finally {
          // Prevent RuntimeException from blocking other log edit write 
          doneWithAutoSyncScheduling();
        }
        //editLogStream may become null,
        //so store a local variable for flush.
        logStream = editLogStream;
      }
      
      // do the sync
      long start = System.currentTimeMillis();
      try {
        if (logStream != null) {
          logStream.flush();
        }
      } catch (IOException ex) {
        synchronized (this) {
          final String msg =
              "Could not sync enough journals to persistent storage. "
              + "Unsynced transactions: " + (txid - synctxid);
          LOG.fatal(msg, new Exception());
          IOUtils.cleanup(LOG, journalSet);
          terminate(1, msg);
        }
      }
  
    } finally {
      // Prevent RuntimeException from blocking other log edit sync 
      synchronized (this) {
        if (sync) {
          synctxid = syncStart;
          isSyncRunning = false;
        }
        this.notifyAll();
     }
    }
  }

  //
  // print statistics every 1 minute.
  //
  private void printStatistics(boolean force) {
    long now = System.currentTimeMillis();
    if (lastPrintTime + 60000 > now && !force) {
      return;
    }
    lastPrintTime = now;
    StringBuilder buf = new StringBuilder();
    buf.append("Number of transactions: ");
    buf.append(numTransactions);
    buf.append(" Total time for transactions(ms): ");
    buf.append(totalTimeTransactions);
    buf.append(" Number of transactions batched in Syncs: ");
    buf.append(numTransactionsBatchedInSync);
    buf.append(" Number of syncs: ");
    buf.append(editLogStream.getNumSync());
    LOG.info(buf);
  }



  
  /**
   * Get all the journals this edit log is currently operating on.
   */
  synchronized List<JournalAndStream> getJournals() {
    return journalSet.getAllJournalStreams();
  }
  
  


  public synchronized long rollEditLog() throws IOException {
    LOG.info("Rolling edit logs");
    endCurrentLogSegment(true);
    
    long nextTxId = getLastWrittenTxId() + 1;
    startLogSegment(nextTxId, true);
    
    assert curSegmentTxId == nextTxId;
    return nextTxId;
  }
  
  /**
   * Start writing to the log segment with the given txid.
   * Transitions from BETWEEN_LOG_SEGMENTS state to IN_LOG_SEGMENT state. 
   */
  synchronized void startLogSegment(final long segmentTxId,
      boolean writeHeaderTxn) throws IOException {
    LOG.info("Starting log segment at " + segmentTxId);
    Preconditions.checkArgument(segmentTxId > 0,
        "Bad txid: %s", segmentTxId);
    Preconditions.checkState(state == State.BETWEEN_LOG_SEGMENTS,
        "Bad state: %s", state);
    Preconditions.checkState(segmentTxId > curSegmentTxId,
        "Cannot start writing to log segment " + segmentTxId +
        " when previous log segment started at " + curSegmentTxId);
    Preconditions.checkArgument(segmentTxId == txid + 1,
        "Cannot start log segment at txid %s when next expected " +
        "txid is %s", segmentTxId, txid + 1);
    
    numTransactions = totalTimeTransactions = numTransactionsBatchedInSync = 0;

    
    try {
      editLogStream = journalSet.startLogSegment(segmentTxId);
    } catch (IOException ex) {
      throw new IOException("Unable to start log segment " +
          segmentTxId + ": too few journals successfully started.", ex);
    }
    
    curSegmentTxId = segmentTxId;
    state = State.IN_SEGMENT;

    if (writeHeaderTxn) {
      logSync();
    }
  }

  
  public synchronized void endCurrentLogSegment(boolean writeEndTxn) throws IOException {
    LOG.info("Ending log segment " + curSegmentTxId);
    Preconditions.checkState(isSegmentOpen(),
        "Bad state: %s", state);
    
    if (writeEndTxn) {
      logSync();
    }

    printStatistics(true);
    
    final long lastTxId = getLastWrittenTxId();
    
    try {
      journalSet.finalizeLogSegment(curSegmentTxId, lastTxId);
      editLogStream = null;
    } catch (IOException e) {
      //All journals have failed, it will be handled in logSync.
    }
    
    state = State.BETWEEN_LOG_SEGMENTS;
  }
  
  /**
   * Abort all current logs. Called from the backup node.
   */
  synchronized void abortCurrentLogSegment() {
    try {
      //Check for null, as abort can be called any time.
      if (editLogStream != null) {
        editLogStream.abort();
        editLogStream = null;
        state = State.BETWEEN_LOG_SEGMENTS;
      }
    } catch (IOException e) {
      LOG.warn("All journals failed to abort", e);
    }
  }

  /**
   * Archive any log files that are older than the given txid.
   * 
   * If the edit log is not open for write, then this call returns with no
   * effect.
   */
  @Override
  public synchronized void purgeLogsOlderThan(final long minTxIdToKeep) {
    // Should not purge logs unless they are open for write.
    // This prevents the SBN from purging logs on shared storage, for example.
    if (!isOpenForWrite()) {
      return;
    }
    
    assert curSegmentTxId == HdfsConstants.INVALID_TXID || // on format this is no-op
      minTxIdToKeep <= curSegmentTxId :
      "cannot purge logs older than txid " + minTxIdToKeep +
      " when current segment starts at " + curSegmentTxId;
    if (minTxIdToKeep == 0) {
      return;
    }
    
    // This could be improved to not need synchronization. But currently,
    // journalSet is not threadsafe, so we need to synchronize this method.
    try {
      journalSet.purgeLogsOlderThan(minTxIdToKeep);
    } catch (IOException ex) {
      //All journals have failed, it will be handled in logSync.
    }
  }

  
  /**
   * The actual sync activity happens while not synchronized on this object.
   * Thus, synchronized activities that require that they are not concurrent
   * with file operations should wait for any running sync to finish.
   */
  synchronized void waitForSyncToFinish() {
    while (isSyncRunning) {
      try {
        wait(1000);
      } catch (InterruptedException ie) {}
    }
  }

  /**
   * Return the txid of the last synced transaction.
   * For test use only
   */
  synchronized long getSyncTxId() {
    return synctxid;
  }


  // sets the initial capacity of the flush buffer.
  synchronized void setOutputBufferCapacity(int size) {
    journalSet.setOutputBufferCapacity(size);
  }

  
  synchronized void logEdit(final int length, final byte[] data) {
    long start = beginTransaction();

    try {
      editLogStream.writeRaw(data, 0, length);
    } catch (IOException ex) {
      // All journals have failed, it will be handled in logSync.
    }
    endTransaction(start);
  }

  public synchronized void recoverUnclosedStreams() {
    Preconditions.checkState(
        state == State.BETWEEN_LOG_SEGMENTS,
        "May not recover segments - wrong state: %s", state);
    try {
      journalSet.recoverUnfinalizedSegments();
    } catch (IOException ex) {
      // All journals have failed, it is handled in logSync.
      // TODO: are we sure this is OK?
    }
  }
  
  @Override
  public void selectInputStreams(Collection<EditLogInputStream> streams,
      long fromTxId, boolean inProgressOk, boolean forReading) throws IOException {
    journalSet.selectInputStreams(streams, fromTxId, inProgressOk, forReading);
  }
  
  
  

  public Collection<EditLogInputStream> selectInputStreams(
      long fromTxId, long toAtLeastTxId) throws IOException {
    return selectInputStreams(fromTxId, toAtLeastTxId,  true);
  }

  /** Select a list of input streams to load */
  public Collection<EditLogInputStream> selectInputStreams(
      long fromTxId, long toAtLeastTxId,
      boolean inProgressOk) throws IOException {
    return selectInputStreams(fromTxId, toAtLeastTxId, inProgressOk,
        true);
  }
  
  /**
   * Select a list of input streams.
   * 
   * @param fromTxId first transaction in the selected streams
   * @param toAtLeast the selected streams must contain this transaction
   * @param inProgessOk set to true if in-progress streams are OK
   * @param forReading whether or not to use the streams to load the edit log
   */
  public synchronized Collection<EditLogInputStream> selectInputStreams(
      long fromTxId, long toAtLeastTxId, 
      boolean inProgressOk, boolean forReading) throws IOException {
    List<EditLogInputStream> streams = new ArrayList<EditLogInputStream>();
    selectInputStreams(streams, fromTxId, inProgressOk, forReading);

    try {
      checkForGaps(streams, fromTxId, toAtLeastTxId, inProgressOk);
    } catch (IOException e) {
        closeAllStreams(streams);
        throw e;
    }
    return streams;
  }
  
  /**
   * Check for gaps in the edit log input stream list.
   * Note: we're assuming that the list is sorted and that txid ranges don't
   * overlap.  This could be done better and with more generality with an
   * interval tree.
   */
  private void checkForGaps(List<EditLogInputStream> streams, long fromTxId,
      long toAtLeastTxId, boolean inProgressOk) throws IOException {
    Iterator<EditLogInputStream> iter = streams.iterator();
    long txId = fromTxId;
    while (true) {
      if (txId > toAtLeastTxId) return;
      if (!iter.hasNext()) break;
      EditLogInputStream elis = iter.next();
      if (elis.getFirstTxId() > txId) break;
      long next = elis.getLastTxId();
      if (next == HdfsConstants.INVALID_TXID) {
        if (!inProgressOk) {
          throw new RuntimeException("inProgressOk = false, but " +
              "selectInputStreams returned an in-progress edit " +
              "log input stream (" + elis + ")");
        }
        // We don't know where the in-progress stream ends.
        // It could certainly go all the way up to toAtLeastTxId.
        return;
      }
      txId = next + 1;
    }
    throw new IOException(String.format("Gap in transactions. Expected to "
        + "be able to read up until at least txid %d but unable to find any "
        + "edit logs containing txid %d", toAtLeastTxId, txId));
  }

  /** 
   * Close all the streams in a collection
   * @param streams The list of streams to close
   */
 public static void closeAllStreams(Iterable<EditLogInputStream> streams) {
    for (EditLogInputStream s : streams) {
      IOUtils.closeStream(s);
    }
  }


}

