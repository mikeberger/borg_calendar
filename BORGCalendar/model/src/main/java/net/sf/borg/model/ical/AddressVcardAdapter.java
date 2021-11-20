package net.sf.borg.model.ical;

import java.io.StringReader;
import java.util.logging.Logger;

import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.parameter.Type;
import net.fortuna.ical4j.vcard.property.BDay;
import net.fortuna.ical4j.vcard.property.Impp;
import net.fortuna.ical4j.vcard.property.Nickname;
import net.fortuna.ical4j.vcard.property.Note;
import net.fortuna.ical4j.vcard.property.Org;
import net.fortuna.ical4j.vcard.property.Url;
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
			for (Parameter pm : p2.getParameters(Parameter.Id.TYPE)) {
				addr.setEmail(p2.getValue());
				Type t = (Type) pm;
				if (t.getValue().contains("PREF"))
					break;
			}
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
		if (addr.getVcard() != null && !addr.getVcard().isEmpty()) {

			try (StringReader r = new StringReader(addr.getVcard())) {
				VCardBuilder builder = new VCardBuilder(r);
				vc = builder.build();
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
				return null;
			} 
		}

		return vc;
	}

}
