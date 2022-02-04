package net.sf.borg.model.sync.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.tasks.TasksScopes;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.SocketClient;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.SyncableEntity;
import net.sf.borg.model.sync.SyncEvent;
import net.sf.borg.model.sync.SyncLog;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

public class GCal {


    private static final String APPLICATION_NAME = "BORG Calendar";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String TOKENS_DIRECTORY_PATH = "/tmp/tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = List.of(CalendarScopes.CALENDAR, TasksScopes.TASKS);
    private static final String CREDENTIALS_FILE_PATH = "/tmp/credentials.json";

    static private final Logger log = Logger.getLogger("net.sf.borg");

    static private String calendarId = "primary";

    public static boolean isSyncing() {
        return Prefs.getBoolPref(PrefName.GOOGLE_SYNC);
    }


    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        File f = new File(CREDENTIALS_FILE_PATH);
        //InputStream in = CalendarQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        InputStream in = new FileInputStream(f);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    static private Calendar connect() throws Exception {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    static public synchronized void sync(Integer years, boolean outward_only) throws Exception {

        calendarId = Prefs.getPref(PrefName.GCAL_CAL_ID);
        log.info("SYNC: Connect");
        Calendar service = connect();

        processSyncMap(service);

        if (!outward_only) {
            syncFromServer(service, years);

            // incoming sync could cause additional outward activity due to borg
            // needing to convert multiple events
            // into one - a limitation of borg
            processSyncMap(service);
        }

        log.info("SYNC: Done");
    }

    static private void processSyncMap(Calendar service) throws Exception {


        List<SyncEvent> syncEvents = SyncLog.getReference().getAll();

        int num_outgoing = syncEvents.size();

        SocketClient.sendLogMessage("SYNC: Process " + num_outgoing + " Outgoing Items");
        log.info("SYNC: Process " + num_outgoing + " Outgoing Items");

        for (SyncEvent se : syncEvents) {
            if (se.getObjectType() == SyncableEntity.ObjectType.APPOINTMENT) {

                try {
                    if (se.getAction().equals(Model.ChangeEvent.ChangeAction.ADD)) {
                        Appointment ap = AppointmentModel.getReference().getAppt(se.getId());
                        if (ap == null)
                            continue;
                        Event ve1 = EntityGCalAdapter.toGCalEvent(ap);
                        if (ve1 != null)
                            addEvent(service, ve1);

                    } else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.CHANGE)) {
                        Event comp = getEvent(service, se);
                        Appointment ap = AppointmentModel.getReference().getAppt(se.getId());

                        if (comp == null) {
                            Event ve1 = EntityGCalAdapter.toGCalEvent(ap);
                            if (ve1 != null)
                                addEvent(service, ve1);

                        } else // TODO - what if both sides updated
                        {

                            Event ve1 = EntityGCalAdapter.toGCalEvent(ap);
                            if (ve1 != null)
                                updateEvent(service, ve1);

                        }
                    } else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.DELETE)) {

                        Event comp = getEvent(service, se);

                        if (comp != null) {
                            log.info("SYNC: removeEvent: " + comp.toString());
                            removeEvent(service, se);

                        } else {
                            log.info("Deleted Appt: " + se.getUid() + " not found on server");
                        }
                    }

                    SyncLog.getReference().delete(se.getId(), se.getObjectType());
                } catch (Exception e) {
                    SocketClient.sendLogMessage("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
                    log.severe("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
                    e.printStackTrace();
                }

            } /*else if (se.getObjectType() == SyncableEntity.ObjectType.TASK) {
                try {
                    if (se.getAction().equals(Model.ChangeEvent.ChangeAction.ADD)) {
                        Task task = TaskModel.getReference().getTask(se.getId());
                        if (task == null)
                            continue;
                        CalendarComponent ve1 = EntityIcalAdapter.toIcal(task, export_todos);
                        if (ve1 != null)
                            addEvent(collection, ve1);

                    } else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.CHANGE)) {
                        Component comp = getEvent(collection, se);
                        Task task = TaskModel.getReference().getTask(se.getId());

                        if (comp == null) {
                            CalendarComponent ve1 = EntityIcalAdapter.toIcal(task, export_todos);
                            if (ve1 != null)
                                addEvent(collection, ve1);

                        } else // TODO - what if both sides updated
                        {
                            CalendarComponent ve1 = EntityIcalAdapter.toIcal(task, export_todos);
                            if (ve1 != null) {
                                updateEvent(collection, ve1);
                            } else {
                                removeEvent(collection, se);

                            }
                        }
                    } else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.DELETE)) {

                        Component comp = getEvent(collection, se);

                        if (comp != null) {
                            log.info("SYNC: removeEvent: " + comp.toString());
                            removeEvent(collection, se);

                        } else {
                            log.info("Deleted Appt: " + se.getUid() + " not found on server");
                        }
                    }

                    SyncLog.getReference().delete(se.getId(), se.getObjectType());
                } catch (Exception e) {
                    SocketClient.sendLogMessage("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
                    log.severe("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
                    e.printStackTrace();
                }
            } else if (se.getObjectType() == SyncableEntity.ObjectType.SUBTASK) {
                try {
                    if (se.getAction().equals(Model.ChangeEvent.ChangeAction.ADD)) {
                        Subtask subtask = TaskModel.getReference().getSubTask(se.getId());
                        if (subtask == null)
                            continue;
                        CalendarComponent ve1 = EntityIcalAdapter.toIcal(subtask, export_todos);
                        if (ve1 != null)
                            addEvent(collection, ve1);

                    } else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.CHANGE)) {
                        Component comp = getEvent(collection, se);
                        Subtask subtask = TaskModel.getReference().getSubTask(se.getId());

                        if (comp == null) {
                            CalendarComponent ve1 = EntityIcalAdapter.toIcal(subtask, export_todos);
                            if (ve1 != null)
                                addEvent(collection, ve1);

                        } else // TODO - what if both sides updated
                        {
                            CalendarComponent ve1 = EntityIcalAdapter.toIcal(subtask, export_todos);
                            if (ve1 != null) {
                                updateEvent(collection, ve1);
                            } else {
                                removeEvent(collection, se);

                            }
                        }
                    } else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.DELETE)) {

                        Component comp = getEvent(collection, se);

                        if (comp != null) {
                            log.info("SYNC: removeEvent: " + comp.toString());
                            removeEvent(collection, se);

                        } else {
                            log.info("Deleted Appt: " + se.getUid() + " not found on server");
                        }
                    }

                    SyncLog.getReference().delete(se.getId(), se.getObjectType());
                } catch (Exception e) {
                    SocketClient.sendLogMessage("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
                    log.severe("SYNC ERROR for: " + se.toString() + ":" + e.getMessage());
                    e.printStackTrace();
                }
            }*/
        }

    }

    private static Event getEvent(Calendar service, SyncEvent se) throws IOException {
       return service.events().get(calendarId,se.getUid()).execute();
    }

    private static void removeEvent(Calendar service, SyncEvent se) throws IOException {
        service.events().delete(calendarId,se.getUid()).execute();
    }

    private static void updateEvent(Calendar service, Event ve1) throws IOException {
        service.events().update(calendarId,ve1.getId(),ve1).execute();
    }

    private static void addEvent(Calendar service, Event ve1) throws IOException {
        ve1.setId(null);
        log.info(ve1.toPrettyString());
        service.events().insert(calendarId,ve1).execute();
    }

    static private void syncFromServer(Calendar service, Integer years) throws Exception {

        SocketClient.sendLogMessage("SYNC: Start Incoming Sync");
        log.info("SYNC: Start Incoming Sync");

        Date after = null;
        GregorianCalendar gcal = new GregorianCalendar();

        gcal.add(java.util.Calendar.YEAR, -1 * ((years == null) ? 50 : years.intValue()));
        after = gcal.getTime();

        ArrayList<String> serverUids = new ArrayList<String>();

        DateTime a = new DateTime(after);
        Events events = service.events().list(calendarId)
                .setMaxResults(1000)
                .setTimeMin(a)
                .setSingleEvents(false)
                .execute();
        List<Event> items = events.getItems();

        log.info("SYNC: " + a.toString());

        SocketClient.sendLogMessage("SYNC: found " + items.size() + " Event Calendars on server");
        log.info("SYNC: found " + items.size() + " Event Calendars on server");
        int count = 0;
        for (Event event : items) {
            count += syncCalendar(event, serverUids);
        }

        SocketClient.sendLogMessage("SYNC: processed " + count + " new/changed Events");

        /*
        count = 0;
        net.fortuna.ical4j.model.Calendar tcals[] = collection.getTasks();
        SocketClient.sendLogMessage("SYNC: found " + tcals.length + " Todo Calendars on server");
        log.info("SYNC: found " + tcals.length + " Todo Calendars on server");
        for (net.fortuna.ical4j.model.Calendar cal : tcals) {
            count += syncCalendar(cal, serverUids);
        }

        SocketClient.sendLogMessage("SYNC: processed " + count + " new/changed Tasks");

         */

        log.fine(serverUids.toString());

        SocketClient.sendLogMessage("SYNC: check for deletes");
        log.info("SYNC: check for deletes");

        // find all appts in Borg that are not on the server
        for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {
            if (ap.getDate().before(after))
                continue;

            if (!serverUids.contains(ap.getUid())) {
                SocketClient.sendLogMessage("Appointment Not Found in Borg - Deleting: " + ap.toString());
                log.info("Appointment Not Found in Borg - Deleting: " + ap.toString());
                SyncLog.getReference().setProcessUpdates(false);
                AppointmentModel.getReference().delAppt(ap.getKey());
                SyncLog.getReference().setProcessUpdates(true);
            }
        }

    }

    static private int syncCalendar(Event event, ArrayList<String> serverUids) throws Exception {


        log.fine("Incoming event: " + event.toString());


        String uid = event.getICalUID();

        // ignore incoming tasks
        // TODO - process completion??
        if (uid.contains("BORGT") || uid.contains("BORGS"))
            return 0;

        serverUids.add(uid);

        // detect single occurrence
        boolean recur = false;
        if (event.getRecurrence() != null && !event.getRecurrence().isEmpty()) {
            for (String rule : event.getRecurrence()) {
                if (rule.startsWith("RRULE:")) {
                    recur = true;
                }
            }
        }

        // don't process recurring events - only tasks
        if (recur) {
            return 0;
        }

        log.fine("Incoming event: " + event.toString());

        Appointment newap = EntityGCalAdapter.toBorg(event);
        if (newap == null)
            return 0;

        Appointment ap = AppointmentModel.getReference().getApptByUid(uid);
        if (ap == null) {
            // not found in BORG, so add it
            try {

                SyncLog.getReference().setProcessUpdates(false);
                log.info("SYNC save: " + event.toString());
                log.info("SYNC save: " + newap.toString());
                AppointmentModel.getReference().saveAppt(newap);
            } finally {
                SyncLog.getReference().setProcessUpdates(true);
                return 1;
            }
        } else if (newap.getLastMod().after(ap.getLastMod())) {
            // was updated after BORG so update BORG

            // check for special case - incoming is repeating todo that is
            // completed
            // if so, then just complete the latest todo instance as android
            // task app can't
            // properly handle recurrence it completes the entire todo
            // instead of one instance.
            if (Repeat.isRepeating(ap) && ap.isTodo() && !newap.isTodo()) {

                log.info("SYNC do todo: " + ap.toString());
                AppointmentModel.getReference().do_todo(ap.getKey(), true);
                // don't suppress sync log - need to sync this todo
                return 1;
            } else {

                try {
                    newap.setKey(ap.getKey());
                    newap.setReminderTimes(ap.getReminderTimes());

                    SyncLog.getReference().setProcessUpdates(false);
                    log.info("SYNC save: " + event.toString());
                    log.info("SYNC save: " + newap.toString());
                    AppointmentModel.getReference().saveAppt(newap);
                } finally {
                    SyncLog.getReference().setProcessUpdates(true);
                    return 1;
                }
            }
        }


        return 0;

    }
/*
    static private void processRecurrence(Task task, String uid) throws Exception {


        Appointment ap = AppointmentModel.getReference().getApptByUid(uid);
        if (ap != null) {

            if (comp instanceof VEvent) {
                log.warning("SYNC: ignoring Vevent for single recurrence - cannot process\n" + comp.toString());
                SocketClient.sendLogMessage(
                        "SYNC: ignoring Vevent for single recurrence - cannot process\n" + comp.toString());
                return;
            }
            // for a recurrence of a VToDo, we only use the
            // COMPLETED
            // status if present - otherwise, we ignore
            Completed cpltd = (Completed) comp.getProperty(Property.COMPLETED);
            Status stat = (Status) comp.getProperty(Property.STATUS);
            if (cpltd == null && (stat == null || !stat.equals(Status.VTODO_COMPLETED))) {
                log.warning("SYNC: ignoring VToDo for single recurrence - cannot process\n" + comp.toString());
                SocketClient.sendLogMessage(
                        "SYNC: ignoring VToDo for single recurrence - cannot process\n" + comp.toString());
                return;
            }

            Date riddate = rid.getDate();

            Date utc = new Date();
            utc.setTime(riddate.getTime());

            // adjust time zone
            if (!rid.isUtc() && !rid.getValue().contains("T")) {
                long u = riddate.getTime() - TimeZone.getDefault().getOffset(riddate.getTime());
                utc.setTime(u);
            }

            Date nt = ap.getNextTodo();
            if (nt == null)
                nt = ap.getDate();
            if (!utc.before(nt)) {
                log.warning("SYNC: completing Todo\n" + comp.toString());
                SocketClient.sendLogMessage("SYNC: completing Todo\n" + comp.toString());
                AppointmentModel.getReference().do_todo(ap.getKey(), false, utc);

            }

            // }
        }
    }

 */
}


