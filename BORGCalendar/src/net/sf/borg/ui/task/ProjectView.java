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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.link.LinkPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import com.toedter.calendar.JDateChooser;

/**
 * UI for viewing and editing a single project
 */
public class ProjectView extends DockableView {

	private static final long serialVersionUID = 1L;

	/**
	 * Action that we are invoking this editor for
	 */
	public enum Action {

		/** add new project */
		ADD,
		/** edit existing project */
		CHANGE,
		/** close existing project and edit as new */
		CLONE;
	}

	/**
	 * Gets the project id from the text in the parent project combo box
	 * 
	 * @param s
	 *            the string from the parent combo box
	 * 
	 * @return the project id
	 * 
	 * @throws Exception
	 * 
	 */
	static private Integer getProjectId(String s) throws Exception {
		// project id is the number before the colon
		int i = s.indexOf(":");
		if (i == -1)
			throw new Exception("Cannot parse project label");
		String ss = s.substring(0, i);

		int pid = Integer.parseInt(ss);
		return new Integer(pid);

	}

	/**
	 * creates a string to show in the parent project combo box that contains
	 * project id and name
	 * 
	 * @param p
	 *            the project
	 * 
	 * @return the project string
	 */
	static private String getProjectString(Project p) {
		return p.getKey() + ":" + p.getDescription();
	}


	/** The category box. */
	private JComboBox categoryBox = null;

	/** The days left text. */
	private JTextField daysLeftText = null;

	/** The description. */
	private JTextField description;

	/** The due date chooser. */
	private JDateChooser dueDateChooser;

	/** The project id text. */
	private JTextField projectIdText;

	/** The menu. */
	private JMenu menu;

	/** The menu bar. */
	private JMenuBar menuBar;

	/** The save menu item. */
	private JMenuItem saveMenuItem;

	/** The link panel. */
	private LinkPanel linkPanel = new LinkPanel();

	/** The open task count. */
	private JTextField openTaskCount = null;

	/** The parent project combo box. */
	private JComboBox parentProjectComboBox = new JComboBox();

	/** The start date chooser. */
	private JDateChooser startDateChooser;

	/** The status combo box. */
	private JComboBox statusComboBox;

	/** The task border. */
	private JPanel taskBorder = null;

	/** The task panel. */
	private TaskListPanel taskPanel = null;

	/** The window title. */
	private String windowTitle = "";

	/** The total task count. */
	private JTextField totalTaskCount = null;

	/**
	 * constructor
	 * 
	 * @param p
	 *            the project
	 * @param function
	 *            the action being taken
	 * @param parentId
	 *            the parent project id, if any
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public ProjectView(Project p, Action function, Integer parentId) {
		super();

		// listen for task model changes
		addModel(TaskModel.getReference());

		initComponents(); // init the GUI widgets

		// load the categories
		try {
			Collection<String> cats = CategoryModel.getReference()
					.getCategories();
			Iterator<String> it = cats.iterator();
			while (it.hasNext()) {
				categoryBox.addItem(it.next());
			}
			categoryBox.setSelectedIndex(0);

			// show the project
			showProject(function, p, parentId);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.DockableView#getFrameTitle()
	 */
	@Override
	public String getFrameTitle() {
		return windowTitle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.DockableView#getMenuForFrame()
	 */
	@Override
	public JMenuBar getMenuForFrame() {
		ResourceHelper.setText(menu, "Menu");
		ResourceHelper.setText(saveMenuItem, "Save");
		saveMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				saveProject();
			}
		});

		menu.add(saveMenuItem);

		menuBar.add(menu);
		return menuBar;
	}

	/**
	 * Initialize the UI.
	 */
	private void initComponents()// GEN-BEGIN:initComponents
	{

		setLayout(new GridBagLayout());

		// undocked menu
		menuBar = new JMenuBar();
		menu = new JMenu();
		saveMenuItem = new JMenuItem();

		/*
		 * project info panel
		 */
		JPanel projectInfoPanel = new JPanel();
		projectInfoPanel.setLayout(new GridBagLayout());

		projectInfoPanel.setBorder(new TitledBorder(Resource
				.getResourceString("ProjectInformation")));

		projectIdText = new JTextField();
		projectIdText.setText("projectIdText");

		description = new JTextField();
		JLabel lblItemNum = new JLabel();
		lblItemNum.setText(Resource.getResourceString("Item_#"));

		dueDateChooser = new JDateChooser();
		JLabel lblStartDate = new JLabel();
		JLabel lblDueDate = new JLabel();
		statusComboBox = new JComboBox();
		JLabel catlabel = new JLabel();

		JLabel lblStatus = new JLabel();
		ResourceHelper.setText(lblStatus, "Status");
		lblStatus.setLabelFor(statusComboBox);

		startDateChooser = new JDateChooser();
		ResourceHelper.setText(lblStartDate, "Start_Date");
		lblStartDate.setLabelFor(startDateChooser);

		ResourceHelper.setText(lblDueDate, "Due_Date");
		lblDueDate.setLabelFor(dueDateChooser);

		ResourceHelper.setText(catlabel, "Category");
		categoryBox = new JComboBox();
		catlabel.setLabelFor(categoryBox);

		projectInfoPanel.add(lblStartDate, GridBagConstraintsFactory.create(3,
				1, GridBagConstraints.BOTH));
		projectInfoPanel.add(lblDueDate, GridBagConstraintsFactory.create(1, 4,
				GridBagConstraints.BOTH));
		projectInfoPanel.add(catlabel, GridBagConstraintsFactory.create(3, 0,
				GridBagConstraints.BOTH));
		projectInfoPanel.add(dueDateChooser, GridBagConstraintsFactory.create(
				2, 4, GridBagConstraints.BOTH, 1.0, 0.0));
		projectInfoPanel.add(startDateChooser, GridBagConstraintsFactory
				.create(4, 1, GridBagConstraints.BOTH, 1.0, 0.0));
		projectInfoPanel.add(lblItemNum, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH));
		projectInfoPanel.add(projectIdText, GridBagConstraintsFactory.create(2,
				0, GridBagConstraints.BOTH));
		projectInfoPanel.add(lblStatus, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH));
		projectInfoPanel.add(categoryBox, GridBagConstraintsFactory.create(4,
				0, GridBagConstraints.BOTH, 1.0, 0.0));
		projectInfoPanel.add(statusComboBox, GridBagConstraintsFactory.create(
				2, 1, GridBagConstraints.BOTH, 1.0, 0.0));

		daysLeftText = new JTextField();
		daysLeftText.setEditable(false);
		projectInfoPanel.add(daysLeftText, GridBagConstraintsFactory.create(4,
				4, GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel daysLeftLabel = new JLabel();
		daysLeftLabel.setText(Resource.getResourceString("Days_Left"));
		daysLeftLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		daysLeftLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		projectInfoPanel.add(daysLeftLabel, GridBagConstraintsFactory.create(3,
				4));
		projectInfoPanel.add(description, GridBagConstraintsFactory.create(2,
				5, GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel descLabel = new JLabel();
		descLabel.setText(Resource.getResourceString("Description"));
		projectInfoPanel.add(descLabel, GridBagConstraintsFactory.create(1, 5,
				GridBagConstraints.BOTH));

		JLabel totalLabel = new JLabel();
		totalLabel.setText(Resource.getResourceString("total_tasks"));
		projectInfoPanel
				.add(totalLabel, GridBagConstraintsFactory.create(3, 5));

		JLabel openLabel = new JLabel();
		openLabel.setText(Resource.getResourceString("open_tasks"));
		projectInfoPanel.add(openLabel, GridBagConstraintsFactory.create(3, 6));

		totalTaskCount = new JTextField();
		totalTaskCount.setEditable(false);
		projectInfoPanel.add(totalTaskCount, GridBagConstraintsFactory.create(
				4, 5, GridBagConstraints.BOTH, 1.0, 0.0));

		openTaskCount = new JTextField();
		openTaskCount.setEditable(false);
		projectInfoPanel.add(openTaskCount, GridBagConstraintsFactory.create(4,
				6, GridBagConstraints.BOTH, 1.0, 0.0));

		projectInfoPanel.add(parentProjectComboBox, GridBagConstraintsFactory
				.create(2, 6, GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel parentLabel = new JLabel(Resource.getResourceString("parent"));
		projectInfoPanel.add(parentLabel, GridBagConstraintsFactory.create(1,
				6, GridBagConstraints.BOTH));

		add(projectInfoPanel, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		/*
		 * button panel
		 */
		JPanel buttonPanel = new JPanel();
		JButton savebutton = new JButton();

		savebutton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(savebutton, "Save");
		savebutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				saveProject();

			}
		});
		buttonPanel.add(savebutton, savebutton.getName());

//		if (hasGantt()) {
//			JButton ganttbutton = new JButton();
//			ganttbutton.setText(Resource.getResourceString("GANTT"));
//			ganttbutton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					ganttActionPerformed();
//				}
//			});
//			buttonPanel.add(ganttbutton);
//		}
//
//		if (RunReport.hasJasper()) {
//			JButton projRptButton = new JButton();
//			ResourceHelper.setText(projRptButton, "Report");
//			projRptButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent evt) {
//					reportButtonActionPerformed();
//				}
//			});
//			buttonPanel.add(projRptButton);
//		}

		add(buttonPanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH));

		/*
		 * link panel
		 */

		linkPanel.setBorder(new TitledBorder(Resource
				.getResourceString("links")));
		add(linkPanel, GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.BOTH));

		/*
		 * panel that will contain the project's task list
		 */
		taskBorder = new JPanel();
		taskBorder.setBorder(new TitledBorder(Resource
				.getResourceString("tasks")));
		taskBorder.setLayout(new GridBagLayout());

		add(taskBorder, GridBagConstraintsFactory.create(0, 3,
				GridBagConstraints.BOTH, 1.0, 1.0));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.DockableView#refresh()
	 */
	@Override
	public void refresh() {
		// the task editor does not refresh itself when the task data
		// model changes
	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * Save project.
	 */
	private void saveProject() {

		// validate that description is present
		if (description.getText() == null || description.getText().equals("")) {
			Errmsg.notice(Resource.getResourceString("empty_desc"));
			return;
		}
		try {

			String num = projectIdText.getText();

			Project p = new Project();

			if (!num.equals("NEW") && !num.equals("CLONE")) {
				p.setKey(Integer.parseInt(num));
			}

			// fill in the fields from the screen
			Calendar cal = startDateChooser.getCalendar();
			if (cal == null)
				cal = new GregorianCalendar();
			p.setStartDate(cal.getTime()); // start date

			cal = dueDateChooser.getCalendar();
			if (cal != null) {
				p.setDueDate(cal.getTime()); // due date

				// validate due date
				if (DateUtil.isAfter(p.getStartDate(), p.getDueDate())) {
					throw new Warning(Resource.getResourceString("sd_dd_warn"));
				}
			}

			p.setDescription(description.getText());
			p.setStatus((String) statusComboBox.getSelectedItem());

			String cat = (String) categoryBox.getSelectedItem();
			if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
				p.setCategory(null);
			} else {
				p.setCategory(cat);
			}

			p.setParent(null);
			String proj = (String) parentProjectComboBox.getSelectedItem();
			try {
				p.setParent(getProjectId(proj));

			} catch (Exception e) {
				// no project selected
			}

			TaskModel.getReference().saveProject(p);
			p.setKey(p.getKey());

			showProject(Action.CHANGE, p, null);
		} catch (Warning w) {
			Errmsg.notice(w.getMessage());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * Show a project
	 * 
	 * @param function
	 *            the action being taken
	 * @param p
	 *            the project
	 * @param parentId
	 *            the parent project id
	 * 
	 * @throws Exception
	 */
	private void showProject(Action function, Project p, Integer parentId)
			throws Exception {

		// set the link panel to show this project's links
		linkPanel.setOwner(p);

		// show all possible parent projects
		parentProjectComboBox.removeAllItems();
		parentProjectComboBox.addItem("");
		Collection<Project> projects = TaskModel.getReference().getProjects();
		if (projects != null) {
			for (Project project : projects) {
				// add open projects that are not the current one
				if ((p == null || p.getKey() != project.getKey())
						&& project.getStatus().equals(
								Resource.getResourceString("OPEN")))
					parentProjectComboBox.addItem(getProjectString(project));
			}
		}

		// if we are showing an existing project - fill in the gui fields for it
		if (p != null) {
			// task number
			projectIdText.setText(Integer.toString(p.getKey()));
			projectIdText.setEditable(false);

			// window title - "Item N"
			windowTitle = Resource.getResourceString("Item_") + " "
					+ p.getKey();

			// due date
			GregorianCalendar gc = new GregorianCalendar();
			Date dd = p.getDueDate();
			if (dd != null) {
				gc.setTime(dd);
				dueDateChooser.setCalendar(gc);
			}

			GregorianCalendar gc2 = new GregorianCalendar();
			dd = p.getStartDate();
			if (dd != null)
				gc2.setTime(dd);
			startDateChooser.setCalendar(gc2);

			int daysleft = TaskModel.daysLeft(p.getDueDate());
			daysLeftText.setText(Integer.toString(daysleft));

			String cat = p.getCategory();
			if (cat != null && !cat.equals("")) {
				categoryBox.setSelectedItem(cat);
			} else {
				categoryBox.setSelectedIndex(0);
			}

			description.setText(p.getDescription());

			statusComboBox.setEditable(false);

			Collection<Task> ptasks = TaskModel.getReference().getTasks(
					p.getKey());
			totalTaskCount.setText(Integer.toString(ptasks.size()));

			int openTasks = 0;
			for (Task pt : ptasks) {
				if (!TaskModel.isClosed(pt)) {
					openTasks++;
				}
			}
			openTaskCount.setText(Integer.toString(openTasks));

			// set parent project
			Integer pid = p.getParent();
			if (pid != null) {
				Project par = TaskModel.getReference().getProject(
						pid.intValue());
				if (TaskModel.isClosed(par)) {
					// if parent closed - would not have been added before
					parentProjectComboBox.addItem(getProjectString(par));
				}
				parentProjectComboBox.setSelectedItem(getProjectString(par));

			}

			// add the task list
			if (taskPanel == null) {
				taskPanel = new TaskListPanel(TaskView.getProjectString(p));
				taskPanel.addClosedTaskFilter();
				taskBorder.add(taskPanel, GridBagConstraintsFactory.create(0,
						0, GridBagConstraints.BOTH, 1.0, 1.0));
			}

		} else {
			// set fields for a new project

			projectIdText.setText("NEW");
			projectIdText.setEditable(false);

			// title
			windowTitle = Resource.getResourceString("NEW_Item");
			statusComboBox.addItem(Resource.getResourceString("OPEN"));
			statusComboBox.setEnabled(false);
			categoryBox.setSelectedIndex(0);
			description.setText(""); // desc
			totalTaskCount.setText("");
			openTaskCount.setText("");

			// parent id may have been passed in
			if (parentId != null) {
				Project par = TaskModel.getReference().getProject(
						parentId.intValue());
				if (TaskModel.isClosed(par)) {
					parentProjectComboBox.addItem(getProjectString(par));
				}
				parentProjectComboBox.setSelectedItem(getProjectString(par));

				String cat = par.getCategory();
				if (cat != null && !cat.equals("")) {
					categoryBox.setSelectedItem(cat);
				} else {
					categoryBox.setSelectedIndex(0);
				}

				GregorianCalendar gc = new GregorianCalendar();
				Date dd = par.getDueDate();
				if (dd != null) {
					gc.setTime(dd);
					dueDateChooser.setCalendar(gc);
				}

				Date sd = par.getStartDate();
				if (sd != null) {
					gc.setTime(sd);
					startDateChooser.setCalendar(gc);
				}

			}

		}

		// can't change status on a new project
		if (p == null) {
			statusComboBox.setEnabled(false);
		}

		// cloning takes the fields filled in for an existing task and resets
		// only those
		// that don't apply to the clone
		if (function == Action.CLONE) {
			// need new task number
			projectIdText.setText("CLONE");
			projectIdText.setEditable(false);

			statusComboBox.removeAllItems();
			statusComboBox.addItem(Resource.getResourceString("OPEN"));
			statusComboBox.setEnabled(false);

		}
		// change existing task
		else if (function == Action.CHANGE) {

			String state = null;
			if (p != null)
				state = p.getStatus();

			// set next state pulldown - projects only move between open and
			// closed
			statusComboBox.removeAllItems();
			statusComboBox.addItem(Resource.getResourceString("OPEN"));
			statusComboBox.addItem(Resource.getResourceString("CLOSED"));
			statusComboBox.setSelectedItem(state);
			statusComboBox.setEnabled(true);

		}

	}
}
