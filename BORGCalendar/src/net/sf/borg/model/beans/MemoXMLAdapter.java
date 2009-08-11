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
package net.sf.borg.model.beans;

import net.sf.borg.common.XTree;
public class MemoXMLAdapter extends BeanXMLAdapter<Memo> {

	public XTree toXml( Memo o )
	{
	
		XTree xt = new XTree();
		xt.name("Memo");
		xt.appendChild("KEY", Integer.toString(o.getKey()));
		if( o.getMemoName() != null && !o.getMemoName().equals(""))
			xt.appendChild("MemoName", o.getMemoName());
		if( o.getMemoText() != null && !o.getMemoText().equals(""))
			xt.appendChild("MemoText", o.getMemoText());
		if( o.getPalmId() != null )
			xt.appendChild("PalmId", BeanXMLAdapter.toString(o.getPalmId()));
		if( o.getNew() == true )
			xt.appendChild("New" ,  BeanXMLAdapter.toString(o.getNew()));
		if( o.getModified() == true )
			xt.appendChild("Modified" ,  BeanXMLAdapter.toString(o.getModified()));
		if( o.getDeleted() == true )
			xt.appendChild("Deleted" ,  BeanXMLAdapter.toString(o.getDeleted()));
		if( o.getPrivate() == true )
			xt.appendChild("Private" ,  BeanXMLAdapter.toString(o.getPrivate()));
		if( o.getCreated() != null )
			xt.appendChild("Created", BeanXMLAdapter.toString(o.getCreated()));
		if( o.getUpdated() != null )
			xt.appendChild("Updated", BeanXMLAdapter.toString(o.getUpdated()));
		return( xt );
	}

	public Memo fromXml( XTree xt )
	{
		Memo ret = new Memo();
		String ks = xt.child("KEY").value();
		ret.setKey( BeanXMLAdapter.toInt(ks) );
		String val = "";
		val = xt.child("MemoName").value();
		if( !val.equals("") )
			ret.setMemoName( val );
		val = xt.child("MemoText").value();
		if( !val.equals("") )
			ret.setMemoText( val );
		val = xt.child("PalmId").value();
		ret.setPalmId( BeanXMLAdapter.toInteger(val) );
		val = xt.child("New").value();
		ret.setNew( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Modified").value();
		ret.setModified( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Deleted").value();
		ret.setDeleted( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Private").value();
		ret.setPrivate( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Created").value();
		ret.setCreated( BeanXMLAdapter.toDate(val) );
		val = xt.child("Updated").value();
		ret.setUpdated( BeanXMLAdapter.toDate(val) );
		return( ret );
	}
}
