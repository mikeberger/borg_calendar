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
package net.sf.borg.ui.calendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.PrintHelper;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.View;



// monthView handles the printing of a single week
public class MonthPreView extends View
{


    private MonthPanel monPanel;

    static private void printMonthPanel( MonthPanel p ) throws Exception
    {
        Object options[] = { new Integer(1), new Integer(2), new Integer(3), new Integer(4),
         new Integer(5), new Integer(6), new Integer(7), new Integer(8),
         new Integer(9), new Integer(10), new Integer(11), new Integer(12)
        };
        Object choice = JOptionPane.showInputDialog(null, Resource.getResourceString("nummonths"), Resource.getResourceString("Print_Chooser"),
            JOptionPane.QUESTION_MESSAGE, null, options, options[0] );
        if( choice == null )
            return;
        Integer i = (Integer) choice;
        p.setPages(i.intValue());

        PrintHelper.printPrintable(p);

    }
    public static void printMonth(int month, int year) throws Exception
    {

        // use the Java print service
        // this relies on monthPanel.print to fill in a Graphic object and respond to the Printable API
        MonthPanel cp = new MonthPanel(month,year);       
        printMonthPanel(cp);

    }

    private void printAction()
    {
        try
        {
            printMonthPanel(monPanel);
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
    }

    public MonthPreView(int month, int year)
    {
        super();
        addModel(AppointmentModel.getReference());
        addModel(TaskModel.getReference());
        monPanel = new MonthPanel(month,year);
        monPanel.setBackground(Color.WHITE);
        monPanel.setPreferredSize(new Dimension(800,600));

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

        getContentPane().add(monPanel, BorderLayout.CENTER);
        ResourceHelper.setTitle(this, "Month_View");
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        pack();
        setVisible(true);

        manageMySize(PrefName.MONTHPREVIEWSIZE);
    }


    public void destroy()
    {
        this.dispose();
    }

    public void refresh()
    {
    }

}




