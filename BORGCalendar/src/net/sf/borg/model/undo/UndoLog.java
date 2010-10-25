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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 * The UndoLog. This class maintains a stack of items that can be undone.
 */
public class UndoLog {

	/** The undo stack. */
	private Stack<UndoItem<?>> undoStack = new Stack<UndoItem<?>>();
	
	/** The singleton. */
	private static UndoLog singleton = new UndoLog();
	
	/**
	 * get a reference to the undo log singleton.
	 * 
	 * @return the undo log singletone
	 */
	public static UndoLog getReference()
	{
		return singleton;
	}
	
	/**
	 * Instantiates a new undo log.
	 */
	private UndoLog()
	{
		// empty
	}
	
	/**
	 * add an undo item to the log.
	 * 
	 * @param item the item
	 */
	public void addItem(UndoItem<?> item)
	{
		undoStack.push(item);
	}
	
	/**
	 * get a description of the top item on the stack.
	 * 
	 * @return a description of the top item
	 */
	public final String getTopItem()
	{
		if( undoStack.empty())
			return null;
		return undoStack.peek().getDescription();
	}
	
	/**
	 * get descriptions for all items in the undo log.
	 * 
	 * @return - a collection containing descriptions of all undo items in the order that they would be pulled off the stack
	 */
	public Collection<String> getItemStrings()
	{
		List<String> strings = new ArrayList<String>();
		for( UndoItem<?> item : undoStack )
		{
			strings.add(0, item.getDescription());
		}
		return strings;
	}

	/**
	 * execute the top undo item on the stack and remove it from the stack.
	 */
	public void executeUndo()
	{
		if( !undoStack.empty())
		{
			UndoItem<?> item = undoStack.pop();
			item.executeUndo();
		}
	}
	
	/**
	 * get rid of all undo items.
	 */
	public void clear()
	{
		undoStack.clear();
	}
	
	/**
	 * get all of the undo items. This method has package visibility. Code outside of the undo package should not be able to get a hold of the undo items themselves
	 * 
	 * @return - the undo Stack
	 */
	Stack<UndoItem<?>> getItems(){
		return undoStack;
	}
	
	/**
	 * Pop an item off of the stack.
	 * 
	 * @return the undo item
	 */
	UndoItem<?> pop()
	{
		return undoStack.pop();
	}
}
