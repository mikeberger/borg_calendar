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

 Copyright 2010 by Mike Berger
 */
package net.sf.borg.ui.popup;

import java.util.Date;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.ui.calendar.AppointmentTextFormat;

/**
 * holds an instance of a reminder message. Keeps track of which reminder times
 * have been shown for this reminder message. Is not aware of the actual UI used
 * to display the reminder.
 */
public class ApptReminderInstance extends ReminderInstance {

	// the appointment
	private Appointment appt;

	/**
	 * constructor
	 * 
	 * @param appt
	 *            the appointment
	 * @param instanceTime
	 *            the instance time.
	 */
	public ApptReminderInstance(Appointment appt, Date instanceTime) {
		this.appt = appt;
		this.setInstanceTime(instanceTime);

	}
	
	/**
	 * if the reminder instance is a todo, then mark it as done/complete
	 */
	@Override
	public void do_todo(boolean delete)
	{
		if( appt != null )
			try {
				AppointmentModel.getReference().do_todo(
						appt.getKey(), delete);
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
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
		ApptReminderInstance other = (ApptReminderInstance) obj;
		if (appt == null) {
			if (other.appt != null)
				return false;
		} else if (appt.getKey() != other.appt.getKey()) {
			return false;
		}

		if (getInstanceTime() == null) {
			if (other.getInstanceTime() != null)
				return false;
		} else if (!getInstanceTime().equals(other.getInstanceTime()))
			return false;
		return true;
	}
	
	
	/**
	 * get the index of the active reminder for this instance - i.e. latest one
	 * passed
	 * 
	 * @return index of reminder or -1 if none
	 */
	@Override
	public int getCurrentReminder() {

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
		long minutesToGo = getInstanceTime().getTime() / (1000 * 60)
				- new Date().getTime() / (1000 * 60);

		// determine which reminder is next
		int nextFutureReminder = 0;
		while (ReminderTimes.getTimes(nextFutureReminder) < minutesToGo) {
			++nextFutureReminder;
		}

		// shouldn't happen
		if (nextFutureReminder >= ReminderTimes.getNum()) {
			return -1;
		}

		if (remTimes[nextFutureReminder] == 'Y') {
			return nextFutureReminder;
		}

		// none found
		return -1;
	}

	/**
	 * get the text for the reminder
	 * @return the reminder text
	 */
	@Override
	public String getText()
	{
		if( appt != null )
			return AppointmentTextFormat.format(appt,getInstanceTime());
		return null;
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
				+ ((getInstanceTime() == null) ? 0 : getInstanceTime().hashCode());
		return result;
	}


	/**
	 * return true if the reminder is a note (is untimed)
	 */
	@Override
	public boolean isNote()
	{
		if( appt != null )
			return AppointmentModel.isNote(appt);
		return false;
	}

	/**
	 * determine if an appointment time is outside of the range of its reminder
	 * times.
	 * 
	 * @param nonexpiring
	 *            if true, the reminder never expires, so do not check the
	 *            latest reminder time
	 * 
	 * @return true, if the appointment tome is not in range of its reminder
	 *         times
	 */
	private boolean isOutsideOfReminderTimes(boolean nonExpiring) {

		// determine how far away the appt is
		long minutesToGo = getInstanceTime().getTime() / (1000 * 60)
				- new Date().getTime() / (1000 * 60);

		int latestReminderTime = 100000;
		int earliestReminderTime = -100000;
		char[] remTimes = new char[ReminderTimes.getNum()];
		try {
			remTimes = (appt.getReminderTimes()).toCharArray();

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
	 * return true if the reminder is a todo - either an appointment todo or another type of todo, such as a task
	 */
	@Override
	public boolean isTodo()
	{
		if( appt != null )
			return appt.isTodo();
		return false;
	}

	/**
	 * reload the model entity from the db and check if it has changed.
	 * @return true if the entity has changed in a way that affects reminders
	 */
	@Override
	public boolean reloadAndCheckForChanges()
	{
		try {
			Appointment origAppt = appt;
			appt = AppointmentModel.getReference().getAppt(
					appt.getKey());
			if (appt == null) {
				return true;
			}

			if (!appt.getDate()
					.equals(origAppt.getDate())) {
				// date changed - delete. new instance will be added on
				// periodic update
				return true;
			}


			// delete it if the text changed - will be added back in
			// periodic check for
			// popups
			if (!appt.getText()
					.equals(origAppt.getText())) {
				return true;
			}

			if (isTodo()) {
				// skip if inst time changed for untimed todos
				Date nt = appt.getNextTodo();
				if (nt == null)
					nt = appt.getDate();

				if (!getInstanceTime().equals(nt)) {
					return true;
				}
			}
		} catch (Exception e) {

			// appt cannot be read, must have been deleted
			// this is an expected case when appointments are deleted
			appt = null;
			return true;
		}
		
		return false;
	}


	/**
	 * determine if an appointment popup should be shown for an appointment that
	 * doesn't yet have a popup associated with it
	 * 
	 * @return true, if successful
	 */
	@Override
	public boolean shouldBeShown() {
		
		if( appt == null )
			return false;

		// check if we should show it based on public/private
		// flags
		boolean showpub = Prefs.getBoolPref(PrefName.SHOWPUBLIC);

		boolean showpriv = Prefs.getBoolPref(PrefName.SHOWPRIVATE);

		if (appt.isPrivate()) {
			if (!showpriv)
				return false;
		} else {
			if (!showpub)
				return false;
		}

		// don't popup untimed appointments that are not todos
		if (isNote() && !isTodo())
			return false;

		boolean expires = true; // true if the reminder eventually stops at some
		// point after the appt

		// todos never expire
		if (isTodo()
				&& appt.getReminderTimes() != null
				&& appt.getReminderTimes().indexOf('Y') != -1) {
			expires = false;
		}

		// a normal timed appt only gets a popup
		// if it's within its range of reminder times
		if (isOutsideOfReminderTimes(!expires))
			return false;

		return true;
	}

}