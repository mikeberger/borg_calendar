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

import net.sf.borg.model.beans.KeyedBean;
import net.sf.borg.model.beans.Link;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.LinkDB;

class LinkJdbcDB extends JdbcBeanDB implements BeanDB, LinkDB {

	LinkJdbcDB(String url, String username) throws Exception {
		super(url, username);
		new JdbcDBUpgrader("select id from links;",
				new String[] {
				"CREATE CACHED TABLE links ("
						+ "id integer default '0' NOT NULL,"
						+ "username varchar(25) NOT NULL,"
						+ "linktype varchar(15) NOT NULL,"
						+ "ownerkey integer default '0' NOT NULL,"
						+ "ownertype varchar(15)," 
						+ "path varchar(250),"
						+ "PRIMARY KEY  (id,username))",
						"CREATE INDEX link_user ON links (username)" }).upgrade();
	}

	LinkJdbcDB(Connection conn) {
		super(conn);
	}

	public void addObj(KeyedBean bean, boolean crypt) throws 
			Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("INSERT INTO links ( id, username, linktype, ownerkey, ownertype, path) "
						+ " VALUES " + "( ?, ?, ?, ?, ?, ?)");

		Link att = (Link) bean;

		stmt.setInt(1, att.getKey());
		stmt.setString(2, username_);
		stmt.setString(3, att.getLinkType());
		stmt.setInt(4, toInt(att.getOwnerKey()));
		stmt.setString(5, att.getOwnerType());
		stmt.setString(6, att.getPath());

		stmt.executeUpdate();

		writeCache(att);

	}

	public void delete(int key) throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("DELETE FROM links WHERE id = ? AND username = ?");
		stmt.setInt(1, key);
		stmt.setString(2, username_);
		stmt.executeUpdate();

		delCache(key);
	}

	public Collection getKeys() throws Exception {
		ArrayList keys = new ArrayList();
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT id FROM links WHERE username = ? ");
		stmt.setString(1, username_);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			keys.add(new Integer(rs.getInt("id")));
		}

		return (keys);

	}

	public int nextkey() throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT MAX(id) FROM links WHERE  username = ?");
		stmt.setString(1, username_);
		ResultSet r = stmt.executeQuery();
		int maxKey = 0;
		if (r.next())
			maxKey = r.getInt(1);
		curMaxKey_ = Math.max(curMaxKey_, maxKey);
		return ++curMaxKey_;
	}

	public KeyedBean newObj() {
		return (new Link());
	}

	PreparedStatement getPSOne(int key) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM links WHERE id = ? AND username = ?");
		stmt.setInt(1, key);
		stmt.setString(2, username_);
		return stmt;
	}

	PreparedStatement getPSAll() throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM links WHERE username = ?");
		stmt.setString(1, username_);
		return stmt;
	}

	KeyedBean createFrom(ResultSet r) throws SQLException {
		Link att = new Link();
		att.setKey(r.getInt("id"));
		att.setLinkType(r.getString("linktype"));
		att.setOwnerKey(new Integer(r.getInt("ownerkey")));
		att.setOwnerType(r.getString("ownertype"));
		att.setPath(r.getString("path"));
		return att;
	}

	public void updateObj(KeyedBean bean, boolean crypt) throws 
			Exception {

		PreparedStatement stmt = connection_
				.prepareStatement("UPDATE links SET linktype = ?, ownerkey = ?, ownertype = ?, path = ?"
						+ " WHERE id = ? AND username = ?");

		Link att = (Link) bean;

		stmt.setString(1, att.getLinkType());
		stmt.setInt(2, att.getOwnerKey().intValue());
		stmt.setString(3, att.getOwnerType());
		stmt.setString(4, att.getPath());
		stmt.setInt(5, att.getKey());

		stmt.setString(6, username_);

		stmt.executeUpdate();

		delCache(att.getKey());
		writeCache(att);
	}

	public Collection getLinks(int ownerkey, String ownertype)
			throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * from links where username = ? and ownerkey = ? and ownertype = ?");
		ResultSet r = null;
		List lst = new ArrayList();
		try {

			stmt.setString(1, username_);
			stmt.setInt(2, ownerkey);
			stmt.setString(3, ownertype);
			r = stmt.executeQuery();
			while (r.next()) {
				Link s = (Link) createFrom(r);
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
