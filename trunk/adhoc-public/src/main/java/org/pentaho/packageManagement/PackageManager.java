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
 *    PackageManager.java
 *    Copyright (c) 2009 Pentaho Corporation
 */

package org.pentaho.packageManagement;

import java.beans.Beans;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Abstract base class for package managers. Contains methods to manage the location
 * of the central package repository, the home directory for installing packages,
 * the name and version of the base software system and a http proxy.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 44030 $
 */
public abstract class PackageManager {
  
  public static PackageManager create() {
    PackageManager pm = new DefaultPackageManager();
    pm.establishProxy();
    
    try {
      // See if org.pentaho.packageManagement.manager has been set
      String managerName = System.getProperty("org.pentaho.packageManagement.manager");
      if (managerName != null && managerName.length() > 0) {
        Object manager = Beans.instantiate(pm.getClass().getClassLoader(), managerName);
        if (manager instanceof PackageManager) {
          pm = (PackageManager)manager;
        }
      } else {

        // See if there is a named package manager specified in $HOME/PackageManager.props
        // that we should try to instantiate
        File packageManagerPropsFile = new File(System.getProperty("user.home") 
            + File.separator + "PackageManager.props");
        if (packageManagerPropsFile.exists()) {
          Properties pmProps = new Properties();
          pmProps.load(new FileInputStream(packageManagerPropsFile));
          managerName = pmProps.getProperty("org.pentaho.packageManager.manager");
          if (managerName != null && managerName.length() > 0) {
            Object manager = Beans.instantiate(pm.getClass().getClassLoader(), managerName);
            if (manager instanceof PackageManager) {
              pm = (PackageManager)manager;
            }
          }
        }
      }
    } catch (Exception ex) {
      // ignore any problems and just return the default package manager
      System.err.println("Problem instantiating package manager. Using DefaultPackageManager.");
    }
    return pm;
  }
  
  /** The local directory for storing the user's installed packages */
  protected File m_packageHome;
  
  /** The URL to the global package meta data repository */
  protected URL m_packageRepository;
  
  /** The name of the base system being managed by this package manager */
  protected String m_baseSystemName;
  
  /** The version of the base system being managed by this package manager */
  protected Object m_baseSystemVersion;
  
  /** Proxy for http connections */
  protected Proxy m_httpProxy;
  
  /** The user name for the proxy */
  protected String m_proxyUsername;
  
  /** The password for the proxy */
  protected String m_proxyPassword;
  
  /**
   * Tries to configure a Proxy object for use in an Authenticator
   * if there is a proxy defined by the properties http.proxyHost
   * and http.proxyPort, and if the user has set values for the
   * properties (note, these are not standard java properties)
   * http.proxyUser and http.proxyPassword.
   * 
   */
  public void establishProxy() {
    // check for proxy properties
    String proxyHost = System.getProperty("http.proxyHost");
    String proxyPort = System.getProperty("http.proxyPort");
    if (proxyHost != null && proxyHost.length() > 0) {
      int portNum = 80;
      if (proxyPort != null && proxyPort.length() > 0) {
        portNum = Integer.parseInt(proxyPort);
      }
      InetSocketAddress sa = new InetSocketAddress(proxyHost, portNum);
      setProxy(new Proxy(Proxy.Type.HTTP, sa));
    }
    
    // check for authentication
    String proxyUserName = System.getProperty("http.proxyUser");
    String proxyPassword = System.getProperty("http.proxyPassword");
    if (proxyUserName != null && proxyUserName.length() > 0 &&
        proxyPassword != null && proxyPassword.length() > 0) {
      
      setProxyUsername(proxyUserName);
      setProxyPassword(proxyPassword);
    }
  }
  
  /**
   * Sets an new default Authenticator that will return the values
   * set through setProxyUsername() and setProxyPassword.
   * 
   * @return true if the Authenticator was set successfully.
   */
  public boolean setProxyAuthentication() {
    if (m_httpProxy != null && m_proxyUsername != null && 
        m_proxyPassword != null) {
      Authenticator.setDefault(new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new
             PasswordAuthentication(m_proxyUsername,m_proxyPassword.toCharArray());
      }});
      return true;
    }
    
    return false;
  }
  
  /**
   * Set the location (directory) of installed packages.
   * 
   * @param packageHome the file system location of installed packages.
   */
  public void setPackageHome(File packageHome) {
    m_packageHome = packageHome;
  }
  
  /**
   * Get the location (directory) of installed packages
   * 
   * @return the directory containing installed packages.
   */
  public File getPackageHome() {
    return m_packageHome;
  }
  
  /**
   * Set the name of the main software system for which we manage
   * packages.
   * 
   * @param baseS the name of the base software system
   */
  public void setBaseSystemName(String baseS) {
    m_baseSystemName = baseS;
  }
  
  /**
   * Get the name of the main software system for which we manage
   * packages.
   * 
   * @return the name of the base software system.
   */
  public String getBaseSystemName() {
    return m_baseSystemName;
  }
  
  /**
   * Set the current version of the base system for which we manage
   * packages. 
   * 
   * @param systemV the current version of the main software system.
   */
  public void setBaseSystemVersion(Object systemV) {
    m_baseSystemVersion = systemV;
  }
  
  /**
   * Get the current installed version of the main system for
   * which we manage packages.
   * 
   * @return the installed version of the base system.
   */
  public Object getBaseSystemVersion() {
    return m_baseSystemVersion;
  }
  
  /**
   * Set the URL to the repository of package meta data.
   * 
   * @param repositoryURL the URL to the repository of package meta data.
   */
  public void setPackageRepositoryURL(URL repositoryURL) {
    m_packageRepository = repositoryURL;
  }
  
  /**
   * Get the URL to the repository of package meta data.
   * 
   * @return the URL to the repository of package meta data.
   */
  public URL getPackageRepositoryURL() {
    return m_packageRepository;
  }
  
  /**
   * Set a proxy to use for accessing the internet (default is no proxy).
   * 
   * @param proxyToUse a proxy to use.
   */
  public void setProxy(Proxy proxyToUse) {
    m_httpProxy = proxyToUse;
  }
  
  /**
   * Get the proxy in use.
   * 
   * @return the proxy in use or null if no proxy is being used.
   */
  public Proxy getProxy() {
    return m_httpProxy;
  }
  
  /**
   * Set the user name for authentication with the proxy.
   * 
   * @param proxyUsername the user name to use for proxy authentication.
   */
  public void setProxyUsername(String proxyUsername) {
    m_proxyUsername = proxyUsername;
  }
  
  /**
   * Set the password for authentication with the proxy.
   * 
   * @param proxyPassword the password to use for proxy authentication.
   */
  public void setProxyPassword(String proxyPassword) {
    m_proxyPassword = proxyPassword;
  }
  
  /**
   * Gets an array of bytes containing a zip of all the repository
   * meta data and supporting files. Does *not* contain any package
   * archives etc., only a snapshot of the meta data. Could be used
   * by clients to establish a cache of meta data.
   * 
   * @param progress optional varargs parameter, that, if supplied, is expected to contain one
   * or more PrintStream objects to write progress to. 
   * @return a zip compressed array of bytes.
   * @throws Exception if the repository meta data can't be returned as a zip
   */
  public abstract byte[] getRepositoryPackageMetaDataOnlyAsZip(PrintStream... progress)
    throws Exception;
  
  /**
   * Get package information from the supplied package archive file.
   * 
   * @param packageArchivePath the path to the package archive file
   * @return a Package object encapsulating the package meta data.
   * @throws Exception if the package meta data can't be retrieved.
   */
  public abstract Package getPackageArchiveInfo(String packageArchivePath) throws Exception;
  
  /**
   * Get package information on the named installed package.
   * 
   * @param packageName the name of the package to get information about.
   * @return a Package object encapsulating the package meta data or null if the
   * package is not installed.
   * 
   * @throws Exception if the package meta data can't be retrieved.
   */
  public abstract Package getInstalledPackageInfo(String packageName) throws Exception;
  
  // public abstract Map getPackageArchiveInfo(URL packageURL);
  
  /**
   * Get package information on the named package from the repository. If multiple
   * versions of the package are available, it assumes that the most recent is
   * required.
   * 
   * @param packageName the name of the package to get information about.
   * @return a Package object encapsulating the package meta data.
   * @throws Exception if the package meta data can't be retrieved.
   */
  public abstract Package getRepositoryPackageInfo(String packageName) throws Exception;
  
  /**
   * Get package information on the named package from the repository.
   * 
   * @param packageName the name of the package to get information about.
   * @param version the version of the package to retrieve (may be null if not applicable).
   * @return a Package object encapsulating the package meta data.
   * @throws Exception if the package meta data can't be retrieved.
   */
  public abstract Package getRepositoryPackageInfo(String packageName, Object version) throws Exception;
  
  /**
   * Get a list of available versions of the named package.
   * 
   * @param packageName the name of the package to get versions.
   * @return a list of available versions (or null if not applicable)
   * @throws Exception if something goes wrong while trying to retrieve the list
   * of versions.
   */
  public abstract List<Object> getRepositoryPackageVersions(String packageName) throws Exception;
  
  /**
   * Get package information on the package at the given URL.
   * 
   * @param packageURL the URL to the package.
   * @return a Package object encapsulating the package meta data
   * @throws Exception if the package meta data can't be retrieved.
   */
  public abstract Package getURLPackageInfo(URL packageURL) throws Exception;
  
  /**
   * Install a package from an archive on the local file system.
   * 
   * @param packageArchivePath the path to the package archive file.
   * @param progress optional varargs parameter, that, if supplied, is expected to contain one
   * or more PrintStream objects to write progress to.
   * @return the name of the package installed
   * @throws Exception if the package can't be installed for some reason.
   */
  public abstract String installPackageFromArchive(String packageArchivePath, 
      PrintStream... progress) throws Exception;
  
  /**
   * Install a package sourced from the repository.
   * 
   * @param packageName the name of the package to install
   * @param version the version of the package to install (may be null if not applicable).
   * @param progress optional varargs parameter, that, if supplied, is expected to contain one
   * or more PrintStream objects to write progress to.
   * @throws Exception if the package can't be installed for some reason.
   */
  public abstract void installPackageFromRepository(String packageName, 
      Object version, PrintStream... progress) throws Exception;
  
  /**
   * Install a package sourced from a given URL.
   * 
   * @param packageURL the URL to the package.
   * @param progress optional varargs parameter, that, if supplied, is expected to contain one
   * or more PrintStream objects to write progress to.
   * @return the name of the package installed
   * @throws Exception if the package can't be installed for some reason.
   */
  public abstract String installPackageFromURL(URL packageURL, 
      PrintStream... progress) throws Exception;
  
  /**
   * Installs all the packages in the supplied list.
   * 
   * @param toInstall a list of Packages to install.
   * @param progress optional varargs parameter, that, if supplied, is expected to contain one
   * or more PrintStream objects to write progress to.
   * @throws Exception if something goes wrong during the installation process.
   */
  public abstract void installPackages(List<Package> toInstall, 
      PrintStream... progress) throws Exception;
  
  /**
   * Uninstall a package.
   * 
   * @param packageName the package to uninstall.
   * @param progress optional varargs parameter, that, if supplied, is expected to contain one
   * or more PrintStream objects to write progress to.
   * @throws Exception if the named package could not be removed for some reason.
   */
  public abstract void uninstallPackage(String packageName, 
      PrintStream... progress) throws Exception;
  
  /**
   * Get a list of installed packages.
   * 
   * @return a list of installed packages.
   * @throws Exception if a list of packages can't be determined.
   */
  public abstract List<Package> getInstalledPackages() throws Exception;
  
  /**
   * Get all packages that the system knows about (i.e. all packages
   * contained in the repository).
   * 
   * @param progress optional varargs parameter, that, if supplied is
   * expected to contain one or more PrintStream objects to write
   * progress to.
   * @return a list of all packages.
   * @throws Exception if a list of packages can't be determined.
   */
  public abstract List<Package> getAllPackages(PrintStream... progress) 
    throws Exception;
  
  /**
   * Get a list of packages that are not currently installed.
   * 
   * @return a list of packages that are not currently installed.
   * @throws Exception if a list of packages can't be determined.
   */
  public abstract List<Package> getAvailablePackages() throws Exception;
  
  /**
   * Gets a full list of packages (encapsulated in Dependency
   * objects) that are required by directly and indirectly by the
   * named target package. Also builds a Map of any packages that
   * are required by more than one package and where there is
   * a conflict of some sort (e.g. multiple conflicting versions).
   *  The keys of this map are package names
   * (strings), and each associated value is a list of Dependency 
   * objects.
   * 
   * @param target the package for which a list of dependencies is
   * required.
   * @param conflicts will hold any conflicts that are discovered while
   * building the full dependency list.
   * @return a list of packages that are directly and indirectly required
   * by the named target package.
   * @throws Exception if a problem occurs while building the dependency
   * list.
   */
  public abstract List<Dependency> 
    getAllDependenciesForPackage(Package target, 
        Map<String, List<Dependency>> conflicts) throws Exception;
}
