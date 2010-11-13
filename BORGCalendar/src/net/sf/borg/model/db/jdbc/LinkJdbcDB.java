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

import net.sf.borg.model.db.LinkDB;
import net.sf.borg.model.entity.Link;

/**
 * provides the JDBC layer for reading/writing Links
 */
public class LinkJdbcDB extends JdbcBeanDB<Link> implements LinkDB {


	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.EntityDB#addObj(net.sf.borg.model.entity.KeyedEntity)
	 */
	@Override
	public void addObj(Link att) throws 
			Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("INSERT INTO links ( id, linktype, ownerkey, ownertype, path) "
						+ " VALUES " + "( ?, ?, ?, ?, ?)");

		
		stmt.setInt(1, att.getKey());
		stmt.setString(2, att.getLinkType());
		stmt.setInt(3, toInt(att.getOwnerKey()));
		stmt.setString(4, att.getOwnerType());
		stmt.setString(5, att.getPath());

		stmt.executeUpdate();
        stmt.close();

		writeCache(att);

	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.EntityDB#delete(int)
	 */
	@Override
	public void delete(int key) throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("DELETE FROM links WHERE id = ?");
		stmt.setInt(1, key);
		stmt.executeUpdate();
        stmt.close();

		delCache(key);
	}

	/**
	 * Gets the keys.
	 * 
	 * @return the keys
	 * 
	 * @throws Exception the exception
	 */
	public Collection<Integer> getKeys() throws Exception {
		ArrayList<Integer> keys = new ArrayList<Integer>();
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT id FROM links");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			keys.add(new Integer(rs.getInt("id")));
		}
		rs.close();
        stmt.close();

		return (keys);

	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.EntityDB#nextkey()
	 */
	@Override
	public int nextkey() throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT MAX(id) FROM links");
		ResultSet r = stmt.executeQuery();
		int maxKey = 0;
		if (r.next())
			maxKey = r.getInt(1);
		r.close();
        stmt.close();

		return ++maxKey;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.EntityDB#newObj()
	 */
	@Override
	public Link newObj() {
		return (new Link());
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#getPSOne(int)
	 */
	@Override
	PreparedStatement getPSOne(int key) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM links WHERE id = ?");
		stmt.setInt(1, key);
		
		return stmt;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#getPSAll()
	 */
	@Override
	PreparedStatement getPSAll() throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM links");
		return stmt;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#createFrom(java.sql.ResultSet)
	 */
	@Override
	Link createFrom(ResultSet r) throws SQLException {
		Link att = new Link();
		att.setKey(r.getInt("id"));
		att.setLinkType(r.getString("linktype"));
		att.setOwnerKey(new Integer(r.getInt("ownerkey")));
		att.setOwnerType(r.getString("ownertype"));
		att.setPath(r.getString("path"));
		return att;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.EntityDB#updateObj(net.sf.borg.model.entity.KeyedEntity)
	 */
	@Override
	public void updateObj(Link att) throws 
			Exception {

		PreparedStatement stmt = connection_
				.prepareStatement("UPDATE links SET linktype = ?, ownerkey = ?, ownertype = ?, path = ?"
						+ " WHERE id = ?");

	
		stmt.setString(1, att.getLinkType());
		stmt.setInt(2, att.getOwnerKey().intValue());
		stmt.setString(3, att.getOwnerType());
		stmt.setString(4, att.getPath());
		stmt.setInt(5, att.getKey());

		stmt.executeUpdate();
        stmt.close();

		delCache(att.getKey());
		writeCache(att);
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.LinkDB#getLinks(int, java.lang.String)
	 */
	@Override
	public Collection<Link> getLinks(int ownerkey, String ownertype)
			throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * from links where ownerkey = ? and ownertype = ?");
		ResultSet r = null;
		List<Link> lst = new ArrayList<Link>();
		try {

			stmt.setInt(1, ownerkey);
			stmt.setString(2, ownertype);
			r = stmt.executeQuery();
			while (r.next()) {
				Link s = createFrom(r);
				lst.add(s);
			}

		} finally {
			if (r != null)
				r.close();
			if (stmt != null)
				stmt.close();
		}
		return lst;
	}

}
