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
package net.sf.borg.plugin.ical;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Prefs;
import net.sf.borg.plugin.common.Resource;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

public class IcalOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 795364188303457966L;

	private JTextField port = new JTextField();
	private JSpinner exportyears = new JSpinner(new SpinnerNumberModel(2, 1,
			100, 1));

	public IcalOptionsPanel() {
		this.setLayout(new java.awt.GridBagLayout());

		this.add(new JLabel(Resource.getResourceString("years_to_export")),
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		this.add(exportyears,
				GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH));

		this.add(new JLabel(Resource.getResourceString("server_port")),
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
		this.add(port, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

	}

	
	@Override
	public void applyChanges() {

		// validate that port is a number
		try {
			int socket = Integer.parseInt(port.getText());
			Prefs.putPref(IcalModule.PORT, new Integer(socket));
		} catch (NumberFormatException e) {
			Errmsg.getErrorHandler().notice(
					Resource.getResourceString("port_warning"));
			;
			port.setText(((Integer)IcalModule.PORT.getDefault()).toString());
			Prefs.putPref(IcalModule.PORT, IcalModule.PORT.getDefault());
			return;
		}
		
		Prefs.putPref(IcalModule.EXPORTYEARS, exportyears.getValue());
		
	}

	
	@Override
	public void loadOptions() {

		int p = Prefs.getIntPref(IcalModule.PORT);
		port.setText(Integer.toString(p));
		
		exportyears.setValue(Prefs.getIntPref(IcalModule.EXPORTYEARS));


	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("ical_options");
	}

	

}
