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
import java.util.TimerTask;
import java.util.Map.Entry;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.ui.View;

/**
 * 
 * @author mberger
 */
public class PopupView extends View {

	static private PopupView singleton = null;
	
	// map that maps appointment keys to the associated popup reminder
	// windows
	private HashMap<Integer,ReminderPopup> pops = new HashMap<Integer,ReminderPopup>();

	private java.util.Timer timer = null;

	final Runnable doPopupChk = new Runnable() {
		public void run() {
			popup_chk();
		}
	};
	
	public static PopupView getReference()
	{
		if( singleton == null )
		{
			singleton = new PopupView();
		}
		return singleton;
	}

	/** Creates a new instance of popups */
	private PopupView() {
		addModel(AppointmentModel.getReference());
		timer = new java.util.Timer();
		// start popups at next minute on system clock
		GregorianCalendar cur = new GregorianCalendar();
		int secs_left = 60 - cur.get(Calendar.SECOND);
		timer.schedule(new TimerTask() {
			public void run() {
				EventQueue.invokeLater(doPopupChk);
			}
		}, secs_left * 1000,
				Prefs.getIntPref(PrefName.REMINDERCHECKMINS) * 60 * 1000);

	}

	public void destroy() {

		timer.cancel();

		// get rid of any open popups
		Collection<ReminderPopup> s = pops.values();
		Iterator<ReminderPopup> i = s.iterator();
		while (i.hasNext()) {

			ReminderPopup pop = i.next();

			// if frame is gone (killed already), then skip it
			if (pop == null)
				continue;
			pop.dispose();

		}
	}

	// find any popups that should no longer be displayed based on updates to
	// the DB and delete them
	public void refresh() {
		ArrayList<Integer> dels = new ArrayList<Integer>();
		Set<Entry<Integer, ReminderPopup>> s = pops.entrySet();
		
		for( Entry<Integer, ReminderPopup> me : s)
		 {
			// get popup frame
			
			Integer apptkey = me.getKey();
			ReminderPopup fr =  me.getValue();

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
				// System.out.println("checking popup for appt: " + apptkey);
				// read the appt and get the date
				Appointment appt = AppointmentModel.getReference().getAppt(
						apptkey.intValue());
				if (!shouldBeShown(appt)) {
					fr.dispose();
					dels.add(apptkey);
					// System.out.println("disposing popup for appt: " +
					// apptkey);
				}
			} catch (Exception e) {
				// appt cannot be read - kill the popup
				dels.add(apptkey);
				fr.dispose();
				// System.out.println("disposing popup for appt: " + apptkey);
			}
		}

		Iterator<Integer> it = dels.iterator();
		while (it.hasNext()) {
			pops.remove(it.next());
		}

	}

	// If the reminder should be shown, return the "minutes before
	// appointment"
	// value
	// of the requested reminder, so that we can display this in the
	// reminder
	// If it should not be shown, return -999
	private int due_for_popup(long mins_to_go, Appointment appt, ReminderPopup p) {
		char[] remTimes = new char[ReminderTimes.getNum()];
		try {
			remTimes = (appt.getReminderTimes()).toCharArray();

		} catch (Exception e) {
			for (int i = 0; i < ReminderTimes.getNum(); ++i) {
				remTimes[i] = 'N';
			}
		}

		int i = 0;
		while (ReminderTimes.getTimes(i) < mins_to_go) {
			++i;
		}

		// shouldnt happen
		if (i >= ReminderTimes.getNum()) {
			return -999;
		}

		if (remTimes[i] == 'Y' && p.reminderShown(i) == 'N') {
			p.setReminderShown(i);
			return ReminderTimes.getTimes(i);
		}

		return -999;

	}

	static private boolean outside_reminder_times(long mins_to_go,
			Appointment appt) {

		int earliest = -999;
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
				earliest = ReminderTimes.getTimes(i);
		}

		// System.out.println("mtg =" + mins_to_go + " rt=" +
		// appt.getReminderTimes() + " l=" + latest + " e=" + earliest);
		if (earliest == -999)
			return true;

		return (mins_to_go > earliest || mins_to_go < ReminderTimes.getTimes(0));
	}

	private static boolean shouldBeShown(Appointment appt) {

		if (appt == null )
			return false;

		// check if we should show it based on public/private
		// flags

		boolean showpub = false;
		boolean showpriv = false;
		String sp = Prefs.getPref(PrefName.SHOWPUBLIC);
		if (sp.equals("true"))
			showpub = true;
		sp = Prefs.getPref(PrefName.SHOWPRIVATE);
		if (sp.equals("true"))
			showpriv = true;
		if (appt.getPrivate()) {
			if (!showpriv)
				return false;
		} else {
			if (!showpub)
				return false;
		}

		// don't popup "notes"
		if (AppointmentModel.isNote(appt) && !appt.getTodo())
			return false;

		if (AppointmentModel.isNote(appt) && appt.getTodo()
				&& appt.getReminderTimes() != null
				&& appt.getReminderTimes().indexOf('Y') != -1) {
			// non-timed todo

			// make sure todo is not done for today
			GregorianCalendar td = new GregorianCalendar();
			td.set(Calendar.HOUR_OF_DAY, 23);
			td.set(Calendar.MINUTE, 59);

			Date nt = appt.getNextTodo();
			if( nt == null ) nt = appt.getDate();
			if (nt != null && nt.after(td.getTime())) {
				return false;
			}
		} else {
			Date d = appt.getDate();

			// set appt time for computation
			GregorianCalendar now = new GregorianCalendar();
			GregorianCalendar acal = new GregorianCalendar();
			acal.setTime(d);

			// need to set appt time to today in case it is a
			// repeating
			// appt. if it is a repeat,
			// the time will be right, but the day will be the day
			// of
			// the first repeat
			acal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now
					.get(Calendar.DATE));

			// skip the appt if it is outside the time frame of the
			// reminder requests
			long mins_to_go = acal.getTimeInMillis() / (1000 * 60)
					- now.getTimeInMillis() / (1000 * 60);

			if (outside_reminder_times(mins_to_go, appt))
				return false;
		}

		return true;
	}

	// check if any new popup windows are needed and pop them up
	// also beep and bring imminent popups to the front
	private void popup_chk() {

		String enable = Prefs.getPref(PrefName.REMINDERS);
		if (enable.equals("false"))
			return;

		// get the list of the today's appts
		List<Integer> l = AppointmentModel.getReference().getAppts(new Date());
		if (l != null) {
			Iterator<Integer> it = l.iterator();
			Appointment appt;

			// iterate through the day's appts
			while (it.hasNext()) {

				Integer ik = it.next();

				try {
					// read the appt record from the data model
					appt = AppointmentModel.getReference().getAppt(
							ik.intValue());

					if (!shouldBeShown(appt))
						continue;

					// skip appt if it is already in the pops list
					// this means that it is already showing - or was shown
					// and
					// killed already
					if (pops.containsKey(ik))
						continue;

					// create a new frame for a popup and add it to the
					// popup
					// map
					// along with the appt key

					ReminderPopup jd = new ReminderPopup(appt);

					pops.put(ik, jd);

					
					jd.setText2("");
					jd.setVisible(true);
					jd.toFront();
					

				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		}
		
		// get past untimed todos that are not done
		Collection<Appointment> tds = AppointmentModel.getReference().get_todos();
		if (tds != null) {
			Iterator<Appointment> it = tds.iterator();

			// iterate through the day's appts
			while (it.hasNext()) {

				Appointment appt = it.next();

				try {
					
					// the only old todos we are checking for here are non-timed
					if( !AppointmentModel.isNote(appt))
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

		Set<Entry<Integer,ReminderPopup>> s = pops.entrySet();
		Iterator<Entry<Integer, ReminderPopup>> i = s.iterator();
		while (i.hasNext()) {
			// get popup frame
			Entry<Integer, ReminderPopup> me = i.next();
			Integer apptkey = me.getKey();
			ReminderPopup fr = me.getValue();

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

				String time_msg;

				if (AppointmentModel.isNote(appt) && appt.getTodo()) {

					// show on the half hour or on startup
					int min = new GregorianCalendar().get(Calendar.MINUTE);
					if (fr.wasShown() && !(min == 0 || min == 30))
						continue;

					time_msg = Resource.getResourceString("To_Do") + " "
							+ Resource.getResourceString("Today");
				} else {
					Date d = appt.getDate();
					if (d == null)
						continue;

					// determine how far away the appt is
					GregorianCalendar acal = new GregorianCalendar();
					acal.setTime(d);
					GregorianCalendar now = new GregorianCalendar();

					// need to set appt time to today in case it is a
					// repeating
					// appt. if it is a repeat,
					// the time will be right, but the day will be the day
					// of
					// the
					// first repeat
					acal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
							now.get(Calendar.DATE));

					long mins_to_go = acal.getTimeInMillis() / (1000 * 60)
							- now.getTimeInMillis() / (1000 * 60);

					// if alarm is due to be shown, show it and play sound
					// if
					// requested
					int alarmid = due_for_popup(mins_to_go, appt, fr);
					if (alarmid == -999)
						continue;

					if (alarmid < 0) {
						time_msg = -alarmid + " "
								+ Resource.getResourceString("minutes_ago");
					} else if (alarmid == 0) {
						time_msg = Resource.getResourceString("Now");
					} else {
						time_msg = alarmid + " "
								+ Resource.getResourceString("minute_reminder");
					}
				}

				fr.setText2(time_msg);
				fr.setVisible(true);
				fr.toFront();
				fr.setVisible(true);
				fr.setShown(true);

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

			} catch (Exception e) {
				// ignore errors here
			}
		}
	}
	
	public void showAll()
	{
		Set<Entry<Integer, ReminderPopup>> s = pops.entrySet();
		Iterator<Entry<Integer, ReminderPopup>> i = s.iterator();
		while (i.hasNext()) {
			// get popup frame
			Entry<Integer, ReminderPopup> me = i.next();
			ReminderPopup fr =  me.getValue();

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
			
			fr.setVisible(true);
			fr.toFront();
			
		}
	}
	
	public void hideAll()
	{
		Set<Entry<Integer, ReminderPopup>> s = pops.entrySet();
		Iterator<Entry<Integer, ReminderPopup>> i = s.iterator();
		while (i.hasNext()) {
			// get popup frame
			Entry<Integer, ReminderPopup> me = i.next();
			ReminderPopup fr =  me.getValue();

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
			
			fr.setVisible(false);		
			
		}
	}
}
