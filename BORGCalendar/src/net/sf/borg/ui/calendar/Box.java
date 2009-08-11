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

public interface Box {

    public abstract void delete();

    public abstract void draw(Graphics2D g2, Component comp);

    public abstract void edit();

    public abstract Rectangle getBounds();

    public abstract String getText();

    public abstract void setBounds(Rectangle bounds);

    public abstract void setSelected(boolean isSelected);
    
    public abstract JPopupMenu getMenu();

}