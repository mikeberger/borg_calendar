package net.sf.borg.model.undo;

public abstract class UndoItem {

	private String description;
	
	public abstract void executeUndo();

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
}
