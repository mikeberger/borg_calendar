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
package net.sf.borg.model;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.XTree;
import net.sf.borg.model.db.MemoDB;
import net.sf.borg.model.db.jdbc.MemoJdbcDB;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.undo.MemoUndoItem;
import net.sf.borg.model.undo.UndoLog;
import net.sf.borg.model.xml.MemoXMLAdapter;

public class MemoModel extends Model {

	private static SimpleDateFormat normalDateFormat_ = new SimpleDateFormat(
			"MM/dd/yyyy hh:mm aa");

	private MemoDB db_; // the database

	static private MemoModel self_ = null;

	public static MemoModel getReference() {
		if( self_ == null)
			self_ = new MemoModel();
		return (self_);
	}


	public MemoDB getDB() {
		return db_;
	}

	public boolean hasMemos() {
		if (db_ != null)
			return true;
		return false;
	}

	public Collection<Memo> getMemos() throws Exception {
		Collection<Memo> memos = db_.readAll();
		Iterator<Memo> it = memos.iterator();
		while (it.hasNext()) {
			Memo memo = it.next();
			parseOutDates(memo);
		}
		return memos;
	}

	public Collection<String> getNames() throws Exception {

		return db_.getNames();
	}

	private MemoModel() {
		db_ = new MemoJdbcDB();	
	}

	public void delete(String name, boolean undo) {

		try {
			Memo m = getMemo(name);

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

	public void saveMemo(Memo memo) {
		saveMemo(memo, false);
	}

	public void saveMemo(Memo memo, boolean undo) {

		try {

			// determine dates
			Date now = new Date();
			if (memo.getCreated() == null)
				memo.setCreated(now);
			memo.setUpdated(now);

			addDateString(memo);
			String name = memo.getMemoName();
			Memo old = db_.readMemo(name);
			if (old == null) {

				memo.setModified(true);
				db_.addMemo(memo);
				if (!undo) {
					UndoLog.getReference()
							.addItem(MemoUndoItem.recordAdd(memo));
				}

			} else {
				memo.setModified(true);
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

				}
			}
		}
	}

	public Memo getMemo(String name) throws Exception {
		Memo m = db_.readMemo(name);

		if (m == null)
			return null;
		parseOutDates(m);

		return m;
	}

	public void export(Writer fw) throws Exception {

		// FileWriter fw = new FileWriter(fname);
		fw.write("<MEMOS>\n");
		MemoXMLAdapter ta = new MemoXMLAdapter();

		// export Memoes
		for (Memo memo : getMemos()) {
			XTree xt = ta.toXml(memo);
			fw.write(xt.toString());
		}

		fw.write("</MEMOS>");

	}

	public void importXml(XTree xt) throws Exception {

		MemoXMLAdapter aa = new MemoXMLAdapter();

		for (int i = 1;; i++) {
			XTree ch = xt.child(i);
			if (ch == null)
				break;

			if (!ch.name().equals("Memo"))
				continue;
			Memo memo = aa.fromXml(ch);
			memo.setKey(-1);
			saveMemo(memo);
		}

		refresh();
	}

	public void refresh() {
		refreshListeners();
	}
}
