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

package com.mbcsoft.platform.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.mbcsoft.platform.common.Errmsg;
import com.mbcsoft.platform.common.PrefName;
import com.mbcsoft.platform.common.Prefs;
import com.mbcsoft.platform.common.Resource;
import com.mbcsoft.platform.common.SocketClient;
import com.mbcsoft.platform.common.SocketHandler;
import com.mbcsoft.platform.common.SocketServer;
import com.mbcsoft.platform.model.JdbcDB;
import com.mbcsoft.platform.model.Model;
import com.mbcsoft.platform.ui.util.ModalMessage;
import com.mbcsoft.platform.ui.util.ScrolledDialog;

public class AppMain implements SocketHandler, Observer {

	/** The singleton. */
	static volatile private AppMain singleton = null;

	static private final Logger log = Logger.getLogger(AppMain.class.getName());

	/**
	 * Gets the singleton.
	 *
	 * @return the singleton
	 */
	static public AppMain getReference() {
		if (singleton == null) {
			AppMain b = new AppMain();
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
	public static void main(String args[]) {
		// create a new borg object and call its init routine with the command
		// line args
		AppMain b = getReference();
		b.init(args);
	}

	/**
	 * close db connections and end the program
	 */
	static public void shutdown() {

		try {
			// close the db
			JdbcDB.close();
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

	/**
	 * message popped up if the socket thread has something to tell the user.
	 */
	private ModalMessage modalMessage = null;

	/**
	 * The socket server - listens for incoming requests such as open requests
	 */
	private SocketServer socketServer_ = null;

	/**
	 * constructor
	 */
	private AppMain() {
		// empty
	}

	/**
	 * process a socket message
	 */
	@Override
	public synchronized String processMessage(String msg) {
		log.fine("Got msg: " + msg);
		if (msg.equals("sync")) {
			try {
				Model.syncModels();
				return ("sync success");
			} catch (Exception e) {
				e.printStackTrace();
				return ("sync error: " + e.toString());
			}
		} else if (msg.equals("shutdown")) {
			System.exit(0);
		} else if (msg.equals("open")) {
			UIControl.toFront();
			return ("ok");
		} else if (msg.startsWith("lock:")) {
			final String lockmsg = msg.substring(5);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (AppMain.this.modalMessage == null || !AppMain.this.modalMessage.isShowing()) {
						AppMain.this.modalMessage = new ModalMessage(lockmsg, false);
						AppMain.this.modalMessage.setVisible(true);
					} else {
						AppMain.this.modalMessage.appendText(lockmsg);
					}
					AppMain.this.modalMessage.setEnabled(false);
					AppMain.this.modalMessage.toFront();
				}
			});

			return ("ok");
		} else if (msg.startsWith("log:")) {
			final String lockmsg = msg.substring(4);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (AppMain.this.modalMessage != null && AppMain.this.modalMessage.isShowing()) {
						AppMain.this.modalMessage.appendText(lockmsg);
						// Borg.this.modalMessage.setEnabled(false);
						// Borg.this.modalMessage.toFront();
					}

				}
			});

			return ("ok");
		} else if (msg.equals("unlock")) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (AppMain.this.modalMessage.isShowing()) {
						AppMain.this.modalMessage.setEnabled(true);
					}
				}
			});

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
			Prefs.setPrefRootNode("com/mbcsoft/test");

		// logging
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		log.addHandler(ch);
		log.setUseParentHandlers(false);

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

		
		// db url
		String dbdir = null;

		try {
			if (testdb != null)
				dbdir = testdb;
			else
				dbdir = JdbcDB.buildDbDir(); // derive db url
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
					dbdir = JdbcDB.buildDbDir();
				} else {
					JOptionPane.showMessageDialog(null, Resource.getResourceString("selectdb"),
							Resource.getResourceString("Notice"), JOptionPane.INFORMATION_MESSAGE);

					// if user wants to set db - let them
					OptionsView.dbSelectOnly();
					return;
				}
			}

			// connect to the db - for now, it is jdbc only
			JdbcDB.connect(dbdir);

			

			// start the UI thread
			final String traynm = trayname;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					UIControl.startUI(traynm);
				}
			});

			
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

	/**
	 * Constructs a URL expression for the location to this current class, where
	 * it lives in the jar file.
	 *
	 * @return
	 * @throws MalformedURLException
	 */
	protected URL getJarURL() throws MalformedURLException {
		Class<?> cls = this.getClass();
		ProtectionDomain domain = cls.getProtectionDomain();
		CodeSource codeSource = domain.getCodeSource();
		URL sourceLocation = codeSource.getLocation();
		return new URL(sourceLocation.toString());
	}

	@Override
	public void update(Observable o, Object arg) {
		AppMain.shutdown();
	}
}