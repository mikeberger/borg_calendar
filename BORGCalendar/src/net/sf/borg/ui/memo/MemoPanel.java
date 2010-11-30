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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.print.PrinterException;
import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.IOHelper;
import net.sf.borg.common.Resource;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PasswordHelper;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TableSorter;

/**
 * UI for editing memos. It has a table that shows all memos by name and an
 * editing panel for editing memo text.
 */
public class MemoPanel extends DockableView implements ListSelectionListener,
		Module {

	private static final long serialVersionUID = 1L;

	/** The memo date format. */
	private static SimpleDateFormat memoDateFormat = new SimpleDateFormat(
			"MM/dd/yyyy hh:mm aa");

	/** The date label. */
	private JLabel dateLabel = new JLabel();

	/** The edited memo index. */
	private int editedMemoIndex = -1;

	/** is memo changed flag */
	private boolean isMemoEdited = false;

	/** The memo list table. */
	private StripedTable memoListTable = null;

	/** The memo text. */
	private JTextArea memoText = null;

	/** The save button. */
	private JButton saveButton = null;

	/** decrypt button */
	private JButton decryptButton = null;

	/**
	 * encryption checkbox
	 */
	private JCheckBox encryptBox = null;

	/**
	 * constructor.
	 */
	public MemoPanel() {
		super();

		// initialize UI
		initialize();

		memoText.setEditable(false);
		saveButton.setEnabled(false);
		decryptButton.setEnabled(false);

		refresh();

		// listen for memo model changes
		MemoModel.getReference().addListener(this);

	}

	/**
	 * Delete the selected memo form the model.
	 */
	private void deleteMemo() {
		String name = getSelectedMemoName();
		if (name == null) {
			Errmsg.notice(Resource.getResourceString("Select_Memo_Warning"));
			return;
		}
		
		// confirm delete
		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Really_Delete_")
				+ "?", Resource
				.getResourceString("Confirm_Delete"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (ret != JOptionPane.OK_OPTION)
			return;

		try {
			MemoModel.getReference().delete(name, false);
			isMemoEdited = false;
			loadMemosFromModel();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * Gets the selected memo name.
	 * 
	 * @return the selected memo name
	 */
	private String getSelectedMemoName() {
		int row = memoListTable.getSelectedRow();
		if (row == -1) {
			return null;
		}

		TableSorter tm = (TableSorter) memoListTable.getModel();
		String memoName = (String) tm.getValueAt(row, 0);
		return memoName;
	}

	/**
	 * This method initializes the UI.
	 * 
	 * */
	private void initialize() {

		this.setLayout(new GridBagLayout());

		// *****************************
		// memo split pane
		// *****************************
		JSplitPane memoSplitPane = new JSplitPane();
		memoSplitPane.setResizeWeight(0.2D);

		JScrollPane memoListScroll = new JScrollPane();
		memoListScroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		memoListScroll.setPreferredSize(new Dimension(100, 423));

		memoListTable = new StripedTable();
		memoListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		memoListTable.setShowGrid(true);
		memoListScroll.setViewportView(memoListTable);

		// table will contain only memo names
		memoListTable.setModel(new TableSorter(new String[] { Resource
				.getResourceString("Memo_Name") },
				new Class[] { java.lang.String.class }));
		ListSelectionModel rowSM = memoListTable.getSelectionModel();
		rowSM.addListSelectionListener(this);

		memoSplitPane.setLeftComponent(memoListScroll);
		JScrollPane memoTextScroll = new JScrollPane();
		memoTextScroll.setPreferredSize(new Dimension(400, 400));

		memoText = new JTextArea();
		memoText.setLineWrap(true);
		memoText.setWrapStyleWord(true);

		// if the memo text is edited, then set a change flag and enable the
		// save button
		memoText.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				isMemoEdited = true;
				editedMemoIndex = memoListTable.getSelectedRow();
				saveButton.setEnabled(true);
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				isMemoEdited = true;
				editedMemoIndex = memoListTable.getSelectedRow();
				saveButton.setEnabled(true);
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				isMemoEdited = true;
				editedMemoIndex = memoListTable.getSelectedRow();
				saveButton.setEnabled(true);
			}
		});

		memoText.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_F) {
					doFind();
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// empty
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// empty
			}

		});
		memoTextScroll.setViewportView(memoText);
		memoSplitPane.setRightComponent(memoTextScroll);
		this.add(memoSplitPane, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		// *****************************
		// panel for dates and private check box
		// *****************************
		GridBagConstraints gridBagConstraints1 = GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.HORIZONTAL);

		gridBagConstraints1.anchor = GridBagConstraints.WEST;

		JPanel dateAndPrivatePanel = new JPanel();
		dateAndPrivatePanel.setLayout(new GridBagLayout());
		dateAndPrivatePanel.add(dateLabel, gridBagConstraints1);

		this.add(dateAndPrivatePanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.HORIZONTAL));

		// *****************************
		// button panel
		// *****************************
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton newButton = new JButton();
		newButton.setText(Resource.getResourceString("New_Memo"));
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newMemo();
			}
		});
		buttonPanel.add(newButton, null);

		saveButton = new JButton();
		saveButton.setText(Resource.getResourceString("Save_Memo"));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveMemo();
			}
		});
		buttonPanel.add(saveButton, null);

		JButton deleteButton = new JButton();
		deleteButton.setText(Resource.getResourceString("Delete_Memo"));
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteMemo();
			}
		});
		buttonPanel.add(deleteButton, null);

		JButton exportButton = new JButton();
		exportButton.setText(Resource.getResourceString("export"));
		exportButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// export a single memo to a file
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
		buttonPanel.add(exportButton, null);

		decryptButton = new JButton();
		decryptButton.setText(Resource.getResourceString("decrypt"));
		decryptButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					Memo m = MemoModel.getReference().getMemo(
							getSelectedMemoName());
					String pw = PasswordHelper.getReference().getPassword();
					if (pw == null)
						return;
					m.decrypt(pw);

					memoText.setText(m.getMemoText());
					memoText.setEditable(true);
					decryptButton.setEnabled(false);
					saveButton.setEnabled(false);
					clearEditFlag();
					memoText.setCaretPosition(0);

				} catch (Exception e1) {
					Errmsg.errmsg(e1);
				}

			}
		});
		buttonPanel.add(decryptButton, null);

		encryptBox = new JCheckBox();
		encryptBox.setText(Resource.getResourceString("EncryptOnSave"));
		buttonPanel.add(encryptBox, null);

		this.add(buttonPanel, GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.BOTH));

	}

	/**
	 * Load memos from the model
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void loadMemosFromModel() throws Exception {
		memoListTable.clearSelection();
		TableSorter tm = (TableSorter) memoListTable.getModel();
		tm.setRowCount(0);
		Collection<String> names = MemoModel.getReference().getNames();
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			tm.addRow(new Object[] { it.next() });
		}
	}

	/**
	 * create a new memo
	 */
	private void newMemo() {

		// if the user is currently editing another memo, confirm that we
		// should discard changes
		if (this.isMemoEdited) {
			int ret = JOptionPane.showConfirmDialog(null, Resource
					.getResourceString("Edited_Memo"), Resource
					.getResourceString("Discard_Text?"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (ret != JOptionPane.OK_OPTION)
				return;
		}

		// get memo name
		String name = JOptionPane.showInputDialog(Resource
				.getResourceString("Enter_Memo_Name"));
		if (name == null)
			return;

		try {
			Memo existing = MemoModel.getReference().getMemo(name);
			if (existing != null) {
				// memo name already used
				Errmsg.notice(Resource.getResourceString("Existing_Memo"));
				return;
			}
		} catch (Exception e1) {
			Errmsg.errmsg(e1);
		}

		// create a new empty memo and save
		Memo m = new Memo();
		m.setMemoName(name);
		try {
			MemoModel.getReference().saveMemo(m);
			isMemoEdited = false;
			loadMemosFromModel();
			selectMemo(name);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * refresh the UI. This does not do anything if the user is currently in the
	 * middle of editing a memo.
	 */
	@Override
	public void refresh() {

		// if the user is editing a row, don't process the refresh
		if (isMemoEdited)
			return;
		try {
			loadMemosFromModel();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * Save the selected memo to the model
	 */
	private void saveMemo() {
		String name = getSelectedMemoName();
		if (name == null) {
			Errmsg.notice(Resource.getResourceString("Select_Memo_Warning"));
			return;
		}
		try {
			Memo m = MemoModel.getReference().getMemo(name);
			m.setMemoText(memoText.getText());
			m.setEncrypted(false);
			if (encryptBox.isSelected()) {
				String pw = PasswordHelper.getReference().getPassword();
				if (pw == null)
					return;
				m.encrypt(pw);
			}
			MemoModel.getReference().saveMemo(m);
			isMemoEdited = false;
			loadMemosFromModel();
			selectMemo(name);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * selecte the named memo for editing
	 * 
	 * @param memoName
	 *            the memo name
	 */
	public void selectMemo(String memoName) {
		TableSorter tm = (TableSorter) memoListTable.getModel();
		int rows = tm.getRowCount();
		for (int i = 0; i < rows; ++i) {
			String name = (String) tm.getValueAt(i, 0);
			if (memoName.equals(name)) {
				// select the memo in the table. this will fire events that
				// cause us to open the memo for edit
				memoListTable.getSelectionModel().setSelectionInterval(i, i);
				break;
			}
		}
	}

	/**
	 * react to the user selecting a memo in the memo list. open it for edit
	 */
	@Override
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
			int ret = JOptionPane.showConfirmDialog(null, Resource
					.getResourceString("Edited_Memo"), Resource
					.getResourceString("Discard_Text?"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			// if user does not want to lose changes, we need to set
			// the
			// selection back to the edited memo
			if (ret != JOptionPane.OK_OPTION) {
				memoListTable.getSelectionModel().setSelectionInterval(
						editedMemoIndex, editedMemoIndex);
				return;
			}
		}

		String memoName = getSelectedMemoName();
		if (memoName == null) {
			memoText.setText("");
			memoText.setEditable(false);
			dateLabel.setText("");
			encryptBox.setSelected(false);
			decryptButton.setEnabled(false);
		} else {

			// show the selected memo
			String text;
			try {
				Memo m = MemoModel.getReference().getMemo(memoName);
				text = m.getMemoText();

				//
				// create the date label string
				//
				String datetext = Resource.getResourceString("created") + ": ";
				if (m.getCreated() != null)
					datetext += memoDateFormat.format(m.getCreated());
				else
					datetext += Resource.getResourceString("unknown");
				datetext += "           "
						+ Resource.getResourceString("updated") + ": ";
				if (m.getUpdated() != null)
					datetext += memoDateFormat.format(m.getUpdated());
				else
					datetext += Resource.getResourceString("unknown");
				dateLabel.setText(datetext);

				encryptBox.setSelected(m.isEncrypted());
				if (m.isEncrypted()) {
					memoText.setText(Resource
							.getResourceString("EncryptedItem"));
					memoText.setEditable(false);
					decryptButton.setEnabled(true);
				} else {
					memoText.setEditable(true);
					memoText.setText(text);
					decryptButton.setEnabled(false);

				}
				memoText.setCaretPosition(0);
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}

		}
		isMemoEdited = false;
		saveButton.setEnabled(false);

	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public String getModuleName() {
		return Resource.getResourceString("Memos");
	}

	@Override
	public void initialize(MultiView parent) {

		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
				"/resource/Edit16.gif")), getModuleName(),
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						par.setView(ViewType.MEMO);
					}
				});

	}

	@Override
	public void print() {
		try {
			String selectedMemo = getSelectedMemoName();
			if( selectedMemo == null ) return;
			
			this.memoText.print(new MessageFormat(selectedMemo),
					new MessageFormat("{0}"));
		} catch (PrinterException e) {
			Errmsg.errmsg(e);
		}

	}

	private void clearEditFlag() {
		isMemoEdited = false;
	}

	@Override
	public ViewType getViewType() {
		return ViewType.MEMO;
	}

	@Override
	public boolean canClose() {
		if (isMemoEdited) {
			/*
			 * confirm discard of changes
			 */
			int ret = JOptionPane.showConfirmDialog(null, Resource
					.getResourceString("Edited_Memo"), Resource
					.getResourceString("Discard_Text?"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			// if user does not want to lose changes, we need to set
			// the
			// selection back to the edited memo
			if (ret != JOptionPane.OK_OPTION) {
				return false;
			}
		}

		try {
			isMemoEdited = false;
			this.loadMemosFromModel();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		return true;
	}

	/**
	 * find action
	 */
	private String searchString = null;
	private void doFind() {

		// current caret is the root of the search. search is forwards.
		int caretIndex = memoText.getCaretPosition();

		// prompt for search string - remember last string
		searchString = JOptionPane.showInputDialog(null, Resource
				.getResourceString("Search_For"), searchString);
		if (searchString != null) {

			// search forwards
			int foundIndex = memoText.getText().indexOf(searchString, caretIndex);
			if (foundIndex != -1) {
				// highlight found text - this also moves caret to end of found string as a side-effect,
				// which is what we want
				memoText.select(foundIndex, foundIndex + searchString.length());
			} else {
				// indicate string not found - put caret back to top so next search will
				// begin at the top
				JOptionPane.showMessageDialog(null, Resource
						.getResourceString("Not_Found_End"));
				memoText.setCaretPosition(0);
			}
		}

	}
	
	@Override
	public String getFrameTitle() {
		return this.getModuleName();
	}

	@Override
	public JMenuBar getMenuForFrame() {
		return null;
	}

	@Override
	public void cleanUp() {
		// on close - unselect memo
		memoListTable.clearSelection();
	}

	
}
