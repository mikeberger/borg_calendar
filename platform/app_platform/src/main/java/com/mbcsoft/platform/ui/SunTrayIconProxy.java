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
package com.mbcsoft.platform.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import com.mbcsoft.platform.common.PrefName;
import com.mbcsoft.platform.common.Prefs;
import com.mbcsoft.platform.common.Resource;

/** communicates with the new java built-in system tray APIs */
public class SunTrayIconProxy  {

	// action that opens the main view
	static private class OpenListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			UIControl.toFront();
		}
	}

	private static int iconSize = 16;

	// the singleton
	static private SunTrayIconProxy singleton = null;

	public static SunTrayIconProxy getReference() {
		return singleton;
	}

	/* flag to indicate is a tray icon was started */
	private static boolean trayIconStarted = false;

	static private Menu actionMenu = new Menu();

	/**
	 * Checks for presence of the tray icon.
	 * 
	 * @return true, if the tray icon started up successfully, false otherwise
	 */
	public static boolean hasTrayIcon() {
		return trayIconStarted;
	}

	static public void startTrayIcon(String trayname) {
		// start the system tray icon - or at least attempt to
		// it doesn't run on all OSs and all WMs
		trayIconStarted = true;
		String usetray = Prefs.getPref(PrefName.USESYSTRAY);
		if (!usetray.equals("true")) {
			trayIconStarted = false;
		} else {
			try {
				singleton = new SunTrayIconProxy();
				singleton.init(trayname);
			} catch (UnsatisfiedLinkError le) {
				le.printStackTrace();
				trayIconStarted = false;
			} catch (NoClassDefFoundError ncf) {
				ncf.printStackTrace();
				trayIconStarted = false;
			} catch (Exception e) {
				e.printStackTrace();
				trayIconStarted = false;
			}
		}

	}

	/** the TrayIcon */
	private TrayIcon trayIcon = null;

	/**
	 * initalize the system tray
	 * 
	 * @param trayname
	 *            the tray name (when the user hovers over the tray icon)
	 * @throws Exception
	 */
	private void init(String trayname) throws Exception {

		if (!SystemTray.isSupported())
			throw new Exception("Systray not supported");

		iconSize = SystemTray.getSystemTray().getTrayIconSize().height;

		trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/borg16.jpg")));

		trayIcon.setToolTip(trayname);

		actionMenu.setLabel(Resource.getResourceString("Open"));

		PopupMenu popup = new PopupMenu();

		String fontName = Prefs.getPref(PrefName.DEFFONT);
		if (!fontName.isEmpty()) {
			Font f = Font.decode(fontName);
			popup.setFont(f);
		}

		popup.add(actionMenu);

		popup.addSeparator();

		MenuItem item = new MenuItem();
		item.setLabel(Resource.getResourceString("Exit"));
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				UIControl.shutDownUI();
			}
		});
		popup.add(item);

		trayIcon.setPopupMenu(popup);
		trayIcon.addActionListener(new OpenListener());

		SystemTray tray = SystemTray.getSystemTray();
		tray.add(trayIcon);

		
	}

	
	

	/**
	 * Add an action to the action menu
	 * 
	 * @param text
	 *            the text for the menu item
	 * @param action
	 *            the action listener for the menu item
	 */
	static public void addAction(String text, ActionListener action) {
		MenuItem item = new MenuItem();
		item.setLabel(text);
		item.addActionListener(action);

		actionMenu.add(item);
	}

}
