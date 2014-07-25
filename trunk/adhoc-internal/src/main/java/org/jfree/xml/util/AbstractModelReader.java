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
 * AbstractModelReader.java
 * ------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AbstractModelReader.java,v 1.8 2005/10/18 13:33:53 mungady Exp $
 *
 * Changes
 * -------
 * 12-Nov-2003 : Initial version
 * 25-Nov-2003 : Updated header (DG);
 *
 */

package org.jfree.xml.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jfree.util.Log;
import org.jfree.util.ObjectUtilities;
import org.jfree.xml.CommentHandler;
import org.jfree.xml.ElementDefinitionException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads the class model from an previously written xml file set.
 * This class provides abstract methods which get called during the parsing
 * (similiar to the SAX parsing, but slightly easier to code).
 *
 * This will need a rewrite in the future, when the structure is finished.
 */
public abstract class AbstractModelReader {

    /** The 'START' state. */
    private static final int STATE_START = 0;
    
    /** The 'IN_OBJECT' state. */
    private static final int IN_OBJECT = 1;

    /** The 'IGNORE_OBJECT' state. */
    private static final int IGNORE_OBJECT = 2;
    
    /** The 'MAPPING' state. */
    private static final int MAPPING_STATE = 3;
    
    /** The 'CONSTRUCTOR' state. */
    private static final int CONSTRUCTOR_STATE = 4;

    /**
     * The SAX2 callback implementation used for parsing the model xml files.
     */
    private class SAXModelHandler extends DefaultHandler {

        /** The resource URL. */
        private URL resource;

        /** The current state. */
        private int state;
        
        /** Open comments. */
        private Stack openComments;
        
        /** Flag to track includes. */
        private boolean isInclude;

        /**
         * Creates a new SAX handler for parsing the model.
         *
         * @param resource  the resource URL.
         * @param isInclude  an include?
         */
        public SAXModelHandler(final URL resource, final boolean isInclude) {
            if (resource == null) {
                throw new NullPointerException();
            }
            this.resource = resource;
            this.openComments = new Stack();
            this.isInclude = isInclude;
        }

        /**
         * Receive notification of the start of an element.
         *
         * @param uri The Namespace URI, or the empty string if the
         *        element has no Namespace URI or if Namespace
         *        processing is not being performed.
         * @param localName The local name (without prefix), or the
         *        empty string if Namespace processing is not being
         *        performed.
         * @param qName The qualified name (with prefix), or the
         *        empty string if qualified names are not available.
         * @param attributes The attributes attached to the element.  If
         *        there are no attributes, it shall be an empty
         *        Attributes object.
         * @exception SAXException Any SAX exception, possibly
         *            wrapping another exception.
         * 
         * @see org.xml.sax.ContentHandler#startElement
         */
        public void startElement(final String uri, final String localName,
                                 final String qName, final Attributes attributes)
            throws SAXException {

            setOpenComment(getCommentHandler().getComments());
            this.openComments.push(getOpenComment());
            setCloseComment(null);

            try {

                if (!this.isInclude && qName.equals(ClassModelTags.OBJECTS_TAG)) {
                    //Log.debug ("Open Comments: " + openComment);
                    startRootDocument();
                    return;
                }

                if (getState() == STATE_START) {
                    startRootElement(qName, attributes);
                }
                else if (getState() == IGNORE_OBJECT) {
                    return;
                }
                else if (getState() == IN_OBJECT) {
                    startObjectElement(qName, attributes);
                }
                else if (getState() == MAPPING_STATE) {
                    if (!qName.equals(ClassModelTags.TYPE_TAG)) {
                        throw new SAXException("Expected 'type' tag");
                    }
                    final String name = attributes.getValue(ClassModelTags.NAME_ATTR);
                    final String target = attributes.getValue(ClassModelTags.CLASS_ATTR);
                    handleMultiplexMapping(name, target);
                }
                else if (getState() == CONSTRUCTOR_STATE) {
                    if (!qName.equals(ClassModelTags.PARAMETER_TAG)) {
                        throw new SAXException("Expected 'parameter' tag");
                    }
                    final String parameterClass = attributes.getValue(ClassModelTags.CLASS_ATTR);
                    final String tagName = attributes.getValue(ClassModelTags.PROPERTY_ATTR); // optional
                    handleConstructorDefinition(tagName, parameterClass);
                }
            }
            catch (ObjectDescriptionException e) {
                throw new SAXException(e);
            }
            finally {
                getCommentHandler().clearComments();
            }
        }

        /**
         * Receive notification of the end of an element.
         *
         * @param uri The Namespace URI, or the empty string if the
         *        element has no Namespace URI or if Namespace
         *        processing is not being performed.
         * @param localName The local name (without prefix), or the
         *        empty string if Namespace processing is not being
         *        performed.
         * @param qName The qualified name (with prefix), or the
         *        empty string if qualified names are not available.
         * @exception SAXException Any SAX exception, possibly
         *            wrapping another exception.
         * @see org.xml.sax.ContentHandler#endElement
         */
        public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {

            setOpenComment((String[]) this.openComments.pop());
            setCloseComment(getCommentHandler().getComments());

            try {
                if (!this.isInclude && qName.equals(ClassModelTags.OBJECTS_TAG)) {
                    endRootDocument();
                    return;
                }

                if (qName.equals(ClassModelTags.OBJECT_TAG)) {
                    if (getState() != IGNORE_OBJECT) {
                        endObjectDefinition();
                    }
                    setState(STATE_START);
                }
                else if (qName.equals(ClassModelTags.MAPPING_TAG)) {
                    setState(STATE_START);
                    endMultiplexMapping();
                }
                else if (qName.equals(ClassModelTags.CONSTRUCTOR_TAG)) {
                    if (getState() != IGNORE_OBJECT) {
                        setState(IN_OBJECT);
                    }
                }
            }
            catch (ObjectDescriptionException e) {
                throw new SAXException(e);
            }
            finally {
                getCommentHandler().clearComments();
            }
        }

        /**
         * Handles the start of an element within an object definition.
         *
         * @param qName The qualified name (with prefix), or the
         *        empty string if qualified names are not available.
         * @param attributes The attributes attached to the element.  If
         *        there are no attributes, it shall be an empty
         *        Attributes object.
         * @throws ObjectDescriptionException if an error occured while
         *        handling this tag
         */
        private void startObjectElement(final String qName, final Attributes attributes)
            throws ObjectDescriptionException {
            if (qName.equals(ClassModelTags.CONSTRUCTOR_TAG)) {
                setState(CONSTRUCTOR_STATE);
            }
            else if (qName.equals(ClassModelTags.LOOKUP_PROPERTY_TAG)) {
                final String name = attributes.getValue(ClassModelTags.NAME_ATTR);
                final String lookupKey = attributes.getValue(ClassModelTags.LOOKUP_ATTR);
                handleLookupDefinition(name, lookupKey);
            }
            else if (qName.equals(ClassModelTags.IGNORED_PROPERTY_TAG)) {
                final String name = attributes.getValue(ClassModelTags.NAME_ATTR);
                handleIgnoredProperty(name);
            }
            else if (qName.equals(ClassModelTags.ELEMENT_PROPERTY_TAG)) {
                final String elementAtt = attributes.getValue(ClassModelTags.ELEMENT_ATTR);
                final String name = attributes.getValue(ClassModelTags.NAME_ATTR);
                handleElementDefinition(name, elementAtt);
            }
            else if (qName.equals(ClassModelTags.ATTRIBUTE_PROPERTY_TAG)) {
                final String name = attributes.getValue(ClassModelTags.NAME_ATTR);
                final String attribName = attributes.getValue(ClassModelTags.ATTRIBUTE_ATTR);
                final String handler = attributes.getValue(ClassModelTags.ATTRIBUTE_HANDLER_ATTR);
                handleAttributeDefinition(name, attribName, handler);
            }
        }

        /**
         * Handles the include or object tag.
         *
         * @param qName The qualified name (with prefix), or the
         *        empty string if qualified names are not available.
         * @param attributes The attributes attached to the element.  If
         *        there are no attributes, it shall be an empty
         *        Attributes object.
         * @throws SAXException if an parser error occured
         * @throws ObjectDescriptionException if an object model related
         *        error occured.
         */
        private void startRootElement(final String qName, final Attributes attributes)
            throws SAXException, ObjectDescriptionException {

            if (qName.equals(ClassModelTags.INCLUDE_TAG)) {
                if (this.isInclude) {
                    Log.warn("Ignored nested include tag.");
                    return;
                }
                final String src = attributes.getValue(ClassModelTags.SOURCE_ATTR);
                try {
                    final URL url = new URL(this.resource, src);
                    startIncludeHandling(url);
                    parseXmlDocument(url, true);
                    endIncludeHandling();
                }
                catch (Exception ioe) {
                    throw new ElementDefinitionException
                        (ioe, "Unable to include file from " + src);
                }
            }
            else if (qName.equals(ClassModelTags.OBJECT_TAG)) {
                setState(IN_OBJECT);
                final String className = attributes.getValue(ClassModelTags.CLASS_ATTR);
                String register = attributes.getValue(ClassModelTags.REGISTER_NAMES_ATTR);
                if (register != null && register.length() == 0) {
                    register = null;
                }
                final boolean ignored = "true".equals(attributes.getValue(ClassModelTags.IGNORE_ATTR));
                if (!startObjectDefinition(className, register, ignored)) {
                    setState(IGNORE_OBJECT);
                }
            }
            else if (qName.equals(ClassModelTags.MANUAL_TAG)) {
                final String className = attributes.getValue(ClassModelTags.CLASS_ATTR);
                final String readHandler = attributes.getValue(ClassModelTags.READ_HANDLER_ATTR);
                final String writeHandler = attributes.getValue(ClassModelTags.WRITE_HANDLER_ATTR);
                handleManualMapping(className, readHandler, writeHandler);
            }
            else if (qName.equals(ClassModelTags.MAPPING_TAG)) {
                setState(MAPPING_STATE);
                final String typeAttr = attributes.getValue(ClassModelTags.TYPE_ATTR);
                final String baseClass = attributes.getValue(ClassModelTags.BASE_CLASS_ATTR);
                startMultiplexMapping(baseClass, typeAttr);
            }
        }

        /**
         * Returns the current state.
         *
         * @return the state.
         */
        private int getState() {
            return this.state;
        }

        /**
         * Sets the current state.
         *
         * @param state  the state.
         */
        private void setState(final int state) {
            this.state = state;
        }
    }

    /** The comment handler. */
    private CommentHandler commentHandler;
    
    /** The close comments. */
    private String[] closeComment;
    
    /** The open comments. */
    private String[] openComment;

    /**
     * Default Constructor.
     */
    public AbstractModelReader() {
        this.commentHandler = new CommentHandler();
    }

    /**
     * Returns the comment handler.
     * 
     * @return The comment handler.
     */
    protected CommentHandler getCommentHandler() {
        return this.commentHandler;
    }

    /**
     * Returns the close comment. 
     * 
     * @return The close comment.
     */
    protected String[] getCloseComment() {
        return this.closeComment;
    }

    /**
     * Returns the open comment. 
     * 
     * @return The open comment.
     */
    protected String[] getOpenComment() {
        return this.openComment;
    }

    /**
     * Sets the close comment.
     * 
     * @param closeComment  the close comment.
     */
    protected void setCloseComment(final String[] closeComment) {
        this.closeComment = closeComment;
    }

    /**
     * Sets the open comment.
     * 
     * @param openComment  the open comment.
     */
    protected void setOpenComment(final String[] openComment) {
        this.openComment = openComment;
    }

    /**
     * Parses an XML document at the given URL.
     * 
     * @param resource  the document URL.
     * 
     * @throws ObjectDescriptionException ??
     */
    protected void parseXml(final URL resource) throws ObjectDescriptionException {
        parseXmlDocument(resource, false);
    }

    /**
     * Parses the given specification and loads all includes specified in the files.
     * This implementation does not check for loops in the include files.
     *
     * @param resource  the url of the xml specification.
     * @param isInclude  an include?
     * 
     * @throws org.jfree.xml.util.ObjectDescriptionException if an error occured which prevented the
     * loading of the specifications.
     */
    protected void parseXmlDocument(final URL resource, final boolean isInclude)
        throws ObjectDescriptionException {
        
        try {
            final InputStream in = new BufferedInputStream(resource.openStream());
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            final SAXParser saxParser = factory.newSAXParser();
            final XMLReader reader = saxParser.getXMLReader();

            final SAXModelHandler handler = new SAXModelHandler(resource, isInclude);
            try {
                reader.setProperty
                    ("http://xml.org/sax/properties/lexical-handler",
                        getCommentHandler());
            }
            catch (SAXException se) {
                Log.debug("Comments are not supported by this SAX implementation.");
            }
            reader.setContentHandler(handler);
            reader.setDTDHandler(handler);
            reader.setErrorHandler(handler);
            reader.parse(new InputSource(in));
            in.close();
        }
        catch (Exception e) {
            // unable to init
            Log.warn("Unable to load factory specifications", e);
            throw new ObjectDescriptionException("Unable to load object factory specs.", e);
        }
    }

    /**
     * Start the root document.
     */
    protected void startRootDocument() {
        // nothing required
    }

    /**
     * End the root document.
     */
    protected void endRootDocument() {
        // nothing required
    }

    /**
     * Start handling an include.
     * 
     * @param resource  the URL.
     */
    protected void startIncludeHandling(final URL resource) {
        // nothing required
    }

    /**
     * End handling an include.
     */
    protected void endIncludeHandling() {
        // nothing required
    }

    /**
     * Callback method for ignored properties. Such properties get marked so that
     * the information regarding these properties won't get lost.
     *
     * @param name the name of the ignored property.
     */
    protected void handleIgnoredProperty(final String name) {
        // nothing required
    }

    /**
     * Handles a manual mapping definition. The manual mapping maps specific
     * read and write handlers to a given base class. Manual mappings always
     * override any other definition.
     *
     * @param className the base class name
     * @param readHandler the class name of the read handler
     * @param writeHandler the class name of the write handler
     * @return true, if the mapping was accepted, false otherwise.
     * @throws ObjectDescriptionException if an unexpected error occured.
     */
    protected abstract boolean handleManualMapping(String className, String readHandler, 
        String writeHandler) throws ObjectDescriptionException;

    /**
     * Starts a object definition. The object definition collects all properties of
     * an bean-class and defines, which constructor should be used when creating the
     * class.
     *
     * @param className the class name of the defined object
     * @param register the (optional) register name, to lookup and reference the object
     * later.
     * @param ignored  ??.
     * 
     * @return true, if the definition was accepted, false otherwise.
     * @throws ObjectDescriptionException if an unexpected error occured.
     */
    protected abstract boolean startObjectDefinition(String className, String register,
        boolean ignored) throws ObjectDescriptionException;

    /**
     * Handles an attribute definition. This method gets called after the object definition
     * was started. The method will be called for every defined attribute property.
     *
     * @param name the name of the property
     * @param attribName the xml-attribute name to use later.
     * @param handlerClass the attribute handler class.
     * @throws ObjectDescriptionException if an error occured.
     */
    protected abstract void handleAttributeDefinition(String name, String attribName,
                                                      String handlerClass)
        throws ObjectDescriptionException;

    /**
     * Handles an element definition. This method gets called after the object definition
     * was started. The method will be called for every defined element property. Element
     * properties are used to describe complex objects.
     *
     * @param name the name of the property
     * @param element the xml-tag name for the child element.
     * @throws ObjectDescriptionException if an error occurs.
     */
    protected abstract void handleElementDefinition(String name, String element) 
        throws ObjectDescriptionException;

    /**
     * Handles an lookup definition. This method gets called after the object definition
     * was started. The method will be called for every defined lookup property. Lookup properties
     * reference previously created object using the object's registry name.
     *
     * @param name the property name of the base object
     * @param lookupKey the register key of the referenced object
     * @throws ObjectDescriptionException if an error occured.
     */
    protected abstract void handleLookupDefinition(String name, String lookupKey) 
        throws ObjectDescriptionException;

    /**
     * Finializes the object definition.
     *
     * @throws ObjectDescriptionException if an error occures.
     */
    protected abstract void endObjectDefinition() throws ObjectDescriptionException;

    /**
     * Starts a multiplex mapping. Multiplex mappings are used to define polymorphic
     * argument handlers. The mapper will collect all derived classes of the given
     * base class and will select the corresponding mapping based on the given type
     * attribute.
     *
     * @param className the base class name
     * @param typeAttr the xml-attribute name containing the mapping key
     */
    protected abstract void startMultiplexMapping(String className, String typeAttr);

    /**
     * Defines an entry for the multiplex mapping. The new entry will be activated
     * when the base mappers type attribute contains this <code>typename</code> and
     * will resolve to the handler for the given classname.
     *
     * @param typeName the type value for this mapping.
     * @param className the class name to which this mapping resolves.
     * @throws ObjectDescriptionException if an error occurs.
     */
    protected abstract void handleMultiplexMapping(String typeName, String className) 
        throws ObjectDescriptionException;

    /**
     * Finializes the multiplexer mapping.
     *
     * @throws ObjectDescriptionException if an error occurs.
     */
    protected abstract void endMultiplexMapping() throws ObjectDescriptionException;

    /**
     * Handles a constructor definition. Only one constructor can be defined for
     * a certain object type. The constructor will be filled using the given properties.
     *
     * @param propertyName the property name of the referenced local property
     * @param parameterClass the parameter class for the parameter.
     * @throws ObjectDescriptionException if an error occured.
     */
    protected abstract void handleConstructorDefinition(String propertyName, String parameterClass)
        throws ObjectDescriptionException;

    /**
     * Loads the given class, and ignores all exceptions which may occur
     * during the loading. If the class was invalid, null is returned instead.
     *
     * @param className the name of the class to be loaded.
     * @return the class or null.
     */
    protected Class loadClass(final String className) {
        if (className == null) {
            return null;
        }
        if (className.startsWith("::")) {
            return BasicTypeSupport.getClassRepresentation(className);
        }
        try {
            return ObjectUtilities.getClassLoader(getClass()).loadClass(className);
        }
        catch (Exception e) {
            // ignore buggy classes for now ..
            Log.warn("Unable to load class", e);
            return null;
        }
    }

}
