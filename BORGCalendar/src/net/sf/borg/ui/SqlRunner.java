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
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.ScrolledDialog;
import net.sf.borg.ui.util.TableSorter;

/**
 * SqlRunner is a UI that lets a user run SQL against the database. It presents the
 * results (if any) in a read-only table. It is mainly for debugging. the average user would never
 * use this.
 *
 */
class SqlRunner extends JDialog {

	private static final long serialVersionUID = 1L;
	private JEditorPane editor;

	public SqlRunner() {

		super();

		setModal(false);

		// init the gui components
		initComponents();

		this.setTitle(Resource.getResourceString("RunSQL"));

		pack();

	}

	/**
	 * initialize the ui - a simple editor with buttons to run the sql or clear the sql
	 */
	private void initComponents()
	{
		this.getContentPane().setLayout(new GridBagLayout());

		editor = new JEditorPane();
		JPanel jPanel1 = new JPanel();
		JButton runButton = new JButton();

		editor.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(
				0, 0, 0)));

		JScrollPane jScrollPane1 = new JScrollPane();
		jScrollPane1.setPreferredSize(new java.awt.Dimension(554, 404));
		jScrollPane1.setViewportView(editor);

		runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Forward16.gif")));

		runButton.setText("Run");
		runButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				runbuttonActionPerformed();
			}
		});

		jPanel1.add(runButton);

		JButton clearButton = new JButton();
		clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		clearButton.setText("Clear");
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				editor.setText("");
			}
		});

		this.getContentPane().add(jScrollPane1, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH, 1.0, 1.0));
		jPanel1.add(clearButton, clearButton.getName());
		this.getContentPane().add(jPanel1, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

	}

	/**
	 * run the SQL 
	 */
	private void runbuttonActionPerformed() {
		try {

			JdbcDB.beginTransaction();
			
			// run the sql 
			ResultSet r = JdbcDB.execSQL(editor.getText());
			JdbcDB.commitTransaction();
			
			// display the results in a table
			if (r != null && r.next()) {
				JTable tbl = new JTable();
				tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

				int cols = r.getMetaData().getColumnCount();
				String colnames[] = new String[cols];
				Class<?> classes[] = new Class[cols];
				for (int c = 0; c < cols; c++) {
					colnames[c] = r.getMetaData().getColumnName(c + 1);
					classes[c] = String.class;
				}
				TableSorter ts = new TableSorter(colnames, classes);
				ts.addMouseListenerToHeaderInTable(tbl);
				tbl.setModel(ts);
				Object row[] = new Object[cols];
				for (; !r.isAfterLast(); r.next()) {
					for (int i = 1; i <= cols; i++) {
						row[i - 1] = r.getString(i);
					}
					ts.addRow(row);
				}
				
				// use a ScrolledDialog to display the table
				ScrolledDialog.showTable(tbl);
			} else
				ScrolledDialog.showNotice(Resource
						.getResourceString("noOutput"));
			
		} catch (Exception e) {
			System.out.println(e.toString());
			try {
				JdbcDB.rollbackTransaction();
			} catch (Exception e2) {
			  // empty
			}
			Errmsg.errmsg(e);
		}

		// since the SQL may affect any of the tables, we need to 
		// just tell all models to refresh
		AppointmentModel.getReference().refresh();
		TaskModel.getReference().refresh();
		AddressModel.getReference().refresh();
		MemoModel.getReference().refresh();
		LinkModel.getReference().refresh();

	}
}
