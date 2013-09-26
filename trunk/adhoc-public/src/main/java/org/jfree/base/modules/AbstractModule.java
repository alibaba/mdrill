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
 * -------------------
 * AbstractModule.java
 * -------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AbstractModule.java,v 1.7 2008/09/10 09:16:54 mungady Exp $
 *
 * Changes
 * -------
 * 05-Jul-2003 : Initial version
 * 07-Jun-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.base.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.jfree.util.ObjectUtilities;


/**
 * The abstract module provides a default implementation of the module interface.
 * <p>
 * The module can be specified in an external property file. The file name of this
 * specification defaults to "module.properties". This file is no real property file,
 * it follows a more complex rule set.
 * <p>
 * Lines starting with '#' are considered comments.
 * Section headers start at the beginning of the line, section properties
 * are indented with at least one whitespace.
 * <p>
 * The first section is always the module info and contains the basic module
 * properties like name, version and a short description.
 * <p>
 * <pre>
 * module-info:
 *   name: xls-export-gui
 *   producer: The JFreeReport project - www.jfree.org/jfreereport
 *   description: A dialog component for the Excel table export.
 *   version.major: 0
 *   version.minor: 84
 *   version.patchlevel: 0
 * </pre>
 * The properties name, producer and description are simple strings. They may
 * span multiple lines, but may not contain a colon (':').
 * The version properties are integer values.
 * <p>
 * This section may be followed by one or more "depends" sections. These
 * sections describe the base modules that are required to be active to make this
 * module work. The package manager will enforce this policy and will deactivate this
 * module if one of the base modules is missing.
 * <p>
 * <pre>
 * depends:
 *   module: org.jfree.report.modules.output.table.xls.XLSTableModule
 *   version.major: 0
 *   version.minor: 84
 * </pre>
 * <p>
 * The property module references to the module implementation of the module package.
 *
 * @author Thomas Morgner
 */
public abstract class AbstractModule extends DefaultModuleInfo implements Module
{
  /**
   * The reader helper provides a pushback interface for the reader to read and
   * buffer  complete lines.
   * @author Thomas Morgner
   */
  private static class ReaderHelper
  {
    /** The line buffer containing the last line read. */
    private String buffer;
    /** The reader from which to read the text. */
    private final BufferedReader reader;

    /**
     * Creates a new reader helper for the given buffered reader.
     *
     * @param reader the buffered reader that is the source of the text.
     */
    protected ReaderHelper(final BufferedReader reader)
    {
      this.reader = reader;
    }

    /**
     * Checks, whether the reader contains a next line. Returns false if the end
     * of the stream has been reached.
     *
     * @return true, if there is a next line to read, false otherwise.
     * @throws IOException if an error occures.
     */
    public boolean hasNext() throws IOException
    {
      if (this.buffer == null)
      {
        this.buffer = readLine();
      }
      return this.buffer != null;
    }

    /**
     * Returns the next line.
     *
     * @return the next line.
     */
    public String next()
    {
      final String line = this.buffer;
      this.buffer = null;
      return line;
    }

    /**
     * Pushes the given line back into the buffer. Only one line can be contained in
     * the buffer at one time.
     *
     * @param line the line that should be pushed back into the buffer.
     */
    public void pushBack(final String line)
    {
      this.buffer = line;
    }

    /**
     * Reads the next line skipping all comment lines.
     *
     * @return the next line, or null if no line can be read.
     * @throws IOException if an IO error occures.
     */
    protected String readLine() throws IOException
    {
      String line = this.reader.readLine();
      while (line != null && (line.length() == 0 || line.startsWith("#")))
      {
        // empty line or comment is ignored
        line = this.reader.readLine();
      }
      return line;
    }

    /**
     * Closes the reader.
     *
     * @throws IOException if an IOError occurs.
     */
    public void close() throws IOException
    {
      this.reader.close();
    }
  }

  /** The list of required modules. */
  private ModuleInfo[] requiredModules;
  /** The list of optional modules. */
  private ModuleInfo[] optionalModules;

  /** The name of the module. */
  private String name;
  /** A short description of the module. */
  private String description;
  /** The name of the module producer. */
  private String producer;
  /** The modules subsystem. */
  private String subsystem;

  /**
   * Default Constructor.
   */
  public AbstractModule()
  {
    setModuleClass(this.getClass().getName());
  }

  /**
   * Loads the default module description from the file "module.properties". This file
   * must be in the same package as the implementing class.
   *
   * @throws ModuleInitializeException if an error occurs.
   */
  protected void loadModuleInfo() throws ModuleInitializeException
  {
    final InputStream in = ObjectUtilities.getResourceRelativeAsStream
            ("module.properties", getClass());
    if (in == null)
    {
      throw new ModuleInitializeException
          ("File 'module.properties' not found in module package.");
    }

    loadModuleInfo(in);
  }

  /**
   * Loads the module descriptiong from the given input stream. The module description
   * must conform to the rules define in the class description. The file must be encoded
   * with "ISO-8859-1" (like property files).
   *
   * @param in the input stream from where to read the file
   * @throws ModuleInitializeException if an error occurs.
   */
  protected void loadModuleInfo(final InputStream in) throws ModuleInitializeException
  {
    if (in == null)
    {
      throw new NullPointerException
          ("Given InputStream is null.");
    }

    try
    {
      final ArrayList optionalModules = new ArrayList();
      final ArrayList dependendModules = new ArrayList();
      final ReaderHelper rh = new ReaderHelper(new BufferedReader
          (new InputStreamReader(in, "ISO-8859-1")));
      try
      {
        while (rh.hasNext())
        {
          final String lastLineRead = rh.next();
          if (lastLineRead.startsWith("module-info:"))
          {
            readModuleInfo(rh);
          }
          else if (lastLineRead.startsWith("depends:"))
          {
            dependendModules.add(readExternalModule(rh));
          }
          else if (lastLineRead.startsWith("optional:"))
          {
            optionalModules.add(readExternalModule(rh));
          }
          else
          {
            // we dont understand the current line, so we skip it ...
            // should we throw a parse exception instead?
          }
        }
      }
      finally
      {
        rh.close();
      }

      this.optionalModules = (ModuleInfo[])
          optionalModules.toArray(new ModuleInfo[optionalModules.size()]);

      this.requiredModules = (ModuleInfo[])
          dependendModules.toArray(new ModuleInfo[dependendModules.size()]);
    }
    catch (IOException ioe)
    {
      throw new ModuleInitializeException("Failed to load properties", ioe);
    }
  }

  /**
   * Reads a multiline value the stream. This will read the stream until
   * a new key is found or the end of the file is reached.
   *
   * @param reader the reader from where to read.
   * @param firstLine the first line (which was read elsewhere).
   * @return the complete value, never null
   * @throws IOException if an IO error occurs.
   */
  private String readValue(final ReaderHelper reader, String firstLine) throws IOException
  {
    final StringBuffer b = new StringBuffer(firstLine.trim());
    boolean newLine = true;
    while (isNextLineValueLine(reader))
    {
      firstLine = reader.next();
      final String trimedLine = firstLine.trim();
      if (trimedLine.length() == 0 && (newLine == false))
      {
        b.append ("\n");
        newLine = true;
      }
      else
      {
        if (newLine == false)
        {
          b.append(" ");
        }
        b.append(parseValue(trimedLine));
        newLine = false;
      }
    }
    return b.toString();
  }

  /**
   * Checks, whether the next line in the reader is a value line.
   *
   * @param reader from where to read the lines.
   * @return true, if the next line is a value line, false otherwise.
   * @throws IOException if an IO error occurs.
   */
  private boolean isNextLineValueLine (final ReaderHelper reader) throws IOException
  {
    if (reader.hasNext() == false)
    {
      return false;
    }
    final String firstLine = reader.next();
    if (firstLine == null)
    {
      return false;
    }
    if (parseKey(firstLine) != null)
    {
      reader.pushBack(firstLine);
      return false;
    }
    reader.pushBack(firstLine);
    return true;
  }

  /**
   * Reads the module definition header. This header contains information about
   * the module itself.
   *
   * @param reader the reader from where to read the content.
   * @throws IOException if an error occures
   */
  private void readModuleInfo(final ReaderHelper reader) throws IOException
  {
    while (reader.hasNext())
    {
      final String lastLineRead = reader.next();

      if (Character.isWhitespace(lastLineRead.charAt(0)) == false)
      {
        // break if the current character is no whitespace ...
        reader.pushBack(lastLineRead);
        return;
      }

      final String line = lastLineRead.trim();
      final String key = parseKey(line);
      if (key != null)
      {
        // parse error: Non data line does not contain a colon
        final String b = readValue(reader, parseValue(line.trim()));

        if ("name".equals(key))
        {
          setName(b);
        }
        else if ("producer".equals(key))
        {
          setProducer(b);
        }
        else if ("description".equals(key))
        {
          setDescription(b);
        }
        else if ("subsystem".equals(key))
        {
          setSubSystem(b);
        }
        else if ("version.major".equals(key))
        {
          setMajorVersion(b);
        }
        else if ("version.minor".equals(key))
        {
          setMinorVersion(b);
        }
        else if ("version.patchlevel".equals(key))
        {
          setPatchLevel(b);
        }
      }
    }
  }

  /**
   * Parses an string to find the key section of the line. This section ends with
   * an colon.
   *
   * @param line the line which to parse
   * @return the key or null if no key is found.
   */
  private String parseKey(final String line)
  {
    final int idx = line.indexOf(':');
    if (idx == -1)
    {
      return null;
    }
    return line.substring(0, idx);
  }

  /**
   * Parses the value section of the given line.
   *
   * @param line the line that should be parsed
   * @return the value, never null
   */
  private String parseValue(final String line)
  {
    final int idx = line.indexOf(':');
    if (idx == -1)
    {
      return line;
    }
    if ((idx + 1) == line.length())
    {
      return "";
    }
    return line.substring(idx + 1);
  }

  /**
   * Reads an external module description. This describes either an optional or
   * a required module.
   *
   * @param reader the reader from where to read the module
   * @return the read module, never null
   * @throws IOException if an error occures.
   */
  private DefaultModuleInfo readExternalModule(final ReaderHelper reader)
      throws IOException
  {
    final DefaultModuleInfo mi = new DefaultModuleInfo();

    while (reader.hasNext())
    {
      final String lastLineRead = reader.next();

      if (Character.isWhitespace(lastLineRead.charAt(0)) == false)
      {
        // break if the current character is no whitespace ...
        reader.pushBack(lastLineRead);
        return mi;
      }

      final String line = lastLineRead.trim();
      final String key = parseKey(line);
      if (key != null)
      {
        final String b = readValue(reader, parseValue(line));
        if ("module".equals(key))
        {
          mi.setModuleClass(b);
        }
        else if ("version.major".equals(key))
        {
          mi.setMajorVersion(b);
        }
        else if ("version.minor".equals(key))
        {
          mi.setMinorVersion(b);
        }
        else if ("version.patchlevel".equals(key))
        {
          mi.setPatchLevel(b);
        }
      }
    }
    return mi;
  }

  /**
   * Returns the name of this module.
   *
   * @see Module#getName()
   *
   * @return the module name
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Defines the name of the module.
   *
   * @param name the module name.
   */
  protected void setName(final String name)
  {
    this.name = name;
  }

  /**
   * Returns the module description.
   * @see Module#getDescription()
   *
   * @return the description of the module.
   */
  public String getDescription()
  {
    return this.description;
  }

  /**
   * Defines the description of the module.
   *
   * @param description the module's desciption.
   */
  protected void setDescription(final String description)
  {
    this.description = description;
  }

  /**
   * Returns the producer of the module.
   *
   * @see Module#getProducer()
   *
   * @return the producer.
   */
  public String getProducer()
  {
    return this.producer;
  }

  /**
   * Defines the producer of the module.
   *
   * @param producer the producer.
   */
  protected void setProducer(final String producer)
  {
    this.producer = producer;
  }

  /**
   * Returns a copy of the required modules array. This array contains all
   * description of the modules that need to be present to make this module work.
   * @see Module#getRequiredModules()
   *
   * @return an array of all required modules.
   */
  public ModuleInfo[] getRequiredModules()
  {
    final ModuleInfo[] retval = new ModuleInfo[this.requiredModules.length];
    System.arraycopy(this.requiredModules, 0, retval, 0, this.requiredModules.length);
    return retval;
  }

  /**
   * Returns a copy of the required modules array. This array contains all
   * description of the optional modules that may improve the modules functonality.
   * @see Module#getRequiredModules()
   *
   * @return an array of all required modules.
   */
  public ModuleInfo[] getOptionalModules()
  {
    final ModuleInfo[] retval = new ModuleInfo[this.optionalModules.length];
    System.arraycopy(this.optionalModules, 0, retval, 0, this.optionalModules.length);
    return retval;
  }

  /**
   * Defines the required module descriptions for this module.
   *
   * @param requiredModules the required modules.
   */
  protected void setRequiredModules(final ModuleInfo[] requiredModules)
  {
    this.requiredModules = new ModuleInfo[requiredModules.length];
    System.arraycopy(requiredModules, 0, this.requiredModules, 0, requiredModules.length);
  }

  /**
   * Defines the optional module descriptions for this module.
   *
   * @param optionalModules the optional modules.
   */
  public void setOptionalModules(final ModuleInfo[] optionalModules)
  {
    this.optionalModules = new ModuleInfo[optionalModules.length];
    System.arraycopy(optionalModules, 0, this.optionalModules, 0, optionalModules.length);
  }

  /**
   * Returns a string representation of this module.
   * @see java.lang.Object#toString()
   *
   * @return the string representation of this module for debugging purposes.
   */
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("Module : ");
    buffer.append(getName());
    buffer.append("\n");
    buffer.append("ModuleClass : ");
    buffer.append(getModuleClass());
    buffer.append("\n");
    buffer.append("Version: ");
    buffer.append(getMajorVersion());
    buffer.append(".");
    buffer.append(getMinorVersion());
    buffer.append(".");
    buffer.append(getPatchLevel());
    buffer.append("\n");
    buffer.append("Producer: ");
    buffer.append(getProducer());
    buffer.append("\n");
    buffer.append("Description: ");
    buffer.append(getDescription());
    buffer.append("\n");
    return buffer.toString();
  }

  /**
   * Tries to load a class to indirectly check for the existence
   * of a certain library.
   *
   * @param name the name of the library class.
   * @return true, if the class could be loaded, false otherwise.
   * @deprecated use the method that passes in a context-class.
   */
  protected static boolean isClassLoadable(final String name)
  {
    try
    {
      final ClassLoader loader = ObjectUtilities.getClassLoader(AbstractModule.class);
      if (loader == null)
      {
        // this should not happen .. If it happens, it measn we dont even have a system-classloader.
        return false;
      }
      loader.loadClass(name);
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  /**
   * Tries to load a class to indirectly check for the existence
   * of a certain library.
   *
   * @param name the name of the library class.
   * @param context the context class to get a classloader from.
   * @return true, if the class could be loaded, false otherwise.
   */
  protected static boolean isClassLoadable(final String name, final Class context)
  {
    try
    {
      ObjectUtilities.getClassLoader(context).loadClass(name);
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  /**
   * Configures the module by loading the configuration properties and
   * adding them to the package configuration.
   *
   * @param subSystem  the subsystem.
   */
  public void configure(final SubSystem subSystem)
  {
    final InputStream in = ObjectUtilities.getResourceRelativeAsStream
            ("configuration.properties", getClass());
    if (in == null)
    {
      return;
    }
    try
    {
      subSystem.getPackageManager().getPackageConfiguration().load(in);
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (IOException e)
      {
        // can be ignored ...
      }
    }
  }

  /**
   * Tries to load an module initializer and uses this initializer to initialize
   * the module.
   *
   * @param classname the class name of the initializer.
   * @throws ModuleInitializeException if an error occures
   * @deprecated Use the method that provides a class-context instead.
   */
  protected void performExternalInitialize(final String classname)
      throws ModuleInitializeException
  {
    try
    {
      final ModuleInitializer mi =
          (ModuleInitializer) ObjectUtilities.loadAndInstantiate(classname, AbstractModule.class, ModuleInitializer.class);
      if (mi == null)
      {
        throw new ModuleInitializeException("Failed to load specified initializer class.");
      }
      mi.performInit();
    }
    catch (ModuleInitializeException mie)
    {
      throw mie;
    }
    catch (Exception e)
    {
      throw new ModuleInitializeException("Failed to load specified initializer class.", e);
    }
  }

  /**
   * ???.
   *
   * @param classname ?
   * @param context ?
   * @throws ModuleInitializeException
   */
  protected void performExternalInitialize(final String classname, final Class context)
      throws ModuleInitializeException
  {
    try
    {
      final ModuleInitializer mi =
          (ModuleInitializer) ObjectUtilities.loadAndInstantiate(classname, context, ModuleInitializer.class);
      if (mi == null)
      {
        throw new ModuleInitializeException("Failed to load specified initializer class.");
      }
      mi.performInit();
    }
    catch (ModuleInitializeException mie)
    {
      throw mie;
    }
    catch (Exception e)
    {
      throw new ModuleInitializeException("Failed to load specified initializer class.", e);
    }
  }

  /**
   * Returns the modules subsystem. If this module is not part of an subsystem
   * then return the modules name, but never null.
   *
   * @return the name of the subsystem.
   */
  public String getSubSystem()
  {
    if (this.subsystem == null)
    {
      return getName();
    }
    return this.subsystem;
  }

  /**
   * Defines the subsystem name for this module.
   *
   * @param name the new name of the subsystem.
   */
  protected void setSubSystem (final String name)
  {
    this.subsystem = name;
  }
}
