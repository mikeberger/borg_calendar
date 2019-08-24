package net.sf.borg.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Test;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.util.CompatibilityHints;

public class IcalTest {

	@Test
	public void testParse() throws Exception {
		System.setProperty("net.fortuna.ical4j.timezone.cache.impl", "net.fortuna.ical4j.util.MapTimeZoneCache");
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
		InputStream fin = IcalTest.class.getResourceAsStream("/ap1.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(fin);
		for (Component comp : calendar.getComponents()) {
			VEvent event = (VEvent) comp;
			// Works fine
			Summary desc = event.getSummary();
			assertNotNull(desc);

			ComponentList<VAlarm> alarms = event.getAlarms();
			assertTrue(alarms.size() > 0);
			
			Property p = event.getProperty("duh");
			assertNull(p);
			
			String evstring = event.toString();
			System.out.println(evstring);

		}
	}

}
