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

import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.XTree;
import net.sf.borg.model.beans.Address;
import net.sf.borg.model.beans.AddressXMLAdapter;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.BeanDataFactoryFactory;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.IBeanDataFactory;





public class AddressModel extends Model {

    
    private BeanDB db_;           // the database
    public BeanDB getDB(){ return(db_); }
    
    private HashMap bdmap_ = new HashMap();
    
    static private AddressModel self_ = null;
    
    private void load_map() {
        
        // clear map
        bdmap_.clear();
        
        try {
            
            // iterate through tasks using taskmodel
            Collection addrs = getAddresses();
            Iterator ti = addrs.iterator();
            while( ti.hasNext() ) {
                Address addr = (Address) ti.next();
                if( addr.getDeleted() )
                    continue;
                
                // use birthday to build a day key
                Date bd = addr.getBirthday();
                if( bd == null ) continue;
                
                GregorianCalendar g = new GregorianCalendar();
                g.setTime(bd);
                
                int key = AppointmentModel.dkey(g);
                int bdkey = AppointmentModel.birthdayKey(key);
                //System.out.println("key is " + Integer.toString(key) + " " + Integer.toString(bdkey) + " date is " + bd);
                
                // add the task string to the btmap_
                // add the task to the mrs_ Vector. This is used by the todo gui
                Object o = bdmap_.get( new Integer(bdkey) );
                if( o == null ) {
                    o = new LinkedList();
                    bdmap_.put( new Integer(bdkey), o );
                }
                
                LinkedList l = (LinkedList) o;
                l.add( addr );
            }
            
        }
        catch( DBException e ) {
            if( e.getRetCode() != DBException.RET_NOT_FOUND ) {
                Errmsg.errmsg(e);
                return;
            }
        }
        catch( Exception e ) {
            
            Errmsg.errmsg(e);
            return;
        }
        
        
        
    }
    
    public static AddressModel getReference()
    { return( self_ ); }
    public static AddressModel create() {
        self_ = new AddressModel();
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
            return;
        }
        db_ = null;
    }
    
    
    public Collection getAddresses() throws DBException, Exception {
        Collection addrs = db_.readAll();
        Iterator it = addrs.iterator();
        while( it.hasNext())
        {
            Address addr = (Address) it.next();
            if( addr.getDeleted())
                it.remove();
        }
    	return addrs;
    }
    
    public Collection getDeletedAddresses() throws DBException, Exception {
        Collection addrs = db_.readAll();
        Iterator it = addrs.iterator();
        while( it.hasNext())
        {
            Address addr = (Address) it.next();
            if( !addr.getDeleted())
                it.remove();
        }
    	return addrs;
    }
    
    public Collection getAddresses( int daykey ) {
        // don't consider year for birthdays
        int bdkey = AppointmentModel.birthdayKey(daykey);
        //System.out.println("bdkey is " + bdkey);
        return( (Collection) bdmap_.get( new Integer( bdkey ) ));
    }
    
    // open the SMDB database
	public void open_db(String url, String username, boolean shared)
		throws Exception
	{
		
		StringBuffer tmp = new StringBuffer(url);
		IBeanDataFactory factory = BeanDataFactoryFactory
				.getInstance().getFactory(tmp, shared);
		url = tmp.toString();
		// let the factory tweak dbdir
		
		
		db_ =
			factory.create(
				Address.class,
				url,
				username);
		load_map();
	}


    public void delete(Address addr, boolean refresh) throws Exception {
        
        try {
            String sync = Prefs.getPref( PrefName.PALM_SYNC);
            if( sync.equals("true"))
            {
                addr.setDeleted(true);
                db_.updateObj(addr,false);
            }
            else
            {
                db_.delete(addr.getKey());
            }
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
        
        if (refresh)
        	refresh();
    }
    
    public void forceDelete( Address addr ) throws Exception {
        
        try {

               db_.delete(addr.getKey());
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
        
        refresh();
    }
    public void saveAddress( Address addr ) throws Exception
    {
        saveAddress(addr,false);
    }
    
    public void saveAddress(Address addr, boolean sync) throws Exception {
        
        int num = addr.getKey();
        
        if( num == -1 ) {
            int newkey = db_.nextkey();
            addr.setKey(newkey);
            if( !sync)
            {
                addr.setNew(true);
            }
            try
            {  db_.addObj(addr, false); }
            catch( DBException e ) {
                Errmsg.errmsg(e);
            }
        }
        else {
            try {
                if( !sync )
                {
                    addr.setModified(true);
                }
                db_.updateObj(addr, false);
            }
            catch( DBException e ) {
                Errmsg.errmsg(e);
            }
        }
        
        // inform views of data change
        refresh();
    }
    
    // allocate a new Row from the DB
    public Address newAddress() {
        return( (Address) db_.newObj() );
    }
    
    public Address getAddress(int num) throws DBException, Exception {
        return( (Address) db_.readObj( num ) );
    }
    
    public void export(Writer fw) throws Exception {
        
        
        //FileWriter fw = new FileWriter(fname);
        fw.write("<ADDRESSES>\n" );
        AddressXMLAdapter ta = new AddressXMLAdapter();
        
        // export options
        try
        {
            Collection opts = db_.getOptions();
            Iterator opiter = opts.iterator();
            while( opiter.hasNext() )
            {
                BorgOption option = (BorgOption) opiter.next();
                XTree xt =  new XTree();
                xt.name("OPTION");
                xt.appendChild(option.getKey(),option.getValue());
                fw.write( xt.toString() );
            }
        }
        catch( DBException e )
        {
            if( e.getRetCode() != DBException.RET_NOT_FOUND )
                Errmsg.errmsg(e);
        }
        
        // export addresses
        try {
            
            Collection addrs = getAddresses();
            Iterator ti = addrs.iterator();
            while( ti.hasNext() ) {
                Address addr = (Address) ti.next();
                
                XTree xt = ta.toXml( addr  );
                fw.write( xt.toString() );
            }
        }
        catch( DBException e ) {
            if( e.getRetCode() != DBException.RET_NOT_FOUND )
                Errmsg.errmsg(e);
        }
        
        fw.write("</ADDRESSES>" );
        
        
        
    }
    
    public void importXml(XTree xt) throws Exception {
        
        AddressXMLAdapter aa = new AddressXMLAdapter();
        
        for( int i = 1;; i++ ) {
            XTree ch = xt.child(i);
            if( ch == null )
                break;
                      
            if( ch.name().equals("OPTION"))
            {
                XTree opt = ch.child(1);
                if( opt == null ) continue;
                
                db_.setOption(new BorgOption(opt.name(), opt.value()));
            }
            
            if( !ch.name().equals("Address") )
                continue;
            Address addr = (Address) aa.fromXml( ch );
            addr.setKey(-1);
            saveAddress(addr);
        }
        
        refresh();
    }
    
    public void refresh()
    {
        load_map();
        refreshListeners();
    }
    
    public void sync() throws DBException {
        db_.sync();
        refresh();
    }

    public void close_db() throws Exception
    {
    	db_.close();
    }
}
