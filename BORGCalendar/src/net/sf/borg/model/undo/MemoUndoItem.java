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
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.entity.Memo;

public class MemoUndoItem extends UndoItem<Memo> {

	@Override
	public void executeUndo() {
		if( action == actionType.DELETE )
		{
			MemoModel.getReference().saveMemo(item, true);
		}
		else if( action == actionType.UPDATE )
		{
			MemoModel.getReference().saveMemo(item, true);
		}
		else if( action == actionType.ADD )
		{
			MemoModel.getReference().delete(item.getMemoName(), true);
		}
	}
	
	private MemoUndoItem()
	{
		
	}
	
	static private String memoString(Memo memo)
	{
		return memo.getMemoName(); 
	}
	
	public static MemoUndoItem recordUpdate(Memo memo)
	{
		MemoUndoItem undoItem = new MemoUndoItem();
		undoItem.item = memo;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getPlainResourceString("Change") + " " + 
				Resource.getPlainResourceString("memo") + " " + memoString(memo));
		return undoItem;
	}
	
	public static MemoUndoItem recordAdd(Memo memo)
	{
		MemoUndoItem undoItem = new MemoUndoItem();
		undoItem.item = memo;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getPlainResourceString("Add") + " " + 
				Resource.getPlainResourceString("memo") + " " + memoString(memo));
		return undoItem;
	}
	
	public static MemoUndoItem recordDelete(Memo memo)
	{
		MemoUndoItem undoItem = new MemoUndoItem();
		undoItem.item = memo;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getPlainResourceString("Delete") + " " +
				Resource.getPlainResourceString("memo") + " " + memoString(memo));
		return undoItem;
	}

}
