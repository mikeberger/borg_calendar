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
package net.sf.borg.model.entity;

import java.util.Date;



public class LabelEntity implements CalendarEntity {

	private String color;
	private Date date;
	private String Text;
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getText() {
		return Text;
	}
	public void setText(String text) {
		Text = text;
	}
	public Integer getDuration() {
		return new Integer(0);
	}
	public Date getNextTodo() {	
		return null;
	}
	public boolean getTodo() {
		return false;
	}
	
	
}
