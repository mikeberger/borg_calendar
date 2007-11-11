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
package net.sf.borg.common;

import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * 
 * Convenience class for retrieving preferences.
 * 
 */
public class Prefs {

    public interface Listener {
	public abstract void prefsChanged();
    }

    static private ArrayList listeners = new ArrayList();

    static public void addListener(Listener listener) {
	listeners.add(listener);
    }

    static public void removeListener(Listener listener) {
	listeners.remove(listener);
    }

    // send a notification to all registered views
    static public void notifyListeners() {
	for (int i = 0; i < listeners.size(); i++) {
	    Listener v = (Listener) listeners.get(i);
	    v.prefsChanged();
	}
    }

    public static String getPref( PrefName pn )
    {
	Object o = getPrefObject(pn);
	if( o instanceof Integer )
	    return Integer.toString(((Integer) o).intValue());
	return (String) getPrefObject(pn);
    }
    
    public static int getIntPref(PrefName pn) {
	return (((Integer) Prefs.getPrefObject(pn)).intValue());
    }

    public static boolean getBoolPref(PrefName pn)
    {
	String s = getPref(pn);
	if( s != null && s.equals("true"))
	    return true;
	return false;
    }

    public static final String getPref(String name, String def) {
	try {
	    Preferences prefs = Preferences.userNodeForPackage(Prefs.class);
	    String val = prefs.get(name, def);
	    return (val);
	} catch (NoClassDefFoundError e) {
	    // must be 1.3
	    return (def);
	}
    }

    public static final void putPref(String name, String val) {
	try {
	    Preferences prefs = Preferences.userNodeForPackage(Prefs.class);
	    prefs.put(name, val);
	} catch (NoClassDefFoundError e) {
	    // must be 1.3
	    return;
	}
    }

    public static final int getPref(String name, int def) {
	try {
	    Preferences prefs = Preferences.userNodeForPackage(Prefs.class);
	    int val = prefs.getInt(name, def);
	    return (val);
	} catch (NoClassDefFoundError e) {
	    // must be 1.3
	    return (def);
	}
    }

    public static final void putPref(String name, int val) {
	try {
	    Preferences prefs = Preferences.userNodeForPackage(Prefs.class);
	    prefs.putInt(name, val);
	} catch (NoClassDefFoundError e) {
	    // must be 1.3
	    return;
	}
    }

    private static Object getPrefObject(PrefName pn) {
	Preferences prefs = Preferences.userNodeForPackage(Prefs.class);
	if (pn.getDefault() instanceof Integer) {
	    int val = prefs.getInt(pn.getName(), ((Integer) pn.getDefault())
		    .intValue());
	    return (new Integer(val));
	}

	String val = prefs.get(pn.getName(), (String) pn.getDefault());
	return (val);
    }

    /*
         * (non-Javadoc)
         * 
         * @see net.sf.borg.common.util.IPrefs#putPref(net.sf.borg.common.util.PrefName,
         *      java.lang.Object)
         */
    public static void putPref(PrefName pn, Object val) {

	Preferences prefs = Preferences.userNodeForPackage(Prefs.class);
	if (pn.getDefault() instanceof Integer) {
	    if( val instanceof Integer)
	    {
		prefs.putInt(pn.getName(), ((Integer) val).intValue());
	    }
	    else
	    {
		prefs.putInt(pn.getName(), Integer.parseInt((String)val));
	    }
	} else {
	    prefs.put(pn.getName(), (String) val);
	}
    }

    private Prefs() {

    }
}
