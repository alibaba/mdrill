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
 * --------------------
 * ReportGenerator.java
 * --------------------
 * (C)opyright 2002-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ParserFrontend.java,v 1.8 2005/11/14 10:58:19 mungady Exp $
 *
 * Changes
 * -------
 * 10-May-2002 : Initial version
 * 12-Dec-2002 : Fixed issues reported by Checkstyle (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jfree.util.Log;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * The reportgenerator initializes the parser and provides an interface
 * the the default parser.
 *
 * To create a report from an URL, use
 * <code>
 * ReportGenerator.getInstance().parseReport (URL myURl, URL contentBase);
 * </code>
 *
 * @author Thomas Morgner
 */
public class ParserFrontend {

    /** The report handler. */
    private FrontendDefaultHandler defaulthandler;

    /** The parser factory. */
    private SAXParserFactory factory;

    /** The DTD. */
    private EntityResolver entityResolver;

    /** A flag indicating whether to use a DTD to validate the xml input. */
    private boolean validateDTD;

    /**
     * Creates a new report generator. The generator uses the singleton pattern by default,
     * so use generator.getInstance() to get the generator.
     *
     * @param parser the parser that is used to coordinate the parsing process.
     */
    protected ParserFrontend(final FrontendDefaultHandler parser) {
        if (parser == null) {
            throw new NullPointerException();
        }
        this.defaulthandler = parser;
    }

    /**
     * Returns <code>true</code> if the report definition should be validated against the
     * DTD, and <code>false</code> otherwise.
     *
     * @return A boolean.
     */
    public boolean isValidateDTD() {
        return this.validateDTD;
    }

    /**
     * Sets a flag that controls whether or not the report definition is validated
     * against the DTD.
     *
     * @param validateDTD  the flag.
     */
    public void setValidateDTD(final boolean validateDTD) {
        this.validateDTD = validateDTD;
    }

    /**
     * Returns the entity resolver.
     *
     * @return The entity resolver.
     */
    public EntityResolver getEntityResolver() {
        return this.entityResolver;
    }

    /**
     * Sets the entity resolver.
     *
     * @param entityResolver  the entity resolver.
     */
    public void setEntityResolver(final EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * Returns a SAX parser.
     *
     * @return a SAXParser.
     *
     * @throws ParserConfigurationException if there is a problem configuring the parser.
     * @throws SAXException if there is a problem with the parser initialisation
     */
    protected SAXParser getParser() throws ParserConfigurationException, SAXException {
        if (this.factory == null) {
            this.factory = SAXParserFactory.newInstance();
            if (isValidateDTD()) {
                try {
                    // dont touch the validating feature, if not needed ..
                    this.factory.setValidating(true);
                }
                catch (Exception ex) {
                    // the parser does not like the idea of validating ...
                    Log.debug("The parser will not validate the xml document.", ex);
                }
            }
        }
        return this.factory.newSAXParser();
    }

    /**
     * Sets the default handler used for parsing reports. This handler is used to
     * initiate parsing.
     *
     * @param handler  the handler.
     */
    public void setDefaultHandler(final FrontendDefaultHandler handler) {
        if (handler == null) {
            throw new NullPointerException();
        }
        this.defaulthandler = handler;
    }

    /**
     * Returns the ElementDefinitionHandler used for parsing reports.
     *
     * @return the report handler.
     */
    public FrontendDefaultHandler getDefaultHandler() {
        return this.defaulthandler;
    }

    /**
     * Creates a new instance of the currently set default handler and sets the contentbase
     * for the handler to <code>contentBase</code>.
     *
     * @param contentBase  the content base.
     *
     * @return the report handler.
     */
    protected FrontendDefaultHandler createDefaultHandler(final URL contentBase) {
        final FrontendDefaultHandler handler = getDefaultHandler().newInstance();
        if (contentBase != null) {
            handler.setConfigProperty(Parser.CONTENTBASE_KEY, contentBase.toExternalForm());
        }
        return handler;
    }

    /**
     * Parses an XML report template file.
     *
     * @param input  the input source.
     * @param contentBase  the content base.
     *
     * @return the report.
     *
     * @throws ElementDefinitionException if an error occurred.
     */
    protected Object parse(final InputSource input, final URL contentBase)
        throws ElementDefinitionException {
        try {
            final SAXParser parser = getParser();
            final XMLReader reader = parser.getXMLReader();

            try {
                reader.setFeature("http://xml.org/sax/features/validation", isValidateDTD());
            }
            catch (SAXException se) {
                Log.debug("The XMLReader will not validate the xml document.", se);
            }
            final FrontendDefaultHandler handler = createDefaultHandler(contentBase);
            configureReader(reader, handler);
            try {
                reader.setContentHandler(handler);
                reader.setDTDHandler(handler);
                if (getEntityResolver() != null) {
                    reader.setEntityResolver(getEntityResolver());
                }
                reader.setErrorHandler(handler);
                reader.parse(input);
                return handler.getResult();
            }
            catch (IOException e) {
                throw new ElementDefinitionException(e);
            }
        }
        catch (ParserConfigurationException e) {
            throw new ElementDefinitionException(e);
        }
        catch (SAXException e) {
            throw new ElementDefinitionException(e);
        }
    }

    /**
     * Configures the xml reader. Use this to set features or properties
     * before the documents get parsed.
     *
     * @param handler the parser implementation that will handle the SAX-Callbacks.
     * @param reader the xml reader that should be configured.
     */
    protected void configureReader(final XMLReader reader, final FrontendDefaultHandler handler) {
        try {
            reader.setProperty
                ("http://xml.org/sax/properties/lexical-handler", handler.getCommentHandler());
        }
        catch (SAXException se) {
            Log.debug("Comments are not supported by this SAX implementation.");
        }
    }

    /**
     * Parses an XML file which is loaded using the given URL. All
     * needed relative file- and resourcespecification are loaded
     * using the URL <code>contentBase</code> as base.
     * <p>
     * After the report is generated, the ReportDefinition-source and the contentbase are
     * stored as string in the reportproperties.
     *
     * @param file  the URL for the report template file.
     * @param contentBase  the URL for the report template content base.
     *
     * @return the parsed report.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ElementDefinitionException if there is a problem parsing the report template.
     */
    public Object parse(final URL file, final URL contentBase)
        throws ElementDefinitionException, IOException {
        if (file == null) {
            throw new NullPointerException("File may not be null");
        }

        final BufferedInputStream bin = new BufferedInputStream(file.openStream());
        final InputSource in = new InputSource(bin);
        in.setSystemId(file.toString());
        final Object result = parse(in, contentBase);
        bin.close();
        return result;
    }

}
