package net.sf.borg.todoconduit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;
import palm.conduit.TodoRecord;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates. All
// rights reserved."

public class RecordManager {

    SyncProperties props;
    int db;
    static private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    
    public RecordManager(SyncProperties props, int db) {
        this.props = props;
        this.db = db;
    }

    public void WipeData() throws Exception {

        SyncManager.purgeAllRecs(db);

        AppointmentModel amod = AppointmentModel.getReference();

        Collection tds = amod.get_todos();
        Iterator it = tds.iterator();
        while (it.hasNext()) {
            Appointment r = (Appointment) it.next();
            TodoRecord rec = new TodoRecord();

            rec.setId(0);
            rec.setDescription(r.getText());

            // date is the next todo field if present, otherwise
            // the due date
            Date nt = r.getNextTodo();
            if (nt == null) {
                nt = r.getDate();
            }

            rec.setDueDate(nt);
            String note = Integer.toString(r.getKey()) + "," + sdf.format(nt);
            rec.setNote(note);
            SyncManager.writeRec(db, rec);
        }

    }

    // this part of sync is one-way. get any updates from HH only.
    // after, we will wipe HH db and fully restore from BORG
    public void SyncData() throws Exception {

        TodoRecord hhRecord;

        // get list og hh ids
        ArrayList hhids = new ArrayList();
        int recordCount = SyncManager.getDBRecordCount(db);
        for (int recordIndex = 0; recordIndex < recordCount; recordIndex++) {
            hhRecord = new TodoRecord();
            hhRecord.setIndex(recordIndex);
            SyncManager.readRecordByIndex(db, hhRecord);
            if (hhRecord.isNew() || hhRecord.isArchived()
                    || hhRecord.isDeleted() || hhRecord.isModified()) {
                //Log.out(hhRecord.toFormattedString() + "mod: " +
                // hhRecord.isModified());

                hhids.add(new Integer(hhRecord.getId()));
            }
        }

        Iterator it = hhids.iterator();
        while (it.hasNext()) {

            Integer id = (Integer) it.next();
            hhRecord = new TodoRecord();
            hhRecord.setId(id.intValue());
            SyncManager.readRecordById(db, hhRecord);

            // Synchronize the record obtained from the handheld
            synchronizeHHRecord(hhRecord);
        }

    }

    public void synchronizeHHRecord(TodoRecord hhRecord) throws Exception {

        Appointment appt = null;
        //Log.out("Sync HH: " + hhRecord.toFormattedString());

        // any record without a BORG id is considered new
        int id = getApptKey(hhRecord);
        if (id != -1) {
            try {
                appt = getRecordById(id);
            }
            catch (Exception e) {
            }
        }

        // if todo points to a deleted BORG record - skip it
        // do not add a new one
        if (appt == null && id != -1)
            return;

        if (appt == null) {
            // add a new todo to BORG
            if (!hhRecord.isArchived() && !hhRecord.isDeleted()) {
                appt = palmToBorg(hhRecord);
                addPCRecord(appt);
            }
        }
        else {
            if (hhRecord.isCompleted()) {
                // do todo
                try {
                    Date nt = appt.getNextTodo();
                    if (nt != null ) {
                        
                        String nts = sdf.format(nt);
                        // if date of repeating todo does not match BORG - skip it
                        if( !nts.equals(getNT(hhRecord)))
                            return;
                    }
                    AppointmentModel.getReference().do_todo(appt.getKey(),
                            false);
                }
                catch (Exception e) {
                }
            }
        }

    }

    private Appointment getRecordById(int id) throws Exception {
        return (AppointmentModel.getReference().getAppt(id));
    }

    private int addPCRecord(Appointment appt) throws Exception {

        AppointmentModel.getReference().syncSave(appt);
        return (appt.getKey());
    }

    static int getApptKey(TodoRecord hh) {

        String notes = hh.getNote();
        if (notes != null) {

            int idx = notes.indexOf(",");
            if (idx != -1) {
                String id = notes.substring(0, idx);
                try {
                    int i = Integer.parseInt(id);
                    return (i);
                }
                catch (Exception e) {
                }

            }
        }

        return (-1);

    }

    static String getNT(TodoRecord hh)
    {
        String note = hh.getNote();
        if( note != null )
        {
            int idx = note.indexOf(",");
            if( idx != -1 )
                return( note.substring(idx+1));
            
        }
        
        return( "" );
    }
    static public Appointment palmToBorg(TodoRecord hh) {
        Appointment appt = AppointmentModel.getReference().newAppt();
        appt.setKey(-1);
        appt.setDate(hh.getDueDate());
        appt.setTodo(true);
        appt.setText(hh.getDescription());
        return appt;
    }
}