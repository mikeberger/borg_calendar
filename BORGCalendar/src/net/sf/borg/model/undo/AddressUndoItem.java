package net.sf.borg.model.undo;

import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.beans.Address;

public class AddressUndoItem extends UndoItem<Address> {

	@Override
	public void executeUndo() {
		if( action == actionType.DELETE )
		{
			AddressModel.getReference().saveAddress(item, true);
		}
		else if( action == actionType.UPDATE )
		{
			AddressModel.getReference().saveAddress(item, true);
		}
		else if( action == actionType.ADD )
		{
			AddressModel.getReference().delete(item, true);
		}
	}
	
	private AddressUndoItem()
	{
		
	}
	
	static private String addrString(Address addr)
	{
		return addr.getFirstName() + " " + addr.getLastName(); 
	}
	
	public static AddressUndoItem recordUpdate(Address addr)
	{
		AddressUndoItem undoItem = new AddressUndoItem();
		undoItem.item = addr;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getPlainResourceString("Change") + " " + 
				Resource.getPlainResourceString("Address") + " " + addrString(addr));
		return undoItem;
	}
	
	public static AddressUndoItem recordAdd(Address addr)
	{
		AddressUndoItem undoItem = new AddressUndoItem();
		undoItem.item = addr;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getPlainResourceString("Add") + " " + 
				Resource.getPlainResourceString("Address") + " " + addrString(addr));
		return undoItem;
	}
	
	public static AddressUndoItem recordDelete(Address addr)
	{
		AddressUndoItem undoItem = new AddressUndoItem();
		undoItem.item = addr;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getPlainResourceString("Delete") + " " +
				Resource.getPlainResourceString("Address") + " " + addrString(addr));
		return undoItem;
	}

}
