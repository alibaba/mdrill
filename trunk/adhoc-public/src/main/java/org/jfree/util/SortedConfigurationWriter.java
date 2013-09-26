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
 * ------------------------------
 * SortedConfigurationWriter.java
 * ------------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   -;
 *
 * $Id: SortedConfigurationWriter.java,v 1.4 2005/11/03 09:55:27 mungady Exp $
 *
 * Changes
 * -------
 *
 */

package org.jfree.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Writes a <code>Configuration</code> instance into a property file, where
 * the keys are sorted by their name. Writing sorted keys make it easier for
 * users to find and change properties in the file.
 *
 * @author Thomas Morgner
 */
public class SortedConfigurationWriter {
    /**
     * A constant defining that text should be escaped in a way
     * which is suitable for property keys.
     */
    private static final int ESCAPE_KEY = 0;
    /**
     * A constant defining that text should be escaped in a way
     * which is suitable for property values.
     */
    private static final int ESCAPE_VALUE = 1;
    /**
     * A constant defining that text should be escaped in a way
     * which is suitable for property comments.
     */
    private static final int ESCAPE_COMMENT = 2;

    /** The system-dependent End-Of-Line separator. */
    private static final String END_OF_LINE = StringUtils.getLineSeparator();

    /**
     * The default constructor, does nothing.
     */
    public SortedConfigurationWriter() {
    }

    /**
     * Returns a description for the given key. This implementation returns
     * null to indicate that no description should be written. Subclasses can
     * overwrite this method to provide comments for every key. These descriptions
     * will be included as inline comments.
     *
     * @param key the key for which a description should be printed.
     * @return the description or null if no description should be printed.
     */
    protected String getDescription(final String key) {
        return null;
    }

    /**
     * Saves the given configuration into a file specified by the given
     * filename.
     *
     * @param filename the filename
     * @param config the configuration
     * @throws IOException if an IOError occurs.
     */
    public void save(final String filename, final Configuration config)
        throws IOException {
        save(new File(filename), config);
    }

    /**
     * Saves the given configuration into a file specified by the given
     * file object.
     *
     * @param file the target file
     * @param config the configuration
     * @throws IOException if an IOError occurs.
     */
    public void save(final File file, final Configuration config)
        throws IOException {
        final BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream(file));
        save(out, config);
        out.close();
    }


    /**
     * Writes the configuration into the given output stream.
     *
     * @param outStream the target output stream
     * @param config the configuration
     * @throws IOException if writing fails.
     */
    public void save(final OutputStream outStream, final Configuration config)
        throws IOException {
        final ArrayList names = new ArrayList();

        // clear all previously set configuration settings ...
        final Iterator defaults = config.findPropertyKeys("");
        while (defaults.hasNext()) {
            final String key = (String) defaults.next();
            names.add(key);
        }

        Collections.sort(names);

        final OutputStreamWriter out =
            new OutputStreamWriter(outStream, "iso-8859-1");

        for (int i = 0; i < names.size(); i++) {
            final String key = (String) names.get(i);
            final String value = config.getConfigProperty(key);

            final String description = getDescription(key);
            if (description != null) {
                writeDescription(description, out);
            }
            saveConvert(key, ESCAPE_KEY, out);
            out.write("=");
            saveConvert(value, ESCAPE_VALUE, out);
            out.write(END_OF_LINE);
        }
        out.flush();

    }

    /**
     * Writes a descriptive comment into the given print writer.
     *
     * @param text   the text to be written. If it contains more than
     *               one line, every line will be prepended by the comment character.
     * @param writer the writer that should receive the content.
     * @throws IOException if writing fails
     */
    private void writeDescription(final String text, final Writer writer)
        throws IOException {
        // check if empty content ... this case is easy ...
        if (text.length() == 0) {
            return;
        }

        writer.write("# ");
        writer.write(END_OF_LINE);
        final LineBreakIterator iterator = new LineBreakIterator(text);
        while (iterator.hasNext()) {
            writer.write("# ");
            saveConvert((String) iterator.next(), ESCAPE_COMMENT, writer);
            writer.write(END_OF_LINE);
        }
    }

    /**
     * Performs the necessary conversion of an java string into a property
     * escaped string.
     *
     * @param text       the text to be escaped
     * @param escapeMode the mode that should be applied.
     * @param writer     the writer that should receive the content.
     * @throws IOException if writing fails
     */
    private void saveConvert(final String text, final int escapeMode,
                             final Writer writer)
        throws IOException {
        final char[] string = text.toCharArray();

        for (int x = 0; x < string.length; x++) {
            final char aChar = string[x];
            switch (aChar) {
                case ' ':
                    {
                        if ((escapeMode != ESCAPE_COMMENT) 
                                && (x == 0 || escapeMode == ESCAPE_KEY)) {
                            writer.write('\\');
                        }
                        writer.write(' ');
                        break;
                    }
                case '\\':
                    {
                        writer.write('\\');
                        writer.write('\\');
                        break;
                    }
                case '\t':
                    {
                        if (escapeMode == ESCAPE_COMMENT) {
                            writer.write(aChar);
                        }
                        else {
                            writer.write('\\');
                            writer.write('t');
                        }
                        break;
                    }
                case '\n':
                    {
                        writer.write('\\');
                        writer.write('n');
                        break;
                    }
                case '\r':
                    {
                        writer.write('\\');
                        writer.write('r');
                        break;
                    }
                case '\f':
                    {
                        if (escapeMode == ESCAPE_COMMENT) {
                            writer.write(aChar);
                        }
                        else {
                            writer.write('\\');
                            writer.write('f');
                        }
                        break;
                    }
                case '#':
                case '"':
                case '!':
                case '=':
                case ':':
                    {
                        if (escapeMode == ESCAPE_COMMENT) {
                            writer.write(aChar);
                        }
                        else {
                            writer.write('\\');
                            writer.write(aChar);
                        }
                        break;
                    }
                default:
                    if ((aChar < 0x0020) || (aChar > 0x007e)) {
                        writer.write('\\');
                        writer.write('u');
                        writer.write(HEX_CHARS[(aChar >> 12) & 0xF]);
                        writer.write(HEX_CHARS[(aChar >> 8) & 0xF]);
                        writer.write(HEX_CHARS[(aChar >> 4) & 0xF]);
                        writer.write(HEX_CHARS[aChar & 0xF]);
                    }
                    else {
                        writer.write(aChar);
                    }
            }
        }
    }

    /** A lookup-table. */
    private static final char[] HEX_CHARS =
        {'0', '1', '2', '3', '4', '5', '6', '7',
         '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
}
