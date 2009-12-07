package net.sf.borg.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * class containing encryption and decryption methods for borg
 * 
 */
public class EncryptionHelper {

	/* the cached keystore object */
	private KeyStore keyStore;
	
	/* cached password */
    private String password;
    
	/**
	 * create a new JCEKS Key Store 
	 * @param location - location (file) for the key store
	 * @param password - key store password
	 * @throws Exception
	 */
	static public void createStore(String location, String password)
			throws Exception {
		KeyStore store = KeyStore.getInstance("JCEKS");
		store.load(null, password.toCharArray());
		store.store(new FileOutputStream(location), password.toCharArray());
	}

	/**
	 * generate a new encryption key in the key store. the key store password will
	 * be used as the key password.
	 * @param location - key store location
	 * @param password - key store password
	 * @param name - key alias
	 * @throws Exception
	 */
	static public void generateKey(String location, String password, String name)
			throws Exception {
		KeyStore store = KeyStore.getInstance("JCEKS");
		store.load(new FileInputStream(location), password.toCharArray());

		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		SecretKey key = keyGen.generateKey();
		
		KeyStore.SecretKeyEntry skEntry =
	        new KeyStore.SecretKeyEntry(key);
		store.setEntry(name, skEntry, 
	        new KeyStore.PasswordProtection(password.toCharArray()));

		store.store(new FileOutputStream(location), password.toCharArray());

	}

	/**
	 * constructor - loads a KeyStore from a file
	 * @param keyStoreLocation - key store location
	 * @param keyStorePassword - key store password
	 * @throws Exception
	 */
	public EncryptionHelper(String keyStoreLocation, String keyStorePassword)
			throws Exception {
		
		this.password = keyStorePassword;
		
		if( keyStoreLocation == null || keyStoreLocation.equals(""))
			throw new Warning(Resource.getResourceString("Key_Store_Not_Set"));
		
		File f = new File(keyStoreLocation);
		if( !f.canRead())
		{
			throw new Warning(Resource.getResourceString("No_Key_Store") + keyStoreLocation);
		}
		keyStore = KeyStore.getInstance("JCEKS");
		keyStore.load(new FileInputStream(keyStoreLocation), password
				.toCharArray());
	}

	/**
	 * encrypt a String using a key from the key store
	 * @param clearText - the string to encrypt
	 * @param keyAlias - the encryption key alias
	 * @return the encrypted string
	 * @throws Exception
	 */
	public String encrypt(String clearText, String keyAlias)
			throws Exception {

		/*
		 * get the key and create the Cipher
		 */
		Key key = keyStore.getKey(keyAlias, password.toCharArray());
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);

		/*
		 * encrypt the clear text
		 */
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, enc);
		os.write(clearText.getBytes());
		os.close();

		/*
		 * get the encrypted bytes and encode to a string
		 */
		byte[] ba = baos.toByteArray();
		BASE64Encoder b64enc = new BASE64Encoder();
		return b64enc.encode(ba);

	}

	/**
	 * decrypt a String using a key from the key store
	 * @param cipherText - the string to decrypt
	 * @param keyAlias - the decryption key alias
	 * @return the encrypted string
	 * @throws Exception
	 */
	public String decrypt(String cipherText, String keyAlias)
			throws Exception {

		/*
		 * get the key and create the Cipher
		 */
		Key key = keyStore.getKey(keyAlias, password.toCharArray());
		Cipher dec = Cipher.getInstance("AES");
		dec.init(Cipher.DECRYPT_MODE, key);

		/*
		 * decode the cipher text from base64 back to a byte array
		 */
		BASE64Decoder b64dec = new BASE64Decoder();
		byte[] decba = b64dec.decodeBuffer(cipherText);

		/*
		 * decrpyt the bytes
		 */
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, dec);
		os.write(decba);
		os.close();

		return baos.toString();
	}
	
	/**
	 * Export the borg key in text form
	 * @param keyAlias the key alias
	 * @param keyStorePassword the keystore password
	 * @return the exproted key as a string
	 * @throws Exception
	 */
	public String exportKey(String keyAlias, String keyStorePassword) throws Exception
	{
		Key key = keyStore.getKey(keyAlias, keyStorePassword.toCharArray());
		BASE64Encoder b64enc = new BASE64Encoder();
		return b64enc.encode(key.getEncoded());
	}
	
	/**
	 * Import a provided key into a KeyStore
	 * @param location - the keystore location
	 * @param encodedKey - the encoded key to import
	 * @param keyAlias - the key alias
	 * @param password - the key store password
	 * @throws Exception
	 */
	static public void importKey(String location, String encodedKey, String keyAlias, String password) throws Exception
	{
		KeyStore store = KeyStore.getInstance("JCEKS");
		store.load(new FileInputStream(location), password.toCharArray());
		
		BASE64Decoder b64dec = new BASE64Decoder();
		byte[] ba = b64dec.decodeBuffer(encodedKey);
		SecretKey key = new SecretKeySpec(ba,"AES");
		KeyStore.SecretKeyEntry skEntry =
	        new KeyStore.SecretKeyEntry(key);
		store.setEntry(keyAlias, skEntry, 
	        new KeyStore.PasswordProtection(password.toCharArray()));

		store.store(new FileOutputStream(location), password.toCharArray());

	}

}
