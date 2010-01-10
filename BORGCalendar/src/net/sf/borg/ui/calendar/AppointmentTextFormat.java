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
 * Copyright 2009 by Mike Berger
 */
package net.sf.borg.ui.calendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;

/**
 * common code to format an appointment's text for display on the UI
 * 
 */
public class AppointmentTextFormat {

	/**
	 * generate the appointment string for display of an appointment on a given
	 * day. Optionally prepend things like time of day, repeat number, vacation
	 * day number...
	 * 
	 * @param appt
	 *            - the appointment
	 * @param date
	 *            - the date that the appt is being displayed for or null. The
	 *            date is used to identify an instance of a repeating appt
	 * @return the formatted appt string to display
	 */
	static public String format(Appointment appt, Date date) {

		if (appt.getText() == null) {
			return "";
		}
		
		Calendar day = new GregorianCalendar();
		day.setTime(date);

		String theFormattedText = "";

		// add time in front of the appt text
		if (!AppointmentModel.isNote(appt)) {
			Date d = appt.getDate();
			if( d != null )
			{	
				SimpleDateFormat sdf = AppointmentModel.getTimeFormat();
				theFormattedText += sdf.format(d) + " ";
			}
		}

		if (appt.isEncrypted()) {
			// don't show the raw encrypted text to the user - show a pre-defined short string
			theFormattedText += Resource
					.getResourceString("EncryptedItemShort");
		} else {
			// if the text is empty - skip it - should never be
			String xx = appt.getText();
			String trunc = Prefs.getPref(PrefName.TRUNCAPPT);
			if (trunc.equals("true")) {
				// !!!!! only show first line of appointment text !!!!!!
				int ii = xx.indexOf('\n');
				if (ii != -1) {
					theFormattedText += xx.substring(0, ii);
				} else {
					theFormattedText += xx;
				}
			} else {
				theFormattedText += xx;
			}
		}

		// add repeat number
		if (Repeat.getRptNum(appt.getFrequency())) {
			theFormattedText += " (" + Repeat.calculateRepeatNumber(day, appt)
					+ ")";
		}

		if (appt.getVacation() != null && appt.getVacation().intValue() != 0) {
			day.set(Calendar.HOUR_OF_DAY, 11);
			double vacationCount = AppointmentModel.getReference()
					.vacationCount(day.getTime());
			theFormattedText = "[" + vacationCount + "] " + theFormattedText;
		}

		return theFormattedText;
	}
}
