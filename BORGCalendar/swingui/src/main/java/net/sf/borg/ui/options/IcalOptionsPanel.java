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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.ical.CalDav;
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
	private JCheckBox todoBox = new JCheckBox();

	private JTextField caldavServer = new JTextField();
	private JTextField caldavPath = new JTextField();
	private JTextField caldavPrincipalPath = new JTextField();
	private JTextField caldavUserPath = new JTextField();
	private JTextField caldavUser = new JTextField();
	private JPasswordField caldavPassword = new JPasswordField();
	private JTextField caldavCal = new JTextField();

	private JCheckBox caldavSSL = new JCheckBox();
	private JCheckBox caldavSelfSigned = new JCheckBox();

	public IcalOptionsPanel() {
		this.setLayout(new java.awt.GridBagLayout());

		JPanel ftppanel = new JPanel();
		ftppanel.setBorder(new TitledBorder("FTP"));
		ftppanel.setLayout(new GridBagLayout());

		JPanel calpanel = new JPanel();
		calpanel.setBorder(new TitledBorder("CALDAV"));
		calpanel.setLayout(new GridBagLayout());

		this.add(new JLabel(Resource.getResourceString("years_to_export")),
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		this.add(exportyears,
				GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH));

		this.add(new JLabel(Resource.getResourceString("server_port")),
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
		this.add(port, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		skipBox.setText(Resource.getResourceString("skip_borg_ical"));
		this.add(skipBox, GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.BOTH, 1.0, 0.0));

		this.add(new JLabel(Resource.getResourceString("ical_import_url")),
				GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));
		this.add(importurl, GridBagConstraintsFactory.create(1, 3,
				GridBagConstraints.BOTH, 1.0, 0.0));

		todoBox.setText(Resource.getResourceString("ical_export_todos"));
		this.add(todoBox, GridBagConstraintsFactory.create(0, 4,
				GridBagConstraints.BOTH, 1.0, 0.0));

		ftppanel.add(new JLabel(Resource.getResourceString("ftpserver")),
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		ftppanel.add(ftpserver, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		ftppanel.add(new JLabel(Resource.getResourceString("ftppath")),
				GridBagConstraintsFactory.create(2, 0, GridBagConstraints.BOTH));
		ftppanel.add(ftppath, GridBagConstraintsFactory.create(3, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		ftppanel.add(new JLabel(Resource.getResourceString("ftpusername")),
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
		ftppanel.add(ftpusername, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel pl = new JLabel(Resource.getResourceString("ftppassword"));
		ftppanel.add(pl,
				GridBagConstraintsFactory.create(2, 1, GridBagConstraints.BOTH));
		pl.setLabelFor(ftppassword);
		ftppanel.add(ftppassword, GridBagConstraintsFactory.create(3, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));
		ftppassword.setEditable(true);

		GridBagConstraints gbc = GridBagConstraintsFactory.create(0, 5,
				GridBagConstraints.BOTH, 1.0, 0.0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.add(ftppanel, gbc);

		calpanel.add(new JLabel(Resource.getResourceString("CALDAV_Server")),
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		calpanel.add(caldavServer, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		calpanel.add(new JLabel(Resource.getResourceString("CALDAV_Path")),
				GridBagConstraintsFactory.create(2, 0, GridBagConstraints.BOTH));
		calpanel.add(caldavPath, GridBagConstraintsFactory.create(3, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		calpanel.add(new JLabel(Resource.getResourceString("CALDAV_User")),
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
		calpanel.add(caldavUser, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel pl2 = new JLabel(Resource.getResourceString("CALDAV_Password"));
		calpanel.add(pl2,
				GridBagConstraintsFactory.create(2, 1, GridBagConstraints.BOTH));
		pl2.setLabelFor(caldavPassword);
		calpanel.add(caldavPassword, GridBagConstraintsFactory.create(3, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));
		caldavPassword.setEditable(true);

		calpanel.add(new JLabel(Resource.getResourceString("CALDAV_Cal")),
				GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));
		calpanel.add(caldavCal, GridBagConstraintsFactory.create(1, 2,
				GridBagConstraints.BOTH, 1.0, 0.0));

		calpanel.add(
				new JLabel(Resource.getResourceString("CALDAV_PrincipalPath")),
				GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));
		calpanel.add(caldavPrincipalPath, GridBagConstraintsFactory.create(1,
				3, GridBagConstraints.BOTH, 1.0, 0.0));

		calpanel.add(new JLabel(Resource.getResourceString("CALDAV_UserPath")),
				GridBagConstraintsFactory.create(2, 3, GridBagConstraints.BOTH));
		calpanel.add(caldavUserPath, GridBagConstraintsFactory.create(3, 3,
				GridBagConstraints.BOTH, 1.0, 0.0));

		caldavSSL.setText(Resource.getResourceString("use_ssl"));
		caldavSelfSigned.setText(Resource.getResourceString("allow_self_signed"));

		calpanel.add(caldavSSL,
				GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));
		calpanel.add(caldavSelfSigned,
				GridBagConstraintsFactory.create(2, 4, GridBagConstraints.BOTH));

		gbc = GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH,
				1.0, 0.0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.add(calpanel, gbc);

	}

	@Override
	public void applyChanges() {

		// validate that port is a number
		try {
			int socket = Integer.parseInt(port.getText());
			Prefs.putPref(PrefName.ICAL_PORT, Integer.valueOf(socket));
		} catch (NumberFormatException e) {
			Errmsg.getErrorHandler().notice(
					Resource.getResourceString("port_warning"));

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
		OptionsPanel.setBooleanPref(todoBox, PrefName.ICAL_EXPORT_TODO);

		Prefs.putPref(PrefName.ICAL_IMPORT_URL, importurl.getText());

		Prefs.putPref(PrefName.CALDAV_USER, caldavUser.getText());
		Prefs.putPref(PrefName.CALDAV_SERVER, caldavServer.getText());
		Prefs.putPref(PrefName.CALDAV_PATH, caldavPath.getText());
		Prefs.putPref(PrefName.CALDAV_PRINCIPAL_PATH,
				caldavPrincipalPath.getText());
		Prefs.putPref(PrefName.CALDAV_USER_PATH, caldavUserPath.getText());
		try {
			CalDav.sep(new String(caldavPassword.getPassword()));
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

		Prefs.putPref(PrefName.CALDAV_CAL, caldavCal.getText());
		OptionsPanel.setBooleanPref(caldavSSL, PrefName.CALDAV_USE_SSL);
		OptionsPanel.setBooleanPref(caldavSelfSigned, PrefName.CALDAV_ALLOW_SELF_SIGNED_CERT);

	}

	@Override
	public void loadOptions() {

		int p = Prefs.getIntPref(PrefName.ICAL_PORT);
		port.setText(Integer.toString(p));

		exportyears.setValue(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS));

		skipBox.setSelected(Prefs.getBoolPref(PrefName.SKIP_BORG));
		todoBox.setSelected(Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO));

		ftpusername.setText(Prefs.getPref(PrefName.FTPUSER));
		ftpserver.setText(Prefs.getPref(PrefName.FTPSERVER));
		ftppath.setText(Prefs.getPref(PrefName.FTPPATH));

		try {
			ftppassword.setText(IcalFTP.gep());
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

		importurl.setText(Prefs.getPref(PrefName.ICAL_IMPORT_URL));

		caldavUser.setText(Prefs.getPref(PrefName.CALDAV_USER));
		caldavCal.setText(Prefs.getPref(PrefName.CALDAV_CAL));
		caldavServer.setText(Prefs.getPref(PrefName.CALDAV_SERVER));
		caldavPath.setText(Prefs.getPref(PrefName.CALDAV_PATH));
		caldavPrincipalPath.setText(Prefs
				.getPref(PrefName.CALDAV_PRINCIPAL_PATH));
		caldavUserPath.setText(Prefs.getPref(PrefName.CALDAV_USER_PATH));
		caldavSSL.setSelected(Prefs.getBoolPref(PrefName.CALDAV_USE_SSL));
		caldavSelfSigned.setSelected(Prefs.getBoolPref(PrefName.CALDAV_ALLOW_SELF_SIGNED_CERT));

		
		try {
			caldavPassword.setText(CalDav.gep());
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("ical_options");
	}

	static public void main(String args[]) {
		JFrame jf = new JFrame();
		jf.setContentPane(new IcalOptionsPanel());
		jf.setVisible(true);
	}

}
