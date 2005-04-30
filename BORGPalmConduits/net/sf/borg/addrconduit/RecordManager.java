package net.sf.borg.addrconduit;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.sf.borg.model.Address;
import net.sf.borg.model.AddressModel;
import palm.conduit.AddressRecord;
import palm.conduit.Record;
import palm.conduit.SyncException;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."

public class RecordManager {

    SyncProperties props;
    //Vector hhRecords; 
    int db;

    public RecordManager(SyncProperties props, int db){
        this.props = props;
        this.db = db;
		//hhRecords = new Vector();
    }


    public void SyncData() throws Exception{

        AddressRecord hhRecord;
        boolean forceReset = false;
        
        // if pc wipes hh - delete hh db first
        if( props.syncType == SyncProperties.SYNC_PC_TO_HH )
        {
            SyncManager.purgeAllRecs(db);
        }
        
        // get list og hh ids
        ArrayList hhids = new ArrayList();
        int recordCount = SyncManager.getDBRecordCount(db);
        for (int recordIndex = 0; recordIndex < recordCount; recordIndex++) {
            hhRecord = new AddressRecord();
            hhRecord.setIndex(recordIndex);
            SyncManager.readRecordByIndex(db, hhRecord);
            hhids.add(new Integer(hhRecord.getId()));
        }       
        
        // check for empty BORG db
        AddressModel amod = AddressModel.getReference();
        Collection addrs = amod.getAddresses();
        if( addrs.size() == 0 )
        {
            // force reset of all palm record borg pointers
            forceReset = true;
        }

        //get record count on the database
        recordCount = SyncManager.getDBRecordCount(db);

        Iterator it = hhids.iterator();
        while (it.hasNext()) {

            Integer id = (Integer) it.next();
            hhRecord = new AddressRecord();
            hhRecord.setId(id.intValue());
            SyncManager.readRecordById(db, hhRecord);
            if( forceReset )
            {
                hhRecord.setCustom(1,"");
            }
            
            // Synchronize the record obtained from the handheld
            synchronizeHHRecord(hhRecord);
        }

        
        addrs = amod.getAddresses();
        it = addrs.iterator();
        while (it.hasNext()) {

        	Address addr = (Address) it.next();
            synchronizePCRecord(addr);
         
        }
        
        addrs = amod.getDeletedAddresses();
        it = addrs.iterator();
        while (it.hasNext()) {

        	Address addr = (Address) it.next();
            synchronizePCRecord(addr);
         
        }
		SyncManager.purgeDeletedRecs(db); //deletes all hh records marked as deleted
        SyncManager.resetSyncFlags(db);  //reset all the sync flags on the hh
        //writeHHRecords();
    }

    public void synchronizePCRecord(Address addr) throws Exception {

		AddressRecord hhRecord;

        if (!addr.getNew()) {

            hhRecord = retrieveHHRecordByBorgId(addr.getKey());

            if (hhRecord == null) {
                if( addr.getDeleted())
                {
                    deletePCRecord(addr);
                }
                else
                {
                    resetPCAttributes(addr);
                    //hhRecords.addElement(borgToPalm(addr));  
                    writeHHRecord(borgToPalm(addr));
                }
            }
            else {

                if (addr.getDeleted()) {
                    deletePCRecord(addr);
                    deleteHHRecord(hhRecord);
                } else if( addr.getModified() ){ 
                    resetPCAttributes(addr);
                    int hhid = hhRecord.getId();
                    hhRecord = borgToPalm(addr);
                    hhRecord.setId(hhid);
                    //hhRecords.addElement(hhRecord); 
                    writeHHRecord(hhRecord);
                }

            }
        } else if (addr.getDeleted()) {
            deletePCRecord(addr);
        } else { 
            resetPCAttributes(addr);
            //hhRecords.addElement(borgToPalm(addr));
            writeHHRecord(borgToPalm(addr));
        }

    }

    public void synchronizeHHRecord(AddressRecord hhRecord) throws Exception{

        Address addr = null;      
        // any record without a BORG id is considered new
        String id = hhRecord.getCustom(1);
        if( id == null || id.equals(""))
            hhRecord.setIsNew(true);
        else
        {
            String cus = hhRecord.getCustom(1);
            try{
                int i = Integer.parseInt(cus);
                addr = getRecordById(i);
            }
            catch(Exception e)
            {
            }
        }

        if (addr == null){ // if there is no pc rec with the matching RecID

            if (hhRecord.isArchived() || hhRecord.isDeleted()){
                deleteHHRecord(hhRecord);
            }
            else{ //default

                //reset the attribute flags for the record
                resetAttributes(hhRecord);

                //add it to the pc records Vector
                int key = addPCRecord(palmToBorg(hhRecord));
                
                // update borg id in HH record
                hhRecord.setCustom(1,Integer.toString(key));
                writeHHRecord(hhRecord);
                
            }
        }
        else {
            
            if (hhRecord.isArchived() || hhRecord.isDeleted()){
                handleDeleted(hhRecord, addr);
            }
            else if( hhRecord.isModified() || hhRecord.isNew())
                handleModified(hhRecord, addr);

        }
    }


    public void handleModified(AddressRecord hhRecord, Address addr) throws Exception{

        	// Record exists on both HH and PC
			if (addr.getDeleted() || !addr.getModified()){
				
			    resetAttributes(hhRecord);
			    
			    //hhRecords.addElement(hhRecord);
			    writeHHRecord(hhRecord);
			    
			    Address modaddr = palmToBorg(hhRecord);
			    modaddr.setKey(addr.getKey());
			    resetPCAttributes(modaddr);
			}
            else if (compareRecords(hhRecord, addr)) {
                // both records have changed identically
                    resetAttributes(hhRecord);
                    //hhRecords.addElement(hhRecord);
                    resetPCAttributes(addr);					
            }
            else { // records are different
                // Change the PC record to a new record so
                // that it gets added to the HH on pass thru pc records
                resetPCAttributes(addr);
                addr.setNew(true);
                AddressModel.getReference().saveAddress(addr,true);
                
                resetAttributes(hhRecord);
                               
                // Add the HH record to the PC table
                int key = addPCRecord(palmToBorg(hhRecord));
                hhRecord.setCustom(1,Integer.toString(key));
                writeHHRecord(hhRecord);
            }

    }


    public void handleDeleted(Record hhRecord, Address addr) throws Exception{

        if (addr.getModified() && !addr.getDeleted()) {
            // (HH = Delete and PC = Modified) causes HH record to be updated not deleted
            resetPCAttributes(addr);
            int hhid = hhRecord.getId();
            hhRecord = borgToPalm(addr);
            hhRecord.setId(hhid);
            //hhRecords.addElement(hhRecord);
            writeHHRecord(hhRecord);
        }
        else {
            // Marked as already deleted from the HH and needs
            // to be deleted from the PC
            deleteHHRecord(hhRecord);
            deletePCRecord(addr);
        }
    }

    private Address getRecordById(int id) throws Exception{
        return( AddressModel.getReference().getAddress(id));
    }

    private void writeHHRecord(Record record) throws SyncException, IOException
    {
        SyncManager.writeRec(db,record);
    }
    
    private void deleteHHRecord(Record record) throws SyncException{
        SyncManager.deleteRecord(db, record);
    }

    private AddressRecord retrieveHHRecordByBorgId(int key) throws IOException{

        //get record count on the database
        int rc = SyncManager.getDBRecordCount(db);
        String id = Integer.toString(key);
        
        for (int ri = 0; ri < rc; ri++) {

            AddressRecord hhRecord = new AddressRecord();
            hhRecord.setIndex(ri);
            SyncManager.readRecordByIndex(db, hhRecord);
            String cus = hhRecord.getCustom(1);
            if( cus == null || cus.equals(""))
                continue;
            
            if( cus.equals(id))
                return(hhRecord);
 
        }

        return null;
    }

    private int addPCRecord(Address addr) throws Exception{
        
        AddressModel.getReference().saveAddress(addr,true);
        return( addr.getKey());
    }

    private void deletePCRecord(Address addr) throws Exception{
        AddressModel.getReference().forceDelete(addr);
    }

    private void resetPCAttributes(Address addr) throws Exception{

        addr.setModified(false);
        addr.setDeleted(false);
        addr.setNew(false);
        AddressModel.getReference().saveAddress(addr,true);
    }
    
    private void resetAttributes(Record record){

        record.setIsModified(false);
        record.setIsArchived(false);
        record.setIsDeleted(false);
        record.setIsNew(false);
    }

    private boolean compareRecords(Record firstRecord, Address addr){
        AddressRecord secondRecord = borgToPalm(addr);
        return firstRecord.equals(secondRecord);
    }
    
    static private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    
    static public AddressRecord borgToPalm(Address addr)
    {

        AddressRecord rec = new AddressRecord();
        rec.setId(0);
        rec.setCustom(1,Integer.toString(addr.getKey()));
        Date bd = addr.getBirthday();
        if( bd != null )
        {
            String bday = sdf.format(bd);
            rec.setCustom(2,bday);
        }
        rec.setCustom(4,addr.getNotes());
        
        rec.setName(addr.getLastName());
        rec.setFirstName(addr.getFirstName());
        rec.setAddress(addr.getStreetAddress());
        rec.setCity(addr.getCity());
        rec.setCompany(addr.getCompany());
        rec.setCountry(addr.getCountry());
        rec.setPhone(0,addr.getWorkPhone());
        rec.setPhone(1,addr.getHomePhone());
        rec.setPhone(2,addr.getFax());
        rec.setPhone(3,addr.getScreenName());
        rec.setPhone(4,addr.getEmail());
        rec.setState(addr.getState());
        rec.setZipCode(addr.getZip());
        rec.setIsNew(addr.getNew());
        rec.setIsDeleted(addr.getDeleted());
        rec.setIsModified(addr.getModified());
    	return rec;
    }
    
    static public Address palmToBorg(AddressRecord hh)
    {
        Address addr = AddressModel.getReference().newAddress();
        addr.setKey(-1);
        addr.setNotes(hh.getCustom(4));
        String bs = hh.getCustom(2);
        if( bs != null && !bs.equals(""))
        {
            try{
                Date d = sdf.parse(bs);
                addr.setBirthday(d);
            }
            catch(Exception e){}
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
        addr.setNew(hh.isNew());
        addr.setDeleted(hh.isDeleted());
        addr.setModified(hh.isModified());
        
    	return addr;
    }
}
