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
 
Copyright 2004 by ==Quiet==
 */
package net.sf.borg.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Resource;
import net.sf.borg.model.TaskModel;

public class TaskConfigurator extends View {

	private javax.swing.JPanel jContentPane = null;

	private JPanel jPanel = null;

	private JPanel jPanel1 = null;

	private JPanel jPanel2 = null;

	private JPanel jPanel3 = null;

	private JPanel jPanel4 = null;

	private JList subtasklist = null;

	private JScrollPane jScrollPane = null;

	private JList nextlist = null;

	private JScrollPane jScrollPane1 = null;

	private JScrollPane jScrollPane2 = null;

	private JList typelist = null;

	private JList statelist = null;

	/**
	 * This is the default constructor
	 */
	private static TaskConfigurator singleton = null;

	private JMenuItem jMenuItem = null;

	private JMenuItem jMenuItem1 = null;

	private JMenuItem jMenuItem2 = null;

	private JPopupMenu typemenu = null;

	private JPopupMenu stateMenu = null;

	private JMenuItem jMenuItem3 = null;

	private JMenuItem jMenuItem4 = null;

	private JMenuItem jMenuItem5 = null;

	private JPopupMenu nextstatemenu = null;
	private JMenuItem jMenuItem6 = null;
	private JMenuItem jMenuItem7 = null;
	private JPopupMenu cbmenu = null;
	private JMenuItem jMenuItem8 = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	private JPanel jPanel5 = null;
	private JMenuItem jMenuItem9 = null;
	public static TaskConfigurator getReference() {
		if (singleton == null || !singleton.isShowing())
			singleton = new TaskConfigurator();
		return (singleton);
	}

	private TaskConfigurator() {
		super();
		addModel(TaskModel.getReference());
		initialize();
		load();
		manageMySize(PrefName.TASKCONFVIEWSIZE);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setTitle(net.sf.borg.common.util.Resource.getResourceString("Task_State_Editor"));
		this.setSize(564, 219);
		this.setContentPane(getJContentPane());
	}

	private void load() {
		Object typesel = typelist.getSelectedValue();
		TaskModel tm = TaskModel.getReference();
		Vector types = tm.getTaskTypes().getTaskTypes();
		typelist.setListData(types);
		if( typesel != null )
			typelist.setSelectedValue(typesel,true);

	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new GridBagLayout());
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.ipadx = -580;
			gridBagConstraints11.ipady = 1;
			gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints11.weightx = 1.0D;
			gridBagConstraints11.weighty = 1.0D;
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridy = 1;
			jContentPane.add(getJPanel5(), gridBagConstraints11);
			jContentPane.add(getJPanel4(), gridBagConstraints12);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridLayout gridLayout9 = new GridLayout();
			jPanel = new JPanel();
			jPanel.setLayout(gridLayout9);
			jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
					net.sf.borg.common.util.Resource.getResourceString("Task_Types"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
					null));
			jPanel.setName("jPanel");
			gridLayout9.setRows(1);
			jPanel.add(getJScrollPane1(), null);
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
			GridLayout gridLayout10 = new GridLayout();
			jPanel1 = new JPanel();
			jPanel1.setLayout(gridLayout10);
			jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, net.sf.borg.common.util.Resource.getResourceString("States"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
					null));
			jPanel1.setName("jPanel1");
			gridLayout10.setRows(1);
			jPanel1.add(getJScrollPane2(), null);
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
			GridLayout gridLayout8 = new GridLayout();
			jPanel2 = new JPanel();
			jPanel2.setLayout(gridLayout8);
			jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, net.sf.borg.common.util.Resource.getResourceString("Next_States"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
					null));
			jPanel2.setName("jPanel2");
			gridLayout8.setRows(1);
			jPanel2.add(getJScrollPane(), null);
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
			GridLayout gridLayout7 = new GridLayout();
			jPanel3 = new JPanel();
			jPanel3.setLayout(gridLayout7);
			jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, net.sf.borg.common.util.Resource.getResourceString("SubTasks"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
					null));
			jPanel3.setName("jPanel3");
			gridLayout7.setRows(1);
			jPanel3.add(getSubtasklist(), null);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jPanel4
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jPanel4 = new JPanel();
			jPanel4.setLayout(new FlowLayout());
			jPanel4.setName("jPanel4");
			jPanel4.add(getJButton(), null);
			jPanel4.add(getJButton1(), null);
		}
		return jPanel4;
	}

	/**
	 * This method initializes subtasklist
	 * 
	 * @return javax.swing.JList
	 */
	private JList getSubtasklist() {
		if (subtasklist == null) {
			subtasklist = new JList();
			subtasklist.setVisibleRowCount(5);
			subtasklist.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mousePressed(java.awt.event.MouseEvent e) {
					if( subtasklist.getSelectedIndex() < 0 )
						return;
					if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
						getCbmenu()
								.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
		}
		return subtasklist;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getNextlist());
			jScrollPane
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			jScrollPane
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}

	/**
	 * This method initializes nextlist
	 * 
	 * @return javax.swing.JList
	 */
	private JList getNextlist() {
		if (nextlist == null) {
			nextlist = new JList();
			nextlist.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
						getNextstatemenu()
								.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
		}
		return nextlist;
	}

	/**
	 * This method initializes jScrollPane1
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTypelist());
			jScrollPane1
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane1
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes jScrollPane2
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getStatelist());
			jScrollPane2
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane2
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return jScrollPane2;
	}

	private void stateSelectHandler() {
		String type = (String) typelist.getSelectedValue();
		String state = (String) statelist.getSelectedValue();
		if (state == null)
			return;
		Vector states = TaskModel.getReference().getTaskTypes().nextStates(
				state, type);
		states.remove(state);
		nextlist.setListData(states);
	}

	private void typeSelectHandler() {
		String type = (String) typelist.getSelectedValue();
		if (type == null)
			return;
		Vector states = TaskModel.getReference().getTaskTypes().getStates(type);
		statelist.setListData(states);
		String cbs[] = TaskModel.getReference().getTaskTypes().checkBoxes(type);
		subtasklist.setListData(cbs);
		nextlist.setListData(new Vector());
	}

	/**
	 * This method initializes typelist
	 * 
	 * @return javax.swing.JList
	 */
	private JList getTypelist() {
		if (typelist == null) {
			typelist = new JList();
			typelist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			typelist.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
						getTypemenu()
								.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
			typelist
					.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
						public void valueChanged(
								javax.swing.event.ListSelectionEvent e) {
							typeSelectHandler();
						}
					});
		}
		return typelist;
	}

	/**
	 * This method initializes statelist
	 * 
	 * @return javax.swing.JList
	 */
	private JList getStatelist() {
		if (statelist == null) {
			statelist = new JList();
			statelist.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
						getStateMenu()
								.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
			statelist.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
						public void valueChanged(
								javax.swing.event.ListSelectionEvent e) {
							stateSelectHandler();
						}
					});
		}
		return statelist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.View#refresh()
	 */
	public void refresh() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.View#destroy()
	 */
	public void destroy() {
		this.dispose();

	}

	/**
	 * This method initializes jMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem() {
		if (jMenuItem == null) {
			jMenuItem = new JMenuItem();
			jMenuItem.setText(net.sf.borg.common.util.Resource.getResourceString("Add_Type"));
			jMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String newtype = JOptionPane
							.showInputDialog(net.sf.borg.common.util.Resource.getResourceString("New_Task_Type"));
					if (newtype == null)
						return;
					TaskModel.getReference().getTaskTypes().addType(newtype);
					load();
				}
			});
		}
		return jMenuItem;
	}

	/**
	 * This method initializes jMenuItem1
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem1() {
		if (jMenuItem1 == null) {
			jMenuItem1 = new JMenuItem();
			jMenuItem1.setText(net.sf.borg.common.util.Resource.getResourceString("Rename_Type"));
			jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if( typelist.getSelectedIndex() < 0 )
					{
						JOptionPane.showMessageDialog(null,net.sf.borg.common.util.Resource.getResourceString("Please_select_a_type"));
						return;
					}
					String newtype = JOptionPane
							.showInputDialog(net.sf.borg.common.util.Resource.getResourceString("New_Task_Type"));
					if (newtype == null)
						return;
					TaskModel.getReference().getTaskTypes().changeType(
							(String) typelist.getSelectedValue(), newtype);
					load();
				}
			});
		}
		return jMenuItem1;
	}

	/**
	 * This method initializes jMenuItem2
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem2() {
		if (jMenuItem2 == null) {
			jMenuItem2 = new JMenuItem();
			jMenuItem2.setText(net.sf.borg.common.util.Resource.getResourceString("Delete_Type"));
			jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if( typelist.getSelectedIndex() < 0 )
					{
						JOptionPane.showMessageDialog(null,net.sf.borg.common.util.Resource.getResourceString("Please_select_a_type"));
						return;
					}
					int ret = JOptionPane.showConfirmDialog(null,
							net.sf.borg.common.util.Resource.getResourceString("Really_Delete_") + typelist.getSelectedValue(),
							net.sf.borg.common.util.Resource.getResourceString("Confirm_Delete"), JOptionPane.OK_CANCEL_OPTION);
					if (ret != JOptionPane.OK_OPTION)
						return;
					TaskModel.getReference().getTaskTypes().deleteType(
							(String) typelist.getSelectedValue());
					load();
				}
			});
		}
		return jMenuItem2;
	}

	/**
	 * This method initializes typemenu
	 * 
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getTypemenu() {
		if (typemenu == null) {
			typemenu = new JPopupMenu();
			typemenu.add(getJMenuItem());
			typemenu.add(getJMenuItem1());
			typemenu.add(getJMenuItem2());
		}
		return typemenu;
	}

	/**
	 * This method initializes stateMenu
	 * 
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getStateMenu() {
		if (stateMenu == null) {
			stateMenu = new JPopupMenu();
			stateMenu.add(getJMenuItem3());
			stateMenu.add(getJMenuItem4());
			stateMenu.add(getJMenuItem5());
		}
		return stateMenu;
	}

	/**
	 * This method initializes jMenuItem3
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem3() {
		if (jMenuItem3 == null) {
			jMenuItem3 = new JMenuItem();
			jMenuItem3.setText(net.sf.borg.common.util.Resource.getResourceString("Add_State"));
			jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if( typelist.getSelectedIndex() < 0 )
					{
						JOptionPane.showMessageDialog(null,net.sf.borg.common.util.Resource.getResourceString("Please_select_a_type"));
						return;
					}
					String newstate = JOptionPane.showInputDialog(net.sf.borg.common.util.Resource.getResourceString("New_State"));
					if (newstate == null)
						return;
					TaskModel.getReference().getTaskTypes().addState(
							(String) typelist.getSelectedValue(),
							newstate);
					load();
				}
			});
		}
		return jMenuItem3;
	}

	/**
	 * This method initializes jMenuItem4
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem4() {
		if (jMenuItem4 == null) {
			jMenuItem4 = new JMenuItem();
			jMenuItem4.setText(net.sf.borg.common.util.Resource.getResourceString("Rename_State"));
			jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {					
					if( statelist.getSelectedIndex() < 0 )
					{
						JOptionPane.showMessageDialog(null,net.sf.borg.common.util.Resource.getResourceString("Please_select_a_state"));
						return;
					}
					String newstate = JOptionPane.showInputDialog(net.sf.borg.common.util.Resource.getResourceString("New_State"));
					if (newstate == null)
						return;
					TaskModel.getReference().getTaskTypes().changeState(
							(String) typelist.getSelectedValue(),
							(String) statelist.getSelectedValue(), newstate);
					load();
				}
			});
		}
		return jMenuItem4;
	}

	/**
	 * This method initializes jMenuItem5
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem5() {
		if (jMenuItem5 == null) {
			jMenuItem5 = new JMenuItem();
			jMenuItem5.setText(net.sf.borg.common.util.Resource.getResourceString("Delete_State"));
			jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if( statelist.getSelectedIndex() < 0 )
					{
						JOptionPane.showMessageDialog(null,net.sf.borg.common.util.Resource.getResourceString("Please_select_a_state"));
						return;
					}
					int ret = JOptionPane.showConfirmDialog(null,
							net.sf.borg.common.util.Resource.getResourceString("Really_Delete_") + statelist.getSelectedValue(),
							net.sf.borg.common.util.Resource.getResourceString("Confirm_Delete"), JOptionPane.OK_CANCEL_OPTION);
					if (ret != JOptionPane.OK_OPTION)
						return;
					TaskModel.getReference().getTaskTypes().deleteState(
							(String) typelist.getSelectedValue(),
							(String) statelist.getSelectedValue());
					load();
				}
			});
		}
		return jMenuItem5;
	}
	/**
	 * This method initializes nextstatemenu	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */    
	private JPopupMenu getNextstatemenu() {
		if (nextstatemenu == null) {
			nextstatemenu = new JPopupMenu();
			nextstatemenu.add(getJMenuItem6());
			nextstatemenu.add(getJMenuItem7());
		}
		return nextstatemenu;
	}
	/**
	 * This method initializes jMenuItem6	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getJMenuItem6() {
		if (jMenuItem6 == null) {
			jMenuItem6 = new JMenuItem();
			jMenuItem6.setText(net.sf.borg.common.util.Resource.getResourceString("Add"));
			jMenuItem6.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if( statelist.getSelectedIndex() < 0 )
					{
						JOptionPane.showMessageDialog(null,net.sf.borg.common.util.Resource.getResourceString("Please_select_a_state"));
						return;
					}
					Vector states = TaskModel.getReference().getTaskTypes().getStates((String)typelist.getSelectedValue());
					Object sarray[] = states.toArray();
					String ns = (String) JOptionPane.showInputDialog(null,net.sf.borg.common.util.Resource.getResourceString("Select_next_state"), 
							net.sf.borg.common.util.Resource.getResourceString("Select_next_state"), JOptionPane.QUESTION_MESSAGE,
							null, sarray, sarray[0]);
					if( ns == null ) return;
					TaskModel.getReference().getTaskTypes().addNextState(
							(String)typelist.getSelectedValue(),
							(String)statelist.getSelectedValue(),
							ns);	
					load();
				}
			});
		}
		return jMenuItem6;
	}
	/**
	 * This method initializes jMenuItem7	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getJMenuItem7() {
		if (jMenuItem7 == null) {
			jMenuItem7 = new JMenuItem();
			jMenuItem7.setText(net.sf.borg.common.util.Resource.getResourceString("Delete"));
			jMenuItem7.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) { 
					if( nextlist.getSelectedIndex() < 0 )
					{
						JOptionPane.showMessageDialog(null,net.sf.borg.common.util.Resource.getResourceString("Please_select_a_state"));
						return;
					}
					String ns = (String) nextlist.getSelectedValue();
					TaskModel.getReference().getTaskTypes().deleteNextState(
							(String)typelist.getSelectedValue(),
							(String)statelist.getSelectedValue(),
							ns);	
					load();
				}
			});
		}
		return jMenuItem7;
	}
	/**
	 * This method initializes cbmenu	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */    
	private JPopupMenu getCbmenu() {
		if (cbmenu == null) {
			cbmenu = new JPopupMenu();
			cbmenu.add(getJMenuItem8());
			cbmenu.add(getJMenuItem9());
		}
		return cbmenu;
	}
	/**
	 * This method initializes jMenuItem8	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getJMenuItem8() {
		if (jMenuItem8 == null) {
			jMenuItem8 = new JMenuItem();
			jMenuItem8.setText(net.sf.borg.common.util.Resource.getResourceString("Change"));
			jMenuItem8.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {

					String cb = JOptionPane.showInputDialog(net.sf.borg.common.util.Resource.getResourceString("New_Subtask_Value"));
					if (cb == null)
						return;
					TaskModel.getReference().getTaskTypes().changeCB(
							(String) typelist.getSelectedValue(), subtasklist.getSelectedIndex(), cb);
					load();
				}
			});
		}
		return jMenuItem8;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText(net.sf.borg.common.util.Resource.getResourceString("Save"));
			jButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) { 
					try{
						TaskModel.getReference().saveTaskTypes();
					}
					catch( Exception ex)
					{
						Errmsg.errmsg(ex);
					}
				}
			});
		}
		return jButton;
	}
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText(net.sf.borg.common.util.Resource.getResourceString("Dismiss"));
			jButton1.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					TaskConfigurator.getReference().dispose();
				}
			});
		}
		return jButton1;
	}
	/**
	 * This method initializes jPanel5	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			GridLayout gridLayout81 = new GridLayout();
			jPanel5 = new JPanel();
			jPanel5.setLayout(gridLayout81);
			gridLayout81.setRows(1);
			jPanel5.add(getJPanel(), null);
			jPanel5.add(getJPanel1(), null);
			jPanel5.add(getJPanel2(), null);
			jPanel5.add(getJPanel3(), null);
		}
		return jPanel5;
	}
	/**
	 * This method initializes jMenuItem9	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getJMenuItem9() {
		if (jMenuItem9 == null) {
			jMenuItem9 = new JMenuItem();
			jMenuItem9.setText(Resource.getResourceString("Delete"));
			jMenuItem9.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					if( subtasklist.getSelectedIndex() < 0) return;
					TaskModel.getReference().getTaskTypes().changeCB(
							(String) typelist.getSelectedValue(), subtasklist.getSelectedIndex(), null);
					load();
				}
			});
		}
		return jMenuItem9;
	}
         } //  @jve:decl-index=0:visual-constraint="10,10"
