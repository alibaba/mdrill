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
 * ---------------
 * ParserUtil.java
 * ---------------
 * (C)opyright 2002-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ParserUtil.java,v 1.3 2005/10/18 13:25:44 mungady Exp $
 *
 * Changes
 * -------
 * 21-May-2002 : Contains utility functions to make parsing easier.
 * 10-Dec-2002 : Fixed issues reported by Checkstyle (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon;
 * 23-Sep-2003 : Minor Javadoc updates (DG);
 *
 */
package org.jfree.xml;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Basic helper functions to ease up the process of parsing.
 *
 * @author Thomas Morgner
 */
public class ParserUtil {

    /**
     * Parses the string <code>text</code> into an int. If text is null or does not
     * contain a parsable value, the message given in <code>message</code> is used to
     * throw a SAXException.
     *
     * @param text  the text to parse.
     * @param message  the error message if parsing fails.
     *
     * @return the int value.
     *
     * @throws SAXException if there is a problem with the parsing.
     */
    public static int parseInt(final String text, final String message) throws SAXException {
        if (text == null) {
            throw new SAXException(message);
        }

        try {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException nfe) {
            throw new SAXException("NumberFormatError: " + message);
        }
    }

    /**
     * Parses an integer.
     *
     * @param text  the text to parse.
     * @param defaultVal  the default value.
     *
     * @return the integer.
     */
    public static int parseInt(final String text, final int defaultVal) {
        if (text == null) {
            return defaultVal;
        }

        try {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException nfe) {
            return defaultVal;
        }
    }

    /**
     * Parses the string <code>text</code> into an float. If text is null or does not
     * contain a parsable value, the message given in <code>message</code> is used to
     * throw a SAXException.
     *
     * @param text  the text to parse.
     * @param message  the error message if parsing fails.
     *
     * @return the float value.
     *
     * @throws SAXException if there is a problem with the parsing.
     */
    public static float parseFloat(final String text, final String message) throws SAXException {
        if (text == null) {
            throw new SAXException(message);
        }
        try {
            return Float.parseFloat(text);
        }
        catch (NumberFormatException nfe) {
            throw new SAXException("NumberFormatError: " + message);
        }
    }

    /**
     * Parses the string <code>text</code> into an float. If text is null or does not
     * contain a parsable value, the message given in <code>message</code> is used to
     * throw a SAXException.
     *
     * @param text  the text to parse.
     * @param defaultVal the defaultValue returned if parsing fails.
     *
     * @return the float value.
     */
    public static float parseFloat(final String text, final float defaultVal) {
        if (text == null) {
            return defaultVal;
        }
        try {
            return Float.parseFloat(text);
        }
        catch (NumberFormatException nfe) {
            return defaultVal;
        }
    }

    /**
     * Parses a boolean. If the string <code>text</code> contains the value of "true", the
     * true value is returned, else false is returned.
     *
     * @param text  the text to parse.
     * @param defaultVal  the default value.
     *
     * @return a boolean.
     */
    public static boolean parseBoolean(final String text, final boolean defaultVal) {
        if (text == null) {
            return defaultVal;
        }
        return text.equalsIgnoreCase("true");
    }

    /**
     * Parses a string. If the <code>text</code> is null, defaultval is returned.
     *
     * @param text  the text to parse.
     * @param defaultVal  the default value.
     *
     * @return a string.
     */
    public static String parseString(final String text, final String defaultVal) {
        if (text == null) {
            return defaultVal;
        }
        return text;
    }

    /**
     * Creates a basic stroke given the width contained as float in the given string.
     * If the string could not be parsed into a float, a basic stroke with the width of
     * 1 is returned.
     *
     * @param weight  a string containing a number (the stroke weight).
     *
     * @return the stroke.
     */
    public static Stroke parseStroke(final String weight) {
        try {
            if (weight != null) {
                final Float w = new Float(weight);
                return new BasicStroke(w.floatValue());
            }
        }
        catch (NumberFormatException nfe) {
            //Log.warn("Invalid weight for stroke", nfe);
        }
        return new BasicStroke(1);
    }

    /**
     * Parses a color entry. If the entry is in hexadecimal or ocal notation, the color is
     * created using Color.decode(). If the string denotes a constant name of on of the color
     * constants defined in java.awt.Color, this constant is used.
     * <p>
     * As fallback the color black is returned if no color can be parsed.
     *
     * @param color  the color (as a string).
     *
     * @return the paint.
     */
    public static Color parseColor(final String color) {
        return parseColor(color, Color.black);
    }

    /**
     * Parses a color entry. If the entry is in hexadecimal or octal notation, the color is
     * created using Color.decode(). If the string denotes a constant name of one of the color
     * constants defined in java.awt.Color, this constant is used.
     * <p>
     * As fallback the supplied default value is returned if no color can be parsed.
     *
     * @param color  the color (as a string).
     * @param defaultValue  the default value (returned if no color can be parsed).
     *
     * @return the paint.
     */
    public static Color parseColor(final String color, final Color defaultValue) {
        if (color == null) {
            return defaultValue;
        }
        try {
            // get color by hex or octal value
            return Color.decode(color);
        }
        catch (NumberFormatException nfe) {
            // if we can't decode lets try to get it by name
            try {
                // try to get a color by name using reflection
                // black is used for an instance and not for the color itselfs
                final Field f = Color.class.getField(color);

                return (Color) f.get(null);
            }
            catch (Exception ce) {
                //Log.warn("No such Color : " + color);
                // if we can't get any color return black
                return defaultValue;
            }
        }
    }


    /**
     * Parses a position of an element. If a relative postion is given, the returnvalue
     * is a negative number between 0 and -100.
     *
     * @param value  the value.
     * @param exceptionMessage  the exception message.
     *
     * @return the float value.
     *
     * @throws SAXException if there is a problem parsing the string.
     */
    public static float parseRelativeFloat(final String value, final String exceptionMessage)
        throws SAXException {
        if (value == null) {
            throw new SAXException(exceptionMessage);
        }
        final String tvalue = value.trim();
        if (tvalue.endsWith("%")) {
            final String number = tvalue.substring(0, tvalue.indexOf("%"));
            final float f = parseFloat(number, exceptionMessage) * -1.0f;
            return f;
        }
        else {
            return parseFloat(tvalue, exceptionMessage);
        }
    }

    /**
     * Parses an element position. The position is stored in the attributes "x", "y", "width" and
     * "height". The attributes are allowed to have relative notion.
     *
     * @param atts  the attributes.
     *
     * @return the element position.
     *
     * @throws SAXException if there is a problem getting the element position.
     */
    public static Rectangle2D getElementPosition(final Attributes atts) throws SAXException {
        final float x = ParserUtil.parseRelativeFloat(atts.getValue("x"),
            "Element x not specified");
        final float y = ParserUtil.parseRelativeFloat(atts.getValue("y"),
            "Element y not specified");
        final float w = ParserUtil.parseRelativeFloat(atts.getValue("width"),
            "Element width not specified");
        final float h = ParserUtil.parseRelativeFloat(atts.getValue("height"),
            "Element height not specified");
        final Rectangle2D.Float retval = new Rectangle2D.Float(x, y, w, h);
        return retval;
    }

}
