package net.sf.borg.model.db;

import lombok.Getter;
import lombok.Setter;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.entity.Address;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;

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
	private static Controller controller;
	
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
	public interface Factory
	{
		AppointmentDB createAppointmentDB();
		CheckListDB createCheckListDB();
		LinkDB createLinkDB();
		MemoDB createMemoDB();
		OptionDB createOptionDB();
		TaskDB createTaskDB();
		EntityDB<Address> createAddressDB();
	}
	
	/**
	 * Interface to be implemented by all DB controllers
	 * It contains methods that are not specific to a DB type
	 *
	 */
	public interface Controller
	{
		/**
		 * build the DB URL based on the user's DB options
		 */
		String buildURL();
		/**
		 * perform the initial connection to the DB
		 * @param url
		 * @throws Exception
		 */
		void connect(String url) throws Exception;
		void close() throws Exception;
		void execSQL(String string) throws Exception;
		ResultSet execQuery(String string) throws Exception;
		void beginTransaction() throws Exception;
		void commitTransaction() throws Exception;
		void rollbackTransaction() throws Exception;
		Connection getConnection();
	}
}
