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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
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

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.popup.ReminderManager;

/** communicates with the new java built-in system tray APIs */
class SunTrayIconProxy implements Prefs.Listener {

	// action that opens the main view
	private class OpenListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			UIControl.toFront();
		}
	}

	private static final int iconSize = 16;

	// the singleton
	static private SunTrayIconProxy singleton = null;

	/* flag to indicate is a tray icon was started */
	private static boolean trayIconStarted = false;

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

		trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/resource/borg16.jpg")));

		trayIcon.setToolTip(trayname);
		PopupMenu popup = new PopupMenu();

		MenuItem item = new MenuItem();
		item.setLabel(Resource.getResourceString("Open_Calendar"));
		item.addActionListener(new OpenListener());
		popup.add(item);

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("Show_Pops"));

		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ReminderManager rm = ReminderManager.getReminderManager();
				if( rm != null )
					rm.showAll();
			}

		});
		popup.add(item);

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("Hide_Pops"));

		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ReminderManager rm = ReminderManager.getReminderManager();
				if( rm != null )
					rm.hideAll();
			}

		});
		popup.add(item);

		item = new MenuItem();
		item.setLabel(Resource.getResourceString("Options"));
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsView.getReference().setVisible(true);
			}
		});
		popup.add(item);
		
		popup.addSeparator();
		
		item = new MenuItem();
		item.setLabel(Resource.getResourceString("About"));
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MainMenu.AboutMIActionPerformed();
			}
		});
		popup.add(item);

		popup.addSeparator();

		item = new MenuItem();
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

		updateImage();

		Prefs.addListener(this);
		
		startRefreshTimer();
	}

	@Override
	public void prefsChanged() {
		updateImage();

	}

	/**
	 * set the icon image to the current date or a fixed icon with a B on it
	 * depending on user preference.
	 */
	public void updateImage() {
		Image image = null;
		if (Prefs.getBoolPref(PrefName.SYSTRAYDATE)) {

			// get date text
			String text = Integer.toString(new GregorianCalendar()
					.get(Calendar.DATE));

			BufferedImage bimage = new BufferedImage(iconSize, iconSize,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bimage.createGraphics();

			// draw icon background
			g.setColor(Color.white);
			g.fillRect(0, 0, iconSize, iconSize);

			// draw date centered
			Font font = new Font("Monospaced", Font.BOLD, 12);
			g.setFont(font);
			FontMetrics metrics = g.getFontMetrics();
			g.setColor(new Color(0, 0, 153));
			g.drawString(text, (iconSize - metrics.stringWidth(text)) / 2,
					(iconSize + metrics.getAscent()) / 2);
			g.dispose();

			image = bimage;

			trayIcon.setToolTip(DateFormat.getDateInstance(DateFormat.MEDIUM)
					.format(new Date()));

		} else {
			image = Toolkit.getDefaultToolkit().getImage(
					getClass().getResource("/resource/borg16.jpg"));

		}

		trayIcon.setImage(image);

	}

	/** start a timer that updates the date icon each day */
	private void startRefreshTimer() {
		
		Calendar cal = new GregorianCalendar();
		int curmins = 60 * cal.get(Calendar.HOUR_OF_DAY)
				+ cal.get(Calendar.MINUTE);
		int midnight = 1440 - curmins;

		Timer updatetimer = new Timer("SysTrayTimer");
		updatetimer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateImage();
					}
				});
			}
		}, midnight * 60 * 1000, 24 * 60 * 60 * 1000);

	}

}
