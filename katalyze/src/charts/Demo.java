/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
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
 * -------------------------
 * TimeSeriesChartDemo1.java
 * -------------------------
 * (C) Copyright 2003-2009, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   ;
 *
 * Changes
 * -------
 * 09-Mar-2005 : Version 1, copied from the demo collection that ships with
 *               the JFreeChart Developer Guide (DG);
 *
 */

package charts;

import java.io.FileInputStream;

import katalyzeapp.Katalyzer;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * An example of a time series chart.  For the most part, default settings are
 * used, except that the renderer is modified to show filled shapes (as well as
 * lines) at each data point.
 */
public class Demo {
	static Logger logger = Logger.getLogger(Demo.class);
   
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("log4j.xml");
		logger.info("Katalyze started");
		
			
		FileInputStream input = new FileInputStream("data/kattislog_vm2010.txt");
			
		Katalyzer katalyzer = new Katalyzer();
		katalyzer.process(input);
		
    }

}
