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
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Theme;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.popup.ReminderManager;

/** communicates with the new java built-in system tray APIs */
public class DorkTrayIconProxy implements Prefs.Listener {

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
	static private DorkTrayIconProxy singleton = null;

	public static DorkTrayIconProxy getReference() {
		return singleton;
	}

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

	static public void startTrayIcon() {
		// start the system tray icon - or at least attempt to
		// it doesn't run on all OSs and all WMs
		trayIconStarted = true;
		String usetray = Prefs.getPref(PrefName.USESYSTRAY);
		if (!usetray.equals("true")) {
			trayIconStarted = false;
		} else {
			try {
				singleton = new DorkTrayIconProxy();
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

	/**
	 * initalize the system tray
	 *
	 * @throws Exception
	 */
	private void init() throws Exception {

		SystemTray systemTray = SystemTray.get();
		if (systemTray == null) {
			throw new RuntimeException("Unable to load SystemTray!");
		}

		systemTray.setImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/borg16.jpg")));
		//systemTray.setStatus("Not Running");

		addAction(Resource.getResourceString("Open_Calendar"), new OpenListener());
		addAction(Resource.getResourceString("Show_Pops"), new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ReminderManager rm = ReminderManager.getReminderManager();
				if (rm != null)
					rm.showAll();
			}

		});

		addAction(Resource.getResourceString("Hide_Pops"), new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ReminderManager rm = ReminderManager.getReminderManager();
				if (rm != null)
					rm.hideAll();
			}

		});

		addAction(Resource.getResourceString("Options"), new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsView.getReference().setVisible(true);
			}
		});

		addAction(Resource.getResourceString("About"), new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MainMenu.AboutMIActionPerformed();
			}
		});
		
		addAction(Resource.getResourceString("Exit"), new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				UIControl.shutDownUI();
			}
		});
		
		iconSize = systemTray.getTrayImageSize();
		
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
			String text = Integer.toString(new GregorianCalendar().get(Calendar.DATE));

			BufferedImage bimage = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bimage.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			// draw icon background
			Theme t = Theme.getCurrentTheme();
			g.setColor(new Color(t.getTrayIconBg()));
			g.fillRect(0, 0, iconSize, iconSize);

			// draw date centered
			Font font = Font.decode(Prefs.getPref(PrefName.TRAYFONT));
			g.setFont(font);
			FontMetrics metrics = g.getFontMetrics();
			g.setColor(new Color(t.getTrayIconFg()));
			g.drawString(text, (iconSize - metrics.stringWidth(text)) / 2, (iconSize + metrics.getAscent()) / 2);
			g.dispose();

			image = bimage;
			
			SystemTray.get().setTooltip("BORG - "
			+ DateFormat.getDateInstance(DateFormat.MEDIUM).format(
					new Date()));


		} else {
			image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/borg16.jpg"));

		}

		SystemTray.get().setImage(image);

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
	 * @param text   the text for the menu item
	 * @param action the action listener for the menu item
	 */
	static public void addAction(String text, ActionListener action) {

		SystemTray systemTray = SystemTray.get();
		systemTray.getMenu().add(new MenuItem(text, action));
	}

	private void sendNotification(String title, String text) {
		//trayIcon.displayMessage(title, text, MessageType.INFO);
	}

	static public void displayNotification(String title, String text) {
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				DorkTrayIconProxy.getReference().sendNotification(title, text);
//			}
//		});
	}

}
