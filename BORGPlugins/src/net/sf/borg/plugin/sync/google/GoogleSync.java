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
import com.google.gdata.data.DateTime;
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

	static public void dump() throws Exception {

		CalendarService myService = new CalendarService("BORG");
		String user = Prefs.getPref(SYNCUSER);
		String pw = GoogleSyncOptionsPanel.gep();
		myService.setUserCredentials(user, pw);

		URL eventUrl = new URL("http://www.google.com/calendar/feeds/" + user
				+ "/private/full");

		CalendarEventFeed myFeed = myService.getFeed(eventUrl,
				CalendarEventFeed.class);
		List<CalendarEventEntry> entries = myFeed.getEntries();
		for (CalendarEventEntry entry : entries) {
			System.out.println(entry.getIcalUID() + " " + entry.getSequence()
					+ " " + entry.getTitle().getPlainText() + " "
					+ entry.getPublished().toUiString() + " "
					+ entry.getEdited().toUiString());
		}
	}

	static public void sync() throws Exception {

		GoogleAppointmentAdapter ad = new GoogleAppointmentAdapter();

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
		query.setMaxResults(10000);
		CalendarEventFeed myFeed = myService.query(query, CalendarEventFeed.class);
		List<CalendarEventEntry> entries = myFeed.getEntries();
		System.out.println("Entries: " + entries.size());
		for (CalendarEventEntry entry : entries) {

			
			if (entry.getSequence() == 0
					|| Math.abs(entry.getPublished().getValue() - entry.getEdited().getValue()) > 5*60*1000) {
				//Appointment appt = ad.toBorg(entry);
				//AppointmentModel.getReference().saveAppt(appt);
				System.out.println("Needs Sync: " + entry.getIcalUID() + " " + entry.getSequence()
					+ " " + entry.getTitle().getPlainText() + " "
					+ entry.getPublished().toUiString() + " "
					+ entry.getEdited().toUiString());
			}
		
			BatchUtils.setBatchId(entry, Integer.toString(count));
			BatchUtils.setBatchOperationType(entry, BatchOperationType.DELETE);
			batchDeleteRequest.getEntries().add(entry);
			count++;
			

		}
		System.out.println("Batch Count: " + count);


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
				//System.out.println(entry.getIcalUID() + " "
				//		+ entry.getTitle().getPlainText());
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
		
		// send all borg events to google
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
		CalendarEventFeed batchResponse = myService.batch(new URL(batchLink.getHref()),
				batchInsertRequest);

		// Ensure that all the operations were successful.
		boolean isSuccess = true;
		for (CalendarEventEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			//System.out.println(entry.getId() + " "
			//		+ entry.getTitle().getPlainText());
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

}
