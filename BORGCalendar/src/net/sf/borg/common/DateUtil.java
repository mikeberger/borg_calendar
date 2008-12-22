package net.sf.borg.common;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {

    public static boolean isAfter(Date d1, Date d2){
    
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
        //System.out.println( DateFormat.getDateTimeInstance().format(tcal.getTime()) + " " + 
    	    //DateFormat.getDateTimeInstance().format(dcal.getTime()) );
        if (tcal.getTime().after(dcal.getTime())) {
    	return true;
        }
        
        return false;
    }

}
