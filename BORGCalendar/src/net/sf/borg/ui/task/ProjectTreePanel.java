/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui.task;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * Presents a split pane showing a tree of projects, subprojects, and tasks on
 * the left and the select item's details on the right.
 */
public class ProjectTreePanel extends JPanel implements TreeSelectionListener,
		MouseListener, Model.Listener, Prefs.Listener {

	private static final long serialVersionUID = 1L;
	
	/** Custom Tree Cell Renderer that shows empty projects as closed folders instead of leaf icons. */
	private class ProjectTreeCellRenderer extends DefaultTreeCellRenderer
	{

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
					row, hasFocus);
			
			// get the tree model node
			if( value instanceof DefaultMutableTreeNode )
			{
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
				
				// get the Borg Node object, if any
				if( treeNode.getUserObject() != null && treeNode.getUserObject() instanceof Node )
				{
					Node node = (Node)treeNode.getUserObject();
					
					// if the Borg Entity is a Project and the node is a leaf in the tree, then show
					// the closed folder icon
					if( leaf && node.getEntity() instanceof Project)
					{
						this.setIcon(closedIcon);
					}	
				}
				
			}
			return this;
		}


		
	}

	/**
	 * A Node in the tree that contains the visible node name and the related
	 * object
	 */
	private class Node {

		/** The entity name. */
		private String name;

		/** The entity */
		private KeyedEntity<?> entity;

		/**
		 * Instantiates a new node.
		 * 
		 * @param name
		 *            the name
		 * @param o
		 *            the entity
		 */
		public Node(String name, KeyedEntity<?> o) {
			super();
			this.name = name;
			entity = o;
		}

		/**
		 * Gets the name.
		 * 
		 * @return the name
		 */
		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}

		/**
		 * Gets the entity.
		 * 
		 * @return the entity
		 */
		public KeyedEntity<?> getEntity() {
			return entity;
		}

		/**
		 * Sets the name.
		 * 
		 * @param name
		 *            the new name
		 */
		@SuppressWarnings("unused")
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Sets the entity.
		 * 
		 * @param entity
		 *            the new entity
		 */
		@SuppressWarnings("unused")
		public void setEntity(KeyedEntity<?> obj) {
			this.entity = obj;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return name;
		}

	}

	/**
	 * Expand or Collapse all nodes in the tree under a given node
	 * 
	 * @param tree
	 *            the tree
	 * @param parent
	 *            the start node
	 * @param expand
	 *            the expand
	 */
	static private void expandOrCollapseSubTree(JTree tree, TreePath parent,
			boolean expand) {

		// recurse to call this method for every child
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandOrCollapseSubTree(tree, path, expand);
			}
		}

		// Expansion or collapse must be done bottom-up, so we do the actual
		// work
		// after the recursive call
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	/**
	 * true if the state of the tree is expanded - used to refresh in the same
	 * state
	 */
	private boolean isExpanded = true;

	/** The project popup menu. */
	private JPopupMenu projmenu = new JPopupMenu();

	/** The root node popup menu */
	private JPopupMenu rootmenu = new JPopupMenu();

	/** The show closed. */
	private JCheckBox showClosedCheckBox = new JCheckBox(Resource
			.getResourceString("show_closed"));

	private JCheckBox showClosedTasksCheckBox = new JCheckBox(Resource
			.getResourceString("show_closed_tasks"));

	/** The tree. */
	private JTree tree = null;

	/** The tree scroll pane. */
	private JScrollPane treeScrollPane = null;

	/** The entity Scroll Pane. */
	private JScrollPane entityScrollPane = new JScrollPane(new JPanel());

	/**
	 * constructor
	 */
	public ProjectTreePanel() {

		super(new GridLayout(0, 1));

		// listen for task model changes
		TaskModel.getReference().addListener(this);

		// Create the nodes.
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(Resource
				.getResourceString("projects"));
		createNodes(rootNode);

		// Create a tree that allows one selection at a time.
		tree = new JTree(rootNode);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		tree.setCellRenderer(new ProjectTreeCellRenderer());

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		// Create the scroll pane and add the tree to it.
		treeScrollPane = new JScrollPane(tree);

		// pane containing tree + show closed check box
		JPanel treePane = new JPanel();
		treePane.setLayout(new GridBagLayout());
		treePane.add(treeScrollPane, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));
		treePane.add(showClosedCheckBox, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH));
		treePane.add(showClosedTasksCheckBox, GridBagConstraintsFactory.create(
				0, 2, GridBagConstraints.BOTH));

		// Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setTopComponent(treePane);
		splitPane.setBottomComponent(entityScrollPane);

		Dimension minimumSize = new Dimension(200, 50);
		entityScrollPane.setMinimumSize(minimumSize);
		treeScrollPane.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(250);

		add(splitPane);

		tree.addMouseListener(this);

		/*
		 * root node popup menu
		 */
		JMenuItem jm = rootmenu.add(Resource.getResourceString("Add") + " "
				+ Resource.getResourceString("project"));
		jm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// show a new project editor
					ProjectView pv = new ProjectView(null,
							ProjectView.Action.ADD, null);
					entityScrollPane.setViewportView(pv);
				} catch (Exception ex) {
					Errmsg.errmsg(ex);
				}
			}

		});

		JMenuItem jmex = rootmenu.add(Resource.getResourceString("expand"));
		jmex.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				isExpanded = true;
				expandOrCollapseAll(isExpanded);
			}

		});
		JMenuItem jmcol = rootmenu.add(Resource.getResourceString("collapse"));
		jmcol.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				isExpanded = false;
				expandOrCollapseAll(isExpanded);
			}

		});

		/*
		 * project node popup menu
		 */
		JMenuItem jm2 = projmenu.add(Resource.getResourceString("Add") + " "
				+ Resource.getResourceString("task"));
		jm2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addTask();
			}

		});
		JMenuItem jm3 = projmenu.add(Resource.getResourceString("Add") + " "
				+ Resource.getResourceString("project"));
		jm3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addSubProject();
			}

		});

		showClosedCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});

		showClosedTasksCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});

		expandOrCollapseAll(isExpanded);
		
		Prefs.addListener(this);
	}

	/**
	 * Add a new sub project as a child of the current project and show an
	 * editor for it
	 */
	private void addSubProject() {

		Object o = getSelectedEntity();
		if (o == null)
			return;
		if (o instanceof Project) {
			Project p = (Project) o;
			try {
				ProjectView pv;
				try {
					pv = new ProjectView(null, ProjectView.Action.ADD,
							new Integer(p.getKey()));
					entityScrollPane.setViewportView(pv);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}
		}

	}

	/**
	 * Add a new sub task as a child of the current project and show an editor
	 * for it
	 */
	private void addTask() {
		Object o = getSelectedEntity();
		if (o == null)
			return;
		if (o instanceof Project) {
			Project p = (Project) o;
			try {
				TaskView pv = new TaskView(null, TaskView.Action.ADD,
						new Integer(p.getKey()));
				entityScrollPane.setViewportView(pv);
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}
		}

	}

	/**
	 * Adds the project children to the tree
	 * 
	 * @param p
	 *            the project
	 * @param node
	 *            the project node
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void addProjectChildren(Project p, DefaultMutableTreeNode node)
			throws Exception {

		Collection<Task> tasks = TaskModel.getReference().getTasks(p.getKey());
		for (Task task : tasks) {
			if (!CategoryModel.getReference().isShown(task.getCategory()))
				continue;

			// filter out closed projects if needed
			if (!showClosedTasksCheckBox.isSelected()
					&& TaskModel.isClosed(task))
				continue;

			String taskdesc = task.getDescription();
			int newlineIndex = taskdesc.indexOf('\n');
			if (newlineIndex != -1)
				taskdesc = taskdesc.substring(0, newlineIndex);

			if (Prefs.getBoolPref(PrefName.TASK_TREE_SHOW_STATUS)) {
				node.add(new DefaultMutableTreeNode(new Node("["
						+ task.getKey() + "-" + task.getState() + "] "
						+ taskdesc, task)));
			} else {
				node.add(new DefaultMutableTreeNode(new Node(taskdesc, task)));
			}
		}

		Collection<Project> subpcoll = TaskModel.getReference().getSubProjects(
				p.getKey());
		for (Project project : subpcoll) {
			if (!CategoryModel.getReference().isShown(project.getCategory()))
				continue;
			if (!showClosedCheckBox.isSelected()
					&& TaskModel.isClosed(project))
				continue;
			DefaultMutableTreeNode subnode = new DefaultMutableTreeNode(
					new Node(project.getDescription(), project));
			node.add(subnode);
			addProjectChildren(project, subnode);
		}

	}

	/**
	 * Creates the entire tree by adding all items in the task model
	 * 
	 * @param top
	 *            the root node
	 */
	private void createNodes(DefaultMutableTreeNode top) {

		Collection<Project> projects;
		try {
			projects = TaskModel.getReference().getProjects();
			for (Project p : projects) {

				// filter out closed projects if needed
				if (!showClosedCheckBox.isSelected() && TaskModel.isClosed(p))
					continue;

				// filter by caegory
				if (!CategoryModel.getReference().isShown(p.getCategory()))
					continue;

				// don't add sub-projects - they are added later
				if (p.getParent() != null)
					continue;

				DefaultMutableTreeNode pnode = new DefaultMutableTreeNode(
						new Node(p.getDescription(), p));

				// add the top level project node
				top.add(pnode);

				// add the project's children
				addProjectChildren(p, pnode);
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * Expand or collapse all nodes
	 * 
	 * @param expand
	 *            if true - expand, else collapse
	 */
	public void expandOrCollapseAll(boolean expand) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();

		// Traverse tree from root
		expandOrCollapseSubTree(tree, new TreePath(root), expand);
	}

	/**
	 * Gets the selected eneity.
	 * 
	 * @return the selected entity
	 */
	private KeyedEntity<?> getSelectedEntity() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (node == null)
			return null;

		Object nodeobj = node.getUserObject();
		if (!(nodeobj instanceof Node))
			return null;

		Node mynode = (Node) node.getUserObject();

		return mynode.getEntity();

	}

	/**
	 * handle right mouse click events and popup the appropriate menu
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			return;

		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		if (selPath == null)
			return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
				.getLastPathComponent();
		if (node == null)
			return;

		tree.setSelectionPath(selPath);
		Object nodeobj = node.getUserObject();
		if (!(nodeobj instanceof Node)) {
			rootmenu.show(this, e.getX(), e.getY());
			return;
		}

		Node mynode = (Node) node.getUserObject();

		Object o = mynode.getEntity();
		if (o == null)
			return;
		if (o instanceof Project)
			projmenu.show(this, e.getX(), e.getY());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent arg0) {
		// empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// empty
	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * refresh the entire tree from the task model
	 */
	public void refresh() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(Resource
				.getResourceString("projects"));
		createNodes(top);
		// Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.setCellRenderer(new ProjectTreeCellRenderer());

		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);
		treeScrollPane.setViewportView(tree);
		tree.addMouseListener(this);
		expandOrCollapseAll(isExpanded);
	}

	/**
	 * handle node selection in the tree. show the appropriate editor in the
	 * right pane
	 * 
	 * @param e
	 *            the selection event
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Object o = getSelectedEntity();
		if (o == null)
			return;

		if (o instanceof Task) {
			Task t = (Task) o;
			try {
				TaskView tv = new TaskView(t, TaskView.Action.CHANGE, t
						.getProject());
				entityScrollPane.setViewportView(tv);
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}
		} else if (o instanceof Project) {
			Project p = (Project) o;
			try {
				ProjectView pv = new ProjectView(p, ProjectView.Action.CHANGE,
						null);
				entityScrollPane.setViewportView(pv);
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}
		}

	}

	@Override
	/**
	 * update when prefs change
	 */
	public void prefsChanged() {
		refresh();
	}

}
