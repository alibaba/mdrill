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
 * ----------------------------
 * AbstractFileSelectionAction.java
 * ----------------------------
 * (C)opyright 2002-2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AbstractFileSelectionAction.java,v 1.4 2005/10/18 13:22:13 mungady Exp $
 *
 * Changes
 * -------
 * 21-Nov-2004 : Initial version
 *
 */
package org.jfree.ui.action;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;

import org.jfree.ui.ExtensionFileFilter;
import org.jfree.util.StringUtils;

/**
 * A base class for all file operations. This implementation provides all methods
 * to let the user select a file.
 *
 * @author Thomas Morgner
 */
public abstract class AbstractFileSelectionAction extends AbstractActionDowngrade {
    /**
     * The FileChooser that is used to perform the selection.
     */
    private JFileChooser fileChooser;
    /**
     * The (optional) parent component.
     */
    private Component parent;

    /**
     * Creates a new FileSelectionAction with the given optional parent component
     * as parent for the file chooser dialog.
     *
     * @param parent the parent
     */
    public AbstractFileSelectionAction(final Component parent) {
        this.parent = parent;
    }

    /**
     * Returns the file extension that should be used for the operation.
     *
     * @return the file extension.
     */
    protected abstract String getFileExtension();

    /**
     * Returns a descriptive text describing the file extension.
     *
     * @return the file description.
     */
    protected abstract String getFileDescription();

    /**
     * Returns the working directory that should be used when initializing
     * the FileChooser.
     *
     * @return the working directory.
     */
    protected File getCurrentDirectory() {
        return new File(".");
    }

    /**
     * Selects a file to use as target for the operation.
     *
     * @param selectedFile    the selected file.
     * @param dialogType  the dialog type.
     * @param appendExtension true, if the file extension should be added if
     *                        necessary, false if the unmodified filename should be used.
     * 
     * @return the selected and approved file or null, if the user canceled
     *         the operation
     */
    protected File performSelectFile(final File selectedFile,
                                     final int dialogType,
                                     final boolean appendExtension) {
        if (this.fileChooser == null) {
            this.fileChooser = createFileChooser();
        }

        this.fileChooser.setSelectedFile(selectedFile);
        this.fileChooser.setDialogType(dialogType);
        final int option = this.fileChooser.showDialog(this.parent, null);
        if (option == JFileChooser.APPROVE_OPTION) {
            final File selFile = this.fileChooser.getSelectedFile();
            String selFileName = selFile.getAbsolutePath();
            if (StringUtils.endsWithIgnoreCase(selFileName, getFileExtension()) == false) {
                selFileName = selFileName + getFileExtension();
            }
            return new File(selFileName);
        }
        return null;
    }

    /**
     * Creates the file chooser.
     *
     * @return the initialized file chooser.
     */
    protected JFileChooser createFileChooser() {
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(
            new ExtensionFileFilter(getFileDescription(), getFileExtension())
        );
        fc.setMultiSelectionEnabled(false);
        fc.setCurrentDirectory(getCurrentDirectory());
        return fc;
    }
    
}
