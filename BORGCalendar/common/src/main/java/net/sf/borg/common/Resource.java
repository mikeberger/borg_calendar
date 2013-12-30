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
package net.sf.borg.common;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Common logic for dealing with Resource Bundles. Resource strings are stored
 * in a standard resource bundle, but may have extra keyboard shortcut
 * information added which defines the keyboard shortcuts for manu items and
 * buttons.
 */
public class Resource {

	/** the borg version */
	private static String version_ = null;

	/**
	 * Get a resource string from the borg bundle. Translates escaped newlines
	 * to real newlines. String will contain any keyboard shortcut info
	 * 
	 * @param key
	 *            the resource key
	 * 
	 * @return the resource string or "??key??" if the string is not found
	 */
	public static String getRawResourceString(String key) {
		try {
			String res = ResourceBundle.getBundle("borg_resource").getString(
					key);

			if (res.indexOf("\\n") == -1)
				return (res);

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < res.length(); i++) {

				if (res.charAt(i) == '\\' && (i < res.length() - 1)
						&& res.charAt(i + 1) == 'n') {
					i++;
					sb.append('\n');
				} else {
					sb.append(res.charAt(i));
				}
			}

			return (sb.toString());
		} catch (MissingResourceException m) {
			return ("?" + key + "?");
		}
	}

	/**
	 * Get a resource string from the borg resource bundle. Translates escaped
	 * newlines to real newlines. Substitute variables
	 * 
	 * @param resourceKey
	 *            the resource key
	 * @param params
	 *            substitutable parameters
	 * 
	 * @return the resource string
	 */
	public static String getResourceString(String resourceKey, Object[] params) {
		return MessageFormat.format(getResourceString(resourceKey), params);
	}

	/**
	 * Get a resource string from the borg resource bundle. Translates escaped
	 * newlines to real newlines.
	 * 
	 * @param resourceKey
	 *            the resource key
	 * 
	 * @return the resource string
	 */
	public static String getResourceString(String resourceKey) {
		return parseResourceText(getRawResourceString(resourceKey));
	}

	/**
	 * Gets the borg version, which is stored in its own properties file
	 * 
	 * @return the version
	 */
	public static String getVersion() {
		if (version_ == null) {
			try {
				// get the version and build info from a properties file in the
				// jar file
				InputStream is = Resource.class.getResource("/properties")
						.openStream();
				Properties props = new Properties();
				props.load(is);
				is.close();
				version_ = props.getProperty("borg.version");
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
		}

		return (version_);
	}

	private static String parseResourceText(String s) {
		int pos;
		if ((pos = s.indexOf('|')) != -1)
			return (s.substring(0, pos));
		return s;
	}


}
