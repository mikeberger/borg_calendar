package net.sf.borg.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import net.sf.borg.common.EncryptionHelper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EncryptionTest {

	private static String location = "keystore";
	private static String pw = "1234";
	private static String keyname = "borg_key";

	@BeforeClass
	static public void setup() throws Exception {
	  // empty
	}

	@AfterClass
	static public void cleanup() {
		File f = new File(location);
		f.delete();
	}

	@Test
	public void helperTest() throws Exception {
		// create key store
		EncryptionHelper.createStore(location, pw);

		// generate a key
		EncryptionHelper.generateKey(location, pw, keyname);

		// test encrypt/decrypt
		EncryptionHelper helper = new EncryptionHelper(location, pw);
		String text = "Hello There 1234329!!!!";
		String cipherText = helper.encrypt(text, keyname);
		String clearText = helper.decrypt(cipherText, keyname);
		assertTrue(text.equals(clearText));

		// export key
		String export = helper.exportKey(keyname, pw);
		System.out.println(export);

		// delete store
		File f = new File(location);
		f.delete();
		
		// recreate store via import
		EncryptionHelper.createStore(location, pw);
		EncryptionHelper.importKey(location, export, keyname, pw);
		
		// verify import
		helper = new EncryptionHelper(location, pw);
		clearText = helper.decrypt(cipherText, keyname);
		assertTrue(text.equals(clearText));
	}
	
	/*
	 * just to export keys for testing
	 */
	public static void main(String args[]) throws Exception
	{
		EncryptionHelper helper = new EncryptionHelper(args[0], args[1]);
		String export = helper.exportKey(keyname, args[1]);
		System.out.println(export);
	}

}
