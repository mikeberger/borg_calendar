package net.sf.borg.ui;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JOptionPane;
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
					System
							.setProperty("Plastic.defaultTheme",
									"ExperienceBlue");
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
		if( Prefs.getBoolPref(PrefName.REMINDERLIST) )
			ReminderListManager.getReference();
		else
			ReminderPopupManager.getReference();

		// create the main window
		MultiView mv = MultiView.getMainView();

		// load the UI modules into the main window
		Calendar cal_ = new GregorianCalendar();
		mv.addModule(new MonthPanel(cal_.get(Calendar.MONTH), cal_
				.get(Calendar.YEAR)));
		mv.addModule(new WeekPanel(cal_.get(Calendar.MONTH), cal_
				.get(Calendar.YEAR), cal_.get(Calendar.DATE)));
		mv.addModule(new DayPanel(cal_.get(Calendar.MONTH), cal_
				.get(Calendar.YEAR), cal_.get(Calendar.DATE)));
		mv.addModule(new YearPanel(cal_.get(Calendar.YEAR)));
		mv.addModule(AddrListView.getReference());
		mv.addModule(TodoView.getReference());
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

		addExternalModule("net.sf.borg.plugin.reports.ReportModule");
		addExternalModule("net.sf.borg.plugin.ical.IcalModule");
		addExternalModule("net.sf.borg.plugin.sync.SyncModule");

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
		// stop popup timer and destroy popups
		ReminderManager rm = ReminderManager.getReminderManager();
		if( rm != null )
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
		String backupdir = Prefs.getPref(PrefName.BACKUPDIR);
		if (backupdir != null && !backupdir.equals("")) {
			try {

				int ret = JOptionPane.showConfirmDialog(null, Resource
						.getResourceString("backup_notice")
						+ " " + backupdir + "?", "BORG",
						JOptionPane.OK_CANCEL_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					ExportImport.exportToZip(backupdir);
				}
			} catch (Exception e) {
				Errmsg.errmsg(e);
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
			//e.printStackTrace();
		}
	}

}
