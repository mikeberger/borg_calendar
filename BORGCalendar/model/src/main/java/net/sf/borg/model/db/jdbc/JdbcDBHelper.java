package net.sf.borg.model.db.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;

import net.sf.borg.model.db.AppointmentDB;
import net.sf.borg.model.db.CheckListDB;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.EntityDB;
import net.sf.borg.model.db.LinkDB;
import net.sf.borg.model.db.MemoDB;
import net.sf.borg.model.db.OptionDB;
import net.sf.borg.model.db.TaskDB;
import net.sf.borg.model.entity.Address;

/**
 * Helper class that provides a db factory and some db control functionality to packages outside of the jdbc package.
 * This is the only functionality from the jdbc package that is visible to other packages
 */
public class JdbcDBHelper implements DBHelper.Factory, DBHelper.Controller {

	@Override
	public AppointmentDB createAppointmentDB() {
		return new ApptJdbcDB();
	}

	@Override
	public CheckListDB createCheckListDB() {
		return new CheckListJdbcDB();
	}

	@Override
	public LinkDB createLinkDB() {
		return new LinkJdbcDB();
	}

	@Override
	public MemoDB createMemoDB() {
		return new MemoJdbcDB();
	}

	@Override
	public OptionDB createOptionDB() {
		return new OptionJdbcDB();
	}

	@Override
	public TaskDB createTaskDB() {
		return new TaskJdbcDB();
	}

	@Override
	public EntityDB<Address> createAddressDB() {
		return new AddrJdbcDB();
	}

	@Override
	public String buildURL() {
		return JdbcDB.buildDbDir();
	}

	@Override
	public void connect(String url) throws Exception {
		JdbcDB.connect(url);
	}

	@Override
	public void close() throws Exception {
		JdbcDB.close();
	}

	@Override
	public void execSQL(String string) throws Exception {
		JdbcDB.execSQL(string);
	}
	
	@Override
	public ResultSet execQuery(String string) throws Exception {
		return JdbcDB.execQuery(string);
	}

	@Override
	public void beginTransaction() throws Exception {
		JdbcDB.beginTransaction();
	}

	@Override
	public void commitTransaction() throws Exception {
		JdbcDB.commitTransaction();
	}

	@Override
	public void rollbackTransaction() throws Exception {
		JdbcDB.rollbackTransaction();
	}

	@Override
	public Connection getConnection() {
		return JdbcDB.getConnection();
	}

	@Override
	public void reopen() throws Exception {
		JdbcDB.reopen();

	}

}
