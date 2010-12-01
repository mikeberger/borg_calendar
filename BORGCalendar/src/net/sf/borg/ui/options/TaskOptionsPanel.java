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

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import net.sf.borg.common.PrefName;
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
		taskTreeStatusBox.setText(Resource.getResourceString("show_task_status_in_tree"));

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
		OptionsPanel.setBooleanPref(taskTreeStatusBox, PrefName.TASK_TREE_SHOW_STATUS);

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
		OptionsPanel.setCheckBox(taskTreeStatusBox, PrefName.TASK_TREE_SHOW_STATUS);

	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("taskOptions");
	}
}
