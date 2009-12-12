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

import java.awt.GridBagConstraints;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * The Class PopupOptionsPanel provies the options tab for editing popup reminder options
 */
class PopupOptionsPanel extends OptionsPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5238561005974597879L;

	/** The checkfreq. */
	private JSpinner checkfreq = new JSpinner();
	
	/** The popenablebox. */
	private JCheckBox popenablebox = new JCheckBox();

	
	/** The soundbox. */
	private JCheckBox soundbox = new JCheckBox();
	
	/** The use beep. */
	private JCheckBox useBeep = new JCheckBox();
	
	/** The number Of Reminder Times. */
	private int numberOfReminderTimes = 0;

	/** The spinners for setting the reminder times */
	private JSpinner spinners[];

	/**
	 * Instantiates a new popup options panel.
	 */
	public PopupOptionsPanel() {
		
		this.setLayout(new java.awt.GridBagLayout());
		
		numberOfReminderTimes = ReminderTimes.getNum();
		
		
		
		ResourceHelper.setText(popenablebox, "enable_popups");
		this.add(popenablebox, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH));

		ResourceHelper.setText(soundbox, "beeps");
		this.add(soundbox, GridBagConstraintsFactory.create(0, 3,
				GridBagConstraints.BOTH));

		JLabel jLabel15 = new JLabel();
		jLabel15.setHorizontalAlignment(SwingConstants.TRAILING);
		ResourceHelper.setText(jLabel15, "min_between_chks");

		this.add(jLabel15, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH));

		checkfreq.setMinimumSize(new java.awt.Dimension(50, 20));
		this.add(checkfreq, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		GridBagConstraints gridBagConstraints65 = GridBagConstraintsFactory
				.create(0, 2, GridBagConstraints.BOTH);
		gridBagConstraints65.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(new JSeparator(), gridBagConstraints65);

		JLabel jLabel16 = new JLabel();
		ResourceHelper.setText(jLabel16, "restart_req");
		this.add(jLabel16, GridBagConstraintsFactory.create(2, 1,
				GridBagConstraints.BOTH));

		useBeep = new JCheckBox();
		ResourceHelper.setText(useBeep, "Use_system_beep");
		this.add(useBeep, GridBagConstraintsFactory.create(0, 4));

		GridBagConstraints gridBagConstraints113 = GridBagConstraintsFactory
				.create(0, 5);
		gridBagConstraints113.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints113.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints113.insets = new java.awt.Insets(18, 18, 18, 18);
		
		JPanel remTimePanel = new JPanel();
		
		// border
		String title = Resource.getResourceString("Popup_Times") + " ("
				+ Resource.getResourceString("Minutes") + ")";
		Border b = BorderFactory.createTitledBorder(remTimePanel.getBorder(), title);
		remTimePanel.setBorder(b);
		
		remTimePanel.setLayout(new GridLayout(2, 0));
		
		// add the spinners
		spinners = new JSpinner[numberOfReminderTimes];
		for (int i = 0; i < numberOfReminderTimes; i++) {
			spinners[i] = new JSpinner(new SpinnerNumberModel(0,-99999,99999,1));
			remTimePanel.add(spinners[i]);
		}
		
		this.add(remTimePanel, gridBagConstraints113);

	}

	/* (non-Javadoc)
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		OptionsPanel.setBooleanPref(popenablebox, PrefName.REMINDERS);
		OptionsPanel.setBooleanPref(soundbox, PrefName.BEEPINGREMINDERS);
		OptionsPanel.setBooleanPref(useBeep, PrefName.USESYSTEMBEEP);
		Integer checkMins = (Integer) checkfreq.getValue();
		int cur = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
		if (checkMins.intValue() != cur) {
			// why does this not save a new pref if the value is the same?
			// I no longer remeber if this matters - will leave as is
			Prefs.putPref(PrefName.REMINDERCHECKMINS, checkMins);
		}

		int arr[] = new int[numberOfReminderTimes];
		for (int i = 0; i < numberOfReminderTimes; i++) {
			Integer ii = (Integer) spinners[i].getValue();
			arr[i] = ii.intValue();
		}
		ReminderTimes.setTimes(arr);
		loadTimes();
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {
		OptionsPanel.setCheckBox(popenablebox, PrefName.REMINDERS);
		OptionsPanel.setCheckBox(soundbox, PrefName.BEEPINGREMINDERS);
		OptionsPanel.setCheckBox(useBeep, PrefName.USESYSTEMBEEP);

		int mins = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
		checkfreq.setValue(new Integer(mins));
		
		// load the times
		loadTimes();

	}
	
	/**
	 * Load the spinner valies from stored prefs
	 */
	private void loadTimes() {
		for (int i = 0; i < numberOfReminderTimes; i++) {
			spinners[i].setValue(new Integer(ReminderTimes.getTimes(i)));
		}
	}


}
