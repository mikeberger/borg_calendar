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

package net.sf.borg.ui;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.EventQueue;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;
import java.util.Map.Entry;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Version;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;

/**
 * 
 * @author mberger
 */
public class PopupView extends View {
	static {
		Version
				.addVersion("$Id$");
	}

	/** Creates a new instance of popups */
	public PopupView() {
		addModel(AppointmentModel.getReference());
		timer = new java.util.Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				EventQueue.invokeLater(doPopupChk);
			}
		}, 5 * 1000, Prefs.getIntPref(PrefName.REMINDERCHECKMINS) * 60 * 1000);

	}

	// map that maps appointment keys to the associated popup reminder windows
	private HashMap pops = new HashMap();

	private java.util.Timer timer = null;

	public void destroy() {

		timer.cancel();

		// get rid of any open popups
		Collection s = pops.values();
		Iterator i = s.iterator();
		while (i.hasNext()) {

			ReminderPopup pop = (ReminderPopup) i.next();

			// if frame is gone (killed already), then skip it
			if (pop == null)
				continue;
			pop.dispose();

		}
	}

	// check if any new popup windows are needed and pop them up
	// also beep and bring imminent popups to the front
	private void popup_chk() {

		String enable = Prefs.getPref(PrefName.REMINDERS);
		if (enable.equals("false"))
			return;

		// determine if we are popping up public/private appts
		boolean showpub = false;
		boolean showpriv = false;
		String sp = Prefs.getPref(PrefName.SHOWPUBLIC);
		if (sp.equals("true"))
			showpub = true;
		sp = Prefs.getPref(PrefName.SHOWPRIVATE);
		if (sp.equals("true"))
			showpriv = true;

		// get the current day/month/year
		GregorianCalendar cal = new GregorianCalendar();
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		int day = cal.get(Calendar.DATE);

		// get the key for today in the data model
		int key = AppointmentModel.dkey(year, month, day);

		// get the list of the today's appts
		Collection l = AppointmentModel.getReference().getAppts(key);
		if (l != null) {
			Iterator it = l.iterator();
			Appointment appt;

			// iterate through the day's appts
			while (it.hasNext()) {

				Integer ik = (Integer) it.next();

				try {
					// read the appt record from the data model
					appt = AppointmentModel.getReference().getAppt(
							ik.intValue());

					// check if we should show it based on public/private flags
					if (appt.getPrivate()) {
						if (!showpriv)
							continue;
					} else {
						if (!showpub)
							continue;
					}

					// don't popup "notes"
					if (AppointmentModel.isNote(appt))
						continue;

					Date d = appt.getDate();

					SimpleDateFormat df = AppointmentModel.getTimeFormat();
					String tx = df.format(d);

					// set appt time for computation
					GregorianCalendar now = new GregorianCalendar();
					GregorianCalendar acal = new GregorianCalendar();
					acal.setTime(d);

					// need to set appt time to today in case it is a repeating
					// appt. if it is a repeat,
					// the time will be right, but the day will be the day of
					// the first repeat
					acal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
							now.get(Calendar.DATE));

					// skip the appt if it is outside the time frame of the
					// reminder requests
					long mins_to_go = (acal.getTimeInMillis() - now
							.getTimeInMillis())
							/ (1000 * 60);

					if (outside_reminder_times(mins_to_go))
						continue;

					// skip appt if it is already in the pops list
					// this means that it is already showing - or was shown and
					// killed already
					if (pops.containsKey(ik))
						continue;

					// get appt text - should never really be null
					String xx = appt.getText();
					if (xx == null) {
						continue;
					}

					// create a new frame for a popup and add it to the popup
					// map
					// along with the appt key

					ReminderPopup jd = new ReminderPopup();

					pops.put(ik, jd);

					// add text to date
					tx += " " + xx;
					jd.setText(tx);
					jd.setText2("");
					jd.setVisible(true);
					jd.toFront();

				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		}

		// if any popups that are already displayed are due for showing - make a
		// sound
		// and raise the popup

		// iterate through existing popups
		String enablebeep = Prefs.getPref(PrefName.BEEPINGREMINDERS);
		if (enablebeep.equals("false")) {
			return;
		}

		Set s = pops.entrySet();
		Iterator i = s.iterator();
		while (i.hasNext()) {
			// get popup frame
			Entry me = (Entry) i.next();
			Integer apptkey = (Integer) me.getKey();
			ReminderPopup fr = (ReminderPopup) me.getValue();

			// if frame is gone (killed already), then skip it
			if (fr == null)
				continue;

			// skip if popup not being shown - but still in map
			if (!fr.isDisplayable()) {
				// free resources from JFrame and remove from map
				// map should be last reference to the frame so garbage
				// collection should now be free to clean it up
				me.setValue(null);
				continue;
			}

			
			try {
				// read the appt and get the date
				Appointment appt = AppointmentModel.getReference().getAppt(
						apptkey.intValue());
				Date d = appt.getDate();
				if (d == null)
					continue;

				// determine how far away the appt is
				GregorianCalendar acal = new GregorianCalendar();
				acal.setTime(d);
				GregorianCalendar now = new GregorianCalendar();

				// need to set appt time to today in case it is a repeating
				// appt. if it is a repeat,
				// the time will be right, but the day will be the day of the
				// first repeat
				acal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now
						.get(Calendar.DATE));

				long mins_to_go = (acal.getTimeInMillis() - now
						.getTimeInMillis())
						/ (1000 * 60);

				// if alarm is due to be shown, show it and play sound if
				// requested
				int alarmid = due_for_popup(mins_to_go, appt, fr);
				if (alarmid != -999) {
				    String time_msg;
				    if (alarmid < 0) {
					time_msg = -alarmid
							+ " "
							+ java.util.ResourceBundle.getBundle(
									"resource/borg_resource").getString(
									"minutes_ago");
				    } else if (alarmid == 0) {
					time_msg = java.util.ResourceBundle.getBundle(
									"resource/borg_resource").getString(
									"Now");
				    }
				    else {
					time_msg = alarmid
							+ " "
							+ java.util.ResourceBundle.getBundle(
									"resource/borg_resource").getString(
									"minute_reminder");
				    }

					fr.setText2(time_msg);
					fr.setVisible(true);
					fr.toFront();
					fr.setVisible(true);

					// play sound
					if (Prefs.getPref(PrefName.USESYSTEMBEEP).equals("true")) {
						java.awt.Toolkit.getDefaultToolkit().beep();
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
				// ignore errors here
			}
		}
	}

	private boolean outside_reminder_times(long mins_to_go) {
		return (mins_to_go > PrefName.REMMINUTES[PrefName.REMMINUTES.length - 1] || 
				mins_to_go < PrefName.REMMINUTES[0]);
	}

	// If the reminder should be shown, return the "minutes before appointment"
	// value
	// of the requested reminder, so that we can display this in the reminder
	// If it should not be shown, return -999
	private int due_for_popup(long mins_to_go, Appointment appt, ReminderPopup p) {
		char[] remTimes = new char[PrefName.REMMINUTES.length];
		try {
			remTimes = (appt.getReminderTimes()).toCharArray();

		} catch (Exception e) {
			for (int i = 0; i < PrefName.REMMINUTES.length; ++i) {
				remTimes[i] = 'N';
			}
		}

		int i = 0;
		while (PrefName.REMMINUTES[i] < mins_to_go) {
			++i;
		}

		// shouldnt happen
		if (i >= PrefName.REMMINUTES.length) {
			return -999;
		}

		if (remTimes[i] == 'Y' && p.reminderShown(i) == 'N') {
			p.setReminderShown(i);
			return PrefName.REMMINUTES[i];
		}  
		
		return -999;
		
	}

	final Runnable doPopupChk = new Runnable() {
		public void run() {
			popup_chk();
		}
	};

	public void refresh() {
	}
}
