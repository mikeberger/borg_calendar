package net.sf.borg.model.undo;

import java.util.Stack;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Project;

public class ProjectUndoItem extends UndoItem<Project> {

	@Override
	public void executeUndo() {
		try {
			if (action == actionType.DELETE) {
				TaskModel.getReference().saveProject(item, true);
			} else if (action == actionType.UPDATE) {
				TaskModel.getReference().saveProject(item, true);
			} else if (action == actionType.ADD) {
					TaskModel.getReference().deleteProject(item.getId());
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	private ProjectUndoItem() {

	}

	static private String itemString(Project st) {
		return st.getDescription();
	}

	public static ProjectUndoItem recordUpdate(Project project) {
		ProjectUndoItem undoItem = new ProjectUndoItem();
		undoItem.item = project;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getPlainResourceString("Change") + " "
				+ Resource.getPlainResourceString("project") + " "
				+ itemString(project));
		return undoItem;
	}

	public static ProjectUndoItem recordAdd(Project project) {
		ProjectUndoItem undoItem = new ProjectUndoItem();
		undoItem.item = project;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getPlainResourceString("Add") + " "
				+ Resource.getPlainResourceString("project") + " "
				+ itemString(project));
		return undoItem;
	}

	/*
	public static ProjectUndoItem recordDelete(Project project) {
		ProjectUndoItem undoItem = new ProjectUndoItem();
		undoItem.item = project;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getPlainResourceString("Delete") + " "
				+ Resource.getPlainResourceString("project") + " "
				+ itemString(project));
		return undoItem;
	}
	*/
	
	@SuppressWarnings("unchecked")
	static ProjectUndoItem getLastTaskItem()
	{
		Stack<UndoItem> items = UndoLog.getReference().getItems();
		for( int idx = items.size() - 1; idx >= 0; idx-- )
		{
			UndoItem item = items.elementAt(idx);
			if( item instanceof ProjectUndoItem )
				return (ProjectUndoItem)item;
		}
		
		return null;
	}
	
}
