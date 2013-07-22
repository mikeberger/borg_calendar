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
package net.sf.borg.plugin.ical;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.logging.Logger;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
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
import net.sf.borg.model.entity.Appointment;

public class AppointmentIcalAdapter {

	static private final Logger log = Logger.getLogger("net.sf.borg");

	static public void exportIcalToFile(String filename, Date after)
			throws Exception {
		Calendar cal = exportIcal(after);
		OutputStream oostr = IOHelper.createOutputStream(filename);
		CalendarOutputter op = new CalendarOutputter();
		op.output(cal, oostr);
		oostr.close();
	}

	static public String exportIcalToString(Date after) throws Exception {
		Calendar cal = exportIcal(after);
		CalendarOutputter op = new CalendarOutputter();
		StringWriter sw = new StringWriter();
		op.output(cal, sw);
		return sw.toString();
	}

	static private Calendar exportIcal(Date after) throws Exception {

		ComponentList clist = new ComponentList();
		boolean showpriv = false;
		if (Prefs.getPref(PrefName.SHOWPRIVATE).equals("true"))
			showpriv = true;

		
		for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {

			// limit by date
			if (after != null) {
				Date latestInstance = Repeat.calculateLastRepeat(ap);
				if (latestInstance != null && latestInstance.before(after))
					continue;
			}

			TextList catlist = new TextList();
			Component ve = new VEvent();

			// set a unique ical id
			// google seems to require uniqueness even if the id was used by an
			// already deleted appt
			// so add a timestamp
			// the id has the borg key before the @ to be used for syncing later
			long updated = new Date().getTime();
			String uidval = Integer.toString(ap.getKey()) + "@BORG" + updated;
			Uid uid = new Uid(uidval);
			ve.getProperties().add(uid);

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
			if (AppointmentModel.isNote(ap)) {
				pl.add(Value.DATE);
				DtStart dts = new DtStart(pl,
						new net.fortuna.ical4j.model.Date(ap.getDate()));
				ve.getProperties().add(dts);
			} else {
				pl.add(Value.DATE_TIME);
				DtStart dts = new DtStart(pl,
						new net.fortuna.ical4j.model.DateTime(ap.getDate()));
				dts.setUtc(true);
				ve.getProperties().add(dts);
			}

			// duration
			if (ap.getDuration() != null && ap.getDuration().intValue() != 0) {
				ve.getProperties().add(
						new Duration(new Dur(0, 0, ap.getDuration().intValue(),
								0)));
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
			if (ap.isPrivate() && !showpriv) {
				ve.getProperties().add(Clazz.PRIVATE);
			}

			// add color as a cetegory
			if (ap.getColor() != null
					&& (ap.getColor().equals("black")
							|| ap.getColor().equals("blue")
							|| ap.getColor().equals("green")
							|| ap.getColor().equals("red") || ap.getColor()
							.equals("white"))) {
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
				if (freq == null) {
					continue;
				}
				if (freq.equals(Repeat.DAILY)) {
					rec += "DAILY";
				} else if (freq.equals(Repeat.WEEKLY)) {
					rec += "WEEKLY";
				} else if (freq.equals(Repeat.BIWEEKLY)) {
					rec += "WEEKLY;INTERVAL=2";
				} else if (freq.equals(Repeat.MONTHLY)) {
					Date dd = ap.getDate();
					GregorianCalendar gc = new GregorianCalendar();
					gc.setTime(dd);
					rec += "MONTHLY;BYMONTHDAY="
							+ gc.get(java.util.Calendar.DATE);
				} else if (freq.equals(Repeat.MONTHLY_DAY)) {
					Date dd = ap.getDate();
					GregorianCalendar gc = new GregorianCalendar();
					gc.setTime(dd);
					int dayOfWeek = gc.get(java.util.Calendar.DAY_OF_WEEK);
					int dayOfWeekMonth = gc
							.get(java.util.Calendar.DAY_OF_WEEK_IN_MONTH);
					String days[] = new String[] { "SU", "MO", "TU", "WE",
							"TH", "FR", "SA" };
					rec += "MONTHLY;BYDAY=" + dayOfWeekMonth
							+ days[dayOfWeek - 1];
				} else if (freq.equals(Repeat.YEARLY)) {
					rec += "YEARLY";
				} else if (freq.equals(Repeat.NDAYS)) {
					rec += "DAILY;INTERVAL="
							+ Repeat.getNValue(ap.getFrequency());
				} else if (freq.equals(Repeat.NWEEKS)) {
					rec += "WEEKLY;INTERVAL="
							+ Repeat.getNValue(ap.getFrequency());
				} else if (freq.equals(Repeat.NMONTHS)) {
					rec += "MONTHLY;INTERVAL="
							+ Repeat.getNValue(ap.getFrequency());
				} else if (freq.equals(Repeat.NYEARS)) {
					rec += "YEARLY;INTERVAL="
							+ Repeat.getNValue(ap.getFrequency());
				} else if (freq.equals(Repeat.WEEKDAYS)) {
					rec += "WEEKLY;BYDAY=MO,TU,WE,TH,FR";
				} else if (freq.equals(Repeat.MWF)) {
					rec += "WEEKLY;BYDAY=MO,WE,FR";
				} else if (freq.equals(Repeat.WEEKENDS)) {
					rec += "WEEKLY;BYDAY=SU,SA";
				} else if (freq.equals(Repeat.TTH)) {
					rec += "WEEKLY;BYDAY=TU,TH";
				} else if (freq.equals(Repeat.DAYLIST)) {
					String days[] = new String[] { "SU", "MO", "TU", "WE",
							"TH", "FR", "SA" };
					rec += "WEEKLY;BYDAY=";
					Collection<Integer> c = Repeat
							.getDaylist(ap.getFrequency());
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
					continue;
				}

				if (ap.getTimes().intValue() != 9999) {
					rec += ";COUNT=" + Repeat.calculateTimes(ap);
				}
				// System.out.println(rec);

				ve.getProperties().add(new RRule(new Recur(rec)));

			}
			clist.add(ve);

		}

		PropertyList pl = new PropertyList();
		pl.add(new ProdId("BORG Calendar"));
		pl.add(Version.VERSION_2_0);
		net.fortuna.ical4j.model.Calendar cal = new net.fortuna.ical4j.model.Calendar(
				pl, clist);

		cal.validate();

		return cal;
	}

	@SuppressWarnings("unchecked")
	static public String importIcal(String file, String category)
			throws Exception {
		
		boolean skip_borg = Prefs.getBoolPref(IcalModule.SKIP_BORG);
		int skipped = 0;
		
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(
				CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		StringBuffer warning = new StringBuffer();
		CalendarBuilder builder = new CalendarBuilder();
		InputStream is = new FileInputStream(file);
		Calendar cal = builder.build(is);
		is.close();

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
				if( skip_borg && uidProp != null)
				{
					String uid = uidProp.getValue();
					if( uid.contains("@BORG"))
					{
						skipped++;
						continue;
					}
				}

				// start with default appt to pull in default options
				Appointment ap = amodel.getDefaultAppointment();
				if (ap == null)
					ap = amodel.newAppt();
				if (category.equals("")
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

		for (Appointment ap : aplist)
			amodel.saveAppt(ap);
		
		warning.append("Imported " + aplist.size() + " Appointments\n");
		warning.append("Skipped " + skipped + " Appointments\n");

		if (warning.length() == 0)
			return (null);

		return (warning.toString());

	}

	static private int tzOffset(long date) {
		return TimeZone.getDefault().getOffset(date);
	}
}
