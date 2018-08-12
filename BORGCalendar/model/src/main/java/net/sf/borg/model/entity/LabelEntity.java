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

import lombok.Data;



/**
 * This class is a non-persisted entity that is used when the model needs to package
 * a transient, calculated entity for the UI, such as a calculated holiday or birthday based on the
 * address book.
 */
@Data
public class LabelEntity implements CalendarEntity {

	private String color;
	private Date date;
	private String Text;

	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDuration()
	 */
	@Override
	public Integer getDuration() {
		return Integer.valueOf(0);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getNextTodo()
	 */
	@Override
	public Date getNextTodo() {	
		return null;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getTodo()
	 */
	@Override
	public boolean isTodo() {
		return false;
	}

	@Override
	public Integer getPriority() {
		return null;
	}

	@Override
	public boolean isPrivate() {
		return false;
	}
	
	
}
