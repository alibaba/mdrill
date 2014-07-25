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
 * ----------------
 * ModelWriter.java
 * ----------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ModelWriter.java,v 1.3 2005/10/18 13:32:20 mungady Exp $
 *
 * Changes
 * -------------------------
 * 21.06.2003 : Initial version
 *
 */

package org.jfree.xml.generator;

import java.io.IOException;
import java.io.Writer;

import org.jfree.xml.generator.model.ClassDescription;
import org.jfree.xml.generator.model.Comments;
import org.jfree.xml.generator.model.DescriptionModel;
import org.jfree.xml.generator.model.IgnoredPropertyInfo;
import org.jfree.xml.generator.model.ManualMappingInfo;
import org.jfree.xml.generator.model.MultiplexMappingInfo;
import org.jfree.xml.generator.model.PropertyInfo;
import org.jfree.xml.generator.model.PropertyType;
import org.jfree.xml.generator.model.TypeInfo;
import org.jfree.xml.util.ClassModelTags;
import org.jfree.xml.writer.AttributeList;
import org.jfree.xml.writer.SafeTagList;
import org.jfree.xml.writer.XMLWriterSupport;

/**
 * A model writer.
 */
public class ModelWriter {

    /** The tags that can be split. */
    private static SafeTagList safeTags;

    /**
     * Returns the safe tag list.
     * 
     * @return The safe tag list.
     */
    public static SafeTagList getSafeTags() {
        if (safeTags == null) {
            safeTags = new SafeTagList();
            safeTags.add(ClassModelTags.OBJECTS_TAG);
            safeTags.add(ClassModelTags.OBJECT_TAG);
            safeTags.add(ClassModelTags.CONSTRUCTOR_TAG);
            safeTags.add(ClassModelTags.ELEMENT_PROPERTY_TAG);
            safeTags.add(ClassModelTags.LOOKUP_PROPERTY_TAG);
            safeTags.add(ClassModelTags.ATTRIBUTE_PROPERTY_TAG);
            safeTags.add(ClassModelTags.PARAMETER_TAG);
            safeTags.add(ClassModelTags.INCLUDE_TAG);
            safeTags.add(ClassModelTags.IGNORED_PROPERTY_TAG);
            safeTags.add(ClassModelTags.MANUAL_TAG);
            safeTags.add(ClassModelTags.MAPPING_TAG);
            safeTags.add(ClassModelTags.TYPE_TAG);
        }
        return safeTags;
    }

    /** A support class for writing XML tags. */
    private XMLWriterSupport writerSupport;
    
    /** A model containing class descriptions. */
    private DescriptionModel model;

    /**
     * Creates a new model writer instance.
     */
    public ModelWriter() {
        this.writerSupport = new XMLWriterSupport(getSafeTags(), 0);
    }

    /**
     * Returns the model.
     * 
     * @return The model.
     */
    public DescriptionModel getModel() {
        return this.model;
    }

    /**
     * Sets the model to be written.
     * 
     * @param model  the model.
     */
    public void setModel(final DescriptionModel model) {
        this.model = model;
    }

    /**
     * Writes an XML header.
     * 
     * @param writer  the writer.
     * 
     * @throws IOException if there is an I/O problem.
     */
    public static void writeXMLHeader(final Writer writer) throws IOException {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.write(XMLWriterSupport.getLineSeparator());
    }

    /**
     * Writes a set of comments.
     * 
     * @param writer  the writer.
     * @param comments  a set of comments.
     * 
     * @throws IOException if there is an I/O problem.
     */
    protected void writeStandardComment(final Writer writer, final Comments comments) throws IOException {
        if ((comments == null) || (comments.getOpenTagComment() == null)) {
            writer.write(
                "<!-- CVSTag: $Id: ModelWriter.java,v 1.3 2005/10/18 13:32:20 mungady Exp $ " 
                + comments + " -->"
            );
            writer.write(XMLWriterSupport.getLineSeparator());
        }
        else {
            writeComment(writer, comments.getOpenTagComment());
        }
    }

    /**
     * Writes a sequence of comments.
     * 
     * @param writer  the writer.
     * @param comments  the comments (<code>null</code> ignored).
     * 
     * @throws IOException if there is an I/O problem.
     */
    protected void writeComment(final Writer writer, final String[] comments) throws IOException {
        if (comments == null) {
            return;
        }
        for (int i = 0; i < comments.length; i++) {
            this.writerSupport.indent(writer, XMLWriterSupport.INDENT_ONLY);
            writer.write("<!--");
            writer.write(comments[i]);
            writer.write("-->");
            writer.write(XMLWriterSupport.getLineSeparator());
        }
    }

    /**
     * Writes the open comments from a set of comments.
     * 
     * @param writer  the writer.
     * @param comments  the set of comments.
     * 
     * @throws IOException if there is an I/O problem.
     */
    protected void writeOpenComment(final Writer writer, final Comments comments) throws IOException {
        if (comments == null) {
            return;
        }
        if (comments.getOpenTagComment() == null) {
            return;
        }
        writeComment(writer, comments.getOpenTagComment());
    }

    /**
     * Writes the close comments from a set of comments.
     * 
     * @param writer  the writer.
     * @param comments  the set of comments.
     * 
     * @throws IOException if there is an I/O problem.
     */
    protected void writeCloseComment(final Writer writer, final Comments comments) throws IOException {
        if (comments == null) {
            return;
        }
        if (comments.getCloseTagComment() == null) {
            return;
        }
        writeComment(writer, comments.getCloseTagComment());
    }

    /**
     * Writes a closed (short) tag with eventually nested comments.
     *
     * @param writer  the writer.
     * @param tagName  the tag name.
     * @param attributes  the attributes.
     * @param comments  the comments.
     * 
     * @throws IOException if there is an I/O problem.
     */
    protected void writeTag(final Writer writer, 
                            final String tagName,
                            final AttributeList attributes,
                            final Comments comments) throws IOException {
        if (comments == null) {
            this.writerSupport.writeTag(writer, tagName, attributes, XMLWriterSupport.CLOSE);
        }
        else {
            writeOpenComment(writer, comments);
            if (comments.getCloseTagComment() != null) {
                this.writerSupport.writeTag(writer, tagName, attributes, XMLWriterSupport.OPEN);
                writeCloseComment(writer, comments);
                this.writerSupport.writeCloseTag(writer, tagName);
            }
            else {
                this.writerSupport.writeTag(writer, tagName, attributes, XMLWriterSupport.CLOSE);
            }
        }
    }

    /**
     * Writes a closed (short) tag with eventually nested comments.
     *
     * @param writer  the writer.
     * @param tagName  the tag name.
     * @param attribute  the attribute name.
     * @param value  the attribute value.
     * @param comments  the comments.
     * 
     * @throws IOException if there is an I/O problem.
     */
    protected void writeTag(final Writer writer, 
                            final String tagName,
                            final String attribute, 
                            final String value,
                            final Comments comments) throws IOException {
        if (comments == null) {
            this.writerSupport.writeTag(writer, tagName, attribute, value , XMLWriterSupport.CLOSE);
        }
        else {
            writeOpenComment(writer, comments);
            if (comments.getCloseTagComment() != null) {
                this.writerSupport.writeTag(
                    writer, tagName, attribute, value, XMLWriterSupport.OPEN
                );
                writeCloseComment(writer, comments);
                this.writerSupport.writeCloseTag(writer, tagName);
            }
            else {
                this.writerSupport.writeTag(
                    writer, tagName, attribute, value, XMLWriterSupport.CLOSE
                );
            }
        }
    }

    /**
     * Writes a model to the specified writer.
     * 
     * @param writer  the writer.
     * 
     * @throws IOException if there is an I/O problem.
     */
    public void write(final Writer writer) throws IOException {
        
        writeStandardComment(writer, this.model.getModelComments());
        this.writerSupport.writeTag(writer, ClassModelTags.OBJECTS_TAG);
        final String[] sources = this.model.getSources();
        for (int i = 0; i < sources.length; i++) {
            final Comments comments = this.model.getIncludeComment(sources[i]);
            writeTag(
                writer, 
                ClassModelTags.INCLUDE_TAG, ClassModelTags.SOURCE_ATTR, sources[i], comments
            );
        }

        for (int i = 0; i < this.model.size(); i++) {
            final ClassDescription cd = this.model.get(i);
            writeClassDescription(writer, cd);
        }

        final ManualMappingInfo[] mappings = getModel().getMappingModel().getManualMapping();
        for (int i = 0; i < mappings.length; i++) {
            final ManualMappingInfo mi = mappings[i];
            writeManualMapping(writer, mi);
        }

        final MultiplexMappingInfo[] mmappings = getModel().getMappingModel().getMultiplexMapping();
        for (int i = 0; i < mmappings.length; i++) {
            final MultiplexMappingInfo mi = mmappings[i];
            writeMultiplexMapping(writer, mi);
        }

        writeCloseComment(writer, this.model.getModelComments());
        this.writerSupport.writeCloseTag(writer, ClassModelTags.OBJECTS_TAG);
        
    }

    /**
     * Writes a manual mapping to the XML output.
     * 
     * @param writer  the writer.
     * @param mi  the mapping info.
     * 
     * @throws IOException if there is an I/O problem.
     */
    protected void writeManualMapping(final Writer writer, final ManualMappingInfo mi) throws IOException {
        final AttributeList al = new AttributeList();
        al.setAttribute(ClassModelTags.CLASS_ATTR, mi.getBaseClass().getName());
        al.setAttribute(ClassModelTags.READ_HANDLER_ATTR, mi.getReadHandler().getName());
        al.setAttribute(ClassModelTags.WRITE_HANDLER_ATTR, mi.getWriteHandler().getName());
        writeTag(writer, ClassModelTags.MANUAL_TAG, al, mi.getComments());
    }

    /**
     * Writes a multiplex mapping to the XML output.
     * 
     * @param writer  the writer.
     * @param mi  the mapping info.
     * 
     * @throws IOException if there is an I/O problem.
     */
    protected void writeMultiplexMapping(final Writer writer, final MultiplexMappingInfo mi)
        throws IOException {
        
        final TypeInfo[] tis = mi.getChildClasses();

        final AttributeList al = new AttributeList();
        al.setAttribute(ClassModelTags.BASE_CLASS_ATTR, mi.getBaseClass().getName());
        al.setAttribute(ClassModelTags.TYPE_ATTR, mi.getTypeAttribute());
        getWriterSupport().writeTag(writer, ClassModelTags.MAPPING_TAG, al, XMLWriterSupport.OPEN);

        for (int j = 0; j < tis.length; j++) {
            final AttributeList tiAttr = new AttributeList();
            tiAttr.setAttribute(ClassModelTags.NAME_ATTR, tis[j].getName());
            tiAttr.setAttribute(ClassModelTags.CLASS_ATTR, tis[j].getType().getName());
            writeTag(writer, ClassModelTags.TYPE_TAG, tiAttr, tis[j].getComments());
        }

        getWriterSupport().writeCloseTag(writer, ClassModelTags.MAPPING_TAG);
    }

    /**
     * Writes a class description.
     * 
     * @param writer  the writer.
     * @param cd  the class description.
     * 
     * @throws IOException if there is an I/O problem.
     */
    protected void writeClassDescription(final Writer writer, final ClassDescription cd) throws IOException {

        if (cd.isUndefined()) {
            return;
        }

        final AttributeList al = new AttributeList();
        al.setAttribute(ClassModelTags.CLASS_ATTR, cd.getName());
        if (cd.getRegisterKey() != null) {
            al.setAttribute(ClassModelTags.REGISTER_NAMES_ATTR, cd.getRegisterKey());
        }
        if (cd.isPreserve()) {
            al.setAttribute(ClassModelTags.IGNORE_ATTR, "true");
        }
        this.writerSupport.writeTag(writer, ClassModelTags.OBJECT_TAG, al, XMLWriterSupport.OPEN);

        final TypeInfo[] constructorInfo = cd.getConstructorDescription();
        if (constructorInfo != null && constructorInfo.length != 0) {
            this.writerSupport.writeTag(writer, ClassModelTags.CONSTRUCTOR_TAG);
            for (int i = 0; i < constructorInfo.length; i++) {
                final AttributeList constructorList = new AttributeList();
                constructorList.setAttribute(
                    ClassModelTags.CLASS_ATTR, constructorInfo[i].getType().getName()
                );
                constructorList.setAttribute(
                    ClassModelTags.PROPERTY_ATTR, constructorInfo[i].getName()
                );
                writeTag(writer, ClassModelTags.PARAMETER_TAG, constructorList, 
                         constructorInfo[i].getComments());
            }
            this.writerSupport.writeCloseTag(writer, ClassModelTags.CONSTRUCTOR_TAG);
        }

        final PropertyInfo[] properties = cd.getProperties();
        for (int i = 0; i < properties.length; i++) {
            writePropertyInfo(writer, properties[i]);
        }

        this.writerSupport.writeCloseTag(writer, ClassModelTags.OBJECT_TAG);
    }

    /**
     * Writes a property info element.
     * 
     * @param writer  the writer.
     * @param ipi  the property info.
     * 
     * @throws IOException if there is an I/O problem.
     */
    private void writePropertyInfo(final Writer writer, final PropertyInfo ipi) throws IOException {
        final AttributeList props = new AttributeList();
        props.setAttribute(ClassModelTags.NAME_ATTR, ipi.getName());

        if (ipi instanceof IgnoredPropertyInfo) {
            writeTag(writer, ClassModelTags.IGNORED_PROPERTY_TAG, props, ipi.getComments());
            return;
        }

        if (ipi.getPropertyType().equals(PropertyType.ATTRIBUTE)) {
            props.setAttribute(ClassModelTags.ATTRIBUTE_ATTR, ipi.getXmlName());
            props.setAttribute(ClassModelTags.ATTRIBUTE_HANDLER_ATTR, ipi.getXmlHandler());
            writeTag(writer, ClassModelTags.ATTRIBUTE_PROPERTY_TAG, props, ipi.getComments());
        }
        else if (ipi.getPropertyType().equals(PropertyType.ELEMENT)) {
            if (ipi.getComments() == null || ipi.getComments().getOpenTagComment() == null)
            {
                this.writerSupport.indent(writer, XMLWriterSupport.INDENT_ONLY);
                writer.write("<!-- property type is " + ipi.getType() + " -->");
                writer.write(System.getProperty("line.separator", "\n"));
            }
            props.setAttribute(ClassModelTags.ELEMENT_ATTR, ipi.getXmlName());
            writeTag(writer, ClassModelTags.ELEMENT_PROPERTY_TAG, props, ipi.getComments());
        }
        else {
            props.setAttribute(ClassModelTags.LOOKUP_ATTR, ipi.getXmlName());
            writeTag(writer, ClassModelTags.LOOKUP_PROPERTY_TAG, props, ipi.getComments());
        }
    }

    /**
     * Returns the writer support object.
     * 
     * @return The writer support object.
     */
    public XMLWriterSupport getWriterSupport() {
        return this.writerSupport;
    }
}
