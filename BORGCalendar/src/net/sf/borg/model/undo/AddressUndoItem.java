package net.sf.borg.model.undo;

import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.beans.Address;

public class AddressUndoItem extends UndoItem {

	private enum actionType {
		ADD, DELETE, UPDATE
	}
	
	private Address addr;
	private actionType action;
	
	@Override
	public void executeUndo() {
		if( action == actionType.DELETE )
		{
			AddressModel.getReference().saveAddress(addr, true);
		}
		else if( action == actionType.UPDATE )
		{
			AddressModel.getReference().saveAddress(addr, true);
		}
		else if( action == actionType.ADD )
		{
			AddressModel.getReference().delete(addr, true);
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
		AddressUndoItem item = new AddressUndoItem();
		item.addr = addr;
		item.action = actionType.UPDATE;
		item.setDescription(Resource.getPlainResourceString("Change") + " " + 
				Resource.getPlainResourceString("Address") + " " + addrString(addr));
		return item;
	}
	
	public static AddressUndoItem recordAdd(Address addr)
	{
		AddressUndoItem item = new AddressUndoItem();
		item.addr = addr;
		item.action = actionType.ADD;
		item.setDescription(Resource.getPlainResourceString("Add") + " " + 
				Resource.getPlainResourceString("Address") + " " + addrString(addr));
		return item;
	}
	
	public static AddressUndoItem recordDelete(Address addr)
	{
		AddressUndoItem item = new AddressUndoItem();
		item.addr = addr;
		item.action = actionType.DELETE;
		item.setDescription(Resource.getPlainResourceString("Delete") + " " +
				Resource.getPlainResourceString("Address") + " " + addrString(addr));
		return item;
	}

}
