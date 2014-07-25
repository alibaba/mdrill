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
 * RootXmlWriteHandler.java
 * ------------------------
 * (C) Copyright 2002-2005, by Object Refinery Limited.
 *
 * Original Author:  Peter Becker;
 * Contributor(s):   -;
 *
 * $Id: RootXmlWriteHandler.java,v 1.5 2005/10/18 13:35:06 mungady Exp $
 *
 * Changes
 * -------
 * 23-Dec-2003 : Added missing Javadocs (DG);
 *
 */
package org.jfree.xml.writer;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.jfree.util.ObjectUtilities;
import org.jfree.xml.util.ManualMappingDefinition;
import org.jfree.xml.util.MultiplexMappingDefinition;
import org.jfree.xml.util.MultiplexMappingEntry;
import org.jfree.xml.util.ObjectFactory;
import org.jfree.xml.util.SimpleObjectFactory;
import org.jfree.xml.writer.coretypes.BasicStrokeWriteHandler;
import org.jfree.xml.writer.coretypes.ColorWriteHandler;
import org.jfree.xml.writer.coretypes.FontWriteHandler;
import org.jfree.xml.writer.coretypes.GenericWriteHandler;
import org.jfree.xml.writer.coretypes.GradientPaintWriteHandler;
import org.jfree.xml.writer.coretypes.InsetsWriteHandler;
import org.jfree.xml.writer.coretypes.ListWriteHandler;
import org.jfree.xml.writer.coretypes.Point2DWriteHandler;
import org.jfree.xml.writer.coretypes.Rectangle2DWriteHandler;
import org.jfree.xml.writer.coretypes.RenderingHintsWriteHandler;

/**
 * A root handler for writing objects to XML format.
 */
public abstract class RootXmlWriteHandler {

    /** A map containg the manual mappings. */
    private SimpleObjectFactory classToHandlerMapping;

    /**
     * Creates a new RootXmlWrite handler with the default mappings enabled.
     */
    public RootXmlWriteHandler() {
        this.classToHandlerMapping = new SimpleObjectFactory();

        // set up handling for Paint objects
        final MultiplexMappingEntry[] paintEntries = new MultiplexMappingEntry[2];
        paintEntries[0] = new MultiplexMappingEntry("color", Color.class.getName());
        paintEntries[1] = new MultiplexMappingEntry("gradientPaint", GradientPaint.class.getName());
        addMultiplexMapping(Paint.class, "type", paintEntries);
        addManualMapping(GradientPaint.class, GradientPaintWriteHandler.class);
        addManualMapping(Color.class, ColorWriteHandler.class);

        // set up handling for Point2D objects
        final MultiplexMappingEntry[] point2DEntries = new MultiplexMappingEntry[2];
        point2DEntries[0] = new MultiplexMappingEntry("float", Point2D.Float.class.getName());
        point2DEntries[1] = new MultiplexMappingEntry("double", Point2D.Double.class.getName());
        addMultiplexMapping(Point2D.class, "type", point2DEntries);
        addManualMapping(Point2D.Float.class, Point2DWriteHandler.class);
        addManualMapping(Point2D.Double.class, Point2DWriteHandler.class);

        // set up handling for Stroke objects
        final MultiplexMappingEntry[] strokeEntries = new MultiplexMappingEntry[1];
        strokeEntries[0] = new MultiplexMappingEntry("basic", BasicStroke.class.getName());
        addMultiplexMapping(Stroke.class, "type", strokeEntries);
        addManualMapping(BasicStroke.class, BasicStrokeWriteHandler.class);

        // set up handling for Rectangle2D objects
        final MultiplexMappingEntry[] rectangle2DEntries = new MultiplexMappingEntry[2];
        rectangle2DEntries[0] = new MultiplexMappingEntry(
            "float", Rectangle2D.Float.class.getName()
        );
        rectangle2DEntries[1] = new MultiplexMappingEntry(
            "double", Rectangle2D.Double.class.getName()
        );
        addMultiplexMapping(Rectangle2D.class, "type", rectangle2DEntries);
        addManualMapping(Rectangle2D.Float.class, Rectangle2DWriteHandler.class);
        addManualMapping(Rectangle2D.Double.class, Rectangle2DWriteHandler.class);

        // set up handling for List objects
        final MultiplexMappingEntry[] listEntries = new MultiplexMappingEntry[4];
        listEntries[0] = new MultiplexMappingEntry("array-list", ArrayList.class.getName());
        listEntries[1] = new MultiplexMappingEntry("linked-list", LinkedList.class.getName());
        listEntries[2] = new MultiplexMappingEntry("vector", Vector.class.getName());
        listEntries[3] = new MultiplexMappingEntry("stack", Stack.class.getName());
        addMultiplexMapping(List.class, "type", listEntries);
        addManualMapping(LinkedList.class, ListWriteHandler.class);
        addManualMapping(Vector.class, ListWriteHandler.class);
        addManualMapping(ArrayList.class, ListWriteHandler.class);
        addManualMapping(Stack.class, ListWriteHandler.class);

        // handle all other direct mapping types
        addManualMapping(RenderingHints.class, RenderingHintsWriteHandler.class);
        addManualMapping(Insets.class, InsetsWriteHandler.class);
        addManualMapping(Font.class, FontWriteHandler.class);
    }

    /**
     * Returns the object factory.
     * 
     * @return the object factory.
     */
    protected abstract ObjectFactory getFactoryLoader();

    /**
     * Adds a new manual mapping to this handler.
     *
     * This method provides support for the manual mapping. The manual mapping
     * will become active before the multiplexers were queried. This facility
     * could be used to override the model definition.
     *
     * @param classToWrite the class, which should be handled
     * @param handler the write handler implementation for that class.
     */
    protected void addManualMapping(final Class classToWrite, final Class handler) {
        if (handler == null) {
            throw new NullPointerException("handler must not be null.");
        }
        if (classToWrite == null) {
            throw new NullPointerException("classToWrite must not be null.");
        }
        if (!XmlWriteHandler.class.isAssignableFrom(handler)) {
            throw new IllegalArgumentException("The given handler is no XmlWriteHandler.");
        }

        this.classToHandlerMapping.addManualMapping
            (new ManualMappingDefinition(classToWrite, null, handler.getName()));
    }

    /**
     * Adds a multiplex mapping.
     * 
     * @param baseClass  the base class.
     * @param typeAttr  the type attribute.
     * @param mdef  the mapping entries.
     */
    protected void addMultiplexMapping(final Class baseClass,
                                       final String typeAttr,
                                       final MultiplexMappingEntry[] mdef) {
        
        this.classToHandlerMapping.addMultiplexMapping(
            new MultiplexMappingDefinition(baseClass, typeAttr, mdef)
        );
        
    }

    /**
     * Tries to find the mapping for the given class. This will first check
     * the manual mapping and then try to use the object factory to resolve
     * the class parameter into a write handler.
     *
     * @param classToWrite the class for which to find a handler.
     * @return the write handler, never null.
     * @throws XMLWriterException if no handler could be found for the given class.
     */
    protected XmlWriteHandler getMapping(final Class classToWrite) throws XMLWriterException {

        if (classToWrite == null) {
            throw new NullPointerException("ClassToWrite is null.");
        }

        // search direct matches, first the direct definitions ...
        ManualMappingDefinition manualMapping =
            this.classToHandlerMapping.getManualMappingDefinition(classToWrite);
        if (manualMapping == null) {
            // search the manual mappings from the xml file.
            manualMapping = getFactoryLoader().getManualMappingDefinition(classToWrite);
        }
        if (manualMapping != null) {
            return loadHandlerClass(manualMapping.getWriteHandler());
        }


        // multiplexer definitions can be safely ignored here, as they are used to
        // map parent classes to more specific child classes. In this case, we already
        // know the child class and can look up the handler directly.

        // of course we have to check for multiplexers later, so that we can apply
        // the mutiplex-attributes.

        // and finally try the generic handler matches ...
        if (this.classToHandlerMapping.isGenericHandler(classToWrite)) {
            return new GenericWriteHandler(
                this.classToHandlerMapping.getFactoryForClass(classToWrite)
            );
        }
        if (getFactoryLoader().isGenericHandler(classToWrite)) {
            return new GenericWriteHandler(getFactoryLoader().getFactoryForClass(classToWrite));
        }

        throw new XMLWriterException("Unable to handle " + classToWrite);
    }

    /**
     * Writes the given object with the specified tagname. This method will
     * do nothing, if the given object is null.
     *
     * @param tagName  the tagname for the xml-element containing the object
     * definition. The tagname must not be null.
     * @param object  the object which should be written.
     * @param baseClass  the base class.
     * @param writer  the xml writer used to write the content, never null.
     * 
     * @throws IOException if an IOException occures.
     * @throws XMLWriterException if an object model related error occures during
     * the writing.
     */
    public void write(final String tagName, final Object object, final Class baseClass, final XMLWriter writer)
        throws IOException, XMLWriterException {
        if (object == null) {
            return;
        }
        if (tagName == null) {
            throw new NullPointerException("RootXmlWriteHandler.write(..) : tagName is null");
        }
        if (writer == null) {
            throw new NullPointerException("RootXmlWriteHandler.write(..) : writer is null");
        }
        if (!baseClass.isInstance(object)) {
            throw new ClassCastException("Object is no instance of " + baseClass);
        }
        final Class classToWrite = object.getClass();
        final XmlWriteHandler handler = getMapping(classToWrite);
        handler.setRootHandler(this);

        String attributeName = null;
        String attributeValue = null;

        // find multiplexer for this class...
        MultiplexMappingDefinition mplex =
            getFactoryLoader().getMultiplexDefinition(baseClass);
        if (mplex == null) {
            mplex = this.classToHandlerMapping.getMultiplexDefinition(baseClass);
        }
        if (mplex != null) {
            final MultiplexMappingEntry entry =
                mplex.getEntryForClass(classToWrite.getName());
            if (entry != null) {
                attributeName = mplex.getAttributeName();
                attributeValue = entry.getAttributeValue();
            }
            else {
                throw new XMLWriterException(
                    "Unable to find child mapping for multiplexer " 
                    + baseClass + " to child " + classToWrite
                );
            }
        }

        handler.write(tagName, object, writer, attributeName, attributeValue);
        writer.allowLineBreak();
    }

    /**
     * Loads the given class, and ignores all exceptions which may occur
     * during the loading. If the class was invalid, null is returned instead.
     *
     * @param className the name of the class to be loaded.
     * @return the class or null.
     * 
     * @throws XMLWriterException if there is a writer exception.
     */
    protected XmlWriteHandler loadHandlerClass(final String className)
        throws XMLWriterException {
        if (className == null) {
            throw new XMLWriterException("LoadHanderClass: Class name not defined");
        }
        try {
            final Class c = ObjectUtilities.getClassLoader(getClass()).loadClass(className);
            return (XmlWriteHandler) c.newInstance();
        }
        catch (Exception e) {
            // ignore buggy classes for now ..
            throw new XMLWriterException("LoadHanderClass: Unable to instantiate " + className, e);
        }
    }
    
}
