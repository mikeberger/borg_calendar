/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.common;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Some common date utility logic
 */
public class DateUtil {

	/**
	 * Checks if one date falls on a later calendar day than another.
	 * 
	 * @param d1
	 *            the first date
	 * @param d2
	 *            the second date
	 * 
	 * @return true, if is after
	 */
	public static boolean isAfter(Date d1, Date d2) {

		GregorianCalendar tcal = new GregorianCalendar();
		tcal.setTime(d1);
		tcal.set(Calendar.HOUR_OF_DAY, 0);
		tcal.set(Calendar.MINUTE, 0);
		tcal.set(Calendar.SECOND, 0);
		GregorianCalendar dcal = new GregorianCalendar();
		dcal.setTime(d2);
		dcal.set(Calendar.HOUR_OF_DAY, 0);
		dcal.set(Calendar.MINUTE, 10);
		dcal.set(Calendar.SECOND, 0);

		if (tcal.getTime().after(dcal.getTime())) {
			return true;
		}

		return false;
	}
	
	/**
	 * set a date to midnight
	 * @param d - the date
	 */
	static public Date setToMidnight(Date d)
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(d);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * return the number of the day of the epoch for a given date. this provides
	 * a decent Date to int converter that returns the same value for all Dates
	 * on a given day.
	 * 
	 * @param d
	 *            the date
	 * 
	 * @return the days from the beginning of the epoch until d
	 */
	static public int dayOfEpoch(Date d) {
		// adjust to local time
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(d);
		cal.set(Calendar.HOUR_OF_DAY, 11);
		return (int) (cal.getTime().getTime() / 1000 / 60 / 60 / 24);
	}
	
	/**
	 * generate a human readable string for a particular number of minutes
	 * 
	 * @param mins - the number of minutes
	 * 
	 * @return the string
	 */
	public static String minuteString(int mins) {
		
		int hours = mins / 60;
		int minsPast = mins % 60;
		
		String minutesString;
		String hoursString;
		
		if (hours > 1) {
			hoursString = hours + " " + Resource.getResourceString("Hours");
		} else if (hours > 0) {
			hoursString = hours + " " + Resource.getResourceString("Hour");
		} else {
			hoursString = "";
		}

		if (minsPast > 1) {
			minutesString = minsPast + " " + Resource.getResourceString("Minutes");
		} else if (minsPast > 0) {
			minutesString = minsPast + " " + Resource.getResourceString("Minute");
		} else if (hours >= 1) {
			minutesString = "";
		} else {
			minutesString = minsPast + " " + Resource.getResourceString("Minutes");
		}

		// space between hours and minutes
		if (!hoursString.equals("") && !minutesString.equals(""))
			minutesString = " " + minutesString;

		return hoursString + minutesString;
	}


}
