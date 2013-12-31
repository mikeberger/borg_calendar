package net.sf.borg.model.ical;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;
import net.fortuna.ical4j.connector.dav.PathResolver;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.sf.borg.common.IOHelper;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model.ChangeEvent.ChangeAction;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.ical.SyncEvent.ObjectType;
import biz.source_code.base64Coder.Base64Coder;

public class CalDav {

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

	static private final String PRODID = "-//MBCSoft/BORG//EN";

	static private final Logger log = Logger.getLogger("net.sf.borg");

	public static boolean isSyncing() {
		String server = Prefs.getPref(PrefName.CALDAV_SERVER);
		if (server != null && !server.isEmpty())
			return true;
		return false;
	}

	private static void addEvent(CalDavCalendarCollection collection,
			Component comp) {

		log.info("SYNC: addEvent: " + comp.toString());

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

	private static CalDavCalendarStore connect() throws Exception {

		IOHelper.setProxy();

		URL url = new URL("http", Prefs.getPref(PrefName.CALDAV_SERVER), -1,
				"/");
		IOHelper.sendLogMessage("SYNC: connect to " + url.toString());
		log.info("SYNC: connect to " + url.toString());

		CalDavCalendarStore store = new CalDavCalendarStore("-", url,
				new BaikalPathResolver());

		if (store.connect(Prefs.getPref(PrefName.CALDAV_USER), gep()
				.toCharArray()))
			return store;

		return null;
	}

	static public void export(Integer years) throws Exception {

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

		String calname = Prefs.getPref(PrefName.CALDAV_CAL);

		CalDavCalendarStore store = connect();
		if (store == null)
			throw new Exception("Failed to connect to CalDav Store");

		String cal_id = new BaikalPathResolver().getUserPath(Prefs
				.getPref(PrefName.CALDAV_USER)) + "/" + calname;
		try {
			store.removeCollection(cal_id);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}

		CalDavCalendarCollection collection = store.addCollection(cal_id,
				calname, calname, new String[] { "VEVENT", "VTODO" }, null);

		ComponentList clist = calendar.getComponents();
		Iterator<Component> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();
			addEvent(collection, comp);
		}

		// if we are exporting VTODOs and there is a second calendar set, then
		// we need to dump the todos as VEVENTS on the second cal
		if (Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO)) {
			String cal2 = Prefs.getPref(PrefName.CALDAV_CAL2);
			if (!cal2.isEmpty()) {
				String cal_id2 = new BaikalPathResolver().getUserPath(Prefs
						.getPref(PrefName.CALDAV_USER)) + "/" + cal2;
				try {
					store.removeCollection(cal_id2);
				} catch (Exception e) {
					log.severe(e.getMessage());
				}

				CalDavCalendarCollection collection2 = store.addCollection(
						cal_id2, cal2, cal2,
						new String[] { "VEVENT", "VTODO" }, null);

				ComponentList clist2 = calendar.getComponents();
				Iterator<Component> it1 = clist2.iterator();
				while (it1.hasNext()) {
					Component comp = it1.next();
					if (comp instanceof VToDo) {
						Appointment ap = AppointmentIcalAdapter.toBorg(comp);
						ap.setUid(ap.getUid() + "TD");
						Component ve = AppointmentIcalAdapter.toIcal(ap, false);
						addEvent(collection2, ve);
					}
				}
			}
		}

	}

	public static String gep() throws Exception {
		String p1 = Prefs.getPref(PrefName.CALDAV_PASSWORD2);
		String p2 = Prefs.getPref(PrefName.CALDAV_PASSWORD);
		if ("".equals(p2))
			return p2;

		if ("".equals(p1)) {
			sep(p2); // transition case
			return p2;
		}

		byte[] ba = Base64Coder.decode(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher dec = Cipher.getInstance("AES");
		dec.init(Cipher.DECRYPT_MODE, key);
		byte[] decba = Base64Coder.decode(p2);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, dec);
		os.write(decba);
		os.close();

		return baos.toString();

	}

	private static CalDavCalendarCollection getCollection(
			CalDavCalendarStore store, String calName) throws Exception {
		String cal_id = new BaikalPathResolver().getUserPath(Prefs
				.getPref(PrefName.CALDAV_USER)) + "/" + calName;
		return store.getCollection(cal_id);
	}

	private static Component getEvent(CalDavCalendarCollection collection,
			String uid) {
		Calendar cal = collection.getCalendar(uid);
		if (cal == null)
			return null;

		ComponentList clist = cal.getComponents();
		Iterator<Component> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();
			if (comp instanceof VEvent || comp instanceof VToDo)
				return comp;
		}
		return null;
	}

	static public void processSyncMap(CalDavCalendarCollection collection,
			CalDavCalendarCollection collection2) throws Exception {

		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		List<SyncEvent> syncEvents = SyncLog.getReference().getAll();
		IOHelper.sendLogMessage("SYNC: Process " + syncEvents.size() + " Outgoing Items");
		log.info("SYNC: Process " + syncEvents.size() + " Outgoing Items");

		for (SyncEvent se : syncEvents) {
			if (se.getObjectType() != ObjectType.APPOINTMENT)
				continue;

			try {
				if (se.getAction().equals(ChangeAction.ADD)) {
					Appointment ap = AppointmentModel.getReference().getAppt(
							se.getId());
					if (ap == null)
						continue;
					Component ve1 = AppointmentIcalAdapter.toIcal(ap,
							export_todos);
					addEvent(collection, ve1);
					if (collection2 != null && ap.isTodo()) {
						Component ve = AppointmentIcalAdapter.toIcal(ap, false);
						Uid uid = (Uid) ve.getProperty(Property.UID);
						uid.setValue(uid.getValue() + "TD");
						ve.getProperty(Property.DTSTART).setValue(
								ve1.getProperty(Property.DTSTART).getValue());
						addEvent(collection2, ve);
					}
				} else if (se.getAction().equals(ChangeAction.CHANGE)) {
					Component comp = getEvent(collection, se.getUid());
					Appointment ap = AppointmentModel.getReference().getAppt(
							se.getId());

					if (comp == null) {
						Component ve1 = AppointmentIcalAdapter.toIcal(ap,
								export_todos);
						addEvent(collection, ve1);
						if (collection2 != null && ap.isTodo()) {
							Component ve = AppointmentIcalAdapter.toIcal(ap,
									false);
							Uid uid = (Uid) ve.getProperty(Property.UID);
							uid.setValue(uid.getValue() + "TD");
							ve.getProperty(Property.DTSTART)
									.setValue(
											ve1.getProperty(Property.DTSTART)
													.getValue());
							addEvent(collection2, ve);
						}
					} else // TODO - what if both sides updated
					{
						// in order to detect if both sides updated, borg would
						// need to store the
						// last modified time sent from borg to the server.

						/*
						 * LastModified lm = null; if (comp instanceof VEvent) {
						 * lm = ((VEvent) comp).getLastModified(); } else if
						 * (comp instanceof VToDo) { lm = ((VToDo)
						 * comp).getLastModified(); } long serverTime =
						 * lm.getDateTime().getTime();
						 */
						Component ve1 = AppointmentIcalAdapter.toIcal(ap,
								export_todos);
						updateEvent(collection,
								ve1);
						if (collection2 != null && ap.isTodo()) {
							Component ve = AppointmentIcalAdapter.toIcal(ap,
									false);
							Uid uid = (Uid) ve.getProperty(Property.UID);
							uid.setValue(uid.getValue() + "TD");
							ve.getProperty(Property.DTSTART)
							.setValue(
									ve1.getProperty(Property.DTSTART)
											.getValue());
							updateEvent(collection2, ve);
						}
					}
				} else if (se.getAction().equals(ChangeAction.DELETE)) {

					Component comp = getEvent(collection, se.getUid());
					
					if (comp != null) {
						log.info("SYNC: removeEvent: " + comp.toString());
						collection.removeCalendar(se.getUid());
						if (collection2 != null && comp instanceof VToDo) {
							collection2.removeCalendar(se.getUid() + "TD");
						}
					}
					else
					{
						log.info("Deleted Appt: " + se.getUid()
								+ " not found on server");
					}
				}

				SyncLog.getReference().delete(se.getId(), se.getObjectType());
			} catch (Exception e) {
				IOHelper.sendLogMessage("SYNC ERROR for: " + se.toString() + ":"
						+ e.getMessage());
				log.severe("SYNC ERROR for: " + se.toString() + ":"
						+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void sep(String s) throws Exception {
		if ("".equals(s)) {
			Prefs.putPref(PrefName.CALDAV_PASSWORD, s);
			return;
		}
		String p1 = Prefs.getPref(PrefName.CALDAV_PASSWORD2);
		if ("".equals(p1)) {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			SecretKey key = keyGen.generateKey();
			p1 = new String(Base64Coder.encode(key.getEncoded()));
			Prefs.putPref(PrefName.CALDAV_PASSWORD2, p1);
		}

		byte[] ba = Base64Coder.decode(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, enc);
		os.write(s.getBytes());
		os.close();
		ba = baos.toByteArray();
		Prefs.putPref(PrefName.CALDAV_PASSWORD,
				new String(Base64Coder.encode(ba)));
	}

	static public void sync(Integer years, boolean outward_only)
			throws Exception {
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);

		CalDavCalendarStore store = connect();
		if (store == null)
			throw new Exception("Failed to connect to CalDav Store");

		log.info("SYNC: Get Collection");

		String calname = Prefs.getPref(PrefName.CALDAV_CAL);

		CalDavCalendarCollection collection = getCollection(store, calname);

		// if we are exporting VTODOs and there is a second calendar set, then
		// we need to dump the todos as VEVENTS on the second cal
		// this is for the various clients that cannot see the todos on the
		// first calendar - like android
		CalDavCalendarCollection collection2 = null;
		if (Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO)) {
			String cal2 = Prefs.getPref(PrefName.CALDAV_CAL2);
			if (!cal2.isEmpty()) {
				collection2 = getCollection(store, cal2);
			}
		}

		processSyncMap(collection, collection2);

		if (!outward_only)
			syncFromServer(collection, years);

		log.info("SYNC: Done");

	}

	static public void syncCalendar(Calendar cal, ArrayList<String> serverUids)
			throws Exception {
		ComponentList clist = cal.getComponents();
		Iterator<Component> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();

			if (!(comp instanceof VEvent || comp instanceof VToDo))
				continue;
			String uid = comp.getProperty(Property.UID).getValue();
			serverUids.add(uid);

			Appointment newap = AppointmentIcalAdapter.toBorg(comp);
			if (newap == null)
				continue;

			if (comp instanceof VToDo) {
				newap.setTodo(true);
			}

			Appointment ap = AppointmentModel.getReference().getApptByUid(uid);
			if (ap == null) {
				// not found in BORG, so add it
				try {

					// for now, VTodo updates should update the synclog so that
					// they get sent back out
					// to the second cal on the next sync
					SyncLog.getReference().setProcessUpdates(
							comp instanceof VToDo);
					log.info("SYNC save: " + newap.toString());
					AppointmentModel.getReference().saveAppt(newap);
				} finally {
					SyncLog.getReference().setProcessUpdates(true);
				}
			} else if (newap.getLastMod().after(ap.getLastMod())) {
				// was updated after BORG so update BORG
				try {
					newap.setKey(ap.getKey());

					// for now, VTodo updates should update the synclog so that
					// they get sent back out
					// to the second cal on the next sync
					SyncLog.getReference().setProcessUpdates(
							comp instanceof VToDo);
					log.info("SYNC save: " + newap.toString());
					AppointmentModel.getReference().saveAppt(newap);
				} finally {
					SyncLog.getReference().setProcessUpdates(true);
				}
			}

		}

	}

	static public void syncFromServer(CalDavCalendarCollection collection,
			Integer years) throws Exception {

		IOHelper.sendLogMessage("SYNC: Start Incoming Sync");
		log.info("SYNC: Start Incoming Sync");

		Date after = null;
		if (years != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.add(java.util.Calendar.YEAR, -1 * years.intValue());
			after = cal.getTime();
		}

		ArrayList<String> serverUids = new ArrayList<String>();

		Calendar cals[] = collection.getEvents();
		IOHelper.sendLogMessage("SYNC: found " + cals.length + " Event Calendars on server");
		log.info("SYNC: found " + cals.length + " Event Calendars on server");
		for (Calendar cal : cals) {
			syncCalendar(cal, serverUids);
		}

		Calendar tcals[] = collection.getTasks();
		IOHelper.sendLogMessage("SYNC: found " + tcals.length + " Todo Calendars on server");
		log.info("SYNC: found " + tcals.length + " Todo Calendars on server");
		for (Calendar cal : tcals) {
			syncCalendar(cal, serverUids);
		}

		log.fine(serverUids.toString());

		IOHelper.sendLogMessage("SYNC: check for deletes");
		log.info("SYNC: check for deletes");

		// find all appts in Borg that are not on the server
		for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {
			if (ap.getDate().before(after))
				continue;

			if (!serverUids.contains(ap.getUid())) {
				IOHelper.sendLogMessage("Appointment Not Found in Borg - Deleting: " + ap.toString());
				log.info("Appointment Not Found in Borg - Deleting: " + ap.toString());
				SyncLog.getReference().setProcessUpdates(false);
				AppointmentModel.getReference().delAppt(ap.getKey());
				SyncLog.getReference().setProcessUpdates(true);
			}
		}

	}

	private static void updateEvent(CalDavCalendarCollection collection,
			Component comp) {

		log.info("SYNC: updateEvent: " + comp.toString());

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

}
