/*
 This file is part of BORG.

 BORG is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 BORG is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with BORG; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Copyright 2003-2010 by Mike Berger
 */
package net.sf.borg.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;



/**
 * CheckList Entity. A CheckList holds a list of items that can be either checked or unchecked
 */
@XmlRootElement(name="CheckList")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class CheckList implements Cloneable {

	/**
	 * Item hold a single net.sf.borg.ui.checklist entry.
	 */
	@XmlRootElement(name="Item")
	@XmlAccessorType(XmlAccessType.FIELD)
	@Data
	public static class Item {
		
		private Boolean checked;
		private String text;
		
		@Override
		protected Object clone() throws CloneNotSupportedException {
			Item copy = new Item();
			copy.setText(text);
			copy.setChecked(checked);
			return copy;
		}
	}
	
	private String CheckListName;
	private List<Item> items = new ArrayList<Item>();
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	@Override
	public CheckList clone() {
		CheckList dst = new CheckList();
		dst.setCheckListName( getCheckListName() );
		dst.items.addAll(items);
		return(dst);
	}

	
}
