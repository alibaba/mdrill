package com.alimama.quanjingmonitor.mdrillImport.reader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.tt.exception.TTQueueException;
import com.alibaba.tt.exception.TTQueueSafemodeException;
import com.alibaba.tt.exception.TTSubChangedException;
import com.alibaba.tt.log.TTLog;
import com.alibaba.tt.log.TTLogInput;
import com.alibaba.tt.log.impl.TTLogBlock;
import com.alibaba.tt.log.impl.TTLogImpl;
import com.alibaba.tt.log.impl.TTLogSimpleInput;
import com.alibaba.tt.queue.TTQueueCluster;
import com.alibaba.tt.queue.impl.MessageKey;
import com.alibaba.tt.queue.impl.TTQueueClusterImpl;
import com.alimama.quanjingmonitor.mdrillImport.ImportReader.RawDataReader;
import com.alimama.quanjingmonitor.tt.DynaClassLoader;

public class TT4Reader extends RawDataReader {
	static {
		String stormhome = System.getProperty("storm.home");
		if (stormhome == null) {
			stormhome = ".";
		}
		File[] ff = { new File(stormhome + "/lib", "libthrift-0.8.0.jar") };
		DynaClassLoader cld = new DynaClassLoader(Thread.currentThread()
				.getContextClassLoader());
		cld.addEtries(ff);
		Thread.currentThread().setContextClassLoader(cld);
	}
	private static final Log LOG = LogFactory.getLog(TT4Reader.class.getName());


	private TTLog _log = null;
	private TTLogInput _inStreamInput = null;
	private long _reseekTimestamp;
	private boolean closed = false;

	public void init(Map config, String confPrefix, int readerIndex,
			int readerCount) throws IOException {
		String logname = (String) config.get(confPrefix + "-log");
		String accesskey = (String) config.get(confPrefix + "-accesskey");
		String subid = (String) config.get(confPrefix + "-subid");
		LOG.debug("Opening TT connection logname=" + logname + ", accesskey="
				+ accesskey + ", subid=" + subid);

		try {
			Date curTime;
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMddHHmmss");
			Object startTime = config.get(confPrefix + "-start-time");
			if (startTime == null) {
				curTime = new Date(System.currentTimeMillis());
			} else {
				curTime = formatDate.parse((String) startTime);
			}
			_log = new TTLogImpl(logname, subid, accesskey);
			if (_inStreamInput != null) {
				_inStreamInput.close();
			}

			TTQueueCluster qc = new TTQueueClusterImpl(_log.getName(),
					_log.getSubid(), _log.getFilter(), _log.getAccesskey());
			int partitionCount = qc.getQueues().length;
			int[][] inputIndicesList = this.groupIntegers(partitionCount,
					readerCount);
			int[] inputIndices = inputIndicesList[readerIndex];

			LOG.info("TimeTunnel init prepare partitionCount:" + partitionCount
					+ ", readerCount:" + readerCount + ", readerIndex:"
					+ readerIndex + "," + String.valueOf(inputIndices));
			_inStreamInput = new TTLogSimpleInput(_log, curTime, inputIndices);
		} catch (Throwable e) {
			LOG.error("TimeTunnel open fail ", e);

			throw new IOException("parse timestamp failed...., ts="
					+ this._reseekTimestamp, e);
		}
		LOG.info("TimeTunnel query client opened");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					close();
				} catch (IOException ioe) {
					LOG.warn("Closing TimeTunnel query client", ioe);
				}
			}
		});

	}

    private final static int NEW_ENTRIES_COUNT = 20;

	
	@Override
	public List<String> read() throws IOException {

		TTLogBlock block = null;
		try {
			block = _inStreamInput.read();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (TTQueueException e) {
			e.printStackTrace();
		} catch (TTQueueSafemodeException e) {
			e.printStackTrace();
		} catch (TTSubChangedException e) {
			e.printStackTrace();
		}
		if (block == null) {
			return null;
		}
		// ack
		MessageKey key = block.getKey();
		key.ack();

		List<String> list = new ArrayList<String>(NEW_ENTRIES_COUNT);
		String buffer = new String(block.getBuffer());
		for (String mesgStr : buffer.split("[\n\r]", -1)) {
			list.add(mesgStr);
		}
		return list;

	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			_inStreamInput.close();
			LOG.info("TimeTunnel query client closed");
		}
	}

	public int[][] groupIntegers(int total, int groupCount) {
		int[][] result = new int[groupCount][];
		int quotient = total / groupCount, remainder = total % groupCount;

		int[] tails = new int[groupCount];
		for (int i = 0; i < groupCount; i++) {
			result[i] = new int[quotient + (i < remainder ? 1 : 0)];
			tails[i] = 0;
		}

		int k = 0;
		for (int i = 0; i < quotient; i++)
			for (int j = 0; j < groupCount; j++) {
				int offset = tails[j]++;
				result[j][offset] = k++;
			}

		for (int i = 0; i < remainder; i++) {
			int offset = tails[i]++;
			result[i][offset] = k++;
		}

		return result;
	}

}
