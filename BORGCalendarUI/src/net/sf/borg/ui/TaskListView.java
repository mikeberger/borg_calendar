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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.io.OSServicesHome;
import net.sf.borg.common.ui.TablePrinter;
import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Version;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.DBException;

/*
 * btgui.java
 *
 * Created on August 3, 2001, 11:09 AM
 */


/**
 *
 * @author  MBERGER
 * @version
 */


// task tracker main window
// this view shows a list of tasks in a table format with all kinds
// of sorting/filtering options. It is really like the "main" window
// for a whole task traking application separate from the calendar
// application. In prior non-java versions of BORG, the task tracker
// and calendar apps were completely separate apps.
public class TaskListView extends View {
    
    static {
        Version.addVersion("$Id$");
    }
    
    
    private TableCellRenderer defrend_;   // default table cell renderer which I need to
    // remember because I am overwriting it, but sometimes need to use it
    
    private static String allcats = Resource.getResourceString("All_Categories");
    
    private static TaskListView singleton = null;
	public static TaskListView getReference() {
        if( singleton == null || !singleton.isShowing())
            singleton = new TaskListView();
        return( singleton );
    }
    
    // override class for the default table cell renderer that will change the colors of table cells
    // based on days left before due date
    private class DLRenderer extends JLabel implements TableCellRenderer {
        
        public DLRenderer() {
            super();
            setOpaque(true); //MUST do this for background to show up.
        }
        
        public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
            
            int i = ((Integer) obj).intValue();
            
            // ok - if we are not drawing column 5 (days left) or if we are drawing days left
            // but days left is >10 and not the 9999 (infinite) value, then just use the default
            // rendered for this cell
            if( column != 6 || (i != 9999 && i >= 10))
                return defrend_.getTableCellRendererComponent( table, obj, isSelected,
                hasFocus, row, column );
            
            // add color to the days-left column (5) as the task due date
            // approaches
            
            // default to white background unless row is selected
            this.setBackground( Color.white );
            if( isSelected )
                this.setBackground( new Color( 204,204,255 ));
            
            // default to black foreground
            this.setForeground( Color.black );
            
            // render the text in the box to be the number of days left
            this.setText( ((Integer) obj).toString() );
            
            // align to the right
            this.setHorizontalAlignment(RIGHT);
            
            // 9999 is used if no due date
            // so show stars - but don't alter the color
            if( i == 9999 )
                this.setText("******");
            
            // yellow alert -- <10 days left
            if( i < 10 )
                this.setBackground( Color.yellow );
            
            // red alert -- <2 days left
            if( i < 2 ) {
                this.setBackground( Color.red );
                this.setForeground( Color.white );
            }
            
            return this;
        }
    }
    
    /** Creates new form btgui */
    private TaskListView() {
        super();
        
        addModel(TaskModel.getReference());
        
        initComponents();
        
        
        
        // add open/closed/all radiobuttons
        // and select "open"
        buttonGroup = new ButtonGroup();
        buttonGroup.add( jRadioButton7 );
        buttonGroup.add( jRadioButton8 );
        buttonGroup.add( jRadioButton9 );
        buttonGroup.add( jRadioButton1 );
        jRadioButton7.setSelected(  true);
        
        // add scroll to the table
        jScrollPane1.setViewportView(jTable1);
        jScrollPane1.getViewport().setBackground( menuBar.getBackground());
        
        // save table cell default renderer for when the custom one is not being used
        defrend_ = jTable1.getDefaultRenderer(Integer.class);
        
        // set renderer to the custom one for integers
        jTable1.setDefaultRenderer(Integer.class, new TaskListView.DLRenderer());
        
        // use a sorted table model
        jTable1.setModel(new TableSorter(
        new String []
        { Resource.getResourceString("Item_#"), Resource.getResourceString("Status"), Resource.getResourceString("Type"), Resource.getResourceString("Category"), Resource.getResourceString("Start_Date"), Resource.getResourceString("Due_Date"), Resource.getResourceString("Days_Left"),  Resource.getResourceString("Description")  },
        new Class [] {
            java.lang.Integer.class,
            java.lang.String.class,
            java.lang.String.class,
            java.lang.String.class,
            Date.class,
            Date.class,
            java.lang.Integer.class,
            java.lang.String.class
        }
        ));
        
        // set up for sorting when a column header is clicked
        TableSorter tm = (TableSorter) jTable1.getModel();
        tm.addMouseListenerToHeaderInTable(jTable1);
        
        // clear all rows
        deleteAll();
        
        // set column widths
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(80);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(80);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(80);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(80);
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(120);
        jTable1.getColumnModel().getColumn(5).setPreferredWidth(120);
        jTable1.getColumnModel().getColumn(6).setPreferredWidth(80);
        jTable1.getColumnModel().getColumn(7).setPreferredWidth(400);
        jTable1.setPreferredScrollableViewportSize(new Dimension( 900,400 ));
        catbox.addItem( allcats );
        catbox.setSelectedItem(allcats );

        catal_ = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                catboxActionPerformed(evt);
            }
        };
        catbox.addActionListener(catal_);
        
        pack();
    }
    
    private java.awt.event.ActionListener catal_ = null;
    public void destroy() {
        this.dispose();
    }
    
    // add a row to the sorted table
    private void addRow( Object [] ro ) {
        TableSorter tm =   (TableSorter)jTable1.getModel();
        tm.addRow(ro);
        tm.tableChanged(new TableModelEvent(tm));
    }
    
    // delete all rows from the sorted table
    private void deleteAll() {
        TableSorter tm = (TableSorter) jTable1.getModel();
        tm.setRowCount(0);
        tm.tableChanged(new TableModelEvent(tm));
    }
    
    // do the default sort - by days left - column 5
    private void defsort() {
        TableSorter tm = (TableSorter) jTable1.getModel();
        if( !tm.isSorted() )
            tm.sortByColumn(6);
        else
            tm.sort();
    }
    
    // resize table based on row count
    private void resize() {
        int row = jTable1.getRowCount();
        jTable1.setPreferredSize(new Dimension(1000, row*16));
        
    }
    
    // get the filter string typed by the user
    private String filter() {
        return( jTextField3.getText() );
    }
    
    // return open/closed/all as an integer based on the radiobuttons
    private int which() {
        if( jRadioButton7.isSelected() == true )
            return(1);
        if( jRadioButton8.isSelected() == true )
            return(2);
        if( jRadioButton9.isSelected() == true )
            return(3);
        return(4);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        java.awt.GridBagConstraints gridBagConstraints;

        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jRadioButton9 = new javax.swing.JRadioButton();
        jButton21 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        catbox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jRadioButton1 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        printit = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        add = new javax.swing.JMenuItem();
        change = new javax.swing.JMenuItem();
        clone = new javax.swing.JMenuItem();
        delete = new javax.swing.JMenuItem();
        close = new javax.swing.JMenuItem();
        optMenu = new javax.swing.JMenu();
        impst = new javax.swing.JMenuItem();
        expst = new javax.swing.JMenuItem();
        resetst = new javax.swing.JMenuItem();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Task_Tracking"));
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        jRadioButton7.setBackground(menuBar.getBackground());
        jRadioButton7.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Open"));
        jRadioButton7.setName("which");
        jRadioButton7.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButton7ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jRadioButton7, gridBagConstraints);

        jRadioButton8.setBackground(menuBar.getBackground());
        jRadioButton8.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Closed"));
        jRadioButton8.setName("which");
        jRadioButton8.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButton8ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jRadioButton8, gridBagConstraints);

        jRadioButton9.setBackground(menuBar.getBackground());
        jRadioButton9.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("All"));
        jRadioButton9.setName("which");
        jRadioButton9.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButton9ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jRadioButton9, gridBagConstraints);

        jButton21.setBackground(menuBar.getBackground());
        jButton21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Find16.gif")));
        jButton21.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Filter:"));
        jButton21.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton21ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jButton21, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jTextField3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(catbox, gridBagConstraints);

        jScrollPane1.setBackground(menuBar.getBackground());
        jScrollPane1.setViewport(jScrollPane1.getViewport());
        jTable1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setGridColor(java.awt.Color.blue);
        jTable1.setPreferredSize(new java.awt.Dimension(700, 500));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                mouseClick(evt);
            }
        });

        jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jRadioButton1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("PerfRvw"));
        jRadioButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jRadioButton1, gridBagConstraints);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Category"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jLabel2, gridBagConstraints);

        fileMenu.setBackground(menuBar.getBackground());
        fileMenu.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("File"));
        printit.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Print_Task_List"));
        printit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                printitActionPerformed(evt);
            }
        });

        fileMenu.add(printit);

        exitMenuItem.setBackground(fileMenu.getBackground());
        exitMenuItem.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Close"));
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setBackground(menuBar.getBackground());
        editMenu.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Action"));
        add.setBackground(editMenu.getBackground());
        add.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Add"));
        add.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addActionPerformed(evt);
            }
        });

        editMenu.add(add);

        change.setBackground(editMenu.getBackground());
        change.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Change"));
        change.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                changeActionPerformed(evt);
            }
        });

        editMenu.add(change);

        clone.setBackground(editMenu.getBackground());
        clone.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Clone"));
        clone.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cloneActionPerformed(evt);
            }
        });

        editMenu.add(clone);

        delete.setBackground(menuBar.getBackground());
        delete.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Delete"));
        delete.setName("delete");
        delete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteActionPerformed(evt);
            }
        });

        editMenu.add(delete);

        close.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Close"));
        close.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                closeActionPerformed(evt);
            }
        });

        editMenu.add(close);

        menuBar.add(editMenu);

        optMenu.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Options"));
        impst.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Import_Task_States_From_XML"));
        impst.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                impstActionPerformed(evt);
            }
        });

        optMenu.add(impst);

        expst.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Export_Task_States_to_XML"));
        expst.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                expstActionPerformed(evt);
            }
        });

        optMenu.add(expst);

        resetst.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Reset_Task_States_to_Default"));
        resetst.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                resetstActionPerformed(evt);
            }
        });

        optMenu.add(resetst);

        menuBar.add(optMenu);

        setJMenuBar(menuBar);

    }//GEN-END:initComponents
        
    private void resetstActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_resetstActionPerformed
    {//GEN-HEADEREND:event_resetstActionPerformed
        try {
            String msg = Resource.getResourceString("reset_state_warning");
            int ret = JOptionPane.showConfirmDialog(null, msg, Resource.getResourceString("Import_WARNING"), JOptionPane.OK_CANCEL_OPTION);
            
            if( ret != JOptionPane.OK_OPTION )
                return;
            TaskModel taskmod_ = TaskModel.getReference();
            taskmod_.resetStates();
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_resetstActionPerformed
    
    private void impstActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_impstActionPerformed
    {//GEN-HEADEREND:event_impstActionPerformed
        
        // import a new task type and status model from a user XML file
        
        try {
            String msg = Resource.getResourceString("import_state_warning");
            int ret = JOptionPane.showConfirmDialog(null, msg, Resource.getResourceString("Import_WARNING"), JOptionPane.OK_CANCEL_OPTION);
            
            if( ret != JOptionPane.OK_OPTION )
                return;
            
            InputStream istr =
            	OSServicesHome
            		.getInstance()
            		.getServices()
            		.fileOpen
            		(
						".",
						Resource
							.getResourceString("Please_choose_File_to_Import_From")
					);
            
            TaskModel taskmod_ = TaskModel.getReference();
            taskmod_.importStates(istr);
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
        
    }//GEN-LAST:event_impstActionPerformed
    
    private void expstActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_expstActionPerformed
    {//GEN-HEADEREND:event_expstActionPerformed
        
        // export the current task type/state model to an XML file
        try {
			TaskModel taskmod_ = TaskModel.getReference();
			ByteArrayOutputStream ostr = new ByteArrayOutputStream();
			taskmod_.exportStates(ostr);
			byte[] buf = ostr.toByteArray();
			ByteArrayInputStream istr = new ByteArrayInputStream(buf);
			
			// Export XML to the file
			OSServicesHome
				.getInstance()
				.getServices()
				.fileSave(".", istr, "state_model.exp");
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
    }//GEN-LAST:event_expstActionPerformed
    
    private void printitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printitActionPerformed
        
        // print the current table of tasks
        try
        { TablePrinter.printTable(jTable1); }
        catch( Exception e )
        { Errmsg.errmsg(e); }
    }//GEN-LAST:event_printitActionPerformed
    
    private void closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeActionPerformed
        
        // force a task to closed state
        
        // get the task number from column 0 of the selected row
        int row = jTable1.getSelectedRow();
        if( row == -1 ) return;
        DefaultTableModel tm = (DefaultTableModel) jTable1.getModel();
        Integer num = (Integer) tm.getValueAt(row,0);
        try {
            // force close of the task
            TaskModel taskmod_ = TaskModel.getReference();
            taskmod_.close(num.intValue());
        }
        catch( Exception e) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_closeActionPerformed
    
    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // refresh because the user has changed the filtering criteria
        refresh();
    }//GEN-LAST:event_jRadioButton1ActionPerformed
    
    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
        
        // delete selected row
        
        // get task number from column 0 of the selected row
        int row = jTable1.getSelectedRow();
        if( row == -1 ) return;
        DefaultTableModel tm = (DefaultTableModel) jTable1.getModel();
        Integer num = (Integer) tm.getValueAt(row,0);
        
        // prompt for ok
        int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("Really_delete_number_") + num, "", JOptionPane.YES_NO_OPTION );
        if( ret == JOptionPane.YES_OPTION ) {
            // delete the task
            try {
                TaskModel taskmod_ = TaskModel.getReference();
                taskmod_.delete(num.intValue());
            }
            catch ( Exception e ) {
                Errmsg.errmsg(e);
            }
        }
    }//GEN-LAST:event_deleteActionPerformed
    
  private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
      // just call refresh when filter button pressed
      
      refresh( );
  }//GEN-LAST:event_jButton21ActionPerformed
  
  private void mouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseClick
      
      // ask controller to bring up task editor on double click
      if( evt.getClickCount() < 2 ) return;
      
      // get task number from column 0 of selected row
      int row = jTable1.getSelectedRow();
      if( row == -1 ) return;
      DefaultTableModel tm = (DefaultTableModel) jTable1.getModel();
      Integer num = (Integer) tm.getValueAt(row,0);
      
      // ask borg class to bring up a task editor window
      task_change(num.intValue());
      
  }//GEN-LAST:event_mouseClick
  
  private void changeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeActionPerformed
      
      // ask controller to bring up task editor
      
      // get task number from column 0 of selected row
      int row = jTable1.getSelectedRow();
      if( row == -1 ) return;
      DefaultTableModel tm = (DefaultTableModel) jTable1.getModel();
      Integer num = (Integer) tm.getValueAt(row,0);
      
      // ask borg class to bring up a task editor window
      task_change(num.intValue());
  }//GEN-LAST:event_changeActionPerformed
  
  private void cloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cloneActionPerformed
      
      // ask controller to bring up clone editor
      
      // get task number from column 0 of selected row
      int row = jTable1.getSelectedRow();
      if( row == -1 ) return;
      DefaultTableModel tm = (DefaultTableModel) jTable1.getModel();
      Integer num = (Integer) tm.getValueAt(row,0);
      
      // ask borg class to bring up a task editor window
      task_clone(num.intValue());
  }//GEN-LAST:event_cloneActionPerformed
  
  private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
      // ask controller to bring up new task editor
      task_add();
  }//GEN-LAST:event_addActionPerformed
  
  private void jRadioButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton9ActionPerformed
      // just call refeesh on radio button chg
      
      refresh();
  }//GEN-LAST:event_jRadioButton9ActionPerformed
  
  private void jRadioButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton8ActionPerformed
      // just call refeesh on radio button chg
      
      refresh();
  }//GEN-LAST:event_jRadioButton8ActionPerformed
  
  private void jRadioButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton7ActionPerformed
      // just call refeesh on radio button chg
      
      refresh();
  }//GEN-LAST:event_jRadioButton7ActionPerformed
  
    private void exitMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        this.dispose(); //System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        this.dispose();  //System.exit(0);
    }//GEN-LAST:event_exitForm
    
    private void catboxActionPerformed(java.awt.event.ActionEvent evt) {      
      refresh( );
  }
    // refresh is called to update the table of shown tasks due to model changes or if the user
    // changes the filtering criteria
    public void refresh() {
        
        // get radiobutton setting (open/close/all/perfrvw)
        int which = which();
        
        int row = 0;
        
        // clear all table rows
        deleteAll();
        
        // get any filter string the user has typed
        String filt = filter();
        String cat = (String) catbox.getSelectedItem();
        if( cat.equals(allcats)) cat = null;
        
        try {
            TaskModel taskmod_ = TaskModel.getReference();
            
            // check for new categories
            // this code avoids blindly replacing the combobox model
            // on every refresh
            Collection cats = taskmod_.getCategories();
            int numcats = cats.size();
            int numcombo = catbox.getItemCount() - 1; // subtract "ALL" row
            if( numcats != numcombo ) {
                
                // init categories
                catbox.removeActionListener( catal_ );
                catbox.removeAllItems();
                catbox.addItem( allcats );
                
                Iterator it = cats.iterator();
                while( it.hasNext()) {
                    String c = (String) it.next();
                    catbox.addItem(c);
                    if( c.equals(cat) )
                        catbox.setSelectedItem(c);
                }
               
                catbox.addActionListener( catal_ );

            }
            Collection tasks = taskmod_.getTasks();
            Iterator ti = tasks.iterator();
            while( ti.hasNext() ) {
                
                Task task = (Task) ti.next();
                
                // get the task state
                String st = task.getState();
                
                // filter on open/closed/all
                
                // show open
                if( which == 1 && (st.equals("CLOSED") || st.equals("PR")))
                    continue;
                
                // show closed
                if( which == 2 && !st.equals("CLOSED") && !st.equals("PR") )
                    continue;
                
                // show perf rvw
                if( which == 4 && !st.equals("PR") )
                    continue;
                
                
                // filter on user filter string
                if( filt.length() != 0 ) {
                    
                    // check if string is in description
                    // or resolution
                    String d = task.getDescription();
                    String r = task.getResolution();
                    
                    if( r == null ) r = "";
                    if( d == null ) d = "";
                    
                    if( d.indexOf(filt) == -1 &&
                    r.indexOf(filt) == -1 )
                        continue;
                }
                
                if( cat != null ) {
                    String tcat = task.getCategory();
                    if( tcat == null) continue;
                    if( !tcat.equals(cat)) continue;
                }
                
                // if we get here - we are displaying this task as a row
                // so fill in an array of objects for the row
                Object [] ro = new Object[8];
                ro[0] = task.getTaskNumber();   // task number
                ro[1] = task.getState();   // task state
                ro[2] = task.getType();   // task type
                ro[3] = task.getCategory();
                ro[4] = task.getStartDate();     // task start date
                ro[5] = task.getDueDate();     // task due date
                
                // calculate days left - today - duedate
                if( ro[5] == null )
                    // 9999 days left if no due date - this is a (cringe, ack, thptt) magic value
                    ro[6] = new Integer(9999);
                else {
                    Date dd = (Date) ro[5];
                    Date now = new Date();
                    int days = new Long((dd.getTime() - now.getTime()) / (1000*60*60*24)).intValue();
                    days++;
                    // if due date is past, set days left to 0
                    // negative days are silly
                    if(days < 0) days = 0;
                    ro[6] = new Integer(days);
                }
                
                
                // strip newlines from the description
                String de = task.getDescription();
                String tmp = "";
                for( int i = 0; de != null && i < de.length(); i++ ) {
                    char c = de.charAt(i);
                    if( c == '\n' || c == '\r' )
                        continue;
                    tmp += c;
                }
                ro[7] = tmp;
                
                // add the task row to table
                addRow(ro);
                row++;
            }
        }
        catch( DBException e ) {
            if( e.getRetCode() != DBException.RET_NOT_FOUND ) {
                Errmsg.errmsg(e);
                System.exit(1);
            }
            
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
            System.exit(1);
        }
        
        // resize the table based on new row count
        resize();
        
        // apply default sort to the table
        defsort();
    }
    
    private void task_clone( int tasknum ) {
        
        try {
            // get the task
            TaskModel taskmod_ = TaskModel.getReference();
            Task task = taskmod_.getMR(tasknum);
            if( task == null )
                return;
            
            // display the task editor
            TaskView tskg = TaskView.getReference(task, TaskView.T_CLONE);
            tskg.show();
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
            System.exit(1);
        }
        
        
    }
    
    // show the task view - to edit a task
    private void task_change( int tasknum ) {
        
        try {
            // get the task from the data model
            TaskModel taskmod_ = TaskModel.getReference();
            Task task = taskmod_.getMR(tasknum);
            if( task == null )
                return;
            
            // display the task editor
            TaskView tskg = TaskView.getReference(task, TaskView.T_CHANGE);
            tskg.show();
            
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
            System.exit(1);
        }
        
        
    }
    
    // show task view - to add a new task
    private void task_add() {
        try {
            // display the task editor
            TaskView tskg = TaskView.getReference(null, TaskView.T_ADD);
            tskg.show();
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
            System.exit(1);
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem add;
    private javax.swing.JComboBox catbox;
    private javax.swing.JMenuItem change;
    private javax.swing.JMenuItem clone;
    private javax.swing.JMenuItem close;
    private javax.swing.JMenuItem delete;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem expst;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem impst;
    private javax.swing.JButton jButton21;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JRadioButton jRadioButton9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu optMenu;
    private javax.swing.JMenuItem printit;
    private javax.swing.JMenuItem resetst;
    // End of variables declaration//GEN-END:variables
    private ButtonGroup buttonGroup;
    
}
