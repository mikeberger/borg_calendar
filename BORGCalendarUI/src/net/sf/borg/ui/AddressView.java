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

import java.text.DateFormat;
import java.util.Date;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Version;
import net.sf.borg.model.Address;
import net.sf.borg.model.AddressModel;



/**
 *
 * @author  MBERGER
 * @version
 */

class AddressView extends View
{
    
    static
    {
        Version.addVersion("$Id$");
    }
    
    private Address addr_;
    
    public AddressView(Address addr)
    {
        super();
        addr_ = addr;
        addModel( AddressModel.getReference() );
        
        initComponents();       // init the GUI widgets
        
        // display the window
        pack();
        showaddr();
    }
    
    public void destroy()
    {
        this.dispose();
    }
    
    public void refresh()
    {}
    
    private void showaddr()
    {
        fntext.setText( addr_.getFirstName() );
        lntext.setText( addr_.getLastName() );
        nntext.setText( addr_.getNickname() );
        emtext.setText( addr_.getEmail() );
        sntext.setText( addr_.getScreenName() );
        wptext.setText( addr_.getWorkPhone() );
        hptext.setText( addr_.getHomePhone() );
        fxtext.setText( addr_.getFax() );
        pgtext.setText( addr_.getPager() );
        wbtext.setText( addr_.getWebPage() );
        notestext.setText( addr_.getNotes() );
        satext.setText( addr_.getStreetAddress() );
        cttext.setText( addr_.getCity() );
        sttext.setText( addr_.getState() );
        cntext.setText( addr_.getCountry() );
        zctext.setText( addr_.getZip() );
        satext1.setText( addr_.getWorkStreetAddress() );
        cttext1.setText( addr_.getWorkCity() );
        sttext1.setText( addr_.getWorkState() );
        cntext1.setText( addr_.getWorkCountry() );
        zctext1.setText( addr_.getWorkZip() );
        comptext.setText( addr_.getCompany() );
        Date bd = addr_.getBirthday();
        if( bd != null )
        {
            DateFormat sdf = DateFormat.getDateInstance(DateFormat.SHORT);
            bdtext.setText( sdf.format(bd));
        }
        else
            bdtext.setText("");
    }
    
    private void saveaddr()
    {
        if( fntext.getText().equals("") || lntext.getText().equals("") )
        {
            Errmsg.notice(Resource.getResourceString("First_and_Last_name_are_Required") );
            return;
        }
        
        addr_.setFirstName( fntext.getText() );
        addr_.setLastName( lntext.getText() );
        addr_.setNickname( nntext.getText() );
        addr_.setEmail( emtext.getText() );
        addr_.setScreenName( sntext.getText() );
        addr_.setWorkPhone( wptext.getText() );
        addr_.setHomePhone( hptext.getText() );
        addr_.setFax( fxtext.getText() );
        addr_.setPager( pgtext.getText() );
        addr_.setWebPage( wbtext.getText() );
        addr_.setNotes( notestext.getText() );
        addr_.setStreetAddress( satext.getText() );
        addr_.setCity( cttext.getText() );
        addr_.setState( sttext.getText() );
        addr_.setCountry( cntext.getText() );
        addr_.setZip( zctext.getText() );
        addr_.setWorkStreetAddress( satext1.getText() );
        addr_.setWorkCity( cttext1.getText() );
        addr_.setWorkState( sttext1.getText() );
        addr_.setWorkCountry( cntext1.getText() );
        addr_.setWorkZip( zctext1.getText() );
        addr_.setCompany( comptext.getText() );
        
        Date bd = null;
        String bdt = bdtext.getText();
        if( bdt == null || bdt.equals(""))
        {
            addr_.setBirthday(null);
        }
        else
        {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            try
            {
                bd = df.parse(bdt);
            }
            catch( Exception e )
            {
                Errmsg.notice(Resource.getResourceString("invdate"));
                return;
            }
            
            if( bd != null )
                addr_.setBirthday(bd);
        }
        
        try
        {
            AddressModel.getReference().saveAddress( addr_ );
            this.dispose();
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        fntext = new javax.swing.JTextField();
        lntext = new javax.swing.JTextField();
        nntext = new javax.swing.JTextField();
        sntext = new javax.swing.JTextField();
        hptext = new javax.swing.JTextField();
        wptext = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        pgtext = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        fxtext = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        emtext = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        wbtext = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        comptext = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        bdtext = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        satext = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        cttext = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        sttext = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        cntext = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        zctext = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        zctext1 = new javax.swing.JTextField();
        cntext1 = new javax.swing.JTextField();
        sttext1 = new javax.swing.JTextField();
        cttext1 = new javax.swing.JTextField();
        satext1 = new javax.swing.JTextField();
        notestext = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Address_Book_Entry"));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(540, 400));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("First_Name:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel2.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Last_Name:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel3.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Nickname:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel4.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Screen_Name:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel5.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Home_Phone:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel5, gridBagConstraints);

        jLabel6.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Work_Phone:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel6, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(fntext, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(lntext, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(nntext, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(sntext, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(hptext, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(wptext, gridBagConstraints);

        jLabel7.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Pager:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(pgtext, gridBagConstraints);

        jLabel8.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Fax:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(fxtext, gridBagConstraints);

        jLabel9.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Email:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel9, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(emtext, gridBagConstraints);

        jLabel14.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Web_Page:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel14, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(wbtext, gridBagConstraints);

        jLabel21.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Company"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel21, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(comptext, gridBagConstraints);

        jLabel22.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Birthday"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(jLabel22, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(bdtext, gridBagConstraints);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("contact"), jPanel1);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("WorkAddress")));
        jLabel10.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Street_Address"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel3.add(jLabel10, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(satext, gridBagConstraints);

        jLabel11.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("City:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel3.add(jLabel11, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(cttext, gridBagConstraints);

        jLabel12.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("State:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel3.add(jLabel12, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(sttext, gridBagConstraints);

        jLabel13.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Country:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel3.add(jLabel13, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(cntext, gridBagConstraints);

        jLabel15.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Zip_Code:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel3.add(jLabel15, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(zctext, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jPanel3, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jPanel5.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("HomeAddress")));
        jLabel16.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Street_Address"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel5.add(jLabel16, gridBagConstraints);

        jLabel17.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("City:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel5.add(jLabel17, gridBagConstraints);

        jLabel18.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("State:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel5.add(jLabel18, gridBagConstraints);

        jLabel19.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Zip_Code:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel5.add(jLabel19, gridBagConstraints);

        jLabel20.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Country:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel5.add(jLabel20, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(zctext1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(cntext1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(sttext1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(cttext1, gridBagConstraints);

        satext1.setMinimumSize(new java.awt.Dimension(4, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(satext1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jPanel5, gridBagConstraints);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Address"), jPanel2);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Notes"), notestext);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jTabbedPane1, gridBagConstraints);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Save16.gif")));
        jButton2.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Save"));
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel4.add(jButton2);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Stop16.gif")));
        jButton3.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Dismiss"));
        jButton3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel4.add(jButton3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel4, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton3ActionPerformed
    {//GEN-HEADEREND:event_jButton3ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
        saveaddr();
    }//GEN-LAST:event_jButton2ActionPerformed
    
    
    // save a task
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bdtext;
    private javax.swing.JTextField cntext;
    private javax.swing.JTextField cntext1;
    private javax.swing.JTextField comptext;
    private javax.swing.JTextField cttext;
    private javax.swing.JTextField cttext1;
    private javax.swing.JTextField emtext;
    private javax.swing.JTextField fntext;
    private javax.swing.JTextField fxtext;
    private javax.swing.JTextField hptext;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField lntext;
    private javax.swing.JTextField nntext;
    private javax.swing.JTextArea notestext;
    private javax.swing.JTextField pgtext;
    private javax.swing.JTextField satext;
    private javax.swing.JTextField satext1;
    private javax.swing.JTextField sntext;
    private javax.swing.JTextField sttext;
    private javax.swing.JTextField sttext1;
    private javax.swing.JTextField wbtext;
    private javax.swing.JTextField wptext;
    private javax.swing.JTextField zctext;
    private javax.swing.JTextField zctext1;
    // End of variables declaration//GEN-END:variables
    
}
