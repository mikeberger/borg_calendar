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

	/** The singleton. */
	static private ReminderPopupManager singleton = null;

	// map that maps appointment keys to the associated popup reminder
	// windows
	private HashMap<Integer, ReminderPopup> pops = new HashMap<Integer, ReminderPopup>();

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
			public void run() {
				EventQueue.invokeLater(doPopupChk);
			}
		}, secs_left * 1000,
				Prefs.getIntPref(PrefName.REMINDERCHECKMINS) * 60 * 1000);

	}

	// find any popups that should no longer be displayed based on updates to
	// the DB and delete them
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
		ArrayList<Integer> deletedPopupKeys = new ArrayList<Integer>();

		// set of popup map entries
		Set<Entry<Integer, ReminderPopup>> entrySet = pops.entrySet();

		// loop through the existing popups
		for (Entry<Integer, ReminderPopup> mapEntry : entrySet) {

			Integer apptkey = mapEntry.getKey();

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
				// read the appt and get the date
				Appointment appt = AppointmentModel.getReference().getAppt(
						apptkey.intValue());
				if (!shouldBeShown(appt)) {
					// dispose of popup and add to delete list
					popupWindow.dispose();
					deletedPopupKeys.add(apptkey);
				}
			} catch (Exception e) {

				// appt cannot be read, must have been delete - kill the popup
				// this is an expected case when appointments are deleted
				deletedPopupKeys.add(apptkey);
				popupWindow.dispose();
			}
		}

		// delete the popup map entries for popups that we disposed of
		for (Integer key : deletedPopupKeys) {
			pops.remove(key);
		}

	}

	/**
	 * determine if a popup window is ready to be popped up and return the
	 * minutes value of the popup if so
	 * 
	 * @param appt
	 *            the appointment
	 * @param p
	 *            the popup for the appointment
	 * 
	 * @return the minutes of the popup
	 * 
	 * @throws Exception
	 *             if not due for popup
	 */
	private int dueForPopup(Appointment appt, ReminderPopup p) throws Exception {

		// get reminder timesd on/off flags for the appointment
		// default is all off if appt has a null value
		char[] remTimes = new char[ReminderTimes.getNum()];
		try {
			remTimes = (appt.getReminderTimes()).toCharArray();
		} catch (Exception e) {
			for (int i = 0; i < ReminderTimes.getNum(); ++i) {
				remTimes[i] = 'N';
			}
		}

		// determine how far away the appt is
		// need to set appt day of year to today in case it is a
		// repeating appt. if it is a repeat,
		// the time will be right, but the day will be the day of
		// the first repeat
		GregorianCalendar apptTime = new GregorianCalendar();
		apptTime.setTime(appt.getDate());
		GregorianCalendar now = new GregorianCalendar();
		apptTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now
				.get(Calendar.DATE));

		long minutesToGo = apptTime.getTimeInMillis() / (1000 * 60)
				- now.getTimeInMillis() / (1000 * 60);

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
	 * @param appt
	 *            the appointment
	 * 
	 * @return true, if the appointment tome is not in range of its reminder
	 *         times
	 */
	static private boolean isOutsideOfReminderTimes(Appointment appt) {

		// determine how far away the appt is
		// need to set appt day of year to today in case it is a
		// repeating appt. if it is a repeat,
		// the time will be right, but the day will be the day of
		// the first repeat
		GregorianCalendar apptTime = new GregorianCalendar();
		apptTime.setTime(appt.getDate());
		GregorianCalendar now = new GregorianCalendar();
		apptTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now
				.get(Calendar.DATE));

		long minutesToGo = apptTime.getTimeInMillis() / (1000 * 60)
				- now.getTimeInMillis() / (1000 * 60);

		int earliestReminderTime = -999;
		char[] remTimes = new char[ReminderTimes.getNum()];
		try {
			remTimes = (appt.getReminderTimes()).toCharArray();

		} catch (Exception e) {
			for (int i = 0; i < ReminderTimes.getNum(); ++i) {
				remTimes[i] = 'N';
			}
		}

		for (int i = 0; i < ReminderTimes.getNum(); i++) {
			if (remTimes[i] == 'Y')
				earliestReminderTime = ReminderTimes.getTimes(i);
		}

		if (earliestReminderTime == -999)
			return true;

		return (minutesToGo > earliestReminderTime || minutesToGo < ReminderTimes
				.getTimes(0));
	}

	/**
	 * determine if an appointment popup should be shown for an appointment that
	 * doesn't yet have a popup associated with it
	 * 
	 * @param appt
	 *            the appointment
	 * 
	 * @return true, if successful
	 */
	private static boolean shouldBeShown(Appointment appt) {

		if (appt == null)
			return false;

		// check if we should show it based on public/private
		// flags
		boolean showpub = Prefs.getBoolPref(PrefName.SHOWPUBLIC);
		;
		boolean showpriv = Prefs.getBoolPref(PrefName.SHOWPRIVATE);
		;
		if (appt.getPrivate()) {
			if (!showpriv)
				return false;
		} else {
			if (!showpub)
				return false;
		}

		// don't popup untimed appointments that are not todos
		if (AppointmentModel.isNote(appt) && !appt.getTodo())
			return false;

		// untimed todos should get popups as long as they
		// are not marked as done for the current day
		// *** we will popup untimed todos if there is at least 1
		// reminder time set - even though we can't use the actual times
		if (AppointmentModel.isNote(appt) && appt.getTodo()
				&& appt.getReminderTimes() != null
				&& appt.getReminderTimes().indexOf('Y') != -1) {

			// make sure todo is not done for today
			GregorianCalendar td = new GregorianCalendar();
			td.set(Calendar.HOUR_OF_DAY, 23);
			td.set(Calendar.MINUTE, 59);

			Date nt = appt.getNextTodo();
			if (nt == null)
				nt = appt.getDate();
			if (nt != null && nt.after(td.getTime())) {
				return false;
			}
		} else {

			// a normal timed appt only gets a popup
			// if it's within its range of reminder times
			if (isOutsideOfReminderTimes(appt))
				return false;
		}

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

		// get the list of the today's appts - popups are only for the current
		// day
		List<Integer> apptKeyList = AppointmentModel.getReference().getAppts(
				new Date());
		if (apptKeyList != null) {

			Appointment appt;

			// iterate through the day's appts
			for (Integer apptKey : apptKeyList) {

				try {
					// read the appt record from the data model
					appt = AppointmentModel.getReference().getAppt(
							apptKey.intValue());

					// if the appointment doesn't qualify for a popup
					// then don't show one
					if (!shouldBeShown(appt))
						continue;

					// skip appt if it is already in the pops list
					// this means that it is already showing - or was shown
					// and killed already
					if (pops.containsKey(apptKey))
						continue;

					// create a new popup and add it to the
					// popup map along with the appt key
					ReminderPopup popup = new ReminderPopup(appt);
					pops.put(apptKey, popup);

					popup.setText2("");
					popup.setVisible(true);
					popup.toFront();

				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		}

		// get past untimed todos that are not done
		Collection<Appointment> tds = AppointmentModel.getReference()
				.get_todos();
		if (tds != null) {
			Iterator<Appointment> it = tds.iterator();

			// iterate through the day's appts
			while (it.hasNext()) {

				Appointment appt = it.next();

				try {

					// the only old todos we are checking for here are non-timed
					if (!AppointmentModel.isNote(appt))
						continue;

					if (!shouldBeShown(appt))
						continue;

					// skip appt if it is already in the pops list
					// this means that it is already showing - or was shown
					// and
					// killed already
					Integer ik = new Integer(appt.getKey());
					if (pops.containsKey(ik))
						continue;

					// create a new frame for a popup and add it to the
					// popup
					// map
					// along with the appt key

					ReminderPopup jd = new ReminderPopup(appt);

					pops.put(ik, jd);
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
		Set<Entry<Integer, ReminderPopup>> entrySet = pops.entrySet();
		for(Entry<Integer, ReminderPopup> popupMapEntry : entrySet) {
			
			Integer apptkey = popupMapEntry.getKey();
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
				Appointment appt = AppointmentModel.getReference().getAppt(
						apptkey.intValue());

				String timeToGoMessage;

				// untimed todo
				if (AppointmentModel.isNote(appt) && appt.getTodo()) {

					// *** show untimed todos on the half hour and on startup
					int min = new GregorianCalendar().get(Calendar.MINUTE);
					if (popup.wasShown() && !(min == 0 || min == 30))
						continue;

					timeToGoMessage = Resource.getResourceString("To_Do") + " "
							+ Resource.getResourceString("Today");
				} else {
					// timed appt
					Date d = appt.getDate();
					if (d == null)
						continue;

					// if alarm is due to be shown, show it and play sound
					try {
						int alarmid = dueForPopup(appt, popup);
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
				popup.setText2(timeToGoMessage);
				
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
				
			}
		}
	}

	/**
	 * show all popups in the list
	 */
	public void showAll() {
		Set<Entry<Integer, ReminderPopup>> entrySet = pops.entrySet();
		for( Entry<Integer, ReminderPopup> popupMapEntry  : entrySet ) {
			
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
		Set<Entry<Integer, ReminderPopup>> entrySet = pops.entrySet();
		for( Entry<Integer, ReminderPopup> popupMapEntry  : entrySet ) {
			
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
