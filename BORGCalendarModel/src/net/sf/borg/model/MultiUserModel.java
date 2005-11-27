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
 
Copyright 2003 by Mike Berger
 */
package net.sf.borg.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.model.db.BeanDataFactoryFactory;

/*
 * This class manages the list of public calendars being viewed
 * The list of all possible public calendars is just the list of all user names in
 * the appointment table on the remote database whose calendars are set to be public - 
 * minus the current user's calendar
 */
public class MultiUserModel extends Model {
    
    static private MultiUserModel self_ = new MultiUserModel();
    
    public static MultiUserModel getReference()
    { return( self_ ); }
    
    // users in a multi-user DB
    private Collection users_ = new ArrayList();
    
    // categories being displayed
    private Collection shownUsers_ = new ArrayList();
    
    public void setShownUsers(Collection users)
    {        
        shownUsers_ = users;
        refreshListeners();
    }
    
    private String ourUserName_;
    public void setOurUserName(String user)
    {
    	ourUserName_ = user;
    }
    public String getOurUserName()
    {
    	return ourUserName_;
    }
    
    public void syncWithDB( ) throws Exception
    {
    	users_.clear();
    	Collection dbusers = AppointmentModel.getReference().getAllUsers();

    	// remove calendars that are not public
    	Iterator it = dbusers.iterator();
    	while( it.hasNext())
    	{
    		String user = (String) it.next();
    		//System.out.println(user);
        	// don't consider our own user name to be one that we can browse - our data is always
        	// shown
    		if( user.equals(ourUserName_))
    			continue;
    		
    		try{
    			AppointmentModel am = AppointmentModel.getReference(user);
				if (am == null) {
					String dbdir = BeanDataFactoryFactory.buildDbDir();
					am = AppointmentModel.create(user);
					am.open_db(dbdir, user, true, false);
				}
    			//System.out.println(user + " " + am.isPublic());
    			if( am.isPublic())
    			{
    				users_.add(user);
    			}
    		
    		}
    		catch( Exception e )
    		{
    			Errmsg.errmsg(e);
    		}
    	}
    	
    	// delete any entries from shown users if they are no longer in the 
    	// users table
    	it = shownUsers_.iterator();
    	while( it.hasNext())
    	{
    		String user = (String) it.next();
    		if( !users_.contains(user))
    		{
    			shownUsers_.remove(user);
    		}
    	}
    }
    
    
    public Collection getUsers() throws Exception
    {
        return users_;        
    }
    
    public boolean isShown(String user )
    {
        if( shownUsers_.contains(user))
            return true;
        return false;
    }
    
    public Collection getShownUsers()
    {
    	ArrayList users = new ArrayList();
    	users.addAll(shownUsers_);
        return( users );
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.Model#remove()
     */
    public void remove() {
        
    }
    
}
