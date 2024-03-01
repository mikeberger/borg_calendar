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
 
Copyright 2009 by Mike Berger
 */
package net.sf.borg.common;

import javax.swing.*;
import java.util.Date;

/**
 * PasswordHelper provides logic to prompt for an encryption password and cache
 * the password for a given amount of time
 */
public class PasswordHelper {

	/* the singleton */
	private static PasswordHelper singleton = null;

	/* the cached password */
	private String password = null;

	/* the creation date of the password */
	private Date creationDate = new Date();

	/**
	 * get a reference to the singleton PasswordHelper
	 * 
	 * @return the PasswordHelper singleton
	 */
	public static PasswordHelper getReference() {
		if (singleton == null)
			singleton = new PasswordHelper();
		return singleton;
	}

	/**
	 * returns the cached password or prompts the user to enter one if none exists
	 * or the current one is expired
	 * 
	 * @return the password
	 * @throws Exception
	 */
	public String getPasswordWithTimeout(String reason) throws Exception {
		// always check current value of password expiration time in prefs in case it
		// has changed
		int pw_ttl = Prefs.getIntPref(PrefName.PASSWORD_TTL);
		Date expirationDate = new Date();
		expirationDate.setTime(creationDate.getTime() + 1000 * pw_ttl);
		if (password == null || expirationDate.before(new Date())) {
			promptForPassword(reason);
		}

		return password;
	}

	public String getPasswordWithoutTimeout(String reason) throws Exception {

		if (password == null) {
			promptForPassword(reason);
		}

		return password;
	}

	private void promptForPassword(String reason) throws Exception {
		// prompt for a new password
		JLabel label = new JLabel(Resource.getResourceString("Password_Prompt") + " " + reason);
		JPasswordField jpf = new JPasswordField();
		int result = JOptionPane.showConfirmDialog(null, new Object[] { label, jpf },
				Resource.getResourceString("Password"), JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
			password = null;
		} else {
			password = new String(jpf.getPassword());

			// validate
			try {
				new EncryptionHelper(password);

				// set expiration
				creationDate = new Date();
			} catch (Exception e) {
				password = null;
				throw e;
			}
		}
	}
}
