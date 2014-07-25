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
 * -------------------
 * ChartUtilities.java
 * -------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Wolfgang Irler;
 *                   Richard Atkinson;
 *                   Xavier Poinsard;
 *
 * Changes
 * -------
 * 11-Dec-2001 : Version 1.  The JPEG method comes from Wolfgang Irler's
 *               JFreeChartServletDemo class (DG);
 * 23-Jan-2002 : Changed saveChartAsXXX() methods to pass IOExceptions back to
 *               caller (DG);
 * 26-Jun-2002 : Added image map methods (DG);
 * 05-Aug-2002 : Added writeBufferedImage methods
 *               Modified writeImageMap method to support flexible image
 *               maps (RA);
 * 26-Aug-2002 : Added saveChartAsJPEG and writeChartAsJPEG methods with info
 *               objects (RA);
 * 05-Sep-2002 : Added writeImageMap() method to support OverLIB
 *               - http://www.bosrup.com/web/overlib (RA);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 17-Oct-2002 : Exposed JPEG quality setting and PNG compression level as
 *               parameters (DG);
 * 25-Oct-2002 : Fixed writeChartAsJPEG() empty method bug (DG);
 * 13-Mar-2003 : Updated writeImageMap method as suggested by Xavier Poinsard
 *               (see Feature Request 688079) (DG);
 * 12-Aug-2003 : Added support for custom image maps using
 *               ToolTipTagFragmentGenerator and URLTagFragmentGenerator (RA);
 * 02-Sep-2003 : Separated PNG encoding from writing chart to an
 *               OutputStream (RA);
 * 04-Dec-2003 : Chart draw() method modified to include anchor point (DG);
 * 20-Feb-2004 : Edited Javadocs and added argument checking (DG);
 * 05-Apr-2004 : Fixed problem with buffered image type (DG);
 * 01-Aug-2004 : Modified to use EncoderUtil for all image encoding (RA);
 * 02-Aug-2004 : Delegated image map related functionality to ImageMapUtil (RA);
 * 13-Jan-2005 : Renamed ImageMapUtil --> ImageMapUtilities, removed method
 *               writeImageMap(PrintWriter, String, ChartRenderingInfo) which
 *               exists in ImageMapUtilities (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 06-Feb-2006 : API doc update (DG);
 * 19-Mar-2007 : Use try-finally to close output stream in saveChartAsXXX()
 *               methods (DG);
 * 10-Jan-2008 : Fix bug 1868251 - don't create image with transparency when
 *               saving to JPEG format (DG);
 *
 */

package org.jfree.chart;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.imagemap.OverLIBToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;

/**
 * A collection of utility methods for JFreeChart.  Includes methods for
 * converting charts to image formats (PNG and JPEG) plus creating simple HTML
 * image maps.
 *
 * @see ImageMapUtilities
 */
public abstract class ChartUtilities {

    /**
     * Applies the current theme to the specified chart.  This method is
     * provided for convenience, the theme itself is stored in the
     * {@link ChartFactory} class.
     *
     * @param chart  the chart (<code>null</code> not permitted).
     *
     * @since 1.0.11
     */
    public static void applyCurrentTheme(JFreeChart chart) {
        ChartFactory.getChartTheme().apply(chart);
    }

    /**
     * Writes a chart to an output stream in PNG format.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsPNG(OutputStream out, JFreeChart chart,
            int width, int height) throws IOException {

        // defer argument checking...
        writeChartAsPNG(out, chart, width, height, null);

    }

    /**
     * Writes a chart to an output stream in PNG format.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param encodeAlpha  encode alpha?
     * @param compression  the compression level (0-9).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsPNG(OutputStream out, JFreeChart chart,
            int width, int height, boolean encodeAlpha, int compression)
            throws IOException {

        // defer argument checking...
        ChartUtilities.writeChartAsPNG(out, chart, width, height, null,
                encodeAlpha, compression);

    }

    /**
     * Writes a chart to an output stream in PNG format.  This method allows
     * you to pass in a {@link ChartRenderingInfo} object, to collect
     * information about the chart dimensions/entities.  You will need this
     * info if you want to create an HTML image map.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  the chart rendering info (<code>null</code> permitted).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsPNG(OutputStream out, JFreeChart chart,
            int width, int height,  ChartRenderingInfo info)
            throws IOException {

        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }
        BufferedImage bufferedImage
                = chart.createBufferedImage(width, height, info);
        EncoderUtil.writeBufferedImage(bufferedImage, ImageFormat.PNG, out);
    }

    /**
     * Writes a chart to an output stream in PNG format.  This method allows
     * you to pass in a {@link ChartRenderingInfo} object, to collect
     * information about the chart dimensions/entities.  You will need this
     * info if you want to create an HTML image map.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  carries back chart rendering info (<code>null</code>
     *              permitted).
     * @param encodeAlpha  encode alpha?
     * @param compression  the PNG compression level (0-9).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsPNG(OutputStream out, JFreeChart chart,
            int width, int height, ChartRenderingInfo info,
            boolean encodeAlpha, int compression) throws IOException {

        if (out == null) {
            throw new IllegalArgumentException("Null 'out' argument.");
        }
        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }
        BufferedImage chartImage = chart.createBufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB, info);
        ChartUtilities.writeBufferedImageAsPNG(out, chartImage, encodeAlpha,
                compression);

    }

    /**
     * Writes a scaled version of a chart to an output stream in PNG format.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the unscaled chart width.
     * @param height  the unscaled chart height.
     * @param widthScaleFactor  the horizontal scale factor.
     * @param heightScaleFactor  the vertical scale factor.
     *
     * @throws IOException if there are any I/O problems.
     */
    public static void writeScaledChartAsPNG(OutputStream out,
            JFreeChart chart, int width, int height, int widthScaleFactor,
            int heightScaleFactor) throws IOException {

        if (out == null) {
            throw new IllegalArgumentException("Null 'out' argument.");
        }
        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }

        double desiredWidth = width * widthScaleFactor;
        double desiredHeight = height * heightScaleFactor;
        double defaultWidth = width;
        double defaultHeight = height;
        boolean scale = false;

        // get desired width and height from somewhere then...
        if ((widthScaleFactor != 1) || (heightScaleFactor != 1)) {
            scale = true;
        }

        double scaleX = desiredWidth / defaultWidth;
        double scaleY = desiredHeight / defaultHeight;

        BufferedImage image = new BufferedImage((int) desiredWidth,
                (int) desiredHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        if (scale) {
            AffineTransform saved = g2.getTransform();
            g2.transform(AffineTransform.getScaleInstance(scaleX, scaleY));
            chart.draw(g2, new Rectangle2D.Double(0, 0, defaultWidth,
                    defaultHeight), null, null);
            g2.setTransform(saved);
            g2.dispose();
        }
        else {
            chart.draw(g2, new Rectangle2D.Double(0, 0, defaultWidth,
                    defaultHeight), null, null);
        }
        out.write(encodeAsPNG(image));

    }

    /**
     * Saves a chart to the specified file in PNG format.
     *
     * @param file  the file name (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void saveChartAsPNG(File file, JFreeChart chart,
            int width, int height) throws IOException {

        // defer argument checking...
        saveChartAsPNG(file, chart, width, height, null);

    }

    /**
     * Saves a chart to a file in PNG format.  This method allows you to pass
     * in a {@link ChartRenderingInfo} object, to collect information about the
     * chart dimensions/entities.  You will need this info if you want to
     * create an HTML image map.
     *
     * @param file  the file (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  the chart rendering info (<code>null</code> permitted).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void saveChartAsPNG(File file, JFreeChart chart,
            int width, int height, ChartRenderingInfo info)
        throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("Null 'file' argument.");
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            ChartUtilities.writeChartAsPNG(out, chart, width, height, info);
        }
        finally {
            out.close();
        }
    }

    /**
     * Saves a chart to a file in PNG format.  This method allows you to pass
     * in a {@link ChartRenderingInfo} object, to collect information about the
     * chart dimensions/entities.  You will need this info if you want to
     * create an HTML image map.
     *
     * @param file  the file (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  the chart rendering info (<code>null</code> permitted).
     * @param encodeAlpha  encode alpha?
     * @param compression  the PNG compression level (0-9).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void saveChartAsPNG(File file, JFreeChart chart,
           int width, int height, ChartRenderingInfo info, boolean encodeAlpha,
           int compression) throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("Null 'file' argument.");
        }
        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }

        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            writeChartAsPNG(out, chart, width, height, info, encodeAlpha,
                    compression);
        }
        finally {
            out.close();
        }

    }

    /**
     * Writes a chart to an output stream in JPEG format.  Please note that
     * JPEG is a poor format for chart images, use PNG if possible.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsJPEG(OutputStream out,
            JFreeChart chart, int width, int height) throws IOException {

        // defer argument checking...
        writeChartAsJPEG(out, chart, width, height, null);

    }

    /**
     * Writes a chart to an output stream in JPEG format.  Please note that
     * JPEG is a poor format for chart images, use PNG if possible.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param quality  the quality setting.
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsJPEG(OutputStream out, float quality,
            JFreeChart chart, int width, int height) throws IOException {

        // defer argument checking...
        ChartUtilities.writeChartAsJPEG(out, quality, chart, width, height,
                null);

    }

    /**
     * Writes a chart to an output stream in JPEG format. This method allows
     * you to pass in a {@link ChartRenderingInfo} object, to collect
     * information about the chart dimensions/entities.  You will need this
     * info if you want to create an HTML image map.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  the chart rendering info (<code>null</code> permitted).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsJPEG(OutputStream out, JFreeChart chart,
            int width, int height, ChartRenderingInfo info)
            throws IOException {

        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }
        BufferedImage image = chart.createBufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB, info);
        EncoderUtil.writeBufferedImage(image, ImageFormat.JPEG, out);

    }

    /**
     * Writes a chart to an output stream in JPEG format.  This method allows
     * you to pass in a {@link ChartRenderingInfo} object, to collect
     * information about the chart dimensions/entities.  You will need this
     * info if you want to create an HTML image map.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param quality  the output quality (0.0f to 1.0f).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  the chart rendering info (<code>null</code> permitted).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsJPEG(OutputStream out, float quality,
            JFreeChart chart, int width, int height, ChartRenderingInfo info)
            throws IOException {

        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }
        BufferedImage image = chart.createBufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB, info);
        EncoderUtil.writeBufferedImage(image, ImageFormat.JPEG, out, quality);

    }

    /**
     * Saves a chart to a file in JPEG format.
     *
     * @param file  the file (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void saveChartAsJPEG(File file, JFreeChart chart,
            int width, int height) throws IOException {

        // defer argument checking...
        saveChartAsJPEG(file, chart, width, height, null);

    }

    /**
     * Saves a chart to a file in JPEG format.
     *
     * @param file  the file (<code>null</code> not permitted).
     * @param quality  the JPEG quality setting.
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void saveChartAsJPEG(File file, float quality,
            JFreeChart chart, int width, int height) throws IOException {

        // defer argument checking...
        saveChartAsJPEG(file, quality, chart, width, height, null);

    }

    /**
     * Saves a chart to a file in JPEG format.  This method allows you to pass
     * in a {@link ChartRenderingInfo} object, to collect information about the
     * chart dimensions/entities.  You will need this info if you want to
     * create an HTML image map.
     *
     * @param file  the file name (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  the chart rendering info (<code>null</code> permitted).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void saveChartAsJPEG(File file, JFreeChart chart,
            int width, int height, ChartRenderingInfo info) throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("Null 'file' argument.");
        }
        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            writeChartAsJPEG(out, chart, width, height, info);
        }
        finally {
            out.close();
        }

    }

    /**
     * Saves a chart to a file in JPEG format.  This method allows you to pass
     * in a {@link ChartRenderingInfo} object, to collect information about the
     * chart dimensions/entities.  You will need this info if you want to
     * create an HTML image map.
     *
     * @param file  the file name (<code>null</code> not permitted).
     * @param quality  the quality setting.
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  the chart rendering info (<code>null</code> permitted).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void saveChartAsJPEG(File file, float quality,
            JFreeChart chart, int width, int height,
            ChartRenderingInfo info) throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("Null 'file' argument.");
        }
        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }

        OutputStream out = new BufferedOutputStream(new FileOutputStream(
                file));
        try {
            writeChartAsJPEG(out, quality, chart, width, height, info);
        }
        finally {
            out.close();
        }

    }

    /**
     * Writes a {@link BufferedImage} to an output stream in JPEG format.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param image  the image (<code>null</code> not permitted).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeBufferedImageAsJPEG(OutputStream out,
            BufferedImage image) throws IOException {

        // defer argument checking...
        writeBufferedImageAsJPEG(out, 0.75f, image);

    }

    /**
     * Writes a {@link BufferedImage} to an output stream in JPEG format.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param quality  the image quality (0.0f to 1.0f).
     * @param image  the image (<code>null</code> not permitted).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeBufferedImageAsJPEG(OutputStream out, float quality,
            BufferedImage image) throws IOException {

        EncoderUtil.writeBufferedImage(image, ImageFormat.JPEG, out, quality);

    }

    /**
     * Writes a {@link BufferedImage} to an output stream in PNG format.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param image  the image (<code>null</code> not permitted).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeBufferedImageAsPNG(OutputStream out,
            BufferedImage image) throws IOException {

        EncoderUtil.writeBufferedImage(image, ImageFormat.PNG, out);

    }

    /**
     * Writes a {@link BufferedImage} to an output stream in PNG format.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param image  the image (<code>null</code> not permitted).
     * @param encodeAlpha  encode alpha?
     * @param compression  the compression level (0-9).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeBufferedImageAsPNG(OutputStream out,
            BufferedImage image, boolean encodeAlpha, int compression)
            throws IOException {

        EncoderUtil.writeBufferedImage(image, ImageFormat.PNG, out,
                compression, encodeAlpha);
    }

    /**
     * Encodes a {@link BufferedImage} to PNG format.
     *
     * @param image  the image (<code>null</code> not permitted).
     *
     * @return A byte array in PNG format.
     *
     * @throws IOException if there is an I/O problem.
     */
    public static byte[] encodeAsPNG(BufferedImage image) throws IOException {
        return EncoderUtil.encode(image, ImageFormat.PNG);
    }

    /**
     * Encodes a {@link BufferedImage} to PNG format.
     *
     * @param image  the image (<code>null</code> not permitted).
     * @param encodeAlpha  encode alpha?
     * @param compression  the PNG compression level (0-9).
     *
     * @return The byte array in PNG format.
     *
     * @throws IOException if there is an I/O problem.
     */
    public static byte[] encodeAsPNG(BufferedImage image, boolean encodeAlpha,
                                     int compression)
            throws IOException {
        return EncoderUtil.encode(image, ImageFormat.PNG, compression,
                encodeAlpha);
    }

    /**
     * Writes an image map to an output stream.
     *
     * @param writer  the writer (<code>null</code> not permitted).
     * @param name  the map name (<code>null</code> not permitted).
     * @param info  the chart rendering info (<code>null</code> not permitted).
     * @param useOverLibForToolTips  whether to use OverLIB for tooltips
     *                               (http://www.bosrup.com/web/overlib/).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeImageMap(PrintWriter writer,
                                     String name,
                                     ChartRenderingInfo info,
                                     boolean useOverLibForToolTips)
        throws IOException {

        ToolTipTagFragmentGenerator toolTipTagFragmentGenerator = null;
        if (useOverLibForToolTips) {
            toolTipTagFragmentGenerator
                    = new OverLIBToolTipTagFragmentGenerator();
        }
        else {
            toolTipTagFragmentGenerator
                    = new StandardToolTipTagFragmentGenerator();
        }
        ImageMapUtilities.writeImageMap(writer, name, info,
                toolTipTagFragmentGenerator,
                new StandardURLTagFragmentGenerator());

    }

    /**
     * Writes an image map to the specified writer.
     *
     * @param writer  the writer (<code>null</code> not permitted).
     * @param name  the map name (<code>null</code> not permitted).
     * @param info  the chart rendering info (<code>null</code> not permitted).
     * @param toolTipTagFragmentGenerator  a generator for the HTML fragment
     *     that will contain the tooltip text (<code>null</code> not permitted
     *     if <code>info</code> contains tooltip information).
     * @param urlTagFragmentGenerator  a generator for the HTML fragment that
     *     will contain the URL reference (<code>null</code> not permitted if
     *     <code>info</code> contains URLs).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeImageMap(PrintWriter writer, String name,
            ChartRenderingInfo info,
            ToolTipTagFragmentGenerator toolTipTagFragmentGenerator,
            URLTagFragmentGenerator urlTagFragmentGenerator)
            throws IOException {

        writer.println(ImageMapUtilities.getImageMap(name, info,
                toolTipTagFragmentGenerator, urlTagFragmentGenerator));
    }

    /**
     * Creates an HTML image map.  This method maps to
     * {@link ImageMapUtilities#getImageMap(String, ChartRenderingInfo,
     * ToolTipTagFragmentGenerator, URLTagFragmentGenerator)}, using default
     * generators.
     *
     * @param name  the map name (<code>null</code> not permitted).
     * @param info  the chart rendering info (<code>null</code> not permitted).
     *
     * @return The map tag.
     */
    public static String getImageMap(String name, ChartRenderingInfo info) {
        return ImageMapUtilities.getImageMap(name, info,
                new StandardToolTipTagFragmentGenerator(),
                new StandardURLTagFragmentGenerator());
    }

    /**
     * Creates an HTML image map.  This method maps directly to
     * {@link ImageMapUtilities#getImageMap(String, ChartRenderingInfo,
     * ToolTipTagFragmentGenerator, URLTagFragmentGenerator)}.
     *
     * @param name  the map name (<code>null</code> not permitted).
     * @param info  the chart rendering info (<code>null</code> not permitted).
     * @param toolTipTagFragmentGenerator  a generator for the HTML fragment
     *     that will contain the tooltip text (<code>null</code> not permitted
     *     if <code>info</code> contains tooltip information).
     * @param urlTagFragmentGenerator  a generator for the HTML fragment that
     *     will contain the URL reference (<code>null</code> not permitted if
     *     <code>info</code> contains URLs).
     *
     * @return The map tag.
     */
    public static String getImageMap(String name, ChartRenderingInfo info,
            ToolTipTagFragmentGenerator toolTipTagFragmentGenerator,
            URLTagFragmentGenerator urlTagFragmentGenerator) {

        return ImageMapUtilities.getImageMap(name, info,
                toolTipTagFragmentGenerator, urlTagFragmentGenerator);

    }

}
