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
		
		// database
		static public PrefName DBTYPE = new PrefName( "dbtype", "local");
		static public PrefName DBDIR = new PrefName( "dbdir", "not-set");
		static public PrefName DBHOST = new PrefName( "dbhost", "localhost");
		static public PrefName DBPORT = new PrefName( "dbport", "3306");
		static public PrefName DBNAME = new PrefName( "dbname", "borg");
		static public PrefName DBUSER = new PrefName( "dbuser", "borg");
		static public PrefName DBPASS = new PrefName( "dbpass", "borg");
		
		
		// misc
		static public PrefName STACKTRACE = new PrefName( "stacktrace", "false");
		static public PrefName BACKGSTART = new PrefName( "backgstart", "false");
		static public PrefName SPLASH = new PrefName( "splash", "true");
		static public PrefName VERCHKLAST = new PrefName( "ver_chk_last", new Integer(-1));
		static public PrefName SHARED = new PrefName( "shared", "false");
		static public PrefName ICALTODOEV = new PrefName( "ical_todo_ev", "false");
		static public PrefName ICALUTC = new PrefName( "icalutc", "true");
		static public PrefName LASTEXPURL = new PrefName( "lastExpUrl", "");
		static public PrefName LASTIMPURL = new PrefName( "lastImpUrl", "");
		static public PrefName LASTIMPURLDAT = new PrefName( "lastImpUrlDat", "");
		static public PrefName SHOWMEMFILES = new PrefName("showMemFiles", "false");
		
		// have to explain this one - reverse the way that the appt editor and day view
		// windows are summoned for people who can't adjust
		static public PrefName REVERSEDAYEDIT = new PrefName("reverseDayEdit", "false");
					
		// printing
		static public PrefName LOGO = new PrefName( "logo", "");
		static public PrefName COLORPRINT = new PrefName( "colorprint", "false");
		static public PrefName WRAP = new PrefName( "wrap", "false");
		
		// what to show
		static public PrefName SHOWPUBLIC = new PrefName( "showpublic", "true");
		static public PrefName SHOWPRIVATE = new PrefName( "showprivate", "false");
		static public PrefName SHOWUSHOLIDAYS = new PrefName( "show_us_holidays", "true");
		static public PrefName SHOWCANHOLIDAYS = new PrefName( "show_can_holidays", "false");
		static public PrefName COLORSORT = new PrefName( "color_sort", "false");
		static public PrefName FIRSTDOW = new PrefName( "first_dow", new Integer(Calendar.SUNDAY));
		static public PrefName MILTIME = new PrefName( "miltime", "false");
		static public PrefName WKSTARTHOUR = new PrefName( "wkStartHour", "7");
		static public PrefName WKENDHOUR = new PrefName( "wkEndHour", "22");
		static public PrefName DEFAULT_APPT = new PrefName("defaultAppt", "" );
		static public PrefName DAYOFYEAR = new PrefName("showDayOfYear", "false");
		
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
		static public PrefName DEFFONT = new PrefName( "defaultfont", "");
		static public PrefName APPTFONT = new PrefName( "apptfont", "SansSerif-10");
		static public PrefName APPTFONTSIZE = new PrefName( "apptfontsize", new Integer(10));
		static public PrefName PREVIEWFONT = new PrefName( "previewfont", "SansSerif-10");
		static public PrefName LNF = new PrefName( "lnf", "javax.swing.plaf.metal.MetalLookAndFeel");
		static public PrefName NOLOCALE = new PrefName( "nolocale", "0");		
		static public PrefName COUNTRY = new PrefName( "country", "");
		static public PrefName LANGUAGE = new PrefName( "language", "");
	
		// window sizes
		static public PrefName CALVIEWSIZE = new PrefName("calviewsize","-1,-1,-1,-1,N");
		static public PrefName ADDRVIEWSIZE = new PrefName("addrviewsize","-1,-1,-1,-1,N");
		static public PrefName ADDRLISTVIEWSIZE = new PrefName("addrlistviewsize","-1,-1,-1,-1,N");
		static public PrefName APPTLISTVIEWSIZE = new PrefName("apptlistviewsize","-1,-1,-1,-1,N");
		static public PrefName DAYVIEWSIZE = new PrefName("dayviewsize","-1,-1,-1,-1,N");
		static public PrefName MONTHPREVIEWSIZE = new PrefName("monthpreviewsize","-1,-1,-1,-1,N");
		static public PrefName OPTVIEWSIZE = new PrefName("optviewsize","-1,-1,-1,-1,N");
		static public PrefName SRCHVIEWSIZE = new PrefName("srchviewsize","-1,-1,-1,-1,N");
		static public PrefName TASKCONFVIEWSIZE = new PrefName("taskconfviewsize","-1,-1,-1,-1,N");
		static public PrefName TASKLISTVIEWSIZE = new PrefName("tasklistviewsize","-1,-1,-1,-1,N");
		static public PrefName TASKVIEWSIZE = new PrefName("taskviewsize","-1,-1,-1,-1,N");
		static public PrefName TODOVIEWSIZE = new PrefName("todoviewsize","-1,-1,-1,-1,N");
		static public PrefName WEEKVIEWSIZE = new PrefName("weekviewsize","-1,-1,-1,-1,N");
		static public PrefName HELPVIEWSIZE = new PrefName("helpviewsize","-1,-1,-1,-1,N");
		
		// bsv 2004-12-20
		// user color scheme
		static public PrefName UCS_ON = new PrefName("ucs_on","false");
		static public PrefName UCS_ONTODO = new PrefName("ucs_ontodo","false");
		static public PrefName UCS_MARKTODO = new PrefName("ucs_marktodo","true");
		static public PrefName UCS_MARKER = new PrefName("ucs_marker",">>");
		static public PrefName UCS_MARKERCOLOR = new PrefName("ucs_markercolor","16250609");
		// appts categories
		static public PrefName UCS_RED = new PrefName("ucs_red","13369395");
		static public PrefName UCS_BLUE = new PrefName("ucs_blue","6684876");
		static public PrefName UCS_GREEN = new PrefName("ucs_green","39168");
		static public PrefName UCS_BLACK = new PrefName("ucs_black","13107");
		static public PrefName UCS_WHITE = new PrefName("ucs_white","16250609");
		// use if for task tracker items
		static public PrefName UCS_NAVY = new PrefName("ucs_navy","13158");
		// use it for system generated holidays
		static public PrefName UCS_PURPLE = new PrefName("ucs_purple","10027212");
		// use it for system generated birthdays
		static public PrefName UCS_BRICK = new PrefName("ucs_brick","10027008");
		// Calendar view day background colors
		// TODO choose correct colors
		static public PrefName UCS_DEFAULT = new PrefName("ucs_default","11316396");
		// original color is Color(225,150,150)
		static public PrefName UCS_TODAY = new PrefName("ucs_today","16751001");
		// original color is Color(245,203,162)
		static public PrefName UCS_HOLIDAY = new PrefName("ucs_holiday","16764108");
		// original color is Color(155,255,153)
		static public PrefName UCS_VACATION = new PrefName("ucs_vacation","13434828");
		// original color is Color(200,255,200)
		static public PrefName UCS_HALFDAY = new PrefName("ucs_halfday","13421823");
		// original color is Color(245,203,162)
		static public PrefName UCS_WEEKEND = new PrefName("ucs_weekend","16764057");
		// original color is Color(255,233,192)
		static public PrefName UCS_WEEKDAY = new PrefName("ucs_weekday","13421772");
		// (bsv 2004-12-20)
		
		// synchronization
		static public PrefName SYNC_ADDR = new PrefName("sync_addr", "false");
		static public PrefName SYNC_APPT = new PrefName("sync_appt", "false");
}
