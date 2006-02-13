package net.sf.borg.addrconduit;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.model.AddressModel;
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
                
                // hard-code MySQL kludge just for me for now
                if( loc.equals("mysql"))
                {
                	dbdir = "jdbc:mysql://localhost/borg?user=borg&password=borg";
                	user = "$default";
                }
                else if( loc.indexOf(':') != -1 )
                {
                	dbdir = loc;
                }
                Log.out("dbdir2=" + dbdir);
                
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