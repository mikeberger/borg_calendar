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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TablePrinter;
import net.sf.borg.ui.util.TableSorter;

import com.toedter.calendar.JDateChooser;

/**
 * UI for searching appointments.
 */
public class SearchView extends DockableView implements Module {

	
	/** The matching appointments. */
	private Vector<Appointment> matchingAppointments = null;

	/** The case sensitive check box. */
	private JCheckBox caseSensitiveCheckBox = null;

	/** The category combo box. */
	private JComboBox categoryComboBox = null;

	/** The end date chooser. */
	private JDateChooser endDateChooser = null;

	/** The end date label. */
	private JLabel endDateLabel = null;

	/** The holiday check box. */
	private JCheckBox holidayCheckBox = null;

	/** The results table. */
	private StripedTable resultsTable = null;

	/** The link check box. */
	private JCheckBox linkCheckBox = null;

	/** The repeat check box. */
	private JCheckBox repeatCheckBox = null;

	/** The search text. */
	private JTextField searchText = null;

	/** The start date chooser. */
	private JDateChooser startDateChooser = null;

	/** The todo check box. */
	private JCheckBox todoCheckBox = null;

	/** The vacation check box. */
	private JCheckBox vacationCheckBox = null;

	/**
	 * constructor.
	 */
	public SearchView() {
		super();

		// init the UI components
		initComponents();

		// listen for appointment model changes
		addModel(AppointmentModel.getReference());

		// show the search results as a 2 column sortable table
		// showing the appt date and text
		resultsTable.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Date"),
				Resource.getResourceString("Item"), "key" }, new Class[] {
				Date.class, java.lang.String.class, Integer.class }));

		// hide column with the key
		TableColumnModel colModel = resultsTable.getColumnModel();
		TableColumn col = colModel.getColumn(2);
		resultsTable.removeColumn(col);

		// populate the category combo box
		categoryComboBox.addItem("");
		try {
			Collection<String> cats = CategoryModel.getReference()
					.getCategories();
			Iterator<String> it = cats.iterator();
			while (it.hasNext()) {
				categoryComboBox.addItem(it.next());
			}
			categoryComboBox.setSelectedIndex(0);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * create the change category button.
	 * 
	 * @return the change category button
	 */
	private JButton createChangeCategoryButton() {
		JButton changeCategoryButton = new JButton();
		ResourceHelper.setText(changeCategoryButton, "chg_cat");
		changeCategoryButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Preferences16.gif"))); // Generated
		changeCategoryButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						
						// change the category of all selected rows
						int rows[] = resultsTable.getSelectedRows();
						if (rows.length == 0) {
							return;
						}

						try {

							Collection<String> allcats = CategoryModel
									.getReference().getCategories();
							Object[] cats = allcats.toArray();

							// ask the user to choose the new category
							Object o = JOptionPane.showInputDialog(null,
									Resource.getResourceString("cat_choose"),
									"", JOptionPane.QUESTION_MESSAGE, null,
									cats, cats[0]);
							if (o == null)
								return;

							TableSorter tm = (TableSorter) resultsTable
									.getModel();

							// get a list of selected appointment keys
							ArrayList<Integer> apptKeys = new ArrayList<Integer>();
							for (int i = 0; i < rows.length; i++) {
								Integer key = (Integer) tm.getValueAt(rows[i],
										2);
								apptKeys.add(key);
							}

							// change the categories
							for( Integer key : apptKeys) {
								Appointment ap = AppointmentModel
										.getReference().getAppt(key.intValue());
								ap.setCategory((String) o);
								AppointmentModel.getReference().saveAppt(ap);

							}
						} catch (Exception ex) {
							Errmsg.errmsg(ex);
							return;
						}

						refresh(); // refresh results
					}
				});
		
		return changeCategoryButton;
	}

	/**
	 * create the delete button.
	 * 
	 * @return the delete button
	 */
	private JButton createDeleteButton() {
		JButton deleteButton = new JButton();
		ResourceHelper.setText(deleteButton, "delete_selected");
		deleteButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Stop16.gif")));
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				
				// delete all selected rows
				
				int rows[] = resultsTable.getSelectedRows();
				if (rows.length == 0) {
					return;
				}

				// confirm delete
				int ret = JOptionPane.showConfirmDialog(null, Resource
						.getResourceString("delete_selected")
						+ "?", "", JOptionPane.YES_NO_OPTION);
				if (ret != JOptionPane.YES_OPTION) {
					return;
				}
				
				// get selected appt keys
				TableSorter tm = (TableSorter) resultsTable.getModel();
				ArrayList<Integer> apptKeys = new ArrayList<Integer>();
				for (int i = 0; i < rows.length; i++) {
					Integer key = (Integer) tm.getValueAt(rows[i], 2);
					apptKeys.add(key);
				}

				// delete the appts
				for(Integer key : apptKeys) {
					AppointmentModel.getReference().delAppt(key.intValue());
				}

				refresh(); // reload results
			}
		});
		
		return deleteButton;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.ui.DockableView#getFrameSizePref()
	 */
	@Override
	public PrefName getFrameSizePref() {
		return PrefName.SRCHVIEWSIZE;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.ui.DockableView#getFrameTitle()
	 */
	@Override
	public String getFrameTitle() {
		return Resource.getResourceString("srch");
	}

	/**
	 * create the results panel.
	 * 
	 * @return the results panel
	 */
	private JPanel createResultsPanel() {

		JPanel resultsPanel = new JPanel();

		resultsPanel.setLayout(new GridBagLayout());
		resultsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, Resource.getResourceString("Search_Results"),
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));

		resultsTable = new StripedTable();
		resultsTable
				.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() < 2)
					return;
				showSelectedAppointment();
			}
		});

		JScrollPane tableScroll = new JScrollPane();
		tableScroll.setViewportView(resultsTable);
		//tableScroll.setPreferredSize(new java.awt.Dimension(100, 100));

		GridBagConstraints gridBagConstraints2 = GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.BOTH, 1.0, 1.0);
		gridBagConstraints2.gridwidth = 2;
		resultsPanel.add(tableScroll, gridBagConstraints2);
		resultsPanel.add(createDeleteButton(), GridBagConstraintsFactory
				.create(0, 1, GridBagConstraints.NONE, 1.0, 0.0));
		resultsPanel.add(createChangeCategoryButton(),
				GridBagConstraintsFactory.create(1, 1, GridBagConstraints.NONE,
						1.0, 0.0));

		return resultsPanel;
	}

	
	/**
	 * Creates the search criteria panel.
	 * 
	 * @return the search criteria panel.
	 */
	private JPanel createSearchCriteriaPanel() {
		
		JPanel searchCriteriaPanel = new JPanel();
		searchCriteriaPanel.setLayout(new GridBagLayout());
		searchCriteriaPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder(null, "",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
						null));
			
		JLabel searchStringLabel = new JLabel();
		ResourceHelper.setText(searchStringLabel, "SearchString");
		searchCriteriaPanel.add(searchStringLabel, GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.HORIZONTAL));
		
		JLabel categoryLabel = new JLabel();
		ResourceHelper.setText(categoryLabel, "Category");
		searchCriteriaPanel.add(categoryLabel, GridBagConstraintsFactory
				.create(0, 1, GridBagConstraints.HORIZONTAL));
		
		JLabel startDateLabel = new JLabel();
		ResourceHelper.setText(startDateLabel, "StartDate");
		searchCriteriaPanel.add(startDateLabel, GridBagConstraintsFactory
				.create(0, 2, GridBagConstraints.HORIZONTAL));
		
		endDateLabel = new JLabel();
		ResourceHelper.setText(endDateLabel, "EndDate");
		searchCriteriaPanel.add(endDateLabel, GridBagConstraintsFactory
				.create(0, 3, GridBagConstraints.HORIZONTAL));
		
		GridBagConstraints gridBagConstraints25 = GridBagConstraintsFactory
		.create(0, 4, GridBagConstraints.BOTH, 0.0, 1.0);
		gridBagConstraints25.gridwidth = 4;
		searchCriteriaPanel.add(createCheckBoxPanel(), gridBagConstraints25);
		
		searchText = new JTextField();
		searchCriteriaPanel.add(searchText, GridBagConstraintsFactory
				.create(1, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0));

		categoryComboBox = new JComboBox();
		searchCriteriaPanel.add(categoryComboBox, GridBagConstraintsFactory
				.create(1, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0));
		
		startDateChooser = new JDateChooser();
		GridBagConstraints gbc1 = GridBagConstraintsFactory
		.create(1, 2, GridBagConstraints.NONE, 1.0, 0.0);
		gbc1.anchor = GridBagConstraints.WEST;
		searchCriteriaPanel.add(startDateChooser, gbc1);

		endDateChooser = new JDateChooser();
		GridBagConstraints gbc2 = GridBagConstraintsFactory
		.create(1, 3, GridBagConstraints.NONE, 1.0, 0.0);
		gbc2.anchor = GridBagConstraints.WEST;
		searchCriteriaPanel.add(endDateChooser, gbc2);
	
		caseSensitiveCheckBox = new JCheckBox();
		caseSensitiveCheckBox.setText(Resource
				.getResourceString("case_sensitive"));
		searchCriteriaPanel.add(caseSensitiveCheckBox, GridBagConstraintsFactory
				.create(2, 0, GridBagConstraints.BOTH));
		return searchCriteriaPanel;
	}

	/**
	 * creates the check box panel
	 * 
	 * @return the check box panel
	 */
	private JPanel createCheckBoxPanel() {
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridBagLayout());
		
		todoCheckBox = new JCheckBox();
		ResourceHelper.setText(todoCheckBox, "To_Do");
		checkBoxPanel.add(todoCheckBox, GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.BOTH, 1.0, 1.0));

		repeatCheckBox = new JCheckBox();
		ResourceHelper.setText(repeatCheckBox, "repeating");
		checkBoxPanel.add(repeatCheckBox, GridBagConstraintsFactory
				.create(1, 0, GridBagConstraints.BOTH, 1.0, 1.0));

		vacationCheckBox = new JCheckBox();
		ResourceHelper.setText(vacationCheckBox, "Vacation");
		checkBoxPanel.add(vacationCheckBox, GridBagConstraintsFactory
				.create(0, 1, GridBagConstraints.BOTH, 1.0, 1.0));

		holidayCheckBox = new JCheckBox();
		ResourceHelper.setText(holidayCheckBox, "Holiday");
		checkBoxPanel.add(holidayCheckBox, GridBagConstraintsFactory
				.create(1, 1, GridBagConstraints.BOTH, 1.0, 1.0));

		linkCheckBox = new JCheckBox();
		linkCheckBox.setText(Resource.getResourceString("haslinks"));
		checkBoxPanel.add(linkCheckBox, GridBagConstraintsFactory
				.create(2, 0, GridBagConstraints.BOTH, 1.0, 1.0));
		return checkBoxPanel;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.ui.DockableView#getMenuForFrame()
	 */
	@Override
	public JMenuBar getMenuForFrame() {
		return null;
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {


		setLayout(new GridBagLayout());
		
		add(createSearchCriteriaPanel(), GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.BOTH, 0.0, 0.0));

		JPanel searchButtonPanel = new JPanel();
		GridLayout gridLayout18 = new GridLayout();
		searchButtonPanel.setLayout(gridLayout18);
		gridLayout18.setRows(1);
		gridLayout18.setHgap(5);

		JButton searchButton = new JButton();
		ResourceHelper.setText(searchButton, "srch");
		searchButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Find16.gif")));
		searchButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				refresh();
			}
		});
		searchButtonPanel.add(searchButton, null);

		add(searchButtonPanel, GridBagConstraintsFactory
				.create(0, 2, GridBagConstraints.NONE));
		
		add(createResultsPanel(), GridBagConstraintsFactory
				.create(0, 3, GridBagConstraints.BOTH, 1.0, 1.0));

	}

	/**
	 * Show the selected appointment in the appointment editor
	 */
	private void showSelectedAppointment() {

		// get the selected row
		int rows[] = resultsTable.getSelectedRows();
		if (rows.length != 1)
			return;
		int row = rows[0];
		TableSorter tm = (TableSorter) resultsTable.getModel();
		
		// get the date of the selected row
		Date d = (Date) tm.getValueAt(row, 0);
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(d);

		// bring up an appt list window for the selected date
		AppointmentListView ag = new AppointmentListView(
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
						.get(Calendar.DATE));

		MultiView.getMainView().addView(ag);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.View#refresh()
	 */
	@Override
	public void refresh() {
		
		// call the data model to do a search by appt text
		matchingAppointments = AppointmentModel.getReference().get_srch(searchText.getText(),
				caseSensitiveCheckBox.isSelected());

		// empty the table
		TableSorter tm = (TableSorter) resultsTable.getModel();
		tm.addMouseListenerToHeaderInTable(resultsTable);
		tm.setRowCount(0);

		// set up the filter criteria
		String selcat = (String) categoryComboBox.getSelectedItem();

		long starttime = 0;
		Calendar cal = startDateChooser.getCalendar();
		if (cal != null) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.add(Calendar.SECOND, -1);
			starttime = cal.getTime().getTime();
		}

		long endtime = 0;
		cal = endDateChooser.getCalendar();
		if (cal != null) {
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			endtime = cal.getTime().getTime();
		}

		// load the search results into the table, filtering the list by the
		// search criteria
		for (int i = 0; i < matchingAppointments.size(); i++) {
			Object[] ro = new Object[3];

			Appointment appt = matchingAppointments.elementAt(i);

			// filter by repeat
			if (repeatCheckBox.isSelected() && !appt.getRepeatFlag())
				continue;

			// filter todos
			if (todoCheckBox.isSelected() && !appt.getTodo())
				continue;

			// filter by vacation
			Integer ii = appt.getVacation();
			if (vacationCheckBox.isSelected()
					&& (ii == null || ii.intValue() != 1))
				continue;

			// filter by holiday
			ii = appt.getHoliday();
			if (holidayCheckBox.isSelected()
					&& (ii == null || ii.intValue() != 1))
				continue;

			// filter by category
			if (selcat.equals(CategoryModel.UNCATEGORIZED)
					&& appt.getCategory() != null
					&& !appt.getCategory().equals(CategoryModel.UNCATEGORIZED))
				continue;
			else if (!selcat.equals("")
					&& !selcat.equals(CategoryModel.UNCATEGORIZED)
					&& !selcat.equals(appt.getCategory()))
				continue;

			// filter by start date
			if (starttime != 0) {
				if (appt.getDate().getTime() < starttime)
					continue;
			}

			// filter by end date
			if (endtime != 0) {
				if (appt.getDate().getTime() > endtime)
					continue;
			}

			// filter by links
			if (linkCheckBox.isSelected()) {
				LinkModel lm = LinkModel.getReference();
				try {
					Collection<Link> lnks = lm.getLinks(appt);
					if (lnks.isEmpty())
						continue;
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}

			// load the appt into the table
			try {
				// get the date and text for the table
				ro[0] = appt.getDate();
				ro[1] = appt.getText();
				ro[2] = new Integer(appt.getKey());
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}
			tm.addRow(ro);
			tm.tableChanged(new TableModelEvent(tm));
		}

		// sort the table by date
		tm.sortByColumn(0);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public String getModuleName() {
		return Resource.getResourceString("srch");
	}

	@Override
	public void initialize(MultiView parent) {
		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
		"/resource/Find16.gif")), getModuleName(), new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				par.setView(ViewType.SEARCH);
			}
		});
	}

	@Override
	public void print() {
		try {
			TablePrinter.printTable(resultsTable);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}	
	}
	
	@Override
	public ViewType getViewType() {
		return ViewType.SEARCH;
	}
	
} 
