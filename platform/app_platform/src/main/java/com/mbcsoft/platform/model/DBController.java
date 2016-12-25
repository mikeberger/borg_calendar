package com.mbcsoft.platform.model;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Interface to be implemented by all DB controllers It contains methods that
 * are not specific to a DB type
 *
 */
public interface DBController {
	/**
		 * build the DB URL based on the user's DB options
		 */
		public String buildURL();

	/**
		 * perform the initial connection to the DB
		 * @param url
		 * @throws Exception
		 */
		public void connect(String url) throws Exception;

	public void close() throws Exception;

	public void reopen() throws Exception;

	public void execSQL(String string) throws Exception;

	public ResultSet execQuery(String string) throws Exception;

	public void beginTransaction() throws Exception;

	public void commitTransaction() throws Exception;

	public void rollbackTransaction() throws Exception;

	public Connection getConnection();
}
