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
package net.sf.borg.plugin.common;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Resource {

	public static String getResourceString(String key) {
		try {
			String res = ResourceBundle.getBundle("resource/plugin_resource")
					.getString(key);

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
			// fall back to BORG resources
			return net.sf.borg.common.Resource.getResourceString(key);
		}
	}



}
