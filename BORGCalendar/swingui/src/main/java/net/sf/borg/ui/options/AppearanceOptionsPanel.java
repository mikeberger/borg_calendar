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

 Copyright 2003-2010 by Mike Berger
 */
package net.sf.borg.ui.options;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes.FlatIJLookAndFeelInfo;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * Provides the UI for editing general appearance options
 */
public class AppearanceOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = -2799946173831477902L;

	private final JCheckBox canadabox;

	private final JCheckBox prioritySortBox;

	private JCheckBox doyBox = null;

	private final JCheckBox holiday1;
	private final JCheckBox iso8601Box = new JCheckBox();

	private final JComboBox<String> lnfBox;
	private final JComboBox<String> themeBox;
	private final JComboBox<String> localebox;

	private final JCheckBox miltime;
	private final JCheckBox mondaycb;

	private JCheckBox truncbox = null;
	private final JComboBox<String> wkendhr;

	private final JComboBox<String> wkstarthr;

	/**
	 * Instantiates a new appearance options panel.
	 */
	public AppearanceOptionsPanel() {

		
		lnfBox = new JComboBox<String>();
		themeBox = new JComboBox<String>();
		holiday1 = new JCheckBox();
		mondaycb = new JCheckBox();
		miltime = new JCheckBox();
		wkstarthr = new JComboBox<String>();
		wkendhr = new JComboBox<String>();
		canadabox = new JCheckBox();
		localebox = new JComboBox<String>();
		prioritySortBox = new JCheckBox();

		this.setLayout(new java.awt.GridBagLayout());

		this.setName(Resource.getResourceString("appearance"));
		
		JLabel jLabel4 = new JLabel();
		ResourceHelper.setText(jLabel4, "Look_and_Feel:");
		jLabel4.setLabelFor(lnfBox);
		this.add(jLabel4,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		lnfBox.setEditable(false);
		lnfBox.setMaximumSize(new java.awt.Dimension(131, 24));
		lnfBox.setPreferredSize(new java.awt.Dimension(50, 24));
		lnfBox.setAutoscrolls(true);
		this.add(lnfBox, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel themeLabel = new JLabel();
		ResourceHelper.setText(themeLabel, "JGoodies_Theme");
		jLabel4.setLabelFor(themeBox);
		this.add(themeLabel,
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

		themeBox.setEditable(true);
		themeBox.setMaximumSize(new java.awt.Dimension(131, 24));
		themeBox.setPreferredSize(new java.awt.Dimension(50, 24));
		themeBox.setAutoscrolls(true);
		this.add(themeBox, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		String[] themes = { "BrownSugar", "DarkStar", "DesertBlue",
				"DesertBluer", "DesertGreen", "DesertRed", "DesertYellow",
				"ExperienceBlue", "ExperienceGreen", "ExperienceRoyale",
				"LightGray", "Silver", "SkyBlue", "SkyBluer", "SkyGreen",
				"SkyKrupp", "SkyPink", "SkyRed", "SkyYellow" };

		// add to the combo box
		for (String theme : themes) {
			themeBox.addItem(theme);
		}

		ResourceHelper.setText(holiday1, "Show_U.S._Holidays");
		this.add(holiday1,
				GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));

		ResourceHelper.setText(mondaycb, "Week_Starts_with_Monday");
		this.add(mondaycb,
				GridBagConstraintsFactory.create(1, 5, GridBagConstraints.BOTH));

		ResourceHelper.setText(miltime, "Use_24_hour_time_format");
		this.add(miltime,
				GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH));

		JLabel jLabel5 = new JLabel();
		ResourceHelper.setText(jLabel5, "Week_View_Start_Hour:_");
		jLabel5.setLabelFor(wkstarthr);
		wkstarthr.setModel(new DefaultComboBoxModel<String>(new String[] { "0", "1",
				"2", "3", "4", "5", "6", "7", "8", "9", "10", "11" }));
		this.add(jLabel5,
				GridBagConstraintsFactory.create(0, 7, GridBagConstraints.BOTH));

		wkendhr.setModel(new DefaultComboBoxModel<String>(new String[] { "12", "13",
				"14", "15", "16", "17", "18", "19", "20", "21", "22", "23",
				"24" }));
		this.add(wkstarthr,
				GridBagConstraintsFactory.create(1, 7, GridBagConstraints.BOTH));

		JLabel jLabel6 = new JLabel();
		ResourceHelper.setText(jLabel6, "Week_View_End_Hour:_");
		jLabel6.setLabelFor(wkendhr);
		this.add(wkendhr,
				GridBagConstraintsFactory.create(1, 8, GridBagConstraints.BOTH));
		this.add(jLabel6,
				GridBagConstraintsFactory.create(0, 8, GridBagConstraints.BOTH));

		ResourceHelper.setText(canadabox, "Show_Canadian_Holidays");
		this.add(canadabox,
				GridBagConstraintsFactory.create(1, 4, GridBagConstraints.BOTH));

		JLabel jLabel8 = new JLabel();
		ResourceHelper.setText(jLabel8, "locale");
		jLabel8.setLabelFor(localebox);
		this.add(jLabel8, GridBagConstraintsFactory.create(0, 12,
				GridBagConstraints.BOTH));

		this.add(localebox, GridBagConstraintsFactory.create(1, 12,
				GridBagConstraints.BOTH));

	

		ResourceHelper.setText(iso8601Box, "ISO_week_number");
		this.add(iso8601Box, GridBagConstraintsFactory.create(0, 9,
				GridBagConstraints.BOTH, 0.0, 0.0));

		ResourceHelper.setText(prioritySortBox, "sort_by_priority");
		this.add(prioritySortBox,
				GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH));

		doyBox = new JCheckBox();
		ResourceHelper.setText(doyBox, "showdoy");
		this.add(doyBox,
				GridBagConstraintsFactory.create(1, 6, GridBagConstraints.BOTH));

		truncbox = new JCheckBox();
		ResourceHelper.setText(truncbox, "truncate_appts");
		this.add(truncbox,
				GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));

		lnfBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				lnfChanged();
			}

		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {

	
		OptionsPanel.setBooleanPref(holiday1, PrefName.SHOWUSHOLIDAYS);
		OptionsPanel.setBooleanPref(canadabox, PrefName.SHOWCANHOLIDAYS);
		OptionsPanel.setBooleanPref(doyBox, PrefName.DAYOFYEAR);
		OptionsPanel.setBooleanPref(prioritySortBox, PrefName.PRIORITY_SORT);
		OptionsPanel.setBooleanPref(miltime, PrefName.MILTIME);
		OptionsPanel.setBooleanPref(truncbox, PrefName.TRUNCAPPT);
		OptionsPanel.setBooleanPref(iso8601Box, PrefName.ISOWKNUMBER);
		

		// first day of week - either monday or sunday
		if (mondaycb.isSelected()) {
			Prefs.putPref(PrefName.FIRSTDOW, Integer.valueOf(Calendar.MONDAY));
		} else {
			Prefs.putPref(PrefName.FIRSTDOW, Integer.valueOf(Calendar.SUNDAY));
		}

		Prefs.putPref(PrefName.WKENDHOUR, wkendhr.getSelectedItem());
		Prefs.putPref(PrefName.WKSTARTHOUR, wkstarthr.getSelectedItem());

		// locale
		Locale[] locs = Locale.getAvailableLocales();
		String choice = (String) localebox.getSelectedItem();
		for (int ii = 0; ii < locs.length; ii++) {
			if (choice.equals(locs[ii].getDisplayName())) {
				Prefs.putPref(PrefName.COUNTRY, locs[ii].getCountry());
				Prefs.putPref(PrefName.LANGUAGE, locs[ii].getLanguage());
			}
		}

		// look and feel
		// we no longer restart automatically or attempt to update the lnf
		// while running
		boolean lnfChange = false;
		String newlnf = (String) lnfBox.getSelectedItem();
		String oldlnf = Prefs.getPref(PrefName.LNF);
		if (!newlnf.equals(oldlnf)) {
			lnfChange = true;
			Prefs.putPref(PrefName.LNF, newlnf);
		}

		String newtheme = (String) themeBox.getSelectedItem();
		String oldtheme = Prefs.getPref(PrefName.GOODIESTHEME);
		if (!newtheme.equals(oldtheme)) {
			lnfChange = true;
			Prefs.putPref(PrefName.GOODIESTHEME, newtheme);
		}

		if (lnfChange) {
			try {
			UIManager.setLookAndFeel(newlnf);
			FlatLaf.updateUI();
			}
			catch( Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
			
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {
		
		OptionsPanel.setCheckBox(holiday1, PrefName.SHOWUSHOLIDAYS);
		OptionsPanel.setCheckBox(canadabox, PrefName.SHOWCANHOLIDAYS);
		OptionsPanel.setCheckBox(doyBox, PrefName.DAYOFYEAR);
		OptionsPanel.setCheckBox(prioritySortBox, PrefName.PRIORITY_SORT);
		OptionsPanel.setCheckBox(miltime, PrefName.MILTIME);

		OptionsPanel.setCheckBox(truncbox, PrefName.TRUNCAPPT);
		OptionsPanel.setCheckBox(iso8601Box, PrefName.ISOWKNUMBER);

		// monday first day of week option
		int fdow = Prefs.getIntPref(PrefName.FIRSTDOW);
		mondaycb.setSelected(fdow == Calendar.MONDAY);

		// start and end hour for the time grids
		String shr = Prefs.getPref(PrefName.WKSTARTHOUR);
		String ehr = Prefs.getPref(PrefName.WKENDHOUR);
		wkstarthr.setSelectedItem(shr);
		wkendhr.setSelectedItem(ehr);

		// add locales
		localebox.removeAllItems();
		Locale[] locs = Locale.getAvailableLocales();
		ArrayList<Locale> loclist = new ArrayList<Locale>();
		Collections.addAll(loclist, locs);
		Comparator<Locale> comparator = new Comparator<Locale>() {

	        @Override
	        public int compare(Locale o1, Locale o2) {
	            return o1.getDisplayName().compareTo(o2.getDisplayName());
	        }
	    };
		Collections.sort(loclist, comparator);
		for (Locale locale : loclist) {
			localebox.addItem(locale.getDisplayName());
		}

		String currentlocale = Locale.getDefault().getDisplayName();
		localebox.setSelectedItem(currentlocale);

		// add installed look and feels to lnfBox
		lnfBox.removeAllItems();
		Collection<String> lnfs = new TreeSet<String>();
		String curlnf = Prefs.getPref(PrefName.LNF);

		// add installed JRE look and feels
		LookAndFeelInfo[] lnfinfo = UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < lnfinfo.length; i++) {
			String name = lnfinfo[i].getClassName();
			lnfs.add(name);
		}

		
		// add the included jgoodies look and feels
		String[] looks = { "com.jgoodies.looks.plastic.PlasticLookAndFeel",
				"com.jgoodies.looks.windows.WindowsLookAndFeel",
				"com.jgoodies.looks.plastic.PlasticXPLookAndFeel",
				"com.jgoodies.looks.plastic.Plastic3DLookAndFeel",
				"com.formdev.flatlaf.FlatLightLaf",
				"com.formdev.flatlaf.FlatDarkLaf"
				};

		for (String look : looks) {
			try {
				Class.forName(look);
				lnfs.add(look);
			} catch (Throwable e) {
				// empty
			}
		}
		
		for( FlatIJLookAndFeelInfo ijInfo : FlatAllIJThemes.INFOS )
		{
			lnfs.add(ijInfo.getClassName());
		}

		// add the look and feel in the preference store
		lnfs.add(curlnf);

		// add to the combo box
		for (String lnf : lnfs) {
			lnfBox.addItem(lnf);
		}

		lnfBox.setSelectedItem(curlnf);
		lnfBox.setEditable(false);

		String curtheme = Prefs.getPref(PrefName.GOODIESTHEME);
		themeBox.setSelectedItem(curtheme);
		themeBox.setEditable(false);

	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("appearance");
	}

	private void lnfChanged() {
		String lnf = (String) lnfBox.getSelectedItem();
		if (lnf == null)
			return;

		themeBox.setEnabled(lnf.contains("goodies"));
	}

}
