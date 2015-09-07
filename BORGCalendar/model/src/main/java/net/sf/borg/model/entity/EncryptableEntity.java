
package net.sf.borg.model.entity;

/**
 * Interface for entities that can have encrypted fields. It is up to
 * the entity specific decrypt and encrypt methods to determine which fields are
 * to be encrypted
 *
 */

public interface EncryptableEntity {
	
	
	/**
	 * decrypt the entity. This will use the password to get the borg encryption
	 * key from the keystore and then will decrypt those fields in the entity that are encrypted.
	 * the entity encrypted flag will be set to false.
	 * @param password the keystore password
	 * @throws Exception
	 */
	public abstract void decrypt(String password) throws Exception;
	
	/**
	 * encrypt the entity. This will use the password to get the borg encryption
	 * key from the keystore and then will encrypt those fields in the entity that are encryptable.
	 * the entity encrypted flag will be set to true.
	 * @param password the keystore password
	 * @throws Exception
	 */
	public abstract void encrypt(String password) throws Exception;


}
