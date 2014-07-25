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
 * -----------------
 * RadialLayout.java
 * -----------------
 * (C) Copyright 2003, 2004, by Bryan Scott (for Australian Antarctic Division).
 *
 * Original Author:  Bryan Scott (for Australian Antarctic Division);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 *
 * Changes:
 * --------
 * 30-Jun-2003 : Version 1 (BS);
 * 24-Jul-2003 : Completed missing Javadocs (DG);
 *
 */

package org.jfree.layout;

import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.io.Serializable;

/**
 * RadialLayout is a component layout manager.  Compents are laid out in a
 * circle. If only one component is contained in the layout it is positioned
 * centrally, otherwise components are evenly spaced around the centre with
 * the first component placed to the North.
 *<P>
 * This code was developed to display CTD rosette firing control
 *
 * WARNING: Not thoughly tested, use at own risk.
 * 
 * @author Bryan Scott (for Australian Antarctic Division)
 */

public class RadialLayout implements LayoutManager, Serializable {
    
    /** For serialization. */
    private static final long serialVersionUID = -7582156799248315534L;
    
    /** The minimum width. */
    private int minWidth = 0;
    
    /** The minimum height. */
    private int minHeight = 0;
    
    /** The maximum component width. */
    private int maxCompWidth = 0;
    
    /** The maximum component height. */
    private int maxCompHeight = 0;
    
    /** The preferred width. */
    private int preferredWidth = 0;
    
    /** The preferred height. */
    private int preferredHeight = 0;
    
    /** Size unknown flag. */
    private boolean sizeUnknown = true;

    /** 
     * Constructs this layout manager with default properties. 
     */
    public RadialLayout() {
        super();
    }

    /**
     * Not used.
     *
     * @param comp  the component.
     */
    public void addLayoutComponent(final Component comp) {
        // not used
    }

    /**
     * Not used.
     *
     * @param comp  the component.
     */
    public void removeLayoutComponent(final Component comp) {
        // not used
    }

    /**
     * Not used.
     *
     * @param name  the component name.
     * @param comp  the component.
     */
    public void addLayoutComponent(final String name, final Component comp) {
        // not used
    }

    /**
     * Not used.
     *
     * @param name  the component name.
     * @param comp  the component.
     */
    public void removeLayoutComponent(final String name, final Component comp) {
        // not used
    }

    /**
     * Sets the sizes attribute of the RadialLayout object.
     *
     * @param  parent  the parent.
     * 
     * @see LayoutManager
     */
    private void setSizes(final Container parent) {
        final int nComps = parent.getComponentCount();
        //Reset preferred/minimum width and height.
        this.preferredWidth = 0;
        this.preferredHeight = 0;
        this.minWidth = 0;
        this.minHeight = 0;
        for (int i = 0; i < nComps; i++) {
            final Component c = parent.getComponent(i);
            if (c.isVisible()) {
                final Dimension d = c.getPreferredSize();
                if (this.maxCompWidth < d.width) {
                    this.maxCompWidth = d.width;
                }
                if (this.maxCompHeight < d.height) {
                    this.maxCompHeight = d.height;
                }
                this.preferredWidth += d.width;
                this.preferredHeight += d.height;
            }
        }
        this.preferredWidth  = this.preferredWidth / 2;
        this.preferredHeight = this.preferredHeight / 2;
        this.minWidth = this.preferredWidth;
        this.minHeight = this.preferredHeight;
    }

    /**
     * Returns the preferred size.
     *
     * @param parent  the parent.
     *
     * @return The preferred size.
     * @see LayoutManager
     */
    public Dimension preferredLayoutSize(final Container parent) {
        final Dimension dim = new Dimension(0, 0);
        setSizes(parent);

        //Always add the container's insets!
        final Insets insets = parent.getInsets();
        dim.width = this.preferredWidth + insets.left + insets.right;
        dim.height = this.preferredHeight + insets.top + insets.bottom;

        this.sizeUnknown = false;
        return dim;
    }

    /**
     * Returns the minimum size.
     *
     * @param parent  the parent.
     *
     * @return The minimum size.
     * @see LayoutManager
     */
    public Dimension minimumLayoutSize(final Container parent) {
        final Dimension dim = new Dimension(0, 0);

        //Always add the container's insets!
        final Insets insets = parent.getInsets();
        dim.width = this.minWidth + insets.left + insets.right;
        dim.height = this.minHeight + insets.top + insets.bottom;

        this.sizeUnknown = false;
        return dim;
    }

   /**
    * This is called when the panel is first displayed, and every time its size
    * changes.
    * Note: You CAN'T assume preferredLayoutSize or minimumLayoutSize will be
    * called -- in the case of applets, at least, they probably won't be.
    *
    * @param  parent  the parent.
    * @see LayoutManager
    */
    public void layoutContainer(final Container parent) {
        final Insets insets = parent.getInsets();
        final int maxWidth = parent.getSize().width 
            - (insets.left + insets.right);
        final int maxHeight = parent.getSize().height 
            - (insets.top + insets.bottom);
        final int nComps = parent.getComponentCount();
        int x = 0;
        int y = 0;

        // Go through the components' sizes, if neither preferredLayoutSize nor
        // minimumLayoutSize has been called.
        if (this.sizeUnknown) {
            setSizes(parent);
        }

        if (nComps < 2) {
            final Component c = parent.getComponent(0);
            if (c.isVisible()) {
                final Dimension d = c.getPreferredSize();
                c.setBounds(x, y, d.width, d.height);
            }
        } 
        else {
            double radialCurrent = Math.toRadians(90);
            final double radialIncrement = 2 * Math.PI / nComps;
            final int midX = maxWidth / 2;
            final int midY = maxHeight / 2;
            final int a = midX - this.maxCompWidth;
            final int b = midY - this.maxCompHeight;
            for (int i = 0; i < nComps; i++) {
                final Component c = parent.getComponent(i);
                if (c.isVisible()) {
                    final Dimension d = c.getPreferredSize();
                    x = (int) (midX
                               - (a * Math.cos(radialCurrent))
                               - (d.getWidth() / 2)
                               + insets.left);
                    y = (int) (midY
                               - (b * Math.sin(radialCurrent))
                               - (d.getHeight() / 2)
                               + insets.top);

                    // Set the component's size and position.
                    c.setBounds(x, y, d.width, d.height);
                }
                radialCurrent += radialIncrement;
            }
        }
    }

    /**
     * Returns the class name.
     * 
     * @return The class name.
     */
    public String toString() {
        return getClass().getName();
    }

    /**
     * Run a demonstration.
     *
     * @param args  ignored.
     * 
     * @throws Exception when an error occurs.
     */
    public static void main(final String[] args) throws Exception {
        final Frame frame = new Frame();
        final Panel panel = new Panel();
        panel.setLayout(new RadialLayout());

        panel.add(new Checkbox("One"));
        panel.add(new Checkbox("Two"));
        panel.add(new Checkbox("Three"));
        panel.add(new Checkbox("Four"));
        panel.add(new Checkbox("Five"));
        panel.add(new Checkbox("One"));
        panel.add(new Checkbox("Two"));
        panel.add(new Checkbox("Three"));
        panel.add(new Checkbox("Four"));
        panel.add(new Checkbox("Five"));

        frame.add(panel);
        frame.setSize(300, 500);
        frame.setVisible(true);
    }

}
