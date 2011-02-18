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
package net.sf.borg.ui;

import java.awt.Component;
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
import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.SearchCriteria;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.address.AddressView;
import net.sf.borg.ui.calendar.AppointmentListView;
import net.sf.borg.ui.memo.MemoPanel;
import net.sf.borg.ui.task.ProjectView;
import net.sf.borg.ui.task.TaskView;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TablePrinter;
import net.sf.borg.ui.util.TableSorter;

import com.toedter.calendar.JDateChooser;

/**
 * UI for searching records.
 */
public class SearchView extends DockableView implements Module {

	private static final long serialVersionUID = 1L;

	private JCheckBox addressCheckBox = null;

	private JCheckBox apptCheckBox = null;

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

	/** The link check box. */
	private JCheckBox linkCheckBox = null;
	
	private JCheckBox memoCheckBox = null;

	private JCheckBox projectCheckBox = null;

	/** The repeat check box. */
	private JCheckBox repeatCheckBox = null;

	/** The results table. */
	private StripedTable resultsTable = null;

	/** The search text. */
	private JTextField searchText = null;

	/** The start date chooser. */
	private JDateChooser startDateChooser = null;

	private JCheckBox taskCheckBox = null;

	/** The todo check box. */
	private JCheckBox todoCheckBox = null;
	
	// whole word search option
	private JCheckBox wholeWordBox = null;

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
		addModel(AddressModel.getReference());
		addModel(TaskModel.getReference());
		addModel(MemoModel.getReference());

		// show the search results as a 2 column sortable table
		// showing the appt date and text
		resultsTable.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Item"),
				Resource.getResourceString("Type"),
				Resource.getResourceString("Date"), 
				"key", "class" },
				new Class[] { String.class, String.class, Date.class, 
						Integer.class, Class.class }));

		// hide columns with the key, class
		TableColumnModel colModel = resultsTable.getColumnModel();
		TableColumn col = colModel.getColumn(3);
		resultsTable.removeColumn(col);
		col = colModel.getColumn(3);
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
					@Override
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
							
							String cat = (String) o;
							if( cat.isEmpty() || CategoryModel.UNCATEGORIZED.equals(cat))
								cat = null;

							TableSorter tm = (TableSorter) resultsTable
									.getModel();

							// get a list of selected items
							ArrayList<KeyedEntity<?>> entities = new ArrayList<KeyedEntity<?>>();
							for (int i = 0; i < rows.length; i++) {
								Integer key = (Integer) tm.getValueAt(rows[i],
										3);
								Class<?> cl = (Class<?>) tm.getValueAt(rows[i], 4);
								try {
									KeyedEntity<?> ent = (KeyedEntity<?>) cl
											.newInstance();
									ent.setKey(key.intValue());
									entities.add(ent);
								} catch (Exception e1) {
									Errmsg.errmsg(e1);
								}

							}

							// change the categories
							for (KeyedEntity<?> ent : entities) {
								if (ent instanceof Appointment) {
									Appointment ap = AppointmentModel
											.getReference().getAppt(
													ent.getKey());
									ap.setCategory(cat);
									AppointmentModel.getReference()
											.saveAppt(ap);
								}
								if (ent instanceof Project) {
									Project ap = TaskModel.getReference()
											.getProject(ent.getKey());
									ap.setCategory(cat);
									TaskModel.getReference().saveProject(ap);
								}
								if (ent instanceof Task) {
									Task ap = TaskModel.getReference().getTask(
											ent.getKey());
									ap.setCategory(cat);
									TaskModel.getReference().savetask(ap);
								}

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
	 * creates the check box panel
	 * 
	 * @return the check box panel
	 */
	private JPanel createCheckBoxPanel() {
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridBagLayout());

		apptCheckBox = new JCheckBox();
		apptCheckBox.setSelected(true);
		ResourceHelper.setText(apptCheckBox, "appointment");
		checkBoxPanel.add(apptCheckBox, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));
		taskCheckBox = new JCheckBox();
		taskCheckBox.setSelected(true);
		ResourceHelper.setText(taskCheckBox, "task");
		checkBoxPanel.add(taskCheckBox, GridBagConstraintsFactory.create(2, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));
		addressCheckBox = new JCheckBox();
		addressCheckBox.setSelected(true);
		ResourceHelper.setText(addressCheckBox, "Address");
		checkBoxPanel.add(addressCheckBox, GridBagConstraintsFactory.create(1,
				0, GridBagConstraints.BOTH, 1.0, 1.0));
		projectCheckBox = new JCheckBox();
		projectCheckBox.setSelected(true);
		ResourceHelper.setText(projectCheckBox, "project");
		checkBoxPanel.add(projectCheckBox, GridBagConstraintsFactory.create(3,
				0, GridBagConstraints.BOTH, 1.0, 1.0));
		memoCheckBox = new JCheckBox();
		memoCheckBox.setSelected(true);
		ResourceHelper.setText(memoCheckBox, "memo");
		checkBoxPanel.add(memoCheckBox, GridBagConstraintsFactory.create(4,
				0, GridBagConstraints.BOTH, 1.0, 1.0));

		todoCheckBox = new JCheckBox();
		ResourceHelper.setText(todoCheckBox, "To_Do");
		checkBoxPanel.add(todoCheckBox, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 1.0, 1.0));

		repeatCheckBox = new JCheckBox();
		ResourceHelper.setText(repeatCheckBox, "repeating");
		checkBoxPanel.add(repeatCheckBox, GridBagConstraintsFactory.create(1,
				1, GridBagConstraints.BOTH, 1.0, 1.0));

		vacationCheckBox = new JCheckBox();
		ResourceHelper.setText(vacationCheckBox, "Vacation");
		checkBoxPanel.add(vacationCheckBox, GridBagConstraintsFactory.create(2,
				1, GridBagConstraints.BOTH, 1.0, 1.0));

		holidayCheckBox = new JCheckBox();
		ResourceHelper.setText(holidayCheckBox, "Holiday");
		checkBoxPanel.add(holidayCheckBox, GridBagConstraintsFactory.create(3,
				1, GridBagConstraints.BOTH, 1.0, 1.0));

		linkCheckBox = new JCheckBox();
		linkCheckBox.setText(Resource.getResourceString("haslinks"));
		checkBoxPanel.add(linkCheckBox, GridBagConstraintsFactory.create(4, 1,
				GridBagConstraints.BOTH, 1.0, 1.0));
		return checkBoxPanel;
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
			@Override
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

				// get selected items
				TableSorter tm = (TableSorter) resultsTable.getModel();
				ArrayList<KeyedEntity<?>> entities = new ArrayList<KeyedEntity<?>>();
				for (int i = 0; i < rows.length; i++) {
					Integer key = (Integer) tm.getValueAt(rows[i], 3);
					Class<?> cl = (Class<?>) tm.getValueAt(rows[i], 4);
					try {
						KeyedEntity<?> ent = (KeyedEntity<?>) cl.newInstance();
						ent.setKey(key.intValue());
						entities.add(ent);
					} catch (Exception e1) {
						Errmsg.errmsg(e1);
					}

				}

				// delete the items
				for (KeyedEntity<?> ent : entities) {
					if (ent instanceof Appointment)
						AppointmentModel.getReference().delAppt(ent.getKey());
					else if (ent instanceof Address)
						AddressModel.getReference().delete((Address) ent);
				}

				refresh(); // reload results
			}
		});

		return deleteButton;
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
				try {
					showSelectedItem();
				} catch (Exception e1) {
					Errmsg.errmsg(e1);
				}
			}
		});

		JScrollPane tableScroll = new JScrollPane();
		tableScroll.setViewportView(resultsTable);
		// tableScroll.setPreferredSize(new java.awt.Dimension(100, 100));

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
		searchCriteriaPanel.add(endDateLabel, GridBagConstraintsFactory.create(
				0, 3, GridBagConstraints.HORIZONTAL));

		GridBagConstraints gridBagConstraints25 = GridBagConstraintsFactory
				.create(0, 4, GridBagConstraints.BOTH, 0.0, 1.0);
		gridBagConstraints25.gridwidth = 4;
		searchCriteriaPanel.add(createCheckBoxPanel(), gridBagConstraints25);

		searchText = new JTextField();
		searchCriteriaPanel.add(searchText, GridBagConstraintsFactory.create(1,
				0, GridBagConstraints.HORIZONTAL, 1.0, 0.0));

		categoryComboBox = new JComboBox();
		searchCriteriaPanel.add(categoryComboBox, GridBagConstraintsFactory
				.create(1, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0));

		startDateChooser = new JDateChooser();
		GridBagConstraints gbc1 = GridBagConstraintsFactory.create(1, 2,
				GridBagConstraints.NONE, 1.0, 0.0);
		gbc1.anchor = GridBagConstraints.WEST;
		searchCriteriaPanel.add(startDateChooser, gbc1);

		endDateChooser = new JDateChooser();
		GridBagConstraints gbc2 = GridBagConstraintsFactory.create(1, 3,
				GridBagConstraints.NONE, 1.0, 0.0);
		gbc2.anchor = GridBagConstraints.WEST;
		searchCriteriaPanel.add(endDateChooser, gbc2);

		caseSensitiveCheckBox = new JCheckBox();
		caseSensitiveCheckBox.setText(Resource
				.getResourceString("case_sensitive"));
		searchCriteriaPanel
				.add(caseSensitiveCheckBox, GridBagConstraintsFactory.create(2,
						0, GridBagConstraints.BOTH));
		
		wholeWordBox = new JCheckBox();
		wholeWordBox.setText(Resource
				.getResourceString("WholeWord"));
		searchCriteriaPanel
				.add(wholeWordBox, GridBagConstraintsFactory.create(2,
						1, GridBagConstraints.BOTH));
		return searchCriteriaPanel;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	/**
	 * fill in the search criteria from the UI settings
	 * 
	 * @return the criteria
	 */
	private SearchCriteria getCriteria() {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setSearchString(searchText.getText());
		criteria.setCaseSensitive(caseSensitiveCheckBox.isSelected());
		criteria.setCategory((String) categoryComboBox.getSelectedItem());
		criteria.setWholeWord(wholeWordBox.isSelected());
		Calendar cal = startDateChooser.getCalendar();
		if (cal != null) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.add(Calendar.SECOND, -1);
			criteria.setStartDate(cal.getTime());
		}
		cal = endDateChooser.getCalendar();
		if (cal != null) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.add(Calendar.SECOND, -1);
			criteria.setEndDate(cal.getTime());
		}
		criteria.setHoliday(holidayCheckBox.isSelected());
		criteria.setRepeating(repeatCheckBox.isSelected());
		criteria.setTodo(todoCheckBox.isSelected());
		criteria.setVacation(vacationCheckBox.isSelected());
		criteria.setHasLinks(linkCheckBox.isSelected());
		return criteria;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.DockableView#getFrameTitle()
	 */
	@Override
	public String getFrameTitle() {
		return Resource.getResourceString("srch");
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

	@Override
	public String getModuleName() {
		return Resource.getResourceString("srch");
	}

	@Override
	public ViewType getViewType() {
		return ViewType.SEARCH;
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {

		setLayout(new GridBagLayout());

		add(createSearchCriteriaPanel(), GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 0.0, 0.0));

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
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				refresh();
			}
		});
		searchButtonPanel.add(searchButton, null);

		add(searchButtonPanel, GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.NONE));

		add(createResultsPanel(), GridBagConstraintsFactory.create(0, 3,
				GridBagConstraints.BOTH, 1.0, 1.0));

	}

	@Override
	public void initialize(MultiView parent) {
		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
				"/resource/Find16.gif")), getModuleName(),
				new ActionListener() {
					@Override
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
	public void update(ChangeEvent event) {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.View#refresh()
	 */
	@Override
	public void refresh() {

		SearchCriteria criteria = getCriteria();

		// empty the table
		TableSorter tm = (TableSorter) resultsTable.getModel();
		tm.addMouseListenerToHeaderInTable(resultsTable);
		tm.setRowCount(0);

		// load the search results into the table, filtering the list by the
		// search criteria

		if (apptCheckBox.isSelected()) {
			Collection<Appointment> appointments = AppointmentModel
					.getReference().search(criteria);
			for (Appointment appt : appointments) {
				Object[] ro = new Object[5];

				try {
					ro[0] = appt.getText();
					ro[1] = Resource.getResourceString("appointment");
					ro[2] = appt.getDate();
					ro[3] = new Integer(appt.getKey());
					ro[4] = Appointment.class;
					tm.addRow(ro);
					tm.tableChanged(new TableModelEvent(tm));
				} catch (Exception e) {
					Errmsg.errmsg(e);
					return;
				}

			}
		}

		if (addressCheckBox.isSelected()) {

			Collection<Address> addresses = AddressModel.getReference().search(
					criteria);
			for (Address addr : addresses) {
				Object[] ro = new Object[5];

				try {
					ro[0] = ((addr.getFirstName() == null) ? "" : (addr
							.getFirstName() + " "))
							+ ((addr.getLastName() == null) ? "" : addr
									.getLastName());
					ro[1] = Resource.getResourceString("Address");
					ro[2] = null;
					ro[3] = new Integer(addr.getKey());
					ro[4] = Address.class;
					tm.addRow(ro);
					tm.tableChanged(new TableModelEvent(tm));
				} catch (Exception e) {
					Errmsg.errmsg(e);
					return;
				}

			}
		}

		Collection<KeyedEntity<?>> taskItems = TaskModel.getReference().search(
				criteria);
		for (KeyedEntity<?> item : taskItems) {
			Object[] ro = new Object[5];

			try {
				if (item instanceof Project && projectCheckBox.isSelected()) {
					ro[0] = ((Project) item).getDescription();
					ro[1] = Resource.getResourceString("project");
					ro[2] = null;
					ro[3] = new Integer(item.getKey());
					ro[4] = Project.class;
					tm.addRow(ro);
					tm.tableChanged(new TableModelEvent(tm));
				} else if (item instanceof Task && taskCheckBox.isSelected()) {
					ro[0] = ((Task) item).getDescription();
					ro[1] = Resource.getResourceString("task");
					ro[2] = null;
					ro[3] = new Integer(item.getKey());
					ro[4] = Task.class;
					tm.addRow(ro);
					tm.tableChanged(new TableModelEvent(tm));
				}

			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}

		}
		
		if (memoCheckBox.isSelected()) {
			Collection<Memo> memos = MemoModel
					.getReference().search(criteria);
			for (Memo memo : memos) {
				Object[] ro = new Object[5];

				try {
					ro[0] = memo.getMemoName();
					ro[1] = Resource.getResourceString("memo");
					ro[2] = null;
					ro[3] = new Integer(0);
					ro[4] = Memo.class;
					tm.addRow(ro);
					tm.tableChanged(new TableModelEvent(tm));
				} catch (Exception e) {
					Errmsg.errmsg(e);
					return;
				}

			}
		}

		// sort the table by date
		tm.sortByColumn(0);
	}

	/**
	 * Show the selected item
	 * 
	 * @throws Exception
	 */
	private void showSelectedItem() throws Exception {

		// get the selected row
		int rows[] = resultsTable.getSelectedRows();
		if (rows.length != 1)
			return;
		int row = rows[0];
		TableSorter tm = (TableSorter) resultsTable.getModel();

		int key = ((Integer) tm.getValueAt(row, 3)).intValue();
		Class<?> cl = (Class<?>) tm.getValueAt(row, 4);
		if (cl == Appointment.class) {
			Appointment ap = AppointmentModel.getReference().getAppt(key);
			if (ap == null) {
				return;
			}

			Calendar cal = new GregorianCalendar();
			cal.setTime(ap.getDate());

			// bring up an appt editor window
			AppointmentListView ag = new AppointmentListView(cal
					.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
					.get(Calendar.DATE));
			ag.showApp(ap.getKey());
			ag.showView();
		}
		// open a project
		else if (cl == Project.class) {
			Project ap = TaskModel.getReference().getProject(key);
			if (ap == null) {
				return;
			}

			new ProjectView(ap, ProjectView.Action.CHANGE, null).showView();
		}
		// open a task
		else if (cl == Task.class) {
			Task ap = TaskModel.getReference().getTask(key);
			if (ap == null) {
				return;
			}

			new TaskView(ap, TaskView.Action.CHANGE, null).showView();
		}
		// open an address
		else if (cl == Address.class) {
			Address ap = AddressModel.getReference().getAddress(key);
			if (ap == null) {
				return;
			}
			new AddressView(ap).showView();
		}
		// open a memo
		else if (cl == Memo.class) {
			Component c = MultiView.getMainView().setView(ViewType.MEMO);

			// show the actual memo
			if (c != null && c instanceof MemoPanel) {
				MemoPanel mp = (MemoPanel) c;
				mp.selectMemo((String) tm.getValueAt(row, 0));
			}
		}
	}

}
