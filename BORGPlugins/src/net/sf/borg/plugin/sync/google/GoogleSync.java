package net.sf.borg.plugin.sync.google;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.SwingUtilities;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Warning;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.plugin.sync.SyncLog;
import net.sf.borg.ui.util.ModalMessage;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.When;

public class GoogleSync {

	static public enum SyncMode {
		OVERWRITE, SYNC, SYNC_OVERWRITE;
	}

	static class SyncThread extends Thread {

		private ModalMessage modalMessage = null;
		private SyncMode mode = SyncMode.SYNC;

		private void deleteEvents(List<CalendarEventEntry> entries,
				CalendarService myService, URL eventUrl) throws Exception {

			CalendarEventFeed batchDeleteRequest = new CalendarEventFeed();
			CalendarEventFeed myFeed = myService.getFeed(eventUrl,
					CalendarEventFeed.class);
			Link batchLink = myFeed
					.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);

			int chunk_size = Prefs.getIntPref(BATCH_CHUNK_SIZE);

			int count = 1;
			for (CalendarEventEntry entry : entries) {

				// add every appt to a batch request to delete them all
				// there is no simple way to empty the primary calendar
				BatchUtils.setBatchId(entry, Integer.toString(count));
				BatchUtils.setBatchOperationType(entry,
						BatchOperationType.DELETE);
				batchDeleteRequest.getEntries().add(entry);
				count++;

				if (count == chunk_size) {
					// send a chunk
					this.showMessage("Sending Delete Batch", false);
					CalendarEventFeed batchResponse = myService.batch(new URL(
							batchLink.getHref()), batchDeleteRequest);
					this.processBatchResponse(batchResponse);
					this.showMessage("Delete Batch Done", false);

					batchDeleteRequest = new CalendarEventFeed();
					count = 1;
				}

			}

			if (count > 1) {
				this.showMessage("Sending Delete Batch", false);

				CalendarEventFeed batchResponse = myService.batch(new URL(
						batchLink.getHref()), batchDeleteRequest);

				this.processBatchResponse(batchResponse);
				this.showMessage("Delete Batch Done", false);

			}
		}

		private void doOverwrite() throws Exception {

			CalendarService myService = new CalendarService("BORG");
			String user = Prefs.getPref(SYNCUSER);
			String pw = GoogleSyncOptionsPanel.gep();
			myService.setUserCredentials(user, pw);

			URL eventUrl = new URL("http://www.google.com/calendar/feeds/"
					+ user + "/private/full");

			List<CalendarEventEntry> entries = this.getEvents(myService,
					eventUrl);

			// delete events from google
			this.deleteEvents(entries, myService, eventUrl);

			// insert all borg events
			this.insertAllEvents(myService, eventUrl);

			SyncLog.getReference().deleteAll();

		}

		private void doSync() throws Exception {

			GoogleAppointmentAdapter ad = new GoogleAppointmentAdapter();

			CalendarService myService = new CalendarService("BORG");
			String user = Prefs.getPref(SYNCUSER);
			String pw = GoogleSyncOptionsPanel.gep();
			myService.setUserCredentials(user, pw);

			URL eventUrl = new URL("http://www.google.com/calendar/feeds/"
					+ user + "/private/full");

			List<CalendarEventEntry> entries = this.getEvents(myService,
					eventUrl);

			List<CalendarEventEntry> toBeDeleted = new ArrayList<CalendarEventEntry>();

			for (CalendarEventEntry entry : entries) {

				try {
					// if chgd or deleted from borg - ignore google entry and
					// delete
					int id = ad.getBorgId(entry);
					if (id != -1) {
						ChangeEvent event = SyncLog.getReference().get(id);
						if (event != null
								&& (event.getAction() == ChangeEvent.ChangeAction.CHANGE || event
										.getAction() == ChangeEvent.ChangeAction.DELETE)) {
							toBeDeleted.add(entry);
							this.showMessage(
									"BORG CHANGED/DELETE from Google: " + id
											+ " " + dumpEntry(entry), false);
							continue;
						}
					}

					Appointment appt = ad.toBorg(entry);

					// delete all entries that caused a borg update - will be
					// sent back over
					
					toBeDeleted.add(entry);
					this.showMessage("GOOGLE CHANGED/DELETE from Google: " + id
							+ " " + dumpEntry(entry), false);

					AppointmentModel.getReference().saveAppt(appt);
				} catch (Warning w) {
					System.out.println(w.getMessage());
					continue;
				} catch (Exception e) {
					this.showMessage("doSync1: " + e.getMessage(), false);
					e.printStackTrace();
					continue;
				}

			}

			// delete events from google
			this.deleteEvents(toBeDeleted, myService, eventUrl);

			// insert all borg events that have chg or add entries in sync log
			this.insertChangedEvents(myService, eventUrl);

			SyncLog.getReference().deleteAll();

		}

		private void doSyncOverwrite() throws Exception {

			GoogleAppointmentAdapter ad = new GoogleAppointmentAdapter();

			CalendarService myService = new CalendarService("BORG");
			String user = Prefs.getPref(SYNCUSER);
			String pw = GoogleSyncOptionsPanel.gep();
			myService.setUserCredentials(user, pw);

			URL eventUrl = new URL("http://www.google.com/calendar/feeds/"
					+ user + "/private/full");

			List<CalendarEventEntry> entries = this.getEvents(myService,
					eventUrl);

			for (CalendarEventEntry entry : entries) {

				try {
					Appointment appt = ad.toBorg(entry);
					AppointmentModel.getReference().saveAppt(appt);
				} catch (Exception e) {
					this.showMessage(e.getMessage(), false);
					continue;
				}

			}

			// delete events from google
			this.deleteEvents(entries, myService, eventUrl);

			// insert all borg events
			this.insertAllEvents(myService, eventUrl);

			SyncLog.getReference().deleteAll();

		}

		private List<CalendarEventEntry> getEvents(CalendarService myService,
				URL eventUrl) throws Exception {

			// get all new & changed appts
			CalendarQuery query = new CalendarQuery(eventUrl);

			// it is possible that the data may get large enough to exceed the
			// amount of
			// data that google will return in one query. If that ever happens,
			// then
			// paging can be done. For now, just up the max limit and hope.
			query.setMaxResults(10000);
			CalendarEventFeed myFeed = myService.query(query,
					CalendarEventFeed.class);
			List<CalendarEventEntry> entries = myFeed.getEntries();
			this.showMessage("Current Google Entries: " + entries.size(), false);
			return entries;

		}

		public SyncMode getMode() {
			return mode;
		}

		private void insertAllEvents(CalendarService myService, URL eventUrl)
				throws Exception {

			CalendarEventFeed batchInsertRequest = new CalendarEventFeed();
			CalendarEventFeed myFeed = myService.getFeed(eventUrl,
					CalendarEventFeed.class);
			Link batchLink = myFeed
					.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);

			GregorianCalendar cal = new GregorianCalendar();
			Collection<Appointment> appts = AppointmentModel.getReference()
					.getAllAppts();

			int chunk_size = Prefs.getIntPref(BATCH_CHUNK_SIZE);

			int count = 1;

			GoogleAppointmentAdapter ad = new GoogleAppointmentAdapter();
			
			int years_to_sync = Prefs.getIntPref(SYNCYEARS);

			int syncFromYear = new GregorianCalendar().get(Calendar.YEAR)
					- years_to_sync;

			for (Appointment appt : appts) {
				cal.setTime(appt.getDate());
				// always sync repeating appts. easier than calculating extent
				if (Repeat.isRepeating(appt)
						|| cal.get(Calendar.YEAR) >= syncFromYear) {
					try {
						CalendarEventEntry ev = ad.fromBorg(appt);
						BatchUtils.setBatchId(ev, Integer.toString(count));
						BatchUtils.setBatchOperationType(ev,
								BatchOperationType.INSERT);
						batchInsertRequest.getEntries().add(ev);
						count++;

						if (count == chunk_size) {
							// send a chunk
							this.showMessage("Sending Insert Batch", false);
							CalendarEventFeed batchResponse = myService.batch(
									new URL(batchLink.getHref()),
									batchInsertRequest);
							this.processBatchResponse(batchResponse);
							this.showMessage("Insert Batch Done", false);

							batchInsertRequest = new CalendarEventFeed();
							count = 1;
						}
					} catch (Exception e) {
						this.showMessage(e.getLocalizedMessage(), false);
					}
				}
			}

			if (count > 1) {
				this.showMessage("Sending Insert Batch", false);

				CalendarEventFeed batchResponse = myService.batch(new URL(
						batchLink.getHref()), batchInsertRequest);
				this.processBatchResponse(batchResponse);
				this.showMessage("Insert Batch Done", false);

			}
		}

		private void insertChangedEvents(CalendarService myService, URL eventUrl)
				throws Exception {

			CalendarEventFeed batchInsertRequest = new CalendarEventFeed();
			CalendarEventFeed myFeed = myService.getFeed(eventUrl,
					CalendarEventFeed.class);
			Link batchLink = myFeed
					.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);

			int chunk_size = Prefs.getIntPref(BATCH_CHUNK_SIZE);

			int count = 1;

			GoogleAppointmentAdapter ad = new GoogleAppointmentAdapter();

			List<ChangeEvent> events = SyncLog.getReference().getAll();
			for (ChangeEvent event : events) {
				if (event.getAction() == ChangeEvent.ChangeAction.ADD
						|| event.getAction() == ChangeEvent.ChangeAction.CHANGE) {
					Integer key = (Integer) event.getObject();
					Appointment appt = AppointmentModel.getReference().getAppt(
							key.intValue());
					try {
						CalendarEventEntry ev = ad.fromBorg(appt);
						BatchUtils.setBatchId(ev, Integer.toString(count));
						BatchUtils.setBatchOperationType(ev,
								BatchOperationType.INSERT);
						batchInsertRequest.getEntries().add(ev);
						this.showMessage("BORG CHANGED/SEND TO GOOGLE: " + key
								+ " " + dumpEntry(ev), false);

						count++;

						if (count == chunk_size) {
							// send a chunk
							this.showMessage("Sending Insert Batch", false);
							CalendarEventFeed batchResponse = myService.batch(
									new URL(batchLink.getHref()),
									batchInsertRequest);
							this.processBatchResponse(batchResponse);
							this.showMessage("Insert Batch Done", false);

							batchInsertRequest = new CalendarEventFeed();
							count = 1;
						}
					} catch (Exception e) {
						this.showMessage("insertChangedEvents1: " + e.getLocalizedMessage(), false);
					}
				}
			}

			if (count > 1) {
				this.showMessage("Sending Insert Batch", false);

				CalendarEventFeed batchResponse = myService.batch(new URL(
						batchLink.getHref()), batchInsertRequest);
				this.processBatchResponse(batchResponse);
				this.showMessage("Insert Batch Done", false);

			}
		}

		private void processBatchResponse(CalendarEventFeed response) {
			// Ensure that all the operations were successful.
			for (CalendarEventEntry entry : response.getEntries()) {
				String batchId = BatchUtils.getBatchId(entry);
				if (!BatchUtils.isSuccess(entry)) {
					BatchStatus status = BatchUtils.getBatchStatus(entry);
					this.showMessage(batchId + "-"
							+ entry.getTitle().getPlainText() + " failed ("
							+ status.getReason() + ") " + status.getContent(),
							false);
				}
			}

		}

		@Override
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (modalMessage == null || !modalMessage.isShowing()) {
						modalMessage = new ModalMessage("Google Sync Started",
								false);
						modalMessage.setVisible(true);
					} else {
						modalMessage.appendText("Google Sync Started");
					}
					modalMessage.setEnabled(false);
					modalMessage.toFront();
				}
			});

			try {
				if (mode == SyncMode.OVERWRITE)
					doOverwrite();
				else if (mode == SyncMode.SYNC_OVERWRITE)
					doSyncOverwrite();
				else
					doSync();
			} catch (Exception e) {
				e.printStackTrace();
				this.showMessage("run1: " + e.getLocalizedMessage(), true);
				return;
			}

			this.showMessage("Google Sync Finished", true);

		}

		public void setMode(SyncMode mode) {
			this.mode = mode;
		}

		void showMessage(String s, boolean unlock) {
			System.out.println(s);
			final String fs = s;
			final boolean funlock = unlock;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (modalMessage.isShowing()) {
						modalMessage.appendText(fs);
						if (funlock == true)
							modalMessage.setEnabled(true);
					}
				}
			});
		}

	}

	static public PrefName BATCH_CHUNK_SIZE = new PrefName(
			"googlesync-chunk-size", new Integer(500));
	static public PrefName SYNCPW = new PrefName("googlesync-pw", "");

	static public PrefName SYNCPW2 = new PrefName("googlesync-pw2", "");

	static public PrefName SYNCUSER = new PrefName("googlesync-user", "");
	
	static public PrefName SYNCYEARS = new PrefName(
			"sync-years", new Integer(10));
	
	static public PrefName NEW_ONLY = new PrefName("googlesync-oneway", "true");

	static public void sync(SyncMode syncmode) throws Exception {
		
		SyncThread thread = new SyncThread();
		thread.setMode(syncmode);
		thread.start();
	}

	static private String dumpEntry(CalendarEventEntry entry) {
		List<When> whens = entry.getTimes();
		if( whens == null || whens.isEmpty())
		{
			return ("[No Times (recurs?)|"
					+ entry.getTitle().getPlainText().trim() + "]");
		}
		When when = whens.get(0);
		DateTime start = when.getStartTime();
		return ("["
				+ DateFormat.getDateTimeInstance().format(
						new Date(start.getValue())) + "|"
				+ entry.getTitle().getPlainText().trim() + "]");
	}

}
