package net.sf.borg.ui;

import java.awt.event.ActionListener;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

public class TrayIconProxy {

	static private TrayIconProxyI singleton = null;

	public static TrayIconProxyI getReference() {
		return singleton;
	}

	/**
	 * Checks for presence of the tray icon.
	 * 
	 * @return true, if the tray icon started up successfully, false otherwise
	 */
	public static boolean hasTrayIcon() {
		return trayIconStarted;
	}

	/* flag to indicate is a tray icon was started */
	private static boolean trayIconStarted = false;

	public static void startTrayIcon() {

		boolean isWindows = System.getProperty("os.name").startsWith("Windows");

		// start the system tray icon - or at least attempt to
		// it doesn't run on all OSs and all WMs
		trayIconStarted = true;
		String usetray = Prefs.getPref(PrefName.USESYSTRAY);
		if (!usetray.equals("true")) {
			trayIconStarted = false;
		} else {
			try {
				boolean forcedork = Prefs.getBoolPref(PrefName.FORCEDORKTRAY);

				if (isWindows && !forcedork)
					singleton = new SunTrayIconProxy();
				else
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

	public static void updateImage() {

		if (!trayIconStarted)
			return;

		singleton.updateImage();

	}

	public static void addAction(String text, ActionListener action) {
		if (!trayIconStarted)
			return;

		singleton.addAction(text, action);
	}

	public interface TrayIconProxyI {

		void init() throws Exception;

		/**
		 * set the icon image to the current date or a fixed icon with a B on it
		 * depending on user preference.
		 */
		void updateImage();

		/**
		 * Add an action to the action menu
		 * 
		 * @param text   the text for the menu item
		 * @param action the action listener for the menu item
		 */

		void addAction(String text, ActionListener action);

	}

}