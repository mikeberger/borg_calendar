package net.sf.borg.model.undo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

@SuppressWarnings("unchecked")
/**
 * This class maintains a stack of items that can be undone.
 */
public class UndoLog {

	// the stack of undo items
	private Stack<UndoItem> undoStack = new Stack<UndoItem>();
	
	private static UndoLog singleton = new UndoLog();
	
	/**
	 * get a reference to the undo log singleton
	 * @return the undo log singletone
	 */
	public static UndoLog getReference()
	{
		return singleton;
	}
	
	private UndoLog()
	{
		
	}
	
	/**
	 * add an undo item to the log
	 * @param item
	 */
	public void addItem(UndoItem item)
	{
		undoStack.push(item);
	}
	
	/**
	 * get a description of the top item on the stack
	 * @return a description of the top item
	 */
	public final String getTopItem()
	{
		if( undoStack.empty())
			return null;
		return undoStack.peek().getDescription();
	}
	
	/**
	 * get descriptions for all items in the undo log
	 * @return - a collection containing descriptions of all undo items in the order that they would be pulled off the stack
	 */
	public Collection<String> getItemStrings()
	{
		List<String> strings = new ArrayList<String>();
		for( UndoItem item : undoStack )
		{
			strings.add(0, item.getDescription());
		}
		return strings;
	}

	/**
	 * execute the top undo item on the stack and remove it from the stack
	 */
	public void executeUndo()
	{
		if( !undoStack.empty())
		{
			UndoItem item = undoStack.pop();
			item.executeUndo();
		}
	}
	
	/**
	 * get rid of all undo items
	 */
	public void clear()
	{
		undoStack.clear();
	}
	
	/**
	 * get all of the undo items. This method has package visibility. Code outside of the undo package should not be able to get a hold of the undo items themselves
	 * @return - the undo Stack 
	 */
	Stack<UndoItem> getItems(){
		return undoStack;
	}
	
	UndoItem pop()
	{
		return undoStack.pop();
	}
}
