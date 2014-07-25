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
 * -------------------------
 * DateChooserPanelDemo.java
 * -------------------------
 * (C) Copyright 2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: DateChooserPanelDemo.java,v 1.1 2005/11/02 14:11:22 mungady Exp $
 *
 * Changes
 * -------
 * 02-Nov-2005 : Version 1;
 *
 */


package org.jfree.demo;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.DateChooserPanel;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demo showing the {@link DateChooserPanel}.
 */
public class DateChooserPanelDemo extends ApplicationFrame {

    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
    public DateChooserPanelDemo(String title) {
        super(title);
        setContentPane(new DateChooserPanel());
    }
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) {
        DateChooserPanelDemo demo = new DateChooserPanelDemo(
            "DateChooserPanel Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

}
