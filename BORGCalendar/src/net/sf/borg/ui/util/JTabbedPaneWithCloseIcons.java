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
package net.sf.borg.ui.util;

// This file was copied from a forum and was unlicensed.
// It has been modified
// BORG does not apply any copyright to this file.
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

import net.sf.borg.ui.DockableView;

/**
 * A JTabbedPane with Close and Undock icons drawn in the tab.
 */
public class JTabbedPaneWithCloseIcons extends JTabbedPane implements
		MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 1L;

	/**
	 * this is the icon that actually contains the symbols for close and
	 * optionally undock.
	 */
	private class CloseTabIcon implements Icon {

		/**
		 * parent component
		 */
		private Component component;

		/**
		 * is undock icon being shown
		 */
		private boolean undock;

		/** The width. */
		private int width;

		/** The x_pos. */
		private int x_pos;

		/** The y_pos. */
		private int y_pos;

		/**
		 * constructor.
		 * 
		 * @param ud
		 *            if true, also include the undock image
		 */
		public CloseTabIcon(boolean ud) {
			this.undock = ud;

			// undock doubles the width
			if (undock)
				width = 2 * ICON_WIDTH;
			else
				width = ICON_WIDTH;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#getIconHeight()
		 */
		public int getIconHeight() {
			return ICON_WIDTH;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#getIconWidth()
		 */
		public int getIconWidth() {
			return width;
		}

		/**
		 * Checks if mouse is on the delete icon
		 * 
		 * @param e
		 *            the mouse event
		 * 
		 * @return true, if mouse is on the delete icon
		 */
		public boolean isMouseOnDelete(int x, int y) {

			Rectangle rect = new Rectangle(x_pos + 2, y_pos + 2,
					ICON_WIDTH - 4, ICON_WIDTH - 4);
			if (rect.contains(x, y)) {
				return true;
			}

			return false;
		}

		/**
		 * Checks if mouse is on the undock icon
		 * 
		 * @param e
		 *            the mouse event
		 * 
		 * @return true, if mouse is on the undock icon
		 */
		public boolean isMouseOnUndock(int x, int y) {

			if (!undock) {
				return false;
			}

			Rectangle rect = new Rectangle(x_pos + ICON_WIDTH + 2, y_pos + 2,
					ICON_WIDTH - 4, ICON_WIDTH - 4);
			if (rect.contains(x, y)) {
				return true;
			}

			return false;
		}

		/**
		 * Paint the icon with optional red highlight
		 * 
		 * @param highlightDelete
		 *            if true, paint delete highlight
		 * @param highlightUndock
		 *            if true, paint undock highlight
		 * @param g
		 *            the Graphics to paint in
		 */
		private void paintHighlight(boolean highlightDelete,
				boolean highlightUndock, Graphics g) {

			Graphics2D g2 = (Graphics2D) g;

			g2.setColor(Color.black);

			if (highlightDelete) {
				g2.setColor(Color.red);
			}

			// draw the delete picture
			g2.drawLine(x_pos + 4, y_pos + 5, x_pos + ICON_WIDTH - 5, y_pos
					+ ICON_WIDTH - 4);
			g2.drawLine(x_pos + 4, y_pos + 4, x_pos + ICON_WIDTH - 4, y_pos
					+ ICON_WIDTH - 4);
			g2.drawLine(x_pos + 5, y_pos + 4, x_pos + ICON_WIDTH - 4, y_pos
					+ ICON_WIDTH - 5);
			g2.drawLine(x_pos + ICON_WIDTH - 4, y_pos + 5, x_pos + 5, y_pos
					+ ICON_WIDTH - 4);
			g2.drawLine(x_pos + ICON_WIDTH - 4, y_pos + 4, x_pos + 4, y_pos
					+ ICON_WIDTH - 4);
			g2.drawLine(x_pos + ICON_WIDTH - 5, y_pos + 4, x_pos + 4, y_pos
					+ ICON_WIDTH - 5);

			if (undock) {

				g2.setColor(Color.black);
				if (highlightUndock) {
					g2.setColor(Color.red);
				}

				// draw the undock picture
				g2.drawRect(x_pos + ICON_WIDTH + 4, y_pos + 4, ICON_WIDTH - 8,
						ICON_WIDTH - 8);
				g2.drawRect(x_pos + ICON_WIDTH + 5, y_pos + 5, ICON_WIDTH - 10,
						ICON_WIDTH - 10);

			}

		}

		/**
		 * Paint icon with hightlight
		 * 
		 * @param e
		 *            the mouse event
		 */
		public void paintHighlight(MouseEvent e) {
			if (component == null)
				return;

			paintHighlight(isMouseOnDelete(e.getX(), e.getY()),
					isMouseOnUndock(e.getX(), e.getY()), component
							.getGraphics());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#paintIcon(java.awt.Component,
		 * java.awt.Graphics, int, int)
		 */
		public void paintIcon(Component c, Graphics g, int x, int y) {
			this.x_pos = x;
			this.y_pos = y;
			this.component = c;

			/*
			 * use the last known mouse position to decide if highlight is needed
			 */
			if (lastMouseEvent != null)
				paintHighlight(isMouseOnDelete(lastMouseEvent.getX(), lastMouseEvent.getY()), 
						isMouseOnUndock(lastMouseEvent.getX(), lastMouseEvent.getY()), g);
			else
				paintHighlight(false, false, g);	
		}
	}

	/** icon size. */
	static private final int ICON_WIDTH = 16;

	/*
	 * save last mouse event so that we know where we are if the
	 * icon is asked to repaint without the mouse moving
	 */
	private MouseEvent lastMouseEvent;

	/**
	 * Instantiates a new tabbed pane with close icons.
	 */
	public JTabbedPaneWithCloseIcons() {
		super();
		addMouseListener(this);
		this.addMouseMotionListener(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JTabbedPane#addTab(java.lang.String, java.awt.Component)
	 */
	@Override
	public void addTab(String title, Component component) {
		// add the extra undock icon onyl if the component is undockable
		if (component instanceof DockableView)
			super.addTab(title, new CloseTabIcon(true), component);
		else
			super.addTab(title, new CloseTabIcon(false), component);
		this.repaint();
	}

	/**
	 * close all tabs that have close icons.
	 */
	public void closeClosableTabs() {
		for (int i = this.getTabCount() - 1; i >= 0; i--) {
			Icon icon = getIconAt(i);
			if (icon == null)
				return;

			this.removeTabAt(i);
		}
	}

	/**
	 * close the currently selected tab.
	 */
	public void closeSelectedTab() {
		int i = this.getSelectedIndex();
		this.removeTabAt(i);
	}

	/**
	 * handle mouse click on a tab icon.
	 * 
	 * @param e
	 *            the e
	 */
	public void mouseClicked(MouseEvent e) {
		// check is mouse is on a tab
		int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
		if (tabNumber < 0)
			return;

		// get icons
		CloseTabIcon icon = (CloseTabIcon) getIconAt(tabNumber);
		if (icon == null)
			return;

		// perform action of the click was on an icon
		if (icon.isMouseOnDelete(e.getX(), e.getY()))
			this.removeTabAt(tabNumber);
		else if (icon.isMouseOnUndock(e.getX(), e.getY()))
			this.undock();

		lastMouseEvent = e;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	@Override
	public void mouseDragged(MouseEvent arg0) {
	  // empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		mouseMoved(e);
		lastMouseEvent = e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		mouseMoved(e);
		lastMouseEvent = e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {

		// if the mouse is on a tab - then highlight it
		for (int tabNumber = 0; tabNumber < this.getComponentCount(); tabNumber++) {
			CloseTabIcon icon = (CloseTabIcon) getIconAt(tabNumber);
			if (icon == null)
				return;

			icon.paintHighlight(e);
		}
		lastMouseEvent = e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		lastMouseEvent = e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		lastMouseEvent = e;
	}

	/**
	 * undock the currently selected tab.
	 */
	public void undock() {
		Component c = getSelectedComponent();
		if (c != null && c instanceof DockableView) {
			DockableView dv = (DockableView) c;
			dv.openInFrame();
		}
	}
}
