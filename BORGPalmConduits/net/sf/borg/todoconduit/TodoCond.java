package net.sf.borg.todoconduit;

import net.sf.borg.model.AppointmentModel;
import palm.conduit.Conduit;
import palm.conduit.ConfigureConduitInfo;
import palm.conduit.Log;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."
public class TodoCond implements Conduit {

    /**
     * Name of the conduit to be displayed on the dialog
     */
    static final String NAME = "BORG Todo Conduit";

 
    public void open(SyncProperties props) {

        int db;

        RecordManager recordMgr;
        AppointmentModel apptModel;

        // Tell the log we are starting
        Log.startSync();

        try {
            if (props.syncType == props.SYNC_DO_NOTHING) {
               
                Log.out("OK TodoCond Do Nothing");
                Log.endSync();
            }
            else
            {

                db = SyncManager.openDB("ToDoDB", 0, SyncManager.OPEN_READ
                        | SyncManager.OPEN_WRITE | SyncManager.OPEN_EXCLUSIVE);

                //read the pc records on the PC
                String dbdir = props.pathName;
                apptModel = AppointmentModel.create();
                apptModel.open_db(dbdir, false, false, false, 1);

                //Create an instance of the RecordManager for synchronizing the
                // records
                recordMgr = new RecordManager(props, db);
                recordMgr.SyncData();
                recordMgr.WipeData();

                // Close DB
                apptModel.close_db();
                SyncManager.closeDB(db);
                
                // Single Log we are successful
                Log.out("OK TodoCond Conduit");
                Log.endSync();
            }
            



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