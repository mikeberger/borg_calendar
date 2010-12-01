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

/**
 * Interface that needs to be implemented by any entity that can appear on the Calendar.
 * This interface would more properly belong in the UI package - but it would take some extra wrapper
 * classes that would be a waste.
 */
public interface CalendarEntity {

	/**
	 * Gets the text.
	 * 
	 * @return the text
	 */
	public String getText();
	
	/**
	 * Gets the color.
	 * 
	 * @return the color
	 */
	public String getColor();
	
	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public Date getDate();
	
	/**
	 * Gets the duration.
	 * 
	 * @return the duration
	 */
	public Integer getDuration();	
	
	/**
	 * Gets the todo flag.
	 * 
	 * @return the todo flag
	 */
	public boolean getTodo();
	
	/**
	 * Gets the next todo date.
	 * 
	 * @return the next todo date
	 */
	public Date getNextTodo();
	
	/**
	 * Gets the priority
	 * @return the priority
	 */
	public Integer getPriority();
	
	
}
