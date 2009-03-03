package net.sf.borg.model.undo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Task;

public class TaskUndoItem extends UndoItem<Task> {

	private Collection<SubtaskUndoItem> subtasks = new ArrayList<SubtaskUndoItem>();

	@Override
	public void executeUndo() {
		try {
			if (action == actionType.DELETE) {
				TaskModel.getReference().savetask(item, true);
				for (SubtaskUndoItem s : subtasks)
				{
					s.item.setTask(item.getTaskNumber());
					s.executeUndo();
				}
			} else if (action == actionType.UPDATE) {
				for (SubtaskUndoItem s : subtasks)
				{
					s.executeUndo();
				}
				TaskModel.getReference().savetask(item, true);
			} else if (action == actionType.ADD) {
					TaskModel.getReference().delete(item.getTaskNumber(), true);
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	private TaskUndoItem() {

	}

	static private String itemString(Task st) {
		return st.getDescription();
	}

	public static TaskUndoItem recordUpdate(Task task) {
		TaskUndoItem undoItem = new TaskUndoItem();
		undoItem.item = task;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getPlainResourceString("Change") + " "
				+ Resource.getPlainResourceString("task") + " "
				+ itemString(task));
		return undoItem;
	}

	public static TaskUndoItem recordAdd(Task task) {
		TaskUndoItem undoItem = new TaskUndoItem();
		undoItem.item = task;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getPlainResourceString("Add") + " "
				+ Resource.getPlainResourceString("task") + " "
				+ itemString(task));
		return undoItem;
	}

	public static TaskUndoItem recordDelete(Task task) {
		TaskUndoItem undoItem = new TaskUndoItem();
		undoItem.item = task;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getPlainResourceString("Delete") + " "
				+ Resource.getPlainResourceString("task") + " "
				+ itemString(task));
		return undoItem;
	}
	
	@SuppressWarnings("unchecked")
	static TaskUndoItem getLastTaskItem()
	{
		Stack<UndoItem> items = UndoLog.getReference().getItems();
		for( int idx = items.size() - 1; idx >= 0; idx-- )
		{
			UndoItem item = items.elementAt(idx);
			if( item instanceof TaskUndoItem )
				return (TaskUndoItem)item;
		}
		
		return null;
	}
	
	void addSubtask(SubtaskUndoItem st)
	{
		subtasks.add(st);
	}

}
