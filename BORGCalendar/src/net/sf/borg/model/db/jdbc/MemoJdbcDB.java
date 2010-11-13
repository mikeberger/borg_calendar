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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.borg.model.db.MemoDB;
import net.sf.borg.model.entity.Memo;

/**
 * provides the JDBC layer for reading/writing Memos.
 */
public class MemoJdbcDB extends JdbcDB implements MemoDB {


	public MemoJdbcDB()
	{
		super();
		new JdbcDBUpgrader("select encrypted from memos",
				"alter table memos add column encrypted char(1) default null").upgrade();
	}
	
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.MemoDB#addMemo(net.sf.borg.model.entity.Memo)
	 */
	@Override
	public void addMemo(Memo m) throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("INSERT INTO memos ( memoname, memotext, encrypted ) "
						+ " VALUES " + "( ?, ?, ? )");

		stmt.setString(1, m.getMemoName());
		stmt.setString(2, m.getMemoText());
		if( m.isEncrypted())
			stmt.setString(3, "Y");
		else
			stmt.setString(3, "N");

		stmt.executeUpdate();
        stmt.close();

	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.MemoDB#delete(java.lang.String)
	 */
	@Override
	public void delete(String name) throws Exception {
		PreparedStatement stmt = connection_.prepareStatement("DELETE FROM memos WHERE memoname = ?");
		stmt.setString(1, name);
		stmt.executeUpdate();
        stmt.close();

	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.MemoDB#getNames()
	 */
	@Override
	public Collection<String> getNames() throws Exception {
		ArrayList<String> keys = new ArrayList<String>();
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT memoname FROM memos ORDER BY memoname");
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			keys.add(rs.getString("memoname"));
		}
		rs.close();
        stmt.close();

		return (keys);

	}
	
	private PreparedStatement getPSOne(String name) throws SQLException {
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM memos WHERE memoname = ?");
		stmt.setString(1, name);
		return stmt;
	}



	private PreparedStatement getPSAll() throws SQLException {
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM memos");
		return stmt;
	}

	
	/**
	 * create a Memo from a result set
	 * @param r the result set
	 * @return the Memo object
	 * @throws SQLException
	 */
	private Memo createFrom(ResultSet r) throws SQLException {
		Memo m = new Memo();

		m.setMemoName(r.getString("memoname"));
		m.setMemoText(r.getString("memotext"));
		String enc = r.getString("encrypted");
		if( enc != null && enc.equals("Y"))
			m.setEncrypted(true);

		return m;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.MemoDB#readAll()
	 */
	@Override
	public Collection<Memo> readAll() throws Exception {
		PreparedStatement stmt = null;
		ResultSet r = null;
		try {
			stmt = getPSAll();
			r = stmt.executeQuery();
			List<Memo> lst = new ArrayList<Memo>();
			while (r.next()) {
				Memo bean = createFrom(r);
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

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.MemoDB#readMemo(java.lang.String)
	 */
	@Override
	public Memo readMemo(String name) throws Exception {

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

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.MemoDB#updateMemo(net.sf.borg.model.entity.Memo)
	 */
	@Override
	public void updateMemo(Memo m) throws Exception {

		PreparedStatement stmt = connection_.prepareStatement("UPDATE memos SET "
				+ " memotext = ?,"
				+ " encrypted = ?"
				+ " WHERE memoname = ?");

		stmt.setString(1, m.getMemoText());
		if( m.isEncrypted())
			stmt.setString(2, "Y");
		else
			stmt.setString(2,"N");
		stmt.setString(3, m.getMemoName());

		stmt.executeUpdate();
        stmt.close();

	}

}
