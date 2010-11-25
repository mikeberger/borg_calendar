package net.sf.borg.ui.task;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;

import net.sf.borg.common.Resource;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * The TaskModule is the UI Module for Tasks that is invoked from the MultiView
 * 
 */
public class TaskModule extends DockableView implements Module {

	
	private static final long serialVersionUID = 1L;
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
		return this;
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
		"/resource/Task16.gif")), getModuleName(), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				par.setView(ViewType.TASK);
			}
		});

		setLayout(new java.awt.GridBagLayout());
		add(taskTabs, GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.BOTH, 1.0, 1.0));
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

	@Override
	public String getFrameTitle() {
		return this.getModuleName();
	}

	@Override
	public JMenuBar getMenuForFrame() {
		return null;
	}

	@Override
	public void refresh() {
		// do nothing - children do their own refresh
		
	}

	@Override
	public void update(ChangeEvent event) {
		// do nothing - children do their own refresh
	}
	
	
}
