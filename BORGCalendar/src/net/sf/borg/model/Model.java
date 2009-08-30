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
import java.util.ArrayList;



/**
 * base class for data models. A Model provides access to a data store to the outside world.
 * Clients of the model can register as Listeners to be provided with feedback any time the model changes.
 */
public abstract class Model
{

    // list of clients to notify when the model changes
    private ArrayList<Listener> listeners;

	/**
	 * Listener for a Model.
	 * 
	 */
	public interface Listener
	{
		
		/**
		 * Called to notify Listener when the Model is changed.
		 */
		public abstract void refresh();
		
	}
    
    /**
     * Instantiates a new model.
     */
    public Model()
    {
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
    * Removes a listener.
    * 
    * @param listener the listener
    */
   public void removeListener(Listener listener) 
    {
        listeners.remove(listener);
    }
    
    /**
     * send a Refresh message to all listeners.
     */
    protected void refreshListeners()
    {
        for( int i = 0; i < listeners.size(); i++ )
        {
            Listener v = listeners.get(i);
            v.refresh();
        }
    }
    
    /**
     * notify all listeners that the model is being destroyed
     */
    protected void removeListeners()
    {
        listeners.clear();     
    }
    
    /**
     * Removes the listeners.
     */
    public void remove(){
    	removeListeners();
    }
    
}
