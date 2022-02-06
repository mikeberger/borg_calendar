package net.sf.borg.ui;

import net.fortuna.ical4j.vcard.VCard;
import net.sf.borg.common.*;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.sync.SyncLog;
import net.sf.borg.model.sync.google.GCal;
import net.sf.borg.model.sync.ical.CalDav;
import net.sf.borg.model.sync.ical.CardDav;
import net.sf.borg.model.sync.ical.ICal;
import net.sf.borg.model.sync.ical.IcalFTP;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.options.GoogleOptionsPanel;
import net.sf.borg.ui.options.IcalOptionsPanel;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.util.FileDrop;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

public class SyncModule implements Module, Prefs.Listener, Model.Listener {

    static private final Logger log = Logger.getLogger("net.sf.borg");
    private static PrefName url_pref = new PrefName("saved_import_url", "");
    private static ActionListener syncButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            try {

                if (GCal.isSyncing()) {
                    runGcalSync(false);
                } else if (CalDav.isSyncing()) {
                    runBackgroundSync(Synctype.FULL);
                } else {
                    JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                Errmsg.getErrorHandler().errmsg(e);
            }
        }
    };
    private static ActionListener syncListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            try {
                if (!CalDav.isSyncing()) {
                    JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                runBackgroundSync(Synctype.FULL);

            } catch (Exception e) {
                Errmsg.getErrorHandler().errmsg(e);
            }
        }
    };
    private JButton syncToolbarButton = null;
    private TrayIcon trayIcon;

    public SyncModule() {
        try {
            trayIcon = new TrayIcon(
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/Refresh16.gif")));
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
    }

    public static JMenu getIcalMenu() {
        JMenu m = new JMenu();
        m.setText("Sync");

        //m.setIcon(new javax.swing.ImageIcon(IcalModule.class.getResource("/resource/Export16.gif")));

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
                    runBackgroundSync(Synctype.ONEWAY);

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
                    runBackgroundSync(Synctype.OVERWRITE);
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

        JMenuItem expftp = new JMenuItem();
        expftp.setText(Resource.getResourceString("exportToFTP"));
        expftp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    IcalFTP.exportftp(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS));
                } catch (Exception e) {
                    Errmsg.getErrorHandler().errmsg(e);
                }
            }
        });

        icsmenu.add(expftp);

        m.add(icsmenu);

        JMenu vcardmenu = new JMenu("VCARD");

        JMenuItem impvc = new JMenuItem();
        impvc.setText(Resource.getResourceString("ImportFile"));
        impvc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                // prompt for a file
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("VCARD",
                        "vcs", "VCS", "vcf", "VCF", "vcard", "VCARD");
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

        JMenuItem imcarddav = new JMenuItem();
        imcarddav.setText(Resource.getResourceString("ImportCarddav"));
        imcarddav.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {


                try {

                    List<VCard> vcards = CardDav.importVcardFromCarddav();
                    String warning = CardDav.importVCard(vcards);
                    if (warning != null && !warning.isEmpty())
                        Errmsg.getErrorHandler().notice(warning);
                    CategoryModel.syncModels();
                } catch (Exception e) {
                    Errmsg.getErrorHandler().errmsg(e);
                }

            }
        });

        vcardmenu.add(imcarddav);

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

                    runGcalSync(false);

                } catch (Exception e) {
                    Errmsg.getErrorHandler().errmsg(e);
                }
            }
        });

        gcalmenu.add(gcalsyncmi);

        JMenuItem gcalow = new JMenuItem("Overwrite");
        gcalow.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (!GCal.isSyncing()) {
                        JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    runGcalSync(true);

                } catch (Exception e) {
                    Errmsg.getErrorHandler().errmsg(e);
                }
            }
        });
        gcalmenu.add(gcalow);
        m.add(gcalmenu);
        return m;
    }

    private static void runGcalSync(boolean overwrite) {

        final boolean ov = overwrite;
        class SyncWorker extends SwingWorker<Void, Object> {
            @Override
            public Void doInBackground() {
                try {

                    // modally lock borg
                    SocketClient.sendMessage("lock:" + Resource.getResourceString("syncing"));
                    GCal.getReference().sync(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS), ov);

                } catch (Exception e) {
                    e.printStackTrace();
                    SocketClient.sendLogMessage(e.toString());
                }

                SocketClient.sendLogMessage(Resource.getResourceString("done"));

                return null;
            }

            @Override
            protected void done() {
                SocketClient.sendMessage("unlock");

            }
        }

        (new SyncWorker()).execute();
    }

    /**
     * run a sync command in a background thread while a modal message is
     * presented
     */
    static private void runBackgroundSync(Synctype type) {

        final Synctype ty = type;
        class SyncWorker extends SwingWorker<Void, Object> {
            @Override
            public Void doInBackground() {
                try {

                    // modally lock borg
                    SocketClient.sendMessage("lock:" + Resource.getResourceString("syncing"));
                    if (ty == Synctype.FULL)
                        CalDav.sync(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS), false);
                    else if (ty == Synctype.ONEWAY)
                        CalDav.sync(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS), true);
                    else if (ty == Synctype.OVERWRITE)
                        CalDav.export(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS));

                } catch (Exception e) {
                    e.printStackTrace();
                    SocketClient.sendLogMessage(e.toString());
                }

                SocketClient.sendLogMessage(Resource.getResourceString("done"));

                return null;
            }

            @Override
            protected void done() {
                SocketClient.sendMessage("unlock");

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

    private static void exportVcard() {

        // prompt for a file
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("VCARD", "vcs",
                "VCS", "VCF", "vcf", "vcard", "VCARD");
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

        if( Prefs.getBoolPref(PrefName.ENABLE_GOOGLE_FEATURE))
        {
            OptionsView.getReference().addPanel(new GoogleOptionsPanel());
        }

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
                new javax.swing.ImageIcon(SyncModule.class.getResource("/resource/Refresh16.gif")),
                Resource.getResourceString("Sync"), syncButtonListener);

        if (trayIcon != null)
            trayIcon.setToolTip("BORG " + Resource.getResourceString("Sync"));
        PopupMenu menu = new PopupMenu();
        MenuItem item = new MenuItem();
        item.setLabel(Resource.getResourceString("Sync"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (GCal.isSyncing()) {
                        runGcalSync(false);
                    } else if (CalDav.isSyncing()) {
                        runBackgroundSync(Synctype.FULL);
                    } else {
                        JOptionPane.showMessageDialog(null, Resource.getResourceString("Sync-Not-Set"), null,
                                JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    Errmsg.getErrorHandler().errmsg(e);
                }
            }
        });
        menu.add(item);
        if (trayIcon != null)
            trayIcon.setPopupMenu(menu);

        try {
            updateSyncButton();
            showTrayIcon();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // import from URL
        String url = Prefs.getPref(PrefName.ICAL_IMPORT_URL);
        if (url != null && !url.isEmpty()) {
            try {

                int res = JOptionPane.showConfirmDialog(null,
                        Resource.getResourceString("ImportUrl") + ": " + url + " ?",
                        Resource.getResourceString("please_confirm"), JOptionPane.OK_CANCEL_OPTION);

                if (res == JOptionPane.YES_OPTION) {

                    String warning = ICal.importIcalFromUrl(url);
                    if (warning != null && !warning.isEmpty())
                        Errmsg.getErrorHandler().notice(warning);
                }
            } catch (Exception e) {
                Errmsg.getErrorHandler().errmsg(e);
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

        if (SunTrayIconProxy.hasTrayIcon()) {

            try {
                if (!(CalDav.isSyncing() || GCal.isSyncing()) || SyncLog.getReference().getAll().isEmpty()) {
                    SystemTray.getSystemTray().remove(trayIcon);
                } else {
                    SystemTray.getSystemTray().add(trayIcon);
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
