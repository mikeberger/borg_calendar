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

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The Class TaskTypes manages the Task type and state information, including
 * the state transitions allowed for each task type.
 */
public class TaskTypes {

	/** element in the XML for a checkbox item. The 5 task checkboxes in old versions of Borg are now an optional list of pre-populated subtasks that get created automatically when a task is created */
	public static final String CHECKBOX = "CB"; // in new versions of borg -

	/** element in the state model XML marking the final state. */
	public static final String FINAL_STATE = "__FINAL__";

	/** element in the state model XML markin the initial state. */
	public static final String INITIAL_STATE = "__INIT__";

	/** a value to return indicating the lack of a built-in subtask (legacy code). */
	public static final String NOCBVALUE = "---------------";

	/**
	 * Gets a child element of a node with a particular name.
	 * 
	 * @param n the node
	 * @param childname the child name
	 * 
	 * @return the child Element
	 */
	static private Element getChild(Node n, String childname) {
		Collection<Element> l = getChildElements(n);
		for (Element e : l) {
			if (e.getNodeName().equals(childname))
				return e;
		}
		return null;
	}

	/**
	 * Gets the child elements of a node.
	 * 
	 * @param n the node
	 * 
	 * @return the child elements
	 */
	static private Collection<Element> getChildElements(Node n) {
		Collection<Element> ret = new ArrayList<Element>();
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				ret.add((Element) node);
			}
		}
		return ret;
	}


	/**
	 * Read xml from a source into a document.
	 * 
	 * @param source the source
	 * 
	 * @return the document
	 * 
	 * @throws Exception the exception
	 */
	static public Document readXml(final InputSource source) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(source);
		return document;

	}

	/**
	 * convert a document to an XML string
	 * 
	 * @param document the document
	 * 
	 * @return the string
	 * 
	 * @throws Exception the exception
	 */
	static public String toXml(Document document) throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		StringWriter stringWriter = new StringWriter(1024);
		transformer.transform(new DOMSource(document), new StreamResult(
				stringWriter));
		String xmlString = stringWriter.getBuffer().toString();
		stringWriter.close();
		return (xmlString);
	}

	/** an XML document containing the rules for the task state transitions per task type and status. 
	 * It is kept in XML for legacy reasons. */
	private Document stateModel; 

	/** a list of the currently known task types. */
	private Vector<String> taskTypes;

	/**
	 * constructor.
	 */
	public TaskTypes() {
		stateModel = null;
		taskTypes = null;
	}

	/**
	 * add a next state transition to a state for a type.
	 * 
	 * @param type the type
	 * @param state the state
	 * @param nextstate the nextstate
	 */
	public void addNextState(String type, String state, String nextstate) {

		Node st = getStateNode(type, state);
		if (st != null) {
			Element next = stateModel.createElement(nextstate);
			st.appendChild(next);
		}

	}

	/**
	 * Adds a state to a type.
	 * 
	 * @param type the type
	 * @param state the state
	 */
	public void addState(String type, String state) {
		Node tp = getTypeNode(type);
		if (tp == null)
			return;

		Node st = getStateNode(type, state);
		if (st != null)
			return;

		Element e = stateModel.createElement(state);
		tp.appendChild(e);

	}

	/**
	 * Adds a new type to the state model with default OPEN and CLOSE states.
	 * 
	 * @param type the type
	 */
	public void addType(String type) {
		if (getTaskTypes().contains(type))
			return;

		Element typeNode = stateModel.createElement(type);
		Element root = stateModel.getDocumentElement();
		root.appendChild(typeNode);

		Element o = stateModel.createElement("OPEN");
		typeNode.appendChild(o);

		Element c1 = stateModel.createElement("CLOSED");
		typeNode.appendChild(c1);

		Element c2 = stateModel.createElement("CLOSED");
		o.appendChild(c2);

		taskTypes = null;
	}

	/**
	 * change a state name for a type.
	 * 
	 * @param type the type
	 * @param state the state
	 * @param newstate the newstate
	 */
	public void changeState(String type, String state, String newstate) {

		Node st = getStateNode(type, state);
		if (st != null) {
			stateModel.renameNode(st, null, newstate);
		}

		Node tp = getTypeNode(type);
		Collection<Element> states = getChildElements(tp);
		for (Element e : states) {
			Collection<Element> nexts = getChildElements(e);
			for (Element next : nexts) {
				stateModel.renameNode(next, null, newstate);
			}
		}

	}

	/**
	 * change a built-in subtask value for a given index int the subtask list,
	 * or add a new one if the index does not correspond to a checkbox.
	 * 
	 * @param type the type
	 * @param index the index
	 * @param value the subtask value
	 */
	public void changeSubtask(String type, int index, String value) {
		if (value == null)
			value = NOCBVALUE;
		Node tp = getTypeNode(type);
		if (tp == null) {
			Errmsg.notice(Resource
					.getResourceString("WARNING!_Could_not_find_task_type_")
					+ type + Resource.getResourceString("checkbox_2"));
			return;
		}

		NodeList nl = ((Element) tp).getElementsByTagName(CHECKBOX);
		Element cb = (Element) nl.item(index);
		if (cb != null)
		{
			cb.setTextContent(value);
		}
		else {
			cb = stateModel.createElement(CHECKBOX);
			Node tx = stateModel.createTextNode(value);
			cb.appendChild(tx);
			tp.appendChild(cb);
		}

	}

	/**
	 * Change a type name.
	 * 
	 * @param type the type name
	 * @param newtype the new type name
	 */
	public void changeType(String type, String newtype) {

		Node tp = getTypeNode(type);
		if (tp != null) {
			stateModel.renameNode(tp, null, newtype);
			taskTypes = null;
		}
	}

	/**
	 * Copy this object.
	 * 
	 * @return the copy
	 * 
	 * @throws Exception the exception
	 */
	public TaskTypes copy() throws Exception {
		String s = toXml(stateModel);
		TaskTypes o = new TaskTypes();
		o.fromString(s);
		return o;
	}

	/**
	 * Delete a next state transition from a state for a type.
	 * 
	 * @param type the type
	 * @param state the state
	 * @param nextstate the nextstate
	 */
	public void deleteNextState(String type, String state, String nextstate) {
		Node st = getStateNode(type, state);
		if (st != null) {
			Collection<Element> nexts = getChildElements(st);
			for (Element next : nexts) {
				if (next.getNodeName().equals(nextstate))
					st.removeChild(next);
			}
		}

	}

	/**
	 * Delete a state from a type.
	 * 
	 * @param type the type
	 * @param state the state
	 */
	public void deleteState(String type, String state) {
		Node tp = getTypeNode(type);
		Node st = getStateNode(type, state);
		if (st != null) {
			tp.removeChild(st);
		}

		Collection<Element> states = getChildElements(tp);
		for (Element e : states) {
			Collection<Element> nexts = getChildElements(e);
			for (Element next : nexts) {
				e.removeChild(next);
			}
		}

	}

	/**
	 * Delete a type.
	 * 
	 * @param type the type
	 */
	public void deleteType(String type) {
		Node tp = getTypeNode(type);
		if (tp != null) {
			stateModel.getDocumentElement().removeChild(tp);
			taskTypes = null;
		}
	}

	/**
	 * populate this object from an XML string - which is how the data is stored
	 * in the db.
	 * 
	 * @param xml the xml
	 * 
	 * @throws Exception the exception
	 */
	public void fromString(String xml) throws Exception {
		stateModel = readXml(new InputSource(new StringReader(xml)));
		taskTypes = null;
	}

	/**
	 * Gets the final state for a type.
	 * 
	 * @param type the type
	 * 
	 * @return the final state
	 */
	public String getFinalState(String type) {
		Node tp = getTypeNode(type);
		if (tp == null) {
			return "CLOSED";
		}

		Collection<Element> nl = getChildElements(tp);
		for (Element st : nl) {
			if (st == null)
				break;
			NodeList nl2 = st.getElementsByTagName(FINAL_STATE);
			if (nl2.getLength() > 0) {
				return nl2.item(0).getNodeName();
			}

		}

		return ("CLOSED");
	}

	/**
	 * Gets the initial state for a type.
	 * 
	 * @param type the type
	 * 
	 * @return the initial state
	 */
	public String getInitialState(String type) {
		// find the task type element under the XML root
		Node tp = getTypeNode(type);
		if (tp == null) {
			return "OPEN";
		}

		Collection<Element> nl = getChildElements(tp);
		for (Element st : nl) {
			if (st == null)
				break;
			NodeList nl2 = st.getElementsByTagName(INITIAL_STATE);
			if (nl2.getLength() > 0) {
				return nl2.item(0).getNodeName();
			}

		}

		return ("OPEN");
	}

	/**
	 * Gets a particular state node for a type.
	 * 
	 * @param type the type
	 * @param state the state
	 * 
	 * @return the state node
	 */
	private Node getStateNode(String type, String state) {
		Element tp = getChild(stateModel.getDocumentElement(), type);
		if (tp != null) {
			Element st = getChild(tp, state);
			return st;
		}
		return null;
	}

	/**
	 * Gets the states for a given task type.
	 * 
	 * @param type the task type
	 * 
	 * @return the states
	 */
	public Vector<String> getStates(String type) {

		Vector<String> v = new Vector<String>();

		Element root = stateModel.getDocumentElement();
		Element tp = getChild(root, type);
		if (tp != null) {
			Collection<Element> l = getChildElements(tp);
			for (Element e : l) {
				if (e.getNodeName().equals(CHECKBOX))
					continue;
				v.add(e.getNodeName());
			}
		} else {
			Errmsg.notice(Resource
					.getResourceString("WARNING!_Could_not_find_task_type_")
					+ type + Resource.getResourceString("state3"));
			return v;
		}

		return (v);
	}

	/**
	 * get the built-in subtasks for a type.
	 * 
	 * @param type the type
	 * 
	 * @return the subtasks
	 */
	public String[] getSubTasks(String type) {

		String ar[] = new String[5];

		// find the task type element under the XML root
		Node tp = getTypeNode(type);
		if (tp == null) {
			Errmsg.notice(Resource
					.getResourceString("WARNING!_Could_not_find_task_type_")
					+ type + Resource.getResourceString("checkbox_2"));
		}

		NodeList nl = ((Element) tp).getElementsByTagName(CHECKBOX);
		for (int i = 0; i < 5; i++) {

			Element cb = (Element) nl.item(i);
			if (cb != null) {
				ar[i] = cb.getTextContent();
			} else {
				ar[i] = NOCBVALUE;
			}
		}

		return ar;
	}

	/**
	 * Gets all task types.
	 * 
	 * @return the task types
	 */
	public Vector<String> getTaskTypes() {
		// if the list was never retrieved yet - then get it from the
		// children of the root of the XML task type/state model
		if (taskTypes == null) {
			taskTypes = new Vector<String>();

			Element root = stateModel.getDocumentElement();
			Collection<Element> l = getChildElements(root);
			for (Element e : l) {
				taskTypes.add(e.getNodeName());
			}

		}

		return (taskTypes);
	}

	/**
	 * Gets a type node.
	 * 
	 * @param type the type
	 * 
	 * @return the type node
	 */
	private Node getTypeNode(String type) {
		Element root = stateModel.getDocumentElement();
		Element tp = getChild(root, type);
		return tp;
	}

	/**
	 * Load the default state model XML from the borg JAR file.
	 * 
	 * @throws Exception the exception
	 */
	public void loadDefault() throws Exception {
		URL tsurl = getClass().getResource("/resource/task_states.xml");
		stateModel = readXml(new InputSource(tsurl.openStream()));
		taskTypes = null;
	}

	/**
	 * get a list of possible Next states for a given state and type.
	 * 
	 * @param state the state
	 * @param type the type
	 * 
	 * @return the vector< string>
	 */
	public Vector<String> nextStates(String state, String type) {

		Vector<String> v = new Vector<String>();

		// can always stay at the current state
		v.add(state);

		// find the task type element under the XML root
		Node tp = getTypeNode(type);
		if (tp == null) {
			Errmsg.notice(Resource
					.getResourceString("WARNING!_Could_not_find_task_type_")
					+ type + Resource.getResourceString("state3"));
			return v;
		}

		// add any ALL states - these are states that any other state can jump
		// to
		// find ALL child element of the task element
		Node all = getStateNode(type, "ALL");

		// add all children of ALL as next states
		if (all != null) {
			Collection<Element> nl = getChildElements(all);
			for (Element ch : nl) {
				if (ch == null)
					break;
				if (ch.getNodeName().equals(state))
					continue;
				v.add(ch.getNodeName());
			}
		}

		// add state specific arcs
		// find the child of the task type element with name = the current state
		Node st = getStateNode(type, state);

		// add names of all children of the current state node as possible next
		// states
		if (st != null) {
			Collection<Element> nl = getChildElements(st);
			for (Element ch : nl) {
				if (ch == null)
					break;
				if (ch.getNodeName().equals(state))
					continue;
				if (ch.getNodeName().equals(INITIAL_STATE))
					continue;
				v.add(ch.getNodeName());
			}
		}
		return (v);
	}

	/**
	 * Sets the initial state for a type.
	 * 
	 * @param type the type
	 * @param state the state
	 */
	public void setInitialState(String type, String state) {
		// find the task type element under the XML root
		Node tp = getTypeNode(type);
		if (tp == null) {
			return;
		}

		Collection<Element> nl = getChildElements(tp);
		for (Element st : nl) {
			if (st == null)
				break;
			if (st.getNodeName().equals(state)) {
				NodeList nl2 = st.getElementsByTagName(INITIAL_STATE);
				if (nl2.getLength() == 0) {
					Element in = stateModel.createElement(INITIAL_STATE);
					st.appendChild(in);
				}
			} else {
				NodeList nl2 = st.getElementsByTagName(INITIAL_STATE);
				if (nl2.getLength() != 0) {
					st.removeChild(nl2.item(0));
				}
			}
		}

	}

	/**
	 * return the task state model as XML
	 * 
	 * @return the XML string
	 * 
	 * @throws Exception the exception
	 */
	public String toXml() throws Exception
	{
		System.out.println(toXml(stateModel));
		return toXml(stateModel);
	}

	/**
	 * validate the state model.
	 * 
	 * @throws Exception the exception
	 */
	public void validate() throws Exception {
		Collection<String> types = getTaskTypes();
		Iterator<String> it = types.iterator();
		while (it.hasNext()) {
			String type = it.next();
			String init = getInitialState(type);
			Collection<String> states = getStates(type);
			if (init.equals("OPEN") && !states.contains("OPEN"))
				throw new Exception(Resource.getResourceString("NoOpenState")
						+ type);
		}
	}

}
