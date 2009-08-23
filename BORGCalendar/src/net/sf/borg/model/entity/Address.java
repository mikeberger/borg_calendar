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



/**
 * The Address Entity
 */
public class Address extends KeyedEntity<Address> implements java.io.Serializable {

	
	private static final long serialVersionUID = 1996612351860988688L;
	
	/** The First name_. */
	private String FirstName_;
	
	/**
	 * Gets the first name.
	 * 
	 * @return the first name
	 */
	public String getFirstName() { return( FirstName_ ); }
	
	/**
	 * Sets the first name.
	 * 
	 * @param xx the new first name
	 */
	public void setFirstName( String xx ){ FirstName_ = xx; }

	/** The Last name_. */
	private String LastName_;
	
	/**
	 * Gets the last name.
	 * 
	 * @return the last name
	 */
	public String getLastName() { return( LastName_ ); }
	
	/**
	 * Sets the last name.
	 * 
	 * @param xx the new last name
	 */
	public void setLastName( String xx ){ LastName_ = xx; }

	/** The Nickname_. */
	private String Nickname_;
	
	/**
	 * Gets the nickname.
	 * 
	 * @return the nickname
	 */
	public String getNickname() { return( Nickname_ ); }
	
	/**
	 * Sets the nickname.
	 * 
	 * @param xx the new nickname
	 */
	public void setNickname( String xx ){ Nickname_ = xx; }

	/** The Email_. */
	private String Email_;
	
	/**
	 * Gets the email.
	 * 
	 * @return the email
	 */
	public String getEmail() { return( Email_ ); }
	
	/**
	 * Sets the email.
	 * 
	 * @param xx the new email
	 */
	public void setEmail( String xx ){ Email_ = xx; }

	/** The Screen name_. */
	private String ScreenName_;
	
	/**
	 * Gets the screen name.
	 * 
	 * @return the screen name
	 */
	public String getScreenName() { return( ScreenName_ ); }
	
	/**
	 * Sets the screen name.
	 * 
	 * @param xx the new screen name
	 */
	public void setScreenName( String xx ){ ScreenName_ = xx; }

	/** The Work phone_. */
	private String WorkPhone_;
	
	/**
	 * Gets the work phone.
	 * 
	 * @return the work phone
	 */
	public String getWorkPhone() { return( WorkPhone_ ); }
	
	/**
	 * Sets the work phone.
	 * 
	 * @param xx the new work phone
	 */
	public void setWorkPhone( String xx ){ WorkPhone_ = xx; }

	/** The Home phone_. */
	private String HomePhone_;
	
	/**
	 * Gets the home phone.
	 * 
	 * @return the home phone
	 */
	public String getHomePhone() { return( HomePhone_ ); }
	
	/**
	 * Sets the home phone.
	 * 
	 * @param xx the new home phone
	 */
	public void setHomePhone( String xx ){ HomePhone_ = xx; }

	/** The Fax_. */
	private String Fax_;
	
	/**
	 * Gets the fax.
	 * 
	 * @return the fax
	 */
	public String getFax() { return( Fax_ ); }
	
	/**
	 * Sets the fax.
	 * 
	 * @param xx the new fax
	 */
	public void setFax( String xx ){ Fax_ = xx; }

	/** The Pager_. */
	private String Pager_;
	
	/**
	 * Gets the pager.
	 * 
	 * @return the pager
	 */
	public String getPager() { return( Pager_ ); }
	
	/**
	 * Sets the pager.
	 * 
	 * @param xx the new pager
	 */
	public void setPager( String xx ){ Pager_ = xx; }

	/** The Street address_. */
	private String StreetAddress_;
	
	/**
	 * Gets the street address.
	 * 
	 * @return the street address
	 */
	public String getStreetAddress() { return( StreetAddress_ ); }
	
	/**
	 * Sets the street address.
	 * 
	 * @param xx the new street address
	 */
	public void setStreetAddress( String xx ){ StreetAddress_ = xx; }

	/** The City_. */
	private String City_;
	
	/**
	 * Gets the city.
	 * 
	 * @return the city
	 */
	public String getCity() { return( City_ ); }
	
	/**
	 * Sets the city.
	 * 
	 * @param xx the new city
	 */
	public void setCity( String xx ){ City_ = xx; }

	/** The State_. */
	private String State_;
	
	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public String getState() { return( State_ ); }
	
	/**
	 * Sets the state.
	 * 
	 * @param xx the new state
	 */
	public void setState( String xx ){ State_ = xx; }

	/** The Zip_. */
	private String Zip_;
	
	/**
	 * Gets the zip.
	 * 
	 * @return the zip
	 */
	public String getZip() { return( Zip_ ); }
	
	/**
	 * Sets the zip.
	 * 
	 * @param xx the new zip
	 */
	public void setZip( String xx ){ Zip_ = xx; }

	/** The Country_. */
	private String Country_;
	
	/**
	 * Gets the country.
	 * 
	 * @return the country
	 */
	public String getCountry() { return( Country_ ); }
	
	/**
	 * Sets the country.
	 * 
	 * @param xx the new country
	 */
	public void setCountry( String xx ){ Country_ = xx; }

	/** The Company_. */
	private String Company_;
	
	/**
	 * Gets the company.
	 * 
	 * @return the company
	 */
	public String getCompany() { return( Company_ ); }
	
	/**
	 * Sets the company.
	 * 
	 * @param xx the new company
	 */
	public void setCompany( String xx ){ Company_ = xx; }

	/** The Work street address_. */
	private String WorkStreetAddress_;
	
	/**
	 * Gets the work street address.
	 * 
	 * @return the work street address
	 */
	public String getWorkStreetAddress() { return( WorkStreetAddress_ ); }
	
	/**
	 * Sets the work street address.
	 * 
	 * @param xx the new work street address
	 */
	public void setWorkStreetAddress( String xx ){ WorkStreetAddress_ = xx; }

	/** The Work city_. */
	private String WorkCity_;
	
	/**
	 * Gets the work city.
	 * 
	 * @return the work city
	 */
	public String getWorkCity() { return( WorkCity_ ); }
	
	/**
	 * Sets the work city.
	 * 
	 * @param xx the new work city
	 */
	public void setWorkCity( String xx ){ WorkCity_ = xx; }

	/** The Work state_. */
	private String WorkState_;
	
	/**
	 * Gets the work state.
	 * 
	 * @return the work state
	 */
	public String getWorkState() { return( WorkState_ ); }
	
	/**
	 * Sets the work state.
	 * 
	 * @param xx the new work state
	 */
	public void setWorkState( String xx ){ WorkState_ = xx; }

	/** The Work zip_. */
	private String WorkZip_;
	
	/**
	 * Gets the work zip.
	 * 
	 * @return the work zip
	 */
	public String getWorkZip() { return( WorkZip_ ); }
	
	/**
	 * Sets the work zip.
	 * 
	 * @param xx the new work zip
	 */
	public void setWorkZip( String xx ){ WorkZip_ = xx; }

	/** The Work country_. */
	private String WorkCountry_;
	
	/**
	 * Gets the work country.
	 * 
	 * @return the work country
	 */
	public String getWorkCountry() { return( WorkCountry_ ); }
	
	/**
	 * Sets the work country.
	 * 
	 * @param xx the new work country
	 */
	public void setWorkCountry( String xx ){ WorkCountry_ = xx; }

	/** The Web page_. */
	private String WebPage_;
	
	/**
	 * Gets the web page.
	 * 
	 * @return the web page
	 */
	public String getWebPage() { return( WebPage_ ); }
	
	/**
	 * Sets the web page.
	 * 
	 * @param xx the new web page
	 */
	public void setWebPage( String xx ){ WebPage_ = xx; }

	/** The Notes_. */
	private String Notes_;
	
	/**
	 * Gets the notes.
	 * 
	 * @return the notes
	 */
	public String getNotes() { return( Notes_ ); }
	
	/**
	 * Sets the notes.
	 * 
	 * @param xx the new notes
	 */
	public void setNotes( String xx ){ Notes_ = xx; }

	/** The Birthday_. */
	private java.util.Date Birthday_;
	
	/**
	 * Gets the birthday.
	 * 
	 * @return the birthday
	 */
	public java.util.Date getBirthday() { return( Birthday_ ); }
	
	/**
	 * Sets the birthday.
	 * 
	 * @param xx the new birthday
	 */
	public void setBirthday( java.util.Date xx ){ Birthday_ = xx; }

	// palm sync stuff
	@Deprecated private boolean New_;
	@Deprecated public boolean getNew() { return( New_ ); }
	@Deprecated public void setNew( boolean xx ){ New_ = xx; }
	@Deprecated private boolean Modified_;
	@Deprecated public boolean getModified() { return( Modified_ ); }
	@Deprecated public void setModified( boolean xx ){ Modified_ = xx; }
	@Deprecated private boolean Deleted_;
	@Deprecated public boolean getDeleted() { return( Deleted_ ); }
	@Deprecated public void setDeleted( boolean xx ){ Deleted_ = xx; }

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
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
		dst.setNew( getNew() );
		dst.setModified( getModified() );
		dst.setDeleted( getDeleted() );
		return(dst);
	}
}
