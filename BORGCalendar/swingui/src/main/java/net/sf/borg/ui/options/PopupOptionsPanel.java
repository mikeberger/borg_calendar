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

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.popup.ReminderListManager;
import net.sf.borg.ui.popup.ReminderManager;
import net.sf.borg.ui.popup.ReminderPopupManager;
import net.sf.borg.ui.popup.ReminderSound;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * The Class PopupOptionsPanel provies the options tab for editing popup
 * reminder options
 */
class PopupOptionsPanel extends OptionsPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5238561005974597879L;

	/** The checkfreq. */
	private final JSpinner checkfreq = new JSpinner();

	/** The number Of Reminder Times. */
	private int numberOfReminderTimes = 0;

	/** The popenablebox. */
	private final JCheckBox popenablebox = new JCheckBox();

	private final JCheckBox reminderListBox = new JCheckBox();

	private final JCheckBox taskReminderBox = new JCheckBox();
	
	//private final JCheckBox taskbarBox = new JCheckBox();

	/** The soundbox. */
	private final JComboBox<String> soundbox = new JComboBox<String>();

	/** The spinners for setting the reminder times */
	private final JSpinner[] spinners;

	private final JSpinner birthdayDays = new JSpinner();

	/**
	 * Instantiates a new popup options panel.
	 */
	public PopupOptionsPanel() {

		this.setLayout(new java.awt.GridBagLayout());

		numberOfReminderTimes = ReminderTimes.getNum();

		ResourceHelper.setText(popenablebox, "enable_popups");
		this.add(popenablebox,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		JLabel jLabel15 = new JLabel();
		jLabel15.setText(Resource.getResourceString("todo_reminder_freq"));

		this.add(jLabel15,
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

		checkfreq.setMinimumSize(new java.awt.Dimension(50, 20));
		this.add(checkfreq, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		/*
		 * sound
		 */
		JPanel soundPanel = new JPanel();
		soundPanel.setLayout(new GridBagLayout());
		soundPanel
				.add(new JLabel(Resource.getResourceString("beep_options")),
						GridBagConstraintsFactory.create(0, 0,
								GridBagConstraints.BOTH));

		soundbox.setEditable(true);
		soundbox.addItem(Resource.getResourceString("default"));
		soundbox.addItem(Resource.getResourceString("no_sound"));
		soundbox.addItem(Resource.getResourceString("Use_system_beep"));
		soundbox.addItem(Resource.getResourceString("Browse") + "...");
		soundbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (soundbox.getSelectedIndex() == 3) {
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new File("."));
					chooser.setDialogTitle(Resource
							.getResourceString("choose_file"));
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

					int returnVal = chooser.showOpenDialog(null);
					if (returnVal != JFileChooser.APPROVE_OPTION)
						return;

					String fileName = chooser.getSelectedFile()
							.getAbsolutePath();
					soundbox.setSelectedItem(fileName);
				}
			}

		});
		soundPanel
				.add(soundbox, GridBagConstraintsFactory.create(1, 0,
						GridBagConstraints.BOTH));

		JButton play = new JButton();
		play.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Forward16.gif")));
		play.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ReminderSound.playReminderSound(getSoundOption());
			}

		});
		soundPanel
				.add(play, GridBagConstraintsFactory.create(2, 0,
						GridBagConstraints.BOTH));

		GridBagConstraints sPanelGBC = GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.VERTICAL);
		sPanelGBC.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		sPanelGBC.anchor = java.awt.GridBagConstraints.WEST;
		sPanelGBC.insets = new Insets(0, 0, 0, 0);

		this.add(soundPanel, sPanelGBC);

		JPanel remTimePanel = new JPanel();

		// border
		String title = Resource.getResourceString("Popup_Times") + " ("
				+ Resource.getResourceString("Minutes") + ")";
		Border b = BorderFactory.createTitledBorder(remTimePanel.getBorder(),
				title);
		remTimePanel.setBorder(b);

		remTimePanel.setLayout(new GridLayout(4, 0));

		// add the spinners
		spinners = new JSpinner[numberOfReminderTimes];
		for (int i = 0; i < numberOfReminderTimes; i++) {
			spinners[i] = new JSpinner(new SpinnerNumberModel(0, -99999, 99999,
					1));
			remTimePanel.add(spinners[i]);
		}

		GridBagConstraints remPanelGBC = GridBagConstraintsFactory.create(0, 3,
				GridBagConstraints.BOTH);
		remPanelGBC.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		remPanelGBC.anchor = java.awt.GridBagConstraints.WEST;

		this.add(remTimePanel, remPanelGBC);

		reminderListBox.setText(Resource.getResourceString("reminder_list"));
		this.add(reminderListBox,
				GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));

		taskReminderBox.setText(Resource
				.getResourceString("show_task_reminders"));
		this.add(taskReminderBox,
				GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH));
		
		//taskbarBox.setText(Resource
		//		.getResourceString("show_taskbar_reminders"));
		//this.add(taskbarBox,
		//		GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH));
		

		this.add(new JLabel(Resource.getResourceString("bd_rem_days")),
				GridBagConstraintsFactory.create(0, 7, GridBagConstraints.BOTH));

		birthdayDays.setMinimumSize(new java.awt.Dimension(50, 20));
		this.add(birthdayDays, GridBagConstraintsFactory.create(1, 7,
				GridBagConstraints.BOTH, 1.0, 0.0));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		OptionsPanel.setBooleanPref(popenablebox, PrefName.REMINDERS);
		OptionsPanel.setBooleanPref(taskReminderBox, PrefName.TASKREMINDERS);
		//OptionsPanel.setBooleanPref(taskbarBox, PrefName.TASKBAR_REMINDERS);
		Prefs.putPref(PrefName.BEEPINGREMINDERS, getSoundOption());

		Integer checkMins = (Integer) checkfreq.getValue();
		int cur = Prefs.getIntPref(PrefName.TODOREMINDERMINS);
		if (checkMins.intValue() != cur) {
			// why does this not save a new pref if the value is the same?
			// I no longer remeber if this matters - will leave as is
			Prefs.putPref(PrefName.TODOREMINDERMINS, checkMins);
		}

		Integer bddays = (Integer) birthdayDays.getValue();
		Prefs.putPref(PrefName.BIRTHDAYREMINDERDAYS, bddays);

		int[] arr = new int[numberOfReminderTimes];
		for (int i = 0; i < numberOfReminderTimes; i++) {
			Integer ii = (Integer) spinners[i].getValue();
			arr[i] = ii.intValue();
		}
		ReminderTimes.setTimes(arr);
		loadTimes();

		// restart reminder popups if the type of UI has changed
		boolean remlist = Prefs.getBoolPref(PrefName.REMINDERLIST);
		boolean newremlist = reminderListBox.isSelected();
		OptionsPanel.setBooleanPref(reminderListBox, PrefName.REMINDERLIST);

		if (remlist != newremlist) {
			// stop existing reminder UI
			ReminderManager.getReminderManager().remove();

			// start new one
			if (newremlist == true) {
				ReminderListManager.getReference();
			} else {
				ReminderPopupManager.getReference();
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
		OptionsPanel.setCheckBox(popenablebox, PrefName.REMINDERS);
		OptionsPanel.setCheckBox(reminderListBox, PrefName.REMINDERLIST);
		OptionsPanel.setCheckBox(taskReminderBox, PrefName.TASKREMINDERS);
		//OptionsPanel.setCheckBox(taskbarBox, PrefName.TASKBAR_REMINDERS);

		String beep = Prefs.getPref(PrefName.BEEPINGREMINDERS);
		if (beep.equals("true")) {
			soundbox.setSelectedIndex(0);
		} else if (beep.equals("false")) {
			soundbox.setSelectedIndex(1);
		} else if (beep.equals("system-beep")) {
			soundbox.setSelectedIndex(2);
		} else {
			soundbox.setSelectedItem(beep);
		}

		int mins = Prefs.getIntPref(PrefName.TODOREMINDERMINS);
		checkfreq.setValue(Integer.valueOf(mins));

		int bddays = Prefs.getIntPref(PrefName.BIRTHDAYREMINDERDAYS);
		birthdayDays.setValue(Integer.valueOf(bddays));

		// load the times
		loadTimes();

	}

	/**
	 * Load the spinner valies from stored prefs
	 */
	private void loadTimes() {
		for (int i = 0; i < numberOfReminderTimes; i++) {
			spinners[i].setValue(Integer.valueOf(ReminderTimes.getTimes(i)));
		}
	}

	/**
	 * get the sound option preference value based on the current sound combo
	 * box setting
	 * 
	 * @return
	 */
	private String getSoundOption() {
		String option = "";
		int i = soundbox.getSelectedIndex();
		if (i == 0) {
			option = "true";
		} else if (i == 1) {
			option = "false";
		} else if (i == 2) {
			option = "system-beep";
		} else {
			option = (String) soundbox.getSelectedItem();
		}
		return option;
	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("popup_reminders");
	}
}
