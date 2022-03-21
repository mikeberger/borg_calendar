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

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.sync.ical.CalDav;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class IcalOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 795364188303457966L;

	private final JSpinner exportyears = new JSpinner(new SpinnerNumberModel(2, 1,
			100, 1));

	private final JCheckBox todoBox = new JCheckBox();

	private final JTextField caldavServer = new JTextField();
	private final JTextField caldavPath = new JTextField();
	private final JTextField caldavPrincipalPath = new JTextField();
	private final JTextField caldavUserPath = new JTextField();
	private final JTextField carddavPath = new JTextField();
	private final JTextField caldavUser = new JTextField();
	private final JPasswordField caldavPassword = new JPasswordField();
	private final JTextField caldavCal = new JTextField();
	private final JTextField addrBook = new JTextField();

	private final JCheckBox caldavSSL = new JCheckBox();
	private final JCheckBox caldavSelfSigned = new JCheckBox();

	public IcalOptionsPanel() {
		this.setLayout(new java.awt.GridBagLayout());


		JPanel calpanel = new JPanel();
		calpanel.setBorder(new TitledBorder("CALDAV"));
		calpanel.setLayout(new GridBagLayout());

		this.add(new JLabel(Resource.getResourceString("years_to_export")),
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		this.add(exportyears,
				GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH));

		todoBox.setText(Resource.getResourceString("ical_export_todos"));
		this.add(todoBox, GridBagConstraintsFactory.create(0, 4,
				GridBagConstraints.BOTH, 1.0, 0.0));


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
		
		calpanel.add(new JLabel(Resource.getResourceString("Address_Book")),
				GridBagConstraintsFactory.create(2, 2, GridBagConstraints.BOTH));
		calpanel.add(addrBook, GridBagConstraintsFactory.create(3, 2,
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
		
		calpanel.add(new JLabel(Resource.getResourceString("CARDDAV_UserPath")),
				GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));
		calpanel.add(carddavPath, GridBagConstraintsFactory.create(1, 4,
				GridBagConstraints.BOTH, 1.0, 0.0));

		caldavSSL.setText(Resource.getResourceString("use_ssl"));
		caldavSelfSigned.setText(Resource.getResourceString("allow_self_signed"));

		calpanel.add(caldavSSL,
				GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH));
		calpanel.add(caldavSelfSigned,
				GridBagConstraintsFactory.create(2, 5, GridBagConstraints.BOTH));

		GridBagConstraints gbc = GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH,
				1.0, 0.0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.add(calpanel, gbc);

	}

	@Override
	public void applyChanges() {


		Prefs.putPref(PrefName.ICAL_EXPORTYEARS, exportyears.getValue());

		OptionsPanel.setBooleanPref(todoBox, PrefName.ICAL_EXPORT_TODO);

		Prefs.putPref(PrefName.CALDAV_USER, caldavUser.getText());
		Prefs.putPref(PrefName.CALDAV_SERVER, caldavServer.getText());
		Prefs.putPref(PrefName.CALDAV_PATH, caldavPath.getText());
		Prefs.putPref(PrefName.CALDAV_PRINCIPAL_PATH,
				caldavPrincipalPath.getText());
		Prefs.putPref(PrefName.CALDAV_USER_PATH, caldavUserPath.getText());
		Prefs.putPref(PrefName.CARDDAV_USER_PATH, carddavPath.getText());
		try {
			CalDav.sep(new String(caldavPassword.getPassword()));
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

		Prefs.putPref(PrefName.CALDAV_CAL, caldavCal.getText());
		Prefs.putPref(PrefName.CARDDAV_BOOK, addrBook.getText());
		OptionsPanel.setBooleanPref(caldavSSL, PrefName.CALDAV_USE_SSL);
		OptionsPanel.setBooleanPref(caldavSelfSigned, PrefName.CALDAV_ALLOW_SELF_SIGNED_CERT);

	}

	@Override
	public void loadOptions() {


		exportyears.setValue(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS));

		todoBox.setSelected(Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO));


		caldavUser.setText(Prefs.getPref(PrefName.CALDAV_USER));
		caldavCal.setText(Prefs.getPref(PrefName.CALDAV_CAL));
		addrBook.setText(Prefs.getPref(PrefName.CARDDAV_BOOK));
		caldavServer.setText(Prefs.getPref(PrefName.CALDAV_SERVER));
		caldavPath.setText(Prefs.getPref(PrefName.CALDAV_PATH));
		caldavPrincipalPath.setText(Prefs
				.getPref(PrefName.CALDAV_PRINCIPAL_PATH));
		caldavUserPath.setText(Prefs.getPref(PrefName.CALDAV_USER_PATH));
		carddavPath.setText(Prefs.getPref(PrefName.CARDDAV_USER_PATH));
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

	static public void main(String[] args) {
		JFrame jf = new JFrame();
		jf.setContentPane(new IcalOptionsPanel());
		jf.setVisible(true);
	}

}
