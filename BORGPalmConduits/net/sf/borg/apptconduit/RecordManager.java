package net.sf.borg.apptconduit;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import palm.conduit.DateRecord;
import palm.conduit.Log;
import palm.conduit.Record;
import palm.conduit.SyncException;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."

public class RecordManager {

    SyncProperties props;
    int db;

    public RecordManager(SyncProperties props, int db) {
        this.props = props;
        this.db = db;
    }

    public void WipeData() throws Exception {

        SyncManager.purgeAllRecs(db);

        AppointmentModel amod = AppointmentModel.getReference();

        Collection appts = amod.getAllAppts();
        Iterator it = appts.iterator();
        while (it.hasNext()) {
            Appointment appt = (Appointment) it.next();
            resetPCAttributes(appt);
            writeHHRecord(borgToPalm(appt));
        }
        appts = amod.getDeletedAppts();
        it = appts.iterator();
        while (it.hasNext()) {
            Appointment appt = (Appointment) it.next();
            deletePCRecord(appt);
        }
    }

    public void SyncData() throws Exception {

        DateRecord hhRecord;

        // get list og hh ids
        ArrayList hhids = new ArrayList();
        int recordCount = SyncManager.getDBRecordCount(db);
        for (int recordIndex = 0; recordIndex < recordCount; recordIndex++) {
            hhRecord = new DateRecord();
            hhRecord.setIndex(recordIndex);
            SyncManager.readRecordByIndex(db, hhRecord);
            int borgid = getApptKey(hhRecord);
            if (borgid == -1 || hhRecord.isNew() || hhRecord.isArchived()
                    || hhRecord.isDeleted() || hhRecord.isModified()) {
                hhids.add(new Integer(hhRecord.getId()));
            }
        }

        Iterator it = hhids.iterator();
        while (it.hasNext()) {

            Integer id = (Integer) it.next();
            hhRecord = new DateRecord();
            hhRecord.setId(id.intValue());
            SyncManager.readRecordById(db, hhRecord);

            // Synchronize the record obtained from the handheld
            synchronizeHHRecord(hhRecord);
        }

        AppointmentModel amod = AppointmentModel.getReference();
        Collection appts = amod.getAllAppts();
        it = appts.iterator();
        while (it.hasNext()) {
            Appointment appt = (Appointment) it.next();
            if (appt.getNew() || appt.getDeleted() || appt.getModified()) {
                synchronizePCRecord(appt);
            }
        }

        appts = amod.getDeletedAppts();
        it = appts.iterator();
        while (it.hasNext()) {
            Appointment appt = (Appointment) it.next();
            synchronizePCRecord(appt);
        }
        SyncManager.purgeDeletedRecs(db); //deletes all hh records marked as
                                          // deleted
        SyncManager.resetSyncFlags(db); //reset all the sync flags on the hh
    }

    public void synchronizePCRecord(Appointment appt) throws Exception {

        DateRecord hhRecord;
        
        //Log.out("Sync PC: " + appt.getKey() + " " + appt.getText());

        if (!appt.getNew()) {

            hhRecord = retrieveHHRecordByBorgId(appt.getKey());

            if (hhRecord == null) {
                if (appt.getDeleted()) {
                    deletePCRecord(appt);
                }
                else {
                    resetPCAttributes(appt);
                    writeHHRecord(borgToPalm(appt));
                }
            }
            else {

                if (appt.getDeleted()) {
                    deletePCRecord(appt);
                    deleteHHRecord(hhRecord);
                }
                else if (appt.getModified()) {
                    resetPCAttributes(appt);
                    int hhid = hhRecord.getId();
                    hhRecord = borgToPalm(appt);
                    hhRecord.setId(hhid);
                    writeHHRecord(hhRecord);
                }

            }
        }
        else if (appt.getDeleted()) {
            deletePCRecord(appt);
        }
        else {
            resetPCAttributes(appt);
            writeHHRecord(borgToPalm(appt));
        }

    }

    public void synchronizeHHRecord(DateRecord hhRecord) throws Exception {

        Appointment appt = null;
        //Log.out("Sync HH: " + hhRecord.toFormattedString());
        // any record without a BORG id is considered new
        int id = getApptKey(hhRecord);
        if (id != -1 )
        {
            try {
                appt = getRecordById(id);
            }
            catch (Exception e) {
            }
        }

        if (appt == null) { // if there is no pc rec with the matching RecID

            if (hhRecord.isArchived() || hhRecord.isDeleted()) {
                deleteHHRecord(hhRecord);
            }
            else { //default

                //reset the attribute flags for the record
                resetAttributes(hhRecord);

                //add it to the pc records Vector
                int hhid = hhRecord.getId();
                appt = palmToBorg(hhRecord);
                
                addPCRecord(appt);
                hhRecord = borgToPalm(appt);
                hhRecord.setId(hhid);
                // update hh with key from borg
                writeHHRecord(hhRecord);

            }
        }
        else {

            if (hhRecord.isArchived() || hhRecord.isDeleted()) {
                handleDeleted(hhRecord, appt);
            }
            else if (hhRecord.isModified() || hhRecord.isNew())
                handleModified(hhRecord, appt);

        }
    }

    public void handleModified(DateRecord hhRecord, Appointment appt)
            throws Exception {

        // Record exists on both HH and PC
        if (appt.getDeleted() || !appt.getModified()) {
            
            // check for date change 
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(hhRecord.getStartDate());
            cal.set( Calendar.SECOND, 0);
            cal.set( Calendar.MILLISECOND, 0);
            Date fixdate = cal.getTime();
            
            if( !appt.getDeleted() && (appt.getDate().getTime() != fixdate.getTime()))
            {
                Log.out("date chg: " + appt.getText() + " " + appt.getDate() + "!=" + fixdate);
                Log.out( appt.getDate().getTime() + "!=" + fixdate.getTime());
                Appointment modappt = palmToBorg(hhRecord);
                AppointmentModel.getReference().delAppt(appt);
                AppointmentModel.getReference().saveAppt(modappt,true);
                
                deleteHHRecord(hhRecord);
                
            }
            else
            {               
                resetAttributes(hhRecord);
                writeHHRecord(hhRecord);
                
                Appointment modaddr = palmToBorg(hhRecord);
                modaddr.setKey(appt.getKey());
                resetPCAttributes(modaddr);
            }
        }
        else if (compareRecords(hhRecord, appt)) {
            // both records have changed identically
            resetAttributes(hhRecord);
            resetPCAttributes(appt);
        }
        else { // records are different
            // Change the PC record to a new record so
            // that it gets added to the HH on pass thru pc records
            resetPCAttributes(appt);
            appt.setNew(true);
            AppointmentModel.getReference().syncSave(appt);

            resetAttributes(hhRecord);

            // Add the HH record to the PC table
            appt = palmToBorg(hhRecord);
            int hhid = hhRecord.getId();
            addPCRecord(appt);
            hhRecord = borgToPalm(appt);
            hhRecord.setId(hhid);
            writeHHRecord(hhRecord);
        }

    }

    public void handleDeleted(Record hhRecord, Appointment addr)
            throws Exception {

        if (addr.getModified()) {
            // (HH = Delete and PC = Modified) causes HH record to be updated
            // not deleted
            resetPCAttributes(addr);
            int hhid = hhRecord.getId();
            hhRecord = borgToPalm(addr);
            hhRecord.setId(hhid);
            writeHHRecord(hhRecord);
        }
        else {
            // Marked as already deleted from the HH and needs
            // to be deleted from the PC
            deleteHHRecord(hhRecord);
            deletePCRecord(addr);
        }
    }

    private Appointment getRecordById(int id) throws Exception {
        return (AppointmentModel.getReference().getAppt(id));
    }

    private void writeHHRecord(Record record) throws SyncException, IOException {
        SyncManager.writeRec(db, record);
    }

    private void deleteHHRecord(Record record) throws SyncException {
        SyncManager.deleteRecord(db, record);
    }

    private DateRecord retrieveHHRecordByBorgId(int key) throws IOException {

        //get record count on the database
        int rc = SyncManager.getDBRecordCount(db);

        for (int ri = 0; ri < rc; ri++) {

            DateRecord hhRecord = new DateRecord();
            hhRecord.setIndex(ri);
            SyncManager.readRecordByIndex(db, hhRecord);

            if (key == getApptKey(hhRecord))
                return (hhRecord);

        }

        return null;
    }

    private int addPCRecord(Appointment appt) throws Exception {

        AppointmentModel.getReference().syncSave(appt);
        return (appt.getKey());
    }

    private void deletePCRecord(Appointment appt) throws Exception {
        AppointmentModel.getReference().forceDelete(appt);
    }

    private void resetPCAttributes(Appointment appt) throws Exception {

        appt.setModified(false);
        appt.setDeleted(false);
        appt.setNew(false);
        AppointmentModel.getReference().syncSave(appt);
    }

    private void resetAttributes(Record record) {

        record.setIsModified(false);
        record.setIsArchived(false);
        record.setIsDeleted(false);
        record.setIsNew(false);
    }

    private boolean compareRecords(Record firstRecord, Appointment addr) {
        DateRecord secondRecord = borgToPalm(addr);
        return firstRecord.equals(secondRecord);
    }

    static private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    static final public int REPEAT_NO_REPEAT = 0, 
    REPEAT_DAILY = 1,
    REPEAT_WEEKLY = 2,
    REPEAT_MONTHLY_BY_DAY = 3, 
    REPEAT_MONTHLY_BY_DATE = 4,
    REPEAT_YEARLY_BY_DATE = 5,
    REPEAT_YEARLY_BY_DAY = 6,
    REPEAT_BAD_BRAND = 7;
    
    static public DateRecord borgToPalm(Appointment appt) {

        DateRecord rec = new DateRecord();
        Date d = appt.getDate();
        rec.setStartDate(d);
        Integer dur = appt.getDuration();
        if (dur != null && dur.intValue() != 0) {
            long t = d.getTime();
            t += dur.intValue() * 60 * 1000;
            rec.setEndDate(new Date(t));
        }
        else if (!AppointmentModel.isNote(appt)){
            long t = d.getTime();
            t += 30 * 60 * 1000;
            rec.setEndDate(new Date(t));
        }
        else
        {
            rec.setIsUntimed(true);
            rec.setEndDate(d);
        }
        
        rec.setDescription(appt.getText());
        
        //
        // NOTE == key,HVT,color,category
        //

        String note = Integer.toString(appt.getKey()) + ",";
        if (appt.getHoliday() != null && appt.getHoliday().intValue() != 0)
            note += "H";

        if (appt.getVacation() != null && appt.getVacation().intValue() != 0)
            note += "V";

        if (appt.getPrivate())
            rec.setIsPrivate(true);

        if (appt.getTodo())
            note += "T";
        
        note += ",";
        
        if( appt.getColor() != null )
        {
            note += appt.getColor();
        }
            
        note += ",";
        
        if( appt.getCategory() != null )
        {
            note += appt.getCategory();
        }
        
        rec.setNote(note);

        String alm = appt.getAlarm();
        if( alm != null && alm.equals("Y"))
        {
            rec.setIsAlarmed(true);
            rec.setAlarmAdvanceTime(5);
        }
        
        // repeat stuff
        if (appt.getRepeatFlag()) {
            rec.setIsRepeating(true);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(d);
            Calendar untilcal = Repeat.until(gc, appt.getFrequency(), appt.getTimes().intValue());
            if( untilcal == null )
            {
                Log.out(appt.getKey() + " " + appt.getFrequency() + " " + appt.getTimes().intValue());
            }
            else
            {
                rec.setRepeatEndDate(untilcal.getTime());
            }
            
            String freq = appt.getFrequency();
            if( freq == null || freq.equals(""))
                freq = "daily";
            
            if (freq.equals("daily")) {
                rec.setRepeatType(REPEAT_DAILY);
            }
            else if (freq.equals("weekly")) {
                rec.setRepeatType(REPEAT_DAILY);
                rec.setRepeatFrequency(7);
            }
            else if (freq.equals("weekdays")) {
                rec.setRepeatType(REPEAT_WEEKLY);
                int days[] = {1,2,3,4,5};
                rec.setRepeatOn( days);
            }
            else if (freq.equals("mwf")) {
                rec.setRepeatType(REPEAT_WEEKLY);
                int days[] = {1,3,5};
                rec.setRepeatOn( days);
            }
            else if (freq.equals("tth")) {
                rec.setRepeatType(REPEAT_WEEKLY);
                int days[] = {2,4};
                rec.setRepeatOn( days);
            }
            else if (freq.equals("weekends")) {
                rec.setRepeatType(REPEAT_WEEKLY);
                int days[] = {0,6};
                rec.setRepeatOn( days);
            }
            else if (freq.equals("biweekly")) {
                rec.setRepeatType(REPEAT_DAILY);
                rec.setRepeatFrequency(14);
            }
            else if (freq.equals("monthly")) {
                rec.setRepeatType(REPEAT_MONTHLY_BY_DATE);
            }
            else if (freq.equals("yearly")) {
                rec.setRepeatType(REPEAT_YEARLY_BY_DATE);
            }
        }

        rec.setId(0);

        rec.setIsNew(appt.getNew());
        rec.setIsDeleted(appt.getDeleted());
        rec.setIsModified(appt.getModified());
        return rec;
    }

    static int getApptKey( DateRecord hh )
    {
        String notes = hh.getNote();
        if( notes != null )
        {
        
            int idx = notes.indexOf(",");
            if( idx != -1 )
            {
                String id = notes.substring(0,idx);
                try{
                    int i = Integer.parseInt(id);
                    return(i);
                }
                catch(Exception e){}
                
            }
        }
        
        return(-1);
        
    }
    
    
    static public Appointment palmToBorg(DateRecord hh) {
        Appointment appt = AppointmentModel.getReference().newAppt();
        appt.setDate(hh.getStartDate());
        String notes = hh.getNote();
        appt.setKey( getApptKey(hh));
        if( notes != null )
        {
        
            // java 1.3 must be used --> has no String.split()
            StringTokenizer tok = new StringTokenizer(notes,",");
            String part = tok.nextToken(); // key
            if( tok.hasMoreTokens())
            {
                part = tok.nextToken();
                if( part.indexOf("H") != -1 )
                    appt.setHoliday(new Integer(1));
                if( part.indexOf("T") != -1 )
                    appt.setTodo(true);
                if( part.indexOf("V") != -1 )
                    appt.setVacation(new Integer(1));  
                
                if( tok.hasMoreTokens())
                {
                    part = tok.nextToken();
                    if( !part.equals(""))
                    {
                        appt.setColor(part);
                    }
                    
                    if( tok.hasMoreTokens())
                    {
                        part = tok.nextToken();
                        if( !part.equals(""))
                        {
                            appt.setCategory(part);
                        }                                              
                    }
                }
            }
        }  
            

        
        appt.setText(hh.getDescription());
        
        if( hh.getIsAlarmed())
            appt.setAlarm("Y");
        
        appt.setPrivate(hh.isPrivate());
        
        if( !hh.getIsUntimed())
        {
            long t = hh.getEndDate().getTime() - hh.getStartDate().getTime();
            appt.setDuration(new Integer((int)t/60000));
        }
        
        int rtype = hh.getRepeatType();
        int freq = hh.getRepeatFrequency();
        Date until = hh.getRepeatEndDate();
        int days = 9999;
        int months = 9999;
        int years = 9999;
        if( until != null )
        {
        	Date d = hh.getStartDate();
        	long t = until.getTime() - d.getTime();
        	//Log.out( d + " " + until + " " + t);
        	GregorianCalendar cal = new GregorianCalendar();
        	cal.setTime(d);
        	int m1 = cal.get(cal.MONTH);
        	int y1 = cal.get(cal.YEAR);
        	cal.setTime(until);
        	int m2 = cal.get(cal.MONTH);
        	int y2 = cal.get(cal.YEAR);
        	days = (int)(t/(1000*60*60*24));
        	months = (y2-y1)*12 + m2-m1;
        	years = y2-y1;
        }
        //Log.out("days=" + days);
        //Log.out("rt=" + rtype);
        //Log.out("freq=" + freq);
        
        if( (rtype == REPEAT_WEEKLY && freq == 1) ||
        	(rtype == REPEAT_DAILY && freq == 7))
        {
        	appt.setFrequency("weekly");
        	appt.setTimes( new Integer( 1+(days/7)));
        	appt.setRepeatFlag(true);
        }
        else if( (rtype == REPEAT_WEEKLY && freq == 2) ||
        		 (rtype == REPEAT_DAILY && freq == 14))
        {
           	appt.setFrequency("biweekly");
        	appt.setTimes( new Integer( 1+(days/14)));
        	appt.setRepeatFlag(true);
        }
        else if( rtype == REPEAT_DAILY && freq == 1)
        {
          	appt.setFrequency("daily");
        	appt.setTimes( new Integer( 1+days));
        	appt.setRepeatFlag(true);
        }
        else if( rtype == REPEAT_MONTHLY_BY_DATE && freq == 1)
        {
          	appt.setFrequency("monthly");
        	appt.setTimes( new Integer( 1+months));
        	appt.setRepeatFlag(true);
        }
        else if( (rtype == REPEAT_MONTHLY_BY_DATE && freq == 12) ||
                 (rtype == REPEAT_YEARLY_BY_DATE ))
        {
          	appt.setFrequency("yearly");
        	appt.setTimes( new Integer( 1+years));
        	appt.setRepeatFlag(true);
        }
        	
        	
        if( days == 9999 )
        	appt.setTimes( new Integer(9999));
        

        appt.setNew(hh.isNew());
        appt.setDeleted(hh.isDeleted());
        appt.setModified(hh.isModified());

        return appt;
    }
}