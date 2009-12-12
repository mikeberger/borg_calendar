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

 Copyright 2003 by Mike Berger
 */

package net.sf.borg.ui.options;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.View;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * UI for editing BORG options
 */
public class OptionsView extends View {

	/**
	 * 
	 * abstract base class for tabs in the options view
	 * 
	 */
	static public abstract class OptionsPanel extends JPanel {
		private static final long serialVersionUID = -4942616624428977307L;

		/**
		 * set a boolean preference from a checkbox
		 * 
		 * @param box
		 *            the checkbox
		 * @param pn
		 *            the preference name
		 */
		static public void setBooleanPref(JCheckBox box, PrefName pn) {
			if (box.isSelected()) {
				Prefs.putPref(pn, "true");
			} else {
				Prefs.putPref(pn, "false");
			}
		}

		/**
		 * set a check box from a boolean preference
		 * 
		 * @param box
		 *            the checkbox
		 * @param pn
		 *            the preference name
		 */
		static public void setCheckBox(JCheckBox box, PrefName pn) {
			String val = Prefs.getPref(pn);
			if (val.equals("true")) {
				box.setSelected(true);
			} else {
				box.setSelected(false);
			}
		}

		/**
		 * save options from the UI to the preference store
		 */
		public abstract void applyChanges();

		/**
		 * load options from the preference store into the UI
		 */
		public abstract void loadOptions();

		/**
		 * Prompt the user to choose a folder
		 * 
		 * @return the folder path or null
		 */
		static String chooseDir() {
		
			String path = null;
			while (true) {
				JFileChooser chooser = new JFileChooser();
		
				chooser.setCurrentDirectory(new File("."));
				chooser
						.setDialogTitle("Please choose directory for database files");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					return (null);
				}
		
				path = chooser.getSelectedFile().getAbsolutePath();
				File dir = new File(path);
				String err = null;
				if (!dir.exists()) {
					err = "Directory [" + path + "] does not exist";
				} else if (!dir.isDirectory()) {
					err = "Directory [" + path + "] is not a directory";
				}
		
				if (err == null) {
					break;
				}
		
				Errmsg.notice(err);
			}
		
			return (path);
		}
	}

	private static final long serialVersionUID = 1L;

	private static OptionsView singleton = null;

	/**
	 * open the options window but with a restricted view that only allows the
	 * db to be set
	 */
	public static void dbSelectOnly() {
		new OptionsView(true).setVisible(true);

	}

	/**
	 * get the options view singleton
	 * 
	 * @return the singleton
	 */
	public static OptionsView getReference() {
		if (singleton == null || !singleton.isShowing()) {
			singleton = new OptionsView(false);
		}
		return (singleton);
	}

	private AppearanceOptionsPanel appearancePanel;
	
	private JButton applyButton;

	private ColorOptionsPanel colorPanel;

	private DatabaseOptionsPanel dbPanel;

	private JButton dismissButton;

	private EmailOptionsPanel emailPanel;

	private EncryptionOptionsPanel encryptionPanel;

	private FontOptionsPanel fontPanel;

	private JTabbedPane jTabbedPane1;

	private MiscellaneousOptionsPanel miscPanel;

	private PopupOptionsPanel popupPanel;

	private TaskOptionsPanel taskPanel;

	private JPanel topPanel = null;

	/**
	 * constructor
	 * 
	 * @param dbonly
	 *            if true, restrict changes to selecting the db only
	 */
	private OptionsView(boolean dbonly) {
		super();

		initComponents();

		if (dbonly) {
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		}

		if (dbonly) {
			// disable lots of non-db-related stuff
			// this is used only on start up when no db is set
			// and we need one set. at that time, no other options
			// really make sense
			jTabbedPane1.setEnabledAt(0, false);
			jTabbedPane1.setEnabledAt(1, false);
			jTabbedPane1.setEnabledAt(3, false);
			jTabbedPane1.setEnabledAt(4, false);
			jTabbedPane1.setEnabledAt(5, false);
			jTabbedPane1.setEnabledAt(6, false);
			jTabbedPane1.setEnabledAt(7, false);

			jTabbedPane1.setSelectedIndex(2);
			dismissButton.setEnabled(false);
			applyButton.setEnabled(false);

			return;

		}

		// automatically maintain the size and position of this view in
		// a preference
		manageMySize(PrefName.OPTVIEWSIZE);
	}

	/**
	 * save all preferences to the preference store based on the current UI
	 * values
	 */
	private void applyChanges() {

		popupPanel.applyChanges();
		emailPanel.applyChanges();
		dbPanel.applyChanges();
		colorPanel.applyChanges();
		appearancePanel.applyChanges();
		fontPanel.applyChanges();
		encryptionPanel.applyChanges();
		miscPanel.applyChanges();
		taskPanel.applyChanges();

		// notify all parts of borg that have registered to know about
		// options changes
		Prefs.notifyListeners();

	}

	/**
	 * destroy this view
	 */
	@Override
	public void destroy() {
		this.dispose();
	}

	/**
	 * get the top level panel
	 * 
	 * @return the top level panel
	 */
	private JPanel getTopPanel() {
		if (topPanel == null) {

			topPanel = new JPanel();
			topPanel.setLayout(new GridBagLayout());

			topPanel.add(jTabbedPane1, GridBagConstraintsFactory.create(0, 0,
					GridBagConstraints.BOTH, 1.0, 1.0));

			JPanel applyDismissPanel = new JPanel();

			applyButton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Save16.gif")));
			ResourceHelper.setText(applyButton, "apply");
			applyButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					applyChanges();
				}
			});
			applyDismissPanel.add(applyButton, null);

			dismissButton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Stop16.gif")));
			ResourceHelper.setText(dismissButton, "Dismiss");
			dismissButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							dispose();
						}
					});
			setDismissButton(dismissButton);
			applyDismissPanel.add(dismissButton, null);

			topPanel.add(applyDismissPanel, GridBagConstraintsFactory.create(0,
					1, GridBagConstraints.BOTH));
		}
		return topPanel;
	}

	private void initComponents() {

		jTabbedPane1 = new JTabbedPane();

		dismissButton = new JButton();
		applyButton = new JButton();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle(Resource.getResourceString("Options"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				dispose();
			}
		});

		appearancePanel = new AppearanceOptionsPanel();
		appearancePanel.loadOptions();
		ResourceHelper.addTab(jTabbedPane1, "appearance", appearancePanel);

		fontPanel = new FontOptionsPanel();
		fontPanel.loadOptions();
		ResourceHelper.addTab(jTabbedPane1, "fonts", fontPanel);

		dbPanel = new DatabaseOptionsPanel();
		dbPanel.loadOptions();
		ResourceHelper.addTab(jTabbedPane1, "DatabaseInformation", dbPanel);

		emailPanel = new EmailOptionsPanel();
		emailPanel.loadOptions();
		ResourceHelper.addTab(jTabbedPane1, "EmailParameters", emailPanel);

		popupPanel = new PopupOptionsPanel();
		popupPanel.loadOptions();
		ResourceHelper.addTab(jTabbedPane1, "popup_reminders", popupPanel);

		miscPanel = new MiscellaneousOptionsPanel();
		miscPanel.loadOptions();
		ResourceHelper.addTab(jTabbedPane1, "misc", miscPanel);

		colorPanel = new ColorOptionsPanel();
		colorPanel.loadOptions();
		ResourceHelper.addTab(jTabbedPane1, "UserColorScheme", colorPanel);

		taskPanel = new TaskOptionsPanel();
		taskPanel.loadOptions();
		ResourceHelper.addTab(jTabbedPane1, "taskOptions", taskPanel);

		encryptionPanel = new EncryptionOptionsPanel();
		encryptionPanel.loadOptions();
		ResourceHelper.addTab(jTabbedPane1, "Encryption", encryptionPanel);

		this.setContentPane(getTopPanel());
		this.setSize(629, 493);

		pack();
	}

	@Override
	public void refresh() {
		// empty
	}

}
