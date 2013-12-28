package net.sf.borg.model.ical;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import biz.source_code.base64Coder.Base64Coder;
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
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model.ChangeEvent.ChangeAction;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.ical.SyncEvent.ObjectType;

public class CalDav {

	static private final String CAL_NAME = "default";
	static private final String PRODID = "-//MBCSoft/BORG//EN";

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

	static public void sync(Integer years, boolean outward_only) throws Exception {
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

		CalDavCalendarCollection collection = getCollection(store);


		processSyncMap(collection);

		if( !outward_only )
			syncFromServer(collection, years);
		
		log.info("SYNC: Done");


	}

	static public void syncFromServer(CalDavCalendarCollection collection, Integer years)
			throws Exception {

		log.info("SYNC: Start Server Sync");
		
		Date after = null;
		if (years != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.add(java.util.Calendar.YEAR, -1 * years.intValue());
			after = cal.getTime();
		}
		
		ArrayList<String> serverUids = new ArrayList<String>();

		Calendar cals[] = collection.getEvents();
		
		log.info("SYNC: found " + cals.length + " Calendars");
		for (Calendar cal : cals) {


			ComponentList clist = cal.getComponents();
			@SuppressWarnings("unchecked")
			Iterator<Component> it = clist.iterator();
			while (it.hasNext()) {
				Component comp = it.next();
				
				if( !(comp instanceof VEvent || comp instanceof VToDo))
					continue;
				String uid = comp.getProperty(Property.UID).getValue();
				serverUids.add(uid);

				Appointment newap = AppointmentIcalAdapter.toBorg(comp);
				if( newap == null ) continue;

				Appointment ap = AppointmentModel.getReference().getApptByUid(
						uid);
				if (ap == null) {
					// not found in BORG, so add it
					if (newap != null) {
						try {
							SyncLog.getReference().setProcessUpdates(false);
							AppointmentModel.getReference().saveAppt(newap);
						} finally {
							SyncLog.getReference().setProcessUpdates(true);
						}
					}
				}
				else if( newap.getLastMod().after(ap.getLastMod()))
				{
					// was updated after BORG so update BORG
					try {
						newap.setKey(ap.getKey());
						SyncLog.getReference().setProcessUpdates(false);
						AppointmentModel.getReference().saveAppt(newap);
					} finally {
						SyncLog.getReference().setProcessUpdates(true);
					}
				}

			}
			
			
		}
		
		log.fine(serverUids.toString());
		
		log.info("SYNC: check for deletes");

		// find all appts in Borg that are not on the server - TODO - dangerous
		for( Appointment ap : AppointmentModel.getReference().getAllAppts())
		{
			if( ap.getDate().before(after))
				continue;
			
			if( !serverUids.contains(ap.getUid()))
				log.info("Appointment Not Found on Server: " + ap.toString());
		}

	}

	static public void processSyncMap(CalDavCalendarCollection collection)
			throws Exception {

		log.info("SYNC: Process Outgoing Items");

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
					log.info("Deleted Appt: " + se.getUid() + " not found on server");
					if (comp != null) {
						collection.removeCalendar(se.getUid());
					}
				}

				SyncLog.getReference().delete(se.getId(), se.getObjectType());
			} catch (Exception e) {
				log.severe("SYNC ERROR for: " + se.toString() + ":"
						+ e.getMessage());
				e.printStackTrace();
			}
		}
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

		CalDavCalendarStore store = connect();
		if (store == null)
			throw new Exception("Failed to connect to CalDav Store");

		String cal_id = new BaikalPathResolver().getUserPath(Prefs.getPref(PrefName.CALDAV_USER)) + "/"
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
		String cal_id = new BaikalPathResolver().getUserPath(Prefs.getPref(PrefName.CALDAV_USER)) + "/"
				+ CAL_NAME;
		return store.getCollection(cal_id);
	}

	private static CalDavCalendarStore connect() throws Exception {
		
		URL url = new URL("http", Prefs.getPref(PrefName.CALDAV_SERVER), -1, "/");
		log.info("SYNC: connect to " + url.toString());

		CalDavCalendarStore store = new CalDavCalendarStore("-", url,
				new BaikalPathResolver());

		if (store.connect(Prefs.getPref(PrefName.CALDAV_USER), gep().toCharArray()))
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
		if( cal == null) return null;

		ComponentList clist = cal.getComponents();
		@SuppressWarnings("unchecked")
		Iterator<Component> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();
			if (comp instanceof VEvent || comp instanceof VToDo)
				return comp;
		}
		return null;
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
		Prefs.putPref(PrefName.CALDAV_PASSWORD, new String(Base64Coder.encode(ba)));
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


}
