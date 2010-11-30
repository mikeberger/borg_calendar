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
package net.sf.borg.ui.checklist;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.CheckListModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.CheckList;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TablePrinter;
import net.sf.borg.ui.util.TableSorter;

/**
 * UI for editing checkLists. It has a table that shows all checkLists by name
 * and an editing panel for editing checkList text.
 */
public class CheckListPanel extends DockableView implements
		ListSelectionListener, Module, TableModelListener {

	private TableCellRenderer defaultTextRenderer = null;

	/**
	 * renders the item text in strike-through if the item is completed *
	 */
	private class ItemTextRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ItemTextRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			JLabel l = (JLabel) defaultTextRenderer
					.getTableCellRendererComponent(table, obj, isSelected,
							hasFocus, row, column);
			this.setHorizontalAlignment(l.getHorizontalAlignment());
			this.setForeground(l.getForeground());
			this.setBackground(l.getBackground());

			String text = l.getText();
			Boolean complete = (Boolean) table.getModel().getValueAt(row, COMPLETE_COLUMN);
			if (complete == Boolean.TRUE) {
				text = "<HTML><STRIKE>" + text + "</STRIKE></HTML>";
			}

			this.setText(text);
			return this;

		}
	}

	// table columns
	/** The Constant COMPLETE_COLUMN. */
	static private final int COMPLETE_COLUMN = 0;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant TEXT_COLUMN. */
	static private final int TEXT_COLUMN = 1;

	/** The checkList list table. */
	private StripedTable checkListListTable = null;

	/** The edited checkList index. */
	private int editedCheckListIndex = -1;

	/** is checkList changed flag. */
	private boolean isCheckListEdited = false;

	/** The table of checklist items. */
	private StripedTable itemTable = null;


	/**
	 * constructor.
	 */
	public CheckListPanel() {
		super();

		// initialize UI
		initialize();

		refresh();

		// listen for checkList model changes
		CheckListModel.getReference().addListener(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.borg.ui.util.JTabbedPaneWithCloseIcons.TabCloseListener#canClose()
	 */
	@Override
	public boolean canClose() {
		if (isCheckListEdited) {
			/*
			 * confirm discard of changes
			 */
			int ret = JOptionPane.showConfirmDialog(null, Resource
					.getResourceString("Edited_CheckList"), Resource
					.getResourceString("Discard_Text?"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			// if user does not want to lose changes, we need to set
			// the
			// selection back to the edited checkList
			if (ret != JOptionPane.OK_OPTION) {
				return false;
			}
		}

		try {
			isCheckListEdited = false;
			this.loadCheckListsFromModel();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		return true;
	}

	/**
	 * Delete the selected checkList form the model.
	 */
	private void deleteCheckList() {
		String name = getSelectedCheckListName();
		if (name == null) {
			Errmsg.notice(Resource
					.getResourceString("Select_CheckList_Warning"));
			return;
		}

		// confirm delete
		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Really_Delete_")
				+ "?", Resource.getResourceString("Confirm_Delete"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (ret != JOptionPane.OK_OPTION)
			return;

		try {
			CheckListModel.getReference().delete(name, false);
			isCheckListEdited = false;
			loadCheckListsFromModel();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * Copy the selected checkList
	 */
	private void copyCheckList() {
		String name = getSelectedCheckListName();
		if (name == null) {
			Errmsg.notice(Resource
					.getResourceString("Select_CheckList_Warning"));
			return;
		}

		// if the user is currently editing another checkList, confirm that we
		// should discard changes
		if (this.isCheckListEdited) {
			int ret = JOptionPane.showConfirmDialog(null, Resource
					.getResourceString("Edited_CheckList"), Resource
					.getResourceString("Discard_Text?"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (ret != JOptionPane.OK_OPTION)
				return;
		}

		String newname = JOptionPane.showInputDialog(Resource
				.getResourceString("Enter_CheckList_Name"));
		if (newname == null)
			return;

		try {
			CheckList existing = CheckListModel.getReference().getCheckList(
					newname);
			if (existing != null) {
				// checkList name already used
				Errmsg.notice(Resource.getResourceString("Existing_CheckList"));
				return;
			}
		} catch (Exception e1) {
			Errmsg.errmsg(e1);
		}

		try {
			CheckList orig = CheckListModel.getReference().getCheckList(name);
			CheckList copy = orig.clone();
			copy.setCheckListName(newname);
			CheckListModel.getReference().saveCheckList(copy);
			loadCheckListsFromModel();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.MultiView.Module#getComponent()
	 */
	@Override
	public JComponent getComponent() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.DockableView#getFrameTitle()
	 */
	@Override
	public String getFrameTitle() {
		return this.getModuleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.DockableView#getMenuForFrame()
	 */
	@Override
	public JMenuBar getMenuForFrame() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.MultiView.Module#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return Resource.getResourceString("CheckLists");
	}

	/**
	 * Gets the selected checkList name.
	 * 
	 * @return the selected checkList name
	 */
	private String getSelectedCheckListName() {
		int row = checkListListTable.getSelectedRow();
		if (row == -1) {
			return null;
		}

		TableSorter tm = (TableSorter) checkListListTable.getModel();
		String checkListName = (String) tm.getValueAt(row, 0);
		return checkListName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.MultiView.Module#getViewType()
	 */
	@Override
	public ViewType getViewType() {
		return ViewType.CHECKLIST;
	}

	/**
	 * This method initializes the UI.
	 * 
	 * */
	private void initialize() {

		this.setLayout(new GridBagLayout());

		// *****************************
		// checkList split pane
		// *****************************
		JSplitPane checkListSplitPane = new JSplitPane();
		checkListSplitPane.setResizeWeight(0.2D);

		JScrollPane checkListListScroll = new JScrollPane();
		checkListListScroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		checkListListScroll.setPreferredSize(new Dimension(100, 423));

		checkListListTable = new StripedTable();
		checkListListTable
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		checkListListTable.setShowGrid(true);
		checkListListScroll.setViewportView(checkListListTable);

		// table will contain only checkList names
		checkListListTable.setModel(new TableSorter(new String[] { Resource
				.getResourceString("CheckList_Name") },
				new Class[] { java.lang.String.class }));
		ListSelectionModel rowSM = checkListListTable.getSelectionModel();
		rowSM.addListSelectionListener(this);

		checkListSplitPane.setLeftComponent(checkListListScroll);
		JScrollPane checkListTextScroll = new JScrollPane();
		checkListTextScroll.setPreferredSize(new Dimension(400, 400));

		itemTable = new StripedTable();

		defaultTextRenderer = itemTable.getDefaultRenderer(String.class);
		itemTable.setDefaultRenderer(String.class, new ItemTextRenderer());

		TableSorter ts = new TableSorter(new String[] { "",
				Resource.getResourceString("Item") }, new Class[] {
				Boolean.class, String.class }, new boolean[] { true, true });
		itemTable.setModel(ts);

		itemTable.getColumnModel().getColumn(COMPLETE_COLUMN).setMaxWidth(30);
		itemTable.getColumnModel().getColumn(COMPLETE_COLUMN).setMinWidth(20);

		itemTable.getModel().addTableModelListener(this);

		itemTable.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);

		ts.addMouseListenerToHeaderInTable(itemTable);

		// popup menu
		new PopupMenuHelper(itemTable, new PopupMenuHelper.Entry[] {
				new PopupMenuHelper.Entry(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						TableSorter model = (TableSorter) itemTable.getModel();
						int index = itemTable.getSelectedRow();
						Object[] row = new Object[2];
						row[COMPLETE_COLUMN] = Boolean.FALSE;
						row[TEXT_COLUMN] = "";
						model.insertRow(index, row);
					}
				}, "Insert_Above"),
				new PopupMenuHelper.Entry(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						TableSorter model = (TableSorter) itemTable.getModel();
						int index = itemTable.getSelectedRow();
						Object[] row = new Object[2];
						row[COMPLETE_COLUMN] = Boolean.FALSE;
						row[TEXT_COLUMN] = "";
						model.insertRow(index + 1, row);
					}
				}, "Insert_Below"),
				new PopupMenuHelper.Entry(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						removeRow();
					}
				}, "Remove") });

		checkListTextScroll.setViewportView(itemTable);
		checkListSplitPane.setRightComponent(checkListTextScroll);
		this.add(checkListSplitPane, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		// *****************************
		// panel for dates and private check box
		// *****************************
		GridBagConstraints gridBagConstraints1 = GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.HORIZONTAL);

		gridBagConstraints1.anchor = GridBagConstraints.WEST;

		// *****************************
		// button panel
		// *****************************
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton newButton = new JButton();
		newButton.setText(Resource.getResourceString("New_CheckList"));
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newCheckList();
			}
		});
		buttonPanel.add(newButton, null);

		JButton saveButton = new JButton();
		saveButton.setText(Resource.getResourceString("Save_CheckList"));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCheckList();
			}
		});
		buttonPanel.add(saveButton, null);

		JButton deleteButton = new JButton();
		deleteButton.setText(Resource.getResourceString("Delete_CheckList"));
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteCheckList();
			}
		});
		buttonPanel.add(deleteButton, null);

		JButton copyButton = new JButton();
		copyButton.setText(Resource.getResourceString("Copy_CheckList"));
		copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyCheckList();
			}
		});
		buttonPanel.add(copyButton, null);

		JButton uncheckButton = new JButton();
		uncheckButton.setText(Resource.getResourceString("Uncheck_All"));
		uncheckButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uncheckAll();
			}
		});
		buttonPanel.add(uncheckButton, null);

		this.add(buttonPanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH));

		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new GridBagLayout());
		JButton upButton = new JButton();
		upButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Up16.gif")));
		sidePanel.add(upButton, GridBagConstraintsFactory.create(0, 0));
		upButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TableSorter model = (TableSorter) itemTable.getModel();
				int index = itemTable.getSelectedRow();
				if (index > 0) {
					model.moveRow(index, index - 1);
					itemTable.getSelectionModel().setSelectionInterval(
							index - 1, index - 1);
				}
			}
		});

		JButton downButton = new JButton();
		downButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Down16.gif")));
		downButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TableSorter model = (TableSorter) itemTable.getModel();
				int index = itemTable.getSelectedRow();
				if (index < model.getRowCount() - 1) {
					model.moveRow(index, index + 1);
					itemTable.getSelectionModel().setSelectionInterval(
							index + 1, index + 1);
				}
			}
		});
		sidePanel.add(downButton, GridBagConstraintsFactory.create(0, 1));

		this.add(sidePanel, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.MultiView.Module#initialize(net.sf.borg.ui.MultiView)
	 */
	@Override
	public void initialize(MultiView parent) {

		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
				"/resource/Preferences16.gif")), getModuleName(),
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						par.setView(ViewType.CHECKLIST);
					}
				});

	}

	/**
	 * Load all checkLists from the model.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void loadCheckListsFromModel() throws Exception {
		checkListListTable.clearSelection();
		TableSorter tm = (TableSorter) checkListListTable.getModel();
		tm.setRowCount(0);
		Collection<String> names = CheckListModel.getReference().getNames();
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			tm.addRow(new Object[] { it.next() });
		}

		TableSorter model = (TableSorter) itemTable.getModel();
		model.setRowCount(0);
		isCheckListEdited = false;

	}

	/**
	 * Load items from a checklist into the item table.
	 * 
	 * @param cl
	 *            the checklist
	 */
	private void loadItems(CheckList cl) {
		TableSorter model = (TableSorter) itemTable.getModel();
		model.setRowCount(0);

		if (cl != null) {
			for (CheckList.Item item : cl.getItems()) {
				Object[] row = new Object[2];
				row[COMPLETE_COLUMN] = item.getChecked();
				row[TEXT_COLUMN] = item.getText();
				model.addRow(row);
			}

			Object[] addrow = new Object[2];
			addrow[COMPLETE_COLUMN] = Boolean.FALSE;
			addrow[TEXT_COLUMN] = "";
			model.addRow(addrow);
		}

	}

	/**
	 * create a new checkList.
	 */
	private void newCheckList() {

		// if the user is currently editing another checkList, confirm that we
		// should discard changes
		if (this.isCheckListEdited) {
			int ret = JOptionPane.showConfirmDialog(null, Resource
					.getResourceString("Edited_CheckList"), Resource
					.getResourceString("Discard_Text?"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (ret != JOptionPane.OK_OPTION)
				return;
		}

		// get checkList name
		String name = JOptionPane.showInputDialog(Resource
				.getResourceString("Enter_CheckList_Name"));
		if (name == null)
			return;

		try {
			CheckList existing = CheckListModel.getReference().getCheckList(
					name);
			if (existing != null) {
				// checkList name already used
				Errmsg.notice(Resource.getResourceString("Existing_CheckList"));
				return;
			}
		} catch (Exception e1) {
			Errmsg.errmsg(e1);
		}

		// create a new empty checkList and save
		CheckList m = new CheckList();
		m.setCheckListName(name);
		try {
			CheckListModel.getReference().saveCheckList(m);
			isCheckListEdited = false;
			loadCheckListsFromModel();
			selectCheckList(name);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.MultiView.Module#print()
	 */
	@Override
	public void print() {
		try {
			String selectedCheckList = getSelectedCheckListName();
			if (selectedCheckList == null)
				return;
			TablePrinter.printTable(itemTable);

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * refresh the UI. This does not do anything if the user is currently in the
	 * middle of editing a checkList.
	 */
	@Override
	public void refresh() {

		// if the user is editing a row, don't process the refresh
		if (isCheckListEdited)
			return;
		try {
			loadCheckListsFromModel();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * Removes the selected row.
	 */
	private void removeRow() {
		TableSorter model = (TableSorter) itemTable.getModel();
		int index = itemTable.getSelectedRow();
		model.removeRow(index);
	}

	/**
	 * Save the selected checkList to the model.
	 */
	private void saveCheckList() {
		String name = getSelectedCheckListName();
		if (name == null) {
			Errmsg.notice(Resource
					.getResourceString("Select_CheckList_Warning"));
			return;
		}
		try {
			CheckList m = CheckListModel.getReference().getCheckList(name);
			setItems(m);

			CheckListModel.getReference().saveCheckList(m);
			isCheckListEdited = false;
			loadCheckListsFromModel();
			selectCheckList(name);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * select the named checkList for editing.
	 * 
	 * @param checkListName
	 *            the checkList name
	 */
	public void selectCheckList(String checkListName) {
		TableSorter tm = (TableSorter) checkListListTable.getModel();
		int rows = tm.getRowCount();
		for (int i = 0; i < rows; ++i) {
			String name = (String) tm.getValueAt(i, 0);
			if (checkListName.equals(name)) {
				// select the checkList in the table. this will fire events that
				// cause us to open the checkList for edit
				checkListListTable.getSelectionModel().setSelectionInterval(i,
						i);
				break;
			}
		}
	}

	/**
	 * Sets the items in the checklist object from the item table contents.
	 * 
	 * @param cl
	 *            the new items
	 */
	private void setItems(CheckList cl) {
		if (itemTable.isEditing())
			itemTable.getCellEditor().stopCellEditing();

		cl.getItems().clear();

		TableSorter model = (TableSorter) itemTable.getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			CheckList.Item item = new CheckList.Item();
			item.setChecked((Boolean) model.getValueAt(i, COMPLETE_COLUMN));
			item.setText((String) model.getValueAt(i, TEXT_COLUMN));
			if (!item.getText().isEmpty())
				cl.getItems().add(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.swing.event.TableModelListener#tableChanged(javax.swing.event.
	 * TableModelEvent)
	 */
	@Override
	public void tableChanged(TableModelEvent arg0) {


		// ignore the table sorters events - we only care about the
		// underlying table model - which is tablesorter.newtablemodel
		// tablesorter is crap
		if (arg0.getSource() instanceof TableSorter)
			return;


		// ignore insert - this only happens when blank rows are added
		if (arg0.getType() == TableModelEvent.INSERT)
			return;

		isCheckListEdited = true;
		editedCheckListIndex = checkListListTable.getSelectedRow();

		// always maintain a new "add" row as the table is updated (without
		// being saved)

		// ignore delete events when deciding to add a blank row
		// problem caused when the table is cleared
		if (arg0.getType() == TableModelEvent.DELETE)
			return;
		
		TableSorter model = (TableSorter) itemTable.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			String text = (String) model.getValueAt(i, TEXT_COLUMN);
			if (text.isEmpty()) {
				// already has a blank row - so don't add one
				return;
			}
		}
		Object[] addrow = new Object[2];
		addrow[COMPLETE_COLUMN] = Boolean.FALSE;
		addrow[TEXT_COLUMN] = "";
		model.addRow(addrow);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.borg.model.Model.Listener#update(net.sf.borg.model.Model.ChangeEvent
	 * )
	 */
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * react to the user selecting a checkList in the checkList list. open it
	 * for edit
	 * 
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// Ignore extra messages.
		if (e.getValueIsAdjusting())
			return;

		// if the user has edited a checkList and is changing the checkList
		// selection...
		if (isCheckListEdited) {

			// if the selection is remaining (or returning) to the
			// edited checkList
			// then do nothing - may be returning to the edited checkList
			// due to the
			// setSelectionInterval line
			// below that resets the selection
			if (editedCheckListIndex == checkListListTable.getSelectedRow())
				return;

			// selection is moving to a new checkList - prompt about
			// discarding
			// changes
			int ret = JOptionPane.showConfirmDialog(null, Resource
					.getResourceString("Edited_CheckList"), Resource
					.getResourceString("Discard_Text?"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			// if user does not want to lose changes, we need to set
			// the
			// selection back to the edited checkList
			if (ret != JOptionPane.OK_OPTION) {
				checkListListTable.getSelectionModel().setSelectionInterval(
						editedCheckListIndex, editedCheckListIndex);
				return;
			}
		}

		String checkListName = getSelectedCheckListName();
		if (checkListName == null) {
			this.loadItems(null);
		} else {

			// show the selected checkList
			try {
				CheckList m = CheckListModel.getReference().getCheckList(
						checkListName);
				this.loadItems(m);
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}

		}
		isCheckListEdited = false;

	}

	private void uncheckAll() {

		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Uncheck_All")
				+ "?", Resource.getResourceString("Uncheck_All"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (ret != JOptionPane.OK_OPTION) {
			return;
		}

		TableSorter model = (TableSorter) itemTable.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			model.setValueAt(Boolean.FALSE, i, COMPLETE_COLUMN);
		}
	}

	@Override
	public void cleanUp() {
		// on close - unselect checklist
		checkListListTable.clearSelection();
	}
}
