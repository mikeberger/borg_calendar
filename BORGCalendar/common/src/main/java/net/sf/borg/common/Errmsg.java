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

import java.util.logging.Logger;


/**
 * standard error handling for Borg
 */
public class Errmsg {
	
	static private final Logger log = Logger.getLogger("net.sf.borg");


	/**
	 * console error handler
	 *
	 */
	private static class DefaultErrorHandler implements ErrorHandler {
		
		@Override
		public void errmsg(Exception e) {

			// treat a warning differently - just show its text
			if (e instanceof Warning) {
				notice(e.getMessage());
				return;
			}

			log.severe(e.toString());
			e.printStackTrace();

		}

		@Override
		public void notice(String s) {

			log.info(s);
			return;

		}
	}

	// initialize to only send errors to the console
	private static ErrorHandler errorHandler = new DefaultErrorHandler();

	public static ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public static void setErrorHandler(ErrorHandler errorHandler) {
		Errmsg.errorHandler = errorHandler;
	}
	
	public static void logError(Exception e)
	{
		log.severe(e.toString());
		e.printStackTrace();
	}

}
