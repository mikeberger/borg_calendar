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

public class JTabbedPaneWithCloseIcons extends JTabbedPane implements
		MouseListener, MouseMotionListener {

	static private final int ICON_WIDTH = 16;

	private class CloseTabIcon implements Icon {
		private boolean undock;

		private Component c;

		private int width;

		private int x_pos;

		private int y_pos;

		public CloseTabIcon(boolean ud) {
			this.undock = ud;
			if (ud)
				width = 2 * ICON_WIDTH;
			else
				width = ICON_WIDTH;

		}

		public int getIconHeight() {
			return ICON_WIDTH;
		}

		public int getIconWidth() {
			return width;
		}

		// hide kludges 2 methods below
		public boolean isDeleteClicked(MouseEvent e) {

			Rectangle rect = new Rectangle(x_pos + 2, y_pos + 2,
					ICON_WIDTH - 4, ICON_WIDTH - 4);
			if (rect.contains(e.getX(), e.getY())) {
				return true;
			}

			return false;
		}

		public boolean isUndockClicked(MouseEvent e) {
			if (!undock) // delete only
			{
				return false;
			}
			Rectangle rect = new Rectangle(x_pos + ICON_WIDTH + 2, y_pos + 2,
					ICON_WIDTH - 4, ICON_WIDTH - 4);
			if (rect.contains(e.getX(), e.getY())) {
				return true;
			}

			return false;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			this.x_pos = x;
			this.y_pos = y;
			this.c = c;
			paintHighlight(false, false, g);
		}

		private void paintHighlight(boolean del, boolean ud, Graphics g) {

			Graphics2D g2 = (Graphics2D) g;

			g2.setColor(Color.black);
			
			if (del) {
				g2.setColor(Color.red);
			}
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
				if (ud) {
					g2.setColor(Color.red);
				}
				g2.drawRect(x_pos + ICON_WIDTH + 4, y_pos + 4, ICON_WIDTH - 8,
						ICON_WIDTH - 8);
				g2.drawRect(x_pos + ICON_WIDTH + 5, y_pos + 5, ICON_WIDTH - 10,
						ICON_WIDTH - 10);

			}

		}

		public void paintHighlight(MouseEvent e) {
			paintHighlight(isDeleteClicked(e), isUndockClicked(e), c
					.getGraphics());
		}
	}

	public JTabbedPaneWithCloseIcons() {
		super();
		addMouseListener(this);
		this.addMouseMotionListener(this);

	}

	public void addTab(String title, Component component) {
		// super.addTab(title, new CloseTabIcon(extraIcon), component);
		if (component instanceof DockableView)
			super.addTab(title, new CloseTabIcon(true), component);
		else
			super.addTab(title, new CloseTabIcon(false), component);
		this.repaint();
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
		mouseMoved(e);
	}

	public void mouseExited(MouseEvent e) {
		mouseMoved(e);
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

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		for (int tabNumber = 0; tabNumber < this.getComponentCount(); tabNumber++) {
			CloseTabIcon icon = (CloseTabIcon) getIconAt(tabNumber);
			if (icon == null)
				return;

			icon.paintHighlight(e);
		}

	}
}
