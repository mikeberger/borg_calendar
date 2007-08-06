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
import java.util.Collection;
import java.util.Iterator;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.common.XTree;
import net.sf.borg.model.beans.Memo;
import net.sf.borg.model.beans.MemoXMLAdapter;
import net.sf.borg.model.db.BeanDataFactoryFactory;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.IBeanDataFactory;
import net.sf.borg.model.db.MemoDB;

public class MemoModel extends Model {

    private MemoDB db_; // the database

    static private MemoModel self_ = null;

    public static MemoModel getReference() {
	return (self_);
    }

    public static MemoModel create() {
	self_ = new MemoModel();
	return (self_);
    }

    public void remove() {
	removeListeners();
	try {
	    if (db_ != null)
		db_.close();
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	    System.exit(0);
	}
	db_ = null;
    }

    public MemoDB getDB() {
	return db_;
    }
    
    public boolean hasMemos()
    {
	if( db_ != null )
	    return true;
	return false;
    }

    public Collection getMemos() throws DBException, Exception {
	Collection memos = db_.readAll();
	Iterator it = memos.iterator();
	while (it.hasNext()) {
	    Memo memo = (Memo) it.next();
	    if (memo.getDeleted())
		it.remove();
	}
	return memos;
    }

    public Collection getNames() throws DBException, Exception {

	return db_.getNames();
    }

    public Collection getDeletedMemos() throws DBException, Exception {
	Collection memos = db_.readAll();
	Iterator it = memos.iterator();
	while (it.hasNext()) {
	    Memo memo = (Memo) it.next();
	    if (!memo.getDeleted())
		it.remove();
	}
	return memos;
    }

    public void open_db(String url, String username, boolean readonly,
	    boolean shared) throws Exception {

	StringBuffer tmp = new StringBuffer(url);
	IBeanDataFactory factory = BeanDataFactoryFactory.getInstance()
		.getFactory(tmp, readonly, shared);
	url = tmp.toString();
	// let the factory tweak dbdir

	db_ = factory.createMemoDB(url, username);
	if (db_ == null)
	    throw new Warning(Resource
		    .getPlainResourceString("MemosNotSupported"));
    }

    public void delete(String name, boolean refresh) throws Exception {

	try {
	    Memo m = db_.readMemo(name);
	    if (m == null)
		return;
	    String sync = Prefs.getPref(PrefName.PALM_SYNC);
	    if (sync.equals("true")) {
		m.setDeleted(true);
		db_.updateMemo(m);
	    } else {
		db_.delete(m.getMemoName());
	    }
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	if (refresh)
	    refresh();
    }

    public void forceDelete(Memo memo) throws Exception {

	try {

	    db_.delete(memo.getMemoName());
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	refresh();
    }

    public void saveMemo(Memo memo) throws Exception {
	saveMemo(memo, false);
    }

    public void saveMemo(Memo memo, boolean sync) throws Exception {

	String name = memo.getMemoName();
	Memo old = db_.readMemo(name);
	if (old == null) {
	    if (!sync) {
		memo.setNew(true);
		memo.setDeleted(false);
		memo.setModified(false);
	    }
	    try {
		db_.addMemo(memo);
	    } catch (DBException e) {
		Errmsg.errmsg(e);
	    }
	} else {
	    try {
		if (!sync) {
		    memo.setModified(true);
		    memo.setDeleted(false);
		}
		db_.updateMemo(memo);
	    } catch (DBException e) {
		Errmsg.errmsg(e);
	    }
	}

	// inform views of data change
	refresh();
    }

    public Memo getMemo(String name) throws DBException, Exception {
	Memo m = db_.readMemo(name);
	if (m == null)
	    return null;
	if (m.getDeleted() == true)
	    return null;
	return m;
    }

    public void export(Writer fw) throws Exception {

	// FileWriter fw = new FileWriter(fname);
	fw.write("<MEMOS>\n");
	MemoXMLAdapter ta = new MemoXMLAdapter();

	// export Memoes
	try {

	    Collection memos = getMemos();
	    Iterator ti = memos.iterator();
	    while (ti.hasNext()) {
		Memo memo = (Memo) ti.next();

		XTree xt = ta.toXml(memo);
		fw.write(xt.toString());
	    }
	} catch (DBException e) {
	    if (e.getRetCode() != DBException.RET_NOT_FOUND)
		Errmsg.errmsg(e);
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
	    Memo memo = (Memo) aa.fromXml(ch);
	    memo.setKey(-1);
	    saveMemo(memo);
	}

	refresh();
    }

    public void refresh() {
	refreshListeners();
    }

    public void sync() {
	refresh();
    }

    public void close_db() throws Exception {
	db_.close();
    }

    public Memo getMemoByPalmId(int id) throws Exception {
	return db_.getMemoByPalmId(id);
    }
}
