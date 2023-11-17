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
	public String getName() {
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
	static public PrefName DBTYPE = new PrefName("dbtype", "h2");

	// legacy Mysql related prefs - deprecated
	static public PrefName DBHOST = new PrefName("dbhost", "localhost");
	static public PrefName DBPORT = new PrefName("dbport", "3306");
	static public PrefName DBNAME = new PrefName("dbname", "borg");
	static public PrefName DBUSER = new PrefName("dbuser", "borg");
	static public PrefName DBPASS = new PrefName("dbpass", "borg");

	/** jdbc url - for generic jdbc only, not hsql or mysql */
	static public PrefName JDBCURL = new PrefName("jdbcurl", "");

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
	static public PrefName SOCKETPORT = new PrefName("socketport", Integer.valueOf(2929));

	/** start as iconified to system tray */
	static public PrefName BACKGSTART = new PrefName("backgstart", "false");

	/** use system tray */
	static public PrefName USESYSTRAY = new PrefName("useSysTray", "true");

	/** show date is system tray */
	static public PrefName SYSTRAYDATE = new PrefName("sysTrayDate", "true");

	/** backup directory for auto backup */
	static public PrefName BACKUPDIR = new PrefName("backupDir", "");

	/** print in color */
	static public PrefName COLORPRINT = new PrefName("colorprint", "false");

	/** show public appointments */
	static public PrefName SHOWPUBLIC = new PrefName("showpublic", "true");

	/** show private appointments */
	static public PrefName SHOWPRIVATE = new PrefName("showprivate", "false");

	/** show us holidays */
	static public PrefName SHOWUSHOLIDAYS = new PrefName("show_us_holidays", "true");

	/** show canadian holidays */
	static public PrefName SHOWCANHOLIDAYS = new PrefName("show_can_holidays", "false");

	/** sort appointments by priority for a day */
	static public PrefName PRIORITY_SORT = new PrefName("priority_sort", "false");

	/** the first day of the week */
	static public PrefName FIRSTDOW = new PrefName("first_dow", Integer.valueOf(Calendar.SUNDAY));

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
	static public PrefName HIDESTRIKETHROUGH = new PrefName("hide_strike", "false");

	/** show the entire undo stack (debugging) */
	static public PrefName SHOW_UNDO_STACK = new PrefName("show_undo_stack", "false");

	static public PrefName REMINDERS = new PrefName("reminders", "true");

	static public PrefName BEEPINGREMINDERS = new PrefName("beeping_reminders", "true");

	/** how often tp pop up reminders for untimed todos */
	static public PrefName TODOREMINDERMINS = new PrefName("todo_reminder_mins", Integer.valueOf(30));

	/** option to consolidate all reminders in a single list window */
	static public PrefName REMINDERLIST = new PrefName("reminder_list", "true");

	/** show reminders for tasks */
	static public PrefName TASKREMINDERS = new PrefName("task_reminders", "true");

	/** days before a birthday to show birthday reminders */
	static public PrefName BIRTHDAYREMINDERDAYS = new PrefName("bd_reminder_days", Integer.valueOf(7));
	
	static public PrefName TASKBAR_REMINDERS = new PrefName("taskbar_reminders", "false");

	static public PrefName EMAILENABLED = new PrefName("email_enabled", "false");
	public static PrefName DAILYEMAILENABLED = new PrefName("daily_email_enabled", "true");;
	static public PrefName EMAILSERVER = new PrefName("email_server", "");
	static public PrefName EMAILADDR = new PrefName("email_addr", "");
	static public PrefName EMAILFROM = new PrefName("email_from", "");
	static public PrefName EMAILLAST = new PrefName("email_last", Integer.valueOf(0));
	static public PrefName EMAILDEBUG = new PrefName("email_debug", "0");
	static public PrefName EMAILTIME = new PrefName("email_time", Integer.valueOf(0));
	static public PrefName EMAILUSER = new PrefName("email_user", "");
	static public PrefName EMAILPASS = new PrefName("email_pass", "");
	static public PrefName EMAILPASS2 = new PrefName("email_pass2", "");
	static public PrefName EMAILPORT = new PrefName("email_port", "25");
	static public PrefName ENABLETLS = new PrefName("enable_tls", "false");

	static public PrefName REMMINS = new PrefName("remmins",
			"-10,-5,0,1,2,3,4,5,10,15,20,30,45,60,90,120,180,240,300,360");

	static public PrefName DEFFONT = new PrefName("defaultfont", "");
	static public PrefName APPTFONT = new PrefName("apptfont", "SansSerif-10");
	static public PrefName DAYVIEWFONT = new PrefName("dayviewfont", "SansSerif-10");
	static public PrefName WEEKVIEWFONT = new PrefName("weekviewfont", "SansSerif-10");
	static public PrefName PRINTFONT = new PrefName("monthviewfont", "SansSerif-6");
	static public PrefName YEARVIEWFONT = new PrefName("yearviewfont", "SansSerif-7");
	static public PrefName TRAYFONT = new PrefName("trayfont", "SansSerif-BOLD-12");

	static public PrefName LNF = new PrefName("lnf", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
	static public PrefName GOODIESTHEME = new PrefName("goodies_theme", "ExperienceBlue");

	static public PrefName COUNTRY = new PrefName("country", "");
	static public PrefName LANGUAGE = new PrefName("language", "");

	/** use user colors on todo view */
	static public PrefName UCS_ONTODO = new PrefName("ucs_ontodo", "false");

	/** mark todos on the calendar */
	static public PrefName UCS_MARKTODO = new PrefName("ucs_marktodo", "true");

	/** characters or image to mark todos with */
	static public PrefName UCS_MARKER = new PrefName("ucs_marker", "redball.gif");

	/** draw gradient color in appointment boxes - can slow down older machines */
	static public PrefName GRADIENT_APPTS = new PrefName("gradient_appts", "true");

	/** when showing tasks project and subtasks, prepend a prefix and id number */
	static public PrefName TASK_SHOW_ABBREV = new PrefName("task_show_abbrev", "false");

	/** show tasks on calendar */
	static public PrefName CAL_SHOW_TASKS = new PrefName("cal_show_tasks", "true");

	/** show subtasks on calendar */
	static public PrefName CAL_SHOW_SUBTASKS = new PrefName("cal_show_subtasks", "true");

	/** show task number and status in task tree */
	static public PrefName TASK_TREE_SHOW_STATUS = new PrefName("task_tree_show_status", "false");

	// days left until due for each color
	static public PrefName RED_DAYS = new PrefName("red_days", Integer.valueOf(2));
	static public PrefName ORANGE_DAYS = new PrefName("orange_days", Integer.valueOf(7));
	static public PrefName YELLOW_DAYS = new PrefName("yellow_days", Integer.valueOf(14));

	/** keystore location */
	static public PrefName KEYSTORE = new PrefName("key_store", "");

	/** encryption key alias in the keystore */
	static public PrefName KEYALIAS = new PrefName("key_alias", "borg_key");

	/** cached password time to live in seconds */
	static public PrefName PASSWORD_TTL = new PrefName("pw_ttl", Integer.valueOf(300));

	/** todo quick add, clear text after add */
	static public PrefName TODO_QUICK_ENTRY_AUTO_CLEAR_TEXT_FIELD = new PrefName(
			"todo_option_auto_clear_text", "false");

	/** todo quick add, default date to today */
	static public PrefName TODO_QUICK_ENTRY_AUTO_SET_DATE_FIELD = new PrefName(
			"todo_option_auto_date_today", "false");
	
	/** show only current todo in a repeating todo */
	static public PrefName TODO_ONLY_SHOW_CURRENT = new PrefName("todo_only_show_current", "false");
	
	/** shutdown action */
	static public PrefName SHUTDOWN_ACTION = new PrefName("shutdown_action", "");
	public static final PrefName SHUTDOWNTIME = new PrefName("shuttime", "0");

	/** debug flag - trigger debug logging */
	public static final PrefName DEBUG = new PrefName("debug", "false");
	public static final PrefName AUDITLOG = new PrefName("auditlog", "false");

	// limit on the max text size that can be put into a text area to prevent
	// memory issues
	public static final PrefName MAX_TEXT_SIZE = new PrefName("max_text_size", Integer.valueOf(1024 * 1024));

	public static PrefName ICAL_EXPORTYEARS = new PrefName("ical-export-years", Integer.valueOf(2));

	// option to export todos as VTODO objects instead of VEVENTS
	public static PrefName ICAL_EXPORT_TODO = new PrefName("ical-export-todo", "false");
	
	public static PrefName CALDAV_SERVER = new PrefName("caldav-server", "");
	public static PrefName CALDAV_PATH = new PrefName("caldav-path", "/baikal/html");
	public static PrefName CALDAV_USER_PATH = new PrefName("caldav-user-path", "dav.php/calendars");
	public static PrefName CARDDAV_USER_PATH = new PrefName("carddav-user-path", "dav.php/addressbooks");
	public static PrefName CALDAV_PRINCIPAL_PATH = new PrefName("caldav-principal-path", "dav.php/principals");
	public static PrefName CALDAV_USER = new PrefName("caldav-user", "");
	public static PrefName CALDAV_PASSWORD = new PrefName("caldav-password", "");
	public static PrefName CALDAV_PASSWORD2 = new PrefName("caldav-password2", "");

	public static PrefName CALDAV_CAL = new PrefName("caldav-cal", "default");
	public static PrefName CARDDAV_BOOK = new PrefName("carddav_book", "default");
	public static PrefName CALDAV_USE_SSL = new PrefName("caldav-use-ssl", "false");
	public static PrefName CALDAV_ALLOW_SELF_SIGNED_CERT = new PrefName("caldav-self-signed", "false");

	public static final PrefName GOOGLE_SYNC = new PrefName("google_sync", "false");
	public static final PrefName GCAL_CAL_ID = new PrefName("google_cal_id", "");
	public static final PrefName GCAL_TASKLIST_ID = new PrefName("google_tasklist_id", "");
	public static final PrefName GOOGLE_CRED_FILE = new PrefName("google_cred_file", "");
	public static final PrefName GOOGLE_TOKEN_DIR = new PrefName("google_token_dir", "");
	public static final PrefName GCAL_EXPORTYEARS = new PrefName("gcal-export-years", Integer.valueOf(2));
	public static final PrefName GOOGLE_SUBSCRIBED = new PrefName("google_subscribed", "");




}
