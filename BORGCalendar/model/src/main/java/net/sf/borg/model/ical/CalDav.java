package net.sf.borg.model.ical;

import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Logger;

import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;
import net.fortuna.ical4j.connector.dav.PathResolver;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model.ChangeEvent.ChangeAction;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.ical.SyncEvent.ObjectType;

public class CalDav {

	static private final String CAL_NAME = "default";
	static private final String PRODID = "-//MBCSoft/BORG//EN";
	static private final String user = "mike"; // TODO
	static private final String pw = "**"; // TODO
	static private final String server = "192.168.1.20"; // TODO

	static private final Logger log = Logger.getLogger("net.sf.borg");

	public static class BaikalPathResolver extends PathResolver {

		@Override
		public String getPrincipalPath(String username) {
			return ("/bkal/cal.php/principals/" + username);
		}

		@Override
		public String getUserPath(String username) {
			return ("/bkal/cal.php/calendars/" + username);
		}

	}

	static public void sync(Integer years) throws Exception {
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);

		processSyncMap();

		// TODO for all events on caldav
		/*
		 * if not found on borg - add else update if caldav lastmod > borg
		 * lastmod
		 */

		// TODO for all events in BORG not in caldav
		// ?? mark as strikethrough??? and add to caldav

	}

	static public void processSyncMap() throws Exception {
		// TODO

		CalDavCalendarStore store = connect();
		if (store == null)
			throw new Exception("Failed to connect to CalDav Store");

		CalDavCalendarCollection collection = getCollection(store);

		for (SyncEvent se : SyncLog.getReference().getAll()) {
			if (se.getObjectType() != ObjectType.APPOINTMENT)
				continue;

			try {
				if (se.getAction().equals(ChangeAction.ADD)) {
					Appointment ap = AppointmentModel.getReference().getAppt(
							se.getId());
					if (ap == null)
						continue;
					addEvent(collection, AppointmentIcalAdapter.toIcal(ap));
				} else if (se.getAction().equals(ChangeAction.CHANGE)) {
					Component comp = getEvent(collection, se.getUid());
					Appointment ap = AppointmentModel.getReference().getAppt(
							se.getId());

					if (comp == null) {
						addEvent(collection, AppointmentIcalAdapter.toIcal(ap));
					} else // TODO - what if both sides updated
					{
						updateEvent(collection,
								AppointmentIcalAdapter.toIcal(ap));
					}
				} else if (se.getAction().equals(ChangeAction.DELETE)) {

					Component comp = getEvent(collection, se.getUid());
					if (comp != null) {
						collection.removeCalendar(se.getUid());
					}
				}

				SyncLog.getReference().delete(se.getId(), se.getObjectType());
			} catch (Exception e) {
				log.severe("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	static public void export(Integer years) throws Exception {

		// TODO - confirm dialog

		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);

		Date after = null;
		if (years != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.add(java.util.Calendar.YEAR, -1 * years.intValue());
			after = cal.getTime();
		}
		Calendar calendar = AppointmentIcalAdapter.exportIcal(after, false);

		CalDavCalendarStore store = connect();
		if (store == null)
			throw new Exception("Failed to connect to CalDav Store");

		String cal_id = new BaikalPathResolver().getUserPath(user) + "/"
				+ CAL_NAME;
		try {
			store.removeCollection(cal_id);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
		CalDavCalendarCollection collection = store.addCollection(cal_id,
				CAL_NAME, CAL_NAME, new String[] { "VEVENT", "VTODO" }, null);

		ComponentList clist = calendar.getComponents();
		@SuppressWarnings("unchecked")
		Iterator<Component> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();
			addEvent(collection, comp);
		}

	}

	private static CalDavCalendarCollection getCollection(
			CalDavCalendarStore store) throws Exception {
		String cal_id = new BaikalPathResolver().getUserPath(user) + "/"
				+ CAL_NAME;
		return store.getCollection(cal_id);
	}

	private static CalDavCalendarStore connect() throws Exception {
		URL url = new URL("http", server, -1, "/");
		CalDavCalendarStore store = new CalDavCalendarStore("-", url,
				new BaikalPathResolver());

		if (store.connect(user, pw.toCharArray()))
			return store;

		return null;
	}

	private static void addEvent(CalDavCalendarCollection collection,
			Component comp) {
		Calendar mycal = new Calendar();
		mycal.getProperties().add(new ProdId(PRODID));
		mycal.getProperties().add(Version.VERSION_2_0);
		mycal.getComponents().add(comp);

		try {
			collection.addCalendar(mycal);
		} catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void updateEvent(CalDavCalendarCollection collection,
			Component comp) {
		Calendar mycal = new Calendar();
		mycal.getProperties().add(new ProdId(PRODID));
		mycal.getProperties().add(Version.VERSION_2_0);
		mycal.getComponents().add(comp);

		try {
			collection.updateCalendar(mycal);
		} catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	private static Component getEvent(CalDavCalendarCollection collection,
			String uid) {
		Calendar cal = collection.getCalendar(uid);

		ComponentList clist = cal.getComponents();
		Iterator<Component> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();
			if (comp instanceof VEvent || comp instanceof VToDo)
				return comp;
		}
		return null;
	}

}
