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
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * Provides the UI for editing Task options
 */
public class TaskOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 1192889023921958906L;
	private JCheckBox calShowSubtaskBox = new JCheckBox();

	private JCheckBox calShowTaskBox = new JCheckBox();
	private JCheckBox taskAbbrevBox = new JCheckBox();
	private JCheckBox taskTreeStatusBox = new JCheckBox();

	private JSpinner redSpinner = new JSpinner(new SpinnerNumberModel());
	private JSpinner orangeSpinner = new JSpinner(new SpinnerNumberModel());
	private JSpinner yellowSpinner = new JSpinner(new SpinnerNumberModel());

	private final ImageIcon redIcon = new ImageIcon(getClass().getResource(
			"/resource/red.png"));
	private final ImageIcon orangeIcon = new ImageIcon(getClass().getResource(
			"/resource/orange.png"));
	private final ImageIcon yellowIcon = new ImageIcon(getClass().getResource(
			"/resource/yellow.png"));
	
	/**
	 * Instantiates a new task options panel.
	 */
	public TaskOptionsPanel() {

		calShowSubtaskBox.setName("calShowSubtaskBox");
		calShowSubtaskBox.setHorizontalAlignment(SwingConstants.LEFT);
		calShowTaskBox.setName("calShowTaskBox");
		calShowTaskBox.setHorizontalAlignment(SwingConstants.LEFT);
		taskAbbrevBox.setName("taskAbbrevBox");
		taskAbbrevBox.setHorizontalAlignment(SwingConstants.LEFT);
		taskTreeStatusBox.setName("taskTreeStatusBox");
		taskTreeStatusBox.setHorizontalAlignment(SwingConstants.LEFT);

		GridBagConstraints gridBagConstraints = GridBagConstraintsFactory
				.create(0, -1, GridBagConstraints.NONE);
		gridBagConstraints.anchor = GridBagConstraints.WEST;

		this.setLayout(new GridBagLayout());
		this.add(taskAbbrevBox, gridBagConstraints);
		this.add(calShowTaskBox, gridBagConstraints);
		this.add(calShowSubtaskBox, gridBagConstraints);
		this.add(taskTreeStatusBox, gridBagConstraints);

		taskAbbrevBox.setText(Resource.getResourceString("task_abbrev"));
		calShowTaskBox.setText(Resource.getResourceString("calShowTask"));
		calShowSubtaskBox.setText(Resource.getResourceString("calShowSubtask"));
		taskTreeStatusBox.setText(Resource
				.getResourceString("show_task_status_in_tree"));

		JPanel spinnerPanel = new JPanel();
		spinnerPanel.setBorder(new TitledBorder(Resource
				.getResourceString("DaysLeftColors")));
		spinnerPanel.setLayout(new GridLayout(4, 2));

		spinnerPanel.add(new JLabel(redIcon));
		spinnerPanel.add(redSpinner);
		spinnerPanel.add(new JLabel(orangeIcon));
		spinnerPanel.add(orangeSpinner);
		spinnerPanel.add(new JLabel(yellowIcon));
		spinnerPanel.add(yellowSpinner);

		this.add(spinnerPanel, GridBagConstraintsFactory
				.create(0, -1, GridBagConstraints.HORIZONTAL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		OptionsPanel.setBooleanPref(taskAbbrevBox, PrefName.TASK_SHOW_ABBREV);
		OptionsPanel.setBooleanPref(calShowTaskBox, PrefName.CAL_SHOW_TASKS);
		OptionsPanel.setBooleanPref(calShowSubtaskBox,
				PrefName.CAL_SHOW_SUBTASKS);
		OptionsPanel.setBooleanPref(taskTreeStatusBox,
				PrefName.TASK_TREE_SHOW_STATUS);
		
		Prefs.putPref(PrefName.RED_DAYS, redSpinner.getValue());
		Prefs.putPref(PrefName.ORANGE_DAYS, orangeSpinner.getValue());
		Prefs.putPref(PrefName.YELLOW_DAYS, yellowSpinner.getValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {

		OptionsPanel.setCheckBox(taskAbbrevBox, PrefName.TASK_SHOW_ABBREV);
		OptionsPanel.setCheckBox(calShowTaskBox, PrefName.CAL_SHOW_TASKS);
		OptionsPanel.setCheckBox(calShowSubtaskBox, PrefName.CAL_SHOW_SUBTASKS);
		OptionsPanel.setCheckBox(taskTreeStatusBox,
				PrefName.TASK_TREE_SHOW_STATUS);
		
		redSpinner.setValue(Prefs.getIntPref(PrefName.RED_DAYS));
		orangeSpinner.setValue(Prefs.getIntPref(PrefName.ORANGE_DAYS));
		yellowSpinner.setValue(Prefs.getIntPref(PrefName.YELLOW_DAYS));
	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("taskOptions");
	}
}
