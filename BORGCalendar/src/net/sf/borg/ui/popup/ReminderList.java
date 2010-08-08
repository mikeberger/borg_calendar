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

 Copyright 2010 by Mike Berger
 */
package net.sf.borg.ui.popup;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.View;
import net.sf.borg.ui.calendar.AppointmentTextFormat;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.TableSorter;

/**
 * ReminderList is a UI to show the list of reminders managed by the ReminderListManager. It shows all reminder
 * messages in a single table.
 * @author mbb
 *
 */
public class ReminderList extends View {

	private static final long serialVersionUID = 1L;

	private JPanel buttonPanel = new JPanel();

	private JButton hideButton = new JButton();

	private JButton doneButton = new JButton();

	private JButton donendButton = new JButton();

	private JTable table = new JTable();

	public ReminderList() {
		super();
		
		initComponents();

		this.setTitle("Borg " + Resource.getResourceString("Reminder"));

		// inst is the ReminderInstance in a hidden column
		// mins is the minutes-to-go of the reminder - for sorting. hidden column.
		table.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Reminder"),
				Resource.getResourceString("Due"), "inst", "mins" },
				new Class[] { String.class, String.class,
						ReminderInstance.class, Integer.class }));

		// hide inst and mins columns
		table.removeColumn(table.getColumnModel().getColumn(2));
		table.removeColumn(table.getColumnModel().getColumn(2));

		pack();

		manageMySize(PrefName.REMINDERLISTSIZE);
	}

	@Override
	public void destroy() {
		this.dispose();
	}

	/**
	 * Gets the selected ReminderInstance.
	 * 
	 * @return the selected reminder instance
	 */
	private ReminderInstance getSelectedReminder() {
		int index = table.getSelectedRow();
		if (index == -1)
			return null;
		try {

			TableSorter tm = (TableSorter) table.getModel();
			
			// column 2 holds the reminder instance
			Object o = tm.getValueAt(index, 2);
			if (o != null && o instanceof ReminderInstance) {
				return (ReminderInstance) o;
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		return null;
	}

	private void initComponents() {

		this.getContentPane().setLayout(new GridBagLayout());
		table = new JTable();
		buttonPanel = new JPanel();

		table.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0,
				0, 0)));

		DefaultListSelectionModel mylsmodel = new DefaultListSelectionModel();
		mylsmodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionModel(mylsmodel);
		table.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			/**
			 * on mouse click, enable/disable the buttons based on what is selected - i.e. if it is a todo
			 */
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				ReminderInstance inst = getSelectedReminder();
				if (inst != null) {
					if (inst.getAppt().getTodo()) {
						doneButton.setEnabled(true);
						donendButton.setEnabled(true);
						hideButton.setEnabled(true);
					} else {
						doneButton.setEnabled(false);
						donendButton.setEnabled(false);
						hideButton.setEnabled(true);
					}
				} else {
					doneButton.setEnabled(false);
					donendButton.setEnabled(false);
					hideButton.setEnabled(false);
				}
			}
		});

		JScrollPane jScrollPane1 = new JScrollPane();
		jScrollPane1.setPreferredSize(new java.awt.Dimension(554, 404));
		jScrollPane1.setViewportView(table);

		hideButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		hideButton.setText(Resource.getResourceString("Hide"));

		hideButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ReminderInstance inst = getSelectedReminder();
				inst.setHidden(true);
				refresh();
			}
		});

		buttonPanel.add(hideButton);

		doneButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		ResourceHelper.setText(doneButton, "Done_(Delete)");
		doneButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ReminderInstance inst = getSelectedReminder();
				try {
					AppointmentModel.getReference().do_todo(
							inst.getAppt().getKey(), true);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		buttonPanel.add(doneButton);

		donendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		ResourceHelper.setText(donendButton, "Done_(No_Delete)");
		donendButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ReminderInstance inst = getSelectedReminder();
				try {
					AppointmentModel.getReference().do_todo(
							inst.getAppt().getKey(), false);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		buttonPanel.add(donendButton);

		JButton resetButton = new JButton();
		resetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		ResourceHelper.setText(resetButton, "Reset");
		resetButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					ReminderListManager m = (ReminderListManager) ReminderListManager
							.getReference();
					List<ReminderInstance> list = m.getReminders();
					for (ReminderInstance inst : list)
						inst.setHidden(false);
					refresh();
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		buttonPanel.add(resetButton);

		this.getContentPane().add(
				jScrollPane1,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH,
						1.0, 1.0));

		this.getContentPane().add(buttonPanel,
				GridBagConstraintsFactory.create(0, 2));

	}

	@Override
	/**
	 * reload the UI from the reminders held by the ReminderListManager
	 */
	public void refresh() {

		// get the list of reminders managed by the ReminderListManager
		ReminderListManager m = (ReminderListManager) ReminderListManager
				.getReference();
		List<ReminderInstance> list = m.getReminders();
		
		// table will be sorted by the hidden column - minutes to go
		// table will not be user sortable
		TableSorter tm = (TableSorter) table.getModel();
		tm.sortByColumn(3);
		tm.setRowCount(0);

		// clear selection and disable all buttons
		table.clearSelection();
		doneButton.setEnabled(false);
		donendButton.setEnabled(false);
		hideButton.setEnabled(false);

		// add all reminders
		for (ReminderInstance inst : list) {

			String message;
			Appointment appt = inst.getAppt();

			// skip hidden reminders. These have been previously hidden by the user
			// and won't come back unless the user resets them
			if (inst.isHidden())
				continue;

			int minutesToGo = 9999999; // time to go of the reminder - init to large value to sort todo's last
			
			// set the reminder due message
			if (AppointmentModel.isNote(appt) && appt.getTodo()) {
				message = Resource.getResourceString("To_Do");
			} else {
				// timed appt
				Date d = inst.getInstanceTime();
				if (d == null)
					continue;

				// get the reminder time and also mark the reminder as shown since w are adding it to the UI
				// it may already have been marked as shown - no problem
				int reminderIndex = inst.getCurrentReminder();
				if (reminderIndex == -1)
					continue;
				inst.markAsShown(reminderIndex);

				// map the reminder index to the user-tunable reminder minutes value
				minutesToGo = ReminderTimes.getTimes(reminderIndex);

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

			}

			// set the shown flag for this reminder - meaning that it was shown for any of its times
			inst.setShown(true);

			// build a table row
			Object[] row = new Object[4];
			
			// date is the next todo field if present, otherwise
			// the due date
			Date nt = appt.getNextTodo();
			if (nt == null) {
				nt = appt.getDate();
			}

			// get appt text
			String tx = DateFormat.getDateInstance(DateFormat.SHORT).format(nt);
				
		    tx += " " + AppointmentTextFormat.format(appt, nt);

			row[0] = tx;

			row[1] = message;

			row[2] = inst;

			row[3] = new Integer(minutesToGo);

			tm.addRow(row);
			tm.tableChanged(new TableModelEvent(tm));

		}

		this.setVisible(true);
		this.toFront();

		ReminderSound.playReminderSound(Prefs
				.getPref(PrefName.BEEPINGREMINDERS));
	}
}
