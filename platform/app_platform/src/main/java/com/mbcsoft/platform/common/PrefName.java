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
package com.mbcsoft.platform.common;

/**
 * PrefName contains all of the Borg preference definitions and default values.
 * It enforces compile time checking of preference names
 */
public class PrefName {
	
	public static PrefName USE_PROXY = new PrefName("use_proxy", "false");
	public static PrefName PROXY_HOST = new PrefName("proxy_host", "");
	public static PrefName PROXY_PORT = new PrefName("proxy_port", new Integer(8080));

	/** The EMAILPORT. */
	static public PrefName EMAILPORT = new PrefName("email_port", "25");

	/** TLS flag */
	static public PrefName ENABLETLS = new PrefName("enable_tls", "false");
	
	/** The EMAILDEBUG. */
	static public PrefName EMAILDEBUG = new PrefName("email_debug", "0");
	
	/** port for the socket listener */
	static public PrefName SOCKETPORT = new PrefName("socketport", new Integer(
			2929));
	
	// database related prefs
	/** database type. */
	static public PrefName DBTYPE = new PrefName("dbtype", "hsqldb");

	/** database host */
	static public PrefName DBHOST = new PrefName("dbhost", "localhost");

	/** databse port */
	static public PrefName DBPORT = new PrefName("dbport", "3306");

	/** database name */
	static public PrefName DBNAME = new PrefName("dbname", "borg");

	/** database user */
	static public PrefName DBUSER = new PrefName("dbuser", "borg");

	/** database password */
	static public PrefName DBPASS = new PrefName("dbpass", "borg");

	/** jdbc url - for generic jdbc only, not hsql or mysql */
	static public PrefName JDBCURL = new PrefName("jdbcurl", "");

	/** interval for db auto-sync in minutes */
	static public PrefName SYNCMINS = new PrefName("sync_mins", new Integer(0));

	/** hsql database directory */
	static public PrefName HSQLDBDIR = new PrefName("hsqldbdir", "not-set");

	/** h2 database directory */
	static public PrefName H2DIR = new PrefName("h2dir", "not-set");
	
	public static final PrefName SHUTDOWNTIME = new PrefName("shuttime", "0");
	
	public static PrefName MEMBAR_TIMEOUT = new PrefName("membar-timeout", -1);
	
	/** keystore location */
	static public PrefName KEYSTORE = new PrefName("key_store", "");

	/** cached password time to live in seconds */
	static public PrefName PASSWORD_TTL = new PrefName("pw_ttl", new Integer(
			300));
	
	/** The COUNTRY. */
	static public PrefName COUNTRY = new PrefName("country", "");

	/** The LANGUAGE. */
	static public PrefName LANGUAGE = new PrefName("language", "");



	// misc
	/** show a stack trace button on error dialogs */
	static public PrefName STACKTRACE = new PrefName("stacktrace", "false");


	/** preference name */
	private String name_;

	/** default value */
	private Object default_;

	/**
	 * Instantiates a new pref name.
	 * 
	 * @param name
	 *            the name
	 * @param def
	 *            the default value
	 */
	public PrefName(String name, Object def) {
		setName(name);
		setDefault(def);
	}

	/**
	 * Sets the name.
	 * 
	 * @param name_
	 *            the new name
	 */
	void setName(String name_) {
		this.name_ = name_;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name_;
	}

	/**
	 * Sets the default value
	 * 
	 * @param default_
	 *            the new default
	 */
	void setDefault(Object default_) {
		this.default_ = default_;
	}

	/**
	 * Gets the default value
	 * 
	 * @return the default
	 */
	public Object getDefault() {
		return default_;
	}

	
}
