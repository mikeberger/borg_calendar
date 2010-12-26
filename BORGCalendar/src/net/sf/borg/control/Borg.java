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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.SocketClient;
import net.sf.borg.common.SocketHandler;
import net.sf.borg.common.SocketServer;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.tool.ConversionTool;
import net.sf.borg.ui.UIControl;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.util.ModalMessage;
import net.sf.borg.ui.util.ScrolledDialog;

/**
 * The Main Class of Borg. It's responsible for starting up the model and
 * spawning various threads, including the main UI thread and various timer
 * threads. It also handles shutdown.
 */
public class Borg implements SocketHandler {

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
	 * add a url to the classpath
	 * 
	 * @param u
	 *            - the url
	 * @throws IOException
	 */
	private static void addURL(URL u) throws IOException {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL",
					new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		} catch (Throwable t) {
			t.printStackTrace();
		}
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
		Borg b = getReference();
		b.init(args);
	}

	/**
	 * close db connections and end the program
	 */
	static public void shutdown() {

		if (getReference().syncTimer_ != null)
			getReference().syncTimer_.cancel();
		if (getReference().mailTimer_ != null)
			getReference().mailTimer_.cancel();

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
	 */
	private ModalMessage modalMessage = null;

	/**
	 * The socket server - listens for incoming requests such as open requests
	 */
	private SocketServer socketServer_ = null;

	/**
	 * The sync timer - controls auto-sync with db - only needed for mysql - and
	 * then not really
	 */
	private java.util.Timer syncTimer_ = null;

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
			UIControl.toFront();
			return ("ok");
		} else if (msg.startsWith("lock:")) {
			final String lockmsg = msg.substring(5);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
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
				@Override
				public void run() {
					if (modalMessage.isShowing()) {
						modalMessage.setEnabled(true);
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
					System.out.println(Resource
							.getResourceString("-db_argument_is_missing"));
					System.exit(1);
				}
				testdb = args[i];
			} else if (args[i].equals("-test")) {
				testing = true;
			} else if (args[i].equals("-runtool")) {
				i++;
				if (i >= args.length) {
					System.out.println("tool name is missing");
					System.exit(1);
				}
				String toolName = args[i];
				try {
					Class<?> toolClass = Class
							.forName("net.sf.borg.model.tool." + toolName);
					Object tool = toolClass.newInstance();
					if (tool instanceof ConversionTool) {
						((ConversionTool) tool).convert();
					}
					System.exit(0);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}

			}

		}

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

		// add the lib folder to the classpath
		if (Prefs.getBoolPref(PrefName.DYNAMIC_LOADING) == true) {
			// compute lib folder relative to the borg.jar file location
			File parentDir = null;
			try {
				URL jarURL = getJarURL();
				String jarPath = jarURL.toURI().getPath();
				if (jarPath != null) {
					parentDir = new File(jarPath).getParentFile();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			File lib = null;

			if (parentDir != null) {
				lib = new File(parentDir, "lib");
				
				// fall back to relative directory if lib folder not child of
				// same folder where borg.jar is
				if (!lib.exists() || !lib.isDirectory()) {
					lib = null;
				}
			}

			if (lib == null) {
				lib = new File("lib");
			}

			System.out.println("using lib dir=" + lib);
			if (lib.isDirectory()) {
				File[] files = lib.listFiles();
				for (File file : files) {
					if (file.getName().endsWith(".jar")) {
						System.out.println("Loading JAR: " + file.getName());
						try {
							Borg.addURL(file.toURI().toURL());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
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
				dbdir = JdbcDB.buildDbDir(); // derive db url from user prefs

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
					Prefs.putPref(PrefName.HSQLDBDIR, borgdir.getAbsolutePath());
					Prefs.putPref(PrefName.DBTYPE, "hsqldb");
					dbdir = JdbcDB.buildDbDir();
				} else {
					JOptionPane.showMessageDialog(null,
							Resource.getResourceString("selectdb"),
							Resource.getResourceString("Notice"),
							JOptionPane.INFORMATION_MESSAGE);

					// if user wants to set db - let them
					OptionsView.dbSelectOnly();
					return;
				}
			}

			// now all errors can go to popup windows
			Errmsg.console(false); // send errors to screen

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
						@Override
						public void run() {
							Errmsg.errmsg(fe);
						}
					});
				}
				// set timer for next mailtime
				mailtime += 24 * 60; // 24 hours from now
			}

			// start up email check timer - every 24 hours
			mailTimer_ = new java.util.Timer("MailTimer");
			mailTimer_.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						EmailReminder.sendDailyEmailReminder(null);
					} catch (Exception e) {
						final Exception fe = e;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
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
				syncTimer_ = new java.util.Timer("SyncTimer");
				syncTimer_.schedule(new TimerTask() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
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
			/*
			 * if something goes wrong, it might be that the database directory
			 * is bad. Maybe it does not exist anymore or something, so give the
			 * user a chance to change it if it will fix the problem
			 */
			Errmsg.errmsg(e);

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
}