/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * -----------------
 * ImageEncoder.java
 * -----------------
 * (C) Copyright 2004-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 01-Aug-2004 : Initial version (RA);
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 *
 */

package org.jfree.chart.encoders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface for abstracting different types of image encoders.
 */
public interface ImageEncoder {

    /**
     * Encodes an image in a particular format.
     *
     * @param bufferedImage  The image to be encoded.
     *
     * @return The byte[] that is the encoded image.
     *
     * @throws IOException
     */
    public byte[] encode(BufferedImage bufferedImage) throws IOException;


    /**
     * Encodes an image in a particular format and writes it to an OutputStream.
     *
     * @param bufferedImage  The image to be encoded.
     * @param outputStream  The OutputStream to write the encoded image to.
     * @throws IOException
     */
    public void encode(BufferedImage bufferedImage, OutputStream outputStream)
        throws IOException;

    /**
     * Get the quality of the image encoding.
     *
     * @return A float representing the quality.
     */
    public float getQuality();

    /**
     * Set the quality of the image encoding (not supported by all
     * ImageEncoders).
     *
     * @param quality  A float representing the quality.
     */
    public void setQuality(float quality);

    /**
     * Get whether the encoder should encode alpha transparency.
     *
     * @return Whether the encoder is encoding alpha transparency.
     */
    public boolean isEncodingAlpha();

    /**
     * Set whether the encoder should encode alpha transparency (not
     * supported by all ImageEncoders).
     *
     * @param encodingAlpha  Whether the encoder should encode alpha
     *                       transparency.
     */
    public void setEncodingAlpha(boolean encodingAlpha);

}
