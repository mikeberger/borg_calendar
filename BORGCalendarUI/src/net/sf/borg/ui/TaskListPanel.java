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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.ui.StripedTable;
import net.sf.borg.common.ui.TablePrinter;
import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Resource;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Project;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.TaskTypes;
import net.sf.borg.model.db.DBException;

/**
 * 
 * @author MBERGER
 * @version
 */

// task tracker main window
// this view shows a list of tasks in a table format with all kinds
// of sorting/filtering options. It is really like the "main" window
// for a whole task traking application separate from the calendar
// application. In prior non-java versions of BORG, the task tracker
// and calendar apps were completely separate apps.
public class TaskListPanel extends JPanel {

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

	    if (obj == null)
		return l;
	    
	    
	    String nm = table.getColumnName(column);
	    if( !nm.equals(Resource.getPlainResourceString("Pri")) && !nm.equals(Resource.getPlainResourceString("Days_Left")))
		return l;
	    //if (column != 4 && column != 8)
		//return l;
	    
	    if (isSelected && !nm.equals(Resource.getPlainResourceString("Days_Left")))
		return l;

	    this.setText(l.getText());
	    this.setHorizontalAlignment(CENTER);
	    this.setBackground(l.getBackground());
	    this.setForeground(l.getForeground());

	    int i = ((Integer) obj).intValue();
	    if (i == 9999)
		this.setText("******");

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

	    if( isSelected )
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

    private JCheckBox caseBox = null;

    private JMenuItem change = new JMenuItem();

    private JButton changebutton1 = null;

    private JMenuItem clone = new JMenuItem();

    private JButton clonebutton1 = null;

    private JMenuItem close = new JMenuItem();

    private JButton closebutton1 = null;

    private TableCellRenderer defrend_;

    private JMenuItem delete = new JMenuItem();

    private JButton deletebutton1 = null;

    /**
         * This method initializes jPanel2
         * 
         * @return javax.swing.JPanel
         */
    private JPanel jPanel2 = null;

    private javax.swing.JTextField jTextField3;

    private JComboBox statusBox = null;

    private JLabel statusLabel = null;

    private StripedTable taskTable;

    JComboBox projectBox = null;

    /** Creates new form btgui */
    public TaskListPanel() {
	super();

	try {
	    initComponents();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    Errmsg.errmsg(e);
	    return;
	}

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

	// reload project filter
	Object o = projectBox.getSelectedItem();
	try {
	    loadProjectBox();
	} catch (Exception e1) {
	    Errmsg.errmsg(e1);
	    return;
	}
	if (o != null)
	    projectBox.setSelectedItem(o);

	// clear all table rows
	deleteAll();

	// get any filter string the user has typed
	String filt = filter();

	String statfilt = (String) statusBox.getSelectedItem();

	String projfilt = (String) projectBox.getSelectedItem();
	Integer projfiltid = null;
	if (!projfilt.equals(Resource.getPlainResourceString("All"))) {
	    try {
		projfiltid = TaskView.getProjectId(projfilt);
	    } catch (Exception e) {
		Errmsg.errmsg(e);
		return;
	    }
	}

	try {
	    TaskModel taskmod_ = TaskModel.getReference();

	    Collection tasks = taskmod_.getTasks();
	    Iterator ti = tasks.iterator();
	    while (ti.hasNext()) {

		Task task = (Task) ti.next();

		// get the task state
		String st = task.getState();
		String type = task.getType();

		if (statfilt
			.equals(Resource.getPlainResourceString("All_Open"))) {
		    // System.out.println(type + " " +
		    // TaskModel.getReference().getTaskTypes().getFinalState(type));
		    if (st.equals(TaskModel.getReference().getTaskTypes()
			    .getFinalState(type))) {
			continue;
		    }
		} else if (!statfilt.equals(Resource
			.getPlainResourceString("All"))
			&& !statfilt.equals(st))
		    continue;

		Integer pid = task.getProject();
		if (projfiltid != null) {
		    if (pid == null || pid.intValue() != projfiltid.intValue())
			continue;
		}

		// category
		String cat = task.getCategory();
		if (cat == null || cat.equals(""))
		    cat = CategoryModel.UNCATEGORIZED;

		if (!CategoryModel.getReference().isShown(cat))
		    continue;

		// filter on user filter string
		if (filt.length() != 0) {

		    // check if string is in description
		    // or resolution
		    String d = task.getDescription();
		    String r = task.getResolution();

		    if (r == null)
			r = "";
		    if (d == null)
			d = "";

		    if (caseBox.isSelected()) {
			if (d.indexOf(filt) == -1 && r.indexOf(filt) == -1)
			    continue;
		    } else {
			String lfilt = filt.toLowerCase();
			String ld = d.toLowerCase();
			String lr = r.toLowerCase();
			if (ld.indexOf(lfilt) == -1 && lr.indexOf(lfilt) == -1)
			    continue;
		    }

		}

		// if we get here - we are displaying this task as a row
		// so fill in an array of objects for the row
		Object[] ro = new Object[11];
		ro[0] = task.getTaskNumber(); // task number
		ro[1] = task.getState(); // task state
		ro[2] = task.getType(); // task type
		ro[3] = task.getCategory();
		ro[4] = task.getPriority();
		ro[5] = task.getStartDate(); // task start date
		ro[6] = task.getDueDate(); // task due date

		// calc elapsed time
		Date end = null;
		if (task.getState().equals("CLOSED")) {
		    end = task.getCD();
		} else {
		    end = new Date();
		}

		if (end == null) {
		    ro[7] = "*******";
		} else {
		    // curently, the dates do not record h/m/s, so can't get
		    // too
		    // accurate
		    long msecs = end.getTime() - task.getStartDate().getTime();
		    long hours = msecs / (1000 * 60 * 60);
		    // long min = msecs / (1000 * 60);

		    int days = (int) (hours / 24);
		    // int hrs = (int) (hours % 24);
		    // int mins = (int) (min % 60);
		    if (days >= 1)
			ro[7] = new Integer(days); // Integer.toString(days)
		    // +
		    // "d";
		    else
			ro[7] = new Integer(0); // "<1d";
		}

		// calculate days left - today - duedate
		if (ro[6] == null)
		    // 9999 days left if no due date - this is a (cringe,
		    // ack,
		    // thptt) magic value
		    ro[8] = new Integer(9999);
		else {
		    Date dd = (Date) ro[6];
		    ro[8] = new Integer(TaskModel.daysLeft(dd));
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
		ro[9] = tmp;

		String ps = "";

		if (pid != null) {
		    Project p = TaskModel.getReference().getProject(
			    pid.intValue());
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

		ro[10] = ps;

		// add the task row to table
		addRow(taskTable, ro);
		row++;
	    }

	} catch (DBException e) {
	    if (e.getRetCode() != DBException.RET_NOT_FOUND) {
		Errmsg.errmsg(e);
	    }

	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	// resize the table based on new row count
	resize();

	// apply default sort to the table
	defsort();
    }

    public void showTasksForProject(Project p) {

	statusBox.setSelectedIndex(0);
	String ps = TaskView.getProjectString(p);
	projectBox.setSelectedItem(ps);
	refresh();

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

    // get the filter string typed by the user
    private String filter() {
	return (jTextField3.getText());
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
         * This method initializes caseBox
         * 
         * @return javax.swing.JCheckBox
         */
    private JCheckBox getCaseBox() {
	if (caseBox == null) {
	    caseBox = new JCheckBox();
	    caseBox.setText(Resource.getResourceString("case_sensitive"));
	}
	return caseBox;
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

    private JPanel getJPanel2() throws Exception {
	if (jPanel2 == null) {

	    FlowLayout flowLayout = new FlowLayout();
	    flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
	    statusLabel = new JLabel();
	    statusLabel
		    .setText(Resource.getPlainResourceString("Status") + ":");
	    jPanel2 = new JPanel();
	    jPanel2.setLayout(flowLayout);
	    jPanel2.add(statusLabel, null);
	    jPanel2.add(getStatusBox(), null);
	    JLabel plabel = new JLabel(Resource
		    .getPlainResourceString("project")
		    + ":");
	    JLabel spacer = new JLabel("           ");
	    jPanel2.add(spacer, null);
	    jPanel2.add(plabel, null);
	    jPanel2.add(getProjectBox());
	}
	return jPanel2;
    }

    private JComboBox getProjectBox() throws Exception {
	if (projectBox == null) {
	    projectBox = new JComboBox();
	    loadProjectBox();
	    projectBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    refresh();
		}
	    });
	}
	return projectBox;
    }

    /**
         * This method initializes statusBox
         * 
         * @return javax.swing.JComboBox
         */
    private JComboBox getStatusBox() {
	if (statusBox == null) {
	    statusBox = new JComboBox();
	    setStatuses(statusBox);
	    statusBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    refresh();
		}
	    });
	}
	return statusBox;
    }

    /**
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the FormEditor.
         */

    private void initComponents() throws Exception {

	initMenuBar();

	GridBagConstraints gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.gridx = 0;
	gridBagConstraints.fill = GridBagConstraints.BOTH;
	gridBagConstraints.gridwidth = 5;
	gridBagConstraints.gridy = 4;
	change.setIcon(new ImageIcon(getClass().getResource(
		"/resource/Edit16.gif")));
	add
		.setIcon(new ImageIcon(getClass().getResource(
			"/resource/Add16.gif")));
	JScrollPane jScrollPane1 = new JScrollPane();
	taskTable = new StripedTable();
	JButton jButton21 = new JButton();
	jTextField3 = new javax.swing.JTextField();
	GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
	gridBagConstraints2.gridx = 4;
	gridBagConstraints2.gridy = 1;

	GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
	GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
	GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
	GridBagConstraints gridBagConstraints8 = new GridBagConstraints();

	this.setLayout(new GridBagLayout());
	gridBagConstraints8.gridx = 2;
	gridBagConstraints8.gridy = 1;
	gridBagConstraints8.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints8.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints9.gridx = 3;
	gridBagConstraints9.gridy = 1;
	gridBagConstraints9.weightx = 1.0;
	gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints9.gridwidth = 1;
	gridBagConstraints9.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints11.gridx = 0;
	gridBagConstraints11.gridy = 3;
	gridBagConstraints11.weightx = 1.0;
	gridBagConstraints11.weighty = 1.0;
	gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints11.gridwidth = 5;
	gridBagConstraints11.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints15.gridx = 0;
	gridBagConstraints15.gridy = 0;
	gridBagConstraints15.gridwidth = 5;
	gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;

	jButton21.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Find16.gif")));
	ResourceHelper.setText(jButton21, "Filter:");
	jButton21.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		jButton21ActionPerformed(evt);
	    }
	});

	this.add(jButton21, gridBagConstraints8);
	this.add(jTextField3, gridBagConstraints9);
	this.add(jScrollPane1, gridBagConstraints11);
	this.add(getJPanel2(), gridBagConstraints15);

	this.add(getCaseBox(), gridBagConstraints2);

	this.add(getButtonPanel(), gridBagConstraints);
	jScrollPane1.setViewportView(taskTable);

	defrend_ = taskTable.getDefaultRenderer(Integer.class);

	// set renderer to the custom one for integers
	taskTable.setDefaultRenderer(Integer.class,
		new TaskListPanel.DLRenderer());

	// use a sorted table model
	taskTable.setModel(new TableSorter(new String[] {
		Resource.getPlainResourceString("Item_#"),
		Resource.getPlainResourceString("Status"),
		Resource.getPlainResourceString("Type"),
		Resource.getPlainResourceString("Category"),
		Resource.getPlainResourceString("Pri"),
		Resource.getPlainResourceString("Start_Date"),
		Resource.getPlainResourceString("Due_Date"),
		Resource.getPlainResourceString("elapsed_time"),
		Resource.getPlainResourceString("Days_Left"),
		Resource.getPlainResourceString("Description"),
		Resource.getPlainResourceString("project") }, new Class[] {
		java.lang.Integer.class, java.lang.String.class,
		java.lang.String.class, java.lang.String.class,
		java.lang.Integer.class, Date.class, Date.class,
		java.lang.Integer.class, java.lang.Integer.class,
		java.lang.String.class, java.lang.String.class }));

	// set up for sorting when a column header is clicked
	TableSorter tm = (TableSorter) taskTable.getModel();
	tm.addMouseListenerToHeaderInTable(taskTable);

	// clear all rows
	deleteAll();

	// jScrollPane1.setViewport(jScrollPane1.getViewport());
	jScrollPane1.setViewportView(taskTable);
	jScrollPane1.setBorder(javax.swing.BorderFactory
		.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
	taskTable.setBorder(new javax.swing.border.LineBorder(
		new java.awt.Color(0, 0, 0)));
	taskTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
	// taskTable.setGridColor(java.awt.Color.blue);
	taskTable.setPreferredSize(new java.awt.Dimension(700, 500));
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
	taskTable.getColumnModel().getColumn(0).setPreferredWidth(80);
	taskTable.getColumnModel().getColumn(1).setPreferredWidth(80);
	taskTable.getColumnModel().getColumn(2).setPreferredWidth(80);
	taskTable.getColumnModel().getColumn(3).setPreferredWidth(80);
	taskTable.getColumnModel().getColumn(5).setPreferredWidth(100);
	taskTable.getColumnModel().getColumn(6).setPreferredWidth(100);
	taskTable.getColumnModel().getColumn(7).setPreferredWidth(80);
	taskTable.getColumnModel().getColumn(8).setPreferredWidth(80);
	taskTable.getColumnModel().getColumn(9).setPreferredWidth(400);
	taskTable.getColumnModel().getColumn(10).setPreferredWidth(80);
	taskTable.setPreferredScrollableViewportSize(new Dimension(900, 400));

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

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {
	// just call refresh when filter button pressed
	refresh();
    }

    private void loadProjectBox() throws Exception {
	projectBox.removeAllItems();
	projectBox.addItem(Resource.getPlainResourceString("All"));
	try {
	    Collection projects = TaskModel.getReference().getProjects();
	    Iterator pi = projects.iterator();
	    while (pi.hasNext()) {
		Project p = (Project) pi.next();
		projectBox.addItem(TaskView.getProjectString(p));
	    }
	}
	// ignore exception if projects not supported
	catch (Exception e) {
	}
    }

    private void mouseClick(java.awt.event.MouseEvent evt) {

	// ask controller to bring up task editor on double click
	if (evt.getClickCount() < 2)
	    return;

	// changeActionPerformed(null);
	showChildren();
    }

    // resize table based on row count
    private void resize() {
	int row = taskTable.getRowCount();
	taskTable.setPreferredSize(new Dimension(1000, row * 16));

    }

    private void setStatuses(JComboBox s) {

	s.addItem(Resource.getPlainResourceString("All_Open"));
	s.addItem(Resource.getPlainResourceString("All"));
	TaskTypes t = TaskModel.getReference().getTaskTypes();
	TreeSet ts = new TreeSet();
	Vector types = t.getTaskTypes();
	Iterator it = types.iterator();
	while (it.hasNext()) {
	    String type = (String) it.next();
	    Collection states = t.getStates(type);
	    Iterator it2 = states.iterator();
	    while (it2.hasNext()) {
		ts.add(it2.next());
	    }
	}
	it = ts.iterator();
	while (it.hasNext()) {
	    s.addItem(it.next());
	}
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
	    String projfilt = (String) projectBox.getSelectedItem();
	    Integer projfiltid = null;
	    if (!projfilt.equals(Resource.getPlainResourceString("All"))) {
		try {
		    projfiltid = TaskView.getProjectId(projfilt);
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		    return;
		}
	    }
	    TaskView tskg = new TaskView(null, TaskView.T_ADD, projfiltid);
	    tskg.setVisible(true);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }

    // show the task view - to edit a task
    private void task_change(int tasknum) {

	try {
	    // get the task from the data model
	    TaskModel taskmod_ = TaskModel.getReference();
	    Task task = taskmod_.getMR(tasknum);
	    if (task == null)
		return;

	    // display the task editor
	    TaskView tskg = new TaskView(task, TaskView.T_CHANGE, null);
	    tskg.setVisible(true);

	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

    }

    private void task_clone(int tasknum) {

	try {
	    // get the task
	    TaskModel taskmod_ = TaskModel.getReference();
	    Task task = taskmod_.getMR(tasknum);
	    if (task == null)
		return;

	    // display the task editor
	    TaskView tskg = new TaskView(task, TaskView.T_CLONE, null);
	    tskg.setVisible(true);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

    }
}
