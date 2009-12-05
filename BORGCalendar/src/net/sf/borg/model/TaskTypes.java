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

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
@XmlRootElement(name = "TaskTypes")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTypes {

	/**
	 * information about a task state
	 */
	static private class TaskState {
		@XmlElement(name="Name")
		public String name;
		@XmlElement(name="NextState")
		public HashSet<String> nextStates = new HashSet<String>(); // names of the possible next states
	}

	/**
	 * information about a task type
	 */
	static private class TaskType {
		// default subtasks for this task type
		@XmlElement(name="DefaultSubtask")
		public HashSet<String> defaultSubtasks = new HashSet<String>(); 
		// final state (defualt is CLOSED)
		@XmlElement(name="FinalState")
		public String finalState;
		// initial state (default is OPEN)
		@XmlElement(name="InitialState")
		public String initialState;
		// type name
		@XmlElement(name="Name")
		public String name;
		// the possible states for this type
		@XmlElement(name="State")
		public HashSet<TaskState> states = new HashSet<TaskState>();
	}

	/**
	 * the set of possible task types
	 */
	@XmlElement(name="TaskType")
	private HashSet<TaskType> taskTypes = new HashSet<TaskType>();

	/**
	 * constructor.
	 */
	public TaskTypes() {
	  // empty
	}

	/**
	 * add a next state transition to a state for a type.
	 * 
	 * @param type
	 *            the type
	 * @param state
	 *            the state
	 * @param nextstate
	 *            the nextstate
	 */
	public void addNextState(String type, String state, String nextstate) {

		TaskState st = getState(type, state);
		if (st != null) {
			st.nextStates.add(nextstate);
		}

	}

	/**
	 * Adds a state to a type.
	 * 
	 * @param type
	 *            the type
	 * @param state
	 *            the state
	 */
	public void addState(String type, String state) {
		TaskType tt = getType(type);
		if (tt != null) {
			TaskState ts = new TaskState();
			ts.name = state;
			tt.states.add(ts);
		}
	}

	/**
	 * add a subtask to a type
	 * @param type the type
	 * @param value the subtask text
	 */
	public void addSubtask(String type, String value) {
		TaskType tt = getType(type);
		if (tt != null)
			tt.defaultSubtasks.add(value);

	}

	/**
	 * Adds a new type to the state model with default OPEN and CLOSE states.
	 * 
	 * @param type
	 *            the type
	 */
	public void addType(String type) {

		TaskType tt = new TaskType();
		tt.name = type;
		addState(type, "OPEN");
		addState(type, "CLOSED");
		addNextState(type, "OPEN", "CLOSED");

		taskTypes.add(tt);
		if (getTaskTypes().contains(type))
			return;

	}

	/**
	 * change a state name for a type.
	 * 
	 * @param type
	 *            the type
	 * @param state
	 *            the state
	 * @param newstate
	 *            the newstate
	 */
	public void changeState(String type, String state, String newstate) {
		TaskState ts = getState(type, state);
		if (ts != null)
			ts.name = newstate;
	}

	/**
	 * Change a type name.
	 * 
	 * @param type
	 *            the type name
	 * @param newtype
	 *            the new type name
	 */
	public void changeType(String type, String newtype) {

		TaskType tt = getType(type);
		if (tt != null)
			tt.name = newtype;
	}

	/**
	 * Deep Copy this object.
	 * 
	 * @return the copy
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public TaskTypes copy() throws Exception {
		String s = toXml();
		JAXBContext jc = JAXBContext.newInstance(TaskTypes.class);
		Unmarshaller u = jc.createUnmarshaller();
		TaskTypes tt = (TaskTypes) u.unmarshal(new StringReader(s));
		return tt;
	}

	/**
	 * Delete a next state transition from a state for a type.
	 * 
	 * @param type
	 *            the type
	 * @param state
	 *            the state
	 * @param nextstate
	 *            the nextstate
	 */
	public void deleteNextState(String type, String state, String nextstate) {
		TaskState st = getState(type, state);
		if (st != null)
			st.nextStates.remove(nextstate);
	}

	/**
	 * Delete a state from a type.
	 * 
	 * @param type
	 *            the type
	 * @param state
	 *            the state
	 */
	public void deleteState(String type, String state) {
		TaskType tt = getType(type);
		TaskState ts = getState(type, state);
		if (tt != null && ts != null)
			tt.states.remove(ts);
	}

	/**
	 * delete a subtask from a type
	 * @param type the type
	 * @param value the subtask text
	 */
	public void deleteSubtask(String type, String value) {
		TaskType tt = getType(type);
		if (tt != null)
			tt.defaultSubtasks.remove(value);

	}

	/**
	 * Delete a type.
	 * 
	 * @param type
	 *            the type
	 */
	public void deleteType(String type) {
		TaskType tt = getType(type);
		if (tt != null)
			taskTypes.remove(tt);
	}

	/**
	 * This method will read a string containin the pre-1.7.2 XML that describes a task state model
	 * and convert it to the 1.7.2 format. As of 1.7.2, this old format would be only found in the SMODEL 
	 * rows of the options table. This method will never be called for a database created by version 1.7.2 or later.
	 * @param xml the pre-1.7.2 format XML for task types
	 * @throws Exception
	 */
	public void fillFromLegacyXml(String xml) throws Exception {

		/*
		 * 
		 * there is no good reason to comment this
		 * 
		 */
		taskTypes.clear();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document stateModel = builder.parse(new InputSource(new StringReader(xml)));
		Collection<Element> types = getChildElements(stateModel
				.getDocumentElement());
		for (Element type : types) {
			TaskType taskType = new TaskType();
			taskType.name = type.getNodeName();

			Collection<Element> typeChildren = getChildElements(type);
			for (Element typeChild : typeChildren) {
				if (typeChild.getNodeName().equals("CB")) {
					// subtask
					if (!typeChild.getTextContent().equals("---------------")) {
						taskType.defaultSubtasks
								.add(typeChild.getTextContent());
					}
				} else {
					TaskState state = new TaskState();
					state.name = typeChild.getNodeName();
					Collection<Element> stateChildren = getChildElements(typeChild);
					for (Element stateChild : stateChildren) {
						if (stateChild.getNodeName().equals("__INIT__")) {
							taskType.initialState = state.name;
						} else if (stateChild.getNodeName().equals("__FINAL__")) {
							taskType.finalState = state.name;
						} else {
							state.nextStates.add(stateChild.getNodeName());
						}
					}

					taskType.states.add(state);
				}

				taskTypes.add(taskType);
			}
		}
	}

	/**
	 * load this TaskTypes object from an XML string. Discard any data that was already present
	 * @param xmlString the XML string
	 * @throws Exception
	 */
	public void fromString(String xmlString) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(TaskTypes.class);
		Unmarshaller u = jc.createUnmarshaller();
		TaskTypes tt = (TaskTypes) u.unmarshal(new StringReader(xmlString));
		this.taskTypes = tt.taskTypes;
	}

	/**
	 * load this TaskTypes object from an XML input stream. Discard any data that was already present
	 * @param is the InputStream
	 * @throws Exception
	 */
	public void fromXml(InputStream is) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(TaskTypes.class);
		Unmarshaller u = jc.createUnmarshaller();
		TaskTypes tt = (TaskTypes) u.unmarshal(is);
		this.taskTypes = tt.taskTypes;
	}

	/**
	 * DOM code used by the legacy XML parser. Gets the child elements of a node.
	 * 
	 * @param n
	 *            the node
	 * 
	 * @return the child elements
	 */
	private ArrayList<Element> getChildElements(Node n) {
		ArrayList<Element> ret = new ArrayList<Element>();
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
	 * Gets the final state for a type.
	 * 
	 * @param type
	 *            the type
	 * 
	 * @return the final state
	 */
	public String getFinalState(String type) {
		TaskType tt = getType(type);
		if (tt != null && tt.finalState != null)
			return tt.finalState;
		return "CLOSED";
	}

	/**
	 * Gets the initial state for a type.
	 * 
	 * @param type
	 *            the type
	 * 
	 * @return the initial state
	 */
	public String getInitialState(String type) {
		TaskType tt = getType(type);
		if (tt != null && tt.initialState != null)
			return tt.initialState;
		return "OPEN";
	}

	/**
	 * Gets a particular state node for a type.
	 * 
	 * @param type
	 *            the type
	 * @param state
	 *            the state
	 * 
	 * @return the state node
	 */
	private TaskState getState(String type, String state) {
		TaskType tt = getType(type);
		if (tt != null) {
			for (TaskState ts : tt.states) {
				if (ts.name.equals(state))
					return ts;
			}
		}
		return null;
	}

	/**
	 * Gets the states for a given task type.
	 * 
	 * @param type
	 *            the task type
	 * 
	 * @return the states
	 */
	public Collection<String> getStates(String type) {

		Vector<String> v = new Vector<String>();
		TaskType tt = getType(type);
		if (tt != null) {
			for (TaskState ts : tt.states) {
				v.add(ts.name);
			}
		}
		return v;
	}

	/**
	 * get the built-in subtasks for a type.
	 * 
	 * @param type
	 *            the type
	 * 
	 * @return the subtasks
	 */
	public String[] getSubTasks(String type) {

		TaskType tt = getType(type);
		if (tt != null)
			return tt.defaultSubtasks.toArray(new String[0]);
		return new String[0];
	}

	/**
	 * Gets all task types.
	 * 
	 * @return the task types
	 */
	public Vector<String> getTaskTypes() {
		Vector<String> v = new Vector<String>();

		for (TaskType tt : taskTypes) {
			v.add(tt.name);
		}

		return v;
	}

	/**
	 * Gets a type node.
	 * 
	 * @param type
	 *            the type
	 * 
	 * @return the type node
	 */
	private TaskType getType(String type) {
		for (TaskType tt : taskTypes) {
			if (tt.name.equals(type))
				return tt;
		}
		return null;
	}

	/**
	 * Load the default state model XML from the borg JAR file.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void loadDefault() throws Exception {
		URL tsurl = getClass().getResource("/resource/task_states.xml");
		fromXml(tsurl.openStream());
	}

	/**
	 * get a list of possible Next states for a given state and type.
	 * 
	 * @param type
	 *            the type
	 * @param state
	 *            the state
	 * 
	 * @return the vector< string>
	 */
	public Collection<String> nextStates(String type, String state) {

		Collection<String> ret = new HashSet<String>();
		ret.add(state);
		TaskState ts = getState(type, state);
		if (ts != null)
			ret.addAll(ts.nextStates);
		return ret;
	}


	/**
	 * Sets the initial state for a type.
	 * 
	 * @param type
	 *            the type
	 * @param state
	 *            the state
	 */
	public void setInitialState(String type, String state) {

		TaskType tt = getType(type);
		if (tt != null)
			tt.initialState = state;

	}

	/**
	 * return the task state model as XML
	 * 
	 * @return the XML string
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public String toXml() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(TaskTypes.class);
		Marshaller m = jc.createMarshaller();
		StringWriter sw = new StringWriter();
		m.marshal(this, sw);
		//System.out.println(sw.toString());
		return sw.toString();
	}

	/**
	 * validate the state model (somewhat).
	 * 
	 * @throws Exception
	 *             the exception
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
