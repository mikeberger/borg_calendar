package net.sf.borg.plugin.sync.google;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

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
		updt.setTzShift(this.tzOffset(updated));
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
				int dayOfWeek = gc.get(GregorianCalendar.DAY_OF_WEEK);
				int dayOfWeekMonth = gc
						.get(GregorianCalendar.DAY_OF_WEEK_IN_MONTH);
				String days[] = new String[] { "SU", "MO", "TU", "WE", "TH",
						"FR", "SA" };
				rec += "MONTHLY;BYDAY=" + dayOfWeekMonth + days[dayOfWeek - 1];
			} else if (freq.equals(Repeat.YEARLY)) {
				rec += "YEARLY";
			} else if (freq.equals(Repeat.NDAYS)) {
				rec += "DAILY;INTERVAL=" + Repeat.getNDays(appt.getFrequency());
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
				startTime.setTzShift(this.tzOffset(appt.getDate().getTime()));
				int duration = 30;
				if (appt.getDuration() != null)
					duration = appt.getDuration().intValue();
				long endt = appt.getDate().getTime() + duration * 60 * 1000;
				DateTime endTime = new DateTime(endt);
				endTime.setTzShift(this.tzOffset(endt));

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
		String ks = uid.substring(0, idx);
		try {
			int key = Integer.parseInt(ks);
			return key;
		} catch (Exception e) {
		}

		return -1;

	}

	@Override
	public Appointment toBorg(CalendarEventEntry extAppt) throws Exception {

		Appointment appt = null;
		boolean needs_update = false;

		// handle appts that came from borg
		if (extAppt.getSequence() >= BORG_SEQUENCE) {

			// fetch borg appt to update
			String uid = extAppt.getIcalUID();
			int idx = uid.indexOf('@');
			String ks = uid.substring(0, idx);
			try {
				int key = Integer.parseInt(ks);
				appt = AppointmentModel.getReference().getAppt(key);
			} catch (Exception e) {
			}

			// need to update appt if google incremented the sequence
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
		if (whens == null) {
			throw new Exception("Appointment " + appt.getText()
					+ " has no event times (recurs?) cannot sync...");
		}
		When when = whens.get(0);
		DateTime start = when.getStartTime();
		DateTime end = when.getEndTime();

		if( start.isDateOnly() && !AppointmentModel.isNote(appt))
			needs_update = true;
		else if (!start.isDateOnly() && AppointmentModel.isNote(appt))
			needs_update = true;
		else if (!start.isDateOnly() && Math.abs(start.getValue() - appt.getDate().getTime()) > 1000 * 60 * 3)
			needs_update = true;
		else if( start.isDateOnly())
		{
			long offset = this.tzOffset(appt.getDate().getTime())*1000*60;
			if (Math.abs(start.getValue() - appt.getDate().getTime() - offset) > 1000 * 60 * 3)
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

		// System.out.println(appt.getText() + " " +
		// DateFormat.getDateTimeInstance().format(appt.getDate()) + " " +
		// start.getTzShift());

		if (start.isDateOnly()) {
			appt.setUntimed("Y");
		} else {
			appt.setUntimed("N");
			long mins = (end.getValue() - start.getValue()) / 1000 / 60;
			if (mins > 0) {
				appt.setDuration((int) mins);
				if (dur != null && dur.intValue() != mins)
					needs_update = true;

			}
		}

		Recurrence rec = extAppt.getRecurrence();
		if (rec != null)
			System.out.println(appt.getText() + ":" + rec.getValue());

		if (ut != null && !ut.equals(appt.getUntimed()))
			needs_update = true;

		// no recurrence for now

		// all other google properties are ignored !!!

		// should always need an update if we are here. Google or Itouch keeps
		// changing
		// the dates on all appts for no reason, so need this extra checking
		if (!needs_update)
			throw new Exception("No Update needed for " + extAppt.getIcalUID());

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
}
