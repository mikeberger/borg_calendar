
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

import javax.swing.JTable;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.borg.ui.ResourceHelper;

/**
 * Helps create and manage context menus for a JTable.
 * @author membar
 */
public class PopupMenuHelper
{
////////////////////////////////////////////////////////
// nested class Entry
		
public static class Entry
{
	public Entry(ActionListener listener, String resourceKey)
	{
		this.listener = listener;
		this.resourceKey = resourceKey;
	}
	
	public final ActionListener getListener()
	{
		return listener;
	}
	
	public final String getResourceKey()
	{
		return resourceKey;
	}
	
	// private //
	private String resourceKey;
	private ActionListener listener;
}
		
// end nested class Entry
////////////////////////////////////////////////////////

public PopupMenuHelper(final JTable table, Entry[] entries)
{
	this.table = table;
	
	JMenuItem mnuitm;
	mnuitms = new JMenuItem[entries.length];
	popup = new JPopupMenu();
	
	for (int i=0; i<entries.length; ++i)
	{
		Entry entry = entries[i];
		popup.add(mnuitm = new JMenuItem());
		ResourceHelper.setText(mnuitm, entry.getResourceKey());
		mnuitm.addActionListener(entry.getListener());
		mnuitms[i] = mnuitm;
	}

	table.addMouseListener(new MyPopupListener());
	table.addKeyListener
	(
		new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				switch (e.getKeyCode())
				{
				case 0x020D:
					// this is the KeyEvent.VK_CONTEXT_MENU under JDK 1.5
					int[] selIndices = table.getSelectedRows();
					if (selIndices.length == 0) return;
					int rowIndex = selIndices[0];
					Rectangle rct = table.getCellRect(rowIndex,0,false);
					popup.show(table, rct.x, rct.y + rct.height);
					break;
				}
			}
		}
	);
}

public final JMenuItem getMenuItemAt(int index) // NO_UCD
{
	return mnuitms[index];
}

// IContextMenu overrides
public final void setEnabled(int index, boolean enabled) // NO_UCD
{
	mnuitms[index].setEnabled(enabled);
}

// private //
private JTable table;
private JPopupMenu popup;
private JMenuItem[] mnuitms;

////////////////////////////////////////////////////////
// inner class MyPopupListener

private class MyPopupListener extends MouseAdapter
{
	// MouseAdapter overrides
	public void mousePressed(MouseEvent e)
	{
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e)
	{
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			// If the row we're right-clicking on isn't selected, select
			// only that row.
			int row = table.rowAtPoint(e.getPoint());
			if (row!=-1 && !table.isRowSelected(row))
			{
				table.getSelectionModel().setSelectionInterval(row,row);
			}
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}

// end inner class MyPopupListener
////////////////////////////////////////////////////////
}
