package net.sf.borg.ui.util;

// This file was copied from a forum and was unlicensed.
// BORG does not apply any copyright to this file.
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

/**
 * A JTabbedPane which has a close ('X') icon on each tab.
 * 
 * To add a tab, use the method addTab(String, Component)
 * 
 * To have an extra icon on each tab (e.g. like in JBuilder, showing the file
 * type) use the method addTab(String, Component, Icon). Only clicking the 'X'
 * closes the tab.
 */
public class JTabbedPaneWithCloseIcons extends JTabbedPane implements MouseListener {
    public JTabbedPaneWithCloseIcons() {
	super();
	addMouseListener(this);
    }

    public void addTab(String title, Component component) {
	this.addTab(title, component, null);
    }

    public void addTab(String title, Component component, Icon extraIcon) {
	super.addTab(title, new CloseTabIcon(extraIcon), component);
    }

    public void mouseClicked(MouseEvent e) {
	int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
	if (tabNumber < 0)
	    return;
	Icon icon = getIconAt(tabNumber);
	if (icon == null)
	    return;
	Rectangle rect = ((CloseTabIcon) icon).getBounds();
	if (rect.contains(e.getX(), e.getY())) {
	    // the tab is being closed
	    this.removeTabAt(tabNumber);
	}
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}

/**
 * The class which generates the 'X' icon for the tabs. The constructor accepts
 * an icon which is extra to the 'X' icon, so you can have tabs like in
 * JBuilder. This value is null if no extra icon is required.
 */
class CloseTabIcon implements Icon {
    private int x_pos;

    private int y_pos;

    private int width;

    private int height;

    private Icon fileIcon;

    public CloseTabIcon(Icon fileIcon) {
	this.fileIcon = fileIcon;
	width = 16;
	height = 16;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
	this.x_pos = x;
	this.y_pos = y;

	if (fileIcon != null) {
	    fileIcon.paintIcon(c, g, x, y);
	} else {
	    Color col = g.getColor();

	    g.setColor(Color.black);
	    int y_p = y + 2;
	    g.drawLine(x + 1, y_p, x + 12, y_p);
	    g.drawLine(x + 1, y_p + 13, x + 12, y_p + 13);
	    g.drawLine(x, y_p + 1, x, y_p + 12);
	    g.drawLine(x + 13, y_p + 1, x + 13, y_p + 12);
	    g.drawLine(x + 3, y_p + 3, x + 10, y_p + 10);
	    g.drawLine(x + 3, y_p + 4, x + 9, y_p + 10);
	    g.drawLine(x + 4, y_p + 3, x + 10, y_p + 9);
	    g.drawLine(x + 10, y_p + 3, x + 3, y_p + 10);
	    g.drawLine(x + 10, y_p + 4, x + 4, y_p + 10);
	    g.drawLine(x + 9, y_p + 3, x + 3, y_p + 9);
	    g.setColor(col);
	}
    }

    public int getIconWidth() {
	return width;// + (fileIcon != null ? fileIcon.getIconWidth() : 0);
    }

    public int getIconHeight() {
	return height;
    }

    public Rectangle getBounds() {
	return new Rectangle(x_pos, y_pos, width, height);
    }
}
