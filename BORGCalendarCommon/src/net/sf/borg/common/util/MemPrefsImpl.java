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
 
Copyright 2003 by ==Quiet==
 */

package net.sf.borg.common.util;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;

import net.sf.borg.common.io.IOHelper;

class MemPrefsImpl implements IPrefs, Serializable
{
    private HashMap prefsMap = new HashMap();
    
	public final String getPref( String name, String def )
	{
		String val = (String) prefsMap.get(name);
		if( val == null ) val = def;
		return(val);
	}

	public final void putPref( String name, String val )
	{
		prefsMap.put( name , val );
		saveModified();
	}

	public final int getPref( String name, int def )
	{
		Integer val = (Integer) prefsMap.get(name);
		if( val == null ) return(def);
		return(val.intValue());
	}

	public final void putPref( String name, int val )
	{
		prefsMap.put( name , new Integer(val) );
		saveModified();
	}
	
	// private //
	private static final String PREFS_FILE = "mem:/prefs.dat";
	
	private void saveModified()
	{
		try
		{
			OutputStream ostr = IOHelper.createOutputStream(PREFS_FILE);
			try
			{
				byte[] prefsData = Prefs.getMemento();
				if (prefsData != null)
					ostr.write(prefsData);
			}
			finally
			{
				ostr.close();
			}
		}
		catch (Exception e)
		{
			// It didn't work. Tough.
		}
	}
}
