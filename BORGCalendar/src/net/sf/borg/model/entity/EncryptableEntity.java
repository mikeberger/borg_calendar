
package net.sf.borg.model.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;


/**
 * Abstract base class for entities that can have encrypted fields. It is up to
 * the entity specific decrypt and encrypt methods to determine which fields are
 * to be encrypted
 *
 * @param <T> the entity class
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class EncryptableEntity<T> extends KeyedEntity<T> {
	
	private static final long serialVersionUID = 1L;
	/** encryption flag  - indicates if an entity instance is encrypted. It is stored in
	 * the database */
	@XmlElement
	private boolean encrypted = false;
	
	
	/**
	 * @return true if the entity is encrypted
	 */
	public boolean isEncrypted() {
		return encrypted;
	}

	/**
	 * set the encrypted flag
	 * @param encrypted the encrypted to set
	 */
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	
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
