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



/**
 * CheckList Entity. A CheckList holds a list of items that can be either checked or unchecked
 */
@XmlRootElement(name="CheckList")
@XmlAccessorType(XmlAccessType.FIELD)
public class CheckList {

	/**
	 * Item hold a single net.sf.borg.ui.checklist entry.
	 */
	@XmlRootElement(name="Item")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Item {
		
		/** The checked flag - true if checked. */
		private Boolean checked;
		
		/** The text. */
		private String text;
		
		/**
		 * Gets the checked flag.
		 *
		 * @return the checked flag
		 */
		public Boolean getChecked() {
			return checked;
		}
		
		/**
		 * Gets the text.
		 *
		 * @return the text
		 */
		public String getText() {
			return text;
		}
		
		/**
		 * Sets the checked flag.
		 *
		 * @param checked the new checked flag
		 */
		public void setChecked(Boolean checked) {
			this.checked = checked;
		}
		
		/**
		 * Sets the text.
		 *
		 * @param text the new text
		 */
		public void setText(String text) {
			this.text = text;
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			Item copy = new Item();
			copy.setText(text);
			copy.setChecked(checked);
			return copy;
		}
	}
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1L;
	
	/** The CheckList name. */
	private String CheckListName;
	
	/** The items. */
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

	
	/**
	 * Gets the checkList name.
	 * 
	 * @return the checkList name
	 */
	public String getCheckListName() { return( CheckListName ); }
	
	/**
	 * Gets the items.
	 *
	 * @return the items
	 */
	public List<Item> getItems() {
		return items;
	}

	/**
	 * Sets the checkList name.
	 * 
	 * @param xx the new checkList name
	 */
	public void setCheckListName( String xx ){ CheckListName = xx; }

	/**
	 * Sets the items.
	 *
	 * @param items the new items
	 */
	public void setItems(List<Item> items) {
		this.items = items;
	}
	
}
