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
 
Copyright 2003 by ==Quiet==
 */
package net.sf.borg.model;

import java.util.Collection;
import java.util.Iterator;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Version;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.BeanDataFactoryFactory;
import net.sf.borg.model.db.DBException;

public class UserModel extends Model {
    static {
        Version.addVersion("$Id$");
    }
    
    private BeanDB db_;           // the database
    
    static private UserModel self_ = null;
    
    public static UserModel getReference()
    { return( self_ ); }
    public static UserModel create() {
        self_ = new UserModel();
        return(self_);
    }
    
    public void remove() {
        removeListeners();
        try {
        	if( db_ != null )
        			db_.close();
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
            System.exit(0);
        }
        db_ = null;
    }
    
    
    public Collection getUsers() throws DBException, Exception {
    	return db_.readAll();
    }
    
    public User getUser( String username ) throws DBException, Exception
    {
        Collection users = db_.readAll();
        Iterator it = users.iterator();
        while( it.hasNext())
        {
            User u = (User) it.next();
            if( u.getUserName().equals(username))
            {
                return(u);
            }
        }
        
        return null;
    }
    
    public int validate(String user, String pass) throws Exception
    {
        User u = getUser(user);

        if( u != null && pass.equals(u.getPassword()))
        {
            return( u.getUserId().intValue());
        }
        
        throw new Exception( "Could not validate user/password");
    }
    
    // open the SMDB database
	public void open_db(String factoryClassName, String url, int userid)
		throws Exception
	{
		db_ =
			BeanDataFactoryFactory.getInstance().getFactory(factoryClassName).create(
				User.class,
				url,
				userid);
	}
    
    public void delete( int num ) throws Exception {
        
        try {
            db_.delete(num);
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
        
        refreshListeners();
        
    }
    
    public void saveUser(User u) throws Exception {
        
        int num = u.getKey();
        
        if( num == -1 ) {
            int newkey = db_.nextkey();
            u.setKey(newkey);
            try
            {  db_.addObj(u, false); }
            catch( DBException e ) {
                Errmsg.errmsg(e);
            }
        }
        else {
            try {
                db_.updateObj(u, false);
            }
            catch( DBException e ) {
                Errmsg.errmsg(e);
            }
        }
        
        // inform views of data change
        refreshListeners();
        
    }
    
    // allocate a new Row from the DB
    public User newUser() {
        return( (User) db_.newObj() );
    }
    
    public User getUser(int num) throws DBException, Exception {
        return( (User) db_.readObj( num ) );
    }
    
    
    public void sync() throws DBException {
        db_.sync();
        refreshListeners();
    }

    public void close_db() throws Exception
    {
    	db_.close();
    }
}
