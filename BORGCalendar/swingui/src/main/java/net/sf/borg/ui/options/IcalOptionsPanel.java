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

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

public class IcalOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 795364188303457966L;

	private final JSpinner exportyears = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));

	private final JCheckBox todoBox = new JCheckBox();

	

	public IcalOptionsPanel() {
		this.setLayout(new java.awt.GridBagLayout());

		

		this.add(new JLabel(Resource.getResourceString("years_to_export")),
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		this.add(exportyears, GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH));

		todoBox.setText(Resource.getResourceString("ical_export_todos"));
		this.add(todoBox, GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH, 1.0, 0.0));

		

	}

	@Override
	public void applyChanges() {

		Prefs.putPref(PrefName.ICAL_EXPORTYEARS, exportyears.getValue());

		OptionsPanel.setBooleanPref(todoBox, PrefName.ICAL_EXPORT_TODO);

		
	}

	@Override
	public void loadOptions() {

		exportyears.setValue(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS));

		todoBox.setSelected(Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO));

	
	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("ical_options");
	}

	static public void main(String[] args) {
		JFrame jf = new JFrame();
		jf.setContentPane(new IcalOptionsPanel());
		jf.setVisible(true);
	}

}
