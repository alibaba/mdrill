/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
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
 * ReadOnlyIterator.java
 * ---------------------
 * (C)opyright 2003-2008, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ResourceBundleSupport.java,v 1.12 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes
 * -------
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

/**
 * An utility class to ease up using property-file resource bundles.
 * <p/>
 * The class support references within the resource bundle set to minimize the
 * occurence of duplicate keys. References are given in the format:
 * <pre>
 * a.key.name=@referenced.key
 * </pre>
 * <p/>
 * A lookup to a key in an other resource bundle should be written by
 * <pre>
 * a.key.name=@@resourcebundle_name@referenced.key
 * </pre>
 *
 * @author Thomas Morgner
 */
public class ResourceBundleSupport
{
  /**
   * The resource bundle that will be used for local lookups.
   */
  private ResourceBundle resources;

  /**
   * A cache for string values, as looking up the cache is faster than looking
   * up the value in the bundle.
   */
  private TreeMap cache;
  /**
   * The current lookup path when performing non local lookups. This prevents
   * infinite loops during such lookups.
   */
  private TreeSet lookupPath;

  /**
   * The name of the local resource bundle.
   */
  private String resourceBase;

  /**
   * The locale for this bundle.
   */
  private Locale locale;

  /**
   * Creates a new instance.
   *
   * @param locale  the locale.
   * @param baseName the base name of the resource bundle, a fully qualified
   *                 class name
   */
  public ResourceBundleSupport(final Locale locale, final String baseName)
  {
    this(locale, ResourceBundleWrapper.getBundle(baseName, locale), baseName);
  }

  /**
   * Creates a new instance.
   *
   * @param locale         the locale for which this resource bundle is
   *                       created.
   * @param resourceBundle the resourcebundle
   * @param baseName       the base name of the resource bundle, a fully
   *                       qualified class name
   */
  protected ResourceBundleSupport(final Locale locale,
                                  final ResourceBundle resourceBundle,
                                  final String baseName)
  {
    if (locale == null)
    {
      throw new NullPointerException("Locale must not be null");
    }
    if (resourceBundle == null)
    {
      throw new NullPointerException("Resources must not be null");
    }
    if (baseName == null)
    {
      throw new NullPointerException("BaseName must not be null");
    }
    this.locale = locale;
    this.resources = resourceBundle;
    this.resourceBase = baseName;
    this.cache = new TreeMap();
    this.lookupPath = new TreeSet();
  }

  /**
   * Creates a new instance.
   *
   * @param locale         the locale for which the resource bundle is
   *                       created.
   * @param resourceBundle the resourcebundle
   */
  public ResourceBundleSupport(final Locale locale,
                               final ResourceBundle resourceBundle)
  {
    this(locale, resourceBundle, resourceBundle.toString());
  }

  /**
   * Creates a new instance.
   *
   * @param baseName the base name of the resource bundle, a fully qualified
   *                 class name
   */
  public ResourceBundleSupport(final String baseName)
  {
    this(Locale.getDefault(), ResourceBundleWrapper.getBundle(baseName),
            baseName);
  }

  /**
   * Creates a new instance.
   *
   * @param resourceBundle the resourcebundle
   * @param baseName       the base name of the resource bundle, a fully
   *                       qualified class name
   */
  protected ResourceBundleSupport(final ResourceBundle resourceBundle,
                                  final String baseName)
  {
    this(Locale.getDefault(), resourceBundle, baseName);
  }

  /**
   * Creates a new instance.
   *
   * @param resourceBundle the resourcebundle
   */
  public ResourceBundleSupport(final ResourceBundle resourceBundle)
  {
    this(Locale.getDefault(), resourceBundle, resourceBundle.toString());
  }

  /**
   * The base name of the resource bundle.
   *
   * @return the resource bundle's name.
   */
  protected final String getResourceBase()
  {
    return this.resourceBase;
  }

  /**
   * Gets a string for the given key from this resource bundle or one of its
   * parents. If the key is a link, the link is resolved and the referenced
   * string is returned instead.
   *
   * @param key the key for the desired string
   * @return the string for the given key
   * @throws NullPointerException     if <code>key</code> is <code>null</code>
   * @throws MissingResourceException if no object for the given key can be
   *                                  found
   * @throws ClassCastException       if the object found for the given key is
   *                                  not a string
   */
  public synchronized String getString(final String key)
  {
    final String retval = (String) this.cache.get(key);
    if (retval != null)
    {
      return retval;
    }
    this.lookupPath.clear();
    return internalGetString(key);
  }

  /**
   * Performs the lookup for the given key. If the key points to a link the
   * link is resolved and that key is looked up instead.
   *
   * @param key the key for the string
   * @return the string for the given key
   */
  protected String internalGetString(final String key)
  {
    if (this.lookupPath.contains(key))
    {
      throw new MissingResourceException
          ("InfiniteLoop in resource lookup",
              getResourceBase(), this.lookupPath.toString());
    }
    final String fromResBundle = this.resources.getString(key);
    if (fromResBundle.startsWith("@@"))
    {
      // global forward ...
      final int idx = fromResBundle.indexOf('@', 2);
      if (idx == -1)
      {
        throw new MissingResourceException
            ("Invalid format for global lookup key.", getResourceBase(), key);
      }
      try
      {
        final ResourceBundle res = ResourceBundleWrapper.getBundle
            (fromResBundle.substring(2, idx));
        return res.getString(fromResBundle.substring(idx + 1));
      }
      catch (Exception e)
      {
        Log.error("Error during global lookup", e);
        throw new MissingResourceException
            ("Error during global lookup", getResourceBase(), key);
      }
    }
    else if (fromResBundle.startsWith("@"))
    {
      // local forward ...
      final String newKey = fromResBundle.substring(1);
      this.lookupPath.add(key);
      final String retval = internalGetString(newKey);

      this.cache.put(key, retval);
      return retval;
    }
    else
    {
      this.cache.put(key, fromResBundle);
      return fromResBundle;
    }
  }

  /**
   * Returns an scaled icon suitable for buttons or menus.
   *
   * @param key   the name of the resource bundle key
   * @param large true, if the image should be scaled to 24x24, or false for
   *              16x16
   * @return the icon.
   */
  public Icon getIcon(final String key, final boolean large)
  {
    final String name = getString(key);
    return createIcon(name, true, large);
  }

  /**
   * Returns an unscaled icon.
   *
   * @param key the name of the resource bundle key
   * @return the icon.
   */
  public Icon getIcon(final String key)
  {
    final String name = getString(key);
    return createIcon(name, false, false);
  }

  /**
   * Returns the mnemonic stored at the given resourcebundle key. The mnemonic
   * should be either the symbolic name of one of the KeyEvent.VK_* constants
   * (without the 'VK_') or the character for that key.
   * <p/>
   * For the enter key, the resource bundle would therefore either contain
   * "ENTER" or "\n".
   * <pre>
   * a.resourcebundle.key=ENTER
   * an.other.resourcebundle.key=\n
   * </pre>
   *
   * @param key the resourcebundle key
   * @return the mnemonic
   */
  public Integer getMnemonic(final String key)
  {
    final String name = getString(key);
    return createMnemonic(name);
  }

  /**
   * Returns an optional mnemonic.
   *
   * @param key  the key.
   *
   * @return The mnemonic.
   */
  public Integer getOptionalMnemonic(final String key)
  {
    final String name = getString(key);
    if (name != null && name.length() > 0)
    {
      return createMnemonic(name);
    }
    return null;
  }

  /**
   * Returns the keystroke stored at the given resourcebundle key.
   * <p/>
   * The keystroke will be composed of a simple key press and the plattform's
   * MenuKeyMask.
   * <p/>
   * The keystrokes character key should be either the symbolic name of one of
   * the KeyEvent.VK_* constants or the character for that key.
   * <p/>
   * For the 'A' key, the resource bundle would therefore either contain
   * "VK_A" or "a".
   * <pre>
   * a.resourcebundle.key=VK_A
   * an.other.resourcebundle.key=a
   * </pre>
   *
   * @param key the resourcebundle key
   * @return the mnemonic
   * @see Toolkit#getMenuShortcutKeyMask()
   */
  public KeyStroke getKeyStroke(final String key)
  {
    return getKeyStroke(key, getMenuKeyMask());
  }

  /**
   * Returns an optional key stroke.
   *
   * @param key  the key.
   *
   * @return The key stroke.
   */
  public KeyStroke getOptionalKeyStroke(final String key)
  {
    return getOptionalKeyStroke(key, getMenuKeyMask());
  }

  /**
   * Returns the keystroke stored at the given resourcebundle key.
   * <p/>
   * The keystroke will be composed of a simple key press and the given
   * KeyMask. If the KeyMask is zero, a plain Keystroke is returned.
   * <p/>
   * The keystrokes character key should be either the symbolic name of one of
   * the KeyEvent.VK_* constants or the character for that key.
   * <p/>
   * For the 'A' key, the resource bundle would therefore either contain
   * "VK_A" or "a".
   * <pre>
   * a.resourcebundle.key=VK_A
   * an.other.resourcebundle.key=a
   * </pre>
   *
   * @param key the resourcebundle key.
   * @param mask  the mask.
   *
   * @return the mnemonic
   * @see Toolkit#getMenuShortcutKeyMask()
   */
  public KeyStroke getKeyStroke(final String key, final int mask)
  {
    final String name = getString(key);
    return KeyStroke.getKeyStroke(createMnemonic(name).intValue(), mask);
  }

  /**
   * Returns an optional key stroke.
   *
   * @param key  the key.
   * @param mask  the mask.
   *
   * @return The key stroke.
   */
  public KeyStroke getOptionalKeyStroke(final String key, final int mask)
  {
    final String name = getString(key);

    if (name != null && name.length() > 0)
    {
      return KeyStroke.getKeyStroke(createMnemonic(name).intValue(), mask);
    }
    return null;
  }

  /**
   * Returns a JMenu created from a resource bundle definition.
   * <p/>
   * The menu definition consists of two keys, the name of the menu and the
   * mnemonic for that menu. Both keys share a common prefix, which is
   * extended by ".name" for the name of the menu and ".mnemonic" for the
   * mnemonic.
   * <p/>
   * <pre>
   * # define the file menu
   * menu.file.name=File
   * menu.file.mnemonic=F
   * </pre>
   * The menu definition above can be used to create the menu by calling
   * <code>createMenu ("menu.file")</code>.
   *
   * @param keyPrefix the common prefix for that menu
   * @return the created menu
   */
  public JMenu createMenu(final String keyPrefix)
  {
    final JMenu retval = new JMenu();
    retval.setText(getString(keyPrefix + ".name"));
    retval.setMnemonic(getMnemonic(keyPrefix + ".mnemonic").intValue());
    return retval;
  }

  /**
   * Returns a URL pointing to a resource located in the classpath. The
   * resource is looked up using the given key.
   * <p/>
   * Example: The load a file named 'logo.gif' which is stored in a java
   * package named 'org.jfree.resources':
   * <pre>
   * mainmenu.logo=org/jfree/resources/logo.gif
   * </pre>
   * The URL for that file can be queried with: <code>getResource("mainmenu.logo");</code>.
   *
   * @param key the key for the resource
   * @return the resource URL
   */
  public URL getResourceURL(final String key)
  {
    final String name = getString(key);
    final URL in = ObjectUtilities.getResource(name, ResourceBundleSupport.class);
    if (in == null)
    {
      Log.warn("Unable to find file in the class path: " + name + "; key=" + key);
    }
    return in;
  }


  /**
   * Attempts to load an image from classpath. If this fails, an empty image
   * icon is returned.
   *
   * @param resourceName the name of the image. The name should be a global
   *                     resource name.
   * @param scale        true, if the image should be scaled, false otherwise
   * @param large        true, if the image should be scaled to 24x24, or
   *                     false for 16x16
   * @return the image icon.
   */
  private ImageIcon createIcon(final String resourceName, final boolean scale,
                               final boolean large)
  {
    final URL in = ObjectUtilities.getResource(resourceName, ResourceBundleSupport.class);
    ;
    if (in == null)
    {
      Log.warn("Unable to find file in the class path: " + resourceName);
      return new ImageIcon(createTransparentImage(1, 1));
    }
    final Image img = Toolkit.getDefaultToolkit().createImage(in);
    if (img == null)
    {
      Log.warn("Unable to instantiate the image: " + resourceName);
      return new ImageIcon(createTransparentImage(1, 1));
    }
    if (scale)
    {
      if (large)
      {
        return new ImageIcon(img.getScaledInstance(24, 24, Image.SCALE_SMOOTH));
      }
      return new ImageIcon(img.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
    }
    return new ImageIcon(img);
  }

  /**
   * Creates the Mnemonic from the given String. The String consists of the
   * name of the VK constants of the class KeyEvent without VK_*.
   *
   * @param keyString the string
   * @return the mnemonic as integer
   */
  private Integer createMnemonic(final String keyString)
  {
    if (keyString == null)
    {
      throw new NullPointerException("Key is null.");
    }
    if (keyString.length() == 0)
    {
      throw new IllegalArgumentException("Key is empty.");
    }
    int character = keyString.charAt(0);
    if (keyString.startsWith("VK_"))
    {
      try
      {
        final Field f = KeyEvent.class.getField(keyString);
        final Integer keyCode = (Integer) f.get(null);
        character = keyCode.intValue();
      }
      catch (Exception nsfe)
      {
        // ignore the exception ...
      }
    }
    return new Integer(character);
  }

  /**
   * Returns the plattforms default menu shortcut keymask.
   *
   * @return the default key mask.
   */
  private int getMenuKeyMask()
  {
    try
    {
      return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }
    catch (UnsupportedOperationException he)
    {
      // headless exception extends UnsupportedOperation exception,
      // but the HeadlessException is not defined in older JDKs...
      return InputEvent.CTRL_MASK;
    }
  }

  /**
   * Creates a transparent image.  These can be used for aligning menu items.
   *
   * @param width  the width.
   * @param height the height.
   * @return the created transparent image.
   */
  private BufferedImage createTransparentImage(final int width,
                                               final int height)
  {
    final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final int[] data = img.getRGB(0, 0, width, height, null, 0, width);
    Arrays.fill(data, 0x00000000);
    img.setRGB(0, 0, width, height, data, 0, width);
    return img;
  }

  /**
   * Creates a transparent icon. The Icon can be used for aligning menu
   * items.
   *
   * @param width  the width of the new icon
   * @param height the height of the new icon
   * @return the created transparent icon.
   */
  public Icon createTransparentIcon(final int width, final int height)
  {
    return new ImageIcon(createTransparentImage(width, height));
  }

  /**
   * Formats the message stored in the resource bundle (using a
   * MessageFormat).
   *
   * @param key       the resourcebundle key
   * @param parameter the parameter for the message
   * @return the formated string
   */
  public String formatMessage(final String key, final Object parameter)
  {
    return formatMessage(key, new Object[]{parameter});
  }

  /**
   * Formats the message stored in the resource bundle (using a
   * MessageFormat).
   *
   * @param key  the resourcebundle key
   * @param par1 the first parameter for the message
   * @param par2 the second parameter for the message
   * @return the formated string
   */
  public String formatMessage(final String key,
                              final Object par1,
                              final Object par2)
  {
    return formatMessage(key, new Object[]{par1, par2});
  }

  /**
   * Formats the message stored in the resource bundle (using a
   * MessageFormat).
   *
   * @param key        the resourcebundle key
   * @param parameters the parameter collection for the message
   * @return the formated string
   */
  public String formatMessage(final String key, final Object[] parameters)
  {
    final MessageFormat format = new MessageFormat(getString(key));
    format.setLocale(getLocale());
    return format.format(parameters);
  }

  /**
   * Returns the current locale for this resource bundle.
   *
   * @return the locale.
   */
  public Locale getLocale()
  {
    return this.locale;
  }
}
