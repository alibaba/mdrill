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
 * CenterLayout.java
 * -----------------
 * (C) Copyright 2000-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: CenterLayout.java,v 1.6 2005/11/16 15:58:40 taqua Exp $
 *
 * Changes (from 5-Nov-2001)
 * -------------------------
 * 05-Nov-2001 : Changed package to com.jrefinery.layout.* (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;

/**
 * A layout manager that displays a single component in the center of its 
 * container.
 *
 * @author David Gilbert
 */
public class CenterLayout implements LayoutManager, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 469319532333015042L;
    
    /**
     * Creates a new layout manager.
     */
    public CenterLayout() {
    }

    /**
     * Returns the preferred size.
     *
     * @param parent  the parent.
     *
     * @return the preferred size.
     */
    public Dimension preferredLayoutSize(final Container parent) {

        synchronized (parent.getTreeLock()) {
            final Insets insets = parent.getInsets();
            if (parent.getComponentCount() > 0) {
                final Component component = parent.getComponent(0);
                final Dimension d = component.getPreferredSize();
                return new Dimension(
                    (int) d.getWidth() + insets.left + insets.right,
                    (int) d.getHeight() + insets.top + insets.bottom
                );
            }
            else {
                return new Dimension(
                    insets.left + insets.right, insets.top + insets.bottom
                );
            }
        }

    }

    /**
     * Returns the minimum size.
     *
     * @param parent  the parent.
     *
     * @return the minimum size.
     */
    public Dimension minimumLayoutSize(final Container parent) {

        synchronized (parent.getTreeLock()) {
            final Insets insets = parent.getInsets();
            if (parent.getComponentCount() > 0) {
                final Component component = parent.getComponent(0);
                final Dimension d = component.getMinimumSize();
                return new Dimension(d.width + insets.left + insets.right,
                                 d.height + insets.top + insets.bottom);
            }
            else {
              return new Dimension(insets.left + insets.right,
                                   insets.top + insets.bottom);
            }
        }

    }

    /**
     * Lays out the components.
     *
     * @param parent  the parent.
     */
    public void layoutContainer(final Container parent) {

        synchronized (parent.getTreeLock()) {
            if (parent.getComponentCount() > 0) {
                final Insets insets = parent.getInsets();
                final Dimension parentSize = parent.getSize();
                final Component component = parent.getComponent(0);
                final Dimension componentSize = component.getPreferredSize();
                final int xx = insets.left + (
                    Math.max((parentSize.width - insets.left - insets.right
                                      - componentSize.width) / 2, 0)
                );
                final int yy = insets.top + (
                    Math.max((parentSize.height - insets.top - insets.bottom
                                      - componentSize.height) / 2, 0));
                component.setBounds(xx, yy, componentSize.width, 
                        componentSize.height);
            }
        }

    }

    /**
     * Not used.
     *
     * @param comp  the component.
     */
    public void addLayoutComponent(final Component comp) {
        // not used.
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

}
