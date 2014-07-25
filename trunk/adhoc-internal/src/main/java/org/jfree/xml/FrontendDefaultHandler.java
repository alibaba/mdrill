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
 * ------------
 * FrontendDefaultHandler.java
 * ------------
 * (C) Copyright 2002-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: FrontendDefaultHandler.java,v 1.9 2008/09/10 09:20:49 mungady Exp $
 *
 * Changes
 * -------
 * 02-Feb-2005 : Initial version.
 *
 */
package org.jfree.xml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Enumeration;

import org.jfree.util.Configuration;
import org.jfree.util.DefaultConfiguration;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The frontenddefault handler connects the SAX-backend with the handler implementations.
 * It must be the base class for all parser implementations used by the ParserFrontEnd.
 *
 * @author Thomas Morgner
 */
public abstract class FrontendDefaultHandler extends DefaultHandler implements Configuration {
    /**
     * A key for the content base.
     */
    public static final String CONTENTBASE_KEY = "content-base";

    /**
     * Storage for the parser configuration.
     */
    private DefaultConfiguration parserConfiguration;

    /**
     * The DocumentLocator can be used to resolve the current parse position.
     */
    private Locator locator;

    /**
     * The current comment handler used to receive xml comments.
     */
    private final CommentHandler commentHandler;

    /**
     * Default constructor.
     */
    protected FrontendDefaultHandler() {
        this.parserConfiguration = new DefaultConfiguration();
        this.commentHandler = new CommentHandler();
    }

    /**
     * Returns the comment handler that is used to collect comments.
     *
     * @return the comment handler.
     */
    public CommentHandler getCommentHandler() {
        return this.commentHandler;
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     * <p/>
     * The locator allows the application to determine the end position of
     * any document-related event, even if the parser is not reporting an
     * error. Typically, the application will use this information for
     * reporting its own errors (such as character content that does not
     * match an application's business rules). The information returned by
     * the locator is probably not sufficient for use with a search engine.
     *
     * @param locator the locator.
     */
    public void setDocumentLocator(final Locator locator) {
        this.locator = locator;
    }

    /**
     * Returns the current locator.
     *
     * @return the locator.
     */
    public Locator getLocator() {
        return this.locator;
    }

    /**
     * Returns the configuration property with the specified key.
     *
     * @param key the property key.
     * @return the property value.
     */
    public String getConfigProperty(final String key) {
        return getConfigProperty(key, null);
    }

    /**
     * Returns the configuration property with the specified key (or the specified default value
     * if there is no such property).
     * <p/>
     * If the property is not defined in this configuration, the code will lookup the property in
     * the parent configuration.
     *
     * @param key          the property key.
     * @param defaultValue the default value.
     * @return the property value.
     */
    public String getConfigProperty(final String key, final String defaultValue) {
        return this.parserConfiguration.getConfigProperty(key, defaultValue);
    }

    /**
     * Sets a parser configuration value.
     *
     * @param key   the key.
     * @param value the value.
     */
    public void setConfigProperty(final String key, final String value) {
        if (value == null) {
            this.parserConfiguration.remove(key);
        }
        else {
            this.parserConfiguration.setProperty(key, value);
        }
    }

    /**
     * Returns the configuration properties.
     *
     * @return An enumeration of the configuration properties.
     */
    public Enumeration getConfigProperties()
    {
      return this.parserConfiguration.getConfigProperties();
    }

  /**
     * Returns a new instance of the parser.
     *
     * @return a new instance of the parser.
     */
    public abstract FrontendDefaultHandler newInstance();

    /**
     * Returns all keys with the given prefix.
     *
     * @param prefix the prefix
     * @return the iterator containing all keys with that prefix
     */
    public Iterator findPropertyKeys(final String prefix) {
        return this.parserConfiguration.findPropertyKeys(prefix);
    }

    /**
     * Returns the parse result. This method is called at the end of the
     * parsing process and expects the generated object.
     *
     * @return the object.
     * @throws SAXException if something went wrong.
     */
    public abstract Object getResult() throws SAXException;

    /**
     * Gets the ContentBase used to resolve relative URLs.
     *
     * @return the current contentbase, or null if no contentBase is set.
     */
    public URL getContentBase() {
        final String contentBase = getConfigProperty(Parser.CONTENTBASE_KEY);
        if (contentBase == null) {
            return null;
        }
        try {
            return new URL(contentBase);
        }
        catch (MalformedURLException mfe) {
            throw new IllegalStateException("Content Base is illegal." + contentBase);
        }
    }

    /**
     * Returns a clone of this instance.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if there is a problem cloning.
     */
    public Object clone () throws CloneNotSupportedException
    {
      final FrontendDefaultHandler o = (FrontendDefaultHandler) super.clone();
      o.parserConfiguration = (DefaultConfiguration) this.parserConfiguration.clone();
      return o;
    }
}
