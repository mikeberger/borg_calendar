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

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.LabelEntity;

import java.io.Serializable;
import java.util.*;

/**
 * Class Day pulls together and manages all of the items that make up the
 * CalendarEntities for a single day. It packages together all of a day's info
 * as needed by a client (i.e. the UI).
 * 
 */
public class Day {
	private int holiday; // set to indicate if any appt in the list is a holiday
	private TreeSet<CalendarEntity> items; // list of appts for the day
	private int vacation; // vacation value for the day

	private static final String BLACK = "black";
	private static final boolean SHOW_PRIVATE_APPT = Prefs.getPref(PrefName.SHOWPRIVATE).equals(SpecialDay.TRUE);
	private static final boolean SHOW_PUBLIC_APPT = Prefs.getPref(PrefName.SHOWPUBLIC).equals(SpecialDay.TRUE);


	/**
	 * Instantiates a new day.
	 */
	private Day() {

		holiday = 0;
		vacation = 0;
		items = new TreeSet<>(new AppointmentCompare());

	}

	/**
	 * class to compare appointment strings for sorting.
	 */
	private static class AppointmentCompare implements Comparator<CalendarEntity>, Serializable {

		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(CalendarEntity so1, CalendarEntity so2) {
			boolean prioritySort = Prefs.getPref(PrefName.PRIORITY_SORT).equals(SpecialDay.TRUE);
			if (prioritySort) {
				Integer p1 = so1.getPriority();
				Integer p2 = so2.getPriority();

				if (p1 != null && p2 != null) {
					if (p1.intValue() != p2.intValue())
						return p1 > p2 ? 1 : -1;
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
				dt1 = getTimeWithoutDate((Appointment) so1);
			}
			if (so2 instanceof Appointment && !AppointmentModel.isNote((Appointment) so2)) {
				dt2 = getTimeWithoutDate((Appointment) so2);
			}

			if (dt1 != null && dt2 != null)
				return dt1.after(dt2) ? 1 : -1;
			if (dt1 != null)
				return -1;
			if (dt2 != null)
				return 1;

			// if we got here, just compare
			// strings lexicographically
			int res = so1.getText().compareTo(so2.getText());
			if (res != 0)
				return res;
			return 1;

		}

		private Date getTimeWithoutDate(Appointment appointment) {
			Calendar cal = new GregorianCalendar();
			cal.setTime(appointment.getDate());
			cal.set(1, Calendar.FEBRUARY, 2000);
			return cal.getTime();
		}

	}

	/**
	 * Adds appointments to the to day.
	 * 
	 * @param day
	 *            the day
	 * @param listOfAppointmentKeys
	 *            list of appointment keys to add	 *
	 * @throws Exception
	 *             the exception
	 */
	private static void addToDay(Day day, Collection<Integer> listOfAppointmentKeys) throws Exception {
		if (listOfAppointmentKeys != null) {
			// iterate through the day's appts
			for (Integer listOfAppointmentKey : listOfAppointmentKeys) {
				Appointment appt = AppointmentModel.getReference().getAppt(listOfAppointmentKey);
				if(checkIfAppointmentToShow(appt))
					setAppointmentToDay(day, appt);
			}
		}
	}

	private static boolean checkIfAppointmentToShow(Appointment appointment) {
		if (appointment.isPrivate()) {
			if (!SHOW_PRIVATE_APPT)
				return false;
		} else {
			if (!SHOW_PUBLIC_APPT)
				return false;
		}
		return true;
	}

	private static void setAppointmentToDay(Day day, Appointment appointment) {
		// skip based on public/private flags
		if (appointment.getColor() == null)
            appointment.setColor(BLACK);

		// add apptto day
		day.addItem(appointment);

		// set vacation and holiday flags at dayinfo level
		Integer vacationValue = appointment.getVacation();
		if (vacationValue != null && vacationValue != 0)
            day.setVacation(vacationValue);

		Integer holidayValue = appointment.getHoliday();
		if (holidayValue != null && holidayValue == 1)
            day.setHoliday(1);
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

		Day dayToGet = new Day();

		// get the list of appt keys from the map_
		Collection<Integer> listOfAppointments = AppointmentModel.getReference().getAppts(cal.getTime());
		addToDay(dayToGet, listOfAppointments);

		// daylight savings time
		GregorianCalendar gc = new GregorianCalendar(year, month, day, 11, 0);
		boolean dstNow = TimeZone.getDefault().inDaylightTime(gc.getTime());
		gc.add(Calendar.DATE, -1);
		boolean dstYesterday = TimeZone.getDefault().inDaylightTime(gc.getTime());
		if (dstNow && !dstYesterday) {
			LabelEntity hol = new LabelEntity();
			hol.setColor(BLACK);
			hol.setText(Resource.getResourceString("Daylight_Savings_Time"));
			dayToGet.addItem(hol);
		} else if (!dstNow && dstYesterday) {
			LabelEntity hol = new LabelEntity();
			hol.setColor(BLACK);
			hol.setText(Resource.getResourceString("Standard_Time"));
			dayToGet.addItem(hol);
		}
		LabelEntity specialDayLabel = SpecialDay.getPossibleSpecialDayLabel(year, month, day, dayToGet);
		if(specialDayLabel != null)
			dayToGet.addItem(specialDayLabel);

		for (Model m : Model.getExistingModels()) {
			if (m instanceof CalendarEntityProvider) {
				List<CalendarEntity> el = ((CalendarEntityProvider) m).getEntities(cal.getTime());
				for (CalendarEntity e : el)
					dayToGet.addItem(e);
			}
		}

		return dayToGet;
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
		return holiday;
	}

	/**
	 * Gets all CalendarEntity items for the Day.
	 * 
	 * @return the items
	 */
	public Collection<CalendarEntity> getItems() {
		return items;
	}

	/**
	 * Gets the vacation value for the Day.
	 * 
	 * @return the vacation value (0 = none, 1 = full day, 2 = half day)
	 */
	public int getVacation() {
		return vacation;
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
