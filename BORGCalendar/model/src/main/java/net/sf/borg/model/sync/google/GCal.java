package net.sf.borg.model.sync.google;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
import com.google.gson.stream.MalformedJsonException;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.ModalMessageServer;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.SyncableEntity;
import net.sf.borg.model.sync.SubscribedCalendars;
import net.sf.borg.model.sync.SyncEvent;
import net.sf.borg.model.sync.SyncLog;

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
	private Collection<String> subscribed = new ArrayList<String>();

	public static boolean isSyncing() {
		return Prefs.getBoolPref(PrefName.GOOGLE_SYNC);
	}

	static public GCal getReference() {
		if (singleton == null) {
			GCal b = new GCal();
			singleton = b;
		}
		return (singleton);
	}

	static private int tzOffset(long date) {
		return TimeZone.getDefault().getOffset(date);
	}

	private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
		// Load client secrets.
		File f = new File(Prefs.getPref(PrefName.GOOGLE_CRED_FILE));
		// InputStream in =
		// CalendarQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		try {
			InputStream in = new FileInputStream(f);

			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

			// Build flow and trigger user authorization request.
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
					clientSecrets, SCOPES)
					.setDataStoreFactory(
							new FileDataStoreFactory(new java.io.File(Prefs.getPref(PrefName.GOOGLE_TOKEN_DIR))))
					.setAccessType("offline").build();
			LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
			Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
			// returns an authorized Credential object.
			return credential;
		} catch (FileNotFoundException fe) {
			throw new Exception("Credentials File not found: " + Prefs.getPref(PrefName.GOOGLE_CRED_FILE));
		} catch (MalformedJsonException mj) {
			log.severe(mj.toString());
			throw new Exception("could not parse JSON credentials file: " + Prefs.getPref(PrefName.GOOGLE_CRED_FILE));
		}

	}

	// null out the google ids so that they are fetched again
	public void resetGoogleIds() {
		calendarId = null;
		taskList = null;
	}

	public void connect() throws Exception {

		if (service != null)
			return;

		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		tservice = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

	}

	/**
	 * Remove the sync relationship of all borg records by deleting the saved google
	 * json and regenrating all UIDs
	 * 
	 * @throws Exception
	 */
	public void unsync() throws Exception {

		// delete all URL fields and regenerate the UIDs
		for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {
			if (ap.getUrl() != null || ap.getUid() != null) {
				ap.setUrl(null);
				ap.setUid(null);
				AppointmentModel.getReference().saveAppt(ap, false);
			}
		}

		// questionable implementation to get around code in save() that preserves url
		// even when nulled out in object
		DBHelper.getController().execSQL("update tasks set url = null");
		DBHelper.getController().execSQL("update subtasks set url = null");

		for (net.sf.borg.model.entity.Task ap : TaskModel.getReference().getTasks()) {
			if (ap.getUrl() != null || ap.getUid() != null) {
				ap.setUrl(null);
				ap.setUid(null);
				TaskModel.getReference().savetask(ap, false);
			}
		}
		for (Subtask ap : TaskModel.getReference().getSubTasks()) {
			if (ap.getUrl() != null || ap.getUid() != null) {
				ap.setUrl(null);
				ap.setUid(null);
				TaskModel.getReference().saveSubTask(ap, false);
			}
		}

		// empty the sync log
		SyncLog.getReference().deleteAll();

	}

	public synchronized void sync(Integer years, boolean fullsync, boolean cleanup) throws Exception {

		Date after = null;
		GregorianCalendar gcal = new GregorianCalendar();

		gcal.add(java.util.Calendar.YEAR, -1 * ((years == null) ? 50 : years.intValue()));
		after = gcal.getTime();

		log.info("SYNC: Connect");
		connect();
		setIds();

		if (fullsync) {
			// get all appointments and add to syncmap
			for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {

				// limit by date
				Date latestInstance = Repeat.calculateLastRepeat(ap);
				if (latestInstance != null && latestInstance.before(after))
					continue;

				// never been synced - so force a new entry if none exists
				if (SyncLog.getReference().get(ap.getKey(), SyncableEntity.ObjectType.APPOINTMENT) == null) {
					SyncEvent event = new SyncEvent();
					event.setId(ap.getKey());
					event.setAction(Model.ChangeEvent.ChangeAction.ADD);
					event.setObjectType(SyncableEntity.ObjectType.APPOINTMENT);
					event.setUid(ap.getUid());
					if (ap.getUrl() != null) {
						event.setUrl(ap.getUrl());
						event.setAction(Model.ChangeEvent.ChangeAction.CHANGE);
					}
					SyncLog.getReference().insert(event);
				}

			}

			for (net.sf.borg.model.entity.Task ap : TaskModel.getReference().getTasks()) {
				if (SyncLog.getReference().get(ap.getKey(), SyncableEntity.ObjectType.TASK) == null) {
					SyncEvent event = new SyncEvent();
					event.setId(ap.getKey());
					event.setAction(Model.ChangeEvent.ChangeAction.ADD);
					event.setObjectType(SyncableEntity.ObjectType.TASK);
					event.setUid(ap.getUid());
					if (ap.getUrl() != null) {
						event.setUrl(ap.getUrl());
						event.setAction(Model.ChangeEvent.ChangeAction.CHANGE);
					}
					SyncLog.getReference().insert(event);
				}

			}
			for (Subtask ap : TaskModel.getReference().getSubTasks()) {
				if (SyncLog.getReference().get(ap.getKey(), SyncableEntity.ObjectType.SUBTASK) == null) {
					SyncEvent event = new SyncEvent();
					event.setId(ap.getKey());
					event.setAction(Model.ChangeEvent.ChangeAction.ADD);
					event.setObjectType(SyncableEntity.ObjectType.SUBTASK);
					event.setUid(ap.getUid());
					if (ap.getUrl() != null) {
						event.setUrl(ap.getUrl());
						event.setAction(Model.ChangeEvent.ChangeAction.CHANGE);
					}
					SyncLog.getReference().insert(event);
				}
			}

		}

		processSyncMap();

		syncFromServer(after, cleanup);

		// incoming sync could cause additional outward activity due to borg
		// needing to convert multiple events
		// into one - a limitation of borg
		processSyncMap();

		syncSubscribed(after);

		log.info("SYNC: Done");
	}

	public void setIds() throws Exception {

		String calname = Prefs.getPref(PrefName.GCAL_CAL_ID);
		String taskname = Prefs.getPref(PrefName.GCAL_TASKLIST_ID);
		subscribed.clear();

		CalendarList cals = service.calendarList().list().execute();
		log.fine(cals.toPrettyString());

		String subs = Prefs.getPref(PrefName.GOOGLE_SUBSCRIBED);
		if (subs != null && !subs.isEmpty()) {
			List<String> subList = Arrays.asList(subs.split(","));
			for (String s : subList) {
				for (CalendarListEntry c : cals.getItems()) {
					if (s.equals(c.getSummary()) || s.equals(c.getId())) {
						subscribed.add(c.getId());
						break;
					}
				}
			}
		}

		if (calendarId == null) {
			for (CalendarListEntry c : cals.getItems()) {
				if (calname.equals(c.getSummary()) || calname.equals(c.getId())) {
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

		ModalMessageServer.getReference().sendLogMessage("SYNC: Process " + num_outgoing + " Outgoing Items");
		log.info("SYNC: Process " + num_outgoing + " Outgoing Items");

		for (SyncEvent se : syncEvents) {
			try {
				if (se.getObjectType() == SyncableEntity.ObjectType.APPOINTMENT) {

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
										logBoth("*** WARNING ***");
										logBoth("Todo was changed in BORG that has no record of google task. Please check google. Manual delete may be needed.");
										logBoth(ap.toString());
										addTask(t);
									} else {

										// check if non-todo changing to todo
										String kind = EntityGCalAdapter.getKindFromJSON(se.getUrl());
										if (kind != null && kind.contains("event")) {
											logBoth("*** WARNING ***");
											logBoth("Event changed to Todo - removing from google calendar and adding to google tasks");
											logBoth(ap.toString());
											// need to delete event from google calendar and add new task
											String id = EntityGCalAdapter.getIdFromJSON(se.getUrl());
											if (id == null)
												id = se.getUid();
											Event comp = getEvent(id);

											if (comp != null) {
												log.info("SYNC: removeEvent: " + comp);
												try {
													removeEvent(comp.getId());
												} catch (IOException e) {
													ModalMessageServer.getReference().sendLogMessage(
															"SYNC ERROR for: " + se + ":" + e.getMessage());
													log.severe("SYNC ERROR for: " + se + ":" + e.getMessage());
												}
											}
											// null out url (google json) so that this will updated with the new task
											// json
											ap.setUrl(null);
											AppointmentModel.getReference().saveAppt(ap);
											t = EntityGCalAdapter.toGCalTask(ap);
											addTask(t);

										} else {
											updateTask(t);
										}
									}
								}
							} else { // non-todo being changed
								String id = EntityGCalAdapter.getIdFromJSON(se.getUrl());
								String kind = EntityGCalAdapter.getKindFromJSON(se.getUrl());

								Event ev = EntityGCalAdapter.toGCalEvent(ap);

								if (kind != null && kind.contains("task")) {
									// task changing to event
									// delete task and then add new event
									logBoth("*** WARNING ***");
									logBoth("Todo changed to Event - removing from google tasks and adding to google calendar");
									logBoth(ap.toString());
									String tid = EntityGCalAdapter.getIdFromTaskJSON(se.getUrl());
									if (tid != null)
										removeTask(tid);

									// null out url (google json) so that this will updated with the new event json
									ap.setUrl(null);
									AppointmentModel.getReference().saveAppt(ap);
									ev = EntityGCalAdapter.toGCalEvent(ap);
									addEvent(ev);
								} else {

									if (id == null)
										id = se.getUid();
									Event comp = getEvent(id);

									if (comp == null) {
										if (ev != null)
											addEvent(ev);

									} else {
										if (ev != null)
											updateEvent(ev);

									}
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
								if (id == null)
									id = se.getUid();
								Event comp = getEvent(id);

								if (comp != null) {
									log.info("SYNC: removeEvent: " + comp);
									try {
										removeEvent(comp.getId());
									} catch (IOException e) {
										ModalMessageServer.getReference()
												.sendLogMessage("SYNC ERROR for: " + se + ":" + e.getMessage());
										log.severe("SYNC ERROR for: " + se + ":" + e.getMessage());
									}

								} else {
									log.info("Deleted Appt: " + se.getUid() + " not found on server");
								}
							}
						}
					}
				}

				else if (se.getObjectType() == SyncableEntity.ObjectType.TASK) {

					if (se.getAction().equals(Model.ChangeEvent.ChangeAction.ADD)) {
						net.sf.borg.model.entity.Task task = TaskModel.getReference().getTask(se.getId());
						if (task != null) {
							Task t = EntityGCalAdapter.toGCalTask(task);
							if (t != null) {
								// check if task exists in google already
								if (t.getEtag() == null)
									addTask(t);
								else
									updateTask(t);
							}
						}

					} else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.CHANGE)) {

						net.sf.borg.model.entity.Task task = TaskModel.getReference().getTask(se.getId());
						if (task != null) {
							Task t = EntityGCalAdapter.toGCalTask(task);
							if (t != null) {
								// check if task exists in google already
								if (t.getEtag() == null) {
									// suspicious case - provide warning
									ModalMessageServer.getReference().sendLogMessage("*** WARNING ***");
									ModalMessageServer.getReference().sendLogMessage(
											"Task was changed in BORG that has no record of google task. Please check google. Manual delete may be needed.");
									ModalMessageServer.getReference().sendLogMessage(task.toString());
									addTask(t);
								} else
									updateTask(t);
							} else {
								String id = EntityGCalAdapter.getIdFromTaskJSON(se.getUrl());
								if (id != null)
									removeTask(id);
							}
						}

					} else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.DELETE)) {

						if (se.getUrl() != null) {
							if (se.getUrl().contains("tasks#task")) {
								String id = EntityGCalAdapter.getIdFromTaskJSON(se.getUrl());
								if (id != null)
									removeTask(id);
							}
						}
					}

				}

				else if (se.getObjectType() == SyncableEntity.ObjectType.SUBTASK) {

					if (se.getAction().equals(Model.ChangeEvent.ChangeAction.ADD)) {
						net.sf.borg.model.entity.Subtask task = TaskModel.getReference().getSubTask(se.getId());
						if (task != null) {
							Task t = EntityGCalAdapter.toGCalTask(task);
							if (t != null) {
								// check if task exists in google already
								if (t.getEtag() == null)
									addTask(t);
								else
									updateTask(t);
							}
						}

					} else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.CHANGE)) {

						net.sf.borg.model.entity.Subtask task = TaskModel.getReference().getSubTask(se.getId());
						if (task != null) {
							Task t = EntityGCalAdapter.toGCalTask(task);
							if (t != null) {
								// check if task exists in google already
								if (t.getEtag() == null) {
									// suspicious case - provide warning
									ModalMessageServer.getReference().sendLogMessage("*** WARNING ***");
									ModalMessageServer.getReference().sendLogMessage(
											"Subtask was changed in BORG that has no record of google task. Please check google. Manual delete may be needed.");
									ModalMessageServer.getReference().sendLogMessage(task.toString());
									addTask(t);
								} else
									updateTask(t);
							} else {
								String id = EntityGCalAdapter.getIdFromTaskJSON(se.getUrl());
								if (id != null)
									removeTask(id);
							}

						}

					} else if (se.getAction().equals(Model.ChangeEvent.ChangeAction.DELETE)) {

						if (se.getUrl() != null) {
							if (se.getUrl().contains("tasks#task")) {
								String id = EntityGCalAdapter.getIdFromTaskJSON(se.getUrl());
								if (id != null)
									removeTask(id);
							}
						}
					}

				}

				SyncLog.getReference().delete(se.getId(), se.getObjectType());
			} catch (Exception e) {
				ModalMessageServer.getReference().sendLogMessage("SYNC ERROR for: " + se + ":" + e.getMessage());
				log.severe("SYNC ERROR for: " + se + ":" + e.getMessage());
				e.printStackTrace();
			}

		}
	}

	private void updateTask(Task t) throws IOException {
		try {
			tservice.tasks().update(taskList, t.getId(), t).execute();
		} catch (Exception e) {
			logBoth("WARNING: google doesn't know about task: " + t.getId() + " " + t.getTitle() + " try to add...");
			addTask(t);
		}
	}

	private void removeTask(String id) throws IOException {
		log.fine("removeTask:" + id);
		try {
			tservice.tasks().delete(taskList, id).execute();
		} catch (IOException e) {
			if (e instanceof GoogleJsonResponseException ge) {
				if (ge.getDetails() != null && ge.getDetails().getCode() == 404) {
					// could not find task to delete in google - may have removed due date and then
					// deleted later
					logBoth("Task not found in google - ignoring:\n" + e.getMessage());
				} else
					throw e;

			} else
				throw e;
		}
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

	private void syncFromServer(Date after, boolean cleanup) throws Exception {

		logBoth("SYNC: Start Incoming Sync");

		String pageToken = "";
		int count = 0;
		ArrayList<String> serverUids = new ArrayList<String>();

		DateTime a = new DateTime(after);

		while (true) {
			Events events = service.events().list(calendarId).setMaxResults(1000).setPageToken(pageToken)
					.setSingleEvents(false).execute();
			List<Event> items = events.getItems();

			log.info("SYNC: " + a);

			logBoth("SYNC: found " + items.size() + " Event Calendars on server");

			for (Event event : items) {
				count += syncEvent(event, serverUids);
			}

			if (events.getNextPageToken() == null)
				break;

			pageToken = events.getNextPageToken();

		}

		logBoth("SYNC: processed " + count + " new/changed Events");

		count = 0;
		pageToken = "";
		while (true) {
			com.google.api.services.tasks.model.Tasks result2 = tservice.tasks().list(taskList).setMaxResults(100)
					.setPageToken(pageToken).execute();
			List<Task> tasks = result2.getItems();
			if (tasks != null) {
				logBoth("SYNC: found " + tasks.size() + " Tasks on server ");

				for (Task task : tasks) {
					count += syncTask(task, serverUids);
				}
			} else
				break;

			if (result2.getNextPageToken() == null)
				break;

			pageToken = result2.getNextPageToken();
		}
		logBoth("SYNC: processed " + count + " new/changed Tasks");

		log.fine(serverUids.toString());

		logBoth("SYNC: check for deletes");

		// find all appts in Borg that are not on the server
		for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {
			if (ap.getDate().before(after))
				continue;

			// if appt was not synced, then don't delete
			if (ap.getUrl() == null || !ap.getUrl().contains("etag")) {
				logBoth("-----------------------------------------------------");
				logBoth("*** Appointment is not synced with google??? " + ap);
				logBoth("-----------------------------------------------------");
				continue;
			}

			// NOTE - a delete of a google task will not cause delete of the BORG appt
			if (!serverUids.contains(ap.getUid())) {

				if (ap.isTodo() && !cleanup) {
					logBoth("-----------------------------------------------------");
					logBoth("*** Todo not found on google - WILL LEAVE IN BORG: " + ap);
					logBoth("*** Use Sync with cleanup option to delete or handle manually");
					logBoth("-----------------------------------------------------");

				} else {
					logBoth("Appointment Not Found on server - Deleting: " + ap);
					SyncLog.getReference().setProcessUpdates(false);
					AppointmentModel.getReference().delAppt(ap.getKey());
					SyncLog.getReference().setProcessUpdates(true);
				}

			}
		}

	}

	private int syncEvent(Event event, ArrayList<String> serverUids) throws Exception {

		log.fine("Incoming event: " + event.toString());

		String uid = event.getICalUID();
		
		// ignore cancelled events - gcal seems to keep a placeholder for cancelled instances in a repeating series
		if( uid == null && event.getStatus().equalsIgnoreCase("cancelled")) {
			log.info("Ignoring cancelled event: " + event.toString());
			return 0;
		}
		

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
			}
			return 1;
		} else {

			// if Borg has same etag as google, then skip
			if (ap.getUrl() != null) {
				Event orig = new GsonFactory().fromString(ap.getUrl(), Event.class);
				if (orig.getEtag().equals(event.getEtag())) {
					log.fine("Etags match - skipping " + event.getSummary());
					return 0;
				}
			}

			// ******* for recurring events, only update the text (and url) - so don't
			// update more than that on the google side
			// to make more changes than that - delete and add on google
			if (recur) {
				logBoth("*** recurring event, partial update only: " + event);
				try {
					ap.setText(newap.getText());
					ap.setUrl(newap.getUrl());

					SyncLog.getReference().setProcessUpdates(false);
					log.info("SYNC save: " + event);
					log.info("SYNC save: " + ap);
					AppointmentModel.getReference().saveAppt(ap);
				} finally {
					SyncLog.getReference().setProcessUpdates(true);
				}
				return 1;
			}

			try {
				newap.setKey(ap.getKey());
				newap.setReminderTimes(ap.getReminderTimes());
				newap.setEncrypted(ap.isEncrypted());

				SyncLog.getReference().setProcessUpdates(false);
				log.info("SYNC save: " + event);
				log.info("SYNC save: " + newap);
				AppointmentModel.getReference().saveAppt(newap);
			} finally {
				SyncLog.getReference().setProcessUpdates(true);
			}
			return 1;

		}

	}

	private int syncTask(Task task, ArrayList<String> serverUids) throws Exception {

		// limited task sync - google tasks are limited in functionality
		// BORG is the master. edits on the google side are not fully supported
		// Here are the limited set of sync conditions when updates are made to google
		// tasks:
		// 1. complete task on google -> do_todo on BORG
		// 2. move non-recurring tasks on google -> update date only in BORG
		// 3. date changed forward on recurring tasks - do_todo in BORG
		// this case assumes the task was completed using aCalendar+ on ANDROID, which
		// handles recurrence, and not a date change to the task
		// BORG recurring tasks are NOT recurring on the google side, except in
		// aCalendar+, so we are limited in what we should do when they
		// are updated on the google side
		// 4. brand new google task - save as new
		// Otherwise, no change in BORG

		log.fine("Incoming task: " + task.toPrettyString());

		int idx = -1;
		String notes = task.getNotes();
		if (notes != null)
			idx = notes.indexOf("UID:");
		if (idx != -1) {
			// match to BORG appt
			String uid = notes.substring(idx + 4);
			if (uid.contains("BORGT")) {
				// this is where a newly created task gets the URL updated
				net.sf.borg.model.entity.Task bt = TaskModel.getReference().getTaskByUid(uid);
				if (bt != null && bt.getUrl() == null) {
					bt.setUrl(task.toPrettyString());
					try {
						SyncLog.getReference().setProcessUpdates(false);
						log.info("SYNC save: " + bt);
						TaskModel.getReference().savetask(bt);
					} finally {
						SyncLog.getReference().setProcessUpdates(true);
					}
					return 1;
				}
				return 0;
			} else if (uid.contains("BORGS")) {
				// this is where a newly created task gets the URL updated
				net.sf.borg.model.entity.Subtask bt = TaskModel.getReference().getSubTaskByUid(uid);
				if (bt != null && bt.getUrl() == null) {
					bt.setUrl(task.toPrettyString());
					try {
						SyncLog.getReference().setProcessUpdates(false);
						log.info("SYNC save: " + bt);
						TaskModel.getReference().saveSubTask(bt);
					} finally {
						SyncLog.getReference().setProcessUpdates(true);
					}
					return 1;
				}
				return 0;
			}

			serverUids.add(uid);
			Appointment ap = AppointmentModel.getReference().getApptByUid(uid);
			if (ap == null) {
				logBoth("SYNC: ***WARNING*** could not find appt with UID: " + uid + " ignoring....");
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
					// return 1;
				}
			}

			if (task.getStatus().equals("completed")) {
				// do_todo
				logBoth("SYNC: do_todo: " + ap);
				AppointmentModel.getReference().do_todo(ap.getKey(), false);
				return 1;
			}

			// TODO - check if date chgd - case where acalendar+ updates recurring todo
			DateTime due = new DateTime(task.getDue());
			Date taskDate = new Date(due.getValue() - tzOffset(due.getValue()));
			log.fine("task due:" + taskDate);
			log.fine("ap due:" + ap.getDate());
			log.fine("ap nt:" + ap.getNextTodo());

			Date d = ap.getNextTodo();
			if (d == null)
				d = ap.getDate();

			log.fine("ap doe:" + DateUtil.dayOfEpoch(d));
			log.fine("task doe:" + DateUtil.dayOfEpoch(taskDate));

			if (Math.abs(taskDate.getTime() - d.getTime()) > 1000 * 60 * 60 * 12) {
				logBoth("TODO time changed on google for " + ap.getText());

				// if incoming date is greater than BORG date, then just do_todo
				if (ap.isRepeatFlag() && taskDate.getTime() - d.getTime() > 1000 * 60 * 60) { // 1 hr cushion - should
																								// be exact though?

					// try to calculate next todo, assuming aCalendar+ was used - which advances the
					// due date
					// loop through future todo times and see if we find a match
					for (Date nextTodo = AppointmentModel.getReference().next_todo(ap,
							null); nextTodo != null; nextTodo = AppointmentModel.getReference().next_todo(ap,
									nextTodo)) {

						if (DateUtil.dayOfEpoch(nextTodo) == DateUtil.dayOfEpoch(taskDate)) {
							// set next todo to incoming task's due date
							ap.setNextTodo(nextTodo);
							AppointmentModel.getReference().saveAppt(ap);
							return 1;
						}
					}

					// for whatever reason, the google task's due date is not a value next todo
					// date, so just advance the todo
					// one time and alert the user to check
					logBoth("-----------------------------------------------------");
					logBoth("CHECK: time advanced for repeating todo - do_todo - please verify");
					logBoth("-----------------------------------------------------");
					AppointmentModel.getReference().do_todo(ap.getKey(), false);
					return 1;
				} else if (!ap.isRepeatFlag()) {
					ap.setDate(DateUtil.setToMidnight(taskDate));
					ap.setText(task.getTitle());
					AppointmentModel.getReference().saveAppt(ap);
					logBoth("-----------------------------------------------------");
					logBoth("CHECK: non-repeating todo date change - please check:" + ap);
					logBoth("-----------------------------------------------------");

					return 1;
				}
			} else if (!ap.getTitle().equals(task.getTitle())) {
				// text only chg
				ap.setText(task.getTitle());
				AppointmentModel.getReference().saveAppt(ap);
				logBoth("-----------------------------------------------------");
				logBoth("CHECK: todo text-only change - please check:" + ap);
				logBoth("-----------------------------------------------------");
			}

		} else {
			// google created task - add new appt
			Appointment ap = EntityGCalAdapter.toBorg(task);
			if (ap == null) {
				log.info("Could not convert task: " + task);
				return 0;
			}
			AppointmentModel.getReference().saveAppt(ap);
			serverUids.add(ap.getUid());
			log.info("SYNC save from google-created task: " + ap);
			return 1;

		}

		return 0;

	}

	public com.google.api.services.calendar.model.Calendar getCalendar(String id) throws IOException {
		return service.calendars().get(id).execute();
	}

	public CalendarListEntry getCalendarListEntry(String id) throws IOException {
		return service.calendarList().get(id).execute();
	}

	// log to both logfile and SYNC popup
	private void logBoth(String s) {
		log.info(s);
		ModalMessageServer.getReference().sendLogMessage(s);
	}

	public void syncSubscribed(Date after) throws Exception {

		SubscribedCalendars.getReference().removeCals();
		for (String id : subscribed) {

			logBoth("SYNC: Start Incoming Sync of Subscribed Calendar: " + id);

			String pageToken = "";

			DateTime a = new DateTime(after);

			while (true) {
				Events events = service.events().list(id).setMaxResults(1000).setPageToken(pageToken)
						.setSingleEvents(false).execute();
				List<Event> items = events.getItems();

				log.info("SYNC: " + a);

				logBoth("SYNC: found " + items.size() + " Event Calendars on server");

				for (Event event : items) {

					Appointment newap = EntityGCalAdapter.toBorg(event);
					if (newap == null)
						continue;

					SubscribedCalendars.getReference().addEvent(newap, id);

				}

				if (events.getNextPageToken() == null)
					break;

				pageToken = events.getNextPageToken();

			}
		}

		SubscribedCalendars.getReference().createCache();
		SubscribedCalendars.getReference().refresh();
	}

}
