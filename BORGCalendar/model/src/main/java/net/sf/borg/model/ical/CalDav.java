package net.sf.borg.model.ical;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
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

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;

import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;
import net.fortuna.ical4j.connector.dav.PathResolver;
import net.fortuna.ical4j.connector.dav.PathResolver.GenericPathResolver;
import net.fortuna.ical4j.connector.dav.property.CSDavPropertyName;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.SocketClient;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.Model.ChangeEvent.ChangeAction;
import net.sf.borg.model.OptionModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Option;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.SyncableEntity;
import net.sf.borg.model.entity.SyncableEntity.ObjectType;
import net.sf.borg.model.entity.Task;

public class CalDav {

	private static final String CTAG_OPTION = "CTAG";

	static private final String PRODID = "-//MBCSoft/BORG//EN";

	static private final int REMOTE_ID = 1;

	static private final Logger log = Logger.getLogger("net.sf.borg");

	public static boolean isSyncing() {
		String server = Prefs.getPref(PrefName.CALDAV_SERVER);
		if (server != null && !server.isEmpty())
			return true;
		return false;
	}

	private static PathResolver createPathResolver() {
		GenericPathResolver pathResolver = new GenericPathResolver();
		String basePath = Prefs.getPref(PrefName.CALDAV_PATH);
		if (!basePath.endsWith("/"))
			basePath += "/";
		pathResolver.setPrincipalPath(basePath + Prefs.getPref(PrefName.CALDAV_PRINCIPAL_PATH));
		pathResolver.setUserPath(basePath + Prefs.getPref(PrefName.CALDAV_USER_PATH));
		return pathResolver;
	}

	private static void addEvent(CalDavCalendarCollection collection, CalendarComponent comp) {

		log.info("SYNC: addEvent: " + comp.toString());

		Calendar mycal = new Calendar();
		mycal.getProperties().add(new ProdId(PRODID));
		mycal.getProperties().add(Version.VERSION_2_0);
		mycal.getComponents().add(comp);
		Property url = comp.getProperty(Property.URL);
		String urlValue = null;
		if (url != null)
			urlValue = url.getValue();

		try {
			if (urlValue != null)
				collection.addCalendar(urlValue, mycal);
			else
				collection.addCalendar(mycal);
		} catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static CalDavCalendarStore connect() throws Exception {

		if (!isSyncing())
			return null;

		//Prefs.setProxy();

		if (Prefs.getBoolPref(PrefName.CALDAV_ALLOW_SELF_SIGNED_CERT)) {
			// Allow access even though certificate is self signed
			Protocol lEasyHttps = new Protocol("https", new EasySslProtocolSocketFactory(), 443);
			Protocol.registerProtocol("https", lEasyHttps);
		} else {
			Protocol sslprot = new Protocol("https", new SSLProtocolSocketFactory(), 443);
			Protocol.registerProtocol("https", sslprot);
		}

		String protocol = Prefs.getBoolPref(PrefName.CALDAV_USE_SSL) ? "https" : "http";

		String server = Prefs.getPref(PrefName.CALDAV_SERVER);
		String serverPart[] = server.split(":");
		int port = -1;
		if (serverPart.length == 2) {
			try {
				port = Integer.parseInt(serverPart[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		URL url = new URL(protocol, serverPart[0], port, Prefs.getPref(PrefName.CALDAV_PATH));
		SocketClient.sendLogMessage("SYNC: connect to " + url.toString());
		log.info("SYNC: connect to " + url.toString());

		CalDavCalendarStore store = new CalDavCalendarStore("-", url, createPathResolver());

		if (store.connect(Prefs.getPref(PrefName.CALDAV_USER), gep().toCharArray()))
			return store;

		return null;
	}

	static public synchronized void export(Integer years) throws Exception {

		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);

		Date after = null;
		if (years != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.add(java.util.Calendar.YEAR, -1 * years.intValue());
			after = cal.getTime();
		}
		Calendar calendar = ICal.exportIcal(after, true);

		String calname = Prefs.getPref(PrefName.CALDAV_CAL);

		CalDavCalendarStore store = connect();
		if (store == null)
			throw new Exception("Failed to connect to CalDav Store");

		String cal_id = createPathResolver().getUserPath(Prefs.getPref(PrefName.CALDAV_USER)) + "/" + calname;
		try {
			store.removeCollection(cal_id);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}

		CalDavCalendarCollection collection = store.addCollection(cal_id, calname, calname,
				new String[] { "VEVENT", "VTODO" }, null);

		ComponentList<CalendarComponent> clist = calendar.getComponents();
		Iterator<CalendarComponent> it = clist.iterator();
		while (it.hasNext()) {
			CalendarComponent comp = it.next();
			addEvent(collection, comp);
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

		byte[] ba = Base64.getDecoder().decode(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher dec = Cipher.getInstance("AES");
		dec.init(Cipher.DECRYPT_MODE, key);
		byte[] decba = Base64.getDecoder().decode(p2);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, dec);
		os.write(decba);
		os.close();

		return baos.toString();

	}

	public static CalDavCalendarCollection getCollection(CalDavCalendarStore store, String calName) throws Exception {
		String cal_id = createPathResolver().getUserPath(Prefs.getPref(PrefName.CALDAV_USER)) + "/" + calName;
		return store.getCollection(cal_id);
	}

	private static Component getEvent(CalDavCalendarCollection collection, SyncEvent se) {
		
		Calendar cal = null;
		if( se.getUrl() != null && !se.getUrl().isEmpty())
			cal = collection.getCalendarFromUri(se.getUrl());
		else
			cal = collection.getCalendar(se.getUid());
		if (cal == null)
			return null;

		ComponentList<CalendarComponent> clist = cal.getComponents();
		Iterator<CalendarComponent> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();
			if (comp instanceof VEvent || comp instanceof VToDo)
				return comp;
		}
		return null;
	}

	static private void processSyncMap(CalDavCalendarCollection collection) throws Exception {

		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		List<SyncEvent> syncEvents = SyncLog.getReference().getAll();

		int num_outgoing = syncEvents.size();
		if (isServerSyncNeeded())
			num_outgoing--;

		SocketClient.sendLogMessage("SYNC: Process " + num_outgoing + " Outgoing Items");
		log.info("SYNC: Process " + num_outgoing + " Outgoing Items");

		for (SyncEvent se : syncEvents) {
			if (se.getObjectType() == ObjectType.APPOINTMENT) {

				try {
					if (se.getAction().equals(ChangeAction.ADD)) {
						Appointment ap = AppointmentModel.getReference().getAppt(se.getId());
						if (ap == null)
							continue;
						CalendarComponent ve1 = EntityIcalAdapter.toIcal(ap, export_todos);
						if (ve1 != null)
							addEvent(collection, ve1);

					} else if (se.getAction().equals(ChangeAction.CHANGE)) {
						Component comp = getEvent(collection, se);
						Appointment ap = AppointmentModel.getReference().getAppt(se.getId());

						if (comp == null) {
							CalendarComponent ve1 = EntityIcalAdapter.toIcal(ap, export_todos);
							if (ve1 != null)
								addEvent(collection, ve1);

						} else // TODO - what if both sides updated
						{

							CalendarComponent ve1 = EntityIcalAdapter.toIcal(ap, export_todos);
							if (ve1 != null)
								updateEvent(collection, ve1);

						}
					} else if (se.getAction().equals(ChangeAction.DELETE)) {

						Component comp = getEvent(collection, se);

						if (comp != null) {
							log.info("SYNC: removeEvent: " + comp.toString());
							removeEvent(collection, se);

						} else {
							log.info("Deleted Appt: " + se.getUid() + " not found on server");
						}
					}

					SyncLog.getReference().delete(se.getId(), se.getObjectType());
				} catch (Exception e) {
					SocketClient.sendLogMessage("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
					log.severe("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
					e.printStackTrace();
				}
			} else if (se.getObjectType() == ObjectType.TASK) {
				try {
					if (se.getAction().equals(ChangeAction.ADD)) {
						Task task = TaskModel.getReference().getTask(se.getId());
						if (task == null)
							continue;
						CalendarComponent ve1 = EntityIcalAdapter.toIcal(task, export_todos);
						if (ve1 != null)
							addEvent(collection, ve1);

					} else if (se.getAction().equals(ChangeAction.CHANGE)) {
						Component comp = getEvent(collection, se);
						Task task = TaskModel.getReference().getTask(se.getId());

						if (comp == null) {
							CalendarComponent ve1 = EntityIcalAdapter.toIcal(task, export_todos);
							if (ve1 != null)
								addEvent(collection, ve1);

						} else // TODO - what if both sides updated
						{
							CalendarComponent ve1 = EntityIcalAdapter.toIcal(task, export_todos);
							if (ve1 != null) {
								updateEvent(collection, ve1);
							} else {
								removeEvent(collection, se);

							}
						}
					} else if (se.getAction().equals(ChangeAction.DELETE)) {

						Component comp = getEvent(collection, se);

						if (comp != null) {
							log.info("SYNC: removeEvent: " + comp.toString());
							removeEvent(collection, se);

						} else {
							log.info("Deleted Appt: " + se.getUid() + " not found on server");
						}
					}

					SyncLog.getReference().delete(se.getId(), se.getObjectType());
				} catch (Exception e) {
					SocketClient.sendLogMessage("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
					log.severe("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
					e.printStackTrace();
				}
			} else if (se.getObjectType() == ObjectType.SUBTASK) {
				try {
					if (se.getAction().equals(ChangeAction.ADD)) {
						Subtask subtask = TaskModel.getReference().getSubTask(se.getId());
						if (subtask == null)
							continue;
						CalendarComponent ve1 = EntityIcalAdapter.toIcal(subtask, export_todos);
						if (ve1 != null)
							addEvent(collection, ve1);

					} else if (se.getAction().equals(ChangeAction.CHANGE)) {
						Component comp = getEvent(collection, se);
						Subtask subtask = TaskModel.getReference().getSubTask(se.getId());

						if (comp == null) {
							CalendarComponent ve1 = EntityIcalAdapter.toIcal(subtask, export_todos);
							if (ve1 != null)
								addEvent(collection, ve1);

						} else // TODO - what if both sides updated
						{
							CalendarComponent ve1 = EntityIcalAdapter.toIcal(subtask, export_todos);
							if (ve1 != null) {
								updateEvent(collection, ve1);
							} else {
								removeEvent(collection, se);

							}
						}
					} else if (se.getAction().equals(ChangeAction.DELETE)) {

						Component comp = getEvent(collection, se);

						if (comp != null) {
							log.info("SYNC: removeEvent: " + comp.toString());
							removeEvent(collection, se);

						} else {
							log.info("Deleted Appt: " + se.getUid() + " not found on server");
						}
					}

					SyncLog.getReference().delete(se.getId(), se.getObjectType());
				} catch (Exception e) {
					SocketClient.sendLogMessage("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
					log.severe("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
					e.printStackTrace();
				}
			}
		}

	}
	
	static private void removeEvent(CalDavCalendarCollection collection, SyncEvent se) throws Exception {
		if( se.getUrl() != null && !se.getUrl().isEmpty())
			collection.removeCalendarFromUri(se.getUrl());
		else
			collection.removeCalendar(se.getUid());
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
			p1 = new String(Base64.getEncoder().encode(key.getEncoded()));
			Prefs.putPref(PrefName.CALDAV_PASSWORD2, p1);
		}

		byte[] ba = Base64.getDecoder().decode(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, enc);
		os.write(s.getBytes());
		os.close();
		ba = baos.toByteArray();
		Prefs.putPref(PrefName.CALDAV_PASSWORD, new String(Base64.getEncoder().encode(ba)));
	}

	/**
	 * check remote server to see if sync needed - must not be run on Event thread
	 *
	 * @throws Exception
	 */
	static public synchronized boolean checkRemoteSync() throws Exception {
		CalDavCalendarStore store = connect();
		if (store == null)
			throw new Exception("Failed to connect to CalDav Store");

		log.info("SYNC: Get Collection");

		String calname = Prefs.getPref(PrefName.CALDAV_CAL);

		CalDavCalendarCollection collection = getCollection(store, calname);

		String ctag = collection.getProperty(CSDavPropertyName.CTAG, String.class);
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

		SyncEvent ev = SyncLog.getReference().get(REMOTE_ID, SyncableEntity.ObjectType.REMOTE);
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
					new SyncEvent(REMOTE_ID, "", "", ChangeEvent.ChangeAction.CHANGE, SyncableEntity.ObjectType.REMOTE));
		else
			SyncLog.getReference().delete(REMOTE_ID, SyncableEntity.ObjectType.REMOTE);

	}

	static public synchronized void sync(Integer years, boolean outward_only) throws Exception {
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
		
		System.setProperty("net.fortuna.ical4j.timezone.cache.impl", "net.fortuna.ical4j.util.MapTimeZoneCache");

		CalDavCalendarStore store = connect();
		if (store == null)
			throw new Exception("Failed to connect to CalDav Store");

		log.info("SYNC: Get Collection");

		String calname = Prefs.getPref(PrefName.CALDAV_CAL);

		CalDavCalendarCollection collection = getCollection(store, calname);

		String ctag = collection.getProperty(CSDavPropertyName.CTAG, String.class);
		log.info("SYNC: CTAG=" + ctag);

		boolean incoming_changes = true;

		String lastCtag = OptionModel.getReference().getOption(CTAG_OPTION);
		if (lastCtag != null && lastCtag.equals(ctag))
			incoming_changes = false;

		processSyncMap(collection);

		if (!incoming_changes)
			SocketClient.sendLogMessage("SYNC: no incoming changes\n");

		if (!outward_only && incoming_changes) {
			syncFromServer(collection, years);

			// incoming sync could cause additional outward activity due to borg
			// needing to convert multiple events
			// into one - a limitation of borg
			processSyncMap(collection);
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

	static private void processRecurrence(Component comp, String uid) throws Exception {

		RecurrenceId rid = (RecurrenceId) comp.getProperty(Property.RECURRENCE_ID);

		Appointment ap = AppointmentModel.getReference().getApptByUid(uid);
		if (ap != null) {

			if (comp instanceof VEvent) {
				log.warning("SYNC: ignoring Vevent for single recurrence - cannot process\n" + comp.toString());
				SocketClient.sendLogMessage(
						"SYNC: ignoring Vevent for single recurrence - cannot process\n" + comp.toString());
				return;
			}
			// for a recurrence of a VToDo, we only use the
			// COMPLETED
			// status if present - otherwise, we ignore
			Completed cpltd = (Completed) comp.getProperty(Property.COMPLETED);
			Status stat = (Status) comp.getProperty(Property.STATUS);
			if (cpltd == null && (stat == null || !stat.equals(Status.VTODO_COMPLETED))) {
				log.warning("SYNC: ignoring VToDo for single recurrence - cannot process\n" + comp.toString());
				SocketClient.sendLogMessage(
						"SYNC: ignoring VToDo for single recurrence - cannot process\n" + comp.toString());
				return;
			}

			Date riddate = rid.getDate();

			Date utc = new Date();
			utc.setTime(riddate.getTime());

			// adjust time zone
			if (!rid.isUtc() && !rid.getValue().contains("T")) {
				long u = riddate.getTime() - TimeZone.getDefault().getOffset(riddate.getTime());
				utc.setTime(u);
			}

			Date nt = ap.getNextTodo();
			if (nt == null)
				nt = ap.getDate();
			if (!utc.before(nt)) {
				log.warning("SYNC: completing Todo\n" + comp.toString());
				SocketClient.sendLogMessage("SYNC: completing Todo\n" + comp.toString());
				AppointmentModel.getReference().do_todo(ap.getKey(), false, utc);

			}

			// }
		}
	}

	static private int syncCalendar(Calendar cal, ArrayList<String> serverUids) throws Exception {

		int count = 0;

		log.fine("Incoming calendar: " + cal.toString());

		ComponentList<CalendarComponent> clist = cal.getComponents();
		Iterator<CalendarComponent> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();

			if (!(comp instanceof VEvent || comp instanceof VToDo))
				continue;

			// copy Url from calendar into every component
			Property url = comp.getProperty(Property.URL);
			if (url == null) {
				url = cal.getProperty(Property.URL);
				if( url != null )
					comp.getProperties().add(url);
			}

			String uid = comp.getProperty(Property.UID).getValue();

			// ignore incoming tasks
			// TODO - process completion??
			if (uid.contains("BORGT") || uid.contains("BORGS"))
				continue;

			serverUids.add(uid);

			// detect single occurrence
			RecurrenceId rid = (RecurrenceId) comp.getProperty(Property.RECURRENCE_ID);
			if (rid != null) {
				processRecurrence(comp, uid);
				continue;
			}

			log.fine("Incoming event: " + comp.toString());

			Appointment newap = EntityIcalAdapter.toBorg(comp);
			if (newap == null)
				continue;

			Appointment ap = AppointmentModel.getReference().getApptByUid(uid);
			if (ap == null) {
				// not found in BORG, so add it
				try {

					SyncLog.getReference().setProcessUpdates(false);
					count++;
					log.info("SYNC save: " + comp.toString());
					log.info("SYNC save: " + newap.toString());
					AppointmentModel.getReference().saveAppt(newap);
				} finally {
					SyncLog.getReference().setProcessUpdates(true);
				}
			} else if (newap.getLastMod().after(ap.getLastMod())) {
				// was updated after BORG so update BORG

				// check for special case - incoming is repeating todo that is
				// completed
				// if so, then just complete the latest todo instance as android
				// task app can't
				// properly handle recurrence it completes the entire todo
				// instead of one instance.
				if (Repeat.isRepeating(ap) && ap.isTodo() && !newap.isTodo()) {
					count++;
					log.info("SYNC do todo: " + ap.toString());
					AppointmentModel.getReference().do_todo(ap.getKey(), true);
					// don't suppress sync log - need to sync this todo
				} else {

					try {
						newap.setKey(ap.getKey());

						SyncLog.getReference().setProcessUpdates(false);
						count++;
						log.info("SYNC save: " + comp.toString());
						log.info("SYNC save: " + newap.toString());
						AppointmentModel.getReference().saveAppt(newap);
					} finally {
						SyncLog.getReference().setProcessUpdates(true);
					}
				}
			}

		}

		return count;

	}

	static private void syncFromServer(CalDavCalendarCollection collection, Integer years) throws Exception {

		SocketClient.sendLogMessage("SYNC: Start Incoming Sync");
		log.info("SYNC: Start Incoming Sync");

		Date after = null;
		GregorianCalendar gcal = new GregorianCalendar();

		gcal.add(java.util.Calendar.YEAR, -1 * ((years == null) ? 50 : years.intValue()));
		after = gcal.getTime();

		gcal = new GregorianCalendar();
		gcal.add(java.util.Calendar.YEAR, 10);
		Date tenYears = gcal.getTime();

		ArrayList<String> serverUids = new ArrayList<String>();

		net.fortuna.ical4j.model.DateTime dtstart = new net.fortuna.ical4j.model.DateTime(after);
		net.fortuna.ical4j.model.DateTime dtend = new net.fortuna.ical4j.model.DateTime(tenYears);
		log.info("SYNC: " + dtstart.toString() + "--" + dtend.toString());

		Calendar cals[] = collection.getEventsForTimePeriod(dtstart, dtend);
		SocketClient.sendLogMessage("SYNC: found " + cals.length + " Event Calendars on server");
		log.info("SYNC: found " + cals.length + " Event Calendars on server");
		int count = 0;
		for (Calendar cal : cals) {
			count += syncCalendar(cal, serverUids);
		}

		SocketClient.sendLogMessage("SYNC: processed " + count + " new/changed Events");

		count = 0;
		Calendar tcals[] = collection.getTasks();
		SocketClient.sendLogMessage("SYNC: found " + tcals.length + " Todo Calendars on server");
		log.info("SYNC: found " + tcals.length + " Todo Calendars on server");
		for (Calendar cal : tcals) {
			count += syncCalendar(cal, serverUids);
		}

		SocketClient.sendLogMessage("SYNC: processed " + count + " new/changed Tasks");

		log.fine(serverUids.toString());

		SocketClient.sendLogMessage("SYNC: check for deletes");
		log.info("SYNC: check for deletes");

		// find all appts in Borg that are not on the server
		for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {
			if (ap.getDate().before(after))
				continue;

			if (!serverUids.contains(ap.getUid())) {
				SocketClient.sendLogMessage("Appointment Not Found in Borg - Deleting: " + ap.toString());
				log.info("Appointment Not Found in Borg - Deleting: " + ap.toString());
				SyncLog.getReference().setProcessUpdates(false);
				AppointmentModel.getReference().delAppt(ap.getKey());
				SyncLog.getReference().setProcessUpdates(true);
			}
		}

	}

	private static void updateEvent(CalDavCalendarCollection collection, CalendarComponent comp) {

		log.info("SYNC: updateEvent: " + comp.toString());

		Calendar mycal = new Calendar();
		mycal.getProperties().add(new ProdId(PRODID));
		mycal.getProperties().add(Version.VERSION_2_0);
		mycal.getComponents().add(comp);
		Property url = comp.getProperty(Property.URL);
		String urlValue = null;
		if (url != null)
			urlValue = url.getValue();

		try {
			if (urlValue != null)
				collection.updateCalendar(urlValue, mycal);
			else
				collection.updateCalendar(mycal);
			
		} catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
	}

}
