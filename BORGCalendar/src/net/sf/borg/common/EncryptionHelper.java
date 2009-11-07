package net.sf.borg.common;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * class containing encryption and decryption methods for borg
 * 
 */
public class EncryptionHelper {

	/* the cached keystore object */
	private KeyStore keyStore;

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
		keyStore = KeyStore.getInstance("JCEKS");
		keyStore.load(new FileInputStream(keyStoreLocation), keyStorePassword
				.toCharArray());
	}

	/**
	 * encrypt a String using a key from the key store
	 * @param clearText - the string to encrypt
	 * @param keyAlias - the encryption key alias
	 * @param password - the keystore password
	 * @return the encrypted string
	 * @throws Exception
	 */
	public String encrypt(String clearText, String keyAlias, String password)
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
	 * @param clearText - the string to decrypt
	 * @param keyAlias - the decryption key alias
	 * @param password - the keystore password
	 * @return the encrypted string
	 * @throws Exception
	 */
	public String decrypt(String cipherText, String keyAlias, String password)
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

}
