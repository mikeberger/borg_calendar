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

/*
 * Model.java
 *
 * Created on May 23, 2003, 11:33 AM
 */

package net.sf.borg.model;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Data;
import net.sf.borg.model.Model.ChangeEvent.ChangeAction;



/**
 * base class for data models. A Model provides access to a data store to the outside world.
 * Clients of the model can register as Listeners to be provided with feedback any time the model changes.
 */
public abstract class Model
{
	static private final Logger log = Logger.getLogger("net.sf.borg");

    /**
	 * The Class ChangeEvent.
	 */
	@Data
	public static class ChangeEvent {

		/**
		 * Enum to hold actions that can happen to an object
		 */
		public static enum ChangeAction {
			ADD, CHANGE, DELETE;
		}

		private ChangeAction action;

		private final Object object;
		
		private Model model;

		/**
		 * Instantiates a new change event.
		 *
		 * @param object the changed object
		 * @param action the action
		 */
		public ChangeEvent(Object object, ChangeAction action)
		{
			this.object = object;
			this.action = action;
		}		
		
	}
	
	/**
	 * list of all instatiated models
	 */
	private static Set<Model> modelList = new HashSet<Model>();
	
	/**
	 * get a list of all instantiated models
	 * @return list of models
	 */
	public static Set<Model> getExistingModels()
	{
		return modelList;
	}
	
	/**
	 * sync all models
	 */
	public static void syncModels()
	{
		for(Model m : modelList)
			m.sync();
	}
	
	/**
	 * sync the model with the underlying DB for DBs that can be changed without going through Borg (i.e. MySQL)
	 */
	protected void sync() {
		// do nothing by default
	}

	/**
	 * Listener for a Model.
	 * 
	 */
	public interface Listener
	{
		
		/**
		 * Called to notify Listener when the Model is changed.
		 */
		public abstract void update(ChangeEvent event);
		
	}
	
	// list of clients to notify when the model changes
    private ArrayList<Listener> listeners;
    
    /**
     * Instantiates a new model.
     */
    public Model()
    {
    	modelList.add(this);
        listeners = new ArrayList<Listener>();
    }
    
    /**
     * Adds a listener.
     * 
     * @param listener the listener
     */
    public void addListener(Listener listener)
    {
        listeners.add(listener);
    }
    
    /**
     * send an update message to all listeners with no change event
     */
    protected void refreshListeners()
    {
    	ChangeEvent ev = new ChangeEvent(null,ChangeAction.CHANGE);
    	ev.model = this;
    	refreshListeners(ev);
    }
    
    /**
     * send an update message to all listeners with a change event.
     */
    protected void refreshListeners(ChangeEvent event)
    {
  
        if( log.isLoggable(Level.FINE))
        	log.fine("Sending ChangeEvent[" + event.toString());

    	event.model = this;
        for( int i = 0; i < listeners.size(); i++ )
        {
            Listener v = listeners.get(i);
            v.update(event);
            if( log.isLoggable(Level.FINE))
            	log.fine("...To Listener: " + v.toString());
        }
    }
    
    /**
     * Removes the listeners.
     */
    public void remove(){
    	removeListeners();
    }
    
    /**
    * Removes a listener.
    * 
    * @param listener the listener
    */
   public void removeListener(Listener listener) 
    {
        listeners.remove(listener);
    }
    
    /**
     * notify all listeners that the model is being destroyed
     */
    protected void removeListeners()
    {
        listeners.clear();     
    }
    
    /**
     * Export the models data to XML
     * @param fw - writer to write the XML to
     * @throws Exception
     */
	public abstract void export(Writer fw) throws Exception;
	
	/**
	 * Import model data from XML
	 * @param is input stream containing XML
	 * @throws Exception
	 */
	public abstract void importXml(InputStream is) throws Exception;

	/**
	 * get the root XML element name for this model's XML representation 
	 * @return the XML root element name
	 */
	public abstract String getExportName();

	/**
	 * return user readable information about the model
	 * @return user readable information String
	 */
	public abstract String getInfo() throws Exception;
    
}
