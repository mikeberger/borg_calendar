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

/*
 * Model.java
 *
 * Created on May 23, 2003, 11:33 AM
 */

package net.sf.borg.model;
import java.util.ArrayList;

import net.sf.borg.common.util.Version;

/**
 *
 * @author  MBERGER
 */

// a model is in charge of the data store and presents the data to the
// rest of the app
// each model allows Views to register with it for callbacks when the
// data changes
public abstract class Model
{
    static
    {
        Version.addVersion("$Id$");
    }
    
    // list of views to notify when the model changes
    private ArrayList listeners;

	/**
	 * @author mbb
	 *
	 * To change the template for this generated type comment go to
	 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
	 */
	public interface Listener
	{
		public abstract void refresh();
		public abstract void remove();
	}
    
    public Model()
    {
        listeners = new ArrayList();
    }
    
    // function to call to register a view with the model
    public void register(Listener listener)
    {
        listeners.add(listener);
    }
    
   // function to call to runegister a view from the model
    public void unregister(Listener listener)
    {
        listeners.remove(listener);
    }
    
    // send a refresh to all registered views
    protected void refreshListeners()
    {
        for( int i = 0; i < listeners.size(); i++ )
        {
            Listener v = (Listener) listeners.get(i);
            v.refresh();
        }
    }
    
    // notify all views that the model is being destroyed
    protected void removeListeners()
    {
        for( int i = 0; i < listeners.size(); i++ )
        {
            Listener v = (Listener) listeners.get(i);
            v.remove();
        }
        
        listeners = new ArrayList();
        
    }
    
    public abstract void remove();
    
}
