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
package net.sf.borg.ui;

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

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.IOHelper;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.ExportImport;
import net.sf.borg.model.Model;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.undo.UndoLog;
import net.sf.borg.ui.ical.IcalModule;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.util.ScrolledDialog;

/**
 * The borg main menu bar
 * 
 */
class MainMenu {

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

		JMenuItem syncMI = new JMenuItem();
		syncMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Refresh16.gif")));
		syncMI.setText(Resource.getResourceString("Synchronize"));
		syncMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					Model.syncModels();
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});
		actionMenu.add(syncMI);

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

	private static void importZipMIActionPerformed() {

		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"*.zip,*.ZIP", "zip", "ZIP");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(IOHelper.getHomeDirectory());
		chooser.setDialogTitle(Resource
				.getResourceString("Please_choose_File_to_Import_From"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		String fileName = chooser.getSelectedFile().getAbsolutePath();
		IOHelper.setHomeDirectory(fileName);

		try {
			ExportImport.importFromZip(fileName);
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
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
				+ " (2003-2020) Michael Berger <mike@mbcsoft.com>\nhttp://mikeberger.github.io/borg_calendar/\n\n"
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
		info += dbtype + " URL: " + DBHelper.getController().buildURL()
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

	/** export */
	private static void exportMIActionPerformed() {

		// user wants to export the task and calendar DBs to an XML file
		String s;
		while (true) {
			// prompt for a directory to store the files
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(IOHelper.getHomeDirectory());
			chooser.setDialogTitle(Resource
					.getResourceString("Please_choose_directory_to_place_XML_files"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setApproveButtonText(Resource
					.getResourceString("select_export_dir"));

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			s = chooser.getSelectedFile().getAbsolutePath();
			IOHelper.setHomeDirectory(s);
			File dir = new File(s);
			String err = null;
			if (!dir.exists()) {
				err = Resource.getResourceString("Directory_[") + s
						+ Resource.getResourceString("]_does_not_exist");
			} else if (!dir.isDirectory()) {
				err = "[" + s
						+ Resource.getResourceString("]_is_not_a_directory");
			}

			if (err == null)
				break;

			Errmsg.getErrorHandler().notice(err);
		}

		try {
			ExportImport.exportToZip(s, false);
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	/** export preferences to an XML file */
	private static void expPrefs() {
		File file;
		while (true) {
			// prompt for a file
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(IOHelper.getHomeDirectory());
			chooser.setDialogTitle(Resource.getResourceString("choose_file"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showSaveDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			String s = chooser.getSelectedFile().getAbsolutePath();
			IOHelper.setHomeDirectory(s);
			file = new File(s);

			break;

		}

		try {
			if (checkOverwrite(file.getAbsolutePath()) == true)
				Prefs.export(file.getAbsolutePath());
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

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

	/** undo menu */
	private JMenu getUndoMenu() {
		JMenu m = new JMenu();
		m.setText(Resource.getResourceString("undo"));
		m.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Refresh16.gif")));
		final JMenu menu = m;
		m.addMenuListener(new MenuListener() {

			@Override
			public void menuCanceled(MenuEvent arg0) {
				// empty
			}

			@Override
			public void menuDeselected(MenuEvent arg0) {
				// empty
			}

			@Override
			public void menuSelected(MenuEvent e) {
				menu.removeAll();

				final String top = UndoLog.getReference().getTopItem();
				if (top != null) {
					JMenuItem mi = new JMenuItem(Resource
							.getResourceString("undo") + ": " + top);
					mi.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							int ret = JOptionPane.showConfirmDialog(
									null,
									Resource.getResourceString("undo")
											+ ": "
											+ top
											+ "\n\n"
											+ Resource
													.getResourceString("please_confirm"),
									"", JOptionPane.OK_CANCEL_OPTION);
							if (ret != JOptionPane.OK_OPTION)
								return;
							try {
								UndoLog.getReference().executeUndo();
							} catch (Exception e) {
								Errmsg.getErrorHandler().errmsg(e);
							}
						}

					});
					menu.add(mi);
					JMenuItem cmi = new JMenuItem();
					cmi.setText(Resource.getResourceString("clear_undos"));
					cmi.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							int ret = JOptionPane.showConfirmDialog(
									null,
									Resource.getResourceString("clear_undos")
											+ "\n\n"
											+ Resource
													.getResourceString("please_confirm"),
									"", JOptionPane.OK_CANCEL_OPTION);
							if (ret != JOptionPane.OK_OPTION)
								return;
							UndoLog.getReference().clear();
						}

					});
					menu.add(cmi);

					boolean show_stack = Prefs
							.getBoolPref(PrefName.SHOW_UNDO_STACK);
					if (show_stack == true) {
						JMenu all_mi = new JMenu(Resource
								.getResourceString("all_undos"));
						for (String item : UndoLog.getReference()
								.getItemStrings()) {
							JMenuItem item_mi = new JMenuItem(Resource
									.getResourceString("undo") + ": " + item);
							all_mi.add(item_mi);
						}

						menu.add(all_mi);
					}
				} else {
					menu.add(new JMenuItem(Resource
							.getResourceString("no_undos")));
				}

			}

		});

		return m;
	}

	/** import from file */
	private static void importMIActionPerformed() {
		try {

			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					Resource.getResourceString("xml_file"), "xml", "XML");
			chooser.setFileFilter(filter);
			chooser.setCurrentDirectory(IOHelper.getHomeDirectory());
			chooser.setDialogTitle(Resource
					.getResourceString("Please_choose_File_to_Import_From"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			String fileName = chooser.getSelectedFile().getAbsolutePath();
			IOHelper.setHomeDirectory(fileName);

			BufferedReader in = new BufferedReader(new FileReader(new File(
					fileName)));
			Model model = ExportImport.getImportModelForXML(in);
			in.close();
			if (model == null) {
				Errmsg.getErrorHandler().notice(
						Resource.getResourceString("import_format_error"));
				return;
			}

			int ret = JOptionPane.showConfirmDialog(
					null,
					Resource.getResourceString("Importing_") + " "
							+ model.getExportName() + ", OK?",
					Resource.getResourceString("Import_WARNING"),
					JOptionPane.OK_CANCEL_OPTION);

			if (ret != JOptionPane.OK_OPTION)
			{
				return;
			}

			try {
				ExportImport.importFromXmlFile(model, new FileInputStream(
						fileName));
			} catch (Exception e) {
				Errmsg.logError(e);
				Errmsg.getErrorHandler().notice(
						Resource.getResourceString("Import_error"));
			}
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	/** import preferences */
	private static void impPrefs() {
		File file;
		while (true) {
			// prompt for a file
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(IOHelper.getHomeDirectory());
			chooser.setDialogTitle(Resource.getResourceString("choose_file"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			String s = chooser.getSelectedFile().getAbsolutePath();
			IOHelper.setHomeDirectory(s);
			file = new File(s);

			break;

		}

		try {
			Prefs.importPrefs(file.getAbsolutePath());
		} catch (Exception e) {

			String err = Resource.getResourceString("import_format_error")
					+ ": " + e.getLocalizedMessage();
			Errmsg.getErrorHandler().notice(err);
		}

	}

}
