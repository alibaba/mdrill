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
 * ----------------------
 * AboutResources_fr.java
 * ----------------------
 *
 * Original Author: Anthony Boulestreau;
 * Contributor(s): -;
 *
 * $Id: AboutResources_fr.java,v 1.5 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes
 * -------
 * 26-Mar-2002 : Version 1 (AB);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui.about.resources;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ListResourceBundle;
import javax.swing.KeyStroke;

/**
 * A resource bundle that stores all the user interface items that might need localisation.
 *
 * @author Anthony Boulestreau
 */
public class AboutResources_fr extends ListResourceBundle {

    /**
     * Default constructor.
     */
    public AboutResources_fr() {
    }

    /**
     * Returns the array of strings in the resource bundle.
     *
     * @return the resources.
     */
    public Object[][] getContents() {
        return CONTENTS;
    }

    /** The resources to be localised. */
    private static final Object[][] CONTENTS = {

            {"about-frame.tab.about", "A propos de"},
            {"about-frame.tab.system", "Syst\u00e8me"},
            {"about-frame.tab.contributors", "D\u00e9veloppeurs"},
            {"about-frame.tab.licence", "Licence"},
            {"about-frame.tab.libraries", "Biblioth\u00e8que"},

            {"contributors-table.column.name", "Nom:"},
            {"contributors-table.column.contact", "Contact:"},

            {"libraries-table.column.name", "Nom:"},
            {"libraries-table.column.version", "Version:"},
            {"libraries-table.column.licence", "Licence:"},
            {"libraries-table.column.info", "Autre Renseignement:"},

            {"system-frame.title", "Propri\u00e9t\u00e9s du Syst\u00e8me"},

            {"system-frame.button.close", "Fermer"},

            {"system-frame.menu.file", "Fichier"},
            {"system-frame.menu.file.mnemonic", new Character('F')},

            {"system-frame.menu.file.close", "Fermer"},
            {"system-frame.menu.file.close.mnemonic", new Character('C')},

            {"system-frame.menu.edit", "Edition"},
            {"system-frame.menu.edit.mnemonic", new Character('E')},

            {"system-frame.menu.edit.copy", "Copier"},
            {"system-frame.menu.edit.copy.mnemonic", new Character('C')},

            {"system-properties-table.column.name", "Nom de la Propri\u00e9t\u00e9:"},
            {"system-properties-table.column.value", "Valeur:"},

            {"system-properties-panel.popup-menu.copy", "Copier" },
            {"system-properties-panel.popup-menu.copy.accelerator",
            KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK) },

    };

}
