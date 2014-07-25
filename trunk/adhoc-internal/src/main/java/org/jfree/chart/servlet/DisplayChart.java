/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * -----------------
 * DisplayChart.java
 * -----------------
 * (C) Copyright 2002-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 19-Aug-2002 : Version 1;
 * 09-Mar-2005 : Added facility to serve up "one time" charts - see
 *               ServletUtilities.java (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 *
 */

package org.jfree.chart.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet used for streaming charts to the client browser from the temporary
 * directory.  You need to add this servlet and mapping to your deployment
 * descriptor (web.xml) in order to get it to work.  The syntax is as follows:
 * <xmp>
 * <servlet>
 *    <servlet-name>DisplayChart</servlet-name>
 *    <servlet-class>org.jfree.chart.servlet.DisplayChart</servlet-class>
 * </servlet>
 * <servlet-mapping>
 *     <servlet-name>DisplayChart</servlet-name>
 *     <url-pattern>/servlet/DisplayChart</url-pattern>
 * </servlet-mapping>
 * </xmp>
 */
public class DisplayChart extends HttpServlet {

    /**
     * Default constructor.
     */
    public DisplayChart() {
        super();
    }

    /**
     * Init method.
     *
     * @throws ServletException never.
     */
    public void init() throws ServletException {
        return;
    }

    /**
     * Service method.
     *
     * @param request  the request.
     * @param response  the response.
     *
     * @throws ServletException ??.
     * @throws IOException ??.
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String filename = request.getParameter("filename");

        if (filename == null) {
            throw new ServletException("Parameter 'filename' must be supplied");
        }

        //  Replace ".." with ""
        //  This is to prevent access to the rest of the file system
        filename = ServletUtilities.searchReplace(filename, "..", "");

        //  Check the file exists
        File file = new File(System.getProperty("java.io.tmpdir"), filename);
        if (!file.exists()) {
            throw new ServletException("File '" + file.getAbsolutePath()
                    + "' does not exist");
        }

        //  Check that the graph being served was created by the current user
        //  or that it begins with "public"
        boolean isChartInUserList = false;
        ChartDeleter chartDeleter = (ChartDeleter) session.getAttribute(
                "JFreeChart_Deleter");
        if (chartDeleter != null) {
            isChartInUserList = chartDeleter.isChartAvailable(filename);
        }

        boolean isChartPublic = false;
        if (filename.length() >= 6) {
            if (filename.substring(0, 6).equals("public")) {
                isChartPublic = true;
            }
        }

        boolean isOneTimeChart = false;
        if (filename.startsWith(ServletUtilities.getTempOneTimeFilePrefix())) {
            isOneTimeChart = true;
        }

        if (isChartInUserList || isChartPublic || isOneTimeChart) {
            //  Serve it up
            ServletUtilities.sendTempFile(file, response);
            if (isOneTimeChart) {
                file.delete();
            }
        }
        else {
            throw new ServletException("Chart image not found");
        }
        return;
    }

}
