package net.sf.borg.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import net.sf.borg.common.EncryptionHelper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class EncryptionTests {
	
	private static String location = "keystore";
	private static String pw = "1234";
	private static String keyname = "borg_key";
	
	@BeforeClass
	static public void setup() throws Exception
	{
		EncryptionHelper.createStore(location, pw);
		
		EncryptionHelper.generateKey(location, pw, keyname);
	}
	
	@AfterClass
	static public void cleanup()
	{
		File f = new File(location);
		f.delete();
	}
	
	
	@Test
	public void helperTest() throws Exception
	{
		EncryptionHelper helper = new EncryptionHelper(location,pw);
		
		String text = "Hello There 1234329!!!!";
		
		String cipherText = helper.encrypt(text, keyname, pw);
		
		String clearText = helper.decrypt(cipherText, keyname, pw);
		
		assertTrue(text.equals(clearText));
	}
	

}
