/*
This file is part of BORG.
 
    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
 
    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
Copyright 2003 by Mike Berger
 */
package net.sf.borg.model.ical;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.IOHelper;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;

public class AppointmentIcalAdapter {

	static private final Logger log = Logger.getLogger("net.sf.borg");

	static public void exportIcalToFile(String filename, Date after)
			throws Exception {
		Calendar cal = exportIcal(after, true);
		OutputStream oostr = IOHelper.createOutputStream(filename);
		CalendarOutputter op = new CalendarOutputter();
		op.output(cal, oostr);
		oostr.close();
	}

	static public String exportIcalToString(Date after) throws Exception {
		Calendar cal = exportIcal(after, true);
		CalendarOutputter op = new CalendarOutputter();
		StringWriter sw = new StringWriter();
		op.output(cal, sw);
		return sw.toString();
	}

	static public Component toIcal(Appointment ap) throws Exception {

		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		TextList catlist = new TextList();
		Component ve = null;

		// export todos as VTODOs if option set
		// This works well in clients such as Mozilla Lightning, which handles
		// VTODOs similar to BORG.
		// On the other hand, VTODOs are not handled well at all by Android
		if (ap.isTodo() && export_todos)
			ve = new VToDo();
		else
			ve = new VEvent();

		String uidval = Integer.toString(ap.getKey()) + "@BORGA-"
				+ ap.getCreateTime().getTime();
		Uid uid = new Uid(uidval);
		ve.getProperties().add(uid);

		ve.getProperties().add(new Created(new DateTime(ap.getCreateTime())));
		ve.getProperties().add(new LastModified(new DateTime(ap.getLastMod())));

		// add text
		String appttext = ap.getText();
		Summary sum = null;
		Description desc = null;

		int ii = appttext.indexOf('\n');
		if (ii != -1) {
			sum = new Summary(appttext.substring(0, ii));
			desc = new Description(appttext.substring(ii + 1));
		} else {
			sum = new Summary(appttext);
		}

		ve.getProperties().add(sum);
		if (desc != null) {
			ve.getProperties().add(desc);
		}

		ParameterList pl = new ParameterList();

		// date
		if (ve instanceof VToDo) {
			// date is the next todo field if present, otherwise
			// the due date
			Date nt = ap.getNextTodo();
			if (nt == null) {
				nt = ap.getDate();
			}

			DtStart dtd = new DtStart();
			dtd.setDate(new net.fortuna.ical4j.model.Date(nt));
			ve.getProperties().add(dtd);
		} else if (AppointmentModel.isNote(ap)) {
			pl.add(Value.DATE);
			DtStart dts = new DtStart(pl, new net.fortuna.ical4j.model.Date(
					ap.getDate()));
			ve.getProperties().add(dts);
			Date end = new Date(ap.getDate().getTime() + 1000 * 60 * 60 * 24);
			DtEnd dte = new DtEnd(pl, new net.fortuna.ical4j.model.Date(end));
			ve.getProperties().add(dte);
		} else {
			pl.add(Value.DATE_TIME);
			DtStart dts = new DtStart(pl,
					new net.fortuna.ical4j.model.DateTime(ap.getDate()));
			dts.setUtc(true);
			ve.getProperties().add(dts);
		}

		// duration
		if (ap.getDuration() != null && ap.getDuration().intValue() != 0) {
			ve.getProperties()
					.add(new Duration(new Dur(0, 0,
							ap.getDuration().intValue(), 0)));
		}

		// vacation is a category
		if (ap.getVacation() != null && ap.getVacation().intValue() != 0) {
			catlist.add("Vacation");
		}

		// holiday is a category
		if (ap.getHoliday() != null && ap.getHoliday().intValue() != 0) {
			catlist.add("Holidays");
		}

		// private
		if (ap.isPrivate()) {
			ve.getProperties().add(Clazz.PRIVATE);
		}

		// add color as a category
		if (ap.getColor() != null
				&& (ap.getColor().equals("black")
						|| ap.getColor().equals("blue")
						|| ap.getColor().equals("green")
						|| ap.getColor().equals("red") || ap.getColor().equals(
						"white"))) {
			catlist.add(ap.getColor());
		}

		if (ap.getCategory() != null && !ap.getCategory().equals("")) {
			catlist.add(ap.getCategory());
		}

		if (!catlist.isEmpty()) {
			ve.getProperties().add(new Categories(catlist));
		}

		// repeat stuff
		if (ap.isRepeatFlag()) {
			// build recur string
			String rec = "FREQ=";
			String freq = Repeat.getFreq(ap.getFrequency());

			if (freq == null || freq.equals(Repeat.DAILY)) {
				rec += "DAILY";
			} else if (freq.equals(Repeat.WEEKLY)) {
				rec += "WEEKLY";
			} else if (freq.equals(Repeat.BIWEEKLY)) {
				rec += "WEEKLY;INTERVAL=2";
			} else if (freq.equals(Repeat.MONTHLY)) {
				Date dd = ap.getDate();
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(dd);
				rec += "MONTHLY;BYMONTHDAY=" + gc.get(java.util.Calendar.DATE);
			} else if (freq.equals(Repeat.MONTHLY_DAY)) {
				Date dd = ap.getDate();
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(dd);
				int dayOfWeek = gc.get(java.util.Calendar.DAY_OF_WEEK);
				int dayOfWeekMonth = gc
						.get(java.util.Calendar.DAY_OF_WEEK_IN_MONTH);
				String days[] = new String[] { "SU", "MO", "TU", "WE", "TH",
						"FR", "SA" };
				rec += "MONTHLY;BYDAY=" + dayOfWeekMonth + days[dayOfWeek - 1];
			} else if (freq.equals(Repeat.YEARLY)) {
				rec += "YEARLY";
			} else if (freq.equals(Repeat.NDAYS)) {
				rec += "DAILY;INTERVAL=" + Repeat.getNValue(ap.getFrequency());
			} else if (freq.equals(Repeat.NWEEKS)) {
				rec += "WEEKLY;INTERVAL=" + Repeat.getNValue(ap.getFrequency());
			} else if (freq.equals(Repeat.NMONTHS)) {
				rec += "MONTHLY;INTERVAL="
						+ Repeat.getNValue(ap.getFrequency());
			} else if (freq.equals(Repeat.NYEARS)) {
				rec += "YEARLY;INTERVAL=" + Repeat.getNValue(ap.getFrequency());
			} else if (freq.equals(Repeat.WEEKDAYS)) {
				rec += "WEEKLY;BYDAY=MO,TU,WE,TH,FR";
			} else if (freq.equals(Repeat.MWF)) {
				rec += "WEEKLY;BYDAY=MO,WE,FR";
			} else if (freq.equals(Repeat.WEEKENDS)) {
				rec += "WEEKLY;BYDAY=SU,SA";
			} else if (freq.equals(Repeat.TTH)) {
				rec += "WEEKLY;BYDAY=TU,TH";
			} else if (freq.equals(Repeat.DAYLIST)) {
				String days[] = new String[] { "SU", "MO", "TU", "WE", "TH",
						"FR", "SA" };
				rec += "WEEKLY;BYDAY=";
				Collection<Integer> c = Repeat.getDaylist(ap.getFrequency());
				Iterator<Integer> it = c.iterator();
				while (it.hasNext()) {
					Integer i = it.next();
					rec += days[i - 1];
					if (it.hasNext())
						rec += ",";
				}

			} else {
				log.warning("Could not export appt " + ap.getKey()
						+ ap.getText());
				return null;
			}

			if (ap.getTimes().intValue() != 9999) {
				rec += ";COUNT=" + Repeat.calculateTimes(ap);
			}
			// System.out.println(rec);

			ve.getProperties().add(new RRule(new Recur(rec)));

		}

		// reminder
		if (!AppointmentModel.isNote(ap) && ap.getReminderTimes() != null
				&& !ap.getReminderTimes().isEmpty()) {

			// add a reminder
			if (ap.getReminderTimes().contains("Y")
					&& (ap.isRepeatFlag() || ap.getDate().after(new Date()))) {
				VAlarm va = new VAlarm(new Dur(0, 0, -1 * 30, 0));
				va.getProperties().add(Action.DISPLAY);
				va.getProperties().add(new Description(ap.getText()));
				va.getProperties().add(
						new net.fortuna.ical4j.model.property.Repeat(2));
				va.getProperties().add(new Duration(new Dur(0, 0, 15, 0)));
				if (ve instanceof VEvent)
					((VEvent) ve).getAlarms().add(va);
				else
					((VToDo) ve).getAlarms().add(va);

			}
		}

		return ve;

	}

	static public Calendar exportIcal(Date after, boolean tasks)
			throws Exception {

		ComponentList clist = new ComponentList();

		exportAppointments(clist, after);
		if (tasks) {
			exportProjects(clist);
			exportTasks(clist);
			exportSubTasks(clist);
		}

		PropertyList pl = new PropertyList();
		pl.add(new ProdId("BORG Calendar"));
		pl.add(Version.VERSION_2_0);
		net.fortuna.ical4j.model.Calendar cal = new net.fortuna.ical4j.model.Calendar(
				pl, clist);

		cal.validate();

		return cal;
	}

	static private void exportAppointments(ComponentList clist, Date after)
			throws Exception {

		for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {

			// limit by date
			if (after != null) {
				Date latestInstance = Repeat.calculateLastRepeat(ap);
				if (latestInstance != null && latestInstance.before(after))
					continue;
			}

			Component ve = toIcal(ap);
			if (ve != null)
				clist.add(ve);

		}

	}

	static private void exportTasks(ComponentList clist) throws Exception {

		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		for (Task t : TaskModel.getReference().getTasks()) {
			if (TaskModel.isClosed(t))
				continue;

			Date due = t.getDueDate();
			if (due == null)
				continue;

			Component ve = null;
			if (export_todos)
				ve = new VToDo();
			else
				ve = new VEvent();

			long updated = new Date().getTime();
			String uidval = Integer.toString(t.getKey()) + "@BORGT" + updated;
			Uid uid = new Uid(uidval);
			ve.getProperties().add(uid);

			// add text
			ve.getProperties().add(new Summary("[T]" + t.getSummary()));
			if (t.getDescription() != null && !t.getDescription().isEmpty())
				ve.getProperties().add(new Description(t.getDescription()));

			ParameterList pl = new ParameterList();
			pl.add(Value.DATE);
			DtStart dts = new DtStart(pl,
					new net.fortuna.ical4j.model.Date(due));
			ve.getProperties().add(dts);

			Date end = new Date(due.getTime() + 1000 * 60 * 60 * 24);
			DtEnd dte = new DtEnd(pl, new net.fortuna.ical4j.model.Date(end));
			ve.getProperties().add(dte);

			clist.add(ve);

		}

	}

	static private void exportProjects(ComponentList clist) throws Exception {

		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		for (Project t : TaskModel.getReference().getProjects()) {
			if (TaskModel.isClosed(t))
				continue;

			Date due = t.getDueDate();
			if (due == null)
				continue;

			Component ve = null;
			if (export_todos)
				ve = new VToDo();
			else
				ve = new VEvent();

			long updated = new Date().getTime();
			String uidval = Integer.toString(t.getKey()) + "@BORGP" + updated;
			Uid uid = new Uid(uidval);
			ve.getProperties().add(uid);

			// add text
			ve.getProperties().add(new Summary("[P]" + t.getDescription()));

			ParameterList pl = new ParameterList();
			pl.add(Value.DATE);
			DtStart dts = new DtStart(pl,
					new net.fortuna.ical4j.model.Date(due));
			ve.getProperties().add(dts);

			Date end = new Date(due.getTime() + 1000 * 60 * 60 * 24);
			DtEnd dte = new DtEnd(pl, new net.fortuna.ical4j.model.Date(end));
			ve.getProperties().add(dte);

			clist.add(ve);

		}
	}

	static private void exportSubTasks(ComponentList clist) throws Exception {

		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		for (Subtask t : TaskModel.getReference().getSubTasks()) {
			if (t.getCloseDate() != null)
				continue;

			Date due = t.getDueDate();
			if (due == null)
				continue;

			Component ve = null;
			if (export_todos)
				ve = new VToDo();
			else
				ve = new VEvent();

			long updated = new Date().getTime();
			String uidval = Integer.toString(t.getKey()) + "@BORGS" + updated;
			Uid uid = new Uid(uidval);
			ve.getProperties().add(uid);

			// add text
			ve.getProperties().add(new Summary("[S]" + t.getDescription()));

			ParameterList pl = new ParameterList();
			pl.add(Value.DATE);
			DtStart dts = new DtStart(pl,
					new net.fortuna.ical4j.model.Date(due));
			ve.getProperties().add(dts);

			Date end = new Date(due.getTime() + 1000 * 60 * 60 * 24);
			DtEnd dte = new DtEnd(pl, new net.fortuna.ical4j.model.Date(end));
			ve.getProperties().add(dte);
			clist.add(ve);

		}
	}

	static public String importIcalFromUrl(String urlString) throws Exception {

		String host = Prefs.getPref(PrefName.PROXY_HOST);
		if (host != null && !host.isEmpty()) {
			System.setProperty("http.proxyHost", host);
			System.setProperty("https.proxyHost", host);

			int port = Prefs.getIntPref(PrefName.PROXY_PORT);
			System.setProperty("http.proxyPort", Integer.toString(port));
			System.setProperty("https.proxyPort", Integer.toString(port));
		} else {
			System.clearProperty("http.proxyHost");
			System.clearProperty("https.proxyHost");
			System.clearProperty("http.proxyPort");
			System.clearProperty("https.proxyPort");
		}

		CalendarBuilder builder = new CalendarBuilder();
		URL url = new URL(urlString);
		InputStream is = url.openStream();
		Calendar cal = builder.build(is);
		is.close();

		return importIcal(cal, null);
	}

	static public String importIcalFromFile(String file, String category)
			throws Exception {
		CalendarBuilder builder = new CalendarBuilder();
		InputStream is = new FileInputStream(file);
		Calendar cal = builder.build(is);
		is.close();

		return importIcal(cal, category);
	}

	@SuppressWarnings("unchecked")
	static private String importIcal(Calendar cal, String category)
			throws Exception {

		boolean skip_borg = Prefs.getBoolPref(PrefName.SKIP_BORG);
		int skipped = 0;
		StringBuffer dups = new StringBuffer();

		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		StringBuffer warning = new StringBuffer();

		try {
			cal.validate();
		} catch (ValidationException e) {
			Errmsg.getErrorHandler().notice(
					"Ical4j validation error: " + e.getLocalizedMessage());
		}

		ArrayList<Appointment> aplist = new ArrayList<Appointment>();

		AppointmentModel amodel = AppointmentModel.getReference();
		ComponentList clist = cal.getComponents();
		Iterator<Component> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();
			if (comp instanceof VEvent || comp instanceof VToDo) {

				Property uidProp = comp.getProperty(Property.UID);
				if (skip_borg && uidProp != null) {
					String uid = uidProp.getValue();
					if (uid.contains("@BORG")) {
						skipped++;
						continue;
					}
				}

				// start with default appt to pull in default options
				Appointment ap = amodel.getDefaultAppointment();
				if (ap == null)
					ap = amodel.newAppt();
				if (category == null || category.equals("")
						|| category.equals(CategoryModel.UNCATEGORIZED)) {
					ap.setCategory(null);
				} else {
					ap.setCategory(category);
				}
				PropertyList pl = comp.getProperties();
				String appttext = "";
				String summary = "";
				Property prop = pl.getProperty(Property.SUMMARY);
				if (prop != null) {
					summary = prop.getValue();
					appttext += prop.getValue();
				}

				prop = pl.getProperty(Property.LOCATION);
				if (prop != null) {
					appttext += "\nLocation: " + prop.getValue();
				}

				prop = pl.getProperty(Property.DESCRIPTION);
				if (prop != null) {
					appttext += "\n" + prop.getValue();
				}

				ap.setUntimed("Y");
				ap.setText(appttext);
				prop = pl.getProperty(Property.DTSTART);
				if (prop != null) {
					DtStart dts = (DtStart) prop;
					Date d = dts.getDate();
					// System.out.println("utc=" + dts.isUtc());
					// System.out.println("dt=" +
					// DateFormat.getDateTimeInstance().format(d));
					// System.out.println("val=" + dts.getValue());

					Date utc = new Date();
					utc.setTime(d.getTime());

					// adjust time zone
					if (!dts.isUtc() && !dts.getValue().contains("T")) {
						// System.out.println( "TZO=" + tzOffset(d.getTime()));
						long u = d.getTime() - tzOffset(d.getTime());
						utc.setTime(u);
					}
					// System.out.println("utcdt=" +
					// DateFormat.getDateTimeInstance().format(utc));

					ap.setDate(utc);

					// check if DATE only
					if (!dts.getValue().contains("T")) {
						// date only
						ap.setUntimed("Y");
					} else {
						ap.setUntimed("N");
						prop = pl.getProperty(Property.DTEND);
						if (prop != null) {
							DtEnd dte = (DtEnd) prop;
							Date de = dte.getDate();
							long dur = (de.getTime() - d.getTime())
									/ (1000 * 60);
							ap.setDuration(new Integer((int) dur));
						}
					}

				}

				if (comp instanceof VToDo) {
					ap.setTodo(true);
				}

				prop = pl.getProperty(Property.DURATION);
				if (prop != null) {
					Duration dur = (Duration) prop;

					int durdays = dur.getDuration().getDays();
					// skip the the duration if >= 1 day
					// not much else we can do about it right now without
					// getting
					// really complicated
					if (durdays < 1) {
						ap.setDuration(new Integer(dur.getDuration()
								.getMinutes()));
					} else {
						warning.append("WARNING: Cannot handle duration greater than 1 day for appt ["
								+ summary + "], using 0\n");
					}

				}

				prop = pl.getProperty(Property.CATEGORIES);
				if (prop != null) {
					Categories cats = (Categories) prop;
					TextList catlist = cats.getCategories();
					Iterator<String> cit = catlist.iterator();
					while (cit.hasNext()) {
						String cat = cit.next();
						if (cat.equals("Holidays")) {
							ap.setHoliday(new Integer(1));
						} else if (cat.equals("Vacation")) {
							ap.setVacation(new Integer(1));
						} else if (cat.equals("ToDo")) {
							ap.setTodo(true);
						} else if (cat.equals("black") | cat.equals("red")
								|| cat.equals("green") || cat.equals("blue")
								|| cat.equals("white")) {
							ap.setColor(cat);
						} else {
							ap.setCategory(cat);
						}
					}
				}

				prop = pl.getProperty(Property.CLASS);
				if (prop != null) {
					Clazz clazz = (Clazz) prop;
					if (clazz.getValue().equals(Clazz.PRIVATE)) {
						ap.setPrivate(true);
					}
				}

				prop = pl.getProperty(Property.RRULE);
				if (prop != null) {
					RRule rr = (RRule) prop;
					Recur recur = rr.getRecur();

					String freq = recur.getFrequency();
					int interval = recur.getInterval();
					if (freq.equals(Recur.DAILY)) {
						ap.setFrequency(Repeat.DAILY);
					} else if (freq.equals(Recur.WEEKLY)) {
						if (interval == 2) {
							ap.setFrequency(Repeat.BIWEEKLY);
						} else {
							ap.setFrequency(Repeat.WEEKLY);
						}

						WeekDayList dl = recur.getDayList();
						if (dl != null && !dl.isEmpty()) {
							String f = Repeat.DAYLIST;
							f += ",";
							for (Object o : dl) {
								WeekDay wd = (WeekDay) o;
								f += WeekDay.getCalendarDay(wd);
							}
							ap.setFrequency(f);

						}

					} else if (freq.equals(Recur.MONTHLY)) {
						ap.setFrequency(Repeat.MONTHLY);
					} else if (freq.equals(Recur.YEARLY)) {
						ap.setFrequency(Repeat.YEARLY);
					} else {
						warning.append("WARNING: Cannot handle frequency of ["
								+ freq + "], for appt [" + summary
								+ "], adding first occurrence only\n");
						aplist.add(ap);
						continue;
					}

					Date until = recur.getUntil();
					if (until != null) {
						ap.setRepeatUntil(until);
					} else {
						int times = recur.getCount();
						if (times < 1)
							times = 9999;
						ap.setTimes(new Integer(times));
					}

					ap.setRepeatFlag(true);

				}

				aplist.add(ap);
			}
		}

		int imported = 0;
		int dup_count = 0;

		for (Appointment ap : aplist) {
			// check for dups
			List<Appointment> appts = AppointmentModel.getReference()
					.getAppointmentsByText(ap.getText());

			for (Appointment dbap : appts) {
				dbap.setCreateTime(null);
				dbap.setLastMod(null);
			}

			if (appts.contains(ap)) {
				dup_count++;
				dups.append("DUP: " + ap.getText() + "\n");
				continue;
			}

			imported++;
			amodel.saveAppt(ap);
		}

		warning.append("Imported: " + imported + "\n");
		warning.append("Skipped: " + skipped + "\n");
		warning.append("Duplicates: " + dup_count + "\n");
		warning.append(dups.toString());

		if (warning.length() == 0)
			return (null);

		return (warning.toString());

	}

	static private int tzOffset(long date) {
		return TimeZone.getDefault().getOffset(date);
	}

	static public void main(String args[]) throws Exception {

		importIcalFromUrl("https://www.google.com/calendar/ical/testborg%40gmail.com/private-1cfabbb9dec4c3d764c2f62acf127599/basic.ics");
	}
}
