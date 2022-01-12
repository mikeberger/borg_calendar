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
package net.sf.borg.ui;

import lombok.Data;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

import javax.swing.*;
import java.awt.*;

/**
 * ViewSize contains the data associated with a windows's position, size, and
 * maximization state It also contains the logic to convert this data to and
 * from a String so that it can be stored.
 */
@Data
class ViewSize {

	/**
	 * DockType stores the various dock statuses for a view - always DOCK, always
	 * UNDOCK, or NOT_SET - whoch means to use the default preference
	 */
	static public enum DockType {
		DOCK, UNDOCK
	}

	/**
	 * store the views size and position.
	 * 
	 * @param resize true if the window is being resized
	 */
	static private void recordSize(JFrame c, PrefName pn, boolean resize) {
		String s = Prefs.getPref(pn);
		ViewSize vsnew = ViewSize.fromString(s);

		if (!resize) {
			// for a move, ignore the event if the size changes - this is
			// part of a maximize or minimize and the resize will follow
			if (vsnew.getHeight() != c.getBounds().height || vsnew.getWidth() != c.getBounds().width)
				return;

			// if x or y < 0, then this is likely the move before a maximize
			// so ignore it
			if (c.getBounds().x < 0 || c.getBounds().y < 0)
				return;

			vsnew.setX(c.getBounds().x);
			vsnew.setY(c.getBounds().y);
			vsnew.setWidth(c.getBounds().width);
			vsnew.setHeight(c.getBounds().height);
		} else if (c.getExtendedState() == Frame.MAXIMIZED_BOTH) {
			vsnew.setMaximized(true);
		} else {
			// only reset bounds if we are not maximized
			vsnew.setMaximized(false);
			vsnew.setX(c.getBounds().x);
			vsnew.setY(c.getBounds().y);
			vsnew.setWidth(c.getBounds().width);
			vsnew.setHeight(c.getBounds().height);

		}

		Prefs.putPref(pn, vsnew.toString());

	}

	/**
	 * Sets the window size and position from the stored preference and then sets up
	 * listeners to store any updates to the window size and position based on user
	 * actions.
	 * 
	 * @param pname the preference name
	 * @return the ViewSize object, which may be of use to the caller
	 */
	static void manageMySize(JFrame frame, PrefName pname) {

		// set the initial size
		String s = Prefs.getPref(pname);
		ViewSize vs = ViewSize.fromString(s);
		vs.setDock(DockType.UNDOCK);
		Prefs.putPref(pname, vs.toString());

		// get dimensions from pref or use defaults
		int x = (vs.getX() != -1) ? vs.getX() : 0;
		int y = (vs.getY() != -1) ? vs.getY() : 0;
		int w = (vs.getWidth() != -1) ? vs.getWidth() : 800;
		int h = (vs.getHeight() != -1) ? vs.getHeight() : 600;

		// determine if the window is offscreen
		boolean isOffscreen = true;
		GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		for (GraphicsDevice device : devices) {
			if (device.getDefaultConfiguration().getBounds().contains(x, y)) {
				isOffscreen = false;
				break;
			}
		}
		
		if( isOffscreen )
		{
			x = 0; y = 0;
		}

		frame.setBounds(new Rectangle(x, y, w, h));

		if (vs.isMaximized()) {
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		}

		frame.validate();

		final PrefName pn = pname;

		// add listeners to record any changes
		frame.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentMoved(java.awt.event.ComponentEvent e) {
				recordSize((JFrame) e.getComponent(), pn, false);
			}

			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				recordSize((JFrame) e.getComponent(), pn, true);
			}
		});

	}

	/**
	 * creates a ViewSize instance From a string.
	 * 
	 * @param s the string
	 * 
	 * @return the ViewSize instance
	 */
	static public ViewSize fromString(String s) {
		ViewSize vs = new ViewSize();
		String toks[] = s.split(",");
		vs.x = Integer.parseInt(toks[0]);
		vs.y = Integer.parseInt(toks[1]);
		vs.width = Integer.parseInt(toks[2]);
		vs.height = Integer.parseInt(toks[3]);
		if (toks[4].equals("Y"))
			vs.maximized = true;
		else
			vs.maximized = false;
		if (toks.length > 5) {
			try {
				vs.dock = DockType.valueOf(toks[5]);
			} catch (Exception e) {
				vs.dock = DockType.DOCK;
			}
		}

		return (vs);
	}

	// size and position
	private int height = -1;
	private int width = -1;
	private int x = -1;
	private int y = -1;
	private boolean maximized = false;

	// state
	private DockType dock = DockType.DOCK;

	/**
	 * get the ViewSize data in a string
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {

		return (Integer.toString(x) + "," + Integer.toString(y) + "," + Integer.toString(width) + ","
				+ Integer.toString(height) + "," + ((maximized == true) ? "Y" : "N") + "," + dock.toString());
	}
}
