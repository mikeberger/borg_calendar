package net.sf.borg.model.ical;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.parameter.Type;
import net.fortuna.ical4j.vcard.property.*;
import net.sf.borg.common.Errmsg;
import net.sf.borg.model.entity.Address;

public class AddressVcardAdapter {

    static private final Logger log = Logger.getLogger("net.sf.borg");

    static public Address fromVcard(VCard vc) {

        if (vc == null)
            return null;

        Address addr = new Address();
        addr.setVcard(vc.toString());

        net.fortuna.ical4j.vcard.property.N name = (net.fortuna.ical4j.vcard.property.N) vc.getProperty(Property.Id.N);
        if (name != null) {
            addr.setFirstName(name.getGivenName());
            addr.setLastName(name.getFamilyName());
        }

        net.fortuna.ical4j.vcard.property.Impp im = (Impp) vc.getProperty(Property.Id.IMPP);
        if (im != null)
            addr.setScreenName(im.getValue());

        net.fortuna.ical4j.vcard.property.Nickname nick = (Nickname) vc.getProperty(Property.Id.NICKNAME);
        if (nick != null)
            addr.setNickname(nick.getValue());

        net.fortuna.ical4j.vcard.property.BDay bday = (BDay) vc.getProperty(Property.Id.BDAY);
        if (bday != null)
            addr.setBirthday(bday.getDate());

        net.fortuna.ical4j.vcard.property.Org org = (Org) vc.getProperty(Property.Id.ORG);
        if (org != null)
            addr.setCompany(org.getValue());

        net.fortuna.ical4j.vcard.property.Note note = (Note) vc.getProperty(Property.Id.NOTE);
        if (note != null)
            addr.setNotes(note.getValue());

        net.fortuna.ical4j.vcard.property.Url url = (Url) vc.getProperty(Property.Id.URL);
        if (url != null)
            addr.setWebPage(url.getValue());

        for (Property p2 : vc.getProperties(Property.Id.EMAIL)) {
            addr.setEmail(p2.getValue());
            if (p2.getParameter(Parameter.Id.PREF) != null)
                break;
        }

        for (Property p2 : vc.getProperties(Property.Id.ADR)) {
            net.fortuna.ical4j.vcard.property.Address vcaddr = (net.fortuna.ical4j.vcard.property.Address) p2;
            for (Parameter pm : p2.getParameters(Parameter.Id.TYPE)) {
                Type t = (Type) pm;
                if (t.getValue().contains("HOME")) {
                    addr.setStreetAddress(vcaddr.getStreet());
                    addr.setCity(vcaddr.getLocality());
                    addr.setState(vcaddr.getRegion());
                    addr.setZip(vcaddr.getPostcode());
                    addr.setCountry(vcaddr.getCountry());
                } else if (t.getValue().contains("WORK")) {
                    addr.setWorkStreetAddress(vcaddr.getStreet());
                    addr.setWorkCity(vcaddr.getLocality());
                    addr.setWorkState(vcaddr.getRegion());
                    addr.setWorkZip(vcaddr.getPostcode());
                    addr.setWorkCountry(vcaddr.getCountry());
                }
            }
        }

        for (Property p2 : vc.getProperties(Property.Id.TEL)) {
            for (Parameter pm : p2.getParameters(Parameter.Id.TYPE)) {
                Type t = (Type) pm;
                if (t.getValue().contains("HOME"))
                    addr.setHomePhone(p2.getValue());
                else if (t.getValue().contains("WORK") && !t.getValue().contains("FAX"))
                    addr.setWorkPhone(p2.getValue());
                else if (t.getValue().contains("FAX"))
                    addr.setFax(p2.getValue());
                else if (t.getValue().contains("CELL"))
                    addr.setCellPhone(p2.getValue());
                else if (t.getValue().contains("PAGER"))
                    addr.setPager(p2.getValue());
            }
        }

        log.fine(addr.toString());
        return addr;
    }

    static public VCard toVcard(Address addr) {

        VCard vc = new VCard();

        vc.getProperties().add(new N(addr.getLastName(), addr.getFirstName(), null, null, null));
        if (addr.getScreenName() != null)
            try {
                vc.getProperties().add(new Impp(new URI(addr.getScreenName())));
            } catch (Exception e) {
                Errmsg.getErrorHandler().notice(e.getMessage());
            }

        if (addr.getNickname() != null)
            vc.getProperties().add(new Nickname(addr.getNickname()));

        if (addr.getBirthday() != null)
            vc.getProperties().add(new BDay(new Date(addr.getBirthday())));

        if (addr.getCompany() != null)
            vc.getProperties().add(new Org(addr.getCompany()));

        if (addr.getNotes() != null)
            vc.getProperties().add(new Note(addr.getNotes()));

        if (addr.getWebPage() != null)
            try {
                vc.getProperties().add(new Url(new URI(addr.getWebPage())));
            } catch (Exception e) {
                Errmsg.getErrorHandler().notice(e.getMessage());
            }

        if (addr.getEmail() != null)
            vc.getProperties().add(new Email(addr.getEmail()));

        if (addr.getStreetAddress() != null || addr.getCity() != null || addr.getState() != null || addr.getCountry() != null || addr.getZip() != null)
            vc.getProperties().add(new net.fortuna.ical4j.vcard.property.Address(null, null, addr.getStreetAddress(), addr.getCity(), addr.getState(), addr.getZip(), addr.getCountry(), new Type("HOME")));

        if (addr.getWorkStreetAddress() != null || addr.getWorkCity() != null || addr.getWorkState() != null || addr.getWorkCountry() != null || addr.getWorkZip() != null)
            vc.getProperties().add(new net.fortuna.ical4j.vcard.property.Address(null, null, addr.getWorkStreetAddress(), addr.getWorkCity(), addr.getWorkState(), addr.getWorkZip(), addr.getWorkCountry(), new Type("WORK")));

        if (addr.getCellPhone() != null)
            vc.getProperties().add(new Telephone(addr.getCellPhone(), new Type("CELL")));
        if (addr.getHomePhone() != null)
            vc.getProperties().add(new Telephone(addr.getHomePhone(), new Type("HOME")));
        if (addr.getWorkPhone() != null)
            vc.getProperties().add(new Telephone(addr.getWorkPhone(), new Type("WORK")));

        if (addr.getVcard() != null && !addr.getVcard().isEmpty()) {

            try (StringReader r = new StringReader(addr.getVcard())) {
                VCardBuilder builder = new VCardBuilder(r);
                VCard vc2 = builder.build();
                if (vc2 != null) {
                    for (Property p : vc2.getProperties()) {
                        if (vc.getProperties(p.getId()).isEmpty()) {
                            vc.getProperties().add(p);
                        }

                    }
                }
            } catch (Exception e) {
                Errmsg.getErrorHandler().errmsg(e);
                return null;
            }
        }

        log.fine(vc.toString());

        return vc;
    }

}
