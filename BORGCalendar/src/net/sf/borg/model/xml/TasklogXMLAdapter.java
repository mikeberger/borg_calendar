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
import net.sf.borg.model.entity.Tasklog;
public class TasklogXMLAdapter extends EntityXMLAdapter<Tasklog> {

	public XTree toXml( Tasklog o )
	{
		
		XTree xt = new XTree();
		xt.name("Tasklog");
		xt.appendChild("KEY", Integer.toString(o.getKey()));
		
		if( o.getlogTime() != null )
			xt.appendChild("logTime", EntityXMLAdapter.toString(o.getlogTime()));
		if( o.getDescription() != null && !o.getDescription().equals(""))
			xt.appendChild("Description", o.getDescription());
		if( o.getTask() != null )
			xt.appendChild("Task", EntityXMLAdapter.toString(o.getTask()));
		return( xt );
	}

	public Tasklog fromXml( XTree xt )
	{
		Tasklog ret = new Tasklog();
		String ks = xt.child("KEY").value();
		ret.setKey( EntityXMLAdapter.toInt(ks) );
		String val = "";
	
		val = xt.child("logTime").value();
		ret.setlogTime( EntityXMLAdapter.toDate(val) );
		val = xt.child("Description").value();
		if( !val.equals("") )
			ret.setDescription( val );
		val = xt.child("Task").value();
		ret.setTask( EntityXMLAdapter.toInteger(val) );
		return( ret );
	}
}
