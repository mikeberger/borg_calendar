package net.sf.borg.addrconduit;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.sf.borg.model.AddressModel;
import net.sf.borg.model.entity.Address;
import palm.conduit.AddressRecord;
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

	public void quickSyncAndWipe() throws Exception {

		Log.out("Begin Quick Appt Sync from HH to PC only.");
		int count = 0;
		boolean allRecordsRead = false;
		while (!allRecordsRead) {

			try {
				AddressRecord hhRecord = new AddressRecord();
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

	private void WipeData() throws Exception {

		SyncManager.purgeAllRecs(db);

		AddressModel amod = AddressModel.getReference();

		Collection addrs = amod.getAddresses();
		Iterator it = addrs.iterator();

		while (it.hasNext()) {
			Address addr = (Address) it.next();

			resetPCAttributes(addr);
			writeHHRecord(borgToPalm(addr));

		}

	}

	private void synchronizeHHRecord(AddressRecord hhRecord) throws Exception {

		// AddrCond.log("Addr Sync HH: " + hhRecord.getName());
		Address addr = null;
		// any record without a BORG id is considered new
		String id = hhRecord.getCustom(1);
		if (id == null || id.equals(""))
			hhRecord.setIsNew(true);
		else {
			String cus = hhRecord.getCustom(1);
			try {
				int i = Integer.parseInt(cus);
				addr = getRecordById(i);
			} catch (Exception e) {
			}
		}

		if (addr == null) { // if there is no pc rec with the matching RecID

			if (!hhRecord.isArchived() && !hhRecord.isDeleted()) {

				// reset the attribute flags for the record
				resetAttributes(hhRecord);
				addPCRecord(palmToBorg(hhRecord));

			}
		} else {

			if (hhRecord.isArchived() || hhRecord.isDeleted()) {
				if (!addr.getModified()) {
					deletePCRecord(addr);
				}
			} else if (hhRecord.isModified() || hhRecord.isNew())
				handleModified(hhRecord, addr);

		}
	}

	private void handleModified(AddressRecord hhRecord, Address addr)
			throws Exception {

		// Record exists on both HH and PC
		if (!addr.getModified()) {

			resetAttributes(hhRecord);

			Address modaddr = palmToBorg(hhRecord);
			modaddr.setKey(addr.getKey());
			AddressModel.getReference().saveAddress(modaddr, true);

		} else if (!compareRecords(hhRecord, addr)) { 

			resetAttributes(hhRecord);

			// Add the HH record to the PC table
			addPCRecord(palmToBorg(hhRecord));
		}

	}

	private Address getRecordById(int id) throws Exception {
		return (AddressModel.getReference().getAddress(id));
	}

	private void writeHHRecord(Record record) throws SyncException, IOException {
		//AddrCond.log("write Palm record - " + record.toString());
		SyncManager.writeRec(db, record);
	}


	private int addPCRecord(Address addr) throws Exception {
		AddrCond.log("add BORG record - " + addr.getKey());
		AddressModel.getReference().saveAddress(addr, true);
		return (addr.getKey());
	}

	private void deletePCRecord(Address addr) throws Exception {
		AddrCond.log("delete BORG record - " + addr.getKey());
		AddressModel.getReference().forceDelete(addr);
	}

	private void resetPCAttributes(Address addr) throws Exception {

		// skip write to PC record if already reset
		if (addr.getModified() == false)
			return;
		addr.setModified(false);
		AddressModel.getReference().saveAddress(addr, true);
	}

	private void resetAttributes(Record record) {

		record.setIsModified(false);
		record.setIsArchived(false);
		record.setIsDeleted(false);
		record.setIsNew(false);
	}

	private boolean compareRecords(Record firstRecord, Address addr) {
		AddressRecord secondRecord = borgToPalm(addr);
		return firstRecord.equals(secondRecord);
	}

	static private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

	static private AddressRecord borgToPalm(Address addr) {

		AddressRecord rec = new AddressRecord();
		rec.setId(0);
		rec.setCustom(1, Integer.toString(addr.getKey()));
		Date bd = addr.getBirthday();
		if (bd != null) {
			String bday = sdf.format(bd);
			rec.setCustom(2, bday);
		}
		rec.setCustom(4, addr.getNotes());

		rec.setName(addr.getLastName());
		rec.setFirstName(addr.getFirstName());
		rec.setAddress(addr.getStreetAddress());
		rec.setCity(addr.getCity());
		rec.setCompany(addr.getCompany());
		rec.setCountry(addr.getCountry());
		rec.setPhone(0, addr.getWorkPhone());
		rec.setPhone(1, addr.getHomePhone());
		rec.setPhone(2, addr.getFax());
		rec.setPhone(3, addr.getScreenName());
		rec.setPhone(4, addr.getEmail());
		rec.setState(addr.getState());
		rec.setZipCode(addr.getZip());
		rec.setIsModified(addr.getModified());
		return rec;
	}

	static private Address palmToBorg(AddressRecord hh) {
		Address addr = AddressModel.getReference().newAddress();
		addr.setKey(-1);
		addr.setNotes(hh.getCustom(4));
		String bs = hh.getCustom(2);
		if (bs != null && !bs.equals("")) {
			try {
				Date d = sdf.parse(bs);
				addr.setBirthday(d);
			} catch (Exception e) {
			}
		}
		addr.setLastName(hh.getName());
		addr.setFirstName(hh.getFirstName());
		addr.setStreetAddress(hh.getAddress());
		addr.setCity(hh.getCity());
		addr.setCompany(hh.getCompany());
		addr.setCountry(hh.getCountry());
		addr.setWorkPhone(hh.getPhone(0));
		addr.setHomePhone(hh.getPhone(1));
		addr.setFax(hh.getPhone(2));
		addr.setScreenName(hh.getPhone(3));
		addr.setEmail(hh.getPhone(4));
		addr.setState(hh.getState());
		addr.setZip(hh.getZipCode());
		addr.setModified(hh.isModified());

		return addr;
	}
}
