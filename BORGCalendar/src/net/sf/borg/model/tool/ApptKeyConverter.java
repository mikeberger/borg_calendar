package net.sf.borg.model.tool;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Vector;

import net.sf.borg.common.DateUtil;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.db.jdbc.ApptJdbcDB;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.entity.Appointment;

public class ApptKeyConverter {

	public static void main(String[] args) {
		try {
			new ApptKeyConverter().convert();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void convert() throws Exception {
		
		ApptJdbcDB db = new ApptJdbcDB();

		try {
			// init cal model & load data from database
			String dbdir = JdbcDB.buildDbDir();

			if (dbdir.equals("not-set")) {
				return;
			}

			System.out.println("DB URL: " + dbdir);

			JdbcDB.connect(dbdir);

			db.beginTransaction();
			
			int lowestKey = 1;
			Collection<Appointment> appts = db.readAll();
			for (Appointment oldappt : appts) {

				// skip already converted appts
				if (oldappt.getKey() < 10000000) {
					continue;
				}

				System.out.println("Processing appt: " + oldappt.getKey());

				// find the lowest key
				while (true) {
					Appointment ap = db.readObj(lowestKey);
					if (ap == null)
						break;
				}

				System.out.println("New Key: " + lowestKey);

				Appointment newappt = oldappt.copy();
				newappt.setKey(lowestKey);

				// adjust skip list
				Vector<String> skipList = newappt.getSkipList();
				if (skipList != null && skipList.size() > 0) {
					for (int i = 0; i < skipList.size(); i++) {
						String key = skipList.elementAt(i);
						int val = Integer.parseInt(key);
						if (val < 900000)
							continue;

						int yr = (val / 1000000) % 1000 + 1900;
						int mo = (val / 10000) % 100 - 1;
						int day = (val / 100) % 100;
						Calendar cal = new GregorianCalendar();
						cal.set(yr, mo, day);
						skipList.setElementAt(Integer.toString(DateUtil
								.dayOfEpoch(cal.getTime())), i);

					}
					newappt.setSkipList(skipList);
				}

				db.addObj(newappt);

				LinkModel.getReference().moveLinks(oldappt, newappt);
				db.delete(oldappt.getKey());

				lowestKey++;
			}

			db.commitTransaction();

		} catch (Exception e) {
			e.printStackTrace();
			db.rollbackTransaction();
		} finally {

			JdbcDB.close();

		}
	}

}
