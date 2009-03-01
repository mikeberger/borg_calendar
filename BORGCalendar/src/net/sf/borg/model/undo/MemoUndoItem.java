package net.sf.borg.model.undo;

import net.sf.borg.common.Resource;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.beans.Memo;

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
