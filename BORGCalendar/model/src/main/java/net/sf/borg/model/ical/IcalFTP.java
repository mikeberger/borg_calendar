package net.sf.borg.model.ical;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

public class IcalFTP {

	private static void checkReply(FTPClient c) throws Exception {
		int reply = c.getReplyCode();

		if (!FTPReply.isPositiveCompletion(reply)) {
			throw new Exception("FTP Error: " + reply);
		}
	}

	public static void exportftp(Integer years) throws Exception {

			String icalString = "";
			if (years != null) {
				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.YEAR, -1 * years.intValue());
				icalString = ICal.exportIcalToString(cal
						.getTime());
			} else {
				icalString = ICal.exportIcalToString(null);
			}

			//System.out.println(icalString);
			FTPClient client = new FTPClient();

			try {
				client.connect(Prefs.getPref(PrefName.FTPSERVER));
				checkReply(client);

				client.login(Prefs.getPref(PrefName.FTPUSER),
						gep());
				checkReply(client);
				client.enterLocalPassiveMode();
				checkReply(client);
				InputStream is = new ByteArrayInputStream(icalString.getBytes());

				//
				// Store file to server
				//
				client.storeFile(Prefs.getPref(PrefName.FTPPATH), is);
				checkReply(client);

				client.logout();
			} finally {
				client.disconnect();
			}

	}

	public static void sep(String s) throws Exception {
		if ("".equals(s)) {
			Prefs.putPref(PrefName.FTPPW, s);
			return;
		}
		String p1 = Prefs.getPref(PrefName.FTPPW2);
		if ("".equals(p1)) {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			SecretKey key = keyGen.generateKey();
			p1 = new String(Base64.getEncoder().encode(key.getEncoded()));
			Prefs.putPref(PrefName.FTPPW2, p1);
		}

		byte[] ba = Base64.getDecoder().decode(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, enc);
		os.write(s.getBytes());
		os.close();
		ba = baos.toByteArray();
		Prefs.putPref(PrefName.FTPPW, new String(Base64.getEncoder().encode(ba)));
	}

	public static String gep() throws Exception {
		String p1 = Prefs.getPref(PrefName.FTPPW2);
		String p2 = Prefs.getPref(PrefName.FTPPW);
		if ("".equals(p2))
			return p2;

		if ("".equals(p1)) {
			sep(p2); // transition case
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
