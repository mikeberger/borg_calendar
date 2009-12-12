/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui;

import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;

/**
 * launches the borg java help
 *
 */
public class HelpLauncher {

	/**
	 * launch the Borg java help
	 */
	public static void launchHelp() throws Exception {
		// Find the HelpSet file and create the HelpSet object:
		String helpHS = "BorgHelp.hs";
		ClassLoader cl = HelpLauncher.class.getClassLoader();
		URL hsURL = HelpSet.findHelpSet(cl, helpHS);
		HelpSet hs = new HelpSet(null, hsURL);
		HelpBroker hb = hs.createHelpBroker();
		hb.initPresentation();
		hb.setDisplayed(true);
	}
}