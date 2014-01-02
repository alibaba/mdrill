package com.alimama.mdrill.editlog.read;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alimama.mdrill.editlog.defined.HdfsConstants;




public class FSEditLogValidate {
    private static final Log LOG = LogFactory.getLog(FSEditLogValidate.class);
	public static class EditLogValidation {
		    private final long endTxId;
		    private final boolean hasCorruptHeader;

		   public EditLogValidation( long endTxId,
		        boolean hasCorruptHeader) {
		      this.endTxId = endTxId;
		      this.hasCorruptHeader = hasCorruptHeader;
		    }


		    public long getEndTxId() { return endTxId; }

		    public boolean hasCorruptHeader() { return hasCorruptHeader; }
		  }
	 
	 public static EditLogValidation validateEditLog(EditLogInputStream in) {
		    long lastTxId = HdfsConstants.INVALID_TXID;
		    long numValid = 0;
		    FSEditLogOp op = null;
		    while (true) {
		      try {
		        if ((op = in.readOp()) == null) {
		          break;
		        }
		      } catch (Throwable t) {
		        LOG.warn("Caught exception after reading " + numValid +
		            " ops from " + in + " while determining its valid length." +
		            "Position was " , t);
		        break;
		      }
		      if (lastTxId == HdfsConstants.INVALID_TXID
		          || op.getTransactionId() > lastTxId) {
		        lastTxId = op.getTransactionId();
		      }
		      numValid++;
		    }
		    return new EditLogValidation(lastTxId, false);
		  }
	
}

