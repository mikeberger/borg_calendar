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

package net.sf.borg.ui.calendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.toedter.calendar.JDateChooser;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.Theme;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.TrayIconProxy;
import net.sf.borg.ui.task.ProjectView;
import net.sf.borg.ui.task.TaskView;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PlainDateEditor;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.TablePrinter;
import net.sf.borg.ui.util.TableSorter;

/**
 * Dockable window that shows a sorted list of all todos, allows the user to
 * take action on the todos, and lets the user quickly enter a new todo
 */
public class TodoView extends DockableView implements Prefs.Listener, Module {

	/**
	 * Adds user colors to the todo table
	 */
	class TodoTableCellRenderer extends DefaultTableCellRenderer {

		private TableCellRenderer originalRenderer = null;

		private static final long serialVersionUID = 1L;

		TodoTableCellRenderer(TableCellRenderer orig) {
			originalRenderer = orig;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			Component c = originalRenderer.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);

			DateFormat sdf = DateFormat.getDateInstance();

			JLabel theTableCellComponent = (JLabel) c;

			// user colors
			if (user_colors) {
				if (isSelected) {
					theTableCellComponent.setForeground(Color.BLACK);
					theTableCellComponent.setBackground(Color.ORANGE);
				} else {

					Theme t = Theme.getCurrentTheme();
					String color = table.getModel().getValueAt(row, 3)
							.toString();
					theTableCellComponent.setForeground(new Color(t.colorFromString(color)));
					theTableCellComponent.setBackground(new Color(t
							.getWeekdayBg()));

				}
			}
			if (column == 0) {
				// format dates
				String dateText = sdf.format(value);
				if (!DateUtil.isAfter((Date) value, new Date()))
					dateText = "* " + dateText;
				theTableCellComponent.setText(dateText);
			} else if (column == 1) {
				// set tooltip to todo text
				theTableCellComponent.setToolTipText((String)table.getModel().getValueAt(row, 6));
			}
			
			if( column == 2 || column == 3)
				theTableCellComponent.setHorizontalAlignment(CENTER);
			else
				theTableCellComponent.setHorizontalAlignment(LEFT);

			return theTableCellComponent;
		}
	}

	/**
	 * Icon to show on toggle buttons - no text is shown, only a color
	 */
	static private class ToggleButtonIcon implements Icon {

		private Color color = Color.BLACK;
		static private final int height = 10;
		static private final int width = 30;

		/**
		 * Instantiates a new toggle button icon.
		 * 
		 * @param col
		 *            the color
		 */
		public ToggleButtonIcon(Color col) {
			color = col;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight() {
			return height;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth() {
			return width;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#paintIcon(java.awt.Component,
		 * java.awt.Graphics, int, int)
		 */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.BLACK);
			g2.drawRect(x, y, width, height);
			g2.setColor(color);
			g2.fillRect(x, y, width, height);
		}
	}

	private static final long serialVersionUID = 1L;

	/** The change date action. */
	private final ActionListener changeDateAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			List<Appointment> aplist = new ArrayList<Appointment>();
			for (KeyedEntity<?> ke : getSelectedItems(true)) {
				aplist.add((Appointment) ke);
			}
			AppointmentListView.onChangeDate(aplist);
		}
	};

	/** The done delete action. */
	private final ActionListener doneDeleteAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			doTodoAction(true);
		}
	};

	/** The done no delete action. */
	private final ActionListener doneNoDeleteAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			doTodoAction(false);
		}
	};

	/** The move to following day action. */
	private final ActionListener moveToFollowingDayAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			List<Appointment> aplist = new ArrayList<Appointment>();
			for (KeyedEntity<?> ke : getSelectedItems(true)) {
				aplist.add((Appointment) ke);
			}
			AppointmentListView.onMoveToFollowingDay(aplist);
		}
	};

	/** The category combo box. */
	private JComboBox<String> categoryComboBox;

	/** The black toggle button. */
	private JToggleButton blackToggleButton;

	/** The blue toggle button. */
	private JToggleButton blueToggleButton;

	/** The green toggle button. */
	private JToggleButton greenToggleButton;

	/** The red toggle button. */
	private JToggleButton redToggleButton;

	/** The white toggle button. */
	private JToggleButton whiteToggleButton;

	/** The todo list. */
	private Collection<KeyedEntity<?>> theTodoList;

	/** The todo date. */
	private JDateChooser todoDate;

	/** The todo table. */
	private JTable todoTable;

	/** The todo text. */
	private JTextField todoText;

	/** Spinner for todo priority. */
	private JSpinner todoPrioritySpinner;

	/** cached copy of user-colors preference */
	private boolean user_colors = false;

	private JLabel totalLabel;

	private boolean isInitialized = false;

	/**
	 * Instantiates a new todo view.
	 */
	public TodoView() {

		super();

	}

	/**
	 * add a todo
	 * 
	 */
	private void addTodoActionPerformed() {

		// get text and date
		String tdtext = todoText.getText();
		Calendar c = todoDate.getCalendar();

		// auto default blank date to today's date.
		if (c == null) {
			if (Prefs
					.getBoolPref(PrefName.TODO_QUICK_ENTRY_AUTO_SET_DATE_FIELD)) {
				c = new GregorianCalendar();
				c.setTime(new Date());
			}
		}

		// warn the user if text or date is missing
		if (tdtext.trim().isEmpty() || c == null) {
			Errmsg.getErrorHandler().notice(
					Resource.getResourceString("todomissingdata"));
			return;
		}

		// load up a default appt from any saved prefs
		Appointment appt = AppointmentModel
				.getDefaultAppointment();
		if (appt == null)
			appt = AppointmentModel.getReference().newAppt();

		// set the date
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.AM_PM, Calendar.AM);
		appt.setDate(c.getTime());

		// set text
		appt.setText(tdtext);

		// todo flag
		appt.setTodo(true);

		// not private
		appt.setPrivate(false);

		// set color
		if (redToggleButton.isSelected())
			appt.setColor("red");
		else if (blueToggleButton.isSelected())
			appt.setColor("blue");
		else if (greenToggleButton.isSelected())
			appt.setColor("green");
		else if (whiteToggleButton.isSelected())
			appt.setColor("white");
		else
			appt.setColor("black");

		// no repeating
		appt.setFrequency(Repeat.ONCE);
		appt.setTimes(Integer.valueOf(1));
		appt.setRepeatFlag(false);

		// set category
		String cat = (String) categoryComboBox.getSelectedItem();
		if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
			appt.setCategory(null);
		} else {
			appt.setCategory(cat);
		}

		appt.setPriority((Integer) todoPrioritySpinner.getValue());
		AppointmentModel.getReference().saveAppt(appt);

		// clear the contents of the todo text field
		// if the preference is set.
		if (Prefs.getBoolPref(PrefName.TODO_QUICK_ENTRY_AUTO_CLEAR_TEXT_FIELD)) {
			todoText.setText("");
		}

		requestFocus();

	}

	private boolean allAppointmentsSelected() {
		List<KeyedEntity<?>> items = getSelectedItems(false);
		for (int i = 0; i < items.size(); ++i) {
			Object o = items.get(i);
			if (!(o instanceof Appointment)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * mark selected items as done. For appointments, mark the todo as done. For
	 * task, projects, subtasks, mark the item as closed
	 * 
	 * @param del
	 *            if true and it is an appointment, delete the appointment when
	 *            all occurrences are done
	 */
	private void doTodoAction(boolean del) {

		// get the selected items
		List<KeyedEntity<?>> items = getSelectedItems(false);
		for (int i = 0; i < items.size(); ++i) {
			try {
				// take different action depending on the entity type
				Object o = items.get(i);
				if (o instanceof Appointment) {
					int key = ((Appointment) o).getKey();
					AppointmentModel.getReference().do_todo(key, del);
				} else if (o instanceof Project) {
					Project p = (Project) o;
					TaskModel.getReference().closeProject(p.getKey());
				} else if (o instanceof Task) {
					Task t = (Task) o;
					TaskModel.getReference().close(t.getKey());
				} else if (o instanceof Subtask) {
					Subtask s = (Subtask) o;
					s.setCloseDate(new Date());
					TaskModel.getReference().saveSubTask(s);
				}

			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
		}
	}

	@Override
	public JPanel getComponent() {

		if (!isInitialized) {
			// listen for pref changes
			Prefs.addListener(this);

			// listen for appt and task model changes
			addModel(AppointmentModel.getReference());
			addModel(TaskModel.getReference());
			addModel(CategoryModel.getReference());

			// init the gui components
			initComponents();

			// the todos will be displayed in a sorted table with 2 columns -
			// data and todo text
			todoTable.setModel(new TableSorter(new String[] {
					Resource.getResourceString("Date"),
					Resource.getResourceString("To_Do"),
					Resource.getResourceString("Category"),
					Resource.getResourceString("Color"), "key",
					Resource.getResourceString("Priority"), "tooltip" }, new Class[] {
					Date.class, java.lang.String.class, java.lang.String.class,
					java.lang.String.class, java.lang.Integer.class,
					java.lang.Integer.class, java.lang.String.class }));

			todoTable.getColumnModel().getColumn(0).setPreferredWidth(140);
			todoTable.getColumnModel().getColumn(1).setPreferredWidth(400);
			todoTable.getColumnModel().getColumn(2).setPreferredWidth(120);
			todoTable.getColumnModel().getColumn(5).setPreferredWidth(50);

			user_colors = Prefs.getBoolPref(PrefName.UCS_ONTODO);

			// set user color renderer
			TableCellRenderer defaultObjectRenderer = todoTable
					.getDefaultRenderer(Object.class);
			todoTable.setDefaultRenderer(Object.class,
					new TodoTableCellRenderer(defaultObjectRenderer));
			TableCellRenderer defaultDateRenderer = todoTable
					.getDefaultRenderer(Date.class);
			todoTable.setDefaultRenderer(Date.class, new TodoTableCellRenderer(
					defaultDateRenderer));
			TableCellRenderer defaultIntegerRenderer = todoTable
					.getDefaultRenderer(Integer.class);
			todoTable.setDefaultRenderer(Integer.class,
					new TodoTableCellRenderer(defaultIntegerRenderer));

			// remove the hidden columns - color and key and tooltip
			todoTable.removeColumn(todoTable.getColumnModel().getColumn(3));
			todoTable.removeColumn(todoTable.getColumnModel().getColumn(3));
			todoTable.removeColumn(todoTable.getColumnModel().getColumn(4));

			refresh();
			isInitialized = true;
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.DockableView#getFrameTitle()
	 */
	@Override
	public String getFrameTitle() {
		return Resource.getResourceString("To_Do_List");
	}

	@Override
	public String getModuleName() {
		return Resource.getResourceString("To_Do");
	}

	/**
	 * Gets the selected items.
	 * 
	 * @param appts_only
	 *            if true, return appointments only
	 * 
	 * @return the selected items
	 */
	private List<KeyedEntity<?>> getSelectedItems(boolean appts_only) {
		List<KeyedEntity<?>> lst = new ArrayList<KeyedEntity<?>>();
		int[] indices = todoTable.getSelectedRows();
		for (int i = 0; i < indices.length; ++i) {
			int index = indices[i];
			try {

				// need to ask the table for the original (befor sorting) index
				// of the selected row
				TableSorter tm = (TableSorter) todoTable.getModel();
				int k = tm.getMappedIndex(index);

				Object o = theTodoList.toArray()[k];
				if (!appts_only || o instanceof Appointment) {
					lst.add((KeyedEntity<?>) o);
				}
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
		}

		return lst;
	}

	private JLabel getTotalLabel() {
		if (totalLabel == null) {
			totalLabel = new JLabel();
		}
		return totalLabel;
	}

	@Override
	public ViewType getViewType() {
		return ViewType.TODO;
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {

		// create widgets (all bunched up because of code generation - not worth
		// cleanup up
		JScrollPane tableScroll = new JScrollPane();
		todoTable = new JTable();
		todoText = new JTextField();

		// add keyboard shortcut to do add todo item when enter pressed on the
		// text field.
		todoText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					addTodoActionPerformed();
				}
			}
		});

		todoDate = new JDateChooser(new PlainDateEditor());

		JButton addTodoButton = new JButton();
		JLabel todoLabel = new JLabel();
		JLabel dateLabel = new JLabel();
		categoryComboBox = new JComboBox<String>();

		// initial toggle button settings
		redToggleButton = new JToggleButton("", false);
		blueToggleButton = new JToggleButton("", false);
		greenToggleButton = new JToggleButton("", false);
		blackToggleButton = new JToggleButton("", true);
		whiteToggleButton = new JToggleButton("", false);

		// initial priority spinner settings
		int val = 5;
		int min = 1;
		int max = 10;
		int step = 1;
		todoPrioritySpinner = new JSpinner(new SpinnerNumberModel(val, min,
				max, step));

		loadCategories();

		// *******************************************************************
		// main table
		// *******************************************************************
		todoTable.setBorder(new LineBorder(new Color(0, 0, 0)));
		DefaultListSelectionModel mylsmodel = new DefaultListSelectionModel();
		mylsmodel
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		todoTable.setSelectionModel(mylsmodel);
		todoTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() < 2)
					return;
				onEditTodo();
			}
		});

		tableScroll.setViewportView(todoTable);
		todoTable.setShowGrid(true);
		todoTable.setIntercellSpacing(new Dimension(1, 1));


		// *******************************************************************
		// quick entry panel
		// *******************************************************************
		JPanel quickEntryPanel = new JPanel();
		quickEntryPanel.setLayout(new GridBagLayout());
		quickEntryPanel.setBorder(BorderFactory.createTitledBorder(null,
				Resource.getResourceString("todoquickentry"),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, null, null));
		addTodoButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(addTodoButton, "Add");
		addTodoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				addTodoActionPerformed();
			}
		});

		JPanel toggleButtonPanel = new JPanel();
		toggleButtonPanel.setLayout(new FlowLayout());
		toggleButtonPanel.add(redToggleButton);
		toggleButtonPanel.add(blueToggleButton);
		toggleButtonPanel.add(greenToggleButton);
		toggleButtonPanel.add(blackToggleButton);
		toggleButtonPanel.add(whiteToggleButton);

		Theme t = Theme.getCurrentTheme();
		redToggleButton.setIcon(new ToggleButtonIcon(new Color(t
				.getTextColor1())));

		blueToggleButton.setIcon(new ToggleButtonIcon(new Color(t
				.getTextColor2())));

		greenToggleButton.setIcon(new ToggleButtonIcon(new Color(t
				.getTextColor3())));

		blackToggleButton.setIcon(new ToggleButtonIcon(new Color(t
				.getTextColor4())));

		whiteToggleButton.setIcon(new ToggleButtonIcon(new Color(t
				.getTextColor5())));

		ButtonGroup mutator = new ButtonGroup();
		mutator.add(redToggleButton);
		mutator.add(blueToggleButton);
		mutator.add(greenToggleButton);
		mutator.add(blackToggleButton);
		mutator.add(whiteToggleButton);

		JLabel categoryLabel = new JLabel();
		ResourceHelper.setText(categoryLabel, "Category");
		categoryLabel.setLabelFor(categoryComboBox);

		toggleButtonPanel.add(categoryLabel);
		toggleButtonPanel.add(categoryComboBox);

		JLabel priorityLabel = new JLabel();
		ResourceHelper.setText(priorityLabel, "Priority");
		priorityLabel.setLabelFor(todoPrioritySpinner);

		toggleButtonPanel.add(priorityLabel);
		toggleButtonPanel.add(todoPrioritySpinner);

		ResourceHelper.setText(todoLabel, "To_Do");
		todoLabel.setLabelFor(todoText);
		ResourceHelper.setText(dateLabel, "Date");
		dateLabel.setLabelFor(todoDate);

		quickEntryPanel.add(todoLabel, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.HORIZONTAL));
		quickEntryPanel.add(todoText, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.HORIZONTAL, 10.0, 0.0));
		quickEntryPanel.add(toggleButtonPanel, GridBagConstraintsFactory
				.create(0, 2, GridBagConstraints.BOTH, 1.0, 0.0));
		quickEntryPanel.add(dateLabel, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.HORIZONTAL, 1.0, 0.0));
		quickEntryPanel.add(todoDate, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.HORIZONTAL));
		quickEntryPanel.add(addTodoButton,
				GridBagConstraintsFactory.create(2, 1));

		// *******************************************************************
		// popup menu
		// *******************************************************************
		ActionListener alEdit = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				onEditTodo();
			}
		};

		final JPopupMenu apptPopupMenu = PopupMenuHelper
				.createPopupMenu(new PopupMenuHelper.Entry[] {
						new PopupMenuHelper.Entry(doneDeleteAction,
								"Done_(Delete)"),
						new PopupMenuHelper.Entry(doneNoDeleteAction,
								"Done_(No_Delete)"),
						new PopupMenuHelper.Entry(alEdit, "Edit"),
						new PopupMenuHelper.Entry(moveToFollowingDayAction,
								"Move_To_Following_Day"),
						new PopupMenuHelper.Entry(changeDateAction,
								"changedate") });

		final JPopupMenu todoPopupMenu = PopupMenuHelper
				.createPopupMenu(new PopupMenuHelper.Entry[] {
						new PopupMenuHelper.Entry(doneDeleteAction,
								"Done_(No_Delete)"),
						new PopupMenuHelper.Entry(alEdit, "Edit") });

		todoTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case 0x020D:
					int[] selIndices = todoTable.getSelectedRows();
					if (selIndices.length == 0)
						return;
					int rowIndex = selIndices[0];
					Rectangle rct = todoTable.getCellRect(rowIndex, 0, false);
					if (getSelectedItems(false).isEmpty())
						return;
					if (allAppointmentsSelected())
						apptPopupMenu
								.show(todoTable, rct.x, rct.y + rct.height);
					else
						todoPopupMenu
								.show(todoTable, rct.x, rct.y + rct.height);

					break;
				}
			}
		});

		todoTable.addMouseListener(new MouseAdapter() {
			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int row = todoTable.rowAtPoint(e.getPoint());
					if (row != -1 && !todoTable.isRowSelected(row)) {
						todoTable.getSelectionModel().setSelectionInterval(row,
								row);
					}
					if (getSelectedItems(false).isEmpty())
						return;
					if (allAppointmentsSelected())
						apptPopupMenu.show(e.getComponent(), e.getX(), e.getY());
					else
						todoPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
		});

		// *******************************************************************
		// button panel
		// *******************************************************************
		JPanel buttonPanel = new JPanel();

		JButton doneButton = new JButton();
		ResourceHelper.setText(doneButton, "Done_(No_Delete)");
		doneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doTodoAction(false);
			}
		});
		doneButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Properties16.gif")));
		buttonPanel.add(doneButton, null);

		JButton doneDeleteButton = new JButton();
		ResourceHelper.setText(doneDeleteButton, "Done_(Delete)");
		doneDeleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doTodoAction(true);
			}
		});
		doneDeleteButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		buttonPanel.add(doneDeleteButton, null);

		// *******************************************************************
		// top level panel
		// *******************************************************************
		setLayout(new GridBagLayout());
		add(tableScroll, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		JPanel medPanel = new JPanel(new BorderLayout());

		medPanel.add(getTotalLabel(), BorderLayout.WEST);
		medPanel.add(buttonPanel, BorderLayout.CENTER);

		add(medPanel,
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
		add(quickEntryPanel, GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.HORIZONTAL, 1.0, 0.0));

	}

	@Override
	public void initialize(MultiView parent) {
		final MultiView par = parent;
		parent.addToolBarItem(
				new ImageIcon(getClass().getResource(
						"/resource/Properties16.gif")), getModuleName(),
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						par.setView(getViewType());
					}
				});
		TrayIconProxy.addAction(getModuleName(), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				par.setView(getViewType());
			}
		});
	}

	private void loadCategories() {
		// load categories
		try {
			categoryComboBox.removeAllItems();
			Collection<String> acats = CategoryModel.getReference()
					.getCategories();
			Iterator<String> ait = acats.iterator();
			while (ait.hasNext()) {
				categoryComboBox.addItem(ait.next());
			}
			categoryComboBox.setSelectedIndex(0);
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	/**
	 * open a todo item for editing
	 */
	private void onEditTodo() {

		int row = todoTable.getSelectedRow();
		if (row == -1)
			return;

		// Ensure only one row is selected.
		todoTable.getSelectionModel().setSelectionInterval(row, row);

		TableSorter tm = (TableSorter) todoTable.getModel();
		int k = tm.getMappedIndex(row);

		// get the object and edit differently based on type
		Object o = theTodoList.toArray()[k];
		if (o instanceof Appointment) {

			Date d = (Date) tm.getValueAt(row, 0);
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(d);

			// bring up an appt editor window
			AppointmentListView ag = new AppointmentListView(
					cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DATE));
			Appointment ap = (Appointment) o;
			ag.showView();

			ag.showApp(ap.getKey());

		} else if (o instanceof Project) {
			try {
				new ProjectView((Project) o, ProjectView.Action.CHANGE, null)
						.showView();

			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
				return;
			}
		} else if (o instanceof Task) {
			try {
				new TaskView((Task) o, TaskView.Action.CHANGE, null).showView();
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
				return;
			}
		} else if (o instanceof Subtask) {
			int taskid = ((Subtask) o).getTask().intValue();
			Task t;
			try {
				t = TaskModel.getReference().getTask(taskid);
				TaskView tskg = new TaskView(t, TaskView.Action.CHANGE, null);
				tskg.setVisible(true);
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
				return;
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.common.Prefs.Listener#prefsChanged()
	 */
	@Override
	public void prefsChanged() {
		user_colors = Prefs.getBoolPref(PrefName.UCS_ONTODO);
		refresh();
	}

	/**
	 * Prints the todo table
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void print() {
		try {
			TablePrinter.printTable(todoTable);
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	/**
	 * load todos from the model and show in the UI
	 */
	@Override
	public void refresh() {

		// get the todos from the data model
		theTodoList = new ArrayList<KeyedEntity<?>>();
		theTodoList.addAll(AppointmentModel.getReference().get_todos());

		// init the table to empty
		TableSorter tm = (TableSorter) todoTable.getModel();
		tm.addMouseListenerToHeaderInTable(todoTable);
		tm.setRowCount(0);

		// add the todo appointment rows to the table
		Iterator<KeyedEntity<?>> tdit = theTodoList.iterator();
		int totalCount = 0;
		while (tdit.hasNext()) {
			Appointment r = (Appointment) tdit.next();

			try {

				// date is the next todo field if present, otherwise
				// the due date
				Date nt = r.getNextTodo();
				if (nt == null) {
					nt = r.getDate();
				}

				// get appt text
				String tx = AppointmentTextFormat.format(r, nt);

				// add the table row
				Object[] ro = new Object[7];
				ro[0] = nt;
				ro[1] = tx;
				ro[2] = r.getCategory(); // category

				// color
				if (r.getColor() == null)
					ro[3] = "black";
				else
					ro[3] = r.getColor();

				// key
				ro[4] = Integer.valueOf(r.getKey());

				// priority
				ro[5] = r.getPriority();
				
				// tooltip
				ro[6] = AppointmentTextFormat.format(r, nt, true);

				tm.addRow(ro);
				totalCount++;
				tm.tableChanged(new TableModelEvent(tm));
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
				return;
			}

			getTotalLabel().setText(totalCount + " items");
		}

		// add open projects with a due date to the list
		String show_abb = Prefs.getPref(PrefName.TASK_SHOW_ABBREV);
		if (Prefs.getBoolPref(PrefName.CAL_SHOW_TASKS)) {

			try {
				Collection<Project> pjs = TaskModel.getReference()
						.getProjects();
				Iterator<Project> it = pjs.iterator();
				while (it.hasNext()) {

					Project pj = it.next();
					if (pj.getDueDate() == null)
						continue;

					// skip closed projects
					if (pj.getStatus().equals(
							Resource.getResourceString("CLOSED")))
						continue;

					// filter by category
					if (!CategoryModel.getReference().isShown(pj.getCategory()))
						continue;

					// build a string for the table - with prefix if needed
					String abb = "";
					if (show_abb.equals("true"))
						abb = "PR" + pj.getKey() + " ";
					String todostring = abb + pj.getText();

					Object[] ro = new Object[7];
					ro[0] = pj.getDueDate();
					ro[1] = todostring;
					ro[2] = pj.getCategory();
					if (pj.getColor() == null)
						ro[3] = "black";
					else
						ro[3] = pj.getColor();
					ro[4] = null;
					ro[5] = null;
					ro[6] = todostring;
					tm.addRow(ro);
					theTodoList.add(pj);
					tm.tableChanged(new TableModelEvent(tm));
				}
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
				return;
			}

			// add open tasks with a due date to the list
			Vector<Task> mrs = TaskModel.getReference().get_tasks();
			for (int i = 0; i < mrs.size(); i++) {

				Task mr = mrs.elementAt(i);
				if (mr.getDueDate() == null)
					continue;

				try {

					// build a string for the table - with prefix if needed
					String abb = "";
					if (show_abb.equals("true"))
						abb = "BT" + mr.getKey() + " ";
					String btstring = abb + mr.getText();

					Object[] ro = new Object[7];
					ro[0] = mr.getDueDate();
					ro[1] = btstring;
					ro[2] = mr.getCategory();
					if (mr.getColor() == null)
						ro[3] = "black";
					else
						ro[3] = mr.getColor();
					ro[4] = null;
					ro[5] = mr.getPriority();
					ro[6] = btstring;

					tm.addRow(ro);
					theTodoList.add(mr);
					tm.tableChanged(new TableModelEvent(tm));
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
					return;
				}

			}
		}

		// add open subtasks with a due date to the list
		if (Prefs.getBoolPref(PrefName.CAL_SHOW_SUBTASKS)) {
			try {
				Collection<Subtask> sts = TaskModel.getReference()
						.getSubTasks();
				Iterator<Subtask> it = sts.iterator();
				while (it.hasNext()) {

					Subtask st = it.next();

					if (st.getDueDate() == null)
						continue;
					if (st.getCloseDate() != null)
						continue;

					Task task = TaskModel.getReference().getTask(
							st.getTask().intValue());
					String cat = task.getCategory();

					if (!CategoryModel.getReference().isShown(cat))
						continue;

					// build a string for the table - with prefix if needed
					String abb = "";
					if (show_abb.equals("true"))
						abb = "ST" + st.getKey() + " ";
					String btstring = abb + st.getText();

					Object[] ro = new Object[7];
					ro[0] = st.getDueDate();
					ro[1] = btstring;
					ro[2] = cat;
					if (st.getColor() == null)
						ro[3] = "black";
					else
						ro[3] = st.getColor();
					ro[4] = null;
					ro[5] = null;
					ro[6] = btstring;

					tm.addRow(ro);
					theTodoList.add(st);
					tm.tableChanged(new TableModelEvent(tm));
				}
			} catch (Exception e) {
				// Errmsg.getErrorHandler().errmsg(e);
				return;
			}
		}
		// sort the table by date
		tm.sortByColumn(0);

	}

	@Override
	public void update(ChangeEvent event) {

		// handle category change
		if (event.getModel() instanceof CategoryModel) {
			loadCategories();
		}

		refresh();
	}
}
