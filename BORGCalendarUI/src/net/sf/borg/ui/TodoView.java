
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
 
Copyright 2003 by ==Quiet==
 */

package net.sf.borg.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;

import net.sf.borg.common.ui.TablePrinter;
import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Version;
import net.sf.borg.common.util.Warning;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import javax.swing.JButton;
import javax.swing.ImageIcon;

// bsv 2004-12-21
import de.wannawork.jcalendar.*;
import java.text.*;


/*
 * tdgui.java
 *
 * Created on November 26, 2001, 3:12 PM
 */

/**
 *
 * @author  MBERGER
 */

// the tdgui displays a list of the current todo items and allows the
// suer to mark them as done
public class TodoView extends View {
    static {
        Version.addVersion("$Id$");
    }
    
    private Vector tds_;   // list of rows currently displayed in todo list
    
    private static TodoView singleton = null;
	public static TodoView getReference() {
        if( singleton == null || !singleton.isShowing())
            singleton = new TodoView();
        return( singleton );
    }
    
    private TodoView() {
        
        super();
        addModel( AppointmentModel.getReference() );
        addModel( TaskModel.getReference() );
        
        // init the gui components
        initComponents();
        
        String s = new java.text.SimpleDateFormat().toLocalizedPattern();
        String date_tip = s.substring(0,s.indexOf(' '));
        tododate.setToolTipText(date_tip);
        
        // the todos will be displayed in a sorted table with 2 columns -
        // data and todo text
        jTable1.setModel(new TableSorter(
        new String []{ Resource.getResourceString("Date"), Resource.getResourceString("To_Do"),ResourceBundle.getBundle("resource/borg_resource").getString("Category")  },
        new Class []{ Date.class,java.lang.String.class, java.lang.String.class}));
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(140);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(400);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(120);
        jTable1.setPreferredScrollableViewportSize(new Dimension( 660,400 ));
        refresh();
        
        manageMySize(PrefName.TODOVIEWSIZE);
        
    }
    
    public void destroy() {
        this.dispose();
    }
    
    // refresh the todo list if the data model changes
    public void refresh() {
        AppointmentModel calmod_ = AppointmentModel.getReference();
        
        // get the to list from the data model
        tds_ = calmod_.get_todos();
        
        // get the list of tasks from the data model
        Vector mrs = TaskModel.getReference().get_tasks();
        
        // init the table to empty
        TableSorter tm = (TableSorter) jTable1.getModel();
        tm.addMouseListenerToHeaderInTable(jTable1);
        tm.setRowCount(0);
        
        // add a tabel row to mark the current date - it will sort
        // to the right spot by date
        Date d = new Date();
        Object [] tod = new Object[2];
        tod[0] = d;
        tod[1] = new String(Resource.getResourceString("======_Today_======"));
        tm.addRow(tod);
        tm.tableChanged(new TableModelEvent(tm));
        
        // add the todo appointment rows to the table
        for( int i = 0; i < tds_.size(); i++ ) {
            Appointment r = (Appointment) tds_.elementAt(i);
            
            try{
                // get appt text
                String tx = r.getText();
                
                // date is the next todo field if present, otherwise
                // the due date
                Date nt = r.getNextTodo();
                if( nt == null ) {
                    nt = r.getDate();
                }
                
                // add the table row
                Object [] ro = new Object[3];
                ro[0] = nt;
                ro[1] = tx;
                ro[2] = r.getCategory();
                tm.addRow(ro);
                tm.tableChanged(new TableModelEvent(tm));
            }
            catch( Exception e ) {
                Errmsg.errmsg(e);
                return;
            }
            
        }
        
        // add the tasks to the list
        // add open tasks with due dates are considered as todos
        for( int i = 0; i < mrs.size(); i++ ) {
            
            Task mr = (Task) mrs.elementAt(i);
            
            try{
                
                // build a string for the table - BT<task number> + description
                String btstring = "BT" + mr.getTaskNumber().toString() + " " + mr.getDescription();
                
                // add row to table
                Object [] ro = new Object[3];
                ro[0] = mr.getDueDate();
                ro[1] = btstring;
                ro[2] = mr.getCategory();
                tm.addRow(ro);
                tm.tableChanged(new TableModelEvent(tm));
            }
            catch( Exception e ) {
                Errmsg.errmsg(e);
                return;
            }
            
        }
        
        // sort the table by date
        tm.sortByColumn(0);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        todotext = new javax.swing.JTextField();
        
        // TODO place jcalendar here
        tododate_cb = new JCalendarComboBox();
        tododate = new javax.swing.JTextField();
        
        addtodo = new javax.swing.JButton();
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        printList = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();

        //getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("To_Do_List"));
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        jScrollPane1.setMinimumSize(new java.awt.Dimension(450, 21));
        jTable1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jTable1.setGridColor(java.awt.Color.blue);
        DefaultListSelectionModel mylsmodel = new DefaultListSelectionModel();
        mylsmodel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
        jTable1.setSelectionModel(mylsmodel );
        jTable1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jTable1MouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(jTable1);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setMinimumSize(new java.awt.Dimension(550, 102));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
                java.util.ResourceBundle.getBundle("resource/borg_resource").getString("todoquickentry"), 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
        addtodo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Save16.gif")));
        addtodo.setText("Add");
        addtodo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addtodoActionPerformed(evt);
            }
        });

        
        jLabel1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("To_Do"));
 
        jLabel2.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Date"));
 
        jLabel3.setFont(new java.awt.Font("MS Sans Serif", 2, 10));
        jLabel3.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("quicktodonotice"));

 


        fileMenu.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Action"));
        jMenuItem1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Done_(No_Delete)"));
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem1ActionPerformed(evt);
            }
        });

        fileMenu.add(jMenuItem1);

        jMenuItem2.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Done_(Delete)"));
        jMenuItem2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem2ActionPerformed(evt);
            }
        });

        fileMenu.add(jMenuItem2);

        printList.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Print_List"));
        printList.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                printListActionPerformed(evt);
            }
        });

        fileMenu.add(printList);

        exitMenuItem.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Exit"));
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        this.setContentPane(getJPanel());

        gridBagConstraints8.gridx = 2;
        gridBagConstraints8.gridy = 2;
        gridBagConstraints8.insets = new java.awt.Insets(4,4,4,4);
        gridBagConstraints9.gridx = 1;
        gridBagConstraints9.gridy = 2;
        gridBagConstraints9.weightx = 0.0D;
        gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints9.insets = new java.awt.Insets(0,4,4,4);
        gridBagConstraints11.gridx = 0;
        gridBagConstraints11.gridy = 3;
        gridBagConstraints11.insets = new java.awt.Insets(4,4,4,4);
        gridBagConstraints11.gridwidth = 3;
        gridBagConstraints12.gridx = 1;
        gridBagConstraints12.gridy = 1;
        gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints12.weightx = 1.0D;
        gridBagConstraints12.insets = new java.awt.Insets(4,4,0,0);
        gridBagConstraints13.gridx = 0;
        gridBagConstraints13.gridy = 1;
        gridBagConstraints13.insets = new java.awt.Insets(4,4,0,0);
        gridBagConstraints13.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints13.weightx = 1.0D;
        gridBagConstraints14.gridx = 0;
        gridBagConstraints14.gridy = 2;
        gridBagConstraints14.weightx = 0.0D;
        gridBagConstraints14.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints14.insets = new java.awt.Insets(0,4,4,4);
        jPanel1.add(addtodo, gridBagConstraints8);
        
        // bsv 2004-12-21
        jPanel1.add(tododate_cb, gridBagConstraints9);
        //jPanel1.add(tododate, gridBagConstraints9);
        
        jPanel1.add(jLabel3, gridBagConstraints11);
        jPanel1.add(jLabel2, gridBagConstraints12);
        jPanel1.add(jLabel1, gridBagConstraints13);
        jPanel1.add(todotext, gridBagConstraints14);
        pack();
    }//GEN-END:initComponents

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
      // ask controller to bring up appt editor on double click
      if( evt.getClickCount() < 2 ) return;
      
      // get task number from column 0 of selected row
      int row = jTable1.getSelectedRow();
      if( row == -1 ) return;
      TableSorter tm = (TableSorter) jTable1.getModel();
      Date d = (Date) tm.getValueAt(row,0);
      
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(d);
      
      //bring up an appt editor window
      AppointmentListView ag = new AppointmentListView(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
      ag.show();
      
      CalendarView cv = CalendarView.getReference();
      if( cv != null ) cv.goTo( cal );
      
    }//GEN-LAST:event_jTable1MouseClicked
    
    private void addtodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addtodoActionPerformed
        
        String tdtext = todotext.getText();
        
        // bsv 2004-12-21
        // TODO investigate how it works
        //String tddate = tododate_cb.getCalendar().getTime().toString();
        //System.out.println(tododate_cb.getCalendar().getTime().toString());
        // some stupid way
        //tododate.setText( tododate_cb.getCalendar().getTime().toString() );
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        tododate.setText( sdf.format( tododate_cb.getCalendar().getTime() ) );
        System.out.println(tododate.getText());
        String tddate = tododate.getText();
        //String tddate = tododate.getText();
        
        Date std = new Date();
        
        if( tdtext.length() == 0 || tddate.length() == 0 ) {
            Errmsg.notice(ResourceBundle.getBundle("resource/borg_resource").getString("todomissingdata"));
            return;
        }
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        try {
            std = df.parse(tddate);
        }
        catch( Exception e ) {
            Errmsg.notice(ResourceBundle.getBundle("resource/borg_resource").getString("todo_date_invalid"));
            return;
        }
        
        GregorianCalendar g = new GregorianCalendar();
        g.setTime(std);
        AppointmentModel calmod_ = AppointmentModel.getReference();
        Appointment r = calmod_.newAppt();
        r.setDate(std);
        r.setText( tdtext );
        r.setTodo(true);
        r.setPrivate( false );
        r.setColor( "black");
        r.setFrequency( "once" );
        r.setTimes(new Integer(1));
        r.setRepeatFlag(false);
        r.setCategory("");
        
        calmod_.saveAppt(r, true);
        
    }//GEN-LAST:event_addtodoActionPerformed
    
    private void printListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printListActionPerformed
        
        // user has requested a print of the table
        try { TablePrinter.printTable(jTable1); }
        catch( Exception e ){ Errmsg.errmsg(e); }
    }//GEN-LAST:event_printListActionPerformed
    
    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem2ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem2ActionPerformed
        // mark a todo done and delete it
        dtcommon(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed
    
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem1ActionPerformed
        // make a todo done and do not delete it
        dtcommon(false);
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    
    // function to mark a todo as done
    private void dtcommon(boolean del) {
        
        // figure out which row is selected to be marked as done
        int index =  jTable1.getSelectedRow();
        if( index == -1 ) return;
        
        try {
            
            // need to ask the table for the original (befor sorting) index of the selected row
            TableSorter tm = (TableSorter) jTable1.getModel();
            int k = tm.getMappedIndex(index);  // get original index - not current sorted position in tbl
            
            // ignore the "today" row - which was the first in the original table (index=0)
            // can't mark it as done
            if( k == 0 ) return;
            
            // do not allow the gui to mark a task as done - must go through tracker for that
            if( k > tds_.size() ) {
                throw new Warning( Resource.getResourceString("Must_use_the_Task_Tracker_to_act_upon_a_task") );
            }
            
            // get the appointment
            Appointment r = (Appointment) tds_.elementAt(k-1);
            int key = r.getKey();
            
            // ask the calendar data model to mark it as done
            AppointmentModel.getReference().do_todo( key,del);
            
            
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
            this.dispose();
        }
    }
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        this.dispose();
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        this.dispose();
    }//GEN-LAST:event_exitForm
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addtodo;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem printList;
    private javax.swing.JTextField tododate;
    private javax.swing.JTextField todotext;
    
    // bsv 2004-12-21
    private JCalendarComboBox tododate_cb;
    
    // End of variables declaration//GEN-END:variables
    
	private JPanel jPanel = null;
	private JPanel jPanel2 = null;
	private JButton doneButton = null;
	private JButton doneDelButton = null;
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints1.insets = new java.awt.Insets(4,4,4,4);
			gridBagConstraints1.gridwidth = 1;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 2;
			gridBagConstraints2.insets = new java.awt.Insets(4,4,4,4);
			gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridy = 1;
			gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
			jPanel.add(jScrollPane1, gridBagConstraints1);
			jPanel.add(jPanel1, gridBagConstraints2);
			jPanel.add(getJPanel2(), gridBagConstraints15);
		}
		return jPanel;
	}
	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.add(getDoneButton(), null);
			jPanel2.add(getDoneDelButton(), null);
		}
		return jPanel2;
	}
	/**
	 * This method initializes doneButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getDoneButton() {
		if (doneButton == null) {
			doneButton = new JButton();
			doneButton.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Done_(No_Delete)"));
			doneButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					dtcommon(false);
				}
			});
			doneButton.setIcon(new ImageIcon(getClass().getResource("/resource/Properties16.gif")));
		}
		return doneButton;
	}
	/**
	 * This method initializes doneDelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getDoneDelButton() {
		if (doneDelButton == null) {
			doneDelButton = new JButton();
			doneDelButton.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Done_(Delete)"));
			doneDelButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					dtcommon(true);
				}
			});
			doneDelButton.setIcon(new ImageIcon(getClass().getResource("/resource/Delete16.gif")));
		}
		return doneDelButton;
	}
    }  //  @jve:decl-index=0:visual-constraint="39,18"
