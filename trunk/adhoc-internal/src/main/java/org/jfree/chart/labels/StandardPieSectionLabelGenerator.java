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
 * -------------------------------------
 * StandardPieSectionLabelGenerator.java
 * -------------------------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 09-Nov-2004 : Version 1, derived from StandardPieItemLabelGenerator (DG);
 * 29-Jul-2005 : Removed unused generateToolTip() method (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 03-May-2006 : Modified DEFAULT_SECTION_LABEL_FORMAT (DG);
 * 10-Jan-2007 : Include attributedLabels in equals() test (DG);
 * 10-Jul-2007 : Added constructors with locale parameter (DG);
 * 23-Apr-2008 : Implemented PublicCloneable (DG);
 *
 */

package org.jfree.chart.labels;

import java.awt.Font;
import java.awt.Paint;
import java.awt.font.TextAttribute;
import java.io.Serializable;
import java.text.AttributedString;
import java.text.NumberFormat;
import java.util.Locale;

import org.jfree.data.general.PieDataset;
import org.jfree.util.ObjectList;
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
public class StandardPieSectionLabelGenerator
        extends AbstractPieItemLabelGenerator
        implements PieSectionLabelGenerator, Cloneable, PublicCloneable,
                   Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 3064190563760203668L;

    /** The default section label format. */
    public static final String DEFAULT_SECTION_LABEL_FORMAT = "{0}";

    /**
     * An optional list of attributed labels (instances of AttributedString).
     */
    private ObjectList attributedLabels;

    /**
     * Creates a new section label generator using
     * {@link #DEFAULT_SECTION_LABEL_FORMAT} as the label format string, and
     * platform default number and percentage formatters.
     */
    public StandardPieSectionLabelGenerator() {
        this(DEFAULT_SECTION_LABEL_FORMAT, NumberFormat.getNumberInstance(),
                NumberFormat.getPercentInstance());
    }

    /**
     * Creates a new instance for the specified locale.
     *
     * @param locale  the local (<code>null</code> not permitted).
     *
     * @since 1.0.7
     */
    public StandardPieSectionLabelGenerator(Locale locale) {
        this(DEFAULT_SECTION_LABEL_FORMAT, locale);
    }

    /**
     * Creates a new section label generator using the specified label format
     * string, and platform default number and percentage formatters.
     *
     * @param labelFormat  the label format (<code>null</code> not permitted).
     */
    public StandardPieSectionLabelGenerator(String labelFormat) {
        this(labelFormat, NumberFormat.getNumberInstance(),
                NumberFormat.getPercentInstance());
    }

    /**
     * Creates a new instance for the specified locale.
     *
     * @param labelFormat  the label format (<code>null</code> not permitted).
     * @param locale  the local (<code>null</code> not permitted).
     *
     * @since 1.0.7
     */
    public StandardPieSectionLabelGenerator(String labelFormat, Locale locale) {
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
    public StandardPieSectionLabelGenerator(String labelFormat,
            NumberFormat numberFormat, NumberFormat percentFormat) {
        super(labelFormat, numberFormat, percentFormat);
        this.attributedLabels = new ObjectList();
    }

    /**
     * Returns the attributed label for a section, or <code>null</code> if none
     * is defined.
     *
     * @param section  the section index.
     *
     * @return The attributed label.
     */
    public AttributedString getAttributedLabel(int section) {
        return (AttributedString) this.attributedLabels.get(section);
    }

    /**
     * Sets the attributed label for a section.
     *
     * @param section  the section index.
     * @param label  the label (<code>null</code> permitted).
     */
    public void setAttributedLabel(int section, AttributedString label) {
        this.attributedLabels.set(section, label);
    }

    /**
     * Generates a label for a pie section.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param key  the section key (<code>null</code> not permitted).
     *
     * @return The label (possibly <code>null</code>).
     */
    public String generateSectionLabel(PieDataset dataset, Comparable key) {
        return super.generateSectionLabel(dataset, key);
    }

    /**
     * Generates an attributed label for the specified series, or
     * <code>null</code> if no attributed label is available (in which case,
     * the string returned by
     * {@link #generateSectionLabel(PieDataset, Comparable)} will
     * provide the fallback).  Only certain attributes are recognised by the
     * code that ultimately displays the labels:
     * <ul>
     * <li>{@link TextAttribute#FONT}: will set the font;</li>
     * <li>{@link TextAttribute#POSTURE}: a value of
     *     {@link TextAttribute#POSTURE_OBLIQUE} will add {@link Font#ITALIC} to
     *     the current font;</li>
     * <li>{@link TextAttribute#WEIGHT}: a value of
     *     {@link TextAttribute#WEIGHT_BOLD} will add {@link Font#BOLD} to the
     *     current font;</li>
     * <li>{@link TextAttribute#FOREGROUND}: this will set the {@link Paint}
     *     for the current</li>
     * <li>{@link TextAttribute#SUPERSCRIPT}: the values
     *     {@link TextAttribute#SUPERSCRIPT_SUB} and
     *     {@link TextAttribute#SUPERSCRIPT_SUPER} are recognised.</li>
     * </ul>
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param key  the key.
     *
     * @return An attributed label (possibly <code>null</code>).
     */
    public AttributedString generateAttributedSectionLabel(PieDataset dataset,
                                                           Comparable key) {
        return getAttributedLabel(dataset.getIndex(key));
    }

    /**
     * Tests the generator for equality with an arbitrary object.
     *
     * @param obj  the object to test against (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StandardPieSectionLabelGenerator)) {
            return false;
        }
        StandardPieSectionLabelGenerator that
                = (StandardPieSectionLabelGenerator) obj;
        if (!this.attributedLabels.equals(that.attributedLabels)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true;
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
