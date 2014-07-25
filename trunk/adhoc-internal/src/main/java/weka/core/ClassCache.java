/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/**
 * ClassCache.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package weka.core;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A singleton that stores all classes on the classpath.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 6882 $
 */
public class ClassCache
  implements RevisionHandler {

  /**
   * For filtering classes.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 6882 $
   */
  public static class ClassFileFilter
    implements FileFilter {

    /**
     * Checks whether the file is a class.
     *
     * @param pathname	the file to check
     * @return		true if a class file
     */
    public boolean accept(File pathname) {
      return pathname.getName().endsWith(".class");
    }
  }

  /**
   * For filtering classes.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 6882 $
   */
  public static class DirectoryFilter
    implements FileFilter {

    /**
     * Checks whether the file is a directory.
     *
     * @param pathname	the file to check
     * @return		true if a directory
     */
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  }

  /** whether to output some debug information. */
  public final static boolean VERBOSE = false;

  /** the key for the default package. */
  public final static String DEFAULT_PACKAGE = "DEFAULT";

  /** for caching all classes on the class path (package-name &lt;-&gt; HashSet with classnames). */
  protected Hashtable<String,HashSet<String>> m_Cache;

  static {
    // notify if VERBOSE is still on
    if (VERBOSE)
      System.err.println(ClassCache.class.getName() + ": VERBOSE ON");
  }

  /**
   * Initializes the cache.
   */
  public ClassCache() {
    super();
    initialize();
  }

  /**
   * Fixes the classname, turns "/" and "\" into "." and removes ".class".
   *
   * @param classname	the classname to process
   * @return		the processed classname
   */
  protected String cleanUp(String classname) {
    String	result;

    result = classname;

    if (result.indexOf("/") > -1)
      result = result.replace("/", ".");
    if (result.indexOf("\\") > -1)
      result = result.replace("\\", ".");
    if (result.endsWith(".class"))
      result = result.substring(0, result.length() - 6);

    return result;
  }

  /**
   * Extracts the package name from the (clean) classname.
   *
   * @param classname	the classname to extract the package from
   * @return		the package name
   */
  protected String extractPackage(String classname) {
    if (classname.indexOf(".") > -1)
      return classname.substring(0, classname.lastIndexOf("."));
    else
      return DEFAULT_PACKAGE;
  }

  /**
   * Adds the classname to the cache.
   *
   * @param classname	the classname, automatically removes ".class" and
   * 			turns "/" or "\" into "."
   * @return		true if adding changed the cache
   */
  public boolean add(String classname) {
    String		pkgname;
    HashSet<String>	names;

    // classname and package
    classname = cleanUp(classname);
    pkgname   = extractPackage(classname);

    // add to cache
    if (!m_Cache.containsKey(pkgname))
      m_Cache.put(pkgname, new HashSet<String>());
    names = m_Cache.get(pkgname);
    return names.add(classname);
  }

  /**
   * Removes the classname from the cache.
   *
   * @param classname	the classname to remove
   * @return		true if the removal changed the cache
   */
  public boolean remove(String classname) {
    String		pkgname;
    HashSet<String>	names;

    classname = cleanUp(classname);
    pkgname   = extractPackage(classname);
    names     = m_Cache.get(pkgname);
    if (names != null)
      return names.remove(classname);
    else
      return false;
  }

  /**
   * Fills the class cache with classes in the specified directory.
   *
   * @param prefix	the package prefix so far, null for default package
   * @param dir		the directory to search
   */
  protected void initFromDir(String prefix, File dir) {
    File[]	files;

    // check classes
    files = dir.listFiles(new ClassFileFilter());
    for (File file: files) {
      if (prefix == null)
	add(file.getName());
      else
	add(prefix + "." + file.getName());
    }

    // descend in directories
    files = dir.listFiles(new DirectoryFilter());
    for (File file: files) {
      if (prefix == null)
	initFromDir(file.getName(), file);
      else
	initFromDir(prefix + "." + file.getName(), file);
    }
  }

  /**
   * Fills the class cache with classes in the specified directory.
   *
   * @param dir		the directory to search
   */
  protected void initFromDir(File dir) {
    if (VERBOSE)
      System.out.println("Analyzing directory: " + dir);
    initFromDir(null, dir);
  }

  /**
   * Fills the class cache with classes from the specified jar.
   *
   * @param file		the jar to inspect
   */
  protected void initFromJar(File file) {
    JarFile		jar;
    JarEntry		entry;
    Enumeration		enm;

    if (VERBOSE)
      System.out.println("Analyzing jar: " + file);

    try {
      jar = new JarFile(file);
      enm = jar.entries();
      while (enm.hasMoreElements()) {
        entry = (JarEntry) enm.nextElement();
        if (entry.getName().endsWith(".class"))
          add(entry.getName());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns all the stored packages.
   *
   * @return		the package names
   */
  public Enumeration<String> packages() {
    return m_Cache.keys();
  }

  /**
   * Returns all the classes for the given package.
   *
   * @param pkgname	the package to get the classes for
   * @return		the classes (sorted by name)
   */
  public HashSet<String> getClassnames(String pkgname) {
    if (m_Cache.containsKey(pkgname))
      return m_Cache.get(pkgname);
    else
      return new HashSet<String>();
  }

  /**
   * Initializes the cache.
   */
  protected void initialize() {
    String		part;
    File		file;
    URLClassLoader 	sysLoader;
    URL[] 		urls;

    m_Cache = new Hashtable<String,HashSet<String>>();

    sysLoader = (URLClassLoader) getClass().getClassLoader();
    urls      = sysLoader.getURLs();
    for (URL url: urls) {
      if (VERBOSE)
        System.out.println("Classpath-part: " + part);

      file = null;
      part = url.toString();
      if (part.startsWith("file:")) {
        part = part.replace(" ", "%20");
        try {
          file = new File(new java.net.URI(part));
        }
        catch (URISyntaxException e) {
          e.printStackTrace();
        }
      }
      else {
	file = new File(part);
      }
      if (file == null) {
	System.err.println("Skipping: " + part);
	continue;
      }

      // find classes
      if (file.isDirectory())
	initFromDir(file);
      else if (file.exists())
	initFromJar(file);
    }
  }

  /**
   * Find all classes that have the supplied matchText String in
   * their suffix.
   *
   * @param matchText 	the text to match
   * @return 		an array list of matching fully qualified class names.
   */
  public ArrayList<String> find(String matchText) {
    ArrayList<String>	result;
    Enumeration<String>	packages;
    Iterator<String>	names;
    String		name;

    result = new ArrayList<String>();

    packages = m_Cache.keys();
    while (packages.hasMoreElements()) {
      names = m_Cache.get(packages.nextElement()).iterator();
      while (names.hasNext()) {
	name = names.next();
	if (name.contains(matchText))
	  result.add(name);
      }
    }

    if (result.size() > 1)
      Collections.sort(result);

    return result;
  }

  /**
   * Returns the revision string.
   *
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 6882 $");
  }

  /**
   * For testing only.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    ClassCache cache = new ClassCache();
    Enumeration<String> packages = cache.packages();
    while (packages.hasMoreElements()) {
      String key = packages.nextElement();
      System.out.println(key + ": " + cache.getClassnames(key).size());
    }
  }
}
