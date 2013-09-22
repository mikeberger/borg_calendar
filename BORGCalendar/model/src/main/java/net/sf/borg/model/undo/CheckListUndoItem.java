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

import net.sf.borg.common.Resource;
import net.sf.borg.model.CheckListModel;
import net.sf.borg.model.entity.CheckList;

/**
 * CheckList Undo Item.
 */
public class CheckListUndoItem extends UndoItem<CheckList> {

	/* (non-Javadoc)
	 * @see net.sf.borg.model.undo.UndoItem#executeUndo()
	 */
	@Override
	public void executeUndo() {
		if( action == actionType.DELETE )
		{
			CheckListModel.getReference().saveCheckList(item, true);
		}
		else if( action == actionType.UPDATE )
		{
			CheckListModel.getReference().saveCheckList(item, true);
		}
		else if( action == actionType.ADD )
		{
			CheckListModel.getReference().delete(item.getCheckListName(), true);
		}
	}
	
	/**
	 * Instantiates a new checkList undo item.
	 */
	private CheckListUndoItem()
	{
		// empty
	}
	
	/**
	 * get a human readable string for this item.
	 * 
	 * @param checkList the checkList
	 * 
	 * @return the string
	 */
	static private String checkListString(CheckList checkList)
	{
		return checkList.getCheckListName(); 
	}
	
	/**
	 * Record a checkList update.
	 * 
	 * @param checkList the checkList
	 * 
	 * @return the checkList undo item
	 */
	public static CheckListUndoItem recordUpdate(CheckList checkList)
	{
		CheckListUndoItem undoItem = new CheckListUndoItem();
		undoItem.item = checkList;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getResourceString("Change") + " " + 
				Resource.getResourceString("CheckList") + " " + checkListString(checkList));
		return undoItem;
	}
	
	/**
	 * Record a checkList add.
	 * 
	 * @param checkList the checkList
	 * 
	 * @return the checkList undo item
	 */
	public static CheckListUndoItem recordAdd(CheckList checkList)
	{
		CheckListUndoItem undoItem = new CheckListUndoItem();
		undoItem.item = checkList;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getResourceString("Add") + " " + 
				Resource.getResourceString("checkList") + " " + checkListString(checkList));
		return undoItem;
	}
	
	/**
	 * Record a checkList delete.
	 * 
	 * @param checkList the checkList
	 * 
	 * @return the checkList undo item
	 */
	public static CheckListUndoItem recordDelete(CheckList checkList)
	{
		CheckListUndoItem undoItem = new CheckListUndoItem();
		undoItem.item = checkList;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getResourceString("Delete") + " " +
				Resource.getResourceString("checkList") + " " + checkListString(checkList));
		return undoItem;
	}

}
