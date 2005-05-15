package net.sf.borg.apptconduit;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.model.AppointmentModel;
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

 
    public void open(SyncProperties props) {

        int db;

        RecordManager recordMgr;
        AppointmentModel apptModel;

        Errmsg.console(true);
        
        // Tell the log we are starting
        Log.startSync();

        try {
            if (props.syncType == SyncProperties.SYNC_DO_NOTHING) {
               
                Log.out("OK ApptCond Do Nothing");
                Log.endSync();
            }
            else
            {

                db = SyncManager.openDB("DatebookDB", 0, SyncManager.OPEN_READ
                        | SyncManager.OPEN_WRITE | SyncManager.OPEN_EXCLUSIVE);
                
                int numrecs = SyncManager.getDBRecordCount(db);

                //read the pc records on the PC
                String dbdir = props.pathName;
                apptModel = AppointmentModel.create();
                apptModel.open_db(dbdir, false, false, false, 1);

                //Create an instance of the RecordManager for synchronizing the
                // records
                recordMgr = new RecordManager(props, db);
                
                // send ALL records to HH if wipe option set by user OR is HH db is empty
                if( props.syncType == SyncProperties.SYNC_PC_TO_HH || numrecs == 0)
                {
                    recordMgr.WipeData();
                }
                else if( props.syncType == SyncProperties.SYNC_HH_TO_PC )
                {
                	recordMgr.quickSyncAndWipe();
                }
                else
                {
                    recordMgr.SyncData();
                }


                // Close DB
                apptModel.close_db();
                SyncManager.closeDB(db);
                
                // Single Log we are successful
                Log.out("OK ApptCond Conduit");
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