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

	static class SyncThread extends Thread {

		private ModalMessage modalMessage = null;

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
				doSync();
			} catch (Exception e) {
				e.printStackTrace();
				this.showMessage(e.getLocalizedMessage(), true);
				return;
			}

			this.showMessage("Google Sync Finished", true);

		}

		private void doSync() throws Exception {

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

			URL eventUrl = new URL("http://www.google.com/calendar/feeds/"
					+ user + "/private/full");

			CalendarEventFeed batchDeleteRequest = new CalendarEventFeed();
			int count = 1;

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
			for (CalendarEventEntry entry : entries) {

				/*
				 * borg sets sequence = BORG_SEQUENCE when sending to google, so
				 * anything with sequence != BORG_SEQUENCE was created on google (noticed by
				 * trial/error) 
				 */		
				 
				if (!GoogleAppointmentAdapter.isFromBORG(entry)) {
					this.showMessage("Needs Sync: " + entry.getIcalUID() + " "
							+ entry.getSequence() + " "
							+ entry.getTitle().getPlainText() + " "
							+ entry.getUpdated().getValue(), false);
					try {
						Appointment appt = ad.toBorg(entry);
						AppointmentModel.getReference().saveAppt(appt);
					} catch (Exception e) {
						this.showMessage(e.getMessage(), false);
						continue;
					}

				}
				else
				{
					// sync if the appt has changed
					DateTime updt = entry.getUpdated();
					if( updt != null )
					{
						String uid = entry.getIcalUID();
						int idx = uid.indexOf('@');
						String up = uid.substring(idx+"@BORGCalendar".length());
						try {
							long uplong = Long.parseLong(up);
							if( updt.getValue() > uplong )
							{
								Appointment appt = ad.toBorg(entry);
								this.showMessage("Needs Sync: " + entry.getIcalUID() + " "
										+ entry.getSequence() + " "
										+ entry.getTitle().getPlainText() + " "
										+ entry.getUpdated().getValue(), false);
								AppointmentModel.getReference().saveAppt(appt);
							}
						} catch (Exception e) {
							this.showMessage(e.getMessage(), false);
						}
					}
				}

				// add every appt to a batch request to delete them all
				// there is no simple way to empty the primary calendar
				BatchUtils.setBatchId(entry, Integer.toString(count));
				BatchUtils.setBatchOperationType(entry,
						BatchOperationType.DELETE);
				batchDeleteRequest.getEntries().add(entry);
				count++;

			}

			// only delete if there were appts in google
			if (count > 1) {
				// delete all events
				myFeed = myService.getFeed(eventUrl, CalendarEventFeed.class);
				Link batchLink = myFeed.getLink(Link.Rel.FEED_BATCH,
						Link.Type.ATOM);

				CalendarEventFeed batchResponse = myService.batch(new URL(
						batchLink.getHref()), batchDeleteRequest);

				// Ensure that all the operations were successful.
				boolean isSuccess = true;
				for (CalendarEventEntry entry : batchResponse.getEntries()) {

					String batchId = BatchUtils.getBatchId(entry);
					if (!BatchUtils.isSuccess(entry)) {
						isSuccess = false;
						BatchStatus status = BatchUtils.getBatchStatus(entry);
						this.showMessage(
								"\n" + batchId + " failed ("
										+ status.getReason() + ") "
										+ status.getContent(), false);
					}
				}
				if (isSuccess) {
					this.showMessage(
							"Successfully processed all events via batch request.",
							false);
				}
			}

			// now that borg has the latest synced view of all appts, send all
			// borg
			// events to google
			CalendarEventFeed batchInsertRequest = new CalendarEventFeed();

			GregorianCalendar cal = new GregorianCalendar();
			Collection<Appointment> appts = AppointmentModel.getReference()
					.getAllAppts();

			count = 1;

			for (Appointment appt : appts) {
				cal.setTime(appt.getDate());
				// always sync repeating appts. easier than calculating extent
				if (Repeat.isRepeating(appt) || cal.get(Calendar.YEAR) >= syncFromYear) {
					try {
						CalendarEventEntry ev = ad.fromBorg(appt);
						BatchUtils.setBatchId(ev, Integer.toString(count));
						BatchUtils.setBatchOperationType(ev,
								BatchOperationType.INSERT);
						batchInsertRequest.getEntries().add(ev);
						count++;
					} catch (Exception e) {
						this.showMessage(e.getLocalizedMessage(), false);
					}
				}
			}

			Link batchLink = myFeed
					.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
			CalendarEventFeed batchResponse = myService.batch(
					new URL(batchLink.getHref()), batchInsertRequest);

			// Ensure that all the operations were successful.
			boolean isSuccess = true;
			for (CalendarEventEntry entry : batchResponse.getEntries()) {
				String batchId = BatchUtils.getBatchId(entry);
				if (!BatchUtils.isSuccess(entry)) {
					isSuccess = false;
					BatchStatus status = BatchUtils.getBatchStatus(entry);
					this.showMessage(batchId + "-"
							+ entry.getTitle().getPlainText() + " failed ("
							+ status.getReason() + ") " + status.getContent(),
							false);
				}
			}
			if (isSuccess) {
				this.showMessage(
						"Successfully processed all events via batch request.",
						false);
			}

		}

	}

	/**
	 * do a sync and wipe. Since Googel cannot fully support everything in the
	 * borg calendar model, just sync google changes to borg and then wipe and
	 * update the entire google calendar with borg data. The changes that can be
	 * made via google and then saved on borg are limited
	 * 
	 * currently, borg will not detect when appts are deleted from google. The
	 * appts will remain in borg
	 * 
	 * @throws Exception
	 */
	static public void sync() throws Exception {
		new SyncThread().start();
	}

}
