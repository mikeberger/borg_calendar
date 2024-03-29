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

package net.sf.borg.ui.util;

import net.sf.borg.common.ErrorHandler;
import net.sf.borg.common.Warning;

import java.util.logging.Logger;

/**
 * UI error handling for Borg
 */
public class UIErrorHandler implements ErrorHandler {
	
	static private final Logger log = Logger.getLogger("net.sf.borg");

	/**
	 * Output an exception to the user.
	 * 
	 * @param e
	 *            the e
	 */
	@Override
	public void errmsg(Exception e) {

		// treat a warning differently - just show its text
		if (e instanceof Warning) {
			notice(e.getMessage());
			return;
		}
		
		// log the error
		log.severe(e.toString());

		// dump the stack trace to stderr
		e.printStackTrace();
		
		ScrolledDialog.showError(e);

	}

	/**
	 * output a notice/warning - just shows text
	 * 
	 * @param s
	 *            the text to show
	 */
	@Override
	public void notice(String s) {

		ScrolledDialog.showNotice(s);
	}

}
