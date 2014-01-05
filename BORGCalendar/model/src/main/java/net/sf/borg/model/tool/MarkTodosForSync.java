package net.sf.borg.model.tool;

import java.util.Collection;

import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.ical.SyncLog;


public class MarkTodosForSync implements ConversionTool{

	public static void main(String[] args) {
		try {		
			new MarkTodosForSync().convert();
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
		
		Collection<Appointment> todos = AppointmentModel.getReference().get_todos();
		for( Appointment todo : todos )
		{
			SyncLog.getReference().update(new ChangeEvent(todo, ChangeEvent.ChangeAction.CHANGE));
		}
		
		
	}

}
