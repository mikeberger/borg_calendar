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
package net.sf.borg.common.util;

import java.util.prefs.Preferences;

class PrefsImpl implements IPrefs
{
	// IPrefs overrides
	public final String getPref( String name, String def )
	{
	    try
	    {
	        Preferences prefs = Preferences.userNodeForPackage(PrefsHome.class);
	        String val = prefs.get(name, def );
	        return(val);
	    }
	    catch( NoClassDefFoundError e )
	    {
	        // must be 1.3
	        return(def);
	    }
	}
	public final void putPref( String name, String val )
	{
	    try
	    {
	        Preferences prefs = Preferences.userNodeForPackage(PrefsHome.class);
	        prefs.put( name, val );
	    }
	    catch( NoClassDefFoundError e )
	    {
	        // must be 1.3
	        return;
	    }
	}
	public final int getPref( String name, int def )
	{
	    try
	    {
	        Preferences prefs = Preferences.userNodeForPackage(PrefsHome.class);
	        int val = prefs.getInt(name, def );
	        return(val);
	    }
	    catch( NoClassDefFoundError e )
	    {
	        // must be 1.3
	        return(def);
	    }
	}
	public final void putPref( String name, int val )
	{
	    try
	    {
	        Preferences prefs = Preferences.userNodeForPackage(PrefsHome.class);
	        prefs.putInt( name, val );
	    }
	    catch( NoClassDefFoundError e )
	    {
	        // must be 1.3
	        return;
	    }
	}
	
	public Object getPref(PrefName pn) {
        Preferences prefs = Preferences.userNodeForPackage(PrefsHome.class);
        if( pn.getDefault() instanceof Integer)
        {
            int val = prefs.getInt(pn.getName(),((Integer)pn.getDefault()).intValue());
            return( new Integer(val));
        }
        
        String val = prefs.get(pn.getName(), (String) pn.getDefault() );
		return(val);
	}
    /* (non-Javadoc)
     * @see net.sf.borg.common.util.IPrefs#putPref(net.sf.borg.common.util.PrefName, java.lang.Object)
     */
    public void putPref(PrefName pn, Object val) {
        
        Preferences prefs = Preferences.userNodeForPackage(PrefsHome.class);
        if( pn.getDefault() instanceof Integer)
        {
            prefs.putInt( pn.getName(), ((Integer) val).intValue());
        }
        else
        {
            prefs.put( pn.getName(), (String) val);
        }
    }
}
