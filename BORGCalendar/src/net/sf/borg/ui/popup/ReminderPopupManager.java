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
/*
 * popups.java
 *
 * Created on January 16, 2004, 3:08 PM
 */

package net.sf.borg.ui.popup;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.entity.Appointment;

/**
 * Manages the lifecycle of Reminder Popup windows.
 */
public class ReminderPopupManager implements Model.Listener {

	/**
	 * a class for holding a unique instance of an appointment that treats
	 * repeats of an appointment as different instances from the original
	 * 
	 */
	static private class AppointmentInstance {

		// the appointment
		private Appointment appt;

		// the instance time - this is the time when this instance occurs. this
		// will be
		// different from the appt time if this instance is a repeat of an
		// appointment
		private Date instanceTime;

		/**
		 * constructor
		 * 
		 * @param appt
		 *            the appointment
		 * @param instancetime
		 *            the instance time.
		 */
		public AppointmentInstance(Appointment appt, Date instanceTime) {
			this.appt = appt;
			this.instanceTime = instanceTime;
		}

		/**
		 * get appointment
		 * 
		 * @return the appointment
		 */
		public Appointment getAppt() {
			return appt;
		}

		/**
		 * get the instance time
		 * 
		 * @return the instance time
		 */
		public Date getInstanceTime() {
			return instanceTime;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((appt == null) ? 0 : appt.getKey());
			result = prime * result
					+ ((instanceTime == null) ? 0 : instanceTime.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AppointmentInstance other = (AppointmentInstance) obj;
			if (appt == null) {
				if (other.appt != null)
					return false;
			} else if (appt.getKey() != other.appt.getKey()) {
				return false;
			}

			if (instanceTime == null) {
				if (other.instanceTime != null)
					return false;
			} else if (!instanceTime.equals(other.instanceTime))
				return false;
			return true;
		}

	}

	/** The singleton. */
	static private ReminderPopupManager singleton = null;

	// map that maps appointment keys to the associated popup reminder
	// windows
	private HashMap<AppointmentInstance, ReminderPopup> pops = new HashMap<AppointmentInstance, ReminderPopup>();

	/**
	 * The timer that periodically checks to see if we need to popup a new
	 * reminder
	 */
	private Timer timer = null;

	/** runnable task that checks if we need to pop up any popups */
	final Runnable doPopupChk = new Runnable() {
		public void run() {
			checkPopups();
		}
	};

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 */
	public static ReminderPopupManager getReference() {
		if (singleton == null) {
			singleton = new ReminderPopupManager();
		}
		return singleton;
	}

	/**
	 * constructor
	 */
	private ReminderPopupManager() {

		// listen for appointment model changes
		AppointmentModel.getReference().addListener(this);

		// start the popup timer
		// for consistency - it will start at the beginning of the next minute
		// the interval between runs is a user preference
		timer = new Timer();
		GregorianCalendar cur = new GregorianCalendar();
		int secs_left = 60 - cur.get(Calendar.SECOND);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				EventQueue.invokeLater(doPopupChk);
			}
		}, secs_left * 1000,
				Prefs.getIntPref(PrefName.REMINDERCHECKMINS) * 60 * 1000);

	}

	/**
	 * stop the timer and remove all popups
	 */
	public void remove() {
		timer.cancel();

		Set<Entry<AppointmentInstance, ReminderPopup>> entrySet = pops
				.entrySet();
		for (Entry<AppointmentInstance, ReminderPopup> popupMapEntry : entrySet) {

			ReminderPopup popup = popupMapEntry.getValue();
			if (popup == null || !popup.isDisplayable())
				continue;
			
			popup.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model.Listener#refresh()
	 */
	/**
	 * refresh the popup states depending on the state of the appointment model.
	 * In particular, clean up any popups that should no longer be shown because
	 * of model changes
	 */
	public void refresh() {

		// list of keys to appointments that no longer need popups
		ArrayList<AppointmentInstance> deletedPopupKeys = new ArrayList<AppointmentInstance>();

		// set of popup map entries
		Set<Entry<AppointmentInstance, ReminderPopup>> entrySet = pops
				.entrySet();

		// loop through the existing popups
		for (Entry<AppointmentInstance, ReminderPopup> mapEntry : entrySet) {

			AppointmentInstance apptInstance = mapEntry.getKey();

			// get the popup window
			ReminderPopup popupWindow = mapEntry.getValue();

			// if frame is gone (killed already), then skip it
			if (popupWindow == null)
				continue;

			// skip if popup has been disposed - but still in map
			if (!popupWindow.isDisplayable()) {
				// remove from map
				// map should be last reference to the frame so garbage
				// collection should now be free to clean it up
				mapEntry.setValue(null);
				continue;
			}

			try {
				Appointment appt = AppointmentModel.getReference().getAppt(
						apptInstance.getAppt().getKey());
				if (appt == null) {
					popupWindow.dispose();
					deletedPopupKeys.add(apptInstance);
					continue;
				}

				if (!appt.getDate().equals(apptInstance.appt.getDate())) {
					// date changed - get rid of popup
					popupWindow.dispose();
					deletedPopupKeys.add(apptInstance);
				}

				// use latest from db in the appt instance so shouldBeShown()
				// can check any updated values
				apptInstance.appt = appt;

				if (!shouldBeShown(apptInstance)) {
					// dispose of popup and add to delete list
					popupWindow.dispose();
					deletedPopupKeys.add(apptInstance);
				}

				// delete it if the text changed - will add back in check for
				// popups
				if (!appt.getText().equals(apptInstance.getAppt().getText())) {
					popupWindow.dispose();
					deletedPopupKeys.add(apptInstance);
				}
			} catch (Exception e) {

				// appt cannot be read, must have been delete - kill the popup
				// this is an expected case when appointments are deleted
				deletedPopupKeys.add(apptInstance);
				popupWindow.dispose();
			}
		}

		// delete the popup map entries for popups that we disposed of
		for (AppointmentInstance inst : deletedPopupKeys) {
			pops.remove(inst);
		}

	}

	/**
	 * determine if a popup window is ready to be popped up and return the
	 * minutes value of the popup if so
	 * 
	 * @param apptInstance
	 *            the appointment instance
	 * @param p
	 *            the popup for the appointment
	 * 
	 * @return the minutes of the popup
	 * 
	 * @throws Exception
	 *             if not due for popup
	 */
	private int dueForPopup(AppointmentInstance apptInstance, ReminderPopup p)
			throws Exception {

		// get reminder timesd on/off flags for the appointment
		// default is all off if appt has a null value
		char[] remTimes = new char[ReminderTimes.getNum()];
		try {
			remTimes = (apptInstance.getAppt().getReminderTimes())
					.toCharArray();
		} catch (Exception e) {
			for (int i = 0; i < ReminderTimes.getNum(); ++i) {
				remTimes[i] = 'N';
			}
		}

		// determine how far away the appt is
		long minutesToGo = apptInstance.getInstanceTime().getTime()
				/ (1000 * 60) - new Date().getTime() / (1000 * 60);

		// determine which reminder is next
		int nextFutureReminder = 0;
		while (ReminderTimes.getTimes(nextFutureReminder) < minutesToGo) {
			++nextFutureReminder;
		}

		// shouldn't happen
		if (nextFutureReminder >= ReminderTimes.getNum()) {
			throw new Exception();
		}

		// if the next reminder time has not been popped up yet
		// then return it so it can be shown
		if (remTimes[nextFutureReminder] == 'Y'
				&& p.reminderShown(nextFutureReminder) == 'N') {
			p.setReminderShown(nextFutureReminder);
			return ReminderTimes.getTimes(nextFutureReminder);
		}

		// not due for popup
		throw new Exception();

	}

	/**
	 * determine if an appointment time is outside of the range of its reminder
	 * times.
	 * 
	 * @param apptInstance
	 *            the appointment instance
	 * @param nonexpiring
	 *            if true, the reminder never expires, so do not check the
	 *            latest reminder time
	 * 
	 * @return true, if the appointment tome is not in range of its reminder
	 * 
	 *         times
	 */
	static private boolean isOutsideOfReminderTimes(
			AppointmentInstance apptInstance, boolean nonExpiring) {

		// determine how far away the appt is
		long minutesToGo = apptInstance.getInstanceTime().getTime()
				/ (1000 * 60) - new Date().getTime() / (1000 * 60);

		int latestReminderTime = 100000;
		int earliestReminderTime = -100000;
		char[] remTimes = new char[ReminderTimes.getNum()];
		try {
			remTimes = (apptInstance.getAppt().getReminderTimes())
					.toCharArray();

		} catch (Exception e) {
			for (int i = 0; i < ReminderTimes.getNum(); ++i) {
				remTimes[i] = 'N';
			}
		}

		for (int i = 0; i < ReminderTimes.getNum(); i++) {
			if (remTimes[i] == 'Y') {
				int time = ReminderTimes.getTimes(i);
				if (time > earliestReminderTime) {
					earliestReminderTime = ReminderTimes.getTimes(i);
				}
				if (time < latestReminderTime) {
					latestReminderTime = ReminderTimes.getTimes(i);
				}
			}
		}

		if (earliestReminderTime == -100000)
			return true;

		return (minutesToGo > earliestReminderTime || (!nonExpiring && minutesToGo < latestReminderTime));
	}

	/**
	 * determine if an appointment popup should be shown for an appointment that
	 * doesn't yet have a popup associated with it
	 * 
	 * @param apptInstance
	 *            the appointment instance
	 * 
	 * @return true, if successful
	 */
	private static boolean shouldBeShown(AppointmentInstance apptInstance) {

		if (apptInstance == null)
			return false;

		// check if we should show it based on public/private
		// flags
		boolean showpub = Prefs.getBoolPref(PrefName.SHOWPUBLIC);

		boolean showpriv = Prefs.getBoolPref(PrefName.SHOWPRIVATE);

		if (apptInstance.getAppt().getPrivate()) {
			if (!showpriv)
				return false;
		} else {
			if (!showpub)
				return false;
		}

		// don't popup untimed appointments that are not todos
		if (AppointmentModel.isNote(apptInstance.getAppt())
				&& !apptInstance.getAppt().getTodo())
			return false;

		boolean expires = true; // true if the reminder eventually stops at some
		// point after the appt

		// untimed todos never expire
		if (AppointmentModel.isNote(apptInstance.getAppt())
				&& apptInstance.getAppt().getTodo()
				&& apptInstance.getAppt().getReminderTimes() != null
				&& apptInstance.getAppt().getReminderTimes().indexOf('Y') != -1) {
			expires = false;
		}

		// a normal timed appt only gets a popup
		// if it's within its range of reminder times
		if (isOutsideOfReminderTimes(apptInstance, !expires))
			return false;

		return true;
	}

	/**
	 * check if any new popup windows are needed and pop them up also beep and
	 * bring existing imminent popups to the front
	 */
	private void checkPopups() {

		// do nothing if the reminder feature is off
		// this check is inside the timer logic so that
		// the user can turn on the feature and not have to restart
		// to get popups
		String enable = Prefs.getPref(PrefName.REMINDERS);
		if (enable.equals("false"))
			return;

		// determine most future day that we have to consider
		int earliestReminderTime = -100000;
		for (int i = 0; i < ReminderTimes.getNum(); i++) {
			int time = ReminderTimes.getTimes(i);
			if (time > earliestReminderTime) {
				earliestReminderTime = ReminderTimes.getTimes(i);
			}
		}

		// determine how many days ahead we have to fetch appointments from
		int daysAhead = earliestReminderTime / (24 * 60) + 1;
		if (daysAhead < 1)
			daysAhead = 1;

		// process the appointments from each day
		// it does not matter if we find appointments that are a few hours
		// too far in the future to be considered - they will be filtered later
		Calendar cal = new GregorianCalendar();
		for (int dayAhead = 0; dayAhead < daysAhead; dayAhead++) {
			List<Integer> apptKeyList = AppointmentModel.getReference()
					.getAppts(cal.getTime());

			if (apptKeyList != null) {

				Appointment appt;

				// iterate through the appts
				for (Integer apptKey : apptKeyList) {

					try {
						// read the appt record from the data model
						appt = AppointmentModel.getReference().getAppt(
								apptKey.intValue());

						// skip untimed todos here - will be collected later on
						if (AppointmentModel.isNote(appt) && appt.getTodo())
							continue;

						// calculate instance time
						Calendar instTime = new GregorianCalendar();
						instTime.setTime(appt.getDate());
						instTime.set(Calendar.SECOND, 0);
						instTime.set(Calendar.MILLISECOND, 0);
						instTime.set(Calendar.YEAR, cal.get(Calendar.YEAR));
						instTime.set(Calendar.YEAR, cal.get(Calendar.YEAR));
						instTime.set(Calendar.MONTH, cal.get(Calendar.MONTH));
						instTime.set(Calendar.DATE, cal.get(Calendar.DATE));

						AppointmentInstance apptInstance = new AppointmentInstance(
								appt, instTime.getTime());

						// if the appointment doesn't qualify for a popup
						// then don't show one
						if (!shouldBeShown(apptInstance))
							continue;

						// skip appt if it is already in the pops list
						// this means that it is already showing - or was shown
						// and killed already
						if (pops.containsKey(apptInstance))
							continue;

						// create a new popup and add it to the
						// popup map along with the appt key
						ReminderPopup popup = new ReminderPopup(appt,
								apptInstance.getInstanceTime());
						pops.put(apptInstance, popup);

						popup.timeToGoMessage("");
						popup.setVisible(true);
						popup.toFront();

					} catch (Exception e) {
						Errmsg.errmsg(e);
					}
				}
			}

			// go to next day
			cal.add(Calendar.DATE, 1);
		}

		// get past untimed todos that are not done
		Collection<Appointment> tds = AppointmentModel.getReference()
				.get_todos();
		if (tds != null) {
			Iterator<Appointment> it = tds.iterator();

			while (it.hasNext()) {

				Appointment appt = it.next();

				try {

					// the only old todos we are checking for here are non-timed
					if (!AppointmentModel.isNote(appt))
						continue;

					// instance date is date of next todo
					Date nt = appt.getNextTodo();
					if (nt == null)
						nt = appt.getDate();

					AppointmentInstance apptInstance = new AppointmentInstance(
							appt, nt);

					if (!shouldBeShown(apptInstance))
						continue;

					// skip appt if it is already in the pops list
					// this means that it is already showing - or was shown
					// and
					// killed already
					if (pops.containsKey(apptInstance))
						continue;

					// create a new frame for a popup and add it to the
					// popup
					// map
					// along with the appt key
					ReminderPopup jd = new ReminderPopup(appt, apptInstance
							.getInstanceTime());

					pops.put(apptInstance, jd);
					jd.setVisible(true);
					jd.toFront();

				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		}

		// if any popups that are already displayed are due for showing - make a
		// sound and raise the popup
		boolean enablebeep = Prefs.getBoolPref(PrefName.BEEPINGREMINDERS);

		// iterate through existing popups
		Set<Entry<AppointmentInstance, ReminderPopup>> entrySet = pops
				.entrySet();
		for (Entry<AppointmentInstance, ReminderPopup> popupMapEntry : entrySet) {

			AppointmentInstance apptInstance = popupMapEntry.getKey();
			ReminderPopup popup = popupMapEntry.getValue();

			// if popup is gone (killed already), then skip it
			if (popup == null)
				continue;

			// skip if popup is disposed - but still in map
			if (!popup.isDisplayable()) {
				// remove from map
				// map should be last reference to the frame so garbage
				// collection should now be free to clean it up
				popupMapEntry.setValue(null);
				continue;
			}

			try {
				// read the appt and get the date
				Appointment appt = apptInstance.getAppt();

				String timeToGoMessage;

				// untimed todo
				if (AppointmentModel.isNote(appt) && appt.getTodo()) {

					// *** show untimed todos on the half hour and on startup
					int min = new GregorianCalendar().get(Calendar.MINUTE);
					if (popup.wasEverShown() && !(min == 0 || min == 30))
						continue;

					timeToGoMessage = Resource.getResourceString("To_Do");
				} else {
					// timed appt
					Date d = apptInstance.getInstanceTime();
					if (d == null)
						continue;

					// if alarm is due to be shown, show it and play sound
					try {
						int alarmid = dueForPopup(apptInstance, popup);
						// create a message saying how much time to go there is
						if (alarmid < 0) {
							timeToGoMessage = -alarmid + " "
									+ Resource.getResourceString("minutes_ago");
						} else if (alarmid == 0) {
							timeToGoMessage = Resource.getResourceString("Now");
						} else {
							timeToGoMessage = alarmid
									+ " "
									+ Resource
											.getResourceString("minute_reminder");
						}
					} catch (Exception e) {
						continue;
					}

				}

				// set the time to go message
				popup.timeToGoMessage(timeToGoMessage);

				popup.setVisible(true);
				popup.toFront();
				popup.setVisible(true);
				popup.setShown(true);

				// play a sound
				if (enablebeep) {
					if (Prefs.getPref(PrefName.USESYSTEMBEEP).equals("true")) {
						Toolkit.getDefaultToolkit().beep();
					} else {
						URL snd = getClass().getResource("/resource/blip.wav");
						AudioClip theSound;
						theSound = Applet.newAudioClip(snd);
						if (theSound != null) {
							theSound.play();
						}
					}
				}
			} catch (Exception e) {
			  // empty
			}
		}
	}

	/**
	 * show all popups in the list
	 */
	public void showAll() {
		Set<Entry<AppointmentInstance, ReminderPopup>> entrySet = pops
				.entrySet();
		for (Entry<AppointmentInstance, ReminderPopup> popupMapEntry : entrySet) {

			ReminderPopup popup = popupMapEntry.getValue();

			// if frame is gone (killed already), then skip it
			if (popup == null)
				continue;

			// skip if popup is disposed - but still in map
			if (!popup.isDisplayable()) {
				// remove from map
				// map should be last reference to the frame so garbage
				// collection should now be free to clean it up
				popupMapEntry.setValue(null);
				continue;
			}

			// pop it up
			popup.setVisible(true);
			popup.toFront();

		}
	}

	/**
	 * Hide all popup windows
	 */
	public void hideAll() {
		Set<Entry<AppointmentInstance, ReminderPopup>> entrySet = pops
				.entrySet();
		for (Entry<AppointmentInstance, ReminderPopup> popupMapEntry : entrySet) {

			ReminderPopup popup = popupMapEntry.getValue();

			// if frame is gone (killed already), then skip it
			if (popup == null)
				continue;

			// skip if popup is disposed - but still in map
			if (!popup.isDisplayable()) {
				// remove from map
				// map should be last reference to the frame so garbage
				// collection should now be free to clean it up
				popupMapEntry.setValue(null);
				continue;
			}

			// hide
			popup.setVisible(false);

		}
	}
}
