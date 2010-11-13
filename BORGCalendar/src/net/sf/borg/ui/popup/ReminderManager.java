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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.entity.Appointment;

/**
 * Abstract Base class for managing reminders. A reminder manager controls the
 * UI that shows reminder instances. It also manages a list of reminders (a model for the UI) and 
 * must check and update the model as time
 * passes. It must also react to appointment model changes.
 */
public abstract class ReminderManager implements Model.Listener {

	/** The singleton. */
	static protected ReminderManager singleton = null;

	public static ReminderManager getReminderManager() {
		return singleton;
	}

	/** runnable task that checks if we need to pop up any popups */
	final Runnable doPopupChk = new Runnable() {
		@Override
		public void run() {
			checkPopups();
		}
	};

	/**
	 * The timer that periodically checks to see if we need to popup a new
	 * reminder
	 */
	private Timer timer = null;

	/**
	 * constructor
	 */
	protected ReminderManager() {

		// listen for appointment model changes
		AppointmentModel.getReference().addListener(this);

		// start the popup timer
		// for consistency - it will start at the beginning of the next minute
		// the interval between runs is a user preference
		timer = new Timer("PopupTimer");
		GregorianCalendar cur = new GregorianCalendar();
		int secs_left = 60 - cur.get(Calendar.SECOND);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(doPopupChk);
			}
		}, secs_left * 1000,
				1 * 60 * 1000);

	}

	/**
	 * show a reminder
	 * 
	 * @param instance
	 *            the reminder instance
	 */
	public abstract void addToUI(ReminderInstance instance);

	/**
	 * check if any new reminder messages needed and show them. Also update any
	 * messages that are already being shown
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

						ReminderInstance apptInstance = new ReminderInstance(
								appt, instTime.getTime());

						// if the appointment doesn't qualify for a popup
						// then don't show one
						if (!apptInstance.shouldBeShown())
							continue;

						addToUI(apptInstance);

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

					ReminderInstance apptInstance = new ReminderInstance(appt,
							nt);

					if (!apptInstance.shouldBeShown())
						continue;

					addToUI(apptInstance);

				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		}

		// update the existing reminders in the model and UI as needed due to passage of time
		periodicUpdate();

	}

	/**
	 * Hide all reminders
	 */
	public abstract void hideAll();

	/**
	 * update any visible reminders as needed - i.e. to catch changes due to the
	 * passage of time. This is NOT called for model changes.
	 */
	public abstract void periodicUpdate();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model.Listener#refresh()
	 */
	/**
	 * refresh the popup states depending on the state of the appointment model.
	 * In particular, clean up any popups that should no longer be shown because
	 * of model changes.
	 */
	abstract public void refresh();

	/**
	 * stop the timer
	 */
	public void remove() {
		timer.cancel();
	}

	/**
	 * show all reminders
	 */
	public abstract void showAll();
	
	/**
	 * determine if we should show a reminder for untimed todos during this periodic update
	 * @return true if we should show the reminder
	 */
	protected boolean shouldShowUntimedTodosNow()
	{
		
		GregorianCalendar now = new GregorianCalendar();
		int todoFreq = Prefs.getIntPref(PrefName.TODOREMINDERMINS);

		// *** show untimed todos periodically and on startup
		// if no todo reminders are wanted, then skip this
		if( todoFreq == 0 )
			return false;
		
		// determine minutes since midnight
		int hr = now.get(Calendar.HOUR_OF_DAY);
		int min = new GregorianCalendar().get(Calendar.MINUTE);
		int mins_since_midnight = hr*60 + min;		
		
		return (mins_since_midnight % todoFreq == 0);
	}

}
