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
 
Copyright 2004 by Mohan Embar - http://www.thisiscool.com/
 */

package net.sf.borg.model.db.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.borg.common.util.XTree;
import net.sf.borg.model.Address;
import net.sf.borg.model.AddressXMLAdapter;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentXMLAdapter;
import net.sf.borg.model.BorgOption;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskXMLAdapter;
import net.sf.borg.model.User;
import net.sf.borg.model.UserXMLAdapter;
import net.sf.borg.model.db.BeanXMLAdapter;
import net.sf.borg.model.db.KeyedBean;

/**
 * Helps marshal and unmarshal between objects and XML.
 */
public class XmlObjectHelper
{
	public static XTree toXml(Object o) throws Exception
	{
	    return toXml(null, o);
	}

	public static Object fromXml(XTree xml) throws Exception
	{
	    String name = xml.name();
		Object result = null;
		for (int i=0; i<XML_CLASSES.length; ++i)
		{
			if (XML_CLASSES[i].getObjectRootName().equals(name))
				return XML_CLASSES[i].fromXml(xml);
		}
		throw new IllegalArgumentException(xml.name());
	}

	// private //
	private static final IXmlObjectHelper[] XML_CLASSES =
	{
	    new BorgOptionXmlObjectHelper(),
	    new CollectionXmlObjectHelper(),
	    new RemoteParmsXmlObjectHelper(),
	    new ComposedObjectXmlObjectHelper(),
	    new KeyedBeanXmlObjectHelper
	    (
	        Address.class,
	        "Address",
	        new AddressXMLAdapter()
	    ),
	    new KeyedBeanXmlObjectHelper
	    (
	        Appointment.class,
	        "Appointment",
	        new AppointmentXMLAdapter()
	    ),
	    new KeyedBeanXmlObjectHelper
	    (
	        Task.class,
	        "Task",
	        new TaskXMLAdapter()
	    ),
	    new KeyedBeanXmlObjectHelper
	    (
	        User.class,
	        "User",
	        new UserXMLAdapter()
	    ),
	};
	
	private static void addPrimitive(XTree xml, String name, String val)
	{
	    xml.appendChild(name, val);
	}
	
	private static void addPrimitive(XTree xml, String name, int val)
	{
	    addPrimitive(xml, name, Integer.toString(val));
	}
	
	private static void addPrimitive(XTree xml, String name, boolean val)
	{
	    addPrimitive(xml, name, Boolean.toString(val));
	}
	
	private static String getStringPrimitive(XTree xml, String name)
	{
	    XTree child = xml.child(name);
	    if (child == null)
	        throw new IllegalArgumentException(xml.name()+"/"+name);
	    return child.value();
	}
	
	private static int getIntPrimitive(XTree xml, String name)
	{
	    String val = getStringPrimitive(xml,name);
	    return Integer.parseInt(val);
	}
	
	private static boolean getBooleanPrimitive(XTree xml, String name)
	{
	    String val = getStringPrimitive(xml,name);
	    return Boolean.valueOf(val).booleanValue();
	}
	
	private static XTree createTree(XTree parent, String name)
	{
	    XTree tree = null;
	    if (parent == null)
	    {
	        tree = new XTree();
	        tree.name(name);
	    }
	    else
	    {
	        tree = parent.appendChild(name);
	    }
	    return tree;
	}
	
	private XmlObjectHelper()
	{}
	
	public static XTree toXml(XTree parent, Object o)
	{
	    for (int i=0; i<XML_CLASSES.length; ++i)
		{
			if (XML_CLASSES[i].getObjectClass().isAssignableFrom(o.getClass()))
			{
			    IXmlObjectHelper iXmlObjectHelper = XML_CLASSES[i]; 
			    XTree tree = createTree(parent, iXmlObjectHelper.getObjectRootName());
			    iXmlObjectHelper.populate(tree, o);
			    return tree;
			}
		}
		throw new IllegalArgumentException(o.getClass().getName());
	}

	////////////////////////////////////////////////////////////
	// nested interface IXmlObjectHelper
	
	private static interface IXmlObjectHelper
	{
		public Class getObjectClass();
		public String getObjectRootName();
        public void populate(XTree xtree, Object o);
		public Object fromXml(XTree xml);
	}

	// end nested class IXmlObjectHelper
	////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////
	// nested class BorgOptionXmlObjectHelper
	
	private static class BorgOptionXmlObjectHelper
		implements IXmlObjectHelper
	{
		public final Class getObjectClass()
        {
            return BorgOption.class;
        }
		
		public final String getObjectRootName()
		{
		    return "BorgOption";
		}

        public final void populate(XTree xtree, Object o)
        {
            BorgOption val = (BorgOption) o;
            addPrimitive(xtree, "Key", val.getKey());
            addPrimitive(xtree, "Value", val.getValue());
        }

        public final Object fromXml(XTree xml)
        {
            String key = getStringPrimitive(xml, "Key"); 
			String value = getStringPrimitive(xml, "Value");
			return new BorgOption(key,value);
		}
	}

	// end nested class BorgOptionXmlObjectHelper
	////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////
	// nested class CollectionXmlObjectHelper
	
	private static class CollectionXmlObjectHelper
		implements IXmlObjectHelper
	{
		public Class getObjectClass()
        {
            return Boolean.class;
        }

		public final String getObjectRootName()
		{
		    return "Collection";
		}

        public final void populate(XTree xtree, Object o)
        {
            Collection col = (Collection) o;
            Iterator itr = col.iterator();
            while (itr.hasNext())
            {
                Object oChild = itr.next();
                toXml(xtree, oChild);
            }
        }

        public Object fromXml(XTree xml)
        {
            // FUTURE: With the current XTree implementation,
            // this operation requires O(n^2) time to execute, where
            // n is the number of children.
            List lst = new ArrayList();
            int numChildren = xml.numChildren();
            for (int i=0; i<numChildren; ++i)
            {
                XTree child = xml.child(i);
                Object oChild = fromXml(child);
                lst.add(oChild);
            }
            return lst;
		}
	}

	// end nested class CollectionXmlObjectHelper
	////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////
	// nested class RemoteParmsXmlObjectHelper
	
	private static class RemoteParmsXmlObjectHelper
		implements IXmlObjectHelper
	{
		public Class getObjectClass()
        {
            return IRemoteProxy.Parms.class;
        }

		public final String getObjectRootName()
		{
		    return "RemoteParms";
		}

        public final void populate(XTree xtree, Object o)
        {
            IRemoteProxy.Parms parms = (IRemoteProxy.Parms) o;
            addPrimitive(xtree, "Class", parms.getClassString());
            addPrimitive(xtree, "Command", parms.getCommand());
            toXml(xtree, parms.getArgs());
        }

        public Object fromXml(XTree xml)
        {
            String classString = getStringPrimitive(xml, "Class");
            String command = getStringPrimitive(xml, "Command");
            Object args = fromXml(xml.child(2));
            return new IRemoteProxy.Parms(classString, command, args);
		}
	}

	// end nested class RemoteParmsXmlObjectHelper
	////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////
	// nested class ComposedObjectXmlObjectHelper
	
	private static class ComposedObjectXmlObjectHelper
		implements IXmlObjectHelper
	{
		public Class getObjectClass()
        {
            return IRemoteProxy.ComposedObject.class;
        }

		public final String getObjectRootName()
		{
		    return "ComposedObject";
		}

        public final void populate(XTree xtree, Object o)
        {
            IRemoteProxy.ComposedObject co = (IRemoteProxy.ComposedObject) o;
            toXml(xtree, co.getO1());
            toXml(xtree, co.getO2());
        }

        public Object fromXml(XTree xml)
        {
            Object o1 = fromXml(xml.child(0));
            Object o2 = fromXml(xml.child(1));
            return new IRemoteProxy.ComposedObject(o1,o2);
		}
	}

	// end nested class ComposedObjectXmlObjectHelper
	////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////
	// nested class KeyedBeanXmlObjectHelper
	
	private static class KeyedBeanXmlObjectHelper
		implements IXmlObjectHelper
	{
		public Class getObjectClass()
        {
            return cls;
        }

		public final String getObjectRootName()
		{
		    return objectRootName;
		}

		public final void populate(XTree xtree, Object o)
        {
            XTree xml = xmlAdapter.toXml((KeyedBean) o);
            xtree.adopt(xml);
        }

        public Object fromXml(XTree xml)
        {
            KeyedBean keyedBean = xmlAdapter.fromXml(xml);
            return keyedBean;
		}
        
        // "package"
        KeyedBeanXmlObjectHelper
        (
            Class cls,
            String objectRootName,
            BeanXMLAdapter xmlAdapter
        )
        {
            this.cls = cls;
            this.objectRootName = objectRootName;
            this.xmlAdapter = xmlAdapter;
        }
        
        // private //
        private Class cls;
        private String objectRootName;
        private BeanXMLAdapter xmlAdapter;
	}

	// end nested class KeyedBeanXmlObjectHelper
	////////////////////////////////////////////////////////////
}
