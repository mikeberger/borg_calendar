package net.sf.borg.model.undo;

import java.util.Stack;

@SuppressWarnings("unchecked")
public class UndoLog {

	
	private Stack<UndoItem> undoStack = new Stack<UndoItem>();
	
	private static UndoLog singleton = new UndoLog();
	
	public static UndoLog getReference()
	{
		return singleton;
	}
	
	private UndoLog()
	{
		
	}
	
	public void addItem(UndoItem item)
	{
		undoStack.push(item);
	}
	
	public final String getTopItem()
	{
		if( undoStack.empty())
			return null;
		return undoStack.peek().getDescription();
	}
	
	public void executeUndo()
	{
		if( !undoStack.empty())
		{
			UndoItem item = undoStack.pop();
			item.executeUndo();
		}
	}
	
	public void clear()
	{
		undoStack.clear();
	}
}
