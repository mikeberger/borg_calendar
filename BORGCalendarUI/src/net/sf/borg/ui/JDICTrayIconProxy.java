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
 * Copyright 2005 by Mike Berger
 */
package net.sf.borg.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.borg.common.util.Resource;
import net.sf.borg.control.Borg;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

public class JDICTrayIconProxy {

	

	static private JDICTrayIconProxy singleton = null;
	static public JDICTrayIconProxy getReference()
	{
		if( singleton == null )
			singleton = new JDICTrayIconProxy();
		return( singleton );
	}
	private TrayIcon TIcon = null;
    
    public void init(String trayname)
    {
        if (TIcon == null)
        {

            Image image = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/resource/borg16.jpg"));
            
			TIcon = new TrayIcon(new ImageIcon(image));
			
            TIcon.setToolTip(trayname);
            JPopupMenu popup = new JPopupMenu();
            
            JMenuItem item = new JMenuItem();
            ResourceHelper.setText(item, "Open_Calendar");            
            item.addActionListener(new OpenListener());
            popup.add(item);
            
            item = new JMenuItem();
            item.setText(Resource.getPlainResourceString("Open") + " " + Resource.getPlainResourceString("Week_View"));            
            item.addActionListener(new OpenWeekListener());
            popup.add(item);
            
            item = new JMenuItem();
            item.setText(Resource.getPlainResourceString("Open") + " " + Resource.getPlainResourceString("Day_View"));            
            item.addActionListener(new OpenDayListener());
            popup.add(item);
                       
            item = new JMenuItem();
            ResourceHelper.setText(item, "Open_Task_List");
            item.addActionListener(new TaskListener());
            popup.add(item);
            
            item = new JMenuItem();
            ResourceHelper.setText(item, "Open_Address_Book");
            item.addActionListener(new AddrListener());
            popup.add(item);
            
            item = new JMenuItem();
            ResourceHelper.setText(item, "To_Do_List");
            item.addActionListener(new TodoListener());
            popup.add(item);
            
            popup.addSeparator();
            
            item = new JMenuItem();
            ResourceHelper.setText(item, "Options");
            item.addActionListener(new OptionsListener());
            popup.add(item);
            
            popup.addSeparator();
            
            item = new JMenuItem();
            ResourceHelper.setText(item, "Exit");
            item.addActionListener(new ExitListener());
            popup.add(item);
            
            TIcon.setPopupMenu(popup);
            TIcon.addActionListener(new OpenListener());
            
            SystemTray tray = SystemTray.getDefaultSystemTray();
            tray.addTrayIcon(TIcon);
        }
    }
    
    // Called when exit option in systray menu is chosen
    static private class ExitListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //WindowsTrayIcon.cleanUp();
           Borg.shutdown();
        }
    }

    // Called when open option is systray menu is chosen
    private class OpenListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            CalendarView cg = CalendarView.getReference(true);
            cg.toFront();
        }
    }

    static private class TaskListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            TaskListView bt_ = TaskListView.getReference();
            bt_.refresh();
            bt_.setVisible(true);
        }
    }

    static private class AddrListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            AddrListView ab = AddrListView.getReference();
            ab.refresh();
            ab.setVisible(true);
        }
    }

    static private class TodoListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            TodoView.getReference().setVisible(true);
        }
    }
    
    static private class OptionsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            OptionsView.getReference().setVisible(true);
        }
    }

    static private class OpenWeekListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
        	GregorianCalendar cal = new GregorianCalendar();
            WeekView dv = new WeekView(cal.get(Calendar.MONTH),cal.get(Calendar.YEAR),
            		cal.get(Calendar.DATE));
            dv.setVisible(true);
            dv.toFront();
        }
    }
    
    static private class OpenDayListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
        	GregorianCalendar cal = new GregorianCalendar();
            DayView dv = new DayView(cal.get(Calendar.MONTH),cal.get(Calendar.YEAR),
            		cal.get(Calendar.DATE));
            dv.setVisible(true);
            dv.toFront();
        }
    }
 
}
