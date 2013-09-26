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
 * -----------------
 * AbstractBoot.java
 * -----------------
 * (C)opyright 2004, 2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AbstractBoot.java,v 1.21 2008/09/10 09:22:57 mungady Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added source headers (DG);
 * 18-Aug-2005 : Added casts to suppress compiler warnings, as suggested in
 *               patch 1260622 (DG);
 *
 */

package org.jfree.base;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.jfree.base.config.HierarchicalConfiguration;
import org.jfree.base.config.PropertyFileConfiguration;
import org.jfree.base.config.SystemPropertyConfiguration;
import org.jfree.base.modules.PackageManager;
import org.jfree.base.modules.SubSystem;
import org.jfree.util.Configuration;
import org.jfree.util.ExtendedConfiguration;
import org.jfree.util.ExtendedConfigurationWrapper;
import org.jfree.util.Log;
import org.jfree.util.ObjectUtilities;

/**
 * The common base for all Boot classes.
 * <p>
 * This initializes the subsystem and all dependent subsystems.
 * Implementors of this class have to provide a public static
 * getInstance() method which returns a singleton instance of the
 * booter implementation.
 * <p>
 * Further creation of Boot object should be prevented using
 * protected or private constructors in that class, or proper
 * initialzation cannot be guaranteed.
 *
 * @author Thomas Morgner
 */
public abstract class AbstractBoot implements SubSystem {

    /** The configuration wrapper around the plain configuration. */
    private ExtendedConfigurationWrapper extWrapper;

    /** A packageManager instance of the package manager. */
    private PackageManager packageManager;

    /** Global configuration. */
    private Configuration globalConfig;

    /** A flag indicating whether the booting is currenly in progress. */
    private boolean bootInProgress;

    /** A flag indicating whether the booting is complete. */
    private boolean bootDone;

    /**
     * Default constructor.
     */
    protected AbstractBoot() {
    }

    /**
     * Returns the packageManager instance of the package manager.
     *
     * @return The package manager.
     */
    public synchronized PackageManager getPackageManager() {
        if (this.packageManager == null) {
            this.packageManager = PackageManager.createInstance(this);
        }
        return this.packageManager;
    }

    /**
     * Returns the global configuration.
     *
     * @return The global configuration.
     */
    public synchronized Configuration getGlobalConfig() {
        if (this.globalConfig == null) {
            this.globalConfig = loadConfiguration();
        }
        return this.globalConfig;
    }

    /**
     * Checks, whether the booting is in progress.
     *
     * @return true, if the booting is in progress, false otherwise.
     */
    public final synchronized boolean isBootInProgress() {
        return this.bootInProgress;
    }

    /**
     * Checks, whether the booting is complete.
     *
     * @return true, if the booting is complete, false otherwise.
     */
    public final synchronized boolean isBootDone() {
        return this.bootDone;
    }

    /**
     * Loads the configuration. This will be called exactly once.
     *
     * @return The configuration.
     */
    protected abstract Configuration loadConfiguration();

    /**
     * Starts the boot process.
     */
    public final void start() {

        synchronized (this) {
            if (isBootDone()) {
                return;
            }
            while (isBootInProgress()) {
              try {
                wait();
              }
              catch (InterruptedException e) {
                // ignore ..
              }
            }
            if (isBootDone()) {
                return;
            }
            this.bootInProgress = true;
        }

        // boot dependent libraries ...
        final BootableProjectInfo info = getProjectInfo();
        if (info != null) {
            final BootableProjectInfo[] childs = info.getDependencies();
            for (int i = 0; i < childs.length; i++) {
                final AbstractBoot boot = loadBooter(childs[i].getBootClass());
                if (boot != null) {
                    // but we're waiting until the booting is complete ...
                    synchronized(boot) {
                      boot.start();
                      while (boot.isBootDone() == false) {
                        try {
                          boot.wait();
                        }
                        catch (InterruptedException e) {
                          // ignore it ..
                        }
                      }
                    }
                }
            }
        }

        performBoot();
        if (info != null)
        {
          Log.info (info.getName() + " " + info.getVersion() + " started.");
        }
        else
        {
          Log.info (getClass() + " started.");
        }

        synchronized (this) {
            this.bootInProgress = false;
            this.bootDone = true;
            notifyAll();
        }
    }

    /**
     * Performs the boot.
     */
    protected abstract void performBoot();

    /**
     * Returns the project info.
     *
     * @return The project info.
     */
    protected abstract BootableProjectInfo getProjectInfo();

    /**
     * Loads the specified booter implementation.
     *
     * @param classname  the class name.
     *
     * @return The boot class.
     */
    protected AbstractBoot loadBooter(final String classname) {
        if (classname == null) {
            return null;
        }
        try {
            final Class c = ObjectUtilities.getClassLoader(
                    getClass()).loadClass(classname);
            final Method m = c.getMethod("getInstance", (Class[]) null);
            return (AbstractBoot) m.invoke(null, (Object[]) null);
        }
        catch (Exception e) {
            Log.info ("Unable to boot dependent class: " + classname);
            return null;
        }
    }

    /**
     * Creates a default configuration setup, which loads its settings from
     * the static configuration (defaults provided by the developers of the
     * library) and the user configuration (settings provided by the deployer).
     * The deployer's settings override the developer's settings.
     *
     * If the parameter <code>addSysProps</code> is set to true, the system
     * properties will be added as third configuration layer. The system
     * properties configuration allows to override all other settings.
     *
     * @param staticConfig the resource name of the developers configuration
     * @param userConfig the resource name of the deployers configuration
     * @param addSysProps a flag defining whether to include the system
     *                    properties into the configuration.
     * @return the configured Configuration instance.
     */
    protected Configuration createDefaultHierarchicalConfiguration
        (final String staticConfig, final String userConfig,
                final boolean addSysProps)
    {
      return createDefaultHierarchicalConfiguration
          (staticConfig, userConfig, addSysProps, PropertyFileConfiguration.class);
    }

    /**
     * Creates a default hierarchical configuration.
     *
     * @param staticConfig  the static configuration.
     * @param userConfig  the user configuration.
     * @param addSysProps  additional system properties.
     * @param source  the source.
     *
     * @return The configuration.
     */
    protected Configuration createDefaultHierarchicalConfiguration
        (final String staticConfig, final String userConfig,
         final boolean addSysProps, final Class source)
    {
        final HierarchicalConfiguration globalConfig
            = new HierarchicalConfiguration();

        if (staticConfig != null) {
          final PropertyFileConfiguration rootProperty
              = new PropertyFileConfiguration();
          rootProperty.load(staticConfig, getClass());
          globalConfig.insertConfiguration(rootProperty);
          globalConfig.insertConfiguration(
                  getPackageManager().getPackageConfiguration());
        }
        if (userConfig != null) {
          String userConfigStripped;
          if (userConfig.startsWith("/")) {
            userConfigStripped = userConfig.substring(1);
          }
          else {
            userConfigStripped = userConfig;
          }
          try {
            final Enumeration userConfigs = ObjectUtilities.getClassLoader
                            (getClass()).getResources(userConfigStripped);
            final ArrayList configs = new ArrayList();
            while (userConfigs.hasMoreElements()) {
              final URL url = (URL) userConfigs.nextElement();
              try {
                final PropertyFileConfiguration baseProperty =
                        new PropertyFileConfiguration();
                final InputStream in = url.openStream();
                baseProperty.load(in);
                in.close();
                configs.add(baseProperty);
              }
              catch(IOException ioe) {
                Log.warn ("Failed to load the user configuration at " + url, ioe);
              }
            }

            for (int i = configs.size() - 1; i >= 0; i--) {
              final PropertyFileConfiguration baseProperty =
                      (PropertyFileConfiguration) configs.get(i);
              globalConfig.insertConfiguration(baseProperty);
            }
          }
          catch (IOException e) {
            Log.warn ("Failed to lookup the user configurations.", e);
          }
        }
        if (addSysProps) {
          final SystemPropertyConfiguration systemConfig
              = new SystemPropertyConfiguration();
          globalConfig.insertConfiguration(systemConfig);
        }
        return globalConfig;
    }

    /**
     * Returns the global configuration as extended configuration.
     *
     * @return the extended configuration.
     */
    public synchronized ExtendedConfiguration getExtendedConfig ()
    {
      if (this.extWrapper == null) {
          this.extWrapper = new ExtendedConfigurationWrapper(getGlobalConfig());
      }
      return this.extWrapper;
    }
}
