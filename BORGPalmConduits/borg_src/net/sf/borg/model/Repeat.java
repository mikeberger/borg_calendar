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
import java.util.GregorianCalendar;

/**
 * A helper class for calculating repeating appointments.
 */
public class Repeat {
	public static final String TTH = "tth";

	public static final String MWF = "mwf";

	public static final String WEEKENDS = "weekends";

	public static final String WEEKDAYS = "weekdays";

	public static final String YEARLY = "yearly";

	public static final String MONTHLY_DAY = "monthly_day";

	public static final String MONTHLY = "monthly";

	public static final String BIWEEKLY = "biweekly";

	public static final String WEEKLY = "weekly";

	public static final String DAILY = "daily";

	public static final String NDAYS = "ndays";

	public static final String DAYLIST = "dlist";

	public static final String ONCE = "once";

	private Calendar start_;

	private Calendar cal;

	private Calendar current_;

	private String frequency_; // passed in string containing multiple items

	private String freq_; // internal freq

	private int field;

	private int dayOfWeekMonth;

	private int dayOfWeek;

	private int count_;

	private int incr;

	static public String getFreq(String f) {
		if (f == null)
			return null;
		int i = f.indexOf(',');
		if (i == -1)
			return (f);
		return (f.substring(0, i));

	}

	static public boolean getRptNum(String f) {
		if (f == null)
			return false;
		if (f.endsWith(",Y"))
			return true;
		return false;
	}

	static public Collection getDaylist(String f) {
		ArrayList daylist = new ArrayList();
		if (f == null || !f.startsWith(DAYLIST))
			return daylist;

		// System.out.println(f + " " + DAYLIST.length());
		int i2 = f.indexOf(',', DAYLIST.length() + 1);
		String list = null;
		if (i2 != -1)
			list = f.substring(DAYLIST.length() + 1, i2);
		else
			list = f.substring(DAYLIST.length() + 1);

		// System.out.println(list);
		if (list.indexOf("1") != -1)
			daylist.add(new Integer(Calendar.SUNDAY));
		if (list.indexOf("2") != -1)
			daylist.add(new Integer(Calendar.MONDAY));
		if (list.indexOf("3") != -1)
			daylist.add(new Integer(Calendar.TUESDAY));
		if (list.indexOf("4") != -1)
			daylist.add(new Integer(Calendar.WEDNESDAY));
		if (list.indexOf("5") != -1)
			daylist.add(new Integer(Calendar.THURSDAY));
		if (list.indexOf("6") != -1)
			daylist.add(new Integer(Calendar.FRIDAY));
		if (list.indexOf("7") != -1)
			daylist.add(new Integer(Calendar.SATURDAY));

		return (daylist);

	}

	static public int getNDays(String f) {
		if (f == null)
			return 0;
		if (!f.startsWith(NDAYS))
			return (0);

		int i2 = f.indexOf(',', NDAYS.length() + 1);
		if (i2 != -1)
			return (Integer.parseInt(f.substring(NDAYS.length() + 1, i2)));

		return (Integer.parseInt(f.substring(NDAYS.length() + 1)));

	}

	Repeat(Calendar start, String frequency) {
		this.start_ = start;
		this.frequency_ = frequency;
		cal = new GregorianCalendar(0, 0, 0);
		cal.setTime(start.getTime());
		current_ = cal;
		count_ = 0;
		incr = 1;
		field = Calendar.DATE;
		dayOfWeek = 0;
		dayOfWeekMonth = 0;

		if (!isRepeating())
			return;

		freq_ = getFreq(frequency);
		if (freq_.equals(WEEKLY))
			incr = 7;
		else if (freq_.equals(BIWEEKLY))
			incr = 14;
		else if (freq_.equals(MONTHLY))
			field = Calendar.MONTH;
		else if (freq_.equals(MONTHLY_DAY)) {
			incr = 0;
			dayOfWeek = start.get(Calendar.DAY_OF_WEEK);
			dayOfWeekMonth = start.get(Calendar.DAY_OF_WEEK_IN_MONTH);
		} else if (freq_.equals(YEARLY))
			field = Calendar.YEAR;
		else if (freq_.equals(MWF)) {
			incr = 0;
		} else if (freq_.equals(TTH)) {
			incr = 0;
		} else if (freq_.equals(NDAYS)) {
			incr = getNDays(frequency_);
		} else if (freq_.equals(DAYLIST)) {
			incr = 0;
		}
	}

	final boolean isRepeating() {
		String f = getFreq(frequency_);
		return f != null && !f.equals(ONCE);
	}

	// our current date
	final Calendar current() {
		return current_;
	}

	// return when the appt repeats until given the count
	final static public Calendar until(Calendar start, String frequency,
			int count) {
		Calendar c = null;

		if (count == 9999) {
			// for unlimited rpt, go for 2 years in the future
			c = new GregorianCalendar();
			c.add(Calendar.YEAR, 2);
			return (c);
		}

		Repeat r = new Repeat(start, frequency);
		for (int i = 1; i < count; i++) {
			c = r.next();
		}

		return (c);
	}

	// calculate the next date of this repeat
	final Calendar next() {
		if (!isRepeating()) {
			current_ = null;
			return current_;
		}

		current_ = cal;
		++count_;

		// add the required increment
		if (incr != 0)
			cal.add(field, incr);

		if (freq_.equals(WEEKDAYS)) {
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			if (dow == Calendar.SATURDAY)
				cal.add(Calendar.DATE, 2);
			else if (dow == Calendar.SUNDAY)
				cal.add(Calendar.DATE, 1);
		} else if (freq_.equals(WEEKENDS)) {
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			if (dow == Calendar.MONDAY)
				cal.add(Calendar.DATE, 5);
			else if (dow == Calendar.TUESDAY)
				cal.add(Calendar.DATE, 4);
			else if (dow == Calendar.WEDNESDAY)
				cal.add(Calendar.DATE, 3);
			else if (dow == Calendar.THURSDAY)
				cal.add(Calendar.DATE, 2);
			else if (dow == Calendar.FRIDAY)
				cal.add(Calendar.DATE, 1);
		} else if (freq_.equals(MWF)) {
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			if (dow == Calendar.FRIDAY) {
				cal.add(Calendar.DATE, 3);
			} else {
				cal.add(Calendar.DATE, 2);
			}
		} else if (freq_.equals(TTH)) {
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			if (dow == Calendar.THURSDAY) {
				cal.add(Calendar.DATE, 5);
			} else {
				cal.add(Calendar.DATE, 2);
			}
		} else if (freq_.equals(MONTHLY_DAY)) {
			// Attempt to find a date falling on the
			// same day of week and week number
			// within a subsequent month.
			cal.setTime(start_.getTime());
			cal.add(Calendar.MONTH, count_);
			cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, dayOfWeekMonth);
			int dowm = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
			if (dowm != dayOfWeekMonth)
				current_ = null;
			// not enough days in this month
		} else if (freq_.equals(DAYLIST)) {
			Collection daylist = getDaylist(frequency_);
			// System.out.println(daylist);
			if (daylist != null && !daylist.isEmpty()) {
				// advance to next day of the week in the list
				while (true) {
					cal.add(Calendar.DATE, 1);
					int dow = cal.get(Calendar.DAY_OF_WEEK);
					if (daylist.contains(new Integer(dow))) {
						break;
					}
				}
			}
		}

		// bug fix - if repeating by month/date, must adjust if original date
		// was
		// 29, 30, or 31.
		int startDate = start_.get(Calendar.DATE);
		int maxDate = cal.getActualMaximum(Calendar.DATE);
		if (field == Calendar.MONTH) {
			if (startDate <= maxDate) {
				cal.set(Calendar.DATE, startDate);
			} else {
				cal.set(Calendar.DATE, maxDate);
			}
		}

		return current_;
	}
}
