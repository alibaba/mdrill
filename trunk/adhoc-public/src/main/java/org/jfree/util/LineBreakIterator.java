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
 * LineBreakIterator.java
 * ----------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: LineBreakIterator.java,v 1.4 2005/11/03 09:55:26 mungady Exp $
 *
 * Changes
 * -------
 * 13-03-2003 : Initial version
 */
package org.jfree.util;

import java.util.Iterator;

/**
 * An iterator that breaks text into lines.
 * The result is equal to BufferedReader.readLine().
 *
 * @author Thomas Morgner
 */
public class LineBreakIterator implements Iterator
{
  /** A useful constant. */
  public static final int DONE = -1;

  /** Storage for the text. */
  private char[] text;

  /** The current position. */
  private int position;

  /**
   * Default constructor.
   */
  public LineBreakIterator()
  {
    setText("");
  }

  /**
   * Creates a new line break iterator.
   *
   * @param text the text to be broken up.
   */
  public LineBreakIterator(final String text)
  {
    setText(text);
  }

  /**
   * Returns the position of the next break.
   *
   * @return A position.
   */
  public synchronized int nextPosition()
  {
    if (this.text == null)
    {
      return DONE;
    }
    if (this.position == DONE)
    {
      return DONE;
    }

    // recognize \n, \r, \r\n

    final int nChars = this.text.length;
    int nextChar = this.position;

    for (;;)
    {
      if (nextChar >= nChars)
      {
        /* End of text reached */
        this.position = DONE;
        return DONE;
      }

      boolean eol = false;
      char c = 0;
      int i;

      // search the next line break, either \n or \r
      for (i = nextChar; i < nChars; i++)
      {
        c = this.text[i];
        if ((c == '\n') || (c == '\r'))
        {
          eol = true;
          break;
        }
      }

      nextChar = i;
      if (eol)
      {
        nextChar++;
        if (c == '\r')
        {
          if ((nextChar < nChars) && (this.text[nextChar] == '\n'))
          {
            nextChar++;
          }
        }
        this.position = nextChar;
        return (this.position);
      }
    }
  }

  /**
   * Same like next(), but returns the End-Of-Text as
   * if there was a linebreak added (Reader.readLine() compatible)
   *
   * @return The next position.
   */
  public int nextWithEnd()
  {
    final int pos = this.position;
    if (pos == DONE)
    {
      return DONE;
    }
    if (pos == this.text.length)
    {
      this.position = DONE;
      return DONE;
    }
    final int retval = nextPosition();
    if (retval == DONE)
    {
      return this.text.length;
    }
    return retval;
  }

  /**
   * Returns the text to be broken up.
   *
   * @return The text.
   */
  public String getText()
  {
    return new String(this.text);
  }

  /**
   * Sets the text to be broken up.
   *
   * @param text  the text.
   */
  public void setText(final String text)
  {
    this.position = 0;
    this.text = text.toCharArray();
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements. (In other
   * words, returns <tt>true</tt> if <tt>next</tt> would return an element
   * rather than throwing an exception.)
   *
   * @return <tt>true</tt> if the iterator has more elements.
   */
  public boolean hasNext()
  {
    return (this.position != DONE);
  }

  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration.
   */
  public Object next()
  {
    if (this.position == DONE)
    {
      // allready at the end ...
      return null;
    }

    final int lastFound = this.position;
    int pos = nextWithEnd();
    if (pos == DONE)
    {
      // the end of the text has been reached ...
      return new String(this.text, lastFound, this.text.length - lastFound);
    }

    // step one char back
    if (pos > 0)
    {
      final int end = lastFound;
      for (; ((pos) > end) && ((this.text[pos - 1] == '\n') || this.text[pos - 1] == '\r'); pos--)
      {
        // search the end of the current linebreak sequence ..
      }
    }
    //System.out.println ("text: " + new String (text));
    //System.out.println ("pos: " + pos + " lastFound: " + lastFound);
    return new String(this.text, lastFound, pos - lastFound);
  }

  /**
   *
   * Removes from the underlying collection the last element returned by the
   * iterator (optional operation).  This method can be called only once per
   * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
   * the underlying collection is modified while the iteration is in
   * progress in any way other than by calling this method.
   *
   * @exception UnsupportedOperationException if the <tt>remove</tt>
   *    operation is not supported by this Iterator.
   * @exception IllegalStateException if the <tt>next</tt> method has not
   *    yet been called, or the <tt>remove</tt> method has already
   *    been called after the last call to the <tt>next</tt>
   *    method.
   */
  public void remove()
  {
      throw new UnsupportedOperationException("This iterator is read-only.");
  }
}
