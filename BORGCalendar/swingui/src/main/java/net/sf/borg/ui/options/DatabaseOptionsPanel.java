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
import net.sf.borg.ui.HelpLauncher;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Provides the UI for editing database options
 */
public class DatabaseOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 7695315940556870216L;

	private final JTextField dbDirText;

	private ButtonGroup dbTypeGroup;

	private JPanel dbTypePanel = null;

	private JRadioButton hsqldbButton;
	private JRadioButton h2Button;
	private JRadioButton sqliteButton;

	private final JTextField hsqldbdir = new JTextField();
	private final JTextField h2dir = new JTextField();
	private final JTextField sqlitedir = new JTextField();

	private JPanel hsqldbPanel;
	private JPanel h2Panel;
	private JPanel sqlitePanel;

	private JRadioButton jdbcButton = null;

	private JPanel jdbcPanel = null;

	private JTextField jdbcText = null;

	/**
	 * Instantiates a new database options panel.
	 */
	public DatabaseOptionsPanel() {

		dbDirText = new JTextField();

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbcm = GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 1.0, 1.0);
		gbcm.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(getDbTypePanel(), GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		GridBagConstraints gridBagConstraints5 = GridBagConstraintsFactory.create(0, 8);
		gridBagConstraints5.weightx = 1.0;
		gridBagConstraints5.anchor = GridBagConstraints.CENTER;
		JButton chgdb = new JButton();
		this.add(chgdb, gridBagConstraints5);
		chgdb.setForeground(new java.awt.Color(255, 0, 51));
		chgdb.setIcon(new ImageIcon(getClass().getResource("/resource/Refresh16.gif")));
		ResourceHelper.setText(chgdb, "Apply_DB_Change");
		chgdb.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				chgdbActionPerformed();
			}
		});

		JButton help = new JButton();
		GridBagConstraints gridBagConstraintsh = GridBagConstraintsFactory.create(1, 8);
		gridBagConstraintsh.weightx = 1.0;
		gridBagConstraintsh.anchor = GridBagConstraints.CENTER;
		this.add(help, gridBagConstraintsh);
		help.setForeground(new java.awt.Color(255, 0, 51));
		help.setIcon(new ImageIcon(getClass().getResource("/resource/Help16.gif")));
		ResourceHelper.setText(help, "Help");

		help.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					HelpLauncher.launchHelp();
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});

		GridBagConstraints gridBagConstraints6h = GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH, 1.0,
				1.0);
		gridBagConstraints6h.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(getHSQLDBPanel(), gridBagConstraints6h);

		GridBagConstraints gridBagConstraints7h = GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH, 1.0,
				1.0);
		gridBagConstraints7h.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(getJdbcPanel(), gridBagConstraints7h);

		GridBagConstraints gridBagConstraints8h = GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH, 1.0,
				1.0);
		gridBagConstraints8h.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(getH2Panel(), gridBagConstraints8h);

		GridBagConstraints gridBagConstraints9h = GridBagConstraintsFactory.create(0, 7, GridBagConstraints.BOTH, 1.0,
				1.0);
		gridBagConstraints9h.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		this.add(getSqlitePanel(), gridBagConstraints9h);
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
	 * change the database settings - will update the settings and then restart the
	 * program
	 */
	private void chgdbActionPerformed() {
		int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("Really_change_the_database?"),
				Resource.getResourceString("Confirm_DB_Change"), JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {

			String hh = hsqldbdir.getText();
			Prefs.putPref(PrefName.HSQLDBDIR, hh);

			String h2 = h2dir.getText();
			Prefs.putPref(PrefName.H2DIR, h2);
			
			String sq = sqlitedir.getText();
			Prefs.putPref(PrefName.SQLITEDIR, sq);

			if (hsqldbButton.isSelected()) {
				Prefs.putPref(PrefName.DBTYPE, "hsqldb");
			} else if (h2Button.isSelected()) {
				Prefs.putPref(PrefName.DBTYPE, "h2");
			} else if( sqliteButton.isSelected()) {
				Prefs.putPref(PrefName.DBTYPE, "sqlite");
			} else {
				Prefs.putPref(PrefName.DBTYPE, "jdbc");
			}

			Prefs.putPref(PrefName.JDBCURL, jdbcText.getText());

			Errmsg.getErrorHandler().notice(Resource.getResourceString("Restart_Warning"));
			System.exit(0);
		}
	}

	/**
	 * show the db settings panel that corresponds to the chosen db type and hide
	 * the other panels
	 *
	 * @param type db type
	 */
	private void dbTypeChange(String type) {
		if (type.equals("hsqldb")) {
			hsqldbPanel.setVisible(true);
			sqlitePanel.setVisible(false);
			jdbcPanel.setVisible(false);
			h2Panel.setVisible(false);
		} else if (type.equals("h2")) {
			hsqldbPanel.setVisible(false);
			jdbcPanel.setVisible(false);
			h2Panel.setVisible(true);
			sqlitePanel.setVisible(false);
		} else if (type.equals("sqlite")) {
			sqlitePanel.setVisible(true);
			hsqldbPanel.setVisible(false);
			jdbcPanel.setVisible(false);
			h2Panel.setVisible(false);
		} else {
			hsqldbPanel.setVisible(false);
			jdbcPanel.setVisible(true);
			h2Panel.setVisible(false);
			sqlitePanel.setVisible(false);
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
			
			sqliteButton = new JRadioButton();
			sqliteButton.setActionCommand("sqlite");
			sqliteButton.setText("SQLITE");
			sqliteButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeChange(e.getActionCommand());
				}
			});
			dbTypePanel.add(sqliteButton, null);

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
			dbTypeGroup.add(sqliteButton);
			dbTypeGroup.add(hsqldbButton);
			dbTypeGroup.add(h2Button);
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
		hsqldbPanel.setBorder(new TitledBorder(Resource.getResourceString("hsqldbinfo")));
		ResourceHelper.setText(hs1, "DataBase_Directory");
		hs1.setLabelFor(dbDirText);
		hsqldbPanel.add(hs1, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		hsqldbPanel.add(hsqldbdir, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 0.5, 0.0));

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

		hsqldbPanel.add(hsb1, GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH));

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
		h2Panel.add(hs1, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		h2Panel.add(h2dir, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 0.5, 0.0));

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

		h2Panel.add(hsb1, GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH));

		return h2Panel;
	}

	/**
	 * get the h2 options panel
	 *
	 * @return the h2 options panel
	 */
	private JPanel getSqlitePanel() {
		sqlitePanel = new JPanel();
		sqlitePanel.setLayout(new java.awt.GridBagLayout());

		JLabel sql1 = new JLabel();
		sqlitePanel.setBorder(new TitledBorder(Resource.getResourceString("sqliteinfo")));
		ResourceHelper.setText(sql1, "DataBase_Directory");
		sql1.setLabelFor(dbDirText);
		sqlitePanel.add(sql1, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		sqlitePanel.add(sqlitedir, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 0.5, 0.0));

		JButton sqb1 = new JButton();
		ResourceHelper.setText(sqb1, "Browse");
		sqb1.addActionListener(new java.awt.event.ActionListener() {
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
				sqlitedir.setText(dbdir);
			}
		});

		sqlitePanel.add(sqb1, GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH));

		return sqlitePanel;
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
			jdbcPanel.setBorder(BorderFactory.createTitledBorder(null, Resource.getResourceString("jdbc"),
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
			jdbcPanel.add(enturlLabel, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
			jdbcPanel.add(jdbcText, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 1.0, 0.0));
		}
		return jdbcPanel;
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
		if (dbtype.equals("hsqldb")) {
			hsqldbButton.setSelected(true);
		} else if (dbtype.equals("h2")) {
			h2Button.setSelected(true);
		} else if (dbtype.equals("sqlite")) {
			sqliteButton.setSelected(true);
		} else {
			jdbcButton.setSelected(true);
		}
		// change to show the db panel that matches the db type
		dbTypeChange(dbtype);

		jdbcText.setText(Prefs.getPref(PrefName.JDBCURL));
		hsqldbdir.setText(Prefs.getPref(PrefName.HSQLDBDIR));
		h2dir.setText(Prefs.getPref(PrefName.H2DIR));
		sqlitedir.setText(Prefs.getPref(PrefName.SQLITEDIR));

	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("DatabaseInformation");
	}

}
