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

 Copyright 2003-2010 by Mike Berger
 */
package net.sf.borg.model;

import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.db.CheckListDB;
import net.sf.borg.model.db.jdbc.CheckListJdbcDB;
import net.sf.borg.model.entity.CheckList;
import net.sf.borg.model.undo.CheckListUndoItem;
import net.sf.borg.model.undo.UndoLog;

/**
 * The CheckList Model manages the CheckList Entities. CheckLists are keyed by a name. CheckLists contain simple text and
 * have stayed simple to be able to sync with the simple checkList functionality of a Palm Pilot.
 */
public class CheckListModel extends Model  {

	/**
	 * class XmlContainer is solely for JAXB XML export/import
	 * to keep the same XML structure as before JAXB was used
	 */
	@XmlRootElement(name="CHECKLISTS")
	private static class XmlContainer {		
		public Collection<CheckList> CheckList;		
	}
	
	/** The db */
	private CheckListDB db_; // the database

	/** The singleton */
	static private CheckListModel self_ = new CheckListModel();

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 */
	public static CheckListModel getReference() {
		return (self_);
	}


	/**
	 * Gets the dB.
	 * 
	 * @return the dB
	 */
	public CheckListDB getDB() {
		return db_;
	}

	/**
	 * Gets all checkLists.
	 * 
	 * @return all checkLists
	 * 
	 * @throws Exception the exception
	 */
	public Collection<CheckList> getCheckLists() throws Exception {
		return db_.readAll();
	}

	/**
	 * Gets all checkList names.
	 * 
	 * @return the checkList names
	 * 
	 * @throws Exception the exception
	 */
	public Collection<String> getNames() throws Exception {

		return db_.getNames();
	}

	/**
	 * Instantiates a new checkList model.
	 */
	private CheckListModel() {
		db_ = new CheckListJdbcDB();	
	}

	/**
	 * Delete a checkList by name
	 * 
	 * @param name the checkList name
	 * @param undo true if we are executing an undo
	 */
	public void delete(String name, boolean undo) {

		try {
			CheckList m = getCheckList(name);
			
			LinkModel.getReference().deleteLinksToEntity(m);

			if (m == null)
				return;
			CheckList orig = m.clone();

			db_.delete(m.getCheckListName());
			if (!undo) {
				UndoLog.getReference().addItem(CheckListUndoItem.recordDelete(orig));
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		refresh();
	}

	/**
	 * Save a checkList.
	 * 
	 * @param checkList the checkList
	 */
	public void saveCheckList(CheckList checkList) {
		saveCheckList(checkList, false);
	}

	/**
	 * Save a checkList.
	 * 
	 * @param checkList the checkList
	 * @param undo true if we are executing an undo
	 */
	public void saveCheckList(CheckList checkList, boolean undo) {

		try {

			String name = checkList.getCheckListName();
			CheckList old = db_.readCheckList(name);
			if (old == null) {
				db_.addCheckList(checkList);
				if (!undo) {
					UndoLog.getReference()
							.addItem(CheckListUndoItem.recordAdd(checkList));
				}

			} else {
				db_.updateCheckList(checkList);
				if (!undo) {
					UndoLog.getReference().addItem(
							CheckListUndoItem.recordUpdate(old));
				}

			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// inform views of data change
		refresh();
	}


	/**
	 * Gets a checkList by name.
	 * 
	 * @param name the checkList name
	 * 
	 * @return the checkList
	 * 
	 * @throws Exception the exception
	 */
	public CheckList getCheckList(String name) throws Exception {
		return db_.readCheckList(name);
	}

	/**
	 * Export to XML
	 * 
	 * @param fw the writer to write XML to
	 * 
	 * @throws Exception the exception
	 */
	public void export(Writer fw) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
        Marshaller m = jc.createMarshaller();
        XmlContainer container = new XmlContainer();
        container.CheckList = getCheckLists();
        m.marshal(container, fw);

	}

	/**
	 * Import xml.
	 * 
	 * @param is the input stream containing the XML
	 * 
	 * @throws Exception the exception
	 */
	public void importXml(InputStream is) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Unmarshaller u = jc.createUnmarshaller();
		
		XmlContainer container =
			  (XmlContainer)u.unmarshal(
			    is );
		
		if( container.CheckList == null ) return;

		for (CheckList checkList : container.CheckList ) {
			saveCheckList(checkList,true);
		}

		refresh();
	}

	/**
	 * Refresh listeners
	 */
	public void refresh() {
		refreshListeners();
	}

	@Override
	public String getExportName() {
		return "CHECKLISTS";
	}


	@Override
	public String getInfo() throws Exception {
		return Resource.getResourceString("CheckLists") + ": "
		+ getCheckLists().size();
	}

}
