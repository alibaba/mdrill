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
 * ------------
 * JCommonXMLInfo.java
 * ------------
 * (C) Copyright 2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: JCommonXMLInfo.java,v 1.5 2006/03/23 19:47:05 taqua Exp $
 *
 * Changes
 * -------
 * 22-Feb-2005 : Initial version.
 * 03-Mar-2005 : Updated copyright.
 *
 */
package org.jfree.xml;

import java.util.Arrays;

import org.jfree.JCommonInfo;
import org.jfree.ui.about.Contributor;
import org.jfree.ui.about.ProjectInfo;

/**
 * The info implementation for the XML classes of JCommon.
 *
 * @author Thomas Morgner
 */
public class JCommonXMLInfo extends ProjectInfo {
    /**
     * The info singleton.
     */
    private static JCommonXMLInfo singleton;

    /**
     * Returns the single instance of this class.
     *
     * @return The single instance of information about the JCommon library.
     */
    public static synchronized JCommonXMLInfo getInstance() {
        if (singleton == null) {
            singleton = new JCommonXMLInfo();
        }
        return singleton;
    }


    /**
     * Constructs an empty project info object.
     */
    private JCommonXMLInfo() {
        final JCommonInfo info = JCommonInfo.getInstance();

        setName("JCommon-XML");
        setVersion(info.getVersion());
        setInfo(info.getInfo());
        setCopyright(info.getCopyright());

        setLicenceName(info.getLicenceName());
        setLicenceText(info.getLicenceText());

        setContributors(Arrays.asList(new Contributor[]{
            new Contributor("David Gilbert", "david.gilbert@object-refinery.com"),
            new Contributor("Thomas Morgner", "taqua@users.sourceforge.net"),
            new Contributor("Peter Becker", "-"),
        }));

        addLibrary(info);
    }
}
