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

package net.sf.borg.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
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
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.ui.StripedTable;
import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.control.EmailReminder;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;

import com.toedter.calendar.JDateChooser;

public class AppointmentListView extends View implements ListSelectionListener {

	private class TimeRenderer extends JLabel implements TableCellRenderer {

		public TimeRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

		        Component c = defrend.getTableCellRendererComponent(table, obj, 
		        	isSelected, hasFocus, row, column);
			// default to white background unless row is selected
			this.setBackground(c.getBackground());
			this.setForeground(c.getForeground());
			
			Date d = (Date) obj;
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(d);
			if (cal.get(Calendar.HOUR_OF_DAY) == 0) {
				this.setText("--------");
			} else {
				SimpleDateFormat sdf = AppointmentModel.getTimeFormat();
				this.setText(sdf.format(d));
			}
			return this;
		}
	}

	static void changeDate(Appointment appt, Calendar cal) {
		// The appointment needs a new key - delete the old ToDo
		// and save a new one with an updated key.
		AppointmentModel model = AppointmentModel.getReference();
		model.delAppt(appt);
		int newkey = AppointmentModel.dkey(cal);
		appt.setDate(cal.getTime());
		appt.setKey(newkey);
		model.saveAppt(appt, true);
		// save it
	}

	static void onChangeDate(Frame parent, List appts) {
		// Move all selected ToDos to following day
		if (appts.size() == 0)
			return;

		AppointmentModel model = AppointmentModel.getReference();
		DateDialog dlg = new DateDialog(parent);
		Calendar cal = new GregorianCalendar();
		cal.setTime(((Appointment) appts.get(0)).getDate());
		dlg.setCalendar(cal);
		dlg.setVisible(true);
		Calendar dlgcal = dlg.getCalendar();
		if (dlgcal == null)
			return;

		cal.set(dlgcal.get(Calendar.YEAR), dlgcal.get(Calendar.MONTH), dlgcal
				.get(Calendar.DAY_OF_MONTH));

		for (int i = 0; i < appts.size(); ++i) {
			try {
				Appointment appt = (Appointment) appts.get(i);
				changeDate(appt, cal);
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

		model.refresh();
	}

	// package //
	// Static helper methods for moving appointments - used by both
	// TodoView and AppointmentListView
	static void onMoveToFollowingDay(Frame parent, List appts) {
		// Move all selected ToDos to following day
		if (appts.size() == 0)
			return;

		AppointmentModel model = AppointmentModel.getReference();
		for (int i = 0; i < appts.size(); ++i) {
			try {
				Appointment appt = (Appointment) appts.get(i);

				// Increment the day
				GregorianCalendar gcal = new GregorianCalendar();
				gcal.setTime(appt.getDate());
				gcal.add(Calendar.DAY_OF_YEAR, 1);
				changeDate(appt, gcal);

			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

		model.refresh();
	}
	
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton add;

	private List alist_ = null;

	private AppointmentPanel apanel_ = null;

	private StripedTable apptTable;

	private GregorianCalendar cal_ = null;

	/**
	 * This method initializes jPanel4
	 * 
	 * @return javax.swing.JPanel
	 */
	private JDateChooser cb_ = null;

	private TableCellRenderer defrend = null;

	private javax.swing.JButton del;

	private javax.swing.JButton delone;

	private javax.swing.JButton dismiss;

	private javax.swing.JMenuItem exitMenuItem;

	private javax.swing.JMenu fileMenu;

	private JPanel jPanel = null;

	private javax.swing.JPanel jPanel1;

	private javax.swing.JPanel jPanel2;

	private javax.swing.JPanel jPanel3;

	private javax.swing.JScrollPane jScrollPane1;

	private int key_;

	private javax.swing.JMenuBar menuBar;

	private JButton mtgMailButton = null;

	private JButton reminderButton = null; // @jve:decl-index=0:visual-constraint="702,73"

	/** Creates new form btgui */
	public AppointmentListView(int year, int month, int day) {
		super();

		addModel(AppointmentModel.getReference());
		addModel(TaskModel.getReference());

		initComponents();

		// add scroll to the table
		jScrollPane1.setViewportView(apptTable);
		jScrollPane1.getViewport().setBackground(menuBar.getBackground());

		// use a sorted table model
		apptTable.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Text"),
				Resource.getResourceString("Time") }, new Class[] {
				java.lang.String.class, java.util.Date.class, }));

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

		apanel_ = new AppointmentPanel(year, month, day);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jPanel2.add(apanel_, gridBagConstraints);

		showDate(year, month, day);

		pack();

		manageMySize(PrefName.APPTLISTVIEWSIZE);
	}

	public void destroy() {
		this.dispose();
	}

	// refresh is called to update the table of shown tasks due to model changes
	// or if the user
	// changes the filtering criteria
	public void refresh() {

		// clear all table rows
		deleteAll();

		try {

			alist_ = AppointmentModel.getReference().getAppts(key_);
			if (alist_ != null) {
				Iterator it = alist_.iterator();
				while (it.hasNext()) {
					Integer key = (Integer) it.next();
					Appointment ap = AppointmentModel.getReference().getAppt(
							key.intValue());

					Object[] ro = new Object[2];
					ro[0] = ap.getText();

					// just get time
					Date d = ap.getDate();
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(d);
					cal.set(2000, 1, 1);
					ro[1] = cal.getTime();

					addRow(ro);
				}
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// resize the table based on new row count
		resize();

		// apply default sort to the table
		defsort();
	}

	public void showApp(int key) {
		apptTable.clearSelection();
		TableSorter tm = (TableSorter) apptTable.getModel();
		int rows = tm.getRowCount();
		for (int i = 0; i < rows; ++i) {
			int j = tm.getMappedIndex(i);
			Integer k = (Integer)alist_.get(j);
			if( key == k.intValue())
			{
				apptTable.getSelectionModel().setSelectionInterval(i,i);
			}
		}
		apanel_.showapp(key, null);
	}

	public void valueChanged(ListSelectionEvent e) {
		// Ignore extra messages.
		if (e.getValueIsAdjusting())
			return;

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		if (lsm.isSelectionEmpty()) {
			apanel_.showapp(-1, null);
			return;
		}
		int row = lsm.getMinSelectionIndex();
		// int row = jTable1.getSelectedRow();
		if (row == -1)
			return;
		TableSorter tm = (TableSorter) apptTable.getModel();
		int i = tm.getMappedIndex(row);

		Integer apptkey = (Integer) alist_.get(i);
		apanel_.showapp(apptkey.intValue(), null);

	}

	private void addActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addActionPerformed
		apptTable.clearSelection();
		apanel_.showapp(-1, null);
	}// GEN-LAST:event_addActionPerformed

	// add a row to the sorted table
	private void addRow(Object[] ro) {
		TableSorter tm = (TableSorter) apptTable.getModel();
		tm.addRow(ro);
		tm.tableChanged(new TableModelEvent(tm));
	}

	// do the default sort - by text
	private void defsort() {
		TableSorter tm = (TableSorter) apptTable.getModel();
		if (!tm.isSorted())
			tm.sortByColumn(1);
		else
			tm.sort();
	}

	private void delActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_delActionPerformed
		int[] keys = getSelectedKeys();
		AppointmentModel model = AppointmentModel.getReference();
		for (int i = 0; i < keys.length; ++i)
			model.delAppt(keys[i]);
		apptTable.clearSelection();
	}// GEN-LAST:event_delActionPerformed

	// delete all rows from the sorted table
	private void deleteAll() {
		TableSorter tm = (TableSorter) apptTable.getModel();
		tm.setRowCount(0);
		tm.tableChanged(new TableModelEvent(tm));
	}

	private void deloneActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_deloneActionPerformed
		int[] keys = getSelectedKeys();
		AppointmentModel model = AppointmentModel.getReference();
		for (int i = 0; i < keys.length; ++i)
			model.delOneOnly(keys[i], key_);
		apptTable.clearSelection();
	}// GEN-LAST:event_deloneActionPerformed

	private void dismissActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_dismissActionPerformed
		this.dispose();
	}// GEN-LAST:event_dismissActionPerformed

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_exitForm
		this.dispose(); 
	}// GEN-LAST:event_exitForm

	private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_exitMenuItemActionPerformed
		this.dispose(); 
	}// GEN-LAST:event_exitMenuItemActionPerformed

	private JDateChooser getDateCB() {
		if (cb_ == null) {
			cb_ = new JDateChooser();
			// cb.setCalendar(cal_);
			cb_.addPropertyChangeListener("date", new PropertyChangeListener(){

			    public void propertyChange(PropertyChangeEvent arg0) {
				Calendar cal = cb_.getCalendar();
				if( cal != null )
				{
				    showDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
						cal.get(Calendar.DATE));
				}
				else
				    cb_.setCalendar(cal_);
				
			    }
			});
			
			/*
			cb_.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					Calendar cal = cb_.getCalendar();
					showDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
							cal.get(Calendar.DATE));
				}
			});*/
		}

		return (cb_);
	}

	// End of variables declaration//GEN-END:variables

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.gridwidth = 1;
			gridBagConstraints11.gridheight = 1;
			gridBagConstraints11.weighty = 1.0D;
			gridBagConstraints11.weightx = 1.0D;
			gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.gridy = 0;
			gridBagConstraints21.gridheight = 1;
			gridBagConstraints21.insets = new java.awt.Insets(0, 0, 0, 0);
			gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
			jPanel.add(jPanel2, gridBagConstraints11);
			jPanel.add(jPanel3, gridBagConstraints21);
		}
		return jPanel;
	}


	/**
	 * This method initializes mtgMailButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getMtgMailButton() {
		if (mtgMailButton == null) {
			mtgMailButton = new JButton();
			ResourceHelper.setText(mtgMailButton, "send_meeting");
			mtgMailButton.setIcon(new ImageIcon(getClass().getResource("/resource/ComposeMail16.gif")));  // Generated
			mtgMailButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					sendMtgMail();
				}
			});
		}
		return mtgMailButton;
	}

	private JButton copyButton = null;
	private JButton getCopyButton() {
		if (copyButton == null) {
		    copyButton = new JButton();
			ResourceHelper.setText(copyButton, "copy_appt");
			copyButton.setIcon(new ImageIcon(getClass().getResource("/resource/Copy16.gif")));  
			copyButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					copyAppt();
				}
			});
		}
		return copyButton;
	}
	
	
	/**
	 * This method initializes reminderButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getReminderButton() {
		if (reminderButton == null) {
			reminderButton = new JButton();
			ResourceHelper.setText(reminderButton, "send_reminder");
			reminderButton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/ComposeMail16.gif")));
			reminderButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							 class MailThread extends Thread {
							    private Calendar cal;
								public MailThread(Calendar c) {
							        cal = c;
							    }
							    public void run() {
							    	EmailReminder.sendDailyEmailReminder(cal);
							    }
							}
						    
							 new MailThread(cal_).start();
							
						}
					});
		}
		return reminderButton;
	}

	private List getSelectedAppointments() {
		int[] keys = getSelectedKeys();
		List appts = new ArrayList(keys.length);
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

	private int[] getSelectedKeys() {
		int[] rows = apptTable.getSelectedRows();
		List keyList = new ArrayList();
		for (int i = 0; i < rows.length; ++i) {
			int row = rows[i];
			TableSorter tm = (TableSorter) apptTable.getModel();
			int j = tm.getMappedIndex(row);

			keyList.add(alist_.get(j));
		}

		int[] keys = new int[keyList.size()];
		for (int i = 0; i < keyList.size(); ++i)
			keys[i] = ((Integer) keyList.get(i)).intValue();

		return keys;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the FormEditor.
	 */
	private void initComponents()// GEN-BEGIN:initComponents
	{

		GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		apptTable = new StripedTable();
		jPanel1 = new javax.swing.JPanel();
		add = new javax.swing.JButton();
		del = new javax.swing.JButton();
		delone = new javax.swing.JButton();
		dismiss = new javax.swing.JButton();
		menuBar = new javax.swing.JMenuBar();
		fileMenu = new javax.swing.JMenu();
		exitMenuItem = new javax.swing.JMenuItem();

		// getContentPane().setLayout(new java.awt.GridBagLayout());

		ResourceHelper.setTitle(this, "Appointment_Editor");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		jPanel2.setLayout(new java.awt.GridBagLayout());
		jPanel3.setLayout(new java.awt.GridBagLayout());

		jPanel3.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("apptlist")));
		jScrollPane1.setBorder(null);
		jScrollPane1.setViewport(jScrollPane1.getViewport());
		apptTable.setBorder(new javax.swing.border.LineBorder(
				new java.awt.Color(0, 0, 0)));
		apptTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
		//apptTable.setGridColor(java.awt.Color.blue);
		apptTable.setPreferredSize(new java.awt.Dimension(700, 500));
		jScrollPane1.setViewportView(apptTable);

		GridBagConstraints gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 1;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints2.weightx = 1.0;
		gridBagConstraints2.weighty = 10.0;
		gridBagConstraints2.insets = new java.awt.Insets(2, 2, 2, 2);
		jPanel1.setLayout(new java.awt.GridLayout(0, 1));

		add.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Edit16.gif")));
		ResourceHelper.setText(add, "EditNew");
		add.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addActionPerformed(evt);
			}
		});

		jPanel1.add(add);
		
		jPanel1.add(getCopyButton(), null);

		del.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		ResourceHelper.setText(del, "Delete");
		del.setToolTipText(Resource.getResourceString("del_tip"));

		ActionListener alDel = new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				delActionPerformed(evt);
			}
		};
		del.addActionListener(alDel);

		jPanel1.add(del);

		delone.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		ResourceHelper.setText(delone, "Delete_One_Only");
		delone.setToolTipText(Resource.getResourceString("doo_tip"));
		ActionListener alDelOne = new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deloneActionPerformed(evt);
			}
		};
		delone.addActionListener(alDelOne);

		dismiss.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Stop16.gif")));
		ResourceHelper.setText(dismiss, "Dismiss");
		dismiss.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dismissActionPerformed(evt);
			}
		});
		setDismissButton(dismiss);

		GridBagConstraints gridBagConstraints3 = new java.awt.GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.gridy = 2;
		gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints3.insets = new java.awt.Insets(2, 2, 2, 2);
		fileMenu.setBackground(menuBar.getBackground());
		ResourceHelper.setText(fileMenu, "File");
		exitMenuItem.setBackground(fileMenu.getBackground());
		ResourceHelper.setText(exitMenuItem, "Close");
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitMenuItemActionPerformed(evt);
			}
		});

		fileMenu.add(exitMenuItem);

		menuBar.add(fileMenu);

		setJMenuBar(menuBar);

		this.setContentPane(getJPanel());
		gridBagConstraints22.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints22.gridx = 0;
		gridBagConstraints22.gridy = 0;
		gridBagConstraints22.weightx = 1.0;
		gridBagConstraints22.insets = new java.awt.Insets(2, 2, 2, 2);
		apptTable
				.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		ActionListener alMoveToFollowingDay = new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				onMoveToFollowingDay(AppointmentListView.this,
						getSelectedAppointments());
			}
		};

		ActionListener alChangeDate = new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				onChangeDate(AppointmentListView.this,
						getSelectedAppointments());
			}
		};

		new PopupMenuHelper(apptTable, new PopupMenuHelper.Entry[] {
				new PopupMenuHelper.Entry(alDel, "Delete"),
				new PopupMenuHelper.Entry(alDelOne, "Delete_One_Only"),
				new PopupMenuHelper.Entry(alMoveToFollowingDay,
						"Move_To_Following_Day"),
				new PopupMenuHelper.Entry(alChangeDate, "changedate"), });

		jPanel3.add(getDateCB(), gridBagConstraints22);
		jPanel3.add(jScrollPane1, gridBagConstraints2);
		jPanel1.add(delone, delone.getName());
		jPanel3.add(jPanel1, gridBagConstraints3);
		jPanel1.add(getReminderButton(), null);
		jPanel1.add(getMtgMailButton(), null);  // Generated
		jPanel1.add(dismiss, dismiss.getName());
	}// GEN-END:initComponents


	// resize table based on row count
	private void resize() {
		int row = apptTable.getRowCount();
		apptTable.setPreferredSize(new Dimension(150, row * 16));

	}

	private void sendMtgMail()
	{
		int[] rows = apptTable.getSelectedRows();
		if( rows.length == 0)
			return;
		int k = rows[0];		
		TableSorter tm = (TableSorter) apptTable.getModel();
		int key = tm.getMappedIndex(k);

		Integer apptkey = (Integer) alist_.get(key);
		try {
			Appointment mtg = AppointmentModel.getReference().getAppt(apptkey.intValue());
			EmailReminder.emailMeeting(mtg);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
		
	}
	
	private void copyAppt()
	{
	    	int[] keys = getSelectedKeys();
	    	if( keys.length != 1)
	    	{
	    	    Errmsg.notice(Resource.getPlainResourceString("select_appt"));
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
	    	
		showDate(dlgcal.get(Calendar.YEAR), dlgcal.get(Calendar.MONTH), dlgcal.get(Calendar.DATE));
	    	Appointment appt;
		try {
		    appt = AppointmentModel.getReference().getAppt(keys[0]);
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		    return;
		}
		apanel_.showapp(-1, appt);
		refresh();
		
	    
	}
	
	private void showDate(int year, int month, int day) {
		cal_ = new GregorianCalendar(year, month, day);
		getDateCB().setCalendar(cal_);
		key_ = AppointmentModel.dkey(year, month, day);
		Date d = cal_.getTime();
		setTitle(Resource.getResourceString("Appointment_Editor_for_")
				+ DateFormat.getDateInstance(DateFormat.SHORT).format(d));

		// clear all rows
		deleteAll();
		apanel_.setDate(year, month, day);
		apanel_.showapp(-1, null);

		refresh();
	}
}
