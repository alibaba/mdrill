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
 * ------------------------
 * JavaSourceCollector.java
 * ------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: JavaSourceCollector.java,v 1.3 2005/10/18 13:32:20 mungady Exp $
 *
 * Changes
 * -------------------------
 * 21.06.2003 : Initial version
 *
 */

package org.jfree.xml.generator;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.jfree.ui.ExtensionFileFilter;
import org.jfree.util.Log;
import org.jfree.util.ObjectUtilities;

/**
 * The class collects all class-files and loads the class objects named
 * by these files.
 */
public class JavaSourceCollector implements SourceCollector {

    /**
     * A file filter.
     */
    private static class CollectorFileFilter extends ExtensionFileFilter implements FileFilter {
        /**
         * Creates a new instance.
         * 
         * @param description  the file description.
         * @param extension  the file extension.
         */
        public CollectorFileFilter(final String description, final String extension) {
            super(description, extension);
        }
    }

    /** A file filter. */
    private CollectorFileFilter eff;
    
    /** The file list. */
    private ArrayList fileList;
    
    /** A list of ignored packages. */
    private ArrayList ignoredPackages;

    /** A list of ignored base classes. */
    private ArrayList ignoredBaseClasses;
    
    /** The start directory. */
    private File startDirectory;
    
    /** The initial package name. */
    private String initialPackageName;

    /**
     * Creates a new source collector.
     * 
     * @param startDirectory  the start directory.
     */
    public JavaSourceCollector(final File startDirectory) {
        this(startDirectory, "");
    }

    /**
     * Creates a new source collector.
     * 
     * @param startDirectory  the base directory.
     * @param packageName  the base package name.
     */
    public JavaSourceCollector(final File startDirectory, final String packageName) {
        this.eff = new CollectorFileFilter("<ignore>", ".java");
        this.fileList = new ArrayList();
        this.startDirectory = startDirectory;
        this.initialPackageName = packageName;
        this.ignoredPackages = new ArrayList();
        this.ignoredBaseClasses = new ArrayList();
    }

    /**
     * Adds a package that should be ignored.
     * 
     * @param pkg  the package name.
     */
    public void addIgnoredPackage(final String pkg) {
        Log.debug (new Log.SimpleMessage("Added IgnPackage: " , pkg));
        this.ignoredPackages.add(pkg);
    }

    /**
     * Adds a base class that should be ignored.
     * 
     * @param baseClass  the base class name.
     */
    public void addIgnoredBaseClass(final String baseClass) {
        final Class loadedClass = loadClass(baseClass);
        if (loadedClass != null) {
            Log.debug (new Log.SimpleMessage("Added IgnClass: " , baseClass));
            this.ignoredBaseClasses.add(loadedClass);
        }
    }

    /**
     * Adds a class to the list of ignored base classes.
     * 
     * @param baseClass  the class.
     */
    public void addIgnoredBaseClass(final Class baseClass) {
        this.ignoredBaseClasses.add(baseClass);
    }

    /**
     * Returns <code>true</code> if the named class is being ignored (because of the package that 
     * it belongs to), and <code>false</code> otherwise.
     * 
     * @param classname  the name of the class to test.
     * 
     * @return A boolean.
     */
    protected boolean isIgnoredPackage(final String classname) {
        for (int i = 0; i < this.ignoredPackages.size(); i++) {
            final String ignoredPackage = (String) this.ignoredPackages.get(i);
            if (classname.startsWith(ignoredPackage)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the named class is being ignored (because it is a descendant
     * of an ignored base class), and <code>false</code> otherwise.
     * 
     * @param c  the class name.
     * 
     * @return A boolean.
     */
    protected boolean isIgnoredBaseClass(final Class c) {
        for (int i = 0; i < this.ignoredBaseClasses.size(); i++) {
            final Class ignoredClass = (Class) this.ignoredBaseClasses.get(i);
            if (ignoredClass.isAssignableFrom(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collects the files/classes.
     */
    public void collectFiles() {
        collectFiles(this.startDirectory, this.initialPackageName);
    }

    /**
     * Collects the files/classes.
     * 
     * @param directory  the starting directory.
     * @param packageName  the initial package name.
     */
    protected void collectFiles(final File directory, final String packageName) {
        final File[] files = directory.listFiles(this.eff);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                collectFiles(files[i], buildJavaName(packageName, files[i].getName()));
            }
            else {
                final String fname = files[i].getName();
                final String className = fname.substring(0, fname.length() - 5);
                final String fullName = buildJavaName(packageName, className);
                if (isIgnoredPackage(fullName)) {
                    Log.debug (new Log.SimpleMessage("Do not process: Ignored: ", className));
                    continue;
                }
                final Class jclass = loadClass(fullName);
                if (jclass == null || isIgnoredBaseClass(jclass)) {
                    continue;
                }
                if (jclass.isInterface() || Modifier.isAbstract(jclass.getModifiers())) {
                    Log.debug (new Log.SimpleMessage("Do not process: Abstract: ", className));
                    continue;
                }
                if (!Modifier.isPublic(jclass.getModifiers())) {
                    Log.debug (new Log.SimpleMessage("Do not process: Not public: ", className));
                    continue;
                }
                this.fileList.add(jclass);
            }
        }
    }

    /**
     * Loads a class by its fully qualified name.
     * 
     * @param name  the class name.
     * 
     * @return The class (or <code>null</code> if there was a problem loading the class).
     */
    protected Class loadClass(final String name) {
        try {
            return ObjectUtilities.getClassLoader(JavaSourceCollector.class).loadClass(name);
        }
        catch (Exception e) {
            Log.warn (new Log.SimpleMessage("Do not process: Failed to load class:", name));
            return null;
        }
    }

    /**
     * Creates a fully qualified Java class or package name.
     * 
     * @param packageName  the base package name.
     * @param newPackage  the class/package name.
     * 
     * @return The fully qualified package/class name.
     */
    protected String buildJavaName(final String packageName, final String newPackage) {
        if (packageName.length() == 0) {
            return newPackage;
        }
        else {
            return packageName + "." + newPackage;
        }
    }

    /**
     * Returns the list of classes as an array.
     * 
     * @return The list of classes.
     */
    public Class[] getClasses() {
        return (Class[]) this.fileList.toArray(new Class[0]);
    }
    
}
