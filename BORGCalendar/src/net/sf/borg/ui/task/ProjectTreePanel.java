package net.sf.borg.ui.task;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Model;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.Task;

public class ProjectTreePanel extends JPanel implements TreeSelectionListener, MouseListener, Model.Listener {
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

    private JPopupMenu projmenu = new JPopupMenu();

    private JPopupMenu taskmenu = new JPopupMenu();

    private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private JScrollPane treeView = null;

    private JTree tree = null;

    private JScrollPane view_scroll = new JScrollPane(new JPanel());

    public ProjectTreePanel() {
	super(new GridLayout(0, 1));
	TaskModel.getReference().addListener(this);
	// Create the nodes.
	DefaultMutableTreeNode top = new DefaultMutableTreeNode(Resource.getPlainResourceString("projects"));
	createNodes(top);
	// Create a tree that allows one selection at a time.
	tree = new JTree(top);
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	// Listen for when the selection changes.
	tree.addTreeSelectionListener(this);

	// Create the scroll pane and add the tree to it.
	treeView = new JScrollPane(tree);

	// Add the scroll panes to a split pane.
	splitPane.setTopComponent(treeView);
	splitPane.setBottomComponent(view_scroll);

	Dimension minimumSize = new Dimension(200, 50);
	view_scroll.setMinimumSize(minimumSize);
	treeView.setMinimumSize(minimumSize);
	splitPane.setDividerLocation(200);
	add(splitPane);
	tree.addMouseListener(this);
	JMenuItem jm = projmenu.add(Resource.getPlainResourceString("Add") + " " + Resource.getPlainResourceString("project"));
	jm.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		add_project();
	    }

	});
	JMenuItem jm2 = taskmenu.add(Resource.getPlainResourceString("Add") + " " + Resource.getPlainResourceString("task"));
	jm2.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		add_task();
	    }

	});
    }

    private void add_project() {
	ProjectView pv;
	try {
	    pv = new ProjectView(null, ProjectView.T_ADD);
	    view_scroll.setViewportView(pv);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
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
    
    private Object getSelectedObject()
    {
	DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	if (node == null)
	    return null;

	Object nodeobj = node.getUserObject();
	if (!(nodeobj instanceof Node))
	    return null;

	Node mynode = (Node) node.getUserObject();

	Object o = mynode.getObj();
	
	return o;
    }

    public void mouseClicked(MouseEvent e) {
	if (e.getButton() == MouseEvent.BUTTON1)
	    return;

	TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
	DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
	if (node == null)
	    return;

	tree.setSelectionPath(selPath);
	Object nodeobj = node.getUserObject();
	if (!(nodeobj instanceof Node)) {
	    projmenu.show(this, e.getX(), e.getY());
	    return;
	}

	Node mynode = (Node) node.getUserObject();

	Object o = mynode.getObj();
	if (o == null)
	    return;
	if (o instanceof Project)
	    taskmenu.show(this, e.getX(), e.getY());

    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent arg0) {
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
		ProjectView pv = new ProjectView(p, ProjectView.T_CHANGE);
		// splitPane.setBottomComponent(pv);
		view_scroll.setViewportView(pv);
	    } catch (Exception e1) {
		Errmsg.errmsg(e1);
		return;
	    }
	}

    }

    public void refresh() {
	DefaultMutableTreeNode top = new DefaultMutableTreeNode(Resource.getPlainResourceString("projects"));
	createNodes(top);
	// Create a tree that allows one selection at a time.
	tree = new JTree(top);
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	// Listen for when the selection changes.
	tree.addTreeSelectionListener(this);
	treeView.setViewportView(tree);
	tree.addMouseListener(this);
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
	    Errmsg.errmsg(e);
	}

    }

    public void remove() {

    }

    // Set the icon for leaf nodes.
    /*
     * ImageIcon leafIcon = createImageIcon("images/middle.gif"); if (leafIcon !=
     * null) { DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
     * renderer.setLeafIcon(leafIcon); tree.setCellRenderer(renderer); } else {
     * System.err.println("Leaf icon missing; using default."); }
     */

}
