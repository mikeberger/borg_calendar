package net.sf.borg.plugin.sync.google;

import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.SwingUtilities;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.ui.util.ModalMessage;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.Link;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;

public class GoogleSync {

	static public PrefName SYNCUSER = new PrefName("googlesync-user", "");
	static public PrefName SYNCPW = new PrefName("googlesync-pw", "");
	static public PrefName SYNCPW2 = new PrefName("googlesync-pw2", "");
	static public PrefName BATCH_CHUNK_SIZE = new PrefName(
			"googlesync-chunk-size", new Integer(500));

	static private final int years_to_sync = 2;

	static class SyncThread extends Thread {

		private ModalMessage modalMessage = null;
		private boolean overwrite = false;

		public boolean isOverwrite() {
			return overwrite;
		}

		public void setOverwrite(boolean overwrite) {
			this.overwrite = overwrite;
		}

		void showMessage(String s, boolean unlock) {
			System.out.println(s);
			final String fs = s;
			final boolean funlock = unlock;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (modalMessage.isShowing()) {
						modalMessage.appendText(fs);
						if (funlock == true)
							modalMessage.setEnabled(true);
					}
				}
			});
		}

		@Override
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {
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
				if (overwrite == true)
					doOverwrite();
				else
					doSync();
			} catch (Exception e) {
				e.printStackTrace();
				this.showMessage(e.getLocalizedMessage(), true);
				return;
			}

			this.showMessage("Google Sync Finished", true);

		}

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

		private void insertEvents(List<CalendarEventEntry> entries,
				CalendarService myService, URL eventUrl) throws Exception {

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
			this.insertEvents(entries, myService, eventUrl);
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
			this.insertEvents(entries, myService, eventUrl);

		}
	}

	static public void sync() throws Exception {
		new SyncThread().start();
	}

	public static void overwrite() {
		SyncThread thread = new SyncThread();
		thread.setOverwrite(true);
		thread.start();

	}

}
