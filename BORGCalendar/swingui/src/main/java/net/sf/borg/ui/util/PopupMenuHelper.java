/*
 This file is part of BORG.

 BORG is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 BORG is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with BORG; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Copyright 2003 by Mike Berger
 */

package net.sf.borg.ui.util;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import net.sf.borg.common.Resource;

/**
 * Helps create and manage context menus for a JTable.
 * 
 * @author membar
 */
public class PopupMenuHelper {

	/**
	 * Holds the Listener and ResourceKey for a Popup Menu Item
	 */
	public static class Entry {

		/** The listener. */
		private ActionListener listener;

		/** The resource key. */
		private String resourceKey;

		/**
		 * Instantiates a new entry.
		 * 
		 * @param listener
		 *            the listener
		 * @param resourceKey
		 *            the resource key
		 */
		public Entry(ActionListener listener, String resourceKey) {
			this.listener = listener;
			this.resourceKey = resourceKey;
		}

		/**
		 * Gets the listener.
		 * 
		 * @return the listener
		 */
		public final ActionListener getListener() {
			return listener;
		}

		/**
		 * Gets the resource key.
		 * 
		 * @return the resource key
		 */
		public final String getResourceKey() {
			return resourceKey;
		}
	}

	/**
	 * mouse adapter for popping up the popup menu on right click for the
	 * selected row
	 */
	private class MyPopupListener extends MouseAdapter {

		/**
		 * show the popup menu if needed
		 * 
		 * @param e
		 *            the mouse event
		 */
		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				// If the row we're right-clicking on isn't selected, select
				// only that row.
				if (component instanceof JTable) {
					JTable t = (JTable) component;
					int row = t.rowAtPoint(e.getPoint());
					if (row != -1 && !t.isRowSelected(row)) {
						t.getSelectionModel().setSelectionInterval(row, row);
					}
				}
				if (component instanceof JList) {
					JList<?> t = (JList<?>) component;
					int index = t.locationToIndex(e.getPoint());
					if (index != -1 && !t.isSelectedIndex(index)) {
						t.setSelectedIndex(index);
					}
				}
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		// MouseAdapter overrides
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}
	}


	/** The popup menu. */
	private JPopupMenu popup;

	/** The table. */
	private JComponent component;

	static public JPopupMenu createPopupMenu(Entry[] entries) {
		
		JPopupMenu p = new JPopupMenu();

		// create menu items from the entries
		for (int i = 0; i < entries.length; ++i) {
			Entry entry = entries[i];
			JMenuItem mnuitm = new JMenuItem();
			p.add(mnuitm);
			mnuitm.setText(Resource.getResourceString(entry.getResourceKey()));
			mnuitm.addActionListener(entry.getListener());
		}
		
		return p;
	}

	/**
	 * Instantiates a new popup menu helper.
	 * 
	 * @param table
	 *            the table
	 * @param entries
	 *            the entries
	 */
	public PopupMenuHelper(final JComponent c, Entry[] entries) {
		this.component = c;

		popup = createPopupMenu(entries);

		// listen for mouse events
		c.addMouseListener(new MyPopupListener());

		// listen for context menu key event
		c.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case 0x020D:
					// this is the KeyEvent.VK_CONTEXT_MENU under JDK 1.5
					if (component instanceof JTable) {
						JTable table = (JTable) component;
						int[] selIndices = table.getSelectedRows();
						if (selIndices.length == 0)
							return;
						int rowIndex = selIndices[0];
						Rectangle rct = table.getCellRect(rowIndex, 0, false);
						popup.show(c, rct.x, rct.y + rct.height);
					} else if (component instanceof JList) {
						JList<?> t = (JList<?>) component;
						int index = t.getSelectedIndex();
						if (index == -1)
							return;
						Rectangle rct = t.getCellBounds(index, index);
						popup.show(c, rct.x, rct.y + rct.height);
					}

					break;

				}
			}
		});
	}

}
