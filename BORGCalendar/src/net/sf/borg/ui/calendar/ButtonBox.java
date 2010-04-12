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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Date;

import javax.swing.Icon;

/**
 * base class for a box that represents a clickable label on the calendar ui
 * sub-classes must implement the onClick method. used for items such as the date
 * and week buttons that let the user navigate to other views
 *
 */
public abstract class ButtonBox extends Box {

	private Date date; // date that the box is associated with

	private Icon icon_ = null; // optinal icon for the label

	private Color backg = null; // background color
	
	private String text = null; // button text

	/**
	 * constructor 
	 * @param d date
	 * @param text label text
	 * @param icon icon or null
	 * @param bounds bounds
	 * @param clip clip
	 */
	public ButtonBox(Date d, String text, Icon icon, Rectangle bounds,
			Rectangle clip) {
		super( bounds, clip );
		date = d;
		this.text = text;
		this.icon_ = icon;
	}

	/**
	 * constructor 
	 * @param d date
	 * @param text label text
	 * @param icon icon or null
	 * @param bounds bounds
	 * @param clip clip
	 * @param b background color
	 */
	public ButtonBox(Date d, String text, Icon icon, Rectangle bounds,
			Rectangle clip, Color b) {
		super( bounds, clip );
		date = d;
		this.text = text;
		this.icon_ = icon;
		this.backg = b;
	}

	/**
	 * get the date
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * draw the button box
	 */
	@Override
	public void draw(Graphics2D g2, Component comp) {

		Shape s = g2.getClip();
		if (clip != null)
			g2.setClip(clip);

		g2.clipRect(bounds.x, 0, bounds.width + 1, 1000);
		if (isSelected == true) {
			g2.setColor(Color.RED);
			g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
		} else if (backg != null) {
			g2.setColor(backg);
			g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
		}
		int smfontHeight = g2.getFontMetrics().getHeight();

		g2.setColor(Color.black);
		g2.drawString(text, bounds.x + 2, bounds.y + smfontHeight);
		if (icon_ != null)
			icon_.paintIcon(null, g2, bounds.x + bounds.width - 16, bounds.y);

		g2.setClip(s);

	}
	
	@Override
	public String getText()
	{
		return text;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	@Override
	public int clicksToActivate() {
		return 1;
	}

}
