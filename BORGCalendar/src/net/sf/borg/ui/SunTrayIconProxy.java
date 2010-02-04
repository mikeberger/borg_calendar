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
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.popup.ReminderPopupManager;

/** communicates with the new java built-in system tray APIs */
class SunTrayIconProxy {

	// action that opens the main view
	private class OpenListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			UIControl.toFront();
		}
	}

	// the singleton
	static private SunTrayIconProxy singleton = null;
	
	/* flag to indicate is a tray icon was started */
	private static boolean trayIcon = false;
	

	static public void startTrayIcon(String trayname)
	{
		// start the system tray icon - or at least attempt to
		// it doesn't run on all OSs and all WMs
		trayIcon = true;
		String usetray = Prefs.getPref(PrefName.USESYSTRAY);
		if (!usetray.equals("true")) {
			trayIcon = false;
		} else {
			try {
				singleton = new SunTrayIconProxy();
				singleton.init(trayname);
			} catch (UnsatisfiedLinkError le) {
				le.printStackTrace();
				trayIcon = false;
			} catch (NoClassDefFoundError ncf) {
				ncf.printStackTrace();
				trayIcon = false;
			} catch (Exception e) {
				e.printStackTrace();
				trayIcon = false;
			}
		}
		
	}
	

	/**
	 * initalize the system tray
	 * @param trayname the tray name (when the user hovers over the tray icon)
	 * @throws Exception
	 */
	private void init(String trayname) throws Exception {
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
				MultiView.getMainView().setView(ViewType.ADDRESS);
				MultiView.getMainView().setVisible(true);
			}
		});
		popup.add(item);

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("To_Do_List"));
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				MultiView.getMainView().setView(ViewType.TODO);
				MultiView.getMainView().setVisible(true);
			}
		});
		popup.add(item);

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("Show_Pops"));

		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				ReminderPopupManager.getReference().showAll();
			}

		});
		popup.add(item);

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("Hide_Pops"));

		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				ReminderPopupManager.getReference().hideAll();
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
				UIControl.shutDownUI();
			}
		});
		popup.add(item);

		TIcon.setPopupMenu(popup);
		TIcon.addActionListener(new OpenListener());

		SystemTray tray = SystemTray.getSystemTray();
		tray.add(TIcon);
	}
	
	/**
	 * Checks for presence of the tray icon.
	 * 
	 * @return true, if the tray icon started up successfully, false otherwise
	 */
	public static boolean hasTrayIcon()
	{
		return trayIcon;
	}

}
