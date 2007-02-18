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

 Copyright 2006 by Mike Berger
 */
package net.sf.borg.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.sf.borg.common.io.IOHelper;
import net.sf.borg.common.ui.OverwriteConfirm;
import net.sf.borg.common.ui.ScrolledDialog;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.XTree;
import net.sf.borg.control.Borg;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentIcalAdapter;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.AppointmentVcalAdapter;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.BeanDataFactoryFactory;

public class MainMenu {
    private JMenu ActionMenu = new javax.swing.JMenu();

    // private JMenuItem TaskTrackMI = new javax.swing.JMenuItem();

    private JMenuItem ToDoMenu = new javax.swing.JMenuItem();

    private JMenuItem AddressMI = new javax.swing.JMenuItem();

    private JMenuItem SearchMI = new javax.swing.JMenuItem();

    private JMenuItem PrintMonthMI = new javax.swing.JMenuItem();

    private JMenuItem printprev = new javax.swing.JMenuItem();

    private JMenuItem syncMI = new javax.swing.JMenuItem();

    private JMenuItem exitMenuItem = new javax.swing.JMenuItem();

    private JMenu OptionMenu = new javax.swing.JMenu();

    private JMenuItem jMenuItem1 = new javax.swing.JMenuItem();

    private JMenu navmenu = new javax.swing.JMenu();

    private JMenuItem nextmi = new javax.swing.JMenuItem();

    private JMenuItem prevmi = new javax.swing.JMenuItem();

    private JMenuItem todaymi = new javax.swing.JMenuItem();

    private JMenuItem gotomi = new javax.swing.JMenuItem();

    private JMenu catmenu = new javax.swing.JMenu();

    private JMenuItem jMenuItem2 = new javax.swing.JMenuItem();

    private JMenuItem jMenuItem3 = new javax.swing.JMenuItem();

    private JMenuItem jMenuItem4 = new javax.swing.JMenuItem();

    private JMenu impexpMenu = new javax.swing.JMenu();

    private JMenu impXML = new javax.swing.JMenu();

    private JMenuItem importMI = new javax.swing.JMenuItem();

    private JMenuItem impurl = new javax.swing.JMenuItem();

    private JMenu expXML = new javax.swing.JMenu();

    private JMenuItem exportMI = new javax.swing.JMenuItem();

    private JMenuItem expurl = new javax.swing.JMenuItem();

    private JMenuItem impical = new javax.swing.JMenuItem();

    private JMenuItem expical = new javax.swing.JMenuItem();

    private JMenu helpmenu = new javax.swing.JMenu();

    private JMenuItem helpMI = new javax.swing.JMenuItem();

    private JMenuItem licsend = new javax.swing.JMenuItem();

    private JMenuItem chglog = new javax.swing.JMenuItem();
    
    private JMenuItem rlsnotes = new javax.swing.JMenuItem();

    private JMenuItem AboutMI = new javax.swing.JMenuItem();

    private JMenuItem dbMI = new javax.swing.JMenuItem();

    private JMenuBar menuBar = new JMenuBar();

    JMenuItem sqlMI = new JMenuItem();

    Navigator nav_ = null;

    private void edittypesActionPerformed(java.awt.event.ActionEvent evt) {
	try {
	    TaskConfigurator.getReference().setVisible(true);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }

    private void resetstActionPerformed(java.awt.event.ActionEvent evt) {
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

    private void impstActionPerformed(java.awt.event.ActionEvent evt) {

	// import a new task type and status model from a user XML file

	try {
	    String msg = Resource.getResourceString("import_state_warning");
	    int ret = JOptionPane.showConfirmDialog(null, msg, Resource
		    .getResourceString("Import_WARNING"),
		    JOptionPane.OK_CANCEL_OPTION);

	    if (ret != JOptionPane.OK_OPTION)
		return;

	    InputStream istr = IOHelper.fileOpen(".", Resource
		    .getResourceString("Please_choose_File_to_Import_From"));

	    TaskModel taskmod_ = TaskModel.getReference();
	    taskmod_.getTaskTypes().importStates(istr);
	    taskmod_.saveTaskTypes(null);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

    }

    private void expstActionPerformed(java.awt.event.ActionEvent evt) {

	// export the current task type/state model to an XML file
	try {
	    TaskModel taskmod_ = TaskModel.getReference();
	    ByteArrayOutputStream ostr = new ByteArrayOutputStream();
	    taskmod_.getTaskTypes().exportStates(ostr);
	    byte[] buf = ostr.toByteArray();
	    ByteArrayInputStream istr = new ByteArrayInputStream(buf);

	    // Export XML to the file
	    IOHelper.fileSave(".", istr, "state_model.exp");
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }

    public MainMenu(Navigator nav) {
	nav_ = nav;
	menuBar.setBorder(new javax.swing.border.BevelBorder(
		javax.swing.border.BevelBorder.RAISED));
	ActionMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Application16.gif")));
	ResourceHelper.setText(ActionMenu, "Action");

	ToDoMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Properties16.gif")));
	ResourceHelper.setText(ToDoMenu, "To_Do");
	ToDoMenu.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		ToDoMenuActionPerformed(evt);
	    }
	});

	ActionMenu.add(ToDoMenu);

	AddressMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/WebComponent16.gif")));
	ResourceHelper.setText(AddressMI, "Address_Book");
	AddressMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		AddressMIActionPerformed(evt);
	    }
	});

	ActionMenu.add(AddressMI);

	SearchMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Find16.gif")));
	ResourceHelper.setText(SearchMI, "srch");
	SearchMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		SearchMIActionPerformed(evt);
	    }
	});

	ActionMenu.add(SearchMI);

	PrintMonthMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Print16.gif")));

	ResourceHelper.setText(PrintMonthMI, "Print");
	PrintMonthMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		PrintMonthMIActionPerformed(evt);
	    }
	});

	ActionMenu.add(PrintMonthMI);

	printprev.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/PrintPreview16.gif")));
	printprev.setText(Resource.getPlainResourceString("Month") + " "
		+ Resource.getPlainResourceString("pprev"));
	printprev.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		printprevActionPerformed(evt);
	    }
	});

	ActionMenu.add(printprev);

	syncMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Refresh16.gif")));
	ResourceHelper.setText(syncMI, "Synchronize");
	syncMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		syncMIActionPerformed(evt);
	    }
	});

	ActionMenu.add(syncMI);

	sqlMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Refresh16.gif")));
	ResourceHelper.setText(sqlMI, "RunSQL");
	sqlMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		new SqlRunner().setVisible(true);
	    }
	});

	ActionMenu.add(sqlMI);

	JMenuItem newWindowMI = new JMenuItem();
	ResourceHelper.setText(newWindowMI, "New_Window");
	newWindowMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		MultiView.openNewView();
	    }
	});

	ActionMenu.add(newWindowMI);

	exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Stop16.gif")));
	ResourceHelper.setText(exitMenuItem, "Exit");
	exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		exitMenuItemActionPerformed(evt);
	    }
	});

	ActionMenu.add(exitMenuItem);

	menuBar.add(ActionMenu);

	OptionMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Preferences16.gif")));
	ResourceHelper.setText(OptionMenu, "Options");
	ResourceHelper.setText(jMenuItem1, "ep");
	jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		jMenuItem1ActionPerformed(evt);
	    }
	});

	OptionMenu.add(jMenuItem1);
	
	JMenu tsm = new JMenu(Resource.getPlainResourceString("task_state_options"));
	JMenuItem edittypes = new JMenuItem();
	JMenuItem impst = new JMenuItem();
	JMenuItem expst = new JMenuItem();
	JMenuItem resetst = new JMenuItem();
	ResourceHelper.setText(edittypes, "edit_types");
	edittypes.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		edittypesActionPerformed(evt);
	    }
	});

	tsm.add(edittypes);

	ResourceHelper.setText(impst, "Import_Task_States_From_XML");
	impst.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		impstActionPerformed(evt);
	    }
	});

	tsm.add(impst);

	ResourceHelper.setText(expst, "Export_Task_States_to_XML");
	expst.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		expstActionPerformed(evt);
	    }
	});

	tsm.add(expst);

	ResourceHelper.setText(resetst, "Reset_Task_States_to_Default");
	resetst.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		resetstActionPerformed(evt);
	    }
	});

	tsm.add(resetst);
	OptionMenu.add(tsm);
	menuBar.add(OptionMenu);

	navmenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Refresh16.gif")));
	ResourceHelper.setText(navmenu, "navmenu");
	ResourceHelper.setText(nextmi, "Next");
	nextmi.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		nav_.next();
	    }
	});

	navmenu.add(nextmi);

	ResourceHelper.setText(prevmi, "Previous");
	prevmi.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		PrevActionPerformed(evt);
	    }
	});

	navmenu.add(prevmi);

	ResourceHelper.setText(todaymi, "Today");
	todaymi.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		nav_.today();
	    }
	});

	navmenu.add(todaymi);

	ResourceHelper.setText(gotomi, "Goto");
	gotomi.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		// GOTO a particular month
		DateDialog dlg = new DateDialog(null);
		Calendar cal = new GregorianCalendar();
		cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));
		dlg.setCalendar(cal);
		dlg.setVisible(true);
		Calendar dlgcal = dlg.getCalendar();
		if (dlgcal == null)
		    return;
		nav_.goTo(dlgcal);
	    }
	});

	navmenu.add(gotomi);

	menuBar.add(navmenu);

	catmenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Preferences16.gif")));
	ResourceHelper.setText(catmenu, "Categories");
	jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Preferences16.gif")));
	ResourceHelper.setText(jMenuItem2, "choosecat");
	jMenuItem2.setActionCommand("Choose Displayed Categories");
	jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		jMenuItem2ActionPerformed(evt);
	    }
	});

	catmenu.add(jMenuItem2);

	jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Add16.gif")));
	ResourceHelper.setText(jMenuItem3, "addcat");
	jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		jMenuItem3ActionPerformed(evt);
	    }
	});

	catmenu.add(jMenuItem3);

	jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Delete16.gif")));
	ResourceHelper.setText(jMenuItem4, "remcat");
	jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		jMenuItem4ActionPerformed(evt);
	    }
	});

	catmenu.add(jMenuItem4);

	menuBar.add(catmenu);

	impexpMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Export16.gif")));
	ResourceHelper.setText(impexpMenu, "impexpMenu");
	impXML.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Import16.gif")));
	ResourceHelper.setText(impXML, "impXML");
	ResourceHelper.setText(importMI, "impmenu");
	importMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		importMIActionPerformed(evt);
	    }
	});

	impXML.add(importMI);

	ResourceHelper.setText(impurl, "impurl");
	impurl.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		impurlActionPerformed(evt);
	    }
	});

	impXML.add(impurl);

	impexpMenu.add(impXML);

	expXML.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Export16.gif")));
	ResourceHelper.setText(expXML, "expXML");
	ResourceHelper.setText(exportMI, "expmenu");
	exportMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		exportMIActionPerformed(evt);
	    }
	});

	expXML.add(exportMI);

	ResourceHelper.setText(expurl, "expurl");
	expurl.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		expurlActionPerformed(evt);
	    }
	});

	expXML.add(expurl);

	impexpMenu.add(expXML);

	impical.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Import16.gif")));
	ResourceHelper.setText(impical, "importical");
	impical.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		impicalActionPerformed(evt);
	    }
	});

	impexpMenu.add(impical);

	expical.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Export16.gif")));
	ResourceHelper.setText(expical, "export_ical");
	expical.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		expicalActionPerformed(evt);
	    }
	});

	impexpMenu.add(expical);

	menuBar.add(impexpMenu);

	JMenu userMenu = new JMenu();
	userMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Preferences16.gif")));
	ResourceHelper.setText(userMenu, "users");
	JMenuItem userChooserMI = new JMenuItem();
	ResourceHelper.setText(userChooserMI, "selectusers");
	userChooserMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		if (dbtype.equals("remote"))
		    UserChooser.getReference().setVisible(true);
		else
		    Errmsg.notice(Resource
			    .getPlainResourceString("multiusernotice"));
	    }
	});
	JCheckBoxMenuItem publicMI = new JCheckBoxMenuItem();
	try {
	    publicMI.setSelected(AppointmentModel.getReference().isPublic());
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
	ResourceHelper.setText(publicMI, "publiccal");
	publicMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		JCheckBoxMenuItem source = (JCheckBoxMenuItem) evt.getSource();
		try {
		    if (source.isSelected()) {
			AppointmentModel.getReference().setPublic(true);
		    } else {
			AppointmentModel.getReference().setPublic(false);
		    }
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		}
	    }
	});
	userMenu.add(userChooserMI);
	userMenu.add(publicMI);
	menuBar.add(userMenu);
	menuBar.add(getReportMenu());
	menuBar.add(Box.createHorizontalGlue());

	String dbtype = Prefs.getPref(PrefName.DBTYPE);
	if (dbtype.equals("local")) {
	    JMenu warning = new JMenu();
	    warning.setIcon(new javax.swing.ImageIcon(getClass().getResource(
	    "/resource/redball.gif")));
	    warning.addMenuListener(new MenuListener() {

		public void menuCanceled(MenuEvent arg0) {}

		public void menuDeselected(MenuEvent arg0) {}

		public void menuSelected(MenuEvent arg0) {
		    Errmsg.notice(Resource.getPlainResourceString("mdb_deprecated"));}
	    });
	    menuBar.add(warning);
	}
	//
	// help menu
	//

	helpmenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		"/resource/Help16.gif")));
	ResourceHelper.setText(helpmenu, "Help");
	ResourceHelper.setText(helpMI, "Help");
	helpMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		helpMIActionPerformed(evt);
	    }
	});

	helpmenu.add(helpMI);

	ResourceHelper.setText(licsend, "License");
	licsend.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		licsendActionPerformed(evt);
	    }
	});

	helpmenu.add(licsend);

	ResourceHelper.setText(chglog, "viewchglog");
	chglog.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		chglogActionPerformed(evt);
	    }
	});

	helpmenu.add(chglog);
	
	ResourceHelper.setText(rlsnotes, "rlsnotes");
	rlsnotes.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		new HelpScreen("/resource/RELEASE_NOTES.txt").setVisible(true);
	    }
	});

	helpmenu.add(rlsnotes);

	ResourceHelper.setText(dbMI, "DatabaseInformation");
	dbMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		dbMIActionPerformed(evt);
	    }
	});

	helpmenu.add(dbMI);

	ResourceHelper.setText(AboutMI, "About");
	AboutMI.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		AboutMIActionPerformed(evt);
	    }
	});

	helpmenu.add(AboutMI);

	menuBar.add(helpmenu);
	catmenu.add(getDelcatMI());
	impexpMenu.add(getExpvcal());

	String shared = Prefs.getPref(PrefName.SHARED);
	
	if (shared.equals("true") || dbtype.equals("remote")
		|| dbtype.equals("mysql")) {
	    syncMI.setEnabled(true);
	} else {
	    syncMI.setEnabled(false);
	}

	if (dbtype.equals("hsqldb") || dbtype.equals("mysql"))
	    sqlMI.setEnabled(true);
	else
	    sqlMI.setEnabled(false);
	importMI.setEnabled(true);
	exportMI.setEnabled(true);
	impical.setEnabled(true);
	expical.setEnabled(true);

    }

    public JMenuBar getMenuBar() {

	return menuBar;
    }

    private void dbMIActionPerformed(java.awt.event.ActionEvent evt) {
	String dbtype = Prefs.getPref(PrefName.DBTYPE);
	String info = Resource.getPlainResourceString("DatabaseInformation")
		+ ":\n\n";
	info += dbtype + " URL: " + BeanDataFactoryFactory.buildDbDir()
		+ "\n\n";

	try {
	    info += Resource.getPlainResourceString("appointments") + ": "
		    + AppointmentModel.getReference().getAllAppts().size()
		    + "\n";
	    info += Resource.getPlainResourceString("addresses") + ": "
		    + AddressModel.getReference().getAddresses().size() + "\n";
	    info += Resource.getPlainResourceString("tasks") + ": "
		    + TaskModel.getReference().getTasks().size() + "\n";

	} catch (Exception e) {
	    Errmsg.errmsg(e);
	    return;
	}

	ScrolledDialog.showNotice(info);
    }

    // create an OutputStream from a URL string
    private OutputStream createOutputStreamFromURL(String urlstr)
	    throws Exception {

	return IOHelper.createOutputStream(new URL(urlstr));
    }

    private void impicalActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_impicalActionPerformed
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
	    CategoryModel catmod = CategoryModel.getReference();
	    Collection allcats = catmod.getCategories();
	    Object[] cats = allcats.toArray();

	    Object o = JOptionPane.showInputDialog(null, Resource
		    .getResourceString("import_cat_choose"), "",
		    JOptionPane.QUESTION_MESSAGE, null, cats, cats[0]);
	    if (o == null)
		return;

	    String warnings = AppointmentIcalAdapter.importIcal(file
		    .getAbsolutePath(), (String) o);

	    if (warnings != null)
		Errmsg.notice(warnings);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	} catch (NoClassDefFoundError ne) {
	    Errmsg.notice("Cannot find ICal library, import disabled");
	}

    }// GEN-LAST:event_impicalActionPerformed

    private void expicalActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_expicalActionPerformed
	// user wants to export the task and calendar DBs to an iCal file
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
	    AppointmentIcalAdapter.exportIcal(file.getAbsolutePath());
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	} catch (NoClassDefFoundError ne) {
	    Errmsg.notice("Cannot find ICal library, export disabled");
	}

    }// GEN-LAST:event_expicalActionPerformed

    private void expurlActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_expurlActionPerformed
    {// GEN-HEADEREND:event_expurlActionPerformed
	try {
	    String prevurl = Prefs.getPref(PrefName.LASTEXPURL);
	    String urlst = JOptionPane.showInputDialog(Resource
		    .getResourceString("enturl"), prevurl);
	    if (urlst == null || urlst.equals(""))
		return;
	    Prefs.putPref(PrefName.LASTEXPURL, urlst);
	    expURLCommon(urlst);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }// GEN-LAST:event_expurlActionPerformed

    private void impurlActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_impurlActionPerformed
	try {
	    String prevurl = Prefs.getPref(PrefName.LASTIMPURL);
	    String urlst = JOptionPane.showInputDialog(Resource
		    .getResourceString("enturl"), prevurl);
	    if (urlst == null || urlst.equals(""))
		return;

	    Prefs.putPref(PrefName.LASTIMPURL, urlst);
	    URL url = new URL(urlst);
	    impURLCommon(urlst, url.openStream());
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }// GEN-LAST:event_impurlActionPerformed

    private void syncMIActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_syncMIActionPerformed
    {// GEN-HEADEREND:event_syncMIActionPerformed
	try {
	    Borg.syncDBs();
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }// GEN-LAST:event_syncMIActionPerformed

    private void chglogActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_chglogActionPerformed
    {// GEN-HEADEREND:event_chglogActionPerformed
	new HelpScreen("/resource/CHANGES.txt").setVisible(true);
    }// GEN-LAST:event_chglogActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem3ActionPerformed
	// add new category

	String inputValue = JOptionPane.showInputDialog(Resource
		.getResourceString("AddCat"));
	if (inputValue == null || inputValue.equals(""))
	    return;
	try {
	    CategoryModel.getReference().addCategory(inputValue);
	    CategoryModel.getReference().showCategory(inputValue);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }// GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem4ActionPerformed
	try {
	    CategoryModel.getReference().syncCategories();
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }// GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem2ActionPerformed
	CategoryChooser.getReference().setVisible(true);
    }// GEN-LAST:event_jMenuItem2ActionPerformed

    private void AddressMIActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_AddressMIActionPerformed
    {// GEN-HEADEREND:event_AddressMIActionPerformed
	AddrListView ab = AddrListView.getReference();
	ab.refresh();
	ab.setVisible(true);
    }// GEN-LAST:event_AddressMIActionPerformed

    private void impCommon(XTree xt) throws Exception {
	String type = xt.name();
	if (!type.equals("TASKS") && !type.equals("APPTS")
		&& !type.equals("MEMOS") && !type.equals("ADDRESSES"))
	    throw new Exception(
		    Resource
			    .getResourceString("Could_not_determine_if_the_import_file_was_for_TASKS,_APPTS,_or_ADDRESSES,_check_the_XML"));

	int ret = JOptionPane.showConfirmDialog(null, Resource
		.getResourceString("Importing_")
		+ type + ", OK?", Resource.getResourceString("Import_WARNING"),
		JOptionPane.OK_CANCEL_OPTION);

	if (ret != JOptionPane.OK_OPTION)
	    return;

	if (type.equals("TASKS")) {
	    TaskModel taskmod = TaskModel.getReference();
	    taskmod.importXml(xt);
	} else if (type.equals("APPTS")) {
	    AppointmentModel calmod = AppointmentModel.getReference();
	    calmod.importXml(xt);
	} else if (type.equals("MEMOS")) {
	    MemoModel memomod = MemoModel.getReference();
	    memomod.importXml(xt);
	} else {
	    AddressModel addrmod = AddressModel.getReference();
	    addrmod.importXml(xt);
	}

	// show any newly imported categories
	CategoryModel.getReference().syncCategories();
	CategoryModel.getReference().showAll();
    }

    private void impURLCommon(String url, InputStream istr) throws Exception {
	XTree xt = XTree.readFromStream(istr);
	if (xt == null)
	    throw new Exception(Resource.getResourceString("Could_not_parse_")
		    + url);
	// System.out.println(xt.toString());
	impCommon(xt);
    }

    private void expURLCommon(String url) throws Exception {
	OutputStream fos = createOutputStreamFromURL(url + "/borg.xml");
	Writer fw = new OutputStreamWriter(fos, "UTF8");
	AppointmentModel.getReference().export(fw);
	fw.close();

	fos = createOutputStreamFromURL(url + "/mrdb.xml");
	fw = new OutputStreamWriter(fos, "UTF8");
	TaskModel.getReference().export(fw);
	fw.close();

	fos = createOutputStreamFromURL(url + "/addr.xml");
	fw = new OutputStreamWriter(fos, "UTF8");
	AddressModel.getReference().export(fw);
	fw.close();

	fos = createOutputStreamFromURL(url + "/memo.xml");
	fw = new OutputStreamWriter(fos, "UTF8");
	MemoModel.getReference().export(fw);
	fw.close();
    }

    private void importMIActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_importMIActionPerformed
    {// GEN-HEADEREND:event_importMIActionPerformed
	try {
	    // String msg =
	    // Resource.getResourceString("This_will_import_tasks,_addresses_or_appointments_into_an_**EMPTY**_database...continue?");
	    // int ret = JOptionPane.showConfirmDialog(null, msg,
	    // Resource.getResourceString("Import_WARNING"),
	    // JOptionPane.OK_CANCEL_OPTION);

	    // if( ret != JOptionPane.OK_OPTION )
	    // return;
	    InputStream istr = IOHelper.fileOpen(".", Resource
		    .getResourceString("Please_choose_File_to_Import_From"));

	    if (istr == null)
		return;

	    // parse xml file
	    XTree xt = XTree.readFromStream(istr);
	    istr.close();
	    if (xt == null)
		throw new Exception(Resource
			.getResourceString("Could_not_parse_")
			+ "XML");

	    impCommon(xt);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }// GEN-LAST:event_importMIActionPerformed

    /**
         * This method initializes delcatMI
         * 
         * @return javax.swing.JMenuItem
         */
    private JMenuItem delcatMI;

    private JMenuItem getDelcatMI() {
	if (delcatMI == null) {
	    delcatMI = new JMenuItem();
	    ResourceHelper.setText(delcatMI, "delete_cat");
	    delcatMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		    "/resource/Delete16.gif")));
	    delcatMI.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {

		    try {
			CategoryModel catmod = CategoryModel.getReference();
			Collection allcats = catmod.getCategories();
			allcats.remove(CategoryModel.UNCATEGORIZED);
			if (allcats.isEmpty())
			    return;
			Object[] cats = allcats.toArray();

			Object o = JOptionPane.showInputDialog(null, Resource
				.getResourceString("delete_cat_choose"), "",
				JOptionPane.QUESTION_MESSAGE, null, cats,
				cats[0]);
			if (o == null)
			    return;

			int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("delcat_warn")
				+ " [" + (String) o + "]!", "",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if (ret == JOptionPane.OK_OPTION) {

			    // appts
			    Iterator itr = AppointmentModel.getReference()
				    .getAllAppts().iterator();
			    while (itr.hasNext()) {
				Appointment ap = (Appointment) itr.next();
				String cat = ap.getCategory();
				if (cat != null && cat.equals(o))
				    AppointmentModel.getReference().delAppt(ap);
			    }

			    // tasks
			    itr = TaskModel.getReference().getTasks()
				    .iterator();
			    while (itr.hasNext()) {
				Task t = (Task) itr.next();
				String cat = t.getCategory();
				if (cat != null && cat.equals(o))
				    TaskModel.getReference().delete(t.getKey());
			    }

			    try {
				CategoryModel.getReference().syncCategories();
			    } catch (Exception ex) {
				Errmsg.errmsg(ex);
			    }
			}
		    } catch (Exception ex) {
			Errmsg.errmsg(ex);
		    }
		}
	    });
	}
	return delcatMI;
    }

    private void AboutMIActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_AboutMIActionPerformed

	// show the About data
	String build_info = "";
	String version = "";
	try {
	    // get the version and build info from a properties file in the
	    // jar
	    // file
	    InputStream is = getClass().getResource("/properties").openStream();
	    Properties props = new Properties();
	    props.load(is);
	    is.close();
	    version = Resource.getVersion();
	    build_info = Resource.getResourceString("Build_Number:_")
		    + props.getProperty("build.number")
		    + Resource.getResourceString("Build_Time:_")
		    + props.getProperty("build.time") + "\n";

	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	// build and show the version info.

	String info = Resource.getResourceString("Berger-Organizer_v")
		+ version + "\n\n" + Resource.getResourceString("copyright") + 
		" (2003-2007) Michael Berger <i_flem@users.sourceforge.net>\n\nhttp://borg-calendar.sourceforge.net\n\n"
		+ Resource.getResourceString("contributions_by") + "\n"
		+ Resource.getResourceString("contrib") + "\n" 
		+ Resource.getResourceString("translations") + "\n\n"
		+ build_info + "\n" + "Java "
		+ System.getProperty("java.version");
	Object opts[] = { Resource.getPlainResourceString("Dismiss") /*
                                                                         * ,
                                                                         * Resource.getResourceString("Show_Detailed_Source_Version_Info")
                                                                         */};
	JOptionPane.showOptionDialog(null, info, Resource
		.getResourceString("About_BORG"), JOptionPane.YES_NO_OPTION,
		JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getClass()
			.getResource("/resource/borg.jpg")), opts, opts[0]);

    }// GEN-LAST:event_AboutMIActionPerformed

    private void helpMIActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_helpMIActionPerformed
	// show the help page
	// new HelpScreen("/resource/help.htm").show();
	try {
	    HelpProxy.launchHelp();
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }// GEN-LAST:event_helpMIActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem1ActionPerformed
	// bring up the options window
	OptionsView.getReference().setVisible(true);
    }// GEN-LAST:event_jMenuItem1ActionPerformed

    private void licsendActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_licsendActionPerformed
	// show the open source license
	new HelpScreen("/resource/license.htm").setVisible(true);
    }// GEN-LAST:event_licsendActionPerformed

    private void PrevActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_PrevActionPerformed
	nav_.prev();
    }// GEN-LAST:event_PrevActionPerformed

    private void PrintMonthMIActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_PrintMonthMIActionPerformed
	if (nav_ instanceof MultiView) {
	    MultiView cv = (MultiView) nav_;
	    cv.print();
	}

    }// GEN-LAST:event_PrintMonthMIActionPerformed

    private void printprevActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_printprevActionPerformed
    {// GEN-HEADEREND:event_printprevActionPerformed
	if (nav_ instanceof MultiView) {
	    MultiView cv = (MultiView) nav_;
	    new MonthPreView(cv.getMonth(), cv.getYear());
	}
    }// GEN-LAST:event_printprevActionPerformed

    private void SearchMIActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_SearchMIActionPerformed
	// user wants to do a search, so prompt for search string and request
	// search results

	new SearchView().setVisible(true);

    }// GEN-LAST:event_SearchMIActionPerformed
    /*
         * private void TaskTrackMIActionPerformed(java.awt.event.ActionEvent
         * evt) {// GEN-FIRST:event_TaskTrackMIActionPerformed TaskListView bt_ =
         * TaskListView.getReference(); bt_.refresh(); bt_.setVisible(true); }//
         * GEN-LAST:event_TaskTrackMIActionPerformed
         */

    private void ToDoMenuActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ToDoMenuActionPerformed
	// ask borg class to bring up the todo window
	try {
	    TodoView.getReference().setVisible(true);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }// GEN-LAST:event_ToDoMenuActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_exitMenuItemActionPerformed
	Borg.shutdown();

    }// GEN-LAST:event_exitMenuItemActionPerformed

    private void exportMIActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_exportMIActionPerformed

	// user wants to export the task and calendar DBs to an XML file
	File dir;
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

	    String s = chooser.getSelectedFile().getAbsolutePath();
	    dir = new File(s);
	    String err = null;
	    if (!dir.exists()) {
		err = Resource.getResourceString("Directory_[") + s
			+ Resource.getResourceString("]_does_not_exist");
	    } else if (!dir.isDirectory()) {
		err = "[" + s
			+ Resource.getResourceString("]_is_not_a_directory");
	    }
	    // else if( !dir.canWrite() ) {
	    // //err = Resource.getResourceString("Directory_[") + s +
	    // Resource.getResourceString("]_is_not_writable");
	    // }

	    if (err == null)
		break;

	    Errmsg.notice(err);
	}

	try {

	    JOptionPane.showMessageDialog(null, Resource
		    .getResourceString("export_notice")
		    + dir.getAbsolutePath());

	    String fname = dir.getAbsolutePath() + "/borg.xml";
	    if (OverwriteConfirm.checkOverwrite(fname)) {
		OutputStream ostr = IOHelper.createOutputStream(fname);
		Writer fw = new OutputStreamWriter(ostr, "UTF8");
		AppointmentModel.getReference().export(fw);
		fw.close();
	    }

	    fname = dir.getAbsolutePath() + "/mrdb.xml";
	    if (OverwriteConfirm.checkOverwrite(fname)) {
		OutputStream ostr = IOHelper.createOutputStream(fname);
		Writer fw = new OutputStreamWriter(ostr, "UTF8");
		TaskModel.getReference().export(fw);
		fw.close();
	    }

	    fname = dir.getAbsolutePath() + "/addr.xml";
	    if (OverwriteConfirm.checkOverwrite(fname)) {
		OutputStream ostr = IOHelper.createOutputStream(fname);
		Writer fw = new OutputStreamWriter(ostr, "UTF8");
		AddressModel.getReference().export(fw);
		fw.close();
	    }

	    if( MemoModel.getReference().hasMemos() )
	    {
		fname = dir.getAbsolutePath() + "/memo.xml";
		if (OverwriteConfirm.checkOverwrite(fname)) {
		    OutputStream ostr = IOHelper.createOutputStream(fname);
		    Writer fw = new OutputStreamWriter(ostr, "UTF8");
		    MemoModel.getReference().export(fw);
		    fw.close();
		}
	    }
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }// GEN-LAST:event_exportMIActionPerformed

    /**
         * This method initializes expvcal
         * 
         * @return javax.swing.JMenuItem
         */
    private JMenuItem expvcal;

    private JMenuItem getExpvcal() {
	if (expvcal == null) {
	    expvcal = new JMenuItem();
	    ResourceHelper.setText(expvcal, "exp_vcal");
	    expvcal.setIcon(new javax.swing.ImageIcon(getClass().getResource(
		    "/resource/Export16.gif")));
	    expvcal.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    File file;
		    while (true) {
			// prompt for a file
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(new File("."));
			chooser.setDialogTitle(Resource
				.getResourceString("choose_file"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
			    return;

			String s = chooser.getSelectedFile().getAbsolutePath();
			file = new File(s);

			break;

		    }

		    try {
			FileWriter w = new FileWriter(file);
			AppointmentVcalAdapter.exportVcal(w);
			w.close();
		    } catch (Exception ex) {
			Errmsg.errmsg(ex);
		    }

		}

	    });
	}
	return expvcal;
    }
    
    private JMenu getReportMenu()
    {
	JMenu m = new JMenu();
	m.setText(Resource.getPlainResourceString("reports"));
	JMenuItem otr = new JMenuItem();
	otr.setText(Resource.getPlainResourceString("open_tasks"));
	otr.addActionListener(new ActionListener(){

	    public void actionPerformed(ActionEvent arg0) {
		RunReport.runReport("open_tasks", null);
	    }
	    
	});
	m.add(otr);
	
	JMenuItem customrpt = new JMenuItem();
	customrpt.setText(Resource.getPlainResourceString("select_rpt"));
	customrpt.addActionListener(new ActionListener(){

	    public void actionPerformed(ActionEvent arg0) {
		try {
		    InputStream is = IOHelper.fileOpen(".", Resource.getPlainResourceString("select_rpt"));
		    if( is == null ) return;
		    RunReport.runReport(is, null);
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		}
		//RunReport.runReport("open_tasks", null);
	    }
	    
	});
	m.add(customrpt);
	return m;
    }

}
