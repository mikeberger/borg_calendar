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
/*
 * Day.java
 *
 * Created on January 1, 2004, 10:19 PM
 */

package net.sf.borg.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.LabelEntity;

/**
 * Class Day pulls together and manages all of the items that make up the
 * CalendarEntities for a single day. It packages together all of a day's info
 * as needed by a client (i.e. the UI).
 * 
 */
public class Day {

	/**
	 * class to compare appointment strings for sorting.
	 */
	private static class apcompare implements Comparator<CalendarEntity>, Serializable {

		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(CalendarEntity so1, CalendarEntity so2) {

			String s1 = so1.getText();
			String s2 = so2.getText();

			String psort = Prefs.getPref(PrefName.PRIORITY_SORT);
			if (psort.equals("true")) {
				Integer p1 = so1.getPriority();
				Integer p2 = so2.getPriority();

				if (p1 != null && p2 != null) {
					if (p1.intValue() != p2.intValue())
						return (p1.intValue() > p2.intValue() ? 1 : -1);
				} else if (p1 != null)
					return -1;
				else if (p2 != null)
					return 1;
			}

			// use appt time of day (not date - due to repeats) to sort next
			// appts with a time come before notes
			Date dt1 = null;
			Date dt2 = null;
			if (so1 instanceof Appointment && !AppointmentModel.isNote((Appointment) so1)) {
				Calendar cal = new GregorianCalendar();
				cal.setTime(((Appointment) so1).getDate());
				cal.set(1, 1, 2000);
				dt1 = cal.getTime();
			}
			if (so2 instanceof Appointment && !AppointmentModel.isNote((Appointment) so2)) {
				Calendar cal = new GregorianCalendar();
				cal.setTime(((Appointment) so2).getDate());
				cal.set(1, 1, 2000);
				dt2 = cal.getTime();
			}

			if (dt1 != null && dt2 != null)
				return (dt1.after(dt2) ? 1 : -1);
			if (dt1 != null)
				return -1;
			if (dt2 != null)
				return 1;

			// if we got here, just compare
			// strings lexicographically
			int res = s1.compareTo(s2);
			if (res != 0)
				return (res);
			return (1);

		}

	}

	private static List<SpecialDay> initSpecialDays(int year, int month) {

		List<SpecialDay> specialDays = new ArrayList<SpecialDay>();

		// American
		specialDays.add(new SpecialDay("Halloween", 31, 9, false, "US"));
		specialDays.add(new SpecialDay("Independence_Day ", 4, 6, true, "US"));
		specialDays.add(new SpecialDay("Ground_Hog_Day", 2, 1, false, "US"));
		specialDays.add(new SpecialDay("Valentine's_Day", 14, 1, false, "US"));
		specialDays.add(new SpecialDay("St._Patrick's_Day", 17, 2, false, "US"));
		specialDays.add(new SpecialDay("Veteran's_Day", 11, 10, false, "US"));
		specialDays.add(new SpecialDay("Labor_Day", nthdom(year, month, Calendar.MONDAY, 1), 8, true, "US"));
		specialDays
				.add(new SpecialDay("Martin_Luther_King_Day", nthdom(year, month, Calendar.MONDAY, 3), 0, false, "US"));
		specialDays.add(new SpecialDay("Presidents_Day", nthdom(year, month, Calendar.MONDAY, 3), 1, false, "US"));
		specialDays.add(new SpecialDay("Memorial_Day", nthdom(year, month, Calendar.MONDAY, -1), 4, true, "US"));
		specialDays.add(new SpecialDay("Columbus_Day", nthdom(year, month, Calendar.MONDAY, 2), 9, false, "US"));
		specialDays.add(new SpecialDay("Mother's_Day", nthdom(year, month, Calendar.SUNDAY, 2), 4, false, "US"));
		specialDays.add(new SpecialDay("Father's_Day", nthdom(year, month, Calendar.SUNDAY, 3), 5, false, "US"));
		specialDays.add(new SpecialDay("Thanksgiving", nthdom(year, month, Calendar.THURSDAY, 4), 10, true, "US"));

		// Canadian
		specialDays.add(new SpecialDay("Canada_Day", 1, 6, false, "CAN"));
		specialDays.add(new SpecialDay("Boxing_Day", 26, 11, false, "CAN"));
		specialDays.add(new SpecialDay("Civic_Holiday", nthdom(year, month, Calendar.MONDAY, 1), 7, false, "CAN"));
		specialDays.add(new SpecialDay("Remembrance_Day", 11, 10, false, "CAN"));
		specialDays.add(new SpecialDay("Labour_Day_(Can)", nthdom(year, month, Calendar.MONDAY, 1), 8, false, "CAN"));
		specialDays.add(new SpecialDay("Commonwealth_Day", nthdom(year, month, Calendar.MONDAY, 2), 2, false, "CAN"));
		specialDays.add(new SpecialDay("Thanksgiving_(Can)", nthdom(year, month, Calendar.MONDAY, 2), 9, false, "CAN"));
		// apart iets
		specialDays.add(new SpecialDay("Victoria_Day", 31, 9, false, "CAN"));

		// Common
		specialDays.add(new SpecialDay("New_Year's_Day", 1, 0, true, "GLOBAL"));
		specialDays.add(new SpecialDay("Christmas", 25, 11, true, "GLOBAL"));

		return specialDays;
	}

	/**
	 * Adds appointments to the to day.
	 * 
	 * @param day
	 *            the day
	 * @param l
	 *            list of appointment keys to add
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param date
	 *            the date
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static void addToDay(Day day, Collection<Integer> l) throws Exception {

		boolean pub = false;
		boolean priv = false;
		String sp = Prefs.getPref(PrefName.SHOWPUBLIC);
		if (sp.equals("true"))
			pub = true;
		sp = Prefs.getPref(PrefName.SHOWPRIVATE);
		if (sp.equals("true"))
			priv = true;

		if (l != null) {
			Iterator<Integer> it = l.iterator();
			Appointment appt;

			// iterate through the day's appts
			while (it.hasNext()) {
				Integer ik = it.next();

				// read the appt from the DB
				appt = AppointmentModel.getReference().getAppt(ik.intValue());

				// skip based on public/private flags
				if (appt.isPrivate()) {
					if (!priv)
						continue;
				} else {
					if (!pub)
						continue;
				}

				String color = appt.getColor();
				if (color == null)
					appt.setColor("black");

				// add apptto day
				day.addItem(appt);

				// set vacation and holiday flags at dayinfo level
				Integer v = appt.getVacation();
				if (v != null && v.intValue() != 0)
					day.setVacation(v.intValue());

				v = appt.getHoliday();
				if (v != null && v.intValue() == 1)
					day.setHoliday(1);

			}
		}

	}

	/**
	 * Gets the Day information for a given day.
	 * 
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param day
	 *            the day
	 * @return the Day object
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static Day getDay(int year, int month, int day) throws Exception {

		// get the base day key
		Calendar cal = new GregorianCalendar(year, month, day);

		Day ret = new Day();

		// get the list of appt keys from the map_
		Collection<Integer> l = AppointmentModel.getReference().getAppts(cal.getTime());
		addToDay(ret, l);

		// daylight savings time
		GregorianCalendar gc = new GregorianCalendar(year, month, day, 11, 00);
		boolean dstNow = TimeZone.getDefault().inDaylightTime(gc.getTime());
		gc.add(Calendar.DATE, -1);
		boolean dstYesterday = TimeZone.getDefault().inDaylightTime(gc.getTime());
		if (dstNow && !dstYesterday) {
			LabelEntity hol = new LabelEntity();
			hol.setColor("black");
			hol.setText(Resource.getResourceString("Daylight_Savings_Time"));
			ret.addItem(hol);
		} else if (!dstNow && dstYesterday) {
			LabelEntity hol = new LabelEntity();
			hol.setColor("black");
			hol.setText(Resource.getResourceString("Standard_Time"));
			ret.addItem(hol);
		}


		String show_us_hols = Prefs.getPref(PrefName.SHOWUSHOLIDAYS);
		String show_can_hols = Prefs.getPref(PrefName.SHOWCANHOLIDAYS);

		LabelEntity hol = new LabelEntity();
		hol.setDate(new GregorianCalendar(year, month, day, 00, 00).getTime());
		hol.setColor("purple");
		hol.setText(null);

		for (SpecialDay current : initSpecialDays(year, month)) {

			if (current.getRegion().equals("US") && show_us_hols.equals("true") && current.isSpecialDay(day, month)) {
				ret.setHoliday(current.isFreeDay() ? 1 : 0);
				hol.setText(Resource.getResourceString(current.getName()));
			}

			if (current.getRegion().equals("CAN") && show_can_hols.equals("true") && current.isSpecialDay(day, month)) {
				ret.setHoliday(current.isFreeDay() ? 1 : 0);
				hol.setText(Resource.getResourceString(current.getName()));
			}

			if (current.getRegion().equals("GLOBAL") && current.isSpecialDay(day, month)) {
				ret.setHoliday(current.isFreeDay() ? 1 : 0);
				hol.setText(Resource.getResourceString(current.getName()));
			}

			if (month == 4) {
				gc = new GregorianCalendar(year, month, 25);
				int diff = gc.get(Calendar.DAY_OF_WEEK);
				diff += 5;
				if (diff > 7)
					diff -= 7;
				if (day == 25 - diff) {
					hol.setText(Resource.getResourceString("Victoria_Day"));
				}
			}

			if (hol.getText() != null) {
				ret.addItem(hol);
			}
		}

		for (Model m : Model.getExistingModels()) {
			if (m instanceof CalendarEntityProvider) {
				List<CalendarEntity> el = ((CalendarEntityProvider) m).getEntities(cal.getTime());
				for (CalendarEntity e : el)
					ret.addItem(e);
			}
		}

		return (ret);
	}

	/**
	 * compute nth day of month for calculating when certain holidays fall.
	 * 
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param dayofweek
	 *            the day of the week
	 * @param week
	 *            the week of the month
	 * 
	 * @return the date
	 */
	private static int nthdom(int year, int month, int dayofweek, int week) {
		GregorianCalendar cal = new GregorianCalendar(year, month, 1);
		cal.set(Calendar.DAY_OF_WEEK, dayofweek);
		cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, week);
		return (cal.get(Calendar.DATE));
	}

	private int holiday; // set to indicate if any appt in the list is a

	private TreeSet<CalendarEntity> items; // list of appts for the day

	private int vacation; // vacation value for the day

	/**
	 * Instantiates a new day.
	 */
	private Day() {

		holiday = 0;
		vacation = 0;
		items = new TreeSet<CalendarEntity>(new apcompare());

	}

	/**
	 * Adds a CalendarEntity item to the Day
	 * 
	 * @param info
	 *            the CalendarEntity
	 */
	private void addItem(CalendarEntity info) {
		items.add(info);
	}

	/**
	 * Gets the holiday flag.
	 * 
	 * @return the holiday (1 = holiday)
	 */
	public int getHoliday() {
		return (holiday);
	}

	/**
	 * Gets all CalendarEntity items for the Day.
	 * 
	 * @return the items
	 */
	public Collection<CalendarEntity> getItems() {
		return (items);
	}

	/**
	 * Gets the vacation value for the Day.
	 * 
	 * @return the vacation value (0 = none, 1 = full day, 2 = half day)
	 */
	public int getVacation() {
		return (vacation);
	}

	/**
	 * Sets the holiday value
	 * 
	 * @param i
	 *            the new holiday value
	 */
	public void setHoliday(int i) {
		holiday = i;
	}

	/**
	 * Sets the vacation value
	 * 
	 * @param i
	 *            the new vacation value
	 */
	public void setVacation(int i) {
		vacation = i;
	}

}
