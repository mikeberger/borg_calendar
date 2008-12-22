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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.TaskTypes;
import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.Task;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TablePrinter;
import net.sf.borg.ui.util.TableSorter;

/**
 * 
 * @author MBERGER
 * @version
 */

class TaskListPanel extends JPanel implements Model.Listener {

	// override class for the default table cell renderer that will change
	// the
	// colors of table cells
	// based on days left before due date
	private class DLRenderer extends JLabel implements TableCellRenderer {

		public DLRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			JLabel l = (JLabel) defrend_.getTableCellRendererComponent(table,
					obj, isSelected, hasFocus, row, column);

			if (obj == null) {
				l.setText("--");
				l.setHorizontalAlignment(CENTER);
				return l;
			}

			String nm = table.getColumnName(column);
			if (!nm.equals(Resource.getPlainResourceString("Pri"))
					&& !nm.equals(Resource.getPlainResourceString("Days_Left")))
				return l;

			if (isSelected
					&& !nm.equals(Resource.getPlainResourceString("Days_Left")))
				return l;

			this.setText(l.getText());
			this.setHorizontalAlignment(CENTER);
			this.setBackground(l.getBackground());
			this.setForeground(l.getForeground());

			int i = ((Integer) obj).intValue();

			// priority
			if (nm.equals(Resource.getPlainResourceString("Pri"))) {

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

			if (isSelected)
				return this;

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

	private JMenuItem add = new JMenuItem();
	private JButton addbutton = null;

	private JPanel buttonPanel = null;

	private JMenuItem change = new JMenuItem();

	private JButton changebutton1 = null;

	private JMenuItem clone = new JMenuItem();

	private JButton clonebutton1 = null;

	private JMenuItem close = new JMenuItem();

	private JButton closebutton1 = null;

	private TableCellRenderer defrend_;

	private JMenuItem delete = new JMenuItem();

	private JButton deletebutton1 = null;

	private String filter = "";
	private boolean filterCaseSensitive = false;

	// filtering criteria
	private String projectName = Resource.getPlainResourceString("All");

	private String status = Resource.getPlainResourceString("All");

	private StripedTable taskTable;

	/** Creates new form btgui */
	public TaskListPanel() {
		super();
		TaskModel.getReference().addListener(this);
		try {
			initComponents();
			taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Errmsg.errmsg(e);
			return;
		}

	}

	public TaskListPanel(String projectName, String status, String filter,
			boolean caseSensitive) {
		super();
		setFilterCriteria(projectName, status, filter, caseSensitive);
		TaskModel.getReference().addListener(this);
		try {
			initComponents();
			taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Errmsg.errmsg(e);
			return;
		}

	}

	public void setFilterCriteria(String projectName, String status,
			String filter, boolean caseSensitive) {
		this.projectName = projectName;
		this.status = status;
		this.filter = filter;
		this.filterCaseSensitive = caseSensitive;

	}

	private void addActionPerformed(java.awt.event.ActionEvent evt) {
		// ask controller to bring up new task editor

		task_add();
	}

	// add a row to the sorted table
	private void addRow(JTable t, Object[] ro) {
		TableSorter tm = (TableSorter) t.getModel();
		tm.addRow(ro);
		tm.tableChanged(new TableModelEvent(tm));
	}

	private void changeActionPerformed(java.awt.event.ActionEvent evt) {

		// get task number from column 0 of selected row
		int row = taskTable.getSelectedRow();
		if (row == -1)
			return;
		TableSorter tm = (TableSorter) taskTable.getModel();
		Integer num = (Integer) tm.getValueAt(row, 0);

		// ask borg class to bring up a task editor window
		task_change(num.intValue());

	}

	private void cloneActionPerformed(java.awt.event.ActionEvent evt) {

		// get task number from column 0 of selected row
		int row = taskTable.getSelectedRow();
		if (row == -1)
			return;
		TableSorter tm = (TableSorter) taskTable.getModel();
		Integer num = (Integer) tm.getValueAt(row, 0);

		// ask borg class to bring up a task editor window
		task_clone(num.intValue());

	}

	private void closeActionPerformed(java.awt.event.ActionEvent evt) {

		// get the task number from column 0 of the selected row
		int row = taskTable.getSelectedRow();
		if (row == -1)
			return;
		TableSorter tm = (TableSorter) taskTable.getModel();
		Integer num = (Integer) tm.getValueAt(row, 0);
		try {
			TaskModel.getReference().close(num.intValue());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	// do the default sort - by days left - column 5
	private void defsort() {
		TableSorter tm = (TableSorter) taskTable.getModel();
		if (!tm.isSorted())
			tm.sortByColumn(6);
		else
			tm.sort();
	}

	private void deleteActionPerformed(java.awt.event.ActionEvent evt) {

		// delete selected row

		// get task number from column 0 of the selected row
		int row = taskTable.getSelectedRow();
		if (row == -1)
			return;
		TableSorter tm = (TableSorter) taskTable.getModel();
		Integer num = (Integer) tm.getValueAt(row, 0);

		// prompt for ok
		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Really_delete_number_")
				+ num, "", JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			// delete the task
			try {
				TaskModel taskmod_ = TaskModel.getReference();
				taskmod_.delete(num.intValue());
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

	}

	// delete all rows from the sorted table
	private void deleteAll() {
		TableSorter tm = (TableSorter) taskTable.getModel();
		tm.setRowCount(0);
		tm.tableChanged(new TableModelEvent(tm));
	}

	/**
	 * This method initializes addbutton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getAddbutton() {
		if (addbutton == null) {
			addbutton = new JButton();
			addbutton.setText(Resource.getPlainResourceString("Add"));
			addbutton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Add16.gif")));
			addbutton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					task_add();
				}
			});
		}
		return addbutton;
	}

	private ActionListener getAL(JMenuItem mnuitm) {
		return mnuitm.getActionListeners()[0];
	}

	/**
	 * This method initializes buttonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());
			buttonPanel.add(getAddbutton(), null);
			buttonPanel.add(getChangebutton1(), null);
			buttonPanel.add(getDeletebutton1(), null);
			buttonPanel.add(getClosebutton1(), null);
			buttonPanel.add(getClonebutton1(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes changebutton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getChangebutton1() {
		if (changebutton1 == null) {
			changebutton1 = new JButton();
			changebutton1.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Edit16.gif")));
			changebutton1.setText(Resource.getPlainResourceString("Change"));
			changebutton1
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							changeActionPerformed(e);
						}
					});
		}
		return changebutton1;
	}

	/**
	 * This method initializes clonebutton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getClonebutton1() {
		if (clonebutton1 == null) {
			clonebutton1 = new JButton();
			clonebutton1.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Copy16.gif")));
			clonebutton1.setText(Resource.getPlainResourceString("Clone"));
			clonebutton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cloneActionPerformed(e);
				}
			});
		}
		return clonebutton1;
	}

	/**
	 * This method initializes closebutton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getClosebutton1() {
		if (closebutton1 == null) {
			closebutton1 = new JButton();
			closebutton1.setIcon(new ImageIcon(getClass().getResource(
					"/resource/greenlight.gif")));
			closebutton1.setText(Resource.getPlainResourceString("Close"));
			closebutton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					closeActionPerformed(e);
				}
			});
		}
		return closebutton1;
	}

	/**
	 * This method initializes deletebutton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getDeletebutton1() {
		if (deletebutton1 == null) {
			deletebutton1 = new JButton();
			deletebutton1.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Delete16.gif")));
			deletebutton1.setText(Resource.getPlainResourceString("Delete"));
			deletebutton1
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							deleteActionPerformed(e);
						}
					});
		}
		return deletebutton1;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the FormEditor.
	 */

	private void initComponents() throws Exception {

		initMenuBar();

		this.setLayout(new GridBagLayout());
		change.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Edit16.gif")));
		add
				.setIcon(new ImageIcon(getClass().getResource(
						"/resource/Add16.gif")));
	
		taskTable = new StripedTable();

		defrend_ = taskTable.getDefaultRenderer(Integer.class);

		// set renderer to the custom one for integers
		taskTable.setDefaultRenderer(Integer.class,
				new TaskListPanel.DLRenderer());

		// use a sorted table model
		taskTable.setModel(new TableSorter(new String[] {
				Resource.getPlainResourceString("Item_#"),
				Resource.getPlainResourceString("Status"),
				Resource.getPlainResourceString("Type"),
				Resource.getPlainResourceString("Pri"),
				Resource.getPlainResourceString("Days_Left"),
				Resource.getPlainResourceString("Description"),
				Resource.getPlainResourceString("Start_Date"),
				Resource.getPlainResourceString("Due_Date"),
				Resource.getPlainResourceString("duration"),
				Resource.getPlainResourceString("elapsed_time"),
				Resource.getPlainResourceString("project"),
				Resource.getPlainResourceString("Category")}, new Class[] {
				Integer.class, 
				String.class,
				String.class, 
				Integer.class,
				Integer.class,
				String.class,
				Date.class, 
				Date.class, 
				Integer.class,
				Integer.class, 
				String.class, 
				String.class }));

		// set up for sorting when a column header is clicked
		TableSorter tm = (TableSorter) taskTable.getModel();
		tm.addMouseListenerToHeaderInTable(taskTable);

		// clear all rows
		deleteAll();

		JScrollPane jScrollPane1 = new JScrollPane();
		jScrollPane1.setViewportView(taskTable);
		jScrollPane1.setBorder(javax.swing.BorderFactory
				.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
		taskTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

		taskTable.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				mouseClick(evt);
			}
		});

		new PopupMenuHelper(taskTable, new PopupMenuHelper.Entry[] {
				new PopupMenuHelper.Entry(getAL(add), "Add"),
				new PopupMenuHelper.Entry(getAL(change), "Change"),
				new PopupMenuHelper.Entry(getAL(clone), "Clone"),
				new PopupMenuHelper.Entry(getAL(delete), "Delete"),
				new PopupMenuHelper.Entry(getAL(close), "Close") });

		// set column widths
		taskTable.getColumnModel().getColumn(7).setPreferredWidth(100);
		taskTable.getColumnModel().getColumn(6).setPreferredWidth(100);
		taskTable.getColumnModel().getColumn(5).setPreferredWidth(400);
		taskTable.setPreferredScrollableViewportSize(new Dimension(800,200));

		this.add(jScrollPane1, GridBagConstraintsFactory.create(0,0,GridBagConstraints.BOTH,1.0,1.0));
		this.add(getButtonPanel(), GridBagConstraintsFactory.create(0,1,GridBagConstraints.BOTH));

		refresh();

	}

	private void initMenuBar() {

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu();

		JMenu editMenu = new JMenu();

		ResourceHelper.setText(fileMenu, "File");

		menuBar.add(fileMenu);

		menuBar.add(editMenu);
		ResourceHelper.setText(editMenu, "Action");
		ResourceHelper.setText(add, "Add");
		add.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addActionPerformed(evt);
			}
		});

		ResourceHelper.setText(change, "Change");
		change.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				changeActionPerformed(evt);
			}
		});

		ResourceHelper.setText(clone, "Clone");
		clone.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cloneActionPerformed(evt);
			}
		});

		editMenu.add(clone);

		ResourceHelper.setText(delete, "Delete");
		delete.setName("delete");
		delete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteActionPerformed(evt);
			}
		});

		editMenu.add(delete);

		ResourceHelper.setText(close, "Close");
		close.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				closeActionPerformed(evt);
			}
		});

		editMenu.add(close);

	}

	private void mouseClick(java.awt.event.MouseEvent evt) {

		// ask controller to bring up task editor on double click
		if (evt.getClickCount() < 2)
			return;

		// changeActionPerformed(null);
		showChildren();
	}

	public void print() {

		// print the current table of tasks
		try {
			TablePrinter.printTable(taskTable);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	// refresh is called to update the table of shown tasks due to model
	// changes
	// or if the user
	// changes the filtering criteria
	public void refresh() {

		int row = 0;

		// clear all table rows
		deleteAll();

		Integer projfiltid = null;
		if (!projectName.equals(Resource.getPlainResourceString("All"))) {
			try {
				projfiltid = TaskView.getProjectId(projectName);
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}
		}

		try {
			TaskModel taskmod_ = TaskModel.getReference();
			TaskTypes tasktypes = taskmod_.getTaskTypes();
			Collection<Task> tasks = taskmod_.getTasks();
			Iterator<Task> ti = tasks.iterator();
			while (ti.hasNext()) {

				Task task = ti.next();

				// get the task state
				String st = task.getState();

				if (status.equals(Resource.getPlainResourceString("All_Open"))) {
					if (TaskModel.isClosed(task)) {
						continue;
					}
				} else if (!status.equals(Resource
						.getPlainResourceString("All"))
						&& !status.equals(st))
					continue;

				Integer pid = task.getProject();
				if (projfiltid != null) {
					if (pid == null || pid.intValue() != projfiltid.intValue())
						continue;
				}

				// category
				if (!CategoryModel.getReference().isShown(task.getCategory()))
					continue;

				// filter on user filter string
				if (filter.length() != 0) {

					// check if string is in description
					// or resolution
					String d = task.getDescription();
					String r = task.getResolution();

					if (r == null)
						r = "";
					if (d == null)
						d = "";

					if (filterCaseSensitive) {
						if (d.indexOf(filter) == -1 && r.indexOf(filter) == -1)
							continue;
					} else {
						String lfilt = filter.toLowerCase();
						String ld = d.toLowerCase();
						String lr = r.toLowerCase();
						if (ld.indexOf(lfilt) == -1 && lr.indexOf(lfilt) == -1)
							continue;
					}

				}

				// if we get here - we are displaying this task as a row
				// so fill in an array of objects for the row
				Object[] ro = new Object[12];
				ro[0] = task.getTaskNumber(); // task number
				ro[1] = task.getState(); // task state
				ro[2] = task.getType(); // task type
				ro[11] = task.getCategory();
				ro[3] = task.getPriority();
				ro[6] = task.getStartDate(); // task start date
				ro[7] = task.getDueDate(); // task due date

				if (task.getDueDate() != null) {
					ro[8] = new Integer(TaskModel.daysBetween(task
							.getStartDate(), task.getDueDate()));
				} else {
					ro[8] = null;
				}

				
				Date end = null;
				if (task.getState().equals(
						tasktypes.getFinalState(task.getType()))) {
					end = task.getCD();
				} else {
					end = new Date();
				}

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
				addRow(taskTable, ro);
				row++;
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// apply default sort to the table
		defsort();
	}

	public void remove() {
		// TODO Auto-generated method stub

	}

	private void showChildren() {

		// get task number from column 0 of selected row
		int row = taskTable.getSelectedRow();
		if (row == -1)
			return;
		TableSorter tm = (TableSorter) taskTable.getModel();
		Integer num = (Integer) tm.getValueAt(row, 0);

		// ask borg class to bring up a task editor window
		task_change(num.intValue());

	}

	// show task view - to add a new task
	private void task_add() {
		try {
			// display the task editor
			Integer projfiltid = null;
			if (!projectName.equals(Resource.getPlainResourceString("All"))) {
				try {
					projfiltid = TaskView.getProjectId(projectName);
				} catch (Exception e) {
					Errmsg.errmsg(e);
					return;
				}
			}
			MultiView.getMainView().addView(
					new TaskView(null, TaskView.T_ADD, projfiltid));

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	// show the task view - to edit a task
	private void task_change(int tasknum) {

		try {
			// get the task from the data model
			TaskModel taskmod_ = TaskModel.getReference();
			Task task = taskmod_.getTask(tasknum);
			if (task == null)
				return;

			// display the task editor
			MultiView.getMainView().addView(
					new TaskView(task, TaskView.T_CHANGE, null));

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	private void task_clone(int tasknum) {

		try {
			// get the task
			TaskModel taskmod_ = TaskModel.getReference();
			Task task = taskmod_.getTask(tasknum);
			if (task == null)
				return;

			// display the task editor
			MultiView.getMainView().addView(
					new TaskView(task, TaskView.T_CLONE, null));

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}
}
