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
 * ----------------
 * JCommonInfo.java
 * ----------------
 * (C)opyright 2003-2008, by Thomas Morgner and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Thomas Morgner;
 *
 * $Id: JCommonInfo.java,v 1.8 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree;

import java.util.Arrays;
import java.util.ResourceBundle;

import org.jfree.base.BaseBoot;
import org.jfree.base.Library;
import org.jfree.ui.about.Contributor;
import org.jfree.ui.about.Licences;
import org.jfree.ui.about.ProjectInfo;
import org.jfree.util.ResourceBundleWrapper;

/**
 * Information about the JCommon project.  One instance of this class is
 * assigned to JCommon.INFO.
 *
 * @author David Gilbert
 */
public class JCommonInfo extends ProjectInfo {

    /** The singleton instance of the project info object. */
    private static JCommonInfo singleton;

    /**
     * Returns the single instance of this class.
     *
     * @return The single instance of information about the JCommon library.
     */
    public static synchronized JCommonInfo getInstance() {
        if (singleton == null) {
            singleton = new JCommonInfo();
        }
        return singleton;
    }

    /**
     * Creates a new instance.
     */
    private JCommonInfo() {

        // get a locale-specific resource bundle...
        final String baseResourceClass = "org.jfree.resources.JCommonResources";
        final ResourceBundle resources = ResourceBundleWrapper.getBundle(
                baseResourceClass);

        setName(resources.getString("project.name"));
        setVersion(resources.getString("project.version"));
        setInfo(resources.getString("project.info"));
        setCopyright(resources.getString("project.copyright"));

        setLicenceName("LGPL");
        setLicenceText(Licences.getInstance().getLGPL());

        setContributors(Arrays.asList(
            new Contributor[] {
                new Contributor("Anthony Boulestreau", "-"),
                new Contributor("Jeremy Bowman", "-"),
                new Contributor("J. David Eisenberg", "-"),
                new Contributor("Paul English", "-"),
                new Contributor("David Gilbert",
                        "david.gilbert@object-refinery.com"),
                new Contributor("Hans-Jurgen Greiner", "-"),
                new Contributor("Arik Levin", "-"),
                new Contributor("Achilleus Mantzios", "-"),
                new Contributor("Thomas Meier", "-"),
                new Contributor("Aaron Metzger", "-"),
                new Contributor("Thomas Morgner", "-"),
                new Contributor("Krzysztof Paz", "-"),
                new Contributor("Nabuo Tamemasa", "-"),
                new Contributor("Mark Watson", "-"),
                new Contributor("Matthew Wright", "-"),
                new Contributor("Hari", "-"),
                new Contributor("Sam (oldman)", "-")
            }
        ));

        addOptionalLibrary(new Library("JUnit", "3.8", "IBM Public Licence",
                "http://www.junit.org/"));

        setBootClass(BaseBoot.class.getName());
    }
}
