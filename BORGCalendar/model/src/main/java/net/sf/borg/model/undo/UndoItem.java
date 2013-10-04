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
package net.sf.borg.model.undo;


/**
 * Abstract base class for holding a single item of work that can be undone.
 */
public abstract class UndoItem<T> {

	/** The description of the event that can be undone. */
	private String description;

	/** The action that was taken. */
	protected actionType action;

	/** the KeyedEntity that can be undone */
	protected T item;

	/**
	 * actions that can be applied to an item that need to be undone
	 */
	protected enum actionType {

		ADD, DELETE, UPDATE
	}

	/**
	 * execute the undo action on the item.
	 */
	public abstract void executeUndo();

	/**
	 * Sets the undo item description.
	 * 
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the undo item description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
