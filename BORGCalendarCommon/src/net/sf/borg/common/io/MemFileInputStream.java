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

package net.sf.borg.common.io;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;

/**
 * An InputStream interface to the MemFiles subsystem.
 * @author Mohan Embar
 */
class MemFileInputStream extends FilterInputStream
{
	MemFileInputStream(String file, MemFiles files)
		throws IOException
	{
		super(new ByteArrayInputStream(files.get(file)));
	}
}
