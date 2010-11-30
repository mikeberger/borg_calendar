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

package net.sf.borg.ui.calendar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.control.EmailReminder;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.util.DateDialog;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TableSorter;

import com.toedter.calendar.JDateChooser;

/**
 * AppointmentListView provides a UI for editing the appoitnments for a day. The
 * UI presents a list of appointments and allows the user to edit the selected
 * appointment in an appointment editor.
 */
public class AppointmentListView extends DockableView implements
		ListSelectionListener {

	private static final long serialVersionUID = 1L;

	/**
	 * show the time of an appointment in the appt table - and shopw dashes for
	 * appointments with no time
	 */
	private class TimeRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		/**
		 * constructor
		 */
		public TimeRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		/**
		 * render the cell
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			Component c = defrend.getTableCellRendererComponent(table, obj,
					isSelected, hasFocus, row, column);
			this.setBackground(c.getBackground());
			this.setForeground(c.getForeground());

			Date d = (Date) obj;
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(d);
			// seconds field is set to zero only for untimed appts
			if (cal.get(Calendar.SECOND) == 0) {
				this.setText("--------");
			} else {
				SimpleDateFormat sdf = AppointmentModel.getTimeFormat();
				this.setText(sdf.format(d));
			}
			return this;
		}
	}

	/**
	 * Prompt the user to change the date of a set of appointments
	 * 
	 * @param appts
	 *            the appts
	 */
	static void onChangeDate(List<Appointment> appts) {
		if (appts.size() == 0)
			return;

		DateDialog dlg = new DateDialog(null);
		Calendar cal = new GregorianCalendar();
		cal.setTime(appts.get(0).getDate());
		dlg.setCalendar(cal);
		dlg.setVisible(true);
		Calendar dlgcal = dlg.getCalendar();
		if (dlgcal == null)
			return;

		cal.set(dlgcal.get(Calendar.YEAR), dlgcal.get(Calendar.MONTH), dlgcal
				.get(Calendar.DAY_OF_MONTH));

		for (int i = 0; i < appts.size(); ++i) {
			try {
				Appointment appt = appts.get(i);
				appt.setDate(cal.getTime());
				AppointmentModel.getReference().saveAppt(appt);
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

	}

	/**
	 * Move a set of appointments to the following day
	 * 
	 * @param appts
	 *            the appts
	 */
	static void onMoveToFollowingDay(List<Appointment> appts) {
		if (appts.size() == 0)
			return;

		for (int i = 0; i < appts.size(); ++i) {
			try {
				Appointment appt = appts.get(i);

				// Increment the day
				GregorianCalendar gcal = new GregorianCalendar();
				gcal.setTime(appt.getDate());
				gcal.add(Calendar.DAY_OF_YEAR, 1);
				appt.setDate(gcal.getTime());
				AppointmentModel.getReference().saveAppt(appt);

			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

	}

	private JButton addButton;
	private AppointmentPanel appointmentPanel = null;
	private StripedTable apptTable;
	private GregorianCalendar cal_ = null; // date of the appt list
	private JDateChooser dateChooser = null;
	private JButton copyButton = null;
	private TableCellRenderer defrend = null; // default table cell renderer
	private JButton deleteButton;
	private JButton deleteOneOnlyButton;
	private JPanel buttonPanel;
	private JPanel appointmentListPanel;
	private JScrollPane apptTableScrollPane;
	private JButton reminderButton = null;
	private String title_ = ""; // tab/window title

	/**
	 * constructor
	 * 
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param day
	 *            the day
	 */
	public AppointmentListView(int year, int month, int day) {
		super();

		addModel(AppointmentModel.getReference());
		addModel(TaskModel.getReference());

		appointmentPanel = new AppointmentPanel(year, month, day);

		initComponents();

		title_ = Resource.getResourceString("Appointment_Editor");

		// add scroll to the table
		apptTableScrollPane.setViewportView(apptTable);

		// use a sorted table model
		apptTable.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Text"),
				Resource.getResourceString("Time"), "Key" }, new Class[] {
				java.lang.String.class, java.util.Date.class, Integer.class }));

		// set renderer to the custom one for time
		defrend = apptTable.getDefaultRenderer(Date.class);
		apptTable.setDefaultRenderer(java.util.Date.class,
				new AppointmentListView.TimeRenderer());

		// set up for sorting when a column header is clicked
		TableSorter tm = (TableSorter) apptTable.getModel();
		tm.addMouseListenerToHeaderInTable(apptTable);

		// set column widths
		apptTable.getColumnModel().getColumn(0).setPreferredWidth(125);
		apptTable.getColumnModel().getColumn(1).setPreferredWidth(75);

		apptTable.setPreferredScrollableViewportSize(new Dimension(150, 100));

		ListSelectionModel rowSM = apptTable.getSelectionModel();
		rowSM.addListSelectionListener(this);

		TableColumnModel tcm = apptTable.getColumnModel();
		TableColumn column = tcm.getColumn(2);
		tcm.removeColumn(column);

		showDate(year, month, day);

	}

	/**
	 * Edit a copy of the selected appointment
	 */
	private void copyAppt() {
		int[] keys = getSelectedKeys();
		if (keys.length != 1) {
			Errmsg.notice(Resource.getResourceString("select_appt"));
			return;
		}

		// get date for new appt
		DateDialog dlg = new DateDialog(null);
		Calendar cal = new GregorianCalendar();
		cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));
		dlg.setCalendar(cal);
		dlg.setVisible(true);
		Calendar dlgcal = dlg.getCalendar();
		if (dlgcal == null)
			return;

		showDate(dlgcal.get(Calendar.YEAR), dlgcal.get(Calendar.MONTH), dlgcal
				.get(Calendar.DATE));
		Appointment appt;
		try {
			appt = AppointmentModel.getReference().getAppt(keys[0]);
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
		appointmentPanel.showapp(-1, appt);
		refresh();

	}

	/**
	 * Gets the copy button.
	 * 
	 * @return the copy button
	 */
	private JButton getCopyButton() {
		if (copyButton == null) {
			copyButton = new JButton();
			ResourceHelper.setText(copyButton, "copy_appt");
			copyButton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Copy16.gif")));
			copyButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					copyAppt();
				}
			});
		}
		return copyButton;
	}

	/**
	 * Gets the date chooser
	 * 
	 * @return the date chooser
	 */
	private JDateChooser getDateCB() {
		if (dateChooser == null) {
			dateChooser = new JDateChooser();
			// cb.setCalendar(cal_);
			dateChooser.addPropertyChangeListener("date",
					new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent arg0) {
							Calendar cal = dateChooser.getCalendar();
							if (cal != null) {
								showDate(cal.get(Calendar.YEAR), cal
										.get(Calendar.MONTH), cal
										.get(Calendar.DATE));
							} else
								dateChooser.setCalendar(cal_);

						}
					});

		}

		return (dateChooser);
	}

	@Override
	public String getFrameTitle() {
		return title_;
	}

	/**
	 * get the reminder button
	 * 
	 * @return the reminder button
	 */
	private JButton getReminderButton() {
		if (reminderButton == null) {
			reminderButton = new JButton();
			ResourceHelper.setText(reminderButton, "send_reminder");
			reminderButton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/ComposeMail16.gif")));
			reminderButton
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							class MailThread extends Thread {
								private Calendar cal;

								public MailThread(Calendar c) {
									cal = c;
									this.setName("Reminder Mail Thread");
								}

								@Override
								public void run() {

									try {
										EmailReminder
												.sendDailyEmailReminder(cal);
									} catch (Exception e) {
										final Exception fe = e;
										SwingUtilities
												.invokeLater(new Runnable() {
													@Override
													public void run() {
														Errmsg.errmsg(fe);
													}
												});
									}
								}
							}

							new MailThread(cal_).start();

						}
					});
		}
		return reminderButton;
	}

	/**
	 * Gets the selected appointments.
	 * 
	 * @return the selected appointments
	 */
	private List<Appointment> getSelectedAppointments() {
		int[] keys = getSelectedKeys();
		List<Appointment> appts = new ArrayList<Appointment>(keys.length);
		AppointmentModel model = AppointmentModel.getReference();
		for (int i = 0; i < keys.length; ++i) {
			try {
				appts.add(model.getAppt(keys[i]));
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}
		return appts;
	}

	/**
	 * Gets the selected appt keys.
	 * 
	 * @return the selected appt keys
	 */
	private int[] getSelectedKeys() {
		int[] rows = apptTable.getSelectedRows();
		List<Integer> keyList = new ArrayList<Integer>();
		for (int i = 0; i < rows.length; ++i) {
			int row = rows[i];
			TableSorter tm = (TableSorter) apptTable.getModel();
			keyList.add((Integer) tm.getValueAt(row, 2));
		}

		int[] keys = new int[keyList.size()];
		for (int i = 0; i < keyList.size(); ++i)
			keys[i] = keyList.get(i).intValue();

		return keys;
	}

	/**
	 * init the ui components
	 */
	private void initComponents()// GEN-BEGIN:initComponents
	{

		appointmentListPanel = new JPanel();
		apptTableScrollPane = new JScrollPane();
		apptTable = new StripedTable();
		buttonPanel = new JPanel();
		addButton = new JButton();
		deleteButton = new JButton();
		deleteOneOnlyButton = new JButton();

		appointmentListPanel.setLayout(new java.awt.GridBagLayout());

		appointmentListPanel.setBorder(new TitledBorder(Resource
				.getResourceString("apptlist")));

		apptTable.setBorder(new LineBorder(new java.awt.Color(0, 0, 0)));
		apptTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		apptTable
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		apptTable.setPreferredSize(new java.awt.Dimension(700, 500));

		apptTableScrollPane.setBorder(null);
		apptTableScrollPane.setViewport(apptTableScrollPane.getViewport());
		apptTableScrollPane.setViewportView(apptTable);

		buttonPanel.setLayout(new java.awt.GridLayout(0, 1));

		addButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Edit16.gif")));
		ResourceHelper.setText(addButton, "EditNew");
		addButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				apptTable.clearSelection();
				appointmentPanel.showapp(-1, null);
			}
		});

		buttonPanel.add(addButton);
		buttonPanel.add(getCopyButton());

		deleteButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		ResourceHelper.setText(deleteButton, "Delete");
		deleteButton.setToolTipText(Resource.getResourceString("del_tip"));

		ActionListener alDel = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				int[] keys = getSelectedKeys();
				AppointmentModel model = AppointmentModel.getReference();
				for (int i = 0; i < keys.length; ++i)
					model.delAppt(keys[i]);
				apptTable.clearSelection();
			}
		};
		deleteButton.addActionListener(alDel);
		buttonPanel.add(deleteButton);

		deleteOneOnlyButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		ResourceHelper.setText(deleteOneOnlyButton, "Delete_One_Only");
		deleteOneOnlyButton.setToolTipText(Resource
				.getResourceString("doo_tip"));
		ActionListener alDelOne = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				int[] keys = getSelectedKeys();
				AppointmentModel model = AppointmentModel.getReference();
				for (int i = 0; i < keys.length; ++i)
					model.delOneOnly(keys[i], cal_.getTime());
				apptTable.clearSelection();
			}
		};
		deleteOneOnlyButton.addActionListener(alDelOne);

		ActionListener alMoveToFollowingDay = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				onMoveToFollowingDay(getSelectedAppointments());
			}
		};

		ActionListener alChangeDate = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				onChangeDate(getSelectedAppointments());
			}
		};

		new PopupMenuHelper(apptTable, new PopupMenuHelper.Entry[] {
				new PopupMenuHelper.Entry(alDel, "Delete"),
				new PopupMenuHelper.Entry(alDelOne, "Delete_One_Only"),
				new PopupMenuHelper.Entry(alMoveToFollowingDay,
						"Move_To_Following_Day"),
				new PopupMenuHelper.Entry(alChangeDate, "changedate"), });

		buttonPanel.add(getReminderButton(), null);
		buttonPanel.add(deleteOneOnlyButton, deleteOneOnlyButton.getName());

		appointmentListPanel.add(getDateCB(), GridBagConstraintsFactory.create(
				0, 0, GridBagConstraints.BOTH, 1.0, 0.0));
		appointmentListPanel.add(apptTableScrollPane, GridBagConstraintsFactory
				.create(0, 1, GridBagConstraints.BOTH, 1.0, 10.0));
		appointmentListPanel.add(buttonPanel, GridBagConstraintsFactory.create(
				0, 2));

		setLayout(new GridBagLayout());
		
		add(appointmentPanel, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));
		add(appointmentListPanel, GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH));

	}

	
	@Override
	/**
	 * reload the UI from the model
	 */
	public void refresh() {

		TableSorter tm = (TableSorter) apptTable.getModel();

		// clear all table rows
		tm.setRowCount(0);
		tm.tableChanged(new TableModelEvent(tm));

		String priv = Prefs.getPref(PrefName.SHOWPRIVATE);
		String pub = Prefs.getPref(PrefName.SHOWPUBLIC);

		try {

			List<Integer> alist_ = AppointmentModel.getReference().getAppts(
					cal_.getTime());
			if (alist_ != null) {
				Iterator<Integer> it = alist_.iterator();
				while (it.hasNext()) {
					Integer key = it.next();
					Appointment ap = AppointmentModel.getReference().getAppt(
							key.intValue());

					if (ap.getPrivate() && !priv.equals("true"))
						continue;
					if (!ap.getPrivate() && !pub.equals("true"))
						continue;

					Object[] ro = new Object[3];
					
					if( ap.isEncrypted())
						ro[0] = Resource.getResourceString("EncryptedItemShort");
					else
						ro[0] = ap.getText();

					// just get time
					Date d = ap.getDate();
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(d);
					cal.set(2000, 1, 1);

					// use second field to tell UI if the appt is untimed
					if (AppointmentModel.isNote(ap))
						cal.set(Calendar.SECOND, 0);
					else
						cal.set(Calendar.SECOND, 1);

					ro[1] = cal.getTime();
					ro[2] = key;

					tm.addRow(ro);
					tm.tableChanged(new TableModelEvent(tm));
				}
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// resize the table based on new row count
		int row = apptTable.getRowCount();
		apptTable.setPreferredSize(new Dimension(150, row * 16));

		// apply default sort to the table
		if (!tm.isSorted())
			tm.sortByColumn(1);
		else
			tm.sort();
	}

	/**
	 * Show an appointment in the list and editor
	 * 
	 * @param key
	 *            the appt key
	 */
	public void showApp(int key) {
		apptTable.clearSelection();
		TableSorter tm = (TableSorter) apptTable.getModel();
		int rows = tm.getRowCount();
		for (int i = 0; i < rows; ++i) {
			Integer k = (Integer) tm.getValueAt(i, 2);
			if (key == k.intValue()) {
				apptTable.getSelectionModel().setSelectionInterval(i, i);
			}
		}
		appointmentPanel.showapp(key, null);
	}

	/**
	 * set the UI to show appts for a particular date.
	 * 
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param day
	 *            the day
	 */
	private void showDate(int year, int month, int day) {
		cal_ = new GregorianCalendar(year, month, day);
		getDateCB().setCalendar(cal_);
		Date d = cal_.getTime();
		title_ = Resource.getResourceString("Appointment_Editor_for_") + " "
				+ DateFormat.getDateInstance(DateFormat.SHORT).format(d);

		// clear all rows
		TableSorter tm = (TableSorter) apptTable.getModel();
		tm.setRowCount(0);
		tm.tableChanged(new TableModelEvent(tm));
		appointmentPanel.setDate(year, month, day);
		appointmentPanel.showapp(-1, null);

		refresh();
	}

	/**
	 * react to ListSelectionEvents
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// Ignore extra messages.
		if (e.getValueIsAdjusting())
			return;

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		if (lsm.isSelectionEmpty()) {
			appointmentPanel.showapp(-1, null);
			return;
		}
		int row = lsm.getMinSelectionIndex();
		if (row == -1)
			return;
		TableSorter tm = (TableSorter) apptTable.getModel();
		Integer apptkey = (Integer) tm.getValueAt(row, 2);

		appointmentPanel.showapp(apptkey.intValue(), null);

	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	@Override
	public JMenuBar getMenuForFrame() {
		return null;
	}
}
