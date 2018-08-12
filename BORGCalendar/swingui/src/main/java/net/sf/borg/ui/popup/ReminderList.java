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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.View;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.TableSorter;

/**
 * ReminderList is a UI to show the list of reminders managed by the
 * ReminderListManager. It shows all reminder messages in a single table.
 * 
 * @author mbb
 * 
 */
public class ReminderList extends View {

	private static final long serialVersionUID = 1L;

	private static final PrefName REMINDERLISTSIZE = new PrefName("reminderlistsize",
	"-1,-1,-1,-1,N");


	private JPanel buttonPanel = new JPanel();

	private JTable reminderTable = new JTable();
	
	// table columns
	static private final int SELECT_COLUMN = 0;
	static private final int TEXT_COLUMN = 1;
	static private final int TOGO_MESSAGE_COLUMN = 2;
	static private final int REMINDER_INSTANCE_COLUMN = 3;
	static private final int TOGO_MINUTES_COLUMN = 4;
	
	/**
	 * renderer to add the todo marker to the reminder text in the table
	 *
	 */
	static private class MyTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
	    		boolean isSelected, boolean hasFocus, int row, int column) {

	    	JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			label.setIcon(null);

	    	if(column == TEXT_COLUMN){
	    			    		
	    		ReminderInstance inst = (ReminderInstance)value;
	    		
	    		// get appt text
				String tx = DateFormat.getDateInstance(DateFormat.SHORT).format(
						inst.getInstanceTime());

				tx += " " + inst.getText();
				
				label.setText(tx);

	    		// add todo icon if needed
				if( inst.isTodo())
				{
					String iconname = Prefs.getPref(PrefName.UCS_MARKER);
					String use_marker = Prefs.getPref(PrefName.UCS_MARKTODO);
					if (use_marker.equals("true")) {
						if (iconname.endsWith(".gif") || iconname.endsWith(".jpg")) {
							Icon todoIcon = new javax.swing.ImageIcon(getClass().getResource(
									"/resource/" + iconname));
							label.setIcon(todoIcon);
							
						} else {
							label.setText(iconname + " " + tx);
						}
					}
				}
				
	    	}
	    	return label;
	    }
	}

	public ReminderList() {
		super();

		initComponents();

		this.setTitle("Borg " + Resource.getResourceString("Reminder"));

		// inst is the ReminderInstance in a hidden column
		// mins is the minutes-to-go of the reminder - for sorting. hidden
		// column.
		reminderTable.setModel(new TableSorter(new String[] { "",
				Resource.getResourceString("Reminder"),
				Resource.getResourceString("Due"), "inst", "mins" },
				new Class[] { Boolean.class, String.class, String.class,
						ReminderInstance.class, Long.class }, 
						new boolean[]{ true, false, false, false, false}));

		// hide inst and mins columns
		reminderTable.removeColumn(reminderTable.getColumnModel().getColumn(3));
		reminderTable.removeColumn(reminderTable.getColumnModel().getColumn(3));
		
		reminderTable.getColumnModel().getColumn(ReminderList.SELECT_COLUMN).setMaxWidth(30);
		reminderTable.getColumnModel().getColumn(ReminderList.SELECT_COLUMN).setMinWidth(20);
		
		reminderTable.setDefaultRenderer(String.class, new MyTableCellRenderer());
		reminderTable.getTableHeader().setReorderingAllowed(false);
		pack();

		manageMySize(REMINDERLISTSIZE);
	}

	@Override
	public void destroy() {
		this.dispose();
	}

	/**
	 * Gets the selected ReminderInstances.
	 * 
	 * @return a List of selected reminder instances
	 */
	private List<ReminderInstance> getSelectedReminders() {
	
		List<ReminderInstance> list = new ArrayList<ReminderInstance>();
		
		TableSorter tm = (TableSorter) reminderTable.getModel();

		for( int row = 0; row < tm.getRowCount(); row++ )
		{
			Boolean selected = (Boolean)tm.getValueAt(row, ReminderList.SELECT_COLUMN);
			if( selected.booleanValue() == true)
			{
				list.add((ReminderInstance)tm.getValueAt(row, ReminderList.REMINDER_INSTANCE_COLUMN));
			}
				
		}
		
		return list;
	
	}

	private void initComponents() {

		this.getContentPane().setLayout(new GridBagLayout());
		reminderTable = new JTable();
		buttonPanel = new JPanel();

		reminderTable.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0,
				0, 0)));
		
		reminderTable.setRowSelectionAllowed(false);

		
		JScrollPane jScrollPane1 = new JScrollPane();
		jScrollPane1.setPreferredSize(new java.awt.Dimension(554, 404));
		jScrollPane1.setViewportView(reminderTable);

		JButton hideButton = new JButton();
		
		hideButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		hideButton.setText(Resource.getResourceString("Hide"));

		hideButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				for( ReminderInstance inst : getSelectedReminders())
				{
					inst.setHidden(true);
				}
				refresh(true);
			}
		});

		buttonPanel.add(hideButton);

		JButton doneButton = new JButton();

		doneButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		ResourceHelper.setText(doneButton, "Done_(Delete)");
		doneButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				for( ReminderInstance inst : getSelectedReminders())
				{
					if( inst.isTodo())
					{
						inst.do_todo(true);
					}
				}			
			}
		});

		buttonPanel.add(doneButton);

		JButton donendButton = new JButton();

		donendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		ResourceHelper.setText(donendButton, "Done_(No_Delete)");
		donendButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				for( ReminderInstance inst : getSelectedReminders())
				{
					if( inst.isTodo())
					{
						inst.do_todo(false);
					}
				}	
			}
		});

		buttonPanel.add(donendButton);

		JButton resetButton = new JButton();
		resetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		ResourceHelper.setText(resetButton, "Unhide_All");
		resetButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					ReminderListManager m = (ReminderListManager) ReminderListManager
							.getReference();
					List<ReminderInstance> list = m.getReminders();
					for (ReminderInstance inst : list)
						inst.setHidden(false);
					refresh(true);
					ReminderManager.getReminderManager().checkModelsForReminders();
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
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
	public void refresh() {
		refresh(false);
	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * reload the UI from the reminders held by the ReminderListManager
	 */
	public void refresh(boolean silent) {

		// get the list of reminders managed by the ReminderListManager
		ReminderListManager m = (ReminderListManager) ReminderListManager
				.getReference();
		List<ReminderInstance> list = m.getReminders();

		// table will be sorted by the hidden column - minutes to go
		// table will not be user sortable
		TableSorter tm = (TableSorter) reminderTable.getModel();
		tm.sortByColumn(ReminderList.TOGO_MINUTES_COLUMN);
		tm.setRowCount(0);

		// clear selection
		reminderTable.clearSelection();

		// add all reminders
		for (ReminderInstance inst : list) {

			// skip hidden reminders. These have been previously hidden by the
			// user
			// and won't come back unless the user resets them
			if (inst.isHidden())
				continue;

			String message = inst.calculateToGoMessage();
			if (message == null)
				continue;

			// set the shown flag for this reminder - meaning that it was shown
			// for any of its times
			inst.setShown(true);

			// build a table row
			Object[] row = new Object[5];

			
			row[ReminderList.SELECT_COLUMN] = Boolean.FALSE; 
			
			row[ReminderList.TEXT_COLUMN] = inst;

			row[ReminderList.TOGO_MESSAGE_COLUMN] = message;

			row[ReminderList.REMINDER_INSTANCE_COLUMN] = inst;

			if (inst.isNote() && inst.isTodo())
				row[ReminderList.TOGO_MINUTES_COLUMN] = Long.valueOf(inst.getInstanceTime().getTime()); // sort todos last
			else
				row[ReminderList.TOGO_MINUTES_COLUMN] = Long.valueOf(inst.minutesToGo());
			
			

			tm.addRow(row);
			tm.tableChanged(new TableModelEvent(tm));

		}

		if (!silent) {
			this.setVisible(true);
			this.toFront();
			ReminderSound.playReminderSound(Prefs
					.getPref(PrefName.BEEPINGREMINDERS));
		}
	}

	/**
	 * just update the reminder times
	 */
	public void updateTimes() {

		int selected = reminderTable.getSelectedRow();

		TableSorter tm = (TableSorter) reminderTable.getModel();

		for (int index = 0; index < tm.getRowCount(); index++) {
			Object o = tm.getValueAt(index, ReminderList.REMINDER_INSTANCE_COLUMN);
			if (o != null && o instanceof ReminderInstance) {

				ReminderInstance inst = (ReminderInstance) o;
				String message = inst.calculateToGoMessage();
				if (message != null) {
					tm.setValueAt(message, index, ReminderList.TOGO_MESSAGE_COLUMN);
				}
			}
		}

		if (selected != -1)
			reminderTable.getSelectionModel().setSelectionInterval(selected, selected);

	}

	
}
