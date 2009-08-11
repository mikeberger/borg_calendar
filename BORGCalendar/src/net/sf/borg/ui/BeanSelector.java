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
 Copyright 2003-2007 by Mike Berger
 */

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
import net.sf.borg.model.beans.Address;
import net.sf.borg.model.beans.Appointment;
import net.sf.borg.model.beans.KeyedBean;
import net.sf.borg.model.beans.Memo;
import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.Task;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TableSorter;

public class BeanSelector extends JDialog {

	private Collection<KeyedBean<?>> rows_ = new ArrayList<KeyedBean<?>>(); // list of rows currently

	// displayed
	private static ArrayList<KeyedBean<?>> list_ = new ArrayList<KeyedBean<?>>();

	private String fields_[];
	private Collection<? extends KeyedBean<?>> records_ = null;

	public static Appointment selectAppointment() throws Exception {

		Collection<Appointment> apps = AppointmentModel.getReference().getAllAppts();
		return ((Appointment) selectBean(apps, new TableSorter(new String[] {
				Resource.getResourceString("Text"),
				Resource.getResourceString("Time") }, new Class[] {
				String.class, Date.class }), new String[] { "Text", "Date" }));

	}

	public static Project selectProject() throws Exception {
		Collection<Project> projects = TaskModel.getReference().getProjects();
		return( (Project) BeanSelector.selectBean(
				projects,new TableSorter(new String[] {
						Resource.getPlainResourceString("Item_#"),
						Resource.getPlainResourceString("Description") },
						new Class[] { Integer.class,String.class }),
						new String[] { "Id", "Description" }));
	}
	
	public static Task selectTask() throws Exception {
		Collection<Task> tasks = TaskModel.getReference().getTasks();
		return( (Task) BeanSelector.selectBean(
				tasks,new TableSorter(new String[] {
						Resource.getPlainResourceString("Item_#"),
						Resource.getPlainResourceString("Description") },
						new Class[] { Integer.class,String.class }),
						new String[] { "TaskNumber", "Description" }));
	}
	
	public static Address selectAddress() throws Exception {
		Collection<Address> addrs = AddressModel.getReference().getAddresses();
		return( (Address) BeanSelector.selectBean(
				addrs,new TableSorter(new String[] {
						Resource.getPlainResourceString("Last"),
						Resource.getPlainResourceString("First") },
						new Class[] { String.class,String.class }),
						new String[] { "LastName", "FirstName" }));
	}

	public static Memo selectMemo() throws Exception {

		Collection<Memo> memos = MemoModel.getReference().getMemos();
		return ((Memo) selectBean(memos, new TableSorter(new String[] {
				Resource.getResourceString("Memo_Name")}, new Class[] {
				String.class}), new String[] { "MemoName" }));
	}

	private static Object selectBean(Collection<? extends KeyedBean<?>> records, TableModel tm,
			String fields[]) {
		new BeanSelector(records, tm, fields, false).setVisible(true);
		if (list_.size() != 0) {
			Object b = list_.get(0);
			return b;
		}

		return null;
	}
/*
	private static Collection selectBeans(Collection records, TableModel tm,
			String fields[]) {
		new BeanSelector(records, tm, fields, true).setVisible(true);
		return list_;
	}
*/
	private BeanSelector(Collection<? extends KeyedBean<?>> records, TableModel tm, String fields[],
			boolean multiple) {

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

		this.setTitle(Resource.getPlainResourceString("Select"));

		if (multiple)
			jTable1
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		else
			jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTable1MouseClicked(evt);
			}
		});

		loadData();

		pack();

	}

	@SuppressWarnings("unchecked")
    public void loadData() {

		// init the table to empty
		TableSorter tm = (TableSorter) jTable1.getModel();
		tm.addMouseListenerToHeaderInTable(jTable1);
		tm.setRowCount(0);

		Iterator<? extends KeyedBean<?>> it = records_.iterator();
		while (it.hasNext()) {
			 KeyedBean<?> r = it.next();

			try {
				Class<? extends KeyedBean<?>> beanClass = (Class<? extends KeyedBean<?>>) r.getClass();
				Object[] ro = new Object[fields_.length];
				for (int i = 0; i < fields_.length; i++) {
					// find method
					String method = "get" + fields_[i];
					Method m = beanClass.getDeclaredMethod(method, (Class[])null);
					ro[i] = m.invoke(r, (Object[])null);
				}
				// add the table row

				tm.addRow(ro);
				rows_.add(r);
				tm.tableChanged(new TableModelEvent(tm));
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}

		}

	}

	private void initComponents()// GEN-BEGIN:initComponents
	{

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0; // Generated
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH; // Generated
		gridBagConstraints.gridy = 1; // Generated
		this.getContentPane().setLayout(new GridBagLayout()); // Generated

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
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTable1MouseClicked(evt);
			}
		});

		jScrollPane1 = new JScrollPane();
		jScrollPane1.setPreferredSize(new java.awt.Dimension(554, 404));
		jScrollPane1.setViewportView(jTable1);

		selectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Add16.gif")));
		selectButton.setText(Resource.getPlainResourceString("Select"));

		selectButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				selectbuttonActionPerformed(evt);
			}
		});

		jPanel1.add(selectButton);

		clearButton = new JButton();
		clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		clearButton.setText(Resource.getPlainResourceString("Clear"));
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearbuttonActionPerformed(evt);
			}
		});

		jPanel1.add(clearButton);

		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.insets = new java.awt.Insets(4, 4, 4, 4);
		this.getContentPane().add(jScrollPane1, gridBagConstraints1);

		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;

		this.getContentPane().add(jPanel1, gridBagConstraints2); // Generated

	}

	private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {
		if (evt.getClickCount() > 1) {
			selectbuttonActionPerformed(null);
		}
	}

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
				Object[] oa = rows_.toArray();
				KeyedBean<?> b = (KeyedBean<?>) oa[k];
				list_.add(b);

			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}
		this.dispose();
	}

	private void clearbuttonActionPerformed(java.awt.event.ActionEvent evt) {
		list_.clear();
		this.dispose();
	}

	private javax.swing.JPanel jPanel1;

	private javax.swing.JScrollPane jScrollPane1;

	private javax.swing.JTable jTable1;

	private javax.swing.JButton selectButton;

	private JButton clearButton;

}
