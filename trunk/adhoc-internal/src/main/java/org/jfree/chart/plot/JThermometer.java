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
 * JThermometer.java
 * -----------------
 * A plot that displays a single value in a thermometer type display.
 *
 * (C) Copyright 2000-2008, Australian Antarctic Division and Contributors.
 *
 * Original Author:  Bryan Scott.
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Irv Thomae;
 *
 * Changes (from 17-Sep-2002)
 * --------------------------
 * 17-Sep-2002 : Reviewed with Checkstyle utility (DG);
 * 18-Sep-2003 : Integrated new methods contributed by Irv Thomae (DG);
 * 08-Jan-2004 : Renamed AbstractTitle --> Title and moved to new package (DG);
 * 31-May-2005 : Fixed typo in method name (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.Serializable;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.RectangleInsets;

/**
 * An initial quick and dirty.  The concept behind this class would be to
 * generate a gui bean that could be used within JBuilder, Netbeans etc...
 */
public class JThermometer extends JPanel implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 1079905665515589820L;

    /** The dataset. */
    private DefaultValueDataset data;

    /** The chart. */
    private JFreeChart chart;

    /** The chart panel. */
    private ChartPanel panel;

    /** The thermometer plot. */
    private ThermometerPlot plot = new ThermometerPlot();

    /**
     * Default constructor.
     */
    public JThermometer() {
        super(new CardLayout());
        this.plot.setInsets(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        this.data = new DefaultValueDataset();
        this.plot.setDataset(this.data);
        this.chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT,
                this.plot, false);
        this.panel = new ChartPanel(this.chart);
        add(this.panel, "Panel");
        setBackground(getBackground());
    }

    /**
     * Adds a subtitle to the chart.
     *
     * @param subtitle  the subtitle.
     */
    public void addSubtitle(Title subtitle) {
        this.chart.addSubtitle(subtitle);
    }

    /**
     * Adds a subtitle to the chart.
     *
     * @param subtitle  the subtitle.
     */
    public void addSubtitle(String subtitle) {
        this.chart.addSubtitle(new TextTitle(subtitle));
    }

    /**
     * Adds a subtitle to the chart.
     *
     * @param subtitle  the subtitle.
     * @param font  the subtitle font.
     */
    public void addSubtitle(String subtitle, Font font) {
        this.chart.addSubtitle(new TextTitle(subtitle, font));
    }

    /**
     * Sets the value format for the thermometer.
     *
     * @param df  the formatter.
     */
    public void setValueFormat(DecimalFormat df) {
        this.plot.setValueFormat(df);
    }

    /**
     * Sets the lower and upper bounds for the thermometer.
     *
     * @param lower  the lower bound.
     * @param upper  the upper bound.
     */
    public void setRange(double lower, double upper) {
        this.plot.setRange(lower, upper);
    }

    /**
     * Sets the range.
     *
     * @param range  the range type.
     * @param displayLow  the low value.
     * @param displayHigh  the high value.
     */
    public void setSubrangeInfo(int range, double displayLow,
                                double displayHigh) {
        this.plot.setSubrangeInfo(range, displayLow, displayHigh);
    }

    /**
     * Sets the range.
     *
     * @param range  the range type.
     * @param rangeLow  the low value for the range.
     * @param rangeHigh  the high value for the range.
     * @param displayLow  the low value for display.
     * @param displayHigh  the high value for display.
     */
    public void setSubrangeInfo(int range,
                             double rangeLow, double rangeHigh,
                             double displayLow, double displayHigh) {

        this.plot.setSubrangeInfo(range, rangeLow, rangeHigh, displayLow,
                displayHigh);

    }

    /**
     * Sets the location at which the temperature value is displayed.
     *
     * @param loc  the location.
     */
    public void setValueLocation(int loc) {
        this.plot.setValueLocation(loc);
        this.panel.repaint();
    }

    /**
     * Sets the value paint.
     *
     * @param paint  the paint.
     */
    public void setValuePaint(Paint paint) {
        this.plot.setValuePaint(paint);
    }

    /**
     * Returns the value of the thermometer.
     *
     * @return The value.
     */
    public Number getValue() {
        if (this.data != null) {
            return this.data.getValue();
        }
        else {
            return null;
        }
    }

    /**
     * Sets the value of the thermometer.
     *
     * @param value  the value.
     */
    public void setValue(double value) {
        setValue(new Double(value));
    }

    /**
     * Sets the value of the thermometer.
     *
     * @param value  the value.
     */
    public void setValue(Number value) {
        if (this.data != null) {
            this.data.setValue(value);
        }
    }

    /**
     * Sets the unit type.
     *
     * @param i  the unit type.
     */
    public void setUnits(int i) {
        if (this.plot != null) {
            this.plot.setUnits(i);
        }
    }

    /**
     * Sets the outline paint.
     *
     * @param p  the paint.
     */
    public void setOutlinePaint(Paint p) {
        if (this.plot != null) {
            this.plot.setOutlinePaint(p);
        }
    }

    /**
     * Sets the foreground color.
     *
     * @param fg  the foreground color.
     */
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (this.plot != null) {
            this.plot.setThermometerPaint(fg);
        }
    }

    /**
     * Sets the background color.
     *
     * @param bg  the background color.
     */
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (this.plot != null) {
            this.plot.setBackgroundPaint(bg);
        }
        if (this.chart != null) {
            this.chart.setBackgroundPaint(bg);
        }
        if (this.panel != null) {
            this.panel.setBackground(bg);
        }
    }

    /**
     * Sets the value font.
     *
     * @param f  the font.
     */
    public void setValueFont(Font f) {
        if (this.plot != null) {
            this.plot.setValueFont(f);
        }
    }

    /**
     * Returns the tick label font.
     *
     * @return The tick label font.
     */
    public Font getTickLabelFont() {
        ValueAxis axis = this.plot.getRangeAxis();
        return axis.getTickLabelFont();
    }

    /**
     * Sets the tick label font.
     *
     * @param font  the font.
     */
    public void setTickLabelFont(Font font) {
        ValueAxis axis = this.plot.getRangeAxis();
        axis.setTickLabelFont(font);
    }

    /**
     * Increases or decreases the tick font size.
     *
     * @param delta  the change in size.
     */
    public void changeTickFontSize(int delta) {
        Font f = getTickLabelFont();
        String fName = f.getFontName();
        Font newFont = new Font(fName, f.getStyle(), (f.getSize() + delta));
        setTickLabelFont(newFont);
    }

    /**
     * Sets the tick font style.
     *
     * @param style  the style.
     */
    public void setTickFontStyle(int style) {
        Font f = getTickLabelFont();
        String fName = f.getFontName();
        Font newFont = new Font(fName, style, f.getSize());
        setTickLabelFont(newFont);
    }

    /**
     * Sets the flag that controls whether or not the display range follows the
     * data value.
     *
     * @param flag  the new value of the flag.
     */
    public void setFollowDataInSubranges(boolean flag) {
        this.plot.setFollowDataInSubranges(flag);
    }

    /**
     * Sets the flag that controls whether or not value lines are displayed.
     *
     * @param b  the new flag value.
     */
    public void setShowValueLines(boolean b) {
        this.plot.setShowValueLines(b);
    }

    /**
     * Sets the location for the axis.
     *
     * @param location  the location.
     */
    public void setShowAxisLocation(int location) {
        this.plot.setAxisLocation(location);
    }

    /**
     * Returns the location for the axis.
     *
     * @return The location.
     */
    public int getShowAxisLocation() {
      return this.plot.getAxisLocation();
    }

}
