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
 *    DefaultPackage.java
 *    Copyright (c) 2009 Pentaho Corporation
 */

package org.pentaho.packageManagement;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A concrete implementation of Package that uses Java properties files/classes
 * to manage package meta data. Assumes that meta data for individual packages
 * is stored on the central repository (or possibly in a local cache - both accessible
 * via http) in properties files that live in a sub-directory with the same name as
 * the package. Furthermore, each property file is assumed to be named as the
 * version number of the package in question with a ".props" extension. A "Latest.props"
 * file should exist for each package and should always hold meta data on the latest
 * version of a package.
 * 
 * @author mhall (mhall{[at]}pentaho{[dot]}com).
 * @version $Revision: 44030 $
 *
 */
public class DefaultPackage extends Package {

  /** Holds the home directory for installed packages */
  protected File m_packageHome;
  
  /** The package manager in use */
  protected PackageManager m_packageManager;
  
  /**
   * Clone this package. Only makes a shallow copy of the meta
   * data map
   * 
   * @return a copy of this package
   */
  public Object clone() {   
    
    DefaultPackage newP = null;
    
    if (m_packageHome != null) {
      newP = new DefaultPackage(new File(m_packageHome.toString()), 
        m_packageManager);
    } else {
      newP = new DefaultPackage(null, m_packageManager);
    }
    
    HashMap<Object, Object> metaData = new HashMap<Object, Object>();
    Set<?> keys = m_packageMetaData.keySet();
    Iterator<?> i = keys.iterator();
    
    while (i.hasNext()) {
      Object key = i.next();
      Object value = m_packageMetaData.get(key);
      metaData.put(key, value);      
    }
    
    newP.setPackageMetaData(metaData);
    
    return newP;
  }
  
  
  /**
   * Constructs an new DefaultPackage.
   * 
   * @param packageHome the directory that packages are installed into.
   * @param manager the package manager in use.
   * @param packageMetaData A Map of package meta data for this package.
   */
  public DefaultPackage(File packageHome, 
      PackageManager manager, 
      Map<?,?> packageMetaData) {
    this(packageHome, manager);
    
    setPackageMetaData(packageMetaData);
  }
  
  /**
   * Constructs a new DefaultPackage.
   * 
   * @param packageHome the directory that packages are installed into.
   * @param manager the package manager in use.
   */
  public DefaultPackage(File packageHome, PackageManager manager) {
    m_packageHome = packageHome;
    m_packageManager = manager;
  }

  /**
   * Convenience method that returns the URL to the package (i.e the provider's URL).
   * This information is assumed to be stored in the package meta data.
   * 
   * @return the URL to the package or null if the URL is not available for some reason
   * @throws Exception if the URL can't be retrieved for some reason
   */
  public URL getPackageURL() throws Exception {
    String url = getPackageMetaDataElement("PackageURL").toString();
    URL packageURL = new URL(url);

    return packageURL;
  }
  
  /**
   * Convenience method to return the name of this package.
   * 
   * @return the name of this package.
   */
  public String getName() {
    return getPackageMetaDataElement("PackageName").toString();
  }
  
  protected static String[] splitNameVersion(String nameAndVersion) {
    String[] result = new String[3];
    
    nameAndVersion = nameAndVersion.trim();
    if (nameAndVersion.indexOf('(') < 0) {
      result[0] = nameAndVersion;
    } else if (nameAndVersion.indexOf(')') >= 0) {
      boolean ok = true;
      result[0] = nameAndVersion.substring(0, nameAndVersion.indexOf('('));
      result[0] = result[0].trim();
      
      String secondInequality = null;
      int delimiterIndex = nameAndVersion.indexOf('|');
      if (delimiterIndex >= 0) {
        secondInequality = nameAndVersion.substring(delimiterIndex + 1, nameAndVersion.length());
        secondInequality = secondInequality.trim();
        String[] result2 = new String[5];
        result2[0] = result[0];
        result = result2;
      } else {
        delimiterIndex = nameAndVersion.length();
      }
      
      nameAndVersion = nameAndVersion.substring(nameAndVersion.indexOf('(')+1, delimiterIndex);
      nameAndVersion = nameAndVersion.trim();
      int pos = 1;
      if (nameAndVersion.charAt(0) == '=') {
        result[1] = "=";
      } else if (nameAndVersion.charAt(1) == '=') {
        pos++;
        if (nameAndVersion.charAt(0) == '<') {
          result[1] = "<=";
        } else {
          result[1] = ">=";          
        }
      } else if (nameAndVersion.charAt(0) == '<') {
        result[1] = "<";
      } else if (nameAndVersion.charAt(0) == '>') {
        result[1] = ">";
      } else {
        ok = false;
      }
      
      if (ok) {
        if (secondInequality != null) {
          delimiterIndex = nameAndVersion.length();
        } else {
          delimiterIndex = nameAndVersion.indexOf(')');
        }
        nameAndVersion = nameAndVersion.substring(pos, delimiterIndex);
        result[2] = nameAndVersion.trim();
      }
      
      // do the second inequality (if present)
      if (secondInequality != null) {
        ok = true;
        pos = 1;
        if (secondInequality.charAt(0) == '=') {
          result[3] = "=";
        } else if (secondInequality.charAt(1) == '=') {
          pos++;
          if (secondInequality.charAt(0) == '<') {
            result[3] = "<=";
          } else {
            result[3] = ">=";
          }
        } else if (secondInequality.charAt(0) == '<') {
          result[3] = "<";
        } else if (secondInequality.charAt(0) == '>') {
          result[3] = ">";
        } else {
          ok = false;
        }
        
        if (ok) {
          secondInequality = secondInequality.substring(pos, 
              secondInequality.indexOf(')'));
          result[4] = secondInequality.trim();          
        }
      }      
    }

    return result;
  }
  
  /**
   * Get the list of packages that this package depends on.
   * 
   * @return the list of packages that this package depends on.
   * @throws Exception if a problem occurs while getting the list
   * of dependencies.
   */
  public List<Dependency> getDependencies() throws Exception {
    List<Dependency> dependencies = new ArrayList<Dependency>();
    String dependenciesS = getPackageMetaDataElement("Depends").toString();
    
    if (dependenciesS != null) {
      StringTokenizer tok = new StringTokenizer(dependenciesS, ",");
      while (tok.hasMoreTokens()) {
        String nextT = tok.nextToken().trim();
        String[] split = splitNameVersion(nextT);
        Package toAdd = null;

        // don't include the base system!
        if (!(split[0].equalsIgnoreCase(m_packageManager.getBaseSystemName()))) {          

          // gets the latest version of this package if split[2] is null
          toAdd = m_packageManager.getRepositoryPackageInfo(split[0], split[2]);
          
          if (split.length == 3) {
            VersionPackageConstraint versionConstraint = new VersionPackageConstraint(toAdd);
            if (split[2] == null) {
              // assume anything up to and including the current version is acceptable
              versionConstraint.
              setVersionConstraint(VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL);
            } else {
              versionConstraint.setVersionConstraint(split[1]);
            }

            Dependency dep = new Dependency(this, versionConstraint);
            dependencies.add(dep);
          } else {
            // ranged constraint
            VersionRangePackageConstraint versionConstraint = 
              new VersionRangePackageConstraint(toAdd);
            VersionPackageConstraint.VersionComparison comp1 = 
              VersionPackageConstraint.getVersionComparison(split[1]);
            VersionPackageConstraint.VersionComparison comp2 =
              VersionPackageConstraint.getVersionComparison(split[3]);
            versionConstraint.setRangeConstraint(split[2], comp1, split[4], comp2);
            
            Dependency dep = new Dependency(this, versionConstraint);
            dependencies.add(dep);
          }
        }
      }
    }
    
    return dependencies;
  }

  /**
   * Gets the dependency on the base system that this package
   * requires.
   * 
   * @return the base system dependency(s) for this package
   * @throws Exception if the base system dependency can't be
   * determined for some reason.
   */
  public List<Dependency> getBaseSystemDependency() throws Exception {
    String dependenciesS = getPackageMetaDataElement("Depends").toString();
    Dependency baseDep = null;
    List<Dependency> baseDeps = new ArrayList<Dependency>();
    
    if (dependenciesS != null) {
      StringTokenizer tok = new StringTokenizer(dependenciesS, ",");
      while (tok.hasMoreTokens()) {
        String nextT = tok.nextToken().trim();
        String[] split = splitNameVersion(nextT);                
        
        if ((split[0].equalsIgnoreCase(m_packageManager.getBaseSystemName()))) {
                  
          // construct a "dummy" package for the base system
          Map<String, String> baseMap = new HashMap<String, String>();
          baseMap.put("PackageName", "weka");
          
          // use a suitably ridiculous max version number if one hasn't been supplied
          // for some reason
          split[2] = (split[2] == null ? "1000.1000.1000" : split[2]);
          baseMap.put("Version", split[2]);
          Package basePackage = new DefaultPackage(null, m_packageManager, baseMap);
          
          if (split.length == 3) {
            VersionPackageConstraint baseConstraint = new VersionPackageConstraint(basePackage);
            VersionPackageConstraint.VersionComparison baseComp = 
              VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL;
            if (split[1] != null) {
              baseComp = 
                VersionPackageConstraint.getVersionComparison(split[1]);
            }
            baseConstraint.setVersionConstraint(baseComp);

            baseDep = new Dependency(this, baseConstraint);
            baseDeps.add(baseDep);
          } else {
            // ranged constraint
            VersionRangePackageConstraint baseConstraint = 
              new VersionRangePackageConstraint(basePackage);
            
            VersionPackageConstraint.VersionComparison comp1 = 
              VersionPackageConstraint.getVersionComparison(split[1]);
            VersionPackageConstraint.VersionComparison comp2 =
              VersionPackageConstraint.getVersionComparison(split[3]);
            baseConstraint.setRangeConstraint(split[2], comp1, split[4], comp2);
            
            baseDep = new Dependency(this, baseConstraint);
          }
        }
      }
    }
    
    if (baseDeps.size() == 0) {
      throw new Exception("[Package] " + getPackageMetaDataElement("PackageName").toString()
          + " can't determine what version of the base system is required!!");
    }
    
    return baseDeps;
  }
  
  private boolean findPackage(String packageName, List<Package> packageList) {
    boolean found = false;
    
    Iterator<Package> i = packageList.iterator();
    
    while (i.hasNext()) {
      Package p = i.next();
      String pName = p.getPackageMetaDataElement("PackageName").toString();
      if (packageName.equals(pName)) {
        found = true;
        break;
      }
    }
    
    return found;
  }
  
  /**
   * Gets a list of packages that this package depends on that are not
   * in the supplied list of packages.
   * 
   * @param packages a list of packages to compare this package's dependencies
   * against.
   * @return those packages that this package depends on that aren't in the supplied
   * list.
   * @throws Exception if the list of missing depenencies can't be determined for
   * some reason.
   */
  public List<Dependency> getMissingDependencies(List<Package> packages) throws Exception {
    List<Dependency> missing = new ArrayList<Dependency>();
    String dependencies = getPackageMetaDataElement("Depends").toString();
    
    if (dependencies != null) {
      StringTokenizer tok = new StringTokenizer(dependencies, ",");
      while (tok.hasMoreTokens()) {
        String nextT = tok.nextToken().trim();
        String[] split = splitNameVersion(nextT);

        // don't consider the base system!
        if (!(split[0].equalsIgnoreCase(m_packageManager.getBaseSystemName()))) {

          // gets the latest version of this package if split[2] is null
          Package tempDep = m_packageManager.getRepositoryPackageInfo(split[0], split[2]);
          if (!findPackage(split[0], packages)) {
            VersionPackageConstraint versionConstraint = new VersionPackageConstraint(tempDep);
            if (split[2] == null) {
              // assume anything up to and including the current version is acceptable
              versionConstraint.
              setVersionConstraint(VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL);
              missing.add(new Dependency(this, versionConstraint));
            } else {
              if (split.length == 3) {
                versionConstraint.setVersionConstraint(split[1]);
                missing.add(new Dependency(this, versionConstraint));
              } else {
                VersionRangePackageConstraint versionRConstraint = 
                  new VersionRangePackageConstraint(tempDep);
                VersionPackageConstraint.VersionComparison comp1 = 
                  VersionPackageConstraint.getVersionComparison(split[1]);
                VersionPackageConstraint.VersionComparison comp2 =
                  VersionPackageConstraint.getVersionComparison(split[3]);
                
                versionRConstraint.setRangeConstraint(split[2], comp1, split[4], comp2);
                missing.add(new Dependency(this, versionRConstraint));
              }
            }
          }
        }
      }
    }
    
    return missing;
  }

  /**
   * Gets a list of packages that this package depends on that are
   * not currently installed.
   * 
   * @return a list of missing packages that this package depends on.
   */
  public List<Dependency> getMissingDependencies() throws Exception {
    List<Package> installedPackages = m_packageManager.getInstalledPackages();
    String dependencies = getPackageMetaDataElement("Depends").toString();
    
    return getMissingDependencies(installedPackages);
    
    /*if (dependencies != null) {
      StringTokenizer tok = new StringTokenizer(dependencies, ",");
      while (tok.hasMoreTokens()) {
        String nextT = tok.nextToken().trim();
        String[] split = splitNameVersion(nextT);

        // don't consider the base system!
        if (!(split[0].equalsIgnoreCase(m_packageManager.getBaseSystemName()))) {

          // gets the latest version of this package if split[2] is null
          Package tempDep = m_packageManager.getRepositoryPackageInfo(split[0], split[2]);
          if (!tempDep.isInstalled()) {
            VersionPackageConstraint versionConstraint = new VersionPackageConstraint(tempDep);
            if (split[2] == null) {
              // assume anything up to and including the current version is acceptable
              versionConstraint.
              setVersionConstraint(VersionPackageConstraint.VersionComparison.LESSTHANOREQUAL);
            } else {
              versionConstraint.setVersionConstraint(split[1]);
            }

            missing.add(new Dependency(this, versionConstraint));
          }
        }
      }
    }
    
    return missing; */
  }
  
  /**
   * Gets those packages from the supplied list that this package depends on
   * and are currently incompatible with this package.
   * 
   * @param packages a list of packages to compare this package's dependencies against
   * @return those packages from the supplied list that are incompatible with respect
   * to this package's dependencies
   * @throws Exception if the list of incompatible dependencies can't be generated for
   * some reason.
   */
  public List<Dependency> getIncompatibleDependencies(List<Package> packages) 
    throws Exception {
    List<Dependency> incompatible = new ArrayList<Dependency>();    
    String dependencies = getPackageMetaDataElement("Depends").toString();
    
    if (dependencies != null) {
      StringTokenizer tok = new StringTokenizer(dependencies, ",");
      while (tok.hasMoreTokens()) {
        String nextT = tok.nextToken().trim();
        String[] splitD = splitNameVersion(nextT);
       
        // check only if a version number was supplied in the dependency list.
        if (splitD[1] != null && splitD[2] != null) {
          for (Package p : packages) {
            String packageNameI = 
              p.getPackageMetaDataElement("PackageName").toString();
            if (packageNameI.trim().equalsIgnoreCase(splitD[0].trim())) {
              // now check version against this one
              String versionI = p.getPackageMetaDataElement("Version").toString().trim();
//              String[] splitI = splitNameVersion(versionI);

              if (splitD.length == 3) {
                VersionPackageConstraint.VersionComparison constraint = 
                  VersionPackageConstraint.getVersionComparison(splitD[1]);
                if (!VersionPackageConstraint.checkConstraint(versionI, constraint, splitD[2])) {
                  VersionPackageConstraint vpc = new VersionPackageConstraint(p);
                  vpc.setVersionConstraint(constraint);
                  incompatible.add(new Dependency(this, vpc));
                }
              } else {
                VersionRangePackageConstraint versionRConstraint = 
                  new VersionRangePackageConstraint(p);
                VersionPackageConstraint.VersionComparison comp1 = 
                  VersionPackageConstraint.getVersionComparison(splitD[1]);
                VersionPackageConstraint.VersionComparison comp2 =
                  VersionPackageConstraint.getVersionComparison(splitD[3]);
                
                versionRConstraint.setRangeConstraint(splitD[2], comp1, splitD[4], comp2);
                incompatible.add(new Dependency(this, versionRConstraint));
              }
              
/*              int comparisonResult = 
                VersionPackageConstraint.compare(versionI, splitD[2]);
              if (!versionOK(splitD[1], comparisonResult)) {
                incompatible.add(p);
              } */
            }
          }
        }
      }
    }
    
    return incompatible;
  }
  
  /**
   * Gets a list of installed packages that this package depends on
   * that are currently incompatible with this package.
   * 
   * @return a list of incompatible installed packages that this package
   * depends on. 
   */
  public List<Dependency> getIncompatibleDependencies() throws Exception {
    List<Package> installedP = m_packageManager.getInstalledPackages();
    String dependencies = getPackageMetaDataElement("Depends").toString();
    
    return getIncompatibleDependencies(installedP);    
    
    /*if (dependencies != null) {
      StringTokenizer tok = new StringTokenizer(dependencies, ",");
      while (tok.hasMoreTokens()) {
        String nextT = tok.nextToken().trim();
        String[] splitD = splitNameVersion(nextT);
       
        // check only if a version number was supplied in the dependency list.
        if (splitD[1] != null && splitD[2] != null) {
          for (Package p : installedP) {
            String packageNameI = 
              p.getPackageMetaDataElement("PackageName").toString();
            if (packageNameI.trim().equalsIgnoreCase(splitD[0].trim())) {
              // now check version against installed
              String versionI = p.getPackageMetaDataElement("Version").toString().trim();
//              String[] splitI = splitNameVersion(versionI);

              VersionPackageConstraint.VersionComparison constraint = 
                VersionPackageConstraint.getVersionComparison(splitD[1]);
              if (!VersionPackageConstraint.checkConstraint(versionI, constraint, splitD[2])) {
                VersionPackageConstraint vpc = new VersionPackageConstraint(p);
                vpc.setVersionConstraint(constraint);
                incompatible.add(new Dependency(this, vpc));
              }              
            }
          }
        }
      }
    }
    
    return incompatible;*/
  }
  
  /*public Object getPackageDependencyVersion(String dependsOnName) {
    String dependencies = getPackageMetaDataElement("Depends").toString();
    Object result = null;
    
    if (dependencies != null) {
      StringTokenizer tok = new StringTokenizer(dependencies, ",");
      while (tok.hasMoreTokens()) {
        String nextT = tok.nextToken().trim();
        String[] splitD = splitNameVersion(nextT);
        if (dependsOnName.trim().equalsIgnoreCase(splitD[0]) && 
            splitD[2] != null) {
          result = splitD[2];
          break;
        }
      }
    }
    
    return result;
  } */
  
  /**
   * Returns true if this package is compatible with the currently installed
   * version of the base system.
   * 
   * @return true if this package is compatible with the main software system.
   * @throws Exception if a problem occurs while checking compatibility.
   */
  public boolean isCompatibleBaseSystem()
    throws Exception {
    
    String baseSystemName = m_packageManager.getBaseSystemName();
    String systemVersion = m_packageManager.getBaseSystemVersion().toString();
    //System.err.println("Base system version " + systemVersion);
    
    String dependencies = getPackageMetaDataElement("Depends").toString();
    if (dependencies == null) {
      return true;
    }
    
    boolean ok = true;
    StringTokenizer tok = new StringTokenizer(dependencies, ",");
    while (tok.hasMoreTokens()) {
      String nextT = tok.nextToken().trim();
      String[] split = splitNameVersion(nextT);
      if (split[0].startsWith(baseSystemName.toLowerCase())) {
        // check the system version
        if (split[1] != null) {
          if (split.length == 3) {
            VersionPackageConstraint.VersionComparison constraint = 
              VersionPackageConstraint.getVersionComparison(split[1]);
            if (!VersionPackageConstraint.checkConstraint(systemVersion, constraint, split[2])) {
              ok = false;
              break;
            }
          } else {
            // construct a "dummy" package for the base system
            Map<String, String> baseMap = new HashMap<String, String>();
            baseMap.put("PackageName", "weka");
            
            baseMap.put("Version", systemVersion);
            Package basePackage = new DefaultPackage(null, m_packageManager, baseMap);

            VersionRangePackageConstraint versionRConstraint = 
              new VersionRangePackageConstraint(basePackage);
            VersionPackageConstraint.VersionComparison comp1 = 
              VersionPackageConstraint.getVersionComparison(split[1]);
            VersionPackageConstraint.VersionComparison comp2 =
              VersionPackageConstraint.getVersionComparison(split[3]);
            
            versionRConstraint.setRangeConstraint(split[2], comp1, split[4], comp2);
            
            if (!versionRConstraint.checkConstraint(basePackage)) {
              ok = false;
              break;
            }
          }
/*          int comparisonResult = VersionPackageConstraint.compare(systemVersion, split[2]);
          ok = versionOK(split[1], comparisonResult);
          if (!ok) {
            break;
          } */
        }
      }
    }
    
    return ok;
  }

  /**
   * Returns true if this package is good to go with the current version
   * of the software and versions of any installed packages that it depends on.
   * 
   * @return true if this package is good to go.
   * @throws Exception if a problem occurs while checking compatibility.
   */
  /*public boolean isCompatible() 
    throws Exception {
    // TODO rewrite this to make use of getIncompatibleDependencies()
    // and getMissingDependencies() (IF this package is installed)
    
    // MIGHT just rename this method to isCompatibleWithBaseSystem

    boolean ok = isCompatibleBaseSystem();
        
    if (ok) {
      // check for any incompatible dependencies
      List<Dependency> incompatible = getIncompatibleDependencies();
      if (incompatible.size() > 0) {
        ok = false;
      }
    }
    
    if (ok && isInstalled()) {
      // first check to see if the installed version is the same as us
     Package installedVersion = 
       m_packageManager.getInstalledPackageInfo(getPackageMetaDataElement("PackageName").toString());
     String installedVersionS = installedVersion.getPackageMetaDataElement("Version").toString();
     String thisVersionS = getPackageMetaDataElement("Version").toString();
     
     if (VersionPackageConstraint.compare(thisVersionS, installedVersionS) ==
       VersionPackageConstraint.VersionComparison.EQUAL) {
       // if equal to the installed version then check to see if any dependencies are
       // missing. If not equal to the installed version then just assume that we
       // we be overwriting the installed version and any missing dependencies will
       // be downloaded during the install.
       List<Dependency> missing = getMissingDependencies();
       if (missing.size() > 0) {
         ok = false;
       }
     }
   }
    
    return ok;
  } */
  
  /**
   * Install this package.
   * 
   * @throws Exception if something goes wrong during installation.
   */
  public void install() throws Exception {
    URL packageURL = getPackageURL();
    
    m_packageManager.installPackageFromURL(packageURL);
  }

  /**
   * Returns true if this package is already installed
   * 
   * @return true if this package is installed
   */
  public boolean isInstalled() {
    File packageDir = new File(m_packageHome.getAbsoluteFile() + File.separator
        + m_packageMetaData.get("PackageName") + File.separator + "Description.props");
    return (packageDir.exists());
  }
  
  public static void main(String[] args) {
    String installed = args[0];
    String toCheckAgainst = args[1];
    String[] splitI = splitNameVersion(installed);
    String[] splitA = splitNameVersion(toCheckAgainst);
    
    try {

      if (splitA.length == 3) {
        
        System.out.println("Checking first version number against second constraint");
        VersionPackageConstraint.VersionComparison constraint = 
          VersionPackageConstraint.getVersionComparison(splitA[1]);

        if (VersionPackageConstraint.checkConstraint(splitI[2], constraint, splitA[2])) {
          System.out.println(splitI[2] + " is compatible with " + args[1]);
        } else {
          System.out.println(splitI[2] + " is not compatible with " + args[1]);
        }
        
        Map<String, String> baseMap = new HashMap<String, String>();
        baseMap.put("PackageName", splitA[0]);        
        baseMap.put("Version", splitA[2]);               
        Package packageA = new DefaultPackage(null, null, baseMap);
        packageA.setPackageMetaData(baseMap);
        VersionPackageConstraint constrA = new VersionPackageConstraint(packageA);
        constrA.setVersionConstraint(constraint);
        
        if (splitI.length == 3) {
          
          VersionPackageConstraint.VersionComparison constraintI = 
            VersionPackageConstraint.getVersionComparison(splitI[1]);
          
          Package packageI = (Package)packageA.clone();
          packageI.setPackageMetaDataElement(VersionPackageConstraint.VERSION_KEY, splitI[2]);
          VersionPackageConstraint constrI = 
            new VersionPackageConstraint(packageI);
          constrI.setVersionConstraint(constraintI);
          
          PackageConstraint pc = null;
          if ((pc = constrI.checkConstraint(constrA)) != null) {
            System.out.println(constrI + " and " + constrA + " are compatible\n\n"
                +"compatible constraint " + pc);
          } else {
            System.out.println(constrI + " and " + constrA + " are not compatible");
          }          
        } else {
          // TODO
        }
        

      } else {
        System.out.println("Checking first version number against second constraint");
        Map<String, String> baseMap = new HashMap<String, String>();
        baseMap.put("PackageName", splitI[0]);

        baseMap.put("Version", splitI[2]);
        Package p = new DefaultPackage(null, null, baseMap);

        VersionRangePackageConstraint c = 
          new VersionRangePackageConstraint(p);

        VersionPackageConstraint.VersionComparison comp1 = 
          VersionPackageConstraint.getVersionComparison(splitA[1]);
        VersionPackageConstraint.VersionComparison comp2 =
          VersionPackageConstraint.getVersionComparison(splitA[3]);
        c.setRangeConstraint(splitA[2], comp1, splitA[4], comp2);
        
        if (c.checkConstraint(p)) {
          System.out.println(splitI[2] + " is compatible with " + args[1]);
        } else {
          System.out.println(splitI[2] + " is not compatible with " + args[1]);
        }
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
/*    int compResult = VersionPackageConstraint.compare(splitI[2], splitA[2]);
    if (versionOK(splitA[1], compResult)) {
      System.out.println("Compatible");
    } else {
      System.out.println("Not ok");
    } */
  }
  
  /**
   * Adds a key, value pair to the meta data map.
   * 
   * @param key the key
   * @param value the value to add
   * @throws Exception if there is no meta data map to add to.
   */
  public void setPackageMetaDataElement(Object key, Object value) 
    throws Exception {
    if (m_packageMetaData == null) {
      throw new Exception("[DefaultPackage] no meta data map has been set!");
    }
    
    // cast to Object is fine because our maps are Properties.
    Map<Object, Object> meta = (Map<Object, Object>)m_packageMetaData;
    
    meta.put(key, value);
  }
  
  public String toString() {
    String packageName = getPackageMetaDataElement("PackageName").toString();
    String version = getPackageMetaDataElement("Version").toString();
    return packageName + " (" + version + ")";
  }
}
