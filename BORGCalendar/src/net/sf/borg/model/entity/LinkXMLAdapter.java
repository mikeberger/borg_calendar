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
package net.sf.borg.model.entity;

import net.sf.borg.common.XTree;
public class LinkXMLAdapter extends EntityXMLAdapter<Link> {

	public XTree toXml( Link o )
	{
		
		XTree xt = new XTree();
		xt.name("Link");
		xt.appendChild("KEY", Integer.toString(o.getKey()));
		if( o.getLinkType() != null )
			xt.appendChild("LinkType", o.getLinkType());
		if( o.getOwnerKey() != null )
			xt.appendChild("OwnerKey", EntityXMLAdapter.toString(o.getOwnerKey()));
		if( o.getOwnerType() != null )
			xt.appendChild("OwnerType", o.getOwnerType());
		if( o.getPath() != null )
			xt.appendChild("Path", o.getPath());
		return( xt );
	}

	public Link fromXml( XTree xt )
	{
		Link ret = new Link();
		String ks = xt.child("KEY").value();
		ret.setKey( EntityXMLAdapter.toInt(ks) );
		String val = "";
		val = xt.child("LinkType").value();
		ret.setLinkType( val );
		val = xt.child("OwnerKey").value();
		ret.setOwnerKey( EntityXMLAdapter.toInteger(val) );
		val = xt.child("OwnerType").value();
		ret.setOwnerType( val );
		val = xt.child("Path").value();
		ret.setPath( val );
		return( ret );
	}
}
