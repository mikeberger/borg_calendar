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
import java.util.ArrayList;
import java.util.Collection;

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
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.View;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * UI for editing BORG options
 */
public class OptionsView extends View {

	/** size of the option window. */
	static private PrefName OPTVIEWSIZE = new PrefName("optviewsize",
			"-1,-1,-1,-1,N");

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
		 * return the panel's display name
		 */
		public abstract String getPanelName();

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
				chooser.setDialogTitle("Please choose directory for database files");
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
		singleton = new OptionsView(true);
		singleton.setVisible(true);
	}

	/**
	 * get the options view singleton
	 * 
	 * @return the singleton
	 */
	public static OptionsView getReference() {
		if (singleton == null) {
			singleton = new OptionsView(false);
		} else if (!singleton.isShowing()) {
			// reload options to reset the tabs
			for (int t = 0; t < singleton.jTabbedPane1.getTabCount(); t++) {
				OptionsPanel panel = (OptionsPanel)singleton.jTabbedPane1.getComponentAt(t);
				panel.loadOptions();
			}
		}

		return (singleton);
	}

	private JButton applyButton;

	private JButton dismissButton;

	private JTabbedPane jTabbedPane1;

	private JPanel topPanel = null;

	private Collection<OptionsPanel> panels = new ArrayList<OptionsPanel>();

	/**
	 * constructor
	 * 
	 * @param dbonly
	 *            if true, restrict changes to selecting the db only
	 */
	private OptionsView(boolean dbonly) {
		super();

		jTabbedPane1 = new JTabbedPane();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle(Resource.getResourceString("Options"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				destroy();
			}
		});

		topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());

		topPanel.add(jTabbedPane1, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		if (dbonly) {
			// setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		} else {

			dismissButton = new JButton();
			applyButton = new JButton();

			JPanel applyDismissPanel = new JPanel();

			applyButton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Save16.gif")));
			ResourceHelper.setText(applyButton, "apply");
			applyButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
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
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							destroy();
						}
					});
			setDismissButton(dismissButton);
			applyDismissPanel.add(dismissButton, null);

			topPanel.add(applyDismissPanel, GridBagConstraintsFactory.create(0,
					1, GridBagConstraints.BOTH));

			addPanel(new AppearanceOptionsPanel());
			addPanel(new FontOptionsPanel());
			addPanel(new EmailOptionsPanel());
			addPanel(new PopupOptionsPanel());
			addPanel(new MiscellaneousOptionsPanel());
			addPanel(new ColorOptionsPanel());
			addPanel(new TaskOptionsPanel());
			addPanel(new TodoOptionsPanel());
			addPanel(new EncryptionOptionsPanel());

		}

		addPanel(new DatabaseOptionsPanel());

		this.setContentPane(topPanel);
		this.setSize(629, 493);

		pack();

		// automatically maintain the size and position of this view in
		// a preference
		if (!dbonly)
			manageMySize(OPTVIEWSIZE);
	}

	/**
	 * save all preferences to the preference store based on the current UI
	 * values
	 */
	private void applyChanges() {

		for (OptionsPanel panel : panels)
			panel.applyChanges();

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

	@Override
	public void refresh() {
		// empty
	}

	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * add an options panel to the options view
	 * 
	 * @param panel
	 *            - the panel
	 */
	public void addPanel(OptionsPanel panel) {
		panel.loadOptions();
		jTabbedPane1.addTab(panel.getPanelName(), panel);
		// jTabbedPane1.add(panel.getPanelName(), panel);
		panels.add(panel);
	}

}
