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
 * --------------------------
 * SunJPEGEncoderAdapter.java
 * --------------------------
 * (C) Copyright 2004-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 01-Aug-2004 : Initial version (RA);
 * 01-Nov-2005 : To remove the dependency on non-supported APIs, use ImageIO
 *               instead of com.sun.image.codec.jpeg.JPEGImageEncoder - this
 *               adapter will only be available on JDK 1.4 or later (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 20-Jul-2006 : Pass quality setting to ImageIO. Also increased default
 *               value to 0.95 (DG);
 *
 */

package org.jfree.chart.encoders;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Adapter class for the Sun JPEG Encoder.  The {@link ImageEncoderFactory}
 * will only return a reference to this class by default if the library has
 * been compiled under a JDK 1.4+ and is being run using a JRE 1.4+.
 */
public class SunJPEGEncoderAdapter implements ImageEncoder {

    /** The quality setting (in the range 0.0f to 1.0f). */
    private float quality = 0.95f;

    /**
     * Creates a new <code>SunJPEGEncoderAdapter</code> instance.
     */
    public SunJPEGEncoderAdapter() {
    }

    /**
     * Returns the quality of the image encoding, which is a number in the
     * range 0.0f to 1.0f (higher values give better quality output, but larger
     * file sizes).  The default value is 0.95f.
     *
     * @return A float representing the quality, in the range 0.0f to 1.0f.
     *
     * @see #setQuality(float)
     */
    public float getQuality() {
        return this.quality;
    }

    /**
     * Set the quality of the image encoding.
     *
     * @param quality  A float representing the quality (in the range 0.0f to
     *     1.0f).
     *
     * @see #getQuality()
     */
    public void setQuality(float quality) {
        if (quality < 0.0f || quality > 1.0f) {
            throw new IllegalArgumentException(
                    "The 'quality' must be in the range 0.0f to 1.0f");
        }
        this.quality = quality;
    }

    /**
     * Returns <code>false</code> always, indicating that this encoder does not
     * encode alpha transparency.
     *
     * @return <code>false</code>.
     */
    public boolean isEncodingAlpha() {
        return false;
    }

    /**
     * Set whether the encoder should encode alpha transparency (this is not
     * supported for JPEG, so this method does nothing).
     *
     * @param encodingAlpha  ignored.
     */
    public void setEncodingAlpha(boolean encodingAlpha) {
        //  No op
    }

    /**
     * Encodes an image in JPEG format.
     *
     * @param bufferedImage  the image to be encoded (<code>null</code> not
     *     permitted).
     *
     * @return The byte[] that is the encoded image.
     *
     * @throws IOException if there is an I/O problem.
     * @throws NullPointerException if <code>bufferedImage</code> is
     *     <code>null</code>.
     */
    public byte[] encode(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encode(bufferedImage, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Encodes an image in JPEG format and writes it to an output stream.
     *
     * @param bufferedImage  the image to be encoded (<code>null</code> not
     *     permitted).
     * @param outputStream  the OutputStream to write the encoded image to
     *     (<code>null</code> not permitted).
     *
     * @throws IOException if there is an I/O problem.
     * @throws NullPointerException if <code>bufferedImage</code> is
     *     <code>null</code>.
     */
    public void encode(BufferedImage bufferedImage, OutputStream outputStream)
            throws IOException {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("Null 'image' argument.");
        }
        if (outputStream == null) {
            throw new IllegalArgumentException("Null 'outputStream' argument.");
        }
        Iterator iterator = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = (ImageWriter) iterator.next();
        ImageWriteParam p = writer.getDefaultWriteParam();
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        p.setCompressionQuality(this.quality);
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(bufferedImage, null, null), p);
        ios.flush();
        writer.dispose();
        ios.close();
    }

}
