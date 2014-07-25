/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    OffscreenChartRenderer.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.image.BufferedImage;
import java.util.List;

import weka.core.Instances;

/**
 * Interface to something that can render certain types of charts
 * in headless mode.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7661 $
 */
public interface OffscreenChartRenderer {

  /**
   * The name of this off screen renderer
   * 
   * @return the name of this off screen renderer
   */
  String rendererName();
  
  /**
   * Gets a short list of additional options (if any),
   * suitable for displaying in a tip text, in HTML form
   * 
   * @return additional options description in simple HTML form
   */
  String optionsTipTextHTML();
  
  /**
   * Render an XY line chart
   * 
   * @param width the width of the resulting chart in pixels
   * @param height the height of the resulting chart in pixels
   * @param series a list of Instances - one for each series to be plotted
   * @param xAxis the name of the attribute for the x-axis (all series Instances 
   * are expected to have an attribute of the same type with this name)
   * @param yAxis the name of the attribute for the y-axis (all series Instances 
   * are expected to have an attribute of the same type with this name)
   * @param optionalArgs optional arguments to the renderer (may be null)
   * 
   * @return a BufferedImage containing the chart
   * @throws Exception if there is a problem rendering the chart
   */
  BufferedImage renderXYLineChart(int width, int height, List<Instances> series, 
      String xAxis, String yAxis, List<String> optionalArgs) throws Exception;
  
  /**
   * Render an XY scatter plot
   * 
   * @param width the width of the resulting chart in pixels
   * @param height the height of the resulting chart in pixels
   * @param series a list of Instances - one for each series to be plotted
   * @param xAxis the name of the attribute for the x-axis (all series Instances 
   * are expected to have an attribute of the same type with this name)
   * @param yAxis the name of the attribute for the y-axis (all series Instances 
   * are expected to have an attribute of the same type with this name)
   * @param optionalArgs optional arguments to the renderer (may be null)
   * 
   * @return a BufferedImage containing the chart
   * @throws Exception if there is a problem rendering the chart
   */
  BufferedImage renderXYScatterPlot(int width, int height, List<Instances> series, 
      String xAxis, String yAxis, List<String> optionalArgs) throws Exception;
  
  /**
   * Render histogram(s) (numeric attribute) or bar chart(s) (nominal attribute).
   * Some implementations may not be able to render more than one histogram/pie
   * on the same chart - the implementation can either throw an exception or
   * just process the first series in this case. 
   * 
   * @param width the width of the resulting chart in pixels
   * @param height the height of the resulting chart in pixels
   * @param series a list of Instances - one for each series to be plotted
   * @param attToPlot the name of the attribute to plot (the attribute, with the,
   * same type, must be present in each series) 
   * @param optionalArgs optional arguments to the renderer (may be null)
   * 
   * @return a BufferedImage containing the chart
   * @throws Exception if there is a problem rendering the chart
   */
  BufferedImage renderHistogram(int width, int height, List<Instances> series, 
      String attsToPlot, List<String> optionalArgs)
    throws Exception;
}
