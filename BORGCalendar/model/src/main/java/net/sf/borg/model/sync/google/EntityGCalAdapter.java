package net.sf.borg.model.sync.google;

import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.tasks.model.Task;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.sync.RecurrenceRule;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class EntityGCalAdapter {

    static private final Logger log = Logger.getLogger("net.sf.borg");

    // to create a new appt from a google created task
    public static Appointment toBorg(Task t) throws IOException {
        Appointment ap = AppointmentModel.getDefaultAppointment();
        if (ap == null)
            ap = new Appointment();

        t.setFactory(new GsonFactory());
        ap.setUrl(t.toPrettyString());
        ap.setText(t.getTitle());
        ap.setTodo(true);
        ap.setUntimed("Y");
        String d = t.getDue();
        DateTime dt = new DateTime(d);

        // TODO - assumes untimed only
        ap.setDate(new Date(dt.getValue()));
        return ap;

    }

    public static Appointment toBorg(Event event) throws Exception {

        // start with default appt to pull in default options
        Appointment ap = AppointmentModel.getDefaultAppointment();
        if (ap == null)
            ap = new Appointment();

        // For GCal - URL is used to store Event JSON
        event.setFactory(new GsonFactory());
        ap.setUrl(event.toPrettyString());

        ap.setCategory(null);

        String appttext = event.getSummary();
        String summary = event.getSummary();


        if (event.getLocation() != null) {
            appttext += "\nLocation: " + event.getLocation();
        }

        if (event.getDescription() != null) {
            appttext += "\n" + event.getDescription();
        }
        ap.setText(appttext);

        ap.setUntimed("Y");

        if (event.getStart().getDateTime() != null) {
            Date utc = new Date();
            utc.setTime(event.getStart().getDateTime().getValue());
            ap.setDate(utc);
            ap.setUntimed("N");
            if (event.getEnd().getDateTime() != null) {

                long dur = event.getEnd().getDateTime().getValue() - utc.getTime();

                ap.setDuration((int) dur);
            }
        } else {
            Date utc = new Date();
            utc.setTime(event.getStart().getDate().getValue()-tzOffset(event.getStart().getDate().getValue()));
            //utc.setTime(u);
            ap.setDate(utc);
            ap.setUntimed("Y");


        }

        String uid = event.getICalUID();

        if (uid == null) {
            ap.setUid("@NOUID-" + UUID.randomUUID());
        } else {
            ap.setUid(uid);
        }


        ap.setCreateTime(new Date(event.getCreated().getValue()));
        ap.setLastMod(new Date(event.getUpdated().getValue()));

        if (event.getExtendedProperties() != null) {
            Map<String, String> props = event.getExtendedProperties().getPrivate();

            if (props.containsKey("holiday"))
                ap.setHoliday(Integer.valueOf(1));
            if (props.containsKey("private"))
                ap.setPrivate(true);
            if (props.containsKey("vacation"))
                ap.setVacation(Integer.valueOf(props.get("vacation")));
            if (props.containsKey("color"))
                ap.setColor(props.get("color"));
            if (props.containsKey("category"))
                ap.setColor(props.get("category"));
        }


        if (event.getRecurrence() != null && !event.getRecurrence().isEmpty())

            for (String rl : event.getRecurrence()) {

                if (rl.startsWith("RRULE:")) {
                    Recur recur = new Recur(event.getRecurrence().get(0).substring(6));

                    String freq = recur.getFrequency();
                    int interval = recur.getInterval();
                    if (freq.equals(Recur.DAILY)) {
                        if (interval > 1) {
                            ap.setFrequency(net.sf.borg.model.Repeat.NDAYS + "," + interval);
                        } else
                            ap.setFrequency(net.sf.borg.model.Repeat.DAILY);
                    } else if (freq.equals(Recur.WEEKLY)) {
                        if (interval == 2) {
                            ap.setFrequency(net.sf.borg.model.Repeat.BIWEEKLY);
                        } else if (interval > 2) {
                            ap.setFrequency(net.sf.borg.model.Repeat.NWEEKS + "," + interval);
                        } else {
                            ap.setFrequency(net.sf.borg.model.Repeat.WEEKLY);

                            // BORG can only handle daylist for weekly
                            WeekDayList dl = recur.getDayList();
                            if (dl != null && !dl.isEmpty()) {
                                String f = net.sf.borg.model.Repeat.DAYLIST;
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
                            ap.setFrequency(net.sf.borg.model.Repeat.NMONTHS + "," + interval);
                        } else
                            ap.setFrequency(net.sf.borg.model.Repeat.MONTHLY);
                    } else if (freq.equals(Recur.YEARLY)) {
                        if (interval > 1) {
                            ap.setFrequency(net.sf.borg.model.Repeat.NYEARS + "," + interval);
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
                } else if (rl.startsWith("EXDATE")) {
                    // TODO
                    log.warning("skipping EXDATE: " + rl);
                    /*
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
                    */

                }
            }


        return ap;
    }

    // get the Event id from the Event JSON
    public static String getIdFromJSON(String json) {

        if (json == null) return null;
        GsonFactory f = new GsonFactory();
        Event e = null;
        try {
            e = f.fromString(json, Event.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return e.getId();

    }

    public static String getIdFromTaskJSON(String json) {

        if (json == null) return null;
        GsonFactory f = new GsonFactory();
        Task e = null;
        try {
            e = f.fromString(json, Task.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return e.getId();

    }

    public static Event toGCalEvent(Appointment ap) {
        Event ev = new Event();
        ev.setKind("calendar#event");
        ev.setEventType("default");
        ev.setFactory(new GsonFactory());

        // Get original event if any and copy conserved fields
        if (ap.getUrl() != null) {
            try {
                Event orig = ev.getFactory().fromString(ap.getUrl(), Event.class);
                ev.setId(orig.getId());
                ev.setKind(orig.getKind());
                ev.setEventType(orig.getEventType());
                ev.setEtag(orig.getEtag());
                ev.setSequence(orig.getSequence());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (ap.isTodo()) return null;
        String uidval = ap.getUid();
        if (uidval == null || uidval.isEmpty()) {
            uidval = ap.getKey() + "@BORGA-" + ap.getCreateTime().getTime();
        }
        ev.setICalUID(uidval);

        ev.setCreated(new DateTime(ap.getCreateTime()));
        ev.setUpdated(new DateTime(ap.getLastMod()));

        String appttext = ap.getText();

        int ii = appttext.indexOf('\n');
        if (ii != -1) {
            ev.setSummary(appttext.substring(0, ii));
            ev.setDescription(appttext.substring(ii + 1));
        } else {
            ev.setSummary(appttext);
        }

        EventDateTime dt = new EventDateTime();
        if (AppointmentModel.isNote(ap)) {
            dt.setDate(new DateTime(true,ap.getDate().getTime(),0));
        } else {
            dt.setDateTime(new DateTime(ap.getDate()));
            dt.setTimeZone("America/New_York");
        }

        ev.setStart(dt);

        EventDateTime edt = new EventDateTime();
        if (!AppointmentModel.isNote(ap) && ap.getDuration() != null && ap.getDuration().intValue() != 0) {
            edt.setDateTime(new DateTime(ap.getDate().getTime() + 1000 * 60 * ap.getDuration().intValue()));
            edt.setTimeZone("America/New_York");
        } else {
            edt.setDate(new DateTime(true,ap.getDate().getTime() + 1000 * 60 * 30 ,0));
        }

        ev.setEnd(edt);

        // TODO categories, holiday, vacation, color
        Map<String, String> propmap = new HashMap<String, String>();

        if (ap.getVacation() != null) {
            propmap.put("vacation", Integer.toString(ap.getVacation()));
        }


        // holiday is a category
        if (ap.getHoliday() != null && ap.getHoliday().intValue() != 0) {
            propmap.put("holiday", "true");
        }

        // private
        if (ap.isPrivate()) {
            propmap.put("private", "true");
        }

        // add color as a category
        if (ap.getColor() != null) {
            propmap.put("color", ap.getColor());
        }

        if (ap.getCategory() != null && !ap.getCategory().equals("")) {
            propmap.put("category", ap.getCategory());
        }

        if (ev.getExtendedProperties() == null)
            ev.setExtendedProperties(new Event.ExtendedProperties());
        ev.getExtendedProperties().setPrivate(propmap);

        if (ap.isRepeatFlag())
            ev.setRecurrence(List.of("RRULE:" + RecurrenceRule.getRRule(ap)));

        return ev;
    }

    static private int tzOffset(long date) {
        return TimeZone.getDefault().getOffset(date);
    }

    public static Task toGCalTask(Appointment appt) {

        if( !appt.isTodo()) return null;

        Task task = new Task();
        task.setKind("tasks#task");
        task.setTitle(appt.getText());
        task.setFactory(new GsonFactory());

        if (appt.getUrl() != null) {
            try {
                Task orig = task.getFactory().fromString(appt.getUrl(), Task.class);
                task.setId(orig.getId());
                task.setKind(orig.getKind());
                task.setEtag(orig.getEtag());
                task.setStatus(orig.getStatus());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Date nt = appt.getNextTodo();
        if (nt == null) {
            nt = appt.getDate();
        }
        DateTime due = new DateTime(nt);

        task.setDue(due.toStringRfc3339());

        String uidval = appt.getUid();
        if (uidval == null || uidval.isEmpty()) {
            uidval = appt.getKey() + "@BORGA-" + appt.getCreateTime().getTime();
        }

        String notes = "";
        if( appt.isRepeatFlag())
        {
            notes += "[" + RecurrenceRule.getRRule(appt) + "]";
        }

        notes += " UID:" + uidval;

        task.setNotes(notes);

        return task;
    }
}
