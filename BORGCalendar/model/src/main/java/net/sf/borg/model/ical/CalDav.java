package net.sf.borg.model.ical;

import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;
import net.fortuna.ical4j.connector.dav.PathResolver;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.CompatibilityHints;

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

	static public void export(Integer years) throws Exception {
		
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
		Calendar calendar = AppointmentIcalAdapter.exportIcal(after);

		URL url = new URL("http", "192.168.1.20", -1, "/");
		CalDavCalendarStore store = new CalDavCalendarStore("-", url,
				new BaikalPathResolver());

		store.connect("mike", "****".toCharArray());

		List<CalDavCalendarCollection> collections = store.getCollections();
		if (collections.isEmpty())
			return;

		ComponentList clist = calendar.getComponents();
		Iterator<Component> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();

			Calendar mycal = new Calendar();
			mycal.getProperties().add(new ProdId("-//MBCSoft/BORG//EN"));
			mycal.getProperties().add(Version.VERSION_2_0);

			mycal.getComponents().add(comp);

			System.out.println(comp.getProperty("SUMMARY"));
			try {
				collections.get(0).merge(mycal);
			} catch (Exception e) {
				System.out.println(e.toString());
			}

		}

	}

}
