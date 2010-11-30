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

package net.sf.borg.ui.address;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Address;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TablePrinter;
import net.sf.borg.ui.util.TableSorter;

/**
 * UI that displays the address book in tabular form and allows actions to be
 * taken on addresses
 * 
 */
public class AddrListView extends DockableView implements Module {

	private static final long serialVersionUID = 1L;

	private Collection<Address> addrs_; // list of rows currently displayed

	// action listeners for buttons that are reused in the menu items
	private ActionListener alAddNew, alEdit, alDelete, alFind;

	// various buttons
	private JButton delbutton;
	private JButton editbutton;
	private JButton findbutton;
	private JButton newbutton;

	// scroll for table
	private JScrollPane tableScrollPane;

	// the table
	private StripedTable addressTable;

	/**
	 * constructor
	 */
	public AddrListView() {

		super();

		addModel(AddressModel.getReference());

		this.setLayout(new GridBagLayout());

		// init the gui components
		initComponents();

		// set the column headings and types
		addressTable.setModel(new TableSorter(new String[] {
				Resource.getResourceString("First"),
				Resource.getResourceString("Last"),
				Resource.getResourceString("Email"),
				Resource.getResourceString("Screen_Name"),
				Resource.getResourceString("Home_Phone"),
				Resource.getResourceString("Work_Phone"),
				Resource.getResourceString("Birthday") }, new Class[] {
				java.lang.String.class, java.lang.String.class,
				java.lang.String.class, java.lang.String.class,
				java.lang.String.class, java.lang.String.class,
				java.util.Date.class }));

		refresh();

	}

	/**
	 * delete action
	 */
	private void delbuttonActionPerformed() {
		// figure out which row is selected to be deleted
		int[] indices = addressTable.getSelectedRows();
		if (indices.length == 0)
			return;

		// confirm delete
		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Delete_Addresses"), Resource
				.getResourceString("Delete"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (ret != JOptionPane.OK_OPTION)
			return;

		// collect the addresses. cannot delete them in this loop
		// as that would change the indexes
		Collection<Address> addresses = new ArrayList<Address>();
		for (int i = 0; i < indices.length; ++i) {
			int index = indices[i];
			// need to ask the table for the original (befor sorting) index
			// of the selected row
			TableSorter tm = (TableSorter) addressTable.getModel();
			int k = tm.getMappedIndex(index); // get original index - not
			// current sorted position
			// in tbl
			Object[] oa = addrs_.toArray();
			Address addr = (Address) oa[k];
			addresses.add(addr);

		}

		// delete the addresses
		for (Address addr : addresses) {
			AddressModel.getReference().delete(addr);
		}

	}

	/**
	 * edit an address
	 */
	private void editRow() {
		// figure out which row is selected.
		int index = addressTable.getSelectedRow();
		if (index == -1)
			return;
		addressTable.getSelectionModel().setSelectionInterval(index, index);
		// ensure only one row is selected.

		try {
			// need to ask the table for the original (befor sorting) index of
			// the selected row
			TableSorter tm = (TableSorter) addressTable.getModel();
			int k = tm.getMappedIndex(index); // get original index - not
			// current sorted position in
			// tbl
			Object[] oa = addrs_.toArray();
			Address addr = (Address) oa[k];

			// show an address editor for the address
			new AddressView(addr).showView();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * find action
	 */
	private void findbuttonActionPerformed() {

		// Search for a string
		String searchstring = "";
		JScrollBar vScrollBar = null;
		do {
			searchstring = JOptionPane.showInputDialog(null, Resource
					.getResourceString("Search_For"), searchstring);
			if (searchstring != null) {
				int tablerowcount = addressTable.getRowCount();
				int tablecolcount = addressTable.getColumnCount();

				for (int iRow = 0; iRow < tablerowcount; iRow++) {
					for (int jCol = 0; jCol < tablecolcount; jCol++) {
						String colvalue = null;
						java.util.Date colvaluedt = null;
						try {
							colvalue = (String) addressTable.getValueAt(iRow,
									jCol);
						} catch (ClassCastException ccex) {
							colvaluedt = (java.util.Date) addressTable
									.getValueAt(iRow, jCol);
							colvalue = colvaluedt.toString();
						}
						if (searchstring != null) {
							boolean match = searchstring
									.equalsIgnoreCase(colvalue);
							if (!match && colvalue != null) {
								String colvalue2 = colvalue.toLowerCase();
								String searchstring2 = searchstring
										.toLowerCase();
								int indexint = colvalue2.indexOf(searchstring2,
										0);
								match = indexint > -1;
							}
							if (match) {
								addressTable
										.setRowSelectionInterval(iRow, iRow);
								vScrollBar = tableScrollPane
										.getVerticalScrollBar();

								int maxVal = vScrollBar.getMaximum();
								int oneCellScrollValue = maxVal / tablerowcount;

								vScrollBar.setValue(iRow * oneCellScrollValue);
								searchstring = JOptionPane
										.showInputDialog(
												null,
												Resource
														.getResourceString("Search_Next"),
												searchstring);
							}
						}
					}
				}
				if (searchstring != null) {
					JOptionPane.showMessageDialog(null, Resource
							.getResourceString("Not_Found_End"));
				}
			}
		} while (searchstring != null);

	}

	@Override
	public String getFrameTitle() {
		return Resource.getResourceString("Address_Book");
	}

	@Override
	public JMenuBar getMenuForFrame() {

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu();
		JMenuItem printList = new JMenuItem();

		ResourceHelper.setText(fileMenu, "Action");

		JMenuItem mnuitm = new JMenuItem();
		ResourceHelper.setText(mnuitm, "Add_New");
		mnuitm.setIcon(newbutton.getIcon());
		mnuitm.addActionListener(alAddNew);
		fileMenu.add(mnuitm);

		mnuitm = new JMenuItem();
		ResourceHelper.setText(mnuitm, "Edit");
		mnuitm.setIcon(editbutton.getIcon());
		mnuitm.addActionListener(alEdit);
		fileMenu.add(mnuitm);

		mnuitm = new JMenuItem();
		ResourceHelper.setText(mnuitm, "Delete");
		mnuitm.setIcon(delbutton.getIcon());
		mnuitm.addActionListener(alDelete);
		fileMenu.add(mnuitm);

		mnuitm = new JMenuItem();
		ResourceHelper.setText(mnuitm, "Find");
		mnuitm.setIcon(findbutton.getIcon());
		mnuitm.addActionListener(alFind);
		fileMenu.add(mnuitm);

		ResourceHelper.setText(printList, "Print_List");
		printList.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Print16.gif")));
		printList.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					TablePrinter.printTable(addressTable);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		fileMenu.add(printList);

		menuBar.add(fileMenu);

		return menuBar;

	}

	/**
	 * init the UI components
	 */
	private void initComponents() {

		tableScrollPane = new JScrollPane();
		addressTable = new StripedTable();
		JPanel buttonPanel = new JPanel();
		newbutton = new JButton();
		editbutton = new JButton();
		delbutton = new JButton();
		findbutton = new JButton();

		tableScrollPane.setPreferredSize(new java.awt.Dimension(554, 404));
		addressTable.setBorder(new LineBorder(new java.awt.Color(0, 0, 0)));
		// jTable1.setGridColor(java.awt.Color.blue);
		DefaultListSelectionModel mylsmodel = new DefaultListSelectionModel();
		mylsmodel
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		addressTable.setSelectionModel(mylsmodel);
		addressTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (evt.getClickCount() < 2)
					return;
				editRow();
			}
		});

		tableScrollPane.setViewportView(addressTable);
		add(tableScrollPane, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		newbutton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Add16.gif")));
		ResourceHelper.setText(newbutton, "Add_New");
		newbutton
				.addActionListener(alAddNew = new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						Address addr = AddressModel.getReference().newAddress();
						addr.setKey(-1);
						new AddressView(addr).showView();
					}
				});

		buttonPanel.add(newbutton);

		editbutton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Edit16.gif")));
		ResourceHelper.setText(editbutton, "Edit");
		editbutton
				.addActionListener(alEdit = new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						editRow();

					}
				});

		buttonPanel.add(editbutton);

		delbutton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		ResourceHelper.setText(delbutton, "Delete");
		delbutton
				.addActionListener(alDelete = new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						delbuttonActionPerformed();
					}
				});

		buttonPanel.add(delbutton);

		// Find Button - Search a text in Address List
		findbutton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Find16.gif")));
		ResourceHelper.setText(findbutton, "Find");
		findbutton
				.addActionListener(alFind = new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						findbuttonActionPerformed();
					}
				});

		buttonPanel.add(findbutton);

		add(buttonPanel, GridBagConstraintsFactory.create(0, 1));

		// add context menu actions
		new PopupMenuHelper(addressTable, new PopupMenuHelper.Entry[] {
				new PopupMenuHelper.Entry(alAddNew, "Add_New"),
				new PopupMenuHelper.Entry(alEdit, "Edit"),
				new PopupMenuHelper.Entry(alDelete, "Delete"),
				new PopupMenuHelper.Entry(alFind, "Find") });

	}

	@Override
	public void refresh() {
		AddressModel addrmod_ = AddressModel.getReference();

		try {
			addrs_ = addrmod_.getAddresses();
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}

		// init the table to empty
		TableSorter tm = (TableSorter) addressTable.getModel();
		tm.addMouseListenerToHeaderInTable(addressTable);
		tm.setRowCount(0);

		Iterator<Address> it = addrs_.iterator();
		while (it.hasNext()) {
			Address r = it.next();

			try {

				// add the table row
				Object[] ro = new Object[7];
				ro[0] = r.getFirstName();
				ro[1] = r.getLastName();
				ro[2] = r.getEmail();
				ro[3] = r.getScreenName();
				ro[4] = r.getHomePhone();
				ro[5] = r.getWorkPhone();
				ro[6] = r.getBirthday();
				tm.addRow(ro);
				tm.tableChanged(new TableModelEvent(tm));
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}

		}

		// sort the table by last name
		tm.sortByColumn(1);

	}

	@Override
	public String getModuleName() {
		return Resource.getResourceString("Address_Book");
	}

	@Override
	public JPanel getComponent() {
		return this;
	}

	@Override
	public void initialize(MultiView parent) {
		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
				"/resource/addr16.jpg")), getModuleName(),
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						par.setView(getViewType());
					}
				});
	}

	@Override
	public void print() {
		try {
			TablePrinter.printTable(addressTable);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}
	
	@Override
	public ViewType getViewType() {
		return ViewType.ADDRESS;
	}

	@Override
	public void update(ChangeEvent event) {
		refresh();
	}


}
