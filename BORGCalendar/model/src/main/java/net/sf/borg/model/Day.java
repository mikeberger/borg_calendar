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
	private final TreeSet<CalendarEntity> items;
	private static final String BLACK = "black";
	private static final String TRUE = "true";
	private static final boolean SHOW_PRIVATE_APPOINTMENTS = Prefs.getPref(PrefName.SHOWPRIVATE).equals(TRUE);
	private static final boolean SHOW_PUBLIC_APPOINTMENTS = Prefs.getPref(PrefName.SHOWPUBLIC).equals(TRUE);

	private int vacation;
	private int holiday;

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
		public int compare(CalendarEntity calendarEntity1, CalendarEntity calendarEntity2) {
			if (Prefs.getPref(PrefName.PRIORITY_SORT).equals(TRUE)) {
				return compareByPriority(calendarEntity1, calendarEntity2);
			}

			// appointments with a time come before notes
			Integer compareValue = compareByTime(calendarEntity1, calendarEntity2);
			if (compareValue != null)
				return compareValue;

			// if we got here, just compare
			// strings lexicographically
			return compareByLexicographically(calendarEntity1, calendarEntity2);

		}

		/**
		 * Method to compare two CalendarEntities by Time
		 * @param calendarEntity1
		 * @param calendarEntity2
         * @return
         */
		private Integer compareByTime(CalendarEntity calendarEntity1, CalendarEntity calendarEntity2) {
			// use appointment time of day (not date - due to repeats) to sort next
			if (isAppointment(calendarEntity1) && isAppointment(calendarEntity2)) {
				Date dt1 = getTimeWithoutDate((Appointment) calendarEntity1);
				Date dt2  = getTimeWithoutDate((Appointment) calendarEntity2);
				return dt1.after(dt2) ? 1 : -1;
			}
			return null;
		}

		/**
		 * Check if a CalendarEntity is an appointment and not a note
		 * @param calendarEntity
         * @return
         */
		private boolean isAppointment(CalendarEntity calendarEntity) {
			return calendarEntity instanceof Appointment && !AppointmentModel.isNote((Appointment) calendarEntity);
		}

		/**
		 * Method to sort CalendarEntities by Lexico Graphically
		 * @param calendarEntity1
		 * @param calendarEntity2
         * @return
         */
		private int compareByLexicographically(CalendarEntity calendarEntity1, CalendarEntity calendarEntity2) {
			return (calendarEntity1.getText().compareTo(calendarEntity2.getText()) != 0) ? calendarEntity1.getText().compareTo(calendarEntity2.getText()) : 1;
		}

		/**
		 * Method to sort CalendarEntities by priority
		 * @param calendarEntity1
		 * @param calendarEntity2
         * @return
         */
		private Integer compareByPriority(CalendarEntity calendarEntity1, CalendarEntity calendarEntity2) {
			Integer calendarEntity1Priority = calendarEntity1.getPriority();
			Integer calendarEntity2Priority = calendarEntity2.getPriority();

			if (calendarEntity1Priority != null && calendarEntity2Priority != null) {
                if (calendarEntity1Priority.intValue() != calendarEntity2Priority.intValue())
                    return calendarEntity1Priority > calendarEntity2Priority ? 1 : -1;
            } else if (calendarEntity1Priority != null)
                return -1;
            return 1;
		}

		/**
		 * Method to get the time of an appointment without looking at de date
		 * @param appointment
         * @return
         */
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
			// iterate through the day's appointments
			for (Integer listOfAppointmentKey : listOfAppointmentKeys) {
				Appointment appointment = AppointmentModel.getReference().getAppt(listOfAppointmentKey);
				if(checkIfAppointmentToShow(appointment))
					setAppointmentToDay(day, appointment);
			}
		}
	}

	/**
	 * check if a appoint has to been shown at the calendar UI
	 * @param appointment
	 * @return
     */
	private static boolean checkIfAppointmentToShow(Appointment appointment) {
		if (appointment.isPrivate()) {
			if (!SHOW_PRIVATE_APPOINTMENTS)
				return false;
		} else {
			if (!SHOW_PUBLIC_APPOINTMENTS)
				return false;
		}
		return true;
	}

	/**
	 * Method to set an Appointment to a day object for the calendar UI
	 * @param day
	 * @param appointment
     */
	private static void setAppointmentToDay(Day day, Appointment appointment) {
		// skip based on public/private flags
		if (appointment.getColor() == null)
            appointment.setColor(BLACK);

		// add appointment to day
		day.addItem(appointment);

		// set vacation and holiday flags at day-info level
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
		Calendar calendar = new GregorianCalendar(year, month, day);

		Day dayToGet = new Day();

		// get the list of appointment keys from the map_
		Collection<Integer> listOfAppointments = AppointmentModel.getReference().getAppts(calendar.getTime());
		addToDay(dayToGet, listOfAppointments);

		// daylight savings time
		addDaylightSavingsTimeOrStandardTime(year, month, day, dayToGet);

		// add possible special day
		addPossibleSpecialDay(year, month, day, dayToGet);

		for (Model model : Model.getExistingModels()) {
			if (model instanceof CalendarEntityProvider) {
				List<CalendarEntity> entityList = ((CalendarEntityProvider) model).getEntities(calendar.getTime());
				for (CalendarEntity entity : entityList)
					dayToGet.addItem(entity);
			}
		}
		return dayToGet;
	}

	/**
	 * Method to add a special day to a Day object for the calendar UI
	 * @param year
	 * @param month
	 * @param day
	 * @param dayToAddLabel
     */
	private static void addPossibleSpecialDay(int year, int month, int day, Day dayToAddLabel) {
		LabelEntity specialDayLabel = SpecialDay.getPossibleSpecialDayLabel(year, month, day, dayToAddLabel);
		if(specialDayLabel != null)
			dayToAddLabel.addItem(specialDayLabel);
	}

	/**
	 * A method to add Daylight savings Time or Standard Time change to a day
	 * @param year
	 * @param month
	 * @param day
	 * @param dayToAddLabel
     */
	private static void addDaylightSavingsTimeOrStandardTime(int year, int month, int day, Day dayToAddLabel) {
		GregorianCalendar gc = new GregorianCalendar(year, month, day, 11, 0);
		boolean dateNow = TimeZone.getDefault().inDaylightTime(gc.getTime());
		gc.add(Calendar.DATE, -1);
		boolean dayBeforeNow = TimeZone.getDefault().inDaylightTime(gc.getTime());
		if (dateNow && !dayBeforeNow) {
			addTimeLabel(dayToAddLabel, Resource.getResourceString("Daylight_Savings_Time"));
		} else if (!dateNow && dayBeforeNow) {
			addTimeLabel(dayToAddLabel, Resource.getResourceString("Standard_Time"));
		}
	}

	/**
	 * Method to add an Time Label to a day
	 *
	 * @param dayToAddLabel is the day that gets the label
	 * @param timeLabel a string as text of the label
     */
	private static void addTimeLabel(Day dayToAddLabel, String timeLabel) {
		LabelEntity label = new LabelEntity();
		label.setColor(BLACK);
		label.setText(timeLabel);
		dayToAddLabel.addItem(label);
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
	private void setVacation(int i) {
		vacation = i;
	}
}