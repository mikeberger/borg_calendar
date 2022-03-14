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


import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.sync.google.GCal;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

/**
 * Provides the UI for editing Miscellaneous options
 */

public class GoogleOptionsPanel extends OptionsPanel {

    static private final Logger log = Logger.getLogger("net.sf.borg");


    private static final long serialVersionUID = 2246952528811147049L;
    private JCheckBox enable_sync_box = new JCheckBox();

    private JTextField cal_box = new JTextField();
    private JTextField task_list_box = new JTextField();
    private JTextField credentials_file_box = new JTextField();
    private JTextField token_dir_box = new JTextField();


    /**
     * Instantiates a new miscellaneous options panel.
     */
    public GoogleOptionsPanel() {

        this.setLayout(new GridBagLayout());

        enable_sync_box.setText("Enable Google Sync");

        this.add(enable_sync_box,
                GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

        this.add(new JLabel("Calendar:"),
                GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
        this.add(cal_box,
                GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH));

        this.add(new JLabel("Task List:"),
                GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));
        this.add(task_list_box,
                GridBagConstraintsFactory.create(1, 2, GridBagConstraints.BOTH));


        JPanel filep = new JPanel();
        filep.setLayout(new GridBagLayout());

        filep.add(new JLabel("Credentials File:"),
                GridBagConstraintsFactory.create(0, 0, GridBagConstraints.NONE));

        filep.add(credentials_file_box, GridBagConstraintsFactory.create(1, 0,
                GridBagConstraints.BOTH, 1.0, 0.0));

        JButton bb = new JButton();
        ResourceHelper.setText(bb, "Browse");
        bb.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String f = OptionsPanel.chooseFile();
                if (f == null) {
                    return;
                }

                credentials_file_box.setText(f);
            }
        });
        filep.add(bb,
                GridBagConstraintsFactory.create(2, 0, GridBagConstraints.NONE));

        filep.add(new JLabel("Tokens Dir:"),
                GridBagConstraintsFactory.create(0, 1, GridBagConstraints.NONE));

        filep.add(token_dir_box, GridBagConstraintsFactory.create(1, 1,
                GridBagConstraints.BOTH, 1.0, 0.0));

        JButton tb = new JButton();
        ResourceHelper.setText(tb, "Browse");
        tb.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String f = OptionsPanel.chooseDir();
                if (f == null) {
                    return;
                }

                token_dir_box.setText(f);
            }
        });
        filep.add(tb,
                GridBagConstraintsFactory.create(2, 1, GridBagConstraints.NONE));

        GridBagConstraints gbc1 = GridBagConstraintsFactory.create(0, 3,
                GridBagConstraints.BOTH, 1.0, 0.0);
        gbc1.gridwidth = 2;
        this.add(filep, gbc1);


    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
     */
    @Override
    public void applyChanges() {


        OptionsPanel.setBooleanPref(enable_sync_box, PrefName.GOOGLE_SYNC);
        Prefs.putPref(PrefName.GCAL_CAL_ID, cal_box.getText());
        Prefs.putPref(PrefName.GCAL_TASKLIST_ID, task_list_box.getText());
        Prefs.putPref(PrefName.GOOGLE_CRED_FILE, credentials_file_box.getText());
        Prefs.putPref(PrefName.GOOGLE_TOKEN_DIR, token_dir_box.getText());

        GCal.getReference().resetGoogleIds();

    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
     */
    @Override
    public void loadOptions() {
        OptionsPanel.setCheckBox(enable_sync_box, PrefName.GOOGLE_SYNC);
        cal_box.setText(Prefs.getPref(PrefName.GCAL_CAL_ID));
        task_list_box.setText(Prefs.getPref(PrefName.GCAL_TASKLIST_ID));
        credentials_file_box.setText(Prefs.getPref(PrefName.GOOGLE_CRED_FILE));
        token_dir_box.setText(Prefs.getPref(PrefName.GOOGLE_TOKEN_DIR));


    }

    @Override
    public String getPanelName() {
        return "Google";
    }
}
