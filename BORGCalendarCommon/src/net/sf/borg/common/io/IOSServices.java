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



import java.io.InputStream;



/**

 * An interface to OS services. Wraps JNLP-related stuff without loading

 * JNLP if it's not present (applet).

 * @author	Mohan Embar

 */

public interface IOSServices

{

public String getClipboardData();

public void setClipboardData(String data);

public InputStream fileOpen(String startDirectory, String title)

	throws Exception;

public void fileSave(

	String startDirectory,

	InputStream istr,

	String defaultFilename)

	throws Exception;

}

