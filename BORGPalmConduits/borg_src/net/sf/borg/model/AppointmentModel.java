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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import net.sf.borg.common.Errmsg;
import net.sf.borg.model.db.AppointmentDB;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.remote.ApptRemoteBeanDB;
import net.sf.borg.model.entity.Appointment;

public class AppointmentModel {

	static private AppointmentModel default_reference_ = null;

	public static AppointmentModel getReference() {
		return (default_reference_);
	}

	public static AppointmentModel create() {
		default_reference_ = new AppointmentModel();
		return (default_reference_);
	}


	private BeanDB db_; // the SMDB database - see mdb.SMDB

	public BeanDB getDB() {
		return (db_);
	}

	/*
	 * map_ contains each "base" day key that has appts and maps it to a list of
	 * appt keys for that day.
	 */

	private AppointmentModel() {
	}

	// get a new row from SMDB. The row will internally contain the Appt
	// schema
	public Appointment newAppt() {
		Appointment appt = (Appointment) db_.newObj();
		return (appt);
	}

	// delete a row from the database
	public void forceDelete(Appointment appt) {
		try {
			db_.delete(appt.getKey());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	// called only from palm sync conduit
	public int syncSave(Appointment r) {
		if (r.getKey() == -1)
			saveAppt(r, true, false, true);
		else
			saveAppt(r, false, false, true);

		return (r.getKey());

	}

	public void saveAppt(Appointment r, boolean add) {
		saveAppt(r, add, false, false);
	}

	
	public void saveAppt(Appointment r, boolean add, boolean bulk, boolean sync) {

		try {
			Appointment orig_appt = null;
			if( r.getKey() != -1 )
				orig_appt = getAppt(r.getKey());
			if (orig_appt == null) {
				db_.addObj(r);			
			} else {
				db_.updateObj(r);						
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);

		}


	}

	// get an appt from the database by key
	public Appointment getAppt(int key) throws Exception {
		Appointment appt = (Appointment) db_.readObj(key);
		return (appt);
	}

	
	// get a vector containing all of the todo appts in the DB
	public Collection get_todos() {

		ArrayList av = new ArrayList();
		try {

			// iterate through appts in the DB
			AppointmentDB kf = (AppointmentDB) db_;
			Collection keycol = kf.getTodoKeys();
			// Collection keycol = AppointmentHelper.getTodoKeys(db_);
			Iterator keyiter = keycol.iterator();
			while (keyiter.hasNext()) {
				Integer ki = (Integer) keyiter.next();
				int key = ki.intValue();

				// read the full appt from the DB and add to the vector
				Appointment appt = (Appointment) db_.readObj(key);
				

				av.add(appt);
			}
		} catch (Exception ee) {
			Errmsg.errmsg(ee);
		}

		return (av);

	}

	
	public void open_db()
			throws Exception {
		db_ = new ApptRemoteBeanDB();
	}

	

	public static boolean isNote(Appointment appt) {
		// return true if the appt Appointment represents a "note" or
		// "non-timed" appt
		// this is true if the time is midnight and duration is 0.
		// this method was used for backward compatibility - as opposed to
		// adding
		// a new flag to the DB
		// 1.6.1 - added new db field to fix bug when time zone changes
		// for backward compatiblity, keep old check in addition to checking new
		// flag
		try {

			if (appt.getUntimed() != null && appt.getUntimed().equals("Y"))
				return true;
			Integer duration = appt.getDuration();
			if (duration != null && duration.intValue() != 0)
				return (false);

			Date d = appt.getDate();
			if (d == null)
				return (true);

			GregorianCalendar g = new GregorianCalendar();
			g.setTime(d);
			int hour = g.get(Calendar.HOUR_OF_DAY);
			if (hour != 0)
				return (false);

			int min = g.get(Calendar.MINUTE);
			if (min != 0)
				return (false);
		} catch (Exception e) {
			return (true);
		}

		return (true);

	}

	public void close_db() throws Exception {
		db_.close();
	}

	public Collection getAllAppts() throws Exception {
		Collection appts = db_.readAll();
		return appts;
	}

}
