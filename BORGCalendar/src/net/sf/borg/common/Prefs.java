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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

	static private ArrayList<Listener> listeners = new ArrayList<Listener>();

	static public void addListener(Listener listener) {
		listeners.add(listener);
	}

	// send a notification to all registered views
	static public void notifyListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			Listener v = listeners.get(i);
			v.prefsChanged();
		}
	}

	public static String getPref(PrefName pn) {
		Object o = getPrefObject(pn);
		if (o instanceof Integer)
			return Integer.toString(((Integer) o).intValue());
		return (String) getPrefObject(pn);
	}

	public static int getIntPref(PrefName pn) {
		return (((Integer) Prefs.getPrefObject(pn)).intValue());
	}

	public static boolean getBoolPref(PrefName pn) {
		String s = getPref(pn);
		if (s != null && s.equals("true"))
			return true;
		return false;
	}

	private static Object getPrefObject(PrefName pn) {
		Preferences prefs = getPrefNode();
		if (pn.getDefault() instanceof Integer) {
			int val = prefs.getInt(pn.getName(), ((Integer) pn.getDefault()).intValue());
			return (new Integer(val));
		}

		String val = prefs.get(pn.getName(), (String) pn.getDefault());
		return (val);
	}

	public static void putPref(PrefName pn, Object val) {

		// System.out.println("putpref-" + pn.getName() + "-" + val);
		Preferences prefs = getPrefNode();
		if (pn.getDefault() instanceof Integer) {
			if (val instanceof Integer) {
				prefs.putInt(pn.getName(), ((Integer) val).intValue());
			} else {
				prefs.putInt(pn.getName(), Integer.parseInt((String) val));
			}
		} else {
			prefs.put(pn.getName(), (String) val);
		}
	}

	static private Preferences getPrefNode() {
		// hard code to original prefs location for backward compatiblity
		Preferences root = Preferences.userRoot();
		return root.node("net/sf/borg/common/util");
	}

	private Prefs() {

	}

	public static void importPrefs(String filename) {
		try {
			InputStream istr = new FileInputStream(filename);
			Preferences.importPreferences(istr);
			istr.close();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	public static void export(String filename) {
		try {
			OutputStream oostr = IOHelper.createOutputStream(filename);
			Preferences prefs = getPrefNode();
			prefs.exportNode(oostr);
			oostr.close();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}
}
