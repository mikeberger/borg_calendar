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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Warning;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Project;
import net.sf.borg.model.TaskModel;

/**
 * 
 * @author MBERGER
 * @version
 */

class ProjectView extends View {



    public ProjectView(Project p, int function) throws Exception {
	super();
	addModel(TaskModel.getReference());

	initComponents(); // init the GUI widgets

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
	showProject(function, p);

	manageMySize(PrefName.PROJVIEWSIZE);
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
	GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
	
	jScrollPane1 = new javax.swing.JScrollPane();
	jTextArea1 = new javax.swing.JTextArea();
	

	jPanel3 = new javax.swing.JPanel();
	itemtext = new javax.swing.JTextField();
	lblItemNum = new javax.swing.JLabel();
	lblStatus = new javax.swing.JLabel();
	startdatechooser = new de.wannawork.jcalendar.JCalendarComboBox();
	duedatechooser = new de.wannawork.jcalendar.JCalendarComboBox();
	lblStartDate = new javax.swing.JLabel();
	lblDueDate = new javax.swing.JLabel();

	statebox = new javax.swing.JComboBox();
	catlabel = new javax.swing.JLabel();
	GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
	
	GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
	GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
	
	GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
	GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
	GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
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


	
	GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
	GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
	GridBagConstraints gridBagConstraints38 = new GridBagConstraints();
	

	jPanel3.setLayout(new java.awt.GridBagLayout());

	jPanel3.setBorder(new javax.swing.border.TitledBorder(Resource
		.getResourceString("ProjectInformation")));

	itemtext.setText("itemtext");

	ResourceHelper.setText(lblItemNum, "Item_#");
	lblItemNum.setLabelFor(itemtext);

	ResourceHelper.setText(lblStatus, "Status");
	lblStatus.setLabelFor(statebox);

	ResourceHelper.setText(lblStartDate, "Start_Date");
	lblStartDate.setLabelFor(startdatechooser);

	ResourceHelper.setText(lblDueDate, "Due_Date");
	lblDueDate.setLabelFor(duedatechooser);

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
		saveProject(evt);
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
	
	gridBagConstraints28.gridx = 0;
	gridBagConstraints28.gridy = 3;
	gridBagConstraints28.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints28.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints29.gridx = 0;
	gridBagConstraints29.gridy = 4;
	gridBagConstraints29.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints29.insets = new java.awt.Insets(4, 4, 4, 4);

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
	gridBagConstraints11.gridx = 3;
	gridBagConstraints11.gridy = 0;
	gridBagConstraints11.weightx = 1.0;
	gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints11.insets = new java.awt.Insets(4, 4, 4, 4);
	
	jPanel3.add(lblStartDate, gridBagConstraints28);
	jPanel3.add(lblDueDate, gridBagConstraints29);
	
	jPanel3.add(catlabel, gridBagConstraints32);
	jPanel3.add(duedatechooser, gridBagConstraints33);
	jPanel3.add(startdatechooser, gridBagConstraints34);
	jPanel3.add(lblItemNum, gridBagConstraints37); // Generated
	gridBagConstraints26.insets = new java.awt.Insets(4, 4, 4, 4);
	jPanel3.add(itemtext, gridBagConstraints38); // Generated
	jPanel3.add(lblStatus, gridBagConstraints1); // Generated
	jPanel3.add(getCatbox(), gridBagConstraints11);
	jPanel3.add(statebox, gridBagConstraints26); // Generated
	jPanel3.add(closeLabel, gridBagConstraints210);
	jPanel3.add(getDaysLeftText(), gridBagConstraints13);
	jPanel3.add(daysLeftLabel, gridBagConstraints22);
	ResourceHelper.setText(closeLabel, "close_date");
	
	
    }// GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton3ActionPerformed
    {// GEN-HEADEREND:event_jButton3ActionPerformed
	this.dispose();
    }// GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton2ActionPerformed
    {// GEN-HEADEREND:event_jButton2ActionPerformed
	saveProject(evt);
    }// GEN-LAST:event_jButton2ActionPerformed

    // save a task
    private void saveProject(java.awt.event.ActionEvent evt)
    {

	// save a task from the data on the screen
	if (jTextArea1.getText() == null || jTextArea1.getText().equals("")) {
	    Errmsg.notice(Resource.getResourceString("empty_desc"));
	    return;
	}
	try {

	    String num = itemtext.getText();

	    // allocate a new task object from the task data model
	    TaskModel taskmod_ = TaskModel.getReference();
	    
	    Project p = new Project();

	    // set the task number to the current number for updates and
	    // -1 for new tasks. task model will convert -1 to next
	    // available number
	    
	    if (num.equals("NEW")) {

		p.setId(new Integer(-1));
		
	    } else if (num.equals("CLONE")) {
		p.setId(new Integer(-1));
		
	    } else {
		p.setId(new Integer(num));
		
	    }

	    // fill in the fields from the screen
	  
	    Calendar cal = startdatechooser.getCalendar();
	    p.setStartDate(cal.getTime()); // start date
	    cal = duedatechooser.getCalendar();
	    p.setDueDate(cal.getTime()); // due date
	    
	    p.setDescription(jTextArea1.getText()); // description
	    
	    // task.setCategory( cattext.getText());
	    String cat = (String) catbox.getSelectedItem();
	    if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
		p.setCategory(null);
	    } else {
		p.setCategory(cat);
	    }	    

	    taskmod_.saveProject(p);

	    // System.out.println(task.getTaskNumber());

	    // refresh window from DB - will update task number for
	    // new tasks and will set the list of available next states from
	    // the task model
	    showProject(T_CHANGE, p);
	} catch (Warning w) {
	    Errmsg.notice(w.getMessage());
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

    }// GEN-LAST:event_savetask

 
    private void disact(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_disact
	this.dispose();
    }// GEN-LAST:event_disact


    private void showProject(int function, Project p) throws Exception {
	
	// if we are showing an existing task - fil; in the gui fields form it
	if (p != null) {
	    // task number
	    itemtext.setText(p.getId().toString());
	    itemtext.setEditable(false);

	    // window title - "Item N"
	    setTitle(Resource.getResourceString("Item_")
		    + p.getId().toString());

	    // due date
	    GregorianCalendar gc = new GregorianCalendar();
	    Date dd = p.getDueDate();
	    if (dd != null)
		gc.setTime(dd);

	    duedatechooser.setCalendar(gc);

	    GregorianCalendar gc2 = new GregorianCalendar();
	    dd = p.getStartDate();
	    if (dd != null)
		gc2.setTime(dd);

	    startdatechooser.setCalendar(gc2);

	   


	    int daysleft = TaskModel.daysLeft(p.getDueDate());
	    daysLeftText.setText(Integer.toString(daysleft));

	    // cattext.setText( task.getCategory() );
	    String cat = p.getCategory();
	    if (cat != null && !cat.equals("")) {
		catbox.setSelectedItem(cat);
	    } else {
		catbox.setSelectedIndex(0);
	    }

	    jTextArea1.setText(p.getDescription()); // description

	    //statebox.addItem(task.getState()); // state
	    statebox.setEditable(false);	  

	} else // initialize new task
	{

	    // task number = NEW
	    itemtext.setText("NEW");
	    itemtext.setEditable(false);

	    // title
	    ResourceHelper.setTitle(this, "NEW_Item");

	    catbox.setSelectedIndex(0);
	    jTextArea1.setText(""); // desc
	    

	}

	if (p == null) {
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
	    statebox.setEnabled(false);

	   

	}
	// change existing task
	else if (function == T_CHANGE) {

	    // determine valid next states based on task type and current
	    // state
	    //String stat = task.getState();
	    
	    // set next state pulldown
	    statebox.removeAllItems();
	    
	    statebox.setEnabled(true);

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

    private javax.swing.JMenu jMenu1;

    private javax.swing.JMenuBar jMenuBar1;

    private javax.swing.JMenuItem jMenuItem1;

    private javax.swing.JMenuItem jMenuItem2;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTextArea jTextArea1;

    private javax.swing.JTextField itemtext;

    private de.wannawork.jcalendar.JCalendarComboBox startdatechooser;

    private de.wannawork.jcalendar.JCalendarComboBox duedatechooser;

    private javax.swing.JComboBox statebox;

    private JPanel jPanel = null;

    private JComboBox catbox = null;

    private JLabel closeLabel = null;

    private JTextField daysLeftText = null;

    private JLabel daysLeftLabel = null;

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

	    jPanel.add(jScrollPane1, gridBagConstraints24);
	    jPanel.add(jPanel4, gridBagConstraints25);



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
} // @jve:decl-index=0:visual-constraint="115,46"
