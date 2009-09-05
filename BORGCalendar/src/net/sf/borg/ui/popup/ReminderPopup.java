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

package net.sf.borg.ui.popup;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.View;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * Popop window for a Reminder
 */
class ReminderPopup extends View {
	
	/** The appointment. */
	private Appointment appointment = null;
	
	/** The appointment information. */
	private JLabel appointmentInformation = null;
	
	/** The time to go message. */
	private JLabel timeToGoMessage = null;
	
	/** The no more reminders button. */
	private JRadioButton noMoreRemindersButton = null;
	
	/** The reminders shown flags */
	private char[] remindersShown;
	
	/** The was ever shown flag. */
	private boolean wasEverShown = false;

	/**
	 * Instantiates a new reminder popup for an appointment
	 * 
	 * @param ap the appointment
	 */
	public ReminderPopup(Appointment ap) {
		
		super();
		
		appointment = ap;
	
		this.setTitle("Borg " + Resource.getResourceString("Reminder"));
		
		initialize();
		
		// set appt info in the reminder
		String apptinfoText = "";
		if (!AppointmentModel.isNote(appointment)) {
			SimpleDateFormat df = AppointmentModel.getTimeFormat();
			apptinfoText = df.format(appointment.getDate());
		}
		apptinfoText += " " + appointment.getText();
		appointmentInformation.setText(apptinfoText);
		
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		// initialize reminders shown flags
		remindersShown = new char[ReminderTimes.getNum()];
		for (int i = 0; i < ReminderTimes.getNum(); ++i) {
			remindersShown[i] = 'N';
		}

	}

	/* (non-Javadoc)
	 * @see net.sf.borg.ui.View#destroy()
	 */
	@Override
	public void destroy() {
		this.dispose();
	}


	/**
	 * This method initializes the UI
	 * 
	 */
	private void initialize() {
		
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		
		appointmentInformation = new JLabel();
		appointmentInformation.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		topPanel.add(appointmentInformation, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.NONE, 1.0, 0.0)); 

		timeToGoMessage = new JLabel();
		timeToGoMessage.setText("");
		timeToGoMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		
		topPanel.add(timeToGoMessage, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.NONE, 1.0, 0.0)); 	
		noMoreRemindersButton = new JRadioButton();
		ResourceHelper.setText(noMoreRemindersButton, "No_more");
		noMoreRemindersButton
				.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		topPanel.add(noMoreRemindersButton, GridBagConstraintsFactory.create(0, 2, GridBagConstraints.NONE, 1.0, 0.0)); 
		
		//
		// button panel 
		//
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		JButton dismissButton = new JButton();
		ResourceHelper.setText(dismissButton, "Dismiss");
		dismissButton.setActionCommand("close_it");
		dismissButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (noMoreRemindersButton.isSelected()) {
					dispose();
				} else {
					setVisible(false);
				}
			}
		});
		buttonPanel.add(dismissButton, GridBagConstraintsFactory.create(0, 0));
		
		
		if (appointment.getTodo() == true) {
			
			JButton doneButton = new JButton();
			ResourceHelper.setText(doneButton, "Done_(Delete)");
			doneButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						AppointmentModel.getReference().do_todo(appointment.getKey(),
								true);
					} catch (Exception e1) {
						Errmsg.errmsg(e1);
					}
					destroy();
				}
			});
			buttonPanel.add(doneButton, GridBagConstraintsFactory.create(1, 0));
			
			JButton doneNoDeleteButton = new JButton();
			ResourceHelper.setText(doneNoDeleteButton, "Done_(No_Delete)");
			doneNoDeleteButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						AppointmentModel.getReference().do_todo(appointment.getKey(),
								false);
					} catch (Exception e1) {
						Errmsg.errmsg(e1);
					}
					destroy();
				}
			});
			buttonPanel.add(doneNoDeleteButton, GridBagConstraintsFactory.create(2, 0));
		}
		
		topPanel.add(buttonPanel, GridBagConstraintsFactory.create(0,3));

		this.setContentPane(topPanel);
		this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		this.setSize(400, 250);

	}

	/* (non-Javadoc)
	 * @see net.sf.borg.ui.View#refresh()
	 */
	@Override
	public void refresh() {
	}

	/**
	 * Return Y if Reminder time i has been shown, otherwise N.
	 * 
	 * @param i the index of the reminder time.
	 * 
	 * @return "Y" is the remidner was shown already
	 */
	public char reminderShown(int i) {
		return remindersShown[i];
	}

	/**
	 * set the flag for reminder i to shown.
	 * 
	 * @param i the index of the reminder time
	 */
	public void setReminderShown(int i) {
		remindersShown[i] = 'Y';
	}

	/**
	 * Sets the shown flag.
	 * 
	 * @param s was this popup ever shown
	 */
	public void setShown(boolean s) {
		wasEverShown = s;
	}

	/**
	 * set the Time to go message.
	 * 
	 * @param str the new time to go message
	 */
	public void timeToGoMessage(String str) {
		timeToGoMessage.setText(str);
	}

	/**
	 * get the shown flag.
	 * 
	 * @return true if the popup was ever shown
	 */
	public boolean wasEverShown() {
		return wasEverShown;
	}

} 
