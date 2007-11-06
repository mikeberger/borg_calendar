package net.sf.borg.ui.task;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.Task;

public class ProjectTreePanel extends JPanel implements TreeSelectionListener {
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

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
	java.net.URL imgURL = ProjectTreePanel.class.getResource(path);
	if (imgURL != null) {
	    return new ImageIcon(imgURL);
	} else {
	    System.err.println("Couldn't find file: " + path);
	    return null;
	}
    }

    private JTree tree;

    private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    
    private JScrollPane view_scroll = new JScrollPane(new JPanel());
    
    public ProjectTreePanel() {
	super(new GridLayout(0, 1));

	// Create the nodes.
	DefaultMutableTreeNode top = new DefaultMutableTreeNode(Resource.getPlainResourceString("projects"));
	createNodes(top);

	// Create a tree that allows one selection at a time.
	tree = new JTree(top);
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	// Set the icon for leaf nodes.
	/*
	ImageIcon leafIcon = createImageIcon("images/middle.gif");
	if (leafIcon != null) {
	    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
	    renderer.setLeafIcon(leafIcon);
	    tree.setCellRenderer(renderer);
	} else {
	    System.err.println("Leaf icon missing; using default.");
	}*/

	// Listen for when the selection changes.
	tree.addTreeSelectionListener(this);

	// Create the scroll pane and add the tree to it.
	JScrollPane treeView = new JScrollPane(tree);

	

	// Add the scroll panes to a split pane.

	splitPane.setTopComponent(treeView);
	splitPane.setBottomComponent(view_scroll);

	Dimension minimumSize = new Dimension(200, 50);
	view_scroll.setMinimumSize(minimumSize);
	treeView.setMinimumSize(minimumSize);
	splitPane.setDividerLocation(200);
	add(splitPane);
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
	DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

	if (node == null)
	    return;

	Object nodeobj = node.getUserObject();
	if (!(nodeobj instanceof Node))
	    return;

	Node mynode = (Node) node.getUserObject();

	Object o = mynode.getObj();
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
		ProjectView pv = new ProjectView(p, ProjectView.T_CHANGE);
		//splitPane.setBottomComponent(pv);
		view_scroll.setViewportView(pv);
	    } catch (Exception e1) {
		Errmsg.errmsg(e1);
		return;
	    }
	}

    }

    private void createNodes(DefaultMutableTreeNode top) {
	
	Collection projects;
	try {
	    projects = TaskModel.getReference().getProjects();
	    Iterator it = projects.iterator();
	    while (it.hasNext()) {
		Project p = (Project) it.next();
		DefaultMutableTreeNode pnode = new DefaultMutableTreeNode(new Node(p.getDescription(), p));
		top.add(pnode);
		Collection tasks = TaskModel.getReference().getTasks(p.getId().intValue());
		Iterator it2 = tasks.iterator();
		while (it2.hasNext()) {
		    Task t = (Task) it2.next();
		    pnode.add(new DefaultMutableTreeNode(new Node("[" + t.getTaskNumber() + "]" + t.getDescription(), t)));
		}
	    }
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}
