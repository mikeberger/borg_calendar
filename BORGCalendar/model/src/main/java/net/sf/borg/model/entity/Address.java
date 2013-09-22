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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;



/**
 * The Address Entity
 */
@XmlRootElement(name="Address")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@EqualsAndHashCode(callSuper=true)
public class Address extends KeyedEntity<Address>  {

	
	private static final long serialVersionUID = 1996612351860988688L;
	
	private String FirstName;
	private String LastName;
	private String Nickname;
	private String Email;
	private String ScreenName;
	private String WorkPhone;
	private String HomePhone;
	private String CellPhone;
	private String Fax;
	private String Pager;
	private String StreetAddress;
	private String City;
	private String State;
	private String Zip;
	private String Country;
	private String Company;
	private String WorkStreetAddress;
	private String WorkCity;
	private String WorkState;
	private String WorkZip;
	private String WorkCountry;
	private String WebPage;
	private String Notes;
	private java.util.Date Birthday;

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	@Override
	protected Address clone() {
		Address dst = new Address();
		dst.setKey( getKey());
		dst.setFirstName( getFirstName() );
		dst.setLastName( getLastName() );
		dst.setNickname( getNickname() );
		dst.setEmail( getEmail() );
		dst.setScreenName( getScreenName() );
		dst.setWorkPhone( getWorkPhone() );
		dst.setHomePhone( getHomePhone() );
		dst.setCellPhone( getCellPhone()  );
		dst.setFax( getFax() );
		dst.setPager( getPager() );
		dst.setStreetAddress( getStreetAddress() );
		dst.setCity( getCity() );
		dst.setState( getState() );
		dst.setZip( getZip() );
		dst.setCountry( getCountry() );
		dst.setCompany( getCompany() );
		dst.setWorkStreetAddress( getWorkStreetAddress() );
		dst.setWorkCity( getWorkCity() );
		dst.setWorkState( getWorkState() );
		dst.setWorkZip( getWorkZip() );
		dst.setWorkCountry( getWorkCountry() );
		dst.setWebPage( getWebPage() );
		dst.setNotes( getNotes() );
		dst.setBirthday( getBirthday() );
		return(dst);
	}
	
}
