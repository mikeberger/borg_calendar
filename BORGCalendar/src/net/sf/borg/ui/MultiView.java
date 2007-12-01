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
import java.awt.print.Printable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;

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
import net.sf.borg.ui.address.AddrListView;
import net.sf.borg.ui.calendar.CalendarPanel;
import net.sf.borg.ui.calendar.DayPanel;
import net.sf.borg.ui.calendar.MonthPanel;
import net.sf.borg.ui.calendar.TodoView;
import net.sf.borg.ui.calendar.WeekPanel;
import net.sf.borg.ui.memo.MemoPanel;
import net.sf.borg.ui.task.ProjectPanel;
import net.sf.borg.ui.task.ProjectTreePanel;
import net.sf.borg.ui.task.TaskListPanel;
import net.sf.borg.ui.util.JTabbedPaneWithCloseIcons;

// weekView handles the printing of a single week
public class MultiView extends View  {

    static private MultiView mainView = null;

    private Calendar cal_ = new GregorianCalendar();

    private JTabbedPaneWithCloseIcons tabs_ = null;

    private DayPanel dayPanel = null;

    private WeekPanel wkPanel = null;
    
    private MonthPanel monthPanel = null;

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
	JMenuBar menubar = new MainMenu().getMenuBar();

	menubar.setBorder(new BevelBorder(BevelBorder.RAISED));

	setJMenuBar(menubar);
	getContentPane().setLayout(new GridBagLayout());
	GridBagConstraints cons = new java.awt.GridBagConstraints();
	cons.gridx = 0;
	cons.gridy = 0;
	cons.fill = java.awt.GridBagConstraints.HORIZONTAL;
	cons.weightx = 0.0;
	cons.weighty = 0.0;

	getContentPane().add(getToolBar(), cons);
	
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
	setVisible(true);
	setView(MONTH);
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
	    
	    if( dayPanel == null )
		 dayPanel = new DayPanel(cal_.get(Calendar.MONTH), cal_.get(Calendar.YEAR), cal_.get(Calendar.DATE));
		    
	    if (!dayPanel.isDisplayable()) {
		tabs_.addTab(Resource.getPlainResourceString("Day_View"), dayPanel);
	    }
	    getTabs().setSelectedComponent(dayPanel);
	} else if (type == WEEK) {
	    if( wkPanel == null )
		wkPanel = new WeekPanel(cal_.get(Calendar.MONTH), cal_.get(Calendar.YEAR), cal_.get(Calendar.DATE));

	    if (!wkPanel.isDisplayable()) {
		tabs_.addTab(Resource.getPlainResourceString("Week_View"), wkPanel);
	    }
	    getTabs().setSelectedComponent(wkPanel);
	} else if (type == MONTH) {

	    String omv = Prefs.getPref(PrefName.OLDMONTHVIEW);
	    if( omv.equals("false"))
	    {
		if( monthPanel == null )
		    monthPanel = new MonthPanel(cal_.get(Calendar.MONTH), cal_.get(Calendar.YEAR));
		if (!monthPanel.isDisplayable()) {
		    tabs_.addTab(Resource.getPlainResourceString("Month_View"), monthPanel);
		}
		getTabs().setSelectedComponent(monthPanel);
	    }
	    else
	    {

		if( calPanel == null )
		    calPanel = new CalendarPanel(this, cal_.get(Calendar.MONTH), cal_.get(Calendar.YEAR));

		if (!calPanel.isDisplayable()) {
		    tabs_.addTab(Resource.getPlainResourceString("Month_View"), calPanel);
		}
		getTabs().setSelectedComponent(calPanel);
	    }
	}
    }

    public void showMemos()
    {
	if (MemoModel.getReference().hasMemos() && memoPanel == null)
	    memoPanel = new MemoPanel();

	if (memoPanel != null && !memoPanel.isDisplayable()) {
	    tabs_.addTab(Resource.getPlainResourceString("Memos"), memoPanel);
	}
	if (memoPanel != null)
	    getTabs().setSelectedComponent(memoPanel);
    }
    
    public void showTasks()
    {
	if( taskPanel == null )
	    taskPanel = new TaskListPanel();

	if (TaskModel.getReference().hasSubTasks() && projPanel == null )
	{
	    projPanel = new ProjectPanel();
	}
	
	if (TaskModel.getReference().hasSubTasks() && ptPanel == null )
	{
	    ptPanel = new ProjectTreePanel();
	}
	
	if (ptPanel != null && !ptPanel.isDisplayable()) 
	    tabs_.addTab(Resource.getPlainResourceString("project_tree"), ptPanel);

	if (projPanel != null && !projPanel.isDisplayable()) {
	    tabs_.addTab(Resource.getPlainResourceString("projects"), projPanel);
	}
	
	if (taskPanel != null && !taskPanel.isDisplayable()) {
	    tabs_.addTab(Resource.getPlainResourceString("tasks"), taskPanel);
	}
	if (ptPanel != null)
	    getTabs().setSelectedComponent(ptPanel);
	else if( taskPanel != null)
	    getTabs().setSelectedComponent(taskPanel);
	
	
    }

    private TaskListPanel taskPanel = null;

    private ProjectPanel projPanel = null;
    
    private ProjectTreePanel ptPanel = null;

    private JTabbedPane getTabs() {
	if (tabs_ == null) {
	    tabs_ = new JTabbedPaneWithCloseIcons();
	}
	return tabs_;
    }

    public void destroy() {
	this.dispose();
	mainView = null;
    }

    public void print() {
	try {
	    Component c = getTabs().getSelectedComponent();
	    if (c instanceof MonthPanel) {
		((MonthPanel)c).printMonths();
	    }
	    else if( c instanceof Printable  )
	    {
		PrintHelper.printPrintable((Printable) c);
	    } else if (c == calPanel) {
		calPanel.print();
	    } else if (c == taskPanel) {
		taskPanel.print();
	    } else if (c == projPanel) {
		projPanel.print();	   
	    } else if (c instanceof TodoView) {
		TodoView.getReference().print();
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
    

    private JToolBar getToolBar()
    {
	JToolBar bar = new JToolBar();
	bar.setFloatable(false);
	
	JButton monbut = new JButton(new ImageIcon(getClass().getResource("/resource/month.jpg")));
	monbut.setToolTipText(Resource.getPlainResourceString("Month_View"));
	monbut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		setView(MONTH);
	    }
	});
	bar.add(monbut);
	
	JButton weekbut = new JButton(new ImageIcon(getClass().getResource("/resource/week.jpg")));
	weekbut.setToolTipText(Resource.getPlainResourceString("Week_View"));
	weekbut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		setView(WEEK);
	    }
	});
	bar.add(weekbut);
	
	JButton daybut = new JButton(new ImageIcon(getClass().getResource("/resource/day.jpg")));
	daybut.setToolTipText(Resource.getPlainResourceString("Day_View"));
	daybut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		setView(DAY);
	    }
	});
	bar.add(daybut);
	
	JButton addrbut = new JButton(new ImageIcon(getClass().getResource("/resource/addr16.jpg")));
	addrbut.setToolTipText(Resource.getPlainResourceString("Address_Book"));
	addrbut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		MultiView.getMainView().addView(AddrListView.getReference());
	    }
	});
	bar.add(addrbut);
	
	JButton todobut = new JButton(new ImageIcon(getClass().getResource("/resource/Properties16.gif")));
	todobut.setToolTipText(Resource.getPlainResourceString("To_Do"));
	todobut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    TodoView tg = TodoView.getReference();
		    MultiView.getMainView().addView(tg);    
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		}
	    }
	});
	bar.add(todobut);
	
	JButton taskbut = new JButton(new ImageIcon(getClass().getResource("/resource/Preferences16.gif")));
	taskbut.setToolTipText(Resource.getPlainResourceString("tasks"));
	taskbut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		MultiView.getMainView().showTasks();
	    }
	});
	bar.add(taskbut);
	
	JButton memobut = new JButton(new ImageIcon(getClass().getResource("/resource/Edit16.gif")));
	memobut.setToolTipText(Resource.getPlainResourceString("Memos"));
	memobut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		MultiView.getMainView().showMemos();
	    }
	});
	bar.add(memobut);
	
	JButton srchbut = new JButton(new ImageIcon(getClass().getResource("/resource/Find16.gif")));
	srchbut.setToolTipText(Resource.getPlainResourceString("srch"));
	srchbut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		MultiView.getMainView().addView(new SearchView());
	    }
	});
	bar.add(srchbut);
	
	JButton printbut = new JButton(new ImageIcon(getClass().getResource("/resource/Print16.gif")));
	printbut.setToolTipText(Resource.getPlainResourceString("Print"));
	printbut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		print();
	    }
	});
	bar.add(printbut);
	
	bar.addSeparator();
	
	JButton clearbut = new JButton(new ImageIcon(getClass().getResource("/resource/Delete16.gif")));
	clearbut.setToolTipText(Resource.getPlainResourceString("close_tabs"));
	clearbut.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		closeTabs();
	    }
	});
	bar.add(clearbut);
	
	
	return bar;
    }

    public void refresh() {
	// TODO Auto-generated method stub
	
    }
    
    public void goTo(Calendar cal)
    {
	if( dayPanel != null ) dayPanel.goTo(cal);
	if( calPanel != null ) calPanel.goTo(cal);
	if( wkPanel != null ) wkPanel.goTo(cal);
    }
}
