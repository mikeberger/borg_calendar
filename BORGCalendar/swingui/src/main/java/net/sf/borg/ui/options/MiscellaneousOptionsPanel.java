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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

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
	
	static private final Logger log = Logger.getLogger("net.sf.borg");


	public enum SHUTDOWN_ACTION {
		PROMPT, BACKUP, EMAIL, NONE
	}

	private static final long serialVersionUID = 2246952528811147049L;
	private JTextField backupDir = new JTextField();
	private JCheckBox colorprint = new JCheckBox();

	private JTextField socketPort = new JTextField();
	private JCheckBox splashbox = new JCheckBox();
	private JCheckBox stackbox = new JCheckBox();

	private JCheckBox useSysTray = new JCheckBox();
	private JCheckBox startToSysTray = new JCheckBox();
	private JCheckBox dateInSysTray = new JCheckBox();

	private JCheckBox verboseLogging = new JCheckBox();

	private JComboBox<String> shutdownAction = new JComboBox<String>();

	private JSpinner flushSpinner = new JSpinner();

	/**
	 * Instantiates a new miscellaneous options panel.
	 */
	public MiscellaneousOptionsPanel() {

		this.setLayout(new java.awt.GridBagLayout());

		ResourceHelper.setText(splashbox, "splash");
		this.add(splashbox,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		ResourceHelper.setText(stackbox, "stackonerr");
		this.add(stackbox,
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

		JLabel sportlabel = new JLabel();
		ResourceHelper.setText(sportlabel, "socket_port");
		this.add(sportlabel,
				GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));

		this.add(socketPort,
				GridBagConstraintsFactory.create(1, 2, GridBagConstraints.BOTH));

		useSysTray.setText(Resource.getResourceString("enable_systray"));
		this.add(useSysTray,
				GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));

		startToSysTray.setText(Resource.getResourceString("StartToSysTray"));
		this.add(startToSysTray,
				GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));

		dateInSysTray.setText(Resource
				.getResourceString("show_date_in_systray"));
		this.add(dateInSysTray,
				GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH));

		JPanel backp = new JPanel();
		backp.setLayout(new GridBagLayout());

		backp.add(new JLabel(Resource.getResourceString("backup_dir") + ": "),
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.NONE));

		backp.add(backupDir, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JButton bb = new JButton();
		ResourceHelper.setText(bb, "Browse");
		bb.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String dbdir = OptionsPanel.chooseDir();
				if (dbdir == null) {
					return;
				}

				backupDir.setText(dbdir);
			}
		});
		backp.add(bb,
				GridBagConstraintsFactory.create(2, 0, GridBagConstraints.NONE));

		backp.add(new JLabel(Resource.getResourceString("on_shutdown")),
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

		shutdownAction.addItem(Resource.getResourceString("prompt_for_backup"));
		shutdownAction.addItem(Resource.getResourceString("exit_no_backup"));
		shutdownAction.addItem(Resource.getResourceString("write_backup_file"));
		shutdownAction.addItem(Resource.getResourceString("backup_with_email"));

		backp.add(shutdownAction, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		GridBagConstraints gbc1 = GridBagConstraintsFactory.create(0, 6,
				GridBagConstraints.BOTH, 1.0, 0.0);
		gbc1.gridwidth = 2;
		this.add(backp, gbc1);

		
		ResourceHelper.setText(colorprint, "Print_In_Color?");
		this.add(colorprint,
				GridBagConstraintsFactory.create(0, 8, GridBagConstraints.BOTH));

	
		verboseLogging.setText(Resource.getResourceString("verbose_logging"));
		gbc1 = GridBagConstraintsFactory.create(0, 10, GridBagConstraints.BOTH);
		gbc1.gridwidth = 2;
		this.add(verboseLogging, gbc1);
		
/*		useProxy.setText(Resource.getResourceString("Use_Proxy"));
		gbc1 = GridBagConstraintsFactory.create(0, 11, GridBagConstraints.BOTH);
		gbc1.gridwidth = 2;
		this.add(useProxy, gbc1);
		
		this.add(new JLabel(Resource.getResourceString("proxy_host")),
				GridBagConstraintsFactory.create(0, 12, GridBagConstraints.BOTH));
		this.add(proxyHostText,
				GridBagConstraintsFactory.create(1, 12, GridBagConstraints.BOTH));
		
		this.add(new JLabel(Resource.getResourceString("proxy_port")),
				GridBagConstraintsFactory.create(0, 13, GridBagConstraints.BOTH));
		this.add(proxyPortText,
				GridBagConstraintsFactory.create(1, 13, GridBagConstraints.BOTH));
*/
		flushSpinner.setModel(new SpinnerNumberModel(0,0,null,1));
		this.add(new JLabel(Resource.getResourceString("db_flush")),
				GridBagConstraintsFactory.create(0, 14, GridBagConstraints.BOTH));
		this.add(flushSpinner,
				GridBagConstraintsFactory.create(1, 14, GridBagConstraints.BOTH));


	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		
		if (verboseLogging.isSelected())
			log.setLevel(Level.ALL);
		else
			log.setLevel(Level.INFO);

		
		OptionsPanel.setBooleanPref(colorprint, PrefName.COLORPRINT);

		OptionsPanel.setBooleanPref(splashbox, PrefName.SPLASH);
		OptionsPanel.setBooleanPref(stackbox, PrefName.STACKTRACE);

		OptionsPanel.setBooleanPref(useSysTray, PrefName.USESYSTRAY);
		OptionsPanel.setBooleanPref(startToSysTray, PrefName.BACKGSTART);
		OptionsPanel.setBooleanPref(dateInSysTray, PrefName.SYSTRAYDATE);
		OptionsPanel.setBooleanPref(verboseLogging, PrefName.DEBUG);
		//OptionsPanel.setBooleanPref(useProxy, PrefName.USE_PROXY);
		
		Prefs.putPref(PrefName.BACKUPDIR, backupDir.getText());

		if (shutdownAction.getSelectedIndex() == 0)
			Prefs.putPref(PrefName.SHUTDOWN_ACTION, SHUTDOWN_ACTION.PROMPT.toString());
		else if (shutdownAction.getSelectedIndex() == 1)
			Prefs.putPref(PrefName.SHUTDOWN_ACTION, SHUTDOWN_ACTION.NONE.toString());
		else if (shutdownAction.getSelectedIndex() == 2)
			Prefs.putPref(PrefName.SHUTDOWN_ACTION, SHUTDOWN_ACTION.BACKUP.toString());
		else if (shutdownAction.getSelectedIndex() == 3)
			Prefs.putPref(PrefName.SHUTDOWN_ACTION, SHUTDOWN_ACTION.EMAIL.toString());

		// validate that socket is a number
		try {
			int socket = Integer.parseInt(socketPort.getText());
			Prefs.putPref(PrefName.SOCKETPORT, Integer.valueOf(socket));
		} catch (NumberFormatException e) {
			Errmsg.getErrorHandler().notice(Resource.getResourceString("socket_warn"));
			socketPort.setText("-1");
			Prefs.putPref(PrefName.SOCKETPORT, Integer.valueOf(-1));
		}
		
		//Prefs.putPref(PrefName.PROXY_HOST, proxyHostText.getText());

//		try {
//			int port = Integer.parseInt(proxyPortText.getText());
//			Prefs.putPref(PrefName.PROXY_PORT, Integer.valueOf(port));
//		} catch (NumberFormatException e) {
//			Errmsg.getErrorHandler().notice(Resource.getResourceString("proxy_warn"));
//		}
//		
		int fmins = (int) flushSpinner.getValue();
		Prefs.putPref(PrefName.FLUSH_MINS, fmins);
		

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
		OptionsPanel.setCheckBox(startToSysTray, PrefName.BACKGSTART);
		OptionsPanel.setCheckBox(dateInSysTray, PrefName.SYSTRAYDATE);
		OptionsPanel.setCheckBox(verboseLogging, PrefName.DEBUG);
		//OptionsPanel.setCheckBox(useProxy, PrefName.USE_PROXY);

		String shutdown_action = Prefs.getPref(PrefName.SHUTDOWN_ACTION);
		if (shutdown_action.isEmpty()
				|| SHUTDOWN_ACTION.PROMPT.toString().equals(shutdown_action))
			shutdownAction.setSelectedIndex(0);
		else if (SHUTDOWN_ACTION.NONE.toString().equals(shutdown_action))
			shutdownAction.setSelectedIndex(1);
		else if (SHUTDOWN_ACTION.BACKUP.toString().equals(shutdown_action))
			shutdownAction.setSelectedIndex(2);
		else if (SHUTDOWN_ACTION.EMAIL.toString().equals(shutdown_action))
			shutdownAction.setSelectedIndex(3);

		int socket = Prefs.getIntPref(PrefName.SOCKETPORT);
		socketPort.setText(Integer.toString(socket));
		
//		proxyHostText.setText(Prefs.getPref(PrefName.PROXY_HOST));
//		
//		int port = Prefs.getIntPref(PrefName.PROXY_PORT);
//		proxyPortText.setText(Integer.toString(port));
		
		int flushMins = Prefs.getIntPref(PrefName.FLUSH_MINS);
		flushSpinner.setValue(flushMins);

	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("misc");
	}
}
