package net.sf.borg.model.sync.ical;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.IOHelper;
import net.sf.borg.common.Warning;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.entity.Address;

public class CardDav {
	
	//static private final Logger log = Logger.getLogger("net.sf.borg");

	static private void setHints() {
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
	}
	
	
	public static void exportToFile(String filename) throws Exception {
		
		OutputStream oostr = IOHelper.createOutputStream(filename);
		OutputStreamWriter wr = new OutputStreamWriter(oostr);
		
		for( Address addr : AddressModel.getReference().getAddresses()) {
			wr.write(AddressVcardAdapter.toVcard(addr).toString());
		}

		wr.close();
		oostr.close();
	}
	
	static public List<VCard> importVcardFromFile(String file) throws Exception {
		
		setHints();
		
		InputStream is = new FileInputStream(file);
		return importVcardFromInputStream(is);
	}
	
	static public List<VCard> importVcardFromInputStream(InputStream is) throws Exception {

		setHints();

		VCardBuilder builder = new VCardBuilder(is);
		List<VCard> l = builder.buildAll();
		is.close();

		return l;
	}

	static public String importVCard(List<VCard> vcards) throws Exception {

		setHints();

		StringBuffer warning = new StringBuffer();

		ArrayList<Address> addrs = new ArrayList<Address>();

		AddressModel amodel = AddressModel.getReference();
		for( VCard vc : vcards){

			Address addr = AddressVcardAdapter.fromVcard(vc);
			if (addr != null)
				addrs.add(addr);

		}

		int imported = 0;

		for (Address addr : addrs) {

			try {
				amodel.saveAddress(addr);
				imported++;
			}
			catch(Warning w){
				Errmsg.getErrorHandler().notice(w.getMessage() + "\n\n" + addr.toString());
			}
			catch(Exception e){
				Errmsg.getErrorHandler().errmsg(e);
			}

		}

		int dels = amodel.removeDuplicates();

		warning.append("Imported: " + imported + "\n");
		warning.append("Duplicates Removed: " + dels + "\n");
		warning.append("Skipped: " + (vcards.size() - imported) + "\n");

		if (warning.length() == 0)
			return (null);

		return (warning.toString());

	}

}
