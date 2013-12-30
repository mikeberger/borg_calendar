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

Copyright 2012 by Mike Berger
 */
package net.sf.borg.ui.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;

import com.toedter.calendar.JDateChooser;

/**
 * Creates a JPanel that lets the user select a date and time
 */
public class DateTimePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame f = new JFrame();
				f.setContentPane(new DateTimePanel(true, true));
				f.pack();
				f.setVisible(true);
				JFrame f2 = new JFrame();
				f2.setContentPane(new DateTimePanel(false, false));
				f2.pack();
				f2.setVisible(true);
			}
		});

	}

	/** The ampm box. */
	private JComboBox<String> ampmBox = new JComboBox<String>();

	private DefaultComboBoxModel<String> ampmModel = new DefaultComboBoxModel<String>(
			new String[] { "AM", "PM" });

	private JDateChooser dateChooser = new JDateChooser();

	/** The hour box. */
	private JComboBox<String> hourBox = new JComboBox<String>();
	
	/**
	 * combo box choices for setting the hour if we are using 24-hour time
	 */
	private DefaultComboBoxModel<String> milHourModel = new DefaultComboBoxModel<String>(
			new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
					"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
					"20", "21", "22", "23" });

	private boolean milTime = false;

	private DefaultComboBoxModel<String> minModel = new DefaultComboBoxModel<String>(
			new String[] { "00", "05", "10", "15", "20", "25", "30", "35",
					"40", "45", "50", "55" });

	/** The minute box. */
	private JComboBox<String> minuteBox = new JComboBox<String>();

	/**
	 * combo box choices for setting 12-hr time
	 */
	private DefaultComboBoxModel<String> normHourModel = new DefaultComboBoxModel<String>(
			new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
					"11", "12" });

	private boolean showDate = true;

	
	public DateTimePanel(boolean showDate, boolean milTime) {
		super();
		this.showDate = showDate;
		this.milTime = milTime;
		initialize();
	}

	/**
	 * add an action listener that gets notified if the time changes
	 * @param l
	 */
	public void addTimeListener(ActionListener l) {
		hourBox.addActionListener(l);
		minuteBox.addActionListener(l);
		ampmBox.addActionListener(l);
	}

	/**
	 * Gets the time shown in the panel.
	 * 
	 * @return the time
	 * @throws Warning
	 */
	public Date getTime() throws Warning {
		Calendar cal = dateChooser.getCalendar();
		if (milTime) {
			cal.set(Calendar.HOUR_OF_DAY, hourBox.getSelectedIndex());
		} else {
			int hr = hourBox.getSelectedIndex() + 1;
			if (hr == 12)
				hr = 0;
			String val = (String) ampmBox.getSelectedItem();
			if (val.equals("PM"))
				hr += 12;
			cal.set(Calendar.HOUR_OF_DAY, hr);

		}

		try {
			int min = Integer.parseInt((String) minuteBox.getSelectedItem());
			if( min < 0 || min > 59 )
				throw new Warning(Resource.getResourceString("InvalidMinute")
						+ ": " + minuteBox.getSelectedItem());
			cal.set(Calendar.MINUTE, min);
		} catch (NumberFormatException n) {
			throw new Warning(Resource.getResourceString("InvalidMinute")
					+ ": " + minuteBox.getSelectedItem());
		}
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	
	private void initialize() {

		this.setLayout(new GridBagLayout());

		this.add(dateChooser,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		GridBagConstraints gbc = GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 1.0, 0.0);
		gbc.insets = new Insets(0, 0, 0, 0);

		JPanel timePanel = new JPanel();
		timePanel.add(hourBox, null);
		minuteBox.setModel(minModel);
		minuteBox.setEditable(true);
		ComboBoxEditor editor = minuteBox.getEditor();
		JTextField textField = (JTextField) editor.getEditorComponent();
		textField.setColumns(2);
		timePanel.add(minuteBox, null);
		ampmBox.setModel(ampmModel);
		timePanel.add(ampmBox, null);
		this.add(timePanel, gbc);

		update(showDate, milTime);

	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		hourBox.setEnabled(enabled);
		minuteBox.setEnabled(enabled);
		ampmBox.setEnabled(enabled);
		dateChooser.setEnabled(enabled);

	}

	/**
	 * Sets the date and time shown in the panel.
	 * 
	 * @param d
	 *            the new time
	 */
	public void setTime(Date d) {
		Calendar cal = new GregorianCalendar();
		if (d != null)
			cal.setTime(d);
		dateChooser.setCalendar(cal);
		if (milTime) {
			hourBox.setSelectedIndex(cal.get(Calendar.HOUR_OF_DAY));
		} else {
			int hr = cal.get(Calendar.HOUR);
			if (hr == 0)
				hr = 12;
			hourBox.setSelectedIndex(hr - 1);
		}
		String mins = Integer.toString(cal.get(Calendar.MINUTE));
		if (mins.length() == 1)
			mins = "0" + mins;
		minuteBox.setSelectedItem(mins);
		if (cal.get(Calendar.HOUR_OF_DAY) < 12)
			ampmBox.setSelectedItem("AM");
		else
			ampmBox.setSelectedItem("PM");

	}

	/**
	 * update to reflect the settings of show date and military time
	 * @param showDate - show the date
	 * @param milTime - use 24 hour time
	 */
	public void update(boolean show_date, boolean mil_Time) {
		if (mil_Time) {
			hourBox.setModel(milHourModel);
			ampmBox.setVisible(false);
		} else {
			hourBox.setModel(normHourModel);
			ampmBox.setVisible(true);
		}

		dateChooser.setVisible(show_date);
	}
}
