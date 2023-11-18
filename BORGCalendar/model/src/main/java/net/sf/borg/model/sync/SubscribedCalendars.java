package net.sf.borg.model.sync;

import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CalendarEntityProvider;
import net.sf.borg.model.Model;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.Theme;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.LabelEntity;

public class SubscribedCalendars extends Model implements CalendarEntityProvider {

	/** The singleton */
	static private final SubscribedCalendars self_ = new SubscribedCalendars();

	/**
	 * Gets the singleton reference.
	 * 
	 * @return the singleton reference
	 */
	public static SubscribedCalendars getReference() {
		return (self_);
	}

	private HashMap<String, HashMap<Integer, Collection<LabelEntity>>> calmap = new HashMap<String, HashMap<Integer, Collection<LabelEntity>>>();

	public void addEvent(Appointment ap, String calendarId) {

		LabelEntity e = new LabelEntity();
		e.setText(formatText(ap, false));
		e.setTooltipText(formatText(ap, true));
		e.setColor(Theme.HOLIDAYCOLOR);
		e.setDate(ap.getDate());

		HashMap<Integer, Collection<LabelEntity>> daymap = calmap.get(calendarId);
		if (daymap == null) {
			daymap = new HashMap<Integer, Collection<LabelEntity>>();
			calmap.put(calendarId, daymap);
		}

		if (!Repeat.isRepeating(ap)) {

			int dkey = DateUtil.dayOfEpoch(ap.getDate());

			Collection<LabelEntity> day = daymap.get(dkey);
			if (day == null) {
				day = new ArrayList<LabelEntity>();
				daymap.put(dkey, day);
			}

			day.add(e);

		} else {

			GregorianCalendar cal = new GregorianCalendar();
			int curyr = cal.get(Calendar.YEAR);

			// repeats
			cal.setTime(ap.getDate());

			Repeat repeat = new Repeat(cal, ap.getFrequency());

			int tm = Repeat.calculateTimes(ap);

			int apptYear = cal.get(Calendar.YEAR);

			// ok, plod through the repeats now
			for (int i = 0; i < tm; i++) {
				Calendar current = repeat.current();
				if (current == null) {
					repeat.next();
					continue;
				}

				// get the day key for the repeat
				int rkey = DateUtil.dayOfEpoch(current.getTime());

				int cyear = current.get(Calendar.YEAR);

				// limit the repeats to 10 years
				if (cyear > curyr + 10 && cyear > apptYear + 10)
					break;

				Collection<LabelEntity> day = daymap.get(rkey);
				if (day == null) {
					day = new ArrayList<LabelEntity>();
					daymap.put(rkey, day);
				}
				day.add(e);

				repeat.next();
			}
		}

	}

	public void removeCal(String calendarId) {
		calmap.remove(calendarId);
	}

	public void removeCals() {
		calmap.clear();
	}

	@Override
	public List<CalendarEntity> getEntities(Date d) {
		ArrayList<CalendarEntity> l = new ArrayList<CalendarEntity>();
		for (HashMap<Integer, Collection<LabelEntity>> cal : calmap.values()) {
			Collection<LabelEntity> c = cal.get(DateUtil.dayOfEpoch(d));
			if (c != null)
				l.addAll(c);
		}

		return l;
	}

	@Override
	public void export(Writer fw) throws Exception {
		// do not export subscribed cals

	}

	@Override
	public void importXml(InputStream is) throws Exception {
		// do nothing

	}

	@Override
	public String getExportName() {
		return null;
	}

	@Override
	public String getInfo() throws Exception {
		return null;
	}

	public void refresh() {
		super.refreshListeners();
	}

	public String formatText(Appointment appt, boolean force_full_text) {

		if (appt.getText() == null) {
			return "";
		}

		Calendar day = new GregorianCalendar();
		day.setTime(appt.getDate());

		String theFormattedText = "";

		// add time in front of the appt text
		if (!AppointmentModel.isNote(appt)) {
			Date d = appt.getDate();
			if (d != null) {
				SimpleDateFormat sdf = AppointmentModel.getTimeFormat();
				theFormattedText += sdf.format(d) + " ";
			}
		}

		// if the text is empty - skip it - should never be
		String xx = appt.getText();
		String trunc = Prefs.getPref(PrefName.TRUNCAPPT);
		if (!force_full_text && trunc.equals("true")) {
			// !!!!! only show first line of appointment text !!!!!!
			int ii = xx.indexOf('\n');
			if (ii != -1) {
				theFormattedText += xx.substring(0, ii);
			} else {
				theFormattedText += xx;
			}
		} else {
			theFormattedText += xx;
		}

		// add repeat number
		if (Repeat.getRptNum(appt.getFrequency())) {
			theFormattedText += " (" + Repeat.calculateRepeatNumber(day, appt) + ")";
		}

		return theFormattedText;
	}
}
