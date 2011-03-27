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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.db.MemoDB;
import net.sf.borg.model.db.jdbc.MemoJdbcDB;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.undo.MemoUndoItem;
import net.sf.borg.model.undo.UndoLog;

/**
 * The Memo Model manages the Memo Entities. Memos are keyed by a name. Memos contain simple text and
 * have stayed simple to be able to sync with the simple memo functionality of a Palm Pilot.
 */
public class MemoModel extends Model implements Searchable<Memo> {

	/** The normalized date format for timestamps in a memo  */
	private static SimpleDateFormat normalDateFormat_ = new SimpleDateFormat(
			"MM/dd/yyyy hh:mm aa");

	/**
	 * class XmlContainer is solely for JAXB XML export/import
	 * to keep the same XML structure as before JAXB was used
	 */
	@XmlRootElement(name="MEMOS")
	private static class XmlContainer {		
		public Collection<Memo> Memo;		
	}
	
	/** The db */
	private MemoDB db_; // the database

	/** The singleton */
	static private MemoModel self_ = new MemoModel();

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 */
	public static MemoModel getReference() {
		return (self_);
	}


	/**
	 * Gets the dB.
	 * 
	 * @return the dB
	 */
	public MemoDB getDB() {
		return db_;
	}

	/**
	 * Gets all memos.
	 * 
	 * @return all memos
	 * 
	 * @throws Exception the exception
	 */
	public Collection<Memo> getMemos() throws Exception {
		Collection<Memo> memos = db_.readAll();
		Iterator<Memo> it = memos.iterator();
		while (it.hasNext()) {
			Memo memo = it.next();
			parseOutDates(memo);
		}
		return memos;
	}

	/**
	 * Gets all memo names.
	 * 
	 * @return the memo names
	 * 
	 * @throws Exception the exception
	 */
	public Collection<String> getNames() throws Exception {

		return db_.getNames();
	}

	/**
	 * Instantiates a new memo model.
	 */
	private MemoModel() {
		db_ = new MemoJdbcDB();	
	}

	/**
	 * Delete a memo by name
	 * 
	 * @param name the memo name
	 * @param undo true if we are executing an undo
	 */
	public void delete(String name, boolean undo) {

		try {
			Memo m = getMemo(name);
			
			LinkModel.getReference().deleteLinksFromEntity(m);
			LinkModel.getReference().deleteLinksToEntity(m);

			if (m == null)
				return;
			Memo orig = m.copy();

			db_.delete(m.getMemoName());
			if (!undo) {
				UndoLog.getReference().addItem(MemoUndoItem.recordDelete(orig));
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		refresh();
	}

	/**
	 * Save a memo.
	 * 
	 * @param memo the memo
	 */
	public void saveMemo(Memo memo) {
		saveMemo(memo, false);
	}

	/**
	 * Save a memo.
	 * 
	 * @param memo the memo
	 * @param undo true if we are executing an undo
	 */
	public void saveMemo(Memo memo, boolean undo) {

		try {

			// determine create an update dates
			Date now = new Date();
			if (memo.getCreated() == null)
				memo.setCreated(now);
			memo.setUpdated(now);

			// add the timestamp string to the text
			addDateString(memo);
			String name = memo.getMemoName();
			Memo old = db_.readMemo(name);
			if (old == null) {
				db_.addMemo(memo);
				if (!undo) {
					UndoLog.getReference()
							.addItem(MemoUndoItem.recordAdd(memo));
				}

			} else {
				db_.updateMemo(memo);
				if (!undo) {
					UndoLog.getReference().addItem(
							MemoUndoItem.recordUpdate(old));
				}

			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// inform views of data change
		refresh();
	}

	/**
	 * Adds the timestamp string to a memo. This is just a string to hold creation and
	 * last update dates. the borg ui shows these dates. The palm pilot knows nothing about them.
	 * On the palm, they appear as extra memo text - and there is nowhere else to keep them
	 * 
	 * @param m the memo
	 */
	static private void addDateString(Memo m) {
		if (m.getCreated() == null || m.getUpdated() == null)
			return;
		if (m.getMemoText() == null)
			m.setMemoText("");
		
		// if memo already has memo text - remove it
		String text = m.getMemoText();
		if (text.startsWith("TS;")) {
			int idx1 = 2;
			int idx2 = text.indexOf(';', idx1 + 1);
			int idx3 = text.indexOf(';', idx2 + 1);
			if (idx2 != -1 && idx3 != -1) {	
					m.setMemoText(text.substring(idx3 + 1));
			}
		}
		
		
		m.setMemoText("TS;" + normalDateFormat_.format(m.getCreated()) + ";"
				+ normalDateFormat_.format(m.getUpdated()) + ";"
				+ m.getMemoText());

	}

	/**
	 * Parses the timestamps.
	 * 
	 * @param m the memp
	 */
	static private void parseOutDates(Memo m) {

		// separate timestamps if needed TS:created;updated;
		String text = m.getMemoText();
		if (text == null)
			return;
		if (text.startsWith("TS;")) {
			int idx1 = 2;
			int idx2 = text.indexOf(';', idx1 + 1);
			int idx3 = text.indexOf(';', idx2 + 1);
			if (idx2 != -1 && idx3 != -1) {
				try {
					Date create = normalDateFormat_.parse(text.substring(
							idx1 + 1, idx2));
					Date update = normalDateFormat_.parse(text.substring(
							idx2 + 1, idx3));
					if (create != null)
						m.setCreated(create);
					if (update != null)
						m.setUpdated(update);
					m.setMemoText(text.substring(idx3 + 1));
				} catch (Exception e) {
				  // empty
				}
			}
		}
	}

	/**
	 * Gets a memo by name.
	 * 
	 * @param name the memo name
	 * 
	 * @return the memo
	 * 
	 * @throws Exception the exception
	 */
	public Memo getMemo(String name) throws Exception {
		Memo m = db_.readMemo(name);

		if (m == null)
			return null;
		parseOutDates(m);

		return m;
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
        container.Memo = getMemos();
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
		
		if( container.Memo == null ) return;

		for (Memo memo : container.Memo ) {
			memo.setKey(-1);
			saveMemo(memo, true);
		}

		refresh();
	}

	/**
	 * Refresh listeners
	 */
	public void refresh() {
		refreshListeners();
	}


	/* (non-Javadoc)
	 * @see net.sf.borg.model.Searchable#search(net.sf.borg.model.SearchCriteria)
	 */
	@Override
	public Collection<Memo> search(SearchCriteria criteria) {
		Collection<Memo> res = new ArrayList<Memo>(); // result collection
		try {
			
			// do not match if a search category is set
			if (!criteria.getCategory().equals("")
					&& !criteria.getCategory().equals(CategoryModel.UNCATEGORIZED))
				return res;

			Collection<Memo> memos = getMemos();

			for (Memo memo : memos) {
				
				// do not search on encrypted memos
				if( memo.isEncrypted() )
					continue;

				String tx = memo.getMemoName() + " " + memo.getMemoText();
				
				if( !criteria.search(tx))
					continue;
				
				// add the appt to the search results
				res.add(memo);

			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
		return (res);
	}
	
	@Override
	public String getExportName() {
		return "MEMOS";
	}


	@Override
	public String getInfo() throws Exception {
		return Resource.getResourceString("Memos") + ": "
		+ getMemos().size();
	}
}
