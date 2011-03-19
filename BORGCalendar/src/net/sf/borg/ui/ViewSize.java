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

/**
 * ViewSize contains the data associated with a windows's position, size, and
 * maximization state It also contains the logic to convert this data to and
 * from a String so that it can be stored.
 */
class ViewSize {

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
		if( toks.length > 5)
		{
			if( toks[5].equals("U"))
				vs.alwaysUndock = true;
			else if( toks[5].equals("D"))
				vs.alwaysDock = true;
		}

		return (vs);
	}
	// docking preference overrides
	private boolean alwaysDock = false;
	
	private boolean alwaysUndock = false;
	private int height = -1;
	
	// maximization flag
	private boolean maximized = false;
	
	// size
	private int width = -1;
	// position
	private int x = -1;

	private int y = -1;

	/**
	 * Instantiates a ViewSize
	 */
	public ViewSize() {
	  // empty
	}

	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the x.
	 * 
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * Gets the y position.
	 * 
	 * @return the y position
	 */
	public int getY() {
		return y;
	}

	/**
	 * Checks if is always dock.
	 *
	 * @return true, if is always dock
	 */
	public boolean isAlwaysDock() {
		return alwaysDock;
	}

	/**
	 * Checks if is always undock.
	 *
	 * @return true, if is always undock
	 */
	public boolean isAlwaysUndock() {
		return alwaysUndock;
	}

	/**
	 * Checks if is maximized.
	 * 
	 * @return true, if is maximized
	 */
	public boolean isMaximized() {
		return maximized;
	}

	/**
	 * Sets the always dock.
	 *
	 * @param alwaysDock the new always dock
	 */
	public void setAlwaysDock(boolean alwaysDock) {
		this.alwaysDock = alwaysDock;
	}

	/**
	 * Sets the always undock.
	 *
	 * @param alwaysUndock the new always undock
	 */
	public void setAlwaysUndock(boolean alwaysUndock) {
		this.alwaysUndock = alwaysUndock;
	}

	/**
	 * Sets the height.
	 * 
	 * @param height the new height
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Sets the maximized flag.
	 * 
	 * @param maximized the new maximized value
	 */
	public void setMaximized(boolean maximized) {
		this.maximized = maximized;
	}

	/**
	 * Sets the width.
	 * 
	 * @param width the new width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Sets the x position.
	 * 
	 * @param x the new x position
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Sets the y.
	 * 
	 * @param y the new y
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * get the ViewSize data in a string
	 * @return the string
	 */
	@Override
	public String toString() {
		String dock = "X";
		if( alwaysUndock == true)
			dock = "U";
		else if( alwaysDock == true )
			dock = "D";
		return (Integer.toString(x) + "," + Integer.toString(y) + ","
				+ Integer.toString(width) + "," + Integer.toString(height)
				+ "," + ((maximized == true) ? "Y" : "N") + "," + dock);
	}
}
