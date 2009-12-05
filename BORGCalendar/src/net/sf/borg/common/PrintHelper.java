/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.common;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Some common printing related utilities
 */
public class PrintHelper {
	
	/**
	 * Initializes the PrinterJob settings and sets a default print service
	 * 
	 * @param job the job
	 */
	private static void initPrinterJobFields(PrinterJob job) {
		job.setJobName("BORG Printout");
		Class<? extends PrinterJob> klass = job.getClass();
		try {
			Class<?> printServiceClass = Class.forName("javax.print.PrintService");
			Method method = klass.getMethod("getPrintService", (Class[]) null);
			Object printService = method.invoke(job, (Object[]) null);
			method = klass.getMethod("setPrintService",
					new Class[] { printServiceClass });
			method.invoke(job, new Object[] { printService });
		} catch (NoSuchMethodException e) {
		  // empty
		} catch (IllegalAccessException e) {
		  // empty
		} catch (InvocationTargetException e) {
		  // empty
		} catch (ClassNotFoundException e) {
		  // empty
		}


	}

	/**
	 * Prints a printable object. Prompts the user for print settings
	 * using the standard native dialog if available
	 * 
	 * @param p the Printable
	 * 
	 * @throws Exception the exception
	 */
	static public void printPrintable(Printable p) throws Exception {

		PrinterJob printJob = PrinterJob.getPrinterJob();
		initPrinterJobFields(printJob);
		
		PageFormat pageFormat = printJob.defaultPage();
		Paper paper = pageFormat.getPaper();
		pageFormat.setOrientation(PageFormat.LANDSCAPE);
		paper.setSize(8.5*72,11*72);
		paper.setImageableArea(0.875*72,0.625*72,6.75*72,9.75*72);
		pageFormat.setPaper(paper);

		printJob.setPrintable(p,pageFormat);
		
		if (printJob.printDialog())
			printJob.print();

	}
}
