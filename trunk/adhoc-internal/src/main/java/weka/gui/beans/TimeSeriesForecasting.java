/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Time Series 
 * Forecasting.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

/*
 *    TimeSeriesForecasting.java
 *    Copyright (C) 2011 Pentaho Corporation
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JPanel;

import org.apache.commons.codec.binary.Base64;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.AbstractForecaster;
import weka.classifiers.timeseries.WekaForecaster;
import weka.classifiers.timeseries.core.TSLagMaker;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.gui.Logger;

/**
 * KnowledgeFlow component for producing a forecast using a time series
 * forecasting model.
 * 
 * @author Mark hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 * 
 */
@KFStep(category = "Time Series", toolTipText = "Forecasting using a time series model")
public class TimeSeriesForecasting extends JPanel implements BeanCommon,
    Visible, EventConstraints, EnvironmentHandler, InstanceListener,
    DataSourceListener {

  /**
   * For serialization
   */
  private static final long serialVersionUID = -7826178727365267059L;

  /** The structure of the data used to train the forecaster */
  protected transient Instances m_header;

  /** The forecaster to use for forecasting */
  protected transient WekaForecaster m_forecaster;

  /**
   * The output instances structure - typically the same as the input structure.
   * Will have additional attributes for upper and lower confidence intervals if
   * the forecaster produces them
   */
  protected transient Instances m_outgoingStructure;

  /**
   * The filename to load from - takes precendence over an encoded forecaster if
   * not null and not equal to "-NONE-"
   */
  protected String m_fileName = "-NONE-";

  /**
   * The file name to save the updated forecaster to if the user has opted to
   * rebuild the forecasting model on the incoming data
   */
  protected String m_saveFileName = "";

  /**
   * Base 64 encoded forecasting model - this allows the model to be embeded in
   * the XML knowledge flow file format rather than loaded from a file at
   * execution time.
   */
  protected String m_encodedForecaster = "-NONE-";

  /**
   * Number of future time steps to forecast - will be ignored if overlay data
   * is being used since the number of instances containing overlay values will
   * dictate the number of forecasted values that can be produced
   */
  protected String m_numberOfStepsToForecast = "1";

  /** True if the forecaster should be rebuilt on incoming data */
  protected boolean m_rebuildForecaster = false;

  /**
   * The number of time units beyond the end of the training data used to train
   * the forecaster that the most recent incoming priming instance is. This is
   * used to adjust the artificial time stamp (if one is being used) to the
   * right value before a forecast is produced
   */
  protected String m_artificialTimeStartOffset = "0";

  /** Logging object */
  protected transient Logger m_log;

  /** Environment variables */
  protected transient Environment m_env;

  /** Upstream component sending us data */
  protected Object m_listenee = null;

  /** Incoming connection type */
  protected String m_incomingConnection = "";

  /** Components listening to our output */
  protected ArrayList<InstanceListener> m_instanceListeners = new ArrayList<InstanceListener>();

  private static enum Status {
    BUSY, IDLE;
  }

  protected Status m_forecastingStatus = Status.IDLE;

  /**
   * Global about information for this component.
   * 
   * @return global information for this component
   */
  public String globalInfo() {
    return "Encapsulates a time series forecasting model and uses it to"
        + " produce forecasts given incoming historical data. Forecaster "
        + "can optionally be rebuilt using the incoming data before a "
        + "forecast is generated.";
  }

  /** Visual representation */
  protected BeanVisual m_visual = new BeanVisual("TimeSeriesForecasting",
      BeanVisual.ICON_PATH + "DefaultClassifier.gif", BeanVisual.ICON_PATH
          + "DefaultClassifier_animated.gif");

  /**
   * Constructor
   */
  public TimeSeriesForecasting() {
    setLayout(new BorderLayout());
    useDefaultVisual();
    add(m_visual, BorderLayout.CENTER);
  }

  /**
   * Use the default images for a data source
   * 
   */
  public void useDefaultVisual() {
    m_visual.loadIcons(BeanVisual.ICON_PATH + "DefaultClassifier.gif",
        BeanVisual.ICON_PATH + "DefaultClassifier_animated.gif");
  }

  /**
   * Set the logging object to use
   * 
   * @param logger the logging object to use
   */
  public void setLog(Logger logger) {
    m_log = logger;
  }

  /**
   * Get the forecaster. Loads the forecaster from a file (if necessary).
   * 
   * @return the forecasting model
   * @throws Exception if there is a problem loading the forecaster
   */
  public WekaForecaster getForecaster() throws Exception {
    if (m_forecaster != null) {
      return m_forecaster;
    } else {

      // try and decode the base64 string (if set)
      List<Object> model = getForecaster(m_encodedForecaster);
      if (model != null) {
        m_forecaster = (WekaForecaster) model.get(0);
        m_header = (Instances) model.get(1);
        return m_forecaster;
      }
    }

    return null;
  }

  /**
   * Decodes and returns a forecasting model (list containing the forecaster and
   * Instances object containing the structure of the data used to train the
   * forecaster) from a base 64 string.
   * 
   * @param base64encoded a List<Object> containing forecaster and header
   *          encoded as a base 64 string
   * 
   * @return the decoded List<Object> containing forecaster and header
   * @throws Exception if there is a problem decoding
   */
  public static List<Object> getForecaster(String base64encoded)
      throws Exception {
    if (base64encoded != null && base64encoded.length() > 0
        && !base64encoded.equals("-NONE-")) {

      byte[] decoded = decodeFromBase64(base64encoded);
      ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
      ObjectInputStream ois = new ObjectInputStream(bis);

      List<Object> model = (List<Object>) ois.readObject();
      ois.close();
      return model;
    }

    return null;
  }

  /**
   * Set the base 64 encoded forecaster.
   * 
   * @param encodedForecaster a base 64 encoded List<Object> containing the
   *          forecaster and header
   */
  public void setEncodedForecaster(String encodedForecaster) {
    m_encodedForecaster = encodedForecaster;
  }

  /**
   * Gets the base 64 encoded forecaster
   * 
   * @return a base 64 string encoding a List<Object> that contains the
   *         forecasting model and the header
   */
  public String getEncodedForecaster() {
    return m_encodedForecaster;
  }

  /**
   * Set the filename to load from.
   * 
   * @param filename the filename to load from
   */
  public void setFilename(String filename) {
    m_fileName = filename;
  }

  /**
   * Get the filename to load from.
   * 
   * @return the filename to load from.
   */
  public String getFilename() {
    return m_fileName;
  }

  /**
   * Set the name of the file to save the forecasting model out to if the user
   * has opted to rebuild the forecaster using the incoming data.
   * 
   * @param fileName the file name to save to.
   */
  public void setSaveFilename(String fileName) {
    m_saveFileName = fileName;
  }

  /**
   * Get the name of the file to save the forecasting model to if the user has
   * opted to rebuild the forecaster using the incoming data.
   * 
   * @return the name of the file to save the forecaster to.
   */
  public String getSaveFilename() {
    return m_saveFileName;
  }

  /**
   * Set whether the forecaster should be rebuilt/re-estimated on the incoming
   * data.
   * 
   * @param rebuild true if the forecaster should be rebuilt using the incoming
   *          data
   */
  public void setRebuildForecaster(boolean rebuild) {
    m_rebuildForecaster = rebuild;
  }

  /**
   * Get whether the forecaster will be rebuilt/re-estimated on the incoming
   * data.
   * 
   * @return true if the forecaster is to be rebuilt on the incoming data
   */
  public boolean getRebuildForecaster() {
    return m_rebuildForecaster;
  }

  /**
   * Returns true if, at this time, the object will accept a connection
   * according to the supplied EventSetDescriptor
   * 
   * @param esd the EventSetDescriptor
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(EventSetDescriptor esd) {
    return connectionAllowed(esd.getName());
  }

  /**
   * Returns true if, at this time, the object will accept a connection with
   * respect to the named event
   * 
   * @param eventName the event
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(String arg0) {
    if (m_listenee != null) {
      return false;
    }
    return true;
  }

  /**
   * Set the number of time steps to forecast beyond the end of the incoming
   * priming data. This will be ignored if the forecaster is using overlay data
   * as the number of instances for which overlay data is present (and targets
   * are missing) in the incoming data will determine how many forecasted values
   * are produced.
   * 
   * @param n the number of steps to forecast.
   */
  public void setNumStepsToForecast(String n) {
    m_numberOfStepsToForecast = n;
  }

  /**
   * Get the number of time steps to forecast beyond the end of the incoming
   * priming data. This will be ignored if the forecaster is using overlay data
   * as the number of instances for which overlay data is present (and targets
   * are missing) in the incoming data will determine how many forecasted values
   * are produced.
   * 
   * @return the number of steps to forecast.
   */
  public String getNumStepsToForecast() {
    return m_numberOfStepsToForecast;
  }

  /**
   * Set the offset, from the value associated with the last training instance,
   * for the artificial time stamp. Has no effect if an artificial time stamp is
   * not in use by the forecaster. If in use, this needs to be set so that the
   * forecaster knows what time stamp value corresponds to the first requested
   * forecast (i.e. it should be equal to the number of recent historical
   * priming instances that occur after the last training instance in time).
   * 
   * @param art the offset from the last artificial time value in the training
   *          data for which the forecast is requested.
   */
  public void setArtificialTimeStartOffset(String art) {
    m_artificialTimeStartOffset = art;
  }

  /**
   * Get the offset, from the value associated with the last training instance,
   * for the artificial time stamp. Has no effect if an artificial time stamp is
   * not in use by the forecaster. If in use, this needs to be set so that the
   * forecaster knows what time stamp value corresponds to the first requested
   * forecast (i.e. it should be equal to the number of recent historical
   * priming instances that occur after the last training instance in time).
   * 
   * @return the offset from the last artificial time value in the training data
   *         for which the forecast is requested.
   */
  public String getArtificialTimeStartOffset() {
    return m_artificialTimeStartOffset;
  }

  /**
   * Notify this object that it has been registered as a listener with a source
   * with respect to the named event
   * 
   * @param eventName the event
   * @param source the source with which this object has been registered as a
   *          listener
   */
  public void connectionNotification(String eventName, Object source) {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
      m_incomingConnection = eventName;
    }
  }

  /**
   * Notify this object that it has been deregistered as a listener with a
   * source with respect to the supplied event name
   * 
   * @param eventName the event
   * @param source the source with which this object has been registered as a
   *          listener
   */
  public void disconnectionNotification(String eventName, Object source) {
    if (m_listenee == source) {
      m_listenee = null;
      m_incomingConnection = "";
    }
  }

  /**
   * Get the custom (descriptive) name for this bean (if one has been set)
   * 
   * @return the custom name (or the default name)
   */
  public String getCustomName() {
    return m_visual.getText();
  }

  /**
   * Returns true if. at this time, the bean is busy with some task.
   * 
   * @return true if the bean is busy.
   */
  public boolean isBusy() {
    return (m_forecastingStatus == Status.BUSY);
  }

  /**
   * Set a custom (descriptive) name for this bean
   * 
   * @param name the name to use
   */
  public void setCustomName(String name) {
    m_visual.setText(name);
  }

  /**
   * Stop the component from executing. Also attempts to tell the immediate
   * upstream component to stop.
   */
  public void stop() {
    m_forecastingStatus = Status.IDLE;
    // tell upstream component to stop
    if (m_listenee != null) {
      if (m_listenee instanceof BeanCommon) {
        ((BeanCommon) m_listenee).stop();
      }
    }
  }

  /**
   * Set environment variables to use.
   * 
   * @param env the environment variables to use
   */
  public void setEnvironment(Environment env) {
    m_env = env;
  }

  /**
   * Gets the visual appearance of this wrapper bean
   */
  public BeanVisual getVisual() {
    return m_visual;
  }

  /**
   * Sets the visual appearance of this wrapper bean
   * 
   * @param newVisual a <code>BeanVisual</code> value
   */
  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }

  /**
   * Encode the model and header into a base 64 string. A List<Object>
   * containing first the model and then the header is encoded.
   * 
   * @param model the forecasting model to encode
   * @param header empty instances object containing just the structure of the
   *          data used to train the forecaster
   * @return a base 64 encoded String
   * @throws Exception if a problem occurs.
   */
  public static String encodeForecasterToBase64(WekaForecaster model,
      Instances header) throws Exception {

    if (model != null && header != null) {
      List<Object> modelAndHeader = new ArrayList<Object>();
      modelAndHeader.add(model);
      modelAndHeader.add(header);

      ByteArrayOutputStream bao = new ByteArrayOutputStream();
      BufferedOutputStream bos = new BufferedOutputStream(bao);
      ObjectOutputStream oo = new ObjectOutputStream(bos);
      oo.writeObject(modelAndHeader);
      oo.flush();

      byte[] modelBytes = bao.toByteArray();

      return encodeToBase64(modelBytes);
    } else {
      throw new Exception("[TimeSeriesForecasting] unable to encode model!");
    }

  }

  protected List<Object> loadModel(String filename) {

    List<Object> loaded = new ArrayList<Object>();
    try {
      if (!isEmpty(filename) && !filename.equals("-NONE-")) {

        // replace any environment variables
        if (m_env == null) {
          m_env = Environment.getSystemWide();
        }

        String filenameN = filename;
        try {
          filenameN = m_env.substitute(filename);
        } catch (Exception e) {
          // quietly ignore
        }

        InputStream is = new FileInputStream(filenameN);
        if (filenameN.toLowerCase().endsWith(".gz")) {
          is = new GZIPInputStream(is);
        }
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
            is));
        WekaForecaster forecaster = (WekaForecaster) ois.readObject();

        Instances header = (Instances) ois.readObject();
        is.close();

        loaded.add(forecaster);
        loaded.add(header);
        return loaded;
      } else {
        logMessage("Model is null or no filename specified to load from!");
        return null;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      logMessage(ex.getMessage());
      return null;
    }
  }

  /**
   * Utility method to check if a String is null or empty ("").
   * 
   * @param aString the String to check.
   * @return true if the supplied String is null or empty.
   */
  protected static boolean isEmpty(String aString) {
    if (aString == null || aString.length() == 0) {
      return true;
    }
    return false;
  }

  /**
   * Logs message to the log or System.out if the log object is not set.
   * 
   * @param message the message to log
   */
  protected void logMessage(String message) {
    message = "[TimeSeriesForecasting] " + statusMessagePrefix() + message;
    if (m_log != null) {
      m_log.logMessage(message);
    } else {
      System.out.println(message);
    }
  }

  /**
   * Logs an error message to the log or System.err if the log object is not set
   * 
   * @param message the message to log
   * @param ex the exception that generated the error (may be null)
   */
  protected void logError(String message, Exception ex) {
    String exceptionMessage = (ex != null) ? ex.getMessage() : "";
    message = "[TimeSeriesForecasting] " + statusMessagePrefix() + " "
        + message + exceptionMessage;
    if (m_log != null) {
      m_log.logMessage(message);
    } else {
      System.err.println(message);
    }
  }

  /**
   * Prints a message to the status area of the log or to System.out if the log
   * object is not set.
   * 
   * @param message the message to output
   */
  protected void statusMessage(String message) {
    message = statusMessagePrefix() + message;
    if (m_log != null) {
      m_log.statusMessage(message);
    } else {
      System.out.println(message);
    }
  }

  /**
   * Prints a fixed error message to the status area of the log
   */
  protected void statusError() {
    String message = statusMessagePrefix() + "ERROR: see log for details";
    if (m_log != null) {
      m_log.statusMessage(message);
    } else {
      System.err.println(message);
    }
  }

  /**
   * Prints a warning message to the status area of the log
   * 
   * @param message the message to log
   */
  protected void statusWarning(String message) {
    message = statusMessagePrefix() + "WARNING: " + message;
    if (m_log != null) {
      m_log.statusMessage(message);
    } else {
      System.out.println(message);
    }
  }

  protected static final String encodeToBase64(byte[] val) throws IOException {
    String string;
    if (val == null) {
      string = null;
    } else {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream gzos = new GZIPOutputStream(baos);
      BufferedOutputStream bos = new BufferedOutputStream(gzos);
      bos.write(val);
      bos.flush();
      bos.close();

      string = new String(Base64.encodeBase64(baos.toByteArray()));
    }
    return string;
  }

  protected static final byte[] decodeFromBase64(String string)
      throws Exception {

    byte[] bytes;
    if (string == null) {
      bytes = new byte[] {};
    } else {
      bytes = Base64.decodeBase64(string.getBytes());
    }
    if (bytes.length > 0) {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      GZIPInputStream gzip = new GZIPInputStream(bais);
      BufferedInputStream bi = new BufferedInputStream(gzip);
      byte[] result = new byte[] {};

      byte[] extra = new byte[1000000];
      int nrExtra = bi.read(extra);
      while (nrExtra >= 0) {
        // add it to bytes...
        //
        int newSize = result.length + nrExtra;
        byte[] tmp = new byte[newSize];
        for (int i = 0; i < result.length; i++)
          tmp[i] = result[i];
        for (int i = 0; i < nrExtra; i++)
          tmp[result.length + i] = extra[i];

        // change the result
        result = tmp;
        nrExtra = bi.read(extra);
      }
      bytes = result;
      gzip.close();
    }

    return bytes;
  }

  /**
   * Returns true, if at the current time, the named event could be generated.
   * Assumes that the supplied event name is an event that could be generated by
   * this bean
   * 
   * @param eventName the name of the event in question
   * @return true if the named event could be generated at this point in time
   */
  public boolean eventGeneratable(String eventName) {
    if (eventName.equals("instance") || eventName.equals("dataset")) {
      if (m_listenee == null) {
        return false;
      }
    }
    return true;
  }

  /** holds overlay data (if present in the incoming data) */
  protected transient Instances m_overlayData;

  /**
   * holds the incoming data for either priming the forecaster or for rebuilding
   * the forecaster
   */
  protected transient Instances m_bufferedPrimeData;

  /** true if the forecaster is using overlay data */
  protected transient boolean m_isUsingOverlayData;

  /** the lag maker in use by the forecaster */
  protected transient TSLagMaker m_modelLagMaker;

  /** name of the time stamp attribute */
  protected transient String m_timeStampName = "";

  /** the fields that the forecaster is predicting */
  protected transient List<String> m_fieldsToForecast;

  private void loadOrDecodeForecaster() {
    // filename takes precedence over encoded forecaster (if any)
    if (!isEmpty(m_fileName) && !m_fileName.equals("-NONE-")) {
      List<Object> loaded = loadModel(m_fileName);
      if (loaded == null) {
        statusError();
        logError("problem loading forecasting model.", null);
        stop();
        return;
      } else {
        m_forecaster = (WekaForecaster) loaded.get(0);
        m_header = (Instances) loaded.get(1);
      }
    } else if (m_encodedForecaster != null && m_encodedForecaster.length() > 0
        && !m_encodedForecaster.equals("-NONE-")) {
      try {
        getForecaster();
      } catch (Exception ex) {
        ex.printStackTrace();
        statusError();
        logError("a problem occurred while decoding the model. See the "
            + "log for details.", ex);
        stop();
        return;
      }
    } else {
      statusError();
      logError("unable to obtain a forecasting model to use.", null);
      stop();
      return;
    }
  }

  /**
   * Accept an incoming instance
   * 
   * @param e incoming InstanceEvent encapsulating the instance to process.
   */
  public void acceptInstance(InstanceEvent e) {

    int status = e.getStatus();
    if (status == InstanceEvent.FORMAT_AVAILABLE) {
      Instances dataset = e.getStructure();

      loadOrDecodeForecaster();

      // check the structure
      if (!m_header.equalHeaders(dataset)) {
        statusError();
        logError("incoming instances structure does not match the structure "
            + "of the data used to train the forecaster.", null);
        stop();
        return;
      }

      m_forecastingStatus = Status.BUSY;
      processInstance(null, true);

      return;
    }

    // get the instance, process it
    processInstance(e.getInstance(), false);

    if (status == InstanceEvent.BATCH_FINISHED) {
      processInstance(null, false); // finished

      // generate forecast
      generateForecast();
      m_forecastingStatus = Status.IDLE;
    }
  }

  protected void processInstance(Instance toProcess, boolean first) {

    if (first) {
      m_overlayData = null;
      m_bufferedPrimeData = null;
      statusMessage("configuring forecaster...");

      m_modelLagMaker = m_forecaster.getTSLagMaker();
      if (!m_modelLagMaker.isUsingAnArtificialTimeIndex()
          && m_modelLagMaker.getAdjustForTrends()) {
        m_timeStampName = m_modelLagMaker.getTimeStampField();
      }

      m_isUsingOverlayData = m_forecaster.isUsingOverlayData();

      if (!m_rebuildForecaster) {
        // m_isIncrementallyPrimeable = true;
        logMessage("forecaster will be primed " + "incrementally.");

        // first reset lag histories
        try {
          m_forecaster.primeForecaster(new Instances(m_header, 0));
        } catch (Exception ex) {
          ex.printStackTrace();
          statusError();
          logError("problem during initialization of the " + "priming data.",
              ex);
          stop();
          return;
        }
      } else {
        logMessage("forecaster will be "
            + "rebuilt/re-estimated on incoming data");
      }

      if (m_isUsingOverlayData) {
        logMessage("forecaster is using "
            + "overlay data. We expect to see overlay attribute "
            + "values for the forecasting period.");
        m_overlayData = new Instances(m_header, 0);
      }

      if (m_rebuildForecaster) {
        m_bufferedPrimeData = new Instances(m_header, 0);
      }

      m_fieldsToForecast = AbstractForecaster.stringToList(m_forecaster
          .getFieldsToForecast());

      // notify instance listeners of the structure (same as incoming
      // but may have confidence bounds attributes added)
      m_outgoingStructure = new Instances(m_header);
      if (m_forecaster.isProducingConfidenceIntervals()) {
        ArrayList<Attribute> atts = new ArrayList<Attribute>();
        for (int i = 0; i < m_header.numAttributes(); i++) {
          atts.add((Attribute) m_header.attribute(i).copy());
        }
        for (String f : m_fieldsToForecast) {
          Attribute lb = new Attribute(f + "_lowerBound");
          Attribute ub = new Attribute(f + "_upperBound");
          atts.add(lb);
          atts.add(ub);
        }
        m_outgoingStructure = new Instances(m_header.relationName() + "_"
            + "plus_forecast", atts, 0);
      }
      InstanceEvent ie = new InstanceEvent(this, m_outgoingStructure);
      // notify all listeners
      notifyInstanceListeners(ie);

    } else if (toProcess == null) {
      // no more input. rebuild forecaster if necessary
      if (m_rebuildForecaster && m_bufferedPrimeData.numInstances() > 0) {

        // push out the historical data first
        for (int i = 0; i < m_bufferedPrimeData.numInstances(); i++) {
          InstanceEvent ie = new InstanceEvent(this,
              m_bufferedPrimeData.instance(i), InstanceEvent.INSTANCE_AVAILABLE);
          // notify listeners here
          notifyInstanceListeners(ie);
        }

        // rebuild the forecaster
        try {
          statusMessage("rebuilding the forecasting model...");
          logMessage("rebuilding the forecasting model.");
          m_forecaster.buildForecaster(m_bufferedPrimeData);

          statusMessage("priming the forecasting model...");
          logMessage("priming the forecasting model.");
          // prime the forecaster
          m_forecaster.primeForecaster(m_bufferedPrimeData);
        } catch (Exception e) {
          e.printStackTrace();
          statusError();
          logError("a problem occurred when rebuilding the forecaster", e);
          stop();
        }
      }

      if (m_rebuildForecaster && !isEmpty(m_saveFileName)) {
        // save the forecaster
        String saveName = m_saveFileName;
        if (m_env == null) {
          m_env = Environment.getSystemWide();
        }

        try {
          saveName = m_env.substitute(saveName);
        } catch (Exception ex) {
          // quietly ignore
        }

        statusMessage("Saving rebuilt forecasting model...");
        logMessage("Saving rebuild forecasting model to \"" + saveName + "\"");
        try {
          OutputStream os = new FileOutputStream(saveName);
          if (saveName.toLowerCase().endsWith(".gz")) {
            os = new GZIPOutputStream(os);
          }
          ObjectOutputStream oos = new ObjectOutputStream(
              new BufferedOutputStream(os));
          oos.writeObject(m_forecaster);
          oos.writeObject(m_header);
          oos.close();
        } catch (IOException e) {
          statusError();
          logError("a problem occurred when trying to save rebuilt model.", e);
          stop();
        }
      }

    } else {
      // if we are expecting overlay data, then check this instance to see if
      // all
      // target values predicted by the forecaster are missing. If so, then this
      // *might* indicate the start of the overlay data. We will start buffering
      // instances into the overlay buffer. If we get an instace with all
      // non-missing targets
      // at some future point then we will flush the overlay buffer either into
      // the
      // forecaster as priming instances (if forecaster is incrementally
      // primeable)
      // or into the buffered prime/training data if forecaster is not
      // incrementally
      // primeable or we are rebuilding/re-estimating the model

      if (m_isUsingOverlayData) {
        boolean allMissing = true;
        for (String field : m_fieldsToForecast) {
          if (!toProcess.isMissing(m_header.attribute(field))) {
            allMissing = false;
            break;
          }
        }

        if (allMissing) {
          // add it to the overlay buffer
          m_overlayData.add(toProcess);
          statusMessage("buffering overlay instance");
        } else {
          // check the overlay buffer - if it's not empty then flush it
          // into either the forecaster directly (if incrementally primeable)
          // or into the priming buffer
          if (m_overlayData.numInstances() > 0) {
            // first buffer this one (will get flushed anyway)
            m_overlayData.add(toProcess);
            logMessage("encountered a supposed "
                + "overlay instance with non-missing target values - "
                + "converting buffered overlay data into "
                + (m_rebuildForecaster ? "training" : "priming") + " data...");
            statusMessage("flushing overlay buffer.");

            for (int i = 0; i < m_overlayData.numInstances(); i++) {
              if (!m_rebuildForecaster) {
                try {
                  m_forecaster.primeForecasterIncremental(m_overlayData
                      .instance(i));

                  // output this instance immediately (make sure that we include
                  // any attributes for confidence intervals - these will be
                  // necessarily missing for historical instances)
                  Instance outgoing = convertToOutputFormat(m_overlayData
                      .instance(i));
                  InstanceEvent ie = new InstanceEvent(this, outgoing,
                      InstanceEvent.INSTANCE_AVAILABLE);
                  // notify listeners here
                  notifyInstanceListeners(ie);
                } catch (Exception e) {
                  e.printStackTrace();
                  statusError();
                  logError("problem occurred during priming.", e);
                  stop();
                  return;
                }

              } else {
                // transfer to the priming buffer
                m_bufferedPrimeData.add(m_overlayData.instance(i));
              }
            }

            // clear out the overlay data
            m_overlayData = new Instances(m_header, 0);
          } else {
            // not all missing and overlay buffer is empty then it's a priming
            // instance

            // either buffer it or send it directly to the forecaster (if
            // incrementally
            // primeable
            if (!m_rebuildForecaster) {
              try {
                m_forecaster.primeForecasterIncremental(toProcess);

                // output this instance immediately (make sure that we include
                // any attributes for confidence intervals - these will be
                // necessarily missing for historical instances)
                Instance outgoing = convertToOutputFormat(toProcess);
                InstanceEvent ie = new InstanceEvent(this, outgoing,
                    InstanceEvent.INSTANCE_AVAILABLE);
                // tell listeners
                notifyInstanceListeners(ie);
              } catch (Exception ex) {
                ex.printStackTrace();
                statusError();
                logError("problem occurred during priming.", ex);
                stop();
                return;
              }
            } else {
              // buffer
              m_bufferedPrimeData.add(toProcess);
            }
          }

        }
      } else {
        // not using overlay data
        if (!m_rebuildForecaster) {
          try {
            m_forecaster.primeForecasterIncremental(toProcess);

            // output this instance immediately (make sure that we include
            // any attributes for confidence intervals - these will be
            // necessarily missing for historical instances)
            Instance outgoing = convertToOutputFormat(toProcess);
            InstanceEvent ie = new InstanceEvent(this, outgoing,
                InstanceEvent.INSTANCE_AVAILABLE);
            notifyInstanceListeners(ie);
          } catch (Exception ex) {
            ex.printStackTrace();
            statusError();
            logError("problem occurred during priming.", ex);
            stop();
            return;
          }
        } else {
          // buffer
          m_bufferedPrimeData.add(toProcess);
        }
      }
    }
  }

  /**
   * Accept an incoming data set
   * 
   * @param dse incoming DataSetEvent encapsulating the instances to process
   */
  public void acceptDataSet(DataSetEvent dse) {

    loadOrDecodeForecaster();

    Instances data = dse.getDataSet();
    if (!m_header.equalHeaders(data)) {
      statusError();
      logError(
          "Incoming instance structure does not match what the forecaster "
              + "was trained with", null);
      return;
    }

    if (dse.isStructureOnly()) {
      return;
    }

    m_forecastingStatus = Status.BUSY;
    processInstance(null, true);

    for (int i = 0; i < data.numInstances(); i++) {
      processInstance(data.instance(i), false);
    }

    processInstance(null, false); // finished

    // generate forecast
    generateForecast();

    m_forecastingStatus = Status.IDLE;
  }

  private void generateForecast() {

    // doesn't matter if we're not using a time stamp
    double lastTimeFromPrime = -1;

    if (m_modelLagMaker.getAdjustForTrends()
        && m_modelLagMaker.getTimeStampField() != null
        && m_modelLagMaker.getTimeStampField().length() > 0
        && !m_modelLagMaker.isUsingAnArtificialTimeIndex()) {
      try {
        lastTimeFromPrime = m_modelLagMaker.getCurrentTimeStampValue();
      } catch (Exception ex) {
        statusError();
        logError("a problem occurred while establishing the current "
            + "time stamp value", ex);
        stop();
        return;
      }
    } else if (m_modelLagMaker.getAdjustForTrends()
        && m_modelLagMaker.isUsingAnArtificialTimeIndex()) {

      // If an artificial time stamp is in use then we need to set the
      // initial value to whatever offset from training that the user has
      // indicated to be the first forecasted point.
      try {
        String artOff = m_artificialTimeStartOffset;
        if (m_env != null) {
          artOff = m_env.substitute(artOff);
        }
        double artificialStartValue = m_modelLagMaker
            .getArtificialTimeStartValue();
        artificialStartValue += Integer.parseInt(artOff);
        m_modelLagMaker.setArtificialTimeStartValue(artificialStartValue);
      } catch (Exception ex) {
        ex.printStackTrace();
        statusError();
        logError("unable to set the value of the artificial time stamp.", ex);
        stop();
        return;
      }
    }

    boolean overlay = (m_overlayData != null
        && m_overlayData.numInstances() > 0 && m_isUsingOverlayData);

    String numS = m_numberOfStepsToForecast;
    if (m_env != null) {
      try {
        numS = m_env.substitute(numS);
      } catch (Exception e) {
        // quietly ignore
        // e.printStackTrace();
      }
    }

    int numSteps = (overlay) ? m_overlayData.numInstances() : Integer
        .parseInt(numS);

    List<List<NumericPrediction>> forecast = null;

    // TODO adapt the log to PrintStream for the forecasting methods
    try {
      if (overlay) {
        forecast = m_forecaster.forecast(numSteps, m_overlayData);
      } else {
        forecast = m_forecaster.forecast(numSteps);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      statusError();
      logError("unable to generate a forecast.", ex);
      stop();
      return;
    }

    // now convert the forecast into instances. If we have overlay
    // data then we can just fill in the forecasted values (and
    // potentially add for confidence intervals)
    double time = lastTimeFromPrime;
    int timeStampIndex = -1;
    if (m_timeStampName.length() > 0) {
      Attribute timeStampAtt = m_outgoingStructure.attribute(m_timeStampName);
      if (timeStampAtt == null) {
        statusError();
        logError("couldn't find time stamp: " + m_timeStampName
            + "in the input data", null);
        stop();
        return;
      }
      timeStampIndex = timeStampAtt.index();
    }

    statusMessage("Generating forecast...");
    logMessage("Generating forecast.");
    for (int i = 0; i < numSteps; i++) {
      Instance outputI = null;
      double[] outVals = new double[m_outgoingStructure.numAttributes()];
      for (int j = 0; j < outVals.length; j++) {
        if (overlay) {
          outVals[j] = m_overlayData.instance(i).value(j);
        } else {
          outVals[j] = Utils.missingValue();
        }
      }
      List<NumericPrediction> predsForStep = forecast.get(i);

      if (timeStampIndex != -1) {
        // set time value
        time = m_modelLagMaker.advanceSuppliedTimeValue(time);
        outVals[timeStampIndex] = time;
      }

      for (int j = 0; j < m_fieldsToForecast.size(); j++) {
        String target = m_fieldsToForecast.get(j);
        int targetI = m_outgoingStructure.attribute(target).index();
        NumericPrediction predForTargetAtStep = predsForStep.get(j);
        double y = predForTargetAtStep.predicted();
        double yHigh = y;
        double yLow = y;
        double[][] conf = predForTargetAtStep.predictionIntervals();
        if (!Utils.isMissingValue(y)) {
          outVals[targetI] = y;
        }

        // any confidence bounds?
        if (conf.length > 0) {
          yLow = conf[0][0];
          yHigh = conf[0][1];
          int indexOfLow = m_outgoingStructure
              .attribute(target + "_lowerBound").index();
          int indexOfHigh = m_outgoingStructure.attribute(
              target + "_upperBound").index();
          outVals[indexOfLow] = yLow;
          outVals[indexOfHigh] = yHigh;
        }
      }
      outputI = new DenseInstance(1.0, outVals);
      outputI.setDataset(m_outgoingStructure);

      // notify listeners of output instance
      InstanceEvent ie = new InstanceEvent(this, outputI,
          InstanceEvent.INSTANCE_AVAILABLE);

      if (i == (numSteps - 1)) {
        ie.setStatus(InstanceEvent.BATCH_FINISHED);
      }
      notifyInstanceListeners(ie);
    }
    statusMessage("Finished.");
    logMessage("Finished. Generated " + numSteps + " forecasted values.");
  }

  private String statusMessagePrefix() {
    return getCustomName()
        + "$"
        + hashCode()
        + "|"
        + ((Utils.joinOptions(m_forecaster.getOptions()).length() > 0) ? Utils
            .joinOptions(m_forecaster.getOptions()) + "|" : "");
  }

  private Instance convertToOutputFormat(Instance incoming) {

    Instance output = (Instance) incoming.copy();
    if (m_forecaster.isProducingConfidenceIntervals()) {
      double[] values = new double[incoming.numAttributes()
          + (m_fieldsToForecast.size() * 2)];
      for (int i = 0; i < incoming.numAttributes(); i++) {
        values[i] = incoming.value(i);
      }

      // set all bounds to missing (initially)
      for (int i = incoming.numAttributes(); i < incoming.numAttributes()
          + (m_fieldsToForecast.size() * 2); i++) {
        values[i] = Utils.missingValue();
      }

      output = new DenseInstance(1.0, values);
    }
    output.setDataset(m_outgoingStructure);

    return output;
  }

  private void notifyInstanceListeners(InstanceEvent e) {
    ArrayList<InstanceListener> l = null;
    synchronized (this) {
      l = (ArrayList<InstanceListener>) m_instanceListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        l.get(i).acceptInstance(e);
      }
    }
  }

  /**
   * Add an listener to be notified of outgoing InstanceEvents
   * 
   * @param l the listener to register to receive outging instance events
   */
  public synchronized void addInstanceListener(InstanceListener l) {
    m_instanceListeners.add(l);
  }

  /**
   * Deregister and remove a listener of InstanceEvents
   * 
   * @param l the listener to remove.
   */
  public synchronized void removeInstanceListener(InstanceListener l) {
    m_instanceListeners.remove(l);
  }
}
