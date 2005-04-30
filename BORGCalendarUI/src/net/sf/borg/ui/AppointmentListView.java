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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Version;
import net.sf.borg.control.Borg;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;
import de.wannawork.jcalendar.JCalendarComboBox;
public class AppointmentListView extends View implements ListSelectionListener {
    
    static {
        Version.addVersion("$Id$");
    }
    
    private int key_;
    private List alist_ = null;
    private AppointmentPanel apanel_ = null;
    private GregorianCalendar cal_ = null;
    
    
       private class TimeRenderer extends JLabel implements TableCellRenderer {
        
        public TimeRenderer() {
            super();
            setOpaque(true); //MUST do this for background to show up.
        }
        
        public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
            
            // default to white background unless row is selected
            this.setBackground( Color.white );
            if( isSelected )
                this.setBackground( new Color( 204,204,255 ));
            
            // default to black foreground
            this.setForeground( Color.black );
            
            Date d = (Date) obj;
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(d);
            if( cal.get(Calendar.HOUR_OF_DAY) == 0)
            {
                this.setText("--------");
            }
            else
            {
                SimpleDateFormat sdf = AppointmentModel.getTimeFormat();
                this.setText(sdf.format(d));
            }
            return this;
        }
    }
    
    /** Creates new form btgui */
    public AppointmentListView(int year, int month, int day) {
        super();
        
        addModel(AppointmentModel.getReference());
        addModel(TaskModel.getReference());
         
        initComponents();
 
        // add scroll to the table
        jScrollPane1.setViewportView(jTable1);
        jScrollPane1.getViewport().setBackground( menuBar.getBackground());
        
        // use a sorted table model
        jTable1.setModel(new TableSorter(
        new String []
        { Resource.getResourceString("Text"), Resource.getResourceString("Time") },
        new Class [] {
            java.lang.String.class,
            java.util.Date.class,
        }
        ));
        

        // set renderer to the custom one for time
        jTable1.setDefaultRenderer(java.util.Date.class, new AppointmentListView.TimeRenderer());
        
        // set up for sorting when a column header is clicked
        TableSorter tm = (TableSorter) jTable1.getModel();
        tm.addMouseListenerToHeaderInTable(jTable1);
        
 
        // set column widths
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(125);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(75);
        
        jTable1.setPreferredScrollableViewportSize(new Dimension( 150,100 ));
        
        ListSelectionModel rowSM = jTable1.getSelectionModel();
        rowSM.addListSelectionListener(this);
        
        apanel_ = new AppointmentPanel( year, month, day );
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(apanel_, gridBagConstraints);

        showDate( year,month,day);
        
        pack();
        
        manageMySize(PrefName.APPTLISTVIEWSIZE);
    }
    
    private void showDate(int year, int month, int day)
    {
        cal_ = new GregorianCalendar(year,month,day);
        getDateCB().setCalendar(cal_);
        key_ = AppointmentModel.dkey( year, month, day );
        Date d = cal_.getTime();
        setTitle( Resource.getResourceString("Appointment_Editor_for_") + DateFormat.getDateInstance(DateFormat.SHORT).format(d));
        
        // clear all rows
        deleteAll();
        apanel_.setDate(year, month, day );
        apanel_.showapp(-1);
         
        refresh();
    }
    
    public void showApp(int key)
    {
        apanel_.showapp(key);
    }
    
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
    
    // do the default sort - by text
    private void defsort() {
        TableSorter tm = (TableSorter) jTable1.getModel();
        if( !tm.isSorted() )
            tm.sortByColumn(1);
        else
            tm.sort();
    }
    
    // resize table based on row count
    private void resize() {
        int row = jTable1.getRowCount();
        jTable1.setPreferredSize(new Dimension(150, row*16));
        
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        

        GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        add = new javax.swing.JButton();
        del = new javax.swing.JButton();
        delone = new javax.swing.JButton();
        dismiss = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();

        //getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Appointment_Editor"));
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        jPanel2.setLayout(new java.awt.GridBagLayout());
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("apptlist")));
        jScrollPane1.setBorder(null);
        jScrollPane1.setViewport(jScrollPane1.getViewport());
        jTable1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setGridColor(java.awt.Color.blue);
        jTable1.setPreferredSize(new java.awt.Dimension(700, 500));
        jScrollPane1.setViewportView(jTable1);

        GridBagConstraints gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 10.0;
        gridBagConstraints2.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        add.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Edit16.gif")));
        add.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("EditNew"));
        add.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addActionPerformed(evt);
            }
        });

        jPanel1.add(add);

        del.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Delete16.gif")));
        del.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Delete"));
        del.setToolTipText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("del_tip"));
        del.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                delActionPerformed(evt);
            }
        });

        jPanel1.add(del);

        delone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Delete16.gif")));
        delone.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Delete_One_Only"));
        delone.setToolTipText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("doo_tip"));
        delone.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deloneActionPerformed(evt);
            }
        });

        dismiss.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Stop16.gif")));
        dismiss.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Dismiss"));
        dismiss.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                dismissActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 2;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.insets = new java.awt.Insets(2, 2, 2, 2);
        fileMenu.setBackground(menuBar.getBackground());
        fileMenu.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("File"));
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

        setJMenuBar(menuBar);

        this.setContentPane(getJPanel());
        gridBagConstraints22.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints22.gridx = 0;
        gridBagConstraints22.gridy = 0;
        gridBagConstraints22.insets = new java.awt.Insets(2,2,2,2);
        jPanel3.add(getJPanel4(), gridBagConstraints22);
        jPanel3.add(jScrollPane1, gridBagConstraints2);
        jPanel1.add(delone, delone.getName());
        jPanel3.add(jPanel1, gridBagConstraints3);
        jPanel1.add(getReminderButton(), null);
        jPanel1.add(dismiss, dismiss.getName());
    }//GEN-END:initComponents
    
    private void deloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deloneActionPerformed
        int row = jTable1.getSelectedRow();
        if( row == -1 ) return;
        TableSorter tm = (TableSorter) jTable1.getModel();
        int i = tm.getMappedIndex(row);
        
        Integer apptkey = (Integer) alist_.get(i);
        AppointmentModel.getReference().delOneOnly(apptkey.intValue(),key_);
        jTable1.clearSelection();
    }//GEN-LAST:event_deloneActionPerformed
    
    private void delActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delActionPerformed
        int row = jTable1.getSelectedRow();
        if( row == -1 ) return;
        TableSorter tm = (TableSorter) jTable1.getModel();
        int i = tm.getMappedIndex(row);
        
        Integer apptkey = (Integer) alist_.get(i);
        AppointmentModel.getReference().delAppt(apptkey.intValue());
        jTable1.clearSelection();
    }//GEN-LAST:event_delActionPerformed
    
    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        jTable1.clearSelection();
        apanel_.showapp(-1);
    }//GEN-LAST:event_addActionPerformed
    
    private void dismissActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dismissActionPerformed
        this.dispose();
    }//GEN-LAST:event_dismissActionPerformed
    
    private void exitMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        this.dispose(); //System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        this.dispose();  //System.exit(0);
    }//GEN-LAST:event_exitForm
    
    public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages.
        if (e.getValueIsAdjusting()) return;
        
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (lsm.isSelectionEmpty()) {
            apanel_.showapp(-1);
            return;
        }
        int row = lsm.getMinSelectionIndex();
        //int row = jTable1.getSelectedRow();
        if( row == -1 ) return;
        TableSorter tm = (TableSorter) jTable1.getModel();
        int i = tm.getMappedIndex(row);
        
        Integer apptkey = (Integer) alist_.get(i);
        apanel_.showapp(apptkey.intValue());
        
    }
    
    // refresh is called to update the table of shown tasks due to model changes or if the user
    // changes the filtering criteria
    public void refresh() {
        
        // clear all table rows
        deleteAll();
        
        try {
            
            alist_ = AppointmentModel.getReference().getAppts(key_);
            if( alist_ != null ) {
                Iterator it = alist_.iterator();
                while( it.hasNext() ) {
                    Integer key = (Integer) it.next();
                    Appointment ap = AppointmentModel.getReference().getAppt(key.intValue());
                    
                    Object [] ro = new Object[2];
                    ro[0] = ap.getText();

                    // just get time
                    Date d = ap.getDate();
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(d);
                    cal.set(2000,1,1);
                    ro[1] = cal.getTime();
                    
                    addRow(ro);
                }
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
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add;
    private javax.swing.JButton del;
    private javax.swing.JButton delone;
    private javax.swing.JButton dismiss;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables
    
	private JPanel jPanel = null;
	private JPanel jPanel4 = null;
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.gridwidth = 1;
			gridBagConstraints11.gridheight = 1;
			gridBagConstraints11.weighty = 1.0D;
			gridBagConstraints11.weightx = 1.0D;
			gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.gridy = 0;
			gridBagConstraints21.gridheight = 1;
			gridBagConstraints21.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
			jPanel.add(jPanel2, gridBagConstraints11);
			jPanel.add(jPanel3, gridBagConstraints21);
		}
		return jPanel;
	}
	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */  
	private JCalendarComboBox cb_ = null;
	private JButton reminderButton = null;  //  @jve:decl-index=0:visual-constraint="702,73"
	private JCalendarComboBox getDateCB()
	{
		if (cb_ == null) {
			cb_ = new JCalendarComboBox();
			//cb.setCalendar(cal_);
			cb_.addChangeListener(new javax.swing.event.ChangeListener() { 
				public void stateChanged(javax.swing.event.ChangeEvent e) { 
				    Calendar cal = cb_.getCalendar();
					showDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
				}
			});
		}
		
		return( cb_ );
	}
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jPanel4 = new JPanel();
		
			jPanel4.add(getDateCB());
		}
		return jPanel4;
	}
	/**
	 * This method initializes reminderButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getReminderButton() {
		if (reminderButton == null) {
			reminderButton = new JButton();
			reminderButton.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("send_reminder"));
			reminderButton.setIcon(new ImageIcon(getClass().getResource("/resource/ComposeMail16.gif")));
			reminderButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					Borg.emailReminder(cal_);
				}
			});
		}
		return reminderButton;
	}
 }
