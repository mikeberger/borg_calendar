package net.sf.borg.ui;

import net.sf.borg.common.*;
import net.sf.borg.model.ExportImport;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.sync.SyncLog;
import net.sf.borg.ui.address.AddrListView;
import net.sf.borg.ui.calendar.*;
import net.sf.borg.ui.checklist.CheckListPanel;
import net.sf.borg.ui.memo.MemoPanel;
import net.sf.borg.ui.options.MiscellaneousOptionsPanel.SHUTDOWN_ACTION;
import net.sf.borg.ui.popup.ReminderListManager;
import net.sf.borg.ui.popup.ReminderManager;
import net.sf.borg.ui.popup.ReminderPopupManager;
import net.sf.borg.ui.task.TaskModule;
import net.sf.borg.ui.util.NwFontChooserS;
import net.sf.borg.ui.util.SplashScreen;
import net.sf.borg.ui.util.UIErrorHandler;

import javax.swing.*;
import java.awt.Dialog.ModalityType;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * Class UIControl provides access to the UI from non-UI classes. UIControl
 * provides the main UI entry point.
 *
 *
 */

public class UIControl {

	static private final Logger log = Logger.getLogger("net.sf.borg");

	private static Observer shutdownListener = null;

	/**
	 * set a shutdown listener to be called back when the UI shuts down
	 *
	 * @param shutdownListener
	 */
	public static void setShutdownListener(Observer shutdownListener) {
		UIControl.shutdownListener = shutdownListener;
	}

	/**
	 * splash screen
	 */
	private static SplashScreen splashScreen = null;

	/**
	 * Main UI initialization.
	 *
	*/
	public static void startUI() {

		Errmsg.setErrorHandler(new UIErrorHandler());

		// check database timestamp
		try {
			DBHelper.checkTimestamp();
		} catch (Warning e1) {
			Errmsg.getErrorHandler().notice(e1.getMessage());
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

		// default font
		String deffont = Prefs.getPref(PrefName.DEFFONT);
		if (!deffont.equals("")) {
			Font f = Font.decode(deffont);
			NwFontChooserS.setDefaultFont(f);
		}
		//JGoodies Plastic L&F does not support the mac cmd key,
		//If the user is running OS X, go to default Java L&F instead
		String os = System.getProperty("os.name").toLowerCase();
		String lnf = Prefs.getPref(PrefName.LNF);

		if(os.contains("mac") && lnf.contains("jgoodies")) {
			//Do nothing.
		} else {
			// set the look and feel
			try {

				// set default jgoodies theme
				if (lnf.contains("jgoodies")) {
					String theme = System.getProperty("Plastic.defaultTheme");
					if (theme == null) {
						System.setProperty("Plastic.defaultTheme",
								Prefs.getPref(PrefName.GOODIESTHEME));
					}
				}

				UIManager.setLookAndFeel(lnf);
				UIManager.getLookAndFeelDefaults().put("ClassLoader",
						UIControl.class.getClassLoader());
			} catch (Exception e) {
				log.severe(e.toString());
			}
		}

		// pop up the splash if the option is set
		if (Prefs.getBoolPref(PrefName.SPLASH)) {
			splashScreen = new SplashScreen(ModalityType.MODELESS);
			splashScreen.setText(Resource.getResourceString("Initializing"));
			splashScreen.setVisible(true);

			/*
			 * in order for the splash to be seen, we will complete
			 * initialization later (in the swing thread).
			 */
			Timer t = new Timer(3000, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					completeUIInitialization();
				}
			});
			t.setRepeats(false);
			t.start();
		} else
			completeUIInitialization();

	}

	/**
	 * complete the parts of the UI initialization that run after the splash
	 * screen has shown for a while
	 *
	 */
	private static void completeUIInitialization() {

		// tray icon
		SunTrayIconProxy.startTrayIcon();

		// create reminder manager
		if (Prefs.getBoolPref(PrefName.REMINDERLIST))
			ReminderListManager.getReference();
		else
			ReminderPopupManager.getReference();

		// create the main window
		MultiView mv = MultiView.getMainView();

		// load the UI modules into the main window
		mv.addModule(new MonthPanel());
		mv.addModule(new WeekPanel());
		mv.addModule(new DayPanel());
		mv.addModule(new YearPanel());
		mv.addModule(new AddrListView());
		mv.addModule(new TodoView());
		mv.addModule(new TaskModule());
		mv.addModule(new MemoPanel());
		mv.addModule(new CheckListPanel());
		mv.addModule(new SearchView());
		mv.addModule(new InfoView("/resource/RELEASE_NOTES.txt", Resource
				.getResourceString("rlsnotes")));
		mv.addModule(new InfoView("/resource/CHANGES.txt", Resource
				.getResourceString("viewchglog")));
		mv.addModule(new InfoView("/resource/borglicense.txt", Resource
				.getResourceString("License")));
		mv.addModule(new FileView(System.getProperty("user.home", "")
				+ "/.borg.log", Resource.getResourceString("view_log")));
		mv.addModule(new SyncModule());


		// allow start to system tray if option set and there is a system tray
		boolean bgStart = Prefs.getBoolPref(PrefName.BACKGSTART)
				&& SunTrayIconProxy.hasTrayIcon();

		// make the main window visible
		if (!bgStart) {
			mv.setVisible(true);
		}

		// open all views that should be shown at startup
		mv.startupViews(bgStart);

		// destroy the splash screen
		if (splashScreen != null) {
			splashScreen.dispose();
			splashScreen = null;
		}
	}

	/**
	 * raise the UI to the front
	 */
	public static void toFront() {
		MultiView.getMainView().setVisible(true);
		MultiView.getMainView().toFront();
		MultiView.getMainView().setState(Frame.NORMAL);
	}

	/**
	 * shuts down the UI, including db backup
	 */
	public static void shutDownUI() {

		// warn about syncing
		try {
			if (SyncLog.getReference().isProcessUpdates()
					&& !SyncLog.getReference().getAll().isEmpty()) {
				int res = JOptionPane.showConfirmDialog(null,
						Resource.getResourceString("Sync-Warn"), null,
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);

				if (res != JOptionPane.YES_OPTION) {
					return;
				}

			}
		} catch (Exception e1) {
			Errmsg.getErrorHandler().errmsg(e1);
		}

		// prompt for shutdown and backup options
		boolean do_backup = false;
		final String backupdir = Prefs.getPref(PrefName.BACKUPDIR);

		if (backupdir != null && !backupdir.equals("")) {

			String shutdown_action = Prefs.getPref(PrefName.SHUTDOWN_ACTION);
			if (shutdown_action.isEmpty()
					|| SHUTDOWN_ACTION.PROMPT.toString()
							.equals(shutdown_action)) {
				JCheckBox b1 = new JCheckBox(
						Resource.getResourceString("backup_notice") + " "
								+ backupdir);
				

				Object[] array = { b1 };

				int res = JOptionPane.showConfirmDialog(null, array,
						Resource.getResourceString("shutdown_options"),
						JOptionPane.OK_CANCEL_OPTION);

				if (res != JOptionPane.YES_OPTION) {
					return;
				}

				if (b1.isSelected() )
					do_backup = true;
				
				
			} else if (SHUTDOWN_ACTION.BACKUP.toString()
					.equals(shutdown_action)) {
				do_backup = true;
			}
		}

		if (do_backup == true) {
			File f = new File(backupdir);

			// verify backup dir
			if (!f.exists()) {
				try {
					f.mkdir();
				} catch (Exception e) {
					// handle the error below
				}
			}
			if (!f.isDirectory() || !f.canWrite()) {
				String msg = MessageFormat.format(Resource.getResourceString("backup_dir_error"), backupdir);
				Errmsg.getErrorHandler().notice(msg);
			}

		}

		// stop popup timer and destroy popups
		ReminderManager rm = ReminderManager.getReminderManager();
		if (rm != null)
			rm.remove();

		new NonUIShutdown(do_backup).execute();

		// show a splash screen for shutdown which locks the UI
		try {
			SplashScreen ban = new SplashScreen(ModalityType.APPLICATION_MODAL);
			ban.setText(Resource.getResourceString("shutdown"));
			ban.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static private class NonUIShutdown extends SwingWorker<Object, Object> {

		private boolean do_backup;

		public NonUIShutdown(boolean b) {
			do_backup = b;
		}

		@Override
		protected Object doInBackground() throws Exception {
			// backup data
			if (do_backup == true) {
				try {
					final String backupdir = Prefs.getPref(PrefName.BACKUPDIR);
					ExportImport.exportToZip(backupdir, false);
					log.info("Export to ZIP Complete");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}


			// non-UI shutdown
			if (shutdownListener != null)
				shutdownListener.update(null, null);
			return null;
		}

	}

	
}
