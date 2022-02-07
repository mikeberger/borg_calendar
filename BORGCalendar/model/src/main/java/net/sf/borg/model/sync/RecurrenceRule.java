package net.sf.borg.model.sync;

import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;

import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Logger;

public class RecurrenceRule {

    static private final Logger log = Logger.getLogger("net.sf.borg");


    static public String getRRule(Appointment ap){
        String rec = "FREQ=";
        String freq = Repeat.getFreq(ap.getFrequency());

        if (freq == null || freq.equals(Repeat.DAILY)) {
            rec += "DAILY";
        } else if (freq.equals(Repeat.WEEKLY)) {
            rec += "WEEKLY";
        } else if (freq.equals(Repeat.BIWEEKLY)) {
            rec += "WEEKLY;INTERVAL=2";
        } else if (freq.equals(Repeat.MONTHLY)) {
            Date dd = ap.getDate();
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(dd);
            rec += "MONTHLY;BYMONTHDAY=" + gc.get(java.util.Calendar.DATE);
        } else if (freq.equals(Repeat.MONTHLY_DAY)) {
            Date dd = ap.getDate();
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(dd);
            int dayOfWeek = gc.get(java.util.Calendar.DAY_OF_WEEK);
            int dayOfWeekMonth = gc.get(java.util.Calendar.DAY_OF_WEEK_IN_MONTH);
            String days[] = new String[] { "SU", "MO", "TU", "WE", "TH", "FR", "SA" };
            rec += "MONTHLY;BYDAY=" + dayOfWeekMonth + days[dayOfWeek - 1];
        } else if (freq.equals(Repeat.MONTHLY_DAY_LAST)) {
            Date dd = ap.getDate();
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(dd);
            int dayOfWeek = gc.get(java.util.Calendar.DAY_OF_WEEK);
            String days[] = new String[] { "SU", "MO", "TU", "WE", "TH", "FR", "SA" };
            rec += "MONTHLY;BYDAY=" + "-1" + days[dayOfWeek - 1];
        } else if (freq.equals(Repeat.YEARLY)) {
            rec += "YEARLY";
        } else if (freq.equals(Repeat.NDAYS)) {
            rec += "DAILY;INTERVAL=" + Repeat.getNValue(ap.getFrequency());
        } else if (freq.equals(Repeat.NWEEKS)) {
            rec += "WEEKLY;INTERVAL=" + Repeat.getNValue(ap.getFrequency());
        } else if (freq.equals(Repeat.NMONTHS)) {
            rec += "MONTHLY;INTERVAL=" + Repeat.getNValue(ap.getFrequency());
        } else if (freq.equals(Repeat.NYEARS)) {
            rec += "YEARLY;INTERVAL=" + Repeat.getNValue(ap.getFrequency());
        } else if (freq.equals(Repeat.WEEKDAYS)) {
            rec += "WEEKLY;BYDAY=MO,TU,WE,TH,FR";
        } else if (freq.equals(Repeat.MWF)) {
            rec += "WEEKLY;BYDAY=MO,WE,FR";
        } else if (freq.equals(Repeat.WEEKENDS)) {
            rec += "WEEKLY;BYDAY=SU,SA";
        } else if (freq.equals(Repeat.TTH)) {
            rec += "WEEKLY;BYDAY=TU,TH";
        } else if (freq.equals(Repeat.DAYLIST)) {
            String days[] = new String[] { "SU", "MO", "TU", "WE", "TH", "FR", "SA" };
            rec += "WEEKLY;BYDAY=";
            Collection<Integer> c = Repeat.getDaylist(ap.getFrequency());
            Iterator<Integer> it = c.iterator();
            while (it.hasNext()) {
                Integer i = it.next();
                rec += days[i - 1];
                if (it.hasNext())
                    rec += ",";
            }

        } else {
            log.warning("Could not export appt " + ap.getKey() + ap.getText());
            return null;
        }

        if (ap.getTimes().intValue() != 9999) {
            rec += ";COUNT=" + Repeat.calculateTimes(ap);
        }

        return rec;
    }
}
