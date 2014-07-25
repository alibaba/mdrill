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
 * ------------------------------
 * KeypointPNGEncoderAdapter.java
 * ------------------------------
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

import com.keypoint.PngEncoder;

/**
 * Adapter class for the Keypoint PNG Encoder.  The ImageEncoderFactory will
 * only return a reference to this class by default if the library has been
 * compiled under a JDK < 1.4 or is being run using a JDK < 1.4.
 */
public class KeypointPNGEncoderAdapter implements ImageEncoder {

    /** The quality setting. */
    private int quality = 9;

    /** Encode alpha? */
    private boolean encodingAlpha = false;

    /**
     * Get the quality of the image encoding.  The underlying encoder uses int
     * values:  0 for no compression, and values 1 through 9 for various levels
     * of compression (1 is best speed, 9 is best compression).
     *
     * @return A float representing the quality.
     */
    public float getQuality() {
        return this.quality;
    }

    /**
     * Set the quality of the image encoding (supported).  The underlying
     * encoder uses int values:  0 for no compression, and values 1 through 9
     * for various levels of compression (1 is best speed, 9 is best
     * compression).
     *
     * @param quality  A float representing the quality.
     */
    public void setQuality(float quality) {
        this.quality = (int) quality;
    }

    /**
     * Get whether the encoder should encode alpha transparency.
     *
     * @return Whether the encoder is encoding alpha transparency.
     */
    public boolean isEncodingAlpha() {
        return this.encodingAlpha;
    }

    /**
     * Set whether the encoder should encode alpha transparency (supported).
     *
     * @param encodingAlpha  Whether the encoder should encode alpha
     *                       transparency.
     */
    public void setEncodingAlpha(boolean encodingAlpha) {
        this.encodingAlpha = encodingAlpha;
    }

    /**
     * Encodes an image in PNG format.
     *
     * @param bufferedImage  The image to be encoded.
     * @return The byte[] that is the encoded image.
     * @throws IOException
     */
    public byte[] encode(BufferedImage bufferedImage) throws IOException {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("Null 'image' argument.");
        }
        PngEncoder encoder = new PngEncoder(bufferedImage, this.encodingAlpha,
                0, this.quality);
        return encoder.pngEncode();
    }

    /**
     * Encodes an image in PNG format and writes it to an
     * <code>OutputStream</code>.
     *
     * @param bufferedImage  The image to be encoded.
     * @param outputStream  The OutputStream to write the encoded image to.
     * @throws IOException
     */
    public void encode(BufferedImage bufferedImage, OutputStream outputStream)
        throws IOException {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("Null 'image' argument.");
        }
        if (outputStream == null) {
            throw new IllegalArgumentException("Null 'outputStream' argument.");
        }
        PngEncoder encoder = new PngEncoder(bufferedImage, this.encodingAlpha,
                0, this.quality);
        outputStream.write(encoder.pngEncode());
    }

}
