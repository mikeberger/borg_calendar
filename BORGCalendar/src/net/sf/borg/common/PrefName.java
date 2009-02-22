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
		static public PrefName DBTYPE = new PrefName( "dbtype", "hsqldb");
		static public PrefName DBHOST = new PrefName( "dbhost", "localhost");
		static public PrefName DBPORT = new PrefName( "dbport", "3306");
		static public PrefName DBNAME = new PrefName( "dbname", "borg");
		static public PrefName DBUSER = new PrefName( "dbuser", "borg");
		static public PrefName DBPASS = new PrefName( "dbpass", "borg");
		static public PrefName JDBCURL = new PrefName( "jdbcurl", "");
		static public PrefName SYNCMINS = new PrefName( "sync_mins", new Integer(0));
		static public PrefName HSQLDBDIR = new PrefName( "hsqldbdir", "not-set");
		
		// misc
		static public PrefName STACKTRACE = new PrefName( "stacktrace", "false");
		static public PrefName SPLASH = new PrefName( "splash", "true");
		static public PrefName LASTEXPURL = new PrefName( "lastExpUrl", "");
		static public PrefName LASTIMPURL = new PrefName( "lastImpUrl", "");
		static public PrefName SOCKETPORT = new PrefName("socketport", new Integer(2929));
		static public PrefName USESYSTRAY = new PrefName("useSysTray", "true");
		static public PrefName BACKUPDIR = new PrefName("backupDir", "");
		
		// printing
		static public PrefName COLORPRINT = new PrefName( "colorprint", "false");
		
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
		static public PrefName TRUNCAPPT = new PrefName("truncate_appt", "true");
		static public PrefName ISOWKNUMBER = new PrefName("isowknumber", "true");
		static public PrefName DOCKPANELS = new PrefName("dock_panels", "true");
		static public PrefName HIDESTRIKETHROUGH = new PrefName("hide_strike", "false");
		
		// reminders/popups
		static public PrefName REMINDERS = new PrefName( "reminders", "true");
		static public PrefName BEEPINGREMINDERS = new PrefName( "beeping_reminders", "true");
		static public PrefName USESYSTEMBEEP = new PrefName( "system_beep", "true");
		static public PrefName REMINDERCHECKMINS = new PrefName( "reminder_check_mins", new Integer(1));
		static public PrefName EMAILENABLED = new PrefName( "email_enabled", "false");
		static public PrefName EMAILSERVER = new PrefName( "email_server", "");
		static public PrefName EMAILADDR = new PrefName( "email_addr", "");
		static public PrefName EMAILLAST = new PrefName( "email_last", new Integer(0));
		static public PrefName EMAILDEBUG = new PrefName( "email_debug", "0");
		static public PrefName EMAILTIME = new PrefName( "email_time", new Integer(0));
		static public PrefName EMAILUSER = new PrefName( "email_user", "");
		static public PrefName EMAILPASS = new PrefName( "email_pass", "");
		static public PrefName EMAILPORT = new PrefName( "email_port", "25");
		static public PrefName REMMINS = new PrefName( "remmins", "-10,-5,0,1,2,3,4,5,10,15,20,30,45,60,90,120,180,240,300,360");
// font-LNF-locale
		static public PrefName DEFFONT = new PrefName( "defaultfont", "");
		static public PrefName APPTFONT = new PrefName( "apptfont", "SansSerif-10");		
		static public PrefName DAYVIEWFONT = new PrefName( "dayviewfont", "SansSerif-10");
		static public PrefName WEEKVIEWFONT = new PrefName( "weekviewfont", "SansSerif-10");
		static public PrefName MONTHVIEWFONT = new PrefName( "monthviewfont", "SansSerif-6");
		static public PrefName LNF = new PrefName( "lnf", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");		
		static public PrefName COUNTRY = new PrefName( "country", "");
		static public PrefName LANGUAGE = new PrefName( "language", "");
	
		static public PrefName ADDRVIEWSIZE = new PrefName("addrviewsize","-1,-1,-1,-1,N");
		static public PrefName ADDRLISTVIEWSIZE = new PrefName("addrlistviewsize","-1,-1,-1,-1,N");
		static public PrefName APPTLISTVIEWSIZE = new PrefName("apptlistviewsize","-1,-1,-1,-1,N");
		static public PrefName DAYVIEWSIZE = new PrefName("dayviewsize","-1,-1,-1,-1,Y");
		static public PrefName OPTVIEWSIZE = new PrefName("optviewsize","-1,-1,-1,-1,N");
		static public PrefName SRCHVIEWSIZE = new PrefName("srchviewsize","-1,-1,-1,-1,N");
		static public PrefName TASKCONFVIEWSIZE = new PrefName("taskconfviewsize","-1,-1,-1,-1,N");
		static public PrefName TASKVIEWSIZE = new PrefName("taskviewsize","-1,-1,-1,-1,N");
		static public PrefName TODOVIEWSIZE = new PrefName("todoviewsize","-1,-1,-1,-1,N");
		static public PrefName HELPVIEWSIZE = new PrefName("helpviewsize","-1,-1,-1,-1,N");
		static public PrefName PROJVIEWSIZE = new PrefName("projviewsize","-1,-1,-1,-1,N");
		static public PrefName GANTTSIZE = new PrefName("ganttsize","-1,-1,-1,-1,N");
		
		// user color scheme
		static public PrefName UCS_ONTODO = new PrefName("ucs_ontodo","false");
		static public PrefName UCS_MARKTODO = new PrefName("ucs_marktodo","true");
		static public PrefName UCS_MARKER = new PrefName("ucs_marker","redball.gif");
		// appts categories
		static public PrefName UCS_RED = new PrefName("ucs_red",new Integer(13369395));
		static public PrefName UCS_BLUE = new PrefName("ucs_blue",new Integer(6684876));
		static public PrefName UCS_GREEN = new PrefName("ucs_green",new Integer(39168));
		static public PrefName UCS_BLACK = new PrefName("ucs_black",new Integer(13107));
		static public PrefName UCS_WHITE = new PrefName("ucs_white",new Integer(16250609));
		// use if for task tracker items
		static public PrefName UCS_NAVY = new PrefName("ucs_navy",new Integer(13158));
		// use it for system generated holidays
		static public PrefName UCS_PURPLE = new PrefName("ucs_purple",new Integer(10027212));
		// use it for system generated birthdays
		static public PrefName UCS_BRICK = new PrefName("ucs_brick",new Integer(10027008));
		// Calendar view day background colors
		
		static public PrefName UCS_DEFAULT = new PrefName("ucs_default",new Integer(16777164));
		static public PrefName UCS_TODAY = new PrefName("ucs_today",new Integer(16751001));
		static public PrefName UCS_HOLIDAY = new PrefName("ucs_holiday",new Integer(16764108));
		static public PrefName UCS_VACATION = new PrefName("ucs_vacation",new Integer(13434828));
		static public PrefName UCS_HALFDAY = new PrefName("ucs_halfday",new Integer(13421823));
		static public PrefName UCS_WEEKEND = new PrefName("ucs_weekend",new Integer(16764057));
		static public PrefName UCS_WEEKDAY = new PrefName("ucs_weekday",new Integer(16777164));
		static public PrefName UCS_STRIPE = new PrefName("ucs_stripe",new Integer(15792890));
		
		
		// synchronization
		static public PrefName PALM_SYNC = new PrefName("palm_sync", "false");
		
		// tasks
		static public PrefName TASK_SHOW_ABBREV = new PrefName("task_show_abbrev", "false");
		static public PrefName CAL_SHOW_TASKS = new PrefName("cal_show_tasks", "true");
		static public PrefName CAL_SHOW_SUBTASKS = new PrefName("cal_show_subtasks", "true");
		static public PrefName GANTT_SHOW_SUBTASKS = new PrefName("gantt_show_subtasks", "true");
		
}
