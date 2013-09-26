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
 * AboutResources_pl.java
 * ----------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 *
 * Original Author:     David Gilbert (for Object Refinery Limited);
 * Polish translation:  Krzysztof Paz (kpaz@samorzad.pw.edu.pl);
 * Fixed char-encoding  Piotr Bzdyl (piotr@geek.krakow.pl)
 *
 * $Id: AboutResources_pl.java,v 1.5 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes
 * -------
 * 15-Mar-2002 : Version 1 (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 31-Jan-2003 : Fixed character encoding (PB);
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
 * @author KP
 */
public class AboutResources_pl extends ListResourceBundle {

    /**
     * Default constructor.
     */
    public AboutResources_pl() {
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

        {"about-frame.tab.about",             "Informacja o"},
        {"about-frame.tab.system",            "System"},
        {"about-frame.tab.contributors",      "Tw\u00f3rcy"},
        {"about-frame.tab.licence",           "Licencja"},
        {"about-frame.tab.libraries",         "Biblioteki"},

        {"contributors-table.column.name",    "Nazwa:"},
        {"contributors-table.column.contact", "Kontakt:"},

        {"libraries-table.column.name",       "Nazwa:"},
        {"libraries-table.column.version",    "Wersja:"},
        {"libraries-table.column.licence",    "Licencja:"},
        {"libraries-table.column.info",       "Inne informacje:"},

        {"system-frame.title",                "W?a\u015bciwo\u015bci systemowe"},

        {"system-frame.button.close",         "Zamknij"},
        {"system-frame.button.close.mnemonic", new Character('Z')},

        {"system-frame.menu.file",                "Plik"},
        {"system-frame.menu.file.mnemonic",       new Character('P')},

        {"system-frame.menu.file.close",          "Zamknij"},
        {"system-frame.menu.file.close.mnemonic", new Character('K')},

        {"system-frame.menu.edit",                "Edycja"},
        {"system-frame.menu.edit.mnemonic",       new Character('E')},

        {"system-frame.menu.edit.copy",           "Kopiuj"},
        {"system-frame.menu.edit.copy.mnemonic",  new Character('C')},

        {"system-properties-table.column.name",   "Nazwa w?a\u015bciwo\u015bci:"},
        {"system-properties-table.column.value",  "Warto\u015b\u0107:"},

        {"system-properties-panel.popup-menu.copy", "Kopiuj" },
        {"system-properties-panel.popup-menu.copy.accelerator",
                            KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK) },

    };

}
