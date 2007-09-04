package net.sf.borg.apptconduit;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.beans.Appointment;
import palm.conduit.DateRecord;
import palm.conduit.Log;
import palm.conduit.Record;
import palm.conduit.SyncException;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."


public class ApptRecordManager {
    private static SimpleDateFormat normalDateFormat_ = new SimpleDateFormat( "MM/dd/yyyy hh:mm aa" );
    SyncProperties props;

    int db;

    HashMap hhmap = new HashMap();

    public ApptRecordManager(SyncProperties props, int db) {
	this.props = props;
	this.db = db;
    }

    public void quickSyncAndWipe() throws Exception {

	Log.out("Begin Quick Appt Sync from HH to PC only.");
	int count = 0;
	boolean allRecordsRead = false;
	while (!allRecordsRead) {

	    try {
		DateRecord hhRecord = new DateRecord();
		SyncManager.readNextModifiedRec(db, hhRecord);
		synchronizeHHRecord(hhRecord);
		count++;
	    } catch (SyncException e) {
		allRecordsRead = true;
	    }
	}
	Log.out("Synced " + count
		+ " records. Begin Wipe and Copy from PC to HH.");
	WipeData();
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

	// get list of hh ids of interest
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

	    if (borgid != -1) {
		hhmap.put(new Integer(borgid), new Integer(recordIndex));
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
	SyncManager.purgeDeletedRecs(db); // deletes all hh records marked
	// as
	// deleted
	SyncManager.resetSyncFlags(db); // reset all the sync flags on the hh
    }

    public void synchronizePCRecord(Appointment appt) throws Exception {

	DateRecord hhRecord;

	ApptCond.log("Sync BORG Record with Palm: " + appt.getKey() + " "
		+ appt.getText() + "(" + appt.getNew() + "," + appt.getModified() + "," + appt.getDeleted() + ")");

	if (!appt.getNew()) {

	    hhRecord = retrieveHHRecordByBorgId(appt.getKey());

	    if (hhRecord == null) {
		if (appt.getDeleted()) {
		    deletePCRecord(appt);
		} else {
		    resetPCAttributes(appt);
		    writeHHRecord(borgToPalm(appt));
		}
	    } else {

		if (appt.getDeleted()) {
		    deletePCRecord(appt);
		    deleteHHRecord(hhRecord);
		} else if (appt.getModified()) {
		    resetPCAttributes(appt);
		    int hhid = hhRecord.getId();
		    hhRecord = borgToPalm(appt);
		    hhRecord.setId(hhid);
		    writeHHRecord(hhRecord);
		}

	    }
	} else if (appt.getDeleted()) {
	    deletePCRecord(appt);
	} else {
	    resetPCAttributes(appt);
	    writeHHRecord(borgToPalm(appt));
	}

    }

    public void synchronizeHHRecord(DateRecord hhRecord) throws Exception {

	Appointment appt = null;
	int[] rptOn = hhRecord.getRepeatOn();
	hhRecord.setRepeatOn(rptOn); // work-around for java conduit bug
	ApptCond.log("Sync Palm Record with BORG: "
		+ hhRecord.toFormattedString());
	hhRecord.setRepeatOn(rptOn); // work-around for java conduit bug

	// any record without a BORG id is considered new
	int id = getApptKey(hhRecord);
	if (id != -1) {
	    try {
		appt = getRecordById(id);
	    } catch (Exception e) {
	    }
	}

	if (appt == null) { // if there is no pc rec with the matching RecID

	    if (hhRecord.isArchived() || hhRecord.isDeleted()) {
		deleteHHRecord(hhRecord);
	    } else { // default

		// reset the attribute flags for the record
		resetAttributes(hhRecord);

		// add it to the pc records Vector
		int hhid = hhRecord.getId();
		appt = palmToBorg(hhRecord);

		addPCRecord(appt);
		hhRecord = borgToPalm(appt);
		hhRecord.setId(hhid);
		// update hh with key from borg
		writeHHRecord(hhRecord);

	    }
	} else {

	    if (hhRecord.isArchived() || hhRecord.isDeleted()) {
		handleDeleted(hhRecord, appt);
	    } else if (hhRecord.isModified() || hhRecord.isNew())
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
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    Date fixdate = cal.getTime();

	    if (!appt.getDeleted()
		    && (appt.getDate().getTime() != fixdate.getTime())) {
		ApptCond.log("date chg: " + appt.getText() + " "
			+ appt.getDate() + "!=" + fixdate);
		ApptCond.log(appt.getDate().getTime() + "!="
			+ fixdate.getTime());
		Appointment modappt = palmToBorg(hhRecord);
		AppointmentModel.getReference().delAppt(appt);
		AppointmentModel.getReference().saveAppt(modappt, true);

		deleteHHRecord(hhRecord);

	    } else {
		resetAttributes(hhRecord);
		writeHHRecord(hhRecord);

		Appointment modaddr = palmToBorg(hhRecord);
		modaddr.setKey(appt.getKey());
		modaddr.setModified(false);
		modaddr.setDeleted(false);
		modaddr.setNew(false);
		AppointmentModel.getReference().syncSave(modaddr);

	    }
	} else if (compareRecords(hhRecord, appt)) {
	    // both records have changed identically
	    resetAttributes(hhRecord);
	    resetPCAttributes(appt);
	} else { // records are different
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

	if (addr.getModified() && !addr.getDeleted()) {
	    // (HH = Delete and PC = Modified) causes HH record to be
	    // updated
	    // not deleted
	    resetPCAttributes(addr);
	    int hhid = hhRecord.getId();
	    hhRecord = borgToPalm(addr);
	    hhRecord.setId(hhid);
	    writeHHRecord(hhRecord);
	} else {
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

	Integer hhkey = (Integer) hhmap.get(new Integer(key));
	if (hhkey != null) {
	    ApptCond.log("Found key in cache: " + key);
	    DateRecord hhRecord = new DateRecord();
	    hhRecord.setIndex(hhkey.intValue());
	    SyncManager.readRecordByIndex(db, hhRecord);
	    if (key == getApptKey(hhRecord)) {
		ApptCond.log("Found matching HH Record in cache: " + key);
		return (hhRecord);
	    }
	}

	ApptCond.log("Did not find key in cache: " + key);

	// get record count on the database
	int rc = SyncManager.getDBRecordCount(db);

	for (int ri = 0; ri < rc; ri++) {

	    DateRecord hhRecord = new DateRecord();
	    hhRecord.setIndex(ri);
	    SyncManager.readRecordByIndex(db, hhRecord);

	    if (key == getApptKey(hhRecord)) {
		hhmap.put(new Integer(key), new Integer(ri));
		ApptCond.log("Found matching HH Record in cache: " + key);
		return (hhRecord);
	    }
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

	// skip write to PC record if already reset
	if (appt.getModified() == false && appt.getDeleted() == false
		&& appt.getNew() == false)
	    return;

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

    static final public int REPEAT_NO_REPEAT = 0, REPEAT_DAILY = 1,
	    REPEAT_WEEKLY = 2, REPEAT_MONTHLY_BY_DAY = 3,
	    REPEAT_MONTHLY_BY_DATE = 4, REPEAT_YEARLY_BY_DATE = 5,
	    REPEAT_YEARLY_BY_DAY = 6, REPEAT_BAD_BRAND = 7;

    static public DateRecord borgToPalm(Appointment appt) {

	DateRecord rec = new DateRecord();
	Date d = appt.getDate();
	rec.setStartDate(d);
	Integer dur = appt.getDuration();
	if (dur != null && dur.intValue() != 0) {
	    long t = d.getTime();
	    t += dur.intValue() * 60 * 1000;
	    rec.setEndDate(new Date(t));
	} else if (!AppointmentModel.isNote(appt)) {
	    long t = d.getTime();
	    t += 30 * 60 * 1000;
	    rec.setEndDate(new Date(t));
	} else {
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

	if (appt.getColor() != null) {
	    note += appt.getColor();
	}

	note += ",";

	if (appt.getCategory() != null) {
	    note += appt.getCategory();
	}
	
	note += ",";
	if( appt.getNextTodo() != null)
	{
	    note += normalDateFormat_.format(appt.getNextTodo());
	}

	note += ",";
	if( appt.getReminderTimes() != null )
	{
	    note += appt.getReminderTimes();
	}
	
	note += ",";
	if( Repeat.getRptNum(appt.getFrequency()))
	{
	    note += "Y";
	}	
	
	rec.setNote(note);

	Vector sl = appt.getSkipList();
	
	if( sl != null )
	{	
	    Vector de = new Vector();
	    for( int i = 0; i < sl.size(); i++)
	    {
		String ks = (String) sl.get(i);
		int key = Integer.parseInt(ks);
		de.add(AppointmentModel.dateFromKey(key));
	    }
	    rec.setDateExceptions(de);
	}
	
	String alm = appt.getAlarm();
	if (alm != null && alm.equals("Y")) {
	    rec.setIsAlarmed(true);
	    rec.setAlarmAdvanceTime(5);
	}

	// repeat stuff
	if (appt.getRepeatFlag()) {
	    rec.setIsRepeating(true);
	    GregorianCalendar gc = new GregorianCalendar();
	    gc.setTime(d);
	    Calendar untilcal = Repeat.until(gc, appt.getFrequency(), appt
		    .getTimes().intValue());
	    if (untilcal == null) {
		Log.out(appt.getKey() + " " + appt.getFrequency() + " "
			+ appt.getTimes().intValue());
	    } else {
		rec.setRepeatEndDate(untilcal.getTime());
	    }

	    String freq = Repeat.getFreq(appt.getFrequency());
	    if (freq == null || freq.equals(""))
		freq = Repeat.DAILY;

	    if (freq.equals(Repeat.DAILY)) {
		rec.setRepeatType(REPEAT_DAILY);
	    } else if (freq.equals(Repeat.WEEKLY)) {
		rec.setRepeatType(REPEAT_DAILY);
		rec.setRepeatFrequency(7);
	    } else if (freq.equals(Repeat.WEEKDAYS)) {
		rec.setRepeatType(REPEAT_WEEKLY);
		int days[] = { 1, 2, 3, 4, 5 };
		rec.setRepeatOn(days);
	    } else if (freq.equals(Repeat.MWF)) {
		rec.setRepeatType(REPEAT_WEEKLY);
		int days[] = { 1, 3, 5 };
		rec.setRepeatOn(days);
	    } else if (freq.equals(Repeat.TTH)) {
		rec.setRepeatType(REPEAT_WEEKLY);
		int days[] = { 2, 4 };
		rec.setRepeatOn(days);
	    } else if (freq.equals(Repeat.WEEKENDS)) {
		rec.setRepeatType(REPEAT_WEEKLY);
		int days[] = { 0, 6 };
		rec.setRepeatOn(days);
	    } else if (freq.equals(Repeat.BIWEEKLY)) {
		rec.setRepeatType(REPEAT_DAILY);
		rec.setRepeatFrequency(14);
	    } else if (freq.equals(Repeat.MONTHLY)) {
		rec.setRepeatType(REPEAT_MONTHLY_BY_DATE);
	    } else if (freq.equals(Repeat.YEARLY)) {
		rec.setRepeatType(REPEAT_YEARLY_BY_DATE);
	    } else if (freq.equals(Repeat.NDAYS)) {
		int incr = Repeat.getNDays(appt.getFrequency());
		rec.setRepeatType(REPEAT_DAILY);
		rec.setRepeatFrequency(incr);
	    } else if( freq.equals(Repeat.DAYLIST))
	    {
		rec.setRepeatType(REPEAT_WEEKLY);
		Collection daylist = Repeat.getDaylist(appt.getFrequency());
		if( daylist.contains(new Integer(Calendar.MONDAY)))
		{
		    rec.addRepeatOnDay(1);
		}
		if( daylist.contains(new Integer(Calendar.TUESDAY)))
		{
		    rec.addRepeatOnDay(2);
		}
		if( daylist.contains(new Integer(Calendar.WEDNESDAY)))
		{
		    rec.addRepeatOnDay(3);
		}
		if( daylist.contains(new Integer(Calendar.THURSDAY)))
		{
		    rec.addRepeatOnDay(4);
		}
		if( daylist.contains(new Integer(Calendar.FRIDAY)))
		{
		    rec.addRepeatOnDay(5);
		}
		if( daylist.contains(new Integer(Calendar.SATURDAY)))
		{
		    rec.addRepeatOnDay(6);
		}
		if( daylist.contains(new Integer(Calendar.SUNDAY)))
		{
		    rec.addRepeatOnDay(0);
		}
		
	    }
	}

	rec.setId(0);

	rec.setIsNew(false);
	rec.setIsDeleted(false);
	rec.setIsModified(false);
	return rec;
    }

    static int getApptKey(DateRecord hh) {
	String notes = hh.getNote();
	if (notes != null) {

	    int idx = notes.indexOf(",");
	    if (idx != -1) {
		String id = notes.substring(0, idx);
		try {
		    int i = Integer.parseInt(id);
		    return (i);
		} catch (Exception e) {
		}

	    }
	}

	return (-1);

    }

    static public Appointment palmToBorg(DateRecord hh) {
	Appointment appt = AppointmentModel.getReference().newAppt();
	appt.setDate(hh.getStartDate());
	String notes = hh.getNote();
	appt.setKey(getApptKey(hh));
	boolean showRptNum = false;
	if (notes != null) {

	    // java 1.3 must be used --> has no String.split()
	    // also need to pass arg 3 to work around stupid bug with
                // StringTokenizer
	    StringTokenizer tok = new StringTokenizer(notes, ",", true);
	    String part = tok.nextToken(); // key
	    tok.nextToken(); // comma
	    for (int col = 1; tok.hasMoreTokens();) {
		part = tok.nextToken();

		if (part.equals(",")) {
		    col++;
		    continue;
		}

		ApptCond.log("Note token " + col + ":" + part);

		if (col == 1) // holiday/todo/vacation
		{
		    if (part.indexOf("H") != -1) {
			appt.setHoliday(new Integer(1));
		    }
		    if (part.indexOf("T") != -1) {
			appt.setTodo(true);
		    }
		    if (part.indexOf("V") != -1) {
			appt.setVacation(new Integer(1));
		    }
		} else if (col == 2) // color
		{
		    if (!part.equals("")) {
			appt.setColor(part);
		    }
		} else if (col == 3) // category
		{
		    if (!part.equals("")) {
			appt.setCategory(part);
		    }
		} else if (col == 4) // next todo
		{
		    if (!part.equals("")) {
			
			try {
			    Date d = normalDateFormat_.parse(part);
			    appt.setNextTodo(d);
			} catch (ParseException e) {
			    ApptCond.log("Error: could not parse dateL " + part);
			}
			
		    }
		} else if (col == 5) // reminders
		{
		    if (!part.equals("")) {
			appt.setReminderTimes(part);
		    }
		}
		else if( col == 6 ) // show rpt num
		{
		    if( part.equals("Y"))
			showRptNum = true;
		}
		
	    }
	}
	
	Vector de = hh.getDateExceptions();
	Vector sl = new Vector();
	Calendar cal = new GregorianCalendar();
	if( de != null )
	{
	    for( int i = 0; i < de.size(); i++ )
	    {
		cal.setTime((Date)de.get(i));
		int dkey = AppointmentModel.dkey(cal);
		sl.add(Integer.toString(dkey));
	    }
	    appt.setSkipList(sl);
	}

	appt.setText(hh.getDescription());

	if (hh.getIsAlarmed())
	    appt.setAlarm("Y");

	appt.setPrivate(hh.isPrivate());

	if (!hh.getIsUntimed()) {
	    long t = hh.getEndDate().getTime() - hh.getStartDate().getTime();
	    appt.setDuration(new Integer((int) t / 60000));
	}

	int rtype = hh.getRepeatType();
	int freq = hh.getRepeatFrequency();
	Date until = hh.getRepeatEndDate();
	int days = 9999;
	int months = 9999;
	int years = 9999;
	if (until != null) {
	    Date d = hh.getStartDate();
	    long t = until.getTime() - d.getTime();
	    // Log.out( d + " " + until + " " + t);
	    //GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(d);
	    int m1 = cal.get(Calendar.MONTH);
	    int y1 = cal.get(Calendar.YEAR);
	    cal.setTime(until);
	    int m2 = cal.get(Calendar.MONTH);
	    int y2 = cal.get(Calendar.YEAR);
	    days = (int) (t / (1000 * 60 * 60 * 24));
	    months = (y2 - y1) * 12 + m2 - m1;
	    years = y2 - y1;
	}
	Log.out("days=" + days);
	Log.out("rt=" + rtype);
	Log.out("freq=" + freq);
	//Log.out("hh=" + hh.toFormattedString());
	int []rptOn = hh.getRepeatOn();
	hh.setRepeatOn(rptOn); // work-around for java conduit bug
	
	Log.out("rptOn.length=" + rptOn.length);
	if( rtype == REPEAT_WEEKLY && freq == 1 && rptOn != null && rptOn.length > 0)
	{
	    String f = Repeat.DAYLIST + ",";
	    
	    for( int i = 0; i < rptOn.length; i++ )
	    {
		Log.out("rptOn " + i + "=" + rptOn[i]);
		if( rptOn[i] == 0 )
			f += "1";
		else if(rptOn[i] == 1 )
			f += "2";
		else if( rptOn[i] == 2)
			f += "3";
		else if( rptOn[i] == 3)
			f += "4";
		else if( rptOn[i] == 4)
			f += "5";
		else if( rptOn[i] == 5)
			f += "6";
		else if( rptOn[i] == 6)
			f += "7";
	    }
	    appt.setFrequency(f);
	    appt.setTimes(new Integer(1 + (days / 7)));
	    appt.setRepeatFlag(true);
	}

	else if ((rtype == REPEAT_WEEKLY && freq == 1)
		|| (rtype == REPEAT_DAILY && freq == 7)) {
	    appt.setFrequency(Repeat.WEEKLY);
	    appt.setTimes(new Integer(1 + (days / 7)));
	    appt.setRepeatFlag(true);
	} else if ((rtype == REPEAT_WEEKLY && freq == 2)
		|| (rtype == REPEAT_DAILY && freq == 14)) {
	    appt.setFrequency(Repeat.BIWEEKLY);
	    appt.setTimes(new Integer(1 + (days / 14)));
	    appt.setRepeatFlag(true);
	} else if (rtype == REPEAT_DAILY && freq == 1) {
	    appt.setFrequency(Repeat.DAILY);
	    appt.setTimes(new Integer(1 + days));
	    appt.setRepeatFlag(true);
	} else if (rtype == REPEAT_MONTHLY_BY_DATE && freq == 1) {
	    appt.setFrequency(Repeat.MONTHLY);
	    appt.setTimes(new Integer(1 + months));
	    appt.setRepeatFlag(true);
	} else if ((rtype == REPEAT_MONTHLY_BY_DATE && freq == 12)
		|| (rtype == REPEAT_YEARLY_BY_DATE)) {
	    appt.setFrequency(Repeat.YEARLY);
	    appt.setTimes(new Integer(1 + years));
	    appt.setRepeatFlag(true);
	}

	if (days == 9999)
	    appt.setTimes(new Integer(9999));

	if( showRptNum )
	    appt.setFrequency(appt.getFrequency() + ",Y");
	
	appt.setNew(false);
	appt.setDeleted(false);
	appt.setModified(false);

	return appt;
    }
}