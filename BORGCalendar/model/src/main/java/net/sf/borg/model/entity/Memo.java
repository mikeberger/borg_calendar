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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import net.sf.borg.common.EncryptionHelper;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;



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
		EncryptionHelper helper = new EncryptionHelper(Prefs.getPref(PrefName.KEYSTORE), password);
		String clearText = helper.decrypt(this.getMemoText(), Prefs.getPref(PrefName.KEYALIAS));
		this.setMemoText(clearText);
		this.setEncrypted(false);
	}

	@Override
	public void encrypt(String password) throws Exception {
		if( isEncrypted() )
			return;
		
		/* encrypt the memo text field */
		EncryptionHelper helper = new EncryptionHelper(Prefs.getPref(PrefName.KEYSTORE), password);
		String cipherText = helper.encrypt(this.getMemoText(), Prefs.getPref(PrefName.KEYALIAS));
		this.setMemoText(cipherText);
		this.setEncrypted(true);
		
	}
	
}
