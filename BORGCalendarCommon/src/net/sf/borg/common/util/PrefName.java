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

import java.util.Calendar;


public class PrefName {

		private String name_;
		private Object default_;
		
		private PrefName( String name, Object def )
		{
			setName(name);
			setDefault(def);
		}
		
		void setName(String name_) {
			this.name_ = name_;
		}

		String getName() {
			return name_;
		}

		void setDefault(Object default_) {
			this.default_ = default_;
		}

		Object getDefault() {
			return default_;
		}

		// misc
		static public PrefName STACKTRACE = new PrefName( "stacktrace", "false");
		static public PrefName DBDIR = new PrefName( "dbdir", "not-set");
		static public PrefName BACKGSTART = new PrefName( "backgstart", "false");
		static public PrefName SPLASH = new PrefName( "splash", "true");
		static public PrefName VERCHKLAST = new PrefName( "ver_chk_last", new Integer(-1));
		static public PrefName SHARED = new PrefName( "shared", "false");
		static public PrefName ICALTODOEV = new PrefName( "ical_todo_ev", "false");
		static public PrefName LASTEXPURL = new PrefName( "lastExpUrl", "");
		static public PrefName LASTIMPURL = new PrefName( "lastImpUrl", "");
		static public PrefName LASTIMPURLDAT = new PrefName( "lastImpUrlDat", "");
		static public PrefName SHOWMEMFILES = new PrefName("showMemFiles", "false");
					
		// printing
		static public PrefName LOGO = new PrefName( "logo", "");
		static public PrefName COLORPRINT = new PrefName( "colorprint", "false");
		static public PrefName WRAP = new PrefName( "wrap", "false");
		
		// what to show
		static public PrefName SHOWPUBLIC = new PrefName( "showpublic", "true");
		static public PrefName SHOWPRIVATE = new PrefName( "shpwprivate", "false");
		static public PrefName SHOWUSHOLIDAYS = new PrefName( "show_us_holidays", "true");
		static public PrefName SHOWCANHOLIDAYS = new PrefName( "show_can_holidays", "false");
		static public PrefName COLORSORT = new PrefName( "color_sort", "false");
		static public PrefName FIRSTDOW = new PrefName( "first_dow", new Integer(Calendar.SUNDAY));
		static public PrefName MILTIME = new PrefName( "miltime", "false");
		static public PrefName WKSTARTHOUR = new PrefName( "wkStartHour", "7");
		static public PrefName WKENDHOUR = new PrefName( "wkEndHour", "22");
		
		// reminders/popups
		static public PrefName REMINDERS = new PrefName( "reminders", "true");
		static public PrefName BEEPINGREMINDERS = new PrefName( "beeping_reminders", "true");
		static public PrefName REMINDERCHECKMINS = new PrefName( "reminder_check_mins", new Integer(5));
		static public PrefName POPBEFOREMINS = new PrefName( "pop_before_mins", new Integer(180));
		static public PrefName POPAFTERMINS = new PrefName( "pop_after_mins", new Integer(30));
		static public PrefName BEEPINGMINS = new PrefName( "beeping_mins", new Integer(15));
		static public PrefName EMAILENABLED = new PrefName( "email_enabled", "false");
		static public PrefName EMAILSERVER = new PrefName( "email_server", "");
		static public PrefName EMAILADDR = new PrefName( "email_addr", "");
		static public PrefName EMAILLAST = new PrefName( "email_last", new Integer(0));
		static public PrefName EMAILDEBUG = new PrefName( "email_debug", "0");
		
		// font-LNF-locale
		static public PrefName DEFFONT = new PrefName( "default_font", "");
		static public PrefName APPTFONT = new PrefName( "apptfont", "SansSerif-10");
		static public PrefName APPTFONTSIZE = new PrefName( "apptfontsize", new Integer(10));
		static public PrefName PREVIEWFONT = new PrefName( "previewfont", "SansSerif-10");
		static public PrefName LNF = new PrefName( "lnf", "javax.swing.plaf.metal.MetalLookAndFeel");
		static public PrefName NOLOCALE = new PrefName( "nolocale", "0");		
		static public PrefName COUNTRY = new PrefName( "country", "");
		static public PrefName LANGUAGE = new PrefName( "language", "");
	
		
}
