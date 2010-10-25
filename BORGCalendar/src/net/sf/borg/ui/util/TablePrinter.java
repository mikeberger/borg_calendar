/* this code was loosely based on code obtained from an online forum
 * that did not contain any copyright information */

/*
 This file is part of BORG.

 BORG is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 BORG is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with BORG; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JTable;

import net.sf.borg.common.PrintHelper;

/**
 * wraps a JTable in a class to make it Printable. contains a methos to send the JTable to a printer
 */
public class TablePrinter implements Printable {

	/**
	 * Sends a JTable to a printer
	 * 
	 * @param tbl the table
	 * 
	 * @throws Exception 
	 */
	static public void printTable(JTable tbl) throws Exception {
		// use print helper to print the table
		PrintHelper.printPrintable(new TablePrinter(tbl));
	}

	/** The table */
	private JTable theTable;

	/**
	 * constructor
	 * 
	 * @param c the table
	 */
	private TablePrinter(JTable c) {
		theTable = c;
	}

	/* (non-Javadoc)
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(Color.black);
		
		int fontHeight = g2.getFontMetrics().getHeight();
		int fontDesent = g2.getFontMetrics().getDescent();

		// leave room for page number
		double pageHeight = pageFormat.getImageableHeight() - fontHeight;
		
		double pageWidth = pageFormat.getImageableWidth();
		
		double tableWidth = theTable.getColumnModel().getTotalColumnWidth();
		
		// set the scaling if the table is wider than the page
		double scale = 1.0;
		if (tableWidth >= pageWidth) {
			scale = pageWidth / tableWidth;
		}

		double headerHeightOnPage = theTable.getTableHeader().getHeight()
				* scale;
		
		double tableWidthOnPage = tableWidth * scale;

		double oneRowHeight = (theTable.getRowHeight() + theTable
				.getRowMargin())
				* scale;
		
		int numRowsOnAPage = (int) ((pageHeight - headerHeightOnPage) / oneRowHeight);
		
		double pageHeightForTable = oneRowHeight * numRowsOnAPage;
		
		int totalNumPages = (int) Math.ceil(((double) theTable.getRowCount())
				/ numRowsOnAPage);
		
		// if we are being called to print a page that is more than the number
		// needed to print the table - then end the printout
		if (pageIndex >= totalNumPages) {
			return NO_SUCH_PAGE;
		}

		g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		
		// draw page number at bottom center
		g2.drawString("Page: " + (pageIndex + 1), (int) pageWidth / 2 - 35,
				(int) (pageHeight + fontHeight - fontDesent));

		g2.translate(0f, headerHeightOnPage);
		g2.translate(0f, -pageIndex * pageHeightForTable);

		// If this piece of the table is smaller
		// than the size available because we are on the last page, then
		// clip to the appropriate bounds.
		if (pageIndex + 1 == totalNumPages) {
			int lastRowPrinted = numRowsOnAPage * pageIndex;
			int numRowsLeft = theTable.getRowCount() - lastRowPrinted;
			g2.setClip(0, (int) (pageHeightForTable * pageIndex), (int) Math
					.ceil(tableWidthOnPage), (int) Math.ceil(oneRowHeight
					* numRowsLeft));
		}
		// else clip to the entire area available.
		else {
			g2.setClip(0, (int) (pageHeightForTable * pageIndex), (int) Math
					.ceil(tableWidthOnPage), (int) Math
					.ceil(pageHeightForTable));
		}

		// scale as needed to fit width
		g2.scale(scale, scale);
		
		// paint the table onto the graphics
		theTable.paint(g2);
		
		// reverse the scaling
		g2.scale(1 / scale, 1 / scale);
		
		// paint the table header at the top
		g2.translate(0f, pageIndex * pageHeightForTable);
		g2.translate(0f, -headerHeightOnPage);
		g2.setClip(0, 0, (int) Math.ceil(tableWidthOnPage), (int) Math
				.ceil(headerHeightOnPage));
		g2.scale(scale, scale);
		theTable.getTableHeader().paint(g2);

		return Printable.PAGE_EXISTS;
	}
}
