package net.sf.borg.model.undo;

import net.sf.borg.common.Resource;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.beans.Memo;

public class MemoUndoItem extends UndoItem {

	private enum actionType {
		ADD, DELETE, UPDATE
	}
	
	private Memo memo;
	private actionType action;
	
	@Override
	public void executeUndo() {
		if( action == actionType.DELETE )
		{
			MemoModel.getReference().saveMemo(memo, true);
		}
		else if( action == actionType.UPDATE )
		{
			MemoModel.getReference().saveMemo(memo, true);
		}
		else if( action == actionType.ADD )
		{
			MemoModel.getReference().delete(memo.getMemoName(), true);
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
		MemoUndoItem item = new MemoUndoItem();
		item.memo = memo;
		item.action = actionType.UPDATE;
		item.setDescription(Resource.getPlainResourceString("Change") + " " + 
				Resource.getPlainResourceString("memo") + " " + memoString(memo));
		return item;
	}
	
	public static MemoUndoItem recordAdd(Memo memo)
	{
		MemoUndoItem item = new MemoUndoItem();
		item.memo = memo;
		item.action = actionType.ADD;
		item.setDescription(Resource.getPlainResourceString("Add") + " " + 
				Resource.getPlainResourceString("memo") + " " + memoString(memo));
		return item;
	}
	
	public static MemoUndoItem recordDelete(Memo memo)
	{
		MemoUndoItem item = new MemoUndoItem();
		item.memo = memo;
		item.action = actionType.DELETE;
		item.setDescription(Resource.getPlainResourceString("Delete") + " " +
				Resource.getPlainResourceString("memo") + " " + memoString(memo));
		return item;
	}

}
