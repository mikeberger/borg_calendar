
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
import java.awt.GridBagConstraints;
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
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Version;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;




import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
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
    
    static private RestartListener rl_ = null;  // someone to call to request a restart
    
    private static OptionsView singleton = null;
    public static OptionsView getReference() {
        if( singleton == null || !singleton.isShowing())
            singleton = new OptionsView(false);
        return( singleton );
    }
    
    
    static public void setRestartListener( RestartListener rl )
    {
    	rl_ = rl;
    }
    
    public static void dbSelectOnly()
    {
    	new OptionsView(true).show();
    	
    }
    
    // dbonly will only allow db changes
    private OptionsView(boolean dbonly)
    {
    	super();
 
    	initComponents();
    	
    	if( !dbonly )
    	{       
    		addModel(AppointmentModel.getReference());
    	}
    	else
    	{
    		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    	}
    	
        
        
        // set the various screen items based on the existing user preferences
        
        
        //
        // database
        //
        String dbtype = Prefs.getPref( PrefName.DBTYPE );
        if( dbtype.equals("local"))
        {
            localFileButton.setSelected(true); 
            jPanel9.setVisible(true);
            jPanel8.setVisible(false);
          
        }
        else
        {
            MySQL.setSelected(true);
        	jPanel8.setVisible(true);
        	jPanel9.setVisible(false);

        }
        
        String dbparam = Prefs.getPref(PrefName.DBDIR);
        jTextField3.setText(dbparam);
        
        dbparam = Prefs.getPref(PrefName.DBNAME);
        jTextField1.setText(dbparam);
        dbparam = Prefs.getPref(PrefName.DBPORT);
        jTextField4.setText(dbparam);
        dbparam = Prefs.getPref(PrefName.DBHOST);
        jTextField2.setText(dbparam);        
        dbparam = Prefs.getPref(PrefName.DBUSER);
        jTextField5.setText(dbparam);
        dbparam = Prefs.getPref(PrefName.DBPASS);
        jPasswordField1.setText(dbparam);
        
        if( dbonly )
        {
        	// disable lots of non-db-related stuff
            jTabbedPane1.setEnabledAt(0,false);
            jTabbedPane1.setEnabledAt(2,false);
            jTabbedPane1.setEnabledAt(3,false);
            jTabbedPane1.setEnabledAt(4,false);
            jTabbedPane1.setEnabledAt(5,false);
            jTabbedPane1.setEnabledAt(6,false);
            jTabbedPane1.setSelectedIndex(1);
            jButton2.setEnabled(false);
            applyButton.setEnabled(false);
            return;
                
        }
        
        // color print option
        String cp = Prefs.getPref(PrefName.COLORPRINT);
        if( cp.equals("true") )
            colorprint.setSelected(true);
        else
            colorprint.setSelected(false);
        
        // options to show public and private appts
        cp = Prefs.getPref(PrefName.SHOWPUBLIC );
        if( cp.equals("true") )
            pubbox.setSelected(true);
        else
            pubbox.setSelected(false);
        cp = Prefs.getPref(PrefName.SHOWPRIVATE);
        if( cp.equals("true") )
            privbox.setSelected(true);
        else
            privbox.setSelected(false);
        

        // print logo directory
        String logo = Prefs.getPref(PrefName.LOGO);
        logofile.setText(logo);
        if( !logo.equals("") )
            logobox.setSelected(true);
        else
            logobox.setSelected(false);
        
        // email enabled
        cp = Prefs.getPref(PrefName.EMAILENABLED );
        if( cp.equals("true") )
            emailbox.setSelected(true);
        else
            emailbox.setSelected(false);
        
        // email server and address
        cp = Prefs.getPref(PrefName.EMAILSERVER);
        smtptext.setText(cp);
        cp = Prefs.getPref(PrefName.EMAILADDR);
        emailtext.setText(cp);
        
        // set email server and address editable if the email option is
        // enabled
        //smtptext.setEditable( !emailbox.isSelected() );
        //emailtext.setEditable( !emailbox.isSelected() );
        
        // logging is not a preference - check the DB to see if logging is really on
        try
        {
            logging.setSelected( AppointmentModel.getReference().isLogging() );
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }
        
        // US holidays
        String ush = Prefs.getPref(PrefName.SHOWUSHOLIDAYS);
        if( ush.equals("true") )
            holiday1.setSelected(true);
        else
            holiday1.setSelected(false);
        
        // CAN holidays
        ush = Prefs.getPref(PrefName.SHOWCANHOLIDAYS);
        if( ush.equals("true") )
            canadabox.setSelected(true);
        else
            canadabox.setSelected(false);
        
        ush = Prefs.getPref(PrefName.DAYOFYEAR);
        if( ush.equals("true") )
            doyBox.setSelected(true);
        else
            doyBox.setSelected(false);
        
        String csort = Prefs.getPref(PrefName.COLORSORT);
        if( csort.equals("true") )
            colorsortbox.setSelected(true);
        else
            colorsortbox.setSelected(false);
        
        int fdow = Prefs.getIntPref(PrefName.FIRSTDOW );
        if( fdow == Calendar.MONDAY )
            mondaycb.setSelected(true);
        else
            mondaycb.setSelected(false);
        
        String mt = Prefs.getPref(PrefName.MILTIME);
        if( mt.equals("true") )
            miltime.setSelected(true);
        else
            miltime.setSelected(false);
        
        String bg = Prefs.getPref(PrefName.BACKGSTART);
        if( bg.equals("true") )
            backgbox.setSelected(true);
        else
            backgbox.setSelected(false);
        
        String splash = Prefs.getPref(PrefName.SPLASH);
        if( splash.equals("true") )
            splashbox.setSelected(true);
        else
            splashbox.setSelected(false);
        
        String stacktrace = Prefs.getPref(PrefName.STACKTRACE);
        if( stacktrace.equals("true") )
            stackbox.setSelected(true);
        else
            stackbox.setSelected(false);
        
        bg = Prefs.getPref(PrefName.WRAP);
        if( bg.equals("true") )
            wrapbox.setSelected(true);
        else
            wrapbox.setSelected(false);
        
        bg = Prefs.getPref(PrefName.REVERSEDAYEDIT);
        if( bg.equals("true") )
            revDayEditbox.setSelected(true);
        else
        	revDayEditbox.setSelected(false);
        
        // auto update check
        int au = Prefs.getIntPref(PrefName.VERCHKLAST );
        if(au != -1 )
            autoupdate.setSelected(true);
        else
            autoupdate.setSelected(false);
        
        // add installed look and feels to lnfBox
        lnfBox.removeAllItems();
        TreeSet lnfs = new TreeSet();
        String curlnf = Prefs.getPref(PrefName.LNF);
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
        
        String shr = Prefs.getPref(PrefName.WKSTARTHOUR);
        String ehr = Prefs.getPref(PrefName.WKENDHOUR);
        wkstarthr.setSelectedItem( shr );
        wkendhr.setSelectedItem( ehr );
        
        // add locales
        String nolocale = Prefs.getPref(PrefName.NOLOCALE);
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
        bg = Prefs.getPref(PrefName.REMINDERS);
        if( bg.equals("true") )
            popenablebox.setSelected(true);
        else
            popenablebox.setSelected(false);
        
        bg = Prefs.getPref(PrefName.BEEPINGREMINDERS);
        if( bg.equals("true") )
            soundbox.setSelected(true);
        else
            soundbox.setSelected(false);
        
        int mins = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
        checkfreq.setValue(new Integer(mins));
        
        mins = Prefs.getIntPref(PrefName.POPBEFOREMINS);
        popminbefore.setValue(new Integer(mins));
        
        mins = Prefs.getIntPref(PrefName.POPAFTERMINS );
        popminafter.setValue(new Integer(mins));
               
        mins = Prefs.getIntPref(PrefName.BEEPINGMINS);
        beepmins.setValue(new Integer(mins));
         
        bg = Prefs.getPref(PrefName.SHARED);
        if( bg.equals("true") )
            sharedbox.setSelected(true);
        else
            sharedbox.setSelected(false);
        
        bg = Prefs.getPref(PrefName.ICALTODOEV );
        if( bg.equals("true") )
            icaltodobox.setSelected(true);
        else
            icaltodobox.setSelected(false);
            
        logobrowse.setEnabled(AppHelper.isApplication());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        

        dbTypeGroup = new javax.swing.ButtonGroup();
        GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
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
        localFileButton = new javax.swing.JRadioButton();
        MySQL = new javax.swing.JRadioButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jPanel9 = new javax.swing.JPanel();
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
        icaltodobox = new javax.swing.JCheckBox();
        revDayEditbox = new javax.swing.JCheckBox();
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
        applyButton = new javax.swing.JButton();

        //getContentPane().setLayout(new java.awt.GridBagLayout());

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
        GridBagConstraints gridBagConstraints0 = new java.awt.GridBagConstraints();
        gridBagConstraints0.gridx = 1;
        gridBagConstraints0.gridy = 1;
        gridBagConstraints0.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints0.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints0.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(privbox, gridBagConstraints0);

        pubbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Show_Public_Appointments"));
        GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(pubbox, gridBagConstraints1);

        incfont.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("set_pre_font"));
        incfont.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        incfont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incfontActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 8;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(incfont, gridBagConstraints2);

        decfont.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("set_appt_font"));
        decfont.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        decfont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decfontActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 8;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(decfont, gridBagConstraints3);

        jLabel4.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Look_and_Feel:"));
        GridBagConstraints gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.gridx = 0;
        gridBagConstraints4.gridy = 0;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints4.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel4, gridBagConstraints4);

        lnfBox.setEditable(true);
        lnfBox.setMaximumSize(new java.awt.Dimension(131, 24));
        lnfBox.setPreferredSize(new java.awt.Dimension(50, 24));
        lnfBox.setAutoscrolls(true);
        GridBagConstraints gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints5.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(lnfBox, gridBagConstraints5);

        holiday1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Show_U.S._Holidays"));
        GridBagConstraints gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridBagConstraints6.gridx = 0;
        gridBagConstraints6.gridy = 3;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints6.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(holiday1, gridBagConstraints6);

        mondaycb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Week_Starts_with_Monday"));
        GridBagConstraints gridBagConstraints7 = new java.awt.GridBagConstraints();
        gridBagConstraints7.gridx = 1;
        gridBagConstraints7.gridy = 4;
        gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints7.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(mondaycb, gridBagConstraints7);

        miltime.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Use_24_hour_time_format"));
        GridBagConstraints gridBagConstraints8 = new java.awt.GridBagConstraints();
        gridBagConstraints8.gridx = 0;
        gridBagConstraints8.gridy = 4;
        gridBagConstraints8.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints8.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(miltime, gridBagConstraints8);

        jLabel5.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Week_View_Start_Hour:_"));
        GridBagConstraints gridBagConstraints9 = new java.awt.GridBagConstraints();
        gridBagConstraints9.gridx = 0;
        gridBagConstraints9.gridy = 5;
        gridBagConstraints9.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints9.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel5, gridBagConstraints9);

        wkstarthr.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "4", "5", "6", "7", "8", "9", "10", "11" }));
        GridBagConstraints gridBagConstraints10 = new java.awt.GridBagConstraints();
        gridBagConstraints10.gridx = 1;
        gridBagConstraints10.gridy = 5;
        gridBagConstraints10.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints10.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints10.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(wkstarthr, gridBagConstraints10);

        wkendhr.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        GridBagConstraints gridBagConstraints11 = new java.awt.GridBagConstraints();
        gridBagConstraints11.gridx = 1;
        gridBagConstraints11.gridy = 6;
        gridBagConstraints11.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints11.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(wkendhr, gridBagConstraints11);

        jLabel6.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Week_View_End_Hour:_"));
        GridBagConstraints gridBagConstraints12 = new java.awt.GridBagConstraints();
        gridBagConstraints12.gridx = 0;
        gridBagConstraints12.gridy = 6;
        gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints12.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel6, gridBagConstraints12);

        wrapbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Wrap_Appointment_Text"));
        GridBagConstraints gridBagConstraints13 = new java.awt.GridBagConstraints();
        gridBagConstraints13.gridx = 0;
        gridBagConstraints13.gridy = 2;
        gridBagConstraints13.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints13.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(wrapbox, gridBagConstraints13);

        canadabox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Show_Canadian_Holidays"));
        GridBagConstraints gridBagConstraints14 = new java.awt.GridBagConstraints();
        gridBagConstraints14.gridx = 1;
        gridBagConstraints14.gridy = 3;
        gridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints14.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(canadabox, gridBagConstraints14);

        jLabel8.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("locale"));
        GridBagConstraints gridBagConstraints15 = new java.awt.GridBagConstraints();
        gridBagConstraints15.gridx = 0;
        gridBagConstraints15.gridy = 9;
        gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints15.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel8, gridBagConstraints15);

        GridBagConstraints gridBagConstraints16 = new java.awt.GridBagConstraints();
        gridBagConstraints16.gridx = 1;
        gridBagConstraints16.gridy = 9;
        gridBagConstraints16.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints16.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(localebox, gridBagConstraints16);

        jButton1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("set_def_font"));
        jButton1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints17 = new java.awt.GridBagConstraints();
        gridBagConstraints17.gridx = 0;
        gridBagConstraints17.gridy = 7;
        gridBagConstraints17.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints17.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints17.weightx = 1.0;
        gridBagConstraints17.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jButton1, gridBagConstraints17);

        colorsortbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("colorsort"));
        GridBagConstraints gridBagConstraints18 = new java.awt.GridBagConstraints();
        gridBagConstraints18.gridx = 1;
        gridBagConstraints18.gridy = 2;
        gridBagConstraints18.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints18.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(colorsortbox, gridBagConstraints18);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("appearance"), jPanel2);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        localFileButton.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("localFile"));
        dbTypeGroup.add(localFileButton);
        localFileButton.setActionCommand("local");
        localFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbTypeAction(evt);
            }
        });

        jPanel4.add(localFileButton, new java.awt.GridBagConstraints());

        MySQL.setText("MySQL");
        dbTypeGroup.add(MySQL);
        MySQL.setActionCommand("mysql");
        MySQL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbTypeAction(evt);
            }
        });

        jPanel4.add(MySQL, new java.awt.GridBagConstraints());

        jPanel8.setLayout(new java.awt.GridBagLayout());

        jPanel8.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("MySQLInfo")));
        jLabel7.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("DatabaseName"));
        GridBagConstraints gridBagConstraints19 = new java.awt.GridBagConstraints();
        gridBagConstraints19.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints19.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel8.add(jLabel7, gridBagConstraints19);

        GridBagConstraints gridBagConstraints20 = new java.awt.GridBagConstraints();
        gridBagConstraints20.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints20.weightx = 1.0;
        jPanel8.add(jTextField1, gridBagConstraints20);

        jLabel17.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("hostname"));
        GridBagConstraints gridBagConstraints21 = new java.awt.GridBagConstraints();
        gridBagConstraints21.gridx = 0;
        gridBagConstraints21.gridy = 1;
        gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints21.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel8.add(jLabel17, gridBagConstraints21);

        GridBagConstraints gridBagConstraints22= new java.awt.GridBagConstraints();
        gridBagConstraints22.gridx = 1;
        gridBagConstraints22.gridy = 1;
        gridBagConstraints22.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints22.weightx = 1.0;
        jPanel8.add(jTextField2, gridBagConstraints22);

        jLabel18.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("port"));
        GridBagConstraints gridBagConstraints23 = new java.awt.GridBagConstraints();
        gridBagConstraints23.gridx = 0;
        gridBagConstraints23.gridy = 2;
        gridBagConstraints23.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints23.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel8.add(jLabel18, gridBagConstraints23);

        GridBagConstraints gridBagConstraints24 = new java.awt.GridBagConstraints();
        gridBagConstraints24.gridx = 1;
        gridBagConstraints24.gridy = 2;
        gridBagConstraints24.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints24.weightx = 1.0;
        jPanel8.add(jTextField4, gridBagConstraints24);

        jLabel19.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("User"));
        GridBagConstraints gridBagConstraints25 = new java.awt.GridBagConstraints();
        gridBagConstraints25.gridx = 0;
        gridBagConstraints25.gridy = 3;
        gridBagConstraints25.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints25.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel8.add(jLabel19, gridBagConstraints25);

        GridBagConstraints gridBagConstraints26 = new java.awt.GridBagConstraints();
        gridBagConstraints26.gridx = 1;
        gridBagConstraints26.gridy = 3;
        gridBagConstraints26.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints26.weightx = 1.0;
        jPanel8.add(jTextField5, gridBagConstraints26);

        jLabel20.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Password"));
        GridBagConstraints gridBagConstraints27 = new java.awt.GridBagConstraints();
        gridBagConstraints27.gridx = 0;
        gridBagConstraints27.gridy = 4;
        gridBagConstraints27.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints27.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel8.add(jLabel20, gridBagConstraints27);

        GridBagConstraints gridBagConstraints28 = new java.awt.GridBagConstraints();
        gridBagConstraints28.gridx = 1;
        gridBagConstraints28.gridy = 4;
        gridBagConstraints28.fill = java.awt.GridBagConstraints.BOTH;
        jPanel8.add(jPasswordField1, gridBagConstraints28);

        GridBagConstraints gridBagConstraints29 = new java.awt.GridBagConstraints();
        gridBagConstraints29.gridx = 0;
        gridBagConstraints29.gridy = 1;
        gridBagConstraints29.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints29.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints29.weightx = 1.0;
        gridBagConstraints29.weighty = 1.0;
        jPanel4.add(jPanel8, gridBagConstraints29);

        jPanel9.setLayout(new java.awt.GridBagLayout());

        jPanel9.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("localFileInfo")));
        jLabel3.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("DataBase_Directory"));
        GridBagConstraints gridBagConstraints30 = new java.awt.GridBagConstraints();
        gridBagConstraints30.gridx = 0;
        gridBagConstraints30.gridy = 0;
        gridBagConstraints30.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints30.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints30.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel9.add(jLabel3, gridBagConstraints30);

        GridBagConstraints gridBagConstraints31 = new java.awt.GridBagConstraints();
        gridBagConstraints31.gridx = 0;
        gridBagConstraints31.gridy = 1;
        gridBagConstraints31.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints31.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints31.weightx = 0.5;
        gridBagConstraints31.insets = new java.awt.Insets(4, 8, 4, 8);
        jPanel9.add(jTextField3, gridBagConstraints31);

        jButton5.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Browse"));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints32 = new java.awt.GridBagConstraints();
        gridBagConstraints32.gridx = 1;
        gridBagConstraints32.gridy = 1;
        gridBagConstraints32.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints32.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel9.add(jButton5, gridBagConstraints32);

        GridBagConstraints gridBagConstraints33 = new java.awt.GridBagConstraints();
        gridBagConstraints33.gridx = 0;
        gridBagConstraints33.gridy = 2;
        gridBagConstraints33.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints33.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints33.weightx = 1.0;
        gridBagConstraints33.weighty = 1.0;
        jPanel4.add(jPanel9, gridBagConstraints33);

        chgdb.setForeground(new java.awt.Color(255, 0, 51));
        chgdb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Refresh16.gif")));
        chgdb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Apply_DB_Change"));
        chgdb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chgdbActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints34 = new java.awt.GridBagConstraints();
        gridBagConstraints34.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel4.add(chgdb, gridBagConstraints34);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("DatabaseInformation"), jPanel4);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("SMTP_Server"));
        GridBagConstraints gridBagConstraints35 = new java.awt.GridBagConstraints();
        gridBagConstraints35.gridx = 0;
        gridBagConstraints35.gridy = 1;
        gridBagConstraints35.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints35.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints35);

        jLabel2.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Your_Email_Address"));
        GridBagConstraints gridBagConstraints36 = new java.awt.GridBagConstraints();
        gridBagConstraints36.gridx = 0;
        gridBagConstraints36.gridy = 2;
        gridBagConstraints36.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints36.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel1.add(jLabel2, gridBagConstraints36);

        smtptext.setColumns(30);
        GridBagConstraints gridBagConstraints37 = new java.awt.GridBagConstraints();
        gridBagConstraints37.gridx = 1;
        gridBagConstraints37.gridy = 1;
        gridBagConstraints37.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints37.weightx = 1.0;
        jPanel1.add(smtptext, gridBagConstraints37);

        emailtext.setColumns(30);
        GridBagConstraints gridBagConstraints38 = new java.awt.GridBagConstraints();
        gridBagConstraints38.gridx = 1;
        gridBagConstraints38.gridy = 2;
        gridBagConstraints38.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints38.weightx = 1.0;
        jPanel1.add(emailtext, gridBagConstraints38);

        emailbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Enable_Email"));
        GridBagConstraints gridBagConstraints39 = new java.awt.GridBagConstraints();
        gridBagConstraints39.gridx = 0;
        gridBagConstraints39.gridy = 0;
        gridBagConstraints39.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints39.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(emailbox, gridBagConstraints39);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("EmailParameters"), jPanel1);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        colorprint.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Print_In_Color?"));
        GridBagConstraints gridBagConstraints40 = new java.awt.GridBagConstraints();
        gridBagConstraints40.gridx = 0;
        gridBagConstraints40.gridy = 0;
        gridBagConstraints40.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints40.anchor = java.awt.GridBagConstraints.NORTHEAST;
        jPanel5.add(colorprint, gridBagConstraints40);

        logobox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Print_Logo_(GIF/JPG/PNG)"));
        GridBagConstraints gridBagConstraints43 = new java.awt.GridBagConstraints();
        gridBagConstraints43.gridx = 0;
        gridBagConstraints43.gridy = 1;
        gridBagConstraints43.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel5.add(logobox, gridBagConstraints43);

        logofile.setEditable(false);
        GridBagConstraints gridBagConstraints41 = new java.awt.GridBagConstraints();
        gridBagConstraints41.gridx = 1;
        gridBagConstraints41.gridy = 1;
        gridBagConstraints41.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints41.weightx = 1.0;
        gridBagConstraints41.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel5.add(logofile, gridBagConstraints41);

        logobrowse.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Browse"));
        logobrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logobrowseActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints42 = new java.awt.GridBagConstraints();
        gridBagConstraints42.gridx = 2;
        gridBagConstraints42.gridy = 1;
        gridBagConstraints42.fill = java.awt.GridBagConstraints.BOTH;
        jPanel5.add(logobrowse, gridBagConstraints42);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("printing"), jPanel5);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        logging.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Enable_Logging_(requires_program_restart)"));
        GridBagConstraints gridBagConstraints44 = new java.awt.GridBagConstraints();
        gridBagConstraints44.gridx = 0;
        gridBagConstraints44.gridy = 2;
        gridBagConstraints44.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints44.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints44.weightx = 1.0;
        jPanel3.add(logging, gridBagConstraints44);

        autoupdate.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Auto_Update_Check"));
        autoupdate.setToolTipText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Enable_a_daily_check_to_the_BORG_website_to_see_if_a_new_version_is_out._Does_not_update_the_product."));
        GridBagConstraints gridBagConstraints45 = new java.awt.GridBagConstraints();
        gridBagConstraints45.gridx = 0;
        gridBagConstraints45.gridy = 3;
        gridBagConstraints45.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints45.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(autoupdate, gridBagConstraints45);

        versioncheck.setFont(new java.awt.Font("Dialog", 0, 10));
        versioncheck.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Check_for_updates_now"));
        versioncheck.setToolTipText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Check_for_the_latest_BORG_version_now"));
        versioncheck.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        versioncheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versioncheckActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints46 = new java.awt.GridBagConstraints();
        gridBagConstraints46.gridx = 1;
        gridBagConstraints46.gridy = 3;
        gridBagConstraints46.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(versioncheck, gridBagConstraints46);

        splashbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("splash"));
        GridBagConstraints gridBagConstraints47 = new java.awt.GridBagConstraints();
        gridBagConstraints47.gridx = 0;
        gridBagConstraints47.gridy = 0;
        gridBagConstraints47.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(splashbox, gridBagConstraints47);

        backgbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Start_in_background_(Windows_only,_TrayIcon_req)"));
        backgbox.setToolTipText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Do_not_open_todo_and_month_view_on_startup,_start_in_systray"));
        GridBagConstraints gridBagConstraints48 = new java.awt.GridBagConstraints();
        gridBagConstraints48.gridx = 0;
        gridBagConstraints48.gridy = 1;
        gridBagConstraints48.gridwidth = 2;
        gridBagConstraints48.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints48.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(backgbox, gridBagConstraints48);

        stackbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("stackonerr"));
        GridBagConstraints gridBagConstraints49 = new java.awt.GridBagConstraints();
        gridBagConstraints49.gridx = 0;
        gridBagConstraints49.gridy = 4;
        gridBagConstraints49.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(stackbox, gridBagConstraints49);

        icaltodobox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("icaltodo"));
        GridBagConstraints gridBagConstraints50 = new java.awt.GridBagConstraints();
        gridBagConstraints50.gridx = 0;
        gridBagConstraints50.gridy = 5;
        gridBagConstraints50.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(icaltodobox, gridBagConstraints50);

        revDayEditbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("revdayedit"));
        GridBagConstraints gridBagConstraints51 = new java.awt.GridBagConstraints();
        gridBagConstraints51.gridx = 0;
        gridBagConstraints51.gridy = 6;
        gridBagConstraints51.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(revDayEditbox, gridBagConstraints51);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("misc"), jPanel3);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        popenablebox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("enable_popups"));
        GridBagConstraints gridBagConstraints52 = new java.awt.GridBagConstraints();
        gridBagConstraints52.gridx = 0;
        gridBagConstraints52.gridy = 0;
        gridBagConstraints52.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints52.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel6.add(popenablebox, gridBagConstraints52);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel9.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("pop_app"));
        GridBagConstraints gridBagConstraints53 = new java.awt.GridBagConstraints();
        gridBagConstraints53.gridx = 0;
        gridBagConstraints53.gridy = 2;
        gridBagConstraints53.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints53.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel6.add(jLabel9, gridBagConstraints53);

        GridBagConstraints gridBagConstraints54 = new java.awt.GridBagConstraints();
        gridBagConstraints54.gridx = 1;
        gridBagConstraints54.gridy = 2;
        gridBagConstraints54.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints54.weightx = 1.0;
        jPanel6.add(popminbefore, gridBagConstraints54);

        jLabel10.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("min_bef_app"));
        GridBagConstraints gridBagConstraints55 = new java.awt.GridBagConstraints();
        gridBagConstraints55.gridx = 2;
        gridBagConstraints55.gridy = 2;
        gridBagConstraints55.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints55.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel6.add(jLabel10, gridBagConstraints55);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel11.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("pop_app"));
        GridBagConstraints gridBagConstraints56 = new java.awt.GridBagConstraints();
        gridBagConstraints56.gridx = 0;
        gridBagConstraints56.gridy = 3;
        gridBagConstraints56.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints56.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel6.add(jLabel11, gridBagConstraints56);

        GridBagConstraints gridBagConstraints57 = new java.awt.GridBagConstraints();
        gridBagConstraints57.gridx = 1;
        gridBagConstraints57.gridy = 3;
        gridBagConstraints57.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints57.weightx = 1.0;
        jPanel6.add(popminafter, gridBagConstraints57);

        jLabel12.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("min_aft_app"));
        GridBagConstraints gridBagConstraints58 = new java.awt.GridBagConstraints();
        gridBagConstraints58.gridx = 2;
        gridBagConstraints58.gridy = 3;
        gridBagConstraints58.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints58.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel6.add(jLabel12, gridBagConstraints58);

        soundbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("beeps"));
        GridBagConstraints gridBagConstraints59 = new java.awt.GridBagConstraints();
        gridBagConstraints59.gridx = 0;
        gridBagConstraints59.gridy = 5;
        gridBagConstraints59.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints59.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel6.add(soundbox, gridBagConstraints59);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel13.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("beepingstarts"));
        GridBagConstraints gridBagConstraints60 = new java.awt.GridBagConstraints();
        gridBagConstraints60.gridx = 0;
        gridBagConstraints60.gridy = 6;
        gridBagConstraints60.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints60.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel6.add(jLabel13, gridBagConstraints60);

        GridBagConstraints gridBagConstraints61 = new java.awt.GridBagConstraints();
        gridBagConstraints61.gridx = 1;
        gridBagConstraints61.gridy = 6;
        gridBagConstraints61.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints61.weightx = 1.0;
        jPanel6.add(beepmins, gridBagConstraints61);

        jLabel14.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("min_bef_app"));
        GridBagConstraints gridBagConstraints62 = new java.awt.GridBagConstraints();
        gridBagConstraints62.gridx = 2;
        gridBagConstraints62.gridy = 6;
        gridBagConstraints62.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints62.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel6.add(jLabel14, gridBagConstraints62);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel15.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("min_between_chks"));
        GridBagConstraints gridBagConstraints63 = new java.awt.GridBagConstraints();
        gridBagConstraints63.gridx = 0;
        gridBagConstraints63.gridy = 1;
        gridBagConstraints63.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints63.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel6.add(jLabel15, gridBagConstraints63);

        checkfreq.setMinimumSize(new java.awt.Dimension(50, 20));
        GridBagConstraints gridBagConstraints64 = new java.awt.GridBagConstraints();
        gridBagConstraints64.gridx = 1;
        gridBagConstraints64.gridy = 1;
        gridBagConstraints64.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints64.weightx = 1.0;
        jPanel6.add(checkfreq, gridBagConstraints64);

        GridBagConstraints gridBagConstraints65 = new java.awt.GridBagConstraints();
        gridBagConstraints65.gridx = 0;
        gridBagConstraints65.gridy = 4;
        gridBagConstraints65.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints65.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints65.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel6.add(jSeparator1, gridBagConstraints65);

        jLabel16.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("restart_req"));
        GridBagConstraints gridBagConstraints66 = new java.awt.GridBagConstraints();
        gridBagConstraints66.gridx = 2;
        gridBagConstraints66.gridy = 1;
        gridBagConstraints66.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints66.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel6.add(jLabel16, gridBagConstraints66);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("popup_reminders"), jPanel6);

        sharedbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("shared"));
        jPanel7.add(sharedbox);

        jTabbedPane1.addTab(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Multi_User"), jPanel7);

        GridBagConstraints gridBagConstraints67 = new java.awt.GridBagConstraints();
        gridBagConstraints67.gridx = 0;
        gridBagConstraints67.gridy = 0;
        gridBagConstraints67.gridwidth = 2;
        gridBagConstraints67.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints67.weightx = 1.0;
        gridBagConstraints67.weighty = 1.0;
        //getContentPane().add(jTabbedPane1, gridBagConstraints67);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Stop16.gif")));
        jButton2.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Dismiss"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        applyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Save16.gif")));
        applyButton.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("apply"));
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apply(evt);
            }
        });

        this.setContentPane(getJPanel());
        gridBagConstraints110.gridx = 1;
        gridBagConstraints110.gridy = 7;
        gridBagConstraints110.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints110.insets = new java.awt.Insets(4,4,4,4);
        jPanel2.add(getDoyBox(), gridBagConstraints110);
        pack();
    }//GEN-END:initComponents

    private void dbTypeAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dbTypeAction
    {//GEN-HEADEREND:event_dbTypeAction
        if( evt.getActionCommand().equals("mysql"))
        {
        	jPanel8.setVisible(true);
        	jPanel9.setVisible(false);
        }
        else
        {
           	jPanel9.setVisible(true);
        	jPanel8.setVisible(false);
        }
    }//GEN-LAST:event_dbTypeAction

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed


    private void apply(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apply
        applyChanges();
    }//GEN-LAST:event_apply

    private void applyChanges()
    {
        if( icaltodobox.isSelected() )
            Prefs.putPref(PrefName.ICALTODOEV, "true" );
        else
            Prefs.putPref(PrefName.ICALTODOEV, "false" );
        
        if( colorsortbox.isSelected() )
            Prefs.putPref(PrefName.COLORSORT, "true" );
        else
            Prefs.putPref(PrefName.COLORSORT, "false" );
        
        if( stackbox.isSelected() )
            Prefs.putPref(PrefName.STACKTRACE, "true" );
        else
            Prefs.putPref(PrefName.STACKTRACE, "false" );
        
        if( sharedbox.isSelected() )
            Prefs.putPref(PrefName.SHARED, "true" );
        else
            Prefs.putPref(PrefName.SHARED, "false" );
        
        Integer i = (Integer) beepmins.getValue();
        int cur = Prefs.getIntPref(PrefName.BEEPINGMINS);
        if( i.intValue() != cur )
            Prefs.putPref(PrefName.BEEPINGMINS, i);
        
        i = (Integer) popminafter.getValue();
        cur = Prefs.getIntPref(PrefName.POPAFTERMINS);
        if( i.intValue() != cur )
            Prefs.putPref(PrefName.POPAFTERMINS, i);
        
        i = (Integer) popminbefore.getValue();
        cur = Prefs.getIntPref(PrefName.POPBEFOREMINS);
        if( i.intValue() != cur )
            Prefs.putPref(PrefName.POPBEFOREMINS, i);
         
        i = (Integer) checkfreq.getValue();
        cur = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
        if( i.intValue() != cur )
            Prefs.putPref(PrefName.REMINDERCHECKMINS, i);
        
        if( soundbox.isSelected() )
            Prefs.putPref(PrefName.BEEPINGREMINDERS, "true" );
        else
            Prefs.putPref(PrefName.BEEPINGREMINDERS, "false" );
        
        if( popenablebox.isSelected() )
            Prefs.putPref(PrefName.REMINDERS, "true" );
        else
            Prefs.putPref(PrefName.REMINDERS, "false" );
        
        if( splashbox.isSelected() )
            Prefs.putPref(PrefName.SPLASH, "true" );
        else
            Prefs.putPref(PrefName.SPLASH, "false" );
        
        // update US holiday preference and refresh the month view accordingly
        if( canadabox.isSelected() )
            Prefs.putPref(PrefName.SHOWCANHOLIDAYS, "true" );
        else
            Prefs.putPref(PrefName.SHOWCANHOLIDAYS, "false" );
        
        if( wrapbox.isSelected() )
            Prefs.putPref(PrefName.WRAP, "true" );
        else
            Prefs.putPref(PrefName.WRAP, "false" );
        
        if( backgbox.isSelected() )
            Prefs.putPref(PrefName.BACKGSTART, "true" );
        else
            Prefs.putPref(PrefName.BACKGSTART, "false" );
        
        if( miltime.isSelected() )
            Prefs.putPref(PrefName.MILTIME, "true" );
        else
            Prefs.putPref(PrefName.MILTIME, "false" );
        
        if( mondaycb.isSelected() )
            Prefs.putPref(PrefName.FIRSTDOW, new Integer(Calendar.MONDAY) );
        else
            Prefs.putPref(PrefName.FIRSTDOW, new Integer(Calendar.SUNDAY) );
        
        if( holiday1.isSelected() )
            Prefs.putPref(PrefName.SHOWUSHOLIDAYS, "true" );
        else
            Prefs.putPref(PrefName.SHOWUSHOLIDAYS, "false" );
        
        if( doyBox.isSelected() )
            Prefs.putPref(PrefName.DAYOFYEAR, "true" );
        else
            Prefs.putPref(PrefName.DAYOFYEAR, "false" );
        
        if( revDayEditbox.isSelected() )
            Prefs.putPref(PrefName.REVERSEDAYEDIT, "true" );
        else
            Prefs.putPref(PrefName.REVERSEDAYEDIT, "false" );
        
        Prefs.putPref( PrefName.WKENDHOUR, wkendhr.getSelectedItem());
        Prefs.putPref( PrefName.WKSTARTHOUR, wkstarthr.getSelectedItem());
        
        // enable/disable auto-update-check
        // value is the last day-of-year that check was done (1-365)
        // phony value 400 will cause check during current day
        // value -1 is the shut-off value
        if( autoupdate.isSelected() )
            Prefs.putPref(PrefName.VERCHKLAST, new Integer(400) );
        else
            Prefs.putPref(PrefName.VERCHKLAST, new Integer(-1) );
        
        if( privbox.isSelected() )
            Prefs.putPref(PrefName.SHOWPRIVATE, "true" );
        else
            Prefs.putPref(PrefName.SHOWPRIVATE, "false" );
        
        // update the show public option and refresh the month view
        if( pubbox.isSelected() )
            Prefs.putPref(PrefName.SHOWPUBLIC, "true" );
        else
            Prefs.putPref(PrefName.SHOWPUBLIC, "false" );
        
        if( colorprint.isSelected() )
            Prefs.putPref(PrefName.COLORPRINT, "true" );
        else
            Prefs.putPref(PrefName.COLORPRINT, "false" );
        
        if( !logobox.isSelected() )
        {
            Prefs.putPref(PrefName.LOGO, "" );
            logofile.setText("");
        }
        else
        {
            Prefs.putPref(PrefName.LOGO, logofile.getText() );
        }
        
        if( emailbox.isSelected() )
        {
            Prefs.putPref(PrefName.EMAILENABLED, "true" );
            Prefs.putPref(PrefName.EMAILSERVER, smtptext.getText() );
            Prefs.putPref(PrefName.EMAILADDR, emailtext.getText() );
        }
        else
            Prefs.putPref(PrefName.EMAILENABLED, "false" );
        
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
        
        Locale locs[] = Locale.getAvailableLocales();
        String choice = (String) localebox.getSelectedItem();
        for( int ii = 0; ii < locs.length; ii++ )
        {
            if( choice.equals(locs[ii].getDisplayName()))
            {
                Prefs.putPref(PrefName.COUNTRY, locs[ii].getCountry() );
                Prefs.putPref(PrefName.LANGUAGE, locs[ii].getLanguage());
            }
        }
        
        String newlnf = (String) lnfBox.getSelectedItem();
        String oldlnf = Prefs.getPref(PrefName.LNF);
        if( !newlnf.equals(oldlnf) )
        {
            try
            {
                UIManager.setLookAndFeel(newlnf);
                // don't try to change the main window l&f - is doesn't work 100%
                //SwingUtilities.updateComponentTreeUI(cg_);
                Prefs.putPref(PrefName.LNF, newlnf );
            }
            catch( Exception e )
            {
                // Errmsg.notice( "Could not find look and feel: " + newlnf );
                Errmsg.notice( e.toString() );
                return;
            }
        }
        
        Prefs.notifyListeners();
        
    }
 
  
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       
        Font f = NwFontChooserS.showDialog(null, null,null);
        if( f == null) return;
        String fs = NwFontChooserS.fontString(f);
        Prefs.putPref(PrefName.DEFFONT, fs );
        NwFontChooserS.setDefaultFont(f);
        SwingUtilities.updateComponentTreeUI(this);
        Prefs.notifyListeners();
    
    }//GEN-LAST:event_jButton1ActionPerformed
    

    private void chgdbActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_chgdbActionPerformed
    {//GEN-HEADEREND:event_chgdbActionPerformed
        int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("Really_change_the_database?"), Resource.getResourceString("Confirm_DB_Change"), JOptionPane.YES_NO_OPTION);
        if( ret == JOptionPane.YES_OPTION )
        {
            String dbdir = jTextField3.getText();
            Prefs.putPref(PrefName.DBDIR, dbdir );
            
            if( MySQL.isSelected())
            {
            	Prefs.putPref( PrefName.DBTYPE, "mysql");
            }
            else
            {
            	Prefs.putPref( PrefName.DBTYPE, "local");
            }
            

            Prefs.putPref( PrefName.DBNAME, jTextField1.getText());
            Prefs.putPref( PrefName.DBPORT, jTextField4.getText());
            Prefs.putPref( PrefName.DBHOST, jTextField2.getText());
            Prefs.putPref( PrefName.DBUSER, jTextField5.getText());
            Prefs.putPref( PrefName.DBPASS, jPasswordField1.getText());
            
            if( rl_ != null )
            	rl_.restart();
            
            this.dispose();
        }
    }//GEN-LAST:event_chgdbActionPerformed
    
 
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
        
 
        // update text field - nothing else changes. DB change will take effect only on restart
        logofile.setText(logo);
    }//GEN-LAST:event_logobrowseActionPerformed
   
    
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
   
    
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        
        // browse for new database dir
        String dbdir = OptionsView.chooseDbDir(false);
        if( dbdir == null ) return;
        
        // update text field - nothing else changes. DB change will take effect only on restart
        jTextField3.setText(dbdir);
        
        
    }//GEN-LAST:event_jButton5ActionPerformed
    
    private void incfontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incfontActionPerformed
        
        Font f = NwFontChooserS.showDialog(null, null, null);
        if( f == null) return;
        String s = NwFontChooserS.fontString(f);
  
        Prefs.putPref( PrefName.PREVIEWFONT, s );
        Prefs.notifyListeners();
        
    }//GEN-LAST:event_incfontActionPerformed
    
    private void decfontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decfontActionPerformed
       
        Font f = NwFontChooserS.showDialog(null, null, null);
        if( f == null) return;
        String s = NwFontChooserS.fontString(f);
  
        Prefs.putPref(PrefName.APPTFONT, s );
        Prefs.notifyListeners();
        
    }//GEN-LAST:event_decfontActionPerformed

        
    
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
	        
	        if( err == null )
	            break;
	        
	        Errmsg.notice( err );
	    }
	    
	    if( update )
	        Prefs.putPref(PrefName.DBDIR, dbdir );
	    return(dbdir);
	}
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton MySQL;
    private javax.swing.JButton applyButton;
    private javax.swing.JCheckBox autoupdate;
    private javax.swing.JCheckBox backgbox;
    private javax.swing.JSpinner beepmins;
    private javax.swing.JCheckBox canadabox;
    private javax.swing.JSpinner checkfreq;
    private javax.swing.JButton chgdb;
    private javax.swing.JCheckBox colorprint;
    private javax.swing.JCheckBox colorsortbox;
    private javax.swing.ButtonGroup dbTypeGroup;
    private javax.swing.JButton decfont;
    private javax.swing.JCheckBox emailbox;
    private javax.swing.JTextField emailtext;
    private javax.swing.JCheckBox holiday1;
    private javax.swing.JCheckBox icaltodobox;
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
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
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
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JComboBox lnfBox;
    private javax.swing.JRadioButton localFileButton;
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
    private javax.swing.JCheckBox revDayEditbox;
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
    
	private JPanel jPanel = null;
	private JPanel jPanel10 = null;
	private JCheckBox doyBox = null;
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints510 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints210 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints210.gridx = 0;
			gridBagConstraints210.gridy = 0;
			gridBagConstraints210.weightx = 1.0;
			gridBagConstraints210.weighty = 1.0;
			gridBagConstraints210.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints210.insets = new java.awt.Insets(4,4,4,4);
			gridBagConstraints210.gridwidth = 1;
			gridBagConstraints510.gridx = 0;
			gridBagConstraints510.gridy = 1;
			gridBagConstraints510.insets = new java.awt.Insets(4,4,4,4);
			jPanel.add(jTabbedPane1, gridBagConstraints210);
			jPanel.add(getJPanel10(), gridBagConstraints510);
		}
		return jPanel;
	}
	/**
	 * This method initializes jPanel10	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel10() {
		if (jPanel10 == null) {
			jPanel10 = new JPanel();
			jPanel10.add(jButton2, null);
			jPanel10.add(applyButton, null);
		}
		return jPanel10;
	}
	/**
	 * This method initializes doyBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getDoyBox() {
		if (doyBox == null) {
			doyBox = new JCheckBox();
			doyBox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("showdoy"));
		}
		return doyBox;
	}
   }
