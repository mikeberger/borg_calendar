package net.sf.borg.memoconduit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sf.borg.model.MemoModel;
import net.sf.borg.model.beans.Memo;
import palm.conduit.MemoRecord;
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

    public void SyncData() throws Exception {

	MemoRecord hhRecord;

	// if pc wipes hh - delete hh db first
	if (props.syncType == SyncProperties.SYNC_PC_TO_HH) {
	    SyncManager.purgeAllRecs(db);
	}

	// get list of hh ids
	ArrayList hhids = new ArrayList();
	int recordCount = SyncManager.getDBRecordCount(db);
	for (int recordIndex = 0; recordIndex < recordCount; recordIndex++) {
	    hhRecord = new MemoRecord();
	    hhRecord.setIndex(recordIndex);
	    SyncManager.readRecordByIndex(db, hhRecord);
	    hhids.add(new Integer(hhRecord.getId()));
	}

	// check for empty BORG db
	MemoModel amod = MemoModel.getReference();

	// get record count on the database
	recordCount = SyncManager.getDBRecordCount(db);

	Iterator it = hhids.iterator();
	while (it.hasNext()) {

	    Integer id = (Integer) it.next();
	    hhRecord = new MemoRecord();
	    hhRecord.setId(id.intValue());
	    SyncManager.readRecordById(db, hhRecord);

	    // Synchronize the record obtained from the handheld
	    synchronizeHHRecord(hhRecord);
	}

	Collection memos = amod.getMemos();
	it = memos.iterator();
	while (it.hasNext()) {

	    Memo addr = (Memo) it.next();
	    synchronizePCRecord(addr);

	}

	memos = amod.getDeletedMemos();
	it = memos.iterator();
	while (it.hasNext()) {

	    Memo addr = (Memo) it.next();
	    synchronizePCRecord(addr);

	}
	SyncManager.purgeDeletedRecs(db); // deletes all hh records marked
	// as deleted
	SyncManager.resetSyncFlags(db); // reset all the sync flags on the hh

    }

    public void synchronizePCRecord(Memo m) throws Exception {

	MemoRecord hhRecord = null;

	if (!m.getNew()) {

	    if( m.getPalmId() != null )
	    {
		hhRecord = retrieveHHRecordByBorgId(m.getPalmId().intValue());
	    }
	    

	    if (hhRecord == null) {
		if (m.getDeleted()) {
		    deletePCRecord(m);
		} else {
		    resetPCAttributes(m);
		    // hhRecords.addElement(borgToPalm(addr));
		    MemoRecord rec = borgToPalm(m);
		    writeHHRecord(rec);
		    m.setPalmId(new Integer(rec.getId()));
		    MemoModel.getReference().saveMemo(m, true);
		}
	    } else {

		if (m.getDeleted()) {
		    deletePCRecord(m);
		    deleteHHRecord(hhRecord);
		} else if (m.getModified()) {
		    resetPCAttributes(m);
		    int hhid = hhRecord.getId();
		    hhRecord = borgToPalm(m);
		    hhRecord.setId(hhid);
		    // hhRecords.addElement(hhRecord);
		    writeHHRecord(hhRecord);
		}

	    }
	} else if (m.getDeleted()) {
	    deletePCRecord(m);
	} else {
	    resetPCAttributes(m);
	    // hhRecords.addElement(borgToPalm(addr));
	    MemoRecord rec = borgToPalm(m);
	    writeHHRecord(rec);
	    m.setPalmId(new Integer(rec.getId()));
	    MemoModel.getReference().saveMemo(m, true);
	}

    }

    public void synchronizeHHRecord(MemoRecord hhRecord) throws Exception {

	Memo m = null;

	try {
	    m = getRecordById(hhRecord.getId());
	} catch (Exception e) {

	}

	if (m == null) { // if there is no pc rec with the matching RecID

	    if (hhRecord.isArchived() || hhRecord.isDeleted()) {
		deleteHHRecord(hhRecord);
	    } else { // default

		// reset the attribute flags for the record
		resetAttributes(hhRecord);

		// add it to the pc records Vector
		addPCRecord(palmToBorg(hhRecord));
		writeHHRecord(hhRecord);

	    }
	} else {

	    if (hhRecord.isArchived() || hhRecord.isDeleted()) {
		handleDeleted(hhRecord, m);
	    } else if (hhRecord.isModified() || hhRecord.isNew())
		handleModified(hhRecord, m);

	}
    }

    public void handleModified(MemoRecord hhRecord, Memo m) throws Exception {

	// Record exists on both HH and PC
	if (m.getDeleted() || !m.getModified()) {

	    resetAttributes(hhRecord);

	    // hhRecords.addElement(hhRecord);
	    writeHHRecord(hhRecord);

	    Memo modaddr = palmToBorg(hhRecord);
	    MemoModel.getReference().saveMemo(modaddr, true);
	    
	} else if (compareRecords(hhRecord, m)) {
	    // both records have changed identically
	    resetAttributes(hhRecord);
	    writeHHRecord(hhRecord);
	    // hhRecords.addElement(hhRecord);
	    resetPCAttributes(m);
	} else { // records are different
	    // Change the PC record to a new record so
	    // that it gets added to the HH on pass thru pc records
	    resetPCAttributes(m);
	    m.setNew(true);
	    MemoModel.getReference().saveMemo(m, true);

	    resetAttributes(hhRecord);

	    // Add the HH record to the PC table
	    addPCRecord(palmToBorg(hhRecord));
	    writeHHRecord(hhRecord);
	}

    }

    public void handleDeleted(Record hhRecord, Memo m) throws Exception {

	if (m.getModified() && !m.getDeleted()) {
	    // (HH = Delete and PC = Modified) causes HH record to be
	    // updated not deleted
	    resetPCAttributes(m);
	    int hhid = hhRecord.getId();
	    hhRecord = borgToPalm(m);
	    hhRecord.setId(hhid);
	    // hhRecords.addElement(hhRecord);
	    writeHHRecord(hhRecord);
	} else {
	    // Marked as already deleted from the HH and needs
	    // to be deleted from the PC
	    deleteHHRecord(hhRecord);
	    deletePCRecord(m);
	}
    }

    private Memo getRecordById(int id) throws Exception {
	return (MemoModel.getReference().getMemoByPalmId(id));
    }

    private void writeHHRecord(Record record) throws SyncException, IOException {
	MemoCond.log("write Palm record - " + record.toString());
	SyncManager.writeRec(db, record);
    }

    private void deleteHHRecord(Record record) throws SyncException {
	MemoCond.log("delete Palm record - " + record.toString());
	SyncManager.deleteRecord(db, record);
    }

    private MemoRecord retrieveHHRecordByBorgId(int key)  {

	MemoRecord hhRecord = new MemoRecord();
	hhRecord.setId(key);
	try{
	SyncManager.readRecordById(db, hhRecord);
	}
	catch( IOException e)
	{
	    return null;
	}
	return hhRecord;
    }

    private void addPCRecord(Memo addr) throws Exception {
	MemoCond.log("add BORG record - " + addr.getKey());
	MemoModel.getReference().saveMemo(addr, true);
    }

    private void deletePCRecord(Memo addr) throws Exception {
	MemoCond.log("delete BORG record - " + addr.getMemoName());
	MemoModel.getReference().forceDelete(addr);
    }

    private void resetPCAttributes(Memo addr) throws Exception {

	// skip write to PC record if already reset
	if (addr.getModified() == false && addr.getDeleted() == false
		&& addr.getNew() == false)
	    return;
	addr.setModified(false);
	addr.setDeleted(false);
	addr.setNew(false);
	MemoModel.getReference().saveMemo(addr, true);
    }

    private void resetAttributes(Record record) {

	record.setIsModified(false);
	record.setIsArchived(false);
	record.setIsDeleted(false);
	record.setIsNew(false);
    }

    private boolean compareRecords(Record firstRecord, Memo addr) {
	MemoRecord secondRecord = borgToPalm(addr);
	return firstRecord.equals(secondRecord);
    }


    static public MemoRecord borgToPalm(Memo m) {

	MemoRecord rec = new MemoRecord();
	rec.setId(0);
	rec.setMemo(m.getMemoName() + "\n" + m.getMemoText());
	rec.setIsNew(m.getNew());
	rec.setIsDeleted(m.getDeleted());
	rec.setIsModified(m.getModified());
	rec.setIsPrivate(m.getPrivate());
	return rec;
    }

    static public Memo palmToBorg(MemoRecord hh) {
	Memo m = new Memo();
	String text = hh.getMemo();
	int ii = text.indexOf('\n');
        if (ii != -1)
        {
            m.setMemoName(text.substring(0, ii));
            m.setMemoText(text.substring(ii+1));
        }
        else
        {
            m.setMemoName(Integer.toString(hh.getId()));
            m.setMemoText(text);
        }
	
	m.setPalmId(new Integer(hh.getId()));
	
	m.setNew(hh.isNew());
	m.setDeleted(hh.isDeleted());
	m.setModified(hh.isModified());
	
	m.setPrivate(hh.isPrivate());

	return m;
    }
}
