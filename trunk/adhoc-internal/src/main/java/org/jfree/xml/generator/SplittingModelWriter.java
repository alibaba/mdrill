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
 * SplittingModelWriter.java
 * -------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: SplittingModelWriter.java,v 1.2 2005/10/18 13:32:20 mungady Exp $
 *
 * Changes
 * -------------------------
 * 12.11.2003 : Initial version
 *
 */

package org.jfree.xml.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.jfree.io.IOUtils;
import org.jfree.util.HashNMap;
import org.jfree.util.Log;
import org.jfree.xml.generator.model.ClassDescription;
import org.jfree.xml.generator.model.DescriptionModel;
import org.jfree.xml.generator.model.ManualMappingInfo;
import org.jfree.xml.generator.model.MappingModel;
import org.jfree.xml.generator.model.MultiplexMappingInfo;
import org.jfree.xml.util.ClassModelTags;

/**
 * A model writer that writes to multiple files.
 */
public class SplittingModelWriter extends ModelWriter {

    /** ??. */
    private HashNMap classDescriptionByPackage;
    
    /** The sources. */
    private ArrayList sources;
    
    /** The target file. */
    private File targetFile;
    
    /** The file extension. */
    private String extension;
    
    /** The plain file name. */
    private String plainFileName;
    
    /** ??. */
    private HashNMap manualMappingByPackage;
    
    /** ??. */
    private HashNMap multiplexMappingByPackage;

    /**
     * Creates a new instance.
     */
    public SplittingModelWriter() {
        super();
    }

    /**
     * Writes the model to the specified target.
     * 
     * @param target  the target file name.
     * 
     * @throws IOException if there is an I/O problem.
     */
    public synchronized void write(final String target) throws IOException {
     
        final DescriptionModel model = getModel();
        this.sources = new ArrayList(Arrays.asList(model.getSources()));
        this.targetFile = new File(target);
        this.plainFileName = IOUtils.getInstance().stripFileExtension(this.targetFile.getName());
        this.extension = IOUtils.getInstance().getFileExtension(target);

        // split into classDescriptionByPackage ...
        this.classDescriptionByPackage = new HashNMap();
        for (int i = 0; i < model.size(); i++) {
            final ClassDescription cd = model.get(i);
            if (cd.getSource() == null) {
                final String packageName = getPackage(cd.getObjectClass());
                final String includeFileName = this.plainFileName + "-" + packageName 
                    + this.extension;
                this.classDescriptionByPackage.add(includeFileName, cd);
            }
            else {
                this.classDescriptionByPackage.add(cd.getSource(), cd);
            }
        }

        final MappingModel mappingModel = model.getMappingModel();

        // split manual mappings into packages ...
        final ManualMappingInfo[] manualMappings = mappingModel.getManualMapping();
        this.manualMappingByPackage = new HashNMap();
        for (int i = 0; i < manualMappings.length; i++) {
            final ManualMappingInfo mapping = manualMappings[i];
            if (mapping.getSource() == null) {
                this.manualMappingByPackage.add("", mapping);
            }
            else {
                this.manualMappingByPackage.add(mapping.getSource(), mapping);
            }
        }

        // split manual mappings into packages ...
        final MultiplexMappingInfo[] multiplexMappings = mappingModel.getMultiplexMapping();
        this.multiplexMappingByPackage = new HashNMap();
        for (int i = 0; i < multiplexMappings.length; i++) {
            final MultiplexMappingInfo mapping = multiplexMappings[i];
            if (mapping.getSource() == null) {
                this.multiplexMappingByPackage.add("", mapping);
            }
            else {
                this.multiplexMappingByPackage.add(mapping.getSource(), mapping);
            }
        }


        final Object[] keys = this.classDescriptionByPackage.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {

            final String includeFileName = (String) keys[i];
            // write if not contained in the master file ...
            if (!includeFileName.equals("")) {
                writePackageFile(includeFileName);
            }
        }

        writeMasterFile();

        this.manualMappingByPackage = null;
        this.multiplexMappingByPackage = null;
        this.classDescriptionByPackage = null;
        this.sources = null;
    }

    /**
     * Writes a file for a package.
     * 
     * @param includeFileName  the name of the file.
     * 
     * @throws IOException if there is an I/O problem.
     */
    private void writePackageFile(final String includeFileName) throws IOException {
        
        final Iterator values = this.classDescriptionByPackage.getAll(includeFileName);
        final Iterator manualMappings = this.manualMappingByPackage.getAll(includeFileName);
        final Iterator multiplexMappings = this.multiplexMappingByPackage.getAll(includeFileName);
        if (!values.hasNext() && !manualMappings.hasNext() && !multiplexMappings.hasNext()) {
            return;
        }

        Log.debug ("Writing included file: " + includeFileName);
        // the current file need no longer be included manually ...
        this.sources.remove(includeFileName);

        final BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(
                    new File(this.targetFile.getParentFile(), includeFileName)
                ), 
                "UTF-8"
            )
        );

        writeXMLHeader(writer);
        writeStandardComment(writer, getModel().getModelComments());
        getWriterSupport().writeTag(writer, ClassModelTags.OBJECTS_TAG);

        while (values.hasNext()) {
            final ClassDescription cd = (ClassDescription) values.next();
            writeClassDescription(writer, cd);
        }


        while (manualMappings.hasNext()) {
            final ManualMappingInfo mi = (ManualMappingInfo) manualMappings.next();
            writeManualMapping(writer, mi);
        }

        while (multiplexMappings.hasNext()) {
            final MultiplexMappingInfo mi = (MultiplexMappingInfo) multiplexMappings.next();
            writeMultiplexMapping(writer, mi);
        }

        writeCloseComment(writer, getModel().getModelComments());
        getWriterSupport().writeCloseTag(writer, ClassModelTags.OBJECTS_TAG);
        writer.close();
    }

    /**
     * Returns the name of the package for the given class. This is a
     * workaround for the classloader behaviour of JDK1.2.2 where no
     * package objects are created.
     *
     * @param c the class for which we search the package.
     * 
     * @return the name of the package, never null.
     */
    public static String getPackage(final Class c) {
        final String className = c.getName();
        final int idx = className.lastIndexOf('.');
        if (idx <= 0) {
            // the default package
            return "";
        }
        else {
            return className.substring(0, idx);
        }
    }

    /**
     * Writes the master file.
     * 
     * @throws IOException if there is an I/O problem.
     */
    private void writeMasterFile() throws IOException {

        Log.debug ("Writing master file: " + this.targetFile);

        final BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(this.targetFile), "UTF-8")
        );

        writeXMLHeader(writer);
        writeStandardComment(writer, getModel().getModelComments());
        getWriterSupport().writeTag(writer, ClassModelTags.OBJECTS_TAG);

        for (int i = 0; i < this.sources.size(); i++) {
            final String includeFileName = (String) this.sources.get(i);
            if (!includeFileName.equals("")) {
                writeTag(writer, ClassModelTags.INCLUDE_TAG, ClassModelTags.SOURCE_ATTR,
                    includeFileName, getModel().getIncludeComment(includeFileName));
            }
        }

        final Object[] keys = this.classDescriptionByPackage.keySet().toArray();
        Arrays.sort(keys);
        for (int i = 0; i < keys.length; i++) {
            final String includeFileName = (String) keys[i];
            if (!includeFileName.equals("")) {
                writeTag(writer, ClassModelTags.INCLUDE_TAG, ClassModelTags.SOURCE_ATTR,
                    includeFileName, getModel().getIncludeComment(includeFileName));
            }
        }

        final Iterator values = this.classDescriptionByPackage.getAll("");
        while (values.hasNext()) {
            final ClassDescription cd = (ClassDescription) values.next();
            writeClassDescription(writer, cd);
        }

        final Iterator manualMappings = this.manualMappingByPackage.getAll("");
        while (manualMappings.hasNext()) {
            final ManualMappingInfo mi = (ManualMappingInfo) manualMappings.next();
            writeManualMapping(writer, mi);
        }

        final Iterator multiplexMappings = this.multiplexMappingByPackage.getAll("");
        while (multiplexMappings.hasNext()) {
            final MultiplexMappingInfo mi = (MultiplexMappingInfo) multiplexMappings.next();
            writeMultiplexMapping(writer, mi);
        }

        writeCloseComment(writer, getModel().getModelComments());
        getWriterSupport().writeCloseTag(writer, ClassModelTags.OBJECTS_TAG);
        writer.close();
    }
    
}
