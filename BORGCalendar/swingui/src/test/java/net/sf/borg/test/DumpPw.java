package net.sf.borg.test;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.ui.util.PasswordHelper;

public class DumpPw {

	public static void main(String args[]) throws Exception {
		
		boolean testing = false;

		// process command line args
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-test")) {
				testing = true;
			}

		}

		// if testing, use alternate prefs so regular prefs can be left alone
		if (testing)
			Prefs.setPrefRootNode("net/sf/borg/test");

		
		System.out.println("STORE = " + Prefs.getPref(PrefName.KEYSTORE));
		System.out.println("EMPASS = " + Prefs.getPref(PrefName.EMAILPASS));
		
		System.out.println("Clear EM Pass = " + PasswordHelper.getReference().decryptText(Prefs.getPref(PrefName.EMAILPASS), "test", false));
		

	}

}
