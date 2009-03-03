package net.sf.borg.model.undo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
	
	public Collection<String> getItemStrings()
	{
		List<String> strings = new ArrayList<String>();
		for( UndoItem item : undoStack )
		{
			strings.add(0, item.getDescription());
		}
		return strings;
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
	
	Stack<UndoItem> getItems(){
		return undoStack;
	}
}
