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
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.SocketClient;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model;
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

    private static final List<String> SCOPES = List.of(CalendarScopes.CALENDAR, TasksScopes.TASKS);

    static private final Logger log = Logger.getLogger("net.sf.borg");

    static volatile private GCal singleton = null;

    private String calendarId;
    private String taskList;
    private Calendar service = null;
    private Tasks tservice = null;

    public static boolean isSyncing() {
        return Prefs.getBoolPref(PrefName.GOOGLE_SYNC) && Prefs.getBoolPref(PrefName.ENABLE_GOOGLE_FEATURE);
    }

    static public GCal getReference() {
        if (singleton == null) {
            GCal b = new GCal();
            singleton = b;
        }
        return (singleton);
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        File f = new File(Prefs.getPref(PrefName.GOOGLE_CRED_FILE));
        //InputStream in = CalendarQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        InputStream in = new FileInputStream(f);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + Prefs.getPref(PrefName.GOOGLE_CRED_FILE));
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(Prefs.getPref(PrefName.GOOGLE_TOKEN_DIR))))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    // null out the google ids so that they are fetched again
    public void resetGoogleIds() {
        calendarId = null;
        taskList = null;
    }

    public void connect() throws Exception {

        if (service != null) return;

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        tservice = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

    }

    public synchronized void sync(Integer years, boolean overwrite) throws Exception {

        Date after = null;
        GregorianCalendar gcal = new GregorianCalendar();

        gcal.add(java.util.Calendar.YEAR, -1 * ((years == null) ? 50 : years.intValue()));
        after = gcal.getTime();

        log.info("SYNC: Connect");
        connect();
        setIds();

        if (overwrite) {
            // get all appointments and add to syncmap
            for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {
                if (ap.getDate().before(after))
                    continue;

                // if not in syncmap then add and null out URL if needed
                if (ap.getUrl() == null) {
                    // never been synced - so force a new entry if none exists
                    if (SyncLog.getReference().get(ap.getKey(), SyncableEntity.ObjectType.APPOINTMENT) == null) {
                        SyncEvent event = new SyncEvent();
                        event.setId(ap.getKey());
                        event.setAction(Model.ChangeEvent.ChangeAction.ADD);
                        event.setObjectType(SyncableEntity.ObjectType.APPOINTMENT);
                        event.setUid(ap.getUid());
                        SyncLog.getReference().insert(event);
                    }
                } else {
                    // removing the URL will force a new sync log entry
                    ap.setUrl(null);
                    AppointmentModel.getReference().saveAppt(ap, false);
                }

            }
        }
        processSyncMap();

        syncFromServer(after);

        // incoming sync could cause additional outward activity due to borg
        // needing to convert multiple events
        // into one - a limitation of borg
        processSyncMap();


        log.info("SYNC: Done");
    }

    private void setIds() throws Exception {

        String calname = Prefs.getPref(PrefName.GCAL_CAL_ID);
        String taskname = Prefs.getPref(PrefName.GCAL_TASKLIST_ID);

        if (calendarId == null) {
            CalendarList cals = service.calendarList().list().execute();
            for (CalendarListEntry c : cals.getItems()) {
                log.fine("Cal Entry: " + c.getSummary() + " : " + c.getId());
                if (calname.equals(c.getSummary())) {
                    calendarId = c.getId();
                    break;
                }
            }

        }
        if (taskList == null) {
            TaskLists result = tservice.tasklists().list().execute();
            List<TaskList> taskLists = result.getItems();

            if (taskLists != null) {
                for (TaskList tasklist : taskLists) {
                    log.fine("TaskList Entry: " + tasklist.getTitle() + " : " + tasklist.getId());
                    if (taskname.equals(tasklist.getTitle())) {
                        taskList = tasklist.getId();
                        break;
                    }
                }
            }
        }

        if (calendarId == null)
            throw new Exception("Could not determine calender id matching: " + calname);
        if (taskList == null)
            throw new Exception("Could not determine task list id matching: " + taskname);
    }

    private void processSyncMap() throws Exception {


        List<SyncEvent> syncEvents = SyncLog.getReference().getAll();

        int num_outgoing = syncEvents.size();

        SocketClient.sendLogMessage("SYNC: Process " + num_outgoing + " Outgoing Items");
        log.info("SYNC: Process " + num_outgoing + " Outgoing Items");

        for (SyncEvent se : syncEvents) {
            if (se.getObjectType() == SyncableEntity.ObjectType.APPOINTMENT) {

                try {
                    if (se.getAction().equals(Model.ChangeEvent.ChangeAction.ADD)) {
                        Appointment ap = AppointmentModel.getReference().getAppt(se.getId());
                        if (ap != null) {
                            if (ap.isTodo()) {
                                Task t = EntityGCalAdapter.toGCalTask(ap);
                                if (t != null) {
                                    // check if task exists in google already
                                    if (t.getEtag() == null)
                                        addTask(t);
                                    else
                                        updateTask(t);
                                }
                            } else {
                                Event ve1 = EntityGCalAdapter.toGCalEvent(ap);
                                if (ve1 != null)
                                    addEvent(ve1);
                            }
                        }

                    } else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.CHANGE)) {

                        Appointment ap = AppointmentModel.getReference().getAppt(se.getId());
                        if (ap != null) {
                            if (ap.isTodo()) {
                                Task t = EntityGCalAdapter.toGCalTask(ap);
                                if (t != null) {
                                    // check if task exists in google already
                                    if (t.getEtag() == null) {
                                        // suspicious case - provide warning
                                        SocketClient.sendLogMessage("*** WARNING ***");
                                        SocketClient.sendLogMessage("Todo was changed in BORG that has no record of google task. Please check google. Manual delete may be needed.");
                                        SocketClient.sendLogMessage(ap.toString());
                                        addTask(t);
                                    } else
                                        updateTask(t);
                                }
                            } else {
                                String id = EntityGCalAdapter.getIdFromJSON(se.getUrl());
                                if (id == null) id = se.getUid();
                                Event comp = getEvent(id);

                                if (comp == null) {
                                    Event ve1 = EntityGCalAdapter.toGCalEvent(ap);
                                    if (ve1 != null)
                                        addEvent(ve1);

                                } else // TODO - what if both sides updated
                                {

                                    Event ve1 = EntityGCalAdapter.toGCalEvent(ap);
                                    if (ve1 != null)
                                        updateEvent(ve1);

                                }
                            }
                        }

                    } else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.DELETE)) {

                        if (se.getUrl() != null) {
                            if (se.getUrl().contains("tasks#task")) {
                                String id = EntityGCalAdapter.getIdFromTaskJSON(se.getUrl());
                                if (id != null)
                                    removeTask(id);
                            } else {
                                String id = EntityGCalAdapter.getIdFromJSON(se.getUrl());
                                if (id == null) id = se.getUid();
                                Event comp = getEvent(id);

                                if (comp != null) {
                                    log.info("SYNC: removeEvent: " + comp);
                                    try {
                                        removeEvent(comp.getId());
                                    } catch (IOException e) {
                                        SocketClient.sendLogMessage("SYNC ERROR for: " + se + ":" + e.getMessage());
                                        log.severe("SYNC ERROR for: " + se + ":" + e.getMessage());
                                    }

                                } else {
                                    log.info("Deleted Appt: " + se.getUid() + " not found on server");
                                }
                            }
                        }
                    }

                    SyncLog.getReference().delete(se.getId(), se.getObjectType());
                } catch (Exception e) {
                    SocketClient.sendLogMessage("SYNC ERROR for: " + se + ":" + e.getMessage());
                    log.severe("SYNC ERROR for: " + se + ":" + e.getMessage());
                    e.printStackTrace();
                }

            }
        }

    }

    private void updateTask(Task t) throws IOException {
        tservice.tasks().update(taskList, t.getId(), t).execute();
    }

    private void removeTask(String id) throws IOException {
        log.fine("removeTask:" + id);
        tservice.tasks().delete(taskList, id).execute();
    }


    private void addTask(Task t) throws IOException {
        tservice.tasks().insert(taskList, t).execute();
    }

    public Event getEvent(String id) {
        try {
            return service.events().get(calendarId, id).execute();
        } catch (IOException e) {
            log.info(e.getMessage());
            // e.printStackTrace();
            return null;
        }
    }

    public void removeEvent(String id) throws IOException {
        service.events().delete(calendarId, id).execute();
    }

    public void updateEvent(Event ve1) throws IOException {
        service.events().update(calendarId, ve1.getId(), ve1).execute();
    }

    public void addEvent(Event ve1) throws IOException {
        ve1.setId(null);
        log.info(ve1.toPrettyString());
        service.events().insert(calendarId, ve1).execute();
    }

    private void syncFromServer(Date after) throws Exception {

        SocketClient.sendLogMessage("SYNC: Start Incoming Sync");
        log.info("SYNC: Start Incoming Sync");

        String pageToken = "";
        int count = 0;
        ArrayList<String> serverUids = new ArrayList<String>();

        DateTime a = new DateTime(after);

        while (true) {
            Events events = service.events().list(calendarId)
                    .setMaxResults(1000)
                    .setPageToken(pageToken)
                    .setSingleEvents(false)
                    .execute();
            List<Event> items = events.getItems();

            log.info("SYNC: " + a);

            SocketClient.sendLogMessage("SYNC: found " + items.size() + " Event Calendars on server");
            log.info("SYNC: found " + items.size() + " Event Calendars on server");

            for (Event event : items) {
                count += syncEvent(event, serverUids);
            }

            if (events.getNextPageToken() == null)
                break;

            pageToken = events.getNextPageToken();

        }

        SocketClient.sendLogMessage("SYNC: processed " + count + " new/changed Events");

        count = 0;
        pageToken = "";
        while (true) {
            com.google.api.services.tasks.model.Tasks result2 = tservice.tasks().list(taskList).setMaxResults(100).setPageToken(pageToken).execute();
            List<Task> tasks = result2.getItems();
            if (tasks != null) {
                log.info("SYNC: found " + tasks.size() + " Tasks on server ");
                SocketClient.sendLogMessage("SYNC: found " + tasks.size() + " Tasks on server");

                for (Task task : tasks) {
                    count += syncTask(task, serverUids);
                }
            } else
                break;

            if (result2.getNextPageToken() == null)
                break;

            pageToken = result2.getNextPageToken();
        }
        SocketClient.sendLogMessage("SYNC: processed " + count + " new/changed Tasks");

        log.fine(serverUids.toString());

        SocketClient.sendLogMessage("SYNC: check for deletes");
        log.info("SYNC: check for deletes");

        // find all appts in Borg that are not on the server
        for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {
            if (ap.getDate().before(after))
                continue;

            // if appt was not synced, then don't delete
            if (ap.getUrl() == null || !ap.getUrl().contains("etag")) {
                log.fine("Appt was not synced - so do not delete: " + ap);
                continue;
            }

            // NOTE - a delete of a google task will not cause delete of the BORG appt
            if (!serverUids.contains(ap.getUid()) && !ap.isTodo()) {
                SocketClient.sendLogMessage("Appointment Not Found on server - Deleting: " + ap);
                log.info("Appointment Not Found on server - Deleting: " + ap);
                SyncLog.getReference().setProcessUpdates(false);
                AppointmentModel.getReference().delAppt(ap.getKey());
                SyncLog.getReference().setProcessUpdates(true);
            }
        }

    }

    private int syncEvent(Event event, ArrayList<String> serverUids) throws Exception {


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

        // don't process recurring events - only recurring tasks
        if (recur) {
            return 0;
        }

        log.fine("Incoming event: " + event);

        Appointment newap = EntityGCalAdapter.toBorg(event);
        if (newap == null)
            return 0;

        Appointment ap = AppointmentModel.getReference().getApptByUid(uid);
        if (ap == null) {
            // not found in BORG, so add it
            try {

                SyncLog.getReference().setProcessUpdates(false);
                log.info("SYNC save: " + event);
                log.info("SYNC save: " + newap);
                AppointmentModel.getReference().saveAppt(newap);
            } finally {
                SyncLog.getReference().setProcessUpdates(true);
                return 1;
            }
        } else {

            // if Borg has same etag as google, then skip
            if (ap.getUrl() != null) {
                Event orig = new GsonFactory().fromString(ap.getUrl(), Event.class);
                if (orig.getEtag().equals(event.getEtag())) {
                    log.fine("Etags match - skipping " + event.getSummary());
                    return 0;
                }
            }


            try {
                newap.setKey(ap.getKey());
                newap.setReminderTimes(ap.getReminderTimes());

                SyncLog.getReference().setProcessUpdates(false);
                log.info("SYNC save: " + event);
                log.info("SYNC save: " + newap);
                AppointmentModel.getReference().saveAppt(newap);
            } finally {
                SyncLog.getReference().setProcessUpdates(true);
                return 1;
            }
        }


    }

    private int syncTask(Task task, ArrayList<String> serverUids) throws Exception {


        log.fine("Incoming task: " + task.toPrettyString());

        int idx = -1;
        String notes = task.getNotes();
        if (notes != null)
            idx = notes.indexOf("UID:");
        if (idx != -1) {
            // match to BORG appt
            String uid = notes.substring(idx + 4);
            if (uid.contains("BORGT") || uid.contains("BORGS"))
                return 0;
            serverUids.add(uid);
            Appointment ap = AppointmentModel.getReference().getApptByUid(uid);
            if (ap == null) {
                log.warning("SYNC: ***WARNING*** could not find appt with UID: " + uid + " ignoring....");
                SocketClient.sendLogMessage("SYNC: ***WARNING*** could not find appt with UID: " + uid + " ignoring....");
                return 0;
            }

            // this is where a newly created todo gets the URL updated
            if (ap.getUrl() == null) {
                ap.setUrl(task.toPrettyString());
                try {
                    SyncLog.getReference().setProcessUpdates(false);
                    log.info("SYNC save: " + ap);
                    AppointmentModel.getReference().saveAppt(ap);
                } finally {
                    SyncLog.getReference().setProcessUpdates(true);
                    // keep going - if there was a problem and URL was incorrectly null, then
                    // we still might have to do todo
                    //return 1;
                }
            }

            if (task.getStatus().equals("completed")) {
                // do_todo
                log.info("SYNC: do_todo");
                AppointmentModel.getReference().do_todo(ap.getKey(), false);
                return 1;
            }

            // TODO - check if date chgd - case where acalendar+ updates recurring todo
            DateTime due = new DateTime(task.getDue());
            log.fine("task due:" + due);
            log.fine("ap due:" + ap.getDate());
            log.fine("ap nt:" + ap.getNextTodo());

            Date d = ap.getNextTodo();
            if (d == null)
                d = ap.getDate();

            // if incoming date is greater than BORG date, then just do_todo
            if (due.getValue() - d.getTime() > 1000 * 60 * 60) { // 1 hr cushion - should be exact though?
                log.info("SYNC: do_todo");
                AppointmentModel.getReference().do_todo(ap.getKey(), false);
                return 1;
            }


        } else {
            // google created task - add new appt
            Appointment ap = EntityGCalAdapter.toBorg(task);
            AppointmentModel.getReference().saveAppt(ap);
            log.info("SYNC save from google-created task: " + ap);
            return 1;

        }

        return 0;

    }

}


