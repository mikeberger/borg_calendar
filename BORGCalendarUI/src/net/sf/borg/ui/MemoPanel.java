package net.sf.borg.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.borg.common.ui.TableSorter;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Resource;
import net.sf.borg.model.Memo;
import net.sf.borg.model.MemoModel;

public class MemoPanel extends JPanel implements ListSelectionListener {

    private static final long serialVersionUID = 1L;

    private JScrollPane jScrollPane = null;

    private JTable memoListTable = null;

    private JTextArea memoText = null;

    private JPanel buttonPanel = null;

    private JButton newButton = null;

    private JButton saveButton = null;

    private JButton delButton = null;

    /**
         * This is the default constructor
         */
    public MemoPanel() {
	super();
	initialize();

	memoListTable.setModel(new TableSorter(new String[] { Resource
		.getResourceString("Memo_Name") },
		new Class[] { java.lang.String.class }));
	ListSelectionModel rowSM = memoListTable.getSelectionModel();
	rowSM.addListSelectionListener(this);
	memoText.setEditable(false);
	refresh();
    }

    /**
         * This method initializes this
         * 
         * @return void
         */
    private void initialize() {
	GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
	gridBagConstraints2.gridx = 0;
	gridBagConstraints2.gridwidth = 3;
	gridBagConstraints2.fill = GridBagConstraints.BOTH;
	gridBagConstraints2.insets = new Insets(4, 4, 4, 4);
	gridBagConstraints2.gridy = 1;
	GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
	gridBagConstraints1.fill = GridBagConstraints.BOTH;
	gridBagConstraints1.insets = new Insets(4, 4, 4, 4);
	gridBagConstraints1.gridx = 2;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.weightx = 1.0;
	GridBagConstraints gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.fill = GridBagConstraints.BOTH;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.weightx = 0.2D;
	gridBagConstraints.weighty = 1.0;
	gridBagConstraints.insets = new Insets(4, 4, 4, 4);
	gridBagConstraints.gridx = 0;
	// this.setSize(300, 200);
	this.setLayout(new GridBagLayout());
	this.add(getJScrollPane(), gridBagConstraints);
	this.add(getMemoText(), gridBagConstraints1);
	this.add(getButtonPanel(), gridBagConstraints2);
    }

    /**
         * This method initializes jScrollPane
         * 
         * @return javax.swing.JScrollPane
         */
    private JScrollPane getJScrollPane() {
	if (jScrollPane == null) {
	    jScrollPane = new JScrollPane();
	    jScrollPane
		    .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
	    memoListTable = new JTable();
	    memoListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    memoListTable.setShowGrid(true);
	}
	return memoListTable;
    }

    /**
         * This method initializes memoText
         * 
         * @return javax.swing.JTextField
         */
    private JTextArea getMemoText() {
	if (memoText == null) {
	    memoText = new JTextArea();
	    memoText.setLineWrap(true);

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
	    newButton.setText(Resource.getPlainResourceString("New_Memo"));
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
	    saveButton.setText(Resource.getPlainResourceString("Save_Memo"));
	    saveButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    saveMemo();
		}
	    });
	}
	return saveButton;
    }

    public void refresh() {

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
	Collection names = MemoModel.getReference().getNames();
	// System.out.println("Names.size=" + names.size());
	Iterator it = names.iterator();
	while (it.hasNext()) {
	    tm.addRow(new Object[] { (String) it.next() });
	}
    }

    public void valueChanged(ListSelectionEvent e) {
	// Ignore extra messages.
	if (e.getValueIsAdjusting())
	    return;

	String memoName = getSelectedMemoName();
	if (memoName == null) {
	    memoText.setText("");
	    memoText.setEditable(false);
	    return;
	}

	String text;
	try {
	    text = MemoModel.getReference().getMemo(memoName).getMemoText();
	} catch (Exception e1) {
	    Errmsg.errmsg(e1);
	    return;
	}
	memoText.setEditable(true);
	memoText.setText(text);

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
	    Errmsg.notice(Resource
		    .getPlainResourceString("Select_Memo_Warning"));
	    return;
	}
	try {
	    Memo m = MemoModel.getReference().getMemo(name);
	    m.setMemoText(memoText.getText());
	    MemoModel.getReference().saveMemo(m);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	refresh();
    }

    private void newMemo() {
	String name = JOptionPane.showInputDialog(Resource
		.getPlainResourceString("Enter_Memo_Name"));
	if (name == null)
	    return;

	try {
	    Memo existing = MemoModel.getReference().getMemo(name);
	    if (existing != null) {
		Errmsg.notice(Resource.getPlainResourceString("Existing_Memo"));
		return;
	    }
	} catch (Exception e1) {
	    Errmsg.errmsg(e1);
	}

	Memo m = new Memo();
	m.setMemoName(name);
	try {
	    MemoModel.getReference().saveMemo(m);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
	refresh();

    }

    private void deleteMemo()
    {
	String name = getSelectedMemoName();
	if (name == null) {
	    Errmsg.notice(Resource
		    .getPlainResourceString("Select_Memo_Warning"));
	    return;
	}
	try {
	    MemoModel.getReference().delete(name, true);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	refresh();
    }
    
    /**
     * This method initializes delButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getDelButton() {
        if (delButton == null) {
    	delButton = new JButton();
    	delButton.setText(Resource.getPlainResourceString("Delete_Memo"));
    	delButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    deleteMemo();
		}
	    });
        }
        return delButton;
    }
}
