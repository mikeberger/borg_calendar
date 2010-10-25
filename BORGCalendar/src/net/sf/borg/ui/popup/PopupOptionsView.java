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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.sf.borg.common.Resource;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.calendar.AppointmentPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * PopupOptionsView displays a dialog that lets the user choose
 * which reminder times to activate or deactivate
 */
public class PopupOptionsView extends JDialog {

	private static final long serialVersionUID = 1L;

	/** The reminder time check boxes. */
	private JCheckBox[] reminderTimeCheckBoxes;
	
	/** The parent appointment panel. */
	private AppointmentPanel parentAppointmentPanel;
	
	/** The reminder times on off array. */
	private char[] reminderTimesOnOffArray;

	/**
	 * Instantiates a new popup options view.
	 * 
	 * @param remtimes the reminder times Y/N (on/off) flags from the appointment
	 * @param appPanel the appointment panel 
	 */
	public PopupOptionsView(String remtimes, String appointmentName, AppointmentPanel appPanel) {
		super();
		
		parentAppointmentPanel = appPanel;
		reminderTimesOnOffArray = remtimes.toCharArray();

		initialize(appointmentName);

		// escape key closes window
		getRootPane().registerKeyboardAction(new ActionListener() {
			@Override
			public final void actionPerformed(ActionEvent e) {
				dispose();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		this.setTitle("Popup_Times");
		
		pack();

		this.setModal(true);

	}

	/**
	 * Creates the button panel.
	 * 
	 * @return the button panel
	 */
	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();

		JButton saveButton = new JButton();
		ResourceHelper.setText(saveButton, "Save");
		saveButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				
				// set the remidner times array from the check box values
				for (int i = 0; i < ReminderTimes.getNum(); ++i) {
					if (reminderTimeCheckBoxes[i].isSelected()) {
						reminderTimesOnOffArray[i] = 'Y';
					} else {
						reminderTimesOnOffArray[i] = 'N';
					}
				}
				
				// save the changes to the appointment panel
				parentAppointmentPanel.setPopupTimesString(new String(reminderTimesOnOffArray));
				dispose();
			}
		});
		buttonPanel.add(saveButton, null);

		JButton clearAllButton = new JButton();
		ResourceHelper.setText(clearAllButton, "clear_all");
		clearAllButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				for (int i = 0; i < ReminderTimes.getNum(); ++i) {
					reminderTimeCheckBoxes[i].setSelected(false);
				}
			}
		});
		buttonPanel.add(clearAllButton, null);
		
		JButton selectAllButton = new JButton();
		ResourceHelper.setText(selectAllButton, "select_all");
		selectAllButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				for (int i = 0; i < ReminderTimes.getNum(); ++i) {
					reminderTimeCheckBoxes[i].setSelected(true);
				}
			}
		});
		buttonPanel.add(selectAllButton, null); 

		JButton dismissButton = new JButton();
		ResourceHelper.setText(dismissButton, "Dismiss");
		dismissButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Stop16.gif")));
		dismissButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dispose();
			}
		});
		
		buttonPanel.add(dismissButton, null);
		return buttonPanel;
	}

	/**
	 * This method initializes the UI.
	 * 
	 * @param appointmentName the appointment name (title)
	 */
	private void initialize(String appointmentName) {
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
	
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridBagLayout());
		
		JLabel jAlarmLabel = new JLabel();
		ResourceHelper.setText(jAlarmLabel, "custom_times_header");
		jAlarmLabel.setText(jAlarmLabel.getText() + " '" + appointmentName
				+ "'");
		checkBoxPanel.add(jAlarmLabel, new GridBagConstraints(0, 0, 2, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(4, 4, 4, 4), 0, 0));

		// create reminder time check boxes
		reminderTimeCheckBoxes = new JCheckBox[ReminderTimes.getNum()];
		for (int i = 0; i < ReminderTimes.getNum(); ++i) {
			reminderTimeCheckBoxes[i] = new JCheckBox(minutes_string(i));
		}
		
		// add boxes in 2 columns
		int boxesPerColumn = reminderTimeCheckBoxes.length / 2;
		
		// left column
		for (int i = 0; i < boxesPerColumn; ++i) {
			checkBoxPanel.add(reminderTimeCheckBoxes[i], new GridBagConstraints(0, i + 1, 1,
					1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));

			if (reminderTimesOnOffArray[i] == 'Y') {
				reminderTimeCheckBoxes[i].setSelected(true);
			} else {
				reminderTimeCheckBoxes[i].setSelected(false);
			}
		}

		// right column
		for (int i = boxesPerColumn; i < reminderTimeCheckBoxes.length; ++i) {
			checkBoxPanel.add(reminderTimeCheckBoxes[i], new GridBagConstraints(1, i - boxesPerColumn
					+ 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
			if (reminderTimesOnOffArray[i] == 'Y') {
				reminderTimeCheckBoxes[i].setSelected(true);
			} else {
				reminderTimeCheckBoxes[i].setSelected(false);
			}
		}
		
		topPanel.add(checkBoxPanel, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH, 1.0, 1.0));
		topPanel.add(createButtonPanel(), GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 0.0, 1.0));

		this.setContentPane(topPanel);
	}

	/**
	 * generate a human readable string for a particular reminder time
	 * 
	 * @param i the index of the remidner time
	 * 
	 * @return the string
	 */
	private String minutes_string(int i) {
		
		int reminderTimeMinutes = ReminderTimes.getTimes(i);
		int reminderTimeMinutesAbsoluteValue = (reminderTimeMinutes >= 0 ? reminderTimeMinutes : -reminderTimeMinutes);
		int reminderTimeHours = reminderTimeMinutesAbsoluteValue / 60;
		int reminderTimeMinutesPastTheHour = reminderTimeMinutesAbsoluteValue % 60;
		
		String minutesString;
		String hoursString;
		
		if (reminderTimeHours > 1) {
			hoursString = reminderTimeHours + " " + Resource.getResourceString("Hours");
		} else if (reminderTimeHours > 0) {
			hoursString = reminderTimeHours + " " + Resource.getResourceString("Hour");
		} else {
			hoursString = "";
		}

		if (reminderTimeMinutesPastTheHour > 1) {
			minutesString = reminderTimeMinutesPastTheHour + " " + Resource.getResourceString("Minutes");
		} else if (reminderTimeMinutesPastTheHour > 0) {
			minutesString = reminderTimeMinutesPastTheHour + " " + Resource.getResourceString("Minute");
		} else if (reminderTimeHours >= 1) {
			minutesString = "";
		} else {
			minutesString = reminderTimeMinutesPastTheHour + " " + Resource.getResourceString("Minutes");
		}

		// space between hours and minutes
		if (!hoursString.equals("") && !minutesString.equals(""))
			minutesString = " " + minutesString;

		String beforeOrAfterString;
		if (reminderTimeMinutes > 0) {
			beforeOrAfterString = " " + Resource.getResourceString("Before");
		} else if (reminderTimeMinutes == 0) {
			beforeOrAfterString = "";
		} else {
			beforeOrAfterString = " " + Resource.getResourceString("After");
		}

		return hoursString + minutesString + beforeOrAfterString;
	}

} 
