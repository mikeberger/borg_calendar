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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.HelpLauncher;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * Provides the UI for editing database options
 */
public class DatabaseOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 7695315940556870216L;

	private JTextField dbDirText;

	private JTextField dbHostText;

	private JTextField dbNameText;

	private JTextField dbPortText;

	private ButtonGroup dbTypeGroup;

	private JPanel dbTypePanel = null;

	private JTextField dbUserText;
	private JRadioButton hsqldbButton;
	private JRadioButton h2Button;

	private JTextField hsqldbdir = new JTextField();
	private JTextField h2dir = new JTextField();

	private JPanel hsqldbPanel;
	private JPanel h2Panel;
	private JRadioButton jdbcButton = null;

	private JPanel jdbcPanel = null;

	private JTextField jdbcText = null;
	private JPasswordField jPasswordField1;

	private JRadioButton MySQLButton = null;

	private JPanel mysqlPanel;

	/**
	 * Instantiates a new database options panel.
	 */
	public DatabaseOptionsPanel() {

		dbNameText = new JTextField();
		dbHostText = new JTextField();
		dbPortText = new JTextField();
		dbUserText = new JTextField();
		jPasswordField1 = new JPasswordField();

		dbDirText = new JTextField();

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbcm = GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 1.0, 1.0);
		gbcm.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(getMysqlPanel(), gbcm);
		this.add(getDbTypePanel(),
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		GridBagConstraints gridBagConstraints5 = GridBagConstraintsFactory
				.create(0, 7);
		gridBagConstraints5.weightx = 1.0;
		gridBagConstraints5.anchor = GridBagConstraints.CENTER;
		JButton chgdb = new JButton();
		this.add(chgdb, gridBagConstraints5);
		chgdb.setForeground(new java.awt.Color(255, 0, 51));
		chgdb.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Refresh16.gif")));
		ResourceHelper.setText(chgdb, "Apply_DB_Change");
		chgdb.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				chgdbActionPerformed();
			}
		});

		JButton help = new JButton();
		GridBagConstraints gridBagConstraintsh = GridBagConstraintsFactory
				.create(1, 7);
		gridBagConstraintsh.weightx = 1.0;
		gridBagConstraintsh.anchor = GridBagConstraints.CENTER;
		this.add(help, gridBagConstraintsh);
		help.setForeground(new java.awt.Color(255, 0, 51));
		help.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Help16.gif")));
		ResourceHelper.setText(help, "Help");

		help.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					HelpLauncher.launchHelp();
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		GridBagConstraints gridBagConstraints6h = GridBagConstraintsFactory
				.create(0, 4, GridBagConstraints.BOTH, 1.0, 1.0);
		gridBagConstraints6h.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(getHSQLDBPanel(), gridBagConstraints6h);

		GridBagConstraints gridBagConstraints7h = GridBagConstraintsFactory
				.create(0, 5, GridBagConstraints.BOTH, 1.0, 1.0);
		gridBagConstraints7h.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(getJdbcPanel(), gridBagConstraints7h);

		GridBagConstraints gridBagConstraints8h = GridBagConstraintsFactory
				.create(0, 6, GridBagConstraints.BOTH, 1.0, 1.0);
		gridBagConstraints8h.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(getH2Panel(), gridBagConstraints8h);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		// empty - db options only applied when chg db button pressed
	}

	/**
	 * change the database settings - will update the settings and then restart
	 * the program
	 */
	private void chgdbActionPerformed() {
		int ret = JOptionPane.showConfirmDialog(null,
				Resource.getResourceString("Really_change_the_database?"),
				Resource.getResourceString("Confirm_DB_Change"),
				JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {

			String hh = hsqldbdir.getText();
			Prefs.putPref(PrefName.HSQLDBDIR, hh);

			String h2 = h2dir.getText();
			Prefs.putPref(PrefName.H2DIR, h2);

			if (MySQLButton.isSelected()) {
				Prefs.putPref(PrefName.DBTYPE, "mysql");
			} else if (hsqldbButton.isSelected()) {
				Prefs.putPref(PrefName.DBTYPE, "hsqldb");
			} else if (h2Button.isSelected()) {
				Prefs.putPref(PrefName.DBTYPE, "h2");
			} else {
				Prefs.putPref(PrefName.DBTYPE, "jdbc");
			}
			Prefs.putPref(PrefName.DBNAME, dbNameText.getText());
			Prefs.putPref(PrefName.DBPORT, dbPortText.getText());
			Prefs.putPref(PrefName.DBHOST, dbHostText.getText());
			Prefs.putPref(PrefName.DBUSER, dbUserText.getText());
			Prefs.putPref(PrefName.DBPASS,
					new String(jPasswordField1.getPassword()));
			Prefs.putPref(PrefName.JDBCURL, jdbcText.getText());

			Errmsg.notice(Resource.getResourceString("Restart_Warning"));
			System.exit(0);
		}
	}

	/**
	 * show the db settings panel that corresponds to the chosen db type and
	 * hide the other panels
	 * 
	 * @param type
	 *            db type
	 */
	private void dbTypeChange(String type) {
		if (type.equals("mysql")) {
			mysqlPanel.setVisible(true);
			hsqldbPanel.setVisible(false);
			jdbcPanel.setVisible(false);
			h2Panel.setVisible(false);
		} else if (type.equals("hsqldb")) {
			mysqlPanel.setVisible(false);
			hsqldbPanel.setVisible(true);
			jdbcPanel.setVisible(false);
			h2Panel.setVisible(false);
		} else if (type.equals("h2")) {
			mysqlPanel.setVisible(false);
			hsqldbPanel.setVisible(false);
			jdbcPanel.setVisible(false);
			h2Panel.setVisible(true);
		} else {
			mysqlPanel.setVisible(false);
			hsqldbPanel.setVisible(false);
			jdbcPanel.setVisible(true);
			h2Panel.setVisible(false);
		}
	}

	/**
	 * get the database type button panel
	 * 
	 * @return the database type button panel
	 */
	private JPanel getDbTypePanel() {
		if (dbTypePanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
			flowLayout.setHgap(40);
			dbTypePanel = new JPanel();
			dbTypePanel.setLayout(flowLayout);

			hsqldbButton = new JRadioButton();
			hsqldbButton.setActionCommand("hsqldb");
			ResourceHelper.setText(hsqldbButton, "hsqldb");
			hsqldbButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeChange(e.getActionCommand());
				}
			});
			dbTypePanel.add(hsqldbButton, null);
			
			h2Button = new JRadioButton();
			h2Button.setActionCommand("h2");
			h2Button.setText("H2");
			h2Button.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeChange(e.getActionCommand());
				}
			});
			dbTypePanel.add(h2Button, null);

			MySQLButton = new JRadioButton();
			MySQLButton.setActionCommand("mysql");
			MySQLButton.setText("MySQL");
			MySQLButton.setMnemonic(KeyEvent.VK_M);
			MySQLButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeChange(e.getActionCommand());
				}
			});
			dbTypePanel.add(MySQLButton, null);

			jdbcButton = new JRadioButton();
			jdbcButton.setActionCommand("jdbc");
			ResourceHelper.setText(jdbcButton, "jdbc");
			jdbcButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeChange(e.getActionCommand());
				}
			});
			dbTypePanel.add(jdbcButton, null);

			dbTypeGroup = new ButtonGroup();
			dbTypeGroup.add(hsqldbButton);
			dbTypeGroup.add(h2Button);
			dbTypeGroup.add(MySQLButton);
			dbTypeGroup.add(jdbcButton);

		}
		return dbTypePanel;
	}

	/**
	 * get the hsql options panel
	 * 
	 * @return the hsql options panel
	 */
	private JPanel getHSQLDBPanel() {
		hsqldbPanel = new JPanel();
		hsqldbPanel.setLayout(new java.awt.GridBagLayout());

		JLabel hs1 = new JLabel();
		hsqldbPanel.setBorder(new TitledBorder(Resource
				.getResourceString("hsqldbinfo")));
		ResourceHelper.setText(hs1, "DataBase_Directory");
		hs1.setLabelFor(dbDirText);
		hsqldbPanel
				.add(hs1, GridBagConstraintsFactory.create(0, 0,
						GridBagConstraints.BOTH));

		hsqldbPanel.add(hsqldbdir, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 0.5, 0.0));

		JButton hsb1 = new JButton();
		ResourceHelper.setText(hsb1, "Browse");
		hsb1.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				// browse for new database dir
				String dbdir = OptionsPanel.chooseDir();
				if (dbdir == null) {
					return;
				}

				// update text field - nothing else changes. DB change will take
				// effect
				// only on restart
				hsqldbdir.setText(dbdir);
			}
		});

		hsqldbPanel
				.add(hsb1, GridBagConstraintsFactory.create(1, 1,
						GridBagConstraints.BOTH));

		return hsqldbPanel;
	}

	/**
	 * get the h2 options panel
	 * 
	 * @return the h2 options panel
	 */
	private JPanel getH2Panel() {
		h2Panel = new JPanel();
		h2Panel.setLayout(new java.awt.GridBagLayout());

		JLabel hs1 = new JLabel();
		h2Panel.setBorder(new TitledBorder(Resource.getResourceString("h2info")));
		ResourceHelper.setText(hs1, "DataBase_Directory");
		hs1.setLabelFor(dbDirText);
		h2Panel.add(hs1,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		h2Panel.add(h2dir, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 0.5, 0.0));

		JButton hsb1 = new JButton();
		ResourceHelper.setText(hsb1, "Browse");
		hsb1.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				// browse for new database dir
				String dbdir = OptionsPanel.chooseDir();
				if (dbdir == null) {
					return;
				}

				// update text field - nothing else changes. DB change will take
				// effect
				// only on restart
				h2dir.setText(dbdir);
			}
		});

		h2Panel.add(hsb1,
				GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH));

		return h2Panel;
	}

	/**
	 * get the generic jdbc options panel
	 * 
	 * @return the jdbc options panel
	 */
	private JPanel getJdbcPanel() {
		if (jdbcPanel == null) {

			JLabel enturlLabel = new JLabel();
			ResourceHelper.setText(enturlLabel, "enturl");
			jdbcText = new JTextField();
			enturlLabel.setLabelFor(jdbcText);
			enturlLabel.setHorizontalTextPosition(SwingConstants.LEFT);
			enturlLabel.setHorizontalAlignment(SwingConstants.LEFT);
			jdbcPanel = new JPanel();
			jdbcPanel.setLayout(new GridBagLayout());
			jdbcPanel.setBorder(BorderFactory.createTitledBorder(null,
					Resource.getResourceString("jdbc"),
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, null, null));
			jdbcPanel.add(enturlLabel, GridBagConstraintsFactory.create(0, 0,
					GridBagConstraints.BOTH));
			jdbcPanel.add(jdbcText, GridBagConstraintsFactory.create(0, 1,
					GridBagConstraints.BOTH, 1.0, 0.0));
		}
		return jdbcPanel;
	}

	/**
	 * get the mysql options panel
	 * 
	 * @return the mysql options panel
	 */
	private JPanel getMysqlPanel() {
		mysqlPanel = new JPanel();
		mysqlPanel.setLayout(new java.awt.GridBagLayout());

		mysqlPanel.setBorder(new TitledBorder(Resource
				.getResourceString("MySQLInfo")));

		JLabel jLabel7 = new JLabel();
		ResourceHelper.setText(jLabel7, "DatabaseName");
		jLabel7.setLabelFor(dbNameText);
		mysqlPanel
				.add(jLabel7, GridBagConstraintsFactory.create(0, 0,
						GridBagConstraints.BOTH));

		mysqlPanel
				.add(dbNameText, GridBagConstraintsFactory.create(1, 0,
						GridBagConstraints.BOTH));

		JLabel jLabel17 = new JLabel();
		ResourceHelper.setText(jLabel17, "hostname");
		jLabel17.setLabelFor(dbHostText);
		mysqlPanel
				.add(jLabel17, GridBagConstraintsFactory.create(0, 1,
						GridBagConstraints.BOTH));

		mysqlPanel.add(dbHostText, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel jLabel18 = new JLabel();
		ResourceHelper.setText(jLabel18, "port");
		jLabel18.setLabelFor(dbPortText);
		mysqlPanel
				.add(jLabel18, GridBagConstraintsFactory.create(0, 2,
						GridBagConstraints.BOTH));

		mysqlPanel.add(dbPortText, GridBagConstraintsFactory.create(1, 2,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel jLabel19 = new JLabel();
		ResourceHelper.setText(jLabel19, "User");
		jLabel19.setLabelFor(dbUserText);
		mysqlPanel
				.add(jLabel19, GridBagConstraintsFactory.create(0, 3,
						GridBagConstraints.BOTH));

		mysqlPanel.add(dbUserText, GridBagConstraintsFactory.create(1, 3,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel jLabel20 = new JLabel();
		ResourceHelper.setText(jLabel20, "Password");
		jLabel20.setLabelFor(jPasswordField1);
		mysqlPanel
				.add(jLabel20, GridBagConstraintsFactory.create(0, 4,
						GridBagConstraints.BOTH));

		mysqlPanel
				.add(jPasswordField1, GridBagConstraintsFactory.create(1, 4,
						GridBagConstraints.BOTH));

		return mysqlPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {
		//
		// database type
		//
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		if (dbtype.equals("mysql")) {
			MySQLButton.setSelected(true);
		} else if (dbtype.equals("hsqldb")) {
			hsqldbButton.setSelected(true);} 
		else if (dbtype.equals("h2")) {
				h2Button.setSelected(true);
		} else {
			jdbcButton.setSelected(true);
		}
		// change to show the db panel that matches the db type
		dbTypeChange(dbtype);

		// more db options
		dbNameText.setText(Prefs.getPref(PrefName.DBNAME));
		dbPortText.setText(Prefs.getPref(PrefName.DBPORT));
		dbHostText.setText(Prefs.getPref(PrefName.DBHOST));
		dbUserText.setText(Prefs.getPref(PrefName.DBUSER));
		jPasswordField1.setText(Prefs.getPref(PrefName.DBPASS));
		jdbcText.setText(Prefs.getPref(PrefName.JDBCURL));
		hsqldbdir.setText(Prefs.getPref(PrefName.HSQLDBDIR));
		h2dir.setText(Prefs.getPref(PrefName.H2DIR));

	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("DatabaseInformation");
	}

}
