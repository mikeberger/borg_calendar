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
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.ScrolledDialog;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.ui.util.TableSorter;

class SqlRunner extends JDialog {
	

	public SqlRunner() {

		super();
		
		setModal(false);
		
		// init the gui components
		initComponents();

		
		this.setTitle(Resource.getResourceString("RunSQL"));

		pack();

	}





	private void initComponents()// GEN-BEGIN:initComponents
	{

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0; // Generated
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH; // Generated
		gridBagConstraints.gridy = 1; // Generated
		this.getContentPane().setLayout(new GridBagLayout()); // Generated

		editor = new javax.swing.JEditorPane();
		jPanel1 = new javax.swing.JPanel();
		runButton = new javax.swing.JButton();

		editor.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(
				0, 0, 0)));

		jScrollPane1 = new JScrollPane();
		jScrollPane1.setPreferredSize(new java.awt.Dimension(554, 404));
		jScrollPane1.setViewportView(editor);

		runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Forward16.gif")));
		
		runButton.setText("Run");
		runButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				runbuttonActionPerformed(evt);
			}
		});

		jPanel1.add(runButton);
		

		clearButton = new JButton();
		clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		clearButton.setText("Clear");
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearbuttonActionPerformed(evt);
			}
		});

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

		jPanel1.add(clearButton, clearButton.getName());
		this.getContentPane().add(jPanel1, gridBagConstraints2); // Generated
		

	}


	private void runbuttonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			
			// System.out.println(sb.toString());
			TaskModel.getReference().beginTransaction();
			ResultSet r = JdbcDB.execSQL(editor.getText());
			TaskModel.getReference().commitTransaction();
			if( r != null && r.next())
			{
				JTable tbl = new JTable();
				tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

				int cols = r.getMetaData().getColumnCount();
				String colnames[] = new String[cols];
				Class<?> classes[] = new Class[cols];
				for( int c = 0; c < cols; c++)
				{
					colnames[c] = r.getMetaData().getColumnName(c+1);
					classes[c] = String.class;
				}
				TableSorter ts = new TableSorter(colnames, classes);
				ts.addMouseListenerToHeaderInTable(tbl);
				tbl.setModel(ts);
				Object row[] = new Object[cols];
				for( ;!r.isAfterLast();r.next())
				{
					for( int i = 1; i <= cols; i++)
					{
						row[i-1] = r.getString(i);
					}
					ts.addRow(row);
				}
				ScrolledDialog.showTable(tbl);
			}
			else
				ScrolledDialog.showNotice(Resource.getResourceString("noOutput"));
				//JOptionPane.showMessageDialog(this, Resource.getResourceString("noOutput"));
		} catch (Exception e) {
			System.out.println(e.toString());
			try {
			    TaskModel.getReference().rollbackTransaction();
			} catch (Exception e2) {
			}
			Errmsg.errmsg(e);
		}
		
		AppointmentModel.getReference().refresh();
		TaskModel.getReference().refresh();
		AddressModel.getReference().refresh();
		MemoModel.getReference().refresh();
		LinkModel.getReference().refresh();
		
		
	}

	private void clearbuttonActionPerformed(java.awt.event.ActionEvent evt) {
		editor.setText("");
	}

	private javax.swing.JPanel jPanel1;

	private javax.swing.JScrollPane jScrollPane1;

	private JEditorPane editor;

	private javax.swing.JButton runButton;

	private JButton clearButton;

	public static void main( String args[])
	{
		new SqlRunner().setVisible(true);
	}
}
