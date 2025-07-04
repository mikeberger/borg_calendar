package net.sf.borg.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.fortuna.ical4j.vcard.VCard;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.IOHelper;
import net.sf.borg.common.ModalMessageServer;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.sync.SyncLog;
import net.sf.borg.model.sync.google.GCal;
import net.sf.borg.model.sync.ical.CalDav;
import net.sf.borg.model.sync.ical.CardDav;
import net.sf.borg.model.sync.ical.ICal;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.options.GoogleOptionsPanel;
import net.sf.borg.ui.options.IcalOptionsPanel;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.util.FileDrop;
import net.sf.borg.ui.util.IconHelper;
import net.sf.borg.ui.util.PasswordHelper;

public class SyncModule implements Module, Prefs.Listener, Model.Listener {

	static private final Logger log = Logger.getLogger("net.sf.borg");
	private static final PrefName url_pref = new PrefName("saved_import_url", "");
	public static final ActionListener syncButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {

				if (GCal.isSyncing()) {
					runGcalSync(false, false);
				} else if (CalDav.isSyncing()) {
					String pass = PasswordHelper.getReference().decryptText(Prefs.getPref(PrefName.CALDAV_PASSWORD), "Unlock Caldav Password", false);
					if( pass != null )
						runBackgroundSync(Synctype.FULL, pass);
				} else {
					JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
							JOptionPane.ERROR_MESSAGE);
				}

			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
		}
	};
	private static final ActionListener syncListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				if (!CalDav.isSyncing()) {
					JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String pass = PasswordHelper.getReference().decryptText(Prefs.getPref(PrefName.CALDAV_PASSWORD), "Unlock Caldav Password", false);
				if( pass != null )
					runBackgroundSync(Synctype.FULL, pass);

			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
		}
	};
	private JButton syncToolbarButton = null;

	public SyncModule() {

	}

	public static JMenu getIcalMenu() {
		JMenu m = new JMenu();
		m.setText("Sync");

		// m.setIcon(new
		// javax.swing.ImageIcon(IcalModule.class.getResource("/resource/Export16.gif")));

		JMenu calmenu = new JMenu("CALDAV");

		JMenuItem caldavs = new JMenuItem();
		caldavs.setText(Resource.getResourceString("CALDAV-Sync"));
		caldavs.addActionListener(syncListener);

		calmenu.add(caldavs);

		JMenuItem caldavso = new JMenuItem();
		caldavso.setText(Resource.getResourceString("CALDAV-Sync-out"));
		caldavso.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!CalDav.isSyncing()) {
						JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					String pass = PasswordHelper.getReference().decryptText(Prefs.getPref(PrefName.CALDAV_PASSWORD), "Unlock Caldav Password", false);
					if( pass != null )
						runBackgroundSync(Synctype.ONEWAY, pass);

				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});

		calmenu.add(caldavso);

		JMenuItem caldavo = new JMenuItem();
		caldavo.setText(Resource.getResourceString("CALDAV-Overwrite"));
		caldavo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!CalDav.isSyncing()) {
						JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("Caldav-Overwrite-Warn"),
							Resource.getResourceString("Confirm"), JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (ret != JOptionPane.OK_OPTION)
						return;
					String pass = PasswordHelper.getReference().decryptText(Prefs.getPref(PrefName.CALDAV_PASSWORD), "Unlock Caldav Password", false);
					if( pass != null )
						runBackgroundSync(Synctype.OVERWRITE, pass);
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});

		calmenu.add(caldavo);

		m.add(calmenu);

		JMenu icsmenu = new JMenu("ICS");

		JMenuItem imp = new JMenuItem();
		imp.setText(Resource.getResourceString("Import"));
		imp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				// prompt for a file
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(Resource.getResourceString("ical_files"),
						"ics", "ICS", "ical", "ICAL", "icalendar");
				chooser.setFileFilter(filter);
				chooser.setCurrentDirectory(new File("."));
				chooser.setDialogTitle(Resource.getResourceString("choose_file"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int returnVal = chooser.showOpenDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				String s = chooser.getSelectedFile().getAbsolutePath();

				try {

					String warning = ICal.importIcalFromFile(s);
					if (warning != null && !warning.isEmpty())
						Errmsg.getErrorHandler().notice(warning);
					CategoryModel.syncModels();
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}

			}
		});

		icsmenu.add(imp);

		JMenuItem impUrl = new JMenuItem();
		impUrl.setText(Resource.getResourceString("ImportUrl"));
		impUrl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				// prompt for a file
				String urlString = JOptionPane.showInputDialog(null, Resource.getResourceString("enturl"),
						Prefs.getPref(url_pref));
				if (urlString == null)
					return;

				Prefs.putPref(url_pref, urlString);
				try {

					String warning = ICal.importIcalFromUrl(urlString);
					if (warning != null && !warning.isEmpty())
						Errmsg.getErrorHandler().notice(warning);
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}

			}
		});

		icsmenu.add(impUrl);

		JMenuItem exp = new JMenuItem();
		exp.setText(Resource.getResourceString("exportToFile"));
		exp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				export(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS));
			}
		});

		icsmenu.add(exp);

		m.add(icsmenu);
		
		JMenuItem expz = new JMenuItem();
		expz.setText(Resource.getResourceString("exportToIcalZipFile"));
		expz.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				exportToZip();
			}
		});

		icsmenu.add(expz);

		m.add(icsmenu);

		JMenu vcardmenu = new JMenu("VCARD");

		JMenuItem impvc = new JMenuItem();
		impvc.setText(Resource.getResourceString("ImportFile"));
		impvc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				// prompt for a file
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("VCARD", "vcs", "VCS", "vcf", "VCF",
						"vcard", "VCARD");
				chooser.setFileFilter(filter);
				chooser.setCurrentDirectory(new File("."));
				chooser.setDialogTitle(Resource.getResourceString("choose_file"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int returnVal = chooser.showOpenDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				String s = chooser.getSelectedFile().getAbsolutePath();

				try {

					List<VCard> vcards = CardDav.importVcardFromFile(s);
					String warning = CardDav.importVCard(vcards);
					if (warning != null && !warning.isEmpty())
						Errmsg.getErrorHandler().notice(warning);
					CategoryModel.syncModels();
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}

			}
		});

		vcardmenu.add(impvc);
		
		JMenuItem expvc = new JMenuItem();
		expvc.setText(Resource.getResourceString("exportToFile"));
		expvc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				exportVcard();
			}
		});

		vcardmenu.add(expvc);

		m.add(vcardmenu);

		JMenu gcalmenu = new JMenu("Google");
		JMenuItem gcalsyncmi = new JMenuItem("Sync");
		gcalsyncmi.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!GCal.isSyncing()) {
						JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					runGcalSync(false, false);

				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});

		gcalmenu.add(gcalsyncmi);
		JMenuItem gcalsyncCleanmi = new JMenuItem(Resource.getResourceString("Sync_Cleanup"));
		gcalsyncCleanmi.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!GCal.isSyncing()) {
						JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					runGcalSync(false, true);

				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});

		gcalmenu.add(gcalsyncCleanmi);

		JMenuItem gcalow = new JMenuItem("Full Sync");
		gcalow.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!GCal.isSyncing()) {
						JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					runGcalSync(true, false);

				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});
		gcalmenu.add(gcalow);
		
		JMenuItem guns = new JMenuItem("UnSync");
		guns.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!GCal.isSyncing()) {
						JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("unsync-Warn"),
							"WARNING", JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (ret != JOptionPane.OK_OPTION)
						return;
					GCal.getReference().unsync();

				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});
		gcalmenu.add(guns);

		m.add(gcalmenu);

		return m;
	}

	private static void runGcalSync(boolean fullsync, boolean cleanup) {

		final boolean full = fullsync;
		class SyncWorker extends SwingWorker<Void, Object> {
			@Override
			public Void doInBackground() {
				try {

					AppointmentModel.getReference().setNotifyListeners(false);
					// modally lock borg
					ModalMessageServer.getReference().sendMessage("lock:" + Resource.getResourceString("syncing"));
					GCal.getReference().sync(Prefs.getIntPref(PrefName.GCAL_EXPORTYEARS), full, cleanup);

				} catch (Exception e) {
					e.printStackTrace();
					log.severe("***ERROR during sync***, please check logs");
					ModalMessageServer.getReference().sendLogMessage(e.getMessage());
					ModalMessageServer.getReference().sendLogMessage("***ERROR during sync***, please check logs");
				}

				ModalMessageServer.getReference().sendLogMessage(Resource.getResourceString("done"));
				AppointmentModel.getReference().setNotifyListeners(true);
				AppointmentModel.getReference().refresh();



				return null;
			}

			@Override
			protected void done() {
				ModalMessageServer.getReference().sendMessage("unlock");

			}
		}

		(new SyncWorker()).execute();
	}

	/**
	 * run a sync command in a background thread while a modal message is presented
	 */
	static private void runBackgroundSync(Synctype type, String pass) {

		final Synctype ty = type;
		class SyncWorker extends SwingWorker<Void, Object> {
			@Override
			public Void doInBackground() {
				try {

					// modally lock borg
					ModalMessageServer.getReference().sendMessage("lock:" + Resource.getResourceString("syncing"));
					if (ty == Synctype.FULL)
						CalDav.sync(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS), false, pass);
					else if (ty == Synctype.ONEWAY)
						CalDav.sync(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS), true, pass);
					else if (ty == Synctype.OVERWRITE)
						CalDav.export(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS), pass);

				} catch (Exception e) {
					e.printStackTrace();
					ModalMessageServer.getReference().sendLogMessage(e.toString());
				}

				ModalMessageServer.getReference().sendLogMessage(Resource.getResourceString("done"));

				return null;
			}

			@Override
			protected void done() {
				ModalMessageServer.getReference().sendMessage("unlock");

			}
		}

		(new SyncWorker()).execute();

	}

	/**
	 * export appts
	 *
	 * @param years - number of years to export or null
	 */
	private static void export(Integer years) {

		// prompt for a file
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		FileNameExtensionFilter filter = new FileNameExtensionFilter(Resource.getResourceString("ical_files"), "ics",
				"ICS", "ical", "ICAL", "icalendar");
		chooser.setFileFilter(filter);
		chooser.setDialogTitle(Resource.getResourceString("choose_file"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int returnVal = chooser.showSaveDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		String s = chooser.getSelectedFile().getAbsolutePath();

		// auto append extension
		if (chooser.getFileFilter() != chooser.getAcceptAllFileFilter()) {
			if (!s.contains(".")) {
				s += ".ics";
			}
		}

		try {
			if (years != null) {
				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.YEAR, -1 * years.intValue());
				ICal.exportIcalToFile(s, cal.getTime());
			} else {
				ICal.exportIcalToFile(s, null);
			}

		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}
	
	private static void exportToZip() {

		String s;
		while (true) {
			// prompt for a directory to store the files
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(IOHelper.getHomeDirectory());
			chooser.setDialogTitle(Resource.getResourceString("select_export_dir"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setApproveButtonText(Resource.getResourceString("select_export_dir"));

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			s = chooser.getSelectedFile().getAbsolutePath();
			IOHelper.setHomeDirectory(s);
			File dir = new File(s);
			String err = null;
			if (!dir.exists()) {
				err = Resource.getResourceString("Directory_[") + s + Resource.getResourceString("]_does_not_exist");
			} else if (!dir.isDirectory()) {
				err = "[" + s + Resource.getResourceString("]_is_not_a_directory");
			}

			if (err == null)
				break;

			Errmsg.getErrorHandler().notice(err);
		}

		try {
			ICal.exportApptsToFileByYear(s);

		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	private static void exportVcard() {

		// prompt for a file
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("VCARD", "vcs", "VCS", "VCF", "vcf", "vcard",
				"VCARD");
		chooser.setFileFilter(filter);
		chooser.setDialogTitle(Resource.getResourceString("choose_file"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int returnVal = chooser.showSaveDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		String s = chooser.getSelectedFile().getAbsolutePath();

		// auto append extension
		if (chooser.getFileFilter() != chooser.getAcceptAllFileFilter()) {
			if (!s.contains(".")) {
				s += ".vcf";
			}
		}

		try {
			CardDav.exportToFile(s);
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	@Override
	public Component getComponent() {
		return null;
	}

	@Override
	public String getModuleName() {
		return "SYNC";
	}

	@Override
	public ViewType getViewType() {
		return null;
	}

	@Override
	public void initialize(MultiView parent) {

		Prefs.addListener(this);
		SyncLog.getReference().addListener(this);

		OptionsView.getReference().addPanel(new IcalOptionsPanel());

		OptionsView.getReference().addPanel(new GoogleOptionsPanel());

		new FileDrop(parent, new FileDrop.Listener() {
			@Override
			public void filesDropped(java.io.File[] files) {
				for (File f : files) {
					String warning;
					try {
						warning = ICal.importIcalFromFile(f.getAbsolutePath());
						if (warning != null && !warning.isEmpty())
							Errmsg.getErrorHandler().notice(warning);
					} catch (Exception e) {
						Errmsg.getErrorHandler().errmsg(e);
					}

				}
			}
		});

		syncToolbarButton = MultiView.getMainView().addToolBarItem(
				IconHelper.getIcon("/resource/Refresh16.gif"),
				Resource.getResourceString("Sync"), syncButtonListener);

		String usetray = Prefs.getPref(PrefName.USESYSTRAY);
		if (usetray.equals("true")) {

			try {
				updateSyncButton();
				showTrayIcon();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		SyncLog.getReference();

	}

	@Override
	public void print() {
		// do nothing
	}

	@Override
	public void prefsChanged() {
		try {
			updateSyncButton();
			showTrayIcon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateSyncButton() throws Exception {

		String label = Integer.toString(SyncLog.getReference().getAll().size());

		if (CalDav.isServerSyncNeeded())
			label += "^";

		syncToolbarButton.setText(label);

	}

	private void showTrayIcon() {

		if (TrayIconProxy.hasTrayIcon()) {

			try {
				if (!(CalDav.isSyncing() || GCal.isSyncing()) || SyncLog.getReference().getAll().isEmpty()) {
					TrayIconProxy.disableTrayIcon();
				} else {
					TrayIconProxy.enableTrayIcon();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	@Override
	public void update(ChangeEvent event) {
		try {
			updateSyncButton();
			showTrayIcon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private enum Synctype {
		FULL, ONEWAY, OVERWRITE
	}

}
