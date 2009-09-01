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
package net.sf.borg.ui.calendar;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPopupMenu;

/**
 * Interface for objects that appear as boxes on the day/week/month UIs
 */
public interface Box {

	/**
	 * delete the box
	 */
    public abstract void delete();

    /**
     * draw the box
     * @param g2 the Graphics2D to draw in
     * @param component that contains the Graphics2D
     */
    public abstract void draw(Graphics2D g2, Component comp);

    /**
     * edit the box
     */
    public abstract void edit();

    /**
     * get the box bounds
     * @return the box bounds
     */
    public abstract Rectangle getBounds();

    /**
     * get the box popup menu
     * @return the popup menu
     */
    public abstract JPopupMenu getMenu();

    /**
     * get the box text
     * @return the text
     */
    public abstract String getText();

    /**
     * set the box bounds (resize)
     * @param bounds the new bounds
     */
    public abstract void setBounds(Rectangle bounds);
    
    /**
     * set the boxes selected status
     * @param isSelected true is selected
     */
    public abstract void setSelected(boolean isSelected);

}