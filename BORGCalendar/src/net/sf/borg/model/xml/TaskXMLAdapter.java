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
		if( o.getTaskNumber() != null )
			xt.appendChild("TaskNumber", EntityXMLAdapter.toString(o.getTaskNumber()));
		if( o.getStartDate() != null )
			xt.appendChild("StartDate", EntityXMLAdapter.toString(o.getStartDate()));
		if( o.getCD() != null )
			xt.appendChild("CD", EntityXMLAdapter.toString(o.getCD()));
		if( o.getDueDate() != null )
			xt.appendChild("DueDate", EntityXMLAdapter.toString(o.getDueDate()));
		if( o.getET() != null )
			xt.appendChild("ET", EntityXMLAdapter.toString(o.getET()));
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
		if( o.getTodoList() != null && !o.getTodoList().equals(""))
			xt.appendChild("TodoList", o.getTodoList());
		if( o.getUserTask1() != null && !o.getUserTask1().equals(""))
			xt.appendChild("UserTask1", o.getUserTask1());
		if( o.getUserTask2() != null && !o.getUserTask2().equals(""))
			xt.appendChild("UserTask2", o.getUserTask2());
		if( o.getUserTask3() != null && !o.getUserTask3().equals(""))
			xt.appendChild("UserTask3", o.getUserTask3());
		if( o.getUserTask4() != null && !o.getUserTask4().equals(""))
			xt.appendChild("UserTask4", o.getUserTask4());
		if( o.getUserTask5() != null && !o.getUserTask5().equals(""))
			xt.appendChild("UserTask5", o.getUserTask5());
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
		val = xt.child("TaskNumber").value();
		ret.setTaskNumber( EntityXMLAdapter.toInteger(val) );
		val = xt.child("StartDate").value();
		ret.setStartDate( EntityXMLAdapter.toDate(val) );
		val = xt.child("CD").value();
		ret.setCD( EntityXMLAdapter.toDate(val) );
		val = xt.child("DueDate").value();
		ret.setDueDate( EntityXMLAdapter.toDate(val) );
		val = xt.child("ET").value();
		ret.setET( EntityXMLAdapter.toDate(val) );
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
		val = xt.child("TodoList").value();
		if( !val.equals("") )
			ret.setTodoList( val );
		val = xt.child("UserTask1").value();
		if( !val.equals("") )
			ret.setUserTask1( val );
		val = xt.child("UserTask2").value();
		if( !val.equals("") )
			ret.setUserTask2( val );
		val = xt.child("UserTask3").value();
		if( !val.equals("") )
			ret.setUserTask3( val );
		val = xt.child("UserTask4").value();
		if( !val.equals("") )
			ret.setUserTask4( val );
		val = xt.child("UserTask5").value();
		if( !val.equals("") )
			ret.setUserTask5( val );
		val = xt.child("Category").value();
		if( !val.equals("") )
			ret.setCategory( val );
		val = xt.child("Project").value();
		ret.setProject( EntityXMLAdapter.toInteger(val) );
		return( ret );
	}
}
