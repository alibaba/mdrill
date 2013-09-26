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
 * -------------------
 * ClassModelTags.java
 * -------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ClassModelTags.java,v 1.3 2005/10/18 13:33:53 mungady Exp $
 *
 * Changes 
 * -------
 * 20-Nov-2003 : Initial version (TM);
 * 25-Nov-2003 : Header and Javadoc updates (DG);
 *  
 */

package org.jfree.xml.util;

/**
 * A collection of tags used in the object model description.
 */
public final class ClassModelTags {

    /** Objects tag text. */
    public static final String OBJECTS_TAG = "objects";
    
    /** Include tag text. */
    public static final String INCLUDE_TAG = "include";
    
    /** Source attribute text. */
    public static final String SOURCE_ATTR = "src";
    
    /** Object tag text. */
    public static final String OBJECT_TAG = "object";
    
    /** Ignore attribute text. */
    public static final String IGNORE_ATTR = "ignore";  
    
    /** Class attribute text. */
    public static final String CLASS_ATTR = "class";
    
    /** Register name attribute text. */
    public static final String REGISTER_NAMES_ATTR = "register-name";
    
    /** Element property tag text. */
    public static final String ELEMENT_PROPERTY_TAG = "element-property";
    
    /** Attribute property tag text. */
    public static final String ATTRIBUTE_PROPERTY_TAG = "attribute-property";
    
    /** Lookup property tag text. */
    public static final String LOOKUP_PROPERTY_TAG = "lookup";
    
    /** Name attribute tag text. */
    public static final String NAME_ATTR = "name";
    
    /** Element attribute text. */
    public static final String ELEMENT_ATTR = "element";
    
    /** Attribute text. */
    public static final String ATTRIBUTE_ATTR = "attribute";
    
    /** Lookup attribute text. */
    public static final String LOOKUP_ATTR = "lookup";
    
    /** Constructor tag text. */
    public static final String CONSTRUCTOR_TAG = "constructor";
    
    /** Parameter tag text. */
    public static final String PARAMETER_TAG = "parameter";
    
    /** Property attribute text. */
    public static final String PROPERTY_ATTR = "property";
    
    /** Attribute handler text. */
    public static final String ATTRIBUTE_HANDLER_ATTR = "handler";
    
    /** Ignored property tag text. */
    public static final String IGNORED_PROPERTY_TAG = "ignore";

    /** Manual tag text. */
    public static final String MANUAL_TAG = "manual";
    
    /** Read handler attribute text. */
    public static final String READ_HANDLER_ATTR = "read-handler";
    
    /** Write handler attribute text. */
    public static final String WRITE_HANDLER_ATTR = "write-handler";

    /** Mapping tag text. */
    public static final String MAPPING_TAG = "mapping";
    
    /** Type attribute text. */
    public static final String TYPE_ATTR = "type-attribute";
    
    /** Base class attribute text. */
    public static final String BASE_CLASS_ATTR = "base-class";
    
    /** Type tag text. */
    public static final String TYPE_TAG = "type";

    /**
     * Private constructor prevents instantiation.
     */
    private ClassModelTags() {
        super();
    }
    
}
