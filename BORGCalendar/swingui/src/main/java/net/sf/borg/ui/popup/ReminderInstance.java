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

import lombok.Data;
import net.sf.borg.common.Resource;
import net.sf.borg.model.ReminderTimes;

/**
 * holds an instance of a reminder message. Keeps track of which reminder times
 * have been shown for this reminder message. Is not aware of the actual UI used
 * to display the reminder.
 */
@Data
abstract public class ReminderInstance {

	/**
	 * was the reminder hidden by the user
	 */
	private boolean hidden = false;

	// the instance time - this is the time when this instance occurs. this
	// will be
	// different from the entity time if this instance is a repeat of an
	// appointment
	private Date instanceTime;

	/** The reminders shown flags - tracks which reminder times have been shown */
	private char[] remindersShown;

	/**
	 * The was ever shown flag - used to decide if the reminder needs to be
	 * shown when the program is first starting up,but we are not on a todo
	 * reminder time
	 */
	private boolean shown = false;

	public ReminderInstance() {

		// initialize reminders shown flags
		setRemindersShown(new char[ReminderTimes.getNum()]);
		for (int i = 0; i < ReminderTimes.getNum(); ++i) {
			getRemindersShown()[i] = 'N';
		}

	}
	
	/**
	 * calculate the to go message for a reminder instance
	 * 
	 * @return the to go message or null if should not be shown
	 */
	public String calculateToGoMessage() {


		String message = null;

		// set the reminder due message
		if (isNote() && isTodo()) {
			message = Resource.getResourceString("To_Do");
		} else {

			// timed appt
			Date d = getInstanceTime();
			if (d == null)
				return null;

			// get the reminder time and also mark the reminder as shown
			// since w are adding it to the UI
			// it may already have been marked as shown - no problem
			int reminderIndex = getCurrentReminder();
			if (reminderIndex == -1)
				return null;
			markAsShown(reminderIndex);

			int minutesToGo = minutesToGo();

			String timeString = "";
			if (minutesToGo != 0) {
				int absmin = Math.abs(minutesToGo);
				int days = absmin / (24 * 60);
				int hours = (absmin % (24 * 60)) / 60;
				int mins = (absmin % 60);

				if (days > 0)
					timeString += days + " "
							+ Resource.getResourceString("Days") + " ";
				if (hours > 0)
					timeString += hours + " "
							+ Resource.getResourceString("Hours") + " ";
				timeString += Integer.toString(mins);

			}

			// create a message saying how much time to go there is
			if (minutesToGo < 0) {
				message = timeString + " "
						+ Resource.getResourceString("minutes_ago");
			} else if (minutesToGo == 0) {
				message = Resource.getResourceString("Now");
			} else {
				message = timeString + " "
						+ Resource.getResourceString("Minutes");
			}

		}

		return message;

	}
	
	/**
	 * if the reminder instance is a todo, then mark it as done/complete
	 */
	abstract public void do_todo(boolean delete);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	/**
	 * determine if a reminder is ready to be popped up and return the number of
	 * the reminder that is ready
	 * 
	 * @return the reminder number or -1 if not ready for a new popup
	 * 
	 */
	final public int dueForPopup() {

		int index = getCurrentReminder();

		// if the current reminder has already been shown, then we should not
		// trigger a new
		// popup - it is either already showing or has been hidden

		if (getRemindersShown()[index] == 'N') {
			return index;
		}

		// not due for popup
		return -1;
	}
	
	
	@Override
	abstract public boolean equals(Object obj);
	

	/**
	 * get the index of the active reminder for this instance - i.e. latest one
	 * passed
	 * 
	 * @return index of reminder or -1 if none
	 */
	abstract public int getCurrentReminder();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	/**
	 * get the text for the reminder
	 * @return the reminder text
	 */
	abstract public String getText();


	@Override
	abstract public int hashCode();

	
	/**
	 * return true if the reminder is a note (is untimed)
	 */
	abstract public boolean isNote();
	

	/**
	 * return true if the reminder is a todo - either an appointment todo or another type of todo, such as a task
	 */
	abstract public boolean isTodo();

	/**
	 * mark a reminder time as shown
	 * 
	 * @param reminderNumber
	 *            the reminder time index
	 */
	final public void markAsShown(int reminderNumber) {
		getRemindersShown()[reminderNumber] = 'Y';
	}
	

	public int minutesToGo() {
		return (int) ((getInstanceTime().getTime() / (60 * 1000) - new Date()
				.getTime()
				/ (60 * 1000)));
	}
	
	
	/**
	 * reload the model entity from the db and check if it has changed.
	 * @return true if the entity has changed in a way that affects reminders
	 */
	abstract public boolean reloadAndCheckForChanges();


	/**
	 * determine if an appointment popup should be shown for an appointment that
	 * doesn't yet have a popup associated with it
	 * 
	 * @return true, if successful
	 */
	abstract public boolean shouldBeShown();

}