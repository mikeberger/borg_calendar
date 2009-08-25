package net.sf.borg.apptconduit;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.entity.Appointment;
import palm.conduit.Category;
import palm.conduit.Log;
import palm.conduit.SyncManager;
import palm.conduit.SyncProperties;
import palm.conduit.TodoRecord;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates. All
// rights reserved."

public class TodoRecordManager {

	SyncProperties props;
	int db;
	static private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	CategoryManager cm = null;
	Vector hhCats = null;

	public TodoRecordManager(SyncProperties props, int db) {
		this.props = props;
		this.db = db;
		cm = new CategoryManager(props, db);
	}

	public void WipeData() throws Exception {

		SyncManager.purgeAllRecs(db);

		if (hhCats == null) {
			hhCats = cm.getHHCategories();
		}

		Iterator cit = hhCats.iterator();
		while (cit.hasNext()) {
			Category cat = (Category) cit.next();
			cat.setName("");
		}
		Category uf = (Category) hhCats.elementAt(0);
		uf.setId(0);
		uf.setIndex(0);
		uf.setName("Unfiled");

		AppointmentModel amod = AppointmentModel.getReference();

		Collection tds = amod.get_todos();
		Iterator it = tds.iterator();
		while (it.hasNext()) {
			Appointment r = (Appointment) it.next();
			TodoRecord rec = new TodoRecord();

			rec.setId(0);
			rec.setDescription(r.getText());
			rec.setIsPrivate(r.getPrivate());

			// date is the next todo field if present, otherwise
			// the due date
			Date nt = r.getNextTodo();
			if (nt == null) {
				nt = r.getDate();
			}

			rec.setDueDate(nt);
			String note = Integer.toString(r.getKey()) + "," + sdf.format(nt);
			rec.setNote(note);

			String s = r.getCategory();
			if (s == null) {
				rec.setCategoryIndex(0);
			} else {
				// check if new cat or one already in list
				Category c = cm.matchName(s, hhCats);
				if (c != null) {
					rec.setCategoryIndex(c.getIndex());
				} else {
					// add new
					int i = cm.getNextIndex(hhCats);
					if (i == -1) {
						rec.setCategoryIndex(0);
						Log.err("cannot add category: " + s);

					} else {
						c = (Category) hhCats.elementAt(i);
						c.setId(i);
						c.setIndex(i);
						c.setName(s);
						rec.setCategoryIndex(i);

					}

				}
			}
			SyncManager.writeRec(db, rec);

		}

		cm.writeHHCategories(hhCats);

	}

}