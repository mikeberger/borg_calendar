
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

import java.awt.Font;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.sf.borg.common.app.AppHelper;
import net.sf.borg.common.ui.NwFontChooserS;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Version;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;




// propgui displays the edit preferences window
public class OptionsView extends View
{
    static
    {
        Version.addVersion("$Id$");
    }
    
	// to break a dependency with the contol package
	public interface RestartListener
	{
		public void restart();
	}
    
    private CalendarView cg_;     // the parent calendar window
    private RestartListener rl_;  // someone to call to request a restart
    
    OptionsView(CalendarView cg, RestartListener rl)
    {
        super();
        
        addModel(AppointmentModel.getReference());
        cg_ = cg;
        rl_ = rl;
        initComponents();
        
        // set the various screen items based on the existing user preferences
        
        // color print option
        String cp = Prefs.getPref("colorprint", "false" );
        if( cp.equals("true") )
            colorprint.setSelected(true);
        else
            colorprint.setSelected(false);
        
        // options to show public and private appts
        cp = Prefs.getPref("showpublic", "true" );
        if( cp.equals("true") )
            pubbox.setSelected(true);
        else
            pubbox.setSelected(false);
        cp = Prefs.getPref("showprivate", "false" );
        if( cp.equals("true") )
            privbox.setSelected(true);
        else
            privbox.setSelected(false);
        
        // database directory
        String dbdir = Prefs.getPref("dbdir", "not-set" );
        jTextField3.setText(dbdir);
        
        // print logo directory
        String logo = Prefs.getPref("logo", "" );
        logofile.setText(logo);
        if( !logo.equals("") )
            logobox.setSelected(true);
        else
            logobox.setSelected(false);
        
        // email enabled
        cp = Prefs.getPref("email_enabled", "false" );
        if( cp.equals("true") )
            emailbox.setSelected(true);
        else
            emailbox.setSelected(false);
        
        // email server and address
        cp = Prefs.getPref("email_server", "" );
        smtptext.setText(cp);
        cp = Prefs.getPref("email_addr", "" );
        emailtext.setText(cp);
        
        // set email server and address editable if the email option is
        // enabled
        smtptext.setEditable( !emailbox.isSelected() );
        emailtext.setEditable( !emailbox.isSelected() );
        
        // logging is not a preference - check the DB to see if logging is really on
        try
        {
            logging.setSelected( AppointmentModel.getReference().isLogging() );
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
        
        // US holidays
        String ush = Prefs.getPref("show_us_holidays", "true" );
        if( ush.equals("true") )
            holiday1.setSelected(true);
        else
            holiday1.setSelected(false);
        
        // US holidays
        ush = Prefs.getPref("show_can_holidays", "false" );
        if( ush.equals("true") )
            canadabox.setSelected(true);
        else
            canadabox.setSelected(false);
        
        String csort = Prefs.getPref("color_sort", "true" );
        if( csort.equals("true") )
            colorsortbox.setSelected(true);
        else
            colorsortbox.setSelected(false);
        
        int fdow = Prefs.getPref("first_dow", Calendar.SUNDAY );
        if( fdow == Calendar.MONDAY )
            mondaycb.setSelected(true);
        else
            mondaycb.setSelected(false);
        
        String mt = Prefs.getPref("miltime", "false" );
        if( mt.equals("true") )
            miltime.setSelected(true);
        else
            miltime.setSelected(false);
        
        String bg = Prefs.getPref("backgstart", "false" );
        if( bg.equals("true") )
            backgbox.setSelected(true);
        else
            backgbox.setSelected(false);
        
        String splash = Prefs.getPref("splash", "true" );
        if( splash.equals("true") )
            splashbox.setSelected(true);
        else
            splashbox.setSelected(false);
        
        String stacktrace = Prefs.getPref("stacktrace", "false" );
        if( stacktrace.equals("true") )
            stackbox.setSelected(true);
        else
            stackbox.setSelected(false);
        
        bg = Prefs.getPref("wrap", "false" );
        if( bg.equals("true") )
            wrapbox.setSelected(true);
        else
            wrapbox.setSelected(false);
        
        // auto update check
        int au = Prefs.getPref("ver_chk_last", -1 );
        if(au != -1 )
            autoupdate.setSelected(true);
        else
            autoupdate.setSelected(false);
        
        // add installed look and feels to lnfBox
        lnfBox.removeAllItems();
        TreeSet lnfs = new TreeSet();
        String curlnf = Prefs.getPref("lnf", "javax.swing.plaf.metal.MetalLookAndFeel" );
        LookAndFeelInfo lnfinfo[] = UIManager.getInstalledLookAndFeels();
        for( int i = 0; i < lnfinfo.length; i++ )
        {
            String name = lnfinfo[i].getClassName();
            lnfs.add(name);
        }
        try
        {
            Class.forName("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
            lnfs.add("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
        }
        catch( Exception e)
        {}
        try
        {
            Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
            lnfs.add("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
        }
        catch( Exception e)
        {}
        
        lnfs.add( curlnf );
        
        
        Iterator it = lnfs.iterator();
        while( it.hasNext())
            lnfBox.addItem( (String) it.next());
        
        lnfBox.setSelectedItem(curlnf);
        
        String shr = Prefs.getPref("wkStartHour", "7" );
        String ehr = Prefs.getPref("wkEndHour", "22" );
        wkstarthr.setSelectedItem( shr );
        wkendhr.setSelectedItem( ehr );
        
        // add locales
        String nolocale = Prefs.getPref("nolocale", "0");
        if( !nolocale.equals("1"))
        {
            localebox.removeAllItems();
            
            Locale locs[] = Locale.getAvailableLocales();
            for( int i = 0; i < locs.length; i++ )
            {
                //String name = locs[i].
                localebox.addItem(locs[i].getDisplayName());
            }
            
            String currentlocale = Locale.getDefault().getDisplayName();
            localebox.setSelectedItem(currentlocale);
        }
        else
        {
            localebox.setEnabled(false);
        }
        
        // popups
        bg = Prefs.getPref("reminders", "true" );
        if( bg.equals("true") )
            popenablebox.setSelected(true);
        else
            popenablebox.setSelected(false);
        
        bg = Prefs.getPref("beeping_reminders", "true" );
        if( bg.equals("true") )
            soundbox.setSelected(true);
        else
            soundbox.setSelected(false);
        
        int mins = Prefs.getPref("reminder_check_mins", 5 );
        checkfreq.setValue(new Integer(mins));
        
        mins = Prefs.getPref("pop_before_mins", 180 );
        popminbefore.setValue(new Integer(mins));
        
        mins = Prefs.getPref("pop_after_mins", 30 );
        popminafter.setValue(new Integer(mins));
               
        mins = Prefs.getPref("beeping_mins", 15 );
        beepmins.setValue(new Integer(mins));
         
        bg = Prefs.getPref("shared", "false" );
        if( bg.equals("true") )
            sharedbox.setSelected(true);
        else
            sharedbox.setSelected(false);
            
        logobrowse.setEnabled(AppHelper.isApplication());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        privbox = new javax.swing.JCheckBox();
        pubbox = new javax.swing.JCheckBox();
        incfont = new javax.swing.JButton();
        decfont = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        lnfBox = new javax.swing.JComboBox();
        holiday1 = new javax.swing.JCheckBox();
        mondaycb = new javax.swing.JCheckBox();
        miltime = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        wkstarthr = new javax.swing.JComboBox();
        wkendhr = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        wrapbox = new javax.swing.JCheckBox();
        canadabox = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        localebox = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        colorsortbox = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        chgdb = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        smtptext = new javax.swing.JTextField();
        emailtext = new javax.swing.JTextField();
        emailbox = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        colorprint = new javax.swing.JCheckBox();
        logobox = new javax.swing.JCheckBox();
        logofile = new javax.swing.JTextField();
        logobrowse = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        logging = new javax.swing.JCheckBox();
        autoupdate = new javax.swing.JCheckBox();
        versioncheck = new javax.swing.JButton();
        splashbox = new javax.swing.JCheckBox();
        backgbox = new javax.swing.JCheckBox();
        stackbox = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        popenablebox = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        popminbefore = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        popminafter = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        soundbox = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        beepmins = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        checkfreq = new javax.swing.JSpinner();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        sharedbox = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Options"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel2.setName(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("appearance"));
        privbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Show_Private_Appointments"));
        privbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(privbox, gridBagConstraints);

        pubbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Show_Public_Appointments"));
        pubbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pubboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(pubbox, gridBagConstraints);

        incfont.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("set_pre_font"));
        incfont.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        incfont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incfontActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(incfont, gridBagConstraints);

        decfont.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("set_appt_font"));
        decfont.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        decfont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decfontActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(decfont, gridBagConstraints);

        jLabel4.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Look_and_Feel:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel4, gridBagConstraints);

        lnfBox.setEditable(true);
        lnfBox.setMaximumSize(new java.awt.Dimension(131, 24));
        lnfBox.setPreferredSize(new java.awt.Dimension(50, 24));
        lnfBox.setAutoscrolls(true);
        lnfBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lnfBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(lnfBox, gridBagConstraints);

        holiday1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Show_U.S._Holidays"));
        holiday1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holiday1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(holiday1, gridBagConstraints);

        mondaycb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Week_Starts_with_Monday"));
        mondaycb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mondaycbActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(mondaycb, gridBagConstraints);

        miltime.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Use_24_hour_time_format"));
        miltime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miltimeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(miltime, gridBagConstraints);

        jLabel5.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Week_View_Start_Hour:_"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel5, gridBagConstraints);

        wkstarthr.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "4", "5", "6", "7", "8", "9", "10", "11" }));
        wkstarthr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wkstarthrActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(wkstarthr, gridBagConstraints);

        wkendhr.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        wkendhr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wkendhrActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(wkendhr, gridBagConstraints);

        jLabel6.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Week_View_End_Hour:_"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel6, gridBagConstraints);

        wrapbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Wrap_Appointment_Text"));
        wrapbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wrapboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(wrapbox, gridBagConstraints);

        canadabox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Show_Canadian_Holidays"));
        canadabox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canadaboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(canadabox, gridBagConstraints);

        jLabel8.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("locale"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel8, gridBagConstraints);

        localebox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localeboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(localebox, gridBagConstraints);

        jButton1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("set_def_font"));
        jButton1.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jButton1, gridBagConstraints);

        colorsortbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("colorsort"));
        colorsortbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorsortboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(colorsortbox, gridBagConstraints);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("appearance"), jPanel2);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("DataBase_Directory_or_URL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel4.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
        jPanel4.add(jTextField3, gridBagConstraints);

        jButton5.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Browse"));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel4.add(jButton5, gridBagConstraints);

        chgdb.setForeground(new java.awt.Color(255, 51, 51));
        chgdb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Apply_DB_Change"));
        chgdb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chgdbActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel4.add(chgdb, gridBagConstraints);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("DatabaseInformation"), jPanel4);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("SMTP_Server"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel2.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Your_Email_Address"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel1.add(jLabel2, gridBagConstraints);

        smtptext.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(smtptext, gridBagConstraints);

        emailtext.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(emailtext, gridBagConstraints);

        emailbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Enable_Email"));
        emailbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(emailbox, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel7.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("**_Must_be_disabled_to_edit_parameters_**"));
        jPanel1.add(jLabel7, new java.awt.GridBagConstraints());

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("EmailParameters"), jPanel1);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        colorprint.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Print_In_Color?"));
        colorprint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorprintActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        jPanel5.add(colorprint, gridBagConstraints);

        logobox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Print_Logo_(GIF/JPG/PNG)"));
        logobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel5.add(logobox, gridBagConstraints);

        logofile.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel5.add(logofile, gridBagConstraints);

        logobrowse.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Browse"));
        logobrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logobrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel5.add(logobrowse, gridBagConstraints);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("printing"), jPanel5);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        logging.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Enable_Logging_(requires_program_restart)"));
        logging.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggingActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(logging, gridBagConstraints);

        autoupdate.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Auto_Update_Check"));
        autoupdate.setToolTipText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Enable_a_daily_check_to_the_BORG_website_to_see_if_a_new_version_is_out._Does_not_update_the_product."));
        autoupdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoupdateActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(autoupdate, gridBagConstraints);

        versioncheck.setFont(new java.awt.Font("Dialog", 0, 10));
        versioncheck.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Check_for_updates_now"));
        versioncheck.setToolTipText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Check_for_the_latest_BORG_version_now"));
        versioncheck.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
        versioncheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versioncheckActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(versioncheck, gridBagConstraints);

        splashbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("splash"));
        splashbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splashboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(splashbox, gridBagConstraints);

        backgbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Start_in_background_(Windows_only,_TrayIcon_req)"));
        backgbox.setToolTipText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Do_not_open_todo_and_month_view_on_startup,_start_in_systray"));
        backgbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(backgbox, gridBagConstraints);

        stackbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("stackonerr"));
        stackbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stackboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(stackbox, gridBagConstraints);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("misc"), jPanel3);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        popenablebox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("enable_popups"));
        popenablebox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popenableboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel6.add(popenablebox, gridBagConstraints);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel9.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("pop_app"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel6.add(jLabel9, gridBagConstraints);

        popminbefore.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                popminbeforeStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel6.add(popminbefore, gridBagConstraints);

        jLabel10.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("min_bef_app"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel6.add(jLabel10, gridBagConstraints);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel11.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("pop_app"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel6.add(jLabel11, gridBagConstraints);

        popminafter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                popminafterStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel6.add(popminafter, gridBagConstraints);

        jLabel12.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("min_aft_app"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel6.add(jLabel12, gridBagConstraints);

        soundbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("beeps"));
        soundbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                soundboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel6.add(soundbox, gridBagConstraints);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel13.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("beepingstarts"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel6.add(jLabel13, gridBagConstraints);

        beepmins.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                beepminsStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel6.add(beepmins, gridBagConstraints);

        jLabel14.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("min_bef_app"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel6.add(jLabel14, gridBagConstraints);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel15.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("min_between_chks"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel6.add(jLabel15, gridBagConstraints);

        checkfreq.setMinimumSize(new java.awt.Dimension(50, 20));
        checkfreq.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkfreqStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel6.add(checkfreq, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel6.add(jSeparator1, gridBagConstraints);

        jLabel16.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("restart_req"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel6.add(jLabel16, gridBagConstraints);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("popup_reminders"), jPanel6);

        sharedbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("shared"));
        sharedbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sharedboxActionPerformed(evt);
            }
        });

        jPanel7.add(sharedbox);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Multi_User"), jPanel7);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jTabbedPane1, gridBagConstraints);

        jButton2.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Dismiss"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jButton2, gridBagConstraints);

        pack();
    }//GEN-END:initComponents

    private void colorsortboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorsortboxActionPerformed
        if( colorsortbox.isSelected() )
            Prefs.putPref("color_sort", "true" );
        else
            Prefs.putPref("color_sort", "false" );
                
        try
        {
            cg_.refresh();
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
    }//GEN-LAST:event_colorsortboxActionPerformed

    private void stackboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stackboxActionPerformed
        if( stackbox.isSelected() )
            Prefs.putPref("stacktrace", "true" );
        else
            Prefs.putPref("stacktrace", "false" );
    }//GEN-LAST:event_stackboxActionPerformed

    private void sharedboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sharedboxActionPerformed
    {//GEN-HEADEREND:event_sharedboxActionPerformed
        if( sharedbox.isSelected() )
            Prefs.putPref("shared", "true" );
        else
            Prefs.putPref("shared", "false" );
    }//GEN-LAST:event_sharedboxActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       
        Font f = NwFontChooserS.showDialog(null, null,null);
        String fs = NwFontChooserS.fontString(f);
        Prefs.putPref("defaultfont", fs );
        NwFontChooserS.setDefaultFont(f);
        SwingUtilities.updateComponentTreeUI(this);
        SwingUtilities.updateComponentTreeUI(cg_);
    
    }//GEN-LAST:event_jButton1ActionPerformed

    private void beepminsStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_beepminsStateChanged
    {//GEN-HEADEREND:event_beepminsStateChanged
        Integer i = (Integer) beepmins.getValue();
        int cur = Prefs.getPref("beeping_mins", 5);
        if( i.intValue() != cur )
            Prefs.putPref("beeping_mins", i.intValue());
    }//GEN-LAST:event_beepminsStateChanged

    private void popminafterStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_popminafterStateChanged
    {//GEN-HEADEREND:event_popminafterStateChanged
        Integer i = (Integer) popminafter.getValue();
        int cur = Prefs.getPref("pop_after_mins", 5);
        if( i.intValue() != cur )
            Prefs.putPref("pop_after_mins", i.intValue());
    }//GEN-LAST:event_popminafterStateChanged

    private void popminbeforeStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_popminbeforeStateChanged
    {//GEN-HEADEREND:event_popminbeforeStateChanged
        Integer i = (Integer) popminbefore.getValue();
        int cur = Prefs.getPref("pop_before_mins", 5);
        if( i.intValue() != cur )
            Prefs.putPref("pop_before_mins", i.intValue());
    }//GEN-LAST:event_popminbeforeStateChanged

    private void checkfreqStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_checkfreqStateChanged
    {//GEN-HEADEREND:event_checkfreqStateChanged
        Integer i = (Integer) checkfreq.getValue();//GEN-LAST:event_checkfreqStateChanged
        int cur = Prefs.getPref("reminder_check_mins", 5);
        if( i.intValue() != cur )
            Prefs.putPref("reminder_check_mins", i.intValue());
        
    }
    private void soundboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_soundboxActionPerformed
    {//GEN-HEADEREND:event_soundboxActionPerformed
        if( soundbox.isSelected() )
            Prefs.putPref("beeping_reminders", "true" );
        else
            Prefs.putPref("beeping_reminders", "false" );
    }//GEN-LAST:event_soundboxActionPerformed

    private void popenableboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_popenableboxActionPerformed
    {//GEN-HEADEREND:event_popenableboxActionPerformed
        if( popenablebox.isSelected() )
            Prefs.putPref("reminders", "true" );
        else
            Prefs.putPref("reminders", "false" );
    }//GEN-LAST:event_popenableboxActionPerformed

    private void splashboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_splashboxActionPerformed
    {//GEN-HEADEREND:event_splashboxActionPerformed
        if( splashbox.isSelected() )
            Prefs.putPref("splash", "true" );
        else
            Prefs.putPref("splash", "false" );
    }//GEN-LAST:event_splashboxActionPerformed
    
    private void localeboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localeboxActionPerformed
        Locale locs[] = Locale.getAvailableLocales();
        String choice = (String) localebox.getSelectedItem();
        for( int i = 0; i < locs.length; i++ )
        {
            if( choice.equals(locs[i].getDisplayName()))
            {
                //Locale.setDefault(locs[i]);
                Prefs.putPref("country", locs[i].getCountry() );
                Prefs.putPref("language", locs[i].getLanguage());
            }
        }
    }//GEN-LAST:event_localeboxActionPerformed
    
    private void lnfBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lnfBoxActionPerformed
        
        String newlnf = (String) lnfBox.getSelectedItem();
        String oldlnf = Prefs.getPref("lnf", "javax.swing.plaf.metal.MetalLookAndFeel" );
        if( !newlnf.equals(oldlnf) )
        {
            try
            {
                UIManager.setLookAndFeel(newlnf);
                // don't try to change the main window l&f - is doesn't work 100%
                //SwingUtilities.updateComponentTreeUI(cg_);
                Prefs.putPref("lnf", newlnf );
            }
            catch( Exception e )
            {
                // Errmsg.notice( "Could not find look and feel: " + newlnf );
                Errmsg.notice( e.toString() );
                return;
            }
        }
        
    }//GEN-LAST:event_lnfBoxActionPerformed
    
    private void chgdbActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_chgdbActionPerformed
    {//GEN-HEADEREND:event_chgdbActionPerformed
        int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("Really_change_the_database?"), Resource.getResourceString("Confirm_DB_Change"), JOptionPane.YES_NO_OPTION);
        if( ret == JOptionPane.YES_OPTION )
        {
            String dbdir = jTextField3.getText();
            Prefs.putPref("dbdir", dbdir );
            rl_.restart();
        }
    }//GEN-LAST:event_chgdbActionPerformed
    
    private void canadaboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_canadaboxActionPerformed
    {//GEN-HEADEREND:event_canadaboxActionPerformed
        
        // update US holiday preference and refresh the month view accordingly
        if( canadabox.isSelected() )
            Prefs.putPref("show_can_holidays", "true" );
        else
            Prefs.putPref("show_can_holidays", "false" );
        
        try
        {
            cg_.refresh();
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
    }//GEN-LAST:event_canadaboxActionPerformed
    
    private void wrapboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_wrapboxActionPerformed
    {//GEN-HEADEREND:event_wrapboxActionPerformed
        if( wrapbox.isSelected() )
            Prefs.putPref("wrap", "true" );
        else
            Prefs.putPref("wrap", "false" );
    }//GEN-LAST:event_wrapboxActionPerformed
    
    private void backgboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_backgboxActionPerformed
    {//GEN-HEADEREND:event_backgboxActionPerformed
        if( backgbox.isSelected() )
            Prefs.putPref("backgstart", "true" );
        else
            Prefs.putPref("backgstart", "false" );
    }//GEN-LAST:event_backgboxActionPerformed
    
    private void logoboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_logoboxActionPerformed
    {//GEN-HEADEREND:event_logoboxActionPerformed
        if( !logobox.isSelected() )
        {
            Prefs.putPref("logo", "" );
            logofile.setText("");
        }
        
    }//GEN-LAST:event_logoboxActionPerformed
    
    private void logobrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_logobrowseActionPerformed
    {//GEN-HEADEREND:event_logobrowseActionPerformed
        
        // browse for new logo file
        logobox.setSelected(true);
        String logo = null;
        while( true )
        {
            JFileChooser chooser = new JFileChooser();
            
            chooser.setCurrentDirectory( new File(".") );
            chooser.setDialogTitle(Resource.getResourceString("Please_choose_the_logo_file_-_GIF/JPG/PNG_only"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal != JFileChooser.APPROVE_OPTION)
                return;
            
            logo = chooser.getSelectedFile().getAbsolutePath();
            File lf = new File(logo);
            String err = null;
            if( !lf.exists() )
            {
                err = Resource.getResourceString("File_[") + logo + Resource.getResourceString("]_does_not_exist");
            }
            else if( !lf.canRead() )
            {
                err = Resource.getResourceString("Database_Directory_[") + logo + Resource.getResourceString("]_is_not_writable");
            }
            
            if( err == null )
                break;
            
            Errmsg.notice( err );
        }
        
        Prefs.putPref("logo", logo );
        
        // update text field - nothing else changes. DB change will take effect only on restart
        logofile.setText(logo);
    }//GEN-LAST:event_logobrowseActionPerformed
    
    private void wkendhrActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_wkendhrActionPerformed
    {//GEN-HEADEREND:event_wkendhrActionPerformed
        Prefs.putPref( "wkEndHour", (String)wkendhr.getSelectedItem());
    }//GEN-LAST:event_wkendhrActionPerformed
    
    private void wkstarthrActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_wkstarthrActionPerformed
    {//GEN-HEADEREND:event_wkstarthrActionPerformed
        Prefs.putPref( "wkStartHour", (String)wkstarthr.getSelectedItem());
        
    }//GEN-LAST:event_wkstarthrActionPerformed
    
    private void miltimeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miltimeActionPerformed
    {//GEN-HEADEREND:event_miltimeActionPerformed
        if( miltime.isSelected() )
            Prefs.putPref("miltime", "true" );
        else
            Prefs.putPref("miltime", "false" );
        
        try
        {
            cg_.refresh();
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
    }//GEN-LAST:event_miltimeActionPerformed
    
    private void mondaycbActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mondaycbActionPerformed
    {//GEN-HEADEREND:event_mondaycbActionPerformed
        
        if( mondaycb.isSelected() )
            Prefs.putPref("first_dow", Calendar.MONDAY );
        else
            Prefs.putPref("first_dow", Calendar.SUNDAY );
        
        try
        {
            cg_.setDayLabels();
            cg_.refresh();
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
    }//GEN-LAST:event_mondaycbActionPerformed
    
    private void versioncheckActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_versioncheckActionPerformed
    {//GEN-HEADEREND:event_versioncheckActionPerformed
        try
        {
            // get version and compare
            URL webverurl = new URL("http://borg-calendar.sourceforge.net/latest_version");
            InputStream is = webverurl.openStream();
            int i;
            String webver = "";
            while( true )
            {
                i = is.read();
                if( i == -1 || i == '\n' || i == '\r') break;
                webver += (char )i;
            }
            
            
            String info = Resource.getResourceString("Your_BORG_version_=_") + Resource.getVersion() + Resource.getResourceString("Latest_version_at_sourceforge_=_") + webver;
            JOptionPane.showMessageDialog(null, info, Resource.getResourceString("BORG_Version_Check"), JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getClass().getResource("/resource/borg.jpg")));
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
        
    }//GEN-LAST:event_versioncheckActionPerformed
    
    private void autoupdateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_autoupdateActionPerformed
    {//GEN-HEADEREND:event_autoupdateActionPerformed
        // enable/disable auto-update-check
        // value is the last day-of-year that check was done (1-365)
        // phony value 400 will cause check during current day
        // value -1 is the shut-off value
        if( autoupdate.isSelected() )
            Prefs.putPref("ver_chk_last", 400 );
        else
            Prefs.putPref("ver_chk_last", -1 );
        
    }//GEN-LAST:event_autoupdateActionPerformed
    
    private void holiday1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_holiday1ActionPerformed
        
        // update US holiday preference and refresh the month view accordingly
        if( holiday1.isSelected() )
            Prefs.putPref("show_us_holidays", "true" );
        else
            Prefs.putPref("show_us_holidays", "false" );
        
        try
        {
            cg_.refresh();
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
    }//GEN-LAST:event_holiday1ActionPerformed
    
    private void loggingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggingActionPerformed
        
        // turn logging on/off
        try
        {
            
            if( logging.isSelected() )
            {
            
                AppointmentModel.getReference().setLogging(true);
				AddressModel.getReference().setLogging(true);
				TaskModel.getReference().setLogging(true);
            }
            else
            {
            
			   	AppointmentModel.getReference().setLogging(false);
			  	AddressModel.getReference().setLogging(false);
			 	TaskModel.getReference().setLogging(false);
            }
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_loggingActionPerformed
    
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        
        // browse for new database dir
        String dbdir = OptionsView.chooseDbDir(false);
        if( dbdir == null ) return;
        
        // update text field - nothing else changes. DB change will take effect only on restart
        jTextField3.setText(dbdir);
        
        
    }//GEN-LAST:event_jButton5ActionPerformed
    
    private void incfontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incfontActionPerformed
        
        Font f = NwFontChooserS.showDialog(null, null, null);
        String s = NwFontChooserS.fontString(f);
  
        Prefs.putPref( "previewfont", s );
        
        // update styles used in month view text panes with new font size
        cg_.updStyles();
        
        try
        {
            // refresh the month view
            cg_.refresh();
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
        
    }//GEN-LAST:event_incfontActionPerformed
    
    private void decfontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decfontActionPerformed
       
        Font f = NwFontChooserS.showDialog(null, null, null);
        String s = NwFontChooserS.fontString(f);
  
        Prefs.putPref( "apptfont", s );
        
        // update styles used in month view text panes with new font size
        cg_.updStyles();
        
        try
        {
            // refresh the month view
            cg_.refresh();
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_decfontActionPerformed
    
    private void privboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privboxActionPerformed
        
        // update the show private option and refresh the month view
        if( privbox.isSelected() )
            Prefs.putPref("showprivate", "true" );
        else
            Prefs.putPref("showprivate", "false" );
        try
        {
            cg_.refresh();
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
        
    }//GEN-LAST:event_privboxActionPerformed
    
    private void pubboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pubboxActionPerformed
        
        // update the show public option and refresh the month view
        if( pubbox.isSelected() )
            Prefs.putPref("showpublic", "true" );
        else
            Prefs.putPref("showpublic", "false" );
        try
        {
            cg_.refresh();
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
        
        
    }//GEN-LAST:event_pubboxActionPerformed
    
    private void colorprintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorprintActionPerformed
        if( colorprint.isSelected() )
            Prefs.putPref("colorprint", "true" );
        else
            Prefs.putPref("colorprint", "false" );
    }//GEN-LAST:event_colorprintActionPerformed
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        
        
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private void emailboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_emailboxActionPerformed
    {//GEN-HEADEREND:event_emailboxActionPerformed
        
        // update the email enabled flag - and enable/disable the server and address
        // text fields accordingly
        if( emailbox.isSelected() )
        {
            Prefs.putPref("email_enabled", "true" );
            Prefs.putPref("email_server", smtptext.getText() );
            Prefs.putPref("email_addr", emailtext.getText() );
        }
        else
            Prefs.putPref("email_enabled", "false" );
        smtptext.setEditable( !emailbox.isSelected() );
        emailtext.setEditable( !emailbox.isSelected() );
        
    }//GEN-LAST:event_emailboxActionPerformed
    
    
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        this.dispose();
    }//GEN-LAST:event_exitForm
    
    public void destroy()
    {
        this.dispose();
    }
    
    public void refresh()
    {
    }

	// prompt the user to enter a database directory
	public static String chooseDbDir(boolean update) {
	    
	    String dbdir = null;
	    while( true ) {
	        JFileChooser chooser = new JFileChooser();
	        
	        chooser.setCurrentDirectory( new File(".") );
	        chooser.setDialogTitle("Please choose directory for database files");
	        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        
	        int returnVal = chooser.showOpenDialog(null);
	        if(returnVal != JFileChooser.APPROVE_OPTION)
	            return(null);
	        
	        dbdir = chooser.getSelectedFile().getAbsolutePath();
	        File dir = new File(dbdir);
	        String err = null;
	        if( !dir.exists() ) {
	            err = "Database Directory [" + dbdir + "] does not exist";
	        }
	        else if( !dir.isDirectory() ) {
	            err = "Database Directory [" + dbdir + "] is not a directory";
	        }
	        else if( !dir.canWrite() ) {
	            err = "Database Directory [" + dbdir + "] is not writable";
	        }
	        
	        if( err == null )
	            break;
	        
	        Errmsg.notice( err );
	    }
	    
	    if( update )
	        Prefs.putPref("dbdir", dbdir );
	    return(dbdir);
	}
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoupdate;
    private javax.swing.JCheckBox backgbox;
    private javax.swing.JSpinner beepmins;
    private javax.swing.JCheckBox canadabox;
    private javax.swing.JSpinner checkfreq;
    private javax.swing.JButton chgdb;
    private javax.swing.JCheckBox colorprint;
    private javax.swing.JCheckBox colorsortbox;
    private javax.swing.JButton decfont;
    private javax.swing.JCheckBox emailbox;
    private javax.swing.JTextField emailtext;
    private javax.swing.JCheckBox holiday1;
    private javax.swing.JButton incfont;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
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
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JComboBox lnfBox;
    private javax.swing.JComboBox localebox;
    private javax.swing.JCheckBox logging;
    private javax.swing.JCheckBox logobox;
    private javax.swing.JButton logobrowse;
    private javax.swing.JTextField logofile;
    private javax.swing.JCheckBox miltime;
    private javax.swing.JCheckBox mondaycb;
    private javax.swing.JCheckBox popenablebox;
    private javax.swing.JSpinner popminafter;
    private javax.swing.JSpinner popminbefore;
    private javax.swing.JCheckBox privbox;
    private javax.swing.JCheckBox pubbox;
    private javax.swing.JCheckBox sharedbox;
    private javax.swing.JTextField smtptext;
    private javax.swing.JCheckBox soundbox;
    private javax.swing.JCheckBox splashbox;
    private javax.swing.JCheckBox stackbox;
    private javax.swing.JButton versioncheck;
    private javax.swing.JComboBox wkendhr;
    private javax.swing.JComboBox wkstarthr;
    private javax.swing.JCheckBox wrapbox;
    // End of variables declaration//GEN-END:variables
    
}
