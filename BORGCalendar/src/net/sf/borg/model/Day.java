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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeSet;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.LabelEntity;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;

/**
 * Class Day pulls together and manages all of the items that make up the CalendarEntities for
 * a single day. It packages together all of a day's info as needed by a client (i.e. the UI).
 * 
 */
public class Day {

    /**
     * class to compare appointment strings for sorting.
     */
    private static class apcompare implements Comparator<CalendarEntity> {

        /**
         * convert the "logical" colors to a number for sorting by color
         * the actual color names listed below are no longer the displayed colors.
         */
        private static int colornum(String color) {

            if (color.equals("red"))
                return (1);
            if (color.equals("blue"))
                return (2);
            if (color.equals("green"))
                return (3);
            if (color.equals("black"))
                return (4);
            if (color.equals("white"))
                return (5);
            if (color.equals("brick"))
                return (6);
            if (color.equals("navy"))
                return (7);
            if (color.equals("purple"))
                return (8);
            return (9);

        }

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CalendarEntity so1, CalendarEntity so2) {

            String s1 = so1.getText();
            String s2 = so2.getText();

            String csort = Prefs.getPref(PrefName.COLORSORT);
            if (csort.equals("true")) {
                // color has first priority in the sort
                String c1 = so1.getColor();
                String c2 = so2.getColor();
                if (!c1.equals(c2)) {
                    if (colornum(c1) > colornum(c2))
                        return (1);
                    return (-1);
                }
            }

            // get times if any
            SimpleDateFormat d1 = AppointmentModel.getTimeFormat();

            try {

                // turn appt times from text into a Date for comparing
                Date dt1 = d1.parse(s1.substring(0, 8));
                Date dt2 = d1.parse(s2.substring(0, 8));

                // compare by date
                if (dt1.after(dt2))
                    return (1);
                return (-1);
            } catch (Exception e) {
                // if we got here, one or both appt had no time
                // and the date parse barfed - so just compare
                // strings lexicographically
                int res = s1.compareTo(s2);
                if (res != 0)
                    return (res);
                return (1);
            }
        }

    }

    /**
     * Adds appointments to the to day.
     * 
     * @param day the day
     * @param l list of appointment keys to add
     * @param year the year
     * @param month the month
     * @param date the date
     * 
     * @throws Exception the exception
     */
    private static void addToDay(Day day, Collection<Integer> l, int year, int month, int date
          ) throws Exception {
    	
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
                String tx = "", txFull = "";
                Integer ik = it.next();

                // read the appt from the DB
                appt = AppointmentModel.getReference().getAppt(ik.intValue());

                // skip based on public/private flags
                if (appt.getPrivate()) {
                    if (!priv)
                        continue;
                } else {
                    if (!pub)
                        continue;
                }

                // add time in front of the appt text
                if (!AppointmentModel.isNote(appt)) {
                    Date d = appt.getDate();
                    SimpleDateFormat sdf = AppointmentModel.getTimeFormat();
                    tx += sdf.format(d) + " ";
                    txFull += sdf.format(d) + " ";
                }

                // if the text is empty - skip it - should never be
                String xx = appt.getText();
                if (xx == null) {
                    tx = txFull = "";
                    continue;
                }

                String trunc = Prefs.getPref(PrefName.TRUNCAPPT);
                if (trunc.equals("true")) {
                    // !!!!! only show first line of appointment text !!!!!!
                    int ii = xx.indexOf('\n');
                    if (ii != -1) {
                        tx += xx.substring(0, ii);
                    } else {
                        tx += xx;
                    }
                } else {
                    tx += xx;
                }
                txFull += xx;

                // add repeat number
                if (Repeat.getRptNum(appt.getFrequency())) {
                    Calendar cur = new GregorianCalendar(year, month, date);
                    tx += " (" + Repeat.calculateRepeatNumber(cur, appt) + ")";
                    txFull += " (" + Repeat.calculateRepeatNumber(cur, appt) + ")";
                }
                
                if( appt.getVacation() != null && appt.getVacation().intValue() != 0)
                {
                	int key = AppointmentModel.dkey(year, month, date);
                	double vacationCount = AppointmentModel.getReference().vacationCount(key);
                	tx = "[" + vacationCount + "] " + tx;
                	txFull = "[" + vacationCount + "] " + txFull;
                }
                appt.setText(tx);

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
     * @param year the year
     * @param month the month
     * @param day the day
     * @return the Day object
     * 
     * @throws Exception the exception
     */
    public static Day getDay(int year, int month, int day) throws Exception {
        
        // get the base day key
        int key = AppointmentModel.dkey(year, month, day);

        Day ret = new Day();

        // get the list of appt keys from the map_
        Collection<Integer> l = AppointmentModel.getReference().getAppts(key);
        addToDay(ret, l, year, month, day);

        // daylight savings time
        GregorianCalendar gc = new GregorianCalendar(year, month, day, 11, 00);
        boolean dstNow = TimeZone.getDefault().inDaylightTime(gc.getTime());
        gc.add(Calendar.DATE, -1);
        boolean dstYesterday = TimeZone.getDefault().inDaylightTime(gc.getTime());
        if (dstNow && !dstYesterday) {
            Appointment hol = new Appointment();
            hol.setColor("black");
            hol.setText(Resource.getResourceString("Daylight_Savings_Time"));
            ret.addItem(hol);
        } else if (!dstNow && dstYesterday) {
            Appointment hol = new Appointment();
            hol.setColor("black");
            hol.setText(Resource.getResourceString("Standard_Time"));
            ret.addItem(hol);
        }

        // add canned US holidays
        // check user preferences first
        String show_us_hols = Prefs.getPref(PrefName.SHOWUSHOLIDAYS);

        if (show_us_hols.equals("true")) {

            // ok, we will add holiday appts
            // to the dayinfo for the US holidays below
            // the dayinfo.holiday flag is set if the holiday
            // is a day off from work and should cause the day to have
            // holiday coloring on the gui
            //
            // so, holidays only exist in the dayinfo objects, which are
            // temporary. they do not get added to the DB or even the appt
            // map_
        	LabelEntity hol = new LabelEntity();

            hol.setDate(new GregorianCalendar(year, month, day, 00, 00).getTime());

            hol.setColor("purple");

            hol.setText(null);
            if (month == 9 && day == 31) {
                hol.setText(Resource.getResourceString("Halloween"));
            } else if (month == 0 && day == 1) {
                hol.setText(Resource.getResourceString("New_Year's_Day"));
                ret.setHoliday(1);
            } else if (month == 11 && day == 25) {
                hol.setText(Resource.getResourceString("Christmas"));
                ret.setHoliday(1);
            } else if (month == 6 && day == 4) {
                hol.setText(Resource.getResourceString("Independence_Day"));
                ret.setHoliday(1);
            } else if (month == 1 && day == 2) {
                hol.setText(Resource.getResourceString("Ground_Hog_Day"));
            } else if (month == 1 && day == 14) {
                hol.setText(Resource.getResourceString("Valentine's_Day"));
            } else if (month == 2 && day == 17) {
                hol.setText(Resource.getResourceString("St._Patrick's_Day"));
            } else if (month == 10 && day == 11) {
                hol.setText(Resource.getResourceString("Veteran's_Day"));
            } else if (month == 8 && day == nthdom(year, month, Calendar.MONDAY, 1)) {
                hol.setText(Resource.getResourceString("Labor_Day"));
                ret.setHoliday(1);
            } else if (month == 0 && day == nthdom(year, month, Calendar.MONDAY, 3)) {
                hol.setText(Resource.getResourceString("Martin_Luther_King_Day"));
            } else if (month == 1 && day == nthdom(year, month, Calendar.MONDAY, 3)) {
                hol.setText(Resource.getResourceString("Presidents_Day"));
            } else if (month == 4 && day == nthdom(year, month, Calendar.MONDAY, -1)) {
                hol.setText(Resource.getResourceString("Memorial_Day"));
                ret.setHoliday(1);
            } else if (month == 9 && day == nthdom(year, month, Calendar.MONDAY, 2)) {
                hol.setText(Resource.getResourceString("Columbus_Day"));
            } else if (month == 4 && day == nthdom(year, month, Calendar.SUNDAY, 2)) {
                hol.setText(Resource.getResourceString("Mother's_Day"));
            } else if (month == 5 && day == nthdom(year, month, Calendar.SUNDAY, 3)) {
                hol.setText(Resource.getResourceString("Father's_Day"));
            } else if (month == 10 && day == nthdom(year, month, Calendar.THURSDAY, 4)) {
                hol.setText(Resource.getResourceString("Thanksgiving"));
                ret.setHoliday(1);
            }

            if (hol.getText() != null)
                ret.addItem(hol);

        }

        // add canned Canadian holidays
        // check user preferences first
        String show_can_hols = Prefs.getPref(PrefName.SHOWCANHOLIDAYS);
        if (show_can_hols.equals("true")) {

        	LabelEntity hol = new LabelEntity();
            hol.setDate(new GregorianCalendar(year, month, day, 00, 00).getTime());

            hol.setColor("purple");

            hol.setText(null);
            if (month == 0 && day == 1) {
                hol.setText(Resource.getResourceString("New_Year's_Day"));
                ret.setHoliday(1);
            } else if (month == 11 && day == 25) {
                hol.setText(Resource.getResourceString("Christmas"));
                ret.setHoliday(1);
            } else if (month == 6 && day == 1) {
                hol.setText(Resource.getResourceString("Canada_Day"));
            } else if (month == 11 && day == 26) {
                hol.setText(Resource.getResourceString("Boxing_Day"));
            } else if (month == 7 && day == nthdom(year, month, Calendar.MONDAY, 1)) {
                hol.setText(Resource.getResourceString("Civic_Holiday"));
            } else if (month == 10 && day == 11) {
                hol.setText(Resource.getResourceString("Remembrance_Day"));
            } else if (month == 8 && day == nthdom(year, month, Calendar.MONDAY, 1)) {
                hol.setText(Resource.getResourceString("Labour_Day_(Can)"));
            } else if (month == 2 && day == nthdom(year, month, Calendar.MONDAY, 2)) {
                hol.setText(Resource.getResourceString("Commonwealth_Day"));
            } else if (month == 9 && day == nthdom(year, month, Calendar.MONDAY, 2)) {
                hol.setText(Resource.getResourceString("Thanksgiving_(Can)"));
            } else if (month == 4) {
                gc = new GregorianCalendar(year, month, 25);
                int diff = gc.get(Calendar.DAY_OF_WEEK);
                diff += 5;
                if (diff > 7)
                    diff -= 7;
                if (day == 25 - diff) {
                    hol.setText(Resource.getResourceString("Victoria_Day"));
                }
            }

            if (hol.getText() != null)
                ret.addItem(hol);

        }
     
        // load any tasks
        if (Prefs.getBoolPref(PrefName.CAL_SHOW_TASKS)) {
            Collection<Project> pcol = TaskModel.getReference().get_projects(key);
            if (pcol != null) {
                for (Project pj : pcol) {                
                    ret.addItem(pj);

                }
            }
            Collection<Task> tasks = TaskModel.getReference().get_tasks(key);
            if (tasks != null) {
                for (Task task : tasks) {                
                    ret.addItem(task);
                }

            }
        }
        // subtasks
        if (Prefs.getBoolPref(PrefName.CAL_SHOW_SUBTASKS)) {
            Collection<Subtask> sts = TaskModel.getReference().get_subtasks(key);
            if (sts != null) {
                for (Subtask st : sts) {                  
                    ret.addItem(st);
                }

            }
        }
        // add birthdays from address book
        Collection<Address> addrs = AddressModel.getReference().getAddresses(key);
        if (addrs != null) {
            for (Address addr : addrs) {
                LabelEntity info = new LabelEntity();
                String color = info.getColor();

                if (color == null)
                    info.setColor("brick");

                Date bd = addr.getBirthday();
                GregorianCalendar g = new GregorianCalendar();
                g.setTime(bd);
                int bdyear = g.get(Calendar.YEAR);
                int yrs = year - bdyear;
                if (yrs < 0)
                    continue;

                String tx = Resource.getPlainResourceString("Birthday") + ": " + addr.getFirstName() + " " + addr.getLastName()
                        + "(" + yrs + ")";
                info.setText(tx);         
                info.setDate(new GregorianCalendar(year, month, day, 00, 00).getTime());
                ret.addItem(info);
            }
        }

        
        
        return (ret);

    }

    /**
     * compute nth day of month for calculating when certain holidays fall.
     * 
     * @param year the year
     * @param month the month
     * @param dayofweek the day of the week
     * @param week the week of the month
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
     * @param info the CalendarEntity
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
     * @param i the new holiday value
     */
    public void setHoliday(int i) {
        holiday = i;
    }

    /**
     * Sets the vacation value
     * 
     * @param i the new vacation value
     */
    public void setVacation(int i) {
        vacation = i;
    }

}
