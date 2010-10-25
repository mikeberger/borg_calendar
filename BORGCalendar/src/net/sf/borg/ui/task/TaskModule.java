package net.sf.borg.ui.task;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import net.sf.borg.common.Resource;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;

/**
 * The TaskModule is the UI Module for Tasks that is invoked from the MultiView
 * 
 */
public class TaskModule implements Module {

	/**
	 * the task tabs managed by thsi module - which are all subtabs on the MultiView's task tab
	 */
	private JTabbedPane taskTabs;

	@Override
	public String getModuleName() {
		return Resource.getResourceString("tasks");
	}

	@Override
	public JComponent getComponent() {
		return taskTabs;
	}

	@Override
	public void initialize(MultiView parent) {
		taskTabs = new JTabbedPane();

		taskTabs.addTab(Resource.getResourceString("project_tree"),
				new ProjectTreePanel());

		taskTabs.addTab(Resource.getResourceString("projects"),
				new ProjectPanel());

		taskTabs.addTab(Resource.getResourceString("tasks"),
				new TaskFilterPanel());

		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
		"/resource/Preferences16.gif")), getModuleName(), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				par.setView(ViewType.TASK);
			}
		});

	}

	@Override
	public void print() {
		Component c = taskTabs.getSelectedComponent();
		if( c instanceof ProjectPanel )
			((ProjectPanel)c).print();
		else if( c instanceof TaskListPanel )
			((TaskListPanel)c).print();
		
	}
	
	@Override
	public ViewType getViewType() {
		return ViewType.TASK;
	}
	
}
