package com.alimama.mdrill.index.utils;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.util.Progressable;

/**
 * This class runs a background thread that once ever 5 seconds checks to see if
 * a progress report is needed. If a report is needed it is issued.
 * 
 * A simple counter {@link #threadsNeedingHeartBeat} handles the number of
 * threads requesting a heart beat.
 * 
 * The expected usage pattern is
 * 
 * <pre>
 *  try {
 *       heartBeater.needHeartBeat();
 *       do something that may take a while
 *    } finally {
 *       heartBeater.cancelHeartBeat();
 *    }
 * </pre>
 * 
 * 
 */
public class HeartBeater extends Thread {
  public static Log LOG = LogFactory.getLog(HeartBeater.class);

  /**
   * count of threads asking for heart beat, at 0 no heart beat done. This could
   * be an atomic long but then missmatches in need/cancel could result in
   * negative counts.
   */
  volatile int threadsNeedingHeartBeat = 0;

  Progressable progress;

  /**
   * The amount of time to wait between checks for the need to issue a heart
   * beat. In milliseconds.
   */
  long waitTimeMs = TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS);

  public Progressable getProgress() {
    return progress;
  }

  /**
   * Create the heart beat object thread set it to daemon priority and start the
   * thread. When the count in {@link #threadsNeedingHeartBeat} is positive, the
   * heart beat will be issued on the progress object ever 5 seconds.
   */
  public HeartBeater(Progressable progress) {
    setDaemon(true);
    this.progress = progress;
    LOG.info("Heart beat reporting class is " + progress.getClass().getName());
    start();
  }

  public void setProgress(Progressable progress) {
    this.progress = progress;
  }

  @Override
  public void run() {
    LOG.info("HeartBeat thread running");
    while (true) {
      try {
        synchronized (this) {
          if (threadsNeedingHeartBeat > 0) {
            progress.progress();
            if (LOG.isInfoEnabled()) {
              LOG.info(String.format("Issuing heart beat for %d threads",
                  threadsNeedingHeartBeat));
            }
          } else {
            if (LOG.isInfoEnabled()) {
              LOG.info(String.format("heartbeat skipped count %d",
                  threadsNeedingHeartBeat));
            }
          }
          this.wait(waitTimeMs);//wait 5 s
        }
      } catch (Throwable e) {
        LOG.error("HeartBeat throwable", e);
      }
    }
  }

  /**
   * inform the background thread that heartbeats are to be issued. Issue a
   * heart beat also
   */
  public synchronized void needHeartBeat() {
    threadsNeedingHeartBeat++;
    // Issue a progress report right away,
    // just in case the the cancel comes before the background thread issues a
    // report.
    // If enough cases like this happen the 600 second timeout can occur
    progress.progress();
    if (threadsNeedingHeartBeat == 1) {
      // this.notify(); // wake up the heartbeater
    }
  }

  /**
   * inform the background thread that this heartbeat request is not needed.
   * This must be called at some point after each {@link #needHeartBeat()}
   * request.
   */
  public synchronized void cancelHeartBeat() {
    if (threadsNeedingHeartBeat > 0) {
      threadsNeedingHeartBeat--;
    } else {
      Exception e = new Exception("Dummy");
      e.fillInStackTrace();
      LOG.warn("extra call to cancelHeartBeat", e);
    }
  }

  public void setStatus(String status) {
    if (progress instanceof TaskInputOutputContext) {
      ((TaskInputOutputContext) progress).setStatus(status);
    }
  }
}
