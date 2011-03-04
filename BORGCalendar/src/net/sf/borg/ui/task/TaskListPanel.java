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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.TaskTypes;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TablePrinter;
import net.sf.borg.ui.util.TableSorter;

/**
 * Shows a table of tasks action buttons.
 */
class TaskListPanel extends JPanel implements Model.Listener {

	private static final long serialVersionUID = 1L;

	/**
	 * renderer for the priority and days left columns in the task table that
	 * shows them in color.
	 */
	private class DLRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		/**
		 * constructor.
		 */
		public DLRenderer() {
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

			// start with default component that would be rendered
			JLabel l = (JLabel) defaultTableCellRenderer
					.getTableCellRendererComponent(table, obj, isSelected,
							hasFocus, row, column);

			// if null object - show dashes
			if (obj == null) {
				l.setText("--");
				l.setHorizontalAlignment(CENTER);
				return l;
			}

			// return the default rendered component if this is not the priority
			// or days left column
			String nm = table.getColumnName(column);
			if (!nm.equals(Resource.getResourceString("Pri"))
					&& !nm.equals(Resource.getResourceString("Days_Left")))
				return l;

			// return the default rendered component if the row is selected
			if (isSelected)
				return l;

			this.setText(l.getText());
			this.setHorizontalAlignment(CENTER);
			this.setBackground(l.getBackground());
			this.setForeground(l.getForeground());

			int i = ((Integer) obj).intValue();

			// set the priority background color
			if (nm.equals(Resource.getResourceString("Pri"))) {

				if (i == 1) {
					this.setBackground(new Color(255, 120, 120));
				} else if (i == 2) {
					this.setBackground(new Color(255, 200, 120));
				} else if (i == 3) {
					this.setBackground(new Color(255, 255, 175));
				} else if (i == 4) {
					this.setBackground(new Color(220, 220, 255));
				} else if (i == 5) {
					this.setBackground(new Color(200, 255, 175));
				}
				return this;
			}

			// yellow alert -- <10 days left
			if (i < 10)
				this.setBackground(new Color(255, 255, 175));

			if (i < 5)
				this.setBackground(new Color(255, 200, 120));

			// red alert -- <2 days left
			if (i < 2) {
				this.setBackground(new Color(255, 120, 120));
			}

			return this;
		}
	}

	/** The button panel. */
	private JPanel buttonPanel = null;

	/** checkbox to filter closed tasks */
	private JCheckBox showClosedTasksCheckBox = null;
	
	/** checkbox to show subtasks */
	private JCheckBox showSubTasksBox = null;

	private JLabel totalLabel;
	private JLabel getTotalLabel() {
		if (totalLabel == null) {
			totalLabel = new JLabel();
		}
		return totalLabel;
	}

	/** The default table cell renderer. */
	private TableCellRenderer defaultTableCellRenderer;

	/** The filter string. */
	private String filterString = "";

	/** The filter case sensitive flag. */
	private boolean filterCaseSensitive = false;

	// filtering criteria
	/** The project name. */
	private String projectName = Resource.getResourceString("All");

	/** The task status. */
	private String taskStatus = Resource.getResourceString("All");

	/** The task table. */
	private StripedTable taskTable;

	/**
	 * constructor
	 */
	public TaskListPanel() {
		super();
		TaskModel.getReference().addListener(this);
		try {
			initComponents();
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}

	}

	/**
	 * Instantiates a new task list panel tied to a particular project
	 * 
	 * @param projectName
	 *            the project name
	 */
	public TaskListPanel(String projectName) {
		super();
		setFilterCriteria(projectName, Resource.getResourceString("All"), "",
				false);
		TaskModel.getReference().addListener(this);
		try {
			initComponents();
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
		
	}

	/**
	 * get the selected task id
	 * 
	 * @return the select task id or null
	 */
	private Integer getSelectedTaskId() {
		// get task number from column 0 of selected row
		int row = taskTable.getSelectedRow();
		if (row == -1)
			return null;
		TableSorter tm = (TableSorter) taskTable.getModel();
		Integer num = (Integer) tm.getValueAt(row, 0);
		return num;
	}

	/**
	 * close the selected task
	 * 
	 * */
	private void closeActionPerformed() {

		Integer num = getSelectedTaskId();
		if (num == null)
			return;
		try {
			TaskModel.getReference().close(num.intValue());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * delete the selected task
	 * 
	 */
	private void deleteActionPerformed() {

		Integer num = getSelectedTaskId();
		if (num == null)
			return;

		// confirm delete
		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Really_delete_number_")
				+ num, "", JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			// delete the task
			try {
				TaskModel.getReference().delete(num.intValue());
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

	}

	/**
	 *create the button panel
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
					task_add();
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
							Integer num = getSelectedTaskId();
							if (num != null)
								task_change(num.intValue());
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
					closeActionPerformed();
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
					Integer num = getSelectedTaskId();
					if (num != null)
						task_clone(num.intValue());
				}
			});
			buttonPanel.add(clonebutton1, null);
			
			addSubTaskFilter();

		}
		return buttonPanel;
	}
	
	/**
	 * add the closed tasks checkbox. would not be called when an external mechanism 
	 * is filtering by status in some other way
	 */
	public void addClosedTaskFilter()
	{
		showClosedTasksCheckBox = new JCheckBox();
		showClosedTasksCheckBox.setText(Resource
				.getResourceString("show_closed_tasks"));
		showClosedTasksCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refresh();
			}

		});
		buttonPanel.add(showClosedTasksCheckBox);
		refresh();
	}
	
	private void addSubTaskFilter()
	{
		showSubTasksBox = new JCheckBox();
		showSubTasksBox.setText(Resource
				.getResourceString("show_subtasks"));
		showSubTasksBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refresh();
			}

		});
		buttonPanel.add(showSubTasksBox);
		refresh();
	}

	/**
	 * initalize the UI
	 * 
	 * @throws Exception
	 */
	private void initComponents() throws Exception {

		this.setLayout(new GridBagLayout());

		/*
		 * task table
		 */
		taskTable = new StripedTable();
		defaultTableCellRenderer = taskTable.getDefaultRenderer(Integer.class);

		// set renderer to the custom one for integers
		taskTable.setDefaultRenderer(Integer.class,
				new TaskListPanel.DLRenderer());

		// use a sorted table model
		taskTable.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Item_#"),
				Resource.getResourceString("Status"),
				Resource.getResourceString("Type"),
				Resource.getResourceString("Pri"),
				Resource.getResourceString("Days_Left"),
				Resource.getResourceString("Description"),
				Resource.getResourceString("Start_Date"),
				Resource.getResourceString("Due_Date"),
				Resource.getResourceString("duration"),
				Resource.getResourceString("elapsed_time"),
				Resource.getResourceString("project"),
				Resource.getResourceString("Category") }, new Class[] {
				Integer.class, String.class, String.class, Integer.class,
				Integer.class, String.class, Date.class, Date.class,
				Integer.class, Integer.class, String.class, String.class }));

		// set up for sorting when a column header is clicked
		TableSorter tm = (TableSorter) taskTable.getModel();
		tm.addMouseListenerToHeaderInTable(taskTable);

		// clear all rows
		tm.setRowCount(0);
		tm.tableChanged(new TableModelEvent(tm));

		JScrollPane taskScroll = new JScrollPane();
		taskScroll.setViewportView(taskTable);
		taskScroll.setBorder(javax.swing.BorderFactory
				.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
		taskTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
		taskTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				// on double click - open task for edit
				if (evt.getClickCount() < 2)
					return;
				Integer num = getSelectedTaskId();
				if (num != null)
					task_change(num.intValue());
			}
		});
		taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// set column widths
		taskTable.getColumnModel().getColumn(7).setPreferredWidth(100);
		taskTable.getColumnModel().getColumn(6).setPreferredWidth(100);
		taskTable.getColumnModel().getColumn(5).setPreferredWidth(400);
		taskTable.setPreferredScrollableViewportSize(new Dimension(800, 200));

		/*
		 * popup menu for task table
		 */
		new PopupMenuHelper(taskTable, new PopupMenuHelper.Entry[] {
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						task_add();
					}
				}, "Add"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						Integer num = getSelectedTaskId();
						if (num != null)
							task_change(num.intValue());
					}
				}, "Change"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						Integer num = getSelectedTaskId();
						if (num != null)
							task_clone(num.intValue());
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
						closeActionPerformed();
					}
				}, "Close") });

		this.add(taskScroll, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		/*
		 * add button panel
		 */
		JPanel midPanel = new JPanel(new BorderLayout());
		midPanel.add(getTotalLabel(), BorderLayout.WEST);
		midPanel.add(getButtonPanel(), BorderLayout.CENTER);
		this.add(midPanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH));

		refresh();

	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * Prints the task table
	 */
	public void print() {

		try {
			TablePrinter.printTable(taskTable);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * reload from the model and re-apply filter criteria
	 */
	public void refresh() {


		// clear all table rows
		TableSorter tm = (TableSorter) taskTable.getModel();
		tm.setRowCount(0);
		tm.tableChanged(new TableModelEvent(tm));

		// get project id to filter on
		Integer projfiltid = null;
		if (!projectName.equals(Resource.getResourceString("All"))) {
			try {
				projfiltid = TaskView.getProjectId(projectName);
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}
		}

		try {
			// loop through all tasks
			TaskTypes tasktypes = TaskModel.getReference().getTaskTypes();
			Collection<Task> tasks = TaskModel.getReference().getTasks();
			int totalItems = 0;
			for (Task task : tasks) {

				// filter by task state
				String st = task.getState();
				if (taskStatus.equals(Resource.getResourceString("All_Open"))) {
					if (TaskModel.isClosed(task)) {
						continue;
					}
				} else if (!taskStatus
						.equals(Resource.getResourceString("All"))
						&& !taskStatus.equals(st))
					continue;
				
				if( showClosedTasksCheckBox != null && !showClosedTasksCheckBox.isSelected() && TaskModel.isClosed(task))
						continue;
				
				// filter by project
				Integer pid = task.getProject();
				if (projfiltid != null) {
					if (pid == null || pid.intValue() != projfiltid.intValue())
						continue;
				}

				// filter by category
				if (!CategoryModel.getReference().isShown(task.getCategory()))
					continue;

				// filter on user filter string
				if (filterString.length() != 0) {

					// check if string is in description
					// or resolution
					String d = task.getDescription();
					String r = task.getResolution();

					if (r == null)
						r = "";
					if (d == null)
						d = "";

					if (filterCaseSensitive) {
						if (d.indexOf(filterString) == -1
								&& r.indexOf(filterString) == -1)
							continue;
					} else {
						String lfilt = filterString.toLowerCase();
						String ld = d.toLowerCase();
						String lr = r.toLowerCase();
						if (ld.indexOf(lfilt) == -1 && lr.indexOf(lfilt) == -1)
							continue;
					}

				}

				// if we get here - we are displaying this task as a row
				// so fill in an array of objects for the row
				Object[] ro = new Object[12];
				ro[0] = new Integer(task.getKey());
				ro[1] = task.getState();
				ro[2] = task.getType();
				ro[11] = task.getCategory();
				ro[3] = task.getPriority();
				ro[6] = task.getStartDate();
				ro[7] = task.getDueDate();

				// duration
				if (task.getDueDate() != null) {
					ro[8] = new Integer(TaskModel.daysBetween(task
							.getStartDate(), task.getDueDate()));
				} else {
					ro[8] = null;
				}

				Date end = null;
				if (task.getState().equals(
						tasktypes.getFinalState(task.getType()))) {
					end = task.getCompletionDate();
				} else {
					end = new Date();
				}

				// elapsed time
				if (end == null) {
					ro[9] = null;
				} else {
					ro[9] = new Integer(TaskModel.daysBetween(task
							.getStartDate(), end));
				}

				// calculate days left - today - duedate
				if (ro[7] == null)
					ro[4] = null;
				else {
					Date dd = (Date) ro[7];
					ro[4] = new Integer(TaskModel.daysLeft(dd));
				}

				// strip newlines from the description
				String de = task.getDescription();
				String tmp = "";
				for (int i = 0; de != null && i < de.length(); i++) {
					char c = de.charAt(i);
					if (c == '\n' || c == '\r') {
						tmp += ' ';
						continue;
					}

					tmp += c;
				}
				ro[5] = tmp;

				// project
				String ps = "";
				if (pid != null) {
					Project p = TaskModel.getReference().getProject(
							pid.intValue());
					if (p != null) {
						String tt = p.getDescription();

						for (int i = 0; tt != null && i < tt.length(); i++) {
							char c = tt.charAt(i);
							if (c == '\n' || c == '\r') {
								ps += ' ';
								continue;
							}

							ps += c;
						}
					}
				}
				ro[10] = ps;

				// add the task row to table
				tm.addRow(ro);

				totalItems ++;
				
				// add subtasks if requested
				if( showSubTasksBox.isSelected())
				{
					Collection<Subtask> subtasks = TaskModel.getReference().getSubTasks(task.getKey());
					for( Subtask subtask : subtasks )
					{
						ro = new Object[12];
						ro[0] = null;
						ro[1] = (subtask.getCloseDate() == null) ? Resource.getResourceString("OPEN"): Resource.getResourceString("CLOSED");
						ro[2] = task.getType();
						ro[11] = task.getCategory();
						ro[3] = task.getPriority();
						ro[6] = subtask.getStartDate();
						ro[7] = subtask.getDueDate();
						
						if (subtask.getDueDate() != null) {
							ro[8] = new Integer(TaskModel.daysBetween(subtask
									.getStartDate(), subtask.getDueDate()));
						} else {
							ro[8] = null;
						}

						end = null;
						if (subtask.getCloseDate() != null){
							end = subtask.getCloseDate();
						} else {
							end = new Date();
						}

						// elapsed time
						if (end == null) {
							ro[9] = null;
						} else {
							ro[9] = new Integer(TaskModel.daysBetween(subtask
									.getStartDate(), end));
						}

						// calculate days left - today - duedate
						if (ro[7] == null)
							ro[4] = null;
						else {
							Date dd = (Date) ro[7];
							ro[4] = new Integer(TaskModel.daysLeft(dd));
						}

						ro[5] = subtask.getDescription();
						
						ro[10] = ps;
						
						tm.addRow(ro);

					}
				}
				tm.tableChanged(new TableModelEvent(tm));
			}
			getTotalLabel().setText(totalItems + " items");
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// apply default sort to the table if not sorted by user
		if (!tm.isSorted())
			tm.sortByColumn(6); // days left
		else
			tm.sort();
	}

	/**
	 * Sets the filter criteria.
	 * 
	 * @param projectName
	 *            the project name
	 * @param status
	 *            the status
	 * @param filter
	 *            the filter string
	 * @param caseSensitive
	 *            the case sensitive match flag
	 */
	public void setFilterCriteria(String projectName, String status,
			String filter, boolean caseSensitive) {
		this.projectName = projectName;
		this.taskStatus = status;
		this.filterString = filter;
		this.filterCaseSensitive = caseSensitive;

	}

	/**
	 * open the task editor in add mode.
	 */
	private void task_add() {
		try {
			// fill in project if we have one
			Integer projfiltid = null;
			if (!projectName.equals(Resource.getResourceString("All"))) {
				try {
					projfiltid = TaskView.getProjectId(projectName);
				} catch (Exception e) {
					Errmsg.errmsg(e);
					return;
				}
			}

			new TaskView(null, TaskView.Action.ADD, projfiltid).showView();

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * open the task editor to edit a task.
	 * 
	 * @param tasknum
	 *            the tasknum
	 */
	static private void task_change(int tasknum) {

		try {
			// get the task from the data model
			Task task = TaskModel.getReference().getTask(tasknum);
			if (task == null)
				return;

			// display the task editor
			new TaskView(task, TaskView.Action.CHANGE, null).showView();

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * close a task and open the editor on the clone
	 * 
	 * @param tasknum
	 *            the tasknum
	 */
	static private void task_clone(int tasknum) {

		try {
			// get the task
			Task task = TaskModel.getReference().getTask(tasknum);
			if (task == null)
				return;

			// display the task editor
			new TaskView(task, TaskView.Action.CLONE, null).showView();

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}
}
