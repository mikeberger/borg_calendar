package net.sf.borg.ui.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.sf.borg.common.EncryptionHelper;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

// one time password migration for old email passwords
public class PwMigration {

	private static final PrefName PWMIGRATED = new PrefName("pwmigrated", "false");
	private static final PrefName EMAILPASS2 = new PrefName("email_pass2", "");

	static public void migratePasswords() {

		boolean done = Prefs.getBoolPref(PWMIGRATED);
		if (done)
			return;

		String keystore = Prefs.getPref(PrefName.KEYSTORE);

		try {

			// if there is a keystore, then migrate the passwords
			if (keystore != null && !keystore.isEmpty()) {

				String emailKey = Prefs.getPref(EMAILPASS2);
				if (emailKey != null && !emailKey.isEmpty()) {
					// decrypt existing pw
					String pw = emailgep();

					// encrypt from new key and store
					EncryptionHelper helper = new EncryptionHelper(
							PasswordHelper.getReference().getEncryptionKeyWithoutTimeout("Migrate Email Password"));
					Prefs.putPref(PrefName.EMAILPASS, helper.encrypt(pw));

					// delete old pref key
					Prefs.delPref(EMAILPASS2);
				}

				
				
			} else {
				
				Prefs.delPref(PrefName.EMAILPASS);
				Prefs.delPref(EMAILPASS2);

			}


		} catch (Exception e) {
			Errmsg.getErrorHandler().notice("Password Migration Failed. Please manually reset passwords.");
			Prefs.delPref(PrefName.EMAILPASS);
			Prefs.delPref(EMAILPASS2);
		}
		
		
		Prefs.putPref(PWMIGRATED, "true");


	}

	

	// legacy code for the email password
	private static String emailgep() throws Exception {
		String p1 = Prefs.getPref(EMAILPASS2);
		String p2 = Prefs.getPref(PrefName.EMAILPASS);
		if ("".equals(p2))
			return p2;

		if ("".equals(p1)) {
			return p2;
		}

		byte[] ba = Base64.getDecoder().decode(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher dec = Cipher.getInstance("AES");
		dec.init(Cipher.DECRYPT_MODE, key);
		byte[] decba = Base64.getDecoder().decode(p2);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, dec);
		os.write(decba);
		os.close();

		return baos.toString();

	}
}
