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

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.XTree;

/**
 * The Class TaskTypes manages the Task type and state information, including
 * the state transitions allowed for each task type.
 */
public class TaskTypes {

	/** a list of the currently known task types */
	private Vector<String> task_types_;

	/**
	 * an XML tree containing the rules for the task state transitions per task
	 * type and status
	 */
	private XTree state_model_; //

	/**
	 * a value to return indicating the lack of a built-in subtask (legacy code)
	 */
	public static final String NOCBVALUE = "---------------";

	/** element in the state model XML markin the initial state */
	public static final String INITIAL_STATE = "__INIT__";

	/** element in the state model XML marking the final state */
	public static final String FINAL_STATE = "__FINAL__";

	/**
	 * element in the XML for a checkbox item. The 5 task checkboxes in old
	 * versions of Borg are now an optional list of pre-populated subtasks that
	 * get created automatically when a task is created
	 */
	public static final String CHECKBOX = "CB"; // in new versions of borg -
												// auto-generated subtasks

	/**
	 * constructor
	 */
	public TaskTypes() {
		state_model_ = null;
		task_types_ = null;
	}

	/**
	 * Copy this object
	 * 
	 * @return the copy
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public TaskTypes copy() throws Exception {
		String s = toString();
		TaskTypes o = new TaskTypes();
		o.fromString(s);
		return o;
	}

	/**
	 * populate this object from an XML string - which is how the data is stored
	 * in the db
	 * 
	 * @param xml
	 *            the xml
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void fromString(String xml) throws Exception {
		XTree newtree = XTree.readFromBuffer(xml);

		if (newtree != null) {
			// if XML is valid - replace the current model
			state_model_ = newtree;
			task_types_ = null;
		}

	}

	/**
	 * Load the default state model XML from the borg JAR file
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void loadDefault() throws Exception {
		URL tsurl = getClass().getResource("/resource/task_states.xml");
		state_model_ = XTree.readFromURL(tsurl);
		task_types_ = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (state_model_.toString());
	}

	/**
	 * Gets the states for a given task type
	 * 
	 * @param type
	 *            the task type
	 * 
	 * @return the states
	 */
	public Vector<String> getStates(String type) {

		Vector<String> v = new Vector<String>();

		// find the task type element under the XML root
		XTree tp = state_model_.child(type);
		if (!tp.exists()) {
			Errmsg.notice(Resource
					.getResourceString("WARNING!_Could_not_find_task_type_")
					+ type + Resource.getResourceString("state3"));
			return v;
		}

		// add names of all children of the current type node
		for (int i = 1;; i++) {
			XTree ch = tp.child(i);
			if (ch == null)
				break;
			if (ch.name().equals(CHECKBOX))
				continue;
			v.add(ch.name());
		}

		return (v);
	}

	/**
	 * Gets all task types.
	 * 
	 * @return the task types
	 */
	public Vector<String> getTaskTypes() {
		// if the list was never retrieved yet - then get it from the
		// children of the root of the XML task type/state model
		if (task_types_ == null) {
			task_types_ = new Vector<String>();
			for (int n = 1;; n++) {
				XTree ty = state_model_.child(n);
				if (ty == null)
					break;
				task_types_.add(ty.name());
			}

		}

		return (task_types_);
	}

	/**
	 * Adds a new type to the state model with default OPEN and CLOSE states.
	 * 
	 * @param type
	 *            the type
	 */
	public void addType(String type) {
		if (getTaskTypes().contains(type))
			return;

		XTree n = state_model_.appendChild(type);
		XTree o = n.appendChild("OPEN");
		o.appendChild("CLOSED");
		n.appendChild("CLOSED");
		task_types_ = null;
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
		XTree tp = state_model_.child(type);
		if (tp.exists())
			tp.name(newtype);
		task_types_ = null;
	}

	/**
	 * Delete a type.
	 * 
	 * @param type
	 *            the type
	 */
	public void deleteType(String type) {
		XTree tp = state_model_.child(type);
		if (tp.exists())
			tp.remove();
		task_types_ = null;
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
		XTree tp = state_model_.child(type);
		if (!tp.exists())
			return;
		XTree ch = tp.child(state);
		if (ch.exists())
			return;
		tp.appendChild(state);
	}

	/**
	 * change a state name for a type
	 * 
	 * @param type
	 *            the type
	 * @param state
	 *            the state
	 * @param newstate
	 *            the newstate
	 */
	public void changeState(String type, String state, String newstate) {
		XTree tp = state_model_.child(type);
		if (!tp.exists())
			return;
		XTree ch = tp.child(state);
		if (!ch.exists())
			return;
		ch.name(newstate);

		for (int n = 1;; n++) {
			ch = tp.child(n);
			if (ch == null)
				break;
			XTree ns = ch.child(state);
			if (ns.exists())
				ns.name(newstate);
		}
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
		XTree tp = state_model_.child(type);
		if (!tp.exists())
			return;
		XTree ch = tp.child(state);
		if (!ch.exists())
			return;
		ch.remove();

		for (int n = 1;; n++) {
			ch = tp.child(n);
			if (ch == null)
				break;
			XTree ns = ch.child(state);
			if (ns.exists())
				ns.remove();
		}
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
		XTree tp = state_model_.child(type);
		if (!tp.exists())
			return;
		XTree ch = tp.child(state);
		if (!ch.exists())
			return;
		XTree ns = ch.child(nextstate);
		if (ns.exists())
			return;
		ch.appendChild(nextstate);
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
		XTree tp = state_model_.child(type);
		if (!tp.exists())
			return;
		XTree ch = tp.child(state);
		if (!ch.exists())
			return;
		XTree ns = ch.child(nextstate);
		if (ns.exists())
			ns.remove();
	}

	/**
	 * change a built-in subtask value for a given index int the subtask list, or add a new one if the
	 * index does not correspond to a checkbox
	 * 
	 * @param type
	 *            the type
	 * @param index
	 *            the index
	 * @param value
	 *            the subtask value
	 */
	public void changeCB(String type, int index, String value) {
		if (value == null)
			value = NOCBVALUE;
		XTree tp = state_model_.child(type);
		if (!tp.exists()) {
			Errmsg.notice(Resource
					.getResourceString("WARNING!_Could_not_find_task_type_")
					+ type + Resource.getResourceString("checkbox_2"));
		}

		XTree cb = tp.child(CHECKBOX, index + 1);
		if (cb.exists()) {
			cb.value(value);
			return;
		}

		tp.appendChild(CHECKBOX, value);

	}

	
	/**
	 * get the built-in subtasks for a type.
	 * 
	 * @param type
	 *            the type
	 * 
	 * @return the subtasks
	 */
	public String[] checkBoxes(String type) {

		String ar[] = new String[5];

		// find the task type element under the XML root
		XTree tp = state_model_.child(type);
		if (!tp.exists()) {
			Errmsg.notice(Resource
					.getResourceString("WARNING!_Could_not_find_task_type_")
					+ type + Resource.getResourceString("checkbox_2"));
		}

		for (int i = 0; i < 5; i++) {

			if (tp.child(CHECKBOX, i + 1).exists()) {
				ar[i] = tp.child(CHECKBOX, i + 1).value();
			} else {
				ar[i] = NOCBVALUE;
			}
		}

		return ar;
	}

	
	/**
	 * get a list of possible Next states for a given state and type.
	 * 
	 * @param state
	 *            the state
	 * @param type
	 *            the type
	 * 
	 * @return the vector< string>
	 */
	public Vector<String> nextStates(String state, String type) {

		Vector<String> v = new Vector<String>();

		// can always stay at the current state
		v.add(state);

		// find the task type element under the XML root
		XTree tp = state_model_.child(type);
		if (!tp.exists()) {
			Errmsg.notice(Resource
					.getResourceString("WARNING!_Could_not_find_task_type_")
					+ type + Resource.getResourceString("state3"));
			return v;
		}

		// add any ALL states - these are states that any other state can jump
		// to
		// find ALL child element of the task element
		XTree all = tp.child("ALL");

		// add all children of ALL as next states
		for (int i = 1;; i++) {
			XTree ch = all.child(i);
			if (ch == null)
				break;
			if (ch.name().equals(state))
				continue;
			v.add(ch.name());
		}

		// add state specific arcs
		// find the child of the task type element with name = the current state
		XTree st = tp.child(state);

		// add names of all children of the current state node as possible next
		// states
		for (int i = 1;; i++) {
			XTree ch = st.child(i);
			if (ch == null)
				break;
			if (ch.name().equals(state))
				continue;
			if (ch.name().equals(INITIAL_STATE))
				continue;
			v.add(ch.name());
		}

		return (v);
	}

	/**
	 * validate the state model
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
				throw new Exception(Resource
						.getPlainResourceString("NoOpenState")
						+ type);
		}
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
		// find the task type element under the XML root
		XTree tp = state_model_.child(type);
		if (!tp.exists()) {
			return;
		}

		for (int i = 1;; i++) {
			XTree st = tp.child(i);
			if (st == null)
				break;
			if (st.name().equals(state)) {
				if (!st.child(INITIAL_STATE).exists())
					st.appendChild(INITIAL_STATE);
			} else if (st.child(INITIAL_STATE).exists()) {
				st.child(INITIAL_STATE).remove();
			}
		}

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
		// find the task type element under the XML root
		XTree tp = state_model_.child(type);
		if (!tp.exists()) {
			return "OPEN";
		}

		for (int i = 1;; i++) {
			XTree st = tp.child(i);
			if (st == null)
				break;

			if (st.child(INITIAL_STATE).exists())
				return st.name();

		}

		return ("OPEN");
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
		// find the task type element under the XML root
		XTree tp = state_model_.child(type);
		if (!tp.exists()) {
			return "CLOSED";
		}

		for (int i = 1;; i++) {
			XTree st = tp.child(i);
			if (st == null)
				break;

			if (st.child(FINAL_STATE).exists())
				return st.name();

		}

		return ("CLOSED");
	}

}
