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
 *    WekaOffscreenChartRenderer.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.image.BufferedImage;
import java.util.List;

import weka.core.Instance;
import weka.core.Instances;
import weka.gui.AttributeVisualizationPanel;
import weka.gui.visualize.Plot2D;
import weka.gui.visualize.PlotData2D;

/**
 * Default OffscreenChartRenderer that uses Weka's built-in chart and graph
 * classes.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7662 $
 */
public class WekaOffscreenChartRenderer extends AbstractOffscreenChartRenderer {
  
  /**
   * The name of this off screen renderer
   * 
   * @return the name of this off screen renderer
   */
  public String rendererName() {
    return "Weka Chart Renderer";
  }
  
  /**
   * Gets a short list of additional options (if any),
   * suitable for displaying in a tip text, in HTML form
   * 
   * @return additional options description in simple HTML form
   */
  public String optionsTipTextHTML() {
    return "<html><ul><li>-title=[chart title]</li>" +
    		"<li>-color=[coloring/class attribute name]</li></html>";
  }

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
  public BufferedImage renderXYLineChart(int width, int height,
      List<Instances> series, String xAxis, String yAxis,
      List<String> optionalArgs) throws Exception {
    BufferedImage osi = new BufferedImage(width, height, 
        BufferedImage.TYPE_INT_RGB);
    
    String plotTitle = "Line chart";
    String userTitle = getOption(optionalArgs, "-title");
    plotTitle = (userTitle != null) ? userTitle : plotTitle;
    
    Plot2D offScreenPlot = new Plot2D();
    offScreenPlot.setSize(width, height);
    
    // master plot
    PlotData2D master = new PlotData2D(series.get(0));
    master.setPlotName(plotTitle);
    boolean[] connectPoints = new boolean[series.get(0).numInstances()];
    for (int i = 0; i < connectPoints.length; i++) {
      connectPoints[i] = true;
    }
    master.setConnectPoints(connectPoints);
    offScreenPlot.setMasterPlot(master);
    // find x and y axis
    Instances masterInstances = series.get(0);
    int xAx = getIndexOfAttribute(masterInstances, xAxis);
    int yAx = getIndexOfAttribute(masterInstances, yAxis);
    if (xAx < 0) {
      xAx = 0;
    }
    if (yAx < 0) {
      yAx = 0;
    }
    
    // plotting axes and color
    offScreenPlot.setXindex(xAx);
    offScreenPlot.setYindex(yAx);
    offScreenPlot.setCindex(masterInstances.numAttributes() - 1);
    String colorAtt = getOption(optionalArgs, "-color");
    int tempC = getIndexOfAttribute(masterInstances, colorAtt);
    if (tempC >= 0) {
      offScreenPlot.setCindex(tempC);
    }
    
    // additional plots
    if (series.size() > 1) {
      for (Instances plotI : series) {
        PlotData2D plotD = new PlotData2D(plotI);
        connectPoints = new boolean[plotI.numInstances()];
        for (int i = 0; i < connectPoints.length; i++) {
          connectPoints[i] = true;
        }
        plotD.setConnectPoints(connectPoints);
        offScreenPlot.addPlot(plotD);
      }
    }
    
    // render
    java.awt.Graphics g = osi.getGraphics();
    offScreenPlot.paintComponent(g);
    
    return osi;
  }

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
  public BufferedImage renderXYScatterPlot(int width, int height,
      List<Instances> series, String xAxis, String yAxis,
      List<String> optionalArgs) throws Exception {
    
    BufferedImage osi = new BufferedImage(width, height, 
        BufferedImage.TYPE_INT_RGB);
    
    String plotTitle = "Scatter plot";
    String userTitle = getOption(optionalArgs, "-title");
    plotTitle = (userTitle != null) ? userTitle : plotTitle;
    
    Plot2D offScreenPlot = new Plot2D();
    offScreenPlot.setSize(width, height);
    
    // master plot
    PlotData2D master = new PlotData2D(series.get(0));
    master.setPlotName(plotTitle);
    master.m_displayAllPoints = true;
    
    offScreenPlot.setMasterPlot(master);
    
    Instances masterInstances = series.get(0);
    int xAx = getIndexOfAttribute(masterInstances, xAxis);
    int yAx = getIndexOfAttribute(masterInstances, yAxis);
    if (xAx < 0) {
      xAx = 0;
    }
    if (yAx < 0) {
      yAx = 0;
    }
    
    // plotting axes and color
    offScreenPlot.setXindex(xAx);
    offScreenPlot.setYindex(yAx);
    offScreenPlot.setCindex(masterInstances.numAttributes() - 1);
    String colorAtt = getOption(optionalArgs, "-color");
    int tempC = getIndexOfAttribute(masterInstances, colorAtt);
    if (tempC >= 0) {
      offScreenPlot.setCindex(tempC);
    }
    
    String hasErrors = getOption(optionalArgs, "-hasErrors");
    // master plot is the error cases
    if (hasErrors != null) {
      int[] plotShapes = new int[masterInstances.numInstances()];
      for (int i = 0; i < plotShapes.length; i++) {
        plotShapes[i] = Plot2D.ERROR_SHAPE;
      }
      master.setShapeType(plotShapes);
    }
    
    // look for  an additional attribute that stores the
    // shape sizes
    String shapeSize = getOption(optionalArgs, "-shapeSize");
    if (shapeSize != null && shapeSize.length() > 0) {
      int shapeSizeI = getIndexOfAttribute(masterInstances, shapeSize);
      
      if (shapeSizeI >= 0) {
        int[] plotSizes = new int[masterInstances.numInstances()];
        for (int i = 0; i < masterInstances.numInstances(); i++) {
          plotSizes[i] = (int)masterInstances.instance(i).value(shapeSizeI);
        }
        master.setShapeSize(plotSizes);
      }
    }        
    
    // additional plots
    if (series.size() > 1) {
      for (Instances plotI : series) {
        PlotData2D plotD = new PlotData2D(plotI);
        plotD.m_displayAllPoints = true;

        offScreenPlot.addPlot(plotD);

        if (shapeSize != null && shapeSize.length() > 0) {
          int shapeSizeI = getIndexOfAttribute(plotI, shapeSize);
          if (shapeSizeI >= 0) {
            int[] plotSizes = new int[plotI.numInstances()];
            for (int i = 0; i < plotI.numInstances(); i++) {
              plotSizes[i] = (int)plotI.instance(i).value(shapeSizeI);
            }
            plotD.setShapeSize(plotSizes);
          }
        }
        
        // all other plots will have x shape if master plot are errors
        if (hasErrors != null) {
          int[] plotShapes = new int[plotI.numInstances()];
          for (int i = 0; i < plotShapes.length; i++) {
            plotShapes[i] = Plot2D.X_SHAPE;
          }
          plotD.setShapeType(plotShapes);
        }
      }
    }
    
    // render
    java.awt.Graphics g = osi.getGraphics();
    offScreenPlot.paintComponent(g);
    
    return osi;
  }

  /**
   * Render histogram(s) (numeric attribute) or pie chart (nominal attribute).
   * Some implementations may not be able to render more than one histogram/pie
   * on the same chart - the implementation can either throw an exception or
   * just process the first series in this case.
   * 
   * This Default implementation uses Weka's built in VisualizeAttributePanel
   * to render with and, as such, can only render histograms. It produces
   * histograms for both numeric and nominal attributes.
   * 
   * @param width the width of the resulting chart in pixels
   * @param height the height of the resulting chart in pixels
   * @param series a list of Instances - one for each series to be plotted
   * @param attsToPlot the attribute to plot
   * corresponding to the Instances in the series list
   * @param optionalArgs optional arguments to the renderer (may be null)
   * 
   * @return a BufferedImage containing the chart
   * @throws Exception if there is a problem rendering the chart
   */
  public BufferedImage renderHistogram(int width, int height,
      List<Instances> series, String attToPlot,
      List<String> optionalArgs) throws Exception {
    
    BufferedImage osi = new BufferedImage(width, height, 
        BufferedImage.TYPE_INT_RGB);
    
    // we can only handle one series per plot so merge all series together
    Instances toPlot = new Instances(series.get(0));
    for (int i = 1; i < series.size(); i++) {
      Instances additional = series.get(i);
      for (Instance temp : additional) {
        toPlot.add(temp);
      }
    }
    
    int attIndex = getIndexOfAttribute(toPlot, attToPlot);
    if (attIndex < 0) {
      attIndex = 0;
    }
    
    String colorAtt = getOption(optionalArgs, "-color");
    int tempC = getIndexOfAttribute(toPlot, colorAtt);
    
    AttributeVisualizationPanel offScreenPlot = 
      new AttributeVisualizationPanel();
    offScreenPlot.setSize(width, height);
    offScreenPlot.setInstances(toPlot);
    offScreenPlot.setAttribute(attIndex);
    if (tempC >= 0) {
      offScreenPlot.setColoringIndex(tempC);
    }        
    
    // render
    java.awt.Graphics g = osi.getGraphics();
    offScreenPlot.paintComponent(g);
    // wait a little while so that the calculation thread can complete
    Thread.sleep(2000);
    offScreenPlot.paintComponent(g);
    
//    offScreenPlot.setAttribute(attIndex);
    
    return osi;
  }
}
