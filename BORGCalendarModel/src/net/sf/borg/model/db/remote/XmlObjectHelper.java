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

import net.sf.borg.common.util.XTree;
import net.sf.borg.model.BorgOption;

/**
 * Helps marshal and unmarshal between objects and XML.
 */
public class XmlObjectHelper
{
	public static XTree toXml(Object o) throws Exception
	{
		/*
	    return toXml(null, o);
	    for (int i=0; i<XML_CLASSES.length; ++i)
		{
			if (o.getClass() == XML_CLASSES[i].getObjectClass())
			{
			    
			}
				return XML_CLASSES[i].toXml(null, o);
		}
		*/
		throw new IllegalArgumentException(o.getClass().getName());
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
	
	};
	
	private XmlObjectHelper()
	{}
	
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
        }

        public Object fromXml(XTree xml)
        {
			String value = xml.value();
			return Boolean.valueOf(value);
		}
	}

	// end nested class CollectionXmlObjectHelper
	////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////
	// nested class ComposedObjectXmlObjectHelper
	
	private static class ComposedObjectXmlObjectHelper
		implements IXmlObjectHelper
	{
		public Class getObjectClass()
        {
            return Boolean.class;
        }

		public final String getObjectRootName()
		{
		    return "ComposedObject";
		}

        public final void populate(XTree xtree, Object o)
        {
        }

        public Object fromXml(XTree xml)
        {
			String value = xml.value();
			return Boolean.valueOf(value);
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
            return Boolean.class;
        }

		public final String getObjectRootName()
		{
		    return "KeyedBean";
		}

        public final void populate(XTree xtree, Object o)
        {
        }

        public Object fromXml(XTree xml)
        {
			String value = xml.value();
			return Boolean.valueOf(value);
		}
	}

	// end nested class KeyedBeanXmlObjectHelper
	////////////////////////////////////////////////////////////
}
