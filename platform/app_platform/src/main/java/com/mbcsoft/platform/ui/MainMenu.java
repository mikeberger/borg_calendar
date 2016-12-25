/*

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

 Copyright 2006 by Mike Berger
 */
package com.mbcsoft.platform.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mbcsoft.platform.common.Errmsg;
import com.mbcsoft.platform.common.IOHelper;
import com.mbcsoft.platform.common.PrefName;
import com.mbcsoft.platform.common.Prefs;
import com.mbcsoft.platform.common.Resource;
import com.mbcsoft.platform.model.Model;
import com.mbcsoft.platform.ui.util.ScrolledDialog;

/**
 * The borg main menu bar
 * 
 */
public class MainMenu {

	private JMenu actionMenu = new JMenu();
	private JMenu helpmenu = new JMenu();
	private JMenu optionsMenu = new JMenu();
	private JMenu pluginMenu = null;
	private JMenuBar menuBar = new JMenuBar();

	/**
	 * constructor
	 */
	public MainMenu() {

		menuBar.setBorder(new javax.swing.border.BevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		actionMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Application16.gif")));

		/*
		 * 
		 * Action Menu - will contain static items below and other actions
		 * inserted by UI Modules UI Module actions will be inserted above the
		 * items below
		 */
		ResourceHelper.setText(actionMenu, "Action");

		
		JMenuItem sqlMI = new JMenuItem();
		sqlMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Refresh16.gif")));
		ResourceHelper.setText(sqlMI, "RunSQL");
		sqlMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				new SqlRunner().setVisible(true);
			}
		});
		actionMenu.add(sqlMI);

		JMenuItem exitMenuItem = new JMenuItem();
		exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Stop16.gif")));
		exitMenuItem.setText(Resource.getResourceString("Exit"));
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				UIControl.shutDownUI();
			}
		});
		actionMenu.add(exitMenuItem);

		menuBar.add(actionMenu);

		/*
		 * 
		 * Option Menu
		 */

		optionsMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Preferences16.gif")));
		ResourceHelper.setText(optionsMenu, "Options");

		JMenuItem editPrefsMenuItem = new JMenuItem();
		ResourceHelper.setText(editPrefsMenuItem, "ep");
		editPrefsMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						OptionsView.getReference().setVisible(true);
					}
				});
		optionsMenu.add(editPrefsMenuItem);

		JMenuItem exportPrefsMI = new JMenuItem();
		exportPrefsMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Export16.gif")));
		ResourceHelper.setText(exportPrefsMI, "export_prefs");
		exportPrefsMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				expPrefs();
			}
		});
		optionsMenu.add(exportPrefsMI);

		JMenuItem importPrefsMI = new JMenuItem();
		importPrefsMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Import16.gif")));
		ResourceHelper.setText(importPrefsMI, "import_prefs");
		importPrefsMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				impPrefs();
			}
		});
		optionsMenu.add(importPrefsMI);

		menuBar.add(optionsMenu);

		/*
		 * category menu
		 */
		menuBar.add(CategoryChooser.getReference().getCategoryMenu());

		/*
		 * 
		 * Import/Export Menu
		 */
		JMenu impexpMenu = new JMenu();
		impexpMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Export16.gif")));
		ResourceHelper.setText(impexpMenu, "impexpMenu");

		JMenuItem importMI = new JMenuItem();
		importMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				importMIActionPerformed();
			}
		});
		importMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Import16.gif")));
		ResourceHelper.setText(importMI, "impXML");
		impexpMenu.add(importMI);

		JMenuItem importZipMI = new JMenuItem();
		ResourceHelper.setText(importZipMI, "import_zip");
		importZipMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				importZipMIActionPerformed();
			}
		});
		importZipMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Import16.gif")));
		impexpMenu.add(importZipMI);

		JMenuItem exportMI = new JMenuItem();
		exportMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exportMIActionPerformed();
			}
		});
		exportMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Export16.gif")));
		ResourceHelper.setText(exportMI, "expXML");
		impexpMenu.add(exportMI);

		menuBar.add(impexpMenu);

		/*
		 * 
		 * Undo Menu
		 */
		menuBar.add(getUndoMenu());

		menuBar.add(IcalModule.getIcalMenu());
		/*
		 * plugin menu
		 */
		menuBar.add(getPluginMenu());

		/*
		 * spacing
		 */
		menuBar.add(Box.createHorizontalGlue());

		/*
		 * 
		 * Help Menu
		 */
		helpmenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Help16.gif")));
		ResourceHelper.setText(helpmenu, "Help");

		JMenuItem helpMI = new JMenuItem();
		ResourceHelper.setText(helpMI, "Help");
		helpMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					HelpLauncher.launchHelp();
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});
		helpmenu.add(helpMI);

		JMenuItem dbMI = new JMenuItem();
		ResourceHelper.setText(dbMI, "DatabaseInformation");
		dbMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dbMIActionPerformed();
			}
		});
		helpmenu.add(dbMI);

		JMenuItem AboutMI = new JMenuItem();
		ResourceHelper.setText(AboutMI, "About");
		AboutMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				AboutMIActionPerformed();
			}
		});

		helpmenu.add(AboutMI);
		helpmenu.insertSeparator(helpmenu.getItemCount() - 1);

		menuBar.add(helpmenu);

		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		if (dbtype.equals("mysql") || dbtype.equals("jdbc")) {
			syncMI.setEnabled(true);
		} else {
			syncMI.setEnabled(false);
		}

	}

	
	/**
	 * Add an action to the action menu
	 * 
	 * @param icon
	 *            the icon for the menu item
	 * @param text
	 *            the text for the menu item
	 * @param action
	 *            the action listener for the menu item
	 * @param insertIndex
	 *            the index to insert the menu item at
	 */
	public void addAction(Icon icon, String text, ActionListener action,
			int insertIndex) {
		JMenuItem item = new JMenuItem();
		item.setIcon(icon);
		item.setText(text);
		item.addActionListener(action);

		actionMenu.insert(item, insertIndex);
	}

	/**
	 * add an item to the help menu
	 * 
	 * @param icon
	 *            the icon for the menu item
	 * @param text
	 *            the menu item text
	 * @param action
	 *            the menu item action
	 */
	public void addHelpMenuItem(Icon icon, String text, ActionListener action) {
		JMenuItem item = new JMenuItem();
		item.setIcon(icon);
		item.setText(text);
		item.addActionListener(action);

		// always insert above About item and its separator bar
		helpmenu.insert(item, helpmenu.getItemCount() - 2);
	}

	/**
	 * add a menu item to the options menu
	 * 
	 * @param item
	 *            the item
	 */
	public void addOptionsMenuItem(JMenuItem item) {
		optionsMenu.add(item);
	}

	/**
	 * show the about window
	 * 
	 */
	static public void AboutMIActionPerformed() {

		// show the About data
		String build_info = "";
		String version = "";
		try {
			// get the version and build info from a properties file in the
			// jar
			// file
			InputStream is = MainMenu.class.getResource("/properties")
					.openStream();
			Properties props = new Properties();
			props.load(is);
			is.close();
			version = Resource.getVersion();
			build_info = Resource.getResourceString("Build_Time:_")
					+ props.getProperty("build.time");
			build_info += "\nGit: " + props.getProperty("build.number");

		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

		// build and show the version info.

		String info = Resource.getResourceString("Berger-Organizer_v")
				+ version
				+ "\n"
				+ Resource.getResourceString("copyright")
				+ " (2003-2016) Michael Berger <i_flem@users.sourceforge.net>\nhttp://borg-calendar.sourceforge.net\n\n"
				+ Resource.getResourceString("contributions_by") + "\n"
				+ Resource.getResourceString("contrib") + "\n"
				+ Resource.getResourceString("translations") + "\n\n"
				+ build_info + "\n" + "Java "
				+ System.getProperty("java.version");
		Object opts[] = { Resource.getResourceString("Dismiss") };
		JOptionPane
				.showOptionDialog(
						null,
						info,
						Resource.getResourceString("About_BORG"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE,
						new ImageIcon(MainMenu.class
								.getResource("/resource/borg.jpg")), opts,
						opts[0]);

	}

	/** show database info */
	private static void dbMIActionPerformed() {
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		String info = Resource.getResourceString("DatabaseInformation")
				+ ":\n\n";
		info += dbtype + " URL: " + DBController.getController().buildURL()
				+ "\n\n";

		try {
			for (Model model : Model.getExistingModels()) {
				info += model.getInfo() + "\n";
			}
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
			return;
		}

		ScrolledDialog.showNotice(info);
	}

	
	static public boolean checkOverwrite(String fname) {

		File f = new File(fname);
		if (!f.exists())
			return true;

		int ret = JOptionPane.showConfirmDialog(
				null,
				net.sf.borg.common.Resource
						.getResourceString("overwrite_warning") + fname + " ?",
				"confirm_overwrite", JOptionPane.OK_CANCEL_OPTION);
		if (ret != JOptionPane.OK_OPTION)
			return false;

		return (true);
	}

	/**
	 * get the menu bar
	 * 
	 * @return the menu bar
	 */
	public JMenuBar getMenuBar() {

		return menuBar;
	}

	/** plugin menu */
	private JMenu getPluginMenu() {
		if (pluginMenu == null) {
			pluginMenu = new JMenu();
			pluginMenu.setText(Resource.getResourceString("Plugins"));
			pluginMenu.setIcon(new javax.swing.ImageIcon(getClass()
					.getResource("/resource/Preferences16.gif")));
			pluginMenu.setVisible(false);
		}
		return pluginMenu;
	}

	/**
	 * add a sub menu to the plugin menu
	 * 
	 * @param menu
	 *            the sub menu
	 */
	public void addPluginSubMenu(JMenu menu) {
		pluginMenu.add(menu);
		pluginMenu.setVisible(true);
	}

	
	

}
