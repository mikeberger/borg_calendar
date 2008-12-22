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
 
Copyright 2004 by Mohan Embar - http://www.thisiscool.com/
 */

package net.sf.borg.control.socketServer;

import net.sf.borg.model.beans.Address;
import net.sf.borg.model.beans.Appointment;
import net.sf.borg.model.beans.Memo;
import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.Subtask;
import net.sf.borg.model.beans.Task;
import net.sf.borg.model.beans.Tasklog;

/**
 * Interface for executing a remote call.
 */
interface IRemoteProxy
{
/////////////////////////////////////////////////////
// nested class Parms

static class Parms
{
	public Parms(String clsstr, String command, Object args, String user)
	{
		this.clsstr = clsstr;
		this.command = command;
		this.args = args;
		this.user = user;

		if (clsstr.equals("Address"))
			cls = Address.class;
		else if (clsstr.equals("Task"))
			cls = Task.class;
		else if (clsstr.equals("Appointment"))
			cls = Appointment.class;
		else if (clsstr.equals("Memo"))
			cls = Memo.class;
		else if (clsstr.equals("Project"))
			cls = Project.class;
		else if (clsstr.equals("Subtask"))
			cls = Subtask.class;
		else if (clsstr.equals("Tasklog"))
			cls = Tasklog.class;
		else
			throw new IllegalArgumentException(clsstr);
	}
	
	@SuppressWarnings("unchecked")
	public final Class getMyClass()			{return cls;}
	public final String getClassString()	{return clsstr;}
	public final String getCommand()		{return command;}
	public final Object getArgs()			{return args;}
	public final String getUser()			{return user;}
	
	// private //
	@SuppressWarnings("unchecked")
	private Class cls;
	private String clsstr;
	private String command;
	private Object args;
	private String user;
}

// end nested class Parms
/////////////////////////////////////////////////////
	
/////////////////////////////////////////////////////
// nested class ComposedObject

static class ComposedObject
{
	public ComposedObject(Object o1, Object o2)
	{
		this.o1 = o1;
		this.o2 = o2;
	}
	
	public final Object getO1()	{return o1;}
	public final Object getO2()	{return o2;}
	
	// private //
	private Object o1, o2;
}

// end nested class ComposedObject
/////////////////////////////////////////////////////

//public String execute(String strXml, IRemoteProxyProvider provider)
	//	throws Exception;
}
