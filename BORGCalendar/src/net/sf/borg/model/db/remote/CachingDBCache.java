/*
This file is part of BORG.
 
	BORG is free software; you can redistribute it and/or modify
	it under the terms of the GNU General public final synchronized License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.
 
	BORG is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General public final synchronized License for more details.
 
	You should have received a copy of the GNU General public final synchronized License
	along with BORG; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
Copyright 2003 by Mike Berger
 */

package net.sf.borg.model.db.remote;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohan Embar
 */
class CachingDBCache implements Serializable
{
	CachingDBCache()
	{}
	
	final Map getObjectMap()	{return mapObjects;}
	final Map getOptionsMap()	{return mapOptions;}
	
	final int getNextKey()		{return ++maxkey;}

	// private //
	private int maxkey = 1;
	private Map mapObjects = new HashMap();
	private Map mapOptions = new HashMap();
}
