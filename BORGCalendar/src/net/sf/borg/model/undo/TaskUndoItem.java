package net.sf.borg.model.undo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.Task;

public class TaskUndoItem extends UndoItem<Task> {

	// collection of subtask undo items
	// subtasks are children of tasks and when a task gets deleted, they are also deleted
	// the subtask undo items are stored as children inside the task ndo item so that
	// the user need not see undo items for every subtask. 
	private Collection<SubtaskUndoItem> subtasks = new ArrayList<SubtaskUndoItem>();

	@Override
	public void executeUndo() {
		try {
			if (action == actionType.DELETE) {
				
				// check if parent project exists
				Integer pid = item.getProject();
				if( pid != null )
				{
					Project p = TaskModel.getReference().getProject(pid);
					if( p == null )
					{
						// if the project has been deleted
						// and we are adding back a task - we need to set
						// its parent to null
						item.setProject(null);
					}
						
				}
				
				// recreate the task
				TaskModel.getReference().savetask(item, true);
				
				// add back the subtasks
				for (SubtaskUndoItem s : subtasks)
				{
					s.item.setTask(item.getTaskNumber());
					s.executeUndo();
				}
			} else if (action == actionType.UPDATE) {
				
				// BORG updates subtasks during the same transaction that updates the
				// task, so rolling back a task needs to include a rollback of any subtask
				// updates
				for (SubtaskUndoItem s : subtasks)
				{
					s.executeUndo();
				}
				TaskModel.getReference().savetask(item, true);
			} else if (action == actionType.ADD) {
				
				// we don't need to delete the subtasks separately
				// there is a cascading delete when the task is deleted
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
	/**
	 * this method gets the last task undo item. It is called when a subtask is changed.
	 * Subtasks are always changed during a task save. The way the task model is written,
	 * the subtasks are updated after the task - so when the subtask undo items are created,
	 * they need to find the last task item to be associted with. There is always a task undo item associated
	 * with a subtask undo
	 */
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
