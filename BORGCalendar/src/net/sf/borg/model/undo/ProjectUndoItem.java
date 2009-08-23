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

import java.util.Stack;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Project;

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
