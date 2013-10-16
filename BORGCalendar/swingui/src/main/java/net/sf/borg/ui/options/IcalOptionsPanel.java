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
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.ical.IcalFTP;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

public class IcalOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 795364188303457966L;

	private JTextField port = new JTextField();
	private JSpinner exportyears = new JSpinner(new SpinnerNumberModel(2, 1,
			100, 1));
	private JCheckBox skipBox = new JCheckBox();
	private JTextField ftpserver = new JTextField();
	private JTextField ftppath = new JTextField();
	private JTextField ftpusername = new JTextField();
	private JPasswordField ftppassword = new JPasswordField();
	private JTextField importurl = new JTextField();

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
		
		skipBox.setText(Resource.getResourceString("skip_borg_ical"));
		this.add(skipBox,GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		this.add(new JLabel(Resource.getResourceString("ftpserver")),
				GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));
		this.add(ftpserver, GridBagConstraintsFactory.create(1, 3,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		this.add(new JLabel(Resource.getResourceString("ftppath")),
				GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));
		this.add(ftppath, GridBagConstraintsFactory.create(1, 4,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		this.add(new JLabel(Resource.getResourceString("ftpusername")),
				GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH));
		this.add(ftpusername, GridBagConstraintsFactory.create(1, 5,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		JLabel pl = new JLabel(Resource.getResourceString("ftppassword"));
		this.add(pl,
				GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH));
		pl.setLabelFor(ftppassword);
		this.add(ftppassword, GridBagConstraintsFactory.create(1, 6,
				GridBagConstraints.BOTH, 1.0, 0.0));
		ftppassword.setEditable(true);
		
		this.add(new JLabel(Resource.getResourceString("ical_import_url")),
				GridBagConstraintsFactory.create(0, 7, GridBagConstraints.BOTH));
		this.add(importurl, GridBagConstraintsFactory.create(1, 7,
				GridBagConstraints.BOTH, 1.0, 0.0));

	}

	@Override
	public void applyChanges() {

		// validate that port is a number
		try {
			int socket = Integer.parseInt(port.getText());
			Prefs.putPref(PrefName.ICAL_PORT, new Integer(socket));
		} catch (NumberFormatException e) {
			Errmsg.getErrorHandler().notice(
					Resource.getResourceString("port_warning"));
			;
			port.setText(((Integer) PrefName.ICAL_PORT.getDefault()).toString());
			Prefs.putPref(PrefName.ICAL_PORT, PrefName.ICAL_PORT.getDefault());
			return;
		}

		Prefs.putPref(PrefName.FTPUSER, ftpusername.getText());
		Prefs.putPref(PrefName.FTPSERVER, ftpserver.getText());
		Prefs.putPref(PrefName.FTPPATH, ftppath.getText());
		try {
			IcalFTP.sep(new String(ftppassword.getPassword()));
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

		Prefs.putPref(PrefName.ICAL_EXPORTYEARS, exportyears.getValue());

		OptionsPanel.setBooleanPref(skipBox, PrefName.SKIP_BORG);

		Prefs.putPref(PrefName.ICAL_IMPORT_URL, importurl.getText());

	}

	@Override
	public void loadOptions() {

		int p = Prefs.getIntPref(PrefName.ICAL_PORT);
		port.setText(Integer.toString(p));

		exportyears.setValue(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS));

		skipBox.setSelected(Prefs.getBoolPref(PrefName.SKIP_BORG));

		ftpusername.setText(Prefs.getPref(PrefName.FTPUSER));
		ftpserver.setText(Prefs.getPref(PrefName.FTPSERVER));
		ftppath.setText(Prefs.getPref(PrefName.FTPPATH));

		try {
			ftppassword.setText(IcalFTP.gep());
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
		
		importurl.setText(Prefs.getPref(PrefName.ICAL_IMPORT_URL));

	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("ical_options");
	}

	
}
