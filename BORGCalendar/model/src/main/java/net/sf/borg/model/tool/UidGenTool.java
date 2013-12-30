package net.sf.borg.model.tool;

import java.util.Collection;

import net.sf.borg.model.db.AppointmentDB;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Appointment;


public class UidGenTool implements ConversionTool{

	public static void main(String[] args) {
		try {		
			new UidGenTool().convert();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	@Override
	public void convert() throws Exception {
		
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());

		// init cal model & load data from database
		String dbdir = DBHelper.getController().buildURL();

		if (dbdir.equals("not-set")) {
			return;
		}
		DBHelper.getController().connect(dbdir);
		
		AppointmentDB db = DBHelper.getFactory().createAppointmentDB();

		try {

			DBHelper.getController().beginTransaction();

			// get all appointments
			Collection<Appointment> appts = db.readAll();
			for (Appointment appt : appts) {

				if (appt.getUid() != null) {
					continue;
				}

				System.out.println("Processing appt: " + appt.getKey());

				appt.setUid(Integer.toString(appt.getKey()) + "@BORGA-" + appt.getCreateTime().getTime());

				// add the converted appointment
				db.updateObj(appt);
				
			}

			DBHelper.getController().commitTransaction();

		} catch (Exception e) {
			DBHelper.getController().rollbackTransaction();
			throw e;
		} finally {
			DBHelper.getController().close();
		}
	}

}
