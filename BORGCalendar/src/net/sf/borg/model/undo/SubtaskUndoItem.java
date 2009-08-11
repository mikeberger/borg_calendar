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

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Subtask;

public class SubtaskUndoItem extends UndoItem<Subtask> {

	@Override
	public void executeUndo() {
		try {
			if (action == actionType.DELETE) {
				TaskModel.getReference().saveSubTask(item, true);
			} else if (action == actionType.UPDATE) {
				TaskModel.getReference().saveSubTask(item, true);
			} else if (action == actionType.ADD) {
				TaskModel.getReference().deleteSubTask(item.getId(),true);
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	private SubtaskUndoItem() {

	}

	static private String itemString(Subtask st) {
		return st.getDescription();
	}

	public static SubtaskUndoItem recordUpdate(Subtask subtask) {
		SubtaskUndoItem undoItem = new SubtaskUndoItem();
		undoItem.item = subtask;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getPlainResourceString("Change") + " "
				+ Resource.getPlainResourceString("subtask") + " "
				+ itemString(subtask));
		
		TaskUndoItem ti = TaskUndoItem.getLastTaskItem();
		if( ti != null )
			ti.addSubtask(undoItem);
		return undoItem;
	}

	public static SubtaskUndoItem recordAdd(Subtask subtask) {
		SubtaskUndoItem undoItem = new SubtaskUndoItem();
		undoItem.item = subtask;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getPlainResourceString("Add") + " "
				+ Resource.getPlainResourceString("subtask") + " "
				+ itemString(subtask));
		TaskUndoItem ti = TaskUndoItem.getLastTaskItem();
		if( ti != null )
			ti.addSubtask(undoItem);
		return undoItem;
	}

	public static SubtaskUndoItem recordDelete(Subtask subtask) {
		SubtaskUndoItem undoItem = new SubtaskUndoItem();
		undoItem.item = subtask;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getPlainResourceString("Delete") + " "
				+ Resource.getPlainResourceString("subtask") + " "
				+ itemString(subtask));
		TaskUndoItem ti = TaskUndoItem.getLastTaskItem();
		if( ti != null )
			ti.addSubtask(undoItem);
		return undoItem;
	}

}
