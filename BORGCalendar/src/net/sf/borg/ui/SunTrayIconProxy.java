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

/** communicates with the new java built-in system tray APIs */
public class SunTrayIconProxy {

	// action that opens the main view
	private class OpenListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MultiView mv = MultiView.getMainView();
			mv.toFront();
			mv.setState(Frame.NORMAL);
		}
	}

	// the singleton
	static private SunTrayIconProxy singleton = null;

	/**
	 * get the singleton
	 * 
	 * @return the singleton
	 */
	static public SunTrayIconProxy getReference() {
		if (singleton == null)
			singleton = new SunTrayIconProxy();
		return (singleton);
	}

	/**
	 * initalize the system tray
	 * @param trayname the tray name (when the user hovers over the tray icon)
	 * @throws Exception
	 */
	public void init(String trayname) throws Exception {
		TrayIcon TIcon = null;

		if (!SystemTray.isSupported())
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
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				MultiView.getMainView().addView(AddrListView.getReference());

			}
		});
		popup.add(item);

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("To_Do_List"));
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				TodoView tg = TodoView.getReference();
				MultiView.getMainView().addView(tg);
			}
		});
		popup.add(item);

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("Show_Pops"));

		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				PopupView.getReference().showAll();
			}

		});
		popup.add(item);

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("Hide_Pops"));

		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				PopupView.getReference().hideAll();
			}

		});
		popup.add(item);

		popup.addSeparator();

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("Options"));
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				OptionsView.getReference().setVisible(true);
			}
		});
		popup.add(item);

		popup.addSeparator();

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("Exit"));
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// WindowsTrayIcon.cleanUp();
				Borg.shutdown();
			}
		});
		popup.add(item);

		TIcon.setPopupMenu(popup);
		TIcon.addActionListener(new OpenListener());

		SystemTray tray = SystemTray.getSystemTray();
		tray.add(TIcon);
	}

}
