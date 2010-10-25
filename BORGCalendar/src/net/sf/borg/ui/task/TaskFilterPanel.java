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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.TaskTypes;
import net.sf.borg.model.entity.Project;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * Wraps a Task List Table with filter criteria. This is the UI for the Tasks
 * sub-tab under the main Tasks tab
 */
public class TaskFilterPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** The case sensitive box. */
	private JCheckBox caseSensitiveBox = null;

	/** The filter string. */
	private JTextField filterString;

	/** The project selector. */
	private JComboBox projectSelector = null;

	/** The status selector. */
	private JComboBox statusSelector = null;

	/** The task list. */
	private TaskListPanel taskList = null;

	/**
	 * Instantiates a new task filter panel.
	 */
	public TaskFilterPanel() {
		super();

		try {
			initComponents();

		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
		
	}

	/**
	 * initialize the UI
	 * @throws Exception
	 */
	private void initComponents() throws Exception {

		JButton filterButton = new JButton();
		filterString = new javax.swing.JTextField();

		this.setLayout(new GridBagLayout());

		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(java.awt.FlowLayout.LEFT);

		JPanel comboBoxPanel = new JPanel();
		comboBoxPanel.setLayout(flowLayout);

		JLabel statusLabel = new JLabel();
		statusLabel.setText(Resource.getResourceString("Status") + ":");
		comboBoxPanel.add(statusLabel, null);

		statusSelector = new JComboBox();
		setStatuses();
		statusSelector
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(
							java.awt.event.ActionEvent evt) {
						refresh();
					}
				});
		comboBoxPanel.add(statusSelector, null);

		JLabel projectLabel = new JLabel(Resource.getResourceString("project")
				+ ":");
		JLabel spacer = new JLabel("           ");
		comboBoxPanel.add(spacer, null);
		comboBoxPanel.add(projectLabel, null);
		
		projectSelector = new JComboBox();
		loadProjectBox();
		projectSelector
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(
							java.awt.event.ActionEvent evt) {
						refresh();
					}
				});
		comboBoxPanel.add(projectSelector);

		GridBagConstraints gridBagConstraints15 = GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.HORIZONTAL);
		gridBagConstraints15.gridwidth = 3;
		this.add(comboBoxPanel, gridBagConstraints15);

		filterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Find16.gif")));
		ResourceHelper.setText(filterButton, "Filter:");
		filterButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				refresh();

			}
		});
		this.add(filterButton, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.HORIZONTAL));
		this.add(filterString, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		caseSensitiveBox = new JCheckBox();
		caseSensitiveBox.setText(Resource
				.getResourceString("case_sensitive"));
		this.add(caseSensitiveBox, GridBagConstraintsFactory.create(2, 1));

		taskList = new TaskListPanel();
		JScrollPane taskListScroll = new JScrollPane();
		taskListScroll.setViewportView(taskList);
		GridBagConstraints gridBagConstraints11 = GridBagConstraintsFactory
				.create(0, 2, GridBagConstraints.BOTH, 1.0, 1.0);
		gridBagConstraints11.gridwidth = 3;
		this.add(taskListScroll, gridBagConstraints11);

		refresh();

	}

	/**
	 * Load the project box with the list of projects and an entry to match All projects
	 * 
	 * @throws Exception the exception
	 */
	private void loadProjectBox() throws Exception {
		projectSelector.removeAllItems();
		projectSelector.addItem(Resource.getResourceString("All"));
		try {
			Collection<Project> projects = TaskModel.getReference()
					.getProjects();
			for(Project p : projects) {
				projectSelector.addItem(TaskView.getProjectString(p));
			}
		}
		catch (Exception e) {
		  // empty
		}
	}

	/**
	 * Prints the task list
	 */
	public void print() {

		// print the current table of tasks
		try {
			taskList.print();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}


	/**
	 * Update the task list based on changes to filter criteria
	 */
	public void refresh() {

		// reload project filter
		Object o = projectSelector.getSelectedItem();
		try {
			loadProjectBox();
		} catch (Exception e1) {
			Errmsg.errmsg(e1);
			return;
		}
		if (o != null)
			projectSelector.setSelectedItem(o);

		// get any filter string the user has typed
		String filt = filterString.getText();

		String statfilt = (String) statusSelector.getSelectedItem();

		String projfilt = (String) projectSelector.getSelectedItem();

		// pass the filter criteria to the task list. It has the filtering
		// capabilities. This panel and the task list used to be one class
		// and when they were split, the filtering was needed in the task list
		// class for other reasons
		taskList.setFilterCriteria(projfilt, statfilt, filt, caseSensitiveBox
				.isSelected());
		taskList.refresh();

	}

	/**
	 * load the status combo box
	 * 
	 */
	private void setStatuses() {

		// add some wildcard-like statuses
		statusSelector.addItem(Resource.getResourceString("All_Open"));
		statusSelector.addItem(Resource.getResourceString("All"));
		
		// get all possible statuses
		TaskTypes t = TaskModel.getReference().getTaskTypes();
		TreeSet<String> statusSet = new TreeSet<String>();
		Vector<String> types = t.getTaskTypes();
		for(String taskType : types ) {
			// create a list of statuses in a Set to remove duplicates
			Collection<String> states = t.getStates(taskType);
			for( String status : states ) {
				statusSet.add(status);
			}
		}
		for(String status : statusSet) {
			statusSelector.addItem(status);
		}
	}

	/**
	 * Set the project filter and show the tasks for a particular project
	 * 
	 * @param p the project
	 */
	public void showTasksForProject(Project p) {

		statusSelector.setSelectedIndex(0);
		String ps = TaskView.getProjectString(p);
		projectSelector.setSelectedItem(ps);
		refresh();

	}
}
