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
 * View.java
 *
 * Created on May 23, 2003, 11:06 AM
 */

package net.sf.borg.ui;

import net.sf.borg.common.util.Version;
import net.sf.borg.model.Model;



/**
 * $Id$
 * @author  MBERGER
 */

// Views show data from a model and respond to refresh
// cand destroy allbacks from Models
abstract class View extends javax.swing.JFrame implements Model.Listener
{
    static
    {
        Version.addVersion("$Id$");
    }
    
    
    public abstract void refresh();
    public abstract void destroy();
    
    public void remove()
    {
        destroy();
    }
    
    public View()
    {
    }
      
    // function to call to register a view with the model
    protected void addModel(Model m)
    {
        m.addListener(this);
    }
     
    
}
