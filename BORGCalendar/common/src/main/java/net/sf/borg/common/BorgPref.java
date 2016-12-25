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

import com.mbcsoft.platform.common.PrefName;

/**
 * PrefName contains all of the Borg preference definitions and default values.
 * It enforces compile time checking of preference names
 */
public class BorgPref  {





	/** show the spash window */
	static public PrefName SPLASH = new PrefName("splash", "true");

	

	/** start as iconified to system tray */
	static public PrefName BACKGSTART = new PrefName("backgstart", "false");

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
	static public PrefName PRIORITY_SORT = new PrefName("priority_sort",
			"false");

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

	/** how often tp pop up reminders for untimed todos */
	static public PrefName TODOREMINDERMINS = new PrefName(
			"todo_reminder_mins", new Integer(30));

	/** option to consolidate all reminders in a single list window */
	static public PrefName REMINDERLIST = new PrefName("reminder_list", "true");

	/** show reminders for tasks */
	static public PrefName TASKREMINDERS = new PrefName("task_reminders",
			"true");

	/** days before a birthday to show birthday reminders */
	static public PrefName BIRTHDAYREMINDERDAYS = new PrefName(
			"bd_reminder_days", new Integer(7));

	/** The EMAILENABLED. */
	static public PrefName EMAILENABLED = new PrefName("email_enabled", "false");

	/** The EMAILSERVER. */
	static public PrefName EMAILSERVER = new PrefName("email_server", "");

	/** The EMAILADDR. */
	static public PrefName EMAILADDR = new PrefName("email_addr", "");
	static public PrefName EMAILFROM = new PrefName("email_from", "");

	/** The EMAILLAST. */
	static public PrefName EMAILLAST = new PrefName("email_last",
			new Integer(0));

	

	/** The EMAILTIME. */
	static public PrefName EMAILTIME = new PrefName("email_time",
			new Integer(0));

	/** The EMAILUSER. */
	static public PrefName EMAILUSER = new PrefName("email_user", "");

	/** The EMAILPASS. */
	static public PrefName EMAILPASS = new PrefName("email_pass", "");
	static public PrefName EMAILPASS2 = new PrefName("email_pass2", "");



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

	static public PrefName YEARVIEWFONT = new PrefName("yearviewfont",
			"SansSerif-7");

	static public PrefName TRAYFONT = new PrefName("trayfont",
			"SansSerif-BOLD-12");

	/** The LNF. */
	static public PrefName LNF = new PrefName("lnf",
			"com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
	static public PrefName GOODIESTHEME = new PrefName("goodies_theme",
			"ExperienceBlue");

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

	/** draw gradient color in appointment boxes - can slow down older machines */
	static public PrefName GRADIENT_APPTS = new PrefName("gradient_appts",
			"true");

	// tasks
	/** when showing tasks project and subtasks, prepend a prefix and id number */
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

	// days left until due for each color
	static public PrefName RED_DAYS = new PrefName("red_days", new Integer(2));
	static public PrefName ORANGE_DAYS = new PrefName("orange_days",
			new Integer(7));
	static public PrefName YELLOW_DAYS = new PrefName("yellow_days",
			new Integer(14));


	/** encryption key alias in the keystore */
	static public PrefName KEYALIAS = new PrefName("key_alias", "borg_key");

	
	/** todo quick add, clear text after add */
	static public PrefName TODO_QUICK_ENTRY_AUTO_CLEAR_TEXT_FIELD = new PrefName(
			"todo_option_auto_clear_text", "false");

	/** todo quick add, default date to today */
	static public PrefName TODO_QUICK_ENTRY_AUTO_SET_DATE_FIELD = new PrefName(
			"todo_option_auto_date_today", "false");

	/** allow dynamically loaded plugins */
	static public PrefName DYNAMIC_LOADING = new PrefName("dynamic_loading",
			"false");

	/** shutdown action */
	static public PrefName SHUTDOWN_ACTION = new PrefName("shutdown_action", "");



	/** debug flag - trigger debug logging */
	public static final PrefName DEBUG = new PrefName("debug", "false");

	// limit on the max text size that can be put into a text area to prevent
	// memory issues
	public static final PrefName MAX_TEXT_SIZE = new PrefName("max_text_size",
			new Integer(1024 * 1024));

	// the email regular expression and phone number regular expression
	// regular expression for validating email addresses
	static public PrefName EMAIL_VALIDATION = new PrefName("Email_Validation",
			"false");

	public static PrefName ICAL_PORT = new PrefName("ical-server-port",
			new Integer(8844));
	public static PrefName ICAL_EXPORTYEARS = new PrefName("ical-export-years",
			new Integer(2));
	
	public static PrefName ICAL_SYNCMINS = new PrefName("ical-syncmins",
			new Integer(15));

	// option to prevent import of appts that were previously exported from borg
	// used when the goal is to only import appointments created outside of
	// borg, but
	// to not import appts that were exported from borg to another calendar and
	// then
	// sent back to borg as part of the export from the other calendar
	public static PrefName SKIP_BORG = new PrefName("ical-skip_borg", "true");

	// FTP
	public static PrefName FTPSERVER = new PrefName("ical-ftp-server",
			"localhost");
	public static PrefName FTPPATH = new PrefName("ical-ftp-path", "borg.ics");
	public static PrefName FTPUSER = new PrefName("ical-ftp-user", "");
	public static PrefName FTPPW = new PrefName("ical-ftp-pw", "");
	public static PrefName FTPPW2 = new PrefName("ical-ftp-pw2", "");
	
	
	
	public static PrefName ICAL_IMPORT_URL = new PrefName("ical-import-url", "");
	
	// option to export todos as VTODO objects instead of VEVENTS
	public static PrefName ICAL_EXPORT_TODO = new PrefName("ical-export-todo", "false");
	
	public static PrefName CALDAV_SERVER = new PrefName("caldav-server", "");
	public static PrefName CALDAV_PATH = new PrefName("caldav-path", "/");
	public static PrefName CALDAV_USER_PATH = new PrefName("caldav-user-path", "/cal.php/calendars/");
	public static PrefName CALDAV_PRINCIPAL_PATH = new PrefName("caldav-principal-path", "/cal.php/principals/");
	public static PrefName CALDAV_USER = new PrefName("caldav-user", "");
	public static PrefName CALDAV_PASSWORD = new PrefName("caldav-password", "");
	public static PrefName CALDAV_PASSWORD2 = new PrefName("caldav-password2", "");

	public static PrefName CALDAV_CAL = new PrefName("caldav-cal", "default");
	//public static PrefName CALDAV_CAL2 = new PrefName("caldav-cal2", "");
	public static PrefName CALDAV_USE_SSL = new PrefName("caldav-use-ssl", "false");
	public static PrefName CALDAV_ALLOW_SELF_SIGNED_CERT = new PrefName("caldav-self-signed", "false");
	


	public static PrefName FLUSH_MINS = new PrefName("flush_mins",
			new Integer(0));

}
