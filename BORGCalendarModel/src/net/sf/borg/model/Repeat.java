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

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sf.borg.common.util.Resource;

/**
 * A helper class for calculating repeating appointments.
 */
public class Repeat
{
	private Calendar start;
	private Calendar cal;
	private Calendar current;
	private String frequency_;
	private int field;
	private int dayOfWeekMonth;
	private int dayOfWeek;
	private boolean monthly_same_day;
	private boolean weekends;
	private boolean weekdays;
	private boolean mwf;
	private boolean tth;
	private int count;

	private int incr;

	static public String freqs[] = { "once", "daily", "weekly", "biweekly",
			"monthly", "monthly_day", "yearly", "weekdays", "weekends", "mwf",
			"tth", "ndays" };
	
	static public String freqToEnglish(String fr) {
		for (int i = 0; i < freqs.length; i++) {
			if (fr.equals(Resource.getResourceString(freqs[i]))) {
				return (freqs[i]);
			}
		}
		return ("once");
	}
	
	// generate the frequency string stored in the appt record
	static public String freqString( String uistring, Integer ndays, boolean rptnum )
	{
		String f = freqToEnglish(uistring);
		if (f.equals("ndays")) {
			f += "," + ndays;
		}

		if( rptnum)
		{
			f += ",Y";
		}
		
		return(f);
		
	}
	
	static public String getFreq(String f)
	{
		if( f == null )
			return null;
		int i = f.indexOf(',');
		if( i == -1 )
			return(f);
		return( f.substring(0,i));
			
	}
	
	static public boolean getRptNum(String f)
	{
		if( f == null ) return false;
		if( f.endsWith(",Y"))
			return true;
		return false;
	}
	
	static public int getNDays( String f )
	{
		if( f == null )
			return 0;
		if( !f.startsWith("ndays,"))
			return(0);

		int i2 = f.indexOf(',', 6);
		if(  i2 != -1 )
			return( Integer.parseInt(f.substring(6,i2)));

		return( Integer.parseInt(f.substring(6)));
			
	}
	
	Repeat(Calendar start, String frequency)
	{
		this.start = start;
		this.frequency_ = frequency;
		cal = new GregorianCalendar(0,0,0);
		cal.setTime(start.getTime());
		current = cal;
		count = 0;
		incr = 1;
		weekdays = false;
		weekends = false;
		mwf = false;
		tth = false;
		monthly_same_day = false;
		field = Calendar.DATE;
		dayOfWeek = 0;
		dayOfWeekMonth = 0;

		if (!isRepeating()) return;
		
		String freq = getFreq(frequency);
		if( freq.equals("weekly"))
			incr = 7;
		else if( freq.equals("biweekly"))
			incr = 14;
		else if( freq.equals("monthly"))
			field = Calendar.MONTH;
		else if( freq.equals("monthly_day"))
		{
			monthly_same_day = true;
			incr = 0;
			dayOfWeek = start.get(Calendar.DAY_OF_WEEK);
			dayOfWeekMonth = start.get(Calendar.DAY_OF_WEEK_IN_MONTH);
		}
		else if( freq.equals("yearly"))
			field = Calendar.YEAR;
		else if( freq.equals("weekdays"))
		{
			weekdays = true;
		}
		else if( freq.equals("weekends"))
		{
			weekends = true;
		}
		else if( freq.equals("mwf"))
		{
			mwf = true;
			incr = 0;
		}		
		else if( freq.equals("tth"))
		{
			tth = true;
			incr = 0;
		}
		else if( freq.equals("ndays"))
		{
            incr = getNDays(frequency_);
		}
	}
	
	final boolean isRepeating()
	{
		String freq = getFreq(frequency_);
		return freq!=null && !freq.equals("once");
	}
	
	// our current date
	final Calendar current()
	{
		return current;
	}
	
	// return when the appt repeats until given the count
	final static public Calendar until(Calendar start, String frequency, int count)
	{
	    Calendar c = null;
	    
	    if( count == 9999)
	    {
	        // for unlimited rpt, go for 2 years in the future
	        c = new GregorianCalendar();
	        c.add(Calendar.YEAR,2);
	        return(c);
	    }
	    
	    Repeat r = new Repeat(start,frequency);
	    for( int i = 1; i < count; i++)
	    {
	        c = r.next();
	    }
	    
	    return(c);
	}
	
	// calculate the number of the repeat
	final static public int calculateRepeatNumber(Calendar current, Appointment appt)
	{	    
	    Calendar start = new GregorianCalendar();
	    Calendar c = start;
	    start.setTime(appt.getDate());
	    Repeat r = new Repeat(start,appt.getFrequency());
	    for( int i = 1; ; i++)
	    {
	    	if( (c.get(Calendar.YEAR) == current.get(Calendar.YEAR)) &&
	    		(c.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)) )
	    		return(i);
	    	if( c.after(current))
	    		return(0);
	        c = r.next();
	    }
	    
	}
	
	// calculate the next date of this repeat
	final Calendar next()
	{
		if (!isRepeating())
		{
			current = null;
			return current;
		}
		
		current = cal;
		++count;
		
		// add the required increment
		if (incr != 0)
			cal.add(field, incr);
                            	
		if( weekdays )
		{
			int dow = cal.get(Calendar.DAY_OF_WEEK );
			if( dow == Calendar.SATURDAY )
				cal.add( Calendar.DATE, 2 );
			else if( dow == Calendar.SUNDAY )
				cal.add( Calendar.DATE, 1 );
		}
		else if( weekends )
		{
			int dow = cal.get(Calendar.DAY_OF_WEEK );
			if( dow == Calendar.MONDAY )
				cal.add( Calendar.DATE, 5 );
			else if( dow == Calendar.TUESDAY )
				cal.add( Calendar.DATE, 4 );
			else if( dow == Calendar.WEDNESDAY )
				cal.add( Calendar.DATE, 3 );
			else if( dow == Calendar.THURSDAY )
				cal.add( Calendar.DATE, 2 );
			else if( dow == Calendar.FRIDAY )
				cal.add( Calendar.DATE, 1 );
		}
		else if( mwf )
		{
			int dow = cal.get(Calendar.DAY_OF_WEEK );
			if( dow == Calendar.FRIDAY )
			{
				cal.add( Calendar.DATE, 3 );
			}
			else 
			{
				cal.add( Calendar.DATE, 2 );
			}
		}
		else if( tth )
		{
			int dow = cal.get(Calendar.DAY_OF_WEEK );
			if( dow == Calendar.THURSDAY )
			{
				cal.add( Calendar.DATE, 5 );
			}
			else 
			{
				cal.add( Calendar.DATE, 2 );
			}
		}
		else if (monthly_same_day)
		{
			// Attempt to find a date falling on the
			// same day of week and week number
			// within a subsequent month.
			cal.setTime(start.getTime());
			cal.add(Calendar.MONTH, count);
			cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, dayOfWeekMonth);
			int dowm = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
			if (dowm != dayOfWeekMonth)
				current = null;
				// not enough days in this month
		}
		
		// bug fix - if repeating by month/date, must adjust if original date was 
		// 29, 30, or 31.
		int startDate = start.get(Calendar.DATE);
		int maxDate = cal.getActualMaximum(Calendar.DATE);
		if( field == Calendar.MONTH )
		{			
			if( startDate <= maxDate )
			{
				cal.set(Calendar.DATE, startDate);
			}
			else
			{
				cal.set(Calendar.DATE, maxDate );
			}
		}
		
		return current;
	}
}
