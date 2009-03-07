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
		ADD, DELETE, UPDATE
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
