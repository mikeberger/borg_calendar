package net.sf.borg.apptconduit;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.J13Helper;
import net.sf.borg.common.util.SocketClient;
import net.sf.borg.model.AppointmentModel;

import net.sf.borg.model.db.remote.IRemoteProxy;
import net.sf.borg.model.db.remote.IRemoteProxyProvider;
import net.sf.borg.model.db.remote.RemoteProxyHome;
import net.sf.borg.model.db.remote.socket.SocketProxy;
import palm.conduit.Conduit;
import palm.conduit.ConfigureConduitInfo;
import palm.conduit.Log;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."
public class ApptCond implements Conduit {

	/**
	 * Name of the conduit to be displayed on the dialog
	 */
	static final String NAME = "BORG Appt Conduit";

	static private int port;

	static public void log(String s) {
		Log.out(s);
		String s2 = J13Helper.replace(s, "\n", "%NL%");
		try {
			SocketClient.sendMsg("localhost", port, "lock:" + s2);
		} catch (IOException e) {

		}
	}

	public void open(SyncProperties props) {

		ApptRecordManager recordMgr;
		TodoRecordManager trecordMgr;
		AppointmentModel apptModel;
		//TaskModel taskModel;

		Errmsg.console(true);

		// Tell the log we are starting
		Log.startSync();

		try {
			if (props.syncType == SyncProperties.SYNC_DO_NOTHING) {

				Log.out("OK ApptCond Do Nothing");
				Log.endSync();
			} else {
				// read the pc records on the PC
				String loc = props.localName;
				String dbdir = props.pathName;
				String user = props.userName;
				Log.out("dbdir=" + dbdir);
				Log.out("user=" + user);
				Log.out("localName=" + loc);

				if (loc.indexOf(':') != -1) {
					dbdir = loc;
				}

				// check for properties file
				String propfile = dbdir + "/db.properties";
				try {
					FileInputStream is = new FileInputStream(propfile);
					Properties dbprops = new Properties();
					dbprops.load(is);
					dbdir = dbprops.getProperty("dburl");
					user = dbprops.getProperty("user");
					port = Integer.parseInt(dbprops.getProperty("port"));
				} catch (Exception e) {
					Log.out("Properties exception: " + e.toString());
				}

				// If we're doing remote stuff, use SocketProxy
				RemoteProxyHome.getInstance().setProxyProvider(
						new IRemoteProxyProvider() {
							public final IRemoteProxy createProxy(String url) {
								// No synchronization needed - we're
								// single-threaded.
								if (proxy == null)
									proxy = new SocketProxy(url, port);
								return proxy;
							}

							public final Credentials getCredentials() {
								return new Credentials("$default", "$default");
							}

							// private //
							private IRemoteProxy proxy = null;
						});

				// shutdown the app - unless we are using a remote socket
				// interface
				if (!dbdir.startsWith("remote:")) {
					try {
						SocketClient.sendMsg("localhost", port, "shutdown");
					} catch (Exception e) {
					}
				} else {
					try {
						SocketClient
								.sendMsg("localhost", port,
										"lock:Appointment HotSync In Progress...Please wait");
					} catch (Exception e) {
					}
				}

				log("dbdir=" + dbdir);
				log("user=" + user);
				apptModel = AppointmentModel.create();
				apptModel.open_db(dbdir, user, false, false);
/*
				taskModel = TaskModel.create();
				try{
				    taskModel.open_db(dbdir, user, false, false);
				}
				catch( Exception e)
				{
				     //ignore exception for subtasks no supported
				}*/
				// have to get todo data into BORG, then get appt data, then
				// sync back
				// appt data and finally overwrite Todo data.
				if (props.syncType != SyncProperties.SYNC_DO_NOTHING) {
					log("Sync Todos from Palm to Borg");
					int tododb = SyncManager.openDB("ToDoDB", 0,
							SyncManager.OPEN_READ | SyncManager.OPEN_WRITE
									| SyncManager.OPEN_EXCLUSIVE);
					trecordMgr = new net.sf.borg.apptconduit.TodoRecordManager(
							props, tododb);
					trecordMgr.SyncData();
					SyncManager.closeDB(tododb);
				}

				int apptdb = SyncManager.openDB("DatebookDB", 0,
						SyncManager.OPEN_READ | SyncManager.OPEN_WRITE
								| SyncManager.OPEN_EXCLUSIVE);
				recordMgr = new net.sf.borg.apptconduit.ApptRecordManager(
						props, apptdb);
				int numrecs = SyncManager.getDBRecordCount(apptdb);

				// send ALL records to HH if wipe option set by user OR is HH db
				// is empty
				if (props.syncType == SyncProperties.SYNC_PC_TO_HH
						|| numrecs == 0) {
					log("Wipe Palm/Sync Appts");
					recordMgr.WipeData();
				} else if (props.syncType == SyncProperties.SYNC_HH_TO_PC) {
					recordMgr.quickSyncAndWipe();
				} else {
					log("Sync Appts");
					recordMgr.SyncData();
				}

				SyncManager.closeDB(apptdb);

				if (props.syncType != SyncProperties.SYNC_DO_NOTHING) {
					int tododb = SyncManager.openDB("ToDoDB", 0,
							SyncManager.OPEN_READ | SyncManager.OPEN_WRITE
									| SyncManager.OPEN_EXCLUSIVE);
					trecordMgr = new net.sf.borg.apptconduit.TodoRecordManager(
							props, tododb);
					log("Wipe Todos and load Palm from BORG");
					trecordMgr.WipeData();
					SyncManager.closeDB(tododb);
				}

				// Close DB
				apptModel.close_db();
				//taskModel.close_db();
				// Single Log we are successful
				log("OK ApptCond Conduit");
				Log.endSync();
				if (dbdir.startsWith("remote:")) {
					try {
						SocketClient.sendMsg("localhost", port, "sync");
						SocketClient.sendMsg("localhost", port,
								"lock:Appointment HotSync Completed");
						SocketClient.sendMsg("localhost", port, "unlock");
					} catch (Exception e) {
					}
				}
			}

		} catch (Throwable t) {

			// If there was an error, dump the stack trace
			// and tell the log the conduit failed

			t.printStackTrace();
			Log.abortSync();
		}
	}

	/**
	 * Returns a String representation of the conduit name.
	 */
	public String name() {
		return NAME;
	}

	public int configure(ConfigureConduitInfo info) {
		ConduitConfigure config = new ConduitConfigure(info, NAME);
		config.createDialog();

		if (config.dataChanged) {
			int propsValue = SyncProperties.SYNC_DO_NOTHING; // default
			propsValue = config.saveState;

			if (config.setDefault) {
				info.syncPermanent = propsValue;
				info.syncPref = ConfigureConduitInfo.PREF_PERMANENT;
			}
			info.syncNew = propsValue;
		}

		return 0;
	}
}