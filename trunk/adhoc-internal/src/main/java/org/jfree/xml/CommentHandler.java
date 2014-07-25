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
 * CommentHandler.java
 * -------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: CommentHandler.java,v 1.3 2005/10/18 13:25:44 mungady Exp $
 *
 * Changes
 * -------------------------
 * 20-Jul-2003 : Initial version
 *
 */

package org.jfree.xml;

import java.util.ArrayList;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * The comment handler is used to collect all XML comments from the
 * SAX parser. The parser implementation must support comments to make
 * this feature work.
 *
 * @author Thomas Morgner
 */
public class CommentHandler implements LexicalHandler {
    
  /** A constant marking a comment on the opening tag. */
  public static final String OPEN_TAG_COMMENT = "parser.comment.open";
  
  /** A constant marking a comment on the closing tag. */
  public static final String CLOSE_TAG_COMMENT = "parser.comment.close";

  /** A list containing all collected comments. */
  private final ArrayList comment;
  
  /** a flag marking whether the SAX parser is currently working in the DTD. */
  private boolean inDTD;

  /**
   * DefaultConstructor.
   */
  public CommentHandler() {
    this.comment = new ArrayList();
  }

  /**
   * Report the start of DTD declarations, if any.
   *
   * <p>This method is empty.</p>
   *
   * @param name The document type name.
   * @param publicId The declared public identifier for the
   *        external DTD subset, or null if none was declared.
   * @param systemId The declared system identifier for the
   *        external DTD subset, or null if none was declared.
   * @exception org.xml.sax.SAXException The application may raise an
   *            exception.
   * @see #endDTD()
   * @see #startEntity(String)
   */
  public void startDTD(final String name, final String publicId,
                       final String systemId) throws SAXException {
    this.inDTD = true;
  }

  /**
   * Report the end of DTD declarations.
   *
   * <p>This method is empty.</p>
   *
   * @exception org.xml.sax.SAXException The application may raise an exception.
   */
  public void endDTD()
      throws SAXException {
    this.inDTD = false;
  }

  /**
   * Report the beginning of some internal and external XML entities.
   *
   * <p>This method is empty.</p>
   *
   * @param name The name of the entity.  If it is a parameter
   *        entity, the name will begin with '%', and if it is the
   *        external DTD subset, it will be "[dtd]".
   * @exception org.xml.sax.SAXException The application may raise an exception.
   * @see #endEntity(String)
   * @see org.xml.sax.ext.DeclHandler#internalEntityDecl
   * @see org.xml.sax.ext.DeclHandler#externalEntityDecl
   */
  public void startEntity(final String name)
      throws SAXException {
    // do nothing
  }

  /**
   * Report the end of an entity.
   *
   * <p>This method is empty.</p>
   *
   * @param name The name of the entity that is ending.
   * @exception org.xml.sax.SAXException The application may raise an exception.
   * @see #startEntity(String)
   */
  public void endEntity(final String name) throws SAXException {
    // do nothing
  }

  /**
   * Report the start of a CDATA section.
   *
   * <p>This method is empty.</p>
   *
   * @exception org.xml.sax.SAXException The application may raise an exception.
   * @see #endCDATA()
   */
  public void startCDATA() throws SAXException {
    // do nothing
  }

  /**
   * Report the end of a CDATA section.
   *
   * <p>This method is empty.</p>
   *
   * @exception org.xml.sax.SAXException The application may raise an exception.
   * @see #startCDATA()
   */
  public void endCDATA() throws SAXException {
    // do nothing
  }

  /**
   * Report an XML comment anywhere in the document.
   *
   * <p>This callback will be used for comments inside or outside the
   * document element, including comments in the external DTD
   * subset (if read).  Comments in the DTD must be properly
   * nested inside start/endDTD and start/endEntity events (if
   * used).</p>
   *
   * @param ch An array holding the characters in the comment.
   * @param start The starting position in the array.
   * @param length The number of characters to use from the array.
   * @exception org.xml.sax.SAXException The application may raise an exception.
   */
  public void comment(final char[] ch, final int start, final int length) throws SAXException {
    if (!this.inDTD) {
        this.comment.add(new String(ch, start, length));
    }
  }

  /**
   * Returns all collected comments as string array.
   * @return the array containing all comments.
   */
  public String[] getComments() {
    if (this.comment.isEmpty()) {
      return null;
    }
    return (String[]) this.comment.toArray(new String[this.comment.size()]);
  }

  /**
   * Clears all comments.
   */
  public void clearComments() {
    this.comment.clear();
  }
}
