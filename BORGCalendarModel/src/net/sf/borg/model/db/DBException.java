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

Copyright 2003 by ==Quiet==
*/
/*
 * MDBException.java
 *
 * Created on October 27, 2001, 3:12 PM
 */


/**
 *
 * @author  mberger
 * @version
 */

package net.sf.borg.model.db;

import net.sf.borg.common.util.Version;



// MDBException provides an exception for the MDB database that
// can also return a return code indicating more info
// was easier than defining an exception class for each type
// of exception and having to throw and catch them all
public class DBException extends java.lang.Exception {
    static {
        Version.addVersion("$Id$");
    } 

    private int retCode;

	// MDB return codes - returned in MDBExceptions - rather than creating different
	// exception types for each error
	public static final int RET_FATAL=-1;

	public static final int RET_SUCCESS=0;

	public static final int RET_NOT_FOUND=1;

	public static final int RET_DUPLICATE=2;

	public static final int RET_READ_ONLY=3;

	public static final int RET_CANT_LOCK=4;
    
    /**
     * Constructs an <code>MDBException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DBException(String msg, int ret) {
        super(msg);
        setRetCode(ret);
    }
    public DBException(String msg) {
        super(msg);
        setRetCode(DBException.RET_FATAL);
    }

	public void setRetCode(int retCode)
	{
		this.retCode = retCode;
	}

	public int getRetCode()
	{
		return retCode;
	}
}


