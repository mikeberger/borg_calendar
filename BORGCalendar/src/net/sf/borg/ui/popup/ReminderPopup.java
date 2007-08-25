
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

package net.sf.borg.ui.popup;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import net.sf.borg.common.Resource;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.View;

public class ReminderPopup extends View {
    private JPanel jPanel = null;
    private JLabel jLabel1 = null;
    private JLabel jLabel2 = null;
    private JRadioButton noMoreButton = null;
    private JPanel jPanel1 = null;
    private JButton jButton = null;
    private char[] remindersShown;
    private boolean shown_ = false;
    public void setShown(boolean s){ shown_ = s; }
    public boolean wasShown(){ return shown_; }
    /**
     * This method initializes this
     *
     * @return void
     */
    private void jbInit() {
        this.setContentPane(getJPanel());
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        this.setSize(400, 250);

    }
    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jLabel1 = new JLabel();
            jLabel2 = new JLabel();
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            gridBagConstraints9.gridx = 0;
            gridBagConstraints9.weightx = 1.0D;  // Generated
            gridBagConstraints9.gridy = 1;
            gridBagConstraints9.ipadx = 100;
            gridBagConstraints9.ipady = 5;
            gridBagConstraints9.insets = new java.awt.Insets(5, 0, 5, 0);
            gridBagConstraints10.gridx = 0;
            gridBagConstraints10.weightx = 1.0D;  // Generated
            gridBagConstraints10.gridy = 3;
            gridBagConstraints10.ipadx = 100;
            gridBagConstraints10.ipady = 0;
            gridBagConstraints10.insets = new java.awt.Insets(5, 0, 5, 0);
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.weightx = 1.0D;  // Generated
            gridBagConstraints11.gridy = 2;
            gridBagConstraints11.ipadx = 100;
            gridBagConstraints11.ipady = 5;
            gridBagConstraints11.insets = new java.awt.Insets(5, 0, 5, 0);
            gridBagConstraints15.gridx = 0;
            gridBagConstraints15.gridy = 4;
            jPanel.add(jLabel1, gridBagConstraints9);  // Generated
            jPanel.add(jLabel2, gridBagConstraints11);  // Generated
            jPanel.add(getNoMoreButton(), gridBagConstraints10);  // Generated
            jPanel.add(getJPanel1(), gridBagConstraints15);
            jLabel1.setText("");
            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel2.setText("");
            jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }
        return jPanel;
    }
    /**
     * This method initializes jRadioButton
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getNoMoreButton() {
        if (noMoreButton == null) {
            noMoreButton = new JRadioButton();
            ResourceHelper.setText(noMoreButton, "No_more");
            noMoreButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }
        return noMoreButton;
    }
    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel();
            jPanel1.setLayout(new GridBagLayout());
            jPanel1.add(getJButton(), new GridBagConstraints());
        }
        return jPanel1;
    }
    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            ResourceHelper.setText(jButton, "OK");
            jButton.setActionCommand("close_it");
            jButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        closeit();                              }
                });
        }
        return jButton;
    }


    public ReminderPopup() {
        super();
        this.setTitle("Borg " + Resource.getResourceString("Reminder"));
        jbInit();
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        remindersShown = new char[ReminderTimes.getNum()];
        for (int i = 0; i < ReminderTimes.getNum(); ++i) {
            remindersShown[i] = 'N';
        }

    }


    public void setText(String str) {
        jLabel1.setText(str);
    }

    public void setText2(String str) {
        jLabel2.setText(str);
    }

    /**
     *  Return Y if Reminder request i has been shown, otherwise N
     */
    public char reminderShown(int i) {
        return remindersShown[i];
    }


    /**
     *  Save the fact that Reminder request i has been shown
     *  and shouldnt be shown again
     */
    public void setReminderShown(int i) {
        remindersShown[i] = 'Y';
    }


    private void closeit() {
        if (noMoreButton.isSelected()) {
                this.dispose();
	}
        else {
                this.setVisible(false);
	}
    }

    public void destroy() {
        this.dispose();
    }

    public void refresh() {
    }

    //  $protect<<$
}  //  @jve:decl-index=0:visual-constraint="10,10"
