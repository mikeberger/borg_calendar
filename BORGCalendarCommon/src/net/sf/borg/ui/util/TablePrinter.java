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



public class TablePrinter implements Printable
{

    private JTable tableView;
    
    
    public int print(Graphics g, PageFormat pageFormat,
    int pageIndex) throws PrinterException
    {
        Graphics2D  g2 = (Graphics2D) g;
        g2.setColor(Color.black);
        int fontHeight=g2.getFontMetrics().getHeight();
        int fontDesent=g2.getFontMetrics().getDescent();
        
        //leave room for page number
        double pageHeight = pageFormat.getImageableHeight()-fontHeight;
        double pageWidth = pageFormat.getImageableWidth();
        double tableWidth = tableView.getColumnModel( ).getTotalColumnWidth();
        double scale = 1.0;
        if (tableWidth >= pageWidth)
        {
            scale =  pageWidth / tableWidth;
        }
        
        double headerHeightOnPage=tableView.getTableHeader().getHeight()*scale;
        double tableWidthOnPage=tableWidth*scale;
        
        double oneRowHeight=(tableView.getRowHeight()+ tableView.getRowMargin())*scale;
        int numRowsOnAPage=(int)((pageHeight-headerHeightOnPage)/oneRowHeight);
        double pageHeightForTable=oneRowHeight*numRowsOnAPage;
        int totalNumPages=(int)Math.ceil(((double)tableView.getRowCount())/numRowsOnAPage);
        if(pageIndex>=totalNumPages)
        {
            return NO_SUCH_PAGE;
        }
        
        g2.translate(pageFormat.getImageableX(),pageFormat.getImageableY());
        //bottom center
        g2.drawString("Page: "+(pageIndex+1),(int)pageWidth/2-35, (int)(pageHeight+fontHeight-fontDesent));
        
        g2.translate(0f,headerHeightOnPage);
        g2.translate(0f,-pageIndex*pageHeightForTable);
        
        //If this piece of the table is smaller
        //than the size available,
        //clip to the appropriate bounds.
        if (pageIndex + 1 == totalNumPages)
        {
            int lastRowPrinted = numRowsOnAPage * pageIndex;
            int numRowsLeft =tableView.getRowCount()- lastRowPrinted;
            g2.setClip(0,
            (int)(pageHeightForTable * pageIndex),
            (int) Math.ceil(tableWidthOnPage),
            (int) Math.ceil(oneRowHeight *
            numRowsLeft));
        }
        //else clip to the entire area available.
        else
        {
            g2.setClip(0,
            (int)(pageHeightForTable*pageIndex),
            (int) Math.ceil(tableWidthOnPage),
            (int) Math.ceil(pageHeightForTable));
        }
        
        g2.scale(scale,scale);
        tableView.paint(g2);
        g2.scale(1/scale,1/scale);
        g2.translate(0f,pageIndex*pageHeightForTable);
        g2.translate(0f, -headerHeightOnPage);
        g2.setClip(0, 0,
        (int) Math.ceil(tableWidthOnPage),
        (int)Math.ceil(headerHeightOnPage));
        g2.scale(scale,scale);
        tableView.getTableHeader().paint(g2);
        //paint header at top
        
        return Printable.PAGE_EXISTS;
    }
    
    static public void printTable(JTable tbl) throws Exception
    {
        PrintHelper.printPrintable( new TablePrinter(tbl));
    }
    
    private TablePrinter( JTable c )
    {
        tableView = c;
    }
}



