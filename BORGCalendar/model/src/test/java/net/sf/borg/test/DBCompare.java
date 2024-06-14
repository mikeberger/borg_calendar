package net.sf.borg.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Appointment;

public class DBCompare {

	static public void main(String[] args) throws Exception {

		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:sqlite:C:\\Users\\deskp\\OneDrive\\borg_h2/borg_sqlite.db");

		Collection<Appointment> origappts = AppointmentModel.getReference().getAllAppts();

		DBHelper.getController().close();
		DBHelper.getController().connect("jdbc:sqlite:C:\\Users\\deskp\\Desktop\\borg_h2/borg_sqlite.db");

		Collection<Appointment> newappts = AppointmentModel.getReference().getAllAppts();

		DBHelper.getController().close();

		Set<String> keyset = new HashSet<String>();
		for (Appointment appt : newappts) {

			keyset.add(apptKey(appt));
		}

		for (Appointment appt : origappts) {
			if (!keyset.contains(apptKey(appt))) {
				System.out.println(appt);
			}
		}

		keyset.clear();

		for (Appointment appt : origappts) {

			keyset.add(apptKey(appt));
		}

		for (Appointment appt : newappts) {
			if (!keyset.contains(apptKey(appt))) {
				System.out.println(appt);
			}
		}

	}

	private static String apptKey(Appointment appt) {
		if (appt.isTodo())
			return "todo-" + appt.getText();
		return appt.getDate() + "-" + appt.getText();
	}

}
