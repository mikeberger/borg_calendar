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

import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.borg.common.Resource;
import net.sf.borg.control.Borg;
import net.sf.borg.ui.address.AddrListView;
import net.sf.borg.ui.calendar.TodoView;
import net.sf.borg.ui.popup.PopupView;

public class SunTrayIconProxy {

	

	static private SunTrayIconProxy singleton = null;
	static public SunTrayIconProxy getReference()
	{
		if( singleton == null )
			singleton = new SunTrayIconProxy();
		return( singleton );
	}
	private TrayIcon TIcon = null;
    
    public void init(String trayname) throws Exception
    {
        if (TIcon == null)
        {
        	
        	if( !SystemTray.isSupported())
        		throw new Exception("Systray not supported");
        		
            Image image = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/resource/borg16.jpg"));
            
			TIcon = new TrayIcon(image);
			
            TIcon.setToolTip(trayname);
            PopupMenu popup = new PopupMenu();
            
            MenuItem item = new MenuItem(); 
            item.setLabel(Resource.getResourceString("Open_Calendar"));
            item.addActionListener(new OpenListener());
            popup.add(item);
            
           
            item = new MenuItem();
            item.setLabel(Resource.getResourceString("Open_Address_Book"));
            item.addActionListener(new AddrListener());
            popup.add(item);
            
            item = new MenuItem();
            item.setLabel(Resource.getResourceString("To_Do_List"));
            item.addActionListener(new TodoListener());
            popup.add(item);
            
            item = new MenuItem();
            item.setLabel(Resource.getResourceString("Show_Pops"));
            
            item.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent arg0) {
					PopupView.getReference().showAll();				
				}
            	
            });
            popup.add(item);
            
            item = new MenuItem();
            item.setLabel(Resource.getResourceString("Hide_Pops"));
            
            item.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent arg0) {
					PopupView.getReference().hideAll();				
				}
            	
            });
            popup.add(item);
            
            popup.addSeparator();
            
            item = new MenuItem();
            item.setLabel(Resource.getResourceString("Options"));
            item.addActionListener(new OptionsListener());
            popup.add(item);
            
            popup.addSeparator();
            
            item = new MenuItem();
            item.setLabel(Resource.getResourceString("Exit"));
            item.addActionListener(new ExitListener());
            popup.add(item);
            
            TIcon.setPopupMenu(popup);
            TIcon.addActionListener(new OpenListener());
            
            SystemTray tray = SystemTray.getSystemTray();
            tray.add(TIcon);
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
            MultiView mv = MultiView.getMainView();
            mv.toFront();
            mv.setState(Frame.NORMAL);
        }
    }

    static private class AddrListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            MultiView.getMainView().addView(AddrListView.getReference());
           
        }
    }

    static private class TodoListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            TodoView tg = TodoView.getReference();
	    MultiView.getMainView().addView(tg);    
        }
    }
    
    static private class OptionsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            OptionsView.getReference().setVisible(true);
        }
    }
 
}
