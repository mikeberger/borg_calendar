package net.sf.borg.todoconduit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import palm.conduit.Category;
import palm.conduit.Log;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;
import palm.conduit.TodoRecord;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates. All
// rights reserved."

public class RecordManager {

    SyncProperties props;
    int db;
    static private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    CategoryManager cm = null;
    Vector hhCats = null;
    
    public RecordManager(SyncProperties props, int db) {
        this.props = props;
        this.db = db;
        cm = new CategoryManager( props, db );
    }

    public void WipeData() throws Exception {

        SyncManager.purgeAllRecs(db);
        
        if( hhCats == null)
        {
            hhCats = cm.getHHCategories();
        }
        
        Iterator cit = hhCats.iterator();
        while( cit.hasNext())
        {
            Category cat = (Category) cit.next();
            cat.setName("");
        }
        Category uf = (Category) hhCats.elementAt(0);
        uf.setId(0);
        uf.setIndex(0);
        uf.setName("Unfiled");

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
            
            String s = r.getCategory();
            if( s == null)
            {
                rec.setCategoryIndex(0);
            }
            else
            {
                // check if new cat or one already in list
                Category c = cm.matchName(s,hhCats);
                if( c != null )
                {
                    rec.setCategoryIndex(c.getIndex());
                }
                else
                {
                    // add new
                    int i = cm.getNextIndex(hhCats);
                    if( i == -1 )
                    {
                        rec.setCategoryIndex(0);
                        Log.err("cannot add category: " + s);
                        
                    }
                    else
                    {
                        c = (Category) hhCats.elementAt(i);
                        c.setId(i);
                        c.setIndex(i);
                        c.setName(s);
                        rec.setCategoryIndex(i);
                        
                    }
                       
                }
            }
            SyncManager.writeRec(db, rec);
            
        }
        
        Collection tasks = TaskModel.getReference().getTasks();
        it = tasks.iterator();
        while( it.hasNext() ) {
            
            Task r = (Task) it.next();
            
            String status = r.getState();
            if( status.equals("CLOSED") || status.equals("PR"))
                continue;
            
            // !!!!! only show first line of task text !!!!!!
            String tx = "BT" + r.getTaskNumber() + ":";
            String xx = r.getDescription();
            int ii = xx.indexOf('\n');
            if( ii != -1 ) {
                tx += xx.substring(0,ii);
            }
            else {
                tx += xx;
            }
            
            TodoRecord rec = new TodoRecord();

            rec.setId(0);
            rec.setDescription(tx);

            // date is the next todo field if present, otherwise
            // the due date
            Date nt = r.getDueDate();
            rec.setDueDate(nt);
            rec.setNote("-9999,");
            
            String s = r.getCategory();
            if( s == null)
            {
                rec.setCategoryIndex(0);
            }
            else
            {
                // check if new cat or one already in list
                Category c = cm.matchName(s,hhCats);
                if( c != null )
                {
                    rec.setCategoryIndex(c.getIndex());
                }
                else
                {
                    // add new
                    int i = cm.getNextIndex(hhCats);
                    if( i == -1 )
                    {
                        rec.setCategoryIndex(0);
                        Log.err("cannot add category: " + s);
                        
                    }
                    else
                    {
                        c = (Category) hhCats.elementAt(i);
                        c.setId(i);
                        c.setIndex(i);
                        c.setName(s);
                        rec.setCategoryIndex(i);
                        
                    }
                       
                }
            }
            SyncManager.writeRec(db, rec);
            
        }
        
        cm.writeHHCategories(hhCats);

    }

    // this part of sync is one-way. get any updates from HH only.
    // after, we will wipe HH db and fully restore from BORG
    public void SyncData() throws Exception {

        TodoRecord hhRecord;
        
        
        // get categories
        if( hhCats == null)
        {
            hhCats = cm.getHHCategories();
        }

        // get list og hh ids
        ArrayList hhids = new ArrayList();
        int recordCount = SyncManager.getDBRecordCount(db);
        for (int recordIndex = 0; recordIndex < recordCount; recordIndex++) {
            hhRecord = new TodoRecord();
            hhRecord.setIndex(recordIndex);
            SyncManager.readRecordByIndex(db, hhRecord);
            int id = getApptKey(hhRecord);
            if (id == -1 || hhRecord.isNew() || hhRecord.isArchived()
                    || hhRecord.isDeleted() || hhRecord.isModified()) {
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
            if (!hhRecord.isArchived() && !hhRecord.isDeleted() && !hhRecord.isCompleted()) {
                appt = palmToBorg(hhRecord);
                
                // set time to 12:00
                Date d = appt.getDate();
                
                // must set todo's with no date to current date - BORG does not
                // support dateless todos
                if( d == null )
                    d = new Date();
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(d);
                cal.set(Calendar.MINUTE,0);
                cal.set(Calendar.HOUR_OF_DAY,0);
                d = cal.getTime();
                appt.setDate(d);
                
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
            else if( hhRecord.isModified())
            {
                // only modify text and date because a BORG todo is really an appt
                // with many other non-todo fields
                appt.setText(hhRecord.getDescription());
                
                Date nt = appt.getNextTodo();
                if (nt == null) {
                    nt = appt.getDate();
                }
 
                Date d = hhRecord.getDueDate();
                if( d == null )
                    d = new Date();
                
                if( nt != d ) // date chg
                {
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(nt);
                    int d1 = cal.get(Calendar.DATE);
                    int m1 = cal.get(Calendar.MONTH);
                    cal.setTime(d);
                    int d2 = cal.get(Calendar.DATE);
                    int m2 = cal.get(Calendar.MONTH);
                    
                    // date change is detected only by chg of day/month
                    // cannot just use chg of Date b/c time is not defined on palm
                    if( d1 != d2 || m1 != m2 )
                    {
                        cal.set(Calendar.MINUTE,0);
                        cal.set(Calendar.HOUR_OF_DAY,0);
                        d = cal.getTime();
                        appt.setDate(d);
                        AppointmentModel.getReference().delAppt(appt);
                        AppointmentModel.getReference().saveAppt(appt,true);
                        return;
                    }
                }

                //category
                String cat = cm.matchId(hhRecord.getCategoryIndex(), hhCats).getName();
                if( !cat.equals("Unfiled"))
                {
                    appt.setCategory(cat);
                } 
                
                AppointmentModel.getReference().syncSave(appt);
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
    
    public Appointment palmToBorg(TodoRecord hh) {
        Appointment appt = AppointmentModel.getReference().newAppt();
        appt.setKey(-1);
        appt.setDate(hh.getDueDate());
        appt.setTodo(true);
        appt.setText(hh.getDescription());
        String cat = cm.matchId(hh.getCategoryIndex(), hhCats).getName();
        if( !cat.equals("Unfiled"))
        {
            appt.setCategory(cat);
        }
        //System.out.println( appt.getText() + " " + cm.matchId(hh.getCategoryIndex(), hhCats).getName());
        return appt;
    }
}