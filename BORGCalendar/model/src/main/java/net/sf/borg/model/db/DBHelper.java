package net.sf.borg.model.db;

import java.text.DateFormat;
import java.util.Date;

import com.mbcsoft.platform.common.PrefName;
import com.mbcsoft.platform.common.Prefs;
import com.mbcsoft.platform.common.Resource;
import com.mbcsoft.platform.common.Warning;
import com.mbcsoft.platform.model.DBController;
import com.mbcsoft.platform.model.EntityDB;
import com.mbcsoft.platform.model.OptionDB;

import lombok.Getter;
import lombok.Setter;
import net.sf.borg.model.entity.Address;

/**
 * DBHelper abstracts and hides the lower level db implementation from the model
 * It holds an application global database factory and database controller.
 * The rest of the application is not aware of the underlying DB provider
 *
 */
public class DBHelper {
	
	@Getter
	@Setter
	private static Factory factory;
	@Getter
	@Setter
	private static DBController controller;
	
	/**
	 * check if the shutdown timestamp stored in the DB differs from the timestamp
	 * stored in the java prefs. throw a Warning if there is a difference, which in the worst case would indicate
	 * a failure to write the DB. One case when this can happen is when a PC is put to sleep and the OS looses
	 * track of a removable drive containing the database. The timestamp to the java prefs succeeds, but the DB write fails
	 * as the DB is no longer mounted. It seems that HSQL can continue running even when the OS unmounts the db filesystem (linux)
	 * @throws Exception
	 */
	public static void checkTimestamp() throws Exception {
		OptionDB odb = getFactory().createOptionDB();
		String option = odb.getOption(PrefName.SHUTDOWNTIME.getName());
		if (option != null
				&& !option.equals(PrefName.SHUTDOWNTIME.getDefault())) {
			String preftime = Prefs.getPref(PrefName.SHUTDOWNTIME);
			if (!preftime.equals(PrefName.SHUTDOWNTIME.getDefault())) {
	
				// only error if prefs have later time than DB
				long preflong = Long.parseLong(preftime);
				long dblong = Long.parseLong(option);
				if (preflong > dblong) {
					Date pdate = new Date(preflong);
					Date dbdate = new Date(dblong);
					throw new Warning(
							Resource.getResourceString("db_time_error")
									+ "\n\n["
									+ DateFormat.getDateTimeInstance().format(
											pdate)
									+ " > "
									+ DateFormat.getDateTimeInstance().format(
											dbdate) + "]");
				}
			}
		}
	}
	
	/**
	 * interface to be implemented by all DB Factories
	 * It contains methods to create the types of DBs used by the models
	 *
	 */
	public static interface Factory
	{
		public AppointmentDB createAppointmentDB();
		public CheckListDB createCheckListDB();
		public LinkDB createLinkDB();
		public MemoDB createMemoDB();
		public OptionDB createOptionDB();
		public TaskDB createTaskDB();
		public EntityDB<Address> createAddressDB();
	}
	
	
}
