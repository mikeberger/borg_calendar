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
 
Copyright 2003 by Mike Berger
 */

package net.sf.borg.model.db.remote;

import java.util.Collection;

import net.sf.borg.model.beans.Appointment;
import net.sf.borg.model.db.AppointmentDB;

/**
 * @author Mohan Embar
 */
public class ApptRemoteBeanDB extends RemoteBeanDB implements AppointmentDB
{
	public ApptRemoteBeanDB()
	{
		super(Appointment.class, "Appointment");
	}

	public Collection getTodoKeys() throws Exception
	{
		return (Collection) call("getTodoKeys", null);
	}

	public Collection getRepeatKeys() throws Exception
	{
		return (Collection) call("getRepeatKeys", null);
	}

	
}
