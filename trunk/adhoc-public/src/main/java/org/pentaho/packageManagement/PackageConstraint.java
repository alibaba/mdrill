/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Package Manager.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

/*
 *    PackageConstraint.java
 *    Copyright (C) 2009 Pentaho Corporation
 */

package org.pentaho.packageManagement;

/**
 * Abstract base class for package constraints. An example implementation
 * might be to encapsulate a constraint with respect to a version
 * number. Checking against a target in this case would
 * typically assume the same package for both this and the target.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 44030 $
 */
public abstract class PackageConstraint {
  
  protected Package m_thePackage;
  
  /**
   * Set the package that this constraint applies to.
   * 
   * @param p the package that this constraint applies to.
   */
  public void setPackage(Package p) {
    m_thePackage = p;
  }
  
  /**
   * Get the package that this constraint applies to.
   * 
   * @return the Package that this constraint applies to.
   */
  public Package getPackage() {
    return m_thePackage;
  }
  
  /**
   * Check the target package against the constraint embodied
   * in this PackageConstraint.
   * 
   * @param target a package to check with respect to the
   * encapsulated package and the constraint.
   * 
   * @return true if the constraint is met by the target package
   * with respect to the encapsulated package + constraint.
   * @throws Exception if the constraint can't be checked for some
   * reason.
   */
  public abstract boolean checkConstraint(Package target) throws Exception;
  
  /**
   * Check the target package constraint against the constraint embodied
   * in this package constraint. Returns either the package constraint that
   * covers both this and the target constraint, or null if this and the target
   * are incompatible.
   * 
   * @param target the package constraint to compare against
   * @return a package constraint that covers this and the supplied constraint,
   * or null if they are incompatible.
   */
  public abstract PackageConstraint checkConstraint(PackageConstraint target)
    throws Exception;
}
