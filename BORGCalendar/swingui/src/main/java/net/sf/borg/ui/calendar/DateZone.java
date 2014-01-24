/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui.calendar;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.ui.ClipBoard;

/**
 * A DateZone is used to mark a rectagular area on the calendar UIs that
 * corresponds to a particular date. It provides the popup menu and the on-click
 * action that occurs when the user clicks on a date, but outside of any other
 * calendar items
 */
class DateZone {

	// the bounds of the zone
	private Rectangle bounds;

	// the date that the zone represents
	private Date date;

	// the popup menu
	private JPopupMenu popmenu = null;
	private JMenuItem pasteItem = null;

	/**
	 * constructor.
	 * 
	 * @param d
	 *            the date that this zone represents
	 * @param bounds
	 *            the bounds of this zone
	 */
	public DateZone(Date d, Rectangle bounds) {
		this.date = d;
		this.bounds = bounds;
	}

	/**
	 * Gets the bounds.
	 * 
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Gets the popup menu.
	 * 
	 * @return the popup menu
	 */
	public JPopupMenu getMenu() {
		JMenuItem mnuitm;
		if (popmenu == null) {
			popmenu = new JPopupMenu();
			popmenu.add(mnuitm = new JMenuItem(Resource
					.getResourceString("Add_New")));
			mnuitm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					onClick();
				}
			});
			popmenu.add(mnuitm = new JMenuItem(Resource
					.getResourceString("todoquickentry")));
			mnuitm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					quickAdd(true);
				}
			});
			popmenu.add(mnuitm = new JMenuItem(Resource
					.getResourceString("Quick_Note")));
			mnuitm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					quickAdd(false);
				}
			});
			popmenu.add(pasteItem = new JMenuItem(Resource
					.getResourceString("Paste")));
			pasteItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					Appointment appt = (Appointment) ClipBoard.getReference()
							.get(Appointment.class);
					pasteAppt(appt);
				}
			});

		}

		pasteItem
				.setEnabled(ClipBoard.getReference().get(Appointment.class) == null ? false
						: true);
		return popmenu;
	}

	/**
	 * take action on a user mouse double-click, opens appt editor for new appt
	 */
	void onClick() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		AppointmentListView ag = new AppointmentListView(
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DATE));
		ag.showView();
		ag.showApp(-1);
	}

	/**
	 * take action on a user mouse double-click on time interval open an appt
	 * editor for a new appt with preset start hour and minute
	 */
	void onClick(GregorianCalendar cal) {
		AppointmentListView ag = new AppointmentListView(
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DATE));
		// set double-click time
		ag.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		ag.showView();
		ag.showApp(-1);
	}

	/**
	 * quick add menu action - prompt for text and create an appt based on
	 * default values
	 * 
	 * @param todo
	 *            - true if added appt should be a todo
	 */
	private void quickAdd(boolean todo) {

		// prompt for todo text
		String tdtext = JOptionPane.showInputDialog("", Resource
				.getResourceString("Please_enter_some_appointment_text"));
		if (tdtext == null || tdtext.trim().isEmpty()) {
			return;
		}

		// load up a default appt from any saved prefs
		Appointment appt = AppointmentModel.getReference()
				.getDefaultAppointment();
		if (appt == null)
			appt = AppointmentModel.getReference().newAppt();

		// set the appt date. leave the appt untimed
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.AM_PM, Calendar.AM);
		appt.setDate(cal.getTime());
		appt.setText(tdtext);
		appt.setTodo(todo);
		appt.setUntimed("Y");

		AppointmentModel.getReference().saveAppt(appt);

	}

	private void pasteAppt(Appointment appt) {
		GregorianCalendar newcal = new GregorianCalendar();
		newcal.setTime(date);

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(appt.getDate());
		cal.set(Calendar.YEAR, newcal.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, newcal.get(Calendar.MONTH));
		cal.set(Calendar.DATE, newcal.get(Calendar.DATE));

		String freq = appt.getFrequency();

		try {
			if (!Repeat.isCompatible(cal,
					Repeat.getFreqString(Repeat.getFreq(freq)),
					Repeat.getDaylist(freq)))
				throw new Warning(Resource.getResourceString("recur_compat"));
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
			return;
		}

		appt.setDate(cal.getTime());

		appt.setKey(-1);
		AppointmentModel.getReference().saveAppt(appt);

	}

	/**
	 * Sets the bounds.
	 * 
	 * @param bounds
	 *            the new bounds
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	/**
	 * Sets the date.
	 * 
	 * @param date
	 *            the new date
	 */
	public void setDate(Date date) {
		this.date = date;
	}
}
