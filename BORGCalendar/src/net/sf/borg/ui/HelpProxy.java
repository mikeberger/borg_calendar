package net.sf.borg.ui;

import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;

public class HelpProxy {

	public static void launchHelp() throws Exception {
		//		 Find the HelpSet file and create the HelpSet object:
		String helpHS = "BorgHelp.hs";
		ClassLoader cl = HelpProxy.class.getClassLoader();
		URL hsURL = HelpSet.findHelpSet(cl, helpHS);
		HelpSet hs = new HelpSet(null, hsURL);

		//		 Create a HelpBroker object:
		HelpBroker hb = hs.createHelpBroker();
		hb.initPresentation();
		hb.setDisplayed(true);
	}
}