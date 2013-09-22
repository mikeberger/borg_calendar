package net.sf.borg.model;

import java.util.StringTokenizer;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

/**
 * This class converts the appointment reminder times to and from a string in the preferences
 * to an array of minutes values
 */
public class ReminderTimes {

	/** The times array - each element is a reminder time in minutes */
	static private int times_[] = null;
	
	/** The number fo reminder times */
	static private int NUM = 20;
	
	/**
	 * Gets the number of reminder times
	 * 
	 * @return the number of reminder times
	 */
	static public int getNum()
	{
		return NUM;
	}
	
	/**
	 * Gets the value (in minutes) for reminder i
	 * 
	 * @param i the number fo the reminder to get
	 * 
	 * @return the time in minutes
	 */
	static public int getTimes(int i)
	{
		if( times_ == null )
		{
			// get times array from prefs if there are none loaded
			times_ = new int[NUM];
			String s = Prefs.getPref(PrefName.REMMINS);
			arrayFromString(s);
		}
		return times_[i];
	}
	
	/**
	 * store the reminder times
	 * 
	 * @param times  the reminder times array
	 */
	static public void setTimes(int times[])
	{
		times_ = times;
		String s = arrayToString(times_);
		Prefs.putPref(PrefName.REMMINS, s);
	}
	
	/**
	 * convert the reminder times array to a string for saving
	 * 
	 * @param a the array of times
	 * 
	 * @return the string
	 */
	static private String arrayToString( int a[])
	{
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < a.length; i++)
		{
			sb.append(Integer.toString(a[i]));
			if( sb.length() != 0)
				sb.append(',');
		}
		
		return sb.toString();
	}
	
	/**
	 * convert a string of times to an array of integers
	 * 
	 * @param s the s
	 */
	static private void arrayFromString( String s)
	{
		StringTokenizer t = new StringTokenizer(s,",");
		for( int i = 0; i < NUM; i++)
		{
			if( t.hasMoreTokens())
			{
				String tok = t.nextToken();
				times_[i] = Integer.parseInt(tok);
			}
			else
			{
				times_[i] = 0;
			}
		}
	}
}
