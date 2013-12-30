package net.sf.borg.ui.task;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.TaskModel;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.SunTrayIconProxy;
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
	
	private boolean isInitialized = false;

	@Override
	public String getModuleName() {
		return Resource.getResourceString("tasks");
	}

	@Override
	public JComponent getComponent() {
		
		if( !isInitialized )
		{
			taskTabs = new JTabbedPane();

			taskTabs.addTab(Resource.getResourceString("project_tree"),
					new ProjectTreePanel());

			taskTabs.addTab(Resource.getResourceString("projects"),
					new ProjectPanel());

			taskTabs.addTab(Resource.getResourceString("tasks"),
					new TaskFilterPanel());
			
			setLayout(new java.awt.GridBagLayout());
			add(taskTabs, GridBagConstraintsFactory
					.create(0, 0, GridBagConstraints.BOTH, 1.0, 1.0));
			isInitialized = true;


		}
		
		return this;
	}

	@Override
	public void initialize(MultiView parent) {
		
		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
		"/resource/Task16.gif")), getModuleName(), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				par.setView(ViewType.TASK);
			}
		});
		SunTrayIconProxy.addAction(getModuleName(), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				par.setView(ViewType.TASK);
			}
		});
		
		JMenuItem edittypes = new JMenuItem();
		JMenuItem resetst = new JMenuItem();
		ResourceHelper.setText(edittypes, "edit_types");
		edittypes.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					TaskConfigurator.getReference().setVisible(true);
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});

		MultiView.getMainView().addOptionsMenuItem(edittypes);

		ResourceHelper.setText(resetst, "Reset_Task_States_to_Default");
		resetst.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetstActionPerformed();
			}
		});

		MultiView.getMainView().addOptionsMenuItem(resetst);
		
	}
	
	/**
	 * reset task state action
	 */
	private static void resetstActionPerformed() {
		try {
			String msg = Resource.getResourceString("reset_state_warning");
			int ret = JOptionPane.showConfirmDialog(null, msg, Resource
					.getResourceString("Import_WARNING"),
					JOptionPane.OK_CANCEL_OPTION);

			if (ret != JOptionPane.OK_OPTION)
				return;
			TaskModel taskmod_ = TaskModel.getReference();
			taskmod_.getTaskTypes().loadDefault();
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	@Override
	public void print() {
		Component c = taskTabs.getSelectedComponent();
		if( c instanceof ProjectPanel )
			((ProjectPanel)c).print();
		else if( c instanceof TaskListPanel )
			((TaskListPanel)c).print();
		else if( c instanceof TaskFilterPanel )
			((TaskFilterPanel)c).print();
		else
			Errmsg.getErrorHandler().notice(Resource.getResourceString("No_Print"));

		
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
	public void refresh() {
		// do nothing - children do their own refresh
		
	}

	@Override
	public void update(ChangeEvent event) {
		// do nothing - children do their own refresh
	}
	
	
}
