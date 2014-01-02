package com.alimama.mdrill.editlog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alimama.mdrill.editlog.defined.CheckableNameNodeResource;
import com.alimama.mdrill.editlog.defined.JournalManager;
import com.alimama.mdrill.editlog.read.EditLogInputStream;
import com.alimama.mdrill.editlog.read.FSEditLogOp;
import com.alimama.mdrill.editlog.read.NameNodeResourcePolicy;
import com.alimama.mdrill.editlog.read.RedundantEditLogInputStream;
import com.alimama.mdrill.editlog.write.EditLogOutputStream;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

public class JournalSet implements JournalManager {

  static final Log LOG = LogFactory.getLog(JournalSet.class);
  
  static final public Comparator<EditLogInputStream>
    EDIT_LOG_INPUT_STREAM_COMPARATOR = new Comparator<EditLogInputStream>() {
      @Override
      public int compare(EditLogInputStream a, EditLogInputStream b) {
        return ComparisonChain.start().
          compare(a.getFirstTxId(), b.getFirstTxId()).
          compare(b.getLastTxId(), a.getLastTxId()).
          result();
      }
    };
  

  public static class JournalAndStream implements CheckableNameNodeResource {
    private final JournalManager journal;
    private boolean disabled = false;
    private EditLogOutputStream stream;
    private boolean required = false;
    
    public JournalAndStream(JournalManager manager, boolean required) {
      this.journal = manager;
      this.required = required;
    }
    
    /**
     * Should be used outside JournalSet only for testing.
     */
    EditLogOutputStream getCurrentStream() {
      return stream;
    }


    public void startLogSegment(long txId) throws IOException {
      Preconditions.checkState(stream == null);
      disabled = false;
      stream = journal.startLogSegment(txId);
    }

    /**
     * Closes the stream, also sets it to null.
     */
    public void closeStream() throws IOException {
      if (stream == null) return;
      stream.close();
      stream = null;
    }

    /**
     * Close the Journal and Stream
     */
    public void close() throws IOException {
      closeStream();

      journal.close();
    }
    
    /**
     * Aborts the stream, also sets it to null.
     */
    public void abort() {
      if (stream == null) return;
      try {
        stream.abort();
      } catch (IOException ioe) {
        LOG.error("Unable to abort stream " + stream, ioe);
      }
      stream = null;
    }

    boolean isActive() {
      return stream != null;
    }
    
    
    @Override
    public String toString() {
      return "JournalAndStream(mgr=" + journal +
        ", " + "stream=" + stream + ")";
    }

    
    JournalManager getManager() {
      return journal;
    }

    boolean isDisabled() {
      return disabled;
    }

    private void setDisabled(boolean disabled) {
      this.disabled = disabled;
    }
    
    @Override
    public boolean isResourceAvailable() {
      return !isDisabled();
    }
    
    @Override
    public boolean isRequired() {
      return required;
    }
  }
 

  private List<JournalAndStream> journals =
      new CopyOnWriteArrayList<JournalSet.JournalAndStream>();
  final int minimumRedundantJournals;
  
  public JournalSet(int minimumRedundantResources) {
    this.minimumRedundantJournals = minimumRedundantResources;
  }
    
  @Override
  public EditLogOutputStream startLogSegment(final long txId) throws IOException {
    mapJournalsAndReportErrors(new JournalClosure() {
      @Override
      public void apply(JournalAndStream jas) throws IOException {
        jas.startLogSegment(txId);
      }
    }, "starting log segment " + txId);
    return new JournalSetOutputStream();
  }
  
  @Override
  public void finalizeLogSegment(final long firstTxId, final long lastTxId)
      throws IOException {
    mapJournalsAndReportErrors(new JournalClosure() {
      @Override
      public void apply(JournalAndStream jas) throws IOException {
        if (jas.isActive()) {
          jas.closeStream();
          jas.getManager().finalizeLogSegment(firstTxId, lastTxId);
        }
      }
    }, "finalize log segment " + firstTxId + ", " + lastTxId);
  }
   
  @Override
  public void close() throws IOException {
    mapJournalsAndReportErrors(new JournalClosure() {
      @Override
      public void apply(JournalAndStream jas) throws IOException {
        jas.close();
      }
    }, "close journal");
  }

  /**
   * In this function, we get a bunch of streams from all of our JournalManager
   * objects.  Then we add these to the collection one by one.
   * 
   * @param streams          The collection to add the streams to.  It may or 
   *                         may not be sorted-- this is up to the caller.
   * @param fromTxId         The transaction ID to start looking for streams at
   * @param inProgressOk     Should we consider unfinalized streams?
   * @param forReading       Whether or not the caller intends to read from
   *                         the returned streams.
   */
  @Override
  public void selectInputStreams(Collection<EditLogInputStream> streams,
      long fromTxId, boolean inProgressOk, boolean forReading) throws IOException {
    final PriorityQueue<EditLogInputStream> allStreams = 
        new PriorityQueue<EditLogInputStream>(64,
            EDIT_LOG_INPUT_STREAM_COMPARATOR);
    for (JournalAndStream jas : journals) {
      if (jas.isDisabled()) {
        LOG.info("Skipping jas " + jas + " since it's disabled");
        continue;
      }
      try {
        jas.getManager().selectInputStreams(allStreams, fromTxId, inProgressOk,
            forReading);
      } catch (IOException ioe) {
        LOG.warn("Unable to determine input streams from " + jas.getManager() +
            ". Skipping.", ioe);
      }
    }
    chainAndMakeRedundantStreams(streams, allStreams, fromTxId);
  }
  
  public static void chainAndMakeRedundantStreams(
      Collection<EditLogInputStream> outStreams,
      PriorityQueue<EditLogInputStream> allStreams, long fromTxId) {
    LinkedList<EditLogInputStream> acc =
        new LinkedList<EditLogInputStream>();
    EditLogInputStream elis;
    while ((elis = allStreams.poll()) != null) {
      if (acc.isEmpty()) {
        acc.add(elis);
      } else {
        long accFirstTxId = acc.get(0).getFirstTxId();
        if (accFirstTxId == elis.getFirstTxId()) {
          acc.add(elis);
        } else if (accFirstTxId < elis.getFirstTxId()) {
          outStreams.add(new RedundantEditLogInputStream(acc, fromTxId));
          acc.clear();
          acc.add(elis);
        } else if (accFirstTxId > elis.getFirstTxId()) {
          throw new RuntimeException("sorted set invariants violated!  " +
              "Got stream with first txid " + elis.getFirstTxId() +
              ", but the last firstTxId was " + accFirstTxId);
        }
      }
    }
    if (!acc.isEmpty()) {
      outStreams.add(new RedundantEditLogInputStream(acc, fromTxId));
      acc.clear();
    }
  }

  /**
   * Returns true if there are no journals, all redundant journals are disabled,
   * or any required journals are disabled.
   * 
   * @return True if there no journals, all redundant journals are disabled,
   * or any required journals are disabled.
   */
  public boolean isEmpty() {
    return !NameNodeResourcePolicy.areResourcesAvailable(journals,
        minimumRedundantJournals);
  }
  
  /**
   * Called when some journals experience an error in some operation.
   */
  private void disableAndReportErrorOnJournals(List<JournalAndStream> badJournals) {
    if (badJournals == null || badJournals.isEmpty()) {
      return; // nothing to do
    }
 
    for (JournalAndStream j : badJournals) {
      LOG.error("Disabling journal " + j);
      j.abort();
      j.setDisabled(true);
    }
  }

  /**
   * Implementations of this interface encapsulate operations that can be
   * iteratively applied on all the journals. For example see
   * {@link JournalSet#mapJournalsAndReportErrors}.
   */
  private interface JournalClosure {
    /**
     * The operation on JournalAndStream.
     * @param jas Object on which operations are performed.
     * @throws IOException
     */
    public void apply(JournalAndStream jas) throws IOException;
  }
  
  /**
   * Apply the given operation across all of the journal managers, disabling
   * any for which the closure throws an IOException.
   * @param closure {@link JournalClosure} object encapsulating the operation.
   * @param status message used for logging errors (e.g. "opening journal")
   * @throws IOException If the operation fails on all the journals.
   */
  private void mapJournalsAndReportErrors(
      JournalClosure closure, String status) throws IOException{

    List<JournalAndStream> badJAS = Lists.newLinkedList();
    for (JournalAndStream jas : journals) {
      try {
        closure.apply(jas);
      } catch (Throwable t) {
        if (jas.isRequired()) {
          final String msg = "Error: " + status + " failed for required journal ("
            + jas + ")";
          LOG.fatal(msg, t);
          // If we fail on *any* of the required journals, then we must not
          // continue on any of the other journals. Abort them to ensure that
          // retry behavior doesn't allow them to keep going in any way.
          abortAllJournals();
          // the current policy is to shutdown the NN on errors to shared edits
          // dir. There are many code paths to shared edits failures - syncs,
          // roll of edits etc. All of them go through this common function 
          // where the isRequired() check is made. Applying exit policy here 
          // to catch all code paths.
          FSEditLog.terminate(1, msg);
        } else {
          LOG.error("Error: " + status + " failed for (journal " + jas + ")", t);
          badJAS.add(jas);          
        }
      }
    }
    disableAndReportErrorOnJournals(badJAS);
    if (!NameNodeResourcePolicy.areResourcesAvailable(journals,
        minimumRedundantJournals)) {
      String message = status + " failed for too many journals";
      LOG.error("Error: " + message);
      throw new IOException(message);
    }
  }
  
  /**
   * Abort all of the underlying streams.
   */
  private void abortAllJournals() {
    for (JournalAndStream jas : journals) {
      if (jas.isActive()) {
        jas.abort();
      }
    }
  }

  /**
   * An implementation of EditLogOutputStream that applies a requested method on
   * all the journals that are currently active.
   */
  private class JournalSetOutputStream extends EditLogOutputStream {

    JournalSetOutputStream() throws IOException {
      super();
    }

    @Override
    public void write(final FSEditLogOp op)
        throws IOException {
      mapJournalsAndReportErrors(new JournalClosure() {
        @Override
        public void apply(JournalAndStream jas) throws IOException {
          if (jas.isActive()) {
            jas.getCurrentStream().write(op);
          }
        }
      }, "write op");
    }

    @Override
    public void writeRaw(final byte[] data, final int offset, final int length)
        throws IOException {
      mapJournalsAndReportErrors(new JournalClosure() {
        @Override
        public void apply(JournalAndStream jas) throws IOException {
          if (jas.isActive()) {
            jas.getCurrentStream().writeRaw(data, offset, length);
          }
        }
      }, "write bytes");
    }

    @Override
    public void create() throws IOException {
      mapJournalsAndReportErrors(new JournalClosure() {
        @Override
        public void apply(JournalAndStream jas) throws IOException {
          if (jas.isActive()) {
            jas.getCurrentStream().create();
          }
        }
      }, "create");
    }

    @Override
    public void close() throws IOException {
      mapJournalsAndReportErrors(new JournalClosure() {
        @Override
        public void apply(JournalAndStream jas) throws IOException {
          jas.closeStream();
        }
      }, "close");
    }

    @Override
    public void abort() throws IOException {
      mapJournalsAndReportErrors(new JournalClosure() {
        @Override
        public void apply(JournalAndStream jas) throws IOException {
          jas.abort();
        }
      }, "abort");
    }

    @Override
    public void setReadyToFlush() throws IOException {
      mapJournalsAndReportErrors(new JournalClosure() {
        @Override
        public void apply(JournalAndStream jas) throws IOException {
          if (jas.isActive()) {
            jas.getCurrentStream().setReadyToFlush();
          }
        }
      }, "setReadyToFlush");
    }

    @Override
    public void flushAndSync(final boolean durable) throws IOException {
      mapJournalsAndReportErrors(new JournalClosure() {
        @Override
        public void apply(JournalAndStream jas) throws IOException {
          if (jas.isActive()) {
            jas.getCurrentStream().flushAndSync(durable);
          }
        }
      }, "flushAndSync");
    }
    
    @Override
    public void flush() throws IOException {
      mapJournalsAndReportErrors(new JournalClosure() {
        @Override
        public void apply(JournalAndStream jas) throws IOException {
          if (jas.isActive()) {
            jas.getCurrentStream().flush();
          }
        }
      }, "flush");
    }
    

    
    @Override
    public long getNumSync() {
      for (JournalAndStream jas : journals) {
        if (jas.isActive()) {
          return jas.getCurrentStream().getNumSync();
        }
      }
      return 0;
    }
  }

  @Override
  public void setOutputBufferCapacity(final int size) {
    try {
      mapJournalsAndReportErrors(new JournalClosure() {
        @Override
        public void apply(JournalAndStream jas) throws IOException {
            jas.getManager().setOutputBufferCapacity(size);
        }
      }, "setOutputBufferCapacity");
    } catch (IOException e) {
      LOG.error("Error in setting outputbuffer capacity");
    }
  }
  
  public List<JournalAndStream> getAllJournalStreams() {
    return journals;
  }

  public List<JournalManager> getJournalManagers() {
    List<JournalManager> jList = new ArrayList<JournalManager>();
    for (JournalAndStream j : journals) {
      jList.add(j.getManager());
    }
    return jList;
  }

  public void add(JournalManager j, boolean required) {
    JournalAndStream jas = new JournalAndStream(j, required);
    journals.add(jas);
  }
  
  void remove(JournalManager j) {
    JournalAndStream jasToRemove = null;
    for (JournalAndStream jas: journals) {
      if (jas.getManager().equals(j)) {
        jasToRemove = jas;
        break;
      }
    }
    if (jasToRemove != null) {
      jasToRemove.abort();
      journals.remove(jasToRemove);
    }
  }

  @Override
  public void purgeLogsOlderThan(final long minTxIdToKeep) throws IOException {
    mapJournalsAndReportErrors(new JournalClosure() {
      @Override
      public void apply(JournalAndStream jas) throws IOException {
        jas.getManager().purgeLogsOlderThan(minTxIdToKeep);
      }
    }, "purgeLogsOlderThan " + minTxIdToKeep);
  }

  @Override
  public void recoverUnfinalizedSegments() throws IOException {
    mapJournalsAndReportErrors(new JournalClosure() {
      @Override
      public void apply(JournalAndStream jas) throws IOException {
        jas.getManager().recoverUnfinalizedSegments();
      }
    }, "recoverUnfinalizedSegments");
  }
  
 
  String getSyncTimes() {
    StringBuilder buf = new StringBuilder();
    for (JournalAndStream jas : journals) {
      if (jas.isActive()) {
        buf.append(jas.getCurrentStream().getTotalSyncTime());
        buf.append(" ");
      }
    }
    return buf.toString();
  }
}

