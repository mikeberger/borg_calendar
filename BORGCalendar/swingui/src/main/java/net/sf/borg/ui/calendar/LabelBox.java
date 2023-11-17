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

import net.sf.borg.model.Theme;
import net.sf.borg.model.entity.LabelEntity;

import java.awt.*;

/**
 * A Box that shows a text label on the calendar. It cannot be edited, moved, or deleted.
 * It is associated with a label entity, such as a hard-coded holiday or birthday
 */
class LabelBox extends Box {

	/**
	 * the model entity that the LabelBox is showing
	 */
	private LabelEntity entity = null;
	
	/**
	 * constructor 
	 * @param ap calendar entity associated with this box
	 * @param bounds bounds
	 * @param clip clip
	 */
	public LabelBox(LabelEntity ap, Rectangle bounds, Rectangle clip) {
		super(bounds, clip);
		entity = ap;
	}

	@Override
	public void draw(Graphics2D g2, Component comp) {

		Shape s = g2.getClip();
		if (clip != null)
			g2.setClip(clip);

		int smfontHeight = g2.getFontMetrics().getHeight();
		Theme t = Theme.getCurrentTheme();

		if (isSelected == true) {
			g2.setColor(new Color(t.getDefaultFg()));
			g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
		}

		if (isSelected == true)
			g2.setColor(new Color(t.getDefaultBg()));
		else
			g2.setColor(new Color(t.colorFromString(getTextColor())));

		g2.drawString(getText(), bounds.x + 2, bounds.y + smfontHeight);
		g2.setColor(Color.black);

		g2.setClip(s);

	}


	@Override
	public String getText() {
		return entity.getText();
	}

	/**
	 * get the text color of the associated entity
	 * @return the text color or null
	 */
	private String getTextColor() {
		if (entity == null)
			return null;

		return entity.getColor();
	}

	@Override
	public void onClick(){
	  //empty
	}

	@Override
	public String getToolTipText() {
		return getText();
	}

}
