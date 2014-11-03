package net.sf.borg.model.ical;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;
import net.fortuna.ical4j.connector.dav.PathResolver;
import net.fortuna.ical4j.connector.dav.PathResolver.GenericPathResolver;
import net.fortuna.ical4j.connector.dav.property.CSDavPropertyName;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.SocketClient;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.Model.ChangeEvent.ChangeAction;
import net.sf.borg.model.OptionModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Option;
import net.sf.borg.model.ical.SyncEvent.ObjectType;
import biz.source_code.base64Coder.Base64Coder;

@SuppressWarnings("unchecked")
public class CalDav {

	public static final String CTAG_OPTION = "CTAG";

	static private final String PRODID = "-//MBCSoft/BORG//EN";

	static private final int REMOTE_ID = 1;

	static private final Logger log = Logger.getLogger("net.sf.borg");

	public static boolean isSyncing() {
		String server = Prefs.getPref(PrefName.CALDAV_SERVER);
		if (server != null && !server.isEmpty())
			return true;
		return false;
	}

	public static PathResolver createPathResolver() {
		GenericPathResolver pathResolver = new GenericPathResolver();
		String basePath = Prefs.getPref(PrefName.CALDAV_PATH);
		if (!basePath.endsWith("/"))
			basePath += "/";
		pathResolver.setPrincipalPath(basePath
				+ Prefs.getPref(PrefName.CALDAV_PRINCIPAL_PATH));
		pathResolver.setUserPath(basePath
				+ Prefs.getPref(PrefName.CALDAV_USER_PATH));
		return pathResolver;
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
		
		if( !isSyncing())
			return null;
			
		Prefs.setProxy();

		URL url = new URL("http", Prefs.getPref(PrefName.CALDAV_SERVER), -1,
				Prefs.getPref(PrefName.CALDAV_PATH));
		SocketClient.sendLogMessage("SYNC: connect to " + url.toString());
		log.info("SYNC: connect to " + url.toString());

		CalDavCalendarStore store = new CalDavCalendarStore("-", url,
				createPathResolver());

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

		String cal_id = createPathResolver().getUserPath(
				Prefs.getPref(PrefName.CALDAV_USER))
				+ "/" + calname;
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
				String cal_id2 = createPathResolver().getUserPath(
						Prefs.getPref(PrefName.CALDAV_USER))
						+ "/" + cal2;
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
		String cal_id = createPathResolver().getUserPath(
				Prefs.getPref(PrefName.CALDAV_USER))
				+ "/" + calName;
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
		SocketClient.sendLogMessage("SYNC: Process " + syncEvents.size()
				+ " Outgoing Items");
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
							ve.getProperty(Property.DTSTART).setValue(
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
						updateEvent(collection, ve1);
						if (collection2 != null && ap.isTodo()) {
							Component ve = AppointmentIcalAdapter.toIcal(ap,
									false);
							Uid uid = (Uid) ve.getProperty(Property.UID);
							uid.setValue(uid.getValue() + "TD");
							ve.getProperty(Property.DTSTART).setValue(
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
					} else {
						log.info("Deleted Appt: " + se.getUid()
								+ " not found on server");
					}
				}

				SyncLog.getReference().delete(se.getId(), se.getObjectType());
			} catch (Exception e) {
				SocketClient.sendLogMessage("SYNC ERROR for: " + se.toString()
						+ ":" + e.getMessage());
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

	/**
	 * check remote server to see if sync needed - must not be run on Event
	 * thread
	 * 
	 * @throws Exception
	 */
	static public boolean checkRemoteSync() throws Exception {
		CalDavCalendarStore store = connect();
		if (store == null)
			throw new Exception("Failed to connect to CalDav Store");

		log.info("SYNC: Get Collection");

		String calname = Prefs.getPref(PrefName.CALDAV_CAL);

		CalDavCalendarCollection collection = getCollection(store, calname);

		String ctag = collection.getProperty(CSDavPropertyName.CTAG,
				String.class);
		log.info("SYNC: CTAG=" + ctag);

		boolean incoming_changes = true;

		String lastCtag = OptionModel.getReference().getOption(CTAG_OPTION);
		if (lastCtag != null && lastCtag.equals(ctag))
			incoming_changes = false;

		return incoming_changes;
	}

	/**
	 * check if syncmap has a record indicating that a remote sync is needed
	 */
	static public boolean isServerSyncNeeded() throws Exception {

		SyncEvent ev = SyncLog.getReference().get(REMOTE_ID,
				SyncEvent.ObjectType.REMOTE);
		if (ev != null)
			return true;

		return false;

	}

	/**
	 * add/delete the symcmap record that indicates that a remote sync is needed
	 * MUST be run on the Event thread as it sends a change event notification
	 */
	static public void setServerSyncNeeded(boolean needed) throws Exception {
		if (needed)
			SyncLog.getReference().insert(
					new SyncEvent(REMOTE_ID, "",
							ChangeEvent.ChangeAction.CHANGE,
							SyncEvent.ObjectType.REMOTE));
		else
			SyncLog.getReference().delete(REMOTE_ID,
					SyncEvent.ObjectType.REMOTE);

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

		String ctag = collection.getProperty(CSDavPropertyName.CTAG,
				String.class);
		log.info("SYNC: CTAG=" + ctag);

		boolean incoming_changes = true;

		String lastCtag = OptionModel.getReference().getOption(CTAG_OPTION);
		if (lastCtag != null && lastCtag.equals(ctag))
			incoming_changes = false;

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

		if (!incoming_changes)
			SocketClient.sendLogMessage("SYNC: no incoming changes\n");

		if (!outward_only && incoming_changes) {
			syncFromServer(collection, years);

			// incoming sync could cause additional outward activity due to borg
			// needing to convert multiple events
			// into one - a limitation of borg
			processSyncMap(collection, collection2);
		}

		// update saved ctag
		collection = getCollection(store, calname);
		ctag = collection.getProperty(CSDavPropertyName.CTAG, String.class);
		OptionModel.getReference().setOption(new Option(CTAG_OPTION, ctag));
		log.info("SYNC: NEW CTAG=" + ctag);

		// remove any remote sync event
		setServerSyncNeeded(false);

		log.info("SYNC: Done");

	}

	static public void processRecurrence(Component comp, String uid)
			throws Exception {

		RecurrenceId rid = (RecurrenceId) comp
				.getProperty(Property.RECURRENCE_ID);

		Appointment ap = AppointmentModel.getReference().getApptByUid(uid);
		if (ap != null) {

			// LastModified lm = (LastModified) comp
			// .getProperty(Property.LAST_MODIFIED);
			// Date lmdate = lm.getDateTime();
			// if (lmdate.after(ap.getLastMod())) {
			if (comp instanceof VEvent) {
				log.warning("SYNC: ignoring Vevent for single recurrence - cannot process\n"
						+ comp.toString());
				SocketClient
						.sendLogMessage("SYNC: ignoring Vevent for single recurrence - cannot process\n"
								+ comp.toString());
				return;
			}
			// for a recurrence of a VToDo, we only use the
			// COMPLETED
			// status if present - otherwise, we ignore
			Completed cpltd = (Completed) comp.getProperty(Property.COMPLETED);
			if (cpltd == null) {
				log.warning("SYNC: ignoring VToDo for single recurrence - cannot process\n"
						+ comp.toString());
				SocketClient
						.sendLogMessage("SYNC: ignoring VToDo for single recurrence - cannot process\n"
								+ comp.toString());
				return;
			}

			Date riddate = rid.getDate();

			Date utc = new Date();
			utc.setTime(riddate.getTime());

			// adjust time zone
			if (!rid.isUtc() && !rid.getValue().contains("T")) {
				long u = riddate.getTime()
						- TimeZone.getDefault().getOffset(riddate.getTime());
				utc.setTime(u);
			}

			Date nt = ap.getNextTodo();
			if (nt == null)
				nt = ap.getDate();
			if (!utc.before(nt)) {
				log.warning("SYNC: completing Todo\n" + comp.toString());
				SocketClient.sendLogMessage("SYNC: completing Todo\n"
						+ comp.toString());
				AppointmentModel.getReference()
						.do_todo(ap.getKey(), false, utc);

			}

			// }
		}
	}

	static public int syncCalendar(Calendar cal, ArrayList<String> serverUids)
			throws Exception {

		int count = 0;

		ComponentList clist = cal.getComponents();
		Iterator<Component> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();

			if (!(comp instanceof VEvent || comp instanceof VToDo))
				continue;
			String uid = comp.getProperty(Property.UID).getValue();
			serverUids.add(uid);

			// detect single occurrence
			RecurrenceId rid = (RecurrenceId) comp
					.getProperty(Property.RECURRENCE_ID);
			if (rid != null) {
				processRecurrence(comp, uid);
				continue;
			}

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
					count++;
					log.info("SYNC save: " + comp.toString());
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
					count++;
					log.info("SYNC save: " + comp.toString());
					log.info("SYNC save: " + newap.toString());
					AppointmentModel.getReference().saveAppt(newap);
				} finally {
					SyncLog.getReference().setProcessUpdates(true);
				}
			}

		}

		return count;

	}

	static public void syncFromServer(CalDavCalendarCollection collection,
			Integer years) throws Exception {

		SocketClient.sendLogMessage("SYNC: Start Incoming Sync");
		log.info("SYNC: Start Incoming Sync");

		Date after = null;
		if (years != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.add(java.util.Calendar.YEAR, -1 * years.intValue());
			after = cal.getTime();
		}

		ArrayList<String> serverUids = new ArrayList<String>();

		Calendar cals[] = collection.getEvents();
		SocketClient.sendLogMessage("SYNC: found " + cals.length
				+ " Event Calendars on server");
		log.info("SYNC: found " + cals.length + " Event Calendars on server");
		int count = 0;
		for (Calendar cal : cals) {
			count += syncCalendar(cal, serverUids);
		}

		SocketClient.sendLogMessage("SYNC: processed " + count
				+ " new/changed Events");

		count = 0;
		Calendar tcals[] = collection.getTasks();
		SocketClient.sendLogMessage("SYNC: found " + tcals.length
				+ " Todo Calendars on server");
		log.info("SYNC: found " + tcals.length + " Todo Calendars on server");
		for (Calendar cal : tcals) {
			count += syncCalendar(cal, serverUids);
		}

		SocketClient.sendLogMessage("SYNC: processed " + count
				+ " new/changed Tasks");

		log.fine(serverUids.toString());

		SocketClient.sendLogMessage("SYNC: check for deletes");
		log.info("SYNC: check for deletes");

		// find all appts in Borg that are not on the server
		for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {
			if (ap.getDate().before(after))
				continue;

			if (!serverUids.contains(ap.getUid())) {
				SocketClient
						.sendLogMessage("Appointment Not Found in Borg - Deleting: "
								+ ap.toString());
				log.info("Appointment Not Found in Borg - Deleting: "
						+ ap.toString());
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
