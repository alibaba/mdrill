/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
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
 * ---------------
 * AboutFrame.java
 * ---------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: AboutFrame.java,v 1.8 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes (from 26-Nov-2001)
 * --------------------------
 * 26-Nov-2001 : Version 1, based on code from JFreeChart demo application (DG);
 * 27-Nov-2001 : Added getPreferredSize() method (DG);
 * 08-Feb-2002 : List of developers is now optional (DG);
 * 15-Mar-2002 : Modified to use a ResourceBundle for elements that require
 *               localisation (DG);
 * 25-Mar-2002 : Added new constructor (DG);
 * 26-Jun-2002 : Removed redundant code (DG);
 * 08-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.ui.about;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.jfree.util.ResourceBundleWrapper;

/**
 * A frame that displays information about the demonstration application.
 *
 * @author David Gilbert
 */
public class AboutFrame extends JFrame {

    /** The preferred size for the frame. */
    public static final Dimension PREFERRED_SIZE = new Dimension(560, 360);

    /** The default border for the panels in the tabbed pane. */
    public static final Border STANDARD_BORDER
            = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    /** Localised resources. */
    private ResourceBundle resources;

    /** The application name. */
    private String application;

    /** The application version. */
    private String version;

    /** The copyright string. */
    private String copyright;

    /** Other info about the application. */
    private String info;

    /** The project logo. */
    private Image logo;

    /** A list of contributors. */
    private List contributors;

    /** The licence. */
    private String licence;

    /**
     * Constructs an about frame.
     *
     * @param title  the frame title.
     * @param project  information about the project.
     */
    public AboutFrame(final String title, final ProjectInfo project) {

        this(title,
             project.getName(),
             "Version " + project.getVersion(),
             project.getInfo(),
             project.getLogo(),
             project.getCopyright(),
             project.getLicenceText(),
             project.getContributors(),
             project);

    }

    /**
     * Constructs an 'About' frame.
     *
     * @param title  the frame title.
     * @param application  the application name.
     * @param version  the version.
     * @param info  other info.
     * @param logo  an optional logo.
     * @param copyright  the copyright notice.
     * @param licence  the licence.
     * @param contributors  a list of developers/contributors.
     * @param project  info about the project.
     */
    public AboutFrame(final String title,
                      final String application, final String version,
                      final String info,
                      final Image logo,
                      final String copyright, final String licence,
                      final List contributors,
                      final ProjectInfo project) {

        super(title);

        this.application = application;
        this.version = version;
        this.copyright = copyright;
        this.info = info;
        this.logo = logo;
        this.contributors = contributors;
        this.licence = licence;

        final String baseName = "org.jfree.ui.about.resources.AboutResources";
        this.resources = ResourceBundleWrapper.getBundle(baseName);

        final JPanel content = new JPanel(new BorderLayout());
        content.setBorder(STANDARD_BORDER);

        final JTabbedPane tabs = createTabs(project);
        content.add(tabs);
        setContentPane(content);

        pack();

    }

    /**
     * Returns the preferred size for the about frame.
     *
     * @return the preferred size.
     */
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    /**
     * Creates a tabbed pane containing an about panel and a system properties
     * panel.
     *
     * @return a tabbed pane.
     * @param project
     */
    private JTabbedPane createTabs(final ProjectInfo project) {

        final JTabbedPane tabs = new JTabbedPane();

        final JPanel aboutPanel = createAboutPanel(project);
        aboutPanel.setBorder(AboutFrame.STANDARD_BORDER);
        final String aboutTab = this.resources.getString(
                "about-frame.tab.about");
        tabs.add(aboutTab, aboutPanel);

        final JPanel systemPanel = new SystemPropertiesPanel();
        systemPanel.setBorder(AboutFrame.STANDARD_BORDER);
        final String systemTab = this.resources.getString(
                "about-frame.tab.system");
        tabs.add(systemTab, systemPanel);

        return tabs;

    }

    /**
     * Creates a panel showing information about the application, including the
     * name, version, copyright notice, URL for further information, and a list
     * of contributors.

     * @param project
     *
     * @return a panel.
     */
    private JPanel createAboutPanel(final ProjectInfo project) {

        final JPanel about = new JPanel(new BorderLayout());

        final JPanel details = new AboutPanel(this.application, this.version,
                this.copyright, this.info, this.logo);

        boolean includetabs = false;
        final JTabbedPane tabs = new JTabbedPane();

        if (this.contributors != null) {
            final JPanel contributorsPanel = new ContributorsPanel(
                    this.contributors);
            contributorsPanel.setBorder(AboutFrame.STANDARD_BORDER);
            final String contributorsTab = this.resources.getString(
                    "about-frame.tab.contributors");
            tabs.add(contributorsTab, contributorsPanel);
            includetabs = true;
        }

        if (this.licence != null) {
            final JPanel licencePanel = createLicencePanel();
            licencePanel.setBorder(STANDARD_BORDER);
            final String licenceTab = this.resources.getString(
                    "about-frame.tab.licence");
            tabs.add(licenceTab, licencePanel);
            includetabs = true;
        }

        if (project != null) {
            final JPanel librariesPanel = new LibraryPanel(project);
            librariesPanel.setBorder(AboutFrame.STANDARD_BORDER);
            final String librariesTab = this.resources.getString(
                    "about-frame.tab.libraries");
            tabs.add(librariesTab, librariesPanel);
            includetabs = true;
        }

        about.add(details, BorderLayout.NORTH);
        if (includetabs) {
            about.add(tabs);
        }

        return about;

    }

    /**
     * Creates a panel showing the licence.
     *
     * @return a panel.
     */
    private JPanel createLicencePanel() {

        final JPanel licencePanel = new JPanel(new BorderLayout());
        final JTextArea area = new JTextArea(this.licence);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);
        area.setEditable(false);
        licencePanel.add(new JScrollPane(area));
        return licencePanel;

    }


}
