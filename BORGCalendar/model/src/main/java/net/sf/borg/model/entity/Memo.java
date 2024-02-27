/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.model.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import net.sf.borg.common.EncryptionHelper;



/**
 * Memo Entity. A Memo is a simple text entry keyed by a memo name. It remains simple
 * since it corresponds to the simple memo objects that can be synced to a palm pilot.
 */
@XmlRootElement(name="Memo")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Memo implements EncryptableEntity, Serializable {

	
	private static final long serialVersionUID = -6793670294661709573L;
	
	private String MemoName;
	private String MemoText;
	private Date Created;
	private Date Updated;
	private boolean encrypted = false;

	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	public Memo copy() {
		Memo dst = new Memo();
		dst.setMemoName( getMemoName() );
		dst.setMemoText( getMemoText() );
		dst.setEncrypted(isEncrypted());
		dst.setCreated(getCreated());
		dst.setUpdated(getUpdated());
		return(dst);
	}

	@Override
	public void decrypt(String password) throws Exception {
		if( !isEncrypted() )
			return;
		
		/* decrypt the memo text field */
		EncryptionHelper helper = new EncryptionHelper( password);
		String clearText = helper.decrypt(this.getMemoText());
		this.setMemoText(clearText);
		this.setEncrypted(false);
	}

	@Override
	public void encrypt(String password) throws Exception {
		if( isEncrypted() )
			return;
		
		/* encrypt the memo text field */
		EncryptionHelper helper = new EncryptionHelper( password);
		String cipherText = helper.encrypt(this.getMemoText());
		this.setMemoText(cipherText);
		this.setEncrypted(true);
		
	}
	
}
