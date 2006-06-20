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
import java.util.TreeSet;

import net.sf.borg.common.util.Resource;
import net.sf.borg.model.db.DBException;


public class CategoryModel extends Model {
    
    static private CategoryModel self_ = new CategoryModel();
    
    static public final String UNCATEGORIZED = Resource.getResourceString("uncategorized");
    
    public static CategoryModel getReference()
    { return( self_ ); }
    
    // categories in DB
    private Collection categories_ = new TreeSet();
    
    // categories being displayed
    private Collection shownCategories_ = new TreeSet();
    
    public void setShownCategories(Collection cats)
    {        
        shownCategories_ = cats;
        refreshListeners();
    }
    
    public void syncCategories() throws Exception, DBException
    {
        categories_.clear();
        categories_.addAll( AppointmentModel.getReference().getDbCategories());
        categories_.addAll( TaskModel.getReference().getCategories());
    }
    
    public Collection getCategories() throws Exception
    {
    	ArrayList cats = new ArrayList();
    	cats.addAll(categories_);
        return( cats );        
    }
    
    public void addCategory(String cat)
    {
        categories_.add(cat);
    }
    
    public void showCategory(String cat)
    {
        shownCategories_.add(cat);
    }
    
    public void addAll(Collection cats)
    {
        categories_.addAll(cats);
        shownCategories_.addAll(cats);
    }
    
    public boolean isShown(String cat )
    {
        if( shownCategories_.contains(cat))
            return true;
        return false;
    }
    
    public Collection getShownCategories()
    {
    	ArrayList cats = new ArrayList();
    	cats.addAll(shownCategories_);
        return( cats );
    }
    
    public void showAll()
    {
    	shownCategories_.clear();
    	shownCategories_.addAll(categories_);
    	refreshListeners();
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.Model#remove()
     */
    public void remove() {
        
    }
    
}
