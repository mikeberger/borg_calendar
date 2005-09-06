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
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;

import net.sf.borg.common.io.IOHelper;
import net.sf.borg.common.ui.TablePrinter;
import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.XSLTransform;
import net.sf.borg.model.Address;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AddressVcardAdapter;
/**
 *
 * @author  MBERGER
 */

// the AddrListView displays a list of the current todo items and allows the
// suer to mark them as done
public class AddrListView extends View
{


    private Collection addrs_;   // list of rows currently displayed

    private static AddrListView singleton = null;
	public static AddrListView getReference()
    {
        if( singleton == null || !singleton.isShowing())
            singleton = new AddrListView();
        return( singleton );
    }

    private AddrListView()
    {

        super();
        addModel( AddressModel.getReference() );

        // init the gui components
        initComponents();

        // the todos will be displayed in a sorted table with 2 columns -
        // data and todo text
        jTable1.setModel(new TableSorter(
        new String []
        { Resource.getResourceString("First"), Resource.getResourceString("Last"), Resource.getResourceString("Email"), Resource.getResourceString("Screen_Name"), Resource.getResourceString("Home_Phone"), Resource.getResourceString("Work_Phone"),
               Resource.getResourceString("Birthday") },
        new Class []
        {
            java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,
            java.lang.String.class, java.util.Date.class
        }));

        refresh();

        manageMySize(PrefName.ADDRLISTVIEWSIZE);

    }

    public void destroy()
    {
        this.dispose();
    }

    public void refresh()
    {
        AddressModel addrmod_ = AddressModel.getReference();

        try
        {
            addrs_ = addrmod_.getAddresses();
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
            return;
        }


        // init the table to empty
        TableSorter tm = (TableSorter) jTable1.getModel();
        tm.addMouseListenerToHeaderInTable(jTable1);
        tm.setRowCount(0);

        Iterator it = addrs_.iterator();
        while( it.hasNext() )
        {
            Address r = (Address) it.next();

            try
            {

                // add the table row
                Object [] ro = new Object[7];
                ro[0] = r.getFirstName();
                ro[1] = r.getLastName();
                ro[2] = r.getEmail();
                ro[3] = r.getScreenName();
                ro[4] = r.getHomePhone();
                ro[5] = r.getWorkPhone();
                ro[6] = r.getBirthday();
                tm.addRow(ro);
                tm.tableChanged(new TableModelEvent(tm));
            }
            catch( Exception e )
            {
                Errmsg.errmsg(e);
                return;
            }

        }

        // sort the table by last name
        tm.sortByColumn(1);

    }

    private void editRow()
    {
        // figure out which row is selected to be marked as done
        int index =  jTable1.getSelectedRow();
        if( index == -1 ) return;

        try
        {
            // need to ask the table for the original (befor sorting) index of the selected row
            TableSorter tm = (TableSorter) jTable1.getModel();
            int k = tm.getMappedIndex(index);  // get original index - not current sorted position in tbl
            Object[] oa = addrs_.toArray();
            Address addr = (Address) oa[k];
            new AddressView( addr ).setVisible(true);
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        newbutton = new javax.swing.JButton();
        editbutton = new javax.swing.JButton();
        delbutton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        printList = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();

        this.setContentPane(getJPanel());
        //getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        ResourceHelper.setTitle(this, "Address_Book");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        jScrollPane1.setPreferredSize(new java.awt.Dimension(554, 404));
        jTable1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jTable1.setGridColor(java.awt.Color.blue);
        DefaultListSelectionModel mylsmodel = new DefaultListSelectionModel();
        mylsmodel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
        jTable1.setSelectionModel(mylsmodel
        );
        jTable1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jTable1MouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        newbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Add16.gif")));
        ResourceHelper.setText(newbutton, "Add_New");
        newbutton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                newbuttonActionPerformed(evt);
            }
        });

        jPanel1.add(newbutton);

        editbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Edit16.gif")));
        ResourceHelper.setText(editbutton, "Edit");
        editbutton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                editbuttonActionPerformed(evt);
            }
        });

        jPanel1.add(editbutton);

        delbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Delete16.gif")));
        ResourceHelper.setText(delbutton, "Delete");
        delbutton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                delbuttonActionPerformed(evt);
            }
        });

        jPanel1.add(delbutton);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Stop16.gif")));
        ResourceHelper.setText(jButton1, "Dismiss");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });
        setDismissButton(jButton1);

        jPanel1.add(jButton1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1, gridBagConstraints);

        ResourceHelper.setText(fileMenu, "Action");
        ResourceHelper.setText(printList, "Print_List");
        printList.setIcon(new ImageIcon(getClass().getResource("/resource/Print16.gif")));
        printList.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                printListActionPerformed(evt);
            }
        });

        fileMenu.add(printList);

        ResourceHelper.setText(exitMenuItem, "Exit");
        exitMenuItem.setIcon(new ImageIcon(getClass().getResource("/resource/Stop16.gif")));
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitMenuItemActionPerformed(evt);
            }
        });

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        fileMenu.add(getImpvcard());
        fileMenu.add(getHtmlitem());
        fileMenu.add(exitMenuItem);

        pack();
    }//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void delbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_delbuttonActionPerformed
    {//GEN-HEADEREND:event_delbuttonActionPerformed
        // figure out which row is selected to be marked as done
        int index =  jTable1.getSelectedRow();
        if( index == -1 ) return;

        try
        {
            // need to ask the table for the original (befor sorting) index of the selected row
            TableSorter tm = (TableSorter) jTable1.getModel();
            int k = tm.getMappedIndex(index);  // get original index - not current sorted position in tbl
            Object[] oa = addrs_.toArray();
            Address addr = (Address) oa[k];
            AddressModel amod = AddressModel.getReference();
            amod.delete( addr );

        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_delbuttonActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTable1MouseClicked
    {//GEN-HEADEREND:event_jTable1MouseClicked
        if( evt.getClickCount() < 2 ) return;
        editRow();
    }//GEN-LAST:event_jTable1MouseClicked

    private void editbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editbuttonActionPerformed
    {//GEN-HEADEREND:event_editbuttonActionPerformed
        editRow();
    }//GEN-LAST:event_editbuttonActionPerformed

    private void newbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newbuttonActionPerformed
    {//GEN-HEADEREND:event_newbuttonActionPerformed
        Address addr = AddressModel.getReference().newAddress();
        addr.setKey(-1);
        new AddressView(addr).setVisible(true);
    }//GEN-LAST:event_newbuttonActionPerformed

    private void printListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printListActionPerformed

        // user has requested a print of the table
        try
        { TablePrinter.printTable(jTable1); }
        catch( Exception e )
        { Errmsg.errmsg(e); }
    }//GEN-LAST:event_printListActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        this.dispose();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        this.dispose();
    }//GEN-LAST:event_exitForm

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton delbutton;
    private javax.swing.JButton editbutton;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton newbutton;
    private javax.swing.JMenuItem printList;
	private JPanel jPanel = null;
	private JMenuItem htmlitem = null;
	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
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
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.insets = new java.awt.Insets(4,4,4,4);
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			jPanel.add(jScrollPane1, gridBagConstraints1);
			jPanel.add(jPanel1, gridBagConstraints2);
		}
		return jPanel;
	}
	/**
	 * This method initializes htmlitem
	 *
	 * @return javax.swing.JMenuItem
	 */
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
			            AddressModel.getReference().export(sw);
			            String sorted = XSLTransform.transform( sw.toString(), "/resource/addrsort.xsl");
			            String output = XSLTransform.transform( sorted, "/resource/addr.xsl");
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

	private JMenuItem impvcard = null;
	private JMenuItem getImpvcard() {
		if (impvcard == null) {
			impvcard = new JMenuItem();
			ResourceHelper.setText(impvcard, "imp_vcard");
			impvcard.setIcon(new ImageIcon(getClass().getResource("/resource/Import16.gif")));
			impvcard.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
			        File file;
			        while( true ) {
			            // prompt for a file
			            JFileChooser chooser = new JFileChooser();

			            chooser.setCurrentDirectory( new File(".") );
			            chooser.setDialogTitle(Resource.getResourceString("choose_file"));
			            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			            int returnVal = chooser.showOpenDialog(null);
			            if(returnVal != JFileChooser.APPROVE_OPTION)
			                return;

			            String s = chooser.getSelectedFile().getAbsolutePath();
			            file = new File(s);
			            String err = null;

			            if( err == null )
			                break;

			            Errmsg.notice( err );
			        }

			    	try {
			    		FileReader r = new FileReader(file);
						AddressVcardAdapter.importVcard(r);
						r.close();
					}
			    	catch (Exception ex) {
						Errmsg.errmsg(ex);
					}


				}
			});
		}
		return impvcard;
	}
  }
