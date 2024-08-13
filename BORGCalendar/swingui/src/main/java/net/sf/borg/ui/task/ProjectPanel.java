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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.IconHelper;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.TablePrinter;
import net.sf.borg.ui.util.TableSorter;

/**
 * shows a list of projects in a table
 */
public class ProjectPanel extends JPanel implements Model.Listener {

	private static final long serialVersionUID = 1L;
	private static final int MAGIC_NO_DUE_DATE = 9999; // magic days left value if no due date





	/** The button panel. */
	private JPanel buttonPanel = null;

	/** The project table. */
	private JTable projectTable;

	/** The project status combo box. */
	private final JComboBox<String> projectStatusComboBox = new JComboBox<String>();

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
			Errmsg.getErrorHandler().errmsg(e);
			return;
		}

	}
	
	public void cleanup(){
		TaskModel.getReference().removeListener(this);
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
			Errmsg.getErrorHandler().errmsg(e);
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
			Errmsg.getErrorHandler().errmsg(e);
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
			Errmsg.getErrorHandler().notice(w.getMessage());
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
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
				+ " " + projectId, Resource.getResourceString("Confirm_Delete"), JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			// delete the task
			try {
				TaskModel.getReference().deleteProject(projectId.intValue());
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
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
			addbutton.setIcon(IconHelper.getIcon(
					"/resource/Add16.gif"));
			addbutton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						new ProjectView(null, ProjectView.Action.ADD, null)
								.showView();
					} catch (Exception ex) {
						Errmsg.getErrorHandler().errmsg(ex);
					}
				}
			});
			buttonPanel.add(addbutton, null);

			JButton changebutton1 = new JButton();
			changebutton1.setIcon(IconHelper.getIcon(
					"/resource/Edit16.gif"));
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
			deletebutton1.setIcon(IconHelper.getIcon(
					"/resource/Delete16.gif"));
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
			closebutton1.setIcon(IconHelper.getIcon(
					"/resource/greenlight.gif"));
			closebutton1.setText(Resource.getResourceString("Close"));
			closebutton1.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					closeSelectedProject();
				}
			});
			buttonPanel.add(closebutton1, null);

			JButton clonebutton1 = new JButton();
			clonebutton1.setIcon(IconHelper.getIcon(
					"/resource/Copy16.gif"));
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
		projectTable = new JTable();



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
							Errmsg.getErrorHandler().errmsg(e);
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

		projectTable.setShowGrid(true);
		projectTable.setIntercellSpacing(new Dimension(1, 1));


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
			Errmsg.getErrorHandler().errmsg(e);
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
				ro[0] = Integer.valueOf(project.getKey());
				ro[1] = project.getCategory();
				ro[2] = project.getStatus();
				ro[3] = project.getStartDate();
				ro[4] = project.getDueDate();

				// number of tasks
				Collection<Task> ptasks = TaskModel.getReference().getTasks(
						project.getKey());
				ro[5] = Integer.valueOf(ptasks.size());

				// open tasks
				int open = 0;
				for (Task pt : ptasks) {
					if (!TaskModel.isClosed(pt)) {
						open++;
					}
				}
				ro[6] = Integer.valueOf(open);

				// days left
				ro[7] = Integer.valueOf(0);
				if (ro[4] == null)
					// if no due date 
					ro[7] = Integer.valueOf(MAGIC_NO_DUE_DATE);
				else {
					Date dd = (Date) ro[4];
					ro[7] = Integer.valueOf(DateUtil.daysLeft(dd));
				}

				// strip newlines from the description
				String de = project.getDescription();
				StringBuffer tmp = new StringBuffer();
				for (int i = 0; de != null && i < de.length(); i++) {
					char c = de.charAt(i);
					if (c == '\n' || c == '\r') {
						tmp.append(' ');
						continue;
					}

					tmp.append(c);
				}
				ro[8] = tmp.toString();

				// add the task row to table
				tm.addRow(ro);
				tm.tableChanged(new TableModelEvent(tm));
			}

		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

	}

	

}
