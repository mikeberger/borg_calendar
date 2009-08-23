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
import net.sf.borg.model.entity.Task;
public class TaskXMLAdapter extends EntityXMLAdapter<Task> {

	public XTree toXml( Task o )
	{
		
		XTree xt = new XTree();
		xt.name("Task");
		xt.appendChild("KEY", Integer.toString(o.getKey()));
		
		if( o.getStartDate() != null )
			xt.appendChild("StartDate", EntityXMLAdapter.toString(o.getStartDate()));
		if( o.getCompletionDate() != null )
			xt.appendChild("CD", EntityXMLAdapter.toString(o.getCompletionDate()));
		if( o.getDueDate() != null )
			xt.appendChild("DueDate", EntityXMLAdapter.toString(o.getDueDate()));
		if( o.getPersonAssigned() != null && !o.getPersonAssigned().equals(""))
			xt.appendChild("PersonAssigned", o.getPersonAssigned());
		if( o.getPriority() != null )
			xt.appendChild("Priority", EntityXMLAdapter.toString(o.getPriority()));
		if( o.getState() != null && !o.getState().equals(""))
			xt.appendChild("State", o.getState());
		if( o.getType() != null && !o.getType().equals(""))
			xt.appendChild("Type", o.getType());
		if( o.getDescription() != null && !o.getDescription().equals(""))
			xt.appendChild("Description", o.getDescription());
		if( o.getResolution() != null && !o.getResolution().equals(""))
			xt.appendChild("Resolution", o.getResolution());
		if( o.getCategory() != null && !o.getCategory().equals(""))
			xt.appendChild("Category", o.getCategory());
		if( o.getProject() != null )
			xt.appendChild("Project", EntityXMLAdapter.toString(o.getProject()));
		return( xt );
	}

	public Task fromXml( XTree xt )
	{
		Task ret = new Task();
		String ks = xt.child("KEY").value();
		ret.setKey( EntityXMLAdapter.toInt(ks) );
		String val = "";
	
		val = xt.child("StartDate").value();
		ret.setStartDate( EntityXMLAdapter.toDate(val) );
		val = xt.child("CD").value();
		ret.setCompletionDate( EntityXMLAdapter.toDate(val) );
		val = xt.child("DueDate").value();
		ret.setDueDate( EntityXMLAdapter.toDate(val) );
		val = xt.child("PersonAssigned").value();
		if( !val.equals("") )
			ret.setPersonAssigned( val );
		val = xt.child("Priority").value();
		ret.setPriority( EntityXMLAdapter.toInteger(val) );
		val = xt.child("State").value();
		if( !val.equals("") )
			ret.setState( val );
		val = xt.child("Type").value();
		if( !val.equals("") )
			ret.setType( val );
		val = xt.child("Description").value();
		if( !val.equals("") )
			ret.setDescription( val );
		val = xt.child("Resolution").value();
		if( !val.equals("") )
			ret.setResolution( val );
		val = xt.child("Category").value();
		if( !val.equals("") )
			ret.setCategory( val );
		val = xt.child("Project").value();
		ret.setProject( EntityXMLAdapter.toInteger(val) );
		return( ret );
	}
}
