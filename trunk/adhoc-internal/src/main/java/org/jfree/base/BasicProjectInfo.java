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
 * ---------------------
 * BasicProjectInfo.java
 * ---------------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: BasicProjectInfo.java,v 1.10 2008/09/10 09:23:34 mungady Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added source headers (DG);
 *
 */

package org.jfree.base;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jfree.util.ObjectUtilities;

/**
 * Basic project info.
 *
 * @author Thomas Morgner
 */
public class BasicProjectInfo extends Library {
    /**
     * A helper class, which simplifies the loading of optional library
     * implementations.
     */
    private static class OptionalLibraryHolder {
        private String libraryClass;
        private transient Library library;

        public OptionalLibraryHolder(final String libraryClass) {
            if (libraryClass == null) {
                throw new NullPointerException("LibraryClass must not be null.");
            }
            this.libraryClass = libraryClass;
        }

        public OptionalLibraryHolder(final Library library) {
          if (library == null) {
              throw new NullPointerException("Library must not be null.");
          }
          this.library = library;
          this.libraryClass = library.getClass().getName();
        }

        public String getLibraryClass() {
            return this.libraryClass;
        }

        public Library getLibrary() {
            if (this.library == null) {
                this.library = loadLibrary(this.libraryClass);
            }
            return this.library;
        }

        protected Library loadLibrary(final String classname) {
            if (classname == null) {
                return null;
            }
            try {
                final Class c = ObjectUtilities.getClassLoader(
                        getClass()).loadClass(classname);
                try {
                    final Method m = c.getMethod("getInstance", (Class[]) null);
                    return (Library) m.invoke(null, (Object[]) null);
                }
                catch(Exception e) {
                    // ok, fall back ...
                }
                return (Library) c.newInstance();
            }
            catch (Exception e) {
                // ok, this library has no 'getInstance()' method. Check the
                // default constructor ...
                return null;
            }
        }

    }

    /** The project copyright statement. */
    private String copyright;

    /** A list of libraries used by the project. */
    private List libraries;

    private List optionalLibraries;

    /**
     * Default constructor.
     */
    public BasicProjectInfo() {
        this.libraries = new ArrayList();
        this.optionalLibraries = new ArrayList();
    }

    /**
     * Creates a new library reference.
     *
     * @param name    the name.
     * @param version the version.
     * @param licence the licence.
     * @param info    the web address or other info.
     */
    public BasicProjectInfo(final String name, final String version,
                            final String licence, final String info) {
        this();
        setName(name);
        setVersion(version);
        setLicenceName(licence);
        setInfo(info);
    }

    /**
     * Creates a new project info instance.
     *
     * @param name  the project name.
     * @param version  the project version.
     * @param info  the project info (web site for example).
     * @param copyright  the copyright statement.
     * @param licenceName  the license name.
     */
    public BasicProjectInfo(final String name, final String version,
                            final String info, final String copyright,
                            final String licenceName) {
        this(name, version, licenceName, info);
        setCopyright(copyright);
    }

    /**
     * Returns the copyright statement.
     *
     * @return The copyright statement.
     */
    public String getCopyright() {
        return this.copyright;
    }

    /**
     * Sets the project copyright statement.
     *
     * @param copyright  the project copyright statement.
     */
    public void setCopyright(final String copyright) {
        this.copyright = copyright;
    }

    /**
     * Sets the project info string (for example, this could be the project URL).
     *
     * @param info  the info string.
     */
    public void setInfo(final String info) {
        super.setInfo(info);
    }

    /**
     * Sets the license name.
     *
     * @param licence  the license name.
     */
    public void setLicenceName(final String licence) {
        super.setLicenceName(licence);
    }

    /**
     * Sets the project name.
     *
     * @param name  the project name.
     */
    public void setName(final String name) {
        super.setName(name);
    }

    /**
     * Sets the project version number.
     *
     * @param version  the version number.
     */
    public void setVersion(final String version) {
        super.setVersion(version);
    }

    /**
     * Returns a list of libraries used by the project.
     *
     * @return the list of libraries.
     */
    public Library[] getLibraries() {
        return (Library[]) this.libraries.toArray
                (new Library[this.libraries.size()]);
    }

    /**
     * Adds a library.
     *
     * @param library  the library.
     */
    public void addLibrary (final Library library) {
        if (library == null) {
            throw new NullPointerException();
        }
        this.libraries.add(library);
    }

    /**
     * Returns a list of optional libraries used by the project.
     *
     * @return the list of libraries.
     */
    public Library[] getOptionalLibraries() {
        final ArrayList libraries = new ArrayList();
        for (int i = 0; i < this.optionalLibraries.size(); i++) {
          OptionalLibraryHolder holder =
                  (OptionalLibraryHolder) this.optionalLibraries.get(i);
          Library l = holder.getLibrary();
          if (l != null) {
              libraries.add(l);
          }
        }
        return (Library[]) libraries.toArray(new Library[libraries.size()]);
    }

    /**
     * Adds an optional library. These libraries will be booted, if they define
     * a boot class. A missing class is considered non-fatal and it is assumed
     * that the programm knows how to handle that.
     *
     * @param libraryClass  the library.
     */
    public void addOptionalLibrary (final String libraryClass) {
        if (libraryClass == null) {
            throw new NullPointerException("Library classname must be given.");
        }
        this.optionalLibraries.add
                (new OptionalLibraryHolder(libraryClass));
    }


    /**
     * Adds an optional library. These libraries will be booted, if they define
     * a boot class. A missing class is considered non-fatal and it is assumed
     * that the programm knows how to handle that.
     *
     * @param library  the library.
     */
    public void addOptionalLibrary (final Library library) {
      if (library == null) {
          throw new NullPointerException("Library must be given.");
      }
      this.optionalLibraries.add(new OptionalLibraryHolder(library));
  }
}
