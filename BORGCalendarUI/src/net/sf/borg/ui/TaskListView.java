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
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.io.IOHelper;
import net.sf.borg.common.ui.TablePrinter;
import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.XSLTransform;
import net.sf.borg.common.util.XTree;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.TaskXMLAdapter;
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
public class TaskListView extends View {



    private TableCellRenderer defrend_; // default table cell renderer which I
    // need to

    // remember because I am overwriting it, but sometimes need to use it

    private static TaskListView singleton = null;

    public static TaskListView getReference() {
        if (singleton == null || !singleton.isShowing())
            singleton = new TaskListView();
        return (singleton);
    }

    // override class for the default table cell renderer that will change the
    // colors of table cells
    // based on days left before due date
    private class DLRenderer extends JLabel implements TableCellRenderer {

        public DLRenderer() {
            super();
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(JTable table,
                Object obj, boolean isSelected, boolean hasFocus, int row,
                int column) {

            int i = ((Integer) obj).intValue();

            // ok - if we are not drawing column 5 (days left) or if we are
            // drawing days left
            // but days left is >10 and not the 9999 (infinite) value, then just
            // use the default
            // rendered for this cell
            if (column != 8 || (i != 9999 && i >= 10))
                return defrend_.getTableCellRendererComponent(table, obj,
                        isSelected, hasFocus, row, column);

            // add color to the days-left column as the task due date
            // approaches

            // default to white background unless row is selected
            this.setBackground(Color.white);
            if (isSelected)
                this.setBackground(new Color(204, 204, 255));

            // default to black foreground
            this.setForeground(Color.black);

            // render the text in the box to be the number of days left
            this.setText(((Integer) obj).toString());

            // align to the right
            this.setHorizontalAlignment(RIGHT);

            // 9999 is used if no due date
            // so show stars - but don't alter the color
            if (i == 9999)
                this.setText("******");

            // yellow alert -- <10 days left
            if (i < 10)
                this.setBackground(Color.yellow);

            // red alert -- <2 days left
            if (i < 2) {
                this.setBackground(Color.red);
                this.setForeground(Color.white);
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
        buttonGroup.add(jRadioButton7);
        buttonGroup.add(jRadioButton8);
        buttonGroup.add(jRadioButton9);
        buttonGroup.add(jRadioButton1);
        jRadioButton7.setSelected(true);

        // add scroll to the table
        jScrollPane1.setViewportView(taskTable);
        //jScrollPane1.getViewport().setBackground( menuBar.getBackground());

        // save table cell default renderer for when the custom one is not being
        // used
        defrend_ = taskTable.getDefaultRenderer(Integer.class);

        // set renderer to the custom one for integers
        taskTable
                .setDefaultRenderer(Integer.class,
                        new TaskListView.DLRenderer());

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
                Resource.getPlainResourceString("Description") }, new Class[] {
                java.lang.Integer.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, Date.class,
                Date.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class }));

        // set up for sorting when a column header is clicked
        TableSorter tm = (TableSorter) taskTable.getModel();
        tm.addMouseListenerToHeaderInTable(taskTable);

        // clear all rows
        deleteAll();

        // set column widths
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        taskTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        taskTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(8).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(9).setPreferredWidth(400);
        taskTable.setPreferredScrollableViewportSize(new Dimension(900, 400));

        pack();

        manageMySize(PrefName.TASKLISTVIEWSIZE);
    }



    public void destroy() {
        this.dispose();
    }

    // add a row to the sorted table
    private void addRow(Object[] ro) {
        TableSorter tm = (TableSorter) taskTable.getModel();
        tm.addRow(ro);
        tm.tableChanged(new TableModelEvent(tm));
    }

    // delete all rows from the sorted table
    private void deleteAll() {
        TableSorter tm = (TableSorter) taskTable.getModel();
        tm.setRowCount(0);
        tm.tableChanged(new TableModelEvent(tm));
    }

    // do the default sort - by days left - column 5
    private void defsort() {
        TableSorter tm = (TableSorter) taskTable.getModel();
        if (!tm.isSorted())
            tm.sortByColumn(6);
        else
            tm.sort();
    }

    // resize table based on row count
    private void resize() {
        int row = taskTable.getRowCount();
        taskTable.setPreferredSize(new Dimension(1000, row * 16));

    }

    // get the filter string typed by the user
    private String filter() {
        return (jTextField3.getText());
    }

    // return open/closed/all as an integer based on the radiobuttons
    private int which() {
        if (jRadioButton7.isSelected() == true)
            return (1);
        if (jRadioButton8.isSelected() == true)
            return (2);
        if (jRadioButton9.isSelected() == true)
            return (3);
        return (4);
    }

    private ActionListener getAL(JMenuItem mnuitm)
    {
    	return mnuitm.getActionListeners()[0];
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jRadioButton9 = new javax.swing.JRadioButton();
        jButton21 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        taskTable = new javax.swing.JTable();
        jRadioButton1 = new javax.swing.JRadioButton();
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
        edittypes = new javax.swing.JMenuItem();
        impst = new javax.swing.JMenuItem();
        expst = new javax.swing.JMenuItem();
        resetst = new javax.swing.JMenuItem();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        ResourceHelper.setTitle(this, "Task_Tracking");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        ResourceHelper.setText(jRadioButton7, "Open");
        jRadioButton7.setName("which");
        jRadioButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton7ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jRadioButton7, gridBagConstraints);

        ResourceHelper.setText(jRadioButton8, "Closed");
        jRadioButton8.setName("which");
        jRadioButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton8ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jRadioButton8, gridBagConstraints);

        ResourceHelper.setText(jRadioButton9, "All");
        jRadioButton9.setName("which");
        jRadioButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton9ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jRadioButton9, gridBagConstraints);

        jButton21.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/resource/Find16.gif")));
        ResourceHelper.setText(jButton21, "Filter:");
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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



        //jScrollPane1.setViewport(jScrollPane1.getViewport());
        jScrollPane1.setViewportView(taskTable);
        jScrollPane1.setBorder(javax.swing.BorderFactory
                .createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        taskTable.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(
                0, 0, 0)));
        taskTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        taskTable.setGridColor(java.awt.Color.blue);
        taskTable.setPreferredSize(new java.awt.Dimension(700, 500));
        taskTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mouseClick(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        ResourceHelper.setText(jRadioButton1, "PerfRvw");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jRadioButton1, gridBagConstraints);

        ResourceHelper.setText(fileMenu, "File");
        ResourceHelper.setText(printit, "Print_Task_List");
        printit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printitActionPerformed(evt);
            }
        });

        fileMenu.add(printit);

        ResourceHelper.setText(exitMenuItem, "Exit");
        exitMenuItem.setIcon(new ImageIcon(getClass().getResource("/resource/Stop16.gif")));
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(getHtmlitem());
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        ResourceHelper.setText(editMenu, "Action");
        ResourceHelper.setText(add, "Add");
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        editMenu.add(add);

        ResourceHelper.setText(change, "Change");
        change.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeActionPerformed(evt);
            }
        });

        editMenu.add(change);

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

        menuBar.add(editMenu);
        
        new PopupMenuHelper
        (
        	taskTable,
        	new PopupMenuHelper.Entry[]
        	{
        		new PopupMenuHelper.Entry(getAL(add), "Add"),
        		new PopupMenuHelper.Entry(getAL(change), "Change"),
        		new PopupMenuHelper.Entry(getAL(clone), "Clone"),
        		new PopupMenuHelper.Entry(getAL(delete), "Delete"),
        		new PopupMenuHelper.Entry(getAL(close), "Close"),
        	}
        );
       
        ResourceHelper.setText(optMenu, "Options");
        ResourceHelper.setText(edittypes, "edit_types");
        edittypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edittypesActionPerformed(evt);
            }
        });

        optMenu.add(edittypes);

        ResourceHelper.setText(impst, "Import_Task_States_From_XML");
        impst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                impstActionPerformed(evt);
            }
        });

        optMenu.add(impst);

        ResourceHelper.setText(expst, "Export_Task_States_to_XML");
        expst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expstActionPerformed(evt);
            }
        });

        optMenu.add(expst);

        ResourceHelper.setText(resetst, "Reset_Task_States_to_Default");
        resetst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetstActionPerformed(evt);
            }
        });

        optMenu.add(resetst);

        menuBar.add(optMenu);

        setJMenuBar(menuBar);

        this.setSize(612, 456);
        this.setContentPane(getJPanel());

			printit.setIcon(new ImageIcon(getClass().getResource("/resource/Print16.gif")));
			optMenu.add(getCatmenuitem());
    }

    private void edittypesActionPerformed(java.awt.event.ActionEvent evt) {
        try {
			TaskConfigurator.getReference().setVisible(true);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
    }

    private void resetstActionPerformed(java.awt.event.ActionEvent evt)
    {
        try {
            String msg = Resource.getResourceString("reset_state_warning");
            int ret = JOptionPane.showConfirmDialog(null, msg, Resource
                    .getResourceString("Import_WARNING"),
                    JOptionPane.OK_CANCEL_OPTION);

            if (ret != JOptionPane.OK_OPTION)
                return;
            TaskModel taskmod_ = TaskModel.getReference();
            taskmod_.getTaskTypes().loadDefault();
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }
    }

    private void impstActionPerformed(java.awt.event.ActionEvent evt)
    {

        // import a new task type and status model from a user XML file

        try {
            String msg = Resource.getResourceString("import_state_warning");
            int ret = JOptionPane.showConfirmDialog(null, msg, Resource
                    .getResourceString("Import_WARNING"),
                    JOptionPane.OK_CANCEL_OPTION);

            if (ret != JOptionPane.OK_OPTION)
                return;

            InputStream istr = IOHelper.fileOpen(
                            ".",
                            Resource
                                    .getResourceString("Please_choose_File_to_Import_From"));

            TaskModel taskmod_ = TaskModel.getReference();
            taskmod_.getTaskTypes().importStates(istr);
            taskmod_.saveTaskTypes(null);
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }

    }

    private void expstActionPerformed(java.awt.event.ActionEvent evt)
    {

        // export the current task type/state model to an XML file
        try {
            TaskModel taskmod_ = TaskModel.getReference();
            ByteArrayOutputStream ostr = new ByteArrayOutputStream();
            taskmod_.getTaskTypes().exportStates(ostr);
            byte[] buf = ostr.toByteArray();
            ByteArrayInputStream istr = new ByteArrayInputStream(buf);

            // Export XML to the file
            IOHelper.fileSave(".", istr,
                    "state_model.exp");
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }
    }

    private void printitActionPerformed(java.awt.event.ActionEvent evt) {

        // print the current table of tasks
        try {
            TablePrinter.printTable(taskTable);
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }
    }

    private void closeActionPerformed(java.awt.event.ActionEvent evt) {

        // force a task to closed state

        // get the task number from column 0 of the selected row
        int row = taskTable.getSelectedRow();
        if (row == -1)
            return;
        TableSorter tm = (TableSorter) taskTable.getModel();
        Integer num = (Integer) tm.getValueAt(row, 0);
        try {
            // force close of the task
            TaskModel taskmod_ = TaskModel.getReference();
            taskmod_.close(num.intValue());
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }
    }

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // refresh because the user has changed the filtering criteria
        refresh();
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

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {
        // just call refresh when filter button pressed
        refresh();
    }

    private void mouseClick(java.awt.event.MouseEvent evt) {

        // ask controller to bring up task editor on double click
        if (evt.getClickCount() < 2)
            return;

        // get task number from column 0 of selected row
        int row = taskTable.getSelectedRow();
        if (row == -1)
            return;
        TableSorter tm = (TableSorter) taskTable.getModel();
        Integer num = (Integer) tm.getValueAt(row, 0);

        // ask borg class to bring up a task editor window
        task_change(num.intValue());

    }

    private void changeActionPerformed(java.awt.event.ActionEvent evt) {

        // ask controller to bring up task editor

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

        // ask controller to bring up clone editor

        // get task number from column 0 of selected row
        int row = taskTable.getSelectedRow();
        if (row == -1)
            return;
        TableSorter tm = (TableSorter) taskTable.getModel();
        Integer num = (Integer) tm.getValueAt(row, 0);

        // ask borg class to bring up a task editor window
        task_clone(num.intValue());
    }

    private void addActionPerformed(java.awt.event.ActionEvent evt) {
        // ask controller to bring up new task editor
        task_add();
    }

    private void jRadioButton9ActionPerformed(java.awt.event.ActionEvent evt) {
        // just call refeesh on radio button chg
        refresh();
    }

    private void jRadioButton8ActionPerformed(java.awt.event.ActionEvent evt) {
        // just call refeesh on radio button chg

        refresh();
    }

    private void jRadioButton7ActionPerformed(java.awt.event.ActionEvent evt) {
        // just call refeesh on radio button chg

        refresh();
    }

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose(); 
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        this.dispose();
    }


    // refresh is called to update the table of shown tasks due to model changes
    // or if the user
    // changes the filtering criteria
    public void refresh() {

        // get radiobutton setting (open/close/all/perfrvw)
        int which = which();

        int row = 0;

        // clear all table rows
        deleteAll();

        // get any filter string the user has typed
        String filt = filter();


        try {
            TaskModel taskmod_ = TaskModel.getReference();


            Collection tasks = taskmod_.getTasks();
            Iterator ti = tasks.iterator();
            while (ti.hasNext()) {

                Task task = (Task) ti.next();

                // get the task state
                String st = task.getState();

                // filter on open/closed/all

                // show open
                if (which == 1 && (st.equals(TaskModel.getReference().getTaskTypes().getFinalState(task.getType())) || st.equals("PR")))
                    continue;

                // show closed
                if (which == 2 && !st.equals(TaskModel.getReference().getTaskTypes().getFinalState(task.getType())) && !st.equals("PR"))
                    continue;

                // show perf rvw
                if (which == 4 && !st.equals("PR"))
                    continue;

                // category
                String cat = task.getCategory();
                if( cat == null || cat.equals(""))
                    cat = CategoryModel.UNCATEGORIZED;

                if( !CategoryModel.getReference().isShown(cat))
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
                    
                    if( caseBox.isSelected())
                    {
                        if (d.indexOf(filt) == -1 && r.indexOf(filt) == -1)
                            continue;
                    }
                    else
                    {
                    	String lfilt = filt.toLowerCase();
                    	String ld = d.toLowerCase();
                    	String lr = r.toLowerCase();
                        if (ld.indexOf(lfilt) == -1 && lr.indexOf(lfilt) == -1)
                            continue;	
                    }

 
                }


                // if we get here - we are displaying this task as a row
                // so fill in an array of objects for the row
                Object[] ro = new Object[10];
                ro[0] = task.getTaskNumber(); // task number
                ro[1] = task.getState(); // task state
                ro[2] = task.getType(); // task type
                ro[3] = task.getCategory();
                ro[4] = task.getPriority();
                ro[5] = task.getStartDate(); // task start date
                ro[6] = task.getDueDate(); // task due date
               
                // calc elapsed time
                Date end = null;
                if( task.getState().equals("CLOSED"))
                {
                	end = task.getCD();
                }
                else
                {
                	end = new Date();
                }
                
                if( end == null )
                {
                	ro[7] = "*******";
                }
                else
                {
                	// curently, the dates do not record h/m/s, so can't get too accurate
                	long msecs = end.getTime() - task.getStartDate().getTime();
                	long hours = msecs / (1000 * 60 * 60);
                	//long min = msecs / (1000 * 60);
                	
                	int days = (int) (hours / 24);
                	//int hrs = (int) (hours % 24);
                	//int mins = (int) (min % 60);
                	if( days >= 1)
                		ro[7] = new Integer(days); //Integer.toString(days) + "d";
                	else
                		ro[7] = new Integer(0); //"<1d";
                }
                
                // calculate days left - today - duedate
                if (ro[6] == null)
                    // 9999 days left if no due date - this is a (cringe, ack,
                    // thptt) magic value
                    ro[8] = new Integer(9999);
                else {
                    Date dd = (Date)ro[6];
                    ro[8] = new Integer(TaskModel.daysLeft(dd));
                }

                // strip newlines from the description
                String de = task.getDescription();
                String tmp = "";
                for (int i = 0; de != null && i < de.length(); i++) {
                    char c = de.charAt(i);
                    if (c == '\n' || c == '\r')
                    {
                    	tmp += ' ';
                    	continue;
                    }
                        
                    tmp += c;
                }
                ro[9] = tmp;

                // add the task row to table
                addRow(ro);
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

    private void task_clone(int tasknum) {

        try {
            // get the task
            TaskModel taskmod_ = TaskModel.getReference();
            Task task = taskmod_.getMR(tasknum);
            if (task == null)
                return;

            // display the task editor
            TaskView tskg = new TaskView(task, TaskView.T_CLONE);
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
            TaskView tskg = new TaskView(task, TaskView.T_CHANGE);
            tskg.setVisible(true);

        } catch (Exception e) {
            Errmsg.errmsg(e);
        }

    }

    // show task view - to add a new task
    private void task_add() {
        try {
            // display the task editor
            TaskView tskg = new TaskView(null, TaskView.T_ADD);
            tskg.setVisible(true);
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }
    }

    private javax.swing.JMenuItem add;

    private javax.swing.JMenuItem change;

    private javax.swing.JMenuItem clone;

    private javax.swing.JMenuItem close;

    private javax.swing.JMenuItem delete;

    private javax.swing.JMenu editMenu;

    private javax.swing.JMenuItem edittypes;

    private javax.swing.JMenuItem exitMenuItem;

    private javax.swing.JMenuItem expst;

    private javax.swing.JMenu fileMenu;

    private javax.swing.JMenuItem impst;

    private javax.swing.JButton jButton21;

    private javax.swing.JRadioButton jRadioButton1;

    private javax.swing.JRadioButton jRadioButton7;

    private javax.swing.JRadioButton jRadioButton8;

    private javax.swing.JRadioButton jRadioButton9;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTable taskTable;

    private javax.swing.JTextField jTextField3;

    private javax.swing.JMenuBar menuBar;

    private javax.swing.JMenu optMenu;

    private javax.swing.JMenuItem printit;

    private javax.swing.JMenuItem resetst;

    private ButtonGroup buttonGroup;

    private JPanel jPanel = null;

    private JPanel jPanel2 = null;

	private JPanel jPanel1 = null;
	private JButton jButton = null;
    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 4;
            gridBagConstraints2.gridy = 1;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
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
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 4;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints1.gridwidth = 5;
            jPanel.add(jButton21, gridBagConstraints8);
            jPanel.add(jTextField3, gridBagConstraints9);
            jPanel.add(jScrollPane1, gridBagConstraints11);
            jPanel.add(getJPanel2(), gridBagConstraints15);
            jPanel.add(getJPanel1(), gridBagConstraints1);
            jPanel.add(getCaseBox(), gridBagConstraints2);
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
            GridLayout gridLayout16 = new GridLayout();
            jPanel2 = new JPanel();
            jPanel2.setLayout(gridLayout16);
            gridLayout16.setRows(-1);
            gridLayout16.setColumns(4);
            jPanel2.add(jRadioButton7, null);
            jPanel2.add(jRadioButton1, null);
            jPanel2.add(jRadioButton8, null);
            jPanel2.add(jRadioButton9, null);
        }
        return jPanel2;
    }
	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.add(getJButton(), null);
		}
		return jPanel1;
	}
	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private void disp(){ this.dispose(); }
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			ResourceHelper.setText(jButton, "Dismiss");
			jButton.setIcon(new ImageIcon(getClass().getResource("/resource/Stop16.gif")));
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					disp();
				}
			});
	        setDismissButton(jButton);
		}
		return jButton;
	}

	private JMenuItem htmlitem;
	private JMenuItem getHtmlitem() {
		if (htmlitem == null) {
			htmlitem = new JMenuItem();
			ResourceHelper.setText(htmlitem, "SaveHTML");
			htmlitem.setIcon(new ImageIcon(getClass().getResource("/resource/WebComponent16.gif")));
			htmlitem.addActionListener(new java.awt.event.ActionListener() {
			    public void actionPerformed(java.awt.event.ActionEvent e) {
			        try{

			            JFileChooser chooser = new JFileChooser();

			            chooser.setCurrentDirectory( new File(".") );
			            chooser.setDialogTitle(Resource.getResourceString("choose_file"));
			            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			            int returnVal = chooser.showOpenDialog(null);
			            if(returnVal != JFileChooser.APPROVE_OPTION)
			                return;

			            String s = chooser.getSelectedFile().getAbsolutePath();

			            OutputStream ostr = IOHelper.createOutputStream(s);
			            OutputStreamWriter fw = new OutputStreamWriter(ostr, "UTF8");

						StringWriter sw = new StringWriter();
						sw.write("<TASKS>\n" );
						// for each appt being shown in list
				        TableSorter tm = (TableSorter) taskTable.getModel();
				        TaskXMLAdapter txa = new TaskXMLAdapter();

				        for(int row=0; row < tm.getRowCount() ;row++)
				        {

				            Integer num = (Integer) tm.getValueAt(row, 0);
				            Task task = TaskModel.getReference().getMR(num.intValue());

							// convert to XML
				            XTree xt = txa.toXml(task);
				            sw.write(xt.toString());

				        }

				        sw.write("</TASKS>\n" );
						// pass through XSLT
			            String output = XSLTransform.transform( sw.toString(), "/resource/task.xsl");

			            fw.write(output);
			            fw.close();


			        }
			        catch( Exception ex) {
			            Errmsg.errmsg(ex);
			        }

			    }
			});
		}
		return htmlitem;
	}

	private JMenuItem catmenuitem;  //  @jve:decl-index=0:visual-constraint="73,12"
	private JCheckBox caseBox = null;
	private JMenuItem getCatmenuitem() {
		if (catmenuitem == null) {
			catmenuitem = new JMenuItem();
	        catmenuitem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Preferences16.gif")));
	        ResourceHelper.setText(catmenuitem, "choosecat");
	        catmenuitem.addActionListener(new java.awt.event.ActionListener() {
	        	public void actionPerformed(java.awt.event.ActionEvent e) {
	        	    CategoryChooser.getReference().setVisible(true);
	        	}
	        });
		}
		return catmenuitem;
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
  } //  @jve:decl-index=0:visual-constraint="55,54"
