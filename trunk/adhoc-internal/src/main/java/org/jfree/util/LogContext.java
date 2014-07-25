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
 * LogContext.java
 * ---------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: LogContext.java,v 1.3 2005/10/18 13:24:19 mungady Exp $
 *
 * Changes 
 * -------
 * 26-Apr-2004 : Initial version (TM);
 *  
 */

package org.jfree.util;

/**
 * A log context.
 *
 * @author Thomas Morgner
 */
public class LogContext {

    /** The prefix string. */
    private String contextPrefix;

    /**
     * Creates a new log context.
     * 
     * @param contextPrefix  the prefix.
     */
    public LogContext(final String contextPrefix) {
        this.contextPrefix = contextPrefix;
    }

    /**
     * Returns true, if the log level allows debug messages to be
     * printed.
     *
     * @return true, if messages with an log level of DEBUG are allowed.
     */
    public boolean isDebugEnabled() {
        return Log.isDebugEnabled();
    }

    /**
     * Returns true, if the log level allows informational
     * messages to be printed.
     *
     * @return true, if messages with an log level of INFO are allowed.
     */
    public boolean isInfoEnabled() {
        return Log.isInfoEnabled();
    }

    /**
     * Returns true, if the log level allows warning messages to be
     * printed.
     *
     * @return true, if messages with an log level of WARN are allowed.
     */
    public boolean isWarningEnabled() {
        return Log.isWarningEnabled();
    }

    /**
     * Returns true, if the log level allows error messages to be
     * printed.
     *
     * @return true, if messages with an log level of ERROR are allowed.
     */
    public boolean isErrorEnabled() {
        return Log.isErrorEnabled();
    }


    /**
     * A convenience method for logging a 'debug' message.
     *
     * @param message the message.
     */
    public void debug(final Object message) {
        log(LogTarget.DEBUG, message);
    }

    /**
     * A convenience method for logging a 'debug' message.
     *
     * @param message the message.
     * @param e       the exception.
     */
    public void debug(final Object message, final Exception e) {
        log(LogTarget.DEBUG, message, e);
    }

    /**
     * A convenience method for logging an 'info' message.
     *
     * @param message the message.
     */
    public void info(final Object message) {
        log(LogTarget.INFO, message);
    }

    /**
     * A convenience method for logging an 'info' message.
     *
     * @param message the message.
     * @param e       the exception.
     */
    public void info(final Object message, final Exception e) {
        log(LogTarget.INFO, message, e);
    }

    /**
     * A convenience method for logging a 'warning' message.
     *
     * @param message the message.
     */
    public void warn(final Object message) {
        log(LogTarget.WARN, message);
    }

    /**
     * A convenience method for logging a 'warning' message.
     *
     * @param message the message.
     * @param e       the exception.
     */
    public void warn(final Object message, final Exception e) {
        log(LogTarget.WARN, message, e);
    }

    /**
     * A convenience method for logging an 'error' message.
     *
     * @param message the message.
     */
    public void error(final Object message) {
        log(LogTarget.ERROR, message);
    }

    /**
     * A convenience method for logging an 'error' message.
     *
     * @param message the message.
     * @param e       the exception.
     */
    public void error(final Object message, final Exception e) {
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
    public void log(final int level, final Object message) {
        if (this.contextPrefix != null) {
            Log.getInstance().doLog(level, new Log.SimpleMessage(this.contextPrefix, ":", message));
        }
        else {
            Log.getInstance().doLog(level, message);
        }
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
    public void log(final int level, final Object message, final Exception e) {
        if (this.contextPrefix != null) {
            Log.getInstance().doLog(
                level, new Log.SimpleMessage(this.contextPrefix, ":", message), e
            );
        }
        else {
            Log.getInstance().doLog(level, message, e);
        }
    }

    /**
     * Tests this object for equality with an arbitrary object.
     * 
     * @param o  the object to test against (<code>null</code> permitted).
     * 
     * @return A boolean.
     */
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogContext)) {
            return false;
        }

        final LogContext logContext = (LogContext) o;

        if (this.contextPrefix != null)
        {
            if (!this.contextPrefix.equals(logContext.contextPrefix)) {
                return false;
            }
        }
        else {
            if (logContext.contextPrefix != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a hashcode.
     * 
     * @return The hashcode.
     */
    public int hashCode() {
        return (this.contextPrefix != null ? this.contextPrefix.hashCode() : 0);
    }
}
