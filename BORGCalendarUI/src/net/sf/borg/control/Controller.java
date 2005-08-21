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
 * View.java
 *
 * Created on May 23, 2003, 11:06 AM
 */

package net.sf.borg.control;

import java.util.ArrayList;


import net.sf.borg.model.Model;


/**
 * $Id$
 * @author  MBERGER
 */

/*
 * Controller.java
 *
 * Created on December 30, 2003, 1:32 PM
 */

abstract public class Controller
{
 
    private ArrayList listeners;
    
    public Controller()
    {
        listeners = new ArrayList();
    }
    
    protected void register(Model m)
    {
    	listeners.add(m);
    }
    
    protected void removeListeners()
    {
        for( int i = 0; i < listeners.size(); i++ )
        {
            Model m = (Model) listeners.get(i);
            m.remove();
        }
        
        listeners = new ArrayList();
        
    }    
    
}
