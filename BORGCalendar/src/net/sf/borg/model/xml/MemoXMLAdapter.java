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
package net.sf.borg.model.xml;

import net.sf.borg.common.XTree;
import net.sf.borg.model.entity.Memo;

/**
 * Memo XML Adapter.
 */
public class MemoXMLAdapter extends EntityXMLAdapter<Memo> {

	/* (non-Javadoc)
	 * @see net.sf.borg.model.xml.EntityXMLAdapter#toXml(net.sf.borg.model.entity.KeyedEntity)
	 */
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
			xt.appendChild("PalmId", EntityXMLAdapter.toString(o.getPalmId()));
		if( o.getNew() == true )
			xt.appendChild("New" ,  EntityXMLAdapter.toString(o.getNew()));
		if( o.getModified() == true )
			xt.appendChild("Modified" ,  EntityXMLAdapter.toString(o.getModified()));
		if( o.getDeleted() == true )
			xt.appendChild("Deleted" ,  EntityXMLAdapter.toString(o.getDeleted()));
		if( o.getPrivate() == true )
			xt.appendChild("Private" ,  EntityXMLAdapter.toString(o.getPrivate()));
		if( o.getCreated() != null )
			xt.appendChild("Created", EntityXMLAdapter.toString(o.getCreated()));
		if( o.getUpdated() != null )
			xt.appendChild("Updated", EntityXMLAdapter.toString(o.getUpdated()));
		return( xt );
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.xml.EntityXMLAdapter#fromXml(net.sf.borg.common.XTree)
	 */
	public Memo fromXml( XTree xt )
	{
		Memo ret = new Memo();
		String ks = xt.child("KEY").value();
		ret.setKey( EntityXMLAdapter.toInt(ks) );
		String val = "";
		val = xt.child("MemoName").value();
		if( !val.equals("") )
			ret.setMemoName( val );
		val = xt.child("MemoText").value();
		if( !val.equals("") )
			ret.setMemoText( val );
		val = xt.child("PalmId").value();
		ret.setPalmId( EntityXMLAdapter.toInteger(val) );
		val = xt.child("New").value();
		ret.setNew( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Modified").value();
		ret.setModified( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Deleted").value();
		ret.setDeleted( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Private").value();
		ret.setPrivate( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Created").value();
		ret.setCreated( EntityXMLAdapter.toDate(val) );
		val = xt.child("Updated").value();
		ret.setUpdated( EntityXMLAdapter.toDate(val) );
		return( ret );
	}
}
