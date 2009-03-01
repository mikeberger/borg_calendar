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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import net.sf.borg.common.Errmsg;
import net.sf.borg.model.beans.Appointment;
import net.sf.borg.model.db.AppointmentDB;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.remote.ApptRemoteBeanDB;

public class AppointmentModel {

	static private AppointmentModel default_reference_ = null;

	public static AppointmentModel getReference() {
		return (default_reference_);
	}

	public static AppointmentModel create() {
		default_reference_ = new AppointmentModel();
		return (default_reference_);
	}

	// return true if an appointment is skipped on a particular date
	public static boolean isSkipped(Appointment ap, Calendar cal) {
		int dk = dkey(cal);
		String sk = Integer.toString(dk);
		Vector skv = ap.getSkipList();
		if (skv != null && skv.contains(sk)) {
			return true;
		}

		return false;
	}

	private BeanDB db_; // the SMDB database - see mdb.SMDB

	public BeanDB getDB() {
		return (db_);
	}

	/*
	 * map_ contains each "base" day key that has appts and maps it to a list of
	 * appt keys for that day.
	 */
	private HashMap map_;

	private AppointmentModel() {
		map_ = new HashMap();
	}

	/**
	 * return a base DB key for a given day
	 */
	public static int dkey(int year, int month, int date) {
		return ((year - 1900) * 1000000 + (month + 1) * 10000 + date * 100);
	}

	public static int dkey(Calendar cal) {
		return dkey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
				.get(Calendar.DATE));
	}

	public static Date dateFromKey(int dkey) {
		Calendar cal = new GregorianCalendar();
		int yr = (dkey / 1000000) % 1000 + 1900;
		int mo = (dkey / 10000) % 100 - 1;
		int day = (dkey / 100) % 100;
		cal.set(yr, mo, day, 0, 0, 0);
		return cal.getTime();
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

		// even if delete fails - still refresh cache info
		// and tell listeners - db failure may have been due to
		// a sync causing a record already deleted error
		// this needs to be reflected in the map
		try {
			// recreate the appointment hashmap
			buildMap();

		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
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

			// check is the appt is private and set encrypt flag if it is
			boolean crypt = r.getPrivate();

			if (add == true) {
				// get the next unused key for a given day
				// to do this, start with the "base" key for a given day.
				// then see if an appt has this key.
				// keep adding 1 until a key is found that has no appt
				GregorianCalendar gcal = new GregorianCalendar();
				gcal.setTime(r.getDate());
				int key = AppointmentModel.dkey(gcal);

				try {
					while (true) {
						Appointment ap = getAppt(key);
						if (ap == null)
							break;
						key++;
					}

				} catch (Exception ee) {
					Errmsg.errmsg(ee);
					return;
				}

				// key is now a free key
				r.setKey(key);
				if (!sync) {
					r.setNew(true);
					
				}

				db_.addObj(r, crypt);
			} else {
				if (!sync) {
					r.setModified(true);
					
				}
				db_.updateObj(r, crypt);
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);

		}

		if (!bulk) {
			try {
				// recreate the appointment hashmap
				buildMap();

			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}
		}

	}

	// get an appt from the database by key
	public Appointment getAppt(int key) throws Exception {
		Appointment appt = (Appointment) db_.readObj(key);
		return (appt);
	}

	// this function is called to mark a to do as done from the todo gui
	// window
	// the user can optionally indicate that the todo is to be deleted - but
	// we must still
	// make sure it is the last repeat if the todo repeats
	public void do_todo(int key, boolean del) throws Exception {
		// read the DB row for the ToDo
		Appointment appt = (Appointment) db_.readObj(key);

		// curtodo is the date of the todo that is to be "done"
		Date curtodo = appt.getNextTodo();
		Date d = appt.getDate();
		if (curtodo == null) {
			curtodo = d;
		}

		// newtodo will be the name of the next todo occurrence (if the todo
		// repeats and is not done)
		Date newtodo = null;

		Integer tms = appt.getTimes();
		String rpt = Repeat.getFreq(appt.getFrequency());

		// find next to do if it repeats by doing calendar math
		if (tms != null && tms.intValue() > 1 && rpt != null
				&& !rpt.equals(Repeat.ONCE)) {
			int tm = tms.intValue();

			Calendar ccal = new GregorianCalendar();
			Calendar ncal = new GregorianCalendar();

			// ccal is the current todo and ncal is the original appt date
			// ncal will be incremented until we find the todo after the
			// one in ccal
			ccal.setTime(curtodo);
			ncal.setTime(d);

			Repeat repeat = new Repeat(ncal, appt.getFrequency());
			for (int i = 1; i < tm; i++) {

				if (ncal != null
						&& ncal.get(Calendar.YEAR) == ccal.get(Calendar.YEAR)
						&& ncal.get(Calendar.MONTH) == ccal.get(Calendar.MONTH)
						&& ncal.get(Calendar.DATE) == ccal.get(Calendar.DATE)) {

					while (true) {
						ncal = repeat.next();
						if (ncal == null)
							break;
						if (isSkipped(appt, ncal))
							continue;

						newtodo = ncal.getTime();
						break;

					}
					// System.out.println("newtodo=" + newtodo.getTime());
					break;
				}

				ncal = repeat.next();

			}
		}

		if (newtodo != null) {
			// a next todo was found, set NT to that value
			// and don't delete the appt
			appt.setNextTodo(newtodo);
			saveAppt(appt, false);
		} else {
			// there is no next todo - shut off the todo
			// unless the user wants it deleted. if so, delete it.
			if (del) {
				forceDelete(appt);
			} else {
				appt.setTodo(false);
				appt.setColor("strike");
				saveAppt(appt, false);
			}
		}

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

		// scan the DB and build the appt map_
		buildMap();

	}

	// the calmodel keeps a hashmap of days to appt keys to avoid hitting
	// the DB when possible - although the DB is cached too to some extent
	// buildmap will rebuild the map based on the DB
	private void buildMap() throws Exception {
		// erase the current map
		map_.clear();

		// get the year for later
		GregorianCalendar cal = new GregorianCalendar();
		int curyr = cal.get(Calendar.YEAR);

		// scan entire DB
		Iterator itr = getAllAppts().iterator();
		AppointmentDB kf = (AppointmentDB) db_;
		Collection rptkeys = kf.getRepeatKeys();

		while (itr.hasNext()) {
			Appointment appt = (Appointment) itr.next();

			// if appt does not repeat, we can add its
			// key to a single day
			int key = appt.getKey();
			Integer ki = new Integer(key);
			if (!rptkeys.contains(ki)) {
				// strip of appt number
				int dkey = (key / 100) * 100;

				// get/add entry for the day in the map
				Object o = map_.get(new Integer(dkey));
				if (o == null) {
					o = new LinkedList();
					map_.put(new Integer(dkey), o);
				}

				// add the appt key to the day's list
				LinkedList l = (LinkedList) o;
				l.add(new Integer(key));
			} else {
				// appt repeats so we have to add all of the repeats
				// into the map (well maybe not all)
				int yr = (key / 1000000) % 1000 + 1900;
				int mo = (key / 10000) % 100 - 1;
				int day = (key / 100) % 100;
				cal.set(yr, mo, day);

				Repeat repeat = new Repeat(cal, appt.getFrequency());
				if (!repeat.isRepeating())
					continue;
				Integer times = appt.getTimes();
				if (times == null)
					times = new Integer(1);
				int tm = times.intValue();

				// ok, plod through the repeats now
				for (int i = 0; i < tm; i++) {
					Calendar current = repeat.current();
					if (current == null) {
						repeat.next();
						continue;
					}

					// get the day key for the repeat
					int rkey = dkey(current);

					int cyear = current.get(Calendar.YEAR);

					// limit the repeats to 2 years
					// from the current year
					// otherwise, an appt repeating 9999 times
					// could kill BORG
					if (cyear > curyr + 2)
						break;

					// check if the repeat is in the skip list
					// if so, skip it
					if (!isSkipped(appt, current)) {
						// add the repeat key to the map
						Object o = map_.get(new Integer(rkey));
						if (o == null) {
							o = new LinkedList();
							map_.put(new Integer(rkey), o);
						}
						LinkedList l = (LinkedList) o;
						l.add(new Integer(key));
					}

					repeat.next();
				}
			}
		}

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
