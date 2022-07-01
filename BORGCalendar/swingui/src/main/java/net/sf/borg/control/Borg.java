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

import net.sf.borg.common.*;
import net.sf.borg.model.*;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.ui.UIControl;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.util.ScrolledDialog;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Timer;
import java.util.*;
import java.util.logging.*;

/**
 * The Main Class of Borg. It's responsible for starting up the model and
 * spawning various threads, including the main UI thread and various timer
 * threads. It also handles shutdown.
 */

public class Borg implements SocketServer.SocketHandler {

	/** The singleton. */
	static volatile private Borg singleton = null;

	static private final Logger log = Logger.getLogger("net.sf.borg");
	static private final Logger auditLog = Logger.getLogger("net.sf.borg.audit");

	/**
	 * Gets the singleton.
	 *
	 * @return the singleton
	 */
	static public Borg getReference() {
		if (singleton == null) {
			Borg b = new Borg();
			singleton = b;
		}
		return (singleton);
	}

	
	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		// create a new borg object and call its init routine with the command
		// line args
		Borg b = getReference();
		b.init(args);
	}

	/**
	 * close db connections and end the program
	 */
	static public void shutdown() {

		if (getReference().mailTimer_ != null)
			getReference().mailTimer_.cancel();

		try {
			// close the db
			DBHelper.getController().close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// wait 3 seconds before exiting for the db to settle down - probably
		// being superstitious.
		Timer shutdownTimer = new java.util.Timer("ShutdownTimer");
		shutdownTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.exit(0);
			}
		}, 3 * 1000, 28 * 60 * 1000);

	}

	/** The timer for sending reminder email. */
	private Timer mailTimer_ = null;
	

	/**
	 * The socket server - listens for incoming requests such as open requests
	 */
	private SocketServer socketServer_ = null;

	/**
	 * constructor
	 */
	private Borg() {
		// empty
	}

	/**
	 * process a socket message
	 */
	@Override
	public synchronized String processSocketMessage(String msg) {
		log.fine("Got msg: " + msg);
		if (msg.equals("open")) {
			UIControl.toFront();
			return ("ok");
		}
		return ("Unknown msg: " + msg);
	}

	/**
	 * Initialize the application
	 *
	 * @param args
	 *            the args
	 */
	private void init(String[] args) {

		// override for testing a different db
		String testdb = null;

		// testing flag
		boolean testing = false;

		// process command line args
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-db")) {
				i++;
				if (i >= args.length) {
					System.out.println(Resource.getResourceString("-db_argument_is_missing"));
					System.exit(1);
				}
				testdb = args[i];
			} else if (args[i].equals("-test")) {
				testing = true;
			}

		}

		// if testing, use alternate prefs so regular prefs can be left alone
		if (testing)
			Prefs.setPrefRootNode("net/sf/borg/test");

		// logging
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		log.addHandler(ch);
		log.setUseParentHandlers(false);

		boolean debug = Prefs.getBoolPref(PrefName.DEBUG);
		if (debug == true)
			log.setLevel(Level.ALL);
		else
			log.setLevel(Level.INFO);

		log.fine("Debug logging turned on");

		// open existing BORG if there is one
		int port = Prefs.getIntPref(PrefName.SOCKETPORT);
		if (port != -1 && !testing) {
			String resp;
			try {
				resp = SocketClient.sendMsg("localhost", port, "open");
				if (resp != null && resp.equals("ok")) {
					// if we found a running borg to open, then exit
					System.exit(0);
				}
			} catch (IOException e) {
				// empty
			}

		}

		// redirect stdout and stderr to files
		try {
			if (!testing) {
				String home = System.getProperty("user.home", "");
				FileOutputStream errStr = new FileOutputStream(home + "/.borg.out", false);
				PrintStream printStream = new PrintStream(errStr);
				System.setErr(printStream);
				System.setOut(printStream);

				FileHandler fh = new FileHandler("%h/.borg.log");
				fh.setFormatter(new SimpleFormatter());
				fh.setLevel(ch.getLevel());
				log.removeHandler(ch);
				log.addHandler(fh);

				FileHandler fh2 = new FileHandler("%h/.borgaudit.log", 1024*1024, 5, true );
				fh2.setFormatter(new SimpleFormatter());
				fh2.setLevel(Level.INFO);
				auditLog.addHandler(fh2);

				if( Prefs.getBoolPref(PrefName.AUDITLOG))
					auditLog.setLevel(Level.INFO);
				else
					auditLog.setLevel(Level.SEVERE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		// locale
		String country = Prefs.getPref(PrefName.COUNTRY);
		String language = Prefs.getPref(PrefName.LANGUAGE);
		if (!language.equals("")) {
			Locale.setDefault(new Locale(language, country));
		}

		// JDBC is only choice for now. In the future, set this based on DBType
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());

		// db url
		String dbdir = null;

		try {
			if (testdb != null)
				dbdir = testdb;
			else
				dbdir = DBHelper.getController().buildURL(); // derive db url
																// from user
																// prefs

			if (dbdir.equals("not-set")) {

				// try to set a valid default
				String home = System.getProperty("user.home", "");
				File borgdir = null;

				// on MAC OS, store borg database into
				// $HOME/Library/BorgCalendar
				String os = System.getProperty("os.name").toLowerCase();
				if (os.indexOf("mac") != -1) {
					borgdir = new File(home, "Library/BorgCalendar");
				}
				// Default:, store as hidden folder inside $HOME
				else {
					borgdir = new File(home + "/.borg_db");
				}
				if (!borgdir.exists()) {
					borgdir.mkdir();
				}
				if (borgdir.isDirectory() && borgdir.canWrite()) {
					Prefs.putPref(PrefName.H2DIR, borgdir.getAbsolutePath());
					Prefs.putPref(PrefName.DBTYPE, "h2");
					dbdir = DBHelper.getController().buildURL();
				} else {
					JOptionPane.showMessageDialog(null, Resource.getResourceString("selectdb"),
							Resource.getResourceString("Notice"), JOptionPane.INFORMATION_MESSAGE);

					// if user wants to set db - let them
					OptionsView.dbSelectOnly();
					return;
				}
			}

			// connect to the db - for now, it is jdbc only
			DBHelper.getController().connect(dbdir);

			// force models to be instantiated
			AppointmentModel.getReference();
			MemoModel.getReference();
			CheckListModel.getReference();
			AddressModel.getReference();
			TaskModel.getReference();
			LinkModel.getReference();

			// start the UI thread
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					UIControl.startUI();
				}
			});

			// calculate email time in minutes from now
			Calendar cal = new GregorianCalendar();
			int emailmins = Prefs.getIntPref(PrefName.EMAILTIME);
			int curmins = 60 * cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE);
			int mailtime = emailmins - curmins;
			if (mailtime < 0) {
				// we are past mailtime - send it now
				try {
					EmailReminder.sendDailyEmailReminder(null);
				} catch (Exception e) {
					final Exception fe = e;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							Errmsg.getErrorHandler().errmsg(fe);
						}
					});
				}
				// set timer for next mailtime
				mailtime += 24 * 60; // 24 hours from now
			}

			// start up email check timer - every 24 hours
			this.mailTimer_ = new java.util.Timer("MailTimer");
			this.mailTimer_.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						EmailReminder.sendDailyEmailReminder(null);
					} catch (Exception e) {
						final Exception fe = e;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								Errmsg.getErrorHandler().errmsg(fe);
							}
						});
					}
				}
			}, mailtime * 60 * 1000, 24 * 60 * 60 * 1000);

			// start socket listener
			if (port != -1 && this.socketServer_ == null) {
				this.socketServer_ = new SocketServer(port, this);
			}

		} catch (Exception e) {
			/*
			 * if something goes wrong, it might be that the database directory
			 * is bad. Maybe it does not exist anymore or something, so give the
			 * user a chance to change it if it will fix the problem
			 */
			Errmsg.getErrorHandler().errmsg(e);

			String es = e.toString();
			es += Resource.getResourceString("db_set_to") + dbdir;
			es += Resource.getResourceString("bad_db_2");

			// prompt for ok
			int ret = ScrolledDialog.showOptionDialog(es);
			if (ret == ScrolledDialog.OK) {
				OptionsView.dbSelectOnly();
				return;
			}

			System.exit(1);

		}

	}


}