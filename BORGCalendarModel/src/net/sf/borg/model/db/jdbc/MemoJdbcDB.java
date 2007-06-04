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

package net.sf.borg.model.db.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.borg.model.Memo;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;
import net.sf.borg.model.db.MemoDB;

class MemoJdbcDB extends JdbcDB implements MemoDB {

    MemoJdbcDB(String url, String username) throws Exception {
	super(url, username);
    }

    MemoJdbcDB(Connection conn) {
	super(conn);
    }

    public void addMemo(Memo m) throws DBException, Exception {
	PreparedStatement stmt = connection_
		.prepareStatement("INSERT INTO memos ( memoname, username, memotext, new, modified, deleted, palmid, private) "
			+ " VALUES " + "( ?, ?, ?, ?, ?, ?, ?, ?)");

	stmt.setString(1, m.getMemoName());
	stmt.setString(2, username_);

	stmt.setString(3, m.getMemoText());
	stmt.setInt(4, toInt(m.getNew()));
	stmt.setInt(5, toInt(m.getModified()));
	stmt.setInt(6, toInt(m.getDeleted()));
	if( m.getPalmId() != null )
	    stmt.setInt(7, m.getPalmId().intValue());
	else
	    stmt.setNull(7, java.sql.Types.INTEGER);
	stmt.setInt(8, toInt(m.getPrivate()));

	stmt.executeUpdate();

    }

    public void delete(String name) throws DBException, Exception {
	PreparedStatement stmt = connection_
		.prepareStatement("DELETE FROM memos WHERE memoname = ? AND username = ?");
	stmt.setString(1, name);
	stmt.setString(2, username_);
	stmt.executeUpdate();

    }

    public Collection getNames() throws Exception {
	ArrayList keys = new ArrayList();
	PreparedStatement stmt = connection_
		.prepareStatement("SELECT memoname FROM memos WHERE username = ? and deleted = 0 ORDER BY memoname");
	stmt.setString(1, username_);
	ResultSet rs = stmt.executeQuery();

	while (rs.next()) {
	    keys.add(rs.getString("memoname"));
	}

	return (keys);

    }
    
    public Memo getMemoByPalmId(int id) throws Exception
    {
	PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM memos WHERE username = ? and palmid = ? ");
	stmt.setString(1, username_);
	stmt.setInt(2,id);
	ResultSet r = null;
	try {
	    Memo m = null;
	    r = stmt.executeQuery();
	    if (r.next()) {
		m = createFrom(r);
	    }
	    return m;
	} finally {
	    if (r != null)
		r.close();
	    if (stmt != null)
		stmt.close();
	}
    }

    private PreparedStatement getPSOne(String name) throws SQLException {
	PreparedStatement stmt = connection_
		.prepareStatement("SELECT * FROM memos WHERE memoname = ? AND username = ?");
	stmt.setString(1, name);
	stmt.setString(2, username_);
	return stmt;
    }

    private PreparedStatement getPSAll() throws SQLException {
	PreparedStatement stmt = connection_
		.prepareStatement("SELECT * FROM memos WHERE username = ?");
	stmt.setString(1, username_);
	return stmt;
    }

    private Memo createFrom(ResultSet r) throws SQLException {
	Memo m = new Memo();

	m.setMemoName(r.getString("memoname"));
	m.setMemoText(r.getString("memotext"));

	m.setNew(r.getInt("new") != 0);
	m.setModified(r.getInt("modified") != 0);
	m.setDeleted(r.getInt("deleted") != 0);
	int palmid = r.getInt("palmid");
	if( !r.wasNull())
	    m.setPalmId(new Integer(palmid));
	m.setPrivate(r.getInt("private") != 0);
	

	return m;
    }

    public Collection readAll() throws DBException, Exception {
	PreparedStatement stmt = null;
	ResultSet r = null;
	try {
	    stmt = getPSAll();
	    r = stmt.executeQuery();
	    List lst = new ArrayList();
	    while (r.next()) {
		KeyedBean bean = createFrom(r);
		lst.add(bean);
	    }
	    return lst;
	} finally {
	    if (r != null)
		r.close();
	    if (stmt != null)
		stmt.close();
	}
    }

    public Memo readMemo(String name) throws DBException, Exception {
	

	PreparedStatement stmt = null;
	ResultSet r = null;
	try {
	    Memo m = null;
	    stmt = getPSOne(name);
	    r = stmt.executeQuery();
	    if (r.next()) {
		m = createFrom(r);
	    }
	    return m;
	} finally {
	    if (r != null)
		r.close();
	    if (stmt != null)
		stmt.close();
	}
    }

    public void updateMemo(Memo m) throws DBException, Exception {

	PreparedStatement stmt = connection_
		.prepareStatement("UPDATE memos SET "
			+ "memotext = ?, new = ?, modified = ?, deleted = ?, palmid = ?, private = ? "
			+ " WHERE memoname = ? AND username = ?");

	stmt.setString(1, m.getMemoText());

	stmt.setInt(2, toInt(m.getNew()));
	stmt.setInt(3, toInt(m.getModified()));
	stmt.setInt(4, toInt(m.getDeleted()));
	if( m.getPalmId() != null )
	    stmt.setInt(5, m.getPalmId().intValue());
	else
	    stmt.setNull(5, java.sql.Types.INTEGER);
	stmt.setInt(6, toInt(m.getPrivate()));
	stmt.setString(7, m.getMemoName());
	stmt.setString(8, username_);

	stmt.executeUpdate();

    }

}
