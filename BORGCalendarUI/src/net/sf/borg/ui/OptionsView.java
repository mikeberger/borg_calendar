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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.sf.borg.common.app.AppHelper;
import net.sf.borg.common.ui.JButtonKnowsBgColor;
import net.sf.borg.common.ui.NwFontChooserS;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.model.AppointmentModel;
import java.awt.FlowLayout;

// propgui displays the edit preferences window
public class OptionsView extends View {

	// to break a dependency with the contol package
	public interface RestartListener {
		public void restart();
	}

	static private RestartListener rl_ = null; // someone to call to request a

	// restart

	private static OptionsView singleton = null;

	public static OptionsView getReference() {
		if (singleton == null || !singleton.isShowing())
			singleton = new OptionsView(false);
		return (singleton);
	}

	static public void setRestartListener(RestartListener rl) {
		rl_ = rl;
	}

	public static void dbSelectOnly() {
		new OptionsView(true).setVisible(true);

	}

	static private void setCheckBox(JCheckBox box, PrefName pn) {
		String val = Prefs.getPref(pn);
		if (val.equals("true"))
			box.setSelected(true);
		else
			box.setSelected(false);
	}

	static private void setBooleanPref(JCheckBox box, PrefName pn) {
		if (box.isSelected())
			Prefs.putPref(pn, "true");
		else
			Prefs.putPref(pn, "false");
	}

	// dbonly will only allow db changes
	private OptionsView(boolean dbonly) {
		super();

		initComponents();
		dbTypeGroup = new javax.swing.ButtonGroup();
		dbTypeGroup.add(localFileButton);
		dbTypeGroup.add(MySQLButton);
		dbTypeGroup.add(remoteButton);

		if (!dbonly) {
			addModel(AppointmentModel.getReference());
		} else {
			setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		}

		// set the various screen items based on the existing user preferences

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

		//
		// database
		//
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		if (dbtype.equals("local")) {
			localFileButton.setSelected(true);
			jPanel9.setVisible(true);
			jPanel8.setVisible(false);
			jPanel12.setVisible(false);

		} else if (dbtype.equals("remote")) {
			remoteButton.setSelected(true);
			jPanel8.setVisible(false);
			jPanel9.setVisible(false);
			jPanel12.setVisible(true);

		} else {
			MySQLButton.setSelected(true);
			jPanel8.setVisible(true);
			jPanel9.setVisible(false);
			jPanel12.setVisible(false);

		}

		dbDirText.setText(Prefs.getPref(PrefName.DBDIR));
		dbNameText.setText(Prefs.getPref(PrefName.DBNAME));
		dbPortText.setText(Prefs.getPref(PrefName.DBPORT));
		dbHostText.setText(Prefs.getPref(PrefName.DBHOST));
		dbUserText.setText(Prefs.getPref(PrefName.DBUSER));
		jPasswordField1.setText(Prefs.getPref(PrefName.DBPASS));
		remoteURLText.setText(Prefs.getPref(PrefName.DBURL));

		if (dbonly) {
			// disable lots of non-db-related stuff
			jTabbedPane1.setEnabledAt(0, false);
			jTabbedPane1.setEnabledAt(2, false);
			jTabbedPane1.setEnabledAt(3, false);
			jTabbedPane1.setEnabledAt(4, false);
			jTabbedPane1.setEnabledAt(5, false);
			jTabbedPane1.setEnabledAt(6, false);
			jTabbedPane1.setEnabledAt(7, false);
			jTabbedPane1.setSelectedIndex(1);
			jButton2.setEnabled(false);
			applyButton.setEnabled(false);
			return;

		}

		// set various simple boolean checkboxes
		setCheckBox(colorprint, PrefName.COLORPRINT);
		setCheckBox(pubbox, PrefName.SHOWPUBLIC);
		setCheckBox(privbox, PrefName.SHOWPRIVATE);
		setCheckBox(emailbox, PrefName.EMAILENABLED);
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
		setCheckBox(sharedbox, PrefName.SHARED);
		setCheckBox(icaltodobox, PrefName.ICALTODOEV);
		setCheckBox(truncbox, PrefName.TRUNCAPPT);

		// print logo directory
		String logo = Prefs.getPref(PrefName.LOGO);
		logofile.setText(logo);
		if (!logo.equals(""))
			logobox.setSelected(true);
		else
			logobox.setSelected(false);

		// email server and address
		smtptext.setText(Prefs.getPref(PrefName.EMAILSERVER));
		emailtext.setText(Prefs.getPref(PrefName.EMAILADDR));

		int fdow = Prefs.getIntPref(PrefName.FIRSTDOW);
		if (fdow == Calendar.MONDAY)
			mondaycb.setSelected(true);
		else
			mondaycb.setSelected(false);

		// auto update check
		int au = Prefs.getIntPref(PrefName.VERCHKLAST);
		if (au != -1)
			autoupdate.setSelected(true);
		else
			autoupdate.setSelected(false);

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
			Class.forName("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
			lnfs.add("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
		} catch (Exception e) {
		}
		try {
			Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
			lnfs.add("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
		} catch (Exception e) {
		}

		lnfs.add(curlnf);

		Iterator it = lnfs.iterator();
		while (it.hasNext())
			lnfBox.addItem(it.next());

		lnfBox.setSelectedItem(curlnf);

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
		// (bsv 2004-12-20)

		logobrowse.setEnabled(AppHelper.isApplication());

		manageMySize(PrefName.OPTVIEWSIZE);
	}

	private void initComponents() {

		remtimelabel = new JLabel();
		syncminlabel = new JLabel();
		syncminlabel.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("sync_mins"));
		GridBagConstraints gridBagConstraints53 = new GridBagConstraints();
		gridBagConstraints53.gridx = 1;
		gridBagConstraints53.gridy = 1;
		gridBagConstraints53.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints53.insets = new java.awt.Insets(4, 4, 4, 4);
		GridBagConstraints gridBagConstraints410 = new GridBagConstraints();
		gridBagConstraints410.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints410.fill = java.awt.GridBagConstraints.BOTH;

		gridBagConstraints53.gridx = 1;
		gridBagConstraints53.gridy = 1;
		gridBagConstraints53.weightx = 1.0;
		gridBagConstraints53.fill = java.awt.GridBagConstraints.HORIZONTAL;

		lslabel = new JLabel();
		lslabel.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("line_spacing"));
		GridBagConstraints gridBagConstraints112 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints113 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
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
		jPanel4 = null;
		jPanel8 = new javax.swing.JPanel();
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
		jPanel9 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		dbDirText = new javax.swing.JTextField();
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
		autoupdate = new javax.swing.JCheckBox();
		versioncheck = new javax.swing.JButton();
		splashbox = new javax.swing.JCheckBox();
		backgbox = new javax.swing.JCheckBox();
		stackbox = new javax.swing.JCheckBox();
		icaltodobox = new javax.swing.JCheckBox();
		revDayEditbox = new javax.swing.JCheckBox();
		jPanel6 = new javax.swing.JPanel();
		popenablebox = new javax.swing.JCheckBox();
		soundbox = new javax.swing.JCheckBox();
		jLabel15 = new javax.swing.JLabel();
		checkfreq = new javax.swing.JSpinner();
		jSeparator1 = new javax.swing.JSeparator();
		jLabel16 = new javax.swing.JLabel();
		jPanel7 = new javax.swing.JPanel();
		sharedbox = new javax.swing.JCheckBox();
		jButton2 = new javax.swing.JButton();
		applyButton = new javax.swing.JButton();

		// bsv 2004-12-20
		cb_ucs_on = new javax.swing.JCheckBox("use user colors in month view");
		cb_ucs_ontodo = new javax.swing.JCheckBox(
				"use user colors in todo list");
		cb_ucs_marktodo = new javax.swing.JCheckBox("mark todo in month view");
		tf_ucs_marker = new JTextField("! ");
		btn_ucs_red = new JButtonKnowsBgColor("red", Color.WHITE, false);
		btn_ucs_blue = new JButtonKnowsBgColor("blue", Color.WHITE, false);
		btn_ucs_green = new JButtonKnowsBgColor("green", Color.WHITE, false);
		btn_ucs_black = new JButtonKnowsBgColor("black", Color.WHITE, false);
		btn_ucs_white = new JButtonKnowsBgColor("white", Color.WHITE, false);
		btn_ucs_tasks = new JButtonKnowsBgColor("tasks", Color.WHITE, false);
		btn_ucs_holidays = new JButtonKnowsBgColor("holidays", Color.WHITE,
				false);
		btn_ucs_birthdays = new JButtonKnowsBgColor("birthdays", Color.WHITE,
				false);
		btn_ucs_default = new JButtonKnowsBgColor("default", Color.WHITE, true);
		btn_ucs_holiday = new JButtonKnowsBgColor("holiday", Color.WHITE, true);
		btn_ucs_halfday = new JButtonKnowsBgColor("halfday", Color.WHITE, true);
		btn_ucs_vacation = new JButtonKnowsBgColor("vacation", Color.WHITE,
				true);
		btn_ucs_today = new JButtonKnowsBgColor("today", Color.WHITE, true);
		btn_ucs_weekend = new JButtonKnowsBgColor("weekend", Color.WHITE, true);
		btn_ucs_weekday = new JButtonKnowsBgColor("weekday", Color.WHITE, true);
		btn_ucs_restore = new JButton("restore defaults");
		// TODO add action listener to btn_ucs_restore
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
				// // Calendar view day background colors
				// // TODO choose correct colors
				btn_ucs_default.setColorProperty(new Color(11316396));
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
				btn_ucs_weekday.setColorProperty(new Color(13421772));
				btn_ucs_weekday.setColorByProperty();
			}
		});
		// (bsv 2004-12-20)

		// getContentPane().setLayout(new java.awt.GridBagLayout());

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(java.util.ResourceBundle.getBundle("resource/borg_resource")
				.getString("Options"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		jPanel2.setLayout(new java.awt.GridBagLayout());

		jPanel2.setName(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("appearance"));
		privbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource")
				.getString("Show_Private_Appointments"));
		GridBagConstraints gridBagConstraints0 = new java.awt.GridBagConstraints();
		gridBagConstraints0.gridx = 1;
		gridBagConstraints0.gridy = 1;
		gridBagConstraints0.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints0.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints0.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel2.add(privbox, gridBagConstraints0);

		pubbox
				.setText(java.util.ResourceBundle.getBundle(
						"resource/borg_resource").getString(
						"Show_Public_Appointments"));
		GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints1.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel2.add(pubbox, gridBagConstraints1);

		incfont.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("set_pre_font"));
		incfont.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		incfont.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				incfontActionPerformed(evt);
			}
		});

		GridBagConstraints gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridy = 9;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints2.weightx = 1.0;
		gridBagConstraints2.insets = new java.awt.Insets(4, 4, 4, 4);
		decfont.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("set_appt_font"));
		decfont.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		decfont.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				decfontActionPerformed(evt);
			}
		});

		GridBagConstraints gridBagConstraints3 = new java.awt.GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.gridy = 9;
		gridBagConstraints3.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints3.weightx = 1.0;
		gridBagConstraints3.insets = new java.awt.Insets(4, 4, 4, 4);
		jLabel4.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Look_and_Feel:"));
		GridBagConstraints gridBagConstraints4 = new java.awt.GridBagConstraints();
		gridBagConstraints4.gridx = 0;
		gridBagConstraints4.gridy = 0;
		gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints4.insets = new java.awt.Insets(4, 4, 4, 4);

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

		holiday1.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Show_U.S._Holidays"));
		GridBagConstraints gridBagConstraints6 = new java.awt.GridBagConstraints();
		gridBagConstraints6.gridx = 0;
		gridBagConstraints6.gridy = 3;
		gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints6.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel2.add(holiday1, gridBagConstraints6);

		mondaycb.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Week_Starts_with_Monday"));
		GridBagConstraints gridBagConstraints7 = new java.awt.GridBagConstraints();
		gridBagConstraints7.gridx = 1;
		gridBagConstraints7.gridy = 4;
		gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints7.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel2.add(mondaycb, gridBagConstraints7);

		miltime.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Use_24_hour_time_format"));
		GridBagConstraints gridBagConstraints8 = new java.awt.GridBagConstraints();
		gridBagConstraints8.gridx = 0;
		gridBagConstraints8.gridy = 4;
		gridBagConstraints8.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints8.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel2.add(miltime, gridBagConstraints8);

		jLabel5.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Week_View_Start_Hour:_"));
		GridBagConstraints gridBagConstraints9 = new java.awt.GridBagConstraints();
		gridBagConstraints9.gridx = 0;
		gridBagConstraints9.gridy = 6;
		gridBagConstraints9.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints9.insets = new java.awt.Insets(4, 4, 4, 4);
		wkstarthr.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"4", "5", "6", "7", "8", "9", "10", "11" }));
		GridBagConstraints gridBagConstraints10 = new java.awt.GridBagConstraints();
		gridBagConstraints10.gridx = 1;
		gridBagConstraints10.gridy = 6;
		gridBagConstraints10.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints10.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints10.insets = new java.awt.Insets(4, 4, 4, 4);
		wkendhr.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"12", "13", "14", "15", "16", "17", "18", "19", "20", "21",
				"22", "23" }));
		GridBagConstraints gridBagConstraints11 = new java.awt.GridBagConstraints();
		gridBagConstraints11.gridx = 1;
		gridBagConstraints11.gridy = 7;
		gridBagConstraints11.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints11.insets = new java.awt.Insets(4, 4, 4, 4);
		jLabel6.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Week_View_End_Hour:_"));
		GridBagConstraints gridBagConstraints12 = new java.awt.GridBagConstraints();
		gridBagConstraints12.gridx = 0;
		gridBagConstraints12.gridy = 7;
		gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints12.insets = new java.awt.Insets(4, 4, 4, 4);
		wrapbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Wrap_Appointment_Text"));
		GridBagConstraints gridBagConstraints13 = new java.awt.GridBagConstraints();
		gridBagConstraints13.gridx = 0;
		gridBagConstraints13.gridy = 2;
		gridBagConstraints13.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints13.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel2.add(wrapbox, gridBagConstraints13);

		canadabox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Show_Canadian_Holidays"));
		GridBagConstraints gridBagConstraints14 = new java.awt.GridBagConstraints();
		gridBagConstraints14.gridx = 1;
		gridBagConstraints14.gridy = 3;
		gridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints14.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel2.add(canadabox, gridBagConstraints14);

		jLabel8.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("locale"));
		GridBagConstraints gridBagConstraints15 = new java.awt.GridBagConstraints();
		gridBagConstraints15.gridx = 0;
		gridBagConstraints15.gridy = 11;
		gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints15.insets = new java.awt.Insets(4, 4, 4, 4);
		GridBagConstraints gridBagConstraints16 = new java.awt.GridBagConstraints();
		gridBagConstraints16.gridx = 1;
		gridBagConstraints16.gridy = 11;
		gridBagConstraints16.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints16.insets = new java.awt.Insets(4, 4, 4, 4);
		jButton1.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("set_def_font"));
		jButton1.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		GridBagConstraints gridBagConstraints17 = new java.awt.GridBagConstraints();
		gridBagConstraints17.gridx = 0;
		gridBagConstraints17.gridy = 8;
		gridBagConstraints17.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints17.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints17.weightx = 1.0;
		gridBagConstraints17.insets = new java.awt.Insets(4, 4, 4, 4);
		colorsortbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("colorsort"));
		GridBagConstraints gridBagConstraints18 = new java.awt.GridBagConstraints();
		gridBagConstraints18.gridx = 0;
		gridBagConstraints18.gridy = 5;
		gridBagConstraints18.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints18.insets = new java.awt.Insets(4, 4, 4, 4);
		jTabbedPane1.addTab(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("appearance"), jPanel2);

		jPanel8.setLayout(new java.awt.GridBagLayout());

		jPanel8.setBorder(new javax.swing.border.TitledBorder(
				java.util.ResourceBundle.getBundle("resource/borg_resource")
						.getString("MySQLInfo")));
		jLabel7.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("DatabaseName"));
		GridBagConstraints gridBagConstraints19 = new java.awt.GridBagConstraints();
		gridBagConstraints19.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints19.insets = new java.awt.Insets(0, 4, 0, 4);
		jPanel8.add(jLabel7, gridBagConstraints19);

		GridBagConstraints gridBagConstraints20 = new java.awt.GridBagConstraints();
		gridBagConstraints20.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints20.weightx = 1.0;
		jPanel8.add(dbNameText, gridBagConstraints20);

		jLabel17.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("hostname"));
		GridBagConstraints gridBagConstraints21 = new java.awt.GridBagConstraints();
		gridBagConstraints21.gridx = 0;
		gridBagConstraints21.gridy = 1;
		gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints21.insets = new java.awt.Insets(0, 4, 0, 4);
		jPanel8.add(jLabel17, gridBagConstraints21);

		GridBagConstraints gridBagConstraints22 = new java.awt.GridBagConstraints();
		gridBagConstraints22.gridx = 1;
		gridBagConstraints22.gridy = 1;
		gridBagConstraints22.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints22.weightx = 1.0;
		jPanel8.add(dbHostText, gridBagConstraints22);

		jLabel18.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("port"));
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
		jPanel8.add(dbPortText, gridBagConstraints24);

		jLabel19.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("User"));
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
		jPanel8.add(dbUserText, gridBagConstraints26);

		jLabel20.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Password"));
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

		jPanel9.setLayout(new java.awt.GridBagLayout());

		jPanel9.setBorder(new javax.swing.border.TitledBorder(
				java.util.ResourceBundle.getBundle("resource/borg_resource")
						.getString("localFileInfo")));
		jLabel3.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("DataBase_Directory"));
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
		jPanel9.add(dbDirText, gridBagConstraints31);

		jButton5.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Browse"));
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

		chgdb.setForeground(new java.awt.Color(255, 0, 51));
		chgdb.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Refresh16.gif")));
		chgdb.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Apply_DB_Change"));
		chgdb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				chgdbActionPerformed(evt);
			}
		});

		jTabbedPane1.addTab(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("DatabaseInformation"),
				getJPanel4());
		jPanel2.add(jLabel4, gridBagConstraints4);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jLabel1.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("SMTP_Server"));
		GridBagConstraints gridBagConstraints35 = new java.awt.GridBagConstraints();
		gridBagConstraints35.gridx = 0;
		gridBagConstraints35.gridy = 1;
		gridBagConstraints35.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints35.insets = new java.awt.Insets(0, 4, 0, 0);
		jPanel1.add(jLabel1, gridBagConstraints35);

		jLabel2.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Your_Email_Address"));
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

		emailbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Enable_Email"));
		GridBagConstraints gridBagConstraints39 = new java.awt.GridBagConstraints();
		gridBagConstraints39.gridx = 0;
		gridBagConstraints39.gridy = 0;
		gridBagConstraints39.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints39.anchor = java.awt.GridBagConstraints.WEST;
		jPanel1.add(emailbox, gridBagConstraints39);

		jTabbedPane1
				.addTab(java.util.ResourceBundle.getBundle(
						"resource/borg_resource").getString("EmailParameters"),
						jPanel1);

		jPanel5.setLayout(new java.awt.GridBagLayout());

		colorprint.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Print_In_Color?"));
		GridBagConstraints gridBagConstraints40 = new java.awt.GridBagConstraints();
		gridBagConstraints40.gridx = 0;
		gridBagConstraints40.gridy = 0;
		gridBagConstraints40.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints40.anchor = java.awt.GridBagConstraints.NORTHEAST;
		jPanel5.add(colorprint, gridBagConstraints40);

		logobox
				.setText(java.util.ResourceBundle.getBundle(
						"resource/borg_resource").getString(
						"Print_Logo_(GIF/JPG/PNG)"));
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

		logobrowse.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Browse"));
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

		jTabbedPane1.addTab(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("printing"), jPanel5);

		jPanel3.setLayout(new java.awt.GridBagLayout());

		autoupdate.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Auto_Update_Check"));
		autoupdate
				.setToolTipText(java.util.ResourceBundle
						.getBundle("resource/borg_resource")
						.getString(
								"Enable_a_daily_check_to_the_BORG_website_to_see_if_a_new_version_is_out._Does_not_update_the_product."));
		GridBagConstraints gridBagConstraints45 = new java.awt.GridBagConstraints();
		gridBagConstraints45.gridx = 0;
		gridBagConstraints45.gridy = 3;
		gridBagConstraints45.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints45.anchor = java.awt.GridBagConstraints.WEST;
		jPanel3.add(autoupdate, gridBagConstraints45);

		versioncheck.setFont(new java.awt.Font("Dialog", 0, 10));
		versioncheck.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Check_for_updates_now"));
		versioncheck.setToolTipText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString(
				"Check_for_the_latest_BORG_version_now"));
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
		jPanel3.add(versioncheck, gridBagConstraints46);

		splashbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("splash"));
		GridBagConstraints gridBagConstraints47 = new java.awt.GridBagConstraints();
		gridBagConstraints47.gridx = 0;
		gridBagConstraints47.gridy = 0;
		gridBagConstraints47.fill = java.awt.GridBagConstraints.BOTH;
		jPanel3.add(splashbox, gridBagConstraints47);

		backgbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString(
				"Start_in_background_(Windows_only,_TrayIcon_req)"));
		backgbox
				.setToolTipText(java.util.ResourceBundle
						.getBundle("resource/borg_resource")
						.getString(
								"Do_not_open_todo_and_month_view_on_startup,_start_in_systray"));
		GridBagConstraints gridBagConstraints48 = new java.awt.GridBagConstraints();
		gridBagConstraints48.gridx = 0;
		gridBagConstraints48.gridy = 1;
		gridBagConstraints48.gridwidth = 2;
		gridBagConstraints48.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints48.anchor = java.awt.GridBagConstraints.WEST;
		jPanel3.add(backgbox, gridBagConstraints48);

		stackbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("stackonerr"));
		GridBagConstraints gridBagConstraints49 = new java.awt.GridBagConstraints();
		gridBagConstraints49.gridx = 0;
		gridBagConstraints49.gridy = 4;
		gridBagConstraints49.fill = java.awt.GridBagConstraints.BOTH;
		jPanel3.add(stackbox, gridBagConstraints49);

		icaltodobox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("icaltodo"));
		GridBagConstraints gridBagConstraints50 = new java.awt.GridBagConstraints();
		gridBagConstraints50.gridx = 0;
		gridBagConstraints50.gridy = 5;
		gridBagConstraints50.fill = java.awt.GridBagConstraints.BOTH;
		jPanel3.add(icaltodobox, gridBagConstraints50);

		revDayEditbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("revdayedit"));
		GridBagConstraints gridBagConstraints51 = new java.awt.GridBagConstraints();
		gridBagConstraints51.gridx = 0;
		gridBagConstraints51.gridy = 7;
		gridBagConstraints51.fill = java.awt.GridBagConstraints.BOTH;
		jTabbedPane1.addTab(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("misc"), jPanel3);

		jPanel6.setLayout(new java.awt.GridBagLayout());

		popenablebox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("enable_popups"));
		GridBagConstraints gridBagConstraints52 = new java.awt.GridBagConstraints();
		gridBagConstraints52.gridx = 0;
		gridBagConstraints52.gridy = 0;
		gridBagConstraints52.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints52.insets = new java.awt.Insets(0, 8, 0, 0);
		jPanel6.add(popenablebox, gridBagConstraints52);

		soundbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("beeps"));
		GridBagConstraints gridBagConstraints59 = new java.awt.GridBagConstraints();
		gridBagConstraints59.gridx = 0;
		gridBagConstraints59.gridy = 5;
		gridBagConstraints59.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints59.insets = new java.awt.Insets(0, 8, 0, 0);
		jPanel6.add(soundbox, gridBagConstraints59);

		jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
		jLabel15.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("min_between_chks"));
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

		jLabel16.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("restart_req"));
		GridBagConstraints gridBagConstraints66 = new java.awt.GridBagConstraints();
		gridBagConstraints66.gridx = 2;
		gridBagConstraints66.gridy = 1;
		gridBagConstraints66.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints66.insets = new java.awt.Insets(0, 8, 0, 0);
		jPanel6.add(jLabel16, gridBagConstraints66);

		jTabbedPane1
				.addTab(java.util.ResourceBundle.getBundle(
						"resource/borg_resource").getString("popup_reminders"),
						jPanel6);

		sharedbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("shared"));
		jTabbedPane1.addTab(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Multi_User"), jPanel7);

		GridBagConstraints gridBagConstraints67 = new java.awt.GridBagConstraints();
		gridBagConstraints67.gridx = 0;
		gridBagConstraints67.gridy = 0;
		gridBagConstraints67.gridwidth = 2;
		gridBagConstraints67.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints67.weightx = 1.0;
		gridBagConstraints67.weighty = 1.0;
		// getContentPane().add(jTabbedPane1, gridBagConstraints67);

		jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Stop16.gif")));
		jButton2.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Dismiss"));
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		applyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		applyButton.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("apply"));
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				apply(evt);
			}
		});

		// added by bsv 2004-12-20

		JPanel njp = new JPanel();
		GridBagConstraints gridBagConstraints116 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints212 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints211 = new GridBagConstraints();

		GridBagConstraints gridBagConstraints115 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints44 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints114 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints310 = new GridBagConstraints();

		njp.setLayout(new BorderLayout());
		jPanel7.setLayout(new GridBagLayout());
		njp.add(cb_ucs_marktodo, BorderLayout.WEST);
		njp.add(tf_ucs_marker, BorderLayout.CENTER);
		getJPanelUCS().add(njp);

		this.setContentPane(getJPanel());
		this.setSize(629, 493);
		gridBagConstraints110.gridx = 1;
		gridBagConstraints110.gridy = 5;
		gridBagConstraints110.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints110.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints111.gridx = 0;
		gridBagConstraints111.gridy = 6;
		gridBagConstraints111.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints310.gridx = 0;
		gridBagConstraints310.gridy = 8;
		gridBagConstraints310.ipadx = 0;
		gridBagConstraints310.ipady = 0;
		gridBagConstraints310.insets = new java.awt.Insets(0, 0, 0, 0);
		gridBagConstraints310.anchor = java.awt.GridBagConstraints.WEST;

		gridBagConstraints112.gridx = 0;
		gridBagConstraints112.gridy = 9;
		gridBagConstraints112.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints112.insets = new java.awt.Insets(0, 8, 0, 0);

		gridBagConstraints113.gridx = 0;
		gridBagConstraints113.gridy = 10;
		gridBagConstraints113.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints113.insets = new java.awt.Insets(0, 8, 0, 0);
		gridBagConstraints114.gridx = 0;
		gridBagConstraints114.gridy = 10;
		gridBagConstraints114.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints114.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints44.gridx = 1;
		gridBagConstraints44.gridy = 10;
		gridBagConstraints44.weightx = 1.0;
		gridBagConstraints44.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints44.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints115.gridx = 1;
		gridBagConstraints115.gridy = 2;
		gridBagConstraints115.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints115.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints211.gridx = 0;
		gridBagConstraints211.gridy = 0;
		gridBagConstraints211.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints211.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints410.gridx = 0;
		gridBagConstraints410.gridy = 1;

		gridBagConstraints116.gridx = 0;
		gridBagConstraints116.gridy = 3;
		gridBagConstraints116.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints116.insets = new java.awt.Insets(4, 4, 4, 4);
		remtimelabel.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("reminder_time"));
		gridBagConstraints212.gridx = 1;
		gridBagConstraints212.gridy = 3;
		gridBagConstraints212.weightx = 1.0;
		gridBagConstraints212.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints212.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints212.anchor = java.awt.GridBagConstraints.WEST;
		jPanel7.add(sharedbox, gridBagConstraints211);
		jPanel7.add(getSyncMins(), gridBagConstraints53);
		jPanel2.add(incfont, gridBagConstraints2);
		jPanel2.add(decfont, gridBagConstraints3);
		jPanel3.add(revDayEditbox, gridBagConstraints51);
		jTabbedPane1.addTab("User Color Scheme", null, getJPanelUCS(), null);
		jPanel3.add(getExputcbox(), gridBagConstraints111);
		jPanel3.add(getPalmcb(), gridBagConstraints310);
		jPanel6.add(getUseBeep(), gridBagConstraints112);
		jPanel2.add(jLabel5, gridBagConstraints9);
		jPanel2.add(wkstarthr, gridBagConstraints10);
		jPanel2.add(wkendhr, gridBagConstraints11);
		jPanel2.add(jLabel6, gridBagConstraints12);
		jPanel2.add(jButton1, gridBagConstraints17);
		jPanel2.add(colorsortbox, gridBagConstraints18);
		jPanel2.add(jLabel8, gridBagConstraints15);
		jPanel2.add(localebox, gridBagConstraints16);
		jPanel2.add(getDoyBox(), gridBagConstraints110);
		jPanel2.add(lslabel, gridBagConstraints114);
		jPanel2.add(getLsbox(), gridBagConstraints44);
		jPanel2.add(getTruncbox(), gridBagConstraints115);
		jPanel7.add(syncminlabel, gridBagConstraints410);
		jPanel1.add(remtimelabel, gridBagConstraints116);
		jPanel1.add(getEmailtimebox(), gridBagConstraints212);

		pack();
	}// GEN-END:initComponents

	private void dbTypeAction(java.awt.event.ActionEvent evt)// GEN-FIRST:event_dbTypeAction
	{// GEN-HEADEREND:event_dbTypeAction
		if (evt.getActionCommand().equals("mysql")) {
			jPanel8.setVisible(true);
			jPanel9.setVisible(false);
			jPanel12.setVisible(false);
		} else if (evt.getActionCommand().equals("remote")) {
			jPanel8.setVisible(false);
			jPanel9.setVisible(false);
			jPanel12.setVisible(true);
		} else {
			jPanel9.setVisible(true);
			jPanel8.setVisible(false);
			jPanel12.setVisible(false);
		}
	}// GEN-LAST:event_dbTypeAction

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton2ActionPerformed
	{// GEN-HEADEREND:event_jButton2ActionPerformed
		this.dispose();
	}// GEN-LAST:event_jButton2ActionPerformed

	private void apply(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_apply
		applyChanges();
	}// GEN-LAST:event_apply

	private void applyChanges() {

		setBooleanPref(colorprint, PrefName.COLORPRINT);
		setBooleanPref(pubbox, PrefName.SHOWPUBLIC);
		setBooleanPref(privbox, PrefName.SHOWPRIVATE);
		setBooleanPref(emailbox, PrefName.EMAILENABLED);
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
		setBooleanPref(sharedbox, PrefName.SHARED);
		setBooleanPref(icaltodobox, PrefName.ICALTODOEV);
		setBooleanPref(truncbox, PrefName.TRUNCAPPT);

		Integer i = (Integer) checkfreq.getValue();
		int cur = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
		if (i.intValue() != cur)
			Prefs.putPref(PrefName.REMINDERCHECKMINS, i);

		i = (Integer) syncmins.getValue();
		cur = Prefs.getIntPref(PrefName.SYNCMINS);
		if (i.intValue() != cur)
			Prefs.putPref(PrefName.SYNCMINS, i);

		if (mondaycb.isSelected())
			Prefs.putPref(PrefName.FIRSTDOW, new Integer(Calendar.MONDAY));
		else
			Prefs.putPref(PrefName.FIRSTDOW, new Integer(Calendar.SUNDAY));

		Prefs.putPref(PrefName.WKENDHOUR, wkendhr.getSelectedItem());
		Prefs.putPref(PrefName.WKSTARTHOUR, wkstarthr.getSelectedItem());

		// enable/disable auto-update-check
		// value is the last day-of-year that check was done (1-365)
		// phony value 400 will cause check during current day
		// value -1 is the shut-off value
		if (autoupdate.isSelected())
			Prefs.putPref(PrefName.VERCHKLAST, new Integer(400));
		else
			Prefs.putPref(PrefName.VERCHKLAST, new Integer(-1));

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
		// (bsv 2004-12-20)

		if (!logobox.isSelected()) {
			Prefs.putPref(PrefName.LOGO, "");
			logofile.setText("");
		} else {
			Prefs.putPref(PrefName.LOGO, logofile.getText());
		}

		if (emailbox.isSelected()) {
			Prefs.putPref(PrefName.EMAILSERVER, smtptext.getText());
			Prefs.putPref(PrefName.EMAILADDR, emailtext.getText());
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
				// don't try to change the main window l&f - is doesn't work
				// 100%
				// SwingUtilities.updateComponentTreeUI(cg_);
				Prefs.putPref(PrefName.LNF, newlnf);
			} catch (Exception e) {
				// Errmsg.notice( "Could not find look and feel: " + newlnf );
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

		Prefs.notifyListeners();

	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed

		Font f = NwFontChooserS.showDialog(null, null, null);
		if (f == null)
			return;
		String fs = NwFontChooserS.fontString(f);
		Prefs.putPref(PrefName.DEFFONT, fs);
		NwFontChooserS.setDefaultFont(f);
		SwingUtilities.updateComponentTreeUI(this);
		Prefs.notifyListeners();

	}// GEN-LAST:event_jButton1ActionPerformed

	private void chgdbActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_chgdbActionPerformed
	{// GEN-HEADEREND:event_chgdbActionPerformed
		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Really_change_the_database?"), Resource
				.getResourceString("Confirm_DB_Change"),
				JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			String dbdir = dbDirText.getText();
			Prefs.putPref(PrefName.DBDIR, dbdir);

			if (MySQLButton.isSelected()) {
				Prefs.putPref(PrefName.DBTYPE, "mysql");
			} else if( remoteButton.isSelected()){
				Prefs.putPref(PrefName.DBTYPE, "remote");
			}else {
				Prefs.putPref(PrefName.DBTYPE, "local");
			}

			Prefs.putPref(PrefName.DBNAME, dbNameText.getText());
			Prefs.putPref(PrefName.DBPORT, dbPortText.getText());
			Prefs.putPref(PrefName.DBHOST, dbHostText.getText());
			Prefs.putPref(PrefName.DBUSER, dbUserText.getText());
			Prefs.putPref(PrefName.DBPASS, new String(jPasswordField1
					.getPassword()));
			Prefs.putPref(PrefName.DBURL, remoteURLText.getText());

			if (rl_ != null)
				rl_.restart();

			this.dispose();
		}
	}// GEN-LAST:event_chgdbActionPerformed

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
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

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

			if (err == null)
				break;

			Errmsg.notice(err);
		}

		// update text field - nothing else changes. DB change will take effect
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
				if (i == -1 || i == '\n' || i == '\r')
					break;
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

	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton5ActionPerformed

		// browse for new database dir
		String dbdir = OptionsView.chooseDbDir(false);
		if (dbdir == null)
			return;

		// update text field - nothing else changes. DB change will take effect
		// only on restart
		dbDirText.setText(dbdir);

	}// GEN-LAST:event_jButton5ActionPerformed

	private void incfontActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_incfontActionPerformed

		Font f = NwFontChooserS.showDialog(null, null, null);
		if (f == null)
			return;
		String s = NwFontChooserS.fontString(f);

		Prefs.putPref(PrefName.PREVIEWFONT, s);
		Prefs.notifyListeners();

	}// GEN-LAST:event_incfontActionPerformed

	private void decfontActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_decfontActionPerformed

		Font f = NwFontChooserS.showDialog(null, null, null);
		if (f == null)
			return;
		String s = NwFontChooserS.fontString(f);

		Prefs.putPref(PrefName.APPTFONT, s);
		Prefs.notifyListeners();

	}// GEN-LAST:event_decfontActionPerformed

	private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_exitForm
		this.dispose();
	}// GEN-LAST:event_exitForm

	public void destroy() {
		this.dispose();
	}

	public void refresh() {
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
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return (null);

			dbdir = chooser.getSelectedFile().getAbsolutePath();
			File dir = new File(dbdir);
			String err = null;
			if (!dir.exists()) {
				err = "Database Directory [" + dbdir + "] does not exist";
			} else if (!dir.isDirectory()) {
				err = "Database Directory [" + dbdir + "] is not a directory";
			}

			if (err == null)
				break;

			Errmsg.notice(err);
		}

		if (update)
			Prefs.putPref(PrefName.DBDIR, dbdir);
		return (dbdir);
	}

	private javax.swing.JButton applyButton;

	private javax.swing.JCheckBox autoupdate;

	private javax.swing.JCheckBox backgbox;

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

	private javax.swing.JTextField dbNameText;

	private javax.swing.JTextField dbHostText;

	private javax.swing.JTextField dbDirText;

	private javax.swing.JTextField dbPortText;

	private javax.swing.JTextField dbUserText;

	private javax.swing.JComboBox lnfBox;

	private javax.swing.JComboBox localebox;

	private javax.swing.JCheckBox logobox;

	private javax.swing.JButton logobrowse;

	private javax.swing.JTextField logofile;

	private javax.swing.JCheckBox miltime;

	private javax.swing.JCheckBox mondaycb;

	private javax.swing.JCheckBox popenablebox;

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

	// added by bsv 2004-12-20
	private javax.swing.JCheckBox cb_ucs_on;

	private javax.swing.JCheckBox cb_ucs_ontodo;

	private javax.swing.JCheckBox cb_ucs_marktodo;

	private javax.swing.JTextField tf_ucs_marker;

	private JButtonKnowsBgColor btn_ucs_red;

	private JButtonKnowsBgColor btn_ucs_blue;

	private JButtonKnowsBgColor btn_ucs_green;

	private JButtonKnowsBgColor btn_ucs_black;

	private JButtonKnowsBgColor btn_ucs_white;

	private JButtonKnowsBgColor btn_ucs_tasks;

	private JButtonKnowsBgColor btn_ucs_holidays;

	private JButtonKnowsBgColor btn_ucs_birthdays;

	private JButtonKnowsBgColor btn_ucs_default;

	private JButtonKnowsBgColor btn_ucs_holiday;

	private JButtonKnowsBgColor btn_ucs_vacation;

	private JButtonKnowsBgColor btn_ucs_halfday;

	private JButtonKnowsBgColor btn_ucs_today;

	private JButtonKnowsBgColor btn_ucs_weekend;

	private JButtonKnowsBgColor btn_ucs_weekday;

	private JButton btn_ucs_restore;

	// (added by bsv 2004-12-20)

	private JPanel jPanel = null;

	private JPanel jPanel10 = null;

	private JCheckBox doyBox = null;

	private JCheckBox exputcbox = null;

	private JPanel jPanelUCS = null;

	private JCheckBox palmcb = null;

	private JCheckBox useBeep = null;

	private JLabel lslabel = null;

	private JComboBox lsbox = null;

	private JCheckBox truncbox = null;

	private JLabel syncminlabel = null;

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */

	private JPanel getJPanel4() {
		if (jPanel4 == null) {

			GridBagConstraints gridBagConstraints210 = new GridBagConstraints();
			jPanel4 = new JPanel();
			jPanel4.setLayout(new GridBagLayout());
			gridBagConstraints210.gridx = 0;
			gridBagConstraints210.gridy = 0;
			gridBagConstraints210.weightx = 1.0;
			gridBagConstraints210.weighty = 1.0;
			gridBagConstraints210.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints210.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints210.gridwidth = 1;

			GridBagConstraints gridBagConstraints29 = new java.awt.GridBagConstraints();
			gridBagConstraints29.gridx = 0;
			gridBagConstraints29.gridy = 2;
			gridBagConstraints29.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			gridBagConstraints29.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints29.weightx = 1.0;
			gridBagConstraints29.weighty = 1.0;
			jPanel4.add(jPanel8, gridBagConstraints29);

			GridBagConstraints gridBagConstraints33 = new java.awt.GridBagConstraints();
			gridBagConstraints33.gridx = 0;
			gridBagConstraints33.gridy = 3;
			gridBagConstraints33.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			gridBagConstraints33.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints33.weightx = 1.0;
			gridBagConstraints33.weighty = 1.0;
			jPanel4.add(jPanel9, gridBagConstraints33);

			GridBagConstraints gridBagConstraints100 = new java.awt.GridBagConstraints();
			gridBagConstraints100.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints100.gridy = 0;
			gridBagConstraints100.gridx = 0;
			jPanel4.add(getJPanel11(), gridBagConstraints100);

			GridBagConstraints gridBagConstraints34 = new java.awt.GridBagConstraints();
			gridBagConstraints34.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints34.gridx = 0; // Generated
			gridBagConstraints34.gridy = 5;

			GridBagConstraints gridBagConstraints101 = new java.awt.GridBagConstraints();
			gridBagConstraints101.gridx = 0;
			gridBagConstraints101.gridy = 4;
			gridBagConstraints101.weightx = 1.0;
			gridBagConstraints101.weighty = 1.0;
			gridBagConstraints101.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints101.fill = java.awt.GridBagConstraints.HORIZONTAL;
			jPanel4.add(chgdb, gridBagConstraints34); // Generated
			jPanel4.add(getJPanel12(), gridBagConstraints101); // Generated

		}
		return jPanel4;
	}

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
			gridBagConstraints210.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints210.gridwidth = 1;
			gridBagConstraints510.gridx = 0;
			gridBagConstraints510.gridy = 2;
			gridBagConstraints510.insets = new java.awt.Insets(4, 4, 4, 4);
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
			jPanel10.add(applyButton, null);
			jPanel10.add(jButton2, null);
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
			doyBox.setText(java.util.ResourceBundle.getBundle(
					"resource/borg_resource").getString("showdoy"));
		}
		return doyBox;
	}

	/**
	 * This method initializes exputcbox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getExputcbox() {
		if (exputcbox == null) {
			exputcbox = new JCheckBox();
			exputcbox.setText(java.util.ResourceBundle.getBundle(
					"resource/borg_resource").getString("exputc"));
		}
		return exputcbox;
	}

	/**
	 * This method initializes jPanel11
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelUCS() {
		if (jPanelUCS == null) {
			jPanelUCS = new JPanel();
			jPanelUCS.setLayout(new GridLayout(10, 2));
			// 1st column-2nd column
			// getJPanelUCS().add( new JLabel("to be improved :)") );
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
			jPanelUCS.add(btn_ucs_restore);

		}
		return jPanelUCS;
	}

	/**
	 * This method initializes palmcb
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getPalmcb() {
		if (palmcb == null) {
			palmcb = new JCheckBox();
			palmcb.setText(java.util.ResourceBundle.getBundle(
					"resource/borg_resource").getString("palmopt"));
		}
		return palmcb;
	}

	/**
	 * This method initializes useBeep
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getUseBeep() {
		if (useBeep == null) {
			useBeep = new JCheckBox();
			useBeep.setText(java.util.ResourceBundle.getBundle(
					"resource/borg_resource").getString("Use_system_beep"));
		}
		return useBeep;
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

	/**
	 * This method initializes truncbox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getTruncbox() {
		if (truncbox == null) {
			truncbox = new JCheckBox();
			truncbox.setText(java.util.ResourceBundle.getBundle(
					"resource/borg_resource").getString("truncate_appts"));
		}
		return truncbox;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JSpinner syncmins;

	private JLabel remtimelabel = null;

	private JSpinner getSyncMins() {
		if (syncmins == null) {
			syncmins = new JSpinner();
		}
		return syncmins;
	}

	/**
	 * This method initializes emailtimebox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JSpinner emailtimebox = null;

	private JPanel jPanel11 = null;

	private JRadioButton localFileButton = null;

	private JRadioButton MySQLButton = null;

	private JPanel jPanel12 = null;

	private JLabel jLabel = null;

	private JTextField remoteURLText = null;

	private JRadioButton remoteButton = null;

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

	/**
	 * This method initializes jPanel11
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel11() {
		if (jPanel11 == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.LEFT);  // Generated
			flowLayout.setHgap(40);  // Generated
			jPanel11 = new JPanel();
			jPanel11.setLayout(flowLayout);  // Generated
			jPanel11.add(getLocalFileButton(), null);  // Generated
			jPanel11.add(getMySQLButton(), null);  // Generated
			jPanel11.add(getRemoteButton(), null);  // Generated
		}
		return jPanel11;
	}

	/**
	 * This method initializes jRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getLocalFileButton() {
		if (localFileButton == null) {
			localFileButton = new JRadioButton();
			localFileButton.setActionCommand("local");
			localFileButton.setText(ResourceBundle.getBundle(
					"resource/borg_resource").getString("localFile"));
			localFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							dbTypeAction(e);
						}
					});
		}
		return localFileButton;
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
			MySQLButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeAction(e);
				}
			});
		}
		return MySQLButton;
	}

	/**
	 * This method initializes jPanel12
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel12() {
		if (jPanel12 == null) {
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
			jLabel.setText("URL"); // Generated
			jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT); // Generated
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT); // Generated
			jPanel12 = new JPanel();
			jPanel12.setLayout(new GridBagLayout()); // Generated
			jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, ResourceBundle.getBundle(
					"resource/borg_resource").getString("rem_server_info"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
					null)); // Generated
			jPanel12.add(jLabel, gridBagConstraints); // Generated
			jPanel12.add(getRemoteURLText(), gridBagConstraints54); // Generated
		}
		return jPanel12;
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

	/**
	 * This method initializes remoteButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getRemoteButton() {
		if (remoteButton == null) {
			remoteButton = new JRadioButton();
			remoteButton.setActionCommand("remote"); // Generated
			remoteButton.setText(ResourceBundle.getBundle(
			"resource/borg_resource").getString("rem_server")); // Generated
			remoteButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeAction(e);
				}
			});
		}
		return remoteButton;
	}
} // @jve:decl-index=0:visual-constraint="107,15"
