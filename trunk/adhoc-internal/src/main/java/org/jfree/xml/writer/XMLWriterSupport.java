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
 * XMLWriterSupport.java
 * ---------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: XMLWriterSupport.java,v 1.6 2005/11/08 14:35:52 mungady Exp $
 *
 * Changes
 * -------
 * 21-Jun-2003 : Initial version (TM);
 * 26-Nov-2003 : Updated Javadocs (DG);
 *
 */

package org.jfree.xml.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * A support class for writing XML files.
 *
 * @author Thomas Morgner
 */
public class XMLWriterSupport {

    /** A constant for controlling the indent function. */
    public static final int OPEN_TAG_INCREASE = 1;

    /** A constant for controlling the indent function. */
    public static final int CLOSE_TAG_DECREASE = 2;

    /** A constant for controlling the indent function. */
    public static final int INDENT_ONLY = 3;

    /** A constant for close. */
    public static final boolean CLOSE = true;

    /** A constant for open. */
    public static final boolean OPEN = false;

    /** The line separator. */
    private static String lineSeparator;

    /** A list of safe tags. */
    private SafeTagList safeTags;

    /** The indent level for that writer. */
    private int indentLevel;

    /** The indent string. */
    private String indentString;

    /** 
     * A flag indicating whether to force a linebreak before printing the next 
     * tag. 
     */
    private boolean newLineOk;

    /**
     * Default Constructor. The created XMLWriterSupport will not have no safe 
     * tags and starts with an indention level of 0.  
     */
    public XMLWriterSupport() {
        this(new SafeTagList(), 0);
    }

    /**
     * Creates a new support instance.
     *
     * @param safeTags  tags that are safe for line breaks.
     * @param indentLevel  the index level.
     */
    public XMLWriterSupport(final SafeTagList safeTags, final int indentLevel) {
        this(safeTags, indentLevel, "    ");
    }

    /**
     * Creates a new support instance.
     *
     * @param safeTags  the tags that are safe for line breaks.
     * @param indentLevel  the indent level.
     * @param indentString  the indent string.
     */
    public XMLWriterSupport(final SafeTagList safeTags, final int indentLevel, 
            final String indentString) {
        if (indentString == null) {
            throw new NullPointerException("IndentString must not be null");
        }

        this.safeTags = safeTags;
        this.indentLevel = indentLevel;
        this.indentString = indentString;
    }

    /**
     * Starts a new block by increasing the indent level.
     *
     * @throws IOException if an IO error occurs.
     */
    public void startBlock() throws IOException {
        this.indentLevel++;
        allowLineBreak();
    }

    /**
     * Ends the current block by decreasing the indent level.
     *
     * @throws IOException if an IO error occurs.
     */
    public void endBlock() throws IOException {
        this.indentLevel--;
        allowLineBreak();
    }

    /**
     * Forces a linebreak on the next call to writeTag or writeCloseTag.
     *
     * @throws IOException if an IO error occurs.
     */
    public void allowLineBreak() throws IOException {
        this.newLineOk = true;
    }

    /**
     * Returns the line separator.
     *
     * @return the line separator.
     */
    public static String getLineSeparator() {
        if (lineSeparator == null) {
            try {
                lineSeparator = System.getProperty("line.separator", "\n");
            }
            catch (SecurityException se) {
                lineSeparator = "\n";
            }
        }
        return lineSeparator;
    }

    /**
     * Writes an opening XML tag that has no attributes.
     *
     * @param w  the writer.
     * @param name  the tag name.
     *
     * @throws java.io.IOException if there is an I/O problem.
     */
    public void writeTag(final Writer w, final String name) throws IOException {
        if (this.newLineOk) {
            w.write(getLineSeparator());
        }
        indent(w, OPEN_TAG_INCREASE);

        w.write("<");
        w.write(name);
        w.write(">");
        if (getSafeTags().isSafeForOpen(name)) {
            w.write(getLineSeparator());
        }
    }

    /**
     * Writes a closing XML tag.
     *
     * @param w  the writer.
     * @param tag  the tag name.
     *
     * @throws java.io.IOException if there is an I/O problem.
     */
    public void writeCloseTag(final Writer w, final String tag) 
            throws IOException {
        // check whether the tag contains CData - we ma not indent such tags
        if (this.newLineOk || getSafeTags().isSafeForOpen(tag)) {
            if (this.newLineOk) {
                w.write(getLineSeparator());
            }
            indent(w, CLOSE_TAG_DECREASE);
        }
        else {
            decreaseIndent();
        }
        w.write("</");
        w.write(tag);
        w.write(">");
        if (getSafeTags().isSafeForClose(tag)) {
            w.write(getLineSeparator());
        }
        this.newLineOk = false;
    }

    /**
     * Writes an opening XML tag with an attribute/value pair.
     *
     * @param w  the writer.
     * @param name  the tag name.
     * @param attributeName  the attribute name.
     * @param attributeValue  the attribute value.
     * @param close  controls whether the tag is closed.
     *
     * @throws java.io.IOException if there is an I/O problem.
     */
    public void writeTag(final Writer w, final String name, 
            final String attributeName, final String attributeValue,
            final boolean close) throws IOException {
        final AttributeList attr = new AttributeList();
        if (attributeName != null) {
            attr.setAttribute(attributeName, attributeValue);
        }
        writeTag(w, name, attr, close);
    }

    /**
     * Writes an opening XML tag along with a list of attribute/value pairs.
     *
     * @param w  the writer.
     * @param name  the tag name.
     * @param attributes  the attributes.
     * @param close  controls whether the tag is closed.
     *
     * @throws java.io.IOException if there is an I/O problem.
     * @deprecated use the attribute list instead of the properties.
     */
    public void writeTag(final Writer w, final String name, 
            final Properties attributes, final boolean close)
            throws IOException {
        final AttributeList attList = new AttributeList();
        final Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {
            final String key = (String) keys.nextElement();
            attList.setAttribute(key, attributes.getProperty(key));
        }
        writeTag(w, name, attList, close);
    }

    /**
     * Writes an opening XML tag along with a list of attribute/value pairs.
     *
     * @param w  the writer.
     * @param name  the tag name.
     * @param attributes  the attributes.
     * @param close  controls whether the tag is closed.
     *
     * @throws java.io.IOException if there is an I/O problem.     
     */
    public void writeTag(final Writer w, final String name, 
            final AttributeList attributes, final boolean close)
            throws IOException {

        if (this.newLineOk) {
            w.write(getLineSeparator());
            this.newLineOk = false;
        }
        indent(w, OPEN_TAG_INCREASE);

        w.write("<");
        w.write(name);
        final Iterator keys = attributes.keys();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final String value = attributes.getAttribute(key);
            w.write(" ");
            w.write(key);
            w.write("=\"");
            w.write(normalize(value));
            w.write("\"");
        }
        if (close) {
            w.write("/>");
            if (getSafeTags().isSafeForClose(name)) {
                w.write(getLineSeparator());
            }
            decreaseIndent();
        }
        else {
            w.write(">");
            if (getSafeTags().isSafeForOpen(name)) {
                w.write(getLineSeparator());
            }
        }
    }

    /**
     * Normalises a string, replacing certain characters with their escape 
     * sequences so that the XML text is not corrupted.
     *
     * @param s  the string.
     *
     * @return the normalised string.
     */
    public static String normalize(final String s) {
        if (s == null) {
            return "";
        }
        final StringBuffer str = new StringBuffer();
        final int len = s.length();

        for (int i = 0; i < len; i++) {
            final char ch = s.charAt(i);

            switch (ch) {
                case '<':
                    {
                        str.append("&lt;");
                        break;
                    }
                case '>':
                    {
                        str.append("&gt;");
                        break;
                    }
                case '&':
                    {
                        str.append("&amp;");
                        break;
                    }
                case '"':
                    {
                        str.append("&quot;");
                        break;
                    }
                case '\n':
                    {
                        if (i > 0) {
                            final char lastChar = str.charAt(str.length() - 1);

                            if (lastChar != '\r') {
                                str.append(getLineSeparator());
                            }
                            else {
                                str.append('\n');
                            }
                        }
                        else {
                            str.append(getLineSeparator());
                        }
                        break;
                    }
                default :
                    {
                        str.append(ch);
                    }
            }
        }

        return (str.toString());
    }

    /**
     * Indent the line. Called for proper indenting in various places.
     *
     * @param writer the writer which should receive the indentention.
     * @param increase the current indent level.
     * @throws java.io.IOException if writing the stream failed.
     */
    public void indent(final Writer writer, final int increase) 
            throws IOException {
        if (increase == CLOSE_TAG_DECREASE) {
            decreaseIndent();
        }
        for (int i = 0; i < this.indentLevel; i++) {
            writer.write(this.indentString); // 4 spaces, we could also try tab,
            // but I do not know whether this works
            // with our XML edit pane
        }
        if (increase == OPEN_TAG_INCREASE) {
            increaseIndent();
        }
    }

    /**
     * Returns the current indent level.
     *
     * @return the current indent level.
     */
    public int getIndentLevel() {
        return this.indentLevel;
    }

    /**
     * Increases the indention by one level.
     */
    protected void increaseIndent() {
        this.indentLevel++;
    }

    /**
     * Decreates the indention by one level.
     */
    protected void decreaseIndent() {
        this.indentLevel--;
    }

    /**
     * Returns the list of safe tags.
     *
     * @return The list.
     */
    public SafeTagList getSafeTags() {
        return this.safeTags;
    }
}
