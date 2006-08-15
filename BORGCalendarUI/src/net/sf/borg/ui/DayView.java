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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.border.BevelBorder;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.PrintHelper;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;

// weekView handles the printing of a single week
class DayView extends View
{

    private DayPanel dayPanel;


    static void printDay(int month, int year, int date) throws Exception
    {

        // use the Java print service
        // this relies on dayPanel.print to fill in a Graphic object and respond to the Printable API
        DayPanel cp = new DayPanel(month,year,date);
        PrintHelper.printPrintable(cp);
    }

    private void printDay() throws Exception
    {
        PrintHelper.printPrintable(dayPanel);
    }

    private void printAction()
    {
        try
        {
            printDay();
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
    }

    public DayView(int month, int year, int date)
    {
        super();
        addModel(AppointmentModel.getReference());
        addModel(TaskModel.getReference());
        dayPanel = new DayPanel(month,year,date);
        dayPanel.setBackground(Color.WHITE);
        dayPanel.setPreferredSize(new Dimension(800,600));

        // for the preview, create a JFrame with the preview panel and print menubar
        JMenuBar menubar = new JMenuBar();
        JMenu pmenu = new JMenu();
        ResourceHelper.setText(pmenu, "Action");
        JMenuItem mitem = new JMenuItem();
        ResourceHelper.setText(mitem, "Print");
        mitem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                printAction();
            }
        });
        pmenu.add(mitem);
        JMenuItem quititem = new JMenuItem();
        ResourceHelper.setText(quititem, "Dismiss");
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

        getContentPane().add(dayPanel, BorderLayout.CENTER);
        ResourceHelper.setTitle(this, "Day_View");
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        pack();
        setVisible(true);

        manageMySize(PrefName.DAYVIEWSIZE);
    }


    public void destroy()
    {
        this.dispose();
    }

    public void refresh()
    {
    	dayPanel.clearData();
    	dayPanel.repaint();
    }

}



