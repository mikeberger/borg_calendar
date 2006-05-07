package net.sf.borg.model;

import java.util.StringTokenizer;

import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;

public class ReminderTimes {

	static private int times_[] = null;
	static private int NUM = 20;
	
	static public int getNum()
	{
		return NUM;
	}
	
	static public int getTimes(int i)
	{
		if( times_ == null )
		{
			// get times array from prefs
			times_ = new int[NUM];
			String s = Prefs.getPref(PrefName.REMMINS);
			arrayFromString(s);
		}
		return times_[i];
	}
	
	static public void setTimes(int times[])
	{
		times_ = times;
		String s = arrayToString(times_);
		Prefs.putPref(PrefName.REMMINS, s);
	}
	
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
