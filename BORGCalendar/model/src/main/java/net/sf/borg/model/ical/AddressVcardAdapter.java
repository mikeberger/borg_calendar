package net.sf.borg.model.ical;

import java.util.logging.Logger;

import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.sf.borg.model.entity.Address;

public class AddressVcardAdapter {

	static private final Logger log = Logger.getLogger("net.sf.borg");
	
	static public Address fromVcard( VCard vc ) {
		
		if( vc == null ) return null;
		
		Address addr = new Address();
		addr.setVcard(vc.toString());
		
		Property p = vc.getProperty(Property.Id.N);
		String names[] = p.getValue().split(";");
		if( names.length > 1) {
			addr.setFirstName(names[1]);
			addr.setLastName(names[0]);
		}
		
		p = vc.getProperty(Property.Id.FN);
		if( p != null ) addr.setScreenName(p.getValue());
		
		p = vc.getProperty(Property.Id.NICKNAME);
		if( p != null ) addr.setNickname(p.getValue());
		
		
		
		
		log.fine(addr.toString());
		return addr;
	}
	
	
	static public VCard toVcard( Address addr ) {
		return null;
	}

}
