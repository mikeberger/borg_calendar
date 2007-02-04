/*
This file is part of BORG.
 
    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
 
    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
Copyright 2004 by ==Quiet==
 */
package net.sf.borgweb.form;


import java.text.DateFormat;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
 

// TODO - this should be generated code
public class AddressForm extends ActionForm {

	private String key_;
	public String getKey() {
		return key_;
	}
	public void setKey(String key_) {
		this.key_ = key_;
	}
	
	private String FirstName_;
	public String getFirstName() { return( FirstName_ ); }
	public void setFirstName( String xx ){ FirstName_ = xx; }

	private String LastName_;
	public String getLastName() { return( LastName_ ); }
	public void setLastName( String xx ){ LastName_ = xx; }

	private String Nickname_;
	public String getNickname() { return( Nickname_ ); }
	public void setNickname( String xx ){ Nickname_ = xx; }

	private String Email_;
	public String getEmail() { return( Email_ ); }
	public void setEmail( String xx ){ Email_ = xx; }

	private String ScreenName_;
	public String getScreenName() { return( ScreenName_ ); }
	public void setScreenName( String xx ){ ScreenName_ = xx; }

	private String WorkPhone_;
	public String getWorkPhone() { return( WorkPhone_ ); }
	public void setWorkPhone( String xx ){ WorkPhone_ = xx; }

	private String HomePhone_;
	public String getHomePhone() { return( HomePhone_ ); }
	public void setHomePhone( String xx ){ HomePhone_ = xx; }

	private String Fax_;
	public String getFax() { return( Fax_ ); }
	public void setFax( String xx ){ Fax_ = xx; }

	private String Pager_;
	public String getPager() { return( Pager_ ); }
	public void setPager( String xx ){ Pager_ = xx; }

	private String StreetAddress_;
	public String getStreetAddress() { return( StreetAddress_ ); }
	public void setStreetAddress( String xx ){ StreetAddress_ = xx; }

	private String City_;
	public String getCity() { return( City_ ); }
	public void setCity( String xx ){ City_ = xx; }

	private String State_;
	public String getState() { return( State_ ); }
	public void setState( String xx ){ State_ = xx; }

	private String Zip_;
	public String getZip() { return( Zip_ ); }
	public void setZip( String xx ){ Zip_ = xx; }

	private String Country_;
	public String getCountry() { return( Country_ ); }
	public void setCountry( String xx ){ Country_ = xx; }

	private String Company_;
	public String getCompany() { return( Company_ ); }
	public void setCompany( String xx ){ Company_ = xx; }

	private String WorkStreetAddress_;
	public String getWorkStreetAddress() { return( WorkStreetAddress_ ); }
	public void setWorkStreetAddress( String xx ){ WorkStreetAddress_ = xx; }

	private String WorkCity_;
	public String getWorkCity() { return( WorkCity_ ); }
	public void setWorkCity( String xx ){ WorkCity_ = xx; }

	private String WorkState_;
	public String getWorkState() { return( WorkState_ ); }
	public void setWorkState( String xx ){ WorkState_ = xx; }

	private String WorkZip_;
	public String getWorkZip() { return( WorkZip_ ); }
	public void setWorkZip( String xx ){ WorkZip_ = xx; }

	private String WorkCountry_;
	public String getWorkCountry() { return( WorkCountry_ ); }
	public void setWorkCountry( String xx ){ WorkCountry_ = xx; }

	private String WebPage_;
	public String getWebPage() { return( WebPage_ ); }
	public void setWebPage( String xx ){ WebPage_ = xx; }

	private String Notes_;
	public String getNotes() { return( Notes_ ); }
	public void setNotes( String xx ){ Notes_ = xx; }

	private String Birthday_;
	public String getBirthday() { return( Birthday_ ); }
	public void setBirthday( String xx ){ Birthday_ = xx; }

	
	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request)
	{
		//String save = request.getParameter("save");
		//f( save == null ){
			//return(null);
		//}
		
		ActionErrors errors = null;
		if( getFirstName() == null || getFirstName().equals("") ||
				getLastName() == null || getLastName().equals("") )
		{
			errors = new ActionErrors();
	        errors.add( "First Name:",
	          new ActionMessage( "errors.Name" ) );
		}
		
		String bd = getBirthday();
		if( bd != null && !bd.equals("") )
		{
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            try
            {
                df.parse(bd);
            }
            catch( Exception e )
            {
            	if( errors == null )
            		errors = new ActionErrors();
            	
            	errors.add( "Birthday:", new ActionMessage("errors.birthday"));
                
            }
            
        }
		
		return(errors);
	}

	

};
