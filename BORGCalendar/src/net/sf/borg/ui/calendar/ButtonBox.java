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
import javax.swing.JPopupMenu;

// ApptDayBox holds the logical information needs to determine
// how an appointment box should be drawn in a day grid
public abstract class ButtonBox implements Box {

    private Rectangle bounds, clip;

    private Date date; // date being displayed - not necessarily date of

    private String text;

    private boolean isSelected = false;
    
    private Icon icon_ = null;
    
    private Color backg = null;

    public ButtonBox(Date d, String text, Icon icon, Rectangle bounds, Rectangle clip) {
	date = d;
	this.text = text;
	this.bounds = bounds;
	this.clip = clip;
	this.icon_ = icon;
    }
    
    public ButtonBox(Date d, String text, Icon icon, Rectangle bounds, Rectangle clip, Color b) {
	date = d;
	this.text = text;
	this.bounds = bounds;
	this.clip = clip;
	this.icon_ = icon;
	this.backg = b;
    }

    public void delete() {

    }

    public Date getDate() {
	return date;
    }

    public void draw(Graphics2D g2, Component comp) {

	Shape s = g2.getClip();
	if (clip != null)
	    g2.setClip(clip);

	g2.clipRect(bounds.x, 0, bounds.width + 1, 1000);
	if (isSelected == true) {
	    g2.setColor(Color.RED);
	    g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
	}
	else if( backg != null )
	{
	    g2.setColor(backg);
	    g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
	}
	int smfontHeight = g2.getFontMetrics().getHeight();

	g2.setColor(Color.black);
	g2.drawString(text, bounds.x + 2, bounds.y + smfontHeight);
	if( icon_ != null )
	    icon_.paintIcon(null, g2, bounds.x + bounds.width - 16, bounds.y );

	g2.setClip(s);

    }

    public Rectangle getBounds() {
	return bounds;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#getText()
     */
    public String getText() {
	return null;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#setBounds(java.awt.Rectangle)
     */
    public void setBounds(Rectangle bounds) {
	this.bounds = bounds;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#setSelected(boolean)
     */
    public void setSelected(boolean isSelected) {
	this.isSelected = isSelected;
    }

    public JPopupMenu getMenu() {
	return null;
    }
}
