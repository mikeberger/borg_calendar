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
 * Copyright 2003 by ==Quiet==
 */
package net.sf.borg.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Version;

import com.jeans.trayicon.TrayIconException;
import com.jeans.trayicon.TrayIconPopup;
import com.jeans.trayicon.TrayIconPopupSimpleItem;
import com.jeans.trayicon.WindowsTrayIcon;


public class TrayIconProxy {
	   static
	    {
	        Version
	                .addVersion("$Id$");
	    }
	static private TrayIconProxy singleton = null;
	static public TrayIconProxy getReference()
	{
		if( singleton == null )
			singleton = new TrayIconProxy();
		return( singleton );
	}
	private WindowsTrayIcon WTIcon = null;
    
    public void init(String trayname)
    {
        if (WTIcon == null)
        {
            WindowsTrayIcon.initTrayIcon("BORG");
            Image image = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/resource/borg16.jpg"));
            try {
				WTIcon = new WindowsTrayIcon(image, 16, 16);
			} catch (TrayIconException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            WTIcon.setToolTipText(trayname);
            TrayIconPopup popup = new TrayIconPopup();
            TrayIconPopupSimpleItem item = new TrayIconPopupSimpleItem(
                    Resource.getResourceString("Open_Calendar"));
            item.setDefault(true);
            item.addActionListener(new OpenListener());
            popup.addMenuItem(item);
            item = new TrayIconPopupSimpleItem(Resource
                    .getResourceString("Open_Task_List"));
            item.addActionListener(new TaskListener());
            popup.addMenuItem(item);
            item = new TrayIconPopupSimpleItem(Resource
                    .getResourceString("Open_Address_Book"));
            item.addActionListener(new AddrListener());
            popup.addMenuItem(item);
            item = new TrayIconPopupSimpleItem(Resource
                    .getResourceString("To_Do_List"));
            item.addActionListener(new TodoListener());
            popup.addMenuItem(item);
            item = new TrayIconPopupSimpleItem(Resource
                    .getResourceString("Options"));
            item.addActionListener(new OptionsListener());
            popup.addMenuItem(item);
            item = new TrayIconPopupSimpleItem(Resource
                    .getResourceString("Exit"));
            item.addActionListener(new ExitListener());
            popup.addMenuItem(item);
            WTIcon.setVisible(true);
            WTIcon.setPopup(popup);
            WTIcon.addMouseListener(new trayMouseListener());
        }
    }
    
    public void cleanUp()
    {
    	WindowsTrayIcon.cleanUp();
    }
    
    // Called when exit option in systray menu is chosen
    static private class ExitListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            WindowsTrayIcon.cleanUp();
            System.exit(0);
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
            bt_.show();
        }
    }

    static private class AddrListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            AddrListView ab = AddrListView.getReference();
            ab.refresh();
            ab.show();
        }
    }

    static private class TodoListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            TodoView.getReference().show();
        }
    }
    
    static private class OptionsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            OptionsView.getReference().show();
        }
    }

    // Test listener for double-click events
    private class trayMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent evt) {
            if ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0
                    && evt.getClickCount() == 2)
            {
                CalendarView cg = CalendarView.getReference(true);
                cg.toFront();
            }
        }
    }

}
