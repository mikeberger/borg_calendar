package net.sf.borg.plugin.sync.google;

import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.entity.Appointment;

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

	/**
	 * do a sync and wipe. Since Googel cannot fully support everything in the
	 * borg calendar model, just sync google changes to borg and then wipe and
	 * update the entire google calendar with borg data. The changes that can be
	 * made via google and then saved on borg are limited
	 * 
	 * currently, borg will not detect when appts are deleted from google. The appts will remain in borg
	 * 
	 * @throws Exception
	 */
	static public void sync() throws Exception {

		System.out.println("Google Sync Started");

		GoogleAppointmentAdapter ad = new GoogleAppointmentAdapter();

		/**
		 **************** only sync last 2 years ************8
		 */
		int years_to_sync = 2;
		int syncFromYear = new GregorianCalendar().get(Calendar.YEAR)
				- years_to_sync;

		CalendarService myService = new CalendarService("BORG");
		String user = Prefs.getPref(SYNCUSER);
		String pw = GoogleSyncOptionsPanel.gep();
		myService.setUserCredentials(user, pw);

		URL eventUrl = new URL("http://www.google.com/calendar/feeds/" + user
				+ "/private/full");

		CalendarEventFeed batchDeleteRequest = new CalendarEventFeed();
		int count = 1;

		// get all new & changed appts
		CalendarQuery query = new CalendarQuery(eventUrl);

		// it is possible that the data may get large enough to exceed the
		// amount of
		// data that google will return in one query. If that ever happens, then
		// paging can be done. For now, just up the max limit and hope.
		query.setMaxResults(10000);
		CalendarEventFeed myFeed = myService.query(query,
				CalendarEventFeed.class);
		List<CalendarEventEntry> entries = myFeed.getEntries();
		System.out.println("Current Google Entries: " + entries.size());
		for (CalendarEventEntry entry : entries) {

			/*
			 * borg sets sequence = 1 when sending to google, so anything with
			 * sequence == 0 was created on google (noticed by trial/error) To
			 * determine events changed on google, the publish (create) and edit
			 * (update) dates are compared. Need to check for a difference of a
			 * fw minutes between the times and the times are not the same for
			 * newly insert appts (also found by trial/error) so - after sync,
			 * don't update via google for 5 mins
			 */
			if (entry.getSequence() == 0
					|| Math.abs(entry.getPublished().getValue()
							- entry.getEdited().getValue()) > 5 * 60 * 1000) {
				System.out.println("Needs Sync: " + entry.getIcalUID() + " "
						+ entry.getSequence() + " "
						+ entry.getTitle().getPlainText() + " "
						+ entry.getPublished().toUiString() + " "
						+ entry.getEdited().toUiString());
				Appointment appt = ad.toBorg(entry);
				AppointmentModel.getReference().saveAppt(appt);
			}

			// add every appt to a batch request to delete them all
			// there is no simple way to empty the primary calendar
			BatchUtils.setBatchId(entry, Integer.toString(count));
			BatchUtils.setBatchOperationType(entry, BatchOperationType.DELETE);
			batchDeleteRequest.getEntries().add(entry);
			count++;

		}
		System.out.println("Batch Count: " + count);

		// only delete if there were appts in google
		if (count > 1) {
			// delete all events
			myFeed = myService.getFeed(eventUrl, CalendarEventFeed.class);
			Link batchLink = myFeed
					.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);

			CalendarEventFeed batchResponse = myService.batch(new URL(batchLink
					.getHref()), batchDeleteRequest);

			// Ensure that all the operations were successful.
			boolean isSuccess = true;
			for (CalendarEventEntry entry : batchResponse.getEntries()) {

				String batchId = BatchUtils.getBatchId(entry);
				if (!BatchUtils.isSuccess(entry)) {
					isSuccess = false;
					BatchStatus status = BatchUtils.getBatchStatus(entry);
					System.out.println("\n" + batchId + " failed ("
							+ status.getReason() + ") " + status.getContent());
				}
			}
			if (isSuccess) {
				System.out
						.println("Successfully processed all events via batch request.");
			}
		}

		// now that borg has the latest synced view of all appts, send all borg events to google
		CalendarEventFeed batchInsertRequest = new CalendarEventFeed();

		GregorianCalendar cal = new GregorianCalendar();
		Collection<Appointment> appts = AppointmentModel.getReference()
				.getAllAppts();

		count = 1;

		for (Appointment appt : appts) {
			cal.setTime(appt.getDate());
			if (cal.get(Calendar.YEAR) >= syncFromYear) {
				CalendarEventEntry ev = ad.fromBorg(appt);
				BatchUtils.setBatchId(ev, Integer.toString(count));
				BatchUtils.setBatchOperationType(ev, BatchOperationType.INSERT);
				batchInsertRequest.getEntries().add(ev);
				count++;
			}
		}

		Link batchLink = myFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
		CalendarEventFeed batchResponse = myService.batch(new URL(batchLink
				.getHref()), batchInsertRequest);

		// Ensure that all the operations were successful.
		boolean isSuccess = true;
		for (CalendarEventEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			// System.out.println(entry.getId() + " "
			// + entry.getTitle().getPlainText());
			if (!BatchUtils.isSuccess(entry)) {
				isSuccess = false;
				BatchStatus status = BatchUtils.getBatchStatus(entry);
				System.out.println("\n" + batchId + " failed ("
						+ status.getReason() + ") " + status.getContent());
			}
		}
		if (isSuccess) {
			System.out
					.println("Successfully processed all events via batch request.");
		}

		System.out.println("Google Sync Finished");

	}

}
