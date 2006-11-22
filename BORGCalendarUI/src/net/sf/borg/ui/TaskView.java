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

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.ui.DateEditor;
import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Resource;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Subtask;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.TaskTypes;

/**
 * 
 * @author MBERGER
 * @version
 */

// taskgui is a View that allows the user to edit a single task
class TaskView extends View {

    private static TaskView singleton = null;

    private JTable stable = new JTable();

    private ArrayList tbd_ = new ArrayList();

    static TaskView getReference(Task task, int function) throws Exception {
	if (singleton == null || !singleton.isShowing())
	    singleton = new TaskView(task, function);
	return (singleton);
    }

    private TableCellRenderer defIntRend_;
    private TableCellRenderer defDateRend_;

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
	    l.setHorizontalAlignment(CENTER);
	    return l;

	}
    }
    
    private class STDDRenderer extends JLabel implements TableCellRenderer {

        public STDDRenderer() {
            super();
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(JTable table,
                Object obj, boolean isSelected, boolean hasFocus, int row,
                int column) {

            Boolean closed = (Boolean) table.getModel().getValueAt(row, 0);
            if( closed.booleanValue() == true || column != 4 || obj == null )
        	return defDateRend_.getTableCellRendererComponent(table, obj,
                        isSelected, hasFocus, row, column);
            
            
            Date dd = (Date) obj;
            Date now = new Date();
            
            int days = ((int)dd.getTime() - (int)now.getTime()) / (1000*60*60*24); 
            
            JLabel l = (JLabel)defDateRend_.getTableCellRendererComponent(table, obj,
                    isSelected, hasFocus, row, column);
            
            this.setBackground(l.getBackground());
            this.setForeground(l.getForeground());
            this.setText(DateFormat.getDateInstance().format(dd));
            
            if( !isSelected )
            {
        	// yellow alert -- <10 days left
                if (days < 10)
                    this.setBackground(Color.yellow);

                // red alert -- <2 days left
                if (days < 2) {
                    this.setBackground(Color.red);
                    this.setForeground(Color.white);
                }
            }
 
            return this;
        }
    }

    private TaskView(Task task, int function) throws Exception {
	super();
	addModel(TaskModel.getReference());

	initComponents(); // init the GUI widgets

	defIntRend_ = stable.getDefaultRenderer(Integer.class);
	defDateRend_ = stable.getDefaultRenderer(Date.class);
	
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

	stable.setModel(new TableSorter(new String[] {
		Resource.getPlainResourceString("done"),
		Resource.getPlainResourceString("subtask_id"),
		Resource.getPlainResourceString("Description"),
		Resource.getPlainResourceString("Start_Date"),
		Resource.getPlainResourceString("Due_Date"),
		Resource.getPlainResourceString("close_date") }, new Class[] {
		java.lang.Boolean.class, Integer.class, java.lang.String.class,
		Date.class, Date.class, Date.class }, new boolean[] { true,
		false, true, true, true, false }));

	stable.setDefaultRenderer(Integer.class, new STIntRenderer());
	stable.setDefaultRenderer(Date.class, new STDDRenderer());

	stable.getColumnModel().getColumn(0).setPreferredWidth(5);
	stable.getColumnModel().getColumn(1).setPreferredWidth(5);
	stable.getColumnModel().getColumn(2).setPreferredWidth(300);
	stable.getColumnModel().getColumn(3).setPreferredWidth(30);
	stable.getColumnModel().getColumn(4).setPreferredWidth(30);
	stable.getColumnModel().getColumn(5).setPreferredWidth(30);

	stable.setDefaultEditor(Date.class, new DateEditor());

	TableSorter ts = (TableSorter) stable.getModel();

	ts.addMouseListenerToHeaderInTable(stable);
	new PopupMenuHelper(stable, new PopupMenuHelper.Entry[] {
		new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
		    public void actionPerformed(java.awt.event.ActionEvent evt) {
			Object o[] = { new Boolean(false), new Integer(0),
				null, new Date(), null, null };
			TableSorter ts = (TableSorter) stable.getModel();

			ts.addRow(o);
		    }
		}, "Add_New"),
		new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
		    public void actionPerformed(java.awt.event.ActionEvent evt) {
			TableSorter ts = (TableSorter) stable.getModel();
			int[] indices = stable.getSelectedRows();
			if (indices.length == 0)
			    return;

			int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Really_Delete_")
				+ "?", Resource
				.getPlainResourceString("Confirm_Delete"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			if (ret != JOptionPane.OK_OPTION)
			    return;

			for (int i = 0; i < indices.length; ++i) {
			    int index = indices[i];
			    Integer id = (Integer) ts.getValueAt(index, 1);
			    tbd_.add(id);
			    ts.removeRow(index);
			}

			// if table is now empty - add 1 row back
			if (ts.getRowCount() == 0) {
			    Object o[] = { new Boolean(false), new Integer(0),
				    null, new Date(), null, null };
			    ts.addRow(o);
			}
		    }
		}, "Delete"), });
	// display the window
	pack();
	showtask(function, task);

	manageMySize(PrefName.TASKVIEWSIZE);
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
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the FormEditor.
         */
    private void initComponents()// GEN-BEGIN:initComponents
    {
	java.awt.GridBagConstraints gridBagConstraints;

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
	startdatechooser = new de.wannawork.jcalendar.JCalendarComboBox();
	duedatechooser = new de.wannawork.jcalendar.JCalendarComboBox();
	pritext = new javax.swing.JTextField();
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
	ResourceHelper.setText(closeLabel, "close_date");

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
	    Task task = taskmod_.newMR();

	    // set the task number to the current number for updates and
	    // -1 for new tasks. task model will convert -1 to next
	    // available number
	    if (num.equals("NEW")) {
		TableSorter ts = (TableSorter) stable.getModel();
		task.setTaskNumber(new Integer(-1));
		String cbs[] = TaskModel.getReference().getTaskTypes()
			.checkBoxes((String) typebox.getSelectedItem());
		for (int i = 0; i < cbs.length; i++) {
		    if (!cbs[i].equals(TaskTypes.NOCBVALUE)) {
			Object o[] = { new Boolean(false), new Integer(0),
				cbs[i], new Date(), null, null };
			ts.addRow(o);
		    }
		}
	    } else {
		task.setTaskNumber(new Integer(num));
	    }

	    // fill in the taks fields from the screen
	    task.setState((String) statebox.getSelectedItem()); // state
	    task.setType((String) typebox.getSelectedItem()); // type
	    Calendar cal = startdatechooser.getCalendar();
	    task.setStartDate(cal.getTime()); // start date
	    cal = duedatechooser.getCalendar();
	    task.setDueDate(cal.getTime()); // due date
	    task.setPriority(pritext.getText()); // priority
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

	    // do not close task if subtasks are open
	    if (task.getState().equals(
		    TaskModel.getReference().getTaskTypes().getFinalState(
			    task.getState()))) {
		TableSorter ts = (TableSorter) stable.getModel();
		for (int r = 0; r < stable.getRowCount(); r++) {
		    Boolean closed = (Boolean) ts.getValueAt(r, 0);
		    if (closed.booleanValue() != true) {
			Errmsg.notice(Resource
				.getResourceString("open_subtasks"));
			return;
		    }
		}
	    }

	    // save the task to the DB
	    taskmod_.savetask(task);

	    
	    // System.out.println(task.getTaskNumber());
	    saveSubtasks(task.getTaskNumber().intValue());

	    // refresh window from DB - will update task number for
	    // new tasks and will set the list of available next states from
	    // the task model
	    showtask(T_CHANGE, task);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	    // JOptionPane.showMessageDialog(null, e.toString(), "Error",
	    // JOptionPane.ERROR_MESSAGE);
	}

    }// GEN-LAST:event_savetask

    private void saveSubtasks(int tasknum) throws Exception {

	Iterator it = tbd_.iterator();
	while (it.hasNext()) {
	    Integer id = (Integer) it.next();
	    System.out.println("deleting sub task: " + id.intValue());
	    TaskModel.getReference().deleteSubTask(id.intValue());
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
	    Date dd = (Date) ts.getValueAt(r, 4);
	    Date cd = (Date) ts.getValueAt(r, 5);

	    if (closed.booleanValue() == true && cd == null)
		cd = new Date();
	    else if (closed.booleanValue() == false && cd != null)
		cd = null;

	    Subtask s = new Subtask();
	    s.setId(id);
	    s.setDescription((String) desc);
	    s.setCloseDate(cd);
	    s.setDueDate(dd);
	    s.setCreateDate(crd);
	    s.setTask(new Integer(tasknum));
	    TaskModel.getReference().saveSubTask(s);

	}
    }

    private void disact(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_disact
	this.dispose();
    }// GEN-LAST:event_disact

    private void showtask(int function, Task task) throws Exception {
	TaskModel taskmod_ = TaskModel.getReference();

	// show a task editor for changing, cloning, or add of a task
	TableSorter ts = (TableSorter) stable.getModel();
	ts.setRowCount(0);

	tbd_.clear();

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

	    pritext.setText(task.getPriority()); // priority
	    patext.setText(task.getPersonAssigned()); // person assigned

	    Date cd = task.getCD();
	    if (cd != null)
		closeDate.setText(DateFormat.getDateInstance(DateFormat.MEDIUM)
			.format(cd));
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
	    Collection col = TaskModel.getReference().getSubTasks(
		    task.getTaskNumber().intValue());
	    Iterator it = col.iterator();
	    while (it.hasNext()) {
		Subtask s = (Subtask) it.next();
		Object o[] = {
			s.getCloseDate() == null ? new Boolean(false)
				: new Boolean(true), s.getId(),
			s.getDescription(), s.getCreateDate(), s.getDueDate(),
			s.getCloseDate() };

		ts.addRow(o);
	    }

	} else // initialize new task
	{

	    // task number = NEW
	    itemtext.setText("NEW");
	    itemtext.setEditable(false);

	    // title
	    ResourceHelper.setTitle(this, "NEW_Item");

	    pritext.setText("3"); // priority default to 3
	    patext.setText(""); // person assigned
	    // cattext.setText("");
	    catbox.setSelectedIndex(0);
	    jTextArea1.setText(""); // desc
	    jTextArea2.setText(""); // resolution

	    Vector tv = taskmod_.getTaskTypes().getTaskTypes();
	    for (int i = 0; i < tv.size(); i++) {
		typebox.addItem(tv.elementAt(i));
	    }

	}

	if (task == null) {
	    statebox.addItem(taskmod_.getTaskTypes().getInitialState(
		    typebox.getSelectedItem().toString()));
	    statebox.setEnabled(false);
	}

	// cloning takes the fields filled in for an existing task and resets
	// only those
	// that don't apply to the clone
	if (function == T_CLONE) {
	    // need new task number
	    itemtext.setText("NEW");
	    itemtext.setEditable(false);

	    statebox.removeAllItems();
	    statebox.addItem(taskmod_.getTaskTypes().getInitialState(
		    typebox.getSelectedItem().toString()));
	    statebox.setEnabled(false);

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

	if (stable.getRowCount() == 0) {
	    Object o[] = { new Boolean(false), new Integer(0), null,
		    new Date(), null, null };
	    ts.addRow(o);
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

    private de.wannawork.jcalendar.JCalendarComboBox startdatechooser;

    private de.wannawork.jcalendar.JCalendarComboBox duedatechooser;

    private javax.swing.JTextField pritext;

    private javax.swing.JTextField patext;

    private javax.swing.JComboBox statebox;

    private javax.swing.JComboBox typebox;

    // End of variables declaration//GEN-END:variables

    private JPanel jPanel = null;

    private JComboBox catbox = null;

    private JTextField closeDate = null;

    private JLabel closeLabel = null;

    /**
         * This method initializes jPanel
         * 
         * @return javax.swing.JPanel
         */
    private JPanel getJPanel() {
	if (jPanel == null) {
	    GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
	    gridBagConstraints4.fill = GridBagConstraints.BOTH;
	    gridBagConstraints4.gridx = 0;
	    gridBagConstraints4.gridy = 0;
	    gridBagConstraints4.weightx = 1.0;
	    gridBagConstraints4.weighty = 1.0;
	    gridBagConstraints4.insets = new Insets(4, 4, 4, 4);
	    GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
	    GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
	    GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
	    jPanel = new JPanel();
	    jPanel.setLayout(new GridBagLayout());
	    gridBagConstraints21.gridx = 0;
	    gridBagConstraints21.gridy = 0;
	    gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
	    gridBagConstraints21.weightx = 1.0D;

	    gridBagConstraints24.gridx = 0;
	    gridBagConstraints24.gridy = 1;
	    gridBagConstraints24.weightx = 1.0;
	    gridBagConstraints24.weighty = 1.0;
	    gridBagConstraints24.fill = java.awt.GridBagConstraints.BOTH;

	    gridBagConstraints25.gridx = 0;
	    gridBagConstraints25.gridy = 3;

	    jPanel.add(jPanel3, gridBagConstraints21); // Generated

	    jPanel.add(jTabbedPane1, gridBagConstraints24);
	    jPanel.add(jPanel4, gridBagConstraints25);

	    // subtasks
	    GridBagConstraints stgbc = new GridBagConstraints();
	    stgbc.gridx = 0;
	    stgbc.gridy = 2;
	    stgbc.weightx = 1.0;
	    stgbc.weighty = 1.0;
	    stgbc.fill = java.awt.GridBagConstraints.BOTH;

	    JScrollPane stscroll = new JScrollPane();
	    stscroll.setPreferredSize(new Dimension(300, 300));
	    stscroll.setViewportView(stable);
	    JPanel stpanel = new JPanel();
	    stpanel.setLayout(new GridBagLayout());
	    stpanel.setBorder(new javax.swing.border.TitledBorder(Resource
		    .getResourceString("Subtasks")));
	    stpanel.add(stscroll, gridBagConstraints4);
	    jPanel.add(stpanel, stgbc);
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
} // @jve:decl-index=0:visual-constraint="115,46"
