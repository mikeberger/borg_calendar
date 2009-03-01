package net.sf.borg.addrconduit;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.J13Helper;
import net.sf.borg.common.SocketClient;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.db.remote.SocketProxy;
import palm.conduit.Conduit;
import palm.conduit.ConfigureConduitInfo;
import palm.conduit.Log;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."

public class AddrCond implements Conduit {

	/**
	 * Name of the conduit to be displayed on the dialog
	 */

	static private int port = 2929;

	static final String NAME = "BORG Address Conduit";

	static public void log(String s) {
		Log.out(s);
		String s2 = J13Helper.replace(s, "\n", "%NL%");
		try {
			SocketClient.sendMsg("localhost", port, "lock:" + s2);
		} catch (IOException e) {

		}
	}

	public void open(SyncProperties props) {

		int db;

		RecordManager recordMgr;
		AddressModel addressModel;

		Errmsg.console(true);

		// Tell the log we are starting
		Log.startSync();

		try {
			if (props.syncType != SyncProperties.SYNC_DO_NOTHING) {

				db = SyncManager.openDB("AddressDB", 0, SyncManager.OPEN_READ
						| SyncManager.OPEN_WRITE | SyncManager.OPEN_EXCLUSIVE);

				// read the pc records on the PC
				String loc = props.localName;
				String dir = props.pathName;

				Log.out("dir=" + dir);
				Log.out("localName=" + loc);

				if (loc.indexOf(':') != -1) {
					dir = loc;
				}

				// check for properties file
				String propfile = dir + "/db.properties";
				try {
					FileInputStream is = new FileInputStream(propfile);
					Properties dbprops = new Properties();
					dbprops.load(is);

					port = Integer.parseInt(dbprops.getProperty("port"));
				} catch (Exception e) {
					Log.out("Properties exception: " + e.toString());
				}
				SocketProxy.setPort(port);

				try {
					SocketClient.sendMsg("localhost", port,
							"lock:Address HotSync In Progress...Please wait");
				} catch (Exception e) {
				}

				
				addressModel = AddressModel.create();
				addressModel.open_db();

				// Create an instance of the RecordManager for synchronizing the
				// records
				recordMgr = new RecordManager(props, db);
				recordMgr.quickSyncAndWipe();

				// Close DB
				addressModel.close_db();
				SyncManager.closeDB(db);
				log("OK AddrCond Conduit");

				try {
					SocketClient.sendMsg("localhost", port, "sync");
					SocketClient.sendMsg("localhost", port,
							"lock:Appointment HotSync Completed");
					SocketClient.sendMsg("localhost", port, "unlock");
				} catch (Exception e) {
				}

			}

			// Single Log we are successful

			Log.endSync();

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