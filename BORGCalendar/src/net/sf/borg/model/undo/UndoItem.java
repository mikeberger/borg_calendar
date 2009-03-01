package net.sf.borg.model.undo;

public abstract class UndoItem<T> {

	private String description;
	protected actionType action;
	
	protected T item;
	
	protected enum actionType {
		ADD, DELETE, UPDATE
	}
	
	public abstract void executeUndo();

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
}
