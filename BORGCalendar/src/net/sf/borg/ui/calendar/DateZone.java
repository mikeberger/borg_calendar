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
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.XTree;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.xml.AppointmentXMLAdapter;
import net.sf.borg.ui.MultiView;

class DateZone {

	private Rectangle bounds;

	private Date date;

	public DateZone(Date d, Rectangle bounds) {
		this.date = d;
		this.bounds = bounds;
	}

	public void createAppt(int topmins, int botmins, String text) {

		// get default appt values, if any
		Appointment appt = null;
		String defApptXml = Prefs.getPref(PrefName.DEFAULT_APPT);
		if (!defApptXml.equals("")) {
			try {
				XTree xt = XTree.readFromBuffer(defApptXml);
				AppointmentXMLAdapter axa = new AppointmentXMLAdapter();
				appt = axa.fromXml(xt);

			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

		if (appt == null) {
			appt = AppointmentModel.getReference().newAppt();
		}

		// System.out.println(top + " " + bottom);
		int realtime = topmins;
		int hour = realtime / 60;
		int min = realtime % 60;
		min = (min / 5) * 5;
		Calendar startCal = new GregorianCalendar();
		startCal.setTime(date);
		startCal.set(Calendar.HOUR_OF_DAY, hour);
		startCal.set(Calendar.MINUTE, min);
		appt.setDate(startCal.getTime());

		int realend = botmins;
		int ehour = realend / 60;
		int emin = realend % 60;
		emin = (emin / 5) * 5;
		int dur = 60 * (ehour - hour) + emin - min;

		appt.setDuration(new Integer(dur));

		if (dur > 0)
			appt.setUntimed(null);

		appt.setText(text);
		AppointmentModel.getReference().saveAppt(appt, true);
	}

	void edit() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		AppointmentListView ag = new AppointmentListView(
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
						.get(Calendar.DATE));
		MultiView.getMainView().addView(ag);

	}

	private void quick_todo() {

		String tdtext = JOptionPane.showInputDialog("", Resource
				.getPlainResourceString("Please_enter_some_appointment_text"));
		if (tdtext == null)
			return;

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);

		AppointmentModel calmod_ = AppointmentModel.getReference();

		Appointment r = calmod_.newAppt();
		String defApptXml = Prefs.getPref(PrefName.DEFAULT_APPT);
		if (!defApptXml.equals("")) {
			try {
				XTree xt = XTree.readFromBuffer(defApptXml);
				AppointmentXMLAdapter axa = new AppointmentXMLAdapter();
				r = axa.fromXml(xt);
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.AM_PM, Calendar.AM);
		r.setDate(cal.getTime());
		r.setText(tdtext);
		r.setTodo(true);

		calmod_.saveAppt(r, true);

	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	private JPopupMenu popmenu = null;

	public JPopupMenu getMenu() {
		JMenuItem mnuitm;
		if (popmenu == null) {
			popmenu = new JPopupMenu();
			popmenu.add(mnuitm = new JMenuItem(Resource
					.getPlainResourceString("Add_New")));
			mnuitm.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					edit();
				}
			});
			popmenu.add(mnuitm = new JMenuItem(Resource
					.getPlainResourceString("todoquickentry")));
			mnuitm.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					quick_todo();
				}
			});

		}
		return popmenu;
	}
}
