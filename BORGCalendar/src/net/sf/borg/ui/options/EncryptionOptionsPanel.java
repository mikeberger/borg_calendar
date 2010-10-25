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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.borg.common.EncryptionHelper;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * Provides the UI for editing Encryption options
 */
public class EncryptionOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 3485306815964774388L;

	private JTextField keyStoreText = new JTextField();

	private JSpinner passwordTimeSpinner = null;

	/**
	 * Instantiates a new encryption options panel.
	 */
	public EncryptionOptionsPanel() {
		this.setLayout(new java.awt.GridBagLayout());

		JPanel ksPanel = new JPanel();
		ksPanel.setLayout(new GridBagLayout());

		ksPanel
				.add(new JLabel(Resource.getResourceString("KeyStore") + ": "),
						GridBagConstraintsFactory.create(0, 0,
								GridBagConstraints.NONE));

		keyStoreText.setEditable(false);
		ksPanel.add(keyStoreText, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JButton bb = new JButton();
		ResourceHelper.setText(bb, "Browse");
		bb.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				JFileChooser chooser = new JFileChooser();

				chooser.setCurrentDirectory(new File(System
						.getProperty("user.home")));
				chooser.setDialogTitle(Resource
						.getResourceString("SelectKeyStore"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int returnVal = chooser.showOpenDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = chooser.getSelectedFile();

				if (!file.canRead()) {
					// create keystore
					JTextArea ta = new JTextArea();
					ta.setText(Resource.getResourceString("create_key_store"));
					ta.setEditable(false);
					ta.setRows(2);
					JPasswordField jpf = new JPasswordField();
					JPasswordField jpf2 = new JPasswordField();
					int result = JOptionPane.showConfirmDialog(null,
							new Object[] { ta, jpf, jpf2 }, Resource
									.getResourceString("Password"),
							JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.CANCEL_OPTION)
						return;

					if (!new String(jpf.getPassword()).equals(new String(jpf2
							.getPassword()))) {
						Errmsg.notice("Passwords do not match");
						return;
					}

					try {
						EncryptionHelper.createStore(file.getAbsolutePath(),
								new String(jpf.getPassword()));
						EncryptionHelper.generateKey(file.getAbsolutePath(),
								new String(jpf.getPassword()), Prefs
										.getPref(PrefName.KEYALIAS));
					} catch (Exception e) {
						Errmsg.errmsg(e);
					}

				}

				keyStoreText.setText(file.getAbsolutePath());
			}
		});
		ksPanel.add(bb, GridBagConstraintsFactory.create(2, 0,
				GridBagConstraints.NONE));

		GridBagConstraints gbc1 = GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 0.0);
		gbc1.gridwidth = 2;
		this.add(ksPanel, gbc1);

		this
				.add(new JLabel(Resource.getResourceString("pw_time")),
						GridBagConstraintsFactory.create(0, 1,
								GridBagConstraints.BOTH));

		passwordTimeSpinner = new JSpinner(new SpinnerNumberModel(0, 0,
				60 * 60 * 24 * 365, 1));

		this.add(passwordTimeSpinner, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		Prefs.putPref(PrefName.KEYSTORE, keyStoreText.getText());
		Prefs.putPref(PrefName.PASSWORD_TTL, passwordTimeSpinner.getValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {
		keyStoreText.setText(Prefs.getPref(PrefName.KEYSTORE));
		passwordTimeSpinner.setValue(new Integer(Prefs
				.getIntPref(PrefName.PASSWORD_TTL)));

	}
	
	@Override
	public String getPanelName() {
		return Resource.getResourceString("Encryption");
	}

}
