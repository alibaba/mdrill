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
 * ---------------------------
 * SerialDateChooserPanel.java
 * ---------------------------
 * (C) Copyright 2001-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: SerialDateChooserPanel.java,v 1.7 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes
 * -------
 * 08-Dec-2001 : Version 1 (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.date.SerialDate;

/**
 * A panel that allows the user to select a date.
 * <P>
 * This class is incomplete and untested.  You should not use it yet...
 *
 * @author David Gilbert
 */
public class SerialDateChooserPanel extends JPanel implements ActionListener {

    /** The default background color for the selected date. */
    public static final Color DEFAULT_DATE_BUTTON_COLOR = Color.red;

    /** The default background color for the current month. */
    public static final Color DEFAULT_MONTH_BUTTON_COLOR = Color.lightGray;

    /** The date selected in the panel. */
    private SerialDate date;

    /** The color for the selected date. */
    private Color dateButtonColor;

    /** The color for dates in the current month. */
    private Color monthButtonColor;

    /** The color for dates that are visible, but not in the current month. */
    private Color chosenOtherButtonColor = Color.darkGray;

    /** The first day-of-the-week. */
    private int firstDayOfWeek = Calendar.SUNDAY;

    /** The range used for selecting years. */
    private int yearSelectionRange = 20;

    /** The font used to display the date. */
    private Font dateFont = new Font("SansSerif", Font.PLAIN, 10);

    /** A combo for selecting the month. */
    private JComboBox monthSelector = null;

    /** A combo for selecting the year. */
    private JComboBox yearSelector = null;

    /** A button for selecting today's date. */
    private JButton todayButton = null;

    /** An array of buttons used to display the days-of-the-month. */
    private JButton[] buttons = null;

    /** A flag that indicates whether or not we are currently refreshing the buttons. */
    private boolean refreshing = false;

    /**
     * Constructs a new date chooser panel, using today's date as the initial selection.
     */
    public SerialDateChooserPanel() {

        this(SerialDate.createInstance(new Date()), false,
             DEFAULT_DATE_BUTTON_COLOR,
             DEFAULT_MONTH_BUTTON_COLOR);

    }

    /**
     * Constructs a new date chooser panel.
     *
     * @param date  the date.
     * @param controlPanel  a flag that indicates whether or not the 'today' button should
     *                      appear on the panel.
     */
    public SerialDateChooserPanel(final SerialDate date, final boolean controlPanel) {

        this(date, controlPanel,
             DEFAULT_DATE_BUTTON_COLOR,
             DEFAULT_MONTH_BUTTON_COLOR);

    }

    /**
     * Constructs a new date chooser panel.
     *
     * @param date  the date.
     * @param controlPanel  the control panel.
     * @param dateButtonColor  the date button color.
     * @param monthButtonColor  the month button color.
     */
    public SerialDateChooserPanel(final SerialDate date, final boolean controlPanel,
                                  final Color dateButtonColor, final Color monthButtonColor) {

        super(new BorderLayout());

        this.date = date;
        this.dateButtonColor = dateButtonColor;
        this.monthButtonColor = monthButtonColor;

        add(constructSelectionPanel(), BorderLayout.NORTH);
        add(getCalendarPanel(), BorderLayout.CENTER);
        if (controlPanel) {
            add(constructControlPanel(), BorderLayout.SOUTH);
        }

    }

    /**
     * Sets the date chosen in the panel.
     *
     * @param date  the new date.
     */
    public void setDate(final SerialDate date) {

        this.date = date;
        this.monthSelector.setSelectedIndex(date.getMonth() - 1);
        refreshYearSelector();
        refreshButtons();

    }

    /**
     * Returns the date selected in the panel.
     *
     * @return the selected date.
     */
    public SerialDate getDate() {
        return this.date;
    }

    /**
     * Handles action-events from the date panel.
     *
     * @param e information about the event that occurred.
     */
    public void actionPerformed(final ActionEvent e) {

        if (e.getActionCommand().equals("monthSelectionChanged")) {
            final JComboBox c = (JComboBox) e.getSource();
            this.date = SerialDate.createInstance(
                this.date.getDayOfMonth(), c.getSelectedIndex() + 1, this.date.getYYYY()
            );
            refreshButtons();
        }
        else if (e.getActionCommand().equals("yearSelectionChanged")) {
            if (!this.refreshing) {
                final JComboBox c = (JComboBox) e.getSource();
                final Integer y = (Integer) c.getSelectedItem();
                this.date = SerialDate.createInstance(
                    this.date.getDayOfMonth(), this.date.getMonth(), y.intValue()
                );
                refreshYearSelector();
                refreshButtons();
            }
        }
        else if (e.getActionCommand().equals("todayButtonClicked")) {
            setDate(SerialDate.createInstance(new Date()));
        }
        else if (e.getActionCommand().equals("dateButtonClicked")) {
            final JButton b = (JButton) e.getSource();
            final int i = Integer.parseInt(b.getName());
            final SerialDate first = getFirstVisibleDate();
            final SerialDate selected = SerialDate.addDays(i, first);
            setDate(selected);
        }

    }

    /**
     * Returns a panel of buttons, each button representing a day in the month.  This is a
     * sub-component of the DatePanel.
     *
     * @return the panel.
     */
    private JPanel getCalendarPanel() {

        final JPanel panel = new JPanel(new GridLayout(7, 7));
        panel.add(new JLabel("Sun", SwingConstants.CENTER));
        panel.add(new JLabel("Mon", SwingConstants.CENTER));
        panel.add(new JLabel("Tue", SwingConstants.CENTER));
        panel.add(new JLabel("Wed", SwingConstants.CENTER));
        panel.add(new JLabel("Thu", SwingConstants.CENTER));
        panel.add(new JLabel("Fri", SwingConstants.CENTER));
        panel.add(new JLabel("Sat", SwingConstants.CENTER));

        this.buttons = new JButton[42];
        for (int i = 0; i < 42; i++) {
            final JButton button = new JButton("");
            button.setMargin(new Insets(1, 1, 1, 1));
            button.setName(Integer.toString(i));
            button.setFont(this.dateFont);
            button.setFocusPainted(false);
            button.setActionCommand("dateButtonClicked");
            button.addActionListener(this);
            this.buttons[i] = button;
            panel.add(button);
        }
        return panel;

    }

    /**
     * Returns the button color according to the specified date.
     *
     * @param targetDate  the target date.
     *
     * @return the button color.
     */
    protected Color getButtonColor(final SerialDate targetDate) {

        if (this.date.equals(this.date)) {
            return this.dateButtonColor;
        }
        else if (targetDate.getMonth() == this.date.getMonth()) {
            return this.monthButtonColor;
        }
        else {
            return this.chosenOtherButtonColor;
        }

    }

    /**
     * Returns the first date that is visible in the grid.  This should always be in the month
     * preceding the month of the selected date.
     *
     * @return the first visible date.
     */
    protected SerialDate getFirstVisibleDate() {

        SerialDate result = SerialDate.createInstance(1, this.date.getMonth(), this.date.getYYYY());
        result = SerialDate.addDays(-1, result);
        while (result.getDayOfWeek() != getFirstDayOfWeek()) {
            result = SerialDate.addDays(-1, result);
        }
        return result;

    }

    /**
     * Returns the first day of the week (controls the labels in the date panel).
     *
     * @return the first day of the week.
     */
    private int getFirstDayOfWeek() {
        return this.firstDayOfWeek;
    }

    /**
     * Update the button labels and colors to reflect date selection.
     */
    protected void refreshButtons() {

        SerialDate current = getFirstVisibleDate();
        for (int i = 0; i < 42; i++) {
            final JButton button = this.buttons[i];
            button.setText(String.valueOf(current.getDayOfWeek()));
            button.setBackground(getButtonColor(current));
            current = SerialDate.addDays(1, current);
        }

    }

    /**
     * Changes the contents of the year selection JComboBox to reflect the chosen date and the year
     * range.
     */
    private void refreshYearSelector() {
        if (!this.refreshing) {
            this.refreshing = true;
            this.yearSelector.removeAllItems();
            final Vector v = getYears(this.date.getYYYY());
            for (Enumeration e = v.elements(); e.hasMoreElements();) {
                this.yearSelector.addItem(e.nextElement());
            }
            this.yearSelector.setSelectedItem(new Integer(this.date.getYYYY()));
            this.refreshing = false;
        }
    }

    /**
     * Returns a vector of years preceding and following the specified year.  The number of years
     * preceding and following is determined by the yearSelectionRange attribute.
     *
     * @param chosenYear  the current year.
     *
     * @return a vector of years.
     */
    private Vector getYears(final int chosenYear) {
        final Vector v = new Vector();
        for (int i = chosenYear - this.yearSelectionRange; 
            i <= chosenYear + this.yearSelectionRange; i++) {
            v.addElement(new Integer(i));
        }
        return v;
    }

    /**
     * Constructs a panel containing two JComboBoxes (for the month and year) and a button
     * (to reset the date to TODAY).
     *
     * @return the panel.
     */
    private JPanel constructSelectionPanel() {
        final JPanel p = new JPanel();
        this.monthSelector = new JComboBox(SerialDate.getMonths());
        this.monthSelector.addActionListener(this);
        this.monthSelector.setActionCommand("monthSelectionChanged");
        p.add(this.monthSelector);

        this.yearSelector = new JComboBox(getYears(0));
        this.yearSelector.addActionListener(this);
        this.yearSelector.setActionCommand("yearSelectionChanged");
        p.add(this.yearSelector);

        return p;
    }

    /**
     * Returns a panel that appears at the bottom of the calendar panel - contains a button for
     * selecting today's date.
     *
     * @return the panel.
     */
    private JPanel constructControlPanel() {

        final JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        this.todayButton = new JButton("Today");
        this.todayButton.addActionListener(this);
        this.todayButton.setActionCommand("todayButtonClicked");
        p.add(this.todayButton);
        return p;

    }

}
