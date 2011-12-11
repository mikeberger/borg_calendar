package net.sf.borg.plugin.sync.google;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import net.sf.borg.common.Prefs;
import net.sf.borg.common.Warning;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.plugin.sync.AppointmentAdapter;

import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.TextContent;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.Recurrence;
import com.google.gdata.data.extensions.When;

public class GoogleAppointmentAdapter implements
		AppointmentAdapter<CalendarEventEntry> {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
			"yyyyMMdd'T'HHmmss'Z'");

	// magic sequence to distinguish appts created by BORG
	private final static int BORG_SEQUENCE = 999;

	@Override
	public CalendarEventEntry fromBorg(Appointment appt) throws Exception {

		CalendarEventEntry ee = new CalendarEventEntry();

		ee.setTitle(new PlainTextConstruct(getTitle(appt)));
		ee.setContent(new PlainTextConstruct(getBody(appt)));
		ee.setSyncEvent(true);

		// set a unique ical id
		// google seems to require uniqueness even if the id was used by an
		// already deleted appt
		// so add a timestamp
		// the id has the borg key before the @ to be used for syncing later
		long updated = new Date().getTime();
		ee.setIcalUID(Integer.toString(appt.getKey()) + "@BORGCalendar"
				+ updated);
		DateTime updt = new DateTime();
		updt.setValue(updated);
		updt.setTzShift(new Integer(this.tzOffset(updated)));
		ee.setUpdated(updt);

		// we'll use sequence just to distinguish appts that came from borg
		ee.setSequence(BORG_SEQUENCE);

		// build the ugly recurrence rules for repeating appts
		// not everything is supported
		if (Repeat.isRepeating(appt)) {

			String rec = "";
			if (AppointmentModel.isNote(appt)) {
				rec += "DTSTART;VALUE=DATE:"
						+ dateFormat.format(appt.getDate()) + "\n";
				rec += "DTEND;VALUE=DATE:" + dateFormat.format(appt.getDate())
						+ "\n";
			} else {

				long offset = this.tzOffset(appt.getDate().getTime());
				Date adjustedStart = new Date(appt.getDate().getTime() - offset
						* 60 * 1000);
				rec += "DTSTART;VALUE=DATE-TIME:"
						+ dateTimeFormat.format(adjustedStart) + "\n";
				int duration = 30;
				if (appt.getDuration() != null)
					duration = appt.getDuration().intValue();
				long endt = adjustedStart.getTime() + duration * 60 * 1000;
				Date enddate = new Date(endt);
				rec += "DTEND;VALUE=DATE-TIME:"
						+ dateTimeFormat.format(enddate) + "\n";

			}

			// build recur string
			rec += "RRULE:FREQ=";
			String freq = Repeat.getFreq(appt.getFrequency());

			if (freq.equals(Repeat.DAILY)) {
				rec += "DAILY";
			} else if (freq.equals(Repeat.WEEKLY)) {
				rec += "WEEKLY";
			} else if (freq.equals(Repeat.BIWEEKLY)) {
				rec += "WEEKLY;INTERVAL=2";
			} else if (freq.equals(Repeat.MONTHLY)) {
				Date dd = appt.getDate();
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(dd);
				rec += "MONTHLY;BYMONTHDAY=" + gc.get(java.util.Calendar.DATE);
			} else if (freq.equals(Repeat.MONTHLY_DAY)) {
				Date dd = appt.getDate();
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(dd);
				int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK);
				int dayOfWeekMonth = gc.get(Calendar.DAY_OF_WEEK_IN_MONTH);
				String days[] = new String[] { "SU", "MO", "TU", "WE", "TH",
						"FR", "SA" };
				rec += "MONTHLY;BYDAY=" + dayOfWeekMonth + days[dayOfWeek - 1];
			} else if (freq.equals(Repeat.YEARLY)) {
				rec += "YEARLY";
			} else if (freq.equals(Repeat.NDAYS)) {
				rec += "DAILY;INTERVAL=" + Repeat.getNValue(appt.getFrequency());
			} else if (freq.equals(Repeat.NWEEKS)) {
				rec += "WEEKLY;INTERVAL=" + Repeat.getNValue(appt.getFrequency());
			} else if (freq.equals(Repeat.NMONTHS)) {
				rec += "MONTHLY;INTERVAL=" + Repeat.getNValue(appt.getFrequency());
			} else if (freq.equals(Repeat.NYEARS)) {
				rec += "YEARLY;INTERVAL=" + Repeat.getNValue(appt.getFrequency());
			} else if( freq.equals(Repeat.WEEKDAYS)){
				rec += "WEEKLY;BYDAY=MO,TU,WE,TH,FR";
			} else {
				throw new Exception("Appointment " + appt.getText()
						+ " has a recurrence that does not sync with google");
			}

			if (appt.getTimes().intValue() != 9999) {
				rec += ";COUNT=" + Repeat.calculateTimes(appt);
			}
			rec += "\n";

			Recurrence recurrence = new Recurrence();
			recurrence.setValue(rec);
			ee.setRecurrence(recurrence);

		}
		// handle non-repating appts
		else {
			if (!AppointmentModel.isNote(appt)) {
				DateTime startTime = new DateTime(appt.getDate());
				startTime.setTzShift(new Integer(this.tzOffset(appt.getDate()
						.getTime())));
				int duration = 30;
				if (appt.getDuration() != null)
					duration = appt.getDuration().intValue();
				long endt = appt.getDate().getTime() + duration * 60 * 1000;
				DateTime endTime = new DateTime(endt);
				endTime.setTzShift(new Integer(this.tzOffset(endt)));

				When eventTimes = new When();
				eventTimes.setStartTime(startTime);
				eventTimes.setEndTime(endTime);
				ee.addTime(eventTimes);

			} else {
				DateTime startTime = new DateTime(appt.getDate());
				startTime.setDateOnly(true);
				When eventTimes = new When();
				eventTimes.setStartTime(startTime);
				ee.addTime(eventTimes);
			}
		}

		return ee;
	}

	public int getBorgId(CalendarEventEntry entry) {

		String uid = entry.getIcalUID();
		int idx = uid.indexOf('@');
		try {
			String ks = uid.substring(0, idx);
			int key = Integer.parseInt(ks);
			return key;
		} catch (Exception e) {
			// ignore
		}

		return -1;

	}

	@Override
	public Appointment toBorg(CalendarEventEntry extAppt) throws Warning,
			Exception {

		boolean newonly = Prefs.getBoolPref(GoogleSync.NEW_ONLY);
		
		if( newonly && extAppt.getSequence() >= BORG_SEQUENCE)
			throw new Warning("Skipping due to NEWONLY option "
					+ extAppt.getIcalUID());

		Appointment appt = null;
		boolean needs_update = false;

		// handle appts that came from borg unless NEW_ONLY pref is set
		if (extAppt.getSequence() >= BORG_SEQUENCE) {

			// fetch borg appt to update
			String uid = extAppt.getIcalUID();
			int idx = uid.indexOf('@');
			String ks = uid.substring(0, idx);
			try {
				int key = Integer.parseInt(ks);
				appt = AppointmentModel.getReference().getAppt(key);
			} catch (Exception e) {
				// ignore
			}

			// need to update appt if google incremented the sequence
			// This check seems faulty. google seems to update this for
			// appts that did not change. NEW_ONLY pref created to stop this -
			// but can't
			// edit google appts now. Must delete and add back.
			if (extAppt.getSequence() > BORG_SEQUENCE)
				needs_update = true;

		} else {
			// sync all google-created appts
			needs_update = true;
		}

		if (appt == null) {
			// handle event added to google or not in borg for some other reason
			appt = AppointmentModel.getReference().getDefaultAppointment();
			if (appt == null)
				appt = AppointmentModel.getReference().newAppt();
		} 
		
		// convert body and content to borg appt text
		String body = "";
		if (extAppt.getContent() != null
				&& extAppt.getContent() instanceof TextContent) {
			TextContent tc = (TextContent) extAppt.getContent();
			body = tc.getContent().getPlainText();
		}

		// google trims body and title, so we need to consider this when
		// checking for differences
		if (!body.trim().equals(getBody(appt).trim()))
			needs_update = true;

		if (!extAppt.getTitle().getPlainText().trim()
				.equals(getTitle(appt).trim()))
			needs_update = true;

		appt.setText(extAppt.getTitle().getPlainText() + "\n" + body);

		// convert date
		List<When> whens = extAppt.getTimes();
		if (whens == null || whens.isEmpty()) {
			/*
			 * Recurrence rec = extAppt.getRecurrence(); if (rec != null) { //
			 * TODO - handle recurrence String rrules =
			 * getPropertyString("RRULE", rec.getValue()); String dtstarts =
			 * getPropertyString("DTSTART", rec.getValue()); String dtsends =
			 * getPropertyString("DTEND", rec.getValue());
			 * 
			 * DtStart dtstart = new DtStart(dtstarts);
			 * appt.setDate(dtstart.getDate());
			 * 
			 * DtStart dtend = new DtStart(dtsends);
			 * 
			 * // RRULE RRule rrule = new RRule(rrules); Recur recur =
			 * rrule.getRecur();
			 * 
			 * if( appt.getRepeatUntil().getTime() !=
			 * recur.getUntil().getTime()) needs_update = true;
			 * appt.setRepeatUntil(recur.getUntil()); String freq = Repeat.ONCE;
			 * if (recur.getFrequency().equals(Recur.DAILY)) freq =
			 * Repeat.DAILY; else if (recur.getFrequency().equals(Recur.WEEKLY))
			 * freq = Repeat.WEEKLY; else if
			 * (recur.getFrequency().equals(Recur.MONTHLY)) freq =
			 * Repeat.MONTHLY; else if
			 * (recur.getFrequency().equals(Recur.YEARLY)) freq = Repeat.YEARLY;
			 * 
			 * if( !freq.equals(appt.getFrequency())) needs_update = true;
			 * appt.setFrequency(freq);
			 * 
			 * System.err.println("***" + rrules + "***");
			 * System.err.println(rec.getValue()); }
			 */
			throw new Warning("Appointment " + appt.getText() + " "
					+ appt.getDate() + " recurs cannot sync...");
		}
		When when = whens.get(0);
		DateTime start = when.getStartTime();
		DateTime end = when.getEndTime();

		if (start.isDateOnly() && !AppointmentModel.isNote(appt))
			needs_update = true;
		else if (!start.isDateOnly() && AppointmentModel.isNote(appt))
			needs_update = true;
		else if (!start.isDateOnly()
				&& Math.abs(start.getValue() - appt.getDate().getTime()) > 1000 * 60 * 3)
			needs_update = true;
		else if (start.isDateOnly()) {
			long offset = this.tzOffset(appt.getDate().getTime()) * 1000 * 60;
			if (Math.abs(start.getValue() - appt.getDate().getTime()
					- offset) > 1000 * 60 * 3)
				needs_update = true;
		}

		// not sure why non-timed need this...
		if (start.isDateOnly()) {
			appt.setDate(new Date(start.getValue()
					- TimeZone.getDefault().getOffset(start.getValue())));
		} else {
			appt.setDate(new Date(start.getValue()));
		}

		Integer dur = appt.getDuration();
		String ut = appt.getUntimed();

		if (start.isDateOnly()) {
			appt.setUntimed("Y");
		} else {
			appt.setUntimed("N");
			long mins = (end.getValue() - start.getValue()) / 1000 / 60;
			if (mins > 0) {
				appt.setDuration(new Integer((int) mins));
				if (dur != null && dur.intValue() != mins)
					needs_update = true;

			}
		}

		if (ut != null && !ut.equals(appt.getUntimed()))
			needs_update = true;

		// all other google properties are ignored !!!

		// should always need an update if we are here. Google or Itouch keeps
		// changing
		// the dates on all appts for no reason, so need this extra checking
		if (!needs_update)
			throw new Warning("No Update needed for " + extAppt.getIcalUID());

		return appt;
	}

	private int tzOffset(long date) {
		return TimeZone.getDefault().getOffset(date) / (60 * 1000);
	}

	/**
	 * return true if the appt was created by BORG based on sequence and UID
	 */
	static boolean isFromBORG(CalendarEventEntry event) {
		if (event.getSequence() >= BORG_SEQUENCE && event.getIcalUID() != null
				&& event.getIcalUID().contains("BORGCalendar"))
			return true;
		return false;
	}

	private String getBody(Appointment appt) {
		// convert borg text to title and content
		String t = appt.getText();
		String body = "";
		if (t == null)
			t = "";
		int newlineIndex = t.indexOf('\n');
		if (newlineIndex != -1) {
			body = t.substring(newlineIndex + 1);
		}

		if (body == null || body.isEmpty())
			body = "No Content";

		return body;

	}

	private String getTitle(Appointment appt) {
		// convert borg text to title and content
		String t = appt.getText();
		String title = "";
		if (t == null)
			t = "";
		int newlineIndex = t.indexOf('\n');
		if (newlineIndex != -1) {
			title = t.substring(0, newlineIndex);
		} else {
			title = t;
		}

		return title;

	}

	// private String getPropertyString(String property, String blob) {
	// int rrs = blob.indexOf(property);
	// int rre = blob.indexOf("\n", rrs);
	// return blob.substring(rrs, rre);
	// }
}
