package net.sf.borg.test;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.ical.CalDav;

public class CalDavTest {
	
	static private final Logger log = Logger.getLogger("net.sf.borg");

	public static void main(String args[])  throws Exception {
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
		
		CalDavCalendarStore store = CalDav.connect();
		
		log.info("SYNC: Start Incoming Sync");

		Date after = null;
		GregorianCalendar gcal = new GregorianCalendar();

		gcal.add(java.util.Calendar.YEAR, -2);
		after = gcal.getTime();

		gcal = new GregorianCalendar();
		gcal.add(java.util.Calendar.YEAR, 10);
		Date tenYears = gcal.getTime();


		net.fortuna.ical4j.model.DateTime dtstart = new net.fortuna.ical4j.model.DateTime(after);
		net.fortuna.ical4j.model.DateTime dtend = new net.fortuna.ical4j.model.DateTime(tenYears);
		log.info("SYNC: " + dtstart.toString() + "--" + dtend.toString());
		String calname = Prefs.getPref(PrefName.CALDAV_CAL);

		CalDavCalendarCollection collection = CalDav.getCollection(store, calname);
		Calendar cals[] = collection.getEventsForTimePeriod(dtstart,dtend);
		log.info("SYNC: found " + cals.length + " Event Calendars on server");
		log.info(cals[0].toString());
		cals = collection.getEvents();
		log.info("SYNC: found " + cals.length + " Event Calendars on server");
		log.info(cals[0].toString());
	}
}