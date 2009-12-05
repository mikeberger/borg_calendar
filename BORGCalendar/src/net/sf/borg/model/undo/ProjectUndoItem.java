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
import net.sf.borg.model.entity.Project;

/**
 * Project Undo Item.
 */
public class ProjectUndoItem extends UndoItem<Project> {

	/* (non-Javadoc)
	 * @see net.sf.borg.model.undo.UndoItem#executeUndo()
	 */
	@Override
	public void executeUndo() {
		try {
			if (action == actionType.DELETE) {
				TaskModel.getReference().saveProject(item, true);
			} else if (action == actionType.UPDATE) {
				TaskModel.getReference().saveProject(item, true);
			} else if (action == actionType.ADD) {
					TaskModel.getReference().deleteProject(item.getKey());
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * Instantiates a new project undo item.
	 */
	private ProjectUndoItem() {
	  // empty
	}

	/**
	 * human readable string for this item.
	 * 
	 * @param st the project
	 * 
	 * @return the string
	 */
	static private String itemString(Project st) {
		return st.getDescription();
	}

	/**
	 * Record a project update.
	 * 
	 * @param project the project
	 * 
	 * @return the project undo item
	 */
	public static ProjectUndoItem recordUpdate(Project project) {
		ProjectUndoItem undoItem = new ProjectUndoItem();
		undoItem.item = project;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getResourceString("Change") + " "
				+ Resource.getResourceString("project") + " "
				+ itemString(project));
		return undoItem;
	}

	/**
	 * Record a project add.
	 * 
	 * @param project the project
	 * 
	 * @return the project undo item
	 */
	public static ProjectUndoItem recordAdd(Project project) {
		ProjectUndoItem undoItem = new ProjectUndoItem();
		undoItem.item = project;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getResourceString("Add") + " "
				+ Resource.getResourceString("project") + " "
				+ itemString(project));
		return undoItem;
	}

	/* undo of project delete is not supported */
	
	
}
