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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;

/**
 * Task Undo Item.
 */
public class TaskUndoItem extends UndoItem<Task> {

	// collection of subtask undo items
	// subtasks are children of tasks and when a task gets deleted, they are also deleted
	// the subtask undo items are stored as children inside the task ndo item so that
	// the user need not see undo items for every subtask. 
	/** The subtasks. */
	private Collection<SubtaskUndoItem> subtasks = new ArrayList<SubtaskUndoItem>();

	/* (non-Javadoc)
	 * @see net.sf.borg.model.undo.UndoItem#executeUndo()
	 */
	@Override
	public void executeUndo() {
		try {
			if (action == actionType.DELETE) {
				
				// check if parent project exists
				Integer pid = item.getProject();
				if( pid != null )
				{
					Project p = TaskModel.getReference().getProject(pid.intValue());
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
					s.item.setTask(new Integer(item.getKey()));
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
					TaskModel.getReference().delete(item.getKey(), true);
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * Instantiates a new task undo item.
	 */
	private TaskUndoItem() {
	  // empty
	}

	/**
	 * human readable string for this item.
	 * 
	 * @param st the Task
	 * 
	 * @return the string
	 */
	static private String itemString(Task st) {
		return st.getDescription();
	}

	/**
	 * Record a Task update.
	 * 
	 * @param task the task
	 * 
	 * @return the task undo item
	 */
	public static TaskUndoItem recordUpdate(Task task) {
		TaskUndoItem undoItem = new TaskUndoItem();
		undoItem.item = task;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getResourceString("Change") + " "
				+ Resource.getResourceString("task") + " "
				+ itemString(task));
		return undoItem;
	}

	/**
	 * Record a Task add.
	 * 
	 * @param task the task
	 * 
	 * @return the task undo item
	 */
	public static TaskUndoItem recordAdd(Task task) {
		TaskUndoItem undoItem = new TaskUndoItem();
		undoItem.item = task;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getResourceString("Add") + " "
				+ Resource.getResourceString("task") + " "
				+ itemString(task));
		return undoItem;
	}

	/**
	 * Record a Task delete.
	 * 
	 * @param task the task
	 * 
	 * @return the task undo item
	 */
	public static TaskUndoItem recordDelete(Task task) {
		TaskUndoItem undoItem = new TaskUndoItem();
		undoItem.item = task;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getResourceString("Delete") + " "
				+ Resource.getResourceString("task") + " "
				+ itemString(task));
		return undoItem;
	}
	
	/**
	 * Gets the last task item.
	 * 
	 * @return the last task item
	 */
	static TaskUndoItem getLastTaskItem()
	{
		Stack<UndoItem<?>> items = UndoLog.getReference().getItems();
		for( int idx = items.size() - 1; idx >= 0; idx-- )
		{
			UndoItem<?> item = items.elementAt(idx);
			if( TaskUndoItem.class.isInstance(item) )
				return TaskUndoItem.class.cast(item);
		}
		
		return null;
	}
	
	/**
	 * Adds a subtask undo item to this task undo item.
	 * 
	 * @param st the subtask
	 */
	void addSubtask(SubtaskUndoItem st)
	{
		subtasks.add(st);
	}

}
