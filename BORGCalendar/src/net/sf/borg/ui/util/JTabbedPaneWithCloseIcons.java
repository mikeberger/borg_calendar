package net.sf.borg.ui.util;

// This file was copied from a forum and was unlicensed.
// It has been modified
// BORG does not apply any copyright to this file.
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import net.sf.borg.common.Resource;
import net.sf.borg.ui.DockableView;


public class JTabbedPaneWithCloseIcons extends JTabbedPane implements MouseListener {

    private class CloseTabIcon implements Icon {
	private Icon fileIcon;

	private int height;

	private int width;

	private int x_pos;

	private int y_pos;

	public CloseTabIcon(Icon fileIcon) {
	    this.fileIcon = fileIcon;
	    width = fileIcon.getIconWidth();
	    height = fileIcon.getIconWidth();
	}

	public int getIconHeight() {
	    return 16;
	}

	public int getIconWidth() {
	    return width;
	}

	// hide kludges 2 methods below
	public boolean isDeleteClicked(MouseEvent e) {
	    if (width == 16) // delete only
	    {
		Rectangle rect = new Rectangle(x_pos, y_pos, width, height);
		if (rect.contains(e.getX(), e.getY())) {
		    return true;
		}
	    } else if (width == 32) // undock + delete
	    {
		Rectangle rect = new Rectangle(x_pos + 16, y_pos, 16, height);
		if (rect.contains(e.getX(), e.getY())) {
		    return true;
		}
	    }

	    return false;
	}

	public boolean isUndockClicked(MouseEvent e) {
	    if (width == 16) // delete only
	    {
		return false;
	    } else if (width == 32) // undock + delete
	    {
		Rectangle rect = new Rectangle(x_pos, y_pos, 16, height);
		if (rect.contains(e.getX(), e.getY())) {
		    return true;
		}
	    }

	    return false;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
	    this.x_pos = x;
	    this.y_pos = y;
	    fileIcon.paintIcon(c, g, x, y);
	}
    }

    public JTabbedPaneWithCloseIcons() {
	super();
	addMouseListener(this);
	
    }

    public void addTab(String title, Component component) {
	// super.addTab(title, new CloseTabIcon(extraIcon), component);
	if (component instanceof DockableView)
	    super.addTab(title, new CloseTabIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/tabundock.gif"))),
		    component);
	else
	    super.addTab(title, new CloseTabIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Delete16.gif"))),
		    component);

    }

    public void closeClosableTabs() {
	for (int i = this.getTabCount() - 1; i >= 0; i--) {
	    Icon icon = getIconAt(i);
	    if (icon == null)
		return;

	    this.removeTabAt(i);

	}

    }

    public void mouseClicked(MouseEvent e) {
	int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
	if (tabNumber < 0)
	    return;

	CloseTabIcon icon = (CloseTabIcon) getIconAt(tabNumber);
	if (icon == null)
	    return;
	if (icon.isDeleteClicked(e))
	    this.removeTabAt(tabNumber);
	else if (icon.isUndockClicked(e))
	    this.undock();

    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void undock() {
	Component c = getSelectedComponent();
	if (c != null && c instanceof DockableView) {
	    DockableView dv = (DockableView) c;
	    dv.openInFrame();
	    // removeTabAt(getSelectedIndex());
	}
    }
}

