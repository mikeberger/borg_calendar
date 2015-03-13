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

 Copyright 2011 by Mike Berger
 */
package net.sf.borg.ui.popup;

import java.util.Date;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Subtask;

/**
 * A Reminder Instance class that holds a Task
 * 
 */
public class SubtaskReminderInstance extends ReminderInstance {

	private Subtask subtask;

	@Override
	public void do_todo(boolean delete) {
		try {
			subtask = TaskModel.getReference().getSubTask(subtask.getKey());
			subtask.setCloseDate(new Date());
			TaskModel.getReference().saveSubTask(subtask);
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubtaskReminderInstance other = (SubtaskReminderInstance) obj;
		if (subtask == null) {
			if (other.subtask != null)
				return false;
		} else if (subtask.getKey() != other.subtask.getKey()) {
			return false;
		}

		if (getInstanceTime() == null) {
			if (other.getInstanceTime() != null)
				return false;
		} else if (!getInstanceTime().equals(other.getInstanceTime()))
			return false;
		return true;
	}

	@Override
	public int getCurrentReminder() {
		return -1;
	}

	/**
	 * @param task
	 */
	public SubtaskReminderInstance(Subtask subtask) {
		super();
		this.subtask = subtask;
		this.setInstanceTime(subtask.getDueDate());
	}

	@Override
	public String getText() {
		return subtask.getText();
	}

	@Override
	public String calculateToGoMessage() {
		return Resource.getResourceString("subtask");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((subtask == null) ? 0 : subtask.getKey());
		result = prime
				* result
				+ ((getInstanceTime() == null) ? 0 : getInstanceTime()
						.hashCode());
		return result;
	}

	@Override
	public boolean isNote() {
		return true;
	}

	@Override
	public boolean isTodo() {
		return true;
	}

	@Override
	public boolean reloadAndCheckForChanges() {
		try {
			Subtask orig = subtask;
			subtask = TaskModel.getReference().getSubTask(subtask.getKey());
			if (subtask == null) {
				return true;
			}
			
			if (subtask.getCloseDate() != null)
				return true;


			if (subtask.getDueDate() == null || !subtask.getDueDate()
					.equals(orig.getDueDate())) {
				// date changed - delete. new instance will be added on
				// periodic update
				return true;
			}


			// delete it if the text changed - will be added back in
			// periodic check for
			// popups
			if (!subtask.getDescription()
					.equals(orig.getDescription())) {
				return true;
			}

			
		} catch (Exception e) {

			// task cannot be read, must have been deleted
			subtask = null;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean shouldBeShown() {
		
		if( !Prefs.getBoolPref(PrefName.TASKREMINDERS))
			return false;
		
		if( subtask == null || subtask.getCloseDate() != null )
			return false;

		// determine how far away the task is
		long minutesToGo = getInstanceTime().getTime() / (1000 * 60)
				- new Date().getTime() / (1000 * 60);

		int earliestReminderTime = -100000;

		for (int i = 0; i < ReminderTimes.getNum(); i++) {
			int time = ReminderTimes.getTimes(i);
			if (time > earliestReminderTime) {
				earliestReminderTime = time;
			}
		}
		
		if( earliestReminderTime == -100000)
			return false;

		return (minutesToGo < earliestReminderTime);
	}

}
