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

import net.sf.borg.common.Resource;
import net.sf.borg.model.entity.Appointment;

/**
 * A helper class for calculating all things about repeating appointments. It is mainly an iterator that
 * can take repeat parameters for an appointment and iterate through the repeats
 */
public class Repeat {
    
	// the various repeat options. each constant represents a different way that 
	// an appointment can repeat
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

    /** the appointment date (ie the first occurrence) */
    private Calendar start_;

    // scratch
    private Calendar cal;

    /** The current repeat occurrence that this object is set to (via iteration) */
    private Calendar current_;

    /** The repeat data from the appt- string that is passed in that encodes frequency, particular repeat days, and a flag to indicate whether to show the repeat number */
    private final String frequency_; 

    /** The repeat frequency */
    private String freq_; 

    /** the calendar field that is incrementing for each repeat */
    private int field;

    /** scratch to hold the day of the week in a month for certain repeat types */
    private int dayOfWeekMonth;

    /** scratch to hold the day of week for certain repeat types */
    private int dayOfWeek;

    /** the number of the repeat we have iterated to currently */
    private int count_;

    /** The amount to increment the calendar field by for each repeat */
    private int incr;

    /** The frequencies string values in an array for mapping (legacy code)*/
    static private String freqs[] = { ONCE, DAILY, WEEKLY, BIWEEKLY, MONTHLY, MONTHLY_DAY, YEARLY, WEEKDAYS, WEEKENDS, MWF, TTH,
            NDAYS, DAYLIST };

    /**
     * Checks  to see if a particular day is valid for certain strict repeat types
     * 
     * @param date the date
     * @param freq the frequency
     * @param daylist the daylist (for repeat with a list of days)
     * 
     * @return true, if the daye is compatible with the repeat frequency
     */
    static public boolean isCompatible(Calendar date, String freq, Collection<Integer> daylist) {
        String f = freqToEnglish(freq);
        int day = date.get(Calendar.DAY_OF_WEEK);
        if (f.equals(WEEKDAYS) && (day == Calendar.SATURDAY || day == Calendar.SUNDAY))
            return false;
        else if (f.equals(WEEKENDS) && (day != Calendar.SATURDAY && day != Calendar.SUNDAY))
            return false;
        else if (f.equals(MWF) && (day != Calendar.MONDAY && day != Calendar.WEDNESDAY && day != Calendar.FRIDAY))
            return false;
        else if (f.equals(TTH) && (day != Calendar.TUESDAY && day != Calendar.THURSDAY))
            return false;
        else if (f.equals(DAYLIST) && !daylist.contains(new Integer(day)))
            return false;
        return true;
    }

    /**
     * get the translated string for a frequency
     * 
     * @param i the index of the frequency
     * 
     * @return the translation
     */
    static public String getFreqString(int i) {
        if (i < 0 || i >= freqs.length)
            return null;
        return (Resource.getResourceString(freqs[i]));
    }

    /**
     * get the translated string for a frequency
     * 
     * @param fr the internal frequency string
     * 
     * @return the translation
     */
    static public String getFreqString(String fr) {
      return (Resource.getResourceString((fr == null) ? ONCE : fr));
    }

    /**
     * convert the translated frequency string to the internal string
     * 
     * @param fr the trnalsated frequency
     * 
     * @return the internal frequency string
     */
    static public String freqToEnglish(String fr) {
        for (int i = 0; i < freqs.length; i++) {
            if (fr.equals(Resource.getResourceString(freqs[i]))) {
                return (freqs[i]);
            }
        }
        return (ONCE);
    }

   
    /**
     * generate the encoded frequency string that is stored in the appointment - that encodes
     * frequency, daylist, and repeat number flag
     * 
     * @param uistring the translated frequency string from the ui
     * @param ndays the ndays field from the UI for NDAYS repeating
     * @param rptnum the "show repeat number" flag
     * @param daylist the daylist for DAYLIST repeating
     * 
     * @return the string
     */
    static public String freqString(String uistring, Integer ndays, boolean rptnum, Collection<Integer> daylist) {
        String f = freqToEnglish(uistring);
        if (f.equals(NDAYS)) {
            f += "," + ndays;
        }

        if (f.equals(DAYLIST)) {
            f += ",";
            if (daylist != null) {
                if (daylist.contains(new Integer(Calendar.SUNDAY)))
                    f += "1";
                if (daylist.contains(new Integer(Calendar.MONDAY)))
                    f += "2";
                if (daylist.contains(new Integer(Calendar.TUESDAY)))
                    f += "3";
                if (daylist.contains(new Integer(Calendar.WEDNESDAY)))
                    f += "4";
                if (daylist.contains(new Integer(Calendar.THURSDAY)))
                    f += "5";
                if (daylist.contains(new Integer(Calendar.FRIDAY)))
                    f += "6";
                if (daylist.contains(new Integer(Calendar.SATURDAY)))
                    f += "7";
            }
        }

        if (rptnum) {
            f += ",Y";
        }

        return (f);

    }

    /**
     * Gets the frequency from the encoded appointment string
     * 
     * @param f the frequency string from the appointment
     * 
     * @return the frequency
     */
    static public String getFreq(String f) {
        if (f == null)
            return null;
        int i = f.indexOf(',');
        if (i == -1)
            return (f);
        return (f.substring(0, i));

    }

    /**
     * Gets the repeat number flag from the encoded appointment string
     * 
     * @param f the frequency string from the appointment
     * 
     * @return the repeat number flag
     */
    static public boolean getRptNum(String f) {
        if (f == null)
            return false;
        if (f.endsWith(",Y"))
            return true;
        return false;
    }

    /**
     * Gets the daylist from the encoded appointment string
     * 
     * @param f the frequency string from the appointment
     * 
     * @return the daylist
     */
    static public Collection<Integer> getDaylist(String f) {
        ArrayList<Integer> daylist = new ArrayList<Integer>();
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

    /**
     * Gets the n days from the encoded appointment string
     * 
     * @param f the encoded appointment string
     * 
     * @return the n days value
     */
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

    /**
     * Instantiates a new repeat object
     * 
     * @param start the start date of the repeat (the appointment date)
     * @param frequency the frequency string from the appointment
     */
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

        // set up the iteration parameters from the frequency
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

    /**
     * 
     * 
     * @return true, if this object represents a repeating item
     */
    final boolean isRepeating() {
        String f = getFreq(frequency_);
        return f != null && !f.equals(ONCE);
    }

    /**
     * Checks if an appointment repeats
     * 
     * @param ap the appointment
     * 
     * @return true, if the appointment is repeating
     */
    public static boolean isRepeating(Appointment ap) {
        String f = getFreq(ap.getFrequency());
        return f != null && !f.equals(ONCE);
    }

    /**
     * get the current date of this iterator
     * 
     * @return the current date
     */
    final Calendar current() {
        return current_;
    }

    /**
     * Calculate the number of a repeat given the date and the appointment
     * 
     * @param current the date
     * @param appt the appointment
     * 
     * @return the number of the repeat (starting with 1)
     */
    final static public int calculateRepeatNumber(Calendar current, Appointment appt) {
        Calendar start = new GregorianCalendar();
        Calendar c = start;
        start.setTime(appt.getDate());
        Repeat r = new Repeat(start, appt.getFrequency());
        for (int i = 1;; i++) {
            if ((c.get(Calendar.YEAR) == current.get(Calendar.YEAR))
                    && (c.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)))
                return (i);
            if (c.after(current))
                return (0);
            c = r.next();
            if (c == null)
                return (0);
        }

    }

    /**
     * iterate to the next repeat date.
     * 
     * @return the next repeat date
     */
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
            Collection<Integer> daylist = getDaylist(frequency_);
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
    
    /**
     * calculate the repeat times value for an appointment based on the until date or the repeat times
     * @param appt the appointment
     * @return repeat times
     */
    static public int calculateTimes( Appointment appt )
    {
    	if( !Repeat.isRepeating(appt))
    		return 1;
    	
    	if( appt.getRepeatUntil() == null)
    	{
    		if( appt.getTimes() != null )
    			return appt.getTimes().intValue();
    		return 1;
    	}
    	
    	Calendar cal = new GregorianCalendar();
    	cal.setTime(appt.getDate());
    	Repeat repeat = new Repeat( cal, appt.getFrequency() );
    	Calendar until = new GregorianCalendar();
    	until.setTime(appt.getRepeatUntil());
    	until.set(Calendar.HOUR_OF_DAY, 23);
    	until.set(Calendar.MINUTE, 59);
    	int times = 0;
    	for( ; repeat.current() != null; repeat.next())
    	{
    		if( repeat.current().after(until))
    			break;
    		times++;
    	}
    	    	
    	return times;
    }
}
