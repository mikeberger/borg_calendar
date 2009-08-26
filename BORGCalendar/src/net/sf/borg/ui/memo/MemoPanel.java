/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui.memo;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.IOHelper;
import net.sf.borg.common.Resource;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TableSorter;

public class MemoPanel extends JPanel implements ListSelectionListener, Model.Listener {

    private static final long serialVersionUID = 1L;

    private static SimpleDateFormat normalDateFormat_ = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");

    private JScrollPane jScrollPane = null;

    private StripedTable memoListTable = null;

    private JTextArea memoText = null;

    private JPanel buttonPanel = null;

    private JButton newButton = null;

    private JButton saveButton = null;

    private JButton delButton = null;

    private JScrollPane jScrollPane1 = null;

    private JSplitPane jSplitPane = null;

    private JButton exportButton = null;

    private boolean isMemoEdited = false;

    private int editedMemoIndex = -1;

    private JPanel jPanel = null;

    private JCheckBox privateBox = null;

    /**
     * This is the default constructor
     */
    public MemoPanel() {
        super();
        initialize();

        memoListTable.setModel(new TableSorter(new String[] { Resource.getResourceString("Memo_Name") },
                new Class[] { java.lang.String.class }));
        ListSelectionModel rowSM = memoListTable.getSelectionModel();
        rowSM.addListSelectionListener(this);
        memoText.setEditable(false);
        getSaveButton().setEnabled(false);
        refresh();
        MemoModel.getReference().addListener(this);

    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridy = 1;
        GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
        gridBagConstraints21.fill = GridBagConstraints.BOTH;
        gridBagConstraints21.weighty = 1.0;
        gridBagConstraints21.gridx = 0;
        gridBagConstraints21.gridy = 0;
        gridBagConstraints21.weightx = 1.0;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridwidth = 1;
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        gridBagConstraints2.insets = new Insets(4, 4, 4, 4);
        gridBagConstraints2.gridy = 2;
        this.setLayout(new GridBagLayout());
        this.setSize(new Dimension(648, 525));
        this.add(getButtonPanel(), gridBagConstraints2);
        this.add(getJSplitPane(), gridBagConstraints21);
        this.add(getJPanel(), gridBagConstraints);
    }

    /**
     * This method initializes jScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane.setPreferredSize(new Dimension(100, 423));
            jScrollPane.setViewportView(getMemoListTable());
        }
        return jScrollPane;
    }

    /**
     * This method initializes memoListTable
     * 
     * @return javax.swing.JTable
     */
    private JTable getMemoListTable() {
        if (memoListTable == null) {
            memoListTable = new StripedTable();
            memoListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            memoListTable.setShowGrid(true);
        }
        return memoListTable;
    }

    private JTextArea getMemoText() {
        if (memoText == null) {
            memoText = new JTextArea();
            memoText.setLineWrap(true);
            memoText.setWrapStyleWord(true);
            memoText.getDocument().addDocumentListener(new DocumentListener() {

                public void changedUpdate(DocumentEvent arg0) {
                    isMemoEdited = true;
                    editedMemoIndex = memoListTable.getSelectedRow();
                    getSaveButton().setEnabled(true);
                }

                public void insertUpdate(DocumentEvent arg0) {
                    isMemoEdited = true;
                    editedMemoIndex = memoListTable.getSelectedRow();
                    getSaveButton().setEnabled(true);
                }

                public void removeUpdate(DocumentEvent arg0) {
                    isMemoEdited = true;
                    editedMemoIndex = memoListTable.getSelectedRow();
                    getSaveButton().setEnabled(true);
                }
            });
        }
        return memoText;
    }

    /**
     * This method initializes buttonPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            buttonPanel.add(getNewButton(), null);
            buttonPanel.add(getSaveButton(), null);
            buttonPanel.add(getDelButton(), null);
            buttonPanel.add(getExportButton(), null);

        }
        return buttonPanel;
    }

    /**
     * This method initializes newButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getNewButton() {
        if (newButton == null) {
            newButton = new JButton();
            newButton.setText(Resource.getResourceString("New_Memo"));
            newButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    newMemo();
                }
            });
        }
        return newButton;
    }

    /**
     * This method initializes saveButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getSaveButton() {
        if (saveButton == null) {
            saveButton = new JButton();
            saveButton.setText(Resource.getResourceString("Save_Memo"));
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveMemo();
                }
            });
        }
        return saveButton;
    }

    // internal refresh from memo activity should call loadTable() instead of
    // refresh()
    // refresh() is for external callers
    public void refresh() {

        // if the user is editing a row, don't process the refresh
        if (isMemoEdited)
            return;
        try {
            loadTable();
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }

    }

    private void loadTable() throws Exception {
        memoListTable.clearSelection();
        TableSorter tm = (TableSorter) memoListTable.getModel();
        tm.setRowCount(0);
        Collection<String> names = MemoModel.getReference().getNames();
        // System.out.println("Names.size=" + names.size());
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            tm.addRow(new Object[] { it.next() });
        }
    }

    public void selectMemo(String memoName) {
        TableSorter tm = (TableSorter) memoListTable.getModel();
        int rows = tm.getRowCount();
        for (int i = 0; i < rows; ++i) {
            String name = (String) tm.getValueAt(i, 0);
            if (memoName.equals(name)) {
                memoListTable.getSelectionModel().setSelectionInterval(i, i);
            }
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        // Ignore extra messages.
        if (e.getValueIsAdjusting())
            return;

        // if the user has edited a memo and is changing the memo
        // selection...
        if (isMemoEdited) {

            // if the selection is remaining (or returning) to the
            // edited memo
            // then do nothing - may be returning to the edited memo
            // due to the
            // setSelectionInterval line
            // below that resets the selection
            if (editedMemoIndex == memoListTable.getSelectedRow())
                return;

            // selection is moving to a new memo - prompt about
            // discarding
            // changes
            int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("Edited_Memo"), Resource
                    .getResourceString("Discard_Text?"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            // if user does not want to lose changes, we need to set
            // the
            // selection back to the edited memo
            if (ret != JOptionPane.OK_OPTION) {
                memoListTable.getSelectionModel().setSelectionInterval(editedMemoIndex, editedMemoIndex);
                return;
            }
        }

        String memoName = getSelectedMemoName();
        if (memoName == null) {
            memoText.setText("");
            memoText.setEditable(false);
            privateBox.setSelected(false);
            dateLabel.setText("");
        } else {

            String text;
            boolean priv = false;
            try {
                Memo m = MemoModel.getReference().getMemo(memoName);
                text = m.getMemoText();
                priv = m.getPrivate();
                String datetext = Resource.getResourceString("created") + ": ";
                if (m.getCreated() != null)
                    datetext += normalDateFormat_.format(m.getCreated());
                else
                    datetext += Resource.getResourceString("unknown");
                datetext += "           " + Resource.getResourceString("updated") + ": ";
                if (m.getUpdated() != null)
                    datetext += normalDateFormat_.format(m.getUpdated());
                else
                    datetext += Resource.getResourceString("unknown");
                dateLabel.setText(datetext);
            } catch (Exception e1) {
                Errmsg.errmsg(e1);
                return;
            }

            memoText.setEditable(true);
            memoText.setText(text);
            privateBox.setSelected(priv);
        }
        isMemoEdited = false;
        getSaveButton().setEnabled(false);

    }

    private String getSelectedMemoName() {
        int row = memoListTable.getSelectedRow();
        if (row == -1) {
            return null;
        }

        TableSorter tm = (TableSorter) memoListTable.getModel();
        String memoName = (String) tm.getValueAt(row, 0);
        return memoName;
    }

    private void saveMemo() {
        String name = getSelectedMemoName();
        if (name == null) {
            Errmsg.notice(Resource.getResourceString("Select_Memo_Warning"));
            return;
        }
        try {
            Memo m = MemoModel.getReference().getMemo(name);
            m.setMemoText(memoText.getText());
            m.setPrivate(privateBox.isSelected());
            MemoModel.getReference().saveMemo(m);
            isMemoEdited = false;
            loadTable();
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }

    }

    private void newMemo() {

        if (this.isMemoEdited) {
            int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("Edited_Memo"), Resource
                    .getResourceString("Discard_Text?"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ret != JOptionPane.OK_OPTION)
                return;
        }

        String name = JOptionPane.showInputDialog(Resource.getResourceString("Enter_Memo_Name"));
        if (name == null)
            return;

        try {
            Memo existing = MemoModel.getReference().getMemo(name);
            if (existing != null) {
                Errmsg.notice(Resource.getResourceString("Existing_Memo"));
                return;
            }
        } catch (Exception e1) {
            Errmsg.errmsg(e1);
        }

        Memo m = new Memo();
        m.setMemoName(name);
        m.setPrivate(false);
        try {
            MemoModel.getReference().saveMemo(m);
            isMemoEdited = false;
            loadTable();
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }

    }

    private void deleteMemo() {
        String name = getSelectedMemoName();
        if (name == null) {
            Errmsg.notice(Resource.getResourceString("Select_Memo_Warning"));
            return;
        }
        try {
            MemoModel.getReference().delete(name, false);
            isMemoEdited = false;
            loadTable();
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }

    }

    /**
     * This method initializes delButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getDelButton() {
        if (delButton == null) {
            delButton = new JButton();
            delButton.setText(Resource.getResourceString("Delete_Memo"));
            delButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteMemo();
                }
            });
        }
        return delButton;
    }

    /**
     * This method initializes jScrollPane1
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane1() {
        if (jScrollPane1 == null) {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setPreferredSize(new Dimension(400, 400));
            jScrollPane1.setViewportView(getMemoText());
        }
        return jScrollPane1;
    }

    /**
     * This method initializes jSplitPane
     * 
     * @return javax.swing.JSplitPane
     */
    private JSplitPane getJSplitPane() {
        if (jSplitPane == null) {
            jSplitPane = new JSplitPane();
            jSplitPane.setResizeWeight(0.2D);
            jSplitPane.setLeftComponent(getJScrollPane());
            jSplitPane.setRightComponent(getJScrollPane1());
        }
        return jSplitPane;
    }

    /**
     * This method initializes exportButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getExportButton() {
        if (exportButton == null) {
            exportButton = new JButton();
            exportButton.setText(Resource.getResourceString("export"));
            exportButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    StringBuffer sb = new StringBuffer();
                    String s = memoText.getText();
                    for (int i = 0; i < s.length(); i++) {
                        if (s.charAt(i) == '\n') {
                            sb.append('\r');
                        }
                        sb.append(s.charAt(i));

                    }
                    byte[] buf2 = sb.toString().getBytes();
                    ByteArrayInputStream istr = new ByteArrayInputStream(buf2);
                    try {
                        IOHelper.fileSave(".", istr, "");
                    } catch (Exception e1) {
                        Errmsg.errmsg(e1);
                    }
                }
            });
        }
        return exportButton;
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JLabel dateLabel = new JLabel();

    private JPanel getJPanel() {
        if (jPanel == null) {
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            // gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints1.insets = new Insets(4, 4, 4, 4);
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.add(dateLabel, gridBagConstraints1);
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints1.anchor = GridBagConstraints.EAST;
            jPanel.add(getPrivateBox(), gridBagConstraints1);
        }
        return jPanel;
    }

    /**
     * This method initializes privateBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getPrivateBox() {
        if (privateBox == null) {
            privateBox = new JCheckBox();
            privateBox.setText(Resource.getResourceString("Private"));
            privateBox.setHorizontalAlignment(SwingConstants.RIGHT);
            privateBox.setHorizontalTextPosition(SwingConstants.RIGHT);
            privateBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    String name = getSelectedMemoName();
                    if (name != null) {
                        isMemoEdited = true;
                        getSaveButton().setEnabled(true);
                    }
                }
            });

        }
        return privateBox;
    }

    public void remove() {
        // TODO Auto-generated method stub

    }

} // @jve:decl-index=0:visual-constraint="10,10"
