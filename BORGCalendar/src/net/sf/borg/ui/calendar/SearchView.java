/*
 * This file is part of BORG. BORG is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. BORG is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with BORG; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA Copyright 2005 by Mike Berger
 */
package net.sf.borg.ui.calendar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Link;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TableSorter;

import com.toedter.calendar.JDateChooser;

public class SearchView extends DockableView {

	private Vector<Appointment> appts_ = null;

	private JCheckBox caseBox = null;

	private JComboBox catbox = null;

	private JButton catbut = null;

	private JButton deleteButton = null;

	private JDateChooser endDateChooser = null;

	private JLabel endDateLabel = null;

	private JCheckBox holidaycb = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel3 = null;

	private JPanel jPanel = null;

	private JPanel jPanel1 = null;

	private JPanel jPanel2 = null;

	private JPanel jPanel3 = null;

	private JScrollPane jScrollPane = null;

	private StripedTable jTable1 = null;

	private JCheckBox repeatcb = null;

	private JButton searchButton = null;

	private JTextField searchText = null;

	private JDateChooser startDateChooser = null;

	private JLabel startDateLabel = null;

	private JCheckBox todocb = null;

	private JCheckBox vacationcb = null;

	private JCheckBox linkcb = null;

	/**
	 * This is the default constructor
	 */
	public SearchView() {
		super();
		initComponents();
		addModel(AppointmentModel.getReference());

		// show the search results as a 2 column sortable table
		// showing the appt date and text
		jTable1.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Date"),
				Resource.getResourceString("Item"), "key" }, new Class[] {
				Date.class, java.lang.String.class, Integer.class }));

		// hide column with the key
		TableColumnModel colModel = jTable1.getColumnModel();
		TableColumn col = colModel.getColumn(2);
		jTable1.removeColumn(col);

		// do the search
		// load();

		catbox.addItem("");

		try {
			Collection<String> cats = CategoryModel.getReference()
					.getCategories();
			Iterator<String> it = cats.iterator();
			while (it.hasNext()) {
				catbox.addItem(it.next());
			}
			catbox.setSelectedIndex(0);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	public PrefName getFrameSizePref() {
		return PrefName.SRCHVIEWSIZE;
	}

	public String getFrameTitle() {
		return Resource.getResourceString("srch");
	}

	public JMenuBar getMenuForFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.View#refresh()
	 */
	public void refresh() {
		load();
	}

	/**
	 * This method initializes caseBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getCaseBox() {
		if (caseBox == null) {
			caseBox = new JCheckBox();
			caseBox.setText(Resource.getResourceString("case_sensitive"));
		}
		return caseBox;
	}

	/**
	 * This method initializes catbox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getCatbox() {
		if (catbox == null) {
			catbox = new JComboBox();
		}
		return catbox;
	}

	/**
	 * This method initializes catbut
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getCatbut() {
		if (catbut == null) {
			catbut = new JButton();
			ResourceHelper.setText(catbut, "chg_cat");
			catbut.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Preferences16.gif"))); // Generated
			catbut.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int rows[] = jTable1.getSelectedRows();
					if (rows.length == 0) {
						return;
					}

					try {

						Collection<String> allcats = CategoryModel
								.getReference().getCategories();
						Object[] cats = allcats.toArray();

						Object o = JOptionPane.showInputDialog(null, Resource
								.getResourceString("cat_choose"), "",
								JOptionPane.QUESTION_MESSAGE, null, cats,
								cats[0]);
						if (o == null)
							return;

						TableSorter tm = (TableSorter) jTable1.getModel();

						ArrayList<Integer> appts = new ArrayList<Integer>();
						for (int i = 0; i < rows.length; i++) {
							Integer key = (Integer) tm.getValueAt(rows[i], 2);
							appts.add(key);
						}

						Iterator<Integer> it = appts.iterator();
						while (it.hasNext()) {
							Integer key = it.next();
							Appointment ap = AppointmentModel.getReference()
									.getAppt(key.intValue());
							ap.setCategory((String) o);
							AppointmentModel.getReference().saveAppt(ap);

						}
					} catch (Exception ex) {
						Errmsg.errmsg(ex);
						return;
					}

					load(); // force update
				}
			});
		}
		return catbut;
	}

	/**
	 * This method initializes deleteButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton();
			ResourceHelper.setText(deleteButton, "delete_selected");
			deleteButton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Stop16.gif")));
			deleteButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int rows[] = jTable1.getSelectedRows();
					if (rows.length == 0) {
						return;
					}

					// put out question
					int ret = JOptionPane.showConfirmDialog(null, Resource
							.getResourceString("delete_selected")
							+ "?", "", JOptionPane.YES_NO_OPTION);
					if (ret != JOptionPane.YES_OPTION) {
						return;
					}
					TableSorter tm = (TableSorter) jTable1.getModel();

					ArrayList<Integer> appts = new ArrayList<Integer>();
					for (int i = 0; i < rows.length; i++) {
						Integer key = (Integer) tm.getValueAt(rows[i], 2);
						appts.add(key);
					}

					Iterator<Integer> it = appts.iterator();
					while (it.hasNext()) {
						Integer key = it.next();
						// System.out.println("delete appt " + key.intValue());
						AppointmentModel.getReference().delAppt(key.intValue());

					}

					load(); // force update
				}
			});
		}
		return deleteButton;
	}

	/**
	 * This method initializes endDatecb
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JLabel getEndDatecb() {
		if (endDateLabel == null) {
			endDateLabel = new JLabel();
			ResourceHelper.setText(endDateLabel, "EndDate");
		}
		return endDateLabel;
	}

	/**
	 * This method initializes endDateChooser
	 * 
	 * @return de.wannawork.jcalendar.JDateChooser
	 */
	private JDateChooser getEndDateChooser() {
		if (endDateChooser == null) {
			endDateChooser = new JDateChooser();
		}
		return endDateChooser;
	}

	/**
	 * This method initializes holidaycb
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getHolidaycb() {
		if (holidaycb == null) {
			holidaycb = new JCheckBox();
			ResourceHelper.setText(holidaycb, "Holiday");
			holidaycb.setPreferredSize(new java.awt.Dimension(100, 10));
		}
		return holidaycb;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1; // Generated
			gridBagConstraints.weightx = 1.0D; // Generated
			gridBagConstraints.gridy = 1; // Generated
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
					Resource.getResourceString("Search_Results"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
					null));
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridwidth = 2; // Generated
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints41.gridx = 0;
			gridBagConstraints41.weightx = 1.0D; // Generated
			gridBagConstraints41.gridy = 1;
			jPanel.add(getJScrollPane(), gridBagConstraints2); // Generated
			jPanel.add(getDeleteButton(), gridBagConstraints41); // Generated
			jPanel.add(getCatbut(), gridBagConstraints); // Generated
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 2;
			gridBagConstraints9.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints9.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints9.gridy = 0;
			jLabel3 = new JLabel();
			jLabel2 = new JLabel();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.gridy = 2;
			gridBagConstraints4.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints4.weightx = 1.0D;
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints4.fill = java.awt.GridBagConstraints.NONE;
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 3;
			gridBagConstraints7.weightx = 1.0D;
			gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints7.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints7.fill = java.awt.GridBagConstraints.NONE;
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.gridy = 1;
			gridBagConstraints13.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
			ResourceHelper.setText(jLabel2, "Category");
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.gridy = 1;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints14.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridy = 0;
			gridBagConstraints15.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
			ResourceHelper.setText(jLabel3, "SearchString");
			gridBagConstraints16.gridx = 1;
			gridBagConstraints16.gridy = 0;
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints19.gridx = 0;
			gridBagConstraints19.gridy = 2;
			gridBagConstraints19.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints19.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints20.gridx = 0;
			gridBagConstraints20.gridy = 3;
			gridBagConstraints20.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints20.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.gridy = 4;
			gridBagConstraints25.gridwidth = 4;
			gridBagConstraints25.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints25.insets = new java.awt.Insets(0, 0, 0, 0);
			gridBagConstraints25.weighty = 0.0D;
			gridBagConstraints25.weightx = 0.0D;
			jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, "",
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
					null));
			jPanel1.add(getStartDateChooser(), gridBagConstraints4);
			jPanel1.add(getEndDateChooser(), gridBagConstraints7);
			jPanel1.add(getStartDatecb(), gridBagConstraints19);
			jPanel1.add(getEndDatecb(), gridBagConstraints20);
			jPanel1.add(getJPanel3(), gridBagConstraints25);
			jPanel1.add(jLabel2, gridBagConstraints13);
			jPanel1.add(getCatbox(), gridBagConstraints14);
			jPanel1.add(jLabel3, gridBagConstraints15);
			jPanel1.add(getSearchText(), gridBagConstraints16);
			jPanel1.add(getCaseBox(), gridBagConstraints9);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridLayout gridLayout18 = new GridLayout();
			jPanel2 = new JPanel();
			jPanel2.setLayout(gridLayout18);
			gridLayout18.setRows(1);
			gridLayout18.setHgap(5);
			jPanel2.add(getSearchButton(), null);

		}
		return jPanel2;
	}

	/**
	 * This method initializes jPanel3
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			jPanel3 = new JPanel();
			jPanel3.setLayout(new GridBagLayout());
			jPanel3.setPreferredSize(new java.awt.Dimension(100, 50));
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.ipadx = 0;
			gridBagConstraints5.ipady = 0;
			gridBagConstraints5.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints5.weightx = 1.0D;
			gridBagConstraints5.weighty = 1.0D;
			gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.ipadx = 0;
			gridBagConstraints6.ipady = 0;
			gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints6.weightx = 1.0D;
			gridBagConstraints6.weighty = 1.0D;
			gridBagConstraints6.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints71.gridx = 0;
			gridBagConstraints71.gridy = 1;
			gridBagConstraints71.ipadx = 0;
			gridBagConstraints71.ipady = 0;
			gridBagConstraints71.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints71.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints71.weightx = 1.0D;
			gridBagConstraints71.weighty = 1.0D;
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.gridy = 1;
			gridBagConstraints8.ipadx = 0;
			gridBagConstraints8.ipady = 0;
			gridBagConstraints8.weightx = 1.0D;
			gridBagConstraints8.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints8.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints8.weighty = 1.0D;
			jPanel3.add(getTodocb(), gridBagConstraints5);
			jPanel3.add(getRepeatcb(), gridBagConstraints6);
			jPanel3.add(getVacationcb(), gridBagConstraints71);
			jPanel3.add(getHolidaycb(), gridBagConstraints8);

			GridBagConstraints linkgbc = new GridBagConstraints();
			linkgbc.gridx = 2;
			linkgbc.gridy = 0;
			linkgbc.ipadx = 0;
			linkgbc.ipady = 0;
			linkgbc.weightx = 1.0D;
			linkgbc.fill = java.awt.GridBagConstraints.BOTH;
			linkgbc.insets = new java.awt.Insets(4, 4, 4, 4);
			linkgbc.weighty = 1.0D;
			linkcb = new JCheckBox();
			linkcb.setText(Resource.getResourceString("haslinks"));
			jPanel3.add(linkcb, linkgbc);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable1());
			jScrollPane.setPreferredSize(new java.awt.Dimension(100, 100));
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable1
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTable1() {
		if (jTable1 == null) {
			jTable1 = new StripedTable();
			// jTable1.setPreferredSize(new java.awt.Dimension(90,90));
			jTable1
					.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					jTable1MouseClicked(e);
				}
			});
		}
		return jTable1;
	}

	/**
	 * This method initializes repeatcb
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getRepeatcb() {
		if (repeatcb == null) {
			repeatcb = new JCheckBox();
			ResourceHelper.setText(repeatcb, "repeating");
			repeatcb.setPreferredSize(new java.awt.Dimension(100, 10));
		}
		return repeatcb;
	}

	/**
	 * This method initializes searchButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getSearchButton() {
		if (searchButton == null) {
			searchButton = new JButton();
			ResourceHelper.setText(searchButton, "srch");
			searchButton.setIcon(new ImageIcon(getClass().getResource(
					"/resource/Find16.gif")));
			searchButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					load();
				}
			});
		}
		return searchButton;
	}

	/**
	 * This method initializes searchText
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getSearchText() {
		if (searchText == null) {
			searchText = new JTextField();
		}
		return searchText;
	}

	/**
	 * This method initializes startDatecb
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JLabel getStartDatecb() {
		if (startDateLabel == null) {
			startDateLabel = new JLabel();
			ResourceHelper.setText(startDateLabel, "StartDate");
		}
		return startDateLabel;
	}

	/**
	 * This method initializes startDateChooser
	 * 
	 * @return de.wannawork.jcalendar.JDateChooser
	 */
	private JDateChooser getStartDateChooser() {
		if (startDateChooser == null) {
			startDateChooser = new JDateChooser();
		}
		return startDateChooser;
	}

	/**
	 * This method initializes todocb
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getTodocb() {
		if (todocb == null) {
			todocb = new JCheckBox();
			ResourceHelper.setText(todocb, "To_Do");
			todocb.setPreferredSize(new java.awt.Dimension(10, 10));
		}
		return todocb;
	}

	/**
	 * This method initializes vacationcb
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getVacationcb() {
		if (vacationcb == null) {
			vacationcb = new JCheckBox();
			ResourceHelper.setText(vacationcb, "Vacation");
			vacationcb.setPreferredSize(new java.awt.Dimension(0, 0));
		}
		return vacationcb;
	}

	private void initComponents() {

		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

		setLayout(new GridBagLayout());
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.gridy = 0;
		gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints3.weighty = 0.0D;
		gridBagConstraints3.ipadx = 1;
		gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints3.weightx = 0.0D;
		gridBagConstraints3.insets = new java.awt.Insets(5, 5, 5, 5);
		gridBagConstraints17.gridx = 0;
		gridBagConstraints17.gridy = 2;
		gridBagConstraints17.fill = java.awt.GridBagConstraints.NONE;
		gridBagConstraints17.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints17.insets = new java.awt.Insets(5, 5, 5, 5);
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 3;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.weightx = 1.0D;
		gridBagConstraints1.weighty = 1.0D;
		gridBagConstraints1.insets = new java.awt.Insets(5, 5, 5, 5);
		add(getJPanel1(), gridBagConstraints3);
		add(getJPanel2(), gridBagConstraints17);
		add(getJPanel(), gridBagConstraints1);

	}

	private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {
		// ask controller to bring up appt editor on double click
		if (evt.getClickCount() < 2)
			return;

		// get task number from column 0 of selected row
		int rows[] = jTable1.getSelectedRows();
		if (rows.length != 1)
			return;
		int row = rows[0];
		TableSorter tm = (TableSorter) jTable1.getModel();
		Date d = (Date) tm.getValueAt(row, 0);

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(d);

		// bring up an appt editor window
		AppointmentListView ag = new AppointmentListView(
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
						.get(Calendar.DATE));

		MultiView.getMainView().addView(ag);

		// MultiView cv = MultiView.getMainView();
		// if (cv != null)
		// cv.goTo(cal);

	}

	private void load() {

		// call the data model to do the actual search
		AppointmentModel cal_ = AppointmentModel.getReference();
		appts_ = cal_.get_srch(searchText.getText(), caseBox.isSelected());

		TableSorter tm = (TableSorter) jTable1.getModel();
		tm.addMouseListenerToHeaderInTable(jTable1);
		tm.setRowCount(0);

		String selcat = (String) catbox.getSelectedItem();

		long starttime = 0;
		Calendar cal = startDateChooser.getCalendar();
		if (cal != null) {

			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.add(Calendar.SECOND, -1);
			starttime = cal.getTime().getTime();
			// //System.out.println( "start: " + cal.getTime());

		}

		long endtime = 0;
		cal = endDateChooser.getCalendar();
		if (cal != null) {

			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			endtime = cal.getTime().getTime();
			// System.out.println( "end: " + cal.getTime());
		}

		// load the search results into the table
		for (int i = 0; i < appts_.size(); i++) {
			Object[] ro = new Object[3];

			// get a single appt row
			Appointment r = appts_.elementAt(i);

			if (repeatcb.isSelected() && !r.getRepeatFlag())
				continue;

			if (todocb.isSelected() && !r.getTodo())
				continue;

			Integer ii = r.getVacation();
			if (vacationcb.isSelected() && (ii == null || ii.intValue() != 1))
				continue;

			ii = r.getHoliday();
			if (holidaycb.isSelected() && (ii == null || ii.intValue() != 1))
				continue;

			if (selcat.equals(CategoryModel.UNCATEGORIZED)
					&& r.getCategory() != null
					&& !r.getCategory().equals(CategoryModel.UNCATEGORIZED))
				continue;

			if (!selcat.equals("")
					&& !selcat.equals(CategoryModel.UNCATEGORIZED)
					&& !selcat.equals(r.getCategory()))
				continue;

			// normal date to midnight???
			if (starttime != 0) {
				if (r.getDate().getTime() < starttime)
					continue;
			}

			if (endtime != 0) {
				if (r.getDate().getTime() > endtime)
					continue;
			}
			// System.out.println( r.getText());

			if (linkcb.isSelected()) {
				LinkModel lm = LinkModel.getReference();

				try {
					Collection<Link> lnks = lm.getLinks(r);
					if (lnks.isEmpty())
						continue;
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}

			}

			try {
				// get the date and text for the table
				ro[0] = r.getDate();
				ro[1] = r.getText();
				ro[2] = new Integer(r.getKey());
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}

			// load the appt into the table
			tm.addRow(ro);
			tm.tableChanged(new TableModelEvent(tm));
		}

		// sort the table by date
		tm.sortByColumn(0);

	}
} // @jve:decl-index=0:visual-constraint="64,38"
