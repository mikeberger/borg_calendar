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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.PrintHelper;
import net.sf.borg.common.Resource;
import net.sf.borg.control.Borg;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Project;
import net.sf.borg.ui.calendar.CalendarPanel;
import net.sf.borg.ui.calendar.DayPanel;
import net.sf.borg.ui.calendar.WeekPanel;
import net.sf.borg.ui.memo.MemoPanel;
import net.sf.borg.ui.task.ProjectPanel;
import net.sf.borg.ui.task.TaskListPanel;
import net.sf.borg.ui.util.DateDialog;
import net.sf.borg.ui.util.JTabbedPaneWithCloseIcons;

// weekView handles the printing of a single week
public class MultiView extends View implements Navigator {

    static private MultiView mainView = null;

    private Calendar cal_ = new GregorianCalendar();

    private JTabbedPaneWithCloseIcons tabs_ = null;

    private DayPanel dayPanel = null;

    private WeekPanel wkPanel = null;

    private CalendarPanel calPanel = null;

    private MemoPanel memoPanel = null;

    static public final int DAY = 1;

    static public final int MONTH = 2;

    static public final int WEEK = 3;

    public static MultiView getMainView() {
	if (mainView == null)
	    mainView = new MultiView();
	else if (!mainView.isShowing())
	    mainView.setVisible(true);
	return (mainView);
    }

    public static void openNewView() {
	new MultiView().setVisible(true);
    }

    private MultiView() {
	super();
	addModel(AppointmentModel.getReference());
	addModel(TaskModel.getReference());
	addModel(AddressModel.getReference());
	addModel(MemoModel.getReference());
	getLayeredPane().registerKeyboardAction(new ActionListener() {
	    public final void actionPerformed(ActionEvent e) {
		if (Borg.getReference().hasTrayIcon())
		    exit();
	    }
	}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

	addWindowListener(new java.awt.event.WindowAdapter() {
	    public void windowClosing(java.awt.event.WindowEvent evt) {
		exit();
	    }
	});

	// for the preview, create a JFrame with the preview panel and print
	// menubar
	JMenuBar menubar = new MainMenu(this).getMenuBar();

	menubar.setBorder(new BevelBorder(BevelBorder.RAISED));

	setJMenuBar(menubar);
	getContentPane().setLayout(new GridBagLayout());
	GridBagConstraints cons = new java.awt.GridBagConstraints();
	cons.gridx = 0;
	cons.gridy = 0;
	cons.fill = java.awt.GridBagConstraints.BOTH;
	cons.weightx = 1.0;
	cons.weighty = 1.0;

	getContentPane().add(getTabs(), cons);

	cons = new java.awt.GridBagConstraints();
	cons.gridx = 0;
	cons.gridy = 1;
	cons.fill = java.awt.GridBagConstraints.BOTH;
	cons.weightx = 0.0;
	cons.weighty = 0.0;

	getContentPane().add(getNavPanel(), cons);

	setTitle("BORG");
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	pack();
	setVisible(true);

	manageMySize(PrefName.DAYVIEWSIZE);
    }

    public int getMonth() {
	return cal_.get(Calendar.MONTH);
    }

    public int getYear() {
	return cal_.get(Calendar.YEAR);
    }

    public void setView(int type) {
	if (type == DAY) {
	    if (!dayPanel.isDisplayable()) {
		tabs_.addTab(Resource.getPlainResourceString("Day_View"), dayPanel);
	    }
	    getTabs().setSelectedComponent(dayPanel);
	} else if (type == WEEK) {
	    if (!dayPanel.isDisplayable()) {
		tabs_.addTab(Resource.getPlainResourceString("Week_View"), wkPanel);
	    }
	    getTabs().setSelectedComponent(wkPanel);
	} else if (type == MONTH) {
	    getTabs().setSelectedComponent(calPanel);
	}
    }

    public void showMemos()
    {
	if (memoPanel != null && !memoPanel.isDisplayable()) {
	    tabs_.addTab(Resource.getPlainResourceString("Memos"), memoPanel);
	}
	if (memoPanel != null)
	    getTabs().setSelectedComponent(memoPanel);
    }
    
    public void showTasks()
    {
	if (projPanel != null && !projPanel.isDisplayable()) {
	    tabs_.addTab(Resource.getPlainResourceString("projects"), projPanel);
	}
	if (projPanel != null)
	    getTabs().setSelectedComponent(projPanel);
	
	if (taskPanel != null && !taskPanel.isDisplayable()) {
	    tabs_.addTab(Resource.getPlainResourceString("tasks"), taskPanel);
	}
	if (taskPanel != null)
	    getTabs().setSelectedComponent(taskPanel);
    }

    private void tabChanged() {
	Component c = tabs_.getSelectedComponent();
	if (c instanceof Navigator)
	    navPanel.setVisible(true);
	else
	    navPanel.setVisible(false);

    }

    private TaskListPanel taskPanel = null;

    private ProjectPanel projPanel = null;

    private JTabbedPane getTabs() {
	if (tabs_ == null) {

	    tabs_ = new JTabbedPaneWithCloseIcons();

	    calPanel = new CalendarPanel(this, cal_.get(Calendar.MONTH), cal_.get(Calendar.YEAR));
	    dayPanel = new DayPanel(cal_.get(Calendar.MONTH), cal_.get(Calendar.YEAR), cal_.get(Calendar.DATE));
	    wkPanel = new WeekPanel(cal_.get(Calendar.MONTH), cal_.get(Calendar.YEAR), cal_.get(Calendar.DATE));

	    if (MemoModel.getReference().hasMemos())
		memoPanel = new MemoPanel();

	    taskPanel = new TaskListPanel();

	    if (TaskModel.getReference().hasSubTasks())
		projPanel = new ProjectPanel();

	    tabs_.addTab(Resource.getPlainResourceString("Month_View"), null, calPanel);
	    tabs_.addTab(Resource.getPlainResourceString("Week_View"), wkPanel);
	    tabs_.addTab(Resource.getPlainResourceString("Day_View"), dayPanel);

	    if (!TaskModel.getReference().hasSubTasks()) {
		JTextArea ta = new JTextArea();
		ta.setText(Resource.getPlainResourceString("ProjectsNotSupported"));
		ta.setEditable(false);
		tabs_.addTab(Resource.getPlainResourceString("projects"),  ta);
	    } else
		tabs_.addTab(Resource.getPlainResourceString("projects"), projPanel);

	    tabs_.addTab(Resource.getPlainResourceString("tasks"), taskPanel);

	    if (!MemoModel.getReference().hasMemos()) {
		JTextArea ta = new JTextArea();
		ta.setText(Resource.getPlainResourceString("MemosNotSupported"));
		ta.setEditable(false);
		tabs_.addTab(Resource.getPlainResourceString("Memos"), ta);
	    } else
		tabs_.addTab(Resource.getPlainResourceString("Memos"), memoPanel);
	    tabs_.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    tabChanged();
		}
	    });
	}
	return tabs_;
    }

    public void destroy() {
	this.dispose();
	mainView = null;
    }

    public void refresh() {
	wkPanel.clearData();
	wkPanel.repaint();
	dayPanel.clearData();
	dayPanel.repaint();
	calPanel.refresh();
	if (memoPanel != null)
	    memoPanel.refresh();
	taskPanel.refresh();
	if (projPanel != null)
	    projPanel.refresh();
    }

    public void next() {
	if (getTabs().getSelectedComponent() == wkPanel) {
	    cal_.add(Calendar.DATE, 7);
	} else if (getTabs().getSelectedComponent() == dayPanel) {
	    cal_.add(Calendar.DATE, 1);
	} else if (getTabs().getSelectedComponent() == calPanel) {
	    cal_.add(Calendar.MONTH, 1);
	}
	wkPanel.goTo(cal_);
	dayPanel.goTo(cal_);
	calPanel.goTo(cal_);

    }

    public void prev() {
	if (getTabs().getSelectedComponent() == wkPanel) {
	    cal_.add(Calendar.DATE, -7);
	} else if (getTabs().getSelectedComponent() == dayPanel) {
	    cal_.add(Calendar.DATE, -1);
	} else if (getTabs().getSelectedComponent() == calPanel) {
	    cal_.add(Calendar.MONTH, -1);
	}
	wkPanel.goTo(cal_);
	dayPanel.goTo(cal_);
	calPanel.goTo(cal_);
    }

    public void today() {
	cal_ = new GregorianCalendar();
	wkPanel.today();
	dayPanel.today();
	calPanel.today();

    }

    public void goTo(Calendar cal) {
	cal_ = cal;
	wkPanel.goTo(cal);
	dayPanel.goTo(cal);
	calPanel.goTo(cal);

    }

    private JPanel navPanel = null;

    private JPanel getNavPanel() {
	if (navPanel == null) {
	    GridLayout gridLayout62 = new GridLayout();
	    navPanel = new JPanel();
	    navPanel.setLayout(gridLayout62);
	    gridLayout62.setRows(1);
	    JButton Prev = new JButton();
	    Prev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Back16.gif")));
	    ResourceHelper.setText(Prev, "<<__Prev");
	    Prev.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    prev();
		}
	    });

	    JButton Next = new JButton();
	    Next.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Forward16.gif")));
	    ResourceHelper.setText(Next, "Next__>>");
	    Next.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
	    Next.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    next();
		}
	    });

	    JButton Today = new JButton();
	    Today.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Home16.gif")));
	    ResourceHelper.setText(Today, "Today");
	    Today.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    today();
		}
	    });

	    JButton Goto = new JButton();
	    Goto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Undo16.gif")));
	    ResourceHelper.setText(Goto, "Go_To");
	    Goto.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DateDialog dlg = new DateDialog(null);
		    dlg.setCalendar(new GregorianCalendar());
		    dlg.setVisible(true);
		    Calendar dlgcal = dlg.getCalendar();
		    if (dlgcal == null)
			return;
		    goTo(dlgcal);
		}
	    });
	    navPanel.add(Prev, null);
	    navPanel.add(Today, null);
	    navPanel.add(Goto, null);
	    navPanel.add(Next, null);
	}
	return navPanel;
    }

    public void print() {
	try {
	    if (getTabs().getSelectedComponent() == wkPanel) {
		PrintHelper.printPrintable(wkPanel);
	    } else if (getTabs().getSelectedComponent() == dayPanel) {
		PrintHelper.printPrintable(dayPanel);
	    } else if (getTabs().getSelectedComponent() == calPanel) {
		calPanel.print();
	    } else if (getTabs().getSelectedComponent() == taskPanel) {
		taskPanel.print();
	    } else if (getTabs().getSelectedComponent() == projPanel) {
		projPanel.print();
	    }
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }

    public void showTasksForProject(Project p) {

	taskPanel.showTasksForProject(p);
	showTasks();

    }

    private void exit() {
	if (!Borg.getReference().hasTrayIcon() && this == mainView) {
	    Borg.shutdown();
	} else {
	    this.dispose();
	}
    }

    public void dock(DockableView dp) {
	tabs_.addTab(dp.getFrameTitle(), dp);
	tabs_.setSelectedIndex(tabs_.getTabCount() - 1);
	dp.remove();

    }

    public void addView(DockableView dp) {
	String dock = Prefs.getPref(PrefName.DOCKPANELS);
	if (dock.equals("true")) {
	    dock(dp);
	} else
	    dp.openInFrame();
    }
    
    public void closeTabs()
    {
	tabs_.closeClosableTabs();
    }
}
