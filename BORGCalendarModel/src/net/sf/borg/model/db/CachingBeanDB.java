/*
This file is part of BORG.
 
	BORG is free software; you can redistribute it and/or modify
	it under the terms of the GNU General public final synchronized License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.
 
	BORG is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General public final synchronized License for more details.
 
	You should have received a copy of the GNU General public final synchronized License
	along with BORG; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
Copyright 2003 by Mike Berger
 */

package net.sf.borg.model.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.borg.model.BorgOption;

public class CachingBeanDB implements BeanDB
{
	public CachingBeanDB(BeanDB delegate)
	{
	    this.delegate = delegate;
	}
	
	// BeanDB overrides
    public final synchronized Collection readAll() throws DBException, Exception {
        return new ArrayList(getObjectMap().values());
    }

	public final synchronized KeyedBean readObj(int key) throws DBException, Exception
	{
		KeyedBean bean = (KeyedBean) getObjectMap().get(new Integer(key));
		return bean==null ? bean : bean.copy();
	}

	public final synchronized KeyedBean newObj()
	{
		try
		{
			return delegate.newObj();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getClass().getName()+": "+e.getMessage());
		}
	}

	public synchronized void addObj(KeyedBean bean, boolean crypt) throws DBException, Exception
	{
		delegate.addObj(bean,crypt);
		getObjectMap().put(new Integer(bean.getKey()), bean);
	}

	public synchronized void updateObj(KeyedBean bean, boolean crypt) throws DBException, Exception
	{
		delegate.updateObj(bean,crypt);
		getObjectMap().put(new Integer(bean.getKey()), bean);
	}

	public synchronized void delete(int key) throws Exception
	{
		getObjectMap().remove(new Integer(key));
		delegate.delete(key);
	}

    public final synchronized Collection getOptions() throws Exception {
		List lst = new ArrayList();
		Iterator itr = getOptionsMap().entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			lst.add(
				new BorgOption(
					(String) entry.getKey(),
					(String) entry.getValue()));
		}
		return lst;
    }

    public final synchronized String getOption(String oname) throws Exception
	{
		return (String) getOptionsMap().get(oname);
	}

    public final void setOption(BorgOption option) throws Exception {
		setOption(option.getKey(), option.getValue());        
    }

	public final synchronized void setOption(String oname, String value) throws Exception
	{
		delegate.setOption(new BorgOption(oname,value));
		getOptionsMap().put(oname, value);
	}

	public final synchronized Collection getOptionKeys() throws Exception
	{
		return getOptionsMap().keySet();
	}

	public final synchronized Collection getKeys() throws Exception
	{
		return getObjectMap().keySet();
	}

	public final synchronized void close() throws Exception
	{
		delegate.close();
	}

	public final synchronized int nextkey() throws Exception
	{
		return delegate.nextkey();
	}

    public final synchronized boolean isDirty() throws DBException, Exception
    {
    	if (data == null) return true;
    		// need to rebuild the world
    	
    	// HACK: We would like to ask our delegate BeanDB if s/he's dirty.
    	// To avoid hammering the server though, we pretend we already know
    	// the answer if we've performed the query less than a second ago
    	// and the response was negative.
    	long lCurTime = System.currentTimeMillis();
    	if (lastNegativeDirtyQueryTimestamp != Long.MIN_VALUE
				&& lCurTime <= (lastNegativeDirtyQueryTimestamp + 1000L))
    		return false;
    	
    	boolean isDelegateDirty = delegate.isDirty();
    	if (!isDelegateDirty)
    		lastNegativeDirtyQueryTimestamp = lCurTime;
    	
    	return isDelegateDirty;
    }
    
	public final synchronized void sync() throws DBException
	{
		delegate.sync();
	}
	
	// protected //
	protected boolean refresh() throws Exception
	{
		if (!isDirty())
			return false;
		
//		System.out.println("Rebuilding the world....");
		
		data = new CachingDBCache();

		// Repopulate our maps.
		Map map = data.getObjectMap();
		Collection col = delegate.readAll();
		for (Iterator itr = col.iterator(); itr.hasNext(); )
		{
			KeyedBean bean = (KeyedBean) itr.next();
			map.put(new Integer(bean.getKey()), bean);
		}
		
		map = data.getOptionsMap();
		col = delegate.getOptions();
		for (Iterator itr = col.iterator(); itr.hasNext(); )
		{
			BorgOption option = (BorgOption) itr.next();
			map.put(option.getKey(), option.getValue());
		}
		
		return true;
	}
	
	protected final Map getObjectMap() throws Exception
	{
		refresh();
		return data.getObjectMap();
	}
	
	protected final Map getOptionsMap() throws Exception
	{
		refresh();
		return data.getOptionsMap();
	}

	// private //
	protected BeanDB delegate;
	private CachingDBCache data;
	private long lastNegativeDirtyQueryTimestamp = Long.MIN_VALUE;
}
