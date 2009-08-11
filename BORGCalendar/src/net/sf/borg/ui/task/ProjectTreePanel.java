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
import java.util.Iterator;

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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.Task;

public class ProjectTreePanel extends JPanel implements TreeSelectionListener,
		MouseListener, Model.Listener {
	private class Node {
		private String name;

		private Object obj;

		public Node(String name, Object o) {
			super();
			this.name = name;
			obj = o;
		}

		public String getName() {
			return name;
		}

		public Object getObj() {
			return obj;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setObj(Object obj) {
			this.obj = obj;
		}

		public String toString() {
			return name;
		}

	}

	static private void expandAll(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}

		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	private boolean expanded_ = true;

	private JPopupMenu projmenu = new JPopupMenu();

	private JPopupMenu rootmenu = new JPopupMenu();

	private JCheckBox showClosed = new JCheckBox(Resource
			.getPlainResourceString("show_closed"));

	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	private JTree tree = null;

	private JScrollPane treeView = null;

	private JScrollPane view_scroll = new JScrollPane(new JPanel());

	public ProjectTreePanel() {
		super(new GridLayout(0, 1));
		TaskModel.getReference().addListener(this);
		// Create the nodes.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(Resource
				.getPlainResourceString("projects"));
		createNodes(top);
		// Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		// Create the scroll pane and add the tree to it.
		treeView = new JScrollPane(tree);

		JPanel leftPane = new JPanel();
		leftPane.setLayout(new GridBagLayout());
		GridBagConstraints cons1 = new GridBagConstraints();
		cons1.fill = GridBagConstraints.BOTH;
		cons1.gridx = 0;
		cons1.gridy = 0;
		cons1.weightx = 1.0;
		cons1.weighty = 1.0;
		leftPane.add(treeView, cons1);
		GridBagConstraints cons2 = new GridBagConstraints();
		cons2.fill = GridBagConstraints.BOTH;
		cons2.gridx = 0;
		cons2.gridy = 1;
		cons2.weightx = 0;
		cons2.weighty = 0;

		leftPane.add(showClosed, cons2);

		// Add the scroll panes to a split pane.
		splitPane.setTopComponent(leftPane);
		splitPane.setBottomComponent(view_scroll);

		Dimension minimumSize = new Dimension(200, 50);
		view_scroll.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(250);
		add(splitPane);
		tree.addMouseListener(this);
		JMenuItem jm = rootmenu.add(Resource.getPlainResourceString("Add")
				+ " " + Resource.getPlainResourceString("project"));
		jm.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				add_project();
			}

		});

		JMenuItem jmex = rootmenu
				.add(Resource.getPlainResourceString("expand"));
		jmex.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				expanded_ = true;
				expandAll(expanded_);
			}

		});
		JMenuItem jmcol = rootmenu.add(Resource
				.getPlainResourceString("collapse"));
		jmcol.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				expanded_ = false;
				expandAll(expanded_);
			}

		});
		JMenuItem jm2 = projmenu.add(Resource.getPlainResourceString("Add")
				+ " " + Resource.getPlainResourceString("task"));
		jm2.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				add_task();
			}

		});
		JMenuItem jm3 = projmenu.add(Resource.getPlainResourceString("Add")
				+ " " + Resource.getPlainResourceString("project"));
		jm3.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				add_subproject();
			}

		});

		showClosed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});

		expandAll(expanded_);
	}

	public void expandAll(boolean expand) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();

		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			return;

		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
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

		Object o = mynode.getObj();
		if (o == null)
			return;
		if (o instanceof Project)
			projmenu.show(this, e.getX(), e.getY());

	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void refresh() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(Resource
				.getPlainResourceString("projects"));
		createNodes(top);
		// Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);
		treeView.setViewportView(tree);
		tree.addMouseListener(this);
		expandAll(expanded_);
	}

	public void remove() {

	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e) {
		Object o = getSelectedObject();
		if (o == null)
			return;
		if (o instanceof Task) {
			Task t = (Task) o;
			try {
				TaskView tv = new TaskView(t, TaskView.T_CHANGE, t.getProject());
				view_scroll.setViewportView(tv);
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}
		} else if (o instanceof Project) {
			Project p = (Project) o;
			try {
				ProjectView pv = new ProjectView(p, ProjectView.T_CHANGE, null);
				// splitPane.setBottomComponent(pv);
				view_scroll.setViewportView(pv);
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}
		}

	}

	private void add_project() {
		ProjectView pv;
		try {
			pv = new ProjectView(null, ProjectView.T_ADD, null);
			view_scroll.setViewportView(pv);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	private void add_subproject() {

		Object o = getSelectedObject();
		if (o == null)
			return;
		if (o instanceof Project) {
			Project p = (Project) o;
			try {
				ProjectView pv;
				try {
					pv = new ProjectView(null, ProjectView.T_ADD, p.getId());
					view_scroll.setViewportView(pv);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}
		}

	}

	private void add_task() {
		Object o = getSelectedObject();
		if (o == null)
			return;
		if (o instanceof Project) {
			Project p = (Project) o;
			try {
				TaskView pv = new TaskView(null, TaskView.T_ADD, p.getId());
				view_scroll.setViewportView(pv);
			} catch (Exception e1) {
				Errmsg.errmsg(e1);
				return;
			}
		}

	}

	private void addProjectChildren(Project p, DefaultMutableTreeNode node)
			throws Exception {
		Collection<?> tasks = TaskModel.getReference().getTasks(
				p.getId().intValue());
		Iterator<?> it2 = tasks.iterator();
		while (it2.hasNext()) {
			Task t = (Task) it2.next();
			if (!CategoryModel.getReference().isShown(t.getCategory()))
				continue;
			node.add(new DefaultMutableTreeNode(new Node("["
					+ t.getTaskNumber() + "-" + t.getState() + "] "
					+ t.getDescription(), t)));
		}

		Collection<?> subpcoll = TaskModel.getReference().getSubProjects(
				p.getId().intValue());
		it2 = subpcoll.iterator();
		while (it2.hasNext()) {
			Project sp = (Project) it2.next();
			if (!CategoryModel.getReference().isShown(sp.getCategory()))
				continue;
			DefaultMutableTreeNode subnode = new DefaultMutableTreeNode(
					new Node(sp.getDescription(), sp));
			node.add(subnode);
			addProjectChildren(sp, subnode);
		}

	}

	private void createNodes(DefaultMutableTreeNode top) {

		Collection<?> projects;
		try {
			projects = TaskModel.getReference().getProjects();
			Iterator<?> it = projects.iterator();
			while (it.hasNext()) {
				Project p = (Project) it.next();
				if (!showClosed.isSelected() && TaskModel.isClosed(p))
					continue;
				if (!CategoryModel.getReference().isShown(p.getCategory()))
					continue;
				if (p.getParent() != null)
					continue;
				DefaultMutableTreeNode pnode = new DefaultMutableTreeNode(
						new Node(p.getDescription(), p));
				top.add(pnode);
				addProjectChildren(p, pnode);
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	private Object getSelectedObject() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (node == null)
			return null;

		Object nodeobj = node.getUserObject();
		if (!(nodeobj instanceof Node))
			return null;

		Node mynode = (Node) node.getUserObject();

		Object o = mynode.getObj();

		return o;
	}

	// Set the icon for leaf nodes.
	/*
	 * ImageIcon leafIcon = createImageIcon("images/middle.gif"); if (leafIcon !=
	 * null) { DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
	 * renderer.setLeafIcon(leafIcon); tree.setCellRenderer(renderer); } else {
	 * System.err.println("Leaf icon missing; using default."); }
	 */

}
