package net.sf.borg.test;

import java.io.InputStream;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.fortuna.ical4j.vcard.VCard;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.sync.ical.AddressVcardAdapter;
import net.sf.borg.model.sync.ical.CardDav;

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

    @Test
    public void testAdapter() throws Exception {
        InputStream fin = IcalTest.class.getResourceAsStream("/test2.vcs");
        List<VCard> vcards = CardDav.importVcardFromInputStream(fin);
        Assert.assertEquals(1, vcards.size());
        VCard vc = vcards.get(0);

        Address addr = AddressVcardAdapter.fromVcard(vc);
        Assert.assertEquals("Michael", addr.getFirstName());
        Assert.assertEquals("Berger", addr.getLastName());
        Assert.assertEquals("9081112222", addr.getCellPhone());
        Assert.assertEquals("1111111111", addr.getHomePhone());
        Assert.assertEquals("2222222222", addr.getWorkPhone());
        Assert.assertEquals("mike@mbcsoft.com", addr.getEmail());
        Assert.assertEquals("922 Main St", addr.getStreetAddress());
        Assert.assertEquals("Some Town", addr.getCity());
        Assert.assertEquals("Florida", addr.getState());
        Assert.assertEquals("12345", addr.getZip());
        Assert.assertEquals("work st", addr.getWorkStreetAddress());
        Assert.assertEquals("Piscataway", addr.getWorkCity());
        Assert.assertEquals("New Jersey", addr.getWorkState());
        Assert.assertEquals("08854", addr.getWorkZip());

        addr.setEmail("updated@email.com");
        addr.setNickname("dopey");
        addr.setZip("00000");
        addr.setCompany("MBCSOFT");

        String vcs = addr.getVcard();
        addr.setVcard(null);

        VCard vc2 = AddressVcardAdapter.toVcard(addr);
        Address addr2 = AddressVcardAdapter.fromVcard(vc2);

        Assert.assertEquals("Michael", addr2.getFirstName());
        Assert.assertEquals("Berger", addr2.getLastName());
        Assert.assertEquals("9081112222", addr2.getCellPhone());
        Assert.assertEquals("1111111111", addr2.getHomePhone());
        Assert.assertEquals("2222222222", addr2.getWorkPhone());
        Assert.assertEquals("updated@email.com", addr2.getEmail());
        Assert.assertEquals("922 Main St", addr2.getStreetAddress());
        Assert.assertEquals("Some Town", addr2.getCity());
        Assert.assertEquals("Florida", addr2.getState());
        Assert.assertEquals("00000", addr2.getZip());
        Assert.assertEquals("work st", addr2.getWorkStreetAddress());
        Assert.assertEquals("Piscataway", addr2.getWorkCity());
        Assert.assertEquals("New Jersey", addr2.getWorkState());
        Assert.assertEquals("08854", addr2.getWorkZip());
        Assert.assertEquals("dopey", addr2.getNickname());
        Assert.assertEquals("MBCSOFT", addr2.getCompany());
        Assert.assertEquals("mbcsoft.com", addr2.getWebPage());


        addr.setVcard(vcs);
        VCard vc3 = AddressVcardAdapter.toVcard(addr);
        addr2 = AddressVcardAdapter.fromVcard(vc3);

        Assert.assertEquals("Michael", addr2.getFirstName());
        Assert.assertEquals("Berger", addr2.getLastName());
        Assert.assertEquals("9081112222", addr2.getCellPhone());
        Assert.assertEquals("1111111111", addr2.getHomePhone());
        Assert.assertEquals("2222222222", addr2.getWorkPhone());
        Assert.assertEquals("updated@email.com", addr2.getEmail());
        Assert.assertEquals("922 Main St", addr2.getStreetAddress());
        Assert.assertEquals("Some Town", addr2.getCity());
        Assert.assertEquals("Florida", addr2.getState());
        Assert.assertEquals("00000", addr2.getZip());
        Assert.assertEquals("work st", addr2.getWorkStreetAddress());
        Assert.assertEquals("Piscataway", addr2.getWorkCity());
        Assert.assertEquals("New Jersey", addr2.getWorkState());
        Assert.assertEquals("08854", addr2.getWorkZip());
        Assert.assertEquals("dopey", addr2.getNickname());
        Assert.assertEquals("MBCSOFT", addr2.getCompany());
        Assert.assertEquals("mbcsoft.com", addr2.getWebPage());


    }
}