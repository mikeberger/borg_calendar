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

// one time password migration for old email and caldav passwords
public class PwMigration {

	private static final PrefName PWMIGRATED = new PrefName("pwmigrated", "false");
	private static final PrefName EMAILPASS2 = new PrefName("email_pass2", "");
	private static final PrefName CALDAV_PASSWORD2 = new PrefName("caldav-password2", "");

	static public void migratePasswords() {

		boolean done = Prefs.getBoolPref(PWMIGRATED);
		if (done)
			return;

		String keystore = Prefs.getPref(PrefName.KEYSTORE);

		try {

			// if there is a keystore, then migrate the email and caldav passwords
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

				String caldavKey = Prefs.getPref(CALDAV_PASSWORD2);
				if (caldavKey != null && !caldavKey.isEmpty()) {
					// decrypt existing pw
					String pw = caldavgep();

					// encrypt from new key and store
					EncryptionHelper helper = new EncryptionHelper(
							PasswordHelper.getReference().getEncryptionKeyWithoutTimeout("Migrate Caldav Password"));
					Prefs.putPref(PrefName.CALDAV_PASSWORD, helper.encrypt(pw));

					// delete old pref key
					Prefs.delPref(CALDAV_PASSWORD2);
				}
			} else {
				// there is no keystore - just delete the old pws. user will have to recreate
				Prefs.delPref(PrefName.CALDAV_PASSWORD);
				Prefs.delPref(CALDAV_PASSWORD2);
				Prefs.delPref(PrefName.EMAILPASS);
				Prefs.delPref(EMAILPASS2);

			}


		} catch (Exception e) {
			Errmsg.getErrorHandler().notice("Password Migration Failed. Please manually reset Caldav and/or Email passwords.");
			Prefs.delPref(PrefName.CALDAV_PASSWORD);
			Prefs.delPref(CALDAV_PASSWORD2);
			Prefs.delPref(PrefName.EMAILPASS);
			Prefs.delPref(EMAILPASS2);
		}
		
		
		Prefs.putPref(PWMIGRATED, "true");


	}

	// legacy code to get the caldav password
	private static String caldavgep() throws Exception {
		String p1 = Prefs.getPref(CALDAV_PASSWORD2);
		String p2 = Prefs.getPref(PrefName.CALDAV_PASSWORD);
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
