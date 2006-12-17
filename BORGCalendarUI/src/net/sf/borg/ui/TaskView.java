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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Warning;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Project;
import net.sf.borg.model.Subtask;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.TaskTypes;
import net.sf.borg.model.Tasklog;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDateChooserCellEditor;

/**
 * 
 * @author MBERGER
 * @version
 */

// taskgui is a View that allows the user to edit a single task
class TaskView extends View {

	private JTable stable = new JTable();

	private JTable logtable = new JTable();

	private ArrayList tbd_ = new ArrayList();

	private TableCellRenderer defIntRend_;

	private TableCellRenderer defDateRend_;

	private TableCellRenderer defStringRend_;

	private class STIntRenderer extends JLabel implements TableCellRenderer {

		public STIntRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			JLabel l = (JLabel) defIntRend_.getTableCellRendererComponent(
					table, obj, isSelected, hasFocus, row, column);
			this.setHorizontalAlignment(CENTER);
			this.setForeground(l.getForeground());
			this.setBackground(l.getBackground());

			if (obj != null && obj instanceof Integer) {
				int i = ((Integer) obj).intValue();
				if (column == 1 && i == 0) {
					// this.setBackground(new Color(220, 220, 255));
					this.setText("--");
				} else {
					this.setText(Integer.toString(i));
				}
			} else if (obj == null)
				this.setText("--");
			return this;

		}
	}

	private class STStringRenderer extends JLabel implements TableCellRenderer {

		public STStringRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			JLabel l = (JLabel) defStringRend_.getTableCellRendererComponent(
					table, obj, isSelected, hasFocus, row, column);
			this.setForeground(l.getForeground());
			this.setBackground(l.getBackground());

			if (obj == null) {
				this.setBackground(new Color(220, 220, 255));
				this.setText("");
			} else {
				this.setText((String) obj);
			}

			return this;

		}
	}

	private class LongDateRenderer extends JLabel implements TableCellRenderer {

		public LongDateRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			Date d = (Date) obj;
			JLabel l = (JLabel) defDateRend_.getTableCellRendererComponent(
					table, obj, isSelected, hasFocus, row, column);

			this.setBackground(l.getBackground());
			this.setForeground(l.getForeground());
			this.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
					DateFormat.MEDIUM).format(d));
			return this;

		}
	}

	private class STDDRenderer extends JLabel implements TableCellRenderer {

		public STDDRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			Boolean closed = (Boolean) table.getModel().getValueAt(row, 0);

			Date dd = (Date) obj;

			JLabel l = (JLabel) defDateRend_.getTableCellRendererComponent(
					table, obj, isSelected, hasFocus, row, column);

			this.setBackground(l.getBackground());
			this.setForeground(l.getForeground());
			this.setHorizontalAlignment(l.getHorizontalAlignment());
			if (dd != null)
				this.setText(DateFormat.getDateInstance().format(dd));
			else {
				this.setText("--");
				this.setHorizontalAlignment(CENTER);
			}

			if (closed.booleanValue() == true || column != 4 || obj == null)
				return this;

			int days = TaskModel.daysLeft(dd);

			if (!isSelected) {
				// yellow alert -- <10 days left
				if (days < 10)
					this.setBackground(new Color(255, 255, 175));

				if (days < 5)
					this.setBackground(new Color(255, 200, 120));

				// red alert -- <2 days left
				if (days < 2) {
					this.setBackground(new Color(255, 120, 120));
				}
			}

			return this;
		}
	}

	public TaskView(Task task, int function) throws Exception {
		super();
		addModel(TaskModel.getReference());

		initComponents(); // init the GUI widgets

		initSubtaskTable();
		initLogTable();

		// set size of text area
		jTextArea1.setRows(15);
		jTextArea1.setColumns(40);

		try {
			Collection cats = CategoryModel.getReference().getCategories();
			Iterator it = cats.iterator();
			while (it.hasNext()) {
				catbox.addItem(it.next());
			}
			catbox.setSelectedIndex(0);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		pack();
		showtask(function, task);

		manageMySize(PrefName.TASKVIEWSIZE);
	}

	private void initSubtaskTable() {

		defIntRend_ = stable.getDefaultRenderer(Integer.class);
		defDateRend_ = stable.getDefaultRenderer(Date.class);
		defStringRend_ = stable.getDefaultRenderer(String.class);
		stable.setModel(new TableSorter(new String[] {
				Resource.getPlainResourceString("Closed"),
				Resource.getPlainResourceString("subtask_id"),
				Resource.getPlainResourceString("Description"),
				Resource.getPlainResourceString("created"),
				Resource.getPlainResourceString("Due_Date"),
				Resource.getPlainResourceString("Days_Left"),
				Resource.getPlainResourceString("close_date") }, new Class[] {
				java.lang.Boolean.class, Integer.class, java.lang.String.class,
				Date.class, Date.class, Integer.class, Date.class },
				new boolean[] { true, false, true, true, true, false, false }));

		stable.setDefaultRenderer(Integer.class, new STIntRenderer());
		stable.setDefaultRenderer(Date.class, new STDDRenderer());
		stable.setDefaultRenderer(String.class, new STStringRenderer());

		stable.getColumnModel().getColumn(0).setPreferredWidth(5);
		stable.getColumnModel().getColumn(1).setPreferredWidth(5);
		stable.getColumnModel().getColumn(2).setPreferredWidth(300);
		stable.getColumnModel().getColumn(3).setPreferredWidth(30);
		stable.getColumnModel().getColumn(4).setPreferredWidth(30);
		stable.getColumnModel().getColumn(5).setPreferredWidth(30);
		stable.getColumnModel().getColumn(6).setPreferredWidth(30);

		if (!TaskModel.getReference().hasSubTasks()) {
			stable.getTableHeader().setEnabled(false);
			stable.getTableHeader().setReorderingAllowed(false);
			stable.getTableHeader().setResizingAllowed(false);
			return;
		}
		stable.setDefaultEditor(Date.class, new JDateChooserCellEditor());

		TableSorter ts = (TableSorter) stable.getModel();

		ts.sortByColumn(4);
		ts.addMouseListenerToHeaderInTable(stable);
		new PopupMenuHelper(stable, new PopupMenuHelper.Entry[] {
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						insertSubtask();
					}
				}, "Add_Subtask"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						TableSorter ts = (TableSorter) stable.getModel();

						Integer ids[] = getSelectedIds();

						for (int i = 0; i < ids.length; ++i) {
							if (ids[i] == null)
								continue;
							for (int row = 0; row < ts.getRowCount(); row++) {
								Integer rowid = (Integer) ts.getValueAt(row, 1);
								if (rowid != null
										&& rowid.intValue() == ids[i]
												.intValue()) {
									ts.setValueAt(null, row, 4);
									break;
								}
							}
						}
					}
				}, "Clear_DueDate"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						TableSorter ts = (TableSorter) stable.getModel();

						int[] indices = stable.getSelectedRows();
						if (indices.length == 0)
							return;

						DateDialog dlg = new DateDialog(null);
						dlg.setCalendar(new GregorianCalendar());
						dlg.setVisible(true);
						Calendar dlgcal = dlg.getCalendar();
						if (dlgcal == null)
							return;

						Integer ids[] = getSelectedIds();

						for (int i = 0; i < ids.length; ++i) {
							if (ids[i] == null)
								continue;
							for (int row = 0; row < ts.getRowCount(); row++) {
								Integer rowid = (Integer) ts.getValueAt(row, 1);
								if (rowid != null
										&& rowid.intValue() == ids[i]
												.intValue()) {
									ts.setValueAt(dlgcal.getTime(), row, 4);
									break;
								}
							}
						}
					}
				}, "Set_DueDate"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						TableSorter ts = (TableSorter) stable.getModel();
						Integer ids[] = getSelectedIds();
						if (ids.length == 0)
							return;

						int ret = JOptionPane.showConfirmDialog(null, Resource
								.getResourceString("Really_Delete_")
								+ "?", Resource
								.getPlainResourceString("Confirm_Delete"),
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (ret != JOptionPane.OK_OPTION)
							return;

						for (int i = 0; i < ids.length; ++i) {
							// System.out.println(ids[i]);
							if (ids[i] == null)
								continue;
							tbd_.add(ids[i]);
							for (int row = 0; row < ts.getRowCount(); row++) {
								Integer rowid = (Integer) ts.getValueAt(row, 1);
								if (rowid != null
										&& rowid.intValue() == ids[i]
												.intValue()) {
									// clear the row
									ts.setValueAt(new Boolean(false), row, 0);
									ts.setValueAt(null, row, 1);
									ts.setValueAt(null, row, 2);
									ts.setValueAt(null, row, 3);
									ts.setValueAt(null, row, 4);
									ts.setValueAt(null, row, 5);
									ts.setValueAt(null, row, 6);
									break;
								}
							}
						}

						// if table is now empty - add 1 row back
						if (ts.getRowCount() == 0) {
							insertSubtask();
						}
					}
				}, "Delete"), });

	}

	private void insertSubtask() {
		Object o[] = { new Boolean(false), null, null, null, null, null, null };
		TableSorter ts = (TableSorter) stable.getModel();

		ts.addRow(o);
	}

	private Integer[] getSelectedIds() {
		TableSorter ts = (TableSorter) stable.getModel();
		int[] indices = stable.getSelectedRows();
		Integer[] ret = new Integer[indices.length];

		for (int i = 0; i < indices.length; ++i) {
			int index = indices[i];
			ret[i] = (Integer) ts.getValueAt(index, 1);
		}

		return ret;
	}

	private void initLogTable() {

		logtable.setModel(new TableSorter(new String[] {
				Resource.getPlainResourceString("Date"),
				Resource.getPlainResourceString("Description"), }, new Class[] {
				Date.class, String.class }, new boolean[] { false, false }));

		logtable.getColumnModel().getColumn(0).setPreferredWidth(5);
		logtable.getColumnModel().getColumn(1).setPreferredWidth(300);

		//logtable.setDefaultEditor(Date.class, new JDateChooserCellEditor());
		logtable.setDefaultRenderer(Date.class, new LongDateRenderer());
		TableSorter ts = (TableSorter) logtable.getModel();

		ts.sortByColumn(0);
		ts.addMouseListenerToHeaderInTable(logtable);
		new PopupMenuHelper(logtable,
				new PopupMenuHelper.Entry[] { new PopupMenuHelper.Entry(
						new java.awt.event.ActionListener() {
							public void actionPerformed(
									java.awt.event.ActionEvent evt) {

								String tasknum = itemtext.getText();
								if (tasknum.equals("CLONE")
										|| tasknum.equals("NEW"))
									return;
								String logentry = JOptionPane
										.showInputDialog(net.sf.borg.common.util.Resource
												.getResourceString("Enter_Log"));
								if (logentry == null)
									return;

								try {
									TaskModel.getReference()
											.addLog(Integer.parseInt(tasknum),
													logentry);
									loadLog(Integer.parseInt(tasknum));
								} catch (Exception e) {
									Errmsg.errmsg(e);
								}
							}
						}, "Add_Log"), });

	}

	public void destroy() {
		this.dispose();
	}

	// the different function values for calls to show task
	static int T_CLONE = 1;

	static int T_ADD = 2;

	static int T_CHANGE = 3;

	// the task editor currently does not refresh itself when the task data
	// model changes
	// - although it should not be changing while the task editor is open
	public void refresh() {
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the FormEditor.
	 */
	JComboBox projBox = new JComboBox();

	private void initComponents()// GEN-BEGIN:initComponents
	{
		java.awt.GridBagConstraints gridBagConstraints;

		GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
		gridBagConstraints22.gridx = 2;
		gridBagConstraints22.insets = new Insets(4, 20, 4, 4);
		gridBagConstraints22.gridy = 4;
		daysLeftLabel = new JLabel();
		daysLeftLabel.setText(Resource.getPlainResourceString("Days_Left"));
		daysLeftLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		daysLeftLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
		gridBagConstraints13.fill = GridBagConstraints.BOTH;
		gridBagConstraints13.gridy = 4;
		gridBagConstraints13.weightx = 1.0;
		gridBagConstraints13.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints13.gridx = 3;
		GridBagConstraints gridBagConstraints210 = new GridBagConstraints();
		gridBagConstraints210.gridx = 2; // Generated
		gridBagConstraints210.insets = new Insets(4, 20, 4, 4); // Generated
		gridBagConstraints210.gridy = 3; // Generated
		closeLabel = new JLabel();
		closeLabel.setText(""); // Generated
		closeLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT); // Generated
		closeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT); // Generated
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH; // Generated
		gridBagConstraints12.gridy = 3; // Generated
		gridBagConstraints12.weightx = 1.0; // Generated
		gridBagConstraints12.insets = new java.awt.Insets(4, 4, 4, 4); // Generated
		gridBagConstraints12.gridx = 3; // Generated
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		jTabbedPane1 = new javax.swing.JTabbedPane();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextArea1 = new javax.swing.JTextArea();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTextArea2 = new javax.swing.JTextArea();

		jPanel3 = new javax.swing.JPanel();
		itemtext = new javax.swing.JTextField();
		lblItemNum = new javax.swing.JLabel();
		lblStatus = new javax.swing.JLabel();
		startdatechooser = new JDateChooser();
		duedatechooser = new JDateChooser();
		pritext = new javax.swing.JComboBox();
		patext = new javax.swing.JTextField();
		lblStartDate = new javax.swing.JLabel();
		lblDueDate = new javax.swing.JLabel();
		lblPri = new javax.swing.JLabel();
		lblPA = new javax.swing.JLabel();
		lblType = new javax.swing.JLabel();
		statebox = new javax.swing.JComboBox();
		typebox = new javax.swing.JComboBox();
		catlabel = new javax.swing.JLabel();
		GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
		jPanel4 = new javax.swing.JPanel();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		jMenuItem1 = new javax.swing.JMenuItem();
		jMenuItem2 = new javax.swing.JMenuItem();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		jTextArea1.setLineWrap(true);
		jTextArea1.setName("Description");
		jScrollPane1.setViewportView(jTextArea1);

		jTabbedPane1.addTab(Resource.getResourceString("Description"),
				jScrollPane1);

		jTextArea2.setLineWrap(true);
		jScrollPane2.setViewportView(jTextArea2);

		jTabbedPane1.addTab(Resource.getResourceString("Resolution"),
				jScrollPane2);

		JScrollPane logPane = new JScrollPane();
		logPane.setViewportView(logtable);

		jTabbedPane1.addTab(Resource.getResourceString("history"), logPane);
		if (!TaskModel.getReference().hasSubTasks()) {
			jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfComponent(logPane),
					false);
		}

		gridBagConstraints = new java.awt.GridBagConstraints();
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints38 = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0D;
		getContentPane().add(jTabbedPane1, gridBagConstraints);

		jPanel3.setLayout(new java.awt.GridBagLayout());

		jPanel3.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("TaskInformation")));

		itemtext.setText("itemtext");

		ResourceHelper.setText(lblItemNum, "Item_#");
		lblItemNum.setLabelFor(itemtext);

		ResourceHelper.setText(lblStatus, "Status");
		lblStatus.setLabelFor(statebox);

		ResourceHelper.setText(lblStartDate, "Start_Date");
		lblStartDate.setLabelFor(startdatechooser);

		ResourceHelper.setText(lblDueDate, "Due_Date");
		lblDueDate.setLabelFor(duedatechooser);

		ResourceHelper.setText(lblPri, "Pri");
		lblPri.setLabelFor(pritext);

		ResourceHelper.setText(lblPA, "PA");
		lblPA.setLabelFor(patext);

		ResourceHelper.setText(lblType, "Type");
		lblType.setLabelFor(typebox);

		// typebox.addActionListener(new java.awt.event.ActionListener() {
		// public void actionPerformed(java.awt.event.ActionEvent evt) {
		// typeboxActionPerformed(evt);
		// }
		// });

		ResourceHelper.setText(catlabel, "Category");
		catlabel.setLabelFor(getCatbox());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		getContentPane().add(jPanel3, gridBagConstraints);

		jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(jButton2, "Save");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		jPanel4.add(jButton2, jButton2.getName());

		jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Stop16.gif")));
		ResourceHelper.setText(jButton3, "Dismiss");
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});
		setDismissButton(jButton3);
		jPanel4.add(jButton3);

		JButton addst = new JButton();
		addst.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Add16.gif")));
		ResourceHelper.setText(addst, "Add_Subtask");
		addst.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				insertSubtask();
			}
		});
		jPanel4.add(addst);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		getContentPane().add(jPanel4, gridBagConstraints);

		ResourceHelper.setText(jMenu1, "Menu");
		ResourceHelper.setText(jMenuItem1, "Save");
		jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				savetask(evt);
			}
		});

		jMenu1.add(jMenuItem1);

		ResourceHelper.setText(jMenuItem2, "Dismiss");
		jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				disact(evt);
			}
		});

		jMenu1.add(jMenuItem2);

		jMenuBar1.add(jMenu1);

		setJMenuBar(jMenuBar1);

		this.setSize(560, 517);
		this.setContentPane(getJPanel());

		gridBagConstraints26.gridx = 1;
		gridBagConstraints26.gridy = 1;
		gridBagConstraints26.weightx = 1.0;
		gridBagConstraints26.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints27.gridx = 0;
		gridBagConstraints27.gridy = 2;
		gridBagConstraints27.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints27.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints28.gridx = 0;
		gridBagConstraints28.gridy = 3;
		gridBagConstraints28.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints28.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints29.gridx = 0;
		gridBagConstraints29.gridy = 4;
		gridBagConstraints29.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints29.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints30.gridx = 2;
		gridBagConstraints30.gridy = 1;
		gridBagConstraints30.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints30.insets = new Insets(4, 20, 4, 4);
		gridBagConstraints31.gridx = 2;
		gridBagConstraints31.gridy = 2;
		gridBagConstraints31.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints31.insets = new Insets(4, 20, 4, 4);
		gridBagConstraints32.gridx = 2;
		gridBagConstraints32.gridy = 0;
		gridBagConstraints32.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints32.insets = new Insets(4, 20, 4, 4);
		gridBagConstraints33.gridx = 1;
		gridBagConstraints33.gridy = 4;
		gridBagConstraints33.weightx = 1.0;
		gridBagConstraints33.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints33.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints34.gridx = 1;
		gridBagConstraints34.gridy = 3;
		gridBagConstraints34.weightx = 1.0;
		gridBagConstraints34.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints34.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints35.gridx = 1;
		gridBagConstraints35.gridy = 2;
		gridBagConstraints35.weightx = 1.0;
		gridBagConstraints35.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints35.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints37.gridx = 0;
		gridBagConstraints37.gridy = 0;
		gridBagConstraints37.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints37.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints38.gridx = 1;
		gridBagConstraints38.gridy = 0;
		gridBagConstraints38.weightx = 1.0;
		gridBagConstraints38.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints38.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints2.gridx = 3;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.weightx = 1.0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints2.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints3.gridx = 3;
		gridBagConstraints3.gridy = 1;
		gridBagConstraints3.weightx = 1.0;
		gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints3.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints11.gridx = 3;
		gridBagConstraints11.gridy = 0;
		gridBagConstraints11.weightx = 1.0;
		gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints11.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(lblType, gridBagConstraints27);
		jPanel3.add(lblStartDate, gridBagConstraints28);
		jPanel3.add(lblDueDate, gridBagConstraints29);
		jPanel3.add(lblPri, gridBagConstraints30);
		jPanel3.add(lblPA, gridBagConstraints31);
		jPanel3.add(catlabel, gridBagConstraints32);
		jPanel3.add(duedatechooser, gridBagConstraints33);
		jPanel3.add(startdatechooser, gridBagConstraints34);
		jPanel3.add(typebox, gridBagConstraints35);
		jPanel3.add(lblItemNum, gridBagConstraints37); // Generated
		gridBagConstraints26.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(itemtext, gridBagConstraints38); // Generated
		jPanel3.add(lblStatus, gridBagConstraints1); // Generated
		jPanel3.add(patext, gridBagConstraints2);
		jPanel3.add(pritext, gridBagConstraints3);
		jPanel3.add(getCatbox(), gridBagConstraints11);
		jPanel3.add(statebox, gridBagConstraints26); // Generated
		jPanel3.add(getCloseDate(), gridBagConstraints12);
		jPanel3.add(closeLabel, gridBagConstraints210);
		jPanel3.add(getDaysLeftText(), gridBagConstraints13);
		jPanel3.add(daysLeftLabel, gridBagConstraints22);
		ResourceHelper.setText(closeLabel, "close_date");

		for (int p = 1; p <= 5; p++) {
			pritext.addItem(new Integer(p));
		}

		JLabel prLabel = new JLabel(Resource.getPlainResourceString("project"));
		GridBagConstraints gridBagConstraints90 = new GridBagConstraints();
		gridBagConstraints90.gridx = 0;
		gridBagConstraints90.gridy = 5;
		gridBagConstraints90.weightx = 1.0;
		gridBagConstraints90.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints90.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(prLabel, gridBagConstraints90);

		GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
		gridBagConstraints91.gridx = 1;
		gridBagConstraints91.gridy = 5;
		gridBagConstraints91.weightx = 1.0;
		gridBagConstraints91.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints91.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(projBox, gridBagConstraints91);

	}// GEN-END:initComponents

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton3ActionPerformed
	{// GEN-HEADEREND:event_jButton3ActionPerformed
		this.dispose();
	}// GEN-LAST:event_jButton3ActionPerformed

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton2ActionPerformed
	{// GEN-HEADEREND:event_jButton2ActionPerformed
		savetask(evt);
	}// GEN-LAST:event_jButton2ActionPerformed

	// save a task
	private void savetask(java.awt.event.ActionEvent evt)// GEN-FIRST:event_savetask
	{// GEN-HEADEREND:event_savetask

		// save a task from the data on the screen
		if (jTextArea1.getText() == null || jTextArea1.getText().equals("")) {
			Errmsg.notice(Resource.getResourceString("empty_desc"));
			return;
		}
		try {

			String num = itemtext.getText();

			// allocate a new task object from the task data model
			TaskModel taskmod_ = TaskModel.getReference();
			taskmod_.beginTransaction();
			Task task = taskmod_.newMR();

			// set the task number to the current number for updates and
			// -1 for new tasks. task model will convert -1 to next
			// available number
			TableSorter ts = (TableSorter) stable.getModel();
			if (num.equals("NEW")) {

				task.setTaskNumber(new Integer(-1));
				String cbs[] = TaskModel.getReference().getTaskTypes()
						.checkBoxes((String) typebox.getSelectedItem());
				for (int i = 0; i < cbs.length; i++) {
					if (!cbs[i].equals(TaskTypes.NOCBVALUE)) {
						Object o[] = { new Boolean(false), null, cbs[i],
								new Date(), null, null };
						ts.addRow(o);
					}
				}
				task.setState(taskmod_.getTaskTypes().getInitialState(
						(String) typebox.getSelectedItem()));
			} else if (num.equals("CLONE")) {
				task.setTaskNumber(new Integer(-1));
				task.setState(taskmod_.getTaskTypes().getInitialState(
						(String) typebox.getSelectedItem()));

			} else {
				task.setTaskNumber(new Integer(num));
				task.setState((String) statebox.getSelectedItem());
			}

			// fill in the taks fields from the screen
			task.setType((String) typebox.getSelectedItem()); // type
			Calendar cal = startdatechooser.getCalendar();
			if( cal == null ) cal = new GregorianCalendar();
			//startdatechooser.setCalendar(new GregorianCalendar());
			task.setStartDate(cal.getTime()); // start date
			cal = duedatechooser.getCalendar();
			if( cal == null ) cal = new GregorianCalendar();
			task.setDueDate(cal.getTime()); // due date
			Integer pri = (Integer) pritext.getSelectedItem();
			task.setPriority(pri); // priority
			task.setPersonAssigned(patext.getText()); // person assigned

			task.setDescription(jTextArea1.getText()); // description
			task.setResolution(jTextArea2.getText()); // resolution

			// task.setCategory( cattext.getText());
			String cat = (String) catbox.getSelectedItem();
			if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
				task.setCategory(null);
			} else {
				task.setCategory(cat);
			}

			task.setProject(null);
			String proj = (String) projBox.getSelectedItem();
			try{
				task.setProject(getProjectId(proj));
			} catch (Exception e) {
				// no project selected
			}

			if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
				task.setCategory(null);
			} else {
				task.setCategory(cat);
			}

			// do not close task if subtasks are open
			if (task.getState().equals(
					TaskModel.getReference().getTaskTypes().getFinalState(
							task.getState()))) {
				for (int r = 0; r < stable.getRowCount(); r++) {
					Boolean closed = (Boolean) ts.getValueAt(r, 0);
					Integer id = (Integer) ts.getValueAt(r, 1);
					if (id == null || id.intValue() == 0)
						continue;
					if (closed.booleanValue() != true) {
						Errmsg.notice(Resource
								.getResourceString("open_subtasks"));
						return;
					}
				}
			}

			// save the task to the DB
			Task orig = TaskModel.getReference().getMR(
					task.getTaskNumber().intValue());
			taskmod_.savetask(task);

			// System.out.println(task.getTaskNumber());

			if (num.equals("NEW") || num.equals("CLONE")) {
				TaskModel.getReference().addLog(
						task.getTaskNumber().intValue(),
						Resource.getPlainResourceString("Task_Created"));
			} else {
				if (orig != null && !orig.getState().equals(task.getState())) {

					TaskModel.getReference().addLog(
							task.getTaskNumber().intValue(),
							Resource.getPlainResourceString("State_Change")
									+ ": " + orig.getState() + " --> "
									+ task.getState());
				}
				String newd = DateFormat.getDateInstance().format(
						task.getDueDate());
				String oldd = DateFormat.getDateInstance().format(
						orig.getDueDate());
				if (orig != null && !newd.equals(oldd)) {

					TaskModel.getReference().addLog(
							task.getTaskNumber().intValue(),
							Resource.getPlainResourceString("DueDate") + " "
									+ Resource.getPlainResourceString("Change")
									+ ": " + oldd + " --> " + newd);
				}
			}

			saveSubtasks(task);
			taskmod_.commitTransaction();

			// refresh window from DB - will update task number for
			// new tasks and will set the list of available next states from
			// the task model
			showtask(T_CHANGE, task);
		} catch (Warning w) {
			Errmsg.notice(w.getMessage());
			try {
				TaskModel.getReference().rollbackTransaction();
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
			try {
				TaskModel.getReference().rollbackTransaction();
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
			}

		}

	}// GEN-LAST:event_savetask

	private void saveSubtasks(Task task) throws Warning, Exception {

		int tasknum = task.getTaskNumber().intValue();
		Iterator it = tbd_.iterator();
		while (it.hasNext()) {
			Integer id = (Integer) it.next();
			// System.out.println("deleting sub task: " + id.intValue());
			TaskModel.getReference().deleteSubTask(id.intValue());
			TaskModel.getReference().addLog(
					tasknum,
					Resource.getPlainResourceString("subtask") + " "
							+ id.toString() + " "
							+ Resource.getPlainResourceString("deleted"));
		}

		tbd_.clear();

		// loop through rows
		TableSorter ts = (TableSorter) stable.getModel();
		for (int r = 0; r < stable.getRowCount(); r++) {
			Object desc = ts.getValueAt(r, 2);
			if (desc == null || desc.equals(""))
				continue;

			Integer id = (Integer) ts.getValueAt(r, 1);

			Boolean closed = (Boolean) ts.getValueAt(r, 0);
			Date crd = (Date) ts.getValueAt(r, 3);
			if (crd == null)
				crd = new Date();
			Date dd = (Date) ts.getValueAt(r, 4);
			Date cd = (Date) ts.getValueAt(r, 6);

			boolean closing = false;
			if (closed.booleanValue() == true && cd == null) {
				cd = new Date();
				closing = true;
			} else if (closed.booleanValue() == false && cd != null)
				cd = null;

			Subtask s = new Subtask();
			s.setId(id);
			s.setDescription((String) desc);
			s.setCloseDate(cd);
			s.setDueDate(dd);

			// validate dd - make sure only date and not time is compared
			if (dd != null) {
				GregorianCalendar tcal = new GregorianCalendar();
				tcal.setTime(task.getDueDate());
				tcal.set(Calendar.HOUR_OF_DAY, 0);
				tcal.set(Calendar.MINUTE, 10);
				tcal.set(Calendar.SECOND, 0);
				GregorianCalendar dcal = new GregorianCalendar();
				dcal.setTime(dd);
				dcal.set(Calendar.HOUR_OF_DAY, 0);
				dcal.set(Calendar.MINUTE, 0);
				dcal.set(Calendar.SECOND, 0);
				if (dcal.getTime().after(tcal.getTime())) {
					String msg = Resource
							.getPlainResourceString("stdd_warning")
							+ ": " + desc;
					throw new Warning(msg);
				}

			}

			s.setCreateDate(crd);
			s.setTask(new Integer(tasknum));
			TaskModel.getReference().saveSubTask(s);
			if (id == null || id.intValue() == 0) {
				TaskModel.getReference().addLog(
						tasknum,
						Resource.getPlainResourceString("subtask") + " "
								+ s.getId().toString() + " "
								+ Resource.getPlainResourceString("created")
								+ ": " + s.getDescription());
			}
			if (closing) {
				TaskModel.getReference().addLog(
						tasknum,
						Resource.getPlainResourceString("subtask") + " "
								+ s.getId().toString() + " "
								+ Resource.getPlainResourceString("Closed")
								+ ": " + s.getDescription());
			}
		}
	}

	private void disact(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_disact
		this.dispose();
	}// GEN-LAST:event_disact

	private void loadLog(int taskid) throws Exception {
		TableSorter tslog = (TableSorter) logtable.getModel();
		tslog.setRowCount(0);
		// add log entries
		Collection logs = TaskModel.getReference().getLogs(taskid);
		Iterator it = logs.iterator();
		while (it.hasNext()) {
			Tasklog s = (Tasklog) it.next();
			Object o[] = { s.getlogTime(), s.getDescription() };

			tslog.addRow(o);
		}
	}

	private void showtask(int function, Task task) throws Exception {
		TaskModel taskmod_ = TaskModel.getReference();

		// show a task editor for changing, cloning, or add of a task
		TableSorter ts = (TableSorter) stable.getModel();
		ts.setRowCount(0);

		tbd_.clear();

		projBox.removeAllItems();
		projBox.addItem("");
		Collection projects = taskmod_.getProjects();
		Iterator pi = projects.iterator();
		while (pi.hasNext()) {
			Project p = (Project) pi.next();
			if( p.getStatus().equals("OPEN"))
			    projBox.addItem(getProjectString(p));
		}

		// if we are showing an existing task - fil; in the gui fields form it
		if (task != null) {
			// task number
			itemtext.setText(task.getTaskNumber().toString());
			itemtext.setEditable(false);

			// window title - "Item N"
			setTitle(Resource.getResourceString("Item_")
					+ task.getTaskNumber().toString());

			// due date
			GregorianCalendar gc = new GregorianCalendar();
			Date dd = task.getDueDate();
			if (dd != null)
				gc.setTime(dd);

			duedatechooser.setCalendar(gc);

			GregorianCalendar gc2 = new GregorianCalendar();
			dd = task.getStartDate();
			if (dd != null)
				gc2.setTime(dd);

			startdatechooser.setCalendar(gc2);

			pritext.setSelectedItem(task.getPriority()); // priority
			patext.setText(task.getPersonAssigned()); // person assigned

			Date cd = task.getCD();
			if (cd != null)
				closeDate.setText(DateFormat.getDateInstance(DateFormat.MEDIUM)
						.format(cd));

			int daysleft = TaskModel.daysLeft(task.getDueDate());
			daysLeftText.setText(Integer.toString(daysleft));

			// cattext.setText( task.getCategory() );
			String cat = task.getCategory();
			if (cat != null && !cat.equals("")) {
				catbox.setSelectedItem(cat);
			} else {
				catbox.setSelectedIndex(0);
			}

			jTextArea1.setText(task.getDescription()); // description
			jTextArea2.setText(task.getResolution()); // resolution

			statebox.addItem(task.getState()); // state
			statebox.setEditable(false);

			// type
			String type = task.getType();
			typebox.addItem(type);
			typebox.setEnabled(false);

			// add subtasks
			if (TaskModel.getReference().hasSubTasks()) {
				Collection col = TaskModel.getReference().getSubTasks(
						task.getTaskNumber().intValue());
				Iterator it = col.iterator();
				while (it.hasNext()) {
					Subtask s = (Subtask) it.next();
					Object o[] = {
							s.getCloseDate() == null ? new Boolean(false)
									: new Boolean(true),
							s.getId(),
							s.getDescription(),
							s.getCreateDate(),
							s.getDueDate(),
							s.getDueDate() != null ? new Integer(TaskModel
									.daysLeft(s.getDueDate())) : null,
							s.getCloseDate() };

					ts.addRow(o);
				}

			}

			loadLog(task.getTaskNumber().intValue());
			
			Integer pid = task.getProject();
			if( pid != null )
			{
				Project p = TaskModel.getReference().getProject(pid.intValue());
				projBox.setSelectedItem(getProjectString(p));
			}

		} else // initialize new task
		{

			// task number = NEW
			itemtext.setText("NEW");
			itemtext.setEditable(false);

			// title
			ResourceHelper.setTitle(this, "NEW_Item");

			pritext.setSelectedItem(new Integer(3)); // priority default to 3
			patext.setText(""); // person assigned
			// cattext.setText("");
			catbox.setSelectedIndex(0);
			jTextArea1.setText(""); // desc
			jTextArea2.setText(""); // resolution

			Vector tv = taskmod_.getTaskTypes().getTaskTypes();
			for (int i = 0; i < tv.size(); i++) {
				typebox.addItem(tv.elementAt(i));
			}
			//duedatechooser.setCalendar(new GregorianCalendar());
			//startdatechooser.setCalendar(new GregorianCalendar());

		}

		if (task == null) {
			// statebox.addItem(taskmod_.getTaskTypes().getInitialState(
			// typebox.getSelectedItem().toString()));
			statebox.setEnabled(false);
		}

		// cloning takes the fields filled in for an existing task and resets
		// only those
		// that don't apply to the clone
		if (function == T_CLONE) {
			// need new task number
			itemtext.setText("CLONE");
			itemtext.setEditable(false);

			statebox.removeAllItems();
			statebox.addItem(taskmod_.getTaskTypes().getInitialState(
					typebox.getSelectedItem().toString()));
			statebox.setEnabled(false);

			// reset all subtask id's
			for (int row = 0; row < stable.getRowCount(); row++) {
				stable.setValueAt(null, row, 1);
			}
			
			
			
		}
		// change existing task
		else if (function == T_CHANGE) {

			// determine valid next states based on task type and current
			// state
			String stat = task.getState();
			String type = task.getType();
			Vector v = taskmod_.getTaskTypes().nextStates(stat, type);

			// set next state pulldown
			statebox.removeAllItems();
			for (int i = 0; i < v.size(); i++) {
				statebox.addItem(v.elementAt(i));
			}
			statebox.setEnabled(true);

		}

		if (TaskModel.getReference().hasSubTasks() && stable.getRowCount() == 0) {
			insertSubtask();
		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel catlabel;

	private javax.swing.JButton jButton2;

	private javax.swing.JButton jButton3;

	private javax.swing.JLabel lblItemNum;

	private javax.swing.JLabel lblStatus;

	private javax.swing.JLabel lblStartDate;

	private javax.swing.JLabel lblDueDate;

	private javax.swing.JLabel lblPri;

	private javax.swing.JLabel lblPA;

	private javax.swing.JLabel lblType;

	private javax.swing.JMenu jMenu1;

	private javax.swing.JMenuBar jMenuBar1;

	private javax.swing.JMenuItem jMenuItem1;

	private javax.swing.JMenuItem jMenuItem2;

	private javax.swing.JPanel jPanel3;

	private javax.swing.JPanel jPanel4;

	private javax.swing.JScrollPane jScrollPane1;

	private javax.swing.JScrollPane jScrollPane2;

	private javax.swing.JTabbedPane jTabbedPane1;

	private javax.swing.JTextArea jTextArea1;

	private javax.swing.JTextArea jTextArea2;

	private javax.swing.JTextField itemtext;

	private JDateChooser startdatechooser;

	private JDateChooser duedatechooser;

	private javax.swing.JComboBox pritext;

	private javax.swing.JTextField patext;

	private javax.swing.JComboBox statebox;

	private javax.swing.JComboBox typebox;

	// End of variables declaration//GEN-END:variables

	private JPanel jPanel = null;

	private JComboBox catbox = null;

	private JTextField closeDate = null;

	private JLabel closeLabel = null;

	private JTextField daysLeftText = null;

	private JLabel daysLeftLabel = null;

	private JSplitPane jSplitPane = null;

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	JPanel stpanel = new JPanel();
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.weighty = 1.0;
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints5.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints5.weightx = 1.0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.BOTH;
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.weighty = 1.0;
			gridBagConstraints4.insets = new Insets(4, 4, 4, 4);
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridy = 0;
			gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints21.weightx = 1.0D;

			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.gridy = 2;

			jPanel.add(getJSplitPane(), gridBagConstraints5);
			jPanel.add(jPanel3, gridBagConstraints21); // Generated

			jPanel.add(jPanel4, gridBagConstraints25);
			// subtasks
			JScrollPane stscroll = new JScrollPane();
			stscroll.setPreferredSize(new Dimension(300, 300));
			stscroll.setViewportView(stable);
			
			stpanel.setLayout(new GridBagLayout());
			stpanel.setBorder(new javax.swing.border.TitledBorder(Resource
					.getResourceString("Subtasks")));
			stpanel.add(stscroll, gridBagConstraints4);
		}
		return jPanel;
	}

	/**
	 * This method initializes catbox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getCatbox() {
		if (catbox == null) {
			catbox = new JComboBox();
		}
		return catbox;
	}

	/**
	 * This method initializes closeDate
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getCloseDate() {
		if (closeDate == null) {
			closeDate = new JTextField();

			closeDate.setEditable(false); // Generated
		}
		return closeDate;
	}

	/**
	 * This method initializes daysLeftText
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getDaysLeftText() {
		if (daysLeftText == null) {
			daysLeftText = new JTextField();
			daysLeftText.setEditable(false);
		}
		return daysLeftText;
	}

	static public String getProjectString(Project p) {
		return p.getId().toString() + ":" + p.getDescription();
	}

	static public Integer getProjectId(String s) throws Exception {
		int i = s.indexOf(":");
		if( i == -1 ) throw new Exception("Cannot parse project label");
		String ss = s.substring(0, i);

		int pid = Integer.parseInt(ss);
		return new Integer(pid);

	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
	    if (jSplitPane == null) {
		jSplitPane = new JSplitPane();
		jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane.setBottomComponent(stpanel);
		jSplitPane.setPreferredSize(new Dimension(400, 400));
		jSplitPane.setDividerLocation(100);
		jSplitPane.setTopComponent(jTabbedPane1);
	    }
	    return jSplitPane;
	}
} // @jve:decl-index=0:visual-constraint="115,46"
