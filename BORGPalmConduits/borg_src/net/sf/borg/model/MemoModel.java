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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.sf.borg.common.Errmsg;
import net.sf.borg.model.beans.Memo;
import net.sf.borg.model.db.MemoDB;
import net.sf.borg.model.db.remote.RemoteMemoDB;

public class MemoModel  {

	private static SimpleDateFormat normalDateFormat_ = new SimpleDateFormat(
			"MM/dd/yyyy hh:mm aa");

	private MemoDB db_; // the database

	static private MemoModel self_ = null;

	public static MemoModel getReference() {
		return (self_);
	}

	public static MemoModel create() {
		self_ = new MemoModel();
		return (self_);
	}

	
	public MemoDB getDB() {
		return db_;
	}

	public Collection getMemos() throws Exception {
		Collection memos = db_.readAll();
		Iterator it = memos.iterator();
		while (it.hasNext()) {
			Memo memo = (Memo) it.next();
			if (memo.getDeleted())
				it.remove();
			parseOutDates(memo);
		}
		return memos;
	}

	public Collection getNames() throws Exception {

		return db_.getNames();
	}

	public Collection getDeletedMemos() throws Exception {
		Collection memos = db_.readAll();
		Iterator it = memos.iterator();
		while (it.hasNext()) {
			Memo memo = (Memo) it.next();
			if (!memo.getDeleted())
				it.remove();
			parseOutDates(memo);
		}
		return memos;
	}

	public void open_db()
			throws Exception {

		db_ = new RemoteMemoDB();
	}

	public void forceDelete(Memo memo) throws Exception {

		try {

			db_.delete(memo.getMemoName());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		refresh();
	}

	public void saveMemo(Memo memo, boolean sync) throws Exception {

		// determine dates
		Date now = new Date();
		if (memo.getCreated() == null)
			memo.setCreated(now);
		memo.setUpdated(now);

		addDateString(memo);
		String name = memo.getMemoName();
		Memo old = db_.readMemo(name);
		if (old == null) {
			if (!sync) {
				memo.setNew(true);
				memo.setDeleted(false);
				memo.setModified(false);
			}

			db_.addMemo(memo);

		} else {

			if (!sync) {
				memo.setModified(true);
				memo.setDeleted(false);
			}
			db_.updateMemo(memo);

		}

		// inform views of data change
		refresh();
	}

	static private void addDateString(Memo m) {
		if (m.getCreated() == null || m.getUpdated() == null)
			return;
		if (m.getMemoText() == null)
			m.setMemoText("");
		m.setMemoText("TS;" + normalDateFormat_.format(m.getCreated()) + ";"
				+ normalDateFormat_.format(m.getUpdated()) + ";"
				+ m.getMemoText());

	}

	static private void parseOutDates(Memo m) {

		// separate timestamps if needed TS:created;updated;
		String text = m.getMemoText();
		if (text == null)
			return;
		if (  text.startsWith("TS;")) {
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

	public void refresh() {
		
	}

	public void close_db() throws Exception {
		db_.close();
	}

	public Memo getMemoByPalmId(int id) throws Exception {
		Memo m = db_.getMemoByPalmId(id);
		if (m != null)
			parseOutDates(m);
		return m;
	}
}
