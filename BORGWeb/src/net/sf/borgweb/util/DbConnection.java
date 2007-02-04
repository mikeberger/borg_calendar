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
 
Copyright 2004 by ==Quiet==
 */
package net.sf.borgweb.util;
import java.sql.*;
import javax.naming.*;
import javax.sql.DataSource;
public class DbConnection
{

	/** Creates a new instance of DbConnection */
	private DbConnection()
	{
	}

	static public Connection getConnection() throws Exception
	{

		InitialContext ctx = new InitialContext();
		DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/borgdb");
		Connection conn = ds.getConnection();

		return (conn);
	}

}
