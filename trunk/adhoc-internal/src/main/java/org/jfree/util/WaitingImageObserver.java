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
 * WaitingImageObserver.java
 * -------------------------
 * (C)opyright 2000-2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner
 * Contributor(s):   Stefan Prange;
 *
 * $Id: WaitingImageObserver.java,v 1.8 2008/09/10 09:24:41 mungady Exp $
 *
 * Changes (from 8-Feb-2002)
 * -------------------------
 * 15-Apr-2002 : first version used by ImageElement.
 * 16-May-2002 : Line delimiters adjusted
 * 04-Jun-2002 : Documentation and added a NullPointerCheck for the constructor.
 * 14-Jul-2002 : BugFixed: WaitingImageObserver dead-locked (bugfix by Stefan
 *               Prange)
 * 18-Mar-2003 : Updated header and made minor Javadoc changes (DG);
 * 21-Sep-2003 : Moved from JFreeReport.
 */

package org.jfree.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.Serializable;

/**
 * This image observer blocks until the image is completely loaded. AWT
 * defers the loading of images until they are painted on a graphic.
 *
 * While printing reports it is not very nice, not to know whether a image
 * was completely loaded, so this observer forces the loading of the image
 * until a final state (either ALLBITS, ABORT or ERROR) is reached.
 *
 * @author Thomas Morgner
 */
public class WaitingImageObserver implements ImageObserver, Serializable,
                                             Cloneable
{
  /** For serialization. */
  static final long serialVersionUID = -807204410581383550L;

  /** The lock. */
  private boolean lock;

  /** The image. */
  private Image image;

  /** A flag that signals an error. */
  private boolean error;

  /**
   * Creates a new <code>ImageObserver<code> for the given <code>Image<code>.
   * The observer has to be started by an external thread.
   *
   * @param image  the image to observe (<code>null</code> not permitted).
   */
  public WaitingImageObserver(final Image image) {
    if (image == null) {
      throw new NullPointerException();
    }
    this.image = image;
    this.lock = true;
  }

  /**
   * Callback function used by AWT to inform that more data is available. The
   * observer waits until either all data is loaded or AWT signals that the
   * image cannot be loaded.
   *
   * @param     img   the image being observed.
   * @param     infoflags   the bitwise inclusive OR of the following
   *               flags:  <code>WIDTH</code>, <code>HEIGHT</code>,
   *               <code>PROPERTIES</code>, <code>SOMEBITS</code>,
   *               <code>FRAMEBITS</code>, <code>ALLBITS</code>,
   *               <code>ERROR</code>, <code>ABORT</code>.
   * @param     x   the <i>x</i> coordinate.
   * @param     y   the <i>y</i> coordinate.
   * @param     width    the width.
   * @param     height   the height.
   *
   * @return    <code>false</code> if the infoflags indicate that the
   *            image is completely loaded; <code>true</code> otherwise.
   */
  public synchronized boolean imageUpdate(
      final Image img,
      final int infoflags,
      final int x,
      final int y,
      final int width,
      final int height) {
    if ((infoflags & ImageObserver.ALLBITS) == ImageObserver.ALLBITS) {
        this.lock = false;
        this.error = false;
        notifyAll();
        return false;
    }
    else if ((infoflags & ImageObserver.ABORT) == ImageObserver.ABORT
        || (infoflags & ImageObserver.ERROR) == ImageObserver.ERROR) {
        this.lock = false;
        this.error = true;
        notifyAll();
        return false;
    }
    //notifyAll();
    return true;
  }

  /**
   * The workerthread. Simply draws the image to a BufferedImage's
   * Graphics-Object and waits for the AWT to load the image.
   */
  public synchronized void waitImageLoaded() {

    if (this.lock == false)
    {
      return;
    }

    final BufferedImage img = new BufferedImage(
        1, 1, BufferedImage.TYPE_INT_RGB
    );
    final Graphics g = img.getGraphics();

    while (this.lock) {
      if (g.drawImage(this.image, 0, 0, img.getWidth(this),
            img.getHeight(this), this)) {
        return;
      }

      try {
        wait(500);
      }
      catch (InterruptedException e) {
        Log.info(
          "WaitingImageObserver.waitImageLoaded(): InterruptedException thrown",
          e
        );
      }
    }
  }

  /**
   * Clones this WaitingImageObserver.
   *
   * @return a clone.
   *
   * @throws CloneNotSupportedException this should never happen.
   * @deprecated cloning may lock down the observer
   */
  public Object clone() throws CloneNotSupportedException {
    return (WaitingImageObserver) super.clone();
  }

  /**
   * Returns <code>true</code> if loading is complete, and <code>false</code>
   * otherwise.
   *
   * @return A boolean.
   */
  public boolean isLoadingComplete() {
    return this.lock == false;
  }

  /**
   * Returns true if there is an error condition, and false otherwise.
   *
   * @return A boolean.
   */
  public boolean isError() {
    return this.error;
  }
}
