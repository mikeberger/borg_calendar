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
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.entity.Address;

public class AddressUndoItem extends UndoItem<Address> {

	@Override
	public void executeUndo() {
		if (action == actionType.DELETE) {
			AddressModel.getReference().saveAddress(item, true);
		} else if (action == actionType.UPDATE) {
			AddressModel.getReference().saveAddress(item, true);
		} else if (action == actionType.ADD) {
			AddressModel.getReference().delete(item, true);
		}
	}

	private AddressUndoItem() {

	}

	static private String addrString(Address addr) {
		return addr.getFirstName() + " " + addr.getLastName();
	}

	public static AddressUndoItem recordUpdate(Address addr) {
		AddressUndoItem undoItem = new AddressUndoItem();
		undoItem.item = addr;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getPlainResourceString("Change") + " "
				+ Resource.getPlainResourceString("Address") + " "
				+ addrString(addr));
		return undoItem;
	}

	public static AddressUndoItem recordAdd(Address addr) {
		AddressUndoItem undoItem = new AddressUndoItem();
		undoItem.item = addr;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getPlainResourceString("Add") + " "
				+ Resource.getPlainResourceString("Address") + " "
				+ addrString(addr));
		return undoItem;
	}

	public static AddressUndoItem recordDelete(Address addr) {
		AddressUndoItem undoItem = new AddressUndoItem();
		undoItem.item = addr;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getPlainResourceString("Delete") + " "
				+ Resource.getPlainResourceString("Address") + " "
				+ addrString(addr));
		return undoItem;
	}

}
