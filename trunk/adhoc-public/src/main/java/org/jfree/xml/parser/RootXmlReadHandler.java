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
 * -----------------------
 * RootXmlReadHandler.java
 * -----------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: RootXmlReadHandler.java,v 1.9 2008/09/10 09:20:16 mungady Exp $
 *
 * Changes (from 25-Nov-2003)
 * --------------------------
 * 25-Nov-2003 : Added Javadocs (DG);
 * 22-Feb-2005 : Fixed a bug when ending nested tags with the same tagname.
 */
package org.jfree.xml.parser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.jfree.util.ObjectUtilities;
import org.jfree.xml.FrontendDefaultHandler;
import org.jfree.xml.ParseException;
import org.jfree.xml.ElementDefinitionException;
import org.jfree.xml.parser.coretypes.BasicStrokeReadHandler;
import org.jfree.xml.parser.coretypes.ColorReadHandler;
import org.jfree.xml.parser.coretypes.FontReadHandler;
import org.jfree.xml.parser.coretypes.GenericReadHandler;
import org.jfree.xml.parser.coretypes.GradientPaintReadHandler;
import org.jfree.xml.parser.coretypes.InsetsReadHandler;
import org.jfree.xml.parser.coretypes.ListReadHandler;
import org.jfree.xml.parser.coretypes.Point2DReadHandler;
import org.jfree.xml.parser.coretypes.Rectangle2DReadHandler;
import org.jfree.xml.parser.coretypes.RenderingHintsReadHandler;
import org.jfree.xml.parser.coretypes.StringReadHandler;
import org.jfree.xml.util.ManualMappingDefinition;
import org.jfree.xml.util.MultiplexMappingDefinition;
import org.jfree.xml.util.MultiplexMappingEntry;
import org.jfree.xml.util.ObjectFactory;
import org.jfree.xml.util.SimpleObjectFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A base root SAX handler.
 */
public abstract class RootXmlReadHandler extends FrontendDefaultHandler {

    /** The current handlers. */
    private Stack currentHandlers;

    /** ??. */
    private Stack outerScopes;

    /** The root handler. */
    private XmlReadHandler rootHandler;

    /** The object registry. */
    private HashMap objectRegistry;

    /** Maps classes to handlers. */
    private SimpleObjectFactory classToHandlerMapping;

    private boolean rootHandlerInitialized;

    /**
     * Creates a new root SAX handler.
     */
    public RootXmlReadHandler() {
        this.objectRegistry = new HashMap();
        this.classToHandlerMapping = new SimpleObjectFactory();
    }

    /**
     * Adds the default mappings.
     */
    protected void addDefaultMappings () {

        final MultiplexMappingEntry[] paintEntries = new MultiplexMappingEntry[2];
        paintEntries[0] = new MultiplexMappingEntry("color", Color.class.getName());
        paintEntries[1] = new MultiplexMappingEntry("gradientPaint", GradientPaint.class.getName());
        addMultiplexMapping(Paint.class, "type", paintEntries);
        addManualMapping(Color.class, ColorReadHandler.class);
        addManualMapping(GradientPaint.class, GradientPaintReadHandler.class);

        final MultiplexMappingEntry[] point2DEntries = new MultiplexMappingEntry[2];
        point2DEntries[0] = new MultiplexMappingEntry("float", Point2D.Float.class.getName());
        point2DEntries[1] = new MultiplexMappingEntry("double", Point2D.Double.class.getName());
        addMultiplexMapping(Point2D.class, "type", point2DEntries);
        addManualMapping(Point2D.Float.class, Point2DReadHandler.class);
        addManualMapping(Point2D.Double.class, Point2DReadHandler.class);

        final MultiplexMappingEntry[] rectangle2DEntries = new MultiplexMappingEntry[2];
        rectangle2DEntries[0] = new MultiplexMappingEntry(
            "float", Rectangle2D.Float.class.getName()
        );
        rectangle2DEntries[1] = new MultiplexMappingEntry(
            "double", Rectangle2D.Double.class.getName()
        );
        addMultiplexMapping(Rectangle2D.class, "type", rectangle2DEntries);
        addManualMapping(Rectangle2D.Float.class, Rectangle2DReadHandler.class);
        addManualMapping(Rectangle2D.Double.class, Rectangle2DReadHandler.class);

        // Handle list types
        final MultiplexMappingEntry[] listEntries = new MultiplexMappingEntry[4];
        listEntries[0] = new MultiplexMappingEntry("array-list", ArrayList.class.getName());
        listEntries[1] = new MultiplexMappingEntry("linked-list", LinkedList.class.getName());
        listEntries[2] = new MultiplexMappingEntry("vector", Vector.class.getName());
        listEntries[3] = new MultiplexMappingEntry("stack", Stack.class.getName());
        addMultiplexMapping(List.class, "type", listEntries);
        addManualMapping(LinkedList.class, ListReadHandler.class);
        addManualMapping(Vector.class, ListReadHandler.class);
        addManualMapping(ArrayList.class, ListReadHandler.class);
        addManualMapping(Stack.class, ListReadHandler.class);

        final MultiplexMappingEntry[] strokeEntries = new MultiplexMappingEntry[1];
        strokeEntries[0] = new MultiplexMappingEntry("basic", BasicStroke.class.getName());
        addMultiplexMapping(Stroke.class, "type", strokeEntries);
        addManualMapping(BasicStroke.class, BasicStrokeReadHandler.class);

        addManualMapping(Font.class, FontReadHandler.class);
        addManualMapping(Insets.class, InsetsReadHandler.class);
        addManualMapping(RenderingHints.class, RenderingHintsReadHandler.class);
        addManualMapping(String.class, StringReadHandler.class);
    }

    /**
     * Returns the object factory.
     *
     * @return The object factory.
     */
    public abstract ObjectFactory getFactoryLoader();

    /**
     * Adds a mapping between a class and the handler for the class.
     *
     * @param classToRead  the class.
     * @param handler  the handler class.
     */
    protected void addManualMapping(final Class classToRead, final Class handler) {
        if (handler == null) {
            throw new NullPointerException("handler must not be null.");
        }
        if (classToRead == null) {
            throw new NullPointerException("classToRead must not be null.");
        }
        if (!XmlReadHandler.class.isAssignableFrom(handler)) {
            throw new IllegalArgumentException("The given handler is no XmlReadHandler.");
        }
        this.classToHandlerMapping.addManualMapping
            (new ManualMappingDefinition(classToRead, handler.getName(), null));
    }

    /**
     * Adds a multiplex mapping.
     *
     * @param baseClass  the base class.
     * @param typeAttr  the type attribute.
     * @param mdef  the mapping entry.
     */
    protected void addMultiplexMapping(final Class baseClass,
                                       final String typeAttr,
                                       final MultiplexMappingEntry[] mdef) {

        this.classToHandlerMapping.addMultiplexMapping(
            new MultiplexMappingDefinition(baseClass, typeAttr, mdef)
        );
    }

    /**
     * Adds an object to the registry.
     *
     * @param key  the key.
     * @param value  the object.
     */
    public void setHelperObject(final String key, final Object value) {
        if (value == null) {
            this.objectRegistry.remove(key);
        }
        else {
            this.objectRegistry.put(key, value);
        }
    }

    /**
     * Returns an object from the registry.
     *
     * @param key  the key.
     *
     * @return The object.
     */
    public Object getHelperObject(final String key) {
        return this.objectRegistry.get(key);
    }

    /**
     * Creates a SAX handler for the specified class.
     *
     * @param classToRead  the class.
     * @param tagName  the tag name.
     * @param atts  the attributes.
     *
     * @return a SAX handler.
     *
     * @throws XmlReaderException if there is a problem with the reader.
     */
    public XmlReadHandler createHandler(final Class classToRead, final String tagName, final Attributes atts)
        throws XmlReaderException {

        final XmlReadHandler retval = findHandlerForClass(classToRead, atts, new ArrayList());
        if (retval == null) {
            throw new NullPointerException("Unable to find handler for class: " + classToRead);
        }
        retval.init(this, tagName);
        return retval;
    }

    /**
     * Finds a handler for the specified class.
     *
     * @param classToRead  the class to be read.
     * @param atts  the attributes.
     * @param history  the history.
     *
     * @return A handler for the specified class.
     *
     * @throws XmlReaderException if there is a problem with the reader.
     */
    private XmlReadHandler findHandlerForClass(final Class classToRead, final Attributes atts,
                                               final ArrayList history)
        throws XmlReaderException {
        final ObjectFactory genericFactory = getFactoryLoader();

        if (history.contains(classToRead)) {
            throw new IllegalStateException("Circular reference detected: " + history);
        }
        history.add(classToRead);
        // check the manual mappings ...
        ManualMappingDefinition manualDefinition =
            this.classToHandlerMapping.getManualMappingDefinition(classToRead);
        if (manualDefinition == null) {
            manualDefinition = genericFactory.getManualMappingDefinition(classToRead);
        }
        if (manualDefinition != null) {
            // Log.debug ("Locating handler for " + manualDefinition.getBaseClass());
            return loadHandlerClass(manualDefinition.getReadHandler());
        }

        // check whether a multiplexer is defined ...
        // find multiplexer for this class...
        MultiplexMappingDefinition mplex =
            getFactoryLoader().getMultiplexDefinition(classToRead);
        if (mplex == null) {
            mplex = this.classToHandlerMapping.getMultiplexDefinition(classToRead);
        }
        if (mplex != null) {
            final String attributeValue = atts.getValue(mplex.getAttributeName());
            if (attributeValue == null) {
                throw new XmlReaderException(
                    "Multiplexer type attribute is not defined: " + mplex.getAttributeName()
                    + " for " + classToRead
                );
            }
            final MultiplexMappingEntry entry =
                mplex.getEntryForType(attributeValue);
            if (entry == null) {
                throw new XmlReaderException(
                    "Invalid type attribute value: " + mplex.getAttributeName() + " = "
                    + attributeValue
                );
            }
            final Class c = loadClass(entry.getTargetClass());
            if (!c.equals(mplex.getBaseClass())) {
                return findHandlerForClass(c, atts, history);
            }
        }

        // check for generic classes ...
        // and finally try the generic handler matches ...
        if (this.classToHandlerMapping.isGenericHandler(classToRead)) {
            return new GenericReadHandler
                (this.classToHandlerMapping.getFactoryForClass(classToRead));
        }
        if (getFactoryLoader().isGenericHandler(classToRead)) {
            return new GenericReadHandler
                (getFactoryLoader().getFactoryForClass(classToRead));
        }
        return null;
    }

    /**
     * Sets the root SAX handler.
     *
     * @param handler  the SAX handler.
     */
    protected void setRootHandler(final XmlReadHandler handler) {
        this.rootHandler = handler;
        this.rootHandlerInitialized = false;
    }

    /**
     * Returns the root SAX handler.
     *
     * @return the root SAX handler.
     */
    protected XmlReadHandler getRootHandler() {
        return this.rootHandler;
    }

    /**
     * Start a new handler stack and delegate to another handler.
     *
     * @param handler  the handler.
     * @param tagName  the tag name.
     * @param attrs  the attributes.
     *
     * @throws XmlReaderException if there is a problem with the reader.
     * @throws SAXException if there is a problem with the parser.
     */
    public void recurse(final XmlReadHandler handler, final String tagName, final Attributes attrs)
        throws XmlReaderException, SAXException {

        this.outerScopes.push(this.currentHandlers);
        this.currentHandlers = new Stack();
        this.currentHandlers.push(handler);
        handler.startElement(tagName, attrs);

    }

    /**
     * Delegate to another handler.
     *
     * @param handler  the new handler.
     * @param tagName  the tag name.
     * @param attrs  the attributes.
     *
     * @throws XmlReaderException if there is a problem with the reader.
     * @throws SAXException if there is a problem with the parser.
     */
    public void delegate(final XmlReadHandler handler, final String tagName, final Attributes attrs)
        throws XmlReaderException, SAXException {
        this.currentHandlers.push(handler);
        handler.init(this, tagName);
        handler.startElement(tagName, attrs);
    }

    /**
     * Hand control back to the previous handler.
     *
     * @param tagName  the tagname.
     *
     * @throws SAXException if there is a problem with the parser.
     * @throws XmlReaderException if there is a problem with the reader.
     */
    public void unwind(final String tagName) throws SAXException, XmlReaderException {
      // remove current handler from stack ..
        this.currentHandlers.pop();
        if (this.currentHandlers.isEmpty() && !this.outerScopes.isEmpty()) {
            // if empty, but "recurse" had been called, then restore the old handler stack ..
            // but do not end the recursed element ..
            this.currentHandlers = (Stack) this.outerScopes.pop();
        }
        else if (!this.currentHandlers.isEmpty()) {
            // if there are some handlers open, close them too (these handlers must be delegates)..
            getCurrentHandler().endElement(tagName);
        }
    }

    /**
     * Returns the current handler.
     *
     * @return The current handler.
     */
    protected XmlReadHandler getCurrentHandler() {
        return (XmlReadHandler) this.currentHandlers.peek();
    }

    /**
     * Starts processing a document.
     *
     * @throws SAXException not in this implementation.
     */
    public void startDocument() throws SAXException {
        this.outerScopes = new Stack();
        this.currentHandlers = new Stack();
        this.currentHandlers.push(this.rootHandler);
    }

    /**
     * Starts processing an element.
     *
     * @param uri  the URI.
     * @param localName  the local name.
     * @param qName  the qName.
     * @param attributes  the attributes.
     *
     * @throws SAXException if there is a parsing problem.
     */
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes)
        throws SAXException {
        if (this.rootHandlerInitialized == false) {
            this.rootHandler.init(this, qName);
            this.rootHandlerInitialized = true;
        }

        try {
            getCurrentHandler().startElement(qName, attributes);
        }
        catch (XmlReaderException xre) {
            throw new ParseException(xre, getLocator());
        }
    }

    /**
     * Process character data.
     *
     * @param ch  the character buffer.
     * @param start  the start index.
     * @param length  the length of the character data.
     *
     * @throws SAXException if there is a parsing error.
     */
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        try {
            getCurrentHandler().characters(ch, start, length);
        }
        catch (SAXException se) {
            throw se;
        }
        catch (Exception e) {
            throw new ParseException(e, getLocator());
        }
    }

    /**
     * Finish processing an element.
     *
     * @param uri  the URI.
     * @param localName  the local name.
     * @param qName  the qName.
     *
     * @throws SAXException if there is a parsing error.
     */
    public void endElement(final String uri, final String localName, final String qName)
        throws SAXException {
        try {
            getCurrentHandler().endElement(qName);
        }
        catch (XmlReaderException xre) {
            throw new ParseException(xre, getLocator());
        }
    }

    /**
     * Loads the given class, and ignores all exceptions which may occur
     * during the loading. If the class was invalid, null is returned instead.
     *
     * @param className the name of the class to be loaded.
     * @return the class or null.
     * @throws XmlReaderException if there is a reader error.
     */
    protected XmlReadHandler loadHandlerClass(final String className)
        throws XmlReaderException {
        try {
            final Class c = loadClass(className);
            return (XmlReadHandler) c.newInstance();
        }
        catch (Exception e) {
            // ignore buggy classes for now ..
            throw new XmlReaderException("LoadHanderClass: Unable to instantiate " + className, e);
        }
    }

    /**
     * Loads the given class, and ignores all exceptions which may occur
     * during the loading. If the class was invalid, null is returned instead.
     *
     * @param className the name of the class to be loaded.
     * @return the class or null.
     * @throws XmlReaderException if there is a reader error.
     */
    protected Class loadClass(final String className)
        throws XmlReaderException {
        if (className == null) {
            throw new XmlReaderException("LoadHanderClass: Class name not defined");
        }
        try {
            final Class c = ObjectUtilities.getClassLoader(getClass()).loadClass(className);
            return c;
        }
        catch (Exception e) {
            // ignore buggy classes for now ..
            throw new XmlReaderException("LoadHanderClass: Unable to load " + className, e);
        }
    }

    /**
     * Returns ???.
     *
     * @return ???.
     *
     * @throws SAXException ???.
     */
    public Object getResult () throws SAXException
    {
        if (this.rootHandler != null) {
          try
          {
            return this.rootHandler.getObject();
          }
          catch (XmlReaderException e)
          {
            throw new ElementDefinitionException(e);
          }
        }
        return null;
    }
}
