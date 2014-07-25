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
 * -------------
 * BaseBoot.java
 * -------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: BaseBoot.java,v 1.11 2007/11/02 17:50:34 taqua Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added source headers (DG);
 *
 */

package org.jfree.base;

import org.jfree.JCommon;
import org.jfree.base.config.ModifiableConfiguration;
import org.jfree.base.log.DefaultLogModule;
import org.jfree.util.Configuration;
import org.jfree.util.ObjectUtilities;

/**
 * The base boot class. This initializes the services provided by
 * JCommon.
 *
 * @author Thomas Morgner
 */
public class BaseBoot extends AbstractBoot {

    /**
     * Singleton instance.
     */
    private static BaseBoot singleton;

    /**
     * The project info.
     */
    private BootableProjectInfo bootableProjectInfo;

    /**
     * Default constructor (private).
     */
    private BaseBoot() {
        this.bootableProjectInfo = JCommon.INFO;
    }

    /**
     * Returns the global configuration as modifiable configuration reference.
     *
     * @return the global configuration
     */
    public static ModifiableConfiguration getConfiguration() {
        return (ModifiableConfiguration) getInstance().getGlobalConfig();
    }

    /**
     * Returns the global configuration for JFreeReport.
     * <p/>
     * In the current implementation, the configuration has no properties defined, but
     * references a parent configuration that: <ul> <li>copies across all the
     * <code>System</code> properties to use as report configuration properties (obviously
     * the majority of them will not apply to reports);</li> <li>itself references a parent
     * configuration that reads its properties from a file <code>jfreereport.properties</code>.
     * </ul>
     *
     * @return the global configuration.
     */
    protected synchronized Configuration loadConfiguration() {
        return createDefaultHierarchicalConfiguration
            ("/org/jfree/base/jcommon.properties",
             "/jcommon.properties", true, BaseBoot.class);
    }

    /**
     * Returns the boot instance.
     *
     * @return The boot instance.
     */
    public static synchronized AbstractBoot getInstance() {
        if (singleton == null) {
            singleton = new BaseBoot();
        }
        return singleton;
    }

    /**
     * Performs the boot process.
     */
    protected void performBoot() {
        // configure the classloader from the properties-file.
        ObjectUtilities.setClassLoaderSource
                (getConfiguration().getConfigProperty("org.jfree.ClassLoader"));

        getPackageManager().addModule(DefaultLogModule.class.getName());
        getPackageManager().load("org.jfree.jcommon.modules.");
        getPackageManager().initializeModules();
    }

    /**
     * Returns the project info.
     *
     * @return The project info.
     */
    protected BootableProjectInfo getProjectInfo() {
        return this.bootableProjectInfo;
    }
}
