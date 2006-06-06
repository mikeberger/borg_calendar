package net.sf.borg.addrconduit;

import java.io.FileInputStream;
import java.util.Properties;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.SocketClient;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.db.remote.IRemoteProxy;
import net.sf.borg.model.db.remote.IRemoteProxyProvider;
import net.sf.borg.model.db.remote.RemoteProxyHome;
import net.sf.borg.model.db.remote.socket.SocketProxy;
import palm.conduit.Conduit;
import palm.conduit.ConfigureConduitInfo;
import palm.conduit.Log;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;

// "Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."

public class AddrCond implements Conduit {

    /**
     * Name of the conduit to be displayed on the dialog
     */
    static final String NAME = "BORG Address Conduit";

  
    public void open(SyncProperties props) {

        int db;

        RecordManager recordMgr;
        AddressModel addressModel;

        Errmsg.console(true);
        
//      If we're doing remote stuff, use SocketProxy
		RemoteProxyHome.getInstance().setProxyProvider(
				new IRemoteProxyProvider() {
					public final IRemoteProxy createProxy(String url) {
						// No synchronization needed - we're single-threaded.
						if (proxy == null)
							proxy = new SocketProxy(url);
						return proxy;
					}

					public final Credentials getCredentials() {
						return new Credentials("$default", "$default");
					}

					// private //
					private IRemoteProxy proxy = null;
				});

		
        // Tell the log we are starting
        Log.startSync();

        try {
            if (props.syncType != SyncProperties.SYNC_DO_NOTHING) {

                db = SyncManager.openDB("AddressDB", 0, SyncManager.OPEN_READ
                        | SyncManager.OPEN_WRITE | SyncManager.OPEN_EXCLUSIVE);

                //read the pc records on the PC
            	String loc = props.localName;
                String dbdir = props.pathName;
                String user = props.userName;
                Log.out("dbdir=" + dbdir);
                Log.out("user=" + user);
                Log.out("localName=" + loc);
                
                if( loc.indexOf(':') != -1 )
                {
                	dbdir = loc;
                }
                
//              check for properties file
                String propfile = dbdir + "/db.properties";
                try{
                	FileInputStream is = new FileInputStream(propfile);
                	Properties dbprops = new Properties();
                	dbprops.load(is);
                	dbdir = dbprops.getProperty("dburl");
                	user = dbprops.getProperty("user");
                }
                catch( Exception e)
                {
                	Log.out("Properties exception: " + e.toString());
                }
                
//              shutdown the app - unless we are using a remote socket interface
				if (!dbdir.startsWith("remote:")) {
					try{
						SocketClient.sendMsg("localhost", 2929, "shutdown");
					}
					catch(Exception e)
					{}
				}
                Log.out("dbdir2=" + dbdir);
                Log.out("user2=" + user);
                addressModel = AddressModel.create();
                addressModel.open_db(dbdir, user, false, false);

                //Create an instance of the RecordManager for synchronizing the
                // records
                recordMgr = new RecordManager(props, db);
                recordMgr.SyncData();

                // Close DB
                addressModel.close_db();
                SyncManager.closeDB(db);
            }

            // Single Log we are successful
            Log.out("OK AddrCond Conduit");
            Log.endSync();

        }
        catch (Throwable t) {

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
            int propsValue = SyncProperties.SYNC_DO_NOTHING; //default
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