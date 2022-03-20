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
package net.sf.borg.model.sync.ical;

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.*;
import net.sf.borg.common.DateUtil;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;
import net.sf.borg.model.sync.RecurrenceRule;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.*;
import java.util.logging.Logger;

public class EntityIcalAdapter {

	static private final Logger log = Logger.getLogger("net.sf.borg");

	static public CalendarComponent toIcal(Appointment ap, boolean export_todos) throws Exception {

		TextList catlist = new TextList();
		CalendarComponent ve = null;

		// export todos as VTODOs if option set
		// This works well in clients such as Mozilla Lightning, which handles
		// VTODOs similar to BORG.
		// On the other hand, VTODOs are not handled well at all by Android
		if (ap.isTodo() && export_todos)
			ve = new VToDo();
		else
			ve = new VEvent();

		String uidval = ap.getUid();
		if (uidval == null || uidval.isEmpty()) {
			uidval = Integer.toString(ap.getKey()) + "@BORGA-" + ap.getCreateTime().getTime();
		}
		Uid uid = new Uid(uidval);
		ve.getProperties().add(uid);

		String urlVal = ap.getUrl();
		if (urlVal != null && !urlVal.isEmpty()) {
			Url url = new Url();
			url.setValue(urlVal);
			ve.getProperties().add(url);
		}

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

			if (AppointmentModel.isNote(ap)) {
				pl.add(Value.DATE);
				DtStart dtd = new DtStart(pl, new net.fortuna.ical4j.model.Date(nt));
				ve.getProperties().add(dtd);

				VToDo todo = (VToDo) ve;
				Due due = new Due(pl, new net.fortuna.ical4j.model.Date(nt));
				todo.getProperties().add(due);
			} else {

				// set time from appt date - next todo has no time
				Calendar dcal = new GregorianCalendar();
				dcal.setTime(ap.getDate());
				Calendar ncal = new GregorianCalendar();
				ncal.setTime(nt);
				ncal.set(Calendar.SECOND, dcal.get(Calendar.SECOND));
				ncal.set(Calendar.MINUTE, dcal.get(Calendar.MINUTE));
				ncal.set(Calendar.HOUR, dcal.get(Calendar.HOUR));
				ncal.set(Calendar.AM_PM, dcal.get(Calendar.AM_PM));
				nt = ncal.getTime();

				pl.add(Value.DATE_TIME);
				DtStart dtd = new DtStart(pl, new net.fortuna.ical4j.model.DateTime(nt));
				ve.getProperties().add(dtd);

				VToDo todo = (VToDo) ve;
				Due due = new Due(pl, new net.fortuna.ical4j.model.DateTime(nt));
				todo.getProperties().add(due);
			}

		} else if (AppointmentModel.isNote(ap)) {
			pl.add(Value.DATE);
			DtStart dts = new DtStart(pl, new net.fortuna.ical4j.model.Date(ap.getDate()));
			ve.getProperties().add(dts);
			Date end = new Date(ap.getDate().getTime() + 1000 * 60 * 60 * 24);
			DtEnd dte = new DtEnd(pl, new net.fortuna.ical4j.model.Date(end));
			ve.getProperties().add(dte);
		} else {
			pl.add(Value.DATE_TIME);
			DtStart dts = new DtStart(pl, new net.fortuna.ical4j.model.DateTime(ap.getDate()));
			dts.setUtc(true);
			ve.getProperties().add(dts);

			// duration
			if (ap.getDuration() != null && ap.getDuration().intValue() != 0) {
				// ve.getProperties()
				// .add(new Duration(new Dur(0, 0,
				// ap.getDuration().intValue(), 0)));
				DtEnd dte = new DtEnd(pl, new net.fortuna.ical4j.model.DateTime(
						ap.getDate().getTime() + 1000 * 60 * ap.getDuration().intValue()));
				dte.setUtc(true);
				ve.getProperties().add(dte);
			}
		}

		// vacation is a category
		if (ap.getVacation() != null && ap.getVacation().intValue() == 1) {
			catlist.add("Vacation");
		} else if (ap.getVacation() != null && ap.getVacation().intValue() == 2) {
			catlist.add("HalfDay");
		}

		// holiday is a category
		if (ap.getHoliday() != null && ap.getHoliday().intValue() != 0) {
			catlist.add("Holidays");
		}

		// private
		if (ap.isPrivate()) {
			ve.getProperties().add(Clazz.PRIVATE);
			catlist.add("Private");
		}

		// add color as a category
		if (ap.getColor() != null) {
				catlist.add("c_" + ap.getColor());
		}

		if (ap.getCategory() != null && !ap.getCategory().equals("")) {
			catlist.add(ap.getCategory());
		}

		if (ap.isTodo())
			catlist.add("ToDo");

		if (!catlist.isEmpty()) {
			ve.getProperties().add(new Categories(catlist));
		}

		// repeat stuff
		if (ap.isRepeatFlag()) {
			// build recur string
			String rec = RecurrenceRule.getRRule(ap);


			ve.getProperties().add(new RRule(new Recur(rec)));

			// skip list
			if (ap.getSkipList() != null) {

				long start_epoch = DateUtil.dayOfEpoch(ap.getDate());
				if (AppointmentModel.isNote(ap)) {
					DateList dl = new DateList(Value.DATE);
					for (String rkey : ap.getSkipList()) {
						long skip_epoch = Long.parseLong(rkey);
						long real_skip = ((skip_epoch - start_epoch) * 24L * 60L * 60L * 1000L)
								+ ap.getDate().getTime();
						Date skdate = new Date(real_skip);
						dl.add(new net.fortuna.ical4j.model.Date(skdate));
					}
					dl.setUtc(true);
					ve.getProperties().add(new ExDate(dl));
				} else {
					DateList dl = new DateList(Value.DATE_TIME);
					for (String rkey : ap.getSkipList()) {
						long skip_epoch = Long.parseLong(rkey);
						long real_skip = ((skip_epoch - start_epoch) * 24L * 60L * 60L * 1000L)
								+ ap.getDate().getTime();
						DateTime skdate = new DateTime(real_skip);
						dl.add(new net.fortuna.ical4j.model.DateTime(skdate));
					}
					dl.setUtc(true);
					ve.getProperties().add(new ExDate(dl));
				}

			}

		}

		// reminder
		if (/* !AppointmentModel.isNote(ap) && */ap.getReminderTimes() != null && !ap.getReminderTimes().isEmpty()) {

			// add a reminder
			if (ap.getReminderTimes().contains("Y") && (ap.isRepeatFlag() || ap.getDate().after(new Date()))) {
				VAlarm va = new VAlarm(new Dur(0, 0, -1 * 30, 0));
				va.getProperties().add(Action.DISPLAY);
				va.getProperties().add(new Description(ap.getText()));
				va.getProperties().add(new net.fortuna.ical4j.model.property.Repeat(2));
				va.getProperties().add(new Duration(new Dur(0, 0, 15, 0)));
				if (ve instanceof VEvent)
					((VEvent) ve).getAlarms().add(va);
				else
					((VToDo) ve).getAlarms().add(va);

			}
		}

		return ve;

	}

	static private int tzOffset(long date) {
		return TimeZone.getDefault().getOffset(date);
	}

	public static Appointment toBorg(Component comp) {
		if (comp instanceof VEvent || comp instanceof VToDo) {

			AppointmentModel amodel = AppointmentModel.getReference();

			// start with default appt to pull in default options
			Appointment ap = amodel.getDefaultAppointment();
			if (ap == null)
				ap = amodel.newAppt();

			ap.setCategory(null);

			PropertyList<Property> pl = comp.getProperties();
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

			// for todos, use DUE over DTSTART - chg for aCalendar+
			if (comp instanceof VToDo) {
				Property propdue = pl.getProperty(Property.DUE);
				if( propdue != null )
					prop = propdue;
			}

			if (prop != null) {
				DateProperty dts = (DateProperty) prop;
				Date d = dts.getDate();

				Date utc = new Date();
				utc.setTime(d.getTime());

				// adjust time zone
				if (!dts.isUtc() && !dts.getValue().contains("T")) {
					// System.out.println( "TZO=" + tzOffset(d.getTime()));
					long u = d.getTime() - tzOffset(d.getTime());
					utc.setTime(u);
				}

				ap.setDate(utc);

				// check if DATE only
				// but assume appt at midnight is untimed
				if (!dts.getValue().contains("T") || dts.getValue().contains("T000000")) {
					// date only
					ap.setUntimed("Y");
				} else {
					ap.setUntimed("N");
					prop = pl.getProperty(Property.DTEND);
					if (prop != null) {
						DtEnd dte = (DtEnd) prop;
						Date de = dte.getDate();
						long dur = (de.getTime() - d.getTime()) / (1000 * 60);
						ap.setDuration(Integer.valueOf((int) dur));
					}
				}

			}

			Uid uid = (Uid) pl.getProperty(Property.UID);
			// if no uid - create one - mainly can happen on ics import - not from caldav
			if (uid == null) {
				ap.setUid("@NOUID-" + UUID.randomUUID());
			} else {
				ap.setUid(uid.getValue());
			}

			// store the URL coming back from the caldav server
			// only store the last part
			Url url = (Url) pl.getProperty(Property.URL);
			if (url != null) {
				String urlVal = url.getValue();
				int idx = urlVal.lastIndexOf('/');
				if (idx == -1) {
					ap.setUrl(urlVal);
				} else {
					ap.setUrl(urlVal.substring(idx + 1));
				}

			}

			LastModified lm = (LastModified) pl.getProperty(Property.LAST_MODIFIED);
			if (lm != null)
				ap.setLastMod(lm.getDateTime());
			else
				ap.setLastMod(new Date());
			Created cr = (Created) pl.getProperty(Property.CREATED);
			if (cr != null)
				ap.setCreateTime(cr.getDateTime());
			else
				ap.setCreateTime(new Date());
			prop = pl.getProperty(Property.DURATION);
			if (prop != null) {
				Duration dur = (Duration) prop;

				int durdays = dur.getDuration().getDays();
				// skip the the duration if >= 1 day
				// not much else we can do about it right now without
				// getting
				// really complicated
				if (durdays < 1) {
					ap.setDuration(Integer.valueOf(dur.getDuration().getMinutes()));
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
						ap.setHoliday(Integer.valueOf(1));
					} else if (cat.equals("Vacation")) {
						ap.setVacation(Integer.valueOf(1));
					} else if (cat.equals("HalfDay")) {
						ap.setVacation(Integer.valueOf(2));
					} else if (cat.equals("Private")) {
						ap.setPrivate(true);
					} else if (cat.equals("ToDo")) {
						ap.setTodo(true);
					} else if (cat.startsWith("c_")) {
						ap.setColor(cat.substring(2));
					} else {
						ap.setCategory(cat);
					}
				}
			}

			prop = pl.getProperty(Property.CLASS);
			if (prop != null) {
				Clazz clazz = (Clazz) prop;
				if (clazz.getValue().equals(Clazz.PRIVATE.getValue())) {
					ap.setPrivate(true);
				}
			}

			if (comp instanceof VToDo) {

				ap.setTodo(true);

				prop = pl.getProperty(Property.STATUS);
				if (prop != null) {
					Status stat = (Status) prop;
					if (stat.equals(Status.VTODO_COMPLETED)) {
						ap.setTodo(false);
						ap.setColor("strike");
					}
				}
			}

			prop = pl.getProperty(Property.RRULE);
			if (prop != null) {
				RRule rr = (RRule) prop;
				Recur recur = rr.getRecur();

				String freq = recur.getFrequency();
				int interval = recur.getInterval();
				if (freq.equals(Recur.DAILY)) {
					if (interval > 1) {
						ap.setFrequency(Repeat.NDAYS + "," + interval);
					} else
						ap.setFrequency(Repeat.DAILY);
				} else if (freq.equals(Recur.WEEKLY)) {
					if (interval == 2) {
						ap.setFrequency(Repeat.BIWEEKLY);
					} else if (interval > 2) {
						ap.setFrequency(Repeat.NWEEKS + "," + interval);
					} else {
						ap.setFrequency(Repeat.WEEKLY);
						
						// BORG can only handle daylist for weekly
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
					}

				} else if (freq.equals(Recur.MONTHLY)) {
					if (interval > 1) {
						ap.setFrequency(Repeat.NMONTHS + "," + interval);
					} else
						ap.setFrequency(Repeat.MONTHLY);
				} else if (freq.equals(Recur.YEARLY)) {
					if (interval > 1) {
						ap.setFrequency(Repeat.NYEARS + "," + interval);
					} else
						ap.setFrequency(Repeat.YEARLY);
				} else {
					log.warning("WARNING: Cannot handle frequency of [" + freq + "], for appt [" + summary
							+ "], adding first occurrence only\n");
					return ap;
				}

				Date until = recur.getUntil();
				if (until != null) {
					long u = until.getTime() - tzOffset(until.getTime());
					ap.setRepeatUntil(new Date(u));
				} else {
					int times = recur.getCount();
					if (times < 1)
						times = 9999;
					ap.setTimes(Integer.valueOf(times));
				}

				ap.setRepeatFlag(true);

				ExDate ex = (ExDate) pl.getProperty(Property.EXDATE);
				if (ex != null) {

					Vector<String> vect = new Vector<String>();

					// add the current appt key to the SKip list
					DateList dl = ex.getDates();
					dl.setUtc(true);
					@SuppressWarnings("rawtypes")
					Iterator it = dl.iterator();
					while (it.hasNext()) {
						Object o = it.next();
						if (o instanceof net.fortuna.ical4j.model.Date) {
							int rkey = (int) (((net.fortuna.ical4j.model.Date) o).getTime() / 1000 / 60 / 60 / 24);
							vect.add(Integer.toString(rkey));
						}
					}

					ap.setSkipList(vect);

				}

			}

			return ap;

		}
		return null;

	}

	static public CalendarComponent toIcal(Project t, boolean export_todos) throws Exception {
		if (TaskModel.isClosed(t))
			return null;

		Date due = t.getDueDate();
		if (due == null)
			return null;

		CalendarComponent ve = null;
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
		DtStart dts = new DtStart(pl, new net.fortuna.ical4j.model.Date(due));
		ve.getProperties().add(dts);

		Date end = new Date(due.getTime() + 1000 * 60 * 60 * 24);
		DtEnd dte = new DtEnd(pl, new net.fortuna.ical4j.model.Date(end));
		ve.getProperties().add(dte);

		return ve;

	}

	static public CalendarComponent toIcal(Task t, boolean export_todos) throws Exception {
		if (TaskModel.isClosed(t))
			return null;

		Date due = t.getDueDate();
		if (due == null)
			return null;

		CalendarComponent ve = null;
		if (export_todos)
			ve = new VToDo();
		else
			ve = new VEvent();

		String uidval = t.getUid();
		if (uidval == null || uidval.isEmpty()) {
			uidval = Integer.toString(t.getKey()) + "@BORGT-" + t.getCreateTime().getTime();
		}
		Uid uid = new Uid(uidval);
		ve.getProperties().add(uid);

		String urlVal = t.getUrl();
		if (urlVal != null && !urlVal.isEmpty()) {
			Url url = new Url();
			url.setValue(urlVal);
			ve.getProperties().add(url);
		}

		ve.getProperties().add(new Created(new DateTime(t.getCreateTime())));
		ve.getProperties().add(new LastModified(new DateTime(t.getLastMod())));

		// add text
		ve.getProperties().add(new Summary("[T]" + t.getSummary()));
		if (t.getDescription() != null && !t.getDescription().isEmpty())
			ve.getProperties().add(new Description(t.getDescription()));

		ParameterList pl = new ParameterList();
		pl.add(Value.DATE);
		DtStart dts = new DtStart(pl, new net.fortuna.ical4j.model.Date(due));
		ve.getProperties().add(dts);
		Due du = new Due(pl, new net.fortuna.ical4j.model.Date(due));
		ve.getProperties().add(du);

		Date end = new Date(due.getTime() + 1000 * 60 * 60 * 24);
		DtEnd dte = new DtEnd(pl, new net.fortuna.ical4j.model.Date(end));
		ve.getProperties().add(dte);

		return ve;

	}

	static public CalendarComponent toIcal(Subtask t, boolean export_todos) throws Exception {

		if (t.getCloseDate() != null)
			return null;

		Date due = t.getDueDate();
		if (due == null)
			return null;

		CalendarComponent ve = null;
		if (export_todos)
			ve = new VToDo();
		else
			ve = new VEvent();

		String uidval = t.getUid();
		if (uidval == null || uidval.isEmpty()) {
			uidval = Integer.toString(t.getKey()) + "@BORGS-" + t.getCreateTime().getTime();
		}
		Uid uid = new Uid(uidval);
		ve.getProperties().add(uid);

		String urlVal = t.getUrl();
		if (urlVal != null && !urlVal.isEmpty()) {
			Url url = new Url();
			url.setValue(urlVal);
			ve.getProperties().add(url);
		}

		ve.getProperties().add(new Created(new DateTime(t.getCreateTime())));
		ve.getProperties().add(new LastModified(new DateTime(t.getLastMod())));

		// add text
		ve.getProperties().add(new Summary("[S]" + t.getDescription()));

		ParameterList pl = new ParameterList();
		pl.add(Value.DATE);
		DtStart dts = new DtStart(pl, new net.fortuna.ical4j.model.Date(due));
		ve.getProperties().add(dts);
		Due du = new Due(pl, new net.fortuna.ical4j.model.Date(due));
		ve.getProperties().add(du);

		Date end = new Date(due.getTime() + 1000 * 60 * 60 * 24);
		DtEnd dte = new DtEnd(pl, new net.fortuna.ical4j.model.Date(end));
		ve.getProperties().add(dte);

		return ve;
	}


}
