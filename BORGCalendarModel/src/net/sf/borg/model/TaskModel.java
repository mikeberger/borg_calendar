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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.borg.common.util.Errmsg;

import net.sf.borg.common.util.XTree;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.IBeanDataFactory;







// taksmodel is the data model class for task data. calmodel is the only class that communicates
// directly with the SMDB database class for tasks.
// However, taskmodel does allow the rest of the app to see Row objects. These are generic
// database objects used to contain a map of field names to data. They represent
// a row of the database. It would be overkill to build a formal task class to hold the
// task data. C++ versions of BORG did this and wasted a lot of effort mapping things
// into and out of the task class.

// taskmodel sets the schema for SMDB. See open_db for this schema. This schema defines the fields
// that are in each task Row.

// records are keyed in SMDB using an integer key. taskmodel will use the task number as the key. the
// number starts at 1 and just increases with each new task.

// unlike the calmodel, which needs to keep a map of appointments in local cache for various
// functions, taskmodel does not keep a local map. the caching done by the SMDB database is sufficient
// for taskmodel's needs
public class TaskModel extends Model implements Model.Listener {

    
    private BeanDB db_;           // the database

    // map of tasks keyed by day - for performance
    private HashMap btmap_;
    private Vector allmap_;
    private TaskTypes taskTypes_ = new TaskTypes();
    
    public LinkedList get_tasks( int daykey ) {
        return( (LinkedList) btmap_.get( new Integer( daykey ) ));
    }
    
    public Vector get_tasks() {
        return( allmap_ );
    }
    
    private TaskModel() {
        btmap_ = new HashMap();
        allmap_ = new Vector();
    }
    
    static private TaskModel self_ = null;
    static public TaskModel getReference()
    { return( self_ ); }
    public static TaskModel create() {
        self_ = new TaskModel();
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
    
    public TaskTypes getTaskTypes()
    {
    	return( taskTypes_);
    }
    
    public void saveTaskTypes() throws Exception
    {
        db_.setOption(new BorgOption("SMODEL", taskTypes_.toString()));
    }
    
    public Collection getCategories() throws Exception, DBException {
        
            TreeSet categories = new TreeSet();
            Iterator itr = db_.readAll().iterator();
            while( itr.hasNext() ) {
                Task t = (Task) itr.next();
                String cat = t.getCategory();
                if( cat != null && !cat.equals("") )
                    categories.add( cat );
            }
                
        return( categories );
        
    }
    
    public Collection getTasks() throws DBException, Exception {
    	return db_.readAll();
    }
    
    // load a map of tasks to day keys for performance. this is because the month views will
    // repeatedly need to retrieve tasks per day and it would be slow to repeatedly traverse
    // the entire task DB looking for them
    // only tasks that are viewed by day need to be cached here - those that are not CLOSED and
    // have a due date - a small fraction of the total
    private void load_map() {
        
        // clear map
        btmap_.clear();
        allmap_.clear();
        
        try {
            
            // iterate through tasks using taskmodel
            Collection tasks = getTasks();
            Iterator ti = tasks.iterator();
            while( ti.hasNext() ) {
                Task mr = (Task) ti.next();
                
                // for each task, get state and skip CLOSED or PR tasks
                String status = mr.getState();
                if( status.equals("CLOSED") || status.equals("PR"))
                    continue;
                
                String cat = mr.getCategory();
                if( cat == null || cat.equals(""))
                    cat = CategoryModel.UNCATEGORIZED;
                
                if( !CategoryModel.getReference().isShown(cat))
                    continue;

                
                // use task due date to build a day key
                Date due = mr.getDueDate();
                if( due == null ) continue;
                
                GregorianCalendar g = new GregorianCalendar();
                g.setTime(due);
                int key = AppointmentModel.dkey( g.get(Calendar.YEAR), g.get(Calendar.MONTH), g.get(Calendar.DATE));
                
                
                // add the task string to the btmap_
                // add the task to the mrs_ Vector. This is used by the todo gui
                Object o = btmap_.get( new Integer(key) );
                if( o == null ) {
                    o = new LinkedList();
                    btmap_.put( new Integer(key), o );
                }
                
                LinkedList l = (LinkedList) o;
                l.add( mr );
                allmap_.add(mr);
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
    /*
    // open using a JDBC database
    public void open_db(Connection conn) throws Exception {
		db_ =
			BeanDataFactoryFactory.getInstance().getFactory("jdbc:").create(
				Task.class,
				conn);

        // get XML that models states/transitions
        // set to default if it does not exist
        // see SMDB.java for the use of SMDB options
        String sm = db_.getOption("SMODEL");
        if( sm == null ) {
            try {
                // load XML from a file in the JAR
            	taskTypes_.loadDefault();
                sm = taskTypes_.toString();
                db_.setOption(new BorgOption("SMODEL", sm ));
            }
            catch( Exception e ) {
                Errmsg.errmsg(e);
                System.exit(1);
            }
        }
        else {
        	taskTypes_.fromString(sm);
        }
        
        
        CategoryModel.getReference().addAll(getCategories());
        CategoryModel.getReference().addListener(this);
        
        // init the task type list to null
        load_map();
        
    }
    */
    // open the SMDB database
    public void open_db(IBeanDataFactory factory, String url, String username)
			throws Exception {
		db_ =
			factory.create(
				Task.class,
				url,
				username);
        
        // get XML that models states/transitions
        // set to default if it does not exist
        // see SMDB.java for the use of SMDB options
        String sm = db_.getOption("SMODEL");
        if( sm == null ) {
            try {
                // load XML from a file in the JAR
            	taskTypes_.loadDefault();
                sm = taskTypes_.toString();
                db_.setOption(new BorgOption("SMODEL", sm ));
            }
            catch( Exception e ) {
                Errmsg.errmsg(e);
                System.exit(1);
            }
        }
        else {
            taskTypes_.fromString(sm);
        }
        
        CategoryModel.getReference().addAll(getCategories());
        CategoryModel.getReference().addListener(this);
        
        load_map();
        
    }
    
    // delete a given task from the DB
    public void delete( int tasknum ) throws Exception {
        
        try {
            db_.delete(tasknum);
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
        
        
        // have the borg class reload the calendar app side of things - so this is one model
        // notifying the other of a change through the controller
        load_map();
        
        refreshListeners();
        
    }
    
    // save a task from a filled in task Row
    public void savetask(Task task) throws Exception {
        
        // add task to DB
        Integer num = task.getTaskNumber();
        
        // if the task number is -1, it is a new task so
        // get a new task number.
        if( num.intValue() == -1 ) {
            int newkey = db_.nextkey();
            task.setKey(newkey);
            task.setTaskNumber(new Integer(newkey));
            try
            {  db_.addObj(task, false); }
            catch( DBException e ) {
                Errmsg.errmsg(e);
            }
        }
        else {
            // task exists - so update existing task in DB
            try {
                int key = task.getTaskNumber().intValue();
                task.setKey(key);
                db_.updateObj(task, false);
            }
            catch( DBException e ) {
                Errmsg.errmsg(e);
            }
        }
        
        
        // have the borg class reload the calendar app side of things - so this is one model
        // notifying the other of a change through the controller
        load_map();
        
        // inform views of data change
        refreshListeners();
        
    }
    
    // allocate a new Row from the DB
    public Task newMR() {
        return( (Task) db_.newObj() );
    }
    
    // read a task from the DB by task number
    public Task getMR(int num) throws DBException, Exception {
        return( (Task) db_.readObj( num ) );
    }
    
    // force a task to be updated to CLOSED state and saved
    public void close(int num) throws Exception {
        Task task = getMR(num);
        task.setState("CLOSED");
        savetask(task);
    }
    
    // export the task data for all tasks to XML
    public void export(Writer fw) throws Exception {
        
    
        //FileWriter fw = new FileWriter(fname);
        fw.write("<TASKS>\n" );
        TaskXMLAdapter ta = new TaskXMLAdapter();
        
        // export options
        try {
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
        catch( DBException e ) {
            if( e.getRetCode() != DBException.RET_NOT_FOUND )
                Errmsg.errmsg(e);
        }
        
        // export tasks
        try {
            
            Collection tasks = getTasks();
            Iterator ti = tasks.iterator();
            while( ti.hasNext() ) {
                Task task = (Task) ti.next();
                
                XTree xt = ta.toXml( task  );
                fw.write( xt.toString() );
            }
        }
        catch( DBException e ) {
            if( e.getRetCode() != DBException.RET_NOT_FOUND )
                Errmsg.errmsg(e);
        }
        
        fw.write("</TASKS>" );
        
        
    }
    
    // export the task data to a file in XML
    public void importXml(XTree xt) throws Exception {
        
 
        TaskXMLAdapter aa = new TaskXMLAdapter();
        
        // for each appt - create an Appointment and store
        for( int i = 1;; i++ ) {
            XTree ch = xt.child(i);
            if( ch == null )
                break;
            
            if( ch.name().equals("OPTION")) {
                XTree opt = ch.child(1);
                if( opt == null ) continue;
                
                if( opt.name().equals("SMODEL")) {
                	taskTypes_.fromString(opt.value());
                    db_.setOption(new BorgOption("SMODEL", taskTypes_.toString()));
                    
                }
                else
                {
                    db_.setOption(new BorgOption(opt.name(), opt.value()));
                }
            }
            
            if( !ch.name().equals("Task") )
                continue;
            Task task = (Task) aa.fromXml( ch );
            try{
                db_.addObj(task, false);
            }
            catch( DBException e)
            {
                if( e.getRetCode() == DBException.RET_DUPLICATE )
                {
                    task.setTaskNumber(new Integer(-1));
                    savetask(task);
                }
                else
                {
                    throw e;
                }
            }
        }
        
        
        // refresh all views that are displaying appt data from this model
        load_map();
        refreshListeners();
              
    }
    
    public void sync() throws DBException {
        db_.sync();
        load_map();
        refreshListeners();
    }
    
    public void close_db() throws Exception
    {
    	db_.close();
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.Model.Listener#refresh()
     */
    public void refresh() {
        try{
            load_map();
            refreshListeners();
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
        
    }
}
