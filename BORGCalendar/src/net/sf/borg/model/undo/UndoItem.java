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

import net.sf.borg.model.beans.KeyedBean;

/**
 * Abstract base class for holding a single item of work that can be undone
 */
public abstract class UndoItem<T extends KeyedBean<T>> {

	private String description;
	protected actionType action;
	
	// the KeyedBean that was updated and that can be rolled back
	protected T item;
	
	// actions that can be applied to an item that need to be undone
	protected enum actionType {
		ADD, DELETE, UPDATE, MOVE
	}
	
	/**
	 * execute the undo action on the item
	 */
	public abstract void executeUndo();

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
}
