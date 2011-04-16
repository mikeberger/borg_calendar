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
package net.sf.borg.ui.task;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;
import net.sf.borg.model.entity.Tasklog;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.link.LinkPanel;
import net.sf.borg.ui.util.DateDialog;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.StripedTable;
import net.sf.borg.ui.util.TableSorter;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDateChooserCellEditor;

/**
 * UI for Viewing and Editing individual Tasks and their Subtasks
 */
public class TaskView extends DockableView {

	private static final long serialVersionUID = 1L;

	/**
	 * Render log table dates in a particular date format
	 */
	private class LogTableDateRenderer extends JLabel implements
			TableCellRenderer {
		private static final long serialVersionUID = 1L;

		public LogTableDateRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			Date d = (Date) obj;
			JLabel l = (JLabel) defaultDateCellRenderer
					.getTableCellRendererComponent(table, obj, isSelected,
							hasFocus, row, column);

			this.setBackground(l.getBackground());
			this.setForeground(l.getForeground());
			// use MEDIUM format
			this.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
					DateFormat.MEDIUM).format(d));
			return this;
		}
	}

	/**
	 * Renders subtask due date in different colors based on proximity to due
	 * date
	 */
	private class SubTaskDueDateRenderer extends JLabel implements
			TableCellRenderer {
		private static final long serialVersionUID = 1L;

		public SubTaskDueDateRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			Boolean closed = (Boolean) table.getModel().getValueAt(row, 0);
			Date dd = (Date) obj;

			JLabel l = (JLabel) defaultDateCellRenderer
					.getTableCellRendererComponent(table, obj, isSelected,
							hasFocus, row, column);

			this.setBackground(l.getBackground());
			this.setForeground(l.getForeground());
			this.setHorizontalAlignment(l.getHorizontalAlignment());

			// if no date, then show dashes
			if (dd != null)
				this.setText(DateFormat.getDateInstance().format(dd));
			else {
				this.setText("--");
				this.setHorizontalAlignment(CENTER);
			}

			// go no further if the task is closed or this is not the due date
			// column
			String nm = table.getColumnName(column);
			if (closed.booleanValue() == true
					|| !nm.equals(Resource.getResourceString("Due_Date"))
					|| obj == null)
				return this;

			/*
			 * color the due date background based on days left
			 */
			int days = TaskModel.daysLeft(dd);
			if (!isSelected) {
				// yellow alert -- <10 days left
				if (days < 10)
					this.setBackground(new Color(255, 255, 175));

				if (days < 5)
					this.setBackground(new Color(255, 200, 120));

				// red alert -- <2 days left
				if (days < 2) {
					this.setBackground(new Color(255, 120, 120));
				}
			}

			return this;
		}
	}

	/**
	 * Renderer for subtask int columns - shows dashes for unsaved subtasks
	 */
	private class SubtaskIntRenderer extends JLabel implements
			TableCellRenderer {

		private static final long serialVersionUID = 1L;

		/**
		 * Instantiates a new sT int renderer.
		 */
		public SubtaskIntRenderer() {
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

			JLabel l = (JLabel) defaultIntegerCellRenderer
					.getTableCellRendererComponent(table, obj, isSelected,
							hasFocus, row, column);
			this.setHorizontalAlignment(CENTER);
			this.setForeground(l.getForeground());
			this.setBackground(l.getBackground());

			if (obj != null && obj instanceof Integer) {
				int i = ((Integer) obj).intValue();
				if (column == 1 && i == 0) {
					this.setText("--");
				} else {
					this.setText(Integer.toString(i));
				}
			} else if (obj == null)
				this.setText("--");
			return this;

		}
	}

	/**
	 * Actions being taken on a task when starting the editor
	 */
	public enum Action {

		ADD, CHANGE, CLONE;
	}

	/**
	 * Gets the project id form a project string containing id and name.
	 * 
	 * @param s
	 *            the project string from the project combo box
	 * 
	 * @return the project id
	 * 
	 * @throws Exception
	 */
	static public Integer getProjectId(String s) throws Exception {
		int i = s.indexOf(":");
		if (i == -1)
			throw new Exception("Cannot parse project label");
		String ss = s.substring(0, i);

		int pid = Integer.parseInt(ss);
		return new Integer(pid);

	}

	/**
	 * Gets the project string for a project to put in the project combo box
	 * 
	 * @param p
	 *            the project
	 * 
	 * @return the project string
	 */
	static public String getProjectString(Project p) {
		return p.getKey() + ":" + p.getDescription();
	}

	/** The link panel. */
	private LinkPanel linkPanel;

	/** The category combo box. */
	private JComboBox categoryComboBox = null;

	/** The close date. */
	private JTextField closeDate = null;

	/** The days left text. */
	private JTextField daysLeftText = null;

	/** The default date cell renderer. */
	private TableCellRenderer defaultDateCellRenderer;

	/** The default integer cell renderer. */
	private TableCellRenderer defaultIntegerCellRenderer;

	/** The due date chooser. */
	private JDateChooser dueDateChooser;

	/** The parent project. */
	private Integer parentProject = null;

	/** The task id text. */
	private JTextField taskIdText;

	/** The task tabbed panel. */
	private JTabbedPane taskTabbedPanel;

	/** The description text. */
	private JTextArea descriptionText;

	/** The resolution text. */
	private JTextArea resolutionText;

	/** The log table. */
	private StripedTable logtable = new StripedTable();

	/** The person assigned text. */
	private JTextField personAssignedText;

	/** The priority text. */
	private JComboBox priorityText;

	/** The project combo box. */
	private JComboBox projectComboBox = new JComboBox();

	/** The sub task table. */
	private StripedTable subTaskTable = new StripedTable();

	/** The start date chooser. */
	private JDateChooser startDateChooser;

	/** The status combo box. */
	private JComboBox statusComboBox;

	/** The sub task ids to be deleted. */
	private ArrayList<Integer> subTaskIdsToBeDeleted = new ArrayList<Integer>();

	/** The window title. */
	private String windowTitle = "";

	/** The task type combo box. */
	private JComboBox taskTypeComboBox;

	/**
	 * constructor
	 * 
	 * @param task
	 *            the task
	 * @param function
	 *            the action being taken on the task
	 * @param projectid
	 *            the projectid or null if no initial project
	 * 
	 * @throws Exception
	 */
	public TaskView(Task task, Action function, Integer projectid)
			throws Exception {
		super();

		// listen for model changes
		addModel(LinkModel.getReference()); // to update link tab color

		parentProject = projectid;

		initComponents(); // init the GUI widgets

		initSubtaskTable();
		initLogTable();

		// set size of text area
		descriptionText.setRows(15);
		descriptionText.setColumns(40);

		// load categories
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

		// show the task
		showtask(function, task);

		refresh();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.DockableView#getFrameTitle()
	 */
	@Override
	public String getFrameTitle() {
		return windowTitle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.DockableView#getMenuForFrame()
	 */
	@Override
	public JMenuBar getMenuForFrame() {
		JMenuBar theMenuBar = new JMenuBar();
		JMenu theMenu = new JMenu();
		JMenuItem saveMenuItem = new JMenuItem();

		ResourceHelper.setText(theMenu, "Menu");
		ResourceHelper.setText(saveMenuItem, "Save");
		saveMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				savetask();
			}
		});

		theMenu.add(saveMenuItem);

		theMenuBar.add(theMenu);

		return theMenuBar;
	}

	/**
	 * Gets the selected subtask ids.
	 * 
	 * @return the selected subtask ids
	 */
	private Integer[] getSelectedSubtaskIds() {
		TableSorter ts = (TableSorter) subTaskTable.getModel();
		int[] indices = subTaskTable.getSelectedRows();
		Integer[] ret = new Integer[indices.length];

		for (int i = 0; i < indices.length; ++i) {
			int index = indices[i];
			ret[i] = (Integer) ts.getValueAt(index, 1);
		}

		return ret;
	}

	/**
	 * Initialize the UI
	 */
	private void initComponents() {
		/*
		 * this was one of the worst code-generated messes in borg. It is mostly
		 * cleaned up now, but is not perfect.
		 */

		setLayout(new GridBagLayout());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());

		/*
		 * information panel
		 */

		JPanel taskInformationPanel = new JPanel();
		taskInformationPanel.setLayout(new GridBagLayout());

		taskInformationPanel.setBorder(new TitledBorder(Resource
				.getResourceString("TaskInformation")));

		taskIdText = new JTextField();
		JLabel lblItemNum = new JLabel();
		JLabel lblStatus = new JLabel();
		startDateChooser = new JDateChooser();
		dueDateChooser = new JDateChooser();
		priorityText = new JComboBox();
		for (int p = 1; p <= 5; p++) {
			priorityText.addItem(new Integer(p));
		}

		personAssignedText = new JTextField();
		JLabel lblStartDate = new JLabel();
		JLabel lblDueDate = new JLabel();
		JLabel lblPri = new JLabel();
		JLabel lblPA = new JLabel();
		JLabel lblType = new JLabel();
		statusComboBox = new JComboBox();
		taskTypeComboBox = new JComboBox();
		JLabel categoryLabel = new JLabel();

		JLabel closeLabel = new JLabel();
		closeLabel.setText("");

		JLabel daysLeftLabel = new JLabel();
		daysLeftLabel.setText(Resource.getResourceString("Days_Left"));
		daysLeftText = new JTextField();
		daysLeftText.setEditable(false);

		closeDate = new JTextField();
		closeDate.setEditable(false);
		ResourceHelper.setText(closeLabel, "close_date");

		JLabel prLabel = new JLabel(Resource.getResourceString("project"));

		taskIdText.setText("taskIdText");

		ResourceHelper.setText(lblItemNum, "Item_#");
		lblItemNum.setLabelFor(taskIdText);

		ResourceHelper.setText(lblStatus, "Status");
		lblStatus.setLabelFor(statusComboBox);

		ResourceHelper.setText(lblStartDate, "Start_Date");
		lblStartDate.setLabelFor(startDateChooser);

		ResourceHelper.setText(lblDueDate, "Due_Date");
		lblDueDate.setLabelFor(dueDateChooser);

		ResourceHelper.setText(lblPri, "Pri");
		lblPri.setLabelFor(priorityText);

		ResourceHelper.setText(lblPA, "PA");
		lblPA.setLabelFor(personAssignedText);

		ResourceHelper.setText(lblType, "Type");
		lblType.setLabelFor(taskTypeComboBox);

		ResourceHelper.setText(categoryLabel, "Category");
		categoryComboBox = new JComboBox();
		categoryLabel.setLabelFor(categoryComboBox);

		taskInformationPanel.add(lblItemNum, GridBagConstraintsFactory.create(
				0, 0, GridBagConstraints.BOTH, 0.0, 0.0));
		taskInformationPanel.add(lblStatus, GridBagConstraintsFactory.create(0,
				1, GridBagConstraints.BOTH, 0.0, 0.0));
		taskInformationPanel.add(lblType, GridBagConstraintsFactory.create(0,
				2, GridBagConstraints.BOTH, 0.0, 0.0));
		taskInformationPanel.add(lblStartDate, GridBagConstraintsFactory
				.create(0, 3, GridBagConstraints.BOTH, 0.0, 0.0));
		taskInformationPanel.add(lblDueDate, GridBagConstraintsFactory.create(
				0, 4, GridBagConstraints.BOTH, 0.0, 0.0));
		taskInformationPanel.add(prLabel, GridBagConstraintsFactory.create(0,
				5, GridBagConstraints.BOTH, 0.0, 0.0));

		taskInformationPanel.add(taskIdText, GridBagConstraintsFactory.create(
				1, 0, GridBagConstraints.BOTH, 1.0, 0.0));
		taskInformationPanel.add(statusComboBox, GridBagConstraintsFactory
				.create(1, 1, GridBagConstraints.BOTH, 1.0, 0.0));
		taskInformationPanel.add(taskTypeComboBox, GridBagConstraintsFactory
				.create(1, 2, GridBagConstraints.BOTH, 1.0, 0.0));
		taskInformationPanel.add(startDateChooser, GridBagConstraintsFactory
				.create(1, 3, GridBagConstraints.BOTH, 1.0, 0.0));
		taskInformationPanel.add(dueDateChooser, GridBagConstraintsFactory
				.create(1, 4, GridBagConstraints.BOTH, 1.0, 0.0));
		taskInformationPanel.add(projectComboBox, GridBagConstraintsFactory
				.create(1, 5, GridBagConstraints.BOTH, 1.0, 0.0));

		taskInformationPanel.add(categoryLabel, GridBagConstraintsFactory
				.create(2, 0, GridBagConstraints.BOTH, 0.0, 0.0));
		taskInformationPanel.add(lblPri, GridBagConstraintsFactory.create(2, 1,
				GridBagConstraints.BOTH, 0.0, 0.0));
		taskInformationPanel.add(lblPA, GridBagConstraintsFactory.create(2, 2,
				GridBagConstraints.BOTH, 0.0, 0.0));
		taskInformationPanel.add(closeLabel, GridBagConstraintsFactory.create(
				2, 3, GridBagConstraints.BOTH, 0.0, 0.0));
		taskInformationPanel.add(daysLeftLabel, GridBagConstraintsFactory
				.create(2, 4, GridBagConstraints.BOTH, 0.0, 0.0));

		taskInformationPanel.add(categoryComboBox, GridBagConstraintsFactory
				.create(3, 0, GridBagConstraints.BOTH, 1.0, 0.0));
		taskInformationPanel.add(priorityText, GridBagConstraintsFactory
				.create(3, 1, GridBagConstraints.BOTH, 1.0, 0.0));
		taskInformationPanel.add(personAssignedText, GridBagConstraintsFactory
				.create(3, 2, GridBagConstraints.BOTH, 1.0, 0.0));
		taskInformationPanel.add(closeDate, GridBagConstraintsFactory.create(3,
				3, GridBagConstraints.BOTH, 1.0, 0.0));
		taskInformationPanel.add(daysLeftText, GridBagConstraintsFactory
				.create(3, 4, GridBagConstraints.BOTH, 1.0, 0.0));

		topPanel.add(taskInformationPanel, GridBagConstraintsFactory.create(0,
				0, GridBagConstraints.BOTH, 1.0, 0.0));

		/*
		 * task tabbed panel
		 */
		taskTabbedPanel = new JTabbedPane();

		JScrollPane descriptionScroll = new JScrollPane();
		descriptionText = new JTextArea();
		descriptionText.setLineWrap(true);
		descriptionText.setName("Description");
		descriptionScroll.setViewportView(descriptionText);
		taskTabbedPanel.addTab(Resource.getResourceString("Description"),
				descriptionScroll);

		JScrollPane resolutionScroll = new JScrollPane();
		resolutionText = new JTextArea();
		resolutionText.setLineWrap(true);
		resolutionScroll.setViewportView(resolutionText);
		taskTabbedPanel.addTab(Resource.getResourceString("Resolution"),
				resolutionScroll);

		JScrollPane logPane = new JScrollPane();
		logPane.setViewportView(logtable);
		taskTabbedPanel.addTab(Resource.getResourceString("history"), logPane);

		linkPanel = new LinkPanel();
		taskTabbedPanel.addTab(Resource.getResourceString("links"), linkPanel);

		/*
		 * sub task panel
		 */
		JScrollPane subTaskScroll = new JScrollPane();
		subTaskScroll.setPreferredSize(new Dimension(300, 300));
		subTaskScroll.setViewportView(subTaskTable);

		JPanel subTaskPanel = new JPanel();
		subTaskPanel.setLayout(new GridBagLayout());
		subTaskPanel.setBorder(new TitledBorder(Resource
				.getResourceString("SubTasks")));
		subTaskPanel.add(subTaskScroll, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		/*
		 * split pane
		 */
		JSplitPane taskSplitPane = new JSplitPane();
		taskSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		taskSplitPane.setBottomComponent(subTaskPanel);
		taskSplitPane.setPreferredSize(new Dimension(400, 400));
		taskSplitPane.setDividerLocation(200);
		taskSplitPane.setOneTouchExpandable(true);
		taskSplitPane.setTopComponent(taskTabbedPanel);

		topPanel.add(taskSplitPane, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 1.0, 1.0));

		/*
		 * button panel
		 */
		JPanel buttonPanel = new JPanel();
		JButton savebutton = new JButton();
		topPanel.add(buttonPanel, GridBagConstraintsFactory.create(0, 2));

		savebutton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(savebutton, "Save");
		savebutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				savetask();
			}
		});

		buttonPanel.add(savebutton, savebutton.getName());

		add(topPanel, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

	}

	/**
	 * Initializes the log table.
	 */
	private void initLogTable() {

		logtable.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Date"),
				Resource.getResourceString("Description"), }, new Class[] {
				Date.class, String.class }, new boolean[] { false, false }));

		logtable.getColumnModel().getColumn(0).setPreferredWidth(5);
		logtable.getColumnModel().getColumn(1).setPreferredWidth(300);

		// renderer for formatting the date
		logtable.setDefaultRenderer(Date.class, new LogTableDateRenderer());

		TableSorter ts = (TableSorter) logtable.getModel();

		// sort by date
		ts.sortByColumn(0);
		ts.addMouseListenerToHeaderInTable(logtable);

		// popup menu
		new PopupMenuHelper(logtable,
				new PopupMenuHelper.Entry[] { new PopupMenuHelper.Entry(
						new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent evt) {

								String tasknum = taskIdText.getText();
								if (tasknum.equals("CLONE")
										|| tasknum.equals("NEW"))
									return;
								String logentry = JOptionPane
										.showInputDialog(net.sf.borg.common.Resource
												.getResourceString("Enter_Log"));
								if (logentry == null)
									return;

								try {
									TaskModel.getReference()
											.addLog(Integer.parseInt(tasknum),
													logentry);
									loadLog(Integer.parseInt(tasknum));
								} catch (Exception e) {
									Errmsg.errmsg(e);
								}
							}
						}, "Add_Log"), });

	}

	/**
	 * Initialize the subtask table.
	 */
	private void initSubtaskTable() {

		defaultIntegerCellRenderer = subTaskTable
				.getDefaultRenderer(Integer.class);
		defaultDateCellRenderer = subTaskTable.getDefaultRenderer(Date.class);

		subTaskTable.setModel(new TableSorter(new String[] {
				Resource.getResourceString("Closed"),
				Resource.getResourceString("subtask_id"),
				Resource.getResourceString("Description"),
				Resource.getResourceString("Start_Date"),
				Resource.getResourceString("Due_Date"),
				Resource.getResourceString("duration"),
				Resource.getResourceString("Days_Left"),
				Resource.getResourceString("close_date") }, new Class[] {
				java.lang.Boolean.class, Integer.class, java.lang.String.class,
				Date.class, Date.class, Integer.class, Integer.class,
				Date.class }, new boolean[] { true, false, true, true, true,
				false, false, false }));

		// renderer for centering ints, dealing with nulls
		subTaskTable
				.setDefaultRenderer(Integer.class, new SubtaskIntRenderer());
		// renderer for colorizing approaching due dates
		subTaskTable.setDefaultRenderer(Date.class,
				new SubTaskDueDateRenderer());

		subTaskTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		subTaskTable.getColumnModel().getColumn(1).setPreferredWidth(5);
		subTaskTable.getColumnModel().getColumn(2).setPreferredWidth(300);
		subTaskTable.getColumnModel().getColumn(3).setPreferredWidth(30);
		subTaskTable.getColumnModel().getColumn(4).setPreferredWidth(30);
		subTaskTable.getColumnModel().getColumn(5).setPreferredWidth(30);
		subTaskTable.getColumnModel().getColumn(6).setPreferredWidth(30);
		subTaskTable.getColumnModel().getColumn(7).setPreferredWidth(30);

		// use a date chooser to edit subtask dates in the table
		subTaskTable.setDefaultEditor(Date.class, new JDateChooserCellEditor());

		TableSorter ts = (TableSorter) subTaskTable.getModel();

		// sort by due date
		//ts.sortByColumn(4);
		ts.addMouseListenerToHeaderInTable(subTaskTable);

		// popup menu
		new PopupMenuHelper(subTaskTable, new PopupMenuHelper.Entry[] {
				
				new PopupMenuHelper.Entry(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {

						TableSorter ts2 = (TableSorter) subTaskTable.getModel();
						Integer ids[] = getSelectedSubtaskIds();
						for (int i = 0; i < ids.length; ++i) {
							if (ids[i] == null)
								continue;
							for (int row = 0; row < ts2.getRowCount(); row++) {
								Integer rowid = (Integer) ts2
										.getValueAt(row, 1);
								if (rowid != null
										&& rowid.intValue() == ids[i]
												.intValue()) {
									ts2.setValueAt(null, row, 4);
									break;
								}
							}
						}
					}
				}, "Clear_DueDate"),
				new PopupMenuHelper.Entry(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {

						TableSorter ts2 = (TableSorter) subTaskTable.getModel();

						int[] indices = subTaskTable.getSelectedRows();
						if (indices.length == 0)
							return;

						// prompt user for due date
						DateDialog dlg = new DateDialog(null);
						dlg.setCalendar(new GregorianCalendar());
						dlg.setVisible(true);
						Calendar dlgcal = dlg.getCalendar();
						if (dlgcal == null)
							return;

						// set the due date
						Integer ids[] = getSelectedSubtaskIds();
						for (int i = 0; i < ids.length; ++i) {
							if (ids[i] == null)
								continue;
							for (int row = 0; row < ts2.getRowCount(); row++) {
								Integer rowid = (Integer) ts2
										.getValueAt(row, 1);
								if (rowid != null
										&& rowid.intValue() == ids[i]
												.intValue()) {
									ts2.setValueAt(dlgcal.getTime(), row, 4);
									break;
								}
							}
						}
					}
				}, "Set_DueDate"),
				new PopupMenuHelper.Entry(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {

						TableSorter ts2 = (TableSorter) subTaskTable.getModel();
						Integer ids[] = getSelectedSubtaskIds();
						if (ids.length == 0)
							return;

						// confirm delete
						int ret = JOptionPane.showConfirmDialog(null, Resource
								.getResourceString("Really_Delete_")
								+ "?", Resource
								.getResourceString("Confirm_Delete"),
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (ret != JOptionPane.OK_OPTION)
							return;

						// to delete, we have to save the id in a list for
						// deletion and
						// null out the table rows so it is not added back
						for (int i = 0; i < ids.length; ++i) {
							// System.out.println(ids[i]);
							if (ids[i] == null)
								continue;

							subTaskIdsToBeDeleted.add(ids[i]);

							for (int row = 0; row < ts2.getRowCount(); row++) {
								Integer rowid = (Integer) ts2
										.getValueAt(row, 1);
								if (rowid != null
										&& rowid.intValue() == ids[i]
												.intValue()) {
									// clear the row
									ts2.setValueAt(new Boolean(false), row, 0);
									ts2.setValueAt(null, row, 1);
									ts2.setValueAt(null, row, 2);
									ts2.setValueAt(null, row, 3);
									ts2.setValueAt(null, row, 4);
									ts2.setValueAt(null, row, 5);
									ts2.setValueAt(null, row, 6);
									ts2.setValueAt(null, row, 7);
									break;
								}
							}
						}

						// if table is now empty - add 1 row back so the user
						// can edit
						if (ts2.getRowCount() == 0) {
							insertSubtask();
						}
					}
				}, "Delete"), });

	}

	/**
	 * Insert a blank subtask row in the table
	 */
	private void insertSubtask() {
		Object o[] = { new Boolean(false), null, null, null, null, null, null };
		TableSorter ts = (TableSorter) subTaskTable.getModel();
		ts.addRow(o);
	}

	/**
	 * Load the tasklog table for a given task
	 * 
	 * @param taskid
	 *            the task id
	 * 
	 * @throws Exception
	 */
	private void loadLog(int taskid) throws Exception {

		TableSorter tslog = (TableSorter) logtable.getModel();
		// clear rows
		tslog.setRowCount(0);

		// add log entries
		Collection<Tasklog> logs = TaskModel.getReference().getLogs(taskid);
		for (Tasklog log : logs) {
			Object o[] = { log.getlogTime(), log.getDescription() };
			tslog.addRow(o);
		}
	}

	/**
	 * refresh only updates the link panel tab to indicate links it does not
	 * refresh task data because the user might be editing
	 */
	@Override
	public void refresh() {
		if (linkPanel != null && linkPanel.hasLinks()) {
			taskTabbedPanel.setForegroundAt(3, Color.red);
		} else
			taskTabbedPanel.setForegroundAt(3, Color.black);
	}

	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * Save subtasks for a task from the UI
	 * 
	 * @param task
	 *            the task
	 * 
	 * @throws Warning
	 * @throws Exception
	 */
	private void saveSubtasks(Task task) throws Warning, Exception {

		int tasknum = task.getKey();

		// delete subtasks marked for deletion
		for (Integer id : subTaskIdsToBeDeleted) {
			TaskModel.getReference().deleteSubTask(id.intValue());
			TaskModel.getReference().addLog(
					tasknum,
					Resource.getResourceString("subtask") + " " + id.toString()
							+ " " + Resource.getResourceString("deleted"));
		}

		subTaskIdsToBeDeleted.clear();

		// stop editing
		if (subTaskTable.isEditing())
			subTaskTable.getCellEditor().stopCellEditing();

		// loop through subtask rows
		TableSorter ts = (TableSorter) subTaskTable.getModel();
		for (int r = 0; r < subTaskTable.getRowCount(); r++) {

			// if no description - there is no subtask in this row
			Object desc = ts.getValueAt(r, 2);
			if (desc == null || desc.equals(""))
				continue;

			// get subtask fields
			Integer id = (Integer) ts.getValueAt(r, 1);

			Boolean closed = (Boolean) ts.getValueAt(r, 0);

			Date crd = (Date) ts.getValueAt(r, 3);
			if (crd == null)
				crd = task.getStartDate();
			Date dd = (Date) ts.getValueAt(r, 4);
			Date cd = (Date) ts.getValueAt(r, 7);

			// check if the subtask is being closed
			boolean closing = false;
			if (closed.booleanValue() == true && cd == null) {
				cd = new Date();
				closing = true;
			} else if (closed.booleanValue() == false && cd != null)
				cd = null;

			Subtask s = new Subtask();
			if (id != null)
				s.setKey(id.intValue());
			s.setDescription((String) desc);
			s.setCloseDate(cd);
			s.setDueDate(dd);

			// validate dd - make sure only date and not time is compared
			if (closed.booleanValue() != true && dd != null
					&& task.getDueDate() != null) {
				if (DateUtil.isAfter(dd, task.getDueDate())) {
					String msg = Resource.getResourceString("stdd_warning")
							+ ": " + desc;
					throw new Warning(msg);
				}

			}

			s.setStartDate(crd);

			// validate that subtask does not start before task
			if (closed.booleanValue() != true && crd != null
					&& task.getStartDate() != null) {
				if (DateUtil.isAfter(task.getStartDate(), crd)) {
					String msg = Resource.getResourceString("stsd_warning")
							+ ": " + desc;
					throw new Warning(msg);
				}

			}

			s.setTask(new Integer(tasknum));

			TaskModel.getReference().saveSubTask(s);

			if (id == null || id.intValue() == 0) {
				TaskModel.getReference().addLog(
						tasknum,
						Resource.getResourceString("subtask") + " "
								+ s.getKey() + " "
								+ Resource.getResourceString("created") + ": "
								+ s.getDescription());
			}
			if (closing) {
				TaskModel.getReference().addLog(
						tasknum,
						Resource.getResourceString("subtask") + " "
								+ s.getKey() + " "
								+ Resource.getResourceString("Closed") + ": "
								+ s.getDescription());
			}
		}
	}

	/**
	 * Save the current task
	 */
	private void savetask() {

		// validate description
		if (descriptionText.getText() == null
				|| descriptionText.getText().equals("")) {
			Errmsg.notice(Resource.getResourceString("empty_desc"));
			return;
		}
		try {

			String num = taskIdText.getText();

			// need to use a transaction as we update a number of tables and may
			// need
			// to roll them all back together
			TaskModel.getReference().beginTransaction();

			Task task = TaskModel.getReference().newMR();

			TableSorter ts = (TableSorter) subTaskTable.getModel();
			if (num.equals("NEW")) {

				// ah legacy crap - add any pre-defined subtasks when creating
				// the new task
				// ancient versions of borg allowed something like this
				String prefDefinedTasks[] = TaskModel.getReference()
						.getTaskTypes().getSubTasks(
								(String) taskTypeComboBox.getSelectedItem());
				for (int i = 0; i < prefDefinedTasks.length; i++) {
					Object o[] = { new Boolean(false), null,
							prefDefinedTasks[i], new Date(), null, null };
					ts.addRow(o);
				}
				// set to initial state
				task.setState(TaskModel.getReference().getTaskTypes()
						.getInitialState(
								(String) taskTypeComboBox.getSelectedItem()));
			} else if (num.equals("CLONE")) {
				// set to initial state
				task.setState(TaskModel.getReference().getTaskTypes()
						.getInitialState(
								(String) taskTypeComboBox.getSelectedItem()));
			} else {
				task.setKey(new Integer(num).intValue());
				task.setState((String) statusComboBox.getSelectedItem());
			}

			// fill in the task fields from the screen
			task.setType((String) taskTypeComboBox.getSelectedItem()); // type
			Calendar cal = startDateChooser.getCalendar();
			if (cal == null)
				cal = new GregorianCalendar();
			task.setStartDate(cal.getTime()); // start date
			cal = dueDateChooser.getCalendar();
			if (cal != null)
				task.setDueDate(cal.getTime()); // due date

			// validate due date
			if (task.getDueDate() != null
					&& DateUtil.isAfter(task.getStartDate(), task.getDueDate())) {
				throw new Warning(Resource.getResourceString("sd_dd_warn"));
			}

			Integer pri = (Integer) priorityText.getSelectedItem();
			task.setPriority(pri);
			task.setPersonAssigned(personAssignedText.getText());
			task.setDescription(descriptionText.getText());
			task.setResolution(resolutionText.getText());

			String cat = (String) categoryComboBox.getSelectedItem();
			if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
				task.setCategory(null);
			} else {
				task.setCategory(cat);
			}

			task.setProject(null);
			String proj = (String) projectComboBox.getSelectedItem();
			try {
				task.setProject(getProjectId(proj));
			} catch (Exception e) {
				// no project selected
			}

			if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
				task.setCategory(null);
			} else {
				task.setCategory(cat);
			}

			// do not close task if subtasks are open
			if (TaskModel.isClosed(task)) {
				for (int r = 0; r < subTaskTable.getRowCount(); r++) {
					Boolean closed = (Boolean) ts.getValueAt(r, 0);
					Integer id = (Integer) ts.getValueAt(r, 1);
					if (id == null || id.intValue() == 0)
						continue;
					if (closed.booleanValue() != true) {
						Errmsg.notice(Resource
								.getResourceString("open_subtasks"));
						return;
					}
				}
			}

			// save the task to the DB
			Task orig = TaskModel.getReference().getTask(task.getKey());
			TaskModel.getReference().savetask(task);

			// add various task log records
			if (num.equals("NEW") || num.equals("CLONE")) {
				TaskModel.getReference().addLog(task.getKey(),
						Resource.getResourceString("Task_Created"));
			} else {
				if (orig != null && !orig.getState().equals(task.getState())) {
					TaskModel.getReference().addLog(
							task.getKey(),
							Resource.getResourceString("State_Change") + ": "
									+ orig.getState() + " --> "
									+ task.getState());
				}

				String newd = "null";
				if (task.getDueDate() != null) {
					newd = DateFormat.getDateInstance().format(
							task.getDueDate());
				}

				String oldd = "null";
				if (orig != null && orig.getDueDate() != null)
					oldd = DateFormat.getDateInstance().format(
							orig.getDueDate());
				if (orig != null && !newd.equals(oldd)) {
					TaskModel.getReference().addLog(
							task.getKey(),
							Resource.getResourceString("DueDate") + " "
									+ Resource.getResourceString("Change")
									+ ": " + oldd + " --> " + newd);
				}
			}

			// save subtasks
			saveSubtasks(task);

			// can commit now - task, subtasks, tasklogs
			TaskModel.getReference().commitTransaction();

			// remove this view
			Container p = this.getParent();
			if (p instanceof JViewport) {
				// special case - if we are docked inside a project tree window,
				// then do not remove
				// ourself
				showtask(Action.CHANGE, task);
			} else
				this.close();

			// go back to task view when saving a task
			MultiView.getMainView().setView(ViewType.TASK);

		} catch (Warning w) {
			Errmsg.notice(w.getMessage());
			try {
				TaskModel.getReference().rollbackTransaction();
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
			try {
				TaskModel.getReference().rollbackTransaction();
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
			}

		}

	}

	/**
	 * load the UI from a task
	 * 
	 * @param function
	 *            the action being taken on the task
	 * @param task
	 *            the task
	 * 
	 * @throws Exception
	 * 
	 */
	private void showtask(Action function, Task task) throws Exception {

		TableSorter ts = (TableSorter) subTaskTable.getModel();
		ts.setRowCount(0);

		subTaskIdsToBeDeleted.clear();

		projectComboBox.removeAllItems();
		projectComboBox.addItem("");

		// populate parent project combo box
		Collection<Project> projects = TaskModel.getReference().getProjects();
		if (projects != null) {
			for (Project p : projects) {
				if (p.getStatus().equals(Resource.getResourceString("OPEN")))
					projectComboBox.addItem(getProjectString(p));
			}
		}

		// if we are showing an existing task - fill in the gui fields from it
		if (task != null) {

			// task number
			taskIdText.setText(Integer.toString(task.getKey()));
			taskIdText.setEditable(false);

			// window title - "Item N"
			windowTitle = Resource.getResourceString("Item_") + " "
					+ task.getKey();

			// due date
			GregorianCalendar gc = new GregorianCalendar();
			Date dd = task.getDueDate();
			if (dd != null) {
				gc.setTime(dd);
				dueDateChooser.setCalendar(gc);
			}

			GregorianCalendar gc2 = new GregorianCalendar();
			dd = task.getStartDate();
			if (dd != null)
				gc2.setTime(dd);
			startDateChooser.setCalendar(gc2);

			priorityText.setSelectedItem(task.getPriority());
			personAssignedText.setText(task.getPersonAssigned());

			Date cd = task.getCompletionDate();
			if (cd != null)
				closeDate.setText(DateFormat.getDateInstance(DateFormat.MEDIUM)
						.format(cd));

			int daysleft = TaskModel.daysLeft(task.getDueDate());
			daysLeftText.setText(Integer.toString(daysleft));

			String cat = task.getCategory();
			if (cat != null && !cat.equals("")) {
				categoryComboBox.setSelectedItem(cat);
			} else {
				categoryComboBox.setSelectedIndex(0);
			}

			descriptionText.setText(task.getDescription());
			resolutionText.setText(task.getResolution());

			statusComboBox.addItem(task.getState());
			statusComboBox.setEditable(false);

			// type
			String type = task.getType();
			taskTypeComboBox.addItem(type);
			taskTypeComboBox.setEnabled(false);

			// add subtasks
			Collection<Subtask> subtasks = TaskModel.getReference()
					.getSubTasks(task.getKey());
			for (Subtask subtask : subtasks) {
				Object o[] = {
						subtask.getCloseDate() == null ? new Boolean(false)
								: new Boolean(true),
						new Integer(subtask.getKey()),
						subtask.getDescription(),
						subtask.getStartDate(),
						subtask.getDueDate(),
						subtask.getDueDate() != null ? new Integer(TaskModel
								.daysBetween(subtask.getStartDate(), subtask
										.getDueDate())) : null,
						subtask.getDueDate() != null ? new Integer(TaskModel
								.daysLeft(subtask.getDueDate())) : null,
						subtask.getCloseDate() };

				ts.addRow(o);
			}

			try {
				// load tasklogs
				loadLog(task.getKey());
			} catch (Warning w) {
				// empty
			}

			Integer pid = task.getProject();
			if (pid != null) {
				Project p = TaskModel.getReference().getProject(pid.intValue());
				if (TaskModel.isClosed(p)) {
					projectComboBox.addItem(getProjectString(p));
				}
				projectComboBox.setSelectedItem(getProjectString(p));

			}

			linkPanel.setOwner(task);

		} else // initialize new task
		{

			linkPanel.setOwner(null);

			// task number = NEW
			taskIdText.setText("NEW");
			taskIdText.setEditable(false);

			// title
			windowTitle = Resource.getResourceString("NEW_Item");

			priorityText.setSelectedItem(new Integer(3)); // priority default to
			// 3
			personAssignedText.setText("");
			categoryComboBox.setSelectedIndex(0);
			descriptionText.setText("");
			resolutionText.setText("");

			// add task types to select from - only for new task
			Vector<String> tv = TaskModel.getReference().getTaskTypes()
					.getTaskTypes();
			for (int i = 0; i < tv.size(); i++) {
				taskTypeComboBox.addItem(tv.elementAt(i));
			}

			// if a parent project already set - then initialize some fields
			// from it
			if (parentProject != null) {
				Project p = TaskModel.getReference().getProject(
						parentProject.intValue());
				projectComboBox.setSelectedItem(getProjectString(p));

				String cat = p.getCategory();
				if (cat != null && !cat.equals("")) {
					categoryComboBox.setSelectedItem(cat);
				} else {
					categoryComboBox.setSelectedIndex(0);
				}

				GregorianCalendar gc = new GregorianCalendar();
				Date dd = p.getDueDate();
				if (dd != null) {
					gc.setTime(dd);
					dueDateChooser.setCalendar(gc);
				}

				Date sd = p.getStartDate();
				if (sd != null) {
					gc.setTime(sd);
					startDateChooser.setCalendar(gc);
				}
			}
		}

		// cannot change status on a new task - will always go to initial state
		// depending on type
		if (task == null) {
			statusComboBox.setEnabled(false);
		}

		// cloning takes the fields filled in for an existing task and resets
		// only those
		// that don't apply to the clone
		if (function == Action.CLONE) {
			// need new task number
			taskIdText.setText("CLONE");
			taskIdText.setEditable(false);

			statusComboBox.removeAllItems();
			statusComboBox.addItem(TaskModel.getReference().getTaskTypes()
					.getInitialState(
							taskTypeComboBox.getSelectedItem().toString()));
			statusComboBox.setEnabled(false);

			// reset all subtask id's - but keep the subtasks - which will be
			// saved with new ids
			for (int row = 0; row < subTaskTable.getRowCount(); row++) {
				subTaskTable.setValueAt(null, row, 1);
			}

		}
		// edit existing task
		else if (function == Action.CHANGE && task != null) {

			// determine valid next states based on task type and current
			// state
			String state = task.getState();
			String type = task.getType();
			Collection<String> v = TaskModel.getReference().getTaskTypes()
					.nextStates(type, state);

			// set next state pulldown
			statusComboBox.removeAllItems();
			for (String s : v) {
				statusComboBox.addItem(s);
			}
			statusComboBox.setSelectedItem(state);
			statusComboBox.setEnabled(true);

		}

		// always add an empty row for quick editing
		insertSubtask();
	}
}
