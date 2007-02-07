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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.sf.borg.common.ui.JButtonKnowsBgColor;
import net.sf.borg.common.ui.NwFontChooserS;
import net.sf.borg.common.ui.StripedTable;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.model.AppointmentModel;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.SwingConstants;

// propgui displays the edit preferences window
public class OptionsView extends View {

    /**
         * 
         */
    private static final long serialVersionUID = 4743111117071445783L;

    // to break a dependency with the contol package
    public interface RestartListener {
	public void restart();
    }

    static private RestartListener rl_ = null; // someone to call to

    // request a

    private static OptionsView singleton = null;

    // restart

    static {
	int rgb = Integer.parseInt(Prefs.getPref(PrefName.UCS_STRIPE));
	StripedTable.setStripeColor(new Color(rgb));
    }

    // prompt the user to enter a database directory
    public static String chooseDbDir(boolean update) {

	String dbdir = null;
	while (true) {
	    JFileChooser chooser = new JFileChooser();

	    chooser.setCurrentDirectory(new File("."));
	    chooser
		    .setDialogTitle("Please choose directory for database files");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	    int returnVal = chooser.showOpenDialog(null);
	    if (returnVal != JFileChooser.APPROVE_OPTION) {
		return (null);
	    }

	    dbdir = chooser.getSelectedFile().getAbsolutePath();
	    File dir = new File(dbdir);
	    String err = null;
	    if (!dir.exists()) {
		err = "Database Directory [" + dbdir + "] does not exist";
	    } else if (!dir.isDirectory()) {
		err = "Database Directory [" + dbdir + "] is not a directory";
	    }

	    if (err == null) {
		break;
	    }

	    Errmsg.notice(err);
	}

	if (update) {
	    Prefs.putPref(PrefName.DBDIR, dbdir);
	}
	return (dbdir);
    }

    public static void dbSelectOnly() {
	new OptionsView(true).setVisible(true);

    }

    public static OptionsView getReference() {
	if (singleton == null || !singleton.isShowing()) {
	    singleton = new OptionsView(false);
	}
	return (singleton);
    }

    static public void setRestartListener(RestartListener rl) {
	rl_ = rl;
    }

    static private void setBooleanPref(JCheckBox box, PrefName pn) {
	if (box.isSelected()) {
	    Prefs.putPref(pn, "true");
	} else {
	    Prefs.putPref(pn, "false");
	}
    }

    static private void setCheckBox(JCheckBox box, PrefName pn) {
	String val = Prefs.getPref(pn);
	if (val.equals("true")) {
	    box.setSelected(true);
	} else {
	    box.setSelected(false);
	}
    }

    private javax.swing.JButton applyButton;

    private JPanel applyDismissPanel = null;

    private javax.swing.JButton apptFontButton;

    private javax.swing.JCheckBox autoupdate;

    private javax.swing.JCheckBox backgbox;

    private JButtonKnowsBgColor btn_ucs_birthdays;

    private JButtonKnowsBgColor btn_ucs_black;

    private JButtonKnowsBgColor btn_ucs_blue;

    private JButtonKnowsBgColor btn_ucs_default;

    private JButtonKnowsBgColor btn_ucs_green;

    private JButtonKnowsBgColor btn_ucs_halfday;

    private JButtonKnowsBgColor btn_ucs_holiday;

    private JButtonKnowsBgColor btn_ucs_holidays;

    private JButtonKnowsBgColor btn_ucs_red;

    private JButton btn_ucs_restore;

    private JButtonKnowsBgColor btn_ucs_stripe;

    private JButtonKnowsBgColor btn_ucs_tasks;

    private JButtonKnowsBgColor btn_ucs_today;

    private JButtonKnowsBgColor btn_ucs_vacation;

    private JButtonKnowsBgColor btn_ucs_weekday;

    private JButtonKnowsBgColor btn_ucs_weekend;

    private JButtonKnowsBgColor btn_ucs_white;

    private javax.swing.JCheckBox canadabox;

    private javax.swing.JCheckBox cb_ucs_marktodo;

    // added by bsv 2004-12-20
    private javax.swing.JCheckBox cb_ucs_on;

    private javax.swing.JCheckBox cb_ucs_ontodo;

    private javax.swing.JSpinner checkfreq;

    private javax.swing.JButton chgdb;

    private javax.swing.JCheckBox colorprint;

    private javax.swing.JCheckBox colorsortbox;

    private javax.swing.JButton dayFontButton = new JButton();

    private javax.swing.JTextField dbDirText;

    private javax.swing.JTextField dbHostText;

    private javax.swing.JTextField dbNameText;

    private javax.swing.JTextField dbPortText;

    private javax.swing.ButtonGroup dbTypeGroup;

    private JPanel dbTypePanel = null;

    private javax.swing.JTextField dbUserText;

    private javax.swing.JButton defFontButton;

    private javax.swing.JButton dismissButton;

    private JCheckBox doyBox = null;

    private javax.swing.JCheckBox emailbox;

    private javax.swing.JTextField emailtext;

    /**
         * This method initializes emailtimebox
         * 
         * @return javax.swing.JComboBox
         */
    private JSpinner emailtimebox = null;

    private JCheckBox exputcbox = null;

    private JCheckBox extraDayBox;

    private javax.swing.JCheckBox holiday1;

    /**
         * This method initializes jRadioButton
         * 
         * @return javax.swing.JRadioButton
         */

    private JRadioButton hsqldbButton;

    private JPanel hsqldbPanel;

    private javax.swing.JCheckBox icaltodobox;

    private javax.swing.JCheckBox iso8601Box = new JCheckBox();

    private javax.swing.JButton jButton5;

    private JRadioButton jdbcButton = null;

    private JPanel jdbcPanel = null;

    private JTextField jdbcText = null;

    private JLabel jLabel = null;

    private javax.swing.JLabel jLabel1;

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

    private JPanel jPanelUCS = null;

    private javax.swing.JPasswordField jPasswordField1;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JTabbedPane jTabbedPane1;

    private javax.swing.JComboBox lnfBox;

    private javax.swing.JComboBox localebox;

    private JRadioButton localFileButton = null;

    // End of variables declaration//GEN-END:variables

    private javax.swing.JPanel localFilePanel;

    private javax.swing.JCheckBox logobox;

    private javax.swing.JButton logobrowse;

    private javax.swing.JTextField logofile;

    private JComboBox lsbox = null;

    private JLabel lslabel = null;

    private javax.swing.JCheckBox miltime;

    private javax.swing.JCheckBox mondaycb;

    private javax.swing.JButton monthFontButton = new JButton();

    private JRadioButton MySQLButton = null;

    private javax.swing.JPanel mysqlPanel;

    private JCheckBox palmcb = null;

    private javax.swing.JCheckBox popenablebox;

    private javax.swing.JButton previewFontButton;

    private javax.swing.JCheckBox privbox;

    private javax.swing.JCheckBox pubbox;

    private JRadioButton remoteButton = null;

    private JPanel remoteServerPanel = null;

    private JTextField remoteURLText = null;

    private JLabel remtimelabel = null;

    private ReminderTimePanel remTimePanel = new ReminderTimePanel();

    // (added by bsv 2004-12-20)

    private javax.swing.JCheckBox revDayEditbox;

    private javax.swing.JCheckBox sharedFileCheckBox;

    private javax.swing.JTextField smtptext;

    private javax.swing.JCheckBox soundbox;

    private javax.swing.JCheckBox splashbox;

    private javax.swing.JCheckBox stackbox;

    private JLabel syncminlabel = null;

    /**
         * This method initializes jTextField
         * 
         * @return javax.swing.JTextField
         */
    private JSpinner syncmins;

    private javax.swing.JTextField tf_ucs_marker;

    private JPanel topPanel = null;

    private JCheckBox truncbox = null;

    private JCheckBox useBeep = null;

    private javax.swing.JButton versioncheck;

    private javax.swing.JButton weekFontButton = new JButton();

    private javax.swing.JComboBox wkendhr;

    private javax.swing.JComboBox wkstarthr;

    private javax.swing.JCheckBox wrapbox;

    JTextField hsqldbdir = new JTextField();

    JCheckBox indEmailBox = new JCheckBox();

    JSpinner indEmailTime = null;

    JPasswordField smpw = new JPasswordField();

    JTextField socketPort = new JTextField();

    JTextField usertext = new JTextField();

    JCheckBox useSysTray = new JCheckBox();

    // dbonly will only allow db changes
    private OptionsView(boolean dbonly) {
	super();

	initComponents();
	dbTypeGroup = new javax.swing.ButtonGroup();
	dbTypeGroup.add(hsqldbButton);
	dbTypeGroup.add(MySQLButton);
	dbTypeGroup.add(remoteButton);
	dbTypeGroup.add(localFileButton);
	dbTypeGroup.add(jdbcButton);

	if (!dbonly) {
	    addModel(AppointmentModel.getReference());
	} else {
	    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	}

	// set the various screen items based on the existing user
	// preferences

	String ls = Prefs.getPref(PrefName.LINESPACING);

	lsbox.addItem("-0.5");
	lsbox.addItem("-0.3");
	lsbox.addItem("-0.15");
	lsbox.addItem("0.0");
	lsbox.addItem("0.5");
	lsbox.addItem("1.0");

	lsbox.setSelectedItem(ls);

	int emmins = Prefs.getIntPref(PrefName.EMAILTIME);
	Calendar cal = new GregorianCalendar(1980, 1, 1, 0, 0, 0);
	cal.add(Calendar.MINUTE, emmins);
	emailtimebox.setValue(cal.getTime());

	getIndEmailtimebox().setValue(
		new Integer(Prefs.getIntPref(PrefName.INDIVEMAILMINS)));

	//
	// database
	//
	String dbtype = Prefs.getPref(PrefName.DBTYPE);
	if (dbtype.equals("mysql")) {
	    MySQLButton.setSelected(true);
	} else if (dbtype.equals("remote")) {
	    remoteButton.setSelected(true);
	} else if (dbtype.equals("hsqldb")) {
	    hsqldbButton.setSelected(true);
	} else if (dbtype.equals("jdbc")) {
	    jdbcButton.setSelected(true);
	} else {
	    localFileButton.setSelected(true);
	}
	dbTypeChange(dbtype);

	dbDirText.setText(Prefs.getPref(PrefName.DBDIR));
	dbNameText.setText(Prefs.getPref(PrefName.DBNAME));
	dbPortText.setText(Prefs.getPref(PrefName.DBPORT));
	dbHostText.setText(Prefs.getPref(PrefName.DBHOST));
	dbUserText.setText(Prefs.getPref(PrefName.DBUSER));
	jPasswordField1.setText(Prefs.getPref(PrefName.DBPASS));
	remoteURLText.setText(Prefs.getPref(PrefName.DBURL));
	jdbcText.setText(Prefs.getPref(PrefName.JDBCURL));
	hsqldbdir.setText(Prefs.getPref(PrefName.HSQLDBDIR));

	if (dbonly) {
	    // disable lots of non-db-related stuff
	    jTabbedPane1.setEnabledAt(0, false);
	    jTabbedPane1.setEnabledAt(1, false);
	    jTabbedPane1.setEnabledAt(3, false);
	    jTabbedPane1.setEnabledAt(4, false);
	    jTabbedPane1.setEnabledAt(5, false);
	    jTabbedPane1.setEnabledAt(6, false);
	    jTabbedPane1.setEnabledAt(7, false);
	    jTabbedPane1.setEnabledAt(8, false);
	    jTabbedPane1.setSelectedIndex(2);
	    dismissButton.setEnabled(false);
	    applyButton.setEnabled(false);
	    return;

	}

	// set various simple boolean checkboxes
	setCheckBox(colorprint, PrefName.COLORPRINT);
	setCheckBox(pubbox, PrefName.SHOWPUBLIC);
	setCheckBox(privbox, PrefName.SHOWPRIVATE);
	setCheckBox(emailbox, PrefName.EMAILENABLED);
	setCheckBox(indEmailBox, PrefName.INDIVEMAILENABLED);
	setCheckBox(holiday1, PrefName.SHOWUSHOLIDAYS);
	setCheckBox(canadabox, PrefName.SHOWCANHOLIDAYS);
	setCheckBox(doyBox, PrefName.DAYOFYEAR);
	setCheckBox(exputcbox, PrefName.ICALUTC);
	setCheckBox(colorsortbox, PrefName.COLORSORT);
	setCheckBox(miltime, PrefName.MILTIME);
	setCheckBox(backgbox, PrefName.BACKGSTART);
	setCheckBox(splashbox, PrefName.SPLASH);
	setCheckBox(stackbox, PrefName.STACKTRACE);
	setCheckBox(wrapbox, PrefName.WRAP);
	setCheckBox(revDayEditbox, PrefName.REVERSEDAYEDIT);
	setCheckBox(popenablebox, PrefName.REMINDERS);
	setCheckBox(soundbox, PrefName.BEEPINGREMINDERS);
	setCheckBox(palmcb, PrefName.PALM_SYNC);
	setCheckBox(useBeep, PrefName.USESYSTEMBEEP);
	setCheckBox(sharedFileCheckBox, PrefName.SHARED);
	setCheckBox(icaltodobox, PrefName.ICALTODOEV);
	setCheckBox(truncbox, PrefName.TRUNCAPPT);
	setCheckBox(iso8601Box, PrefName.ISOWKNUMBER);
	setCheckBox(extraDayBox, PrefName.SHOWEXTRADAYS);
	setCheckBox(useSysTray, PrefName.USESYSTRAY);
	setCheckBox(taskAbbrevBox, PrefName.TASK_SHOW_ABBREV);
	setCheckBox(calShowTaskBox, PrefName.CAL_SHOW_TASKS);
	setCheckBox(calShowSubtaskBox, PrefName.CAL_SHOW_SUBTASKS);

	int socket = Prefs.getIntPref(PrefName.SOCKETPORT);
	socketPort.setText(Integer.toString(socket));

	// print logo directory
	String logo = Prefs.getPref(PrefName.LOGO);
	logofile.setText(logo);
	if (!logo.equals("")) {
	    logobox.setSelected(true);
	} else {
	    logobox.setSelected(false);
	}

	// email server and address
	smtptext.setText(Prefs.getPref(PrefName.EMAILSERVER));
	emailtext.setText(Prefs.getPref(PrefName.EMAILADDR));
	usertext.setText(Prefs.getPref(PrefName.EMAILUSER));
	smpw.setText(Prefs.getPref(PrefName.EMAILPASS));

	int fdow = Prefs.getIntPref(PrefName.FIRSTDOW);
	if (fdow == Calendar.MONDAY) {
	    mondaycb.setSelected(true);
	} else {
	    mondaycb.setSelected(false);
	}

	// auto update check
	int au = Prefs.getIntPref(PrefName.VERCHKLAST);
	if (au != -1) {
	    autoupdate.setSelected(true);
	} else {
	    autoupdate.setSelected(false);
	}

	// add installed look and feels to lnfBox
	lnfBox.removeAllItems();
	TreeSet lnfs = new TreeSet();
	String curlnf = Prefs.getPref(PrefName.LNF);
	LookAndFeelInfo lnfinfo[] = UIManager.getInstalledLookAndFeels();
	for (int i = 0; i < lnfinfo.length; i++) {
	    String name = lnfinfo[i].getClassName();
	    lnfs.add(name);
	}
	try {
	    Class.forName("com.jgoodies.looks.plastic.PlasticLookAndFeel");
	    lnfs.add("com.jgoodies.looks.plastic.PlasticLookAndFeel");
	} catch (Exception e) {
	}
	try {
	    Class.forName("com.jgoodies.looks.windows.WindowsLookAndFeel");
	    lnfs.add("com.jgoodies.looks.windows.WindowsLookAndFeel");
	} catch (Exception e) {
	}
	try {
	    Class.forName("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
	    lnfs.add("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
	} catch (Exception e) {
	}
	try {
	    Class.forName("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
	    lnfs.add("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
	} catch (Exception e) {
	}
	try {
	    Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
	    lnfs.add("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
	} catch (Exception e) {
	}
	try {
	    Class.forName("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
	    lnfs.add("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
	} catch (Exception e) {
	}

	lnfs.add(curlnf);

	Iterator it = lnfs.iterator();
	while (it.hasNext()) {
	    lnfBox.addItem(it.next());
	}

	lnfBox.setSelectedItem(curlnf);
	lnfBox.setEditable(false);

	String shr = Prefs.getPref(PrefName.WKSTARTHOUR);
	String ehr = Prefs.getPref(PrefName.WKENDHOUR);
	wkstarthr.setSelectedItem(shr);
	wkendhr.setSelectedItem(ehr);

	// add locales
	String nolocale = Prefs.getPref(PrefName.NOLOCALE);
	if (!nolocale.equals("1")) {
	    localebox.removeAllItems();

	    Locale locs[] = Locale.getAvailableLocales();
	    for (int i = 0; i < locs.length; i++) {
		// String name = locs[i].
		localebox.addItem(locs[i].getDisplayName());
	    }

	    String currentlocale = Locale.getDefault().getDisplayName();
	    localebox.setSelectedItem(currentlocale);
	} else {
	    localebox.setEnabled(false);
	}

	int mins = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
	checkfreq.setValue(new Integer(mins));

	mins = Prefs.getIntPref(PrefName.SYNCMINS);
	syncmins.setValue(new Integer(mins));

	// bsv 2004-12-20
	// initiate user color scheme variables
	setCheckBox(cb_ucs_on, PrefName.UCS_ON);
	setCheckBox(cb_ucs_ontodo, PrefName.UCS_ONTODO);
	setCheckBox(cb_ucs_marktodo, PrefName.UCS_MARKTODO);

	tf_ucs_marker.setText(Prefs.getPref(PrefName.UCS_MARKER));
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_RED));
	btn_ucs_red.setColorProperty(new Color(mins));
	btn_ucs_red.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_BLUE));
	btn_ucs_blue.setColorProperty(new Color(mins));
	btn_ucs_blue.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_GREEN));
	btn_ucs_green.setColorProperty(new Color(mins));
	btn_ucs_green.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_BLACK));
	btn_ucs_black.setColorProperty(new Color(mins));
	btn_ucs_black.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_WHITE));
	btn_ucs_white.setColorProperty(new Color(mins));
	btn_ucs_white.setColorByProperty();

	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_NAVY));
	btn_ucs_tasks.setColorProperty(new Color(mins));
	btn_ucs_tasks.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_PURPLE));
	btn_ucs_holidays.setColorProperty(new Color(mins));
	btn_ucs_holidays.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_BRICK));
	btn_ucs_birthdays.setColorProperty(new Color(mins));
	btn_ucs_birthdays.setColorByProperty();

	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_DEFAULT));
	btn_ucs_default.setColorProperty(new Color(mins));
	btn_ucs_default.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_TODAY));
	btn_ucs_today.setColorProperty(new Color(mins));
	btn_ucs_today.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_HOLIDAY));
	btn_ucs_holiday.setColorProperty(new Color(mins));
	btn_ucs_holiday.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_HALFDAY));
	btn_ucs_halfday.setColorProperty(new Color(mins));
	btn_ucs_halfday.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_VACATION));
	btn_ucs_vacation.setColorProperty(new Color(mins));
	btn_ucs_vacation.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_WEEKEND));
	btn_ucs_weekend.setColorProperty(new Color(mins));
	btn_ucs_weekend.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_WEEKDAY));
	btn_ucs_weekday.setColorProperty(new Color(mins));
	btn_ucs_weekday.setColorByProperty();
	mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_STRIPE));
	btn_ucs_stripe.setColorProperty(new Color(mins));
	btn_ucs_stripe.setColorByProperty();

	logobrowse.setEnabled(true);

	manageMySize(PrefName.OPTVIEWSIZE);
    }

    public void destroy() {
	this.dispose();
    }

    public void refresh() {
    }

    private void apply(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_apply
	applyChanges();
    }// GEN-LAST:event_apply

    private void applyChanges() {

	setBooleanPref(colorprint, PrefName.COLORPRINT);
	setBooleanPref(pubbox, PrefName.SHOWPUBLIC);
	setBooleanPref(privbox, PrefName.SHOWPRIVATE);
	setBooleanPref(emailbox, PrefName.EMAILENABLED);
	setBooleanPref(indEmailBox, PrefName.INDIVEMAILENABLED);
	setBooleanPref(holiday1, PrefName.SHOWUSHOLIDAYS);
	setBooleanPref(canadabox, PrefName.SHOWCANHOLIDAYS);
	setBooleanPref(doyBox, PrefName.DAYOFYEAR);
	setBooleanPref(exputcbox, PrefName.ICALUTC);
	setBooleanPref(colorsortbox, PrefName.COLORSORT);
	setBooleanPref(miltime, PrefName.MILTIME);
	setBooleanPref(backgbox, PrefName.BACKGSTART);
	setBooleanPref(splashbox, PrefName.SPLASH);
	setBooleanPref(stackbox, PrefName.STACKTRACE);
	setBooleanPref(wrapbox, PrefName.WRAP);
	setBooleanPref(revDayEditbox, PrefName.REVERSEDAYEDIT);
	setBooleanPref(popenablebox, PrefName.REMINDERS);
	setBooleanPref(soundbox, PrefName.BEEPINGREMINDERS);
	setBooleanPref(palmcb, PrefName.PALM_SYNC);
	setBooleanPref(useBeep, PrefName.USESYSTEMBEEP);
	setBooleanPref(sharedFileCheckBox, PrefName.SHARED);
	setBooleanPref(icaltodobox, PrefName.ICALTODOEV);
	setBooleanPref(truncbox, PrefName.TRUNCAPPT);
	setBooleanPref(iso8601Box, PrefName.ISOWKNUMBER);
	setBooleanPref(extraDayBox, PrefName.SHOWEXTRADAYS);
	setBooleanPref(useSysTray, PrefName.USESYSTRAY);
	setBooleanPref(taskAbbrevBox, PrefName.TASK_SHOW_ABBREV);
	setBooleanPref(calShowTaskBox, PrefName.CAL_SHOW_TASKS);
	setBooleanPref(calShowSubtaskBox, PrefName.CAL_SHOW_SUBTASKS);

	try {
	    int socket = Integer.parseInt(socketPort.getText());
	    Prefs.putPref(PrefName.SOCKETPORT, new Integer(socket));
	} catch (NumberFormatException e) {
	    Errmsg.notice(Resource.getPlainResourceString("socket_warn"));
	    socketPort.setText("-1");
	    Prefs.putPref(PrefName.SOCKETPORT, new Integer(-1));
	    return;
	}

	Integer i = (Integer) getIndEmailtimebox().getValue();
	int cur = Prefs.getIntPref(PrefName.INDIVEMAILMINS);
	if (i.intValue() != cur) {
	    Prefs.putPref(PrefName.INDIVEMAILMINS, i);
	}

	i = (Integer) checkfreq.getValue();
	cur = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
	if (i.intValue() != cur) {
	    Prefs.putPref(PrefName.REMINDERCHECKMINS, i);
	}

	i = (Integer) syncmins.getValue();
	cur = Prefs.getIntPref(PrefName.SYNCMINS);
	if (i.intValue() != cur) {
	    Prefs.putPref(PrefName.SYNCMINS, i);
	}

	if (mondaycb.isSelected()) {
	    Prefs.putPref(PrefName.FIRSTDOW, new Integer(Calendar.MONDAY));
	} else {
	    Prefs.putPref(PrefName.FIRSTDOW, new Integer(Calendar.SUNDAY));
	}

	Prefs.putPref(PrefName.WKENDHOUR, wkendhr.getSelectedItem());
	Prefs.putPref(PrefName.WKSTARTHOUR, wkstarthr.getSelectedItem());

	// enable/disable auto-update-check
	// value is the last day-of-year that check was done (1-365)
	// phony value 400 will cause check during current day
	// value -1 is the shut-off value
	if (autoupdate.isSelected()) {
	    Prefs.putPref(PrefName.VERCHKLAST, new Integer(400));
	} else {
	    Prefs.putPref(PrefName.VERCHKLAST, new Integer(-1));
	}

	// bsv 2004-12-20
	setBooleanPref(cb_ucs_on, PrefName.UCS_ON);
	setBooleanPref(cb_ucs_ontodo, PrefName.UCS_ONTODO);
	setBooleanPref(cb_ucs_marktodo, PrefName.UCS_MARKTODO);

	if (cb_ucs_marktodo.isSelected()) {
	    Prefs.putPref(PrefName.UCS_MARKER, tf_ucs_marker.getText());
	}

	Integer ucsi = new Integer((btn_ucs_red.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_RED, ucsi.toString());
	ucsi = new Integer((btn_ucs_blue.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_BLUE, ucsi.toString());
	ucsi = new Integer((btn_ucs_green.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_GREEN, ucsi.toString());
	ucsi = new Integer((btn_ucs_black.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_BLACK, ucsi.toString());
	ucsi = new Integer((btn_ucs_white.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_WHITE, ucsi.toString());

	ucsi = new Integer((btn_ucs_tasks.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_NAVY, ucsi.toString());
	ucsi = new Integer((btn_ucs_holidays.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_PURPLE, ucsi.toString());
	ucsi = new Integer((btn_ucs_birthdays.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_BRICK, ucsi.toString());

	ucsi = new Integer((btn_ucs_default.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_DEFAULT, ucsi.toString());
	ucsi = new Integer((btn_ucs_holiday.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_HOLIDAY, ucsi.toString());
	ucsi = new Integer((btn_ucs_halfday.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_HALFDAY, ucsi.toString());
	ucsi = new Integer((btn_ucs_vacation.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_VACATION, ucsi.toString());
	ucsi = new Integer((btn_ucs_today.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_TODAY, ucsi.toString());
	ucsi = new Integer((btn_ucs_weekend.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_WEEKEND, ucsi.toString());
	ucsi = new Integer((btn_ucs_weekday.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_WEEKDAY, ucsi.toString());
	ucsi = new Integer((btn_ucs_stripe.getColorProperty()).getRGB());
	Prefs.putPref(PrefName.UCS_STRIPE, ucsi.toString());
	StripedTable.setStripeColor(new Color(ucsi.intValue()));

	if (!logobox.isSelected()) {
	    Prefs.putPref(PrefName.LOGO, "");
	    logofile.setText("");
	} else {
	    Prefs.putPref(PrefName.LOGO, logofile.getText());
	}

	if (emailbox.isSelected()) {
	    Prefs.putPref(PrefName.EMAILSERVER, smtptext.getText());
	    Prefs.putPref(PrefName.EMAILADDR, emailtext.getText());
	    Prefs.putPref(PrefName.EMAILUSER, usertext.getText());
	    Prefs.putPref(PrefName.EMAILPASS, new String(smpw.getPassword()));

	}

	Locale locs[] = Locale.getAvailableLocales();
	String choice = (String) localebox.getSelectedItem();
	for (int ii = 0; ii < locs.length; ii++) {
	    if (choice.equals(locs[ii].getDisplayName())) {
		Prefs.putPref(PrefName.COUNTRY, locs[ii].getCountry());
		Prefs.putPref(PrefName.LANGUAGE, locs[ii].getLanguage());
	    }
	}

	String newlnf = (String) lnfBox.getSelectedItem();
	String oldlnf = Prefs.getPref(PrefName.LNF);
	if (!newlnf.equals(oldlnf)) {
	    try {
		UIManager.getLookAndFeelDefaults().put("ClassLoader",
			getClass().getClassLoader());
		UIManager.setLookAndFeel(newlnf);
		// don't try to change the main window l&f - is
		// doesn't work
		// 100%
		// SwingUtilities.updateComponentTreeUI(cg_);
		Prefs.putPref(PrefName.LNF, newlnf);
		// reset the option window's size since a change
		// of LNF
		// may cause the LNF combo box to be hidden
		Prefs.putPref(PrefName.OPTVIEWSIZE, new ViewSize().toString());
	    } catch (Exception e) {
		// Errmsg.notice( "Could not find look and feel:
		// " + newlnf );
		Errmsg.notice(e.toString());
		return;
	    }
	}

	Date d = (Date) emailtimebox.getValue();
	Calendar cal = new GregorianCalendar();
	cal.setTime(d);
	int hour = cal.get(Calendar.HOUR_OF_DAY);
	int min = cal.get(Calendar.MINUTE);
	Prefs.putPref(PrefName.EMAILTIME, new Integer(hour * 60 + min));

	String ls = (String) lsbox.getSelectedItem();
	Prefs.putPref(PrefName.LINESPACING, ls);

	remTimePanel.setTimes();

	Prefs.notifyListeners();

    }

    private void chgdbActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_chgdbActionPerformed
    {// GEN-HEADEREND:event_chgdbActionPerformed
	int ret = JOptionPane.showConfirmDialog(null, Resource
		.getResourceString("Really_change_the_database?"), Resource
		.getResourceString("Confirm_DB_Change"),
		JOptionPane.YES_NO_OPTION);
	if (ret == JOptionPane.YES_OPTION) {
	    String dbdir = dbDirText.getText();
	    Prefs.putPref(PrefName.DBDIR, dbdir);
	    String hh = hsqldbdir.getText();
	    Prefs.putPref(PrefName.HSQLDBDIR, hh);

	    if (MySQLButton.isSelected()) {
		Prefs.putPref(PrefName.DBTYPE, "mysql");
	    } else if (remoteButton.isSelected()) {
		Prefs.putPref(PrefName.DBTYPE, "remote");
	    } else if (hsqldbButton.isSelected()) {
		Prefs.putPref(PrefName.DBTYPE, "hsqldb");
	    } else if (jdbcButton.isSelected()) {
		Prefs.putPref(PrefName.DBTYPE, "jdbc");
	    } else {
		Prefs.putPref(PrefName.DBTYPE, "local");
	    }

	    Prefs.putPref(PrefName.DBNAME, dbNameText.getText());
	    Prefs.putPref(PrefName.DBPORT, dbPortText.getText());
	    Prefs.putPref(PrefName.DBHOST, dbHostText.getText());
	    Prefs.putPref(PrefName.DBUSER, dbUserText.getText());
	    Prefs.putPref(PrefName.DBPASS, new String(jPasswordField1
		    .getPassword()));
	    Prefs.putPref(PrefName.DBURL, remoteURLText.getText());
	    Prefs.putPref(PrefName.JDBCURL, jdbcText.getText());

	    if (rl_ != null) {
		rl_.restart();
	    }

	    this.dispose();
	}
    }// GEN-LAST:event_chgdbActionPerformed

    private void dbTypeAction(java.awt.event.ActionEvent evt)// GEN-FIRST:event_dbTypeAction
    {// GEN-HEADEREND:event_dbTypeAction
	dbTypeChange(evt.getActionCommand());
    }// GEN-LAST:event_dbTypeAction

    private void dbTypeChange(String type) {
	if (type.equals("mysql")) {
	    mysqlPanel.setVisible(true);
	    localFilePanel.setVisible(false);
	    remoteServerPanel.setVisible(false);
	    hsqldbPanel.setVisible(false);
	    jdbcPanel.setVisible(false);
	} else if (type.equals("remote")) {
	    mysqlPanel.setVisible(false);
	    localFilePanel.setVisible(false);
	    remoteServerPanel.setVisible(true);
	    hsqldbPanel.setVisible(false);
	    jdbcPanel.setVisible(false);
	} else if (type.equals("hsqldb")) {
	    mysqlPanel.setVisible(false);
	    localFilePanel.setVisible(false);
	    remoteServerPanel.setVisible(false);
	    hsqldbPanel.setVisible(true);
	    jdbcPanel.setVisible(false);
	} else if (type.equals("jdbc")) {
	    mysqlPanel.setVisible(false);
	    localFilePanel.setVisible(false);
	    remoteServerPanel.setVisible(false);
	    hsqldbPanel.setVisible(false);
	    jdbcPanel.setVisible(true);
	} else {
	    localFilePanel.setVisible(true);
	    mysqlPanel.setVisible(false);
	    remoteServerPanel.setVisible(false);
	    hsqldbPanel.setVisible(false);
	    jdbcPanel.setVisible(false);
	}
    }

    private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_exitForm
	this.dispose();
    }// GEN-LAST:event_exitForm

    private void fontActionPerformed(java.awt.event.ActionEvent evt,
	    PrefName fontname) {// GEN-FIRST:event_incfontActionPerformed

	Font pf = Font.decode(Prefs.getPref(fontname));
	Font f = NwFontChooserS.showDialog(null, null, pf);
	if (f == null) {
	    return;
	}
	String s = NwFontChooserS.fontString(f);

	Prefs.putPref(fontname, s);
	if (fontname == PrefName.DEFFONT) {
	    NwFontChooserS.setDefaultFont(f);
	    SwingUtilities.updateComponentTreeUI(this);
	}

	Prefs.notifyListeners();

    }

    private JPanel getAppearancePanel() {
	JPanel appearancePanel = new JPanel();
	appearancePanel.setLayout(new java.awt.GridBagLayout());

	appearancePanel.setName(Resource.getResourceString("appearance"));
	ResourceHelper.setText(privbox, "Show_Private_Appointments");
	GridBagConstraints gridBagConstraints0 = new java.awt.GridBagConstraints();
	gridBagConstraints0.gridx = 1;
	gridBagConstraints0.gridy = 1;
	gridBagConstraints0.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints0.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints0.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(privbox, gridBagConstraints0);

	ResourceHelper.setText(pubbox, "Show_Public_Appointments");
	GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(pubbox, gridBagConstraints1);

	ResourceHelper.setText(jLabel4, "Look_and_Feel:");
	jLabel4.setLabelFor(lnfBox);
	GridBagConstraints gridBagConstraints4 = new java.awt.GridBagConstraints();
	gridBagConstraints4.gridx = 0;
	gridBagConstraints4.gridy = 0;
	gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints4.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(jLabel4, gridBagConstraints4);

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
	appearancePanel.add(lnfBox, gridBagConstraints5);

	ResourceHelper.setText(holiday1, "Show_U.S._Holidays");
	GridBagConstraints gridBagConstraints6 = new java.awt.GridBagConstraints();
	gridBagConstraints6.gridx = 0;
	gridBagConstraints6.gridy = 3;
	gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints6.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(holiday1, gridBagConstraints6);

	ResourceHelper.setText(mondaycb, "Week_Starts_with_Monday");
	GridBagConstraints gridBagConstraints7 = new java.awt.GridBagConstraints();
	gridBagConstraints7.gridx = 1;
	gridBagConstraints7.gridy = 4;
	gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints7.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(mondaycb, gridBagConstraints7);

	ResourceHelper.setText(miltime, "Use_24_hour_time_format");
	GridBagConstraints gridBagConstraints8 = new java.awt.GridBagConstraints();
	gridBagConstraints8.gridx = 0;
	gridBagConstraints8.gridy = 4;
	gridBagConstraints8.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints8.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(miltime, gridBagConstraints8);

	ResourceHelper.setText(jLabel5, "Week_View_Start_Hour:_");
	jLabel5.setLabelFor(wkstarthr);
	GridBagConstraints gridBagConstraints9 = new java.awt.GridBagConstraints();
	gridBagConstraints9.gridx = 0;
	gridBagConstraints9.gridy = 6;
	gridBagConstraints9.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints9.insets = new java.awt.Insets(4, 4, 4, 4);
	wkstarthr.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
		"4", "5", "6", "7", "8", "9", "10", "11" }));
	appearancePanel.add(jLabel5, gridBagConstraints9);

	GridBagConstraints gridBagConstraints10 = new java.awt.GridBagConstraints();
	gridBagConstraints10.gridx = 1;
	gridBagConstraints10.gridy = 6;
	gridBagConstraints10.fill = java.awt.GridBagConstraints.VERTICAL;
	gridBagConstraints10.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints10.insets = new java.awt.Insets(4, 4, 4, 4);
	wkendhr.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
		"12", "13", "14", "15", "16", "17", "18", "19", "20", "21",
		"22", "23" }));
	appearancePanel.add(wkstarthr, gridBagConstraints10);

	GridBagConstraints gridBagConstraints11 = new java.awt.GridBagConstraints();
	gridBagConstraints11.gridx = 1;
	gridBagConstraints11.gridy = 7;
	gridBagConstraints11.fill = java.awt.GridBagConstraints.VERTICAL;
	gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints11.insets = new java.awt.Insets(4, 4, 4, 4);
	ResourceHelper.setText(jLabel6, "Week_View_End_Hour:_");
	jLabel6.setLabelFor(wkendhr);
	appearancePanel.add(wkendhr, gridBagConstraints11);

	GridBagConstraints gridBagConstraints12 = new java.awt.GridBagConstraints();
	gridBagConstraints12.gridx = 0;
	gridBagConstraints12.gridy = 7;
	gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints12.insets = new java.awt.Insets(4, 4, 4, 4);
	ResourceHelper.setText(wrapbox, "Wrap_Appointment_Text");
	appearancePanel.add(jLabel6, gridBagConstraints12);

	GridBagConstraints gridBagConstraints13 = new java.awt.GridBagConstraints();
	gridBagConstraints13.gridx = 0;
	gridBagConstraints13.gridy = 2;
	gridBagConstraints13.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints13.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(wrapbox, gridBagConstraints13);

	ResourceHelper.setText(canadabox, "Show_Canadian_Holidays");
	GridBagConstraints gridBagConstraints14 = new java.awt.GridBagConstraints();
	gridBagConstraints14.gridx = 1;
	gridBagConstraints14.gridy = 3;
	gridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints14.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(canadabox, gridBagConstraints14);

	ResourceHelper.setText(jLabel8, "locale");
	jLabel8.setLabelFor(localebox);
	GridBagConstraints gridBagConstraints15 = new java.awt.GridBagConstraints();
	gridBagConstraints15.gridx = 0;
	gridBagConstraints15.gridy = 11;
	gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints15.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(jLabel8, gridBagConstraints15);

	GridBagConstraints gridBagConstraints16 = new java.awt.GridBagConstraints();
	gridBagConstraints16.gridx = 1;
	gridBagConstraints16.gridy = 11;
	gridBagConstraints16.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints16.insets = new java.awt.Insets(4, 4, 4, 4);

	appearancePanel.add(localebox, gridBagConstraints16);

	GridBagConstraints gridBagConstraints97 = new java.awt.GridBagConstraints();
	gridBagConstraints97.gridx = 1;
	gridBagConstraints97.gridy = 8;
	gridBagConstraints97.fill = java.awt.GridBagConstraints.VERTICAL;
	gridBagConstraints97.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints97.weightx = 1.0;
	gridBagConstraints97.insets = new java.awt.Insets(4, 4, 4, 4);
	ResourceHelper.setText(iso8601Box, "ISO_week_number");
	appearancePanel.add(iso8601Box, gridBagConstraints97);

	GridBagConstraints gridBagConstraints18 = new java.awt.GridBagConstraints();
	gridBagConstraints18.gridx = 0;
	gridBagConstraints18.gridy = 5;
	gridBagConstraints18.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints18.insets = new java.awt.Insets(4, 4, 4, 4);
	ResourceHelper.setText(colorsortbox, "colorsort");
	appearancePanel.add(colorsortbox, gridBagConstraints18);

	GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
	gridBagConstraints110.gridx = 1;
	gridBagConstraints110.gridy = 5;
	gridBagConstraints110.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints110.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(getDoyBox(), gridBagConstraints110);

	lslabel = new JLabel();
	ResourceHelper.setText(lslabel, "line_spacing");
	lslabel.setLabelFor(getLsbox());
	GridBagConstraints gridBagConstraints114 = new GridBagConstraints();
	gridBagConstraints114.gridx = 0;
	gridBagConstraints114.gridy = 10;
	gridBagConstraints114.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints114.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(lslabel, gridBagConstraints114);

	GridBagConstraints gridBagConstraints115 = new GridBagConstraints();
	gridBagConstraints115.gridx = 1;
	gridBagConstraints115.gridy = 2;
	gridBagConstraints115.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints115.fill = java.awt.GridBagConstraints.BOTH;
	appearancePanel.add(getTruncbox(), gridBagConstraints115);

	GridBagConstraints gridBagConstraints44 = new GridBagConstraints();
	gridBagConstraints44.gridx = 1;
	gridBagConstraints44.gridy = 10;
	gridBagConstraints44.weightx = 1.0;
	gridBagConstraints44.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints44.insets = new java.awt.Insets(4, 4, 4, 4);
	appearancePanel.add(getLsbox(), gridBagConstraints44);

	return appearancePanel;
    }

    private JPanel getApplyDismissPanel() {
	if (applyDismissPanel == null) {
	    applyDismissPanel = new JPanel();

	    applyButton.setIcon(new javax.swing.ImageIcon(getClass()
		    .getResource("/resource/Save16.gif")));
	    ResourceHelper.setText(applyButton, "apply");
	    applyButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    apply(evt);
		}
	    });
	    applyDismissPanel.add(applyButton, null);

	    dismissButton.setIcon(new javax.swing.ImageIcon(getClass()
		    .getResource("/resource/Stop16.gif")));
	    ResourceHelper.setText(dismissButton, "Dismiss");
	    dismissButton
		    .addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(
				java.awt.event.ActionEvent evt) {
			    jButton2ActionPerformed(evt);
			}
		    });
	    setDismissButton(dismissButton);
	    applyDismissPanel.add(dismissButton, null);
	}
	return applyDismissPanel;
    }

    private JPanel getDBPanel() {

	JPanel dbPanel = new JPanel();

	dbPanel = new JPanel();
	dbPanel.setLayout(new GridBagLayout());

	GridBagConstraints gridBagConstraints2 = new java.awt.GridBagConstraints();
	gridBagConstraints2.gridx = 0;
	gridBagConstraints2.gridy = 1;
	gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints2.weightx = 1.0;
	gridBagConstraints2.weighty = 1.0;
	dbPanel.add(getMysqlPanel(), gridBagConstraints2);

	GridBagConstraints gridBagConstraints3 = new java.awt.GridBagConstraints();
	gridBagConstraints3.gridx = 0;
	gridBagConstraints3.gridy = 2;
	gridBagConstraints3.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints3.weightx = 1.0;
	gridBagConstraints3.weighty = 1.0;
	dbPanel.add(getLocalFilePanel(), gridBagConstraints3);

	GridBagConstraints gridBagConstraints4 = new java.awt.GridBagConstraints();
	gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints4.gridy = 0;
	gridBagConstraints4.gridx = 0;
	dbPanel.add(getDbTypePanel(), gridBagConstraints4);

	GridBagConstraints gridBagConstraints5 = new java.awt.GridBagConstraints();
	gridBagConstraints5.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints5.gridx = 0; // Generated
	gridBagConstraints5.gridy = 6;
	dbPanel.add(chgdb, gridBagConstraints5); // Generated
	chgdb.setForeground(new java.awt.Color(255, 0, 51));
	chgdb.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Refresh16.gif")));
	ResourceHelper.setText(chgdb, "Apply_DB_Change");
	chgdb.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		chgdbActionPerformed(evt);
	    }
	});

	GridBagConstraints gridBagConstraints6 = new java.awt.GridBagConstraints();
	gridBagConstraints6.gridx = 0;
	gridBagConstraints6.gridy = 3;
	gridBagConstraints6.weightx = 1.0;
	gridBagConstraints6.weighty = 1.0;
	gridBagConstraints6.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
	dbPanel.add(getRemoteServerPanel(), gridBagConstraints6); // Generated

	GridBagConstraints gridBagConstraints6h = new java.awt.GridBagConstraints();
	gridBagConstraints6h.gridx = 0;
	gridBagConstraints6h.gridy = 4;
	gridBagConstraints6h.weightx = 1.0;
	gridBagConstraints6h.weighty = 1.0;
	gridBagConstraints6h.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints6h.fill = java.awt.GridBagConstraints.HORIZONTAL;
	dbPanel.add(getHSQLDBPanel(), gridBagConstraints6h); // Generated

	GridBagConstraints gridBagConstraints7h = new java.awt.GridBagConstraints();
	gridBagConstraints7h.gridx = 0;
	gridBagConstraints7h.gridy = 5;
	gridBagConstraints7h.weightx = 1.0;
	gridBagConstraints7h.weighty = 1.0;
	gridBagConstraints7h.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints7h.fill = java.awt.GridBagConstraints.HORIZONTAL;
	dbPanel.add(getJdbcPanel(), gridBagConstraints7h); // Generated

	return dbPanel;
    }

    private JPanel getDbTypePanel() {
	if (dbTypePanel == null) {
	    FlowLayout flowLayout = new FlowLayout();
	    flowLayout.setAlignment(java.awt.FlowLayout.LEFT); // Generated
	    flowLayout.setHgap(40); // Generated
	    dbTypePanel = new JPanel();
	    dbTypePanel.setLayout(flowLayout); // Generated
	    dbTypePanel.add(getHSQLDBFileButton(), null);
	    dbTypePanel.add(getMySQLButton(), null); // Generated
	    dbTypePanel.add(getRemoteButton(), null); // Generated
	    dbTypePanel.add(getLocalFileButton(), null); // Generated
	    dbTypePanel.add(getJdbcButton(), null); // Generated

	}
	return dbTypePanel;
    }

    private JCheckBox getDoyBox() {
	if (doyBox == null) {
	    doyBox = new JCheckBox();
	    ResourceHelper.setText(doyBox, "showdoy");
	}
	return doyBox;
    }

    private JPanel getEmailPanel() {
	JPanel emailPanel = new JPanel();
	emailPanel.setLayout(new java.awt.GridBagLayout());

	ResourceHelper.setText(jLabel1, "SMTP_Server");
	GridBagConstraints gridBagConstraints35 = new java.awt.GridBagConstraints();
	gridBagConstraints35.gridx = 0;
	gridBagConstraints35.gridy = 1;
	gridBagConstraints35.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints35.insets = new java.awt.Insets(0, 4, 0, 0);
	emailPanel.add(jLabel1, gridBagConstraints35);
	jLabel1.setLabelFor(smtptext);

	JLabel userlabel = new JLabel();
	ResourceHelper.setText(userlabel, "SMTP_user");
	GridBagConstraints gridBagConstraintsUL = new java.awt.GridBagConstraints();
	gridBagConstraintsUL.gridx = 0;
	gridBagConstraintsUL.gridy = 2;
	gridBagConstraintsUL.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraintsUL.insets = new java.awt.Insets(0, 4, 0, 0);
	emailPanel.add(userlabel, gridBagConstraintsUL);
	userlabel.setLabelFor(usertext);

	GridBagConstraints gridBagConstraintsTF = new java.awt.GridBagConstraints();
	gridBagConstraintsTF.gridx = 1;
	gridBagConstraintsTF.gridy = 2;
	gridBagConstraintsTF.fill = java.awt.GridBagConstraints.BOTH;
	// gridBagConstraintsTF.insets = new java.awt.Insets(0, 4, 0,
	// 0);
	emailPanel.add(usertext, gridBagConstraintsTF);

	JLabel passlabel = new JLabel();
	ResourceHelper.setText(passlabel, "SMTP_password");
	GridBagConstraints gridBagConstraintsPWL = new java.awt.GridBagConstraints();
	gridBagConstraintsPWL.gridx = 0;
	gridBagConstraintsPWL.gridy = 3;
	gridBagConstraintsPWL.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraintsPWL.insets = new java.awt.Insets(0, 4, 0, 0);
	emailPanel.add(passlabel, gridBagConstraintsPWL);
	passlabel.setLabelFor(smpw);

	GridBagConstraints gridBagConstraintsPW = new java.awt.GridBagConstraints();
	gridBagConstraintsPW.gridx = 1;
	gridBagConstraintsPW.gridy = 3;
	gridBagConstraintsPW.fill = java.awt.GridBagConstraints.BOTH;
	// gridBagConstraintsTF.insets = new java.awt.Insets(0, 4, 0,
	// 0);
	emailPanel.add(smpw, gridBagConstraintsPW);

	ResourceHelper.setText(jLabel2, "Your_Email_Address");
	GridBagConstraints gridBagConstraints36 = new java.awt.GridBagConstraints();
	gridBagConstraints36.gridx = 0;
	gridBagConstraints36.gridy = 4;
	gridBagConstraints36.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints36.insets = new java.awt.Insets(0, 4, 0, 4);
	emailPanel.add(jLabel2, gridBagConstraints36);
	jLabel2.setLabelFor(emailtext);

	smtptext.setColumns(30);
	GridBagConstraints gridBagConstraints37 = new java.awt.GridBagConstraints();
	gridBagConstraints37.gridx = 1;
	gridBagConstraints37.gridy = 1;
	gridBagConstraints37.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints37.weightx = 1.0;
	emailPanel.add(smtptext, gridBagConstraints37);

	emailtext.setColumns(30);
	GridBagConstraints gridBagConstraints38 = new java.awt.GridBagConstraints();
	gridBagConstraints38.gridx = 1;
	gridBagConstraints38.gridy = 4;
	gridBagConstraints38.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints38.weightx = 1.0;
	emailPanel.add(emailtext, gridBagConstraints38);

	ResourceHelper.setText(emailbox, "Enable_Email");
	GridBagConstraints gridBagConstraints39 = new java.awt.GridBagConstraints();
	gridBagConstraints39.gridx = 0;
	gridBagConstraints39.gridy = 0;
	gridBagConstraints39.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints39.anchor = java.awt.GridBagConstraints.WEST;
	emailPanel.add(emailbox, gridBagConstraints39);

	GridBagConstraints gridBagConstraints116 = new GridBagConstraints();

	gridBagConstraints116.gridx = 0;
	gridBagConstraints116.gridy = 5;
	gridBagConstraints116.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints116.insets = new java.awt.Insets(4, 4, 4, 4);
	ResourceHelper.setText(remtimelabel, "reminder_time");
	remtimelabel.setLabelFor(emailtimebox);
	emailPanel.add(remtimelabel, gridBagConstraints116);

	GridBagConstraints gridBagConstraints212 = new GridBagConstraints();
	gridBagConstraints212.gridx = 1;
	gridBagConstraints212.gridy = 5;
	gridBagConstraints212.weightx = 1.0;
	gridBagConstraints212.fill = java.awt.GridBagConstraints.VERTICAL;
	gridBagConstraints212.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints212.anchor = java.awt.GridBagConstraints.WEST;
	emailPanel.add(getEmailtimebox(), gridBagConstraints212);

	ResourceHelper.setText(indEmailBox, "Email_Ind");
	GridBagConstraints gridBagConstraintse1 = new java.awt.GridBagConstraints();
	gridBagConstraintse1.gridx = 0;
	gridBagConstraintse1.gridy = 6;
	gridBagConstraintse1.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraintse1.anchor = java.awt.GridBagConstraints.WEST;
	emailPanel.add(indEmailBox, gridBagConstraintse1);

	JLabel itimel = new JLabel();
	GridBagConstraints gridBagConstraintse3 = new GridBagConstraints();
	gridBagConstraintse3.gridx = 0;
	gridBagConstraintse3.gridy = 7;
	gridBagConstraintse3.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraintse3.insets = new java.awt.Insets(4, 4, 4, 4);
	ResourceHelper.setText(itimel, "Email_Ind_Min");
	itimel.setLabelFor(getIndEmailtimebox());
	emailPanel.add(itimel, gridBagConstraintse3);

	GridBagConstraints gridBagConstraintse2 = new GridBagConstraints();
	gridBagConstraintse2.gridx = 1;
	gridBagConstraintse2.gridy = 7;
	gridBagConstraintse2.weightx = 1.0;
	gridBagConstraintse2.fill = java.awt.GridBagConstraints.VERTICAL;
	gridBagConstraintse2.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraintse2.anchor = java.awt.GridBagConstraints.WEST;
	emailPanel.add(getIndEmailtimebox(), gridBagConstraintse2);

	return emailPanel;
    }

    private JSpinner getEmailtimebox() {
	if (emailtimebox == null) {
	    emailtimebox = new JSpinner(new SpinnerDateModel());
	    JSpinner.DateEditor de = new JSpinner.DateEditor(emailtimebox,
		    "HH:mm");
	    emailtimebox.setEditor(de);
	    // emailtimebox.setValue(new Date());

	}
	return emailtimebox;
    }

    private JCheckBox getExputcbox() {
	if (exputcbox == null) {
	    exputcbox = new JCheckBox();
	    ResourceHelper.setText(exputcbox, "exputc");
	}
	return exputcbox;
    }

    private JCheckBox getExtraDayBox() {
	if (extraDayBox == null) {
	    extraDayBox = new JCheckBox();
	    ResourceHelper.setText(extraDayBox, "show_extra");
	}
	return extraDayBox;
    }

    private JPanel getFontPanel() {
	JPanel fontPanel = new JPanel();
	fontPanel.setLayout(new FlowLayout());

	ResourceHelper.setText(previewFontButton, "set_pre_font");
	previewFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
		javax.swing.border.BevelBorder.RAISED));
	// previewFontButton.setFont(Font.decode(Prefs.getPref(PrefName.PREVIEWFONT)));
	previewFontButton
		.addActionListener(new java.awt.event.ActionListener() {
		    public void actionPerformed(java.awt.event.ActionEvent evt) {
			fontActionPerformed(evt, PrefName.PREVIEWFONT);
			// previewFontButton.setFont(Font.decode(Prefs.getPref(PrefName.PREVIEWFONT)));
		    }
		});
	fontPanel.add(previewFontButton);

	ResourceHelper.setText(apptFontButton, "set_appt_font");
	apptFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
		javax.swing.border.BevelBorder.RAISED));
	// apptFontButton.setFont(Font.decode(Prefs.getPref(PrefName.APPTFONT)));
	apptFontButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		fontActionPerformed(evt, PrefName.APPTFONT);
		// apptFontButton.setFont(Font.decode(Prefs.getPref(PrefName.APPTFONT)));
	    }
	});
	fontPanel.add(apptFontButton);

	ResourceHelper.setText(defFontButton, "set_def_font");
	defFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
		javax.swing.border.BevelBorder.RAISED));
	// if( !Prefs.getPref(PrefName.DEFFONT).equals(""))
	// defFontButton.setFont(Font.decode(Prefs.getPref(PrefName.DEFFONT)));
	defFontButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		fontActionPerformed(evt, PrefName.DEFFONT);
		// defFontButton.setFont(Font.decode(Prefs.getPref(PrefName.DEFFONT)));
	    }
	});
	fontPanel.add(defFontButton);

	ResourceHelper.setText(dayFontButton, "dview_font");
	dayFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
		javax.swing.border.BevelBorder.RAISED));
	// dayFontButton.setFont(Font.decode(Prefs.getPref(PrefName.DAYVIEWFONT)));
	dayFontButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		fontActionPerformed(evt, PrefName.DAYVIEWFONT);
		// dayFontButton.setFont(Font.decode(Prefs.getPref(PrefName.DAYVIEWFONT)));
	    }
	});
	fontPanel.add(dayFontButton);

	ResourceHelper.setText(weekFontButton, "wview_font");
	weekFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
		javax.swing.border.BevelBorder.RAISED));
	// weekFontButton.setFont(Font.decode(Prefs.getPref(PrefName.WEEKVIEWFONT)));
	weekFontButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		fontActionPerformed(evt, PrefName.WEEKVIEWFONT);
		// weekFontButton.setFont(Font.decode(Prefs.getPref(PrefName.WEEKVIEWFONT)));
	    }
	});
	fontPanel.add(weekFontButton);

	ResourceHelper.setText(monthFontButton, "mview_font");
	monthFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
		javax.swing.border.BevelBorder.RAISED));
	// monthFontButton.setFont(Font.decode(Prefs.getPref(PrefName.MONTHVIEWFONT)));
	monthFontButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		fontActionPerformed(evt, PrefName.MONTHVIEWFONT);
		// monthFontButton.setFont(Font.decode(Prefs.getPref(PrefName.MONTHVIEWFONT)));
	    }
	});
	fontPanel.add(monthFontButton);

	return fontPanel;
    }

    private JRadioButton getHSQLDBFileButton() {
	if (hsqldbButton == null) {
	    hsqldbButton = new JRadioButton();
	    hsqldbButton.setActionCommand("hsqldb");
	    ResourceHelper.setText(hsqldbButton, "hsqldb");
	    hsqldbButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    dbTypeAction(e);
		}
	    });
	}
	return hsqldbButton;
    }

    private JPanel getHSQLDBPanel() {
	hsqldbPanel = new JPanel();
	hsqldbPanel.setLayout(new java.awt.GridBagLayout());

	JLabel hs1 = new JLabel();
	hsqldbPanel.setBorder(new javax.swing.border.TitledBorder(Resource
		.getResourceString("hsqldbinfo")));
	ResourceHelper.setText(hs1, "DataBase_Directory");
	hs1.setLabelFor(dbDirText);
	GridBagConstraints gridBagConstraints30 = new java.awt.GridBagConstraints();
	gridBagConstraints30.gridx = 0;
	gridBagConstraints30.gridy = 0;
	gridBagConstraints30.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints30.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints30.insets = new java.awt.Insets(0, 8, 0, 0);
	hsqldbPanel.add(hs1, gridBagConstraints30);

	GridBagConstraints gridBagConstraints31 = new java.awt.GridBagConstraints();
	gridBagConstraints31.gridx = 0;
	gridBagConstraints31.gridy = 1;
	gridBagConstraints31.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints31.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints31.weightx = 0.5;
	gridBagConstraints31.insets = new java.awt.Insets(4, 8, 4, 8);
	hsqldbPanel.add(hsqldbdir, gridBagConstraints31);

	JButton hsb1 = new JButton();
	ResourceHelper.setText(hsb1, "Browse");
	hsb1.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		hsqldbActionPerformed(evt);
	    }
	});

	GridBagConstraints gridBagConstraints32 = new java.awt.GridBagConstraints();
	gridBagConstraints32.gridx = 1;
	gridBagConstraints32.gridy = 1;
	gridBagConstraints32.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints32.insets = new java.awt.Insets(4, 4, 4, 4);
	hsqldbPanel.add(hsb1, gridBagConstraints32);

	return hsqldbPanel;
    }

    private JSpinner getIndEmailtimebox() {
	if (indEmailTime == null) {
	    indEmailTime = new JSpinner(new SpinnerNumberModel());
	}
	return indEmailTime;
    }

    private JRadioButton getJdbcButton() {
	if (jdbcButton == null) {
	    jdbcButton = new JRadioButton();
	    jdbcButton.setActionCommand("jdbc");
	    ResourceHelper.setText(jdbcButton, "jdbc");
	    jdbcButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    dbTypeAction(e);
		}
	    });
	}
	return jdbcButton;
    }

    private JPanel getJdbcPanel() {
	if (jdbcPanel == null) {
	    GridBagConstraints gridBagConstraints54 = new GridBagConstraints();
	    gridBagConstraints54.fill = java.awt.GridBagConstraints.HORIZONTAL; // Generated
	    gridBagConstraints54.gridy = 1; // Generated
	    gridBagConstraints54.ipadx = 0; // Generated
	    gridBagConstraints54.weightx = 1.0; // Generated
	    gridBagConstraints54.insets = new java.awt.Insets(4, 4, 4, 4); // Generated
	    gridBagConstraints54.gridx = 0; // Generated
	    GridBagConstraints gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0; // Generated
	    gridBagConstraints.ipadx = 0; // Generated
	    gridBagConstraints.ipady = 5; // Generated
	    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4); // Generated
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST; // Generated
	    gridBagConstraints.gridy = 0; // Generated

	    JLabel jLabel = new JLabel();
	    ResourceHelper.setText(jLabel, "enturl");
	    jLabel.setLabelFor(getJdbcText());
	    jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT); // Generated
	    jLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT); // Generated
	    jdbcPanel = new JPanel();
	    jdbcPanel.setLayout(new GridBagLayout()); // Generated
	    jdbcPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
		    null, Resource.getResourceString("jdbc"),
		    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
		    javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
		    null)); // Generated
	    jdbcPanel.add(jLabel, gridBagConstraints); // Generated
	    jdbcPanel.add(getJdbcText(), gridBagConstraints54); // Generated
	}
	return jdbcPanel;
    }

    private JTextField getJdbcText() {
	if (jdbcText == null) {
	    jdbcText = new JTextField();
	}
	return jdbcText;
    }

    private JPanel getJPanelUCS() {
	if (jPanelUCS == null) {
	    jPanelUCS = new JPanel();
	    jPanelUCS.setLayout(new GridLayout(10, 2));

	    cb_ucs_on = new javax.swing.JCheckBox();
	    ResourceHelper.setText(cb_ucs_on, "ucolortext0");
	    cb_ucs_ontodo = new javax.swing.JCheckBox();
	    ResourceHelper.setText(cb_ucs_ontodo, "ucolortext1");
	    cb_ucs_marktodo = new javax.swing.JCheckBox();
	    ResourceHelper.setText(cb_ucs_marktodo, "ucolortext2");
	    tf_ucs_marker = new JTextField("! "); //$NON-NLS-1$
	    btn_ucs_red = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext4"), Color.WHITE, false); //$NON-NLS-1$
	    btn_ucs_blue = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext5"), Color.WHITE, false); //$NON-NLS-1$
	    btn_ucs_green = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext6"), Color.WHITE, false); //$NON-NLS-1$
	    btn_ucs_black = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext7"), Color.WHITE, false); //$NON-NLS-1$
	    btn_ucs_white = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext8"), Color.WHITE, false); //$NON-NLS-1$
	    btn_ucs_tasks = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext9"), Color.WHITE, false); //$NON-NLS-1$
	    btn_ucs_holidays = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext10"), Color.WHITE, //$NON-NLS-1$
		    false);
	    btn_ucs_birthdays = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext11"), Color.WHITE, //$NON-NLS-1$
		    false);
	    btn_ucs_default = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext12"), Color.WHITE, true); //$NON-NLS-1$
	    btn_ucs_holiday = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext13"), Color.WHITE, true); //$NON-NLS-1$
	    btn_ucs_halfday = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext14"), Color.WHITE, true); //$NON-NLS-1$
	    btn_ucs_vacation = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext15"), Color.WHITE, //$NON-NLS-1$
		    true);
	    btn_ucs_today = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext16"), Color.WHITE, true); //$NON-NLS-1$
	    btn_ucs_weekend = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext17"), Color.WHITE, true); //$NON-NLS-1$
	    btn_ucs_weekday = new JButtonKnowsBgColor(Resource
		    .getResourceString("ucolortext18"), Color.WHITE, true); //$NON-NLS-1$
	    btn_ucs_stripe = new JButtonKnowsBgColor(Resource
		    .getResourceString("stripecolor"), Color.WHITE, true);

	    btn_ucs_restore = new JButton(Resource
		    .getResourceString("restore_defaults")); //$NON-NLS-1$

	    btn_ucs_restore.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    btn_ucs_red.setColorProperty(new Color(13369395));
		    btn_ucs_red.setColorByProperty();
		    btn_ucs_blue.setColorProperty(new Color(6684876));
		    btn_ucs_blue.setColorByProperty();
		    btn_ucs_green.setColorProperty(new Color(39168));
		    btn_ucs_green.setColorByProperty();
		    btn_ucs_black.setColorProperty(new Color(13107));
		    btn_ucs_black.setColorByProperty();
		    btn_ucs_white.setColorProperty(new Color(16250609));
		    btn_ucs_white.setColorByProperty();
		    btn_ucs_tasks.setColorProperty(new Color(13158));
		    btn_ucs_tasks.setColorByProperty();
		    btn_ucs_holidays.setColorProperty(new Color(10027212));
		    btn_ucs_holidays.setColorByProperty();
		    btn_ucs_birthdays.setColorProperty(new Color(10027008));
		    btn_ucs_birthdays.setColorByProperty();
		    // // Calendar view day background
		    // colors

		    btn_ucs_default.setColorProperty(new Color(16777164));
		    btn_ucs_default.setColorByProperty();
		    btn_ucs_today.setColorProperty(new Color(16751001));
		    btn_ucs_today.setColorByProperty();
		    btn_ucs_holiday.setColorProperty(new Color(16764108));
		    btn_ucs_holiday.setColorByProperty();
		    btn_ucs_vacation.setColorProperty(new Color(13434828));
		    btn_ucs_vacation.setColorByProperty();
		    btn_ucs_halfday.setColorProperty(new Color(13421823));
		    btn_ucs_halfday.setColorByProperty();
		    btn_ucs_weekend.setColorProperty(new Color(16764057));
		    btn_ucs_weekend.setColorByProperty();
		    btn_ucs_weekday.setColorProperty(new Color(16777164));
		    btn_ucs_weekday.setColorByProperty();
		    btn_ucs_stripe.setColorProperty(new Color(15792890));
		    btn_ucs_stripe.setColorByProperty();
		}
	    });

	    jPanelUCS.add(cb_ucs_on);
	    jPanelUCS.add(cb_ucs_ontodo);
	    jPanelUCS.add(btn_ucs_red);
	    jPanelUCS.add(btn_ucs_default);
	    jPanelUCS.add(btn_ucs_blue);
	    jPanelUCS.add(btn_ucs_today);
	    jPanelUCS.add(btn_ucs_green);
	    jPanelUCS.add(btn_ucs_holiday);
	    jPanelUCS.add(btn_ucs_black);
	    jPanelUCS.add(btn_ucs_halfday);
	    jPanelUCS.add(btn_ucs_white);
	    jPanelUCS.add(btn_ucs_vacation);
	    jPanelUCS.add(btn_ucs_tasks);
	    jPanelUCS.add(btn_ucs_weekend);
	    jPanelUCS.add(btn_ucs_holidays);
	    jPanelUCS.add(btn_ucs_weekday);
	    jPanelUCS.add(btn_ucs_birthdays);
	    jPanelUCS.add(btn_ucs_stripe);
	    jPanelUCS.add(btn_ucs_restore);

	    JPanel njp = new JPanel();
	    njp.setLayout(new BorderLayout());
	    njp.add(cb_ucs_marktodo, BorderLayout.WEST);
	    njp.add(tf_ucs_marker, BorderLayout.CENTER);
	    getJPanelUCS().add(njp);
	}
	return jPanelUCS;
    }

    private JRadioButton getLocalFileButton() {
	if (localFileButton == null) {
	    localFileButton = new JRadioButton();
	    localFileButton.setActionCommand("local");
	    ResourceHelper.setText(localFileButton, "localFile");
	    localFileButton
		    .addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
			    dbTypeAction(e);
			}
		    });
	}
	return localFileButton;
    }

    private JPanel getLocalFilePanel() {
	localFilePanel = new JPanel();
	localFilePanel.setLayout(new java.awt.GridBagLayout());

	localFilePanel.setBorder(new javax.swing.border.TitledBorder(Resource
		.getResourceString("localFileInfo")));
	JTextArea warning = new JTextArea();
	warning.setText("**** "
		+ Resource.getPlainResourceString("mdb_deprecated") + " ****");
	GridBagConstraints gridBagConstraintsw = new java.awt.GridBagConstraints();
	warning.setEditable(false);
	warning.setWrapStyleWord(true);
	warning.setLineWrap(true);
	warning.setBackground(Color.white);
	warning.setForeground(Color.red);
	gridBagConstraintsw.gridx = 0;
	gridBagConstraintsw.gridy = 0;
	gridBagConstraintsw.weighty = 1.0;
	gridBagConstraintsw.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraintsw.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraintsw.insets = new java.awt.Insets(0, 20, 0, 0);
	localFilePanel.add(warning, gridBagConstraintsw);

	ResourceHelper.setText(jLabel3, "DataBase_Directory");
	jLabel3.setLabelFor(dbDirText);
	GridBagConstraints gridBagConstraints30 = new java.awt.GridBagConstraints();
	gridBagConstraints30.gridx = 0;
	gridBagConstraints30.gridy = 1;
	gridBagConstraints30.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints30.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints30.insets = new java.awt.Insets(0, 8, 0, 0);
	localFilePanel.add(jLabel3, gridBagConstraints30);

	GridBagConstraints gridBagConstraints31 = new java.awt.GridBagConstraints();
	gridBagConstraints31.gridx = 0;
	gridBagConstraints31.gridy = 2;
	gridBagConstraints31.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints31.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints31.weightx = 0.5;
	gridBagConstraints31.insets = new java.awt.Insets(4, 8, 4, 8);
	localFilePanel.add(dbDirText, gridBagConstraints31);

	ResourceHelper.setText(jButton5, "Browse");
	jButton5.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		jButton5ActionPerformed(evt);
	    }
	});

	GridBagConstraints gridBagConstraints32 = new java.awt.GridBagConstraints();
	gridBagConstraints32.gridx = 1;
	gridBagConstraints32.gridy = 2;
	gridBagConstraints32.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints32.insets = new java.awt.Insets(4, 4, 4, 4);
	localFilePanel.add(jButton5, gridBagConstraints32);

	return localFilePanel;
    }

    /**
         * This method initializes lsbox
         * 
         * @return javax.swing.JComboBox
         */
    private JComboBox getLsbox() {
	if (lsbox == null) {
	    lsbox = new JComboBox();
	}
	return lsbox;
    }

    private JPanel getMiscPanel() {
	JPanel miscPanel = new JPanel();

	miscPanel.setLayout(new java.awt.GridBagLayout());

	ResourceHelper.setText(autoupdate, "Auto_Update_Check");
	autoupdate
		.setToolTipText(Resource
			.getResourceString("Enable_a_daily_check_to_the_BORG_website_to_see_if_a_new_version_is_out._Does_not_update_the_product."));
	GridBagConstraints gridBagConstraints45 = new java.awt.GridBagConstraints();
	gridBagConstraints45.gridx = 0;
	gridBagConstraints45.gridy = 3;
	gridBagConstraints45.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints45.anchor = java.awt.GridBagConstraints.WEST;
	miscPanel.add(autoupdate, gridBagConstraints45);

	versioncheck.setFont(new java.awt.Font("Dialog", 0, 10));
	ResourceHelper.setText(versioncheck, "Check_for_updates_now");
	versioncheck.setToolTipText(Resource
		.getResourceString("Check_for_the_latest_BORG_version_now"));
	versioncheck.setBorder(new javax.swing.border.SoftBevelBorder(
		javax.swing.border.BevelBorder.RAISED));
	versioncheck.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		versioncheckActionPerformed(evt);
	    }
	});

	GridBagConstraints gridBagConstraints46 = new java.awt.GridBagConstraints();
	gridBagConstraints46.gridx = 1;
	gridBagConstraints46.gridy = 3;
	gridBagConstraints46.anchor = java.awt.GridBagConstraints.WEST;
	miscPanel.add(versioncheck, gridBagConstraints46);

	ResourceHelper.setText(splashbox, "splash");
	GridBagConstraints gridBagConstraints47 = new java.awt.GridBagConstraints();
	gridBagConstraints47.gridx = 0;
	gridBagConstraints47.gridy = 0;
	gridBagConstraints47.fill = java.awt.GridBagConstraints.BOTH;
	miscPanel.add(splashbox, gridBagConstraints47);

	ResourceHelper.setText(backgbox,
		"Start_in_background_(Windows_only,_TrayIcon_req)");
	backgbox
		.setToolTipText(Resource
			.getResourceString("Do_not_open_todo_and_month_view_on_startup,_start_in_systray"));
	GridBagConstraints gridBagConstraints48 = new java.awt.GridBagConstraints();
	gridBagConstraints48.gridx = 0;
	gridBagConstraints48.gridy = 1;
	gridBagConstraints48.gridwidth = 2;
	gridBagConstraints48.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints48.anchor = java.awt.GridBagConstraints.WEST;
	miscPanel.add(backgbox, gridBagConstraints48);

	ResourceHelper.setText(stackbox, "stackonerr");
	GridBagConstraints gridBagConstraints49 = new java.awt.GridBagConstraints();
	gridBagConstraints49.gridx = 0;
	gridBagConstraints49.gridy = 4;
	gridBagConstraints49.fill = java.awt.GridBagConstraints.BOTH;
	miscPanel.add(stackbox, gridBagConstraints49);

	ResourceHelper.setText(icaltodobox, "icaltodo");
	GridBagConstraints gridBagConstraints50 = new java.awt.GridBagConstraints();
	gridBagConstraints50.gridx = 0;
	gridBagConstraints50.gridy = 5;
	gridBagConstraints50.fill = java.awt.GridBagConstraints.BOTH;
	miscPanel.add(icaltodobox, gridBagConstraints50);

	ResourceHelper.setText(revDayEditbox, "revdayedit");
	GridBagConstraints gridBagConstraints51 = new java.awt.GridBagConstraints();
	gridBagConstraints51.gridx = 0;
	gridBagConstraints51.gridy = 7;
	gridBagConstraints51.fill = java.awt.GridBagConstraints.BOTH;
	miscPanel.add(revDayEditbox, gridBagConstraints51);

	GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
	gridBagConstraints111.gridx = 0;
	gridBagConstraints111.gridy = 6;
	gridBagConstraints111.fill = java.awt.GridBagConstraints.HORIZONTAL;
	miscPanel.add(getExputcbox(), gridBagConstraints111);

	GridBagConstraints gridBagConstraints310 = new GridBagConstraints();
	gridBagConstraints310.gridx = 0;
	gridBagConstraints310.gridy = 8;
	gridBagConstraints310.ipadx = 0;
	gridBagConstraints310.ipady = 0;
	gridBagConstraints310.insets = new java.awt.Insets(0, 0, 0, 0);
	gridBagConstraints310.anchor = java.awt.GridBagConstraints.WEST;
	miscPanel.add(getPalmcb(), gridBagConstraints310);

	JLabel sportlabel = new JLabel();
	ResourceHelper.setText(sportlabel, "socket_port");
	GridBagConstraints gridBagConstraints311 = new GridBagConstraints();
	gridBagConstraints311.gridx = 0;
	gridBagConstraints311.gridy = 9;
	gridBagConstraints311.anchor = java.awt.GridBagConstraints.WEST;
	miscPanel.add(sportlabel, gridBagConstraints311);

	GridBagConstraints gridBagConstraints312 = new GridBagConstraints();
	gridBagConstraints312.gridx = 1;
	gridBagConstraints312.gridy = 9;
	gridBagConstraints312.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints312.anchor = java.awt.GridBagConstraints.EAST;
	miscPanel.add(socketPort, gridBagConstraints312);

	GridBagConstraints gridBagConstraints313 = new GridBagConstraints();
	gridBagConstraints313.gridx = 0;
	gridBagConstraints313.gridy = 10;
	gridBagConstraints313.fill = java.awt.GridBagConstraints.HORIZONTAL;
	miscPanel.add(getExtraDayBox(), gridBagConstraints313);

	GridBagConstraints gridBagConstraintsUST = new GridBagConstraints();
	gridBagConstraintsUST.gridx = 0;
	gridBagConstraintsUST.gridy = 11;
	gridBagConstraintsUST.fill = java.awt.GridBagConstraints.HORIZONTAL;
	useSysTray.setText(Resource.getPlainResourceString("enable_systray"));
	miscPanel.add(useSysTray, gridBagConstraintsUST);

	return miscPanel;
    }

    private JPanel taskOptionPanel = null;  //  @jve:decl-index=0:visual-constraint="12,2528"

    private JCheckBox taskAbbrevBox = new JCheckBox();

    private JCheckBox calShowTaskBox = new JCheckBox();

    private JCheckBox calShowSubtaskBox = new JCheckBox();

    private JPanel getTaskOptionPanel() {
	if (taskOptionPanel == null) {
	    GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
	    gridBagConstraints20.insets = new Insets(4, 4, 4, 4);
	    gridBagConstraints20.gridy = 2;
	    gridBagConstraints20.ipady = 0;
	    gridBagConstraints20.fill = GridBagConstraints.NONE;
	    gridBagConstraints20.anchor = GridBagConstraints.WEST;
	    gridBagConstraints20.gridx = 0;
	    GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
	    gridBagConstraints19.insets = new Insets(4, 4, 4, 4);
	    gridBagConstraints19.gridy = 1;
	    gridBagConstraints19.ipady = 0;
	    gridBagConstraints19.fill = GridBagConstraints.NONE;
	    gridBagConstraints19.anchor = GridBagConstraints.WEST;
	    gridBagConstraints19.gridx = 0;
	    GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
	    gridBagConstraints17.gridx = 0;
	    gridBagConstraints17.ipadx = 0;
	    gridBagConstraints17.insets = new Insets(4, 4, 4, 4);
	    gridBagConstraints17.fill = GridBagConstraints.NONE;
	    gridBagConstraints17.anchor = GridBagConstraints.WEST;
	    gridBagConstraints17.gridy = 0;
	    taskOptionPanel = new JPanel();

	    taskOptionPanel.setLayout(new GridBagLayout());
	    taskOptionPanel.setSize(new Dimension(168, 159));
	    taskOptionPanel.add(taskAbbrevBox, gridBagConstraints17);
	    taskOptionPanel.add(calShowTaskBox, gridBagConstraints19);
	    taskOptionPanel.add(calShowSubtaskBox, gridBagConstraints20);
	    taskAbbrevBox.setText(Resource
		    .getPlainResourceString("task_abbrev"));
	    calShowTaskBox.setText(Resource
		    .getPlainResourceString("calShowTask"));
	    calShowSubtaskBox.setText(Resource
		    .getPlainResourceString("calShowSubtask"));
	}
	return taskOptionPanel;
    }

    private JPanel getMultiUserPanel() {
	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());

	GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.gridwidth = 2;
	gridBagConstraints1.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
	panel.add(sharedFileCheckBox, gridBagConstraints1);

	GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
	gridBagConstraints2.gridx = 1;
	gridBagConstraints2.gridy = 1;
	gridBagConstraints2.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints2.weightx = 1.0;
	gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
	panel.add(getSyncMins(), gridBagConstraints2);

	syncminlabel = new JLabel();
	ResourceHelper.setText(syncminlabel, "sync_mins");
	syncminlabel.setLabelFor(getSyncMins());
	GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
	gridBagConstraints3.insets = new java.awt.Insets(4, 4, 4, 4);
	gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints3.gridx = 0;
	gridBagConstraints3.gridy = 1;
	gridBagConstraints3.weightx = 1.0;
	panel.add(syncminlabel, gridBagConstraints3);

	return panel;
    }

    /**
         * This method initializes jRadioButton1
         * 
         * @return javax.swing.JRadioButton
         */
    private JRadioButton getMySQLButton() {
	if (MySQLButton == null) {
	    MySQLButton = new JRadioButton();
	    MySQLButton.setActionCommand("mysql");
	    MySQLButton.setText("MySQL");
	    MySQLButton.setMnemonic(KeyEvent.VK_M);
	    MySQLButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    dbTypeAction(e);
		}
	    });
	}
	return MySQLButton;
    }

    private JPanel getMysqlPanel() {
	mysqlPanel = new javax.swing.JPanel();
	mysqlPanel.setLayout(new java.awt.GridBagLayout());

	mysqlPanel.setBorder(new javax.swing.border.TitledBorder(Resource
		.getResourceString("MySQLInfo")));
	ResourceHelper.setText(jLabel7, "DatabaseName");
	jLabel7.setLabelFor(dbNameText);
	GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints1.insets = new java.awt.Insets(0, 4, 0, 4);
	mysqlPanel.add(jLabel7, gridBagConstraints1);

	GridBagConstraints gridBagConstraints2 = new java.awt.GridBagConstraints();
	gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints2.weightx = 1.0;
	mysqlPanel.add(dbNameText, gridBagConstraints2);

	ResourceHelper.setText(jLabel17, "hostname");
	jLabel17.setLabelFor(dbHostText);
	GridBagConstraints gridBagConstraints3 = new java.awt.GridBagConstraints();
	gridBagConstraints3.gridx = 0;
	gridBagConstraints3.gridy = 1;
	gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints3.insets = new java.awt.Insets(0, 4, 0, 4);
	mysqlPanel.add(jLabel17, gridBagConstraints3);

	GridBagConstraints gridBagConstraints4 = new java.awt.GridBagConstraints();
	gridBagConstraints4.gridx = 1;
	gridBagConstraints4.gridy = 1;
	gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints4.weightx = 1.0;
	mysqlPanel.add(dbHostText, gridBagConstraints4);

	ResourceHelper.setText(jLabel18, "port");
	jLabel18.setLabelFor(dbPortText);
	GridBagConstraints gridBagConstraints5 = new java.awt.GridBagConstraints();
	gridBagConstraints5.gridx = 0;
	gridBagConstraints5.gridy = 2;
	gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints5.insets = new java.awt.Insets(0, 4, 0, 4);
	mysqlPanel.add(jLabel18, gridBagConstraints5);

	GridBagConstraints gridBagConstraints6 = new java.awt.GridBagConstraints();
	gridBagConstraints6.gridx = 1;
	gridBagConstraints6.gridy = 2;
	gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints6.weightx = 1.0;
	mysqlPanel.add(dbPortText, gridBagConstraints6);

	ResourceHelper.setText(jLabel19, "User");
	jLabel19.setLabelFor(dbUserText);
	GridBagConstraints gridBagConstraints25 = new java.awt.GridBagConstraints();
	gridBagConstraints25.gridx = 0;
	gridBagConstraints25.gridy = 3;
	gridBagConstraints25.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints25.insets = new java.awt.Insets(0, 4, 0, 4);
	mysqlPanel.add(jLabel19, gridBagConstraints25);

	GridBagConstraints gridBagConstraints7 = new java.awt.GridBagConstraints();
	gridBagConstraints7.gridx = 1;
	gridBagConstraints7.gridy = 3;
	gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints7.weightx = 1.0;
	mysqlPanel.add(dbUserText, gridBagConstraints7);

	ResourceHelper.setText(jLabel20, "Password");
	jLabel20.setLabelFor(jPasswordField1);
	GridBagConstraints gridBagConstraints8 = new java.awt.GridBagConstraints();
	gridBagConstraints8.gridx = 0;
	gridBagConstraints8.gridy = 4;
	gridBagConstraints8.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints8.insets = new java.awt.Insets(0, 4, 0, 4);
	mysqlPanel.add(jLabel20, gridBagConstraints8);

	GridBagConstraints gridBagConstraints9 = new java.awt.GridBagConstraints();
	gridBagConstraints9.gridx = 1;
	gridBagConstraints9.gridy = 4;
	gridBagConstraints9.fill = java.awt.GridBagConstraints.BOTH;
	mysqlPanel.add(jPasswordField1, gridBagConstraints9);

	return mysqlPanel;
    }

    /**
         * This method initializes palmcb
         * 
         * @return javax.swing.JCheckBox
         */
    private JCheckBox getPalmcb() {
	if (palmcb == null) {
	    palmcb = new JCheckBox();
	    ResourceHelper.setText(palmcb, "palmopt");
	}
	return palmcb;
    }

    private JPanel getPrintPanel() {
	JPanel printPanel = new JPanel();
	printPanel.setLayout(new java.awt.GridBagLayout());

	ResourceHelper.setText(colorprint, "Print_In_Color?");
	GridBagConstraints gridBagConstraints40 = new java.awt.GridBagConstraints();
	gridBagConstraints40.gridx = 0;
	gridBagConstraints40.gridy = 0;
	gridBagConstraints40.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints40.anchor = java.awt.GridBagConstraints.NORTHEAST;
	printPanel.add(colorprint, gridBagConstraints40);

	ResourceHelper.setText(logobox, "Print_Logo_(GIF/JPG/PNG)");
	GridBagConstraints gridBagConstraints43 = new java.awt.GridBagConstraints();
	gridBagConstraints43.gridx = 0;
	gridBagConstraints43.gridy = 1;
	gridBagConstraints43.fill = java.awt.GridBagConstraints.HORIZONTAL;
	printPanel.add(logobox, gridBagConstraints43);

	logofile.setEditable(false);
	GridBagConstraints gridBagConstraints41 = new java.awt.GridBagConstraints();
	gridBagConstraints41.gridx = 1;
	gridBagConstraints41.gridy = 1;
	gridBagConstraints41.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints41.weightx = 1.0;
	gridBagConstraints41.insets = new java.awt.Insets(0, 8, 0, 8);
	printPanel.add(logofile, gridBagConstraints41);

	ResourceHelper.setText(logobrowse, "Browse");
	logobrowse.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		logobrowseActionPerformed(evt);
	    }
	});

	GridBagConstraints gridBagConstraints42 = new java.awt.GridBagConstraints();
	gridBagConstraints42.gridx = 2;
	gridBagConstraints42.gridy = 1;
	gridBagConstraints42.fill = java.awt.GridBagConstraints.BOTH;
	printPanel.add(logobrowse, gridBagConstraints42);
	return printPanel;
    }

    private JPanel getReminderPanel() {

	JPanel reminderPanel = new JPanel();
	reminderPanel.setLayout(new java.awt.GridBagLayout());

	ResourceHelper.setText(popenablebox, "enable_popups");
	GridBagConstraints gridBagConstraints52 = new java.awt.GridBagConstraints();
	gridBagConstraints52.gridx = 0;
	gridBagConstraints52.gridy = 0;
	gridBagConstraints52.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints52.insets = new java.awt.Insets(0, 8, 0, 0);
	reminderPanel.add(popenablebox, gridBagConstraints52);

	ResourceHelper.setText(soundbox, "beeps");
	GridBagConstraints gridBagConstraints59 = new java.awt.GridBagConstraints();
	gridBagConstraints59.gridx = 0;
	gridBagConstraints59.gridy = 3;
	gridBagConstraints59.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints59.insets = new java.awt.Insets(0, 8, 0, 0);
	reminderPanel.add(soundbox, gridBagConstraints59);

	jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
	ResourceHelper.setText(jLabel15, "min_between_chks");
	jLabel15.setLabelFor(getEmailtimebox());
	GridBagConstraints gridBagConstraints63 = new java.awt.GridBagConstraints();
	gridBagConstraints63.gridx = 0;
	gridBagConstraints63.gridy = 1;
	gridBagConstraints63.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints63.insets = new java.awt.Insets(0, 0, 0, 8);
	reminderPanel.add(jLabel15, gridBagConstraints63);

	checkfreq.setMinimumSize(new java.awt.Dimension(50, 20));
	GridBagConstraints gridBagConstraints64 = new java.awt.GridBagConstraints();
	gridBagConstraints64.gridx = 1;
	gridBagConstraints64.gridy = 1;
	gridBagConstraints64.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints64.weightx = 1.0;
	reminderPanel.add(checkfreq, gridBagConstraints64);

	GridBagConstraints gridBagConstraints65 = new java.awt.GridBagConstraints();
	gridBagConstraints65.gridx = 0;
	gridBagConstraints65.gridy = 2;
	gridBagConstraints65.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints65.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints65.insets = new java.awt.Insets(8, 8, 8, 8);
	reminderPanel.add(jSeparator1, gridBagConstraints65);

	ResourceHelper.setText(jLabel16, "restart_req");
	GridBagConstraints gridBagConstraints66 = new java.awt.GridBagConstraints();
	gridBagConstraints66.gridx = 2;
	gridBagConstraints66.gridy = 1;
	gridBagConstraints66.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints66.insets = new java.awt.Insets(0, 8, 0, 0);
	reminderPanel.add(jLabel16, gridBagConstraints66);
	ResourceHelper.setText(sharedFileCheckBox, "shared");

	GridBagConstraints gridBagConstraints112 = new GridBagConstraints();
	gridBagConstraints112.gridx = 0;
	gridBagConstraints112.gridy = 4;
	gridBagConstraints112.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints112.insets = new java.awt.Insets(0, 8, 0, 0);
	reminderPanel.add(getUseBeep(), gridBagConstraints112);

	GridBagConstraints gridBagConstraints113 = new GridBagConstraints();
	gridBagConstraints113.gridx = 0;
	gridBagConstraints113.gridy = 5;
	gridBagConstraints113.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints113.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints113.insets = new java.awt.Insets(18, 18, 18, 18);
	reminderPanel.add(remTimePanel, gridBagConstraints113);

	return reminderPanel;
    }

    /**
         * This method initializes remoteButton
         * 
         * @return javax.swing.JRadioButton
         */
    private JRadioButton getRemoteButton() {
	if (remoteButton == null) {
	    remoteButton = new JRadioButton();
	    remoteButton.setActionCommand("remote"); // Generated
	    ResourceHelper.setText(remoteButton, "rem_server"); // Generated
	    remoteButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    dbTypeAction(e);
		}
	    });
	}
	return remoteButton;
    }

    /**
         * This method initializes jPanel12
         * 
         * @return javax.swing.JPanel
         */
    private JPanel getRemoteServerPanel() {
	if (remoteServerPanel == null) {
	    GridBagConstraints gridBagConstraints54 = new GridBagConstraints();
	    gridBagConstraints54.fill = java.awt.GridBagConstraints.HORIZONTAL; // Generated
	    gridBagConstraints54.gridy = 1; // Generated
	    gridBagConstraints54.ipadx = 0; // Generated
	    gridBagConstraints54.weightx = 1.0; // Generated
	    gridBagConstraints54.insets = new java.awt.Insets(4, 4, 4, 4); // Generated
	    gridBagConstraints54.gridx = 0; // Generated
	    GridBagConstraints gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0; // Generated
	    gridBagConstraints.ipadx = 0; // Generated
	    gridBagConstraints.ipady = 5; // Generated
	    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4); // Generated
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST; // Generated
	    gridBagConstraints.gridy = 0; // Generated

	    jLabel = new JLabel();
	    ResourceHelper.setText(jLabel, "enturl");
	    jLabel.setLabelFor(getRemoteURLText());
	    jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT); // Generated
	    jLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT); // Generated
	    remoteServerPanel = new JPanel();
	    remoteServerPanel.setLayout(new GridBagLayout()); // Generated
	    remoteServerPanel
		    .setBorder(javax.swing.BorderFactory
			    .createTitledBorder(
				    null,
				    Resource
					    .getResourceString("rem_server_info"),
				    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				    javax.swing.border.TitledBorder.DEFAULT_POSITION,
				    null, null)); // Generated
	    remoteServerPanel.add(jLabel, gridBagConstraints); // Generated
	    remoteServerPanel.add(getRemoteURLText(), gridBagConstraints54); // Generated
	}
	return remoteServerPanel;
    }

    /**
         * This method initializes remoteURLText
         * 
         * @return javax.swing.JTextField
         */
    private JTextField getRemoteURLText() {
	if (remoteURLText == null) {
	    remoteURLText = new JTextField();
	}
	return remoteURLText;
    }

    private JSpinner getSyncMins() {
	if (syncmins == null) {
	    syncmins = new JSpinner();
	}
	return syncmins;
    }

    private JPanel getTopPanel() {
	if (topPanel == null) {
	    GridBagConstraints gridBagConstraints510 = new GridBagConstraints();
	    GridBagConstraints gridBagConstraints210 = new GridBagConstraints();
	    topPanel = new JPanel();
	    topPanel.setLayout(new GridBagLayout());
	    gridBagConstraints210.gridx = 0;
	    gridBagConstraints210.gridy = 0;
	    gridBagConstraints210.weightx = 1.0;
	    gridBagConstraints210.weighty = 1.0;
	    gridBagConstraints210.fill = java.awt.GridBagConstraints.BOTH;
	    gridBagConstraints210.insets = new java.awt.Insets(4, 4, 4, 4);
	    gridBagConstraints210.gridwidth = 1;
	    gridBagConstraints510.gridx = 0;
	    gridBagConstraints510.gridy = 2;
	    gridBagConstraints510.insets = new java.awt.Insets(4, 4, 4, 4);
	    topPanel.add(jTabbedPane1, gridBagConstraints210);
	    topPanel.add(getApplyDismissPanel(), gridBagConstraints510);
	}
	return topPanel;
    }

    /**
         * This method initializes truncbox
         * 
         * @return javax.swing.JCheckBox
         */
    private JCheckBox getTruncbox() {
	if (truncbox == null) {
	    truncbox = new JCheckBox();
	    ResourceHelper.setText(truncbox, "truncate_appts");
	}
	return truncbox;
    }

    /**
         * This method initializes useBeep
         * 
         * @return javax.swing.JCheckBox
         */
    private JCheckBox getUseBeep() {
	if (useBeep == null) {
	    useBeep = new JCheckBox();
	    ResourceHelper.setText(useBeep, "Use_system_beep");
	}
	return useBeep;
    }

    private void hsqldbActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton5ActionPerformed

	// browse for new database dir
	String dbdir = OptionsView.chooseDbDir(false);
	if (dbdir == null) {
	    return;
	}

	// update text field - nothing else changes. DB change will take
	// effect
	// only on restart
	hsqldbdir.setText(dbdir);

    }

    private void initComponents() {

	calShowSubtaskBox.setName("calShowSubtaskBox");
	calShowSubtaskBox.setHorizontalAlignment(SwingConstants.LEFT);
	calShowTaskBox.setName("calShowTaskBox");
	calShowTaskBox.setHorizontalAlignment(SwingConstants.LEFT);
	taskAbbrevBox.setName("taskAbbrevBox");
	taskAbbrevBox.setHorizontalAlignment(SwingConstants.LEFT);
	remtimelabel = new JLabel();
	jTabbedPane1 = new javax.swing.JTabbedPane();
	privbox = new javax.swing.JCheckBox();
	pubbox = new javax.swing.JCheckBox();
	previewFontButton = new javax.swing.JButton();
	apptFontButton = new javax.swing.JButton();
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
	defFontButton = new javax.swing.JButton();
	colorsortbox = new javax.swing.JCheckBox();
	jLabel7 = new javax.swing.JLabel();
	dbNameText = new javax.swing.JTextField();
	jLabel17 = new javax.swing.JLabel();
	dbHostText = new javax.swing.JTextField();
	jLabel18 = new javax.swing.JLabel();
	dbPortText = new javax.swing.JTextField();
	jLabel19 = new javax.swing.JLabel();
	dbUserText = new javax.swing.JTextField();
	jLabel20 = new javax.swing.JLabel();
	jPasswordField1 = new javax.swing.JPasswordField();
	jLabel3 = new javax.swing.JLabel();
	dbDirText = new javax.swing.JTextField();
	jButton5 = new javax.swing.JButton();
	chgdb = new javax.swing.JButton();
	jLabel1 = new javax.swing.JLabel();
	jLabel2 = new javax.swing.JLabel();
	smtptext = new javax.swing.JTextField();
	emailtext = new javax.swing.JTextField();
	emailbox = new javax.swing.JCheckBox();
	colorprint = new javax.swing.JCheckBox();
	logobox = new javax.swing.JCheckBox();
	logofile = new javax.swing.JTextField();
	logobrowse = new javax.swing.JButton();
	autoupdate = new javax.swing.JCheckBox();
	versioncheck = new javax.swing.JButton();
	splashbox = new javax.swing.JCheckBox();
	backgbox = new javax.swing.JCheckBox();
	stackbox = new javax.swing.JCheckBox();
	icaltodobox = new javax.swing.JCheckBox();
	revDayEditbox = new javax.swing.JCheckBox();
	popenablebox = new javax.swing.JCheckBox();
	soundbox = new javax.swing.JCheckBox();
	jLabel15 = new javax.swing.JLabel();
	checkfreq = new javax.swing.JSpinner();
	jSeparator1 = new javax.swing.JSeparator();
	jLabel16 = new javax.swing.JLabel();
	sharedFileCheckBox = new javax.swing.JCheckBox();
	dismissButton = new javax.swing.JButton();
	applyButton = new javax.swing.JButton();

	setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	ResourceHelper.setTitle(this, "Options");
	addWindowListener(new java.awt.event.WindowAdapter() {
	    public void windowClosing(java.awt.event.WindowEvent evt) {
		exitForm(evt);
	    }
	});

	ResourceHelper.addTab(jTabbedPane1, "appearance", getAppearancePanel());
	ResourceHelper.addTab(jTabbedPane1, "fonts", getFontPanel());
	ResourceHelper
		.addTab(jTabbedPane1, "DatabaseInformation", getDBPanel());
	ResourceHelper.addTab(jTabbedPane1, "EmailParameters", getEmailPanel());
	ResourceHelper.addTab(jTabbedPane1, "popup_reminders",
		getReminderPanel());
	ResourceHelper.addTab(jTabbedPane1, "printing", getPrintPanel());
	ResourceHelper.addTab(jTabbedPane1, "Multi_User", getMultiUserPanel());
	ResourceHelper.addTab(jTabbedPane1, "misc", getMiscPanel());
	ResourceHelper.addTab(jTabbedPane1, "UserColorScheme", getJPanelUCS());
	ResourceHelper
		.addTab(jTabbedPane1, "taskOptions", getTaskOptionPanel());

	this.setContentPane(getTopPanel());
	this.setSize(629, 493);

	pack();
    }// GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton2ActionPerformed
    {// GEN-HEADEREND:event_jButton2ActionPerformed
	this.dispose();
    }// GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton5ActionPerformed

	// browse for new database dir
	String dbdir = OptionsView.chooseDbDir(false);
	if (dbdir == null) {
	    return;
	}

	// update text field - nothing else changes. DB change will take
	// effect
	// only on restart
	dbDirText.setText(dbdir);

    }// GEN-LAST:event_jButton5ActionPerformed

    private void logobrowseActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_logobrowseActionPerformed
    {// GEN-HEADEREND:event_logobrowseActionPerformed

	// browse for new logo file
	logobox.setSelected(true);
	String logo = null;
	while (true) {
	    JFileChooser chooser = new JFileChooser();

	    chooser.setCurrentDirectory(new File("."));
	    chooser
		    .setDialogTitle(Resource
			    .getResourceString("Please_choose_the_logo_file_-_GIF/JPG/PNG_only"));
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

	    int returnVal = chooser.showOpenDialog(null);
	    if (returnVal != JFileChooser.APPROVE_OPTION) {
		return;
	    }

	    logo = chooser.getSelectedFile().getAbsolutePath();
	    File lf = new File(logo);
	    String err = null;
	    if (!lf.exists()) {
		err = Resource.getResourceString("File_[") + logo
			+ Resource.getResourceString("]_does_not_exist");
	    } else if (!lf.canRead()) {
		err = Resource.getResourceString("Database_Directory_[") + logo
			+ Resource.getResourceString("]_is_not_writable");
	    }

	    if (err == null) {
		break;
	    }

	    Errmsg.notice(err);
	}

	// update text field - nothing else changes. DB change will take
	// effect
	// only on restart
	logofile.setText(logo);
    }// GEN-LAST:event_logobrowseActionPerformed

    private void versioncheckActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_versioncheckActionPerformed
    {// GEN-HEADEREND:event_versioncheckActionPerformed
	try {
	    // get version and compare
	    URL webverurl = new URL(
		    "http://borg-calendar.sourceforge.net/latest_version");
	    InputStream is = webverurl.openStream();
	    int i;
	    String webver = "";
	    while (true) {
		i = is.read();
		if (i == -1 || i == '\n' || i == '\r') {
		    break;
		}
		webver += (char) i;
	    }

	    String info = Resource.getResourceString("Your_BORG_version_=_")
		    + Resource.getVersion()
		    + Resource
			    .getResourceString("Latest_version_at_sourceforge_=_")
		    + webver;
	    JOptionPane.showMessageDialog(null, info, Resource
		    .getResourceString("BORG_Version_Check"),
		    JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getClass()
			    .getResource("/resource/borg.jpg")));
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

    }// GEN-LAST:event_versioncheckActionPerformed
}
