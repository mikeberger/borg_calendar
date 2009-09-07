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
 
Copyright 2008 by Mike Berger
 */
package net.sf.borg.ui.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * A factory for creating GridBagConstraints objects in a standard way. Saves lots
 * of LOC.
 */
public class GridBagConstraintsFactory {

	static private final Insets defaultInsets = new Insets(4, 4, 4, 4);

	/**
	 * Creates GridBagConstraints
	 * 
	 * @param x the x
	 * @param y the y
	 * 
	 * @return the grid bag constraints
	 */
	public static GridBagConstraints create(int x, int y) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.insets = defaultInsets;
		return gbc;
	}

	/**
	 * Creates GridBagConstraints
	 * 
	 * @param x the x
	 * @param y the y
	 * @param fill the fill
	 * 
	 * @return the grid bag constraints
	 */
	public static GridBagConstraints create(int x, int y, int fill) {
		GridBagConstraints gbc = create(x, y);
		gbc.fill = fill;
		return gbc;
	}

	/**
	 * Creates GridBagConstraints
	 * 
	 * @param x the x
	 * @param y the y
	 * @param fill the fill
	 * @param weightx the weightx
	 * @param weighty the weighty
	 * 
	 * @return the grid bag constraints
	 */
	public static GridBagConstraints create(int x, int y, int fill,
			double weightx, double weighty) {
		GridBagConstraints gbc = create(x, y, fill);
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		return gbc;
	}

}
