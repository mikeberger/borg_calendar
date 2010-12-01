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

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.model.db.CheckListDB;
import net.sf.borg.model.entity.CheckList;

/**
 * provides the JDBC layer for reading/writing CheckLists.
 */
public class CheckListJdbcDB extends JdbcDB implements CheckListDB {

	public CheckListJdbcDB() {
		super();
		new JdbcDBUpgrader(
				"select name from checkLists",
				"CREATE CACHED TABLE checkLists (name varchar(50) NOT NULL,text longvarchar,PRIMARY KEY (name));")
				.upgrade();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.borg.model.db.CheckListDB#addCheckList(net.sf.borg.model.entity
	 * .CheckList)
	 */
	@Override
	public void addCheckList(CheckList m) throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("INSERT INTO checkLists ( name, text ) "
						+ " VALUES " + "( ?, ? )");

		stmt.setString(1, m.getCheckListName());
		stmt.setString(2, getItemsXml(m));

		stmt.executeUpdate();
		stmt.close();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.db.CheckListDB#delete(java.lang.String)
	 */
	@Override
	public void delete(String name) throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("DELETE FROM checkLists WHERE name = ?");
		stmt.setString(1, name);
		stmt.executeUpdate();
		stmt.close();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.db.CheckListDB#getNames()
	 */
	@Override
	public Collection<String> getNames() throws Exception {
		ArrayList<String> keys = new ArrayList<String>();
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT name FROM checkLists ORDER BY name");
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			keys.add(rs.getString("name"));
		}
		rs.close();
		stmt.close();

		return (keys);

	}

	private PreparedStatement getPSOne(String name) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM checkLists WHERE name = ?");
		stmt.setString(1, name);
		return stmt;
	}

	private PreparedStatement getPSAll() throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM checkLists");
		return stmt;
	}

	/**
	 * create a CheckList from a result set
	 * 
	 * @param r
	 *            the result set
	 * @return the CheckList object
	 * @throws SQLException
	 */
	private CheckList createFrom(ResultSet r) throws Exception {
		CheckList m = new CheckList();

		m.setCheckListName(r.getString("name"));
		setItemsFromXml(m,r.getString("text"));

		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.db.CheckListDB#readAll()
	 */
	@Override
	public Collection<CheckList> readAll() throws Exception {
		PreparedStatement stmt = null;
		ResultSet r = null;
		try {
			stmt = getPSAll();
			r = stmt.executeQuery();
			List<CheckList> lst = new ArrayList<CheckList>();
			while (r.next()) {
				CheckList bean = createFrom(r);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.db.CheckListDB#readCheckList(java.lang.String)
	 */
	@Override
	public CheckList readCheckList(String name) throws Exception {

		PreparedStatement stmt = null;
		ResultSet r = null;
		try {
			CheckList m = null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.borg.model.db.CheckListDB#updateCheckList(net.sf.borg.model.entity
	 * .CheckList)
	 */
	@Override
	public void updateCheckList(CheckList m) throws Exception {

		PreparedStatement stmt = connection_
				.prepareStatement("UPDATE checkLists SET "
						+ " text = ? WHERE name = ?");

		stmt.setString(1, getItemsXml(m));
		stmt.setString(2, m.getCheckListName());

		stmt.executeUpdate();
		stmt.close();

	}

	@XmlRootElement(name = "ITEMS")
	private static class XmlContainer {
		public Collection<CheckList.Item> items;
	}

	/**
	 * get the net.sf.borg.ui.checklist items from a net.sf.borg.ui.checklist in XML for peristing
	 * 
	 * @param cl
	 * @return items XML string
	 * @throws JAXBException
	 */
	private String getItemsXml(CheckList cl) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Marshaller m = jc.createMarshaller();
		XmlContainer container = new XmlContainer();
		container.items = cl.getItems();
		StringWriter sw = new StringWriter();
		m.marshal(container, sw);
		return sw.toString();

	}

	/**
	 * set the items in a net.sf.borg.ui.checklist from XML
	 * 
	 * @param cl
	 *            - the net.sf.borg.ui.checklist
	 * @param itemXml
	 *            - the XML
	 * @throws JAXBException 
	 */
	private void setItemsFromXml(CheckList cl, String itemXml) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Unmarshaller u = jc.createUnmarshaller();

		XmlContainer container = (XmlContainer) u.unmarshal(new StringReader(
				itemXml));
		
		cl.getItems().clear();
		if( container.items != null )
			cl.getItems().addAll(container.items);

	}
}
