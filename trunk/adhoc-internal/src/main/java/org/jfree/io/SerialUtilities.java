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
 * SerialUtilities.java
 * --------------------
 * (C) Copyright 2000-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Arik Levin;
 *
 * $Id: SerialUtilities.java,v 1.15 2011/10/11 12:45:02 matinh Exp $
 *
 * Changes
 * -------
 * 25-Mar-2003 : Version 1 (DG);
 * 18-Sep-2003 : Added capability to serialize GradientPaint (DG);
 * 26-Apr-2004 : Added read/writePoint2D() methods (DG);
 * 22-Feb-2005 : Added support for Arc2D - see patch 1147035 by Arik Levin (DG);
 * 29-Jul-2005 : Added support for AttributedString (DG);
 * 10-Oct-2011 : Added support for AlphaComposite instances (MH);
 *
 */

package org.jfree.io;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Map;

/**
 * A class containing useful utility methods relating to serialization.
 *
 * @author David Gilbert
 */
public class SerialUtilities {

    /**
     * Private constructor prevents object creation.
     */
    private SerialUtilities() {
    }

    /**
     * Returns <code>true</code> if a class implements <code>Serializable</code>
     * and <code>false</code> otherwise.
     *
     * @param c  the class.
     *
     * @return A boolean.
     */
    public static boolean isSerializable(final Class c) {
        /**
        final Class[] interfaces = c.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(Serializable.class)) {
                return true;
            }
        }
        Class cc = c.getSuperclass();
        if (cc != null) {
            return isSerializable(cc);
        }
         */
        return (Serializable.class.isAssignableFrom(c));
    }

    /**
     * Reads a <code>Paint</code> object that has been serialised by the
     * {@link SerialUtilities#writePaint(Paint, ObjectOutputStream)} method.
     *
     * @param stream  the input stream (<code>null</code> not permitted).
     *
     * @return The paint object (possibly <code>null</code>).
     *
     * @throws IOException  if there is an I/O problem.
     * @throws ClassNotFoundException  if there is a problem loading a class.
     */
    public static Paint readPaint(final ObjectInputStream stream)
        throws IOException, ClassNotFoundException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        Paint result = null;
        final boolean isNull = stream.readBoolean();
        if (!isNull) {
            final Class c = (Class) stream.readObject();
            if (isSerializable(c)) {
                result = (Paint) stream.readObject();
            }
            else if (c.equals(GradientPaint.class)) {
                final float x1 = stream.readFloat();
                final float y1 = stream.readFloat();
                final Color c1 = (Color) stream.readObject();
                final float x2 = stream.readFloat();
                final float y2 = stream.readFloat();
                final Color c2 = (Color) stream.readObject();
                final boolean isCyclic = stream.readBoolean();
                result = new GradientPaint(x1, y1, c1, x2, y2, c2, isCyclic);
            }
        }
        return result;

    }

    /**
     * Serialises a <code>Paint</code> object.
     *
     * @param paint  the paint object (<code>null</code> permitted).
     * @param stream  the output stream (<code>null</code> not permitted).
     *
     * @throws IOException if there is an I/O error.
     */
    public static void writePaint(final Paint paint,
                                  final ObjectOutputStream stream)
        throws IOException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        if (paint != null) {
            stream.writeBoolean(false);
            stream.writeObject(paint.getClass());
            if (paint instanceof Serializable) {
                stream.writeObject(paint);
            }
            else if (paint instanceof GradientPaint) {
                final GradientPaint gp = (GradientPaint) paint;
                stream.writeFloat((float) gp.getPoint1().getX());
                stream.writeFloat((float) gp.getPoint1().getY());
                stream.writeObject(gp.getColor1());
                stream.writeFloat((float) gp.getPoint2().getX());
                stream.writeFloat((float) gp.getPoint2().getY());
                stream.writeObject(gp.getColor2());
                stream.writeBoolean(gp.isCyclic());
            }
        }
        else {
            stream.writeBoolean(true);
        }

    }

    /**
     * Reads a <code>Stroke</code> object that has been serialised by the
     * {@link SerialUtilities#writeStroke(Stroke, ObjectOutputStream)} method.
     *
     * @param stream  the input stream (<code>null</code> not permitted).
     *
     * @return The stroke object (possibly <code>null</code>).
     *
     * @throws IOException  if there is an I/O problem.
     * @throws ClassNotFoundException  if there is a problem loading a class.
     */
    public static Stroke readStroke(final ObjectInputStream stream)
        throws IOException, ClassNotFoundException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        Stroke result = null;
        final boolean isNull = stream.readBoolean();
        if (!isNull) {
            final Class c = (Class) stream.readObject();
            if (c.equals(BasicStroke.class)) {
                final float width = stream.readFloat();
                final int cap = stream.readInt();
                final int join = stream.readInt();
                final float miterLimit = stream.readFloat();
                final float[] dash = (float[]) stream.readObject();
                final float dashPhase = stream.readFloat();
                result = new BasicStroke(
                    width, cap, join, miterLimit, dash, dashPhase
                );
            }
            else {
                result = (Stroke) stream.readObject();
            }
        }
        return result;

    }

    /**
     * Serialises a <code>Stroke</code> object.  This code handles the
     * <code>BasicStroke</code> class which is the only <code>Stroke</code>
     * implementation provided by the JDK (and isn't directly
     * <code>Serializable</code>).
     *
     * @param stroke  the stroke object (<code>null</code> permitted).
     * @param stream  the output stream (<code>null</code> not permitted).
     *
     * @throws IOException if there is an I/O error.
     */
    public static void writeStroke(final Stroke stroke,
                                   final ObjectOutputStream stream)
        throws IOException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        if (stroke != null) {
            stream.writeBoolean(false);
            if (stroke instanceof BasicStroke) {
                final BasicStroke s = (BasicStroke) stroke;
                stream.writeObject(BasicStroke.class);
                stream.writeFloat(s.getLineWidth());
                stream.writeInt(s.getEndCap());
                stream.writeInt(s.getLineJoin());
                stream.writeFloat(s.getMiterLimit());
                stream.writeObject(s.getDashArray());
                stream.writeFloat(s.getDashPhase());
            }
            else {
                stream.writeObject(stroke.getClass());
                stream.writeObject(stroke);
            }
        }
        else {
            stream.writeBoolean(true);
        }
    }

    /**
     * Reads a <code>Composite</code> object that has been serialised by the
     * {@link SerialUtilities#writeComposite(Composite, ObjectOutputStream)}
     * method.
     *
     * @param stream  the input stream (<code>null</code> not permitted).
     *
     * @return The composite object (possibly <code>null</code>).
     *
     * @throws IOException  if there is an I/O problem.
     * @throws ClassNotFoundException  if there is a problem loading a class.
     * 
     * @since 1.0.17
     */
    public static Composite readComposite(final ObjectInputStream stream)
        throws IOException, ClassNotFoundException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        Composite result = null;
        final boolean isNull = stream.readBoolean();
        if (!isNull) {
            final Class c = (Class) stream.readObject();
            if (isSerializable(c)) {
                result = (Composite) stream.readObject();
            }
            else if (c.equals(AlphaComposite.class)) {
                final int rule = stream.readInt();
                final float alpha = stream.readFloat();
                result = AlphaComposite.getInstance(rule, alpha);
            }
        }
        return result;

    }

    /**
     * Serialises a <code>Composite</code> object.
     *
     * @param composite  the composite object (<code>null</code> permitted).
     * @param stream  the output stream (<code>null</code> not permitted).
     *
     * @throws IOException if there is an I/O error.
     * 
     * @since 1.0.17
     */
    public static void writeComposite(final Composite composite,
                                      final ObjectOutputStream stream)
        throws IOException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        if (composite != null) {
            stream.writeBoolean(false);
            stream.writeObject(composite.getClass());
            if (composite instanceof Serializable) {
                stream.writeObject(composite);
            }
            else if (composite instanceof AlphaComposite) {
                final AlphaComposite ac = (AlphaComposite) composite;
                stream.writeInt(ac.getRule());
                stream.writeFloat(ac.getAlpha());
            }
        }
        else {
            stream.writeBoolean(true);
        }
    }

    /**
     * Reads a <code>Shape</code> object that has been serialised by the
     * {@link #writeShape(Shape, ObjectOutputStream)} method.
     *
     * @param stream  the input stream (<code>null</code> not permitted).
     *
     * @return The shape object (possibly <code>null</code>).
     *
     * @throws IOException  if there is an I/O problem.
     * @throws ClassNotFoundException  if there is a problem loading a class.
     */
    public static Shape readShape(final ObjectInputStream stream)
        throws IOException, ClassNotFoundException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        Shape result = null;
        final boolean isNull = stream.readBoolean();
        if (!isNull) {
            final Class c = (Class) stream.readObject();
            if (c.equals(Line2D.class)) {
                final double x1 = stream.readDouble();
                final double y1 = stream.readDouble();
                final double x2 = stream.readDouble();
                final double y2 = stream.readDouble();
                result = new Line2D.Double(x1, y1, x2, y2);
            }
            else if (c.equals(Rectangle2D.class)) {
                final double x = stream.readDouble();
                final double y = stream.readDouble();
                final double w = stream.readDouble();
                final double h = stream.readDouble();
                result = new Rectangle2D.Double(x, y, w, h);
            }
            else if (c.equals(Ellipse2D.class)) {
                final double x = stream.readDouble();
                final double y = stream.readDouble();
                final double w = stream.readDouble();
                final double h = stream.readDouble();
                result = new Ellipse2D.Double(x, y, w, h);
            }
            else if (c.equals(Arc2D.class)) {
                final double x = stream.readDouble();
                final double y = stream.readDouble();
                final double w = stream.readDouble();
                final double h = stream.readDouble();
                final double as = stream.readDouble(); // Angle Start
                final double ae = stream.readDouble(); // Angle Extent
                final int at = stream.readInt();       // Arc type
                result = new Arc2D.Double(x, y, w, h, as, ae, at);
            }
            else if (c.equals(GeneralPath.class)) {
                final GeneralPath gp = new GeneralPath();
                final float[] args = new float[6];
                boolean hasNext = stream.readBoolean();
                while (!hasNext) {
                    final int type = stream.readInt();
                    for (int i = 0; i < 6; i++) {
                        args[i] = stream.readFloat();
                    }
                    switch (type) {
                        case PathIterator.SEG_MOVETO :
                            gp.moveTo(args[0], args[1]);
                            break;
                        case PathIterator.SEG_LINETO :
                            gp.lineTo(args[0], args[1]);
                            break;
                        case PathIterator.SEG_CUBICTO :
                            gp.curveTo(args[0], args[1], args[2],
                                    args[3], args[4], args[5]);
                            break;
                        case PathIterator.SEG_QUADTO :
                            gp.quadTo(args[0], args[1], args[2], args[3]);
                            break;
                        case PathIterator.SEG_CLOSE :
                            gp.closePath();
                            break;
                        default :
                            throw new RuntimeException(
                                    "JFreeChart - No path exists");
                    }
                    gp.setWindingRule(stream.readInt());
                    hasNext = stream.readBoolean();
                }
                result = gp;
            }
            else {
                result = (Shape) stream.readObject();
            }
        }
        return result;

    }

    /**
     * Serialises a <code>Shape</code> object.
     *
     * @param shape  the shape object (<code>null</code> permitted).
     * @param stream  the output stream (<code>null</code> not permitted).
     *
     * @throws IOException if there is an I/O error.
     */
    public static void writeShape(final Shape shape,
                                  final ObjectOutputStream stream)
        throws IOException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        if (shape != null) {
            stream.writeBoolean(false);
            if (shape instanceof Line2D) {
                final Line2D line = (Line2D) shape;
                stream.writeObject(Line2D.class);
                stream.writeDouble(line.getX1());
                stream.writeDouble(line.getY1());
                stream.writeDouble(line.getX2());
                stream.writeDouble(line.getY2());
            }
            else if (shape instanceof Rectangle2D) {
                final Rectangle2D rectangle = (Rectangle2D) shape;
                stream.writeObject(Rectangle2D.class);
                stream.writeDouble(rectangle.getX());
                stream.writeDouble(rectangle.getY());
                stream.writeDouble(rectangle.getWidth());
                stream.writeDouble(rectangle.getHeight());
            }
            else if (shape instanceof Ellipse2D) {
                final Ellipse2D ellipse = (Ellipse2D) shape;
                stream.writeObject(Ellipse2D.class);
                stream.writeDouble(ellipse.getX());
                stream.writeDouble(ellipse.getY());
                stream.writeDouble(ellipse.getWidth());
                stream.writeDouble(ellipse.getHeight());
            }
            else if (shape instanceof Arc2D) {
                final Arc2D arc = (Arc2D) shape;
                stream.writeObject(Arc2D.class);
                stream.writeDouble(arc.getX());
                stream.writeDouble(arc.getY());
                stream.writeDouble(arc.getWidth());
                stream.writeDouble(arc.getHeight());
                stream.writeDouble(arc.getAngleStart());
                stream.writeDouble(arc.getAngleExtent());
                stream.writeInt(arc.getArcType());
            }
            else if (shape instanceof GeneralPath) {
                stream.writeObject(GeneralPath.class);
                final PathIterator pi = shape.getPathIterator(null);
                final float[] args = new float[6];
                stream.writeBoolean(pi.isDone());
                while (!pi.isDone()) {
                    final int type = pi.currentSegment(args);
                    stream.writeInt(type);
                    // TODO: could write this to only stream the values
                    // required for the segment type
                    for (int i = 0; i < 6; i++) {
                        stream.writeFloat(args[i]);
                    }
                    stream.writeInt(pi.getWindingRule());
                    pi.next();
                    stream.writeBoolean(pi.isDone());
                }
            }
            else {
                stream.writeObject(shape.getClass());
                stream.writeObject(shape);
            }
        }
        else {
            stream.writeBoolean(true);
        }
    }

    /**
     * Reads a <code>Point2D</code> object that has been serialised by the
     * {@link #writePoint2D(Point2D, ObjectOutputStream)} method.
     *
     * @param stream  the input stream (<code>null</code> not permitted).
     *
     * @return The point object (possibly <code>null</code>).
     *
     * @throws IOException  if there is an I/O problem.
     */
    public static Point2D readPoint2D(final ObjectInputStream stream)
        throws IOException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        Point2D result = null;
        final boolean isNull = stream.readBoolean();
        if (!isNull) {
            final double x = stream.readDouble();
            final double y = stream.readDouble();
            result = new Point2D.Double(x, y);
        }
        return result;

    }

    /**
     * Serialises a <code>Point2D</code> object.
     *
     * @param p  the point object (<code>null</code> permitted).
     * @param stream  the output stream (<code>null</code> not permitted).
     *
     * @throws IOException if there is an I/O error.
     */
    public static void writePoint2D(final Point2D p,
                                    final ObjectOutputStream stream)
        throws IOException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        if (p != null) {
            stream.writeBoolean(false);
            stream.writeDouble(p.getX());
            stream.writeDouble(p.getY());
        }
        else {
            stream.writeBoolean(true);
        }
    }

    /**
     * Reads a <code>AttributedString</code> object that has been serialised by
     * the {@link SerialUtilities#writeAttributedString(AttributedString,
     * ObjectOutputStream)} method.
     *
     * @param stream  the input stream (<code>null</code> not permitted).
     *
     * @return The attributed string object (possibly <code>null</code>).
     *
     * @throws IOException  if there is an I/O problem.
     * @throws ClassNotFoundException  if there is a problem loading a class.
     */
    public static AttributedString readAttributedString(
            ObjectInputStream stream)
            throws IOException, ClassNotFoundException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        AttributedString result = null;
        final boolean isNull = stream.readBoolean();
        if (!isNull) {
            // read string and attributes then create result
            String plainStr = (String) stream.readObject();
            result = new AttributedString(plainStr);
            char c = stream.readChar();
            int start = 0;
            while (c != CharacterIterator.DONE) {
                int limit = stream.readInt();
                Map atts = (Map) stream.readObject();
                result.addAttributes(atts, start, limit);
                start = limit;
                c = stream.readChar();
            }
        }
        return result;
    }

    /**
     * Serialises an <code>AttributedString</code> object.
     *
     * @param as  the attributed string object (<code>null</code> permitted).
     * @param stream  the output stream (<code>null</code> not permitted).
     *
     * @throws IOException if there is an I/O error.
     */
    public static void writeAttributedString(AttributedString as,
            ObjectOutputStream stream) throws IOException {

        if (stream == null) {
            throw new IllegalArgumentException("Null 'stream' argument.");
        }
        if (as != null) {
            stream.writeBoolean(false);
            AttributedCharacterIterator aci = as.getIterator();
            // build a plain string from aci
            // then write the string
            StringBuffer plainStr = new StringBuffer();
            char current = aci.first();
            while (current != CharacterIterator.DONE) {
                plainStr = plainStr.append(current);
                current = aci.next();
            }
            stream.writeObject(plainStr.toString());

            // then write the attributes and limits for each run
            current = aci.first();
            int begin = aci.getBeginIndex();
            while (current != CharacterIterator.DONE) {
                // write the current character - when the reader sees that this
                // is not CharacterIterator.DONE, it will know to read the
                // run limits and attributes
                stream.writeChar(current);

                // now write the limit, adjusted as if beginIndex is zero
                int limit = aci.getRunLimit();
                stream.writeInt(limit - begin);

                // now write the attribute set
                Map atts = new HashMap(aci.getAttributes());
                stream.writeObject(atts);
                current = aci.setIndex(limit);
            }
            // write a character that signals to the reader that all runs
            // are done...
            stream.writeChar(CharacterIterator.DONE);
        }
        else {
            // write a flag that indicates a null
            stream.writeBoolean(true);
        }

    }

}

