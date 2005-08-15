package net.sf.borg.apptconduit;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.BeanDataFactoryFactory;
import net.sf.borg.model.db.IBeanDataFactory;
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


        ApptRecordManager recordMgr;
        TodoRecordManager trecordMgr;
        AppointmentModel apptModel;
        TaskModel taskModel;

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
                //read the pc records on the PC
                String dbdir = props.pathName;
           
                StringBuffer tmp = new StringBuffer(dbdir);
                IBeanDataFactory factory = BeanDataFactoryFactory.getInstance().getFactory(tmp, false, false);
                //dbdir = tmp.toString();
                apptModel = AppointmentModel.create();
                apptModel.open_db(factory, dbdir , "");;

                taskModel = TaskModel.create();
                taskModel.open_db(factory, dbdir , "");
               
                // have to get todo data into BORG, then get appt data, then sync back
                // appt data and finally overwrite Todo data. 
                if (props.syncType != SyncProperties.SYNC_DO_NOTHING) {
                	Log.out("Sync Todo");
                    int tododb = SyncManager.openDB("ToDoDB", 0, SyncManager.OPEN_READ
                            | SyncManager.OPEN_WRITE | SyncManager.OPEN_EXCLUSIVE);
                    trecordMgr = new net.sf.borg.apptconduit.TodoRecordManager(props, tododb);
                	trecordMgr.SyncData();
                	SyncManager.closeDB(tododb);
                }
                
                int apptdb = SyncManager.openDB("DatebookDB", 0, SyncManager.OPEN_READ
                        | SyncManager.OPEN_WRITE | SyncManager.OPEN_EXCLUSIVE);
                recordMgr = new net.sf.borg.apptconduit.ApptRecordManager(props, apptdb);
                int numrecs = SyncManager.getDBRecordCount(apptdb);
                
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
                	Log.out("Sync Appt");
                    recordMgr.SyncData();
                }
                
                SyncManager.closeDB(apptdb);

                if (props.syncType != SyncProperties.SYNC_DO_NOTHING) {
                    int tododb = SyncManager.openDB("ToDoDB", 0, SyncManager.OPEN_READ
                            | SyncManager.OPEN_WRITE | SyncManager.OPEN_EXCLUSIVE);
                    trecordMgr = new net.sf.borg.apptconduit.TodoRecordManager(props, tododb);
                    Log.out("Wipe Todo");
                	trecordMgr.WipeData();
                	SyncManager.closeDB(tododb);
                }
              
                
                // Close DB
                apptModel.close_db();
                taskModel.close_db();
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