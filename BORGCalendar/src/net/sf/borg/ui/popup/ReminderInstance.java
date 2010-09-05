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

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.entity.Appointment;

/**
 * holds an instance of a reminder message. Keeps track of which reminder times
 * have been shown for this reminder message. Is not aware of the actual UI used
 * to dislay the reminder.
 */
class ReminderInstance {

	// the appointment
	private Appointment appt;

	/**
	 * was the reminder hidden by the user
	 */
	private boolean hidden = false;

	// the instance time - this is the time when this instance occurs. this
	// will be
	// different from the appt time if this instance is a repeat of an
	// appointment
	private Date instanceTime;

	/** The reminders shown flags - tracks which reminder times have been shown */
	private char[] remindersShown;

	/**
	 * The was ever shown flag - used to decide if the reminder needs to be
	 * shown when the program is first starting up,but we are not on a todo
	 * reminder time
	 */
	private boolean wasEverShown = false;

	/**
	 * constructor
	 * 
	 * @param appt
	 *            the appointment
	 * @param instanceTime
	 *            the instance time.
	 */
	public ReminderInstance(Appointment appt, Date instanceTime) {
		this.appt = appt;
		this.instanceTime = instanceTime;

		// initialize reminders shown flags
		remindersShown = new char[ReminderTimes.getNum()];
		for (int i = 0; i < ReminderTimes.getNum(); ++i) {
			remindersShown[i] = 'N';
		}

	}

	/**
	 * determine if a reminder is ready to be popped up and return the number of
	 * the reminder that is ready
	 * 
	 * @return the reminder number or -1 if not ready for a new popup
	 * 
	 */
	public int dueForPopup() {

		int index = getCurrentReminder();

		// if the current reminder has already been shown, then we should not
		// trigger a new
		// popup - it is either already showing or has been hidden

		if (remindersShown[index] == 'N') {
			return index;
		}

		// not due for popup
		return -1;
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
		ReminderInstance other = (ReminderInstance) obj;
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

	/**
	 * get appointment
	 * 
	 * @return the appointment
	 */
	public Appointment getAppt() {
		return appt;
	}

	/**
	 * get the index of the active reminder for this instance - i.e. latest one
	 * passed
	 * 
	 * @return index of reminder or -1 if none
	 */
	public int getCurrentReminder() {

		// get reminder timesd on/off flags for the appointment
		// default is all off if appt has a null value
		char[] remTimes = new char[ReminderTimes.getNum()];
		try {
			remTimes = (getAppt().getReminderTimes()).toCharArray();
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

	/**
	 * @return true if this instance has been marked as hidden (dismissed by
	 *         user)
	 */
	public boolean isHidden() {
		return hidden;
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
			remTimes = (getAppt().getReminderTimes()).toCharArray();

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
	 * mark a reminder time as shown
	 * 
	 * @param reminderNumber
	 *            the reminder time index
	 */
	public void markAsShown(int reminderNumber) {
		remindersShown[reminderNumber] = 'Y';
	}

	public void setAppt(Appointment appt) {
		this.appt = appt;
	}

	/**
	 * set the hidden flag
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * Sets the shown flag.
	 * 
	 * @param s
	 *            was this popup ever shown
	 */
	public void setShown(boolean s) {
		wasEverShown = s;
	}

	/**
	 * determine if an appointment popup should be shown for an appointment that
	 * doesn't yet have a popup associated with it
	 * 
	 * @return true, if successful
	 */
	public boolean shouldBeShown() {

		// check if we should show it based on public/private
		// flags
		boolean showpub = Prefs.getBoolPref(PrefName.SHOWPUBLIC);

		boolean showpriv = Prefs.getBoolPref(PrefName.SHOWPRIVATE);

		if (getAppt().getPrivate()) {
			if (!showpriv)
				return false;
		} else {
			if (!showpub)
				return false;
		}

		// don't popup untimed appointments that are not todos
		if (AppointmentModel.isNote(getAppt()) && !getAppt().getTodo())
			return false;

		boolean expires = true; // true if the reminder eventually stops at some
		// point after the appt

		// untimed todos never expire
		if (AppointmentModel.isNote(getAppt()) && getAppt().getTodo()
				&& getAppt().getReminderTimes() != null
				&& getAppt().getReminderTimes().indexOf('Y') != -1) {
			expires = false;
		}

		// a normal timed appt only gets a popup
		// if it's within its range of reminder times
		if (isOutsideOfReminderTimes(!expires))
			return false;

		return true;
	}

	/**
	 * get the shown flag.
	 * 
	 * @return true if the popup was ever shown
	 */
	public boolean wasEverShown() {
		return wasEverShown;
	}

}