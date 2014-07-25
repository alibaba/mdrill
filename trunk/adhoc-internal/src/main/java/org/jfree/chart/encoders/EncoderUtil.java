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
 * ----------------
 * EncoderUtil.java
 * ----------------
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
 * A collection of utility methods for encoding images and returning them as a
 * byte[] or writing them directly to an OutputStream.
 */
public class EncoderUtil {

    /**
     * Encode the image in a specific format.
     *
     * @param image  The image to be encoded.
     * @param format  The {@link ImageFormat} to use.
     *
     * @return The byte[] that is the encoded image.
     * @throws IOException
     */
    public static byte[] encode(BufferedImage image, String format)
        throws IOException {
        ImageEncoder imageEncoder = ImageEncoderFactory.newInstance(format);
        return imageEncoder.encode(image);
    }

    /**
     * Encode the image in a specific format.
     *
     * @param image  The image to be encoded.
     * @param format  The {@link ImageFormat} to use.
     * @param encodeAlpha  Whether to encode alpha transparency (not supported
     *                     by all ImageEncoders).
     * @return The byte[] that is the encoded image.
     * @throws IOException
     */
    public static byte[] encode(BufferedImage image, String format,
                                boolean encodeAlpha) throws IOException {
        ImageEncoder imageEncoder
            = ImageEncoderFactory.newInstance(format, encodeAlpha);
        return imageEncoder.encode(image);
    }

    /**
     * Encode the image in a specific format.
     *
     * @param image  The image to be encoded.
     * @param format  The {@link ImageFormat} to use.
     * @param quality  The quality to use for the image encoding (not supported
     *                 by all ImageEncoders).
     * @return The byte[] that is the encoded image.
     * @throws IOException
     */
    public static byte[] encode(BufferedImage image, String format,
                                float quality) throws IOException {
        ImageEncoder imageEncoder
            = ImageEncoderFactory.newInstance(format, quality);
        return imageEncoder.encode(image);
    }

    /**
     * Encode the image in a specific format.
     *
     * @param image  The image to be encoded.
     * @param format  The {@link ImageFormat} to use.
     * @param quality  The quality to use for the image encoding (not supported
     *                 by all ImageEncoders).
     * @param encodeAlpha  Whether to encode alpha transparency (not supported
     *                     by all ImageEncoders).
     * @return The byte[] that is the encoded image.
     * @throws IOException
     */
    public static byte[] encode(BufferedImage image, String format,
                                float quality, boolean encodeAlpha)
        throws IOException {
        ImageEncoder imageEncoder
            = ImageEncoderFactory.newInstance(format, quality, encodeAlpha);
        return imageEncoder.encode(image);
    }

    /**
     * Encode the image in a specific format and write it to an OutputStream.
     *
     * @param image  The image to be encoded.
     * @param format  The {@link ImageFormat} to use.
     * @param outputStream  The OutputStream to write the encoded image to.
     * @throws IOException
     */
    public static void writeBufferedImage(BufferedImage image, String format,
        OutputStream outputStream) throws IOException {
        ImageEncoder imageEncoder = ImageEncoderFactory.newInstance(format);
        imageEncoder.encode(image, outputStream);
    }

    /**
     * Encode the image in a specific format and write it to an OutputStream.
     *
     * @param image  The image to be encoded.
     * @param format  The {@link ImageFormat} to use.
     * @param outputStream  The OutputStream to write the encoded image to.
     * @param quality  The quality to use for the image encoding (not
     *                 supported by all ImageEncoders).
     * @throws IOException
     */
    public static void writeBufferedImage(BufferedImage image, String format,
        OutputStream outputStream, float quality) throws IOException {
        ImageEncoder imageEncoder
            = ImageEncoderFactory.newInstance(format, quality);
        imageEncoder.encode(image, outputStream);
    }

    /**
     * Encode the image in a specific format and write it to an OutputStream.
     *
     * @param image  The image to be encoded.
     * @param format  The {@link ImageFormat} to use.
     * @param outputStream  The OutputStream to write the encoded image to.
     * @param encodeAlpha  Whether to encode alpha transparency (not
     *                     supported by all ImageEncoders).
     * @throws IOException
     */
    public static void writeBufferedImage(BufferedImage image, String format,
        OutputStream outputStream, boolean encodeAlpha) throws IOException {
        ImageEncoder imageEncoder
            = ImageEncoderFactory.newInstance(format, encodeAlpha);
        imageEncoder.encode(image, outputStream);
    }

    /**
     * Encode the image in a specific format and write it to an OutputStream.
     *
     * @param image  The image to be encoded.
     * @param format  The {@link ImageFormat} to use.
     * @param outputStream  The OutputStream to write the encoded image to.
     * @param quality  The quality to use for the image encoding (not
     *                 supported by all ImageEncoders).
     * @param encodeAlpha  Whether to encode alpha transparency (not supported
     *                     by all ImageEncoders).
     * @throws IOException
     */
    public static void writeBufferedImage(BufferedImage image, String format,
        OutputStream outputStream, float quality, boolean encodeAlpha)
        throws IOException {
        ImageEncoder imageEncoder
            = ImageEncoderFactory.newInstance(format, quality, encodeAlpha);
        imageEncoder.encode(image, outputStream);
    }

}
