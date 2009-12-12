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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * Provides the UI for editing Miscellaneous options
 */
public class MiscellaneousOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 2246952528811147049L;
	private JTextField backupDir = new JTextField();
	private JCheckBox colorprint;

	private JTextField socketPort = new JTextField();
	private JCheckBox splashbox;
	private JCheckBox stackbox;

	private JCheckBox useSysTray = new JCheckBox();

	/**
	 * Instantiates a new miscellaneous options panel.
	 */
	public MiscellaneousOptionsPanel() {
		
		colorprint = new JCheckBox();

		splashbox = new JCheckBox();
		stackbox = new JCheckBox();
		
		this.setLayout(new java.awt.GridBagLayout());

		ResourceHelper.setText(splashbox, "splash");
		this.add(splashbox, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH));

		ResourceHelper.setText(stackbox, "stackonerr");
		this.add(stackbox, GridBagConstraintsFactory.create(0, 40,
				GridBagConstraints.BOTH));

		JLabel sportlabel = new JLabel();
		ResourceHelper.setText(sportlabel, "socket_port");
		this.add(sportlabel, GridBagConstraintsFactory.create(0, 9,
				GridBagConstraints.BOTH));

		this.add(socketPort, GridBagConstraintsFactory.create(1, 9,
				GridBagConstraints.BOTH));

		useSysTray.setText(Resource.getResourceString("enable_systray"));
		this.add(useSysTray, GridBagConstraintsFactory.create(0, 10,
				GridBagConstraints.BOTH));

		JPanel backp = new JPanel();
		backp.setLayout(new GridBagLayout());

		backp
				.add(
						new JLabel(Resource.getResourceString("backup_dir")
								+ ": "), GridBagConstraintsFactory.create(0, 0,
								GridBagConstraints.NONE));

		backp.add(backupDir, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JButton bb = new JButton();
		ResourceHelper.setText(bb, "Browse");
		bb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String dbdir = OptionsPanel.chooseDir();
				if (dbdir == null) {
					return;
				}

				backupDir.setText(dbdir);
			}
		});
		backp.add(bb, GridBagConstraintsFactory.create(2, 0,
				GridBagConstraints.NONE));

		GridBagConstraints gbc1 = GridBagConstraintsFactory.create(0, 11,
				GridBagConstraints.BOTH, 1.0, 0.0);
		gbc1.gridwidth = 2;
		this.add(backp, gbc1);

		ResourceHelper.setText(colorprint, "Print_In_Color?");
		this.add(colorprint, GridBagConstraintsFactory.create(0, 12,
				GridBagConstraints.BOTH));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		OptionsPanel.setBooleanPref(colorprint, PrefName.COLORPRINT);

		OptionsPanel.setBooleanPref(splashbox, PrefName.SPLASH);
		OptionsPanel.setBooleanPref(stackbox, PrefName.STACKTRACE);

		OptionsPanel.setBooleanPref(useSysTray, PrefName.USESYSTRAY);

		Prefs.putPref(PrefName.BACKUPDIR, backupDir.getText());

		// validate that socket is a number
		try {
			int socket = Integer.parseInt(socketPort.getText());
			Prefs.putPref(PrefName.SOCKETPORT, new Integer(socket));
		} catch (NumberFormatException e) {
			Errmsg.notice(Resource.getResourceString("socket_warn"));
			socketPort.setText("-1");
			Prefs.putPref(PrefName.SOCKETPORT, new Integer(-1));
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {
		backupDir.setText(Prefs.getPref(PrefName.BACKUPDIR));
		OptionsPanel.setCheckBox(colorprint, PrefName.COLORPRINT);

		OptionsPanel.setCheckBox(splashbox, PrefName.SPLASH);
		OptionsPanel.setCheckBox(stackbox, PrefName.STACKTRACE);

		OptionsPanel.setCheckBox(useSysTray, PrefName.USESYSTRAY);
		int socket = Prefs.getIntPref(PrefName.SOCKETPORT);
		socketPort.setText(Integer.toString(socket));

	}

}
