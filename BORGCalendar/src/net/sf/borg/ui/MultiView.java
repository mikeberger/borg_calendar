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
package net.sf.borg.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.ui.util.JTabbedPaneWithCloseIcons;

/**
 * This is the main Borg UI class. It provides the the main borg tabbed window.
 */
public class MultiView extends View {

	private static final long serialVersionUID = 1L;
	
	/** The size of the main borg window. */
	static private PrefName MULTIVIEWSIZE = new PrefName("dayviewsize",
			"-1,-1,-1,-1,Y");

	/**
	 * interface implemented by all UI Modules. The MultiView manages a set of
	 * UI Modules. Each Module is responsible for providing a component to show
	 * in a multiview tab, responding to print requests, and requesting its own
	 * toolbar and menu items
	 * 
	 */
	public static interface Module {

		/**
		 * get the module's name
		 * 
		 * @return the name
		 */
		public String getModuleName();

		/**
		 * get the modules ViewType
		 * 
		 * @return the ViewType
		 */
		public ViewType getViewType();

		/**
		 * get the Component for this Module
		 * 
		 * @return the Component or null if none to show
		 */
		public Component getComponent();

		/**
		 * print the Module
		 */
		public void print();

		/**
		 * called by the parent Multiview to allow the Module to initialize its
		 * toolbar items, menu items, and anything else that must be initalized
		 * before its Module methods can be called
		 * 
		 * @param parent
		 *            the parent MultiView
		 */
		public void initialize(MultiView parent);
	}

	/**
	 * Interface implemented by Calendar Modules that act as modules but also
	 * react to requests to change the shown date
	 * 
	 */
	public static interface CalendarModule extends Module {
		/**
		 * update the module to show a particular date
		 * 
		 * @param cal
		 */
		public void goTo(Calendar cal);
	}

	/** argument values for setView() */
	public enum ViewType {
		DAY, MONTH, WEEK, YEAR, TASK, MEMO, SEARCH, TODO, ADDRESS, CHECKLIST;
	}

	/** The main view singleton */
	static private MultiView mainView = null;

	/**
	 * Get the main view singleton. Make it visible if it is not showing.
	 * 
	 * @return the main view singleton
	 */
	public static MultiView getMainView() {
		if (mainView == null)
			mainView = new MultiView();
		return (mainView);
	}

	/**
	 * toolbar
	 */
	private JToolBar bar = new JToolBar();

	/**
	 * the main menu
	 */
	MainMenu mainMenu = new MainMenu();

	/**
	 * Set of all modules ordered by the Module ordering number
	 */
	private List<Module> moduleSet = new ArrayList<Module>();

	/** The tabs */
	private JTabbedPaneWithCloseIcons tabs_ = null;

	/**
	 * Instantiates a new multi view.
	 */
	private MultiView() {
		super();

		// escape key closes the window
		getLayeredPane().registerKeyboardAction(new ActionListener() {
			@Override
			public final void actionPerformed(ActionEvent e) {
				if (SunTrayIconProxy.hasTrayIcon())
					closeMainwindow();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		// delete key removes tabs
		getLayeredPane().registerKeyboardAction(new ActionListener() {
			@Override
			public final void actionPerformed(ActionEvent e) {
				Component c = getTabs().getSelectedComponent();
				getTabs().remove(c);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		// window close button closes the window
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeMainwindow();
			}
		});

		// create the menu bar
		JMenuBar menubar = mainMenu.getMenuBar();
		menubar.setBorder(new BevelBorder(BevelBorder.RAISED));
		setJMenuBar(menubar);
		getContentPane().setLayout(new GridBagLayout());

		mainMenu.addAction(new ImageIcon(getClass().getResource(
				"/resource/Print16.gif")), Resource.getResourceString("Print"),
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						print();
					}
				}, 0);

		mainMenu.addAction(new ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")), Resource
				.getResourceString("close_tabs"), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				closeTabs();
			}
		}, 1);

		// add the tool bar
		GridBagConstraints cons = new java.awt.GridBagConstraints();
		cons.gridx = 0;
		cons.gridy = 0;
		cons.fill = java.awt.GridBagConstraints.HORIZONTAL;
		cons.weightx = 0.0;
		cons.weighty = 0.0;
		getContentPane().add(getToolBar(), cons);

		// add the tabs
		cons = new java.awt.GridBagConstraints();
		cons.gridx = 0;
		cons.gridy = 1;
		cons.fill = java.awt.GridBagConstraints.BOTH;
		cons.weightx = 1.0;
		cons.weighty = 1.0;
		getContentPane().add(getTabs(), cons);

		setTitle("BORG");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setView(ViewType.MONTH); // start month view
		manageMySize(MULTIVIEWSIZE);
	}

	/**
	 * add a toolbar button
	 * 
	 * @param icon
	 *            the toolbar icon
	 * @param tooltip
	 *            the tooltip for the button
	 * @param action
	 *            the action listener for the button
	 */
	public void addToolBarItem(Icon icon, String tooltip, ActionListener action) {
		JButton button = new JButton(icon);
		button.setToolTipText(tooltip);
		button.addActionListener(action);
		bar.add(button, toolBarInsertIndex);

		mainMenu.addAction(icon, tooltip, action, toolBarInsertIndex++);
	}

	private int toolBarInsertIndex = 0;

	/**
	 * add a help menu item
	 * 
	 * @param icon
	 *            the menu item icon
	 * @param tooltip
	 *            the text for the menu item
	 * @param action
	 *            the action listener for the menu item
	 */
	public void addHelpMenuItem(Icon icon, String tooltip, ActionListener action) {
		mainMenu.addHelpMenuItem(icon, tooltip, action);
	}

	/**
	 * add a plugin sub menu
	 * 
	 * @param menu
	 *            - the sub menu
	 */
	public void addPluginSubMenu(JMenu menu) {
		mainMenu.addPluginSubMenu(menu);
	}

	/**
	 * close the main view. If the system tray icon is active, the program stays
	 * running. If no system tray icon is active, the program shuts down
	 * entirely
	 */
	private void closeMainwindow() {
		if (!SunTrayIconProxy.hasTrayIcon() && this == mainView) {
			UIControl.shutDownUI();
		} else {
			this.dispose();
		}
	}

	/**
	 * Close the currently selected tab.
	 */
	public void closeSelectedTab() {
		tabs_.closeSelectedTab();
	}

	/**
	 * Close all tabs.
	 */
	public void closeTabs() {
		tabs_.closeClosableTabs();
	}

	/** destroy this window */
	@Override
	public void destroy() {
		this.dispose();
		mainView = null;
	}

	/**
	 * Add a plain component as a tab, and do not treat as a module
	 * 
	 * @param c
	 *            the component
	 */
	public void addView(String title, Component c) {
		tabs_.addTab(title, c);
		tabs_.setSelectedIndex(tabs_.getTabCount() - 1);
	}

	/**
	 * get the Module for a given ViewType. called by ui components other than
	 * the module in question that want to request a particular view
	 * 
	 * @param type
	 *            the view type
	 * @return the Module or null
	 */
	private Module getModuleForView(ViewType type) {
		for (Module m : moduleSet) {
			if (type == m.getViewType())
				return m;
		}
		return null;
	}

	/**
	 * Gets the tabs.
	 * 
	 * @return the tabs
	 */
	private JTabbedPane getTabs() {
		if (tabs_ == null) {
			tabs_ = new JTabbedPaneWithCloseIcons();
		}
		return tabs_;
	}

	/**
	 * Gets the tool bar.
	 * 
	 * @return the tool bar
	 */
	private JToolBar getToolBar() {
		bar.setFloatable(false);

		JButton printbut = new JButton(new ImageIcon(getClass().getResource(
				"/resource/Print16.gif")));
		printbut.setToolTipText(Resource.getResourceString("Print"));
		printbut.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				print();
			}
		});
		bar.add(printbut);

		bar.addSeparator();

		JButton clearbut = new JButton(new ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		clearbut.setToolTipText(Resource.getResourceString("close_tabs"));
		clearbut.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				closeTabs();
			}
		});
		bar.add(clearbut);

		return bar;
	}

	/**
	 * have the Calendar Modules show a particular day
	 * 
	 * @param cal
	 *            the day to show
	 */
	public void goTo(Calendar cal) {
		for (Module m : moduleSet) {
			if (m instanceof CalendarModule)
				((CalendarModule) m).goTo(cal);
		}
	}

	/**
	 * add a new module to the multi view
	 * 
	 * @param m
	 *            the module
	 */
	public void addModule(Module m) {
		moduleSet.add(m);
		m.initialize(this);
	}

	/**
	 * prints the currently selected tab if pritning is supported for that tab
	 */
	public void print() {

		Component c = getTabs().getSelectedComponent();
		for (Module m : moduleSet) {
			if (m.getComponent() == c) {
				m.print();
				return;
			}
		}

	}

	/**
	 * refresh the view based on model changes. currently does nothing for this
	 * view.
	 */
	@Override
	public void refresh() {
		// nothing to refresh for this view
	}

	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * Sets the currently selected tab to be a particular view as defined in
	 * ViewType.
	 * 
	 * @param type
	 *            the new view
	 * @return the Component, if any that is now being displayed
	 */
	public Component setView(ViewType type) {

		Module m = getModuleForView(type);
		if (m != null) {
			Component component = m.getComponent();
			if (component != null) {
				if (component instanceof DockableView) {
					
					((DockableView) component).showView();

					if (((DockableView) component).isDocked())
						getTabs().setSelectedComponent(component);

					return component;
				}

				if (!component.isDisplayable()) {
					tabs_.addTab(m.getModuleName(), component);
				}

				getTabs().setSelectedComponent(component);
			}
			return component;
		}
		return null;
	}
}
