package net.sf.borg.plugin.sync.google;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

	@Override
	public CalendarEventEntry fromBorg(Appointment appt) {
		
		CalendarEventEntry ee = new CalendarEventEntry();
		String t = appt.getText();
		String title = "";
		String body = "";
		if (t == null)
			t = "";
		int newlineIndex = t.indexOf('\n');
		if (newlineIndex != -1) {
			title = t.substring(0, newlineIndex);
			body = t.substring(newlineIndex + 1);
		} else {
			title = t;
		}
		
		ee.setTitle(new PlainTextConstruct(title));
		ee.setContent(new PlainTextConstruct(body));
		ee.setIcalUID(Integer.toString(appt.getKey()) + "@BORGCalendar" + new Date().getTime());
		ee.setSequence(1);
		//ee.setSyncEvent(true);
		
		if (appt.getRepeatFlag() && appt.getFrequency() != null) {
			
			String rec = "";
			if( AppointmentModel.isNote(appt))
			{
				rec += "DTSTART;VALUE=DATE:" + dateFormat.format(appt.getDate()) + "\n";
				rec += "DTEND;VALUE=DATE:" + dateFormat.format(appt.getDate()) + "\n";
			}
			else
			{
				
				Date adjustedStart = new Date(appt.getDate().getTime() + 4*60*60*1000);
				rec += "DTSTART;VALUE=DATE-TIME:" + dateTimeFormat.format(adjustedStart) + "\n";
				int duration = 30;
				if( appt.getDuration() != null )
					duration = appt.getDuration().intValue();
				long endt = adjustedStart.getTime() + duration * 60 * 1000;	
				Date enddate = new Date(endt);
				rec += "DTEND;VALUE=DATE-TIME:" + dateTimeFormat.format(enddate) + "\n";

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
				rec += "MONTHLY;BYMONTHDAY="
						+ gc.get(java.util.Calendar.DATE);
			} else if (freq.equals(Repeat.MONTHLY_DAY)) {
				Date dd = appt.getDate();
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(dd);
				int dayOfWeek = gc.get(GregorianCalendar.DAY_OF_WEEK);
	            int dayOfWeekMonth = gc.get(GregorianCalendar.DAY_OF_WEEK_IN_MONTH);
				String days[] = new String[]{"SU", "MO", "TU", "WE", "TH", "FR", "SA"};
	            rec += "MONTHLY;BYDAY=" + dayOfWeekMonth + days[dayOfWeek-1];
			} else if (freq.equals(Repeat.YEARLY)) {
				rec += "YEARLY";
			} else if( freq.equals(Repeat.NDAYS)) {
				rec += "DAILY;INTERVAL=" + Repeat.getNDays(appt.getFrequency());
			} else {
				rec += "DAILY";
			}

			if (appt.getTimes().intValue() != 9999) {
				rec += ";COUNT=" + appt.getTimes();
			}
			rec += "\n";
			
			//System.out.println(appt.getText() + "--" + rec);
			
			Recurrence recurrence = new Recurrence();
			recurrence.setValue(rec);
			ee.setRecurrence(recurrence);
			
		}
		else
		{
			if( !AppointmentModel.isNote(appt))
			{
				DateTime startTime = new DateTime(appt.getDate());
				startTime.setTzShift(5*60);
				int duration = 30;
				if( appt.getDuration() != null )
					duration = appt.getDuration().intValue();
				long endt = appt.getDate().getTime() + duration * 60 * 1000;		
				DateTime endTime = new DateTime(endt);
				endTime.setTzShift(5*60);

				When eventTimes = new When();
				eventTimes.setStartTime(startTime);
				eventTimes.setEndTime(endTime);
				ee.addTime(eventTimes);

			}
			else
			{
				DateTime startTime = new DateTime(appt.getDate());
				startTime.setDateOnly(true);
				When eventTimes = new When();
				eventTimes.setStartTime(startTime);
				ee.addTime(eventTimes);
			}
		}

		return ee;
	}

	@Override
	// does not handle deleting from google
	public Appointment toBorg(CalendarEventEntry extAppt) {
		Appointment appt = null;
		
		if( extAppt.getSequence() == 0)
		{
			// added to google - not in borg
			appt = AppointmentModel.getReference().getDefaultAppointment();
			if( appt == null )
				appt = AppointmentModel.getReference().newAppt();
			
			String body = "";
			if( extAppt.getContent() != null && extAppt.getContent() instanceof TextContent)
			{
				TextContent tc = (TextContent) extAppt.getContent();
				body = tc.getContent().getPlainText();
				
			}
			appt.setText(extAppt.getTitle().getPlainText() + "\n" + body);
			List<When> whens = extAppt.getTimes();
			When when = whens.get(0);
			DateTime start = when.getStartTime();
			DateTime end = when.getEndTime();
			appt.setDate(new Date(start.getValue()));
			if( start.isDateOnly())
			{
				appt.setUntimed("Y");
			}
		}
		else
		{
			// borg appt that was changed in google
		}
		
		return appt;
	}

}
