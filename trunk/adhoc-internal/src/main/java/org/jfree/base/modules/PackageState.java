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
 * PackageState.java
 * -----------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: PackageState.java,v 1.4 2006/04/14 13:00:56 taqua Exp $
 *
 * Changes
 * -------
 * 10-Jul-2003 : Initial version
 * 07-Jun-2004 : Added JCommon header (DG);
 * 
 */

package org.jfree.base.modules;

import org.jfree.util.Log;

/**
 * The package state class is used by the package manager to keep track of
 * the activation level of the installed or errornous packages.
 *
 * @author Thomas Morgner
 */
public class PackageState
{
  /** A constant defining that the package is new. */
  public static final int STATE_NEW = 0;
  
  /** A constant defining that the package has been loaded and configured. */
  public static final int STATE_CONFIGURED = 1;
  
  /** A constant defining that the package was initialized and is ready to use. */
  public static final int STATE_INITIALIZED = 2;
  
  /** A constant defining that the package produced an error and is not available. */
  public static final int STATE_ERROR = -2;

  /** The module class that contains the package information. */
  private final Module module;
  /** The state of the module. */
  private int state;

  /**
   * Creates a new package state for the given module. The module state will
   * be initialized to STATE_NEW.
   *
   * @param module the module.
   */
  public PackageState(final Module module)
  {
    this (module, STATE_NEW);
  }

  /**
   * Creates a new package state for the given module. The module state will
   * be initialized to the given initial state.
   *
   * @param module the module.
   * @param state the initial state
   */
  public PackageState(final Module module, final int state)
  {
    if (module == null)
    {
      throw new NullPointerException("Module must not be null.");
    }
    if (state != STATE_CONFIGURED && state != STATE_ERROR 
            && state != STATE_INITIALIZED && state != STATE_NEW)
    {
      throw new IllegalArgumentException("State is not valid");
    }
    this.module = module;
    this.state = state;
  }

  /**
   * Configures the module and raises the state to STATE_CONFIGURED if the
   * module is not yet configured.
   *
   * @param subSystem  the sub-system.
   * 
   * @return true, if the module was configured, false otherwise.
   */
  public boolean configure(final SubSystem subSystem)
  {
    if (this.state == STATE_NEW)
    {
      try
      {
        this.module.configure(subSystem);
        this.state = STATE_CONFIGURED;
        return true;
      }
      catch (NoClassDefFoundError noClassDef)
      {
        Log.warn (new Log.SimpleMessage("Unable to load module classes for ",
                this.module.getName(), ":", noClassDef.getMessage()));
        this.state = STATE_ERROR;
      }
      catch (Exception e)
      {
        if (Log.isDebugEnabled())
        {
          // its still worth a warning, but now we are more verbose ...
          Log.warn("Unable to configure the module " + this.module.getName(), e);
        }
        else if (Log.isWarningEnabled())
        {
          Log.warn("Unable to configure the module " + this.module.getName());
        }
        this.state = STATE_ERROR;
      }
    }
    return false;
  }

  /**
   * Returns the module managed by this state implementation.
   *
   * @return the module.
   */
  public Module getModule()
  {
    return this.module;
  }

  /**
   * Returns the current state of the module. This method returns either
   * STATE_NEW, STATE_CONFIGURED, STATE_INITIALIZED or STATE_ERROR.
   *
   * @return the module state.
   */
  public int getState()
  {
    return this.state;
  }

  /**
   * Initializes the contained module and raises the set of the module to
   * STATE_INITIALIZED, if the module was not yet initialized. In case of an
   * error, the module state will be set to STATE_ERROR and the module will
   * not be available.
   *
   * @param subSystem  the sub-system.
   * 
   * @return true, if the module was successfully initialized, false otherwise.
   */
  public boolean initialize(final SubSystem subSystem)
  {
    if (this.state == STATE_CONFIGURED)
    {
      try
      {
          this.module.initialize(subSystem);
          this.state = STATE_INITIALIZED;
          return true;
      }
      catch (NoClassDefFoundError noClassDef)
      {
        Log.warn (new Log.SimpleMessage("Unable to load module classes for ",
                this.module.getName(), ":", noClassDef.getMessage()));
        this.state = STATE_ERROR;
      }
      catch (ModuleInitializeException me)
      {
        if (Log.isDebugEnabled())
        {
          // its still worth a warning, but now we are more verbose ...
          Log.warn("Unable to initialize the module " + this.module.getName(), me);
        }
        else if (Log.isWarningEnabled())
        {
          Log.warn("Unable to initialize the module " + this.module.getName());
        }
        this.state = STATE_ERROR;
      }
      catch (Exception e)
      {
        if (Log.isDebugEnabled())
        {
          // its still worth a warning, but now we are more verbose ...
          Log.warn("Unable to initialize the module " + this.module.getName(), e);
        }
        else if (Log.isWarningEnabled())
        {
          Log.warn("Unable to initialize the module " + this.module.getName());
        }
        this.state = STATE_ERROR;
      }
    }
    return false;
  }

  /**
   * Compares this object with the given other object for equality. 
   * @see java.lang.Object#equals(java.lang.Object)
   * 
   * @param o the other object to be compared
   * @return true, if the other object is also a PackageState containing
   * the same module, false otherwise. 
   */
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (!(o instanceof PackageState))
    {
      return false;
    }

    final PackageState packageState = (PackageState) o;

    if (!this.module.getModuleClass().equals(packageState.module.getModuleClass()))
    {
      return false;
    }

    return true;
  }

  /**
   * Computes a hashcode for this package state. 
   * @see java.lang.Object#hashCode()
   * 
   * @return the hashcode.
   */
  public int hashCode()
  {
    return this.module.hashCode();
  }
}
