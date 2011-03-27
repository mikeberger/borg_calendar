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

 Copyright 2003-2010 by Mike Berger
 */

package net.sf.borg.model;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.CategoryModel.CategorySource;
import net.sf.borg.model.db.AppointmentDB;
import net.sf.borg.model.db.EntityDB;
import net.sf.borg.model.db.jdbc.ApptJdbcDB;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Link;
import net.sf.borg.model.undo.AppointmentUndoItem;
import net.sf.borg.model.undo.UndoLog;

/**
 * the appointment model provides the model layer APIs for working with
 * Appointment Entities
 * 
 */
public class AppointmentModel extends Model implements Model.Listener,
		CategorySource, Searchable<Appointment> {

	/**
	 * class XmlContainer is solely for JAXB XML export/import to keep the same
	 * XML structure as before JAXB was used
	 */
	@XmlRootElement(name = "APPTS")
	private static class XmlContainer {
		public Collection<Appointment> Appointment;
	}

	/** The singleton */
	static private AppointmentModel self_ = new AppointmentModel();

	/**
	 * Gets the singleton reference.
	 * 
	 * @return the singleton reference
	 */
	public static AppointmentModel getReference() {
		return (self_);
	}

	/**
	 * Gets the time format to use for all time processing.
	 * 
	 * @return the time format
	 */
	public static SimpleDateFormat getTimeFormat() {
		String mt = Prefs.getPref(PrefName.MILTIME);
		if (mt.equals("true")) {
			return (new SimpleDateFormat("HH:mm"));
		}

		return (new SimpleDateFormat("h:mm a"));

	}

	/**
	 * Checks an appointment is a note (not associated with a time of day).
	 * 
	 * @param appt
	 *            the appointment
	 * 
	 * @return true, if it is a note
	 */
	public static boolean isNote(Appointment appt) {
		// return true if the appt Appointment represents a "note" or
		// "non-timed" appt
		if (appt.getUntimed() != null && appt.getUntimed().equals("Y"))
			return true;
		return (false);

	}

	/**
	 * Checks if an appointment is skipped on a particular date
	 * 
	 * @param ap
	 *            the Appointment
	 * @param cal
	 *            the date
	 * 
	 * @return true, if is skipped
	 */
	public static boolean isSkipped(Appointment ap, Calendar cal) {
		int dk = DateUtil.dayOfEpoch(cal.getTime());
		String sk = Integer.toString(dk);
		Vector<String> skv = ap.getSkipList();
		if (skv != null && skv.contains(sk)) {
			return true;
		}

		return false;
	}

	/** The underlying database object */
	private EntityDB<Appointment> db_;

	/**
	 * map_ contains each "base" day key that has appts and maps it to a list of
	 * appt keys for that day.
	 */
	private HashMap<Integer, Collection<Integer>> map_;

	/**
	 * The vacation map - a map of vacation values for each day (i.e. no
	 * vacation, half-day, full-day)
	 */
	private HashMap<Integer, Integer> vacationMap_;

	/**
	 * Instantiates a new appointment model.
	 * 
	 */
	private AppointmentModel()  {
		map_ = new HashMap<Integer, Collection<Integer>>();
		vacationMap_ = new HashMap<Integer, Integer>();
		db_ = new ApptJdbcDB();

		// init categories and currentcategories
		CategoryModel.getReference().addSource(this);
		CategoryModel.getReference().addListener(this);

		// scan the DB and build the appt map_
		try {
			buildMap();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * Builds the map (cache) of days to appointments. Also builds the vacation
	 * map. This is not purely for caching as it also maps multiple days to a
	 * single appointment to support repeating appointments
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void buildMap() throws Exception {
		// erase the current map
		map_.clear();
		vacationMap_.clear();

		// get the year for later
		GregorianCalendar cal = new GregorianCalendar();
		int curyr = cal.get(Calendar.YEAR);

		// scan entire DB
		AppointmentDB kf = (AppointmentDB) db_;
		Collection<Integer> rptkeys = kf.getRepeatKeys();

		for (Appointment appt : getAllAppts()) {

			if (!CategoryModel.getReference().isShown(appt.getCategory())) {
				continue;
			}

			int dkey = DateUtil.dayOfEpoch(appt.getDate());

			// if appt does not repeat, we can add its
			// key to a single day
			int key = appt.getKey();
			Integer ki = new Integer(key);
			if (!rptkeys.contains(ki)) {

				// get/add entry for the day in the map
				Collection<Integer> o = map_.get(new Integer(dkey));
				if (o == null) {
					o = new LinkedList<Integer>();
					map_.put(new Integer(dkey), o);
				}

				// add the appt key to the day's list
				LinkedList<Integer> l = (LinkedList<Integer>) o;
				l.add(new Integer(key));

				// add day key to vacation map if appt has vacation
				if (appt.getVacation() != null
						&& appt.getVacation().intValue() != 0) {
					vacationMap_.put(new Integer(dkey), appt.getVacation());
				}
			} else {

				cal.setTime(appt.getDate());

				Repeat repeat = new Repeat(cal, appt.getFrequency());
				if (!repeat.isRepeating())
					continue;
				
				int tm = Repeat.calculateTimes(appt);

				// ok, plod through the repeats now
				for (int i = 0; i < tm; i++) {
					Calendar current = repeat.current();
					if (current == null) {
						repeat.next();
						continue;
					}

					// get the day key for the repeat
					int rkey = DateUtil.dayOfEpoch(current.getTime());

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
						Collection<Integer> o = map_.get(new Integer(rkey));
						if (o == null) {
							o = new LinkedList<Integer>();
							map_.put(new Integer(rkey), o);
						}
						LinkedList<Integer> l = (LinkedList<Integer>) o;
						l.add(new Integer(key));

						// add day key to vacation map if appt has vacation
						if (appt.getVacation() != null
								&& appt.getVacation().intValue() != 0) {
							vacationMap_.put(new Integer(rkey), appt
									.getVacation());
						}
					}

					repeat.next();
				}
			}
		}

	}

	/**
	 * Delete an appt.
	 * 
	 * @param appt
	 *            the appt
	 */
	public void delAppt(Appointment appt) {
		delAppt(appt, false);
	}

	/**
	 * Delete an appt.
	 * 
	 * @param appt
	 *            the appt
	 * @param undo
	 *            true if we are executing an undo
	 */
	public void delAppt(Appointment appt, boolean undo) {

		Appointment orig_appt = null;
		try {

			orig_appt = getAppt(appt.getKey());

			LinkModel.getReference().deleteLinksFromEntity(appt);
			LinkModel.getReference().deleteLinksToEntity(appt);

			db_.delete(appt.getKey());
			if (!undo) {
				UndoLog.getReference().addItem(
						AppointmentUndoItem.recordDelete(orig_appt));
			}

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

			// refresh all views that are displaying appt data from this
			// model
			refreshListeners(new ChangeEvent(new Integer(appt.getKey()), ChangeEvent.ChangeAction.DELETE));
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
	}

	/**
	 * Delete an appt by key.
	 * 
	 * @param key
	 *            the key
	 */
	public void delAppt(int key) {
		try {
			Appointment appt = getAppt(key);
			if (appt != null)
				delAppt(appt);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * delete one occurrence of a repeating appointment
	 * 
	 * @param key
	 *            the appointment key
	 * @param rptDate
	 *            the date of the repeat to be deleted
	 */
	public void delOneOnly(int key, Date rptDate) {
		try {

			int rkey = DateUtil.dayOfEpoch(rptDate);

			Appointment appt = db_.readObj(key);

			// get the list of repeats that have been deleted - the SKip
			// list
			Vector<String> vect = appt.getSkipList();
			if (vect == null)
				vect = new Vector<String>();

			// add the current appt key to the SKip list
			vect.add(Integer.toString(rkey));
			appt.setSkipList(vect);
			saveAppt(appt, false);

			// if we are deleting the next todo then do it
			Date nt = appt.getNextTodo();
			if (nt == null)
				nt = appt.getDate();
			
			if (rkey == DateUtil.dayOfEpoch(nt)) {
				do_todo(appt.getKey(), false);
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
	}
	
	/**
	 * Mark a todo appointment as done. If the appointment repeats, adjust the
	 * next todo value. If the todo is all done (including repeats), optionally
	 * delete it
	 * 
	 * @param key
	 *            the appointment key
	 * @param del
	 *            if true, delete the todo when all done. Otherwise, mark it as
	 *            no longer being a todo.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void do_todo(int key, boolean del) throws Exception {
		this.do_todo(key, del, null);
	}
	
	/**
	 * Mark a todo appointment as done. If the appointment repeats, adjust the
	 * next todo value. If the todo is all done (including repeats), optionally
	 * delete it
	 * 
	 * @param key
	 *            the appointment key
	 * @param del
	 *            if true, delete the todo when all done. Otherwise, mark it as
	 *            no longer being a todo.
	 * @param date date of the repeat that is being marked as done. If null, then the next todo is the one.
	 * If set, then all todos up to and including the date are marked as done.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void do_todo(int key, boolean del, Date date) throws Exception {
		// read the DB row for the ToDo
		Appointment appt = db_.readObj(key);

		// curtodo is the date of the todo that is to be "done"
		Date curtodo = appt.getNextTodo();
		Date d = appt.getDate();
		if (curtodo == null) {
			curtodo = d;
		}
		
		if( date != null)
			curtodo = date;

		// newtodo will be the name of the next todo occurrence (if the todo
		// repeats and is not done)
		Date newtodo = null;

		int tm = Repeat.calculateTimes(appt);
		String rpt = Repeat.getFreq(appt.getFrequency());

		// find next to do if it repeats by doing calendar math
		if (tm > 1 && rpt != null
				&& Repeat.isRepeating(appt)) {

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
				delAppt(appt);
			} else {
				appt.setTodo(false);
				appt.setColor("strike"); // strike the text to indicate a done
				// todo
				saveAppt(appt, false);
			}
		}

	}

	/**
	 * Export appointments as XML.
	 * 
	 * @param fw
	 *            the Writer to write XML to
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void export(Writer fw) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Marshaller m = jc.createMarshaller();
		XmlContainer container = new XmlContainer();
		container.Appointment = getAllAppts();
		m.marshal(container, fw);
	}

	
	/**
	 * Gets all appointments that are marked as todos.
	 * 
	 * @return the todos
	 */
	public Collection<Appointment> get_todos() {

		ArrayList<Appointment> av = new ArrayList<Appointment>();
		try {

			// iterate through appts in the DB
			AppointmentDB kf = (AppointmentDB) db_;
			Collection<Integer> keycol = kf.getTodoKeys();
			// Collection keycol = AppointmentHelper.getTodoKeys(db_);
			for (Integer ki : keycol) {
				int key = ki.intValue();

				// read the full appt from the DB and add to the vector
				Appointment appt = db_.readObj(key);

				// if category set, filter appts
				if (!CategoryModel.getReference().isShown(appt.getCategory())) {
					continue;
				}

				av.add(appt);
			}
		} catch (Exception ee) {
			Errmsg.errmsg(ee);
		}

		return (av);

	}

	/**
	 * Get all appts.
	 * 
	 * @return all appts
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Appointment> getAllAppts() throws Exception {
		Collection<Appointment> appts = db_.readAll();
		return appts;
	}

	/**
	 * Gets an appt by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the appt
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Appointment getAppt(int key) throws Exception {
		Appointment appt = db_.readObj(key);
		return (appt);
	}

	/**
	 * Get a list of appointment ids for a given day
	 * 
	 * @param d
	 *            the date
	 * 
	 * @return the appts
	 */
	public List<Integer> getAppts(Date d) {
		return ((List<Integer>) map_.get(new Integer(DateUtil.dayOfEpoch(d))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.CategoryModel.CategorySource#getCategories()
	 */
	@Override
	public Collection<String> getCategories() {

		TreeSet<String> dbcat = new TreeSet<String>();
		dbcat.add(CategoryModel.UNCATEGORIZED);
		try {
			for (Appointment ap : getAllAppts()) {
				String cat = ap.getCategory();
				if (cat != null && !cat.equals(""))
					dbcat.add(cat);
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		return (dbcat);

	}

	/**
	 * Gets the underlying dB.
	 * 
	 * @return the dB
	 */
	@Deprecated
	public EntityDB<Appointment> getDB() {
		return (db_);
	}

	/**
	 * return true if there are any todos in the entire appointment table
	 * 
	 * @return true, if any todos exist
	 */
	public boolean haveTodos() {
		try {
			AppointmentDB kf = (AppointmentDB) db_;
			Collection<Integer> keycol = kf.getTodoKeys();
			// Collection keycol = AppointmentHelper.getTodoKeys(db_);
			if (keycol.size() != 0)
				return (true);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		return (false);
	}

	/**
	 * Import xml.
	 * 
	 * @param is
	 *            the input stream containing the XML
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void importXml(InputStream is) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Unmarshaller u = jc.createUnmarshaller();

		XmlContainer container = (XmlContainer) u
				.unmarshal(is);
		
		if( container.Appointment == null ) return;

		// use key from import file if importing into empty db
		int nextkey = db_.nextkey();
		boolean use_keys = (nextkey == 1) ? true : false;
		for (Appointment appt : container.Appointment) {
			if( !use_keys )
				appt.setKey(nextkey++);
			db_.addObj(appt);
		}

		// rebuild the hashmap
		buildMap();

		CategoryModel.getReference().syncCategories();

		// refresh all views that are displaying appt data from this model
		refreshListeners();

	}

	/**
	 * create a new appointment.
	 * 
	 * @return the appointment
	 */
	public Appointment newAppt() {
		Appointment appt = db_.newObj();
		return (appt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model.Listener#refresh()
	 */
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}
	
	public void refresh()
	{
		try {
			buildMap();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// refresh all views that are displaying appt data from this model
		refreshListeners();
	}

	/**
	 * Save an appointment.
	 * 
	 * @param r
	 *            the appointment
	 */
	public void saveAppt(Appointment r) {
		saveAppt(r, false);
	}

	/**
	 * Save an appointment.
	 * 
	 * @param r
	 *            the appointment
	 * @param undo
	 *            true if we are executing an undo
	 */
	public void saveAppt(Appointment r, boolean undo) {

		ChangeEvent.ChangeAction action = ChangeEvent.ChangeAction.ADD;
		try {
			Appointment orig_appt = null;
			if (r.getKey() != -1)
				orig_appt = getAppt(r.getKey());
			if (orig_appt == null) {

				// if undo is adding back record - force the key to
				// be what is in the record
				if (!undo) {
					r.setKey(db_.nextkey());
				}

				db_.addObj(r);
				if (!undo) {
					UndoLog.getReference().addItem(
							AppointmentUndoItem.recordAdd(r));
				}
			} else {

				action = ChangeEvent.ChangeAction.CHANGE;
				db_.updateObj(r);
				if (!undo) {
					UndoLog.getReference().addItem(
							AppointmentUndoItem.recordUpdate(orig_appt));
				}
			}

			// update category list
			String cat = r.getCategory();
			if (cat != null && !cat.equals(""))
				CategoryModel.getReference().addCategory(cat);

		} catch (Exception e) {
			Errmsg.errmsg(e);

		}

		try {
			// recreate the appointment hashmap
			buildMap();

			// refresh all views that are displaying appt data from this
			// model
			refreshListeners(new ChangeEvent(new Integer(r.getKey()), action));
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
	}

	/**
	 * Sync with the db.
	 */
	public void sync() {
		db_.sync();
		try {
			// recreate the appointment hashmap
			buildMap();

			// refresh all views that are displaying appt data from this
			// model
			refreshListeners();
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
	}

	/**
	 * determine the number of vacation days up to and including the given day
	 * in a particular year
	 * 
	 * @param d
	 *            the Date
	 * 
	 * @return the double
	 */
	public double vacationCount(Date d) {

		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		int dk = DateUtil.dayOfEpoch(d);
		int yearStartKey = DateUtil.dayOfEpoch(cal.getTime());
		double count = 0;

		Set<Integer> vkeys = vacationMap_.keySet();
		for (Integer i : vkeys) {
			int vdaykey = i.intValue();
			if (vdaykey >= yearStartKey && vdaykey <= dk) {
				Integer vnum = vacationMap_.get(i);
				if (vnum.intValue() == 2) {
					count += 0.5;
				} else {
					count += 1.0;
				}
			}
		}

		return count;

	}

	/**
	 * save the default appointment in the prefs
	 * 
	 * @param appt
	 *            the appointment
	 */
	public void saveDefaultAppointment(Appointment appt) {

		try {
			JAXBContext jc = JAXBContext.newInstance(Appointment.class);
			Marshaller m = jc.createMarshaller();
			StringWriter sw = new StringWriter();
			m.marshal(appt, sw);
			Prefs.putPref(PrefName.DEFAULT_APPT, sw.toString());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * get the default appointment from prefs
	 * 
	 * @return the default appointment
	 */
	public Appointment getDefaultAppointment() {

		String defApptXml = Prefs.getPref(PrefName.DEFAULT_APPT);
		if (!defApptXml.equals("")) {
			try {
				JAXBContext jc = JAXBContext.newInstance(Appointment.class);
				Unmarshaller u = jc.createUnmarshaller();
				String xmlString = defApptXml.toString();
				Appointment ap =  (Appointment) u.unmarshal(new StringReader(xmlString));
			
				// transition from pre-1.7.2
				if( ap.getDate() == null )
					return null;
				return ap;
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.Searchable#search(net.sf.borg.model.SearchCriteria)
	 */
	@Override
	public Collection<Appointment> search(SearchCriteria criteria) {
		
		Collection<Appointment> res = new ArrayList<Appointment>(); // result collection
		try {

			// load all appts into appt list
			Collection<Appointment> allappts = getAllAppts();

			for (Appointment appt : allappts) {
				// read each appt

				// if category set, filter appts
				if (!CategoryModel.getReference().isShown(appt.getCategory())) {
					continue;
				}
				
				// do not search on encrypted appts
				if( appt.isEncrypted() )
					continue;

				String tx = appt.getText();
				Date d = appt.getDate();
				if (d == null || tx == null)
					continue;

				if( !criteria.search(tx))
					continue;
				
				// filter by repeat
				if (criteria.isRepeating() && !appt.getRepeatFlag())
					continue;

				// filter todos
				if (criteria.isTodo() && !appt.getTodo())
					continue;

				// filter by vacation
				Integer ii = appt.getVacation();
				if (criteria.isVacation()
						&& (ii == null || ii.intValue() != 1))
					continue;

				// filter by holiday
				ii = appt.getHoliday();
				if (criteria.isHoliday()
						&& (ii == null || ii.intValue() != 1))
					continue;

				// filter by category
				if (criteria.getCategory().equals(CategoryModel.UNCATEGORIZED)
						&& appt.getCategory() != null
						&& !appt.getCategory().equals(CategoryModel.UNCATEGORIZED))
					continue;
				else if (!criteria.getCategory().equals("")
						&& !criteria.getCategory().equals(CategoryModel.UNCATEGORIZED)
						&& !criteria.getCategory().equals(appt.getCategory()))
					continue;

				// filter by start date
				if (criteria.getStartDate() != null) {
					if (appt.getDate().before(criteria.getStartDate()))
						continue;
				}

				// filter by end date
				if (criteria.getEndDate() != null) {
					if (appt.getDate().after(criteria.getEndDate()))
						continue;
				}

				// filter by links
				if (criteria.hasLinks()) {
					LinkModel lm = LinkModel.getReference();
					try {
						Collection<Link> lnks = lm.getLinks(appt);
						if (lnks.isEmpty())
							continue;
					} catch (Exception e) {
						Errmsg.errmsg(e);
					}
				}


				// add the appt to the search results
				res.add(appt);

			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
		return (res);
	}
	
	@Override
	public String getExportName() {
		return "APPTS";
	}

	@Override
	public String getInfo() throws Exception {
		return Resource.getResourceString("appointments") + ": "
		+ getAllAppts().size();
	}

}
