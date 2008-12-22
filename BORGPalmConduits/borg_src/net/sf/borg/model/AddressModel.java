/*
This file is part of BORG.
 
    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
 
    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
Copyright 2003 by Mike Berger
 */
package net.sf.borg.model;

import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.sf.borg.common.Errmsg;
import net.sf.borg.model.beans.Address;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.remote.RemoteBeanDataFactory;

public class AddressModel  {

    private BeanDB db_; // the database

    public BeanDB getDB() {
	return (db_);
    }

    private HashMap bdmap_ = new HashMap();

    static private AddressModel self_ = null;

    private void load_map() {

	// clear map
	bdmap_.clear();

	try {

	    // iterate through tasks using taskmodel
	    Collection addrs = getAddresses();
	    Iterator ti = addrs.iterator();
	    while (ti.hasNext()) {
		Address addr = (Address) ti.next();
		if (addr.getDeleted())
		    continue;

		// use birthday to build a day key
		Date bd = addr.getBirthday();
		if (bd == null)
		    continue;

		GregorianCalendar g = new GregorianCalendar();
		g.setTime(bd);

		int key = AppointmentModel.dkey(g);
		int bdkey = AppointmentModel.birthdayKey(key);
		// System.out.println("key is " + Integer.toString(key) + " " +
		// Integer.toString(bdkey) + " date is " + bd);

		// add the task string to the btmap_
		// add the task to the mrs_ Vector. This is used by the todo gui
		Object o = bdmap_.get(new Integer(bdkey));
		if (o == null) {
		    o = new LinkedList();
		    bdmap_.put(new Integer(bdkey), o);
		}

		LinkedList l = (LinkedList) o;
		l.add(addr);
	    }

	} catch (Exception e) {

	    Errmsg.errmsg(e);
	    return;
	}

    }

    public static AddressModel getReference() {
	return (self_);
    }

    public static AddressModel create() {
	self_ = new AddressModel();
	return (self_);
    }

  
    public Collection getAddresses() throws Exception {
	Collection addrs = db_.readAll();
	Iterator it = addrs.iterator();
	while (it.hasNext()) {
	    Address addr = (Address) it.next();
	    if (addr.getDeleted())
		it.remove();
	}
	return addrs;
    }

    public Collection getDeletedAddresses() throws Exception {
	Collection addrs = db_.readAll();
	Iterator it = addrs.iterator();
	while (it.hasNext()) {
	    Address addr = (Address) it.next();
	    if (!addr.getDeleted())
		it.remove();
	}
	return addrs;
    }

   

    // open the SMDB database
    public void open_db(String url, String username, boolean shared)
	    throws Exception {

	StringBuffer tmp = new StringBuffer(url);
	
	url = tmp.toString();
	// let the factory tweak dbdir

	db_ = RemoteBeanDataFactory.getInstance().create(Address.class, url, username);
	load_map();
    }

    

    public void forceDelete(Address addr) throws Exception {

	try {

	    db_.delete(addr.getKey());
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	refresh();
    }

  
    public void saveAddress(Address addr, boolean sync) throws Exception {

	int num = addr.getKey();

	if (num == -1) {
	    int newkey = db_.nextkey();
	    addr.setKey(newkey);
	    if (!sync) {
		addr.setNew(true);
	    }
	    db_.addObj(addr, false);

	} else {

	    if (!sync) {
		addr.setModified(true);
	    }
	    db_.updateObj(addr, false);

	}

	// inform views of data change
	refresh();
    }

    // allocate a new Row from the DB
    public Address newAddress() {
	return ((Address) db_.newObj());
    }

    public Address getAddress(int num) throws Exception {
	return ((Address) db_.readObj(num));
    }

    public void refresh() {
	load_map();
	
    }

   
    public void close_db() throws Exception {
	db_.close();
    }
}
