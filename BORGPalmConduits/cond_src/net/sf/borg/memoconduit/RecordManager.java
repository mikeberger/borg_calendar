package net.sf.borg.memoconduit;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import net.sf.borg.model.MemoModel;
import net.sf.borg.model.beans.Memo;
import palm.conduit.Log;
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

	private void WipeData() throws Exception {

		SyncManager.purgeAllRecs(db);

		MemoModel amod = MemoModel.getReference();

		Collection memos = amod.getMemos();
		Iterator it = memos.iterator();

		while (it.hasNext()) {
			Memo memo = (Memo) it.next();
			resetPCAttributes(memo);
			writeHHRecord(borgToPalm(memo));
		}

	}

	public void quickSyncAndWipe() throws Exception {

		Log.out("Begin Quick Appt Sync from HH to PC only.");
		int count = 0;
		boolean allRecordsRead = false;
		while (!allRecordsRead) {

			try {
				MemoRecord hhRecord = new MemoRecord();
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

	private void synchronizeHHRecord(MemoRecord hhRecord) throws Exception {

		Memo m = null;

		try {
			m = getRecordByName(hhRecord);
		} catch (Exception e) {

		}

		if (m == null) { // if there is no pc rec with the matching RecID

			if (!hhRecord.isArchived() && !hhRecord.isDeleted()) {

				// reset the attribute flags for the record
				resetAttributes(hhRecord);

				// add it to the pc records Vector
				addPCRecord(palmToBorg(hhRecord));

			}
		} else {

			if (hhRecord.isArchived() || hhRecord.isDeleted()) {
				MemoCond.log("Found deleted Palm record - " + hhRecord.getId());
				if (!m.getModified()) {
					MemoCond.log("BORG record is not modified");
					deletePCRecord(m);
				}

			} else if (hhRecord.isModified() || hhRecord.isNew())
				handleModified(hhRecord, m);

		}
	}

	private void handleModified(MemoRecord hhRecord, Memo m) throws Exception {

		// Record exists on both HH and PC
		if (!m.getModified()) {

			resetAttributes(hhRecord);
			Memo modaddr = palmToBorg(hhRecord);
			MemoModel.getReference().saveMemo(modaddr, true);

		} else if (!compareRecords(hhRecord, m)) {

			resetAttributes(hhRecord);
			addPCRecord(palmToBorg(hhRecord));
		}

	}

	//private Memo getRecordById(int id) throws Exception {
		//return (MemoModel.getReference().getMemoByPalmId(id));
	//}
	
	private Collection memos = null;
	private Memo getRecordByName(MemoRecord rec) throws Exception {
		
		if( memos == null )
		{
			memos = MemoModel.getReference().getMemos();
		}
			
		String text = rec.getMemo();
		int ii = text.indexOf('\n');
		if (ii != -1) {
			String name = text.substring(0, ii);
			Iterator it = memos.iterator();
			while( it.hasNext())
			{
				Memo x = (Memo) it.next();
				if( x.getMemoName().equals(name))
					return x;
			}
		} 
		
		return null;
	}

	private void writeHHRecord(Record record) throws SyncException, IOException {
		//MemoCond.log("write Palm record - " + record.toString());
		SyncManager.writeRec(db, record);
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
		if (addr.getModified() == false && addr.getNew() == false)
			return;
		addr.setModified(false);
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

	static private MemoRecord borgToPalm(Memo m) {

		MemoRecord rec = new MemoRecord();
		rec.setId(0);
		rec.setMemo(m.getMemoName() + "\n" + m.getMemoText());
		rec.setIsNew(m.getNew());
		rec.setIsModified(m.getModified());
		rec.setIsPrivate(m.getPrivate());
		return rec;
	}

	static private Memo palmToBorg(MemoRecord hh) {
		Memo m = new Memo();
		String text = hh.getMemo();
		int ii = text.indexOf('\n');
		if (ii != -1) {
			m.setMemoName(text.substring(0, ii));
			m.setMemoText(text.substring(ii + 1));
		} else {
			m.setMemoName(Integer.toString(hh.getId()));
			m.setMemoText(text);
		}

		m.setPalmId(new Integer(hh.getId()));

		m.setNew(hh.isNew());
		m.setModified(hh.isModified());

		m.setPrivate(hh.isPrivate());

		return m;
	}
}
