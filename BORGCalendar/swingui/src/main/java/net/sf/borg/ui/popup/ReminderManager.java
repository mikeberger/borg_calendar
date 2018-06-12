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
import java.util.Vector;

import javax.swing.SwingUtilities;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;

/**
 * Abstract Base class for managing reminders. A reminder manager controls the
 * UI that shows reminder instances. It also manages a list of reminders (a
 * model for the UI) and must check and update the model as time passes. It must
 * also react to appointment model changes.
 */
public abstract class ReminderManager implements Model.Listener, Prefs.Listener {

	/** The singleton. */
	static protected ReminderManager singleton = null;

	public static ReminderManager getReminderManager() {
		return singleton;
	}

	/** runnable task that checks if we need to pop up any popups */
	final Runnable doModelCheck = new Runnable() {
		@Override
		public void run() {
			checkModelsForReminders();
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
		TaskModel.getReference().addListener(this);
		AddressModel.getReference().addListener(this);

		Prefs.addListener(this);

		// start the timer
		// for consistency - it will start at the beginning of the next minute
		// the interval between runs is a user preference
		timer = new Timer("ReminderTimer");
		GregorianCalendar cur = new GregorianCalendar();
		int secs_left = 60 - cur.get(Calendar.SECOND);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(doModelCheck);
			}
		}, secs_left * 1000, 1 * 60 * 1000);

	}

	@Override
	public void prefsChanged() {
		refresh();
	}

	/**
	 * add a reminder to the UI
	 *
	 * @param instance
	 *            the reminder instance
	 */
	public abstract void addToUI(ReminderInstance instance);

	/**
	 * check the models to see if any new reminder messages needed and show
	 * them. Also update any messages that are already being shown
	 */
	public void checkModelsForReminders() {

		// do nothing if the reminder feature is off
		// this check is inside the timer logic so that
		// the user can turn on the feature and not have to restart
		// to get popups
		String enable = Prefs.getPref(PrefName.REMINDERS);
		if (enable.equals("false"))
			return;

		getAppointmentReminders();

		getBirthdayReminders();

		getTaskReminders();

		// update the existing reminders in the model and UI as needed due to
		// passage of time
		periodicUpdate();

	}

	private void getTaskReminders() {
		if (Prefs.getBoolPref(PrefName.TASKREMINDERS)) {

			try {
				Collection<Project> pjs = TaskModel.getReference()
						.getProjects();
				for (Project pj : pjs) {
					if (pj.getDueDate() == null)
						continue;

					// skip closed projects
					if (pj.getStatus().equals(
							Resource.getResourceString("CLOSED")))
						continue;

					// filter by category
					if (!CategoryModel.getReference().isShown(pj.getCategory()))
						continue;

					ReminderInstance inst = new ProjectReminderInstance(pj);

					if (!inst.shouldBeShown())
						continue;

					addToUI(inst);

				}
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}

			// get all tasks
			Vector<Task> mrs = TaskModel.getReference().get_tasks();
			for (int i = 0; i < mrs.size(); i++) {

				Task mr = mrs.elementAt(i);
				if (mr.getDueDate() == null)
					continue;

				ReminderInstance inst = new TaskReminderInstance(mr);

				if (!inst.shouldBeShown())
					continue;

				addToUI(inst);

			}

			try {
				Collection<Subtask> sts = TaskModel.getReference()
						.getSubTasks();
				for (Subtask st : sts) {
					if (st.getDueDate() == null)
						continue;
					if (st.getCloseDate() != null)
						continue;

					Task task = TaskModel.getReference().getTask(
							st.getTask().intValue());
					String cat = task.getCategory();

					if (!CategoryModel.getReference().isShown(cat))
						continue;

					ReminderInstance inst = new SubtaskReminderInstance(st);

					if (!inst.shouldBeShown())
						continue;

					addToUI(inst);

				}
			} catch (Exception e) {

			}
		}

	}

	public void getAppointmentReminders() {

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
						if (AppointmentModel.isNote(appt) && appt.isTodo())
							continue;

						// calculate instance time
						Calendar instTime = new GregorianCalendar();
						instTime.setTime(appt.getDate());
						instTime.set(Calendar.SECOND, 0);
						instTime.set(Calendar.MILLISECOND, 0);
						instTime.set(Calendar.YEAR, cal.get(Calendar.YEAR));
						instTime.set(Calendar.MONTH, cal.get(Calendar.MONTH));
						instTime.set(Calendar.DATE, cal.get(Calendar.DATE));

						ReminderInstance apptInstance = new ApptReminderInstance(
								appt, instTime.getTime());

						// if the appointment doesn't qualify for a popup
						// then don't show one
						if (!apptInstance.shouldBeShown())
							continue;

						addToUI(apptInstance);

					} catch (Exception e) {
						Errmsg.getErrorHandler().errmsg(e);
					}
				}
			}

			// go to next day
			cal.add(Calendar.DATE, 1);
		}

		// get todos that are not done
		Collection<Appointment> tds = AppointmentModel.getReference()
				.get_todos();
		if (tds != null) {
			Iterator<Appointment> it = tds.iterator();

			while (it.hasNext()) {

				Appointment appt = it.next();

				// skip timed todos here - done above
				if (!AppointmentModel.isNote(appt))
					continue;

				try {

					// instance date is date of next todo
					Date nt = appt.getNextTodo();
					if (nt == null)
						nt = appt.getDate();

					ReminderInstance apptInstance = new ApptReminderInstance(
							appt, nt);

					if (!apptInstance.shouldBeShown())
						continue;

					addToUI(apptInstance);

				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		}

	}

	public void getBirthdayReminders() {

		// birthdays
		int bd_days = Prefs.getIntPref(PrefName.BIRTHDAYREMINDERDAYS);
		if (bd_days >= 0) {
			Date now = new Date();
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(now);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			Collection<Address> addrs;
			try {
				addrs = AddressModel.getReference().getAddresses();
				if (addrs != null) {
					for (Address addr : addrs) {

						Date bd = addr.getBirthday();
						if (bd == null)
							continue;

						// set time to end of the day. this will allow reminders
						// to pop up all day on the
						// birthday itself
						Calendar bdcal = new GregorianCalendar();
						bdcal.setTime(bd);
						bdcal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
						bdcal.set(Calendar.SECOND, 59);
						bdcal.set(Calendar.MINUTE, 59);
						bdcal.set(Calendar.HOUR_OF_DAY, 23);

						// if the birthday is passed for this year, try next
						// year (in case we are close enough
						// to January birthdays for next year)
						if (bdcal.before(cal)) {
							bdcal.add(Calendar.YEAR, 1);
						}

						ReminderInstance inst = new BirthdayReminderInstance(
								addr, bdcal.getTime());

						if (!inst.shouldBeShown())
							continue;

						addToUI(inst);

					}
				}

			} catch (Exception e1) {
				Errmsg.getErrorHandler().errmsg(e1);
			}
		}

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
	 * refresh the popup states depending on the state of the models.
	 * In particular, clean up any reminders that should no longer be shown because
	 * of model changes.
	 */
	abstract public void refresh();

	/**
	 * stop the timer
	 */
	public void remove() {
		timer.cancel();

		singleton = null;
	}

	/**
	 * show all reminders
	 */
	public abstract void showAll();

	/**
	 * determine if we should show a reminder for untimed todos during this
	 * periodic update
	 *
	 * @return true if we should show the reminder
	 */
	protected boolean shouldShowUntimedTodosNow() {

		GregorianCalendar now = new GregorianCalendar();
		int todoFreq = Prefs.getIntPref(PrefName.TODOREMINDERMINS);

		// *** show untimed todos periodically and on startup
		// if no todo reminders are wanted, then skip this
		if (todoFreq == 0)
			return false;

		// determine minutes since midnight
		int hr = now.get(Calendar.HOUR_OF_DAY);
		int min = new GregorianCalendar().get(Calendar.MINUTE);
		int mins_since_midnight = hr * 60 + min;

		return (mins_since_midnight % todoFreq == 0);
	}

}
