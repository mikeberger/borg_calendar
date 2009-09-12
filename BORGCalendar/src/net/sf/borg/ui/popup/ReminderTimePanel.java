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
package net.sf.borg.ui.popup;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import net.sf.borg.common.Resource;
import net.sf.borg.model.ReminderTimes;

/**
 * Panel used to edit the minutes of each of the user tunable reminder times. This panel is
 * used in the main options window
 */
public class ReminderTimePanel extends JPanel {

	/** The number Of Reminder Times. */
	private int numberOfReminderTimes = 0;

	/** The spinners for setting the reminder times */
	private JSpinner spinners[];

	/**
	 * constructor.
	 */
	public ReminderTimePanel() {
		super();
		
		numberOfReminderTimes = ReminderTimes.getNum();
		
		initialize();
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		
		// border
		String title = Resource.getResourceString("Popup_Times") + " ("
				+ Resource.getResourceString("Minutes") + ")";
		Border b = BorderFactory.createTitledBorder(this.getBorder(), title);
		setBorder(b);
		
		setLayout(new GridLayout(2, 0));
		
		// add the spinners
		spinners = new JSpinner[numberOfReminderTimes];
		for (int i = 0; i < numberOfReminderTimes; i++) {
			spinners[i] = new JSpinner(new SpinnerNumberModel(0,-99999,99999,1));
			this.add(spinners[i]);
		}
		
		// load the times
		loadTimes();
	}

	/**
	 * Load the spinner valies from stored prefs
	 */
	private void loadTimes() {
		for (int i = 0; i < numberOfReminderTimes; i++) {
			spinners[i].setValue(new Integer(ReminderTimes.getTimes(i)));
		}
	}

	/**
	 * Stores the reminder times from the UI settings
	 */
	public void setTimes() {
		int arr[] = new int[numberOfReminderTimes];
		for (int i = 0; i < numberOfReminderTimes; i++) {
			Integer ii = (Integer) spinners[i].getValue();
			arr[i] = ii.intValue();
		}
		ReminderTimes.setTimes(arr);
		loadTimes();
	}

}
