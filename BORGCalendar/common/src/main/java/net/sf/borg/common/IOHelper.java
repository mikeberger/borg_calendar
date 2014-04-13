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
 
 Copyright 2004 by Mohan Embar - http://www.thisiscool.com/
 */

package net.sf.borg.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

/**
 * standard routines for file I/O with prompting
 */
public class IOHelper {

	static final Logger log = Logger.getLogger("net.sf.borg");

	/**
	 * The home directory; gets updated to ensure it is the last used directory,
	 * initialized as default
	 */
	static private File homeDirectory = new File(".");

	/**
	 * Gets the home directory
	 * 
	 * @return the directory
	 */
	public static File getHomeDirectory() {
		return (homeDirectory);
	}

	/**
	 * Sets the home directory.
	 */
	public static void setHomeDirectory(String newHome) {
		homeDirectory = new File(newHome);
	}

	/**
	 * Creates an output stream to a URL
	 * 
	 * @param url
	 *            the url
	 * 
	 * @return the output stream
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static OutputStream createOutputStream(URL url) throws Exception {
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		return connection.getOutputStream();
	}

	/**
	 * create an output stream to a file, creating parent dirs as needed
	 * 
	 * @param file
	 *            the file
	 * 
	 * @return the output stream
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static OutputStream createOutputStream(String file) throws Exception {
		File fil = new File(file);
		fil.getParentFile().mkdirs();
		// create the containing directory if it doesn't
		// already exist.
		return new FileOutputStream(fil);

	}

}
