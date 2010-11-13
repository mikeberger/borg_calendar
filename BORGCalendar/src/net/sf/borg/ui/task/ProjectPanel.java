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

package net.sf.borg.ui.task;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TablePrinter;
import net.sf.borg.ui.util.TableSorter;

/**
 * shows a list of projects in a table
 */
public class ProjectPanel extends JPanel implements Model.Listener {

	private static final long serialVersionUID = 1L;
	private static final int MAGIC_NO_DUE_DATE = 9999; // magic days left value if no due date

	/**
	 * renderer to show the days left column in color
	 */
	private class ProjIntRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		/**
		 * constructor
		 */
		public ProjIntRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			// get the default component to start with
			JLabel l = (JLabel) defaultTableCellRenderer
					.getTableCellRendererComponent(table, obj, isSelected,
							hasFocus, row, column);

			// if this is not the days left column then return the default
			// component
			String nm = table.getColumnName(column);
			if (obj == null
					|| !nm.equals(Resource.getResourceString("Days_Left")))
				return l;

			// get the days left
			int daysLeft = ((Integer) obj).intValue();

			this.setText(l.getText());
			this.setHorizontalAlignment(CENTER);
			this.setBackground(l.getBackground());
			this.setForeground(l.getForeground());

			if (daysLeft == MAGIC_NO_DUE_DATE)
				this.setText("--");

			// keep selected color as is
			if (isSelected)
				return this;

			// yellow alert -- <10 days left
			if (daysLeft < 10)
				this.setBackground(new Color(255, 255, 175));

			if (daysLeft < 5)
				this.setBackground(new Color(255, 200, 120));

			// red alert -- <2 days left
			if (daysLeft < 2) {
				this.setBackground(new Color(255, 120, 120));
			}

			return this;
		}
	}

	/** The button panel. */
	private JPanel buttonPanel = null;

	/** The default table cell renderer. */
	private TableCellRenderer defaultTableCellRenderer;

	/** The project table. */
	private StripedTable projectTable;

	/** The project status combo box. */
	private JComboBox projectStatusComboBox = new JComboBox();

	/**
	 * constructor
	 */
	public ProjectPanel() {
		super();

		// listen for task model changes
		TaskModel.getReference().addListener(this);

		try {
			initComponents();
			refresh();
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}

	}

	/**
	 * project change requested- bring up the project editor
	 * 
	 */
	private void projectChangeRequested() {

		int row = projectTable.getSelectedRow();
		if (row == -1)
			return;

		TableSorter tm = (TableSorter) projectTable.getModel();
		Integer projectId = (Integer) tm.getValueAt(row, 0);

		try {
			Project p = TaskModel.getReference().getProject(projectId.intValue());
			if (p == null)
				return;

			new ProjectView(p, ProjectView.Action.CHANGE, null).showView();

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * Clone seleced project
	 * 
	 */
	private void cloneSelectedProject() {

		// get prject id from column 0 of selected row
		int row = projectTable.getSelectedRow();
		if (row == -1)
			return;
		TableSorter tm = (TableSorter) projectTable.getModel();
		Integer projectId = (Integer) tm.getValueAt(row, 0);

		try {
			Project p = TaskModel.getReference().getProject(projectId.intValue());
			if (p == null)
				return;

			new ProjectView(p, ProjectView.Action.CLONE, null).showView();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * Close selected project
	 * 
	 * */
	private void closeSelectedProject() {

		int row = projectTable.getSelectedRow();
		if (row == -1)
			return;

		TableSorter tm = (TableSorter) projectTable.getModel();
		Integer projectId = (Integer) tm.getValueAt(row, 0);
		try {
			TaskModel.getReference().closeProject(projectId.intValue());
		} catch (Warning w) {
			Errmsg.notice(w.getMessage());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * delete selected task
	 * */
	private void deleteActionPerformed() {

		int row = projectTable.getSelectedRow();
		if (row == -1)
			return;

		TableSorter tm = (TableSorter) projectTable.getModel();
		Integer projectId = (Integer) tm.getValueAt(row, 0);

		// confirm delete
		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Really_delete_number_")
				+ projectId, "", JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			// delete the task
			try {
				TaskModel.getReference().deleteProject(projectId.intValue());
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

	}

	/**
	 * get the button panel
	 * 
	 * @return the button panel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());

			JButton addbutton = new JButton();
			addbutton.setText(Resource.getResourceString("Add"));
			addbutton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Add16.gif")));
			addbutton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						new ProjectView(null, ProjectView.Action.ADD, null)
								.showView();
					} catch (Exception ex) {
						Errmsg.errmsg(ex);
					}
				}
			});
			buttonPanel.add(addbutton, null);

			JButton changebutton1 = new JButton();
			changebutton1.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Edit16.gif")));
			changebutton1.setText(Resource.getResourceString("Change"));
			changebutton1
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent e) {
							projectChangeRequested();
						}
					});
			buttonPanel.add(changebutton1, null);

			JButton deletebutton1 = new JButton();
			deletebutton1.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Delete16.gif")));
			deletebutton1.setText(Resource.getResourceString("Delete"));
			deletebutton1
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent e) {
							deleteActionPerformed();
						}
					});
			buttonPanel.add(deletebutton1, null);

			JButton closebutton1 = new JButton();
			closebutton1.setIcon(new ImageIcon(getClass().getResource(
					"/resource/greenlight.gif")));
			closebutton1.setText(Resource.getResourceString("Close"));
			closebutton1.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					closeSelectedProject();
				}
			});
			buttonPanel.add(closebutton1, null);

			JButton clonebutton1 = new JButton();
			clonebutton1.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Copy16.gif")));
			clonebutton1.setText(Resource.getResourceString("Clone"));
			clonebutton1.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cloneSelectedProject();
				}
			});
			buttonPanel.add(clonebutton1, null);

		}
		return buttonPanel;
	}

	/**
	 * initialize the UI
	 * 
	 * @throws Exception
	 */
	private void initComponents() throws Exception {

		this.setLayout(new java.awt.GridBagLayout());

		/*
		 * Filter Panel
		 */
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(flowLayout);

		JLabel statusLabel = new JLabel();
		statusLabel.setText(Resource.getResourceString("Status") + ":");
		filterPanel.add(statusLabel, null);

		projectStatusComboBox.removeAllItems();
		projectStatusComboBox.addItem(Resource.getResourceString("All"));
		projectStatusComboBox.addItem(Resource.getResourceString("OPEN"));
		projectStatusComboBox.addItem(Resource.getResourceString("CLOSED"));
		projectStatusComboBox.setSelectedItem(Resource
				.getResourceString("OPEN"));
		projectStatusComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						refresh();
					}
				});
		filterPanel.add(projectStatusComboBox, null);

		this.add(filterPanel, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.HORIZONTAL));

		/*
		 * project table
		 */
		JScrollPane tableScroll = new JScrollPane();
		projectTable = new StripedTable();

		defaultTableCellRenderer = projectTable
				.getDefaultRenderer(Integer.class);

		// set renderer to the custom one for integers
		projectTable.setDefaultRenderer(Integer.class,
				new ProjectPanel.ProjIntRenderer());

		// use a sorted table model
		projectTable.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Item_#"),
				Resource.getResourceString("Category"),
				Resource.getResourceString("Status"),
				Resource.getResourceString("Start_Date"),
				Resource.getResourceString("Due_Date"),
				Resource.getResourceString("total_tasks"),
				Resource.getResourceString("open_tasks"),
				Resource.getResourceString("Days_Left"),
				Resource.getResourceString("Description") }, new Class[] {
				java.lang.Integer.class, java.lang.String.class, String.class,
				Date.class, Date.class, java.lang.Integer.class, Integer.class,
				Integer.class, java.lang.String.class }));

		// popup menu
		new PopupMenuHelper(projectTable, new PopupMenuHelper.Entry[] {
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							new ProjectView(null, ProjectView.Action.ADD, null)
									.showView();
						} catch (Exception e) {
							Errmsg.errmsg(e);
						}
					}
				}, "Add"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						projectChangeRequested();
					}
				}, "Change"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						cloneSelectedProject();
					}
				}, "Clone"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						deleteActionPerformed();
					}
				}, "Delete"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						closeSelectedProject();
					}
				}, "Close") });

		projectTable.getColumnModel().getColumn(0).setPreferredWidth(80);
		projectTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		projectTable.getColumnModel().getColumn(2).setPreferredWidth(80);
		projectTable.getColumnModel().getColumn(3).setPreferredWidth(80);
		projectTable.getColumnModel().getColumn(5).setPreferredWidth(80);
		projectTable.getColumnModel().getColumn(6).setPreferredWidth(80);
		projectTable.getColumnModel().getColumn(7).setPreferredWidth(80);
		projectTable.getColumnModel().getColumn(8).setPreferredWidth(400);

		projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// set up for sorting when a column header is clicked
		TableSorter tm = (TableSorter) projectTable.getModel();
		tm.addMouseListenerToHeaderInTable(projectTable);

		// clear all rows
		tm.setRowCount(0);
		tm.tableChanged(new TableModelEvent(tm));

		tableScroll.setViewportView(projectTable);

		projectTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

		this.add(tableScroll, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 1.0, 1.0));

		/*
		 * button panel
		 */
		this.add(getButtonPanel(), GridBagConstraintsFactory.create(0, 2));

	}

	/**
	 * Prints the project list
	 */
	public void print() {

		try {
			TablePrinter.printTable(projectTable);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * refresh when the task model changes
	 */
	public void refresh() {

		int row = 0;

		// clear all table rows
		TableSorter tm = (TableSorter) projectTable.getModel();
		tm.setRowCount(0);
		tm.tableChanged(new TableModelEvent(tm));

		String pstatfilt = (String) projectStatusComboBox.getSelectedItem();

		try {

			// add projects to project table
			Collection<Project> projects = TaskModel.getReference()
					.getProjects();
			for (Project project : projects) {

				// filter by status
				if (!pstatfilt.equals(Resource.getResourceString("All"))
						&& !pstatfilt.equals(project.getStatus()))
					continue;

				// filter by category
				if (!CategoryModel.getReference()
						.isShown(project.getCategory()))
					continue;

				// if we get here - we are displaying this task as a row
				// so fill in an array of objects for the row
				Object[] ro = new Object[10];
				ro[0] = new Integer(project.getKey());
				ro[1] = project.getCategory();
				ro[2] = project.getStatus();
				ro[3] = project.getStartDate();
				ro[4] = project.getDueDate();

				// number of tasks
				Collection<Task> ptasks = TaskModel.getReference().getTasks(
						project.getKey());
				ro[5] = new Integer(ptasks.size());

				// open tasks
				int open = 0;
				for (Task pt : ptasks) {
					if (!TaskModel.isClosed(pt)) {
						open++;
					}
				}
				ro[6] = new Integer(open);

				// days left
				ro[7] = new Integer(0);
				if (ro[4] == null)
					// if no due date 
					ro[7] = new Integer(MAGIC_NO_DUE_DATE);
				else {
					Date dd = (Date) ro[4];
					ro[7] = new Integer(TaskModel.daysLeft(dd));
				}

				// strip newlines from the description
				String de = project.getDescription();
				String tmp = "";
				for (int i = 0; de != null && i < de.length(); i++) {
					char c = de.charAt(i);
					if (c == '\n' || c == '\r') {
						tmp += ' ';
						continue;
					}

					tmp += c;
				}
				ro[8] = tmp;

				// add the task row to table
				tm.addRow(ro);
				tm.tableChanged(new TableModelEvent(tm));
				row++;
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	

}
