package net.sf.borg.test;

import net.fortuna.ical4j.connector.dav.CardDavCollection;
import net.fortuna.ical4j.connector.dav.CardDavStore;
import net.fortuna.ical4j.vcard.VCard;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.ical.AddressVcardAdapter;
import net.sf.borg.model.ical.CardDav;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CardDavTest {

    static private final Logger log = Logger.getLogger("net.sf.borg");

    @BeforeClass
    public static void setUp() throws Exception {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        log.addHandler(ch);
        log.setUseParentHandlers(false);

        log.setLevel(Level.ALL);


    }

    //@Test
    public void testCardDav() throws Exception {
        CardDavStore store = CardDav.connect();

        CardDavCollection col = CardDav.getCollection(store, Prefs.getPref(PrefName.CARDDAV_BOOK));
        System.out.println(col.getId());

        for (VCard vc : col.getComponents()) {
            /*Address addr = */
            AddressVcardAdapter.fromVcard(vc);
        }
    }

    @Test
    public void testImport() throws Exception {
        InputStream fin = IcalTest.class.getResourceAsStream("/test.vcs");
        List<VCard> vcards = CardDav.importVcardFromInputStream(fin);
        Assert.assertEquals(2, vcards.size());
        for (VCard vc : vcards) {
            /*Address addr = */
            AddressVcardAdapter.fromVcard(vc);
        }
    }
}