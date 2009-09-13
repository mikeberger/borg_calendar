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

package net.sf.borg.control.socketServer;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.sf.borg.common.XTree;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.BorgOption;
import net.sf.borg.model.entity.Memo;

/**
 * Helps marshal and unmarshal between objects and XML. Only used for palm-sync
 * now.
 */
@SuppressWarnings("unchecked")
@Deprecated
class XmlObjectHelper {
	public static XTree toXml(Object o) {
		return toXml(null, o);
	}

	public static Object fromXml(XTree xml) throws Exception {
		if (xml == null)
			return null;

		String name = xml.name();
		if (name.equals("Null"))
			return null;

		for (int i = 0; i < XML_CLASSES.length; ++i) {
			if (XML_CLASSES[i].getObjectRootName().equals(name))
				return XML_CLASSES[i].toObject(xml);
		}

		throw new IllegalArgumentException(xml.name());
	}

	// private //
	private static final PrimitiveXmlObjectHelper NULL_HELPER = new PrimitiveXmlObjectHelper(
			null, "Null");

	private static final IXmlObjectHelper[] XML_CLASSES = {
			new BorgOptionXmlObjectHelper(), new CollectionXmlObjectHelper(),
			new PrimitiveXmlObjectHelper(String.class, "String"),
			new PrimitiveXmlObjectHelper(Boolean.class, "Boolean"),
			new PrimitiveXmlObjectHelper(Integer.class, "Integer"),
			new RemoteParmsXmlObjectHelper(),
			new ComposedObjectXmlObjectHelper(),
			new BeanXmlObjectHelper(Address.class, "Address"),
			new BeanXmlObjectHelper(Appointment.class, "Appointment"),
			new BeanXmlObjectHelper(Memo.class, "Memo") };

	private static void addPrimitive(XTree xml, String name, String val) {
		xml.appendChild(name, val);
	}

	private static String getStringPrimitive(XTree xml, String name) {
		XTree child = xml.child(name);
		if (child == null)
			throw new IllegalArgumentException(xml.name() + "/" + name);
		return child.value();
	}

	private static XTree createTree(XTree parent, String name) {
		XTree tree = null;
		if (parent == null) {
			tree = new XTree();
			tree.name(name);
		} else {
			tree = parent.appendChild(name);
		}
		return tree;
	}

	private XmlObjectHelper() {
	}

	public static XTree toXml(XTree parent, Object o) {
		IXmlObjectHelper helper = null;
		if (o == null)
			helper = NULL_HELPER;
		else {
			for (int i = 0; i < XML_CLASSES.length; ++i) {
				if (XML_CLASSES[i].getObjectClass().isAssignableFrom(
						o.getClass())) {
					helper = XML_CLASSES[i];
					break;
				}
			}
		}

		if (helper == null)
			throw new IllegalArgumentException();

		XTree tree = createTree(parent, helper.getObjectRootName());
		helper.populate(tree, o);
		return tree;
	}

	private static interface IXmlObjectHelper {
		public Class getObjectClass();

		public String getObjectRootName();

		public void populate(XTree xtree, Object o);

		public Object toObject(XTree xml) throws Exception;
	}

	private static class BorgOptionXmlObjectHelper implements IXmlObjectHelper {
		public final Class getObjectClass() {
			return BorgOption.class;
		}

		public final String getObjectRootName() {
			return "BorgOption";
		}

		public final void populate(XTree xtree, Object o) {
			BorgOption val = (BorgOption) o;
			addPrimitive(xtree, "Key", val.getKey());
			addPrimitive(xtree, "Value", val.getValue());
		}

		public final Object toObject(XTree xml) throws Exception {
			String key = getStringPrimitive(xml, "Key");
			String value = getStringPrimitive(xml, "Value");
			return new BorgOption(key, value);
		}
	}

	// end nested class BorgOptionXmlObjectHelper

	private static class PrimitiveXmlObjectHelper implements IXmlObjectHelper {
		public Class getObjectClass() {
			return cls;
		}

		public final String getObjectRootName() {
			return name;
		}

		public final void populate(XTree xtree, Object o) {
			if (o != null)
				xtree.value(o.toString());
		}

		public Object toObject(XTree xml) throws Exception {
			String val = xml.value();
			Object o = null;
			if (name.equals("String"))
				o = val;
			else if (name.equals("Integer"))
				o = Integer.valueOf(val);
			else if (name.equals("Boolean"))
				o = Boolean.valueOf(val);
			return o;
		}

		// internal //
		PrimitiveXmlObjectHelper(Class cls, String name) {
			this.cls = cls;
			this.name = name;
		}

		// private //
		private Class cls;

		private String name;
	}

	private static class CollectionXmlObjectHelper implements IXmlObjectHelper {
		public Class getObjectClass() {
			return Collection.class;
		}

		public final String getObjectRootName() {
			return "Collection";
		}

		public final void populate(XTree xtree, Object o) {
			Collection col = (Collection) o;
			Iterator itr = col.iterator();
			while (itr.hasNext()) {
				Object oChild = itr.next();
				toXml(xtree, oChild);
			}
		}

		public Object toObject(XTree xml) throws Exception {

			List lst = new ArrayList();
			int numChildren = xml.numChildren();
			for (int i = 1; i <= numChildren; ++i) {
				XTree child = xml.child(i);
				Object oChild = fromXml(child);
				lst.add(oChild);
			}
			return lst;
		}
	}

	private static class RemoteParmsXmlObjectHelper implements IXmlObjectHelper {
		public Class getObjectClass() {
			return IRemoteProxy.Parms.class;
		}

		public final String getObjectRootName() {
			return "RemoteParms";
		}

		public final void populate(XTree xtree, Object o) {
			IRemoteProxy.Parms parms = (IRemoteProxy.Parms) o;
			addPrimitive(xtree, "Class", parms.getClassString());
			addPrimitive(xtree, "Command", parms.getCommand());
			toXml(xtree, parms.getArgs());
		}

		public Object toObject(XTree xml) throws Exception {
			String classString = getStringPrimitive(xml, "Class");
			String command = getStringPrimitive(xml, "Command");
			Object args = fromXml(xml.child(3));
			return new IRemoteProxy.Parms(classString, command, args);
		}
	}

	private static class ComposedObjectXmlObjectHelper implements
			IXmlObjectHelper {
		public Class getObjectClass() {
			return IRemoteProxy.ComposedObject.class;
		}

		public final String getObjectRootName() {
			return "ComposedObject";
		}

		public final void populate(XTree xtree, Object o) {
			IRemoteProxy.ComposedObject co = (IRemoteProxy.ComposedObject) o;
			toXml(xtree, co.getO1());
			toXml(xtree, co.getO2());
		}

		public Object toObject(XTree xml) throws Exception {
			Object o1 = fromXml(xml.child(1));
			Object o2 = fromXml(xml.child(2));
			return new IRemoteProxy.ComposedObject(o1, o2);
		}
	}

	// end nested class ComposedObjectXmlObjectHelper
	// //////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////
	// nested class KeyedBeanXmlObjectHelper

	private static class BeanXmlObjectHelper implements IXmlObjectHelper {
		public Class getObjectClass() {
			return cls;
		}

		public final String getObjectRootName() {
			return objectRootName;
		}

		public final void populate(XTree xtree, Object o) {
			try {
				JAXBContext jc = JAXBContext.newInstance(cls);
				Marshaller m = jc.createMarshaller();
				StringWriter sw = new StringWriter();
				m.marshal(o, sw);
				XTree xml = XTree.readFromBuffer(sw.toString());
				xtree.adopt(xml);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public Object toObject(XTree xml) {

			try {
				JAXBContext jc = JAXBContext.newInstance(cls);
				Unmarshaller u = jc.createUnmarshaller();

				String xmlString = xml.toString();

				return u.unmarshal(new StringReader(xmlString));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		// "package"
		BeanXmlObjectHelper(Class cls, String objectRootName) {
			this.cls = cls;
			this.objectRootName = objectRootName;
		}

		// private //
		private Class cls;

		private String objectRootName;

	}

}
