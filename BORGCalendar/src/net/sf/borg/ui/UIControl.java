package net.sf.borg.ui;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.Timer;
import javax.swing.UIManager;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.control.Borg;
import net.sf.borg.model.ExportImport;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.address.AddrListView;
import net.sf.borg.ui.calendar.DayPanel;
import net.sf.borg.ui.calendar.MonthPanel;
import net.sf.borg.ui.calendar.TodoView;
import net.sf.borg.ui.calendar.WeekPanel;
import net.sf.borg.ui.calendar.YearPanel;
import net.sf.borg.ui.checklist.CheckListPanel;
import net.sf.borg.ui.memo.MemoPanel;
import net.sf.borg.ui.options.MiscellaneousOptionsPanel.SHUTDOWN_ACTION;
import net.sf.borg.ui.popup.ReminderListManager;
import net.sf.borg.ui.popup.ReminderManager;
import net.sf.borg.ui.popup.ReminderPopupManager;
import net.sf.borg.ui.task.TaskModule;
import net.sf.borg.ui.util.NwFontChooserS;
import net.sf.borg.ui.util.SplashScreen;

/**
 * Class UIControl provides access to the UI from non-UI classes. UIControl
 * provides the main UI entry point.
 * 
 * 
 */
public class UIControl {

	/**
	 * splash screen
	 */
	private static SplashScreen splashScreen = null;

	/**
	 * Main UI initialization.
	 * 
	 * @param trayname
	 *            - name for the tray icon
	 */
	public static void startUI(String trayname) {

		// default font
		String deffont = Prefs.getPref(PrefName.DEFFONT);
		if (!deffont.equals("")) {
			Font f = Font.decode(deffont);
			NwFontChooserS.setDefaultFont(f);
		}

		// set the look and feel
		String lnf = Prefs.getPref(PrefName.LNF);
		try {

			// set default jgoodies theme
			if (lnf.contains("jgoodies")) {
				String theme = System.getProperty("Plastic.defaultTheme");
				if (theme == null) {
					System.setProperty("Plastic.defaultTheme", "ExperienceBlue");
				}
			}

			UIManager.setLookAndFeel(lnf);
			UIManager.getLookAndFeelDefaults().put("ClassLoader",
					UIControl.class.getClassLoader());
		} catch (Exception e) {
			// System.out.println(e.toString());
		}

		// pop up the splash if the option is set
		if (Prefs.getBoolPref(PrefName.SPLASH)) {
			splashScreen = new SplashScreen();
			splashScreen.setText(Resource.getResourceString("Initializing"));
			splashScreen.setVisible(true);
			final String tn = trayname;

			/*
			 * in order for the splash to be seen, we will complete
			 * initialization later (in the swing thread).
			 */
			Timer t = new Timer(3000, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					completeUIInitialization(tn);
				}
			});
			t.setRepeats(false);
			t.start();
		} else
			completeUIInitialization(trayname);

	}

	/**
	 * complete the parts of the UI initialization that run after the splash
	 * screen has shown for a while
	 * 
	 * @param trayname
	 *            name for the tray icon
	 */
	private static void completeUIInitialization(String trayname) {

		// tray icon
		SunTrayIconProxy.startTrayIcon(trayname);

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
		mv.addModule(new InfoView("/resource/license.htm", Resource
				.getResourceString("License")));

		if (Prefs.getBoolPref(PrefName.DYNAMIC_LOADING) == true) {
			addExternalModule("net.sf.borg.plugin.reports.ReportModule");
			addExternalModule("net.sf.borg.plugin.ical.IcalModule");
			addExternalModule("net.sf.borg.plugin.sync.SyncModule");
		}

		// make the main window visible
		if (!Prefs.getBoolPref(PrefName.BACKGSTART)
				|| !SunTrayIconProxy.hasTrayIcon())
			mv.setVisible(true);

		// show the month view
	    mv.setView(ViewType.MONTH);

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

		// prompt for shutdown and backup options
		boolean do_backup = false;
		boolean backup_email = false;
		final String backupdir = Prefs.getPref(PrefName.BACKUPDIR);

		if (backupdir != null && !backupdir.equals("")) {

			String shutdown_action = Prefs.getPref(PrefName.SHUTDOWN_ACTION);
			if (shutdown_action.isEmpty()
					|| SHUTDOWN_ACTION.PROMPT.toString()
							.equals(shutdown_action)) {
				JRadioButton b1 = new JRadioButton(
						Resource.getResourceString("backup_notice") + " "
								+ backupdir);
				JRadioButton b2 = new JRadioButton(
						Resource.getResourceString("exit_no_backup"));
				JRadioButton b3 = new JRadioButton(
						Resource.getResourceString("dont_exit"));
				JRadioButton b4 = new JRadioButton(
						Resource.getResourceString("backup_with_email"));

				b1.setSelected(true);

				ButtonGroup group = new ButtonGroup();
				group.add(b1);
				group.add(b2);
				group.add(b3);
				group.add(b4);

				Object[] array = { b1, b4, b2, b3, };

				int res = JOptionPane.showConfirmDialog(null, array,
						Resource.getResourceString("shutdown_options"),
						JOptionPane.OK_CANCEL_OPTION);

				if (res != JOptionPane.YES_OPTION) {
					return;
				}

				if (b3.isSelected())
					return;
				if (b1.isSelected() || b4.isSelected())
					do_backup = true;
				if (b4.isSelected())
					backup_email = true;
			} else if (SHUTDOWN_ACTION.BACKUP.toString()
					.equals(shutdown_action)) {
				do_backup = true;
			} else if (SHUTDOWN_ACTION.EMAIL.toString().equals(shutdown_action)) {
				do_backup = true;
				backup_email = true;
			}

		}

		// stop popup timer and destroy popups
		ReminderManager rm = ReminderManager.getReminderManager();
		if (rm != null)
			rm.remove();

		// show a splash screen for shutdown
		try {
			SplashScreen ban = new SplashScreen();
			ban.setText(Resource.getResourceString("shutdown"));
			ban.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// backup data
		if (do_backup == true) {
			try {
				ExportImport.exportToZip(backupdir, backup_email);
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}
		}

		// non-UI shutdown
		Borg.shutdown();

	}

	/**
	 * load and add a module that is found on the classpath
	 * 
	 * @param className
	 *            - the name of the module class
	 */
	private static void addExternalModule(String className) {
		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			Class<?> clazz = cl.loadClass(className);
			MultiView.Module module = (MultiView.Module) clazz.newInstance();
			MultiView.getMainView().addModule(module);
		} catch (Exception e) {
			System.out.println(e.toString());
			// e.printStackTrace();
		}
	}

}
