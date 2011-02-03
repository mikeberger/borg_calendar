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
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.control.Borg;
import net.sf.borg.model.ExportImport;
import net.sf.borg.model.Model;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.undo.UndoLog;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.task.TaskConfigurator;
import net.sf.borg.ui.util.ScrolledDialog;

// TODO - javadoc not really done, still contains way too much logic
/**
 * The borg main menu bar
 * 
 */
class MainMenu {

	private JMenu actionMenu = new JMenu();
	private JMenu helpmenu = new JMenu();
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
					Borg.syncDBs();
				} catch (Exception e) {
					Errmsg.errmsg(e);
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
		JMenu OptionMenu = new JMenu();

		OptionMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Preferences16.gif")));
		ResourceHelper.setText(OptionMenu, "Options");

		JMenuItem editPrefsMenuItem = new JMenuItem();
		ResourceHelper.setText(editPrefsMenuItem, "ep");
		editPrefsMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						OptionsView.getReference().setVisible(true);
					}
				});
		OptionMenu.add(editPrefsMenuItem);

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
		OptionMenu.add(exportPrefsMI);

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
		OptionMenu.add(importPrefsMI);

		/*
		 * Task State Options Sub Menu
		 */
		JMenu tsm = new JMenu(Resource.getResourceString("task_state_options"));
		JMenuItem edittypes = new JMenuItem();
		JMenuItem resetst = new JMenuItem();
		ResourceHelper.setText(edittypes, "edit_types");
		edittypes.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					TaskConfigurator.getReference().setVisible(true);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		tsm.add(edittypes);

		ResourceHelper.setText(resetst, "Reset_Task_States_to_Default");
		resetst.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetstActionPerformed();
			}
		});

		tsm.add(resetst);
		OptionMenu.add(tsm);
		menuBar.add(OptionMenu);

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
					Errmsg.errmsg(e);
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

		menuBar.add(helpmenu);

		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		if (dbtype.equals("mysql") || dbtype.equals("jdbc")) {
			syncMI.setEnabled(true);
		} else {
			syncMI.setEnabled(false);
		}

	}

	private void importZipMIActionPerformed() {
		
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "*.zip,*.ZIP", "zip", "ZIP");
		    chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(new File("."));
		chooser.setDialogTitle(Resource
				.getResourceString("Please_choose_File_to_Import_From"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		String fileName = chooser.getSelectedFile().getAbsolutePath();

		try {
			ExportImport.importFromZip(fileName);
		} catch (Exception e) {
			Errmsg.errmsg(e);
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

		helpmenu.add(item);
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
			build_info = 
				Resource.getResourceString("Build_Time:_")
				+ props.getProperty("build.time");

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// build and show the version info.

		String info = Resource.getResourceString("Berger-Organizer_v")
				+ version
				+ "\n"
				+ Resource.getResourceString("copyright")
				+ " (2003-2011) Michael Berger <i_flem@users.sourceforge.net>\nhttp://borg-calendar.sourceforge.net\n\n"
				+ Resource.getResourceString("contributions_by") + "\n"
				+ Resource.getResourceString("contrib") + "\n"
				+ Resource.getResourceString("translations") + "\n\n"
				+ build_info + "\n" + "Java "
				+ System.getProperty("java.version");
		Object opts[] = { Resource.getResourceString("Dismiss") };
		JOptionPane.showOptionDialog(null, info, Resource
				.getResourceString("About_BORG"), JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE, new ImageIcon(MainMenu.class
						.getResource("/resource/borg.jpg")), opts, opts[0]);

	}

	/** show database info */
	private void dbMIActionPerformed() {
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		String info = Resource.getResourceString("DatabaseInformation")
				+ ":\n\n";
		info += dbtype + " URL: " + JdbcDB.getUrl() + "\n\n";

		try {
			for( Model model : Model.getExistingModels())
			{
				info += model.getInfo() + "\n";
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}

		ScrolledDialog.showNotice(info);
	}

	/** export */
	private void exportMIActionPerformed() {

		// user wants to export the task and calendar DBs to an XML file
		String s;
		while (true) {
			// prompt for a directory to store the files
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(new File("."));
			chooser
					.setDialogTitle(Resource
							.getResourceString("Please_choose_directory_to_place_XML_files"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setApproveButtonText(Resource
					.getResourceString("select_export_dir"));

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			s = chooser.getSelectedFile().getAbsolutePath();
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

			Errmsg.notice(err);
		}

		try {
			ExportImport.exportToZip(s, false);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/** export preferences to an XML file */
	private void expPrefs() {
		File file;
		while (true) {
			// prompt for a file
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(new File("."));
			chooser.setDialogTitle(Resource.getResourceString("choose_file"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			String s = chooser.getSelectedFile().getAbsolutePath();
			file = new File(s);

			break;

		}

		try {
			Prefs.export(file.getAbsolutePath());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

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
							.getResourceString("undo")
							+ ": " + top);
					mi.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							int ret = JOptionPane
									.showConfirmDialog(
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
							UndoLog.getReference().executeUndo();
						}

					});
					menu.add(mi);
					JMenuItem cmi = new JMenuItem();
					cmi.setText(Resource.getResourceString("clear_undos"));
					cmi.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							int ret = JOptionPane
									.showConfirmDialog(
											null,
											Resource
													.getResourceString("clear_undos")
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
									.getResourceString("undo")
									+ ": " + item);
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
	private void importMIActionPerformed() {
		try {

			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(new File("."));
			chooser.setDialogTitle(Resource
					.getResourceString("Please_choose_File_to_Import_From"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			String fileName = chooser.getSelectedFile().getAbsolutePath();

			BufferedReader in = new BufferedReader(new FileReader(
					new File(fileName)));
			Model model = ExportImport.getImportModelForXML(in);

			int ret = JOptionPane.showConfirmDialog(null, Resource
					.getResourceString("Importing_")
					+ " " + model.getExportName() + ", OK?", Resource
					.getResourceString("Import_WARNING"),
					JOptionPane.OK_CANCEL_OPTION);

			if (ret != JOptionPane.OK_OPTION)
				return;

			ExportImport.importFromXmlFile(model, new FileInputStream(fileName));
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/** import preferences */
	private void impPrefs() {
		File file;
		while (true) {
			// prompt for a file
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(new File("."));
			chooser.setDialogTitle(Resource.getResourceString("choose_file"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			String s = chooser.getSelectedFile().getAbsolutePath();
			file = new File(s);

			break;

		}

		try {
			Prefs.importPrefs(file.getAbsolutePath());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * reset task state action
	 */
	private void resetstActionPerformed() {
		try {
			String msg = Resource.getResourceString("reset_state_warning");
			int ret = JOptionPane.showConfirmDialog(null, msg, Resource
					.getResourceString("Import_WARNING"),
					JOptionPane.OK_CANCEL_OPTION);

			if (ret != JOptionPane.OK_OPTION)
				return;
			TaskModel taskmod_ = TaskModel.getReference();
			taskmod_.getTaskTypes().loadDefault();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

}
