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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.XTree;

// object to hold the task type and state information
// uses an XTree to hold the data
public class TaskTypes {

    private Vector task_types_; // a list of the currently known task types
    private XTree state_model_; // an XML tree containing the rules for the task state transitions
    // per task type and status
    
    public static final String NOCBVALUE = "---------------";
    public static final String INITIAL_STATE = "__INIT__";
    public static final String CHECKBOX = "CB";
    
    public TaskTypes() {
        state_model_ = null;
        task_types_ = null;
    }
    
    public TaskTypes copy() throws Exception
    {
    	String s = toString();
    	TaskTypes o = new TaskTypes();
    	o.fromString(s);
    	return o;
    }
    
    public void fromString(String xml) throws Exception
    {
        XTree newtree = XTree.readFromBuffer(xml);
        
        if( newtree != null ) {
            // if XML is valid - replace the current model
            state_model_ = newtree;
            task_types_ = null;
        }
        
    }
    
    public void loadDefault() throws Exception
    {
    	URL tsurl = getClass().getResource("/resource/task_states.xml");
		state_model_ = XTree.readFromURL(tsurl);
		task_types_ = null;
    }
    
    public String toString()
    {
    	return( state_model_.toString());
    }
    
    // return a list of all states for a given type
    public Vector getStates( String type ) {
        
        Vector v = new Vector();
        
        // find the task type element under the XML root
        XTree tp = state_model_.child(type);
        if( !tp.exists()) {
            Errmsg.notice(Resource.getResourceString("WARNING!_Could_not_find_task_type_") + type + Resource.getResourceString("state3") );
            return v;
        }
        
        // add names of all children of the current type node
        for( int i = 1;; i++ ) {
            XTree ch = tp.child(i);
            if( ch == null )
                break;
            if( ch.name().equals(CHECKBOX) )
                continue;
            v.add( ch.name() );
        }
        
        
        return( v );
    }
    

	// return the list of current known task types
	public Vector getTaskTypes() {
	    // if the list was never retrieved yet - then get it from the
	    // children of the root of the XML task type/state model
	    if( task_types_ == null ) {
	        task_types_ = new Vector();
	        for( int n = 1;;n++ ) {
	            XTree ty = state_model_.child(n);
	            if( ty == null )
	                break;
	            task_types_.add(ty.name());
	        }
	        
	    }
	    
	    return( task_types_ );
	}
	
	// add a new type with default states
	public void addType(String type)
	{
		if( getTaskTypes().contains(type))
			return;
		
		XTree n = state_model_.appendChild(type);
		XTree o = n.appendChild("OPEN");
		o.appendChild("CLOSED");
		n.appendChild("CLOSED");
		task_types_ = null;
	}
	
	// change the name of a type
	public void changeType(String type, String newtype)
	{
	    XTree tp = state_model_.child(type);
	    if( tp.exists())
	    	tp.name(newtype);
	    task_types_ = null;
	}
	
	// delete a type
	public void deleteType(String type)
	{
	    XTree tp = state_model_.child(type);
	    if( tp.exists())
	    	tp.remove();
	    task_types_ = null;
	}
	
	// add a state to a given type
	public void addState(String type, String state)
	{
	    XTree tp = state_model_.child(type);
	    if( !tp.exists()) return;
	    XTree ch = tp.child(state);
	    if( ch.exists()) return;
	    tp.appendChild(state);	    
	}
	
	// change a state name and adjust any matching next states
	public void changeState(String type, String state, String newstate)
	{
	    XTree tp = state_model_.child(type);
	    if( !tp.exists()) return;
	    XTree ch = tp.child(state);
	    if( !ch.exists()) return;
	    ch.name(newstate);
	    
        for( int n = 1;;n++ ) {
            ch = tp.child(n);
            if( ch == null )
                break;
            XTree ns = ch.child(state);
            if( ns.exists())
            	ns.name(newstate);
        }
	}
	
	// delete a state and any matching next states
	public void deleteState(String type, String state)
	{
		XTree tp = state_model_.child(type);
		if( !tp.exists()) return;
		XTree ch = tp.child(state);
		if( !ch.exists()) return;
		ch.remove();
		
		for( int n = 1;;n++ ) {
			ch = tp.child(n);
			if( ch == null )
				break;
			XTree ns = ch.child(state);
			if( ns.exists())
				ns.remove();
		}
	}
	
	// add a new "next state" to a given state
	public void addNextState(String type, String state, String nextstate)
	{
	    XTree tp = state_model_.child(type);
	    if( !tp.exists()) return;
	    XTree ch = tp.child(state);
	    if( !ch.exists()) return;
	    XTree ns = ch.child(nextstate);
	    if( ns.exists()) return;
	    ch.appendChild(nextstate);
	}
	
	// delete a next state
	public void deleteNextState(String type, String state, String nextstate)
	{
	    XTree tp = state_model_.child(type);
	    if( !tp.exists()) return;
	    XTree ch = tp.child(state);
	    if( !ch.exists()) return;
	    XTree ns = ch.child(nextstate);
	    if( ns.exists()) ns.remove();
	}
	
	// change a checkbox (subtask) value for a given index or add a new one if the index does not
	// correspond to a checkbox
	public void changeCB( String type, int index, String value )
	{
		if( value == null )
			value = NOCBVALUE;
	    XTree tp = state_model_.child(type);
	    if( !tp.exists()) {
	        Errmsg.notice(Resource.getResourceString("WARNING!_Could_not_find_task_type_") + type + Resource.getResourceString("checkbox_2") );
	    }
	    
	    XTree cb = tp.child(CHECKBOX,index+1);
	    if( cb.exists() )
	    {
	    	cb.value(value);
	    	return;
	    }
	    
	    tp.appendChild(CHECKBOX,value);
	    
	}
	
	// get the list of checkbox strings for a given type
	// from the state model
	public String[] checkBoxes( String type ) {
	    
	    String ar[] = new String[5];
	    
	    // find the task type element under the XML root
	    XTree tp = state_model_.child(type);
	    if( !tp.exists()) {
	        Errmsg.notice(Resource.getResourceString("WARNING!_Could_not_find_task_type_") + type + Resource.getResourceString("checkbox_2") );
	    }
	    
	    for( int i = 0; i < 5; i++ ) {
	        
	        if( tp.child(CHECKBOX, i+1 ).exists() ) {
	            ar[i] = tp.child(CHECKBOX, i+1 ).value();
	        }
	        else {
	            ar[i] = NOCBVALUE;
	        }
	    }
	    
	    return ar;
	}
	
	// write out the current XML task type/state model to a file as XML
	public void exportStates(OutputStream ostr) throws Exception {
	    Writer fw = new OutputStreamWriter(ostr, "UTF8");
	    String sm = state_model_.toString();
	    fw.write(sm);
	    fw.close();
	}
	
	// read a task type/state model from an XML file
	public void importStates(InputStream istr) throws Exception {
	    // read XML from a file
	    XTree newtree = XTree.readFromStream(istr);
	    
	    if( newtree != null ) {
	        // if XML is valid - replace the current model
	        state_model_ = newtree;
	        task_types_ = null;
	    }
	    
	    Errmsg.notice(Resource.getResourceString("model_updated") );
	}
	
	// compute a vector of possible next states given the current task type and state
	public Vector nextStates( String state, String type ) {
	    
	    Vector v = new Vector();
	    
	    // can always stay at the current state
	    v.add( state );
	    
	    // find the task type element under the XML root
	    XTree tp = state_model_.child(type);
	    if( !tp.exists()) {
	        Errmsg.notice(Resource.getResourceString("WARNING!_Could_not_find_task_type_") + type + Resource.getResourceString("state3") );
	        return v;
	    }
	    
	    // add any ALL states - these are states that any other state can jump to
	    // find ALL child element of the task element
	    XTree all = tp.child("ALL");
	    
	    // add all children of ALL as next states
	    for( int i = 1;; i++ ) {
	        XTree ch = all.child(i);
	        if( ch == null )
	            break;
	        if( ch.name().equals(state) )
	            continue;
	        v.add( ch.name() );
	    }
	    
	    
	    // add state specific arcs
	    // find the child of the task type element with name = the current state
	    XTree st = tp.child(state);
	    
	    // add names of all children of the current state node as possible next states
	    for( int i = 1;; i++ ) {
	        XTree ch = st.child(i);
	        if( ch == null )
	            break;
	        if( ch.name().equals(state) )
	            continue;
	        if( ch.name().equals(INITIAL_STATE))
	        	continue;
	        v.add( ch.name() );
	    }
	    
	    
	    return( v );
	}
	
	public void validate() throws Exception
	{
		Collection types = getTaskTypes();
		Iterator it = types.iterator();
		while( it.hasNext() )
		{
			String type = (String) it.next();
			Collection states = getStates(type);
			if( !states.contains("OPEN"))
				throw new Exception( Resource.getPlainResourceString("NoOpenState") + type);
		}
	}
	
	public void setInitialState(String type, String state)
	{
	    // find the task type element under the XML root
	    XTree tp = state_model_.child(type);
	    if( !tp.exists()) {
	        return;
	    }
	    
	    
	    for( int i = 1;; i++ ) {
	        XTree st = tp.child(i);
	        if( st == null )
	            break;
	        if( st.name().equals(state) )
	        {
	        	if( !st.child(INITIAL_STATE).exists())
	        		st.appendChild(INITIAL_STATE);
	        }
	        else if( st.child(INITIAL_STATE).exists())
	        {
	        	st.child(INITIAL_STATE).remove();
	        }	        
	    }
	    
	}
	
	public String getInitialState(String type)
	{
		 // find the task type element under the XML root
	    XTree tp = state_model_.child(type);
	    if( !tp.exists()) {
	        return "OPEN";
	    }
	        
	    for( int i = 1;; i++ ) {
	        XTree st = tp.child(i);
	        if( st == null )
	            break;
	        
	        if( st.child(INITIAL_STATE).exists())
	        	return st.name();
	               
	    }
	    
	    return("OPEN");
	}
	
}
