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

import net.sf.borg.model.beans.Link;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.LinkDB;

public class LinkJdbcDB extends JdbcBeanDB<Link> implements BeanDB<Link>, LinkDB {

	public LinkJdbcDB(String url) throws Exception {
		super(url);
	}

	LinkJdbcDB(Connection conn) {
		super(conn);
	}

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

		writeCache(att);

	}

	public void delete(int key) throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("DELETE FROM links WHERE id = ?");
		stmt.setInt(1, key);
		stmt.executeUpdate();

		delCache(key);
	}

	public Collection<Integer> getKeys() throws Exception {
		ArrayList<Integer> keys = new ArrayList<Integer>();
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT id FROM links");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			keys.add(new Integer(rs.getInt("id")));
		}

		return (keys);

	}

	public int nextkey() throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT MAX(id) FROM links");
		ResultSet r = stmt.executeQuery();
		int maxKey = 0;
		if (r.next())
			maxKey = r.getInt(1);
		return ++maxKey;
	}

	public Link newObj() {
		return (new Link());
	}

	PreparedStatement getPSOne(int key) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM links WHERE id = ?");
		stmt.setInt(1, key);
		return stmt;
	}

	PreparedStatement getPSAll() throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM links");
		return stmt;
	}

	Link createFrom(ResultSet r) throws SQLException {
		Link att = new Link();
		att.setKey(r.getInt("id"));
		att.setLinkType(r.getString("linktype"));
		att.setOwnerKey(new Integer(r.getInt("ownerkey")));
		att.setOwnerType(r.getString("ownertype"));
		att.setPath(r.getString("path"));
		return att;
	}

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

		delCache(att.getKey());
		writeCache(att);
	}

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
