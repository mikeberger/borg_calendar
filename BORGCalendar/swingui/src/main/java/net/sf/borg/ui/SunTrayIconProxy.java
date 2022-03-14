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

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Theme;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.popup.ReminderManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.util.Timer;
import java.util.*;
import java.util.logging.Logger;

/** communicates with the new java built-in system tray APIs */
public class SunTrayIconProxy implements Prefs.Listener {

	// action that opens the main view
	static private class OpenListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			UIControl.toFront();
		}
	}

	static private final Logger log = Logger.getLogger("net.sf.borg");

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

	static public void startTrayIcon() {
		// start the system tray icon - or at least attempt to
		// it doesn't run on all OSs and all WMs
		trayIconStarted = true;
		String usetray = Prefs.getPref(PrefName.USESYSTRAY);
		if (!usetray.equals("true")) {
			trayIconStarted = false;
		} else {
			try {
				singleton = new SunTrayIconProxy();
				singleton.init();
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
	 * @throws Exception
	 */
	private void init() throws Exception {

		if (!SystemTray.isSupported())
			throw new Exception("Systray not supported");

		iconSize = SystemTray.getSystemTray().getTrayIconSize().height;

		trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/resource/borg16.jpg")));

		trayIcon.setToolTip("BORG");

		actionMenu.setLabel(Resource.getResourceString("Open"));

		PopupMenu popup = new PopupMenu();

		String fontName = Prefs.getPref(PrefName.DEFFONT);
		if( !fontName.isEmpty())
		{
			Font f = Font.decode(fontName);
			popup.setFont(f);
		}

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
				if (rm != null)
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
				if (rm != null)
					rm.hideAll();
			}

		});
		popup.add(item);

		popup.add(actionMenu);

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

			log.fine("Updating systray image...");

			// get date text
			String text = Integer.toString(new GregorianCalendar()
					.get(Calendar.DATE));

			BufferedImage bimage = new BufferedImage(iconSize, iconSize,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bimage.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  
			
			// draw icon background
			Theme t = Theme.getCurrentTheme();
			g.setColor(new Color(t.getTrayIconBg()));
			g.fillRect(0, 0, iconSize, iconSize);

			// draw date centered
			Font font = Font.decode(Prefs.getPref(PrefName.TRAYFONT));
			g.setFont(font);
			FontMetrics metrics = g.getFontMetrics();
			g.setColor(new Color(t.getTrayIconFg()));
			g.drawString(text, (iconSize - metrics.stringWidth(text)) / 2,
					(iconSize + metrics.getAscent()) / 2);
			g.dispose();

			image = bimage;

			trayIcon.setToolTip("BORG - "
					+ DateFormat.getDateInstance(DateFormat.MEDIUM).format(
							new Date()));

		} else {
			image = Toolkit.getDefaultToolkit().getImage(
					getClass().getResource("/resource/borg16.jpg"));

		}

		trayIcon.setImage(image);

	}

	/** start a timer that updates the date icon */
	private void startRefreshTimer() {

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
		}, 60 * 1000, 15 * 60 * 1000);

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
