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
package net.sf.borg.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TableSorter;

/**
 * displays dialogs to let the user select various borg entities
 */
public class EntitySelector extends JDialog {

	private static final long serialVersionUID = 1L;

	/** The used to hold the chosen entities */
	private static ArrayList<KeyedEntity<?>> list_ = new ArrayList<KeyedEntity<?>>();

	/**
	 * Prompt the user to select an address.
	 * 
	 * @return the address
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static Address selectAddress() throws Exception {
		Collection<Address> addrs = AddressModel.getReference().getAddresses();
		return ((Address) EntitySelector.selectBean(addrs, new TableSorter(
				new String[] { Resource.getResourceString("Last"),
						Resource.getResourceString("First") }, new Class[] {
						String.class, String.class }), new String[] {
				"LastName", "FirstName" }));
	}

	/**
	 * Prompt the user to select an appointment.
	 * 
	 * @return the appointment
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static Appointment selectAppointment() throws Exception {

		Collection<Appointment> apps = AppointmentModel.getReference()
				.getAllAppts();
		return ((Appointment) selectBean(apps, new TableSorter(new String[] {
				Resource.getResourceString("Text"),
				Resource.getResourceString("Time") }, new Class[] {
				String.class, Date.class }), new String[] { "Text", "Date" }));

	}

	/**
	 * Prompt the user to select an entity.
	 * 
	 * @param records
	 *            the list of entities
	 * @param tm
	 *            the table model to insert the entities into
	 * @param fields
	 *            the array of fields to show in the table
	 * 
	 * @return the object
	 */
	private static Object selectBean(
			Collection<? extends KeyedEntity<?>> records, TableModel tm,
			String fields[]) {
		new EntitySelector(records, tm, fields, false).setVisible(true);
		if (list_.size() != 0) {
			Object b = list_.get(0);
			return b;
		}

		return null;
	}

	/**
	 * Prompt the user to select a memo.
	 * 
	 * @return the memo
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static Memo selectMemo() throws Exception {

		Collection<Memo> memos = MemoModel.getReference().getMemos();
		return ((Memo) selectBean(memos, new TableSorter(
				new String[] { Resource.getResourceString("Memo_Name") },
				new Class[] { String.class }), new String[] { "MemoName" }));
	}

	/**
	 * Prompt the user to select a project.
	 * 
	 * @return the project
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static Project selectProject() throws Exception {
		Collection<Project> projects = TaskModel.getReference().getProjects();
		return ((Project) EntitySelector.selectBean(projects, new TableSorter(
				new String[] { Resource.getResourceString("Item_#"),
						Resource.getResourceString("Description") },
				new Class[] { Integer.class, String.class }), new String[] {
				"Key", "Description" }));
	}

	/**
	 * Prompt the user to select a task.
	 * 
	 * @return the task
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static Task selectTask() throws Exception {
		Collection<Task> tasks = TaskModel.getReference().getTasks();
		return ((Task) EntitySelector.selectBean(tasks, new TableSorter(
				new String[] { Resource.getResourceString("Item_#"),
						Resource.getResourceString("Description") },
				new Class[] { Integer.class, String.class }), new String[] {
				"Key", "Description" }));
	}

	private JButton clearButton;

	/** The names of the entity fields to show in the table */
	private String fields_[];

	private javax.swing.JPanel jPanel1;

	private javax.swing.JScrollPane jScrollPane1;

	private javax.swing.JTable jTable1;

	/** The records_. */
	private Collection<? extends KeyedEntity<?>> records_ = null;

	private javax.swing.JButton selectButton;

	/**
	 * constructor
	 * 
	 * @param records
	 *            the list of entities
	 * @param tm
	 *            the table model to insert the entities into
	 * @param fields
	 *            the array of fields to show in the table
	 * 
	 * @param multiple
	 *            if true, allow multiple entities to be selected
	 */
	private EntitySelector(Collection<? extends KeyedEntity<?>> records,
			TableModel tm, String fields[], boolean multiple) {

		super();
		setModal(true);
		records_ = records;

		fields_ = fields;
		list_.clear();
		
		// init the gui components
		initComponents();

		if (tm != null) {
			jTable1.setModel(tm);
		}

		this.setTitle(Resource.getResourceString("Select"));

		if (multiple)
			jTable1
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		else
			jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTable1MouseClicked(evt);
			}
		});

		loadData();

		pack();

	}

	/**
	 * Clearbutton action performed.
	 * 
	 * @param evt
	 *            the evt
	 */
	private void clearbuttonActionPerformed(java.awt.event.ActionEvent evt) {
		list_.clear();
		this.dispose();
	}
	/**
	 * Initialize the UI components
	 */
	private void initComponents()
	{

		this.getContentPane().setLayout(new GridBagLayout()); 
		jTable1 = new StripedTable();
		jPanel1 = new javax.swing.JPanel();
		selectButton = new javax.swing.JButton();

		jTable1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(
				0, 0, 0)));

		DefaultListSelectionModel mylsmodel = new DefaultListSelectionModel();
		mylsmodel
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jTable1.setSelectionModel(mylsmodel);
		jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTable1MouseClicked(evt);
			}
		});

		jScrollPane1 = new JScrollPane();
		jScrollPane1.setPreferredSize(new java.awt.Dimension(554, 404));
		jScrollPane1.setViewportView(jTable1);

		selectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Add16.gif")));
		selectButton.setText(Resource.getResourceString("Select"));

		selectButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				selectbuttonActionPerformed(evt);
			}
		});

		jPanel1.add(selectButton);

		clearButton = new JButton();
		clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		clearButton.setText(Resource.getResourceString("Clear"));
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearbuttonActionPerformed(evt);
			}
		});

		jPanel1.add(clearButton);

		this.getContentPane().add(jScrollPane1, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH, 1.0, 1.0));

		this.getContentPane().add(jPanel1, GridBagConstraintsFactory.create(0, 2)); 

	}
	/**
	 * table mouse clicked.
	 * 
	 * @param evt
	 *            the evt
	 */
	private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {
		if (evt.getClickCount() > 1) {
			selectbuttonActionPerformed(null);
		}
	}
	/**
	 * Load the entities from the database into the table.
	 */
	@SuppressWarnings("unchecked")
	public void loadData() {

		// init the table to empty
		TableSorter tm = (TableSorter) jTable1.getModel();
		tm.addMouseListenerToHeaderInTable(jTable1);
		tm.setRowCount(0);

		Iterator<? extends KeyedEntity<?>> it = records_.iterator();
		while (it.hasNext()) {
			KeyedEntity<?> r = it.next();

			try {
				Class<? extends KeyedEntity<?>> beanClass = (Class<? extends KeyedEntity<?>>) r
						.getClass();
				Object[] ro = new Object[fields_.length];
				for (int i = 0; i < fields_.length; i++) {
					// find method
					String method = "get" + fields_[i];
					Method m = beanClass.getMethod(method, (Class[]) null);
					ro[i] = m.invoke(r, (Object[]) null);
				}
				// add the table row

				tm.addRow(ro);
				tm.tableChanged(new TableModelEvent(tm));
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}

		}

	}
	/**
	 * Selectbutton action performed.
	 * 
	 * @param evt
	 *            the evt
	 */
	private void selectbuttonActionPerformed(java.awt.event.ActionEvent evt) {
		// figure out which row is selected.
		list_.clear();
		int index[] = jTable1.getSelectedRows();
		if (index.length == 0)
			return;

		for (int i = 0; i < index.length; i++) {
			try {
				// need to ask the table for the original (befor sorting) index
				// of
				// the selected row
				TableSorter tm = (TableSorter) jTable1.getModel();
				int k = tm.getMappedIndex(index[i]); // get original index -
				// not
				// current sorted position in
				// tbl
				Object[] oa = records_.toArray();
				KeyedEntity<?> b = (KeyedEntity<?>) oa[k];
				list_.add(b);

			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}
		this.dispose();
	}

}
