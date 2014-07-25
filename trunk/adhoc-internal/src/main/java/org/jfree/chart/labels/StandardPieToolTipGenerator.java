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
 * --------------------------------
 * StandardPieToolTipGenerator.java
 * --------------------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Richard Atkinson;
 *                   Andreas Schroeder;
 *
 * Changes
 * -------
 * 13-Dec-2001 : Version 1 (DG);
 * 16-Jan-2002 : Completed Javadocs (DG);
 * 29-Aug-2002 : Changed to format numbers using default locale (RA);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 30-Oct-2002 : Changed PieToolTipGenerator interface (DG);
 * 21-Mar-2003 : Implemented Serializable (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 19-Aug-2003 : Renamed StandardPieToolTipGenerator -->
 *               StandardPieItemLabelGenerator (DG);
 * 10-Mar-2004 : Modified to use MessageFormat class (DG);
 * 31-Mar-2004 : Added javadocs for the MessageFormat usage (AS);
 * 15-Apr-2004 : Split PieItemLabelGenerator interface into
 *               PieSectionLabelGenerator and PieToolTipGenerator (DG);
 * 25-Nov-2004 : Moved some code into abstract super class (DG);
 * 29-Jul-2005 : Removed implementation of PieSectionLabelGenerator
 *               interface (DG);
 * 10-Jul-2007 : Added constructors with locale argument (DG);
 *
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

import org.jfree.data.general.PieDataset;
import org.jfree.util.PublicCloneable;

/**
 * A standard item label generator for plots that use data from a
 * {@link PieDataset}.
 * <p>
 * For the label format, use {0} where the pie section key should be inserted,
 * {1} for the absolute section value and {2} for the percent amount of the pie
 * section, e.g. <code>"{0} = {1} ({2})"</code> will display as
 * <code>apple = 120 (5%)</code>.
 */
public class StandardPieToolTipGenerator extends AbstractPieItemLabelGenerator
        implements PieToolTipGenerator, Cloneable, PublicCloneable,
            Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 2995304200445733779L;

    /** The default tooltip format. */
    public static final String DEFAULT_TOOLTIP_FORMAT = "{0}: ({1}, {2})";

    /**
     * The default section label format.
     *
     * @deprecated As of 1.0.7, use {@link #DEFAULT_TOOLTIP_FORMAT} instead.
     */
    public static final String DEFAULT_SECTION_LABEL_FORMAT = "{0} = {1}";

    /**
     * Creates an item label generator using default number formatters.
     */
    public StandardPieToolTipGenerator() {
        this(DEFAULT_TOOLTIP_FORMAT);
    }

    /**
     * Creates a pie tool tip generator for the specified locale, using the
     * default format string.
     *
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @since 1.0.7
     */
    public StandardPieToolTipGenerator(Locale locale) {
        this(DEFAULT_TOOLTIP_FORMAT, locale);
    }

    /**
     * Creates a pie tool tip generator for the default locale.
     *
     * @param labelFormat  the label format (<code>null</code> not permitted).
     */
    public StandardPieToolTipGenerator(String labelFormat) {
        this(labelFormat, Locale.getDefault());
    }

    /**
     * Creates a pie tool tip generator for the specified locale.
     *
     * @param labelFormat  the label format (<code>null</code> not permitted).
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @since 1.0.7
     */
    public StandardPieToolTipGenerator(String labelFormat, Locale locale) {
        this(labelFormat, NumberFormat.getNumberInstance(locale),
                NumberFormat.getPercentInstance(locale));
    }

    /**
     * Creates an item label generator using the specified number formatters.
     *
     * @param labelFormat  the label format string (<code>null</code> not
     *                     permitted).
     * @param numberFormat  the format object for the values (<code>null</code>
     *                      not permitted).
     * @param percentFormat  the format object for the percentages
     *                       (<code>null</code> not permitted).
     */
    public StandardPieToolTipGenerator(String labelFormat,
            NumberFormat numberFormat, NumberFormat percentFormat) {
        super(labelFormat, numberFormat, percentFormat);
    }

    /**
     * Generates a tool tip text item for one section in a pie chart.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param key  the section key (<code>null</code> not permitted).
     *
     * @return The tool tip text (possibly <code>null</code>).
     */
    public String generateToolTip(PieDataset dataset, Comparable key) {
        return generateSectionLabel(dataset, key);
    }

    /**
     * Returns an independent copy of the generator.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  should not happen.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
