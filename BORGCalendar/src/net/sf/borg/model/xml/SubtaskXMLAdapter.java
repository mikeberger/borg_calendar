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
import net.sf.borg.model.entity.Subtask;
public class SubtaskXMLAdapter extends EntityXMLAdapter<Subtask> {

	public XTree toXml( Subtask o )
	{
		
		XTree xt = new XTree();
		xt.name("Subtask");
		xt.appendChild("KEY", Integer.toString(o.getKey()));
	
		if( o.getStartDate() != null )
			xt.appendChild("CreateDate", EntityXMLAdapter.toString(o.getStartDate()));
		if( o.getCloseDate() != null )
			xt.appendChild("CloseDate", EntityXMLAdapter.toString(o.getCloseDate()));
		if( o.getDueDate() != null )
			xt.appendChild("DueDate", EntityXMLAdapter.toString(o.getDueDate()));
		if( o.getDescription() != null && !o.getDescription().equals(""))
			xt.appendChild("Description", o.getDescription());
		if( o.getTask() != null )
			xt.appendChild("Task", EntityXMLAdapter.toString(o.getTask()));
		return( xt );
	}

	public Subtask fromXml( XTree xt )
	{
		Subtask ret = new Subtask();
		String ks = xt.child("KEY").value();
		ret.setKey( EntityXMLAdapter.toInt(ks) );
		String val = "";
		val = xt.child("CreateDate").value();
		ret.setStartDate( EntityXMLAdapter.toDate(val) );
		val = xt.child("CloseDate").value();
		ret.setCloseDate( EntityXMLAdapter.toDate(val) );
		val = xt.child("DueDate").value();
		ret.setDueDate( EntityXMLAdapter.toDate(val) );
		val = xt.child("Description").value();
		if( !val.equals("") )
			ret.setDescription( val );
		val = xt.child("Task").value();
		ret.setTask( EntityXMLAdapter.toInteger(val) );
		return( ret );
	}
}
