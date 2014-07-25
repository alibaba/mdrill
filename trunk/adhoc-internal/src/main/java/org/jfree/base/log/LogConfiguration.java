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
 * ---------------------
 * LogConfiguration.java
 * ---------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: LogConfiguration.java,v 1.6 2005/12/18 23:29:18 taqua Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.base.log;

import org.jfree.base.BaseBoot;
import org.jfree.util.PrintStreamLogTarget;

/**
 * A log configuration class. This implementation is a simple frontend
 * to the global configuration.
 *
 * @author Thomas Morgner
 */
public class LogConfiguration {
  /** The default 'disable logging' property value. */
  public static final String DISABLE_LOGGING_DEFAULT = "false";

  /** The 'log level' property key. */
  public static final String LOGLEVEL = "org.jfree.base.LogLevel";

  /** The default 'log level' property value. */
  public static final String LOGLEVEL_DEFAULT = "Info";

  /** The 'log target' property key. */
  public static final String LOGTARGET = "org.jfree.base.LogTarget";

  /** The default 'log target' property value. */
  public static final String LOGTARGET_DEFAULT =
          PrintStreamLogTarget.class.getName();

  /** The 'disable logging' property key. */
  public static final String DISABLE_LOGGING = "org.jfree.base.NoDefaultDebug"; 

  /**
   * Default constructor.
   */
  private LogConfiguration() {
      // nothing required.
  }

  /**
   * Returns the current log target.
   *
   * @return the log target.
   */
  public static String getLogTarget()
  {
    return BaseBoot.getInstance().getGlobalConfig().getConfigProperty
            (LOGTARGET, LOGTARGET_DEFAULT);
  }

  /**
   * Sets the log target.
   *
   * @param logTarget  the new log target.
   */
  public static void setLogTarget(final String logTarget)
  {
      BaseBoot.getConfiguration().setConfigProperty (LOGTARGET, logTarget);
  }

  /**
   * Returns the log level.
   *
   * @return the log level.
   */
  public static String getLogLevel()
  {
    return BaseBoot.getInstance().getGlobalConfig().getConfigProperty
            (LOGLEVEL, LOGLEVEL_DEFAULT);
  }

  /**
   * Sets the log level, which is read from the global report configuration at
   * the point that the classloader loads the {@link org.jfree.util.Log} class.
   * <p>
   * Valid log levels are:
   *
   * <ul>
   * <li><code>"Error"</code> - error messages;</li>
   * <li><code>"Warn"</code> - warning messages;</li>
   * <li><code>"Info"</code> - information messages;</li>
   * <li><code>"Debug"</code> - debug messages;</li>
   * </ul>
   *
   * Notes:
   * <ul>
   * <li>the setting is not case sensitive.</li>
   * <li>changing the log level after the {@link org.jfree.util.Log} class has been
   * loaded will have no effect.</li>
   * <li>to turn of logging altogether, use the {@link #setDisableLogging} method.</li>
   * </ul>
   *
   * @param level  the new log level.
   */
  public static void setLogLevel(final String level)
  {
    BaseBoot.getConfiguration().setConfigProperty(LOGLEVEL, level);
  }

  /**
   * Returns <code>true</code> if logging is disabled, and <code>false</code> otherwise.
   *
   * @return true, if logging is completly disabled, false otherwise.
   */
  public static boolean isDisableLogging()
  {
    return BaseBoot.getInstance().getGlobalConfig().getConfigProperty
        (DISABLE_LOGGING, DISABLE_LOGGING_DEFAULT).equalsIgnoreCase("true");
  }

  /**
   * Sets the flag that disables logging.
   * <p>
   * To switch off logging globally, you can use the following code:
   * <p>
   * <code>ReportConfiguration.getGlobalConfig().setDisableLogging(true);</code>
   *
   * @param disableLogging  the flag.
   */
  public static void setDisableLogging(final boolean disableLogging)
  {
    BaseBoot.getConfiguration().setConfigProperty
            (DISABLE_LOGGING, String.valueOf(disableLogging));
  }


}
