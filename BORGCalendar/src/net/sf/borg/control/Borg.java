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
 * Copyright 2003 by Mike Berger
 */

package net.sf.borg.control;

import java.awt.Font;
import java.awt.Frame;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.SocketClient;
import net.sf.borg.common.SocketHandler;
import net.sf.borg.common.SocketServer;
import net.sf.borg.control.socketServer.SingleInstanceHandler;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.OptionsView;
import net.sf.borg.ui.SunTrayIconProxy;
import net.sf.borg.ui.calendar.TodoView;
import net.sf.borg.ui.popup.ReminderPopupManager;
import net.sf.borg.ui.util.Banner;
import net.sf.borg.ui.util.ModalMessage;
import net.sf.borg.ui.util.NwFontChooserS;

/**
 * The Main Class of Borg. It's responsible for starting up the model and
 * spawning various threads, including the main UI thread and various timer
 * threads. It also handles shutdown.
 */
public class Borg implements OptionsView.RestartListener, SocketHandler {

	/** splash window */
	static private Banner ban_ = null; // start up banner

	/** The singleton. */
	static private Borg singleton = null;

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 */
	static public Borg getReference() {
		if (singleton == null)
			singleton = new Borg();
		return (singleton);
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String args[]) {

		// create a new borg object and call its init routing with the command
		// line args
		Borg b = getReference();
		b.init(args);
	}

	/**
	 * Shutdown.close db connections. backup the database if the auto-backup
	 * feature is on.
	 */
	static public void shutdown() {

		// backup data
		String backupdir = Prefs.getPref(PrefName.BACKUPDIR);
		if (backupdir != null && !backupdir.equals("")) {
			try {

				int ret = JOptionPane.showConfirmDialog(null, Resource
						.getResourceString("backup_notice")
						+ " " + backupdir + "?", "BORG",
						JOptionPane.OK_CANCEL_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyyMMddHHmmss");
					String uniq = sdf.format(new Date());
					ZipOutputStream out = new ZipOutputStream(
							new FileOutputStream(backupdir + "/borg" + uniq
									+ ".zip"));
					Writer fw = new OutputStreamWriter(out, "UTF8");

					out.putNextEntry(new ZipEntry("borg.xml"));
					AppointmentModel.getReference().export(fw);
					fw.flush();
					out.closeEntry();

					out.putNextEntry(new ZipEntry("task.xml"));
					TaskModel.getReference().export(fw);
					fw.flush();
					out.closeEntry();

					out.putNextEntry(new ZipEntry("addr.xml"));
					AddressModel.getReference().export(fw);
					fw.flush();
					out.closeEntry();

					if (MemoModel.getReference().hasMemos()) {

						out.putNextEntry(new ZipEntry("memo.xml"));
						MemoModel.getReference().export(fw);
						fw.flush();
						out.closeEntry();
					}

					if (LinkModel.getReference().hasLinks()) {
						out.putNextEntry(new ZipEntry("link.xml"));
						LinkModel.getReference().export(fw);
						fw.flush();
						out.closeEntry();
					}

					out.close();
				}
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}

		}

		// close the db
		try {
			Banner ban = new Banner();
			ban.setText(Resource.getResourceString("shutdown"));
			ban.setVisible(true);
			JdbcDB.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// wait 3 seconds before exiting for the db to settle down - probably
		// being superstitious.
		Timer shutdownTimer = new java.util.Timer();
		shutdownTimer.schedule(new TimerTask() {
			public void run() {
				System.exit(0);
			}
		}, 3 * 1000, 28 * 60 * 1000);

	}

	/**
	 * Sync dbs - mainly clears caches. applies to mysql where outside clients
	 * can update the db.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	static public synchronized void syncDBs() throws Exception {

		AppointmentModel.getReference().sync();
		AddressModel.getReference().sync();
		TaskModel.getReference().sync();

	}

	/** The timer for sending reminder email. */
	private Timer mailTimer_ = null;

	/**
	 * message popped up if the socket thread has something to tell the user.
	 * mainly used to block user activity during a palm sync and report sync
	 * progress
	 */
	private ModalMessage modalMessage = null;

	/**
	 * The socket server - listens for incoming requests such as open requests
	 * and palm sync requests
	 */
	private SocketServer socketServer_ = null;

	/**
	 * The sync timer - controls auto-sync with db - only needed for mysql - and
	 * then not really
	 */
	private java.util.Timer syncTimer_ = null;

	/** flag indicating if a system tray icon is running. */
	private boolean trayIcon = true;

	/**
	 * constructor
	 */
	private Borg() {

	}

	/**
	 * Checks for presence of the tray icon.
	 * 
	 * @return true, if the tray icon started up successfully, false otherwise
	 */
	public boolean hasTrayIcon() {
		return trayIcon;
	}

/** process a socket message. the only messages commonly used are open: when another borg process is started, which
	 * just opens this one, lock: when a palm sync requests a lock on user activity and messages starting with "<",which
	 * are redirected to the remote db server, which is how palm sync talks to the db of the running borg.
	 */
	public synchronized String processMessage(String msg) {
		// System.out.println("Got msg: " + msg);
		if (msg.equals("sync")) {
			try {
				syncDBs();
				return ("sync success");
			} catch (Exception e) {
				e.printStackTrace();
				return ("sync error: " + e.toString());
			}
		} else if (msg.equals("shutdown")) {
			System.exit(0);
		} else if (msg.equals("open")) {
			MultiView.getMainView().toFront();
			MultiView.getMainView().setState(Frame.NORMAL);
			return ("ok");
		} else if (msg.startsWith("lock:")) {
			final String lockmsg = msg.substring(5);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (modalMessage == null || !modalMessage.isShowing()) {
						modalMessage = new ModalMessage(lockmsg, false);
						modalMessage.setVisible(true);
					} else {
						modalMessage.appendText(lockmsg);
					}
					modalMessage.setEnabled(false);
					modalMessage.toFront();
				}
			});

			return ("ok");
		} else if (msg.equals("unlock")) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (modalMessage.isShowing()) {
						modalMessage.setEnabled(true);
					}
				}
			});

			return ("ok");
		} else if (msg.startsWith("<")) {
			return SingleInstanceHandler.execute(msg);
		}
		return ("Unknown msg: " + msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.OptionsView.RestartListener#restart()
	 */
	public void restart() {

		if (syncTimer_ != null)
			syncTimer_.cancel();
		if (mailTimer_ != null)
			mailTimer_.cancel();

		init(new String[0]);
	}

	/**
	 * Initialize the application
	 * 
	 * @param args
	 *            the args
	 */
	private void init(String args[]) {

		// override for testing a different db
		String testdb = null;

		// override for tray icon name
		String trayname = "BORG";

		// testing flag
		boolean testing = false;

		// process command line args
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-trayname")) {
				i++;
				if (i >= args.length) {
					System.out.println("Error: missing trayname argument");
					System.exit(1);
				}
				trayname = args[i];
			} else if (args[i].equals("-db")) {
				i++;
				if (i >= args.length) {
					System.out.println(Resource
							.getResourceString("-db_argument_is_missing"));
					System.exit(1);
				}
				testdb = args[i];
			} else if (args[i].equals("-test")) {
				testing = true;
			}
		}
		
		// open existing BORG if there is one
		int port = Prefs.getIntPref(PrefName.SOCKETPORT);
		if (port != -1 && !testing) {
			String resp;
			try {
				resp = SocketClient.sendMsg("localhost", port, "open");
				if (resp != null && resp.equals("ok")) {
					System.exit(0);
				}
			} catch (IOException e) {

			}

		}

		// redirect stdout and stderr to files
		try {
			if (!testing) {
				String home = System.getProperty("user.home", "");
				FileOutputStream errStr = new FileOutputStream(home
						+ "/.borg.err", false);
				PrintStream printStream = new PrintStream(errStr);
				System.setErr(printStream);
				FileOutputStream outStr = new FileOutputStream(home
						+ "/.borg.out", false);
				printStream = new PrintStream(outStr);
				System.setOut(printStream);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// register as a restart listener. on rare occassions the options window
		// can request an
		// app restart
		OptionsView.setRestartListener(this);

		// check if splash window is enabled
		boolean splash = true;
		String spl = Prefs.getPref(PrefName.SPLASH);
		if (spl.equals("false")) {
			splash = false;
		}

		// default font
		String deffont = Prefs.getPref(PrefName.DEFFONT);
		if (!deffont.equals("")) {
			Font f = Font.decode(deffont);
			NwFontChooserS.setDefaultFont(f);
		}

		// set the look and feel
		String lnf = Prefs.getPref(PrefName.LNF);
		try {
			UIManager.setLookAndFeel(lnf);
			UIManager.getLookAndFeelDefaults().put("ClassLoader",
					getClass().getClassLoader());
		} catch (Exception e) {
			// System.out.println(e.toString());
		}

		// locale
		String country = Prefs.getPref(PrefName.COUNTRY);
		String language = Prefs.getPref(PrefName.LANGUAGE);
		if (!language.equals("")) {
			Locale.setDefault(new Locale(language, country));
		}

		// pop up the splash
		if (splash) {
			ban_ = new Banner();
			ban_.setText(Resource.getResourceString("Initializing"));
			ban_.setVisible(true);
		}

		// db url
		String dbdir = null;

		try {
			if (testdb != null)
				dbdir = testdb;
			else
				dbdir = JdbcDB.buildDbDir(); // derive db url from user prefs

			// if no db set - tell user
			if (dbdir.equals("not-set")) {

				JOptionPane.showMessageDialog(null, Resource
						.getResourceString("selectdb"), Resource
						.getResourceString("Notice"),
						JOptionPane.INFORMATION_MESSAGE);

				if (ban_ != null)
					ban_.dispose();

				// if user wants to set db - let them
				OptionsView.dbSelectOnly();
				return;
			}

			// now all errors can go to popup windows
			Errmsg.console(false); // send errors to screen

			// connect to the db - for now it is jdbc only
			JdbcDB.connect(dbdir);

			if (splash)
				ban_.setText(Resource
						.getResourceString("Loading_Appt_Database"));

			// initialize the appointment model
			AppointmentModel.getReference();

			// init task model & load database
			if (splash)
				ban_.setText(Resource
						.getResourceString("Loading_Task_Database"));

			// init task model
			TaskModel.getReference();

			if (splash)
				ban_.setText(Resource
						.getResourceString("Opening_Address_Database"));

			// init address model
			AddressModel.getReference();

			if (splash)
				ban_.setText(Resource.getResourceString("Opening_Main_Window"));

			// start the UI thread
			final String traynm = trayname;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					swingStart(traynm);
				}
			});

			if (splash)
				ban_.dispose();
			ban_ = null;

			// calculate email time in minutes from now
			Calendar cal = new GregorianCalendar();
			int emailmins = Prefs.getIntPref(PrefName.EMAILTIME);
			int curmins = 60 * cal.get(Calendar.HOUR_OF_DAY)
					+ cal.get(Calendar.MINUTE);
			int mailtime = emailmins - curmins;
			if (mailtime < 0) {
				// we are past mailtime - send it now
				try {
					EmailReminder.sendDailyEmailReminder(null);
				} catch (Exception e) {
					final Exception fe = e;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							Errmsg.errmsg(fe);
						}
					});
				}
				// set timer for next mailtime
				mailtime += 24 * 60; // 24 hours from now
			}

			// start up email check timer - every 24 hours
			mailTimer_ = new java.util.Timer();
			mailTimer_.schedule(new TimerTask() {
				public void run() {
					try {
						EmailReminder.sendDailyEmailReminder(null);
					} catch (Exception e) {
						final Exception fe = e;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								Errmsg.errmsg(fe);
							}
						});
					}
				}
			}, mailtime * 60 * 1000, 24 * 60 * 60 * 1000);

			// start autosync timer
			int syncmins = Prefs.getIntPref(PrefName.SYNCMINS);
			String dbtype = Prefs.getPref(PrefName.DBTYPE);
			if ((dbtype.equals("mysql") || dbtype.equals("jdbc"))
					&& syncmins != 0) {
				syncTimer_ = new java.util.Timer();
				syncTimer_.schedule(new TimerTask() {
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								try {
									syncDBs();
								} catch (Exception e) {
									Errmsg.errmsg(e);
								}
							}
						});
					}
				}, syncmins * 60 * 1000, syncmins * 60 * 1000);
			}

			// start socket listener
			if (port != -1 && socketServer_ == null) {
				socketServer_ = new SocketServer(port, this);
			}

		} catch (Exception e) {
			// if something goes wrong, it might be that the database
			// directory
			// is bad. Maybe
			// it does not exist anymore or something, so give the user a
			// chance
			// to change it
			// if it will fix the problem

			Errmsg.errmsg(e);

			// get rid of NESTED exceptions for SQL exceptions - they make
			// the
			// error window too large
			String es = e.toString();
			int i1 = es.indexOf("** BEGIN NESTED");
			int i2 = es.indexOf("** END NESTED");

			if (i1 != -1 && i2 != -1) {
				int i3 = es.indexOf('\n', i1);
				String newstring = es.substring(0, i3) + "\n-- removed --\n"
						+ es.substring(i2);
				es = newstring;
			}
			es += Resource.getResourceString("db_set_to") + dbdir;
			es += Resource.getResourceString("bad_db_2");

			// prompt for ok
			int ret = JOptionPane
					.showConfirmDialog(null, es, Resource
							.getResourceString("BORG_Error"),
							JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION) {
				if (ban_ != null)
					ban_.dispose();
				OptionsView.dbSelectOnly();
				return;
			}

			System.exit(1);

		}

	}

	/**
	 * show the todo view.
	 */
	private void startTodoView() {

		try {
			// bring up todo window
			TodoView tg = TodoView.getReference();
			MultiView.getMainView().addView(tg);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * swing thread logic
	 * 
	 * @param trayname
	 *            the trayname
	 */
	private void swingStart(String trayname) {

		// start the system tray icon - or at least attempt to
		// it doesn't run on all OSs and all WMs
		trayIcon = true;
		String usetray = Prefs.getPref(PrefName.USESYSTRAY);
		if (!usetray.equals("true")) {
			trayIcon = false;
		} else {
			try {
				SunTrayIconProxy tip = SunTrayIconProxy.getReference();
				tip.init(trayname);
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

		// create popups view
		ReminderPopupManager.getReference();

		// start main month view
		// CalendarView.getReference(trayIcon);
		MultiView mv = MultiView.getMainView();
		mv.setVisible(true);

		// start todo view if there are todos
		if (AppointmentModel.getReference().haveTodos()) {
			startTodoView();
		}

	}

}