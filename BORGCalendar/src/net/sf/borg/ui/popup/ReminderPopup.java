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
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.View;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * Popop window for a single Reminder
 */
class ReminderPopup extends View {

	private static final long serialVersionUID = 1L;

	/** The Reminder Instance being shown. */
	private ReminderInstance reminderInstance = null;

	/** The appointment information. */
	private JLabel appointmentInformation = null;

	/** The time to go message. */
	private JLabel timeToGoMessage = null;

	/** The no more reminders button. */
	private JRadioButton noMoreRemindersButton = null;

	/**
	 * Instantiates a new reminder popup for an appointment
	 * 
	 * @param inst
	 *            the reminder instance to show
	 */
	public ReminderPopup(ReminderInstance inst) {

		super();

		reminderInstance = inst;

		this.setTitle("Borg " + Resource.getResourceString("Reminder"));

		initialize();

		// set appt info in the reminder
		String apptinfoText = "";
		if (!AppointmentModel.isNote(reminderInstance.getAppt())) {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
					DateFormat.SHORT);
			apptinfoText = df.format(inst.getInstanceTime());
		} else {
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
			apptinfoText = df.format(inst.getInstanceTime());
		}

		if (reminderInstance.getAppt().isEncrypted())
			apptinfoText += " "
					+ Resource.getResourceString("EncryptedItemShort");
		else
			apptinfoText += " " + reminderInstance.getAppt().getText();
		appointmentInformation.setText(apptinfoText);

		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

	}

	/*
	 * (non-Javadoc)
	 * 
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
		appointmentInformation
				.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		topPanel.add(appointmentInformation, GridBagConstraintsFactory.create(
				0, 0, GridBagConstraints.NONE, 1.0, 0.0));

		timeToGoMessage = new JLabel();
		timeToGoMessage.setText("");
		timeToGoMessage
				.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

		topPanel.add(timeToGoMessage, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.NONE, 1.0, 0.0));
		noMoreRemindersButton = new JRadioButton();
		ResourceHelper.setText(noMoreRemindersButton, "No_more");
		noMoreRemindersButton
				.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		topPanel.add(noMoreRemindersButton, GridBagConstraintsFactory.create(0,
				2, GridBagConstraints.NONE, 1.0, 0.0));

		//
		// button panel
		//
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		JButton dismissButton = new JButton();
		ResourceHelper.setText(dismissButton, "Dismiss");
		dismissButton.setActionCommand("close_it");
		dismissButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (noMoreRemindersButton.isSelected()) {
					reminderInstance.setHidden(true);
					dispose();
				} else {
					setVisible(false);
				}
			}
		});
		buttonPanel.add(dismissButton, GridBagConstraintsFactory.create(0, 0));

		if (reminderInstance.getAppt().getTodo() == true) {

			JButton doneButton = new JButton();
			ResourceHelper.setText(doneButton, "Done_(Delete)");
			doneButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						AppointmentModel.getReference().do_todo(
								reminderInstance.getAppt().getKey(), true);
					} catch (Exception e1) {
						Errmsg.errmsg(e1);
					}
					destroy();
				}
			});
			buttonPanel.add(doneButton, GridBagConstraintsFactory.create(1, 0));

			JButton doneNoDeleteButton = new JButton();
			ResourceHelper.setText(doneNoDeleteButton, "Done_(No_Delete)");
			doneNoDeleteButton
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent e) {
							try {
								AppointmentModel.getReference().do_todo(
										reminderInstance.getAppt().getKey(),
										false);
							} catch (Exception e1) {
								Errmsg.errmsg(e1);
							}
							destroy();
						}
					});
			buttonPanel.add(doneNoDeleteButton, GridBagConstraintsFactory
					.create(2, 0));
		}

		topPanel.add(buttonPanel, GridBagConstraintsFactory.create(0, 3));

		this.setContentPane(topPanel);
		this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		this.setSize(400, 250);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.View#refresh()
	 */
	@Override
	public void refresh() {
		// empty
	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	public ReminderInstance getReminderInstance() {
		return reminderInstance;
	}

	/**
	 * update the popup's message
	 */
	public void updateMessage() {
		// read the appt and get the date
		Appointment appt = reminderInstance.getAppt();

		String message;

		// untimed todo
		if (AppointmentModel.isNote(appt) && appt.getTodo()) {
			message = Resource.getResourceString("To_Do");
		} else {
			// timed appt
			Date d = reminderInstance.getInstanceTime();
			if (d == null)
				return;

			// if alarm is due to be shown, show it and play sound

			int reminderIndex = reminderInstance.dueForPopup();
			if( reminderIndex == -1 )
				return;
			
			int minutesToGo = ReminderTimes.getTimes(reminderIndex);
			
			// create a message saying how much time to go there is
			if (minutesToGo < 0) {
				message = -minutesToGo + " "
						+ Resource.getResourceString("minutes_ago");
			} else if (minutesToGo == 0) {
				message = Resource.getResourceString("Now");
			} else {
				message = minutesToGo + " "
						+ Resource.getResourceString("minute_reminder");
			}
			
			getReminderInstance().markAsShown(reminderIndex);

		}
		
		timeToGoMessage.setText(message);
		
		setVisible(true);
		toFront();
		setVisible(true);
		
		getReminderInstance().setShown(true);
		
		// play a sound
		ReminderSound.playReminderSound(Prefs
				.getPref(PrefName.BEEPINGREMINDERS));
	}

}
