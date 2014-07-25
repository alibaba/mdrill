/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 * 
 * ---------------
 * DefaultLog.java
 * ---------------
 * (C) Copyright 2004, by Object Refinery Limited.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: DefaultLog.java,v 1.9 2006/02/19 21:10:48 taqua Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.base.log;

import org.jfree.util.Log;
import org.jfree.util.LogTarget;
import org.jfree.util.PrintStreamLogTarget;

/**
 * A default log implementation. The Log class defines how to create Logger-contexts
 * and how to forward messages to the logtargets.
 *
 * @author Thomas Morgner
 */
public class DefaultLog extends Log {

    /** The default log target. */
    private static final PrintStreamLogTarget DEFAULT_LOG_TARGET =
          new PrintStreamLogTarget();

    /** The default log instance. */
    private static final DefaultLog defaultLogInstance;

    /**
     * Creates a new log.
     */
    protected DefaultLog () {
        // nothing required
    }

    static {
        defaultLogInstance = new DefaultLog();
        defaultLogInstance.addTarget(DEFAULT_LOG_TARGET);
        try {
            // check the system property. This is the developers backdoor to activate
            // debug output as soon as possible.
          final String property = System.getProperty("org.jfree.DebugDefault", "false");
          if (Boolean.valueOf(property).booleanValue()) {
              defaultLogInstance.setDebuglevel(LogTarget.DEBUG);
          }
          else {
              defaultLogInstance.setDebuglevel(LogTarget.WARN);
          }
        }
        catch (SecurityException se) {
            defaultLogInstance.setDebuglevel(LogTarget.WARN);
        }
    }

    /**
     * Initializes the log system after the log module was loaded and a log target
     * was defined. This is the second step of the log initialisation.
     */
    public void init() {
        removeTarget(DEFAULT_LOG_TARGET);
        final String logLevel = LogConfiguration.getLogLevel();
        if (logLevel.equalsIgnoreCase("error")) {
            setDebuglevel(LogTarget.ERROR);
        }
        else if (logLevel.equalsIgnoreCase("warn")) {
            setDebuglevel(LogTarget.WARN);
        }
        else if (logLevel.equalsIgnoreCase("info")) {
            setDebuglevel(LogTarget.INFO);
        }
        else if (logLevel.equalsIgnoreCase("debug")) {
            setDebuglevel(LogTarget.DEBUG);
        }
    }

    /**
     * Adds a log target to this facility. Log targets get informed, via the
     * LogTarget interface, whenever a message is logged with this class.
     *
     * @param target the target.
     */
    public synchronized void addTarget(final LogTarget target)
    {
      super.addTarget(target);
      // as soon as there is a real log target added, we do no longer need
      // the default logging. This was only installed to be able to send messages
      // if the deepest basic logging failed.
      if (target != DEFAULT_LOG_TARGET) {
          removeTarget(DEFAULT_LOG_TARGET);
      }
    }

  /**
     * Returns the default log.
     * 
     * @return The default log.
     */
    public static DefaultLog getDefaultLog() {
        return defaultLogInstance;
    }

    /**
     * Makes this implementation the default instance.
     */
    public static void installDefaultLog () {
      Log.defineLog(defaultLogInstance);
    }
}
