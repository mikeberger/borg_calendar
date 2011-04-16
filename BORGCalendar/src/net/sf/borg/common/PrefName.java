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

/**
 * PrefName contains all of the Borg preference definitions and default values.
 * It enforces compile time checking of preference names
 */
public class PrefName {


	/** preference name */
	private String name_;

	/** default value */
	private Object default_;

	/**
	 * Instantiates a new pref name.
	 * 
	 * @param name
	 *            the name
	 * @param def
	 *            the default value
	 */
	public PrefName(String name, Object def) {
		setName(name);
		setDefault(def);
	}

	/**
	 * Sets the name.
	 * 
	 * @param name_
	 *            the new name
	 */
	void setName(String name_) {
		this.name_ = name_;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	String getName() {
		return name_;
	}

	/**
	 * Sets the default value
	 * 
	 * @param default_
	 *            the new default
	 */
	void setDefault(Object default_) {
		this.default_ = default_;
	}

	/**
	 * Gets the default value
	 * 
	 * @return the default
	 */
	public Object getDefault() {
		return default_;
	}

	// database related prefs
	/** database type. */
	static public PrefName DBTYPE = new PrefName("dbtype", "hsqldb");

	/** database host */
	static public PrefName DBHOST = new PrefName("dbhost", "localhost");

	/** databse port */
	static public PrefName DBPORT = new PrefName("dbport", "3306");

	/** database name */
	static public PrefName DBNAME = new PrefName("dbname", "borg");

	/** database user */
	static public PrefName DBUSER = new PrefName("dbuser", "borg");

	/** database password */
	static public PrefName DBPASS = new PrefName("dbpass", "borg");

	/** jdbc url - for generic jdbc only, not hsql or mysql */
	static public PrefName JDBCURL = new PrefName("jdbcurl", "");

	/** interval for db auto-sync in minutes */
	static public PrefName SYNCMINS = new PrefName("sync_mins", new Integer(0));

	/** hsql database directory */
	static public PrefName HSQLDBDIR = new PrefName("hsqldbdir", "not-set");
	
	/** h2 database directory */
	static public PrefName H2DIR = new PrefName("h2dir", "not-set");

	// misc
	/** show a stack trace button on error dialogs */
	static public PrefName STACKTRACE = new PrefName("stacktrace", "false");

	/** show the spash window */
	static public PrefName SPLASH = new PrefName("splash", "true");

	/** port for the socket listener */
	static public PrefName SOCKETPORT = new PrefName("socketport", new Integer(
			2929));
	
	/** start as iconified to system tray */
	static public PrefName BACKGSTART = new PrefName( "backgstart", "false");

	/** use system tray */
	static public PrefName USESYSTRAY = new PrefName("useSysTray", "true");
	
	/** show date is system tray */
	static public PrefName SYSTRAYDATE = new PrefName("sysTrayDate", "true");

	/** backup directory for auto backup */
	static public PrefName BACKUPDIR = new PrefName("backupDir", "");

	// printing
	/** print in color */
	static public PrefName COLORPRINT = new PrefName("colorprint", "false");

	// what to show
	/** show public appontments */
	static public PrefName SHOWPUBLIC = new PrefName("showpublic", "true");

	/** show private appointments */
	static public PrefName SHOWPRIVATE = new PrefName("showprivate", "false");

	/** show us holidays */
	static public PrefName SHOWUSHOLIDAYS = new PrefName("show_us_holidays",
			"true");

	/** show canadian holidays */
	static public PrefName SHOWCANHOLIDAYS = new PrefName("show_can_holidays",
			"false");

	/** sort appointments by priority for a day */
	static public PrefName PRIORITY_SORT = new PrefName("priority_sort", "false");

	/** the first day of the week */
	static public PrefName FIRSTDOW = new PrefName("first_dow", new Integer(
			Calendar.SUNDAY));

	/** show military time */
	static public PrefName MILTIME = new PrefName("miltime", "false");

	/** earliest hour in week grid */
	static public PrefName WKSTARTHOUR = new PrefName("wkStartHour", "7");

	/** latest hour in week grid */
	static public PrefName WKENDHOUR = new PrefName("wkEndHour", "22");

	/** contents of the default appointment in XML */
	static public PrefName DEFAULT_APPT = new PrefName("defaultAppt", "");

	/** show the day of the year */
	static public PrefName DAYOFYEAR = new PrefName("showDayOfYear", "false");

	/** truncate appointment text after 1 line */
	static public PrefName TRUNCAPPT = new PrefName("truncate_appt", "true");

	/** use iso week numbering */
	static public PrefName ISOWKNUMBER = new PrefName("isowknumber", "true");

	/** do not show strikethrough appointments */
	static public PrefName HIDESTRIKETHROUGH = new PrefName("hide_strike",
			"false");

	/** show the entire undo stack (debugging) */
	static public PrefName SHOW_UNDO_STACK = new PrefName("show_undo_stack",
			"false");

	// reminders/popups
	/** The REMINDERS. */
	static public PrefName REMINDERS = new PrefName("reminders", "true");

	/** The BEEPINGREMINDERS */
	static public PrefName BEEPINGREMINDERS = new PrefName("beeping_reminders",
			"true");

	/** how often tp pop up reminders for untimed todos*/
	static public PrefName TODOREMINDERMINS = new PrefName(
			"todo_reminder_mins", new Integer(30));
	
	/** option to consolidate all reminders in a single list window */
	static public PrefName REMINDERLIST = new PrefName("reminder_list", "true");

	/** The EMAILENABLED. */
	static public PrefName EMAILENABLED = new PrefName("email_enabled", "false");

	/** The EMAILSERVER. */
	static public PrefName EMAILSERVER = new PrefName("email_server", "");

	/** The EMAILADDR. */
	static public PrefName EMAILADDR = new PrefName("email_addr", "");

	/** The EMAILLAST. */
	static public PrefName EMAILLAST = new PrefName("email_last",
			new Integer(0));

	/** The EMAILDEBUG. */
	static public PrefName EMAILDEBUG = new PrefName("email_debug", "0");

	/** The EMAILTIME. */
	static public PrefName EMAILTIME = new PrefName("email_time",
			new Integer(0));

	/** The EMAILUSER. */
	static public PrefName EMAILUSER = new PrefName("email_user", "");

	/** The EMAILPASS. */
	static public PrefName EMAILPASS = new PrefName("email_pass", "");
	static public PrefName EMAILPASS2 = new PrefName("email_pass2", "");

	/** The EMAILPORT. */
	static public PrefName EMAILPORT = new PrefName("email_port", "25");
	
	/** TLS flag */
	static public PrefName ENABLETLS = new PrefName("enable_tls", "false");

	/** The REMMINS. */
	static public PrefName REMMINS = new PrefName("remmins",
			"-10,-5,0,1,2,3,4,5,10,15,20,30,45,60,90,120,180,240,300,360");
	// font-LNF-locale
	/** The DEFFONT. */
	static public PrefName DEFFONT = new PrefName("defaultfont", "");

	/** The APPTFONT. */
	static public PrefName APPTFONT = new PrefName("apptfont", "SansSerif-10");

	/** The DAYVIEWFONT. */
	static public PrefName DAYVIEWFONT = new PrefName("dayviewfont",
			"SansSerif-10");

	/** The WEEKVIEWFONT. */
	static public PrefName WEEKVIEWFONT = new PrefName("weekviewfont",
			"SansSerif-10");

	/** The PRINTFONT. */
	static public PrefName PRINTFONT = new PrefName("monthviewfont",
			"SansSerif-6");

	/** The LNF. */
	static public PrefName LNF = new PrefName("lnf",
			"com.jgoodies.looks.plastic.PlasticXPLookAndFeel");

	/** The COUNTRY. */
	static public PrefName COUNTRY = new PrefName("country", "");

	/** The LANGUAGE. */
	static public PrefName LANGUAGE = new PrefName("language", "");

	// user color scheme
	/** use user colors on todo view */
	static public PrefName UCS_ONTODO = new PrefName("ucs_ontodo", "false");

	/** mark todos on the calendar */
	static public PrefName UCS_MARKTODO = new PrefName("ucs_marktodo", "true");

	/** characters or image to mark todos with */
	static public PrefName UCS_MARKER = new PrefName("ucs_marker",
			"redball.gif");
	
	/** default colors - they are tunable and do not have to match the color in the name */
	static public PrefName UCS_RED = new PrefName("ucs_red", new Integer(
			13369395));
	static public PrefName UCS_BLUE = new PrefName("ucs_blue", new Integer(
			6684876));
	static public PrefName UCS_GREEN = new PrefName("ucs_green", new Integer(
			39168));
	static public PrefName UCS_BLACK = new PrefName("ucs_black", new Integer(
			13107));
	static public PrefName UCS_WHITE = new PrefName("ucs_white", new Integer(
			16250609));
	static public PrefName UCS_NAVY = new PrefName("ucs_navy", new Integer(
			13158));
	static public PrefName UCS_PURPLE = new PrefName("ucs_purple", new Integer(
			10027212));
	static public PrefName UCS_BRICK = new PrefName("ucs_brick", new Integer(
			10027008));
	static public PrefName UCS_DEFAULT = new PrefName("ucs_default",
			new Integer(240*256*256+240*256+240));

	/** color for current day */
	static public PrefName UCS_TODAY = new PrefName("ucs_today", new Integer(
			255*256*256+200*256+200));

	/** color for holidays */
	static public PrefName UCS_HOLIDAY = new PrefName("ucs_holiday",
			new Integer(255*256*256+225*256+196));

	/** color for vacation days */
	static public PrefName UCS_VACATION = new PrefName("ucs_vacation",
			new Integer(13434828));

	/** color for half days */
	static public PrefName UCS_HALFDAY = new PrefName("ucs_halfday",
			new Integer(13421823));

	/** color for weekends */
	static public PrefName UCS_WEEKEND = new PrefName("ucs_weekend",
			new Integer(255*256*256+225*256+196));

	/** color for weekdays */
	static public PrefName UCS_WEEKDAY = new PrefName("ucs_weekday",
			new Integer(16777164));

	/** color for alternating table stripes */
	static public PrefName UCS_STRIPE = new PrefName("ucs_stripe", new Integer(
			15792890));
	
	/** draw gradient color in appointment boxes - can slow down older machines */
	static public PrefName GRADIENT_APPTS = new PrefName("gradient_appts", "true");

	// tasks
	/** when showing tasks project and subtasks, prepend a prefix and id number  */
	static public PrefName TASK_SHOW_ABBREV = new PrefName("task_show_abbrev",
			"false");

	/** show tasks on calendar */
	static public PrefName CAL_SHOW_TASKS = new PrefName("cal_show_tasks",
			"true");

	/** show subtasks on calendar */
	static public PrefName CAL_SHOW_SUBTASKS = new PrefName(
			"cal_show_subtasks", "true");
	
	/** show task number and status in task tree */
	static public PrefName TASK_TREE_SHOW_STATUS = new PrefName(
			"task_tree_show_status", "false");


	/** keystore location */
	static public PrefName KEYSTORE = new PrefName("key_store", "");
	
	/** encryption key alias in the keystore */
	static public PrefName KEYALIAS = new PrefName("key_alias", "borg_key");
	
	/** cached password time to live in seconds */
	static public PrefName PASSWORD_TTL = new PrefName("pw_ttl", new Integer(300));

 	/** todo quick add, clear text after add */
 	static public PrefName TODO_QUICK_ENTRY_AUTO_CLEAR_TEXT_FIELD = new PrefName("todo_option_auto_clear_text", "false");
 	
 	/** todo quick add, default date to today */
 	static public PrefName TODO_QUICK_ENTRY_AUTO_SET_DATE_FIELD = new PrefName("todo_option_auto_date_today", "false");

 	/** allow dynamically loaded plugins */
 	static public PrefName DYNAMIC_LOADING = new PrefName("dynamic_loading", "false");
 	
 	/** shutdown action */
 	static public PrefName SHUTDOWN_ACTION = new PrefName("shutdown_action", "");
}
