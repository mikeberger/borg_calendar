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



/**
 * The Address Entity
 */
@XmlRootElement(name="Address")
@XmlAccessorType(XmlAccessType.FIELD)
public class Address extends KeyedEntity<Address>  {

	
	private static final long serialVersionUID = 1996612351860988688L;
	
	/** The First name. */
	private String FirstName;
	
	/**
	 * Gets the first name.
	 * 
	 * @return the first name
	 */
	public String getFirstName() { return( FirstName ); }
	
	/**
	 * Sets the first name.
	 * 
	 * @param xx the new first name
	 */
	public void setFirstName( String xx ){ FirstName = xx; }

	/** The Last name. */
	private String LastName;
	
	/**
	 * Gets the last name.
	 * 
	 * @return the last name
	 */
	public String getLastName() { return( LastName ); }
	
	/**
	 * Sets the last name.
	 * 
	 * @param xx the new last name
	 */
	public void setLastName( String xx ){ LastName = xx; }

	/** The Nickname. */
	private String Nickname;
	
	/**
	 * Gets the nickname.
	 * 
	 * @return the nickname
	 */
	public String getNickname() { return( Nickname ); }
	
	/**
	 * Sets the nickname.
	 * 
	 * @param xx the new nickname
	 */
	public void setNickname( String xx ){ Nickname = xx; }

	/** The Email. */
	private String Email;
	
	/**
	 * Gets the email.
	 * 
	 * @return the email
	 */
	public String getEmail() { return( Email ); }
	
	/**
	 * Sets the email.
	 * 
	 * @param xx the new email
	 */
	public void setEmail( String xx ){ Email = xx; }

	/** The Screen name. */
	private String ScreenName;
	
	/**
	 * Gets the screen name.
	 * 
	 * @return the screen name
	 */
	public String getScreenName() { return( ScreenName ); }
	
	/**
	 * Sets the screen name.
	 * 
	 * @param xx the new screen name
	 */
	public void setScreenName( String xx ){ ScreenName = xx; }

	/** The Work phone. */
	private String WorkPhone;
	
	/**
	 * Gets the work phone.
	 * 
	 * @return the work phone
	 */
	public String getWorkPhone() { return( WorkPhone ); }
	
	/**
	 * Sets the work phone.
	 * 
	 * @param xx the new work phone
	 */
	public void setWorkPhone( String xx ){ WorkPhone = xx; }
	/** The Home phone. */
	
	private String HomePhone;
	/**
	 * Gets the home phone.
	 * 
	 * @return the home phone
	 */
	public String getHomePhone() { return( HomePhone ); }
	
	/**
	 * Sets the home phone.
	 * 
	 * @param xx the new home phone
	 */
	public void setHomePhone( String xx ){ HomePhone = xx; }

	/** The Fax. */
	private String Fax;
	
	/**
	 * Gets the fax.
	 * 
	 * @return the fax
	 */
	public String getFax() { return( Fax ); }
	
	/**
	 * Sets the fax.
	 * 
	 * @param xx the new fax
	 */
	public void setFax( String xx ){ Fax = xx; }

	/** The Pager. */
	private String Pager;
	
	/**
	 * Gets the pager.
	 * 
	 * @return the pager
	 */
	public String getPager() { return( Pager ); }
	
	/**
	 * Sets the pager.
	 * 
	 * @param xx the new pager
	 */
	public void setPager( String xx ){ Pager = xx; }

	/** The Street address. */
	private String StreetAddress;
	
	/**
	 * Gets the street address.
	 * 
	 * @return the street address
	 */
	public String getStreetAddress() { return( StreetAddress ); }
	
	/**
	 * Sets the street address.
	 * 
	 * @param xx the new street address
	 */
	public void setStreetAddress( String xx ){ StreetAddress = xx; }

	/** The City. */
	private String City;
	
	/**
	 * Gets the city.
	 * 
	 * @return the city
	 */
	public String getCity() { return( City ); }
	
	/**
	 * Sets the city.
	 * 
	 * @param xx the new city
	 */
	public void setCity( String xx ){ City = xx; }

	/** The State. */
	private String State;
	
	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public String getState() { return( State ); }
	
	/**
	 * Sets the state.
	 * 
	 * @param xx the new state
	 */
	public void setState( String xx ){ State = xx; }

	/** The Zip. */
	private String Zip;
	
	/**
	 * Gets the zip.
	 * 
	 * @return the zip
	 */
	public String getZip() { return( Zip ); }
	
	/**
	 * Sets the zip.
	 * 
	 * @param xx the new zip
	 */
	public void setZip( String xx ){ Zip = xx; }

	/** The Country. */
	private String Country;
	
	/**
	 * Gets the country.
	 * 
	 * @return the country
	 */
	public String getCountry() { return( Country ); }
	
	/**
	 * Sets the country.
	 * 
	 * @param xx the new country
	 */
	public void setCountry( String xx ){ Country = xx; }

	/** The Company. */
	private String Company;
	
	/**
	 * Gets the company.
	 * 
	 * @return the company
	 */
	public String getCompany() { return( Company ); }
	
	/**
	 * Sets the company.
	 * 
	 * @param xx the new company
	 */
	public void setCompany( String xx ){ Company = xx; }

	/** The Work street address. */
	private String WorkStreetAddress;
	
	/**
	 * Gets the work street address.
	 * 
	 * @return the work street address
	 */
	public String getWorkStreetAddress() { return( WorkStreetAddress ); }
	
	/**
	 * Sets the work street address.
	 * 
	 * @param xx the new work street address
	 */
	public void setWorkStreetAddress( String xx ){ WorkStreetAddress = xx; }

	/** The Work city. */
	private String WorkCity;
	
	/**
	 * Gets the work city.
	 * 
	 * @return the work city
	 */
	public String getWorkCity() { return( WorkCity ); }
	
	/**
	 * Sets the work city.
	 * 
	 * @param xx the new work city
	 */
	public void setWorkCity( String xx ){ WorkCity = xx; }

	/** The Work state. */
	private String WorkState;
	
	/**
	 * Gets the work state.
	 * 
	 * @return the work state
	 */
	public String getWorkState() { return( WorkState ); }
	
	/**
	 * Sets the work state.
	 * 
	 * @param xx the new work state
	 */
	public void setWorkState( String xx ){ WorkState = xx; }

	/** The Work zip. */
	private String WorkZip;
	
	/**
	 * Gets the work zip.
	 * 
	 * @return the work zip
	 */
	public String getWorkZip() { return( WorkZip ); }
	
	/**
	 * Sets the work zip.
	 * 
	 * @param xx the new work zip
	 */
	public void setWorkZip( String xx ){ WorkZip = xx; }

	/** The Work country. */
	private String WorkCountry;
	
	/**
	 * Gets the work country.
	 * 
	 * @return the work country
	 */
	public String getWorkCountry() { return( WorkCountry ); }
	
	/**
	 * Sets the work country.
	 * 
	 * @param xx the new work country
	 */
	public void setWorkCountry( String xx ){ WorkCountry = xx; }

	/** The Web page. */
	private String WebPage;
	
	/**
	 * Gets the web page.
	 * 
	 * @return the web page
	 */
	public String getWebPage() { return( WebPage ); }
	
	/**
	 * Sets the web page.
	 * 
	 * @param xx the new web page
	 */
	public void setWebPage( String xx ){ WebPage = xx; }

	/** The Notes. */
	private String Notes;
	
	/**
	 * Gets the notes.
	 * 
	 * @return the notes
	 */
	public String getNotes() { return( Notes ); }
	
	/**
	 * Sets the notes.
	 * 
	 * @param xx the new notes
	 */
	public void setNotes( String xx ){ Notes = xx; }

	/** The Birthday. */
	private java.util.Date Birthday;
	
	/**
	 * Gets the birthday.
	 * 
	 * @return the birthday
	 */
	public java.util.Date getBirthday() { return( Birthday ); }
	
	/**
	 * Sets the birthday.
	 * 
	 * @param xx the new birthday
	 */
	public void setBirthday( java.util.Date xx ){ Birthday = xx; }


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
