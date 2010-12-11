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
import java.util.Date;

import javax.swing.JPopupMenu;

/**
 * Interface for objects that appear as boxes on the day/week/month UIs
 */
abstract public class Box {

	/**
	 * Interface implemented by Boxes that can be dragged
	 */
	interface Draggable {

		/**
		 * called when object has been moved
		 * 
		 * @param realtime
		 *            time of day in minutes to which the object has been
		 *            dragged. -1 indicates object dragged off of the time grid
		 * @param d
		 *            date that the object was dragged to
		 * @throws Exception
		 */
		public abstract void move(int realtime, Date d) throws Exception;
	}

	protected Rectangle bounds, clip;

	protected boolean isSelected = false;
		
	/**
	 * constructor.
	 * 
	 * @param bounds box bounds
	 * @param clip box clip
	 */
	public Box(Rectangle bounds, Rectangle clip) {
		this.bounds = bounds;
		this.clip = clip;
	}

	/**
	 * delete the box
	 */
	public void delete() {
	  // empty
	}

	/**
	 * draw the box
	 * 
	 * @param g2
	 *            the Graphics2D to draw in
	 * @param comp
	 *            that contains the Graphics2D
	 */
	public abstract void draw(Graphics2D g2, Component comp);

	/**
	 * action called when the box is clicked
	 */
	public abstract void onClick();
	
	/**
	 * how many clicks are required to activate this box - default is double-click
	 * @return clicks required to activate the box
	 */
	public int clicksToActivate()
	{
		return 2;
	}

	/**
	 * get bounds
	 * @return bounds
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * get the box popup menu
	 * 
	 * @return the popup menu
	 */
	public JPopupMenu getMenu() {
		return null;
	}

	/**
	 * get the box text
	 * 
	 * @return the text
	 */
	abstract public String getText();

	/**
	 * set bounds
	 * @param bounds new bounds
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
		if( clip == null )
			clip = bounds;
	}

	/**
	 * set selected
	 * @param isSelected new selected value
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	/**
	 * gets the tool tip text for this box
	 * @return the tool tip text
	 */
	abstract public String getToolTipText();
	

}