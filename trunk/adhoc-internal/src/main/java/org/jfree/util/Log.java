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
 * --------
 * Log.java
 * --------
 * (C)opyright 2002-2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: Log.java,v 1.5 2006/06/08 17:42:20 taqua Exp $
 *
 * Changes
 * -------
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 * 11-Jun-2003 : Removing LogTarget did not work. 
 * 
 */

package org.jfree.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A simple logging facility. Create a class implementing the {@link org.jfree.util.LogTarget}
 * interface to use this feature.
 *
 * @author Thomas Morgner
 */
public class Log {

    /**
     * A simple message class.
     */
    public static class SimpleMessage {

        /**
         * The message.
         */
        private String message;

        /**
         * The parameters.
         */
        private Object[] param;

        /**
         * Creates a new message.
         *
         * @param message the message text.
         * @param param1  parameter 1.
         */
        public SimpleMessage(final String message, final Object param1) {
            this.message = message;
            this.param = new Object[]{param1};
        }

        /**
         * Creates a new message.
         *
         * @param message the message text.
         * @param param1  parameter 1.
         * @param param2  parameter 2.
         */
        public SimpleMessage(final String message, final Object param1,
                             final Object param2) {
            this.message = message;
            this.param = new Object[]{param1, param2};
        }

        /**
         * Creates a new message.
         *
         * @param message the message text.
         * @param param1  parameter 1.
         * @param param2  parameter 2.
         * @param param3  parameter 3.
         */
        public SimpleMessage(final String message, final Object param1,
                             final Object param2, final Object param3) {
            this.message = message;
            this.param = new Object[]{param1, param2, param3};
        }

        /**
         * Creates a new message.
         *
         * @param message the message text.
         * @param param1  parameter 1.
         * @param param2  parameter 2.
         * @param param3  parameter 3.
         * @param param4  parameter 4.
         */
        public SimpleMessage(final String message, final Object param1,
                             final Object param2, final Object param3,
                             final Object param4) {
            this.message = message;
            this.param = new Object[]{param1, param2, param3, param4};
        }

        /**
         * Creates a new message.
         *
         * @param message the message text.
         * @param param   the parameters.
         */
        public SimpleMessage(final String message, final Object[] param) {
            this.message = message;
            this.param = param;
        }

        /**
         * Returns a string representation of the message (useful for debugging).
         *
         * @return the string.
         */
        public String toString() {
            final StringBuffer b = new StringBuffer();
            b.append(this.message);
            if (this.param != null) {
                for (int i = 0; i < this.param.length; i++) {
                    b.append(this.param[i]);
                }
            }
            return b.toString();
        }
    }


    /**
     * The logging threshold.
     */
    private int debuglevel;

    /**
     * Storage for the log targets.
     */
    private LogTarget[] logTargets;

    /** The log contexts. */
    private HashMap logContexts;

    /**
     * the singleton instance of the Log system.
     */
    private static Log singleton;

    /**
     * Creates a new Log instance. The Log is used to manage the log targets.
     */
    protected Log() {
        this.logContexts = new HashMap();
        this.logTargets = new LogTarget[0];
        this.debuglevel = 100;
    }

    /**
     * Returns the singleton Log instance. A new instance is created if necessary.
     *
     * @return the singleton instance.
     */
    public static synchronized Log getInstance() {
        if (singleton == null) {
            singleton = new Log();
        }
        return singleton;
    }

    /**
     * Redefines or clears the currently used log instance.
     *
     * @param log the new log instance or null, to return to the default implementation.
     */
    protected static synchronized void defineLog(final Log log) {
        singleton = log;
    }

    /**
     * Returns the currently defined debug level. The higher the level, the more details
     * are printed.
     *
     * @return the debug level.
     */
    public int getDebuglevel() {
        return this.debuglevel;
    }

    /**
     * Defines the debug level for the log system.
     *
     * @param debuglevel the new debug level
     * @see #getDebuglevel()
     */
    protected void setDebuglevel(final int debuglevel) {
        this.debuglevel = debuglevel;
    }

    /**
     * Adds a log target to this facility. Log targets get informed, via the LogTarget interface,
     * whenever a message is logged with this class.
     *
     * @param target the target.
     */
    public synchronized void addTarget(final LogTarget target) {
        if (target == null) {
            throw new NullPointerException();
        }
        final LogTarget[] data = new LogTarget[this.logTargets.length + 1];
        System.arraycopy(this.logTargets, 0, data, 0, this.logTargets.length);
        data[this.logTargets.length] = target;
        this.logTargets = data;
    }

    /**
     * Removes a log target from this facility.
     *
     * @param target the target to remove.
     */
    public synchronized void removeTarget(final LogTarget target) {
        if (target == null) {
            throw new NullPointerException();
        }
        final ArrayList l = new ArrayList();
        l.addAll(Arrays.asList(this.logTargets));
        l.remove(target);

        final LogTarget[] targets = new LogTarget[l.size()];
        this.logTargets = (LogTarget[]) l.toArray(targets);
    }

    /**
     * Returns the registered logtargets.
     *
     * @return the logtargets.
     */
    public LogTarget[] getTargets() {
        return (LogTarget[]) this.logTargets.clone();
    }

    /**
     * Replaces all log targets by the given target.
     *
     * @param target the new and only logtarget.
     */
    public synchronized void replaceTargets(final LogTarget target) {
        if (target == null) {
            throw new NullPointerException();
        }
        this.logTargets = new LogTarget[]{target};
    }

    /**
     * A convenience method for logging a 'debug' message.
     *
     * @param message the message.
     */
    public static void debug(final Object message) {
        log(LogTarget.DEBUG, message);
    }

    /**
     * A convenience method for logging a 'debug' message.
     *
     * @param message the message.
     * @param e       the exception.
     */
    public static void debug(final Object message, final Exception e) {
        log(LogTarget.DEBUG, message, e);
    }

    /**
     * A convenience method for logging an 'info' message.
     *
     * @param message the message.
     */
    public static void info(final Object message) {
        log(LogTarget.INFO, message);
    }

    /**
     * A convenience method for logging an 'info' message.
     *
     * @param message the message.
     * @param e       the exception.
     */
    public static void info(final Object message, final Exception e) {
        log(LogTarget.INFO, message, e);
    }

    /**
     * A convenience method for logging a 'warning' message.
     *
     * @param message the message.
     */
    public static void warn(final Object message) {
        log(LogTarget.WARN, message);
    }

    /**
     * A convenience method for logging a 'warning' message.
     *
     * @param message the message.
     * @param e       the exception.
     */
    public static void warn(final Object message, final Exception e) {
        log(LogTarget.WARN, message, e);
    }

    /**
     * A convenience method for logging an 'error' message.
     *
     * @param message the message.
     */
    public static void error(final Object message) {
        log(LogTarget.ERROR, message);
    }

    /**
     * A convenience method for logging an 'error' message.
     *
     * @param message the message.
     * @param e       the exception.
     */
    public static void error(final Object message, final Exception e) {
        log(LogTarget.ERROR, message, e);
    }

    /**
     * Logs a message to the main log stream.  All attached log targets will also
     * receive this message. If the given log-level is higher than the given debug-level
     * in the main config file, no logging will be done.
     *
     * @param level   log level of the message.
     * @param message text to be logged.
     */
    protected void doLog(int level, final Object message) {
        if (level > 3) {
            level = 3;
        }
        if (level <= this.debuglevel) {
            for (int i = 0; i < this.logTargets.length; i++) {
                final LogTarget t = this.logTargets[i];
                t.log(level, message);
            }
        }
    }

    /**
     * Logs a message to the main log stream.  All attached log targets will also
     * receive this message. If the given log-level is higher than the given debug-level
     * in the main config file, no logging will be done.
     *
     * @param level   log level of the message.
     * @param message text to be logged.
     */
    public static void log(final int level, final Object message) {
        getInstance().doLog(level, message);
    }

    /**
     * Logs a message to the main log stream. All attached logTargets will also
     * receive this message. If the given log-level is higher than the given debug-level
     * in the main config file, no logging will be done.
     * <p/>
     * The exception's stacktrace will be appended to the log-stream
     *
     * @param level   log level of the message.
     * @param message text to be logged.
     * @param e       the exception, which should be logged.
     */
    public static void log(final int level, final Object message, final Exception e) {
        getInstance().doLog(level, message, e);
    }

    /**
     * Logs a message to the main log stream. All attached logTargets will also
     * receive this message. If the given log-level is higher than the given debug-level
     * in the main config file, no logging will be done.
     * <p/>
     * The exception's stacktrace will be appended to the log-stream
     *
     * @param level   log level of the message.
     * @param message text to be logged.
     * @param e       the exception, which should be logged.
     */
    protected void doLog(int level, final Object message, final Exception e) {
        if (level > 3) {
            level = 3;
        }

        if (level <= this.debuglevel) {
            for (int i = 0; i < this.logTargets.length; i++) {
                final LogTarget t = this.logTargets[i];
                t.log(level, message, e);
            }
        }
    }

    /**
     * Initializes the logging system. Implementors should
     * override this method to supply their own log configuration.
     */
    public void init() {
        // this method is intentionally empty.
    }

    /**
     * Returns true, if the log level allows debug messages to be
     * printed.
     *
     * @return true, if messages with an log level of DEBUG are allowed.
     */
    public static boolean isDebugEnabled() {
        return getInstance().getDebuglevel() >= LogTarget.DEBUG;
    }

    /**
     * Returns true, if the log level allows informational
     * messages to be printed.
     *
     * @return true, if messages with an log level of INFO are allowed.
     */
    public static boolean isInfoEnabled() {
        return getInstance().getDebuglevel() >= LogTarget.INFO;
    }

    /**
     * Returns true, if the log level allows warning messages to be
     * printed.
     *
     * @return true, if messages with an log level of WARN are allowed.
     */
    public static boolean isWarningEnabled() {
        return getInstance().getDebuglevel() >= LogTarget.WARN;
    }

    /**
     * Returns true, if the log level allows error messages to be
     * printed.
     *
     * @return true, if messages with an log level of ERROR are allowed.
     */
    public static boolean isErrorEnabled() {
        return getInstance().getDebuglevel() >= LogTarget.ERROR;
    }

    /**
     * Creates a log context.
     * 
     * @param context  the class (<code>null</code> not permitted).
     * 
     * @return A log context.
     */
    public static LogContext createContext(final Class context) {
        return createContext(context.getName());
    }

    /**
     * Creates a log context.
     * 
     * @param context  the label for the context.
     * 
     * @return A log context.
     */
    public static LogContext createContext(final String context) {
        return getInstance().internalCreateContext(context);
    }

    /**
     * Creates a log context.
     * 
     * @param context  the name of the logging context (a common prefix).
     * 
     * @return A log context.
     */
    protected LogContext internalCreateContext(final String context) {
        synchronized (this) {
            LogContext ctx = (LogContext) this.logContexts.get(context);
            if (ctx == null) {
                ctx = new LogContext(context);
                this.logContexts.put(context, ctx);
            }
            return ctx;
        }
    }
    
}
