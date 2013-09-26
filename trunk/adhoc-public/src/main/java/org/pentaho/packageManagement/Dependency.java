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
 *    Dependency.java
 *    Copyright (C) 2009 Pentaho Corporation
 */

package org.pentaho.packageManagement;

/**
 * Class that encapsulates a dependency between two packages
 * 
 * @author mhall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 44030 $
 */
public class Dependency {
  
  /** The source package */
  protected Package m_sourcePackage;
  
  /** The target package (wrapped in a PackageConstraint) */
  protected PackageConstraint m_targetPackage;
  
  /**
   * Construct a new Dependency from a supplied source package and
   * PackageConstraint containing the target package.
   * 
   * @param source the source package.
   * @param target the target package (wrapped in a PackageConstraint).
   */
  public Dependency(Package source, PackageConstraint target) {
    m_sourcePackage = source;
    m_targetPackage = target;
  }
  
  /**
   * Set the source package.
   * 
   * @param source the source package.
   */
  public void setSource(Package source) {
    m_sourcePackage = source;
  }
  
  /**
   * Get the source package.
   * 
   * @return the source package.
   */
  public Package getSource() {
    return m_sourcePackage;
  }
  
  /**
   * Set the target package constraint.
   * 
   * @param target the target package (wrapped in a PackageConstraint).
   */
  public void setTarget(PackageConstraint target) {
    m_targetPackage = target;
  }
  
  /**
   * Get the target package constraint.
   * 
   * @return the target package (wrapped in a PackageConstraint).
   */
  public PackageConstraint getTarget() {
    return m_targetPackage;
  }
  
  public String toString() {
    return m_sourcePackage.toString() + " --> " + m_targetPackage.toString();
  }
}
