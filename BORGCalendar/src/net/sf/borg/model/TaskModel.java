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

import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.common.XTree;
import net.sf.borg.model.CategoryModel.CategorySource;
import net.sf.borg.model.db.EntityDB;
import net.sf.borg.model.db.TaskDB;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.db.jdbc.TaskJdbcDB;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.ProjectXMLAdapter;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.SubtaskXMLAdapter;
import net.sf.borg.model.entity.Task;
import net.sf.borg.model.entity.TaskXMLAdapter;
import net.sf.borg.model.entity.Tasklog;
import net.sf.borg.model.entity.TasklogXMLAdapter;
import net.sf.borg.model.undo.ProjectUndoItem;
import net.sf.borg.model.undo.SubtaskUndoItem;
import net.sf.borg.model.undo.TaskUndoItem;
import net.sf.borg.model.undo.UndoLog;

public class TaskModel extends Model implements Model.Listener, Transactional,
		CategorySource {

	private EntityDB<Task> db_; // the database

	public EntityDB<Task> getDB() {
		return (db_);
	}

	// map of tasks keyed by day - for performance
	private HashMap<Integer, Collection<Task>> btmap_;

	private Vector<Task> allmap_;

	private HashMap<Integer, Collection<Subtask>> stmap_;

	private HashMap<Integer, Collection<Project>> pmap_;

	private TaskTypes taskTypes_ = new TaskTypes();

	public Collection<Task> get_tasks(int daykey) {
		return (btmap_.get(new Integer(daykey)));
	}

	public Collection<Subtask> get_subtasks(int daykey) {
		return (stmap_.get(new Integer(daykey)));
	}

	public Collection<Project> get_projects(int daykey) {
		return (pmap_.get(new Integer(daykey)));
	}

	public Vector<Task> get_tasks() {
		return (allmap_);
	}

	static private TaskModel self_ = null;

	static public TaskModel getReference()  {
		if( self_ == null )
			try {
				self_ = new TaskModel();
				self_.load_map();
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return null;
			}
		return (self_);
	}

	public TaskTypes getTaskTypes() {
		return (taskTypes_);
	}

	public void saveTaskTypes(TaskTypes tt) throws Exception {
		if (tt != null) {
			tt.validate();
			taskTypes_ = tt.copy();
		}
		db_.setOption(new BorgOption("SMODEL", taskTypes_.toString()));
	}

	public Collection<String> getCategories() {

		TreeSet<String> categories = new TreeSet<String>();
		try {
			for (Task t : db_.readAll()) {
				String cat = t.getCategory();
				if (cat != null && !cat.equals(""))
					categories.add(cat);
			}
		} catch (Exception e1) {
			Errmsg.errmsg(e1);
		}

		try {
			for (Project t : getProjects()) {
				String cat = t.getCategory();
				if (cat != null && !cat.equals(""))
					categories.add(cat);
			}
		} catch (Exception e) {
			// ignore this one
		}
		return (categories);

	}

	public Collection<Task> getTasks() throws Exception {
		return db_.readAll();
	}

	// load a map of tasks to day keys for performance. this is because the
	// month views will
	// repeatedly need to retrieve tasks per day and it would be slow to
	// repeatedly traverse
	// the entire task DB looking for them
	// only tasks that are viewed by day need to be cached here - those that
	// are not CLOSED and
	// have a due date - a small fraction of the total
	private void load_map() {

		// clear map
		btmap_.clear();
		allmap_.clear();
		stmap_.clear();
		pmap_.clear();

		try {

			// iterate through tasks using taskmodel
			for (Task mr : getTasks()) {
				// for each task, get state and skip CLOSED or PR tasks
				if (isClosed(mr))
					continue;

				if (!CategoryModel.getReference().isShown(mr.getCategory()))
					continue;

				// use task due date to build a day key
				Date due = mr.getDueDate();
				if (due == null)
					continue;

				GregorianCalendar g = new GregorianCalendar();
				g.setTime(due);
				int key = AppointmentModel.dkey(g);

				// add the task string to the btmap_
				// add the task to the mrs_ Vector. This is used by the todo gui
				Collection<Task> o = btmap_.get(new Integer(key));
				if (o == null) {
					o = new LinkedList<Task>();
					btmap_.put(new Integer(key), o);
				}

				o.add(mr);
				allmap_.add(mr);
			}

			if (db_ instanceof TaskDB) {

				for (Project pj : getProjects()) {
					if (pj.getDueDate() == null)
						continue;

					if (pj.getStatus().equals(
							Resource.getPlainResourceString("CLOSED")))
						continue;

					if (!CategoryModel.getReference().isShown(pj.getCategory()))
						continue;

					// use task due date to build a day key
					Date due = pj.getDueDate();
					GregorianCalendar g = new GregorianCalendar();
					g.setTime(due);
					int key = AppointmentModel.dkey(g);

					// add the string to the btmap_
					Collection<Project> o = pmap_.get(new Integer(key));
					if (o == null) {
						o = new LinkedList<Project>();
						pmap_.put(new Integer(key), o);
					}

					o.add(pj);
				}

				for (Subtask st : getSubTasks()) {
					if (st.getCloseDate() != null || st.getDueDate() == null)
						continue;

					Task mr = getTask(st.getTask().intValue());
					String cat = mr.getCategory();
					if (cat == null || cat.equals(""))
						cat = CategoryModel.UNCATEGORIZED;

					if (!CategoryModel.getReference().isShown(cat))
						continue;

					// use task due date to build a day key
					Date due = st.getDueDate();
					GregorianCalendar g = new GregorianCalendar();
					g.setTime(due);
					int key = AppointmentModel.dkey(g);

					// add the string to the btmap_
					Collection<Subtask> o = stmap_.get(new Integer(key));
					if (o == null) {
						o = new LinkedList<Subtask>();
						stmap_.put(new Integer(key), o);
					}

					o.add(st);
				}
			}

		} catch (Exception e) {

			Errmsg.errmsg(e);
			return;
		}

	}

	
	private TaskModel() throws Exception {
		
		btmap_ = new HashMap<Integer, Collection<Task>>();
		stmap_ = new HashMap<Integer, Collection<Subtask>>();
		pmap_ = new HashMap<Integer, Collection<Project>>();
		allmap_ = new Vector<Task>();
		
		db_ = new TaskJdbcDB();

	
		String sm = db_.getOption("SMODEL");
		if (sm == null) {
			try {
				// load XML from a file in the JAR
				// System.out.println("Loading default task model");
				taskTypes_.loadDefault();
				sm = taskTypes_.toString();
				db_.setOption(new BorgOption("SMODEL", sm));
			} catch (NoSuchMethodError nsme) {
				// running in a Palm conduit under JRE 1.3
				// ignore
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}
		} else {
			taskTypes_.fromString(sm);
		}

		CategoryModel.getReference().addSource(this);
		CategoryModel.getReference().addListener(this);


	}

	public void delete(int tasknum) throws Exception {
		delete(tasknum, false);
	}

	// delete a given task from the DB
	public void delete(int tasknum, boolean undo) throws Exception {

		try {
			LinkModel.getReference().deleteLinks(tasknum, Task.class);
			if (!undo) {
				Task task = getTask(tasknum);
				UndoLog.getReference().addItem(TaskUndoItem.recordDelete(task));
				// subtasks are removed by cascading delete, so set undo records
				// here
				Collection<Subtask> coll = getSubTasks(task.getTaskNumber());
				if (coll != null) {
					for (Subtask st : coll) {
						SubtaskUndoItem.recordDelete(st);
					}
				}
			}
			db_.delete(tasknum);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// have the borg class reload the calendar app side of things - so this
		// is one model
		// notifying the other of a change through the controller
		load_map();

		refreshListeners();

	}

	public void deleteProject(int id) throws Exception {

		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("cannot_undo"), null,
				JOptionPane.OK_CANCEL_OPTION);

		if (ret != JOptionPane.OK_OPTION)
			return;

		try {
			if (db_ instanceof TaskDB == false)
				throw new Warning(Resource
						.getPlainResourceString("SubtaskNotSupported"));
			TaskDB sdb = (TaskDB) db_;
			beginTransaction();
			LinkModel.getReference().deleteLinks(id, Project.class);

			sdb.deleteProject(id);
			commitTransaction();
		} catch (Exception e) {
			rollbackTransaction();
			Errmsg.errmsg(e);
		}

		// have the borg class reload the calendar app side of things - so this
		// is one model
		// notifying the other of a change through the controller
		load_map();

		refreshListeners();

	}

	public void savetask(Task task) throws Exception {
		savetask(task, false);
	}

	// save a task from a filled in task Row
	public void savetask(Task task, boolean undo) throws Exception {

		// validations
		if (task.getProject() != null) {
			Project p = TaskModel.getReference().getProject(
					task.getProject().intValue());
			if (p != null && task.getDueDate() != null
					&& p.getDueDate() != null
					&& DateUtil.isAfter(task.getDueDate(), p.getDueDate())) {
				throw new Warning(Resource
						.getPlainResourceString("taskdd_warning"));
			}
		}

		// add task to DB
		Integer num = task.getTaskNumber();
		Task indb = null;
		if (num != null)
			indb = getTask(num);

		// if the task number is -1, it is a new task so
		// get a new task number.
		if (num == null || num.intValue() == -1 || indb == null) {
			if (!undo || num == null) {
				int newkey = db_.nextkey();
				task.setKey(newkey);
				task.setTaskNumber(new Integer(newkey));
			}
			db_.addObj(task);
			if (!undo) {
				Task t = getTask(task.getTaskNumber());
				UndoLog.getReference().addItem(TaskUndoItem.recordAdd(t));
			}

		} else {
			// task exists - so update existing task in DB

			// update close date
			if (task.getState() != null && isClosed(task))
				task.setCD(new Date());
			int key = task.getTaskNumber().intValue();
			task.setKey(key);
			if (!undo) {
				Task t = getTask(task.getTaskNumber());
				UndoLog.getReference().addItem(TaskUndoItem.recordUpdate(t));
			}
			db_.updateObj(task);

		}

		// have the borg class reload the calendar app side of things - so this
		// is one model
		// notifying the other of a change through the controller
		load_map();

		// inform views of data change
		refreshListeners();

	}

	// allocate a new Row from the DB
	public Task newMR() {
		return (db_.newObj());
	}

	// read a task from the DB by task number
	public Task getTask(int num) throws Exception {
		return (db_.readObj(num));
	}

	// force a task to be updated to CLOSED state and saved
	public void close(int num) throws Exception, Warning {

		for (Subtask st : TaskModel.getReference().getSubTasks(num)) {
			if (st.getCloseDate() == null) {
				throw new Warning(Resource.getResourceString("open_subtasks"));
			}
		}

		Task task = getTask(num);
		task.setState(TaskModel.getReference().getTaskTypes().getFinalState(
				task.getType()));
		savetask(task);
	}

	public void closeProject(int num) throws Exception, Warning {

		Project p = getProject(num);
		p.setStatus(Resource.getPlainResourceString("CLOSED"));
		saveProject(p);
	}

	// export the task data for all tasks to XML
	public void export(Writer fw) throws Exception {

		// FileWriter fw = new FileWriter(fname);
		fw.write("<TASKS>\n");
		TaskXMLAdapter ta = new TaskXMLAdapter();

		// export options
		for (BorgOption option : db_.getOptions()) {
			XTree xt = new XTree();
			xt.name("OPTION");
			xt.appendChild(option.getKey(), option.getValue());
			fw.write(xt.toString());
		}

		ProjectXMLAdapter pa = new ProjectXMLAdapter();

		// export projects
		if (TaskModel.getReference().hasSubTasks()) {

			for (Project p : getProjects()) {
				XTree xt = pa.toXml(p);
				fw.write(xt.toString());
			}

		}
		// export tasks
		for (Task task : getTasks()) {
			XTree xt = ta.toXml(task);
			fw.write(xt.toString());
		}

		SubtaskXMLAdapter sta = new SubtaskXMLAdapter();

		// export subtasks
		if (TaskModel.getReference().hasSubTasks()) {
			for (Subtask stask : getSubTasks()) {
				XTree xt = sta.toXml(stask);
				fw.write(xt.toString());
			}

			TasklogXMLAdapter tla = new TasklogXMLAdapter();

			// export tasklogs
			for (Tasklog tlog : getLogs()) {
				XTree xt = tla.toXml(tlog);
				fw.write(xt.toString());
			}

		}
		fw.write("</TASKS>");

	}

	public Collection<Project> getProjects() throws Exception {
		if (db_ instanceof TaskDB == false)
			return new ArrayList<Project>();
		TaskDB sdb = (TaskDB) db_;
		return sdb.getProjects();
	}

	public Project getProject(int id) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		return sdb.getProject(id);
	}

	// export the task data to a file in XML
	public void importXml(XTree xt) throws Exception {

		TaskXMLAdapter aa = new TaskXMLAdapter();
		SubtaskXMLAdapter sa = new SubtaskXMLAdapter();
		TasklogXMLAdapter la = new TasklogXMLAdapter();
		ProjectXMLAdapter pa = new ProjectXMLAdapter();

		JdbcDB.execSQL("SET REFERENTIAL_INTEGRITY FALSE;");

		// for each appt - create an Appointment and store
		for (int i = 1;; i++) {
			XTree ch = xt.child(i);
			if (ch == null)
				break;

			if (ch.name().equals("OPTION")) {
				XTree opt = ch.child(1);
				if (opt == null)
					continue;

				if (opt.name().equals("SMODEL")) {
					taskTypes_.fromString(opt.value());
					db_.setOption(new BorgOption("SMODEL", taskTypes_
							.toString()));

				} else {
					db_.setOption(new BorgOption(opt.name(), opt.value()));
				}
			}

			else if (ch.name().equals("Task")) {
				Task task = aa.fromXml(ch);
				if (task.getPriority() == null)
					task.setPriority(new Integer(3));

				db_.addObj(task);

				if (TaskModel.getReference().hasSubTasks()) {
					// migrate from old subtask mechanism
					if (!isClosed(task) && task.getTodoList() != null
							&& task.getUserTask1() != null) {
						// add system subtasks
						String todos = task.getTodoList();
						String cbs[] = TaskModel.getReference().getTaskTypes()
								.checkBoxes(task.getType());
						for (int sti = 0; sti < cbs.length; sti++) {
							if (!cbs[sti].equals(TaskTypes.NOCBVALUE)) {
								Subtask st = new Subtask();
								st.setStartDate(new Date());
								st.setDescription(cbs[sti]);
								st.setTask(task.getTaskNumber());
								if (todos.indexOf(Integer.toString(sti)) != -1) {
									st.setCloseDate(new Date());
								}
								saveSubTask(st);
							}
						}
						if (task.getUserTask1() != null) {
							Subtask st = new Subtask();
							st.setStartDate(new Date());
							st.setDescription(task.getUserTask1());
							st.setTask(task.getTaskNumber());
							if (todos.indexOf("6") != -1) {
								st.setCloseDate(new Date());
							}
							saveSubTask(st);
						}
						if (task.getUserTask2() != null) {
							Subtask st = new Subtask();
							st.setStartDate(new Date());
							st.setDescription(task.getUserTask2());
							st.setTask(task.getTaskNumber());
							if (todos.indexOf("7") != -1) {
								st.setCloseDate(new Date());
							}
							saveSubTask(st);
						}
						if (task.getUserTask3() != null) {
							Subtask st = new Subtask();
							st.setStartDate(new Date());
							st.setDescription(task.getUserTask3());
							st.setTask(task.getTaskNumber());
							if (todos.indexOf("8") != -1) {
								st.setCloseDate(new Date());
							}
							saveSubTask(st);
						}
						if (task.getUserTask4() != null) {
							Subtask st = new Subtask();
							st.setStartDate(new Date());
							st.setDescription(task.getUserTask4());
							st.setTask(task.getTaskNumber());
							if (todos.indexOf("9") != -1) {
								st.setCloseDate(new Date());
							}
							saveSubTask(st);
						}
						if (task.getUserTask5() != null) {
							Subtask st = new Subtask();
							st.setStartDate(new Date());
							st.setDescription(task.getUserTask5());
							st.setTask(task.getTaskNumber());
							if (todos.indexOf("A") != -1) {
								st.setCloseDate(new Date());
							}
							saveSubTask(st);
						}
						task.setTodoList(null);
						task.setUserTask1(null);
						task.setUserTask2(null);
						task.setUserTask3(null);
						task.setUserTask4(null);
						task.setUserTask5(null);
						savetask(task);
					}
				}

			}

			else if (ch.name().equals("Subtask")) {
				Subtask subtask = sa.fromXml(ch);
				try {
					subtask.setId(null);
					saveSubTask(subtask);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}

			else if (ch.name().equals("Tasklog")) {
				Tasklog tlog = la.fromXml(ch);
				try {
					tlog.setId(null);
					saveLog(tlog);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			} else if (ch.name().equals("Project")) {
				Project p = pa.fromXml(ch);
				try {
					TaskDB sdb = (TaskDB) db_;
					sdb.addProject(p);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		}
		JdbcDB.execSQL("SET REFERENTIAL_INTEGRITY TRUE;");
		// refresh all views that are displaying appt data from this model
		load_map();
		refreshListeners();

	}

	public void sync() {
		db_.sync();
		load_map();
		refreshListeners();
	}

	/*
	 * TODO UCdetector: Remove unused code: public void close_db() throws
	 * Exception { db_.close(); }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model.Listener#refresh()
	 */
	public void refresh() {
		try {
			load_map();
			refreshListeners();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	public Collection<Subtask> getSubTasks(int taskid) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getSubTasks(taskid);

	}

	public Collection<Subtask> getSubTasks() throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getSubTasks();

	}

	public Subtask getSubTask(int id) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getSubTask(id);

	}

	public Collection<Task> getTasks(int projectid) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getTasks(projectid);

	}

	public Collection<Project> getSubProjects(int projectid) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getSubProjects(projectid);

	}

	public Collection<Project> getAllSubProjects(int projectid)
			throws Exception {
		Collection<Project> c = new ArrayList<Project>();
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		addSubProjectsToCollection(c, projectid);
		return c;
	}

	private void addSubProjectsToCollection(Collection<Project> c, int projectid)
			throws Exception {

		// add my children
		Collection<Project> children = getSubProjects(projectid);
		if (children.isEmpty())
			return;
		c.addAll(children);

		// add my children's children
		Iterator<Project> it = children.iterator();
		while (it.hasNext()) {
			Project p = it.next();
			addSubProjectsToCollection(c, p.getId().intValue());
		}

	}

	public void deleteSubTask(int id) throws Exception {
		deleteSubTask(id, false);
	}

	public void deleteSubTask(int id, boolean undo) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		if (!undo) {
			Subtask st = sdb.getSubTask(id);
			SubtaskUndoItem.recordDelete(st);
		}
		sdb.deleteSubTask(id);
		load_map();
		refreshListeners();
	}

	public void saveSubTask(Subtask s) throws Exception {
		saveSubTask(s, false);
	}

	public void saveSubTask(Subtask s, boolean undo) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		if (s.getId() == null || s.getId().intValue() <= 0
				|| null == sdb.getSubTask(s.getId())) {
			if (!undo || s.getId() == null)
				s.setId(new Integer(sdb.nextSubTaskKey()));
			sdb.addSubTask(s);
			if (!undo) {
				Subtask st = sdb.getSubTask(s.getId());
				SubtaskUndoItem.recordAdd(st);
			}
		} else {
			if (!undo) {
				Subtask st = sdb.getSubTask(s.getId());
				SubtaskUndoItem.recordUpdate(st);
			}
			sdb.updateSubTask(s);
		}

		load_map();
		refreshListeners();
	}

	public void addLog(int taskid, String desc) throws Exception {
		if (db_ instanceof TaskDB == false)
			return;
		// throw new Exception(Resource
		// .getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		sdb.addLog(taskid, desc);
	}

	private void saveLog(Tasklog tlog) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		sdb.saveLog(tlog);
	}

	public Collection<Tasklog> getLogs(int taskid) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		return sdb.getLogs(taskid);
	}

	public Collection<Tasklog> getLogs() throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		return sdb.getLogs();
	}

	public static int daysLeft(Date dd) {

		if (dd == null)
			return 0;
		Calendar today = new GregorianCalendar();
		Calendar dcal = new GregorianCalendar();
		dcal.setTime(dd);

		// find days left
		int days = 0;
		if (dcal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
			days = dcal.get(Calendar.DAY_OF_YEAR)
					- today.get(Calendar.DAY_OF_YEAR);
		} else {
			days = new Long((dd.getTime() - today.getTime().getTime())
					/ (1000 * 60 * 60 * 24)).intValue();
		}

		// if due date is past, set days left to 0
		// negative days are silly
		if (days < 0)
			days = 0;
		return days;
	}

	public static int daysBetween(Date start, Date dd) {

		if (dd == null)
			return 0;
		Calendar startcal = new GregorianCalendar();
		Calendar dcal = new GregorianCalendar();
		dcal.setTime(dd);
		startcal.setTime(start);

		// find days left
		int days = 0;
		if (dcal.get(Calendar.YEAR) == startcal.get(Calendar.YEAR)) {
			days = dcal.get(Calendar.DAY_OF_YEAR)
					- startcal.get(Calendar.DAY_OF_YEAR);
		} else {
			days = new Long((dd.getTime() - startcal.getTime().getTime())
					/ (1000 * 60 * 60 * 24)).intValue();
		}

		// if due date is past, set days left to 0
		// negative days are silly
		if (days < 0)
			days = 0;
		return days;
	}

	public boolean hasSubTasks() {
		return db_ instanceof TaskDB;
	}

	public void beginTransaction() throws Exception {
		if (db_ instanceof Transactional) {
			Transactional t = (Transactional) db_;
			t.beginTransaction();
		}

	}

	public void commitTransaction() throws Exception {
		if (db_ instanceof Transactional) {
			Transactional t = (Transactional) db_;
			t.commitTransaction();
		}
	}

	public void rollbackTransaction() throws Exception {
		if (db_ instanceof Transactional) {
			Transactional t = (Transactional) db_;
			t.rollbackTransaction();
		}
	}

	public void saveProject(Project p) throws Exception {
		saveProject(p, false);
	}

	public void saveProject(Project p, boolean undo) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		// validation that task due dates are before project due date
		if (p.getId() != null && p.getId().intValue() != -1) {
			for (Task t : TaskModel.getReference().getTasks(
					p.getId().intValue())) {
				if (p.getDueDate() != null && t.getDueDate() != null
						&& !TaskModel.isClosed(t)
						&& DateUtil.isAfter(t.getDueDate(), p.getDueDate())) {
					throw new Warning(Resource
							.getPlainResourceString("projdd_warning")
							+ ": " + t.getTaskNumber());
				}
			}

			for (Project child : TaskModel.getReference().getSubProjects(
					p.getId().intValue())) {
				if (p.getDueDate() != null && child.getDueDate() != null
						&& !TaskModel.isClosed(child)
						&& DateUtil.isAfter(child.getDueDate(), p.getDueDate())) {
					throw new Warning(Resource
							.getPlainResourceString("projchild_warning")
							+ ": " + child.getId().intValue());
				}
			}
		}

		// validate against parent
		if (p.getParent() != null) {
			Project par = TaskModel.getReference().getProject(
					p.getParent().intValue());
			if (par != null) {
				if (p.getDueDate() != null && par.getDueDate() != null
						&& DateUtil.isAfter(p.getDueDate(), par.getDueDate())) {
					throw new Warning(Resource
							.getPlainResourceString("projpar_warning"));
				}
			}
		}

		if (p.getId() != null
				&& p.getStatus().equals(
						Resource.getPlainResourceString("CLOSED"))) {
			// make sure that all tasks are closed
			for (Task pt : TaskModel.getReference().getTasks(
					p.getId().intValue())) {
				if (!isClosed(pt)) {
					throw new Warning(Resource
							.getPlainResourceString("close_proj_warn"));
				}
			}
		}

		TaskDB sdb = (TaskDB) db_;
		if (p.getId() == null || p.getId().intValue() <= 0) {
			if (!undo || p.getId() == null)
				p.setId(new Integer(sdb.nextProjectKey()));
			sdb.addProject(p);
			if (!undo) {
				Project t = getProject(p.getId());
				UndoLog.getReference().addItem(ProjectUndoItem.recordAdd(t));
			}
		} else {
			if (!undo) {
				Project t = getProject(p.getId());
				UndoLog.getReference().addItem(ProjectUndoItem.recordUpdate(t));
			}
			sdb.updateProject(p);
		}

		load_map();
		refreshListeners();
	}

	static public boolean isClosed(Task t) {
		String stat = t.getState();
		String type = t.getType();
		return stat.equals(TaskModel.getReference().getTaskTypes()
				.getFinalState(type));
	}

	static public boolean isClosed(Project p) {
		String stat = p.getStatus();
		return stat.equals(Resource.getPlainResourceString("CLOSED"));
	}
}
