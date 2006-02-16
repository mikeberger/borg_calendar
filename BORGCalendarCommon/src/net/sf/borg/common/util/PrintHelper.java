package net.sf.borg.common.util;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PrintHelper {
	private static void initPrinterJobFields(PrinterJob job) {
		job.setJobName("BORG Printout");
		Class klass = job.getClass();
		try {
			Class printServiceClass = Class.forName("javax.print.PrintService");
			Method method = klass.getMethod("getPrintService", (Class[]) null);
			Object printService = method.invoke(job, (Object[]) null);
			method = klass.getMethod("setPrintService",
					new Class[] { printServiceClass });
			method.invoke(job, new Object[] { printService });
		} catch (NoSuchMethodException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (ClassNotFoundException e) {
		}


	}

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
