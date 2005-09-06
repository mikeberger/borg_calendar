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
package net.sf.borg.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.border.BevelBorder;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;




// weekView handles the printing of a single week
class WeekView extends View
{


    private WeekPanel wkPanel;


    static private void printPrintable( Printable p ) throws Exception
    {
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
	    aset.add(new Copies(1));
        aset.add(OrientationRequested.LANDSCAPE);

        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(p);
        if (printJob.printDialog(aset))
                printJob.print(aset);

    }
    static void printWeek(int month, int year, int date) throws Exception
    {

        // use the Java print service
        // this relies on weekPanel.print to fill in a Graphic object and respond to the Printable API
        WeekPanel cp = new WeekPanel(month,year,date);
        printPrintable(cp);
    }

    private void printWeek() throws Exception
    {
        printPrintable(wkPanel);
    }

    private void printAction()
    {
        try
        {
            printWeek();
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
    }

    public WeekView(int month, int year, int date)
    {
        super();
        addModel(AppointmentModel.getReference());
        addModel(TaskModel.getReference());
        wkPanel = new WeekPanel(month,year,date);
        wkPanel.setBackground(Color.WHITE);
        wkPanel.setPreferredSize(new Dimension(800,600));

        // for the preview, create a JFrame with the preview panel and print menubar
        JMenuBar menubar = new JMenuBar();
        JMenu pmenu = new JMenu();
        pmenu.setText(Resource.getResourceString("Action"));
        JMenuItem mitem = new JMenuItem();
        mitem.setText(Resource.getResourceString("Print"));
        mitem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                printAction();
            }
        });
        pmenu.add(mitem);
        JMenuItem quititem = new JMenuItem();
        quititem.setText(Resource.getResourceString("Dismiss"));
        quititem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                try{destroy();}catch(Exception e){}
            }
        });
        pmenu.add(quititem);
        menubar.add(pmenu);
        menubar.setBorder(new BevelBorder(BevelBorder.RAISED));

        setJMenuBar(menubar);

        getContentPane().add(wkPanel, BorderLayout.CENTER);
     	ResourceHelper.setTitle(this, "Week_View");
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        pack();
        setVisible(true);

        manageMySize( PrefName.WEEKVIEWSIZE);
    }


    public void destroy()
    {
        this.dispose();
    }

    public void refresh()
    {
    }

}



