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
 * ------------------
 * TextBlockDemo.java
 * ------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: TextBlockDemo.java,v 1.4 2007/11/02 17:50:35 taqua Exp $
 *
 * Changes
 * -------
 * 07-Jan-2004 : Version 1 (DG);
 *
 */

package org.jfree.demo;

import java.awt.Font;
import javax.swing.JPanel;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo of the TextBlock class.
 *
 * @author David Gilbert
 */
public class TextBlockDemo extends ApplicationFrame {

    /**
     * Creates a new demo instance.
     *
     * @param title  the frame title.
     */
    public TextBlockDemo(final String title) {
        super(title);
        setContentPane(createContentPane());
    }


    /**
     * Creates the content pane for the demo frame.
     *
     * @return The content pane.
     */
    private JPanel createContentPane() {
        final JPanel content = new TextBlockPanel("This is some really long text that we will use "
            + "for testing purposes.  You'll need to resize the window to see how the TextBlock "
            + "is dynamically updated.  Also note what happens when there is a really long "
            + "word like ABCDEFGHIJKLMNOPQRSTUVWXYZ (OK, that's not really a word).", 
            new Font("Serif", Font.PLAIN, 14));
        return content;
    }

    /**
     * The starting point for the demo.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {

        final TextBlockDemo demo = new TextBlockDemo("TextBlock Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}
