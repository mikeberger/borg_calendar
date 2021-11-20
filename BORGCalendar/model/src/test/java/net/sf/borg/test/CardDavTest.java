package net.sf.borg.test;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.fortuna.ical4j.connector.dav.CardDavCollection;
import net.fortuna.ical4j.connector.dav.CardDavStore;
import net.fortuna.ical4j.vcard.VCard;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.ical.AddressVcardAdapter;
import net.sf.borg.model.ical.CardDav;

public class CardDavTest {

	static private final Logger log = Logger.getLogger("net.sf.borg");

	public static void main(String args[]) throws Exception {
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		log.addHandler(ch);
		log.setUseParentHandlers(false);

		log.setLevel(Level.ALL);

		CardDavStore store = CardDav.connect();

		CardDavCollection col = CardDav.getCollection(store, Prefs.getPref(PrefName.CARDDAV_BOOK));
		System.out.println(col.getId());

		for (VCard vc : col.getComponents()) {
			System.out.println(vc.toString());
			/*Address addr = */AddressVcardAdapter.fromVcard(vc);
			//System.out.println(addr.toString());
		}

	}
}