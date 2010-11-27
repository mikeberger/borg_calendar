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
 
Copyright 2004 by Mike Berger
 */
package net.sf.borg.ui.task;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.TaskTypes;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.View;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * UI for editing task types, their state transitions, and pre-defined subtasks
 */
public class TaskConfigurator extends View {

	private static final long serialVersionUID = 1L;
	
	/** size of the task config window */
	static private PrefName TASKCONFVIEWSIZE = new PrefName("taskconfviewsize",
			"-1,-1,-1,-1,N");
	
	/**
	 * Renders the initial task state in a different color
	 */
	private class TypeListRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 1L;

		public TypeListRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.toString());

			// get initial state for the selected type
			String init = "";
			if (typeList.getSelectedValue() != null) {
				init = taskTypes.getInitialState(typeList.getSelectedValue()
						.toString());
			}

			// highlist this state if it is the initial state
			if (value.toString().equals(init)) {
				setBackground(isSelected ? Color.red : Color.white);
				setForeground(isSelected ? Color.white : Color.red);
			} else {
				// return default component
				return defaultRenderer.getListCellRendererComponent(list,
						value, index, isSelected, cellHasFocus);
			}
			return this;
		}
	}

	/** The default table cell Renderer */
	static private ListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	/** The singleton */
	private static TaskConfigurator singleton = null;

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 * 
	 * @throws Exception
	 */
	public static TaskConfigurator getReference() throws Exception {
		if (singleton == null || !singleton.isShowing())
			singleton = new TaskConfigurator();
		return (singleton);
	}

	/** The sub task menu. */
	private JPopupMenu subTaskMenu = null;

	/** The next state list. */
	private JList nextStateList = null;

	/** The next state menu. */
	private JPopupMenu nextStateMenu = null;

	/** The state list. */
	private JList stateList = null;

	/** The state menu. */
	private JPopupMenu stateMenu = null;

	/** The sub task list. */
	private JList subTaskList = null;

	/** The task types. */
	private TaskTypes taskTypes;

	/** The type list. */
	private JList typeList = null;

	/** The typeMenu. */
	private JPopupMenu typeMenu = null;

	/**
	 * constructor
	 * 
	 * @throws Exception
	 */
	private TaskConfigurator() throws Exception {
		super();

		initialize();

		taskTypes = TaskModel.getReference().getTaskTypes().copy();

		refresh();

		manageMySize(TASKCONFVIEWSIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.View#destroy()
	 */
	@Override
	public void destroy() {
		this.dispose();

	}

	/**
	 * Gets the sub task menu.
	 * 
	 * @return the sub task menu
	 */
	private JPopupMenu getSubTaskMenu() {

		if (subTaskMenu == null) {
			subTaskMenu = new JPopupMenu();

			JMenuItem addItem = new JMenuItem();
			addItem.setText(Resource.getResourceString("Add"));
			addItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String subtask = JOptionPane
							.showInputDialog(net.sf.borg.common.Resource
									.getResourceString("New_Subtask_Value"));
					if (subtask == null)
						return;
		
					taskTypes.addSubtask((String) typeList.getSelectedValue(),
							subtask);
					refresh();
				}
			});

			subTaskMenu.add(addItem);

			JMenuItem changeSubtaskItem = new JMenuItem();
			changeSubtaskItem.setText(net.sf.borg.common.Resource
					.getResourceString("Change"));
			changeSubtaskItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					String subtask = JOptionPane
							.showInputDialog(net.sf.borg.common.Resource
									.getResourceString("New_Subtask_Value"));
					if (subtask == null)
						return;
					// set subtask value
					taskTypes.deleteSubtask((String) typeList
							.getSelectedValue(), (String) subTaskList
							.getSelectedValue());
					taskTypes.addSubtask((String) typeList.getSelectedValue(),
							subtask);
					refresh();
				}
			});

			subTaskMenu.add(changeSubtaskItem);

			JMenuItem deleteItem = new JMenuItem();
			deleteItem.setText(Resource.getResourceString("Delete"));
			deleteItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (subTaskList.getSelectedIndex() < 0)
						return;
					// set subtask to null
					taskTypes.deleteSubtask((String) typeList
							.getSelectedValue(), (String) subTaskList
							.getSelectedValue());
					refresh();
				}
			});

			subTaskMenu.add(deleteItem);
		}
		return subTaskMenu;
	}

	/**
	 * Gets the next state menu.
	 * 
	 * @return the next state menu
	 */
	private JPopupMenu getNextStateMenu() {
		if (nextStateMenu == null) {
			nextStateMenu = new JPopupMenu();

			JMenuItem addItem = new JMenuItem();
			addItem.setText(net.sf.borg.common.Resource
					.getResourceString("Add"));
			addItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (stateList.getSelectedIndex() < 0) {
						JOptionPane
								.showMessageDialog(
										null,
										net.sf.borg.common.Resource
												.getResourceString("Please_select_a_state"));
						return;
					}

					// prompt for selection of next state from a list
					Collection<String> states = taskTypes
							.getStates((String) typeList.getSelectedValue());
					Object sarray[] = states.toArray();
					String ns = (String) JOptionPane.showInputDialog(null,
							net.sf.borg.common.Resource
									.getResourceString("Select_next_state"),
							net.sf.borg.common.Resource
									.getResourceString("Select_next_state"),
							JOptionPane.QUESTION_MESSAGE, null, sarray,
							sarray[0]);
					if (ns == null)
						return;

					// add next state
					taskTypes.addNextState(
							(String) typeList.getSelectedValue(),
							(String) stateList.getSelectedValue(), ns);
					refresh();
				}
			});

			nextStateMenu.add(addItem);

			JMenuItem deleteItem = new JMenuItem();
			deleteItem.setText(net.sf.borg.common.Resource
					.getResourceString("Delete"));
			deleteItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (nextStateList.getSelectedIndex() < 0) {
						JOptionPane
								.showMessageDialog(
										null,
										net.sf.borg.common.Resource
												.getResourceString("Please_select_a_state"));
						return;
					}

					// delete next state
					String ns = (String) nextStateList.getSelectedValue();
					taskTypes.deleteNextState((String) typeList
							.getSelectedValue(), (String) stateList
							.getSelectedValue(), ns);
					refresh();
				}
			});

			nextStateMenu.add(deleteItem);
		}
		return nextStateMenu;
	}

	/**
	 * Gets the state menu.
	 * 
	 * @return the state menu
	 */
	private JPopupMenu getStateMenu() {
		if (stateMenu == null) {
			stateMenu = new JPopupMenu();

			JMenuItem addItem = new JMenuItem();
			addItem.setText(net.sf.borg.common.Resource
					.getResourceString("Add_State"));
			addItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (typeList.getSelectedIndex() < 0) {
						JOptionPane
								.showMessageDialog(
										null,
										net.sf.borg.common.Resource
												.getResourceString("Please_select_a_type"));
						return;
					}
					String newstate = JOptionPane
							.showInputDialog(net.sf.borg.common.Resource
									.getResourceString("New_State"));
					if (newstate == null)
						return;

					// add a new state
					taskTypes.addState((String) typeList.getSelectedValue(),
							newstate);
					refresh();
				}
			});

			stateMenu.add(addItem);

			JMenuItem renameItem = new JMenuItem();
			renameItem.setText(net.sf.borg.common.Resource
					.getResourceString("Rename_State"));
			renameItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (stateList.getSelectedIndex() < 0) {
						JOptionPane
								.showMessageDialog(
										null,
										net.sf.borg.common.Resource
												.getResourceString("Please_select_a_state"));
						return;
					}
					String newstate = JOptionPane
							.showInputDialog(net.sf.borg.common.Resource
									.getResourceString("New_State"));
					if (newstate == null)
						return;

					// rename state
					taskTypes.changeState((String) typeList.getSelectedValue(),
							(String) stateList.getSelectedValue(), newstate);
					refresh();
				}
			});

			stateMenu.add(renameItem);

			JMenuItem deleteItem = new JMenuItem();
			deleteItem.setText(net.sf.borg.common.Resource
					.getResourceString("Delete_State"));
			deleteItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (stateList.getSelectedIndex() < 0) {
						JOptionPane
								.showMessageDialog(
										null,
										net.sf.borg.common.Resource
												.getResourceString("Please_select_a_state"));
						return;
					}

					// confirm delete
					int ret = JOptionPane.showConfirmDialog(null,
							net.sf.borg.common.Resource
									.getResourceString("Really_Delete_")
									+ " " + stateList.getSelectedValue(),
							net.sf.borg.common.Resource
									.getResourceString("Confirm_Delete"),
							JOptionPane.OK_CANCEL_OPTION);
					if (ret != JOptionPane.OK_OPTION)
						return;
					// delete state
					taskTypes.deleteState((String) typeList.getSelectedValue(),
							(String) stateList.getSelectedValue());
					refresh();
				}
			});

			stateMenu.add(deleteItem);
		}
		return stateMenu;
	}

	/**
	 * Gets the type menu.
	 * 
	 * @return the type menu
	 */
	private JPopupMenu getTypeMenu() {
		if (typeMenu == null) {
			typeMenu = new JPopupMenu();

			JMenuItem addItem = new JMenuItem();
			addItem.setText(net.sf.borg.common.Resource
					.getResourceString("Add_Type"));
			addItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String newtype = JOptionPane
							.showInputDialog(net.sf.borg.common.Resource
									.getResourceString("New_Task_Type"));
					if (newtype == null)
						return;
					// add a new type
					taskTypes.addType(newtype);
					refresh();
				}
			});

			typeMenu.add(addItem);

			JMenuItem renameItem = new JMenuItem();
			renameItem.setText(net.sf.borg.common.Resource
					.getResourceString("Rename_Type"));
			renameItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (typeList.getSelectedIndex() < 0) {
						JOptionPane
								.showMessageDialog(
										null,
										net.sf.borg.common.Resource
												.getResourceString("Please_select_a_type"));
						return;
					}
					String newtype = JOptionPane
							.showInputDialog(net.sf.borg.common.Resource
									.getResourceString("New_Task_Type"));
					if (newtype == null)
						return;
					// rename type
					taskTypes.changeType((String) typeList.getSelectedValue(),
							newtype);
					refresh();
				}
			});

			typeMenu.add(renameItem);

			JMenuItem deleteItem = new JMenuItem();
			deleteItem.setText(net.sf.borg.common.Resource
					.getResourceString("Delete_Type"));
			deleteItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (typeList.getSelectedIndex() < 0) {
						JOptionPane
								.showMessageDialog(
										null,
										net.sf.borg.common.Resource
												.getResourceString("Please_select_a_type"));
						return;
					}
					// confirm delete
					int ret = JOptionPane.showConfirmDialog(null,
							net.sf.borg.common.Resource
									.getResourceString("Really_Delete_")
									+ " " + typeList.getSelectedValue(),
							net.sf.borg.common.Resource
									.getResourceString("Confirm_Delete"),
							JOptionPane.OK_CANCEL_OPTION);
					if (ret != JOptionPane.OK_OPTION)
						return;
					// delete type
					taskTypes.deleteType((String) typeList.getSelectedValue());
					refresh();
				}
			});

			typeMenu.add(deleteItem);

			JMenuItem initialStateItem = new JMenuItem();
			initialStateItem.setText(net.sf.borg.common.Resource
					.getResourceString("Set_Initial_State"));
			initialStateItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (typeList.getSelectedIndex() < 0) {
						JOptionPane
								.showMessageDialog(
										null,
										net.sf.borg.common.Resource
												.getResourceString("Please_select_a_type"));
						return;
					}

					// prompt for selection of initial state from a list
					Collection<String> states = taskTypes
							.getStates((String) typeList.getSelectedValue());
					Object sarray[] = states.toArray();
					String ns = (String) JOptionPane.showInputDialog(null,
							net.sf.borg.common.Resource
									.getResourceString("Select_initial_state"),
							net.sf.borg.common.Resource
									.getResourceString("Select_initial_state"),
							JOptionPane.QUESTION_MESSAGE, null, sarray,
							sarray[0]);
					if (ns == null)
						return;

					// set initial state
					taskTypes.setInitialState((String) typeList
							.getSelectedValue(), ns);
					refresh();
				}
			});

			typeMenu.add(initialStateItem);
		}
		return typeMenu;
	}

	/**
	 * initialize the UI
	 * 
	 */
	private void initialize() {

		this.setTitle(net.sf.borg.common.Resource
				.getResourceString("Task_State_Editor"));
		this.setSize(564, 219);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());

		GridLayout gridLayout = new GridLayout();
		gridLayout.setRows(1);

		/*
		 * list containing panel
		 */
		JPanel listContainerPanel = new JPanel();
		listContainerPanel.setLayout(gridLayout);

		GridLayout gridLayout9 = new GridLayout();

		/*
		 * task type panel
		 */
		JPanel taskTypePanel = new JPanel();
		taskTypePanel.setLayout(gridLayout9);
		taskTypePanel.setBorder(BorderFactory.createTitledBorder(null,
				net.sf.borg.common.Resource.getResourceString("Task_Types"),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, null, null));
		gridLayout9.setRows(1);

		JScrollPane taskTypeScroll = new JScrollPane();

		typeList = new JList();
		typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		typeList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					getTypeMenu().show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		typeList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				typeSelectHandler();
			}
		});

		taskTypeScroll.setViewportView(typeList);
		taskTypeScroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		taskTypeScroll
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		taskTypePanel.add(taskTypeScroll, null);

		listContainerPanel.add(taskTypePanel, null);

		/*
		 * task states panel
		 */
		GridLayout gridLayout10 = new GridLayout();
		JPanel taskStatesPanel = new JPanel();
		taskStatesPanel.setLayout(gridLayout10);
		taskStatesPanel.setBorder(BorderFactory.createTitledBorder(null,
				net.sf.borg.common.Resource.getResourceString("States"),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, null, null));
		gridLayout10.setRows(1);

		JScrollPane stateScroll = new JScrollPane();

		stateList = new JList();
		stateList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					getStateMenu().show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		stateList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				stateSelectHandler();
			}
		});
		stateList.setCellRenderer(new TypeListRenderer());

		stateScroll.setViewportView(stateList);
		stateScroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		stateScroll
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		

		taskStatesPanel.add(stateScroll, null);
		
		listContainerPanel.add(taskStatesPanel, null);

		/*
		 * next state panel
		 */
		GridLayout gridLayout8 = new GridLayout();
		JPanel nextStatePanel = new JPanel();
		nextStatePanel.setLayout(gridLayout8);
		nextStatePanel.setBorder(BorderFactory.createTitledBorder(null,
				net.sf.borg.common.Resource.getResourceString("Next_States"),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, null, null));
		gridLayout8.setRows(1);

		JScrollPane nextStateScroll = new JScrollPane();

		nextStateList = new JList();
		nextStateList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					getNextStateMenu().show(e.getComponent(), e.getX(),
							e.getY());
				}
			}
		});

		nextStateScroll.setViewportView(nextStateList);
		nextStateScroll
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		nextStateScroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		nextStatePanel.add(nextStateScroll, null);

		listContainerPanel.add(nextStatePanel, null);

		/*
		 * sub task panel
		 */
		GridLayout gridLayout7 = new GridLayout();
		JPanel subTaskPanel = new JPanel();
		subTaskPanel.setLayout(gridLayout7);
		subTaskPanel.setBorder(BorderFactory.createTitledBorder(null,
				net.sf.borg.common.Resource.getResourceString("SubTasks"),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, null, null));
		gridLayout7.setRows(1);

		subTaskList = new JList();
		subTaskList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					getSubTaskMenu().show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		
		JScrollPane subTaskScroll = new JScrollPane();
		subTaskScroll.setViewportView(subTaskList);
		subTaskScroll
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		subTaskScroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		subTaskPanel.add(subTaskScroll, null);

		listContainerPanel.add(subTaskPanel, null);

		topPanel.add(listContainerPanel, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		/*
		 * button panel
		 */
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton saveButton = new JButton();
		ResourceHelper.setText(saveButton, "Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					TaskModel.getReference().saveTaskTypes(taskTypes);
					TaskConfigurator.getReference().dispose();
				} catch (Exception ex) {
					Errmsg.errmsg(ex);
				}
			}
		});

		buttonPanel.add(saveButton, null);

		JButton dismissButton = new JButton();
		ResourceHelper.setText(dismissButton, "Dismiss");
		dismissButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					TaskConfigurator.getReference().dispose();
				} catch (Exception e1) {
				  // empty
				}
			}
		});
		setDismissButton(dismissButton);

		buttonPanel.add(dismissButton, null);

		topPanel.add(buttonPanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.NONE, 0.0, 0.0));

		this.setContentPane(topPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.View#refresh()
	 */
	@Override
	public void refresh() {
		/*
		 * reload the task types from the model reset the selected type if the
		 * user had one selected this selection triggers further UI updates
		 */
		Object typesel = typeList.getSelectedValue();
		Vector<String> types = taskTypes.getTaskTypes();
		typeList.setListData(types);
		if (typesel != null)
			typeList.setSelectedValue(typesel, true);
	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * display next states when user selects a state
	 */
	private void stateSelectHandler() {
		String type = (String) typeList.getSelectedValue();
		String state = (String) stateList.getSelectedValue();
		if (state == null)
			return;
		Collection<String> states = taskTypes.nextStates(type, state);
		states.remove(state);
		nextStateList.setListData(states.toArray());
	}

	/**
	 * when the user selected a type, display its states, next states, and
	 * subtasks
	 */
	private void typeSelectHandler() {
		String type = (String) typeList.getSelectedValue();
		if (type == null)
			return;
		Collection<String> states = taskTypes.getStates(type);
		stateList.setListData(states.toArray());
		String cbs[] = taskTypes.getSubTasks(type);
		subTaskList.setListData(cbs);
		nextStateList.setListData(new Vector<String>());
	}
}
